#include "NativePayload.h"
#include <filesystem>
#include <fstream>
#include <iostream>
#include <iterator>
#include <stdexcept>

static std::vector<unsigned char> read(const wchar_t* path) {
    std::ifstream file(std::filesystem::path(path), std::ios::binary);
    if (!file) throw std::runtime_error("Missing test input");
    return {std::istreambuf_iterator<char>(file), std::istreambuf_iterator<char>()};
}
static void require(bool value, const char* message) { if (!value) throw std::runtime_error(message); }
int wmain(int argc, wchar_t** argv) {
    try {
        require(argc == 3, "Need raw DLL and compressed resource");
        auto raw = read(argv[1]), packed = read(argv[2]);
        std::vector<unsigned char> decoded;
        require(soju::decodeNativePayload(raw.data(), raw.size(), decoded) && decoded == raw, "Legacy raw resource changed");
        require(soju::decodeNativePayload(packed.data(), packed.size(), decoded) && decoded == raw, "Compressed resource differs from original DLL");
        require(!soju::decodeNativePayload(nullptr, 0, decoded) && decoded.empty(), "Null input accepted");
        require(!soju::decodeNativePayload(packed.data(), 7, decoded), "Truncated magic accepted");
        require(!soju::decodeNativePayload(packed.data(), 8, decoded), "Missing stream accepted");
        require(!soju::decodeNativePayload(packed.data(), packed.size() / 2, decoded), "Truncated stream accepted");
        std::vector<unsigned char> bad(128, 0);
        require(!soju::decodeNativePayload(bad.data(), bad.size(), decoded), "Invalid PE accepted");
        std::memcpy(bad.data(), "RSLZ0001", 8);
        require(!soju::decodeNativePayload(bad.data(), bad.size(), decoded), "Corrupt stream accepted");
        std::cout << "PASS: native LZMS decoder byte-exact roundtrip, legacy raw support, null/truncated/corrupt stream rejection\n";
        return 0;
    } catch (const std::exception& error) { std::cerr << error.what() << '\n'; return 1; }
}
