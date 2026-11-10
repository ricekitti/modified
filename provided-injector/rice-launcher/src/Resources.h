#pragma once
#include <string>

// File-system layout for the launcher: where the exe lives, where its jars are,
// and the per-user %LOCALAPPDATA%\RiceSlinky data tree.
namespace soju {

struct Paths {
    std::wstring exeDir;       // folder containing soju.exe
    std::wstring dataRoot;     // %LOCALAPPDATA%\soju
    std::wstring logs;         // %LOCALAPPDATA%\soju\logs
    std::wstring cache;        // %LOCALAPPDATA%\soju\cache
    std::wstring configs;      // %LOCALAPPDATA%\soju\configs
    std::wstring data;         // %LOCALAPPDATA%\soju\data
    std::wstring runtime;      // %LOCALAPPDATA%\soju\runtime
};

class Resources {
public:
    static Paths resolve();                 // fills Paths and creates the data tree
    static bool  migrateFromJade(const Paths& p); // copies %LOCALAPPDATA%\Jade settings if present; non-destructive
    static bool  materializeBundledRuntime(const Paths& p); // extracts the compressed client and native bridge

    // Locates a bundled resource next to the exe (exeDir, exeDir\resources, exeDir\required).
    // Returns empty if not found.
    static std::wstring locate(const std::wstring& exeDir, const std::wstring& fileName);

    static bool fileExists(const std::wstring& path);
    static bool dirExists(const std::wstring& path);
};

} // namespace soju
