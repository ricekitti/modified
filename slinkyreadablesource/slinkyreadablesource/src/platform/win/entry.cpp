// Windows entry of the client DLL: it is injected into the game process (LoadLibrary), so DllMain only starts the
// start-up thread (slinky_setup runs JNI and file I/O, which must not happen under the loader lock).
#include "slinky/platform.h"
#include "slinky/win32.h"

extern "C" void start_unload_thread(HMODULE self);   // protected/input.cpp: unload thread (teardown, then FreeLibraryAndExitThread)
extern "C" void injection_notice_ready();
extern "C" __declspec(dllimport) DWORD WINAPI GetModuleFileNameW(HMODULE module, wchar_t *path, DWORD size);

// The Rice launcher waits for this per-injection status file beside its DLL snapshot.
static void report_startup(HMODULE self, bool failed) {
    wchar_t path[32768];
    DWORD length = GetModuleFileNameW(self, path, 32768);
    if (!length || length >= 32768) return;
    while (length && path[length - 1] != L'\\' && path[length - 1] != L'/') --length;
    const wchar_t name[] = L"rice-native.log";
    if (!length || length + sizeof(name) / sizeof(wchar_t) > 32768) return;
    for (unsigned i = 0; i < sizeof(name) / sizeof(wchar_t); ++i) path[length + i] = name[i];
    HANDLE file = CreateFileW(path, GENERIC_WRITE, FILE_SHARE_READ, 0, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, 0);
    if (file == INVALID_HANDLE) return;
    const char *message = failed ? "FINAL STATUS: FAILED\r\n" : "FINAL STATUS: RUNNING\r\n";
    DWORD size = 0, written;
    while (message[size]) ++size;
    WriteFile(file, message, size, &written, 0);
    CloseHandle(file);
}

static DWORD start_thread(LPVOID self) {
    bool failed = slinky_setup(self) != 0;
    report_startup((HMODULE)self, failed);
    if (!failed) injection_notice_ready();
    if (failed) start_unload_thread((HMODULE)self);   // failed: report was printed, take the DLL out again
    return 0;
}

extern "C" BOOL WINAPI DllMain(HINSTANCE self, DWORD reason, void *) {
    if (reason == 1 /* DLL_PROCESS_ATTACH */) {
        DisableThreadLibraryCalls(self);
        CloseHandle(CreateThread(0, 0, start_thread, self, 0, 0));
    }
    return 1;
}

// ---- diagnostics: debugger output, the process's stderr handle and %TEMP%\slinky.log ----
// (launchers do not keep the game's stderr, and an unload problem must stay visible after the exit)
extern "C" {
__declspec(dllimport) DWORD WINAPI GetTempPathW(DWORD n, wchar_t *buf);
}
extern "C" void slinky_log_write(const char *line) {
    char buf[600];
    int n = 0;
    for (const char *p = "Slinky: "; *p; p++) buf[n++] = *p;
    for (; *line && n < 597; line++) buf[n++] = *line;
    buf[n++] = '\r';
    buf[n++] = '\n';
    buf[n] = 0;
    OutputDebugStringA(buf);
    DWORD done;
    HANDLE err = GetStdHandle((DWORD)-12 /* STD_ERROR_HANDLE */);
    if (err && err != INVALID_HANDLE) WriteFile(err, buf, (DWORD)n, &done, 0);
    wchar_t path[320];
    DWORD len = GetTempPathW(300, path);
    if (len && len < 300) {
        const wchar_t name[] = L"slinky.log";
        for (int i = 0; i <= 10; i++) path[len + i] = name[i];
        HANDLE f = CreateFileW(path, 4 /* FILE_APPEND_DATA */, FILE_SHARE_READ | 2 /* FILE_SHARE_WRITE */,
                              0, 4 /* OPEN_ALWAYS */, FILE_ATTRIBUTE_NORMAL, 0);
        if (f != INVALID_HANDLE) {
            WriteFile(f, buf, (DWORD)n, &done, 0);
            CloseHandle(f);
        }
    }
}

// ---- OpenGL: wglGetProcAddress for extensions, opengl32 itself for GL 1.1 ----
typedef void *(WINAPI *WglGetProcAddress)(const char *);
extern "C" void *slinky_gl_get_proc(const char *name) {
    static HMODULE gl;
    static WglGetProcAddress wgl;
    if (!gl) {
        gl = GetModuleHandleA("opengl32");
        if (!gl) gl = LoadLibraryA("opengl32.dll");
        if (!gl) return 0;
        wgl = (WglGetProcAddress)GetProcAddress(gl, "wglGetProcAddress");
    }
    void *p = wgl ? wgl(name) : 0;
    if (p && p != (void *)1 && p != (void *)2 && p != (void *)3 && p != (void *)-1) return p;   // wgl failure values
    return GetProcAddress(gl, name);
}
