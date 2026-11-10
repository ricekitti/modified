#pragma once
#include <string>
#include <vector>
#include <windows.h>

// Native Minecraft/Lunar/Forge detection, matching the loader's bundled detector:
// a 1.8.9 game window (class "LWJGL") owned by a process that has jvm.dll + lwjgl[64].dll loaded.
namespace soju {

struct GameProcess {
    DWORD pid = 0;
    std::wstring exePath;        // full path of the game's javaw.exe
    std::wstring environment;    // "Lunar", "Forge" or "Minecraft"
    int javaMajor = 0;           // detected JVM major (typically 8 for 1.8.9)
};

class Process {
public:
    // Returns supported 1.8.9 game processes. `notes` receives human-readable INFO lines
    // (e.g. a modern-LWJGL game that soju cannot target, or a window still loading).
    static std::vector<GameProcess> detectGames(std::vector<std::wstring>& notes);

    // Best-effort command line of a process (empty if it cannot be read).
    static std::wstring commandLine(DWORD pid);
};

} // namespace soju
