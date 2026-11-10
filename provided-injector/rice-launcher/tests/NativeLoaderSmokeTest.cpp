#include "Loader.h"
#include <iostream>

int wmain(int argc, wchar_t** argv) {
    if (argc != 4) return 2;
    bool expectedReady = wcscmp(argv[3], L"ready") == 0 || wcscmp(argv[3], L"delayed") == 0;
    bool exits = wcscmp(argv[3], L"exit") == 0;
    auto result = soju::Loader::runNative(argv[1], wcstoul(argv[2], nullptr, 10),
        [](const std::wstring& line) { std::wcout << line << L"\n"; });
    if (result.success != expectedReady) return 1;
    if (!expectedReady && result.message.find(exits ? L"Minecraft exited during soju initialization" : L"fixture startup failure: exact message preserved") == std::wstring::npos)
        return 1;
    std::cout << "Native launcher integration check passed\n";
    return 0;
}
