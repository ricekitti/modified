#include "AuthManager.h"
#include "LicensePrompt.h"
#include "Loader.h"
#include "Logger.h"
#include "Process.h"
#include "Resources.h"

#include <windows.h>
#include <algorithm>
#include <chrono>
#include <fcntl.h>
#include <array>
#include <iostream>
#include <io.h>
#include <string>
#include <thread>
#include <vector>

namespace {

bool terminalColors = false;
const wchar_t* tone(const wchar_t* color) { return terminalColors ? color : L""; }
const wchar_t* pink() { return tone(L"\x1b[38;2;244;176;206m"); }
const wchar_t* dim() { return tone(L"\x1b[38;2;152;145;155m"); }
const wchar_t* reset() { return tone(L"\x1b[0m"); }


std::string narrow(const std::wstring& value) {
    if (value.empty()) return "";
    int size = WideCharToMultiByte(CP_UTF8, 0, value.c_str(), (int)value.size(), nullptr, 0, nullptr, nullptr);
    std::string out(size, '\0');
    WideCharToMultiByte(CP_UTF8, 0, value.c_str(), (int)value.size(), &out[0], size, nullptr, nullptr);
    return out;
}

std::wstring widen(const std::string& value) {
    if (value.empty()) return L"";
    int size = MultiByteToWideChar(CP_UTF8, 0, value.c_str(), (int)value.size(), nullptr, 0);
    std::wstring out(size, L'\0');
    MultiByteToWideChar(CP_UTF8, 0, value.c_str(), (int)value.size(), &out[0], size);
    return out;
}

int columns() {
    CONSOLE_SCREEN_BUFFER_INFO info{};
    if (GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), &info))
        return info.srWindow.Right - info.srWindow.Left + 1;
    return 80;
}

std::wstring margin(int extra = 0) {
    return std::wstring((std::max)(0, (columns() - 54) / 2) + extra, L' ');
}

void centered(const std::wstring& text) {
    std::wcout << std::wstring((std::max)(0, (columns() - static_cast<int>(text.size())) / 2), L' ') << text << L"\n";
}

// Legacy console hosts support an application font; Windows Terminal keeps its profile font.
struct ConsoleFont {
    HANDLE output = GetStdHandle(STD_OUTPUT_HANDLE);
    CONSOLE_FONT_INFOEX original{};
    bool changed = false;
    ConsoleFont() {
        original.cbSize = sizeof(original);
        if (!GetCurrentConsoleFontEx(output, FALSE, &original)) return;
        CONSOLE_FONT_INFOEX font = original;
        wcscpy_s(font.FaceName, L"Consolas");
        font.dwFontSize = {0, 18};
        font.FontWeight = FW_NORMAL;
        changed = SetCurrentConsoleFontEx(output, FALSE, &font) != FALSE;
    }
    ~ConsoleFont() { if (changed) SetCurrentConsoleFontEx(output, FALSE, &original); }
};

void setupConsole() {
    SetConsoleTitleW(L"Rice Slinky | Loader");
    HANDLE output = GetStdHandle(STD_OUTPUT_HANDLE);
    DWORD mode = 0;
    if (GetConsoleMode(output, &mode))
        terminalColors = SetConsoleMode(output, mode | ENABLE_VIRTUAL_TERMINAL_PROCESSING) != FALSE;
    _setmode(_fileno(stdout), _O_U16TEXT);
    _setmode(_fileno(stdin), _O_U16TEXT);
}

void banner() {
    const wchar_t* logo[] = {
        LR"( ____  ___ ____ _____ )",
        LR"(|  _ \|_ _/ ___| ____|)",
        LR"(| |_) || | |   |  _|  )",
        LR"(|  _ < | | |___| |___ )",
        LR"(|_| \_\___\____|_____|)"
    };
    size_t width = 0;
    for (auto row : logo) width = (std::max)(width, wcslen(row));
    const std::wstring indent((std::max)(0, (columns() - static_cast<int>(width)) / 2), L' ');
    std::wcout << L"\n" << pink();
    for (auto row : logo) std::wcout << indent << row << L"\n";
    std::wcout << reset() << L"\n";
    centered(L"Rice Slinky / v1.8");
    std::wcout << dim();
    centered(L"Minecraft 1.8.9");
    std::wcout << L"\n";
    centered(L"------------------------------------------------------");
    std::wcout << reset();
}

void stage(int step, const wchar_t* name) {
    std::wcout << L"\n" << margin() << pink() << L"[" << step << L"/4] " << name << reset() << L"\n";
}

void status(const std::string& text, bool ok = true) {
    std::wcout << margin(4) << (ok ? dim() : tone(L"\x1b[38;2;250;130;146m"))
               << (ok ? L"· " : L"! ") << widen(text) << reset() << L"\n" << std::flush;
}

void confirmed(const wchar_t* text) {
    std::wcout << margin(4) << pink() << L"✓ " << reset() << text << L"\n" << std::flush;
}

void preview() {
    std::wcout << L"\n" << margin() << dim() << L"Design preview · no login or game connection" << reset() << L"\n";
    stage(1, L"Runtime"); confirmed(L"Embedded client ready");
    stage(2, L"Access"); confirmed(L"License accepted");
    stage(3, L"Minecraft"); confirmed(L"Minecraft 1.8.9 · PID 1234 (example)");
    stage(4, L"Launch"); status("Connecting to Minecraft..."); confirmed(L"Client started");
    std::wcout << L"\n" << margin() << pink() << L"READY" << reset() << L"  Return to Minecraft.\n\n";
}

std::wstring readFileUtf16(const std::wstring& path) {
    HANDLE file = CreateFileW(path.c_str(), GENERIC_READ, FILE_SHARE_READ, nullptr, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, nullptr);
    if (file == INVALID_HANDLE_VALUE) return L"";
    LARGE_INTEGER size{};
    std::wstring result;
    if (GetFileSizeEx(file, &size) && size.QuadPart > 0 && size.QuadPart <= 1024 * sizeof(wchar_t)) {
        std::vector<wchar_t> buffer((size_t)(size.QuadPart / sizeof(wchar_t)) + 1, L'\0');
        DWORD read = 0;
        if (ReadFile(file, buffer.data(), (DWORD)(buffer.size() * sizeof(wchar_t) - sizeof(wchar_t)), &read, nullptr)) {
            result.assign(buffer.data(), read / sizeof(wchar_t));
            result.erase(std::remove_if(result.begin(), result.end(), [](wchar_t c) {
                return c == L'\0' || c == L'\r' || c == L'\n' || c == L'\t';
            }), result.end());
        }
    }
    CloseHandle(file);
    return result;
}

void writeFileUtf16(const std::wstring& path, const std::wstring& value) {
    HANDLE file = CreateFileW(path.c_str(), GENERIC_WRITE, 0, nullptr, CREATE_ALWAYS, FILE_ATTRIBUTE_HIDDEN, nullptr);
    if (file == INVALID_HANDLE_VALUE) return;
    DWORD written = 0;
    WriteFile(file, value.data(), (DWORD)(value.size() * sizeof(wchar_t)), &written, nullptr);
    CloseHandle(file);
}

void waitExit() {
    std::wcout << L"\n" << margin() << dim() << L"Press Enter to close." << reset() << std::flush;
    std::wstring ignored;
    std::getline(std::wcin, ignored);
}

void showSuccessPage() {
    std::wcout << L"\n" << margin() << pink() << L"READY" << reset() << L"  Rice Slinky is running. Return to Minecraft.\n";
}

} // namespace

int wmain(int argc, wchar_t** argv) {
    ConsoleFont consoleFont;
    setupConsole();
    banner();
    if (argc == 2 && wcscmp(argv[1], L"--preview") == 0) { preview(); return 0; }
    if (argc == 2 && (wcscmp(argv[1], L"--help") == 0 || wcscmp(argv[1], L"-h") == 0)) {
        std::wcout << L"\n  rice-loader.exe                         Start the loader\n"
                   << L"  rice-loader.exe --preview               Preview the console design\n"
                   << L"  rice-loader.exe --check-runtime [path]  Check embedded client extraction\n\n";
        return 0;
    }

    soju::Paths paths = soju::Resources::resolve();
    const bool checkRuntime = argc >= 2 && wcscmp(argv[1], L"--check-runtime") == 0;
    if (checkRuntime && argc == 3) paths.runtime = argv[2];
    soju::Logger::init(paths.logs, false);
    soju::Logger::info(L"Rice Slinky injector starting");

    stage(1, L"Runtime");
    status("Preparing embedded client...");
    if (!checkRuntime) soju::Resources::migrateFromJade(paths);
    if (!soju::Resources::materializeBundledRuntime(paths)) {
        status("Failed to extract embedded runtime.", false);
        if (!checkRuntime) waitExit();
        return 1;
    }
    if (checkRuntime) {
        std::wstring payload = paths.runtime + L"\\RiceNative.dll";
        std::wcout << L"\nRice Slinky payload: " << payload << L"\n";
        return soju::Resources::fileExists(payload) ? 0 : 1;
    }

    confirmed(L"Client ready");
    stage(2, L"Access");
    std::wstring licensePath = paths.dataRoot + L"\\license.dat";
    soju::AuthManager auth;
    const bool accepted = soju::promptForLicense(readFileUtf16(licensePath),
        [](bool, std::wstring& key) {
            std::wcout << margin(4) << L"License key " << dim() << L"(hidden; Ctrl+V to paste)"
                       << reset() << L": " << std::flush;
            struct InputMode {
                HANDLE input = GetStdHandle(STD_INPUT_HANDLE);
                DWORD original = 0;
                bool console = GetConsoleMode(input, &original) != FALSE;
                InputMode() { if (console) SetConsoleMode(input, (original | ENABLE_LINE_INPUT | ENABLE_PROCESSED_INPUT) & ~ENABLE_ECHO_INPUT); }
                ~InputMode() { if (console) SetConsoleMode(input, original); }
            } inputMode;
            const bool read = static_cast<bool>(std::getline(std::wcin, key));
            std::wcout << L"\n";
            return read;
        },
        [&](const std::wstring& key) {
            status("Checking license...");
            return auth.authenticateLicense(key);
        },
        [&](const std::wstring& key) { writeFileUtf16(licensePath, key); },
        [](const std::wstring& message) { status(narrow(message), false); });
    if (!accepted) return 1;

    confirmed(L"License accepted");
    stage(3, L"Minecraft");
    status("Looking for Minecraft 1.8.9...");
    std::vector<std::wstring> notes;
    std::vector<soju::GameProcess> games;
    for (int i = 0; i < 45; ++i) {
        notes.clear();
        games = soju::Process::detectGames(notes);
        if (!games.empty()) break;
        std::this_thread::sleep_for(std::chrono::milliseconds(300));
    }
    if (games.empty()) {
        status("Minecraft 1.8.9 was not found. Open the game and try again.", false);
        waitExit();
        return 1;
    }
    soju::GameProcess game = games.front();
    status("Target: " + narrow(game.environment) + " PID " + std::to_string(game.pid));

    confirmed(L"Minecraft connected");

    std::wstring payload = soju::Resources::locate(paths.runtime, L"RiceNative.dll");
    if (payload.empty()) {
        status("Rice Slinky runtime is missing.", false);
        waitExit();
        return 1;
    }

    stage(4, L"Launch");
    status("Starting Rice Slinky...");
    soju::LoadResult loaded = soju::Loader::runNative(paths.runtime, game.pid, [](const std::wstring& line) {
        if (!line.empty()) {
            std::wcout << margin(4) << dim() << L"· " << line << reset() << L"\n";
        }
    });
    if (!loaded.success) {
        status("Injection failed: " + narrow(loaded.message), false);
        waitExit();
        return 1;
    }
    showSuccessPage();
    waitExit();
    return 0;
}
