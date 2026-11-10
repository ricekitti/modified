#include "Process.h"
#include "Logger.h"

#include <windows.h>
#include <tlhelp32.h>
#include <psapi.h>
#include <algorithm>
#include <set>

#pragma comment(lib, "psapi.lib")

namespace soju {

static std::wstring toLower(std::wstring s) {
    std::transform(s.begin(), s.end(), s.begin(), towlower);
    return s;
}

// --- module inspection -----------------------------------------------------

struct Modules {
    bool hasJvm = false;
    bool hasLwjgl = false;   // lwjgl.dll / lwjgl64.dll => 1.8.x LWJGL2
    bool hasGlfw = false;    // glfw.dll => modern LWJGL3 (not 1.8.9)
    std::wstring jvmPath;
};

static Modules inspectModules(DWORD pid) {
    Modules m;
    HANDLE snap = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE | TH32CS_SNAPMODULE32, pid);
    if (snap == INVALID_HANDLE_VALUE) return m;
    MODULEENTRY32W me; me.dwSize = sizeof(me);
    if (Module32FirstW(snap, &me)) {
        do {
            std::wstring name = toLower(me.szModule);
            if (name == L"jvm.dll") { m.hasJvm = true; m.jvmPath = me.szExePath; }
            else if (name == L"lwjgl64.dll" || name == L"lwjgl.dll") m.hasLwjgl = true;
            else if (name == L"glfw.dll") m.hasGlfw = true;
        } while (Module32NextW(snap, &me));
    }
    CloseHandle(snap);
    return m;
}

static std::wstring imagePath(DWORD pid) {
    HANDLE h = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, FALSE, pid);
    if (!h) return L"";
    wchar_t buf[MAX_PATH]; DWORD size = MAX_PATH;
    std::wstring path = QueryFullProcessImageNameW(h, 0, buf, &size) ? std::wstring(buf, size) : L"";
    CloseHandle(h);
    return path;
}

// --- command line via NtQueryInformationProcess (best effort) --------------

typedef struct { PVOID Reserved1; PVOID PebBaseAddress; PVOID Reserved2[2];
                 ULONG_PTR UniqueProcessId; PVOID Reserved3; } PROCESS_BASIC_INFO;
typedef LONG (WINAPI *NtQIP)(HANDLE, ULONG, PVOID, ULONG, PULONG);

std::wstring Process::commandLine(DWORD pid) {
    HMODULE ntdll = GetModuleHandleW(L"ntdll.dll");
    if (!ntdll) return L"";
    auto NtQuery = reinterpret_cast<NtQIP>(GetProcAddress(ntdll, "NtQueryInformationProcess"));
    if (!NtQuery) return L"";
    HANDLE h = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, pid);
    if (!h) return L"";

    std::wstring result;
    PROCESS_BASIC_INFO pbi = {};
    if (NtQuery(h, 0 /*ProcessBasicInformation*/, &pbi, sizeof(pbi), nullptr) == 0 && pbi.PebBaseAddress) {
        // PEB.ProcessParameters is at offset 0x20 on x64; CommandLine (UNICODE_STRING) at 0x70.
        BYTE* paramsPtr = nullptr;
        SIZE_T read = 0;
        if (ReadProcessMemory(h, (BYTE*)pbi.PebBaseAddress + 0x20, &paramsPtr, sizeof(paramsPtr), &read) && paramsPtr) {
            USHORT length = 0;
            PVOID buffer = nullptr;
            ReadProcessMemory(h, paramsPtr + 0x70, &length, sizeof(length), &read);
            ReadProcessMemory(h, paramsPtr + 0x78, &buffer, sizeof(buffer), &read);
            if (length > 0 && length < 0x8000 && buffer) {
                std::wstring tmp(length / sizeof(wchar_t), L'\0');
                if (ReadProcessMemory(h, buffer, &tmp[0], length, &read)) result = tmp;
            }
        }
    }
    CloseHandle(h);
    return result;
}

static std::wstring classifyEnvironment(DWORD pid, const std::wstring& exePath) {
    std::wstring hay = toLower(exePath) + L" " + toLower(Process::commandLine(pid));
    if (hay.find(L"lunarclient") != std::wstring::npos || hay.find(L"lunar") != std::wstring::npos) return L"Lunar";
    if (hay.find(L"forge") != std::wstring::npos || hay.find(L"fml") != std::wstring::npos) return L"Forge";
    return L"Minecraft";
}

static int jvmMajorFromPath(const std::wstring& jvmPath) {
    // jvm.dll lives in <home>\bin\<server|client>\jvm.dll; climb to <home> and read release.
    std::wstring p = jvmPath;
    for (int i = 0; i < 3 && !p.empty(); ++i) {
        size_t slash = p.find_last_of(L"\\/");
        if (slash == std::wstring::npos) break;
        p = p.substr(0, slash);
    }
    // Read <home>\release if present.
    HANDLE f = CreateFileW((p + L"\\release").c_str(), GENERIC_READ, FILE_SHARE_READ, nullptr,
                           OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, nullptr);
    if (f == INVALID_HANDLE_VALUE) return 0;
    char buf[4096]; DWORD n = 0; int major = 0;
    if (ReadFile(f, buf, sizeof(buf) - 1, &n, nullptr)) {
        buf[n] = 0;
        std::string s(buf);
        auto pos = s.find("JAVA_VERSION=");
        if (pos != std::string::npos) {
            std::string v = s.substr(pos + 13);
            v.erase(std::remove(v.begin(), v.end(), '"'), v.end());
            if (v.rfind("1.", 0) == 0 && v.size() > 2) major = v[2] - '0';
            else { for (char c : v) { if (c < '0' || c > '9') break; major = major * 10 + (c - '0'); } }
        }
    }
    CloseHandle(f);
    return major;
}

// --- window enumeration ----------------------------------------------------

struct Candidate { DWORD pid; bool lwjglClass; bool glfwClass; };

static BOOL CALLBACK enumProc(HWND hwnd, LPARAM param) {
    auto* out = reinterpret_cast<std::vector<Candidate>*>(param);
    wchar_t cls[64] = {}, title[256] = {};
    GetClassNameW(hwnd, cls, 64);
    GetWindowTextW(hwnd, title, 256);
    std::wstring c = cls, t = toLower(title);
    bool isLwjgl = (c == L"LWJGL");
    bool isGlfw = (c == L"GLFW30");
    bool titleMc = t.find(L"minecraft") != std::wstring::npos;
    if (isLwjgl || isGlfw || titleMc) {
        DWORD pid = 0;
        GetWindowThreadProcessId(hwnd, &pid);
        if (pid) out->push_back({ pid, isLwjgl, isGlfw });
    }
    return TRUE;
}

std::vector<GameProcess> Process::detectGames(std::vector<std::wstring>& notes) {
    std::vector<Candidate> candidates;
    EnumWindows(enumProc, reinterpret_cast<LPARAM>(&candidates));

    std::vector<GameProcess> games;
    std::set<DWORD> seen;
    for (const auto& c : candidates) {
        if (!seen.insert(c.pid).second) continue;
        Modules m = inspectModules(c.pid);
        if (!m.hasJvm) continue; // not a JVM window
        if (c.glfwClass || m.hasGlfw) {
            notes.push_back(L"A modern Minecraft (PID " + std::to_wstring(c.pid) +
                            L") is running; soju requires 1.8.9.");
            continue;
        }
        if (!m.hasLwjgl) {
            notes.push_back(L"A game window (PID " + std::to_wstring(c.pid) +
                            L") is still loading; wait for the title screen.");
            continue;
        }
        GameProcess g;
        g.pid = c.pid;
        g.exePath = imagePath(c.pid);
        g.environment = classifyEnvironment(c.pid, g.exePath);
        g.javaMajor = jvmMajorFromPath(m.jvmPath);
        games.push_back(g);
        Logger::info(L"Minecraft detected  PID: " + std::to_wstring(g.pid) +
                     L"  Java: " + (g.javaMajor ? std::to_wstring(g.javaMajor) : L"?") +
                     L"  Environment: " + g.environment);
    }
    return games;
}

} // namespace soju
