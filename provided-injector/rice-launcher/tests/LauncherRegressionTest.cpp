#include "Loader.h"
#include "Integrity.h"
#include "JavaRuntime.h"
#include <fstream>
#include <iostream>
#include <stdexcept>

static void check(bool condition, const char* message) {
    if (!condition) throw std::runtime_error(message);
}

int wmain(int argc, wchar_t** argv) {
    if (argc > 1 && wcscmp(argv[1], L"--runtime") == 0) {
        wchar_t executable[32768];
        GetModuleFileNameW(nullptr, executable, 32768);
        std::wstring directory(executable);
        directory.resize(directory.find_last_of(L"\\/"));
        bool found = false;
        auto runtime = soju::JavaRuntime::findBest(17, found);
        if (!found || runtime.home != directory + L"\\runtime") return 1;
        std::wcout << L"Bundled runtime selected: " << runtime.javaExe << L"\n";
        return 0;
    }
    // Acts as a local loader fixture; no game attachment or network calls occur.
    if (argc > 1) {
        const int mode = _wtoi(argv[argc - 1]);
        if (mode == 1) std::cout << "client is ready\r\n";
        if (mode == 2) std::cout << "accepted the agent";
        if (mode == 3) std::cout << "no readiness confirmation";
        if (mode == 4) std::cout << "client is ready";
        if (mode == 5) std::cout << "soju: IOException: fixture startup failure";
        return mode >= 4 ? 1 : 0;
    }
    try {
        wchar_t exe[MAX_PATH];
        check(GetModuleFileNameW(nullptr, exe, MAX_PATH) != 0, "Executable path");
        std::wstring directory(exe);
        directory.resize(directory.find_last_of(L"\\/"));
        for (DWORD mode = 1; mode <= 4; ++mode) {
            int lines = 0;
            auto result = soju::Loader::run(exe, L"fixture", directory, mode,
                [&](const std::wstring&) { ++lines; });
            check(result.success == (mode <= 2), "Readiness/exit code handling");
            check(lines == 1, "Output must be emitted exactly once");
        }
        check(soju::Loader::run(exe, L"fixture", directory, 2, {}).success,
              "Readiness without callback");
        auto failedClient = soju::Loader::run(exe, L"fixture", directory, 5, {});
        check(!failedClient.success && failedClient.message == L"soju: IOException: fixture startup failure",
              "Client failure message preserved");
        auto missing = soju::Loader::run(directory + L"\\nonexistent-fixture.exe", L"fixture", directory, 1, {});
        check(!missing.success && missing.message.find(L"error 2)") != std::wstring::npos,
              "CreateProcess error preserved");

        wchar_t tempDir[MAX_PATH], tempFile[MAX_PATH];
        check(GetTempPathW(MAX_PATH, tempDir) != 0, "Temp directory");
        check(GetTempFileNameW(tempDir, L"sjt", 0, tempFile) != 0, "Temp file");
        struct Cleanup { const wchar_t* path; ~Cleanup() { DeleteFileW(path); } } cleanup{tempFile};
        { std::ofstream out(tempFile, std::ios::binary); out << "abc"; }
        check(soju::Integrity::sha256File(tempFile) ==
              L"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
              "SHA-256 known vector");
        check(soju::Integrity::sha256File(directory + L"\\nonexistent-fixture.bin").empty(),
              "Missing files must not produce hashes");
        { std::ofstream out(tempFile); out << "invalid manifest line"; }
        check(!soju::Integrity::verifyManifest(directory, tempFile).ok, "Malformed manifest rejected");
        { std::ofstream out(tempFile); out << " # comment only\n"; }
        check(!soju::Integrity::verifyManifest(directory, tempFile).ok, "Empty manifest rejected");
        std::cout << "Launcher regression checks passed\n";
        return 0;
    } catch (const std::exception& failure) {
        std::cerr << failure.what() << '\n';
        return 1;
    }
}
