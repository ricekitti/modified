#include "Launcher.h"
#include <shellapi.h>

int WINAPI wWinMain(HINSTANCE instance, HINSTANCE, PWSTR, int) {
    int count = 0;
    LPWSTR* args = CommandLineToArgvW(GetCommandLineW(), &count);
    std::wstring preview, render;
    for (int i = 1; args && i < count; ++i) {
        if (wcscmp(args[i], L"--preview") == 0 && i + 1 < count) preview = args[++i];
        else if (wcscmp(args[i], L"--render") == 0 && i + 1 < count) render = args[++i];
    }
    if (args) LocalFree(args);
    // Rendering is always isolated from credentials, network calls and game attachment.
    if (!render.empty() && preview.empty()) preview = L"auth";
    soju::Launcher launcher;
    return launcher.run(instance, preview, render);
}
