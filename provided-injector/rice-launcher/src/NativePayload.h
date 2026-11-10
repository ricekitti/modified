#pragma once
#include <windows.h>
#include <compressapi.h>
#include <cstring>
#include <vector>

namespace soju {
// Resource 205: magic followed by a Windows Compression API LZMS stream.
// Old launchers/build reports used a raw PE image; continue to decode those too.
inline bool decodeNativePayload(const void* source, size_t length, std::vector<unsigned char>& output) {
    output.clear();
    constexpr size_t maximum = 256u * 1024u * 1024u;
    if (!source || length < 8 || length > maximum) return false;
    const auto* bytes = static_cast<const unsigned char*>(source);
    if (std::memcmp(bytes, "RSLZ0001", 8) != 0) {
        if (length < 64 || bytes[0] != 'M' || bytes[1] != 'Z') return false;
        output.assign(bytes, bytes + length);
        return true;
    }
    if (length == 8) return false;
    DECOMPRESSOR_HANDLE handle = nullptr;
    if (!CreateDecompressor(COMPRESS_ALGORITHM_LZMS, nullptr, &handle)) return false;
    struct Cleanup { DECOMPRESSOR_HANDLE handle; ~Cleanup() { CloseDecompressor(handle); } } cleanup{handle};
    SIZE_T size = 0;
    BOOL measured = Decompress(handle, bytes + 8, length - 8, nullptr, 0, &size);
    if ((!measured && GetLastError() != ERROR_INSUFFICIENT_BUFFER) || size < 64 || size > maximum) return false;
    std::vector<unsigned char> decoded(size);
    SIZE_T written = 0;
    if (!Decompress(handle, bytes + 8, length - 8, decoded.data(), decoded.size(), &written) || written != size ||
        decoded[0] != 'M' || decoded[1] != 'Z') return false;
    output.swap(decoded);
    return true;
}
}
