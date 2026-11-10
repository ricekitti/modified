using System;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

internal sealed class InjectorForm : Form
{
    private readonly ComboBox targets = new ComboBox();
    private readonly TextBox dllPath = new TextBox();
    private readonly Button inject = new Button();
    private readonly Label status = new Label();

    [STAThread]
    private static void Main()
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new InjectorForm());
    }

    public InjectorForm()
    {
        Text = "Rice / Slinky Injector";
        ClientSize = new System.Drawing.Size(600, 245);
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        StartPosition = FormStartPosition.CenterScreen;
        Controls.Add(new Label { Text = "Select your Minecraft Java process (64-bit):", Left = 16, Top = 16, Width = 550 });
        targets.SetBounds(16, 42, 458, 28);
        targets.DropDownStyle = ComboBoxStyle.DropDownList;
        Controls.Add(targets);
        var refresh = new Button { Text = "Refresh", Left = 484, Top = 40, Width = 100 };
        refresh.Click += delegate { RefreshTargets(); };
        Controls.Add(refresh);
        Controls.Add(new Label { Text = "DLL file:", Left = 16, Top = 82 });
        dllPath.SetBounds(16, 105, 458, 25);
        dllPath.Text = Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,
            @"..\slinkyreadablesource\slinkyreadablesource\build\slinky.dll"));
        Controls.Add(dllPath);
        var browse = new Button { Text = "Browse...", Left = 484, Top = 103, Width = 100 };
        browse.Click += delegate
        {
            using (var dialog = new OpenFileDialog { Filter = "DLL files (*.dll)|*.dll", CheckFileExists = true })
                if (dialog.ShowDialog(this) == DialogResult.OK) dllPath.Text = dialog.FileName;
        };
        Controls.Add(browse);
        inject.Text = "Inject DLL";
        inject.SetBounds(16, 150, 130, 32);
        inject.Click += InjectClicked;
        Controls.Add(inject);
        status.SetBounds(16, 193, 568, 45);
        Controls.Add(status);
        RefreshTargets();
    }

    private sealed class Target
    {
        public int Id;
        public long Started;
        public string Display;
        public override string ToString() { return Display; }
    }

    private void RefreshTargets()
    {
        targets.Items.Clear();
        foreach (var process in Process.GetProcesses())
        using (process)
        {
            try
            {
                if (process.ProcessName != "java" && process.ProcessName != "javaw") continue;
                targets.Items.Add(new Target { Id = process.Id, Started = process.StartTime.ToUniversalTime().Ticks,
                    Display = process.ProcessName + " — PID " + process.Id + " — " + process.MainWindowTitle });
            }
            catch (Win32Exception) { }
            catch (InvalidOperationException) { }
        }
        // Require an explicit choice even when only one Java process is running.
        status.Text = targets.Items.Count == 0 ? "No Java processes found. Start Minecraft, then click Refresh." : "Select the correct Minecraft process.";
    }

    private async void InjectClicked(object sender, EventArgs e)
    {
        var target = targets.SelectedItem as Target;
        if (target == null) { status.Text = "Select a Minecraft process first."; return; }
        inject.Enabled = false;
        targets.Enabled = false;
        string path = dllPath.Text;
        status.Text = "Loading DLL...";
        try
        {
            string result = await Task.Run(() => LoadDll(target, path));
            status.Text = result;
        }
        catch (Exception error) { status.Text = "Failed: " + error.Message; }
        finally { inject.Enabled = true; targets.Enabled = true; }
    }

    private static void Check(bool success, string action)
    {
        if (!success) throw new Win32Exception(Marshal.GetLastWin32Error(), action);
    }

    private static string LoadDll(Target target, string path)
    {
        path = Path.GetFullPath(path);
        if (!File.Exists(path)) throw new FileNotFoundException("DLL file not found.", path);
        // Validate the PE signature, AMD64 machine and DLL flag before loading.
        using (var reader = new BinaryReader(File.OpenRead(path)))
        {
            if (reader.ReadUInt16() != 0x5A4D) throw new InvalidDataException("Not a Windows DLL.");
            reader.BaseStream.Position = 0x3C;
            int header = reader.ReadInt32();
            if (header < 64 || header > reader.BaseStream.Length - 24) throw new InvalidDataException("Invalid PE header.");
            reader.BaseStream.Position = header;
            if (reader.ReadUInt32() != 0x4550 || reader.ReadUInt16() != 0x8664)
                throw new InvalidDataException("Select a 64-bit Windows DLL.");
            reader.BaseStream.Position = header + 22;
            if ((reader.ReadUInt16() & 0x2000) == 0) throw new InvalidDataException("Selected file is not a DLL.");
        }
        using (var process = Process.GetProcessById(target.Id))
        {
            if (process.StartTime.ToUniversalTime().Ticks != target.Started ||
                (process.ProcessName != "java" && process.ProcessName != "javaw"))
                throw new InvalidOperationException("Process changed. Refresh and select Minecraft again.");
            IntPtr handle = OpenProcess(0x043A, false, target.Id);
            Check(handle != IntPtr.Zero, "Cannot open Minecraft. Check that both programs run at the same permission level.");
            IntPtr memory = IntPtr.Zero;
            IntPtr thread = IntPtr.Zero;
            bool mayFree = true;
            try
            {
                bool wow64;
                Check(IsWow64Process(handle, out wow64), "Cannot determine process architecture.");
                if (wow64) throw new InvalidOperationException("This injector requires 64-bit Java.");
                foreach (ProcessModule module in process.Modules)
                    if (string.Equals(module.FileName, path, StringComparison.OrdinalIgnoreCase))
                        return "This DLL is already loaded in the selected process.";
                IntPtr loadLibrary = GetProcAddress(GetModuleHandle("kernel32.dll"), "LoadLibraryW");
                Check(loadLibrary != IntPtr.Zero, "Cannot resolve LoadLibraryW.");
                IntPtr owner;
                Check(GetModuleHandleEx(6, loadLibrary, out owner), "Cannot resolve the loader module.");
                var ownerPath = new StringBuilder(32768);
                Check(GetModuleFileName(owner, ownerPath, ownerPath.Capacity) != 0, "Cannot read loader module path.");
                IntPtr remoteLoadLibrary = IntPtr.Zero;
                foreach (ProcessModule module in process.Modules)
                    if (string.Equals(module.ModuleName, Path.GetFileName(ownerPath.ToString()), StringComparison.OrdinalIgnoreCase))
                        remoteLoadLibrary = new IntPtr(module.BaseAddress.ToInt64() + loadLibrary.ToInt64() - owner.ToInt64());
                if (remoteLoadLibrary == IntPtr.Zero) throw new InvalidOperationException("Loader module not found in target.");
                byte[] bytes = Encoding.Unicode.GetBytes(path + "\0");
                memory = VirtualAllocEx(handle, IntPtr.Zero, (UIntPtr)bytes.Length, 0x3000, 4);
                Check(memory != IntPtr.Zero, "Cannot allocate DLL path in target.");
                UIntPtr written;
                Check(WriteProcessMemory(handle, memory, bytes, (UIntPtr)bytes.Length, out written), "Cannot write DLL path.");
                if (written.ToUInt64() != (ulong)bytes.Length) throw new IOException("DLL path write was incomplete.");
                thread = CreateRemoteThread(handle, IntPtr.Zero, UIntPtr.Zero, remoteLoadLibrary, memory, 0, IntPtr.Zero);
                Check(thread != IntPtr.Zero, "Cannot start DLL loader.");
                mayFree = false;
                uint wait = WaitForSingleObject(thread, 15000);
                if (wait == 0x102) return "Loader still running after 15 seconds. Do not inject again yet.";
                Check(wait == 0, "Waiting for DLL loader failed.");
                mayFree = true;
                // Enumerate modules rather than relying on the truncated 32-bit thread return value.
                process.Refresh();
                foreach (ProcessModule module in process.Modules)
                    if (string.Equals(module.FileName, path, StringComparison.OrdinalIgnoreCase))
                        return "DLL loaded successfully. In-game initialization is separate.";
                throw new InvalidOperationException("DLL did not remain loaded. Check its dependencies and initialization.");
            }
            finally
            {
                if (thread != IntPtr.Zero) CloseHandle(thread);
                if (memory != IntPtr.Zero && mayFree) VirtualFreeEx(handle, memory, UIntPtr.Zero, 0x8000);
                CloseHandle(handle);
            }
        }
    }

    [DllImport("kernel32.dll", SetLastError = true)] private static extern IntPtr OpenProcess(uint access, bool inherit, int pid);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool IsWow64Process(IntPtr process, out bool wow64);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] private static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Ansi, ExactSpelling = true, SetLastError = true)] private static extern IntPtr GetProcAddress(IntPtr module, string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] private static extern bool GetModuleHandleEx(uint flags, IntPtr address, out IntPtr module);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] private static extern uint GetModuleFileName(IntPtr module, StringBuilder path, int size);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern IntPtr VirtualAllocEx(IntPtr process, IntPtr address, UIntPtr size, uint type, uint protection);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool VirtualFreeEx(IntPtr process, IntPtr address, UIntPtr size, uint type);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern bool WriteProcessMemory(IntPtr process, IntPtr address, byte[] data, UIntPtr size, out UIntPtr written);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern IntPtr CreateRemoteThread(IntPtr process, IntPtr attributes, UIntPtr stack, IntPtr start, IntPtr parameter, uint flags, IntPtr threadId);
    [DllImport("kernel32.dll", SetLastError = true)] private static extern uint WaitForSingleObject(IntPtr handle, uint milliseconds);
    [DllImport("kernel32.dll")] private static extern bool CloseHandle(IntPtr handle);
}
