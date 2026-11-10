#include "JavaRuntime.h"
#include "Logger.h"

#include <windows.h>
#include <fstream>
#include <algorithm>
#include <set>

namespace soju {

static bool fileExists(const std::wstring& p) {
    DWORD a = GetFileAttributesW(p.c_str());
    return a != INVALID_FILE_ATTRIBUTES && !(a & FILE_ATTRIBUTE_DIRECTORY);
}

// Parses a <home>\release file's JAVA_VERSION into a major number. "1.8.0_x" -> 8, "21.0.1" -> 21.
static int majorFromRelease(const std::wstring& home) {
    std::ifstream in(home + L"\\release");
    if (!in) return 0;
    std::string line;
    while (std::getline(in, line)) {
        auto pos = line.find("JAVA_VERSION=");
        if (pos == std::string::npos) continue;
        std::string v = line.substr(pos + 13);
        // strip quotes
        v.erase(std::remove(v.begin(), v.end(), '"'), v.end());
        if (v.rfind("1.", 0) == 0 && v.size() > 2) return v[2] - '0';      // 1.8 -> 8
        int major = 0;
        for (char c : v) { if (c < '0' || c > '9') break; major = major * 10 + (c - '0'); }
        return major;
    }
    return 0;
}

static JavaInstall inspect(const std::wstring& home) {
    JavaInstall j;
    if (home.empty()) return j;
    std::wstring h = home;
    while (!h.empty() && (h.back() == L'\\' || h.back() == L'/')) h.pop_back();
    std::wstring javaExe = h + L"\\bin\\java.exe";
    if (!fileExists(javaExe)) return j;
    j.home = h;
    j.javaExe = javaExe;
    j.isJdk = fileExists(h + L"\\bin\\javac.exe");
    j.major = majorFromRelease(h);
    return j;
}

static void addFromRegistry(HKEY root, const wchar_t* subkey, std::vector<JavaInstall>& out) {
    HKEY key;
    if (RegOpenKeyExW(root, subkey, 0, KEY_READ | KEY_WOW64_64KEY, &key) != ERROR_SUCCESS) return;
    wchar_t name[256];
    DWORD index = 0, nameLen = 256;
    while (RegEnumKeyExW(key, index++, name, &nameLen, nullptr, nullptr, nullptr, nullptr) == ERROR_SUCCESS) {
        nameLen = 256;
        HKEY ver;
        if (RegOpenKeyExW(key, name, 0, KEY_READ | KEY_WOW64_64KEY, &ver) == ERROR_SUCCESS) {
            wchar_t home[MAX_PATH]; DWORD size = sizeof(home); DWORD type = 0;
            // Adoptium stores JavaHome under <name>\hotspot\MSI; JavaSoft under <name> directly.
            if (RegGetValueW(ver, nullptr, L"JavaHome", RRF_RT_REG_SZ, &type, home, &size) == ERROR_SUCCESS) {
                JavaInstall j = inspect(home);
                if (!j.home.empty()) out.push_back(j);
            } else if (RegGetValueW(ver, L"hotspot\\MSI", L"Path", RRF_RT_REG_SZ, &type, home, &size) == ERROR_SUCCESS) {
                JavaInstall j = inspect(home);
                if (!j.home.empty()) out.push_back(j);
            }
            RegCloseKey(ver);
        }
    }
    RegCloseKey(key);
}

static void addFromVendorDirs(std::vector<JavaInstall>& out) {
    const wchar_t* roots[] = {
        L"C:\\Program Files\\Eclipse Adoptium",
        L"C:\\Program Files\\Java",
        L"C:\\Program Files\\Microsoft",
        L"C:\\Program Files\\Amazon Corretto",
        L"C:\\Program Files\\Zulu",
        L"C:\\Program Files\\BellSoft",
        L"C:\\Program Files\\Semeru",
    };
    for (const wchar_t* r : roots) {
        std::wstring pattern = std::wstring(r) + L"\\*";
        WIN32_FIND_DATAW fd;
        HANDLE h = FindFirstFileW(pattern.c_str(), &fd);
        if (h == INVALID_HANDLE_VALUE) continue;
        do {
            if (!(fd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) continue;
            if (!wcscmp(fd.cFileName, L".") || !wcscmp(fd.cFileName, L"..")) continue;
            JavaInstall j = inspect(std::wstring(r) + L"\\" + fd.cFileName);
            if (!j.home.empty()) out.push_back(j);
        } while (FindNextFileW(h, &fd));
        FindClose(h);
    }
}

std::vector<JavaInstall> JavaRuntime::discover() {
    std::vector<JavaInstall> all;

    wchar_t env[MAX_PATH];
    if (GetEnvironmentVariableW(L"JAVA_HOME", env, MAX_PATH)) {
        JavaInstall j = inspect(env);
        if (!j.home.empty()) all.push_back(j);
    }
    addFromRegistry(HKEY_LOCAL_MACHINE, L"SOFTWARE\\Eclipse Adoptium\\JDK", all);
    addFromRegistry(HKEY_LOCAL_MACHINE, L"SOFTWARE\\Eclipse Adoptium\\JRE", all);
    addFromRegistry(HKEY_LOCAL_MACHINE, L"SOFTWARE\\JavaSoft\\JDK", all);
    addFromRegistry(HKEY_LOCAL_MACHINE, L"SOFTWARE\\JavaSoft\\Java Development Kit", all);
    addFromVendorDirs(all);

    // De-duplicate by home (case-insensitive).
    std::vector<JavaInstall> unique;
    std::set<std::wstring> seen;
    for (auto& j : all) {
        std::wstring key = j.home;
        std::transform(key.begin(), key.end(), key.begin(), towlower);
        if (seen.insert(key).second) unique.push_back(j);
    }
    for (auto& j : unique)
        Logger::debug(L"Java found: " + j.home + L" (major " + std::to_wstring(j.major) +
                      (j.isJdk ? L", JDK)" : L", JRE)"));
    return unique;
}

JavaInstall JavaRuntime::findBest(int minMajor, bool& found, const std::wstring& privateHome) {
    JavaInstall embedded = inspect(privateHome);
    if (embedded.major >= minMajor &&
        fileExists(embedded.home + L"\\bin\\attach.dll") &&
        fileExists(embedded.home + L"\\bin\\server\\jvm.dll")) {
        found = true;
        Logger::info(L"Using embedded Java runtime: " + embedded.home);
        return embedded;
    }
    wchar_t executable[32768];
    DWORD length = GetModuleFileNameW(nullptr, executable, 32768);
    if (length && length < 32768) {
        std::wstring directory(executable, length);
        auto slash = directory.find_last_of(L"\\/");
        if (slash != std::wstring::npos) {
            JavaInstall bundled = inspect(directory.substr(0, slash) + L"\\runtime");
            if (bundled.major >= minMajor &&
                fileExists(bundled.home + L"\\bin\\attach.dll") &&
                fileExists(bundled.home + L"\\bin\\server\\jvm.dll")) {
                found = true;
                Logger::info(L"Using bundled Java runtime: " + bundled.home);
                return bundled;
            }
        }
    }
    auto all = discover();
    JavaInstall best;
    found = false;
    for (auto& j : all) {
        if (j.major < minMajor) continue;
        bool better = !found
            || (j.isJdk && !best.isJdk)
            || (j.isJdk == best.isJdk && j.major > best.major);
        if (better) { best = j; found = true; }
    }
    return best;
}

} // namespace soju
