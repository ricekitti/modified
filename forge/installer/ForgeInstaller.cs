using System;
using System.Drawing;
using System.IO;
using System.Reflection;
using System.Security.Cryptography;
using System.Windows.Forms;

internal static class ForgeInstaller
{
    private const string JarName = "rice-modified-forge-1.8.9.jar";

    private static byte[] Payload()
    {
        using (Stream input = Assembly.GetExecutingAssembly().GetManifestResourceStream("RiceModified.Forge.jar"))
        using (var output = new MemoryStream())
        {
            if (input == null) throw new InvalidDataException("Missing Forge mod resource.");
            input.CopyTo(output);
            byte[] data = output.ToArray();
            string expected;
            using (var reader = new StreamReader(Assembly.GetExecutingAssembly().GetManifestResourceStream("RiceModified.Forge.sha256")))
                expected = reader.ReadToEnd().Trim();
            if (!String.Equals(Hash(data), expected, StringComparison.OrdinalIgnoreCase))
                throw new InvalidDataException("Embedded mod verification failed.");
            return data;
        }
    }

    private static string Hash(byte[] bytes)
    {
        using (var hash = SHA256.Create()) return BitConverter.ToString(hash.ComputeHash(bytes)).Replace("-", "").ToLowerInvariant();
    }

    private static string Install(string folder, byte[] data, bool overwrite)
    {
        string directory = Path.GetFullPath(folder);
        string destination = Path.Combine(directory, JarName);
        if (File.Exists(destination))
        {
            if (Hash(File.ReadAllBytes(destination)) == Hash(data)) return destination;
            if (!overwrite) throw new IOException("A different build already exists in this folder.");
        }
        Directory.CreateDirectory(directory);
        string temporary = Path.Combine(directory, ".rice-install-" + Guid.NewGuid().ToString("N") + ".tmp");
        try
        {
            File.WriteAllBytes(temporary, data);
            if (File.Exists(destination)) File.Replace(temporary, destination, null);
            else File.Move(temporary, destination);
        }
        finally { if (File.Exists(temporary)) File.Delete(temporary); }
        return destination;
    }

    [STAThread]
    private static int Main(string[] args)
    {
        try
        {
            byte[] data = Payload();
            // Used to verify installer extraction in an isolated build directory.
            if (args.Length == 2 && args[0] == "--extract")
            {
                Install(args[1], data, false);
                return 0;
            }
            if (args.Length != 0) return 2;
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            using (var form = new Form())
            {
                form.Text = "Rice Modified - Forge Installer";
                form.ClientSize = new Size(570, 320);
                form.FormBorderStyle = FormBorderStyle.FixedDialog;
                form.MaximizeBox = false;
                form.StartPosition = FormStartPosition.CenterScreen;
                form.Font = new Font("Segoe UI", 10);
                var title = new Label { Text = "Rice Modified for Forge 1.8.9", Location = new Point(24, 22), AutoSize = true, Font = new Font("Segoe UI", 17, FontStyle.Bold) };
                var info = new Label { Text = "Choose the mods folder for your Minecraft 1.8.9 Forge profile.\nFor Lunar, open your Forge profile's mods folder in the launcher.\nRestart the game after installation. Right Shift opens the menu.", Location = new Point(26, 73), Size = new Size(520, 78) };
                var folder = new TextBox { Text = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft", "mods"), Location = new Point(26, 164), Size = new Size(414, 28) };
                var browse = new Button { Text = "Browse...", Location = new Point(450, 162), Size = new Size(94, 31) };
                var status = new Label { Text = "Experimental port. Live Lunar and Mac compatibility remains unverified.", Location = new Point(26, 209), Size = new Size(520, 42) };
                var install = new Button { Text = "Install Forge mod", Location = new Point(365, 267), Size = new Size(179, 34) };
                browse.Click += delegate {
                    using (var dialog = new FolderBrowserDialog())
                    {
                        dialog.Description = "Select the mods folder of your Forge 1.8.9 profile";
                        if (Directory.Exists(folder.Text)) dialog.SelectedPath = folder.Text;
                        if (dialog.ShowDialog(form) == DialogResult.OK) folder.Text = dialog.SelectedPath;
                    }
                };
                install.Click += delegate {
                    try
                    {
                        if (String.IsNullOrWhiteSpace(folder.Text)) throw new IOException("Choose a mods folder first.");
                        string destination = Path.Combine(Path.GetFullPath(folder.Text), JarName);
                        bool overwrite = false;
                        if (File.Exists(destination) && Hash(File.ReadAllBytes(destination)) != Hash(data))
                        {
                            if (MessageBox.Show(form, "Replace the existing Rice Modified Forge build in this folder?", "Replace build", MessageBoxButtons.YesNo, MessageBoxIcon.Question) != DialogResult.Yes) return;
                            overwrite = true;
                        }
                        Install(folder.Text, data, overwrite);
                        status.Text = "Installed. Launch this profile with Forge 1.8.9, then press Right Shift.";
                    }
                    catch (Exception error) { MessageBox.Show(form, error.Message, "Installation failed", MessageBoxButtons.OK, MessageBoxIcon.Error); }
                };
                form.Controls.AddRange(new Control[] { title, info, folder, browse, status, install });
                Application.Run(form);
            }
            return 0;
        }
        catch (Exception error)
        {
            if (args.Length == 0) MessageBox.Show(error.Message, "Rice Modified", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }
}
