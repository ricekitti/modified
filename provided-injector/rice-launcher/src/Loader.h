#pragma once
#include <string>
#include <functional>
#include <windows.h>

// Drives the existing Java loader (soju-loader.jar) as a child process and relays its status.
// soju.exe is the front end; the proven loader still performs attachment/injection.
namespace soju {

struct LoadResult {
    bool success = false;
    std::wstring message;
};

class Loader {
public:
    static LoadResult runNative(const std::wstring& workDir, DWORD pid,
                               const std::function<void(const std::wstring&)>& onLine,
                               const std::function<void(DWORD, const std::string&)>& onVisualSession = {});
    // Runs: "<javaExe>" -jar "<loaderJar>" --pid <pid>   (workDir must contain soju.jar).
    // Each output line is passed to onLine for live status; returns overall success/failure.
    static LoadResult run(const std::wstring& javaExe,
                          const std::wstring& loaderJar,
                          const std::wstring& workDir,
                          DWORD pid,
                          const std::function<void(const std::wstring&)>& onLine);
};

} // namespace soju
