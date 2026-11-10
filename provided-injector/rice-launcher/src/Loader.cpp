#include "Loader.h"
#include "Logger.h"

#include <windows.h>
#include <vector>
#include <algorithm>
#include <bcrypt.h>
#include <filesystem>
#include <tlhelp32.h>
#include <fstream>
#include <thread>
#include <chrono>

namespace soju {

namespace {
struct ScopedHandle {
    HANDLE value = nullptr;
    explicit ScopedHandle(HANDLE handle) : value(handle) {}
    ~ScopedHandle() { if (value && value != INVALID_HANDLE_VALUE) CloseHandle(value); }
    explicit operator bool() const { return value && value != INVALID_HANDLE_VALUE; }
};

BYTE* remoteModuleBase(DWORD pid, const wchar_t* moduleName) {
    ScopedHandle snapshot(CreateToolhelp32Snapshot(TH32CS_SNAPMODULE | TH32CS_SNAPMODULE32, pid));
    if (!snapshot) return nullptr;
    MODULEENTRY32W module{};
    module.dwSize = sizeof(module);
    if (Module32FirstW(snapshot.value, &module)) do {
        if (!_wcsicmp(module.szModule, moduleName)) return module.modBaseAddr;
    } while (Module32NextW(snapshot.value, &module));
    return nullptr;
}

std::string readBinaryFile(const std::wstring& path) {
    std::ifstream input(std::filesystem::path(path), std::ios::binary);
    return input ? std::string(std::istreambuf_iterator<char>(input), {}) : std::string();
}
} // namespace

static std::wstring utf8ToWide(const std::string& s) {
    if (s.empty()) return L"";
    int n = MultiByteToWideChar(CP_UTF8, 0, s.data(), (int)s.size(), nullptr, 0);
    if (n <= 0) return L"";
    std::wstring w(n, L'\0');
    MultiByteToWideChar(CP_UTF8, 0, s.data(), (int)s.size(), &w[0], n);
    return w;
}

LoadResult Loader::runNative(const std::wstring& workDir, DWORD pid,
                            const std::function<void(const std::wstring&)>& onLine,
                            const std::function<void(DWORD, const std::string&)>& onVisualSession) {
    (void)onVisualSession;
    LoadResult result;
    unsigned char random[16];
    if (BCryptGenRandom(nullptr, random, sizeof(random), BCRYPT_USE_SYSTEM_PREFERRED_RNG) != 0) {
        result.message = L"Could not generate an injection nonce.";
        return result;
    }
    const char* hex = "0123456789abcdef";
    std::string nonce;
    for (unsigned char value : random) { nonce += hex[value >> 4]; nonce += hex[value & 15]; }
    std::wstring directory = workDir + L"\\slinky-" + std::wstring(nonce.begin(), nonce.end());
    if (!CreateDirectoryW(directory.c_str(), nullptr)) {
        result.message = L"Could not create the native bridge snapshot.";
        return result;
    }
    std::wstring bridge = directory + L"\\RiceNative.dll";
    std::wstring logPath = directory + L"\\rice-native.log";
    if (!CopyFileW((workDir + L"\\RiceNative.dll").c_str(), bridge.c_str(), TRUE)) {
        result.message = L"Could not copy the Rice Slinky payload.";
        return result;
    }
    ScopedHandle process(OpenProcess(PROCESS_CREATE_THREAD | PROCESS_VM_OPERATION | PROCESS_VM_READ |
                                     PROCESS_VM_WRITE | PROCESS_QUERY_INFORMATION | SYNCHRONIZE, FALSE, pid));
    if (!process) {
        result.message = L"Could not open Minecraft (Windows error " + std::to_wstring(GetLastError()) + L").";
        return result;
    }
    const SIZE_T pathBytes = (bridge.size() + 1) * sizeof(wchar_t);
    void* remotePath = VirtualAllocEx(process.value, nullptr, pathBytes, MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
    if (!remotePath) {
        result.message = L"Could not allocate the Rice Slinky path in Minecraft.";
        return result;
    }
    SIZE_T written = 0;
    if (!WriteProcessMemory(process.value, remotePath, bridge.c_str(), pathBytes, &written) || written != pathBytes) {
        VirtualFreeEx(process.value, remotePath, 0, MEM_RELEASE);
        result.message = L"Could not copy the Rice Slinky path into Minecraft.";
        return result;
    }
    HMODULE localKernel = GetModuleHandleW(L"kernel32.dll");
    FARPROC localLoad = localKernel ? GetProcAddress(localKernel, "LoadLibraryW") : nullptr;
    HMODULE loaderOwner = nullptr;
    wchar_t loaderPath[MAX_PATH]{};
    if (localLoad && GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS |
            GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT, reinterpret_cast<LPCWSTR>(localLoad), &loaderOwner))
        GetModuleFileNameW(loaderOwner, loaderPath, MAX_PATH);
    const wchar_t* loaderName = wcsrchr(loaderPath, L'\\');
    BYTE* targetKernel = loaderName ? remoteModuleBase(pid, loaderName + 1) : nullptr;
    if (!loaderOwner || !localLoad || !targetKernel) {
        VirtualFreeEx(process.value, remotePath, 0, MEM_RELEASE);
        result.message = L"Could not locate the Windows DLL loader.";
        return result;
    }
    auto start = reinterpret_cast<LPTHREAD_START_ROUTINE>(targetKernel +
        (reinterpret_cast<BYTE*>(localLoad) - reinterpret_cast<BYTE*>(loaderOwner)));
    ScopedHandle thread(CreateRemoteThread(process.value, nullptr, 0, start, remotePath, 0, nullptr));
    if (!thread) {
        VirtualFreeEx(process.value, remotePath, 0, MEM_RELEASE);
        result.message = L"Could not start Rice Slinky inside Minecraft.";
        return result;
    }
    if (onLine) onLine(L"Loading Rice Slinky into Minecraft...");
    DWORD wait = WaitForSingleObject(thread.value, 30000);
    if (wait != WAIT_OBJECT_0) {
        result.message = L"Minecraft did not finish loading Rice Slinky in time.";
        return result; // Keep remotePath valid while the remote loader may still read it.
    }
    VirtualFreeEx(process.value, remotePath, 0, MEM_RELEASE);
    DWORD exitCode = 0;
    GetExitCodeThread(thread.value, &exitCode);
    if (!exitCode) {
        result.message = L"Windows could not load the Rice Slinky payload.";
        return result;
    }
    if (onLine) onLine(L"Waiting for Rice Slinky to finish starting...");
    for (int attempt = 0; attempt < 800; ++attempt) {
        std::string log = readBinaryFile(logPath);
        if (log.find("FINAL STATUS: RUNNING") != std::string::npos ||
            log.find("FINAL STATUS: RELOADED") != std::string::npos) {
            result.success = true;
            break;
        }
        if (log.find("FINAL STATUS: FAILED") != std::string::npos ||
            log.find("FINAL STATUS: ALREADY_RUNNING") != std::string::npos) {
            result.message = log.find("ALREADY_RUNNING") != std::string::npos
                ? L"Rice Slinky is already active in this Minecraft session."
                : L"Rice Slinky reported a startup failure. See rice-native.log.";
            break;
        }
        if (WaitForSingleObject(process.value, 0) == WAIT_OBJECT_0) {
            result.message = L"Minecraft exited while Rice Slinky was starting.";
            break;
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(150));
    }
    if (!result.success && result.message.empty())
        result.message = L"Rice Slinky did not confirm startup in time. See rice-native.log.";
    if (result.success) result.message = L"Rice Slinky loaded successfully";
    Logger::info(result.message);
    if (onLine) onLine(result.success ? L"Rice Slinky is ready." : result.message);
    return result;
}

LoadResult Loader::run(const std::wstring& javaExe,
                       const std::wstring& loaderJar,
                       const std::wstring& workDir,
                       DWORD pid,
                       const std::function<void(const std::wstring&)>& onLine) {
    LoadResult result;

    SECURITY_ATTRIBUTES sa = {};
    sa.nLength = sizeof(sa);
    sa.bInheritHandle = TRUE;

    HANDLE readPipe = nullptr, writePipe = nullptr;
    if (!CreatePipe(&readPipe, &writePipe, &sa, 0)) {
        result.message = L"Could not create an output pipe for the loader.";
        return result;
    }
    SetHandleInformation(readPipe, HANDLE_FLAG_INHERIT, 0);

    std::wstring cmd = L"\"" + javaExe + L"\" -jar \"" + loaderJar + L"\" --pid " + std::to_wstring(pid);
    std::vector<wchar_t> cmdBuf(cmd.begin(), cmd.end());
    cmdBuf.push_back(L'\0');

    STARTUPINFOW si = {};
    si.cb = sizeof(si);
    si.dwFlags = STARTF_USESTDHANDLES;
    si.hStdOutput = writePipe;
    si.hStdError = writePipe;
    si.hStdInput = GetStdHandle(STD_INPUT_HANDLE);

    PROCESS_INFORMATION pi = {};
    Logger::info(L"Starting loader: " + cmd);
    BOOL ok = CreateProcessW(nullptr, cmdBuf.data(), nullptr, nullptr, TRUE,
                             CREATE_NO_WINDOW, nullptr, workDir.c_str(), &si, &pi);
    const DWORD createError = ok ? ERROR_SUCCESS : GetLastError();
    CloseHandle(writePipe); // our copy; child keeps its own
    if (!ok) {
        CloseHandle(readPipe);
        result.message = L"Failed to start the Java loader (CreateProcess error " +
                         std::to_wstring(createError) + L").";
        return result;
    }

    // Read merged stdout/stderr until the pipe closes, emitting whole lines.
    std::string pending;
    char buf[4096];
    DWORD read = 0;
    bool sawReady = false;
    std::wstring loaderError;
    const auto emitLine = [&](std::string raw) {
        if (!raw.empty() && raw.back() == '\r') raw.pop_back();
        std::wstring line = utf8ToWide(raw);
        std::wstring lower = line;
        std::transform(lower.begin(), lower.end(), lower.begin(), towlower);
        if (lower.find(L"is ready") != std::wstring::npos ||
            lower.find(L"accepted the agent") != std::wstring::npos)
            sawReady = true;
        Logger::info(L"[loader] " + line);
        if (lower.rfind(L"soju: ", 0) == 0) loaderError = line;
        if (onLine) onLine(line);
    };
    while (ReadFile(readPipe, buf, sizeof(buf), &read, nullptr) && read > 0) {
        pending.append(buf, read);
        size_t nl;
        while ((nl = pending.find('\n')) != std::string::npos) {
            std::string raw = pending.substr(0, nl);
            pending.erase(0, nl + 1);
            emitLine(raw);
        }
    }
    if (!pending.empty()) emitLine(pending);
    CloseHandle(readPipe);

    WaitForSingleObject(pi.hProcess, INFINITE);
    DWORD exitCode = 1;
    GetExitCodeProcess(pi.hProcess, &exitCode);
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);

    result.success = (exitCode == 0) && sawReady;
    if (result.success) {
        result.message = L"soju loaded successfully";
    } else if (exitCode != 0) {
        result.message = loaderError.empty()
            ? L"Loader exited with code " + std::to_wstring(exitCode) +
              L". See the game log and %LOCALAPPDATA%\\soju\\logs."
            : loaderError;
    } else {
        result.message = L"Loader finished without confirming readiness. Check the logs.";
    }
    return result;
}

} // namespace soju
