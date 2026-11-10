#include "Integrity.h"
#include "Logger.h"

#include <windows.h>
#include <bcrypt.h>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <cctype>

#pragma comment(lib, "bcrypt.lib")

#ifndef STATUS_SUCCESS
#define STATUS_SUCCESS ((NTSTATUS)0x00000000L)
#endif

namespace soju {

std::wstring Integrity::sha256File(const std::wstring& path) {
    HANDLE file = CreateFileW(path.c_str(), GENERIC_READ, FILE_SHARE_READ, nullptr,
                              OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, nullptr);
    if (file == INVALID_HANDLE_VALUE) return L"";

    BCRYPT_ALG_HANDLE alg = nullptr;
    BCRYPT_HASH_HANDLE hash = nullptr;
    std::wstring result;
    std::vector<BYTE> hashBuf(32);

    do {
        if (BCryptOpenAlgorithmProvider(&alg, BCRYPT_SHA256_ALGORITHM, nullptr, 0) != STATUS_SUCCESS) break;
        if (BCryptCreateHash(alg, &hash, nullptr, 0, nullptr, 0, 0) != STATUS_SUCCESS) break;

        std::vector<BYTE> buffer(1 << 16);
        DWORD read = 0;
        bool failed = false;
        for (;;) {
            if (!ReadFile(file, buffer.data(), (DWORD)buffer.size(), &read, nullptr)) {
                failed = true;
                break;
            }
            if (read == 0) break;
            if (BCryptHashData(hash, buffer.data(), read, 0) != STATUS_SUCCESS) { failed = true; break; }
        }
        if (failed) break;
        if (BCryptFinishHash(hash, hashBuf.data(), (ULONG)hashBuf.size(), 0) != STATUS_SUCCESS) break;

        wchar_t hex[65];
        for (int i = 0; i < 32; ++i) swprintf(hex + i * 2, 3, L"%02x", hashBuf[i]);
        result.assign(hex, 64);
    } while (false);

    if (hash) BCryptDestroyHash(hash);
    if (alg)  BCryptCloseAlgorithmProvider(alg, 0);
    CloseHandle(file);
    return result;
}

IntegrityResult Integrity::verifyManifest(const std::wstring& baseDir, const std::wstring& manifestPath) {
    IntegrityResult result;
    std::ifstream in(manifestPath);
    if (!in) {
        result.ok = true;
        result.reason = L"manifest-absent";
        return result;
    }

    std::string line;
    bool hasEntries = false;
    while (std::getline(in, line)) {
        const auto start = line.find_first_not_of(" \t\r");
        if (start == std::string::npos || line[start] == '#') continue;
        std::istringstream ss(line);
        std::string expected, name, extra;
        ss >> expected >> name;
        if (expected.size() != 64 || name.empty() || (ss >> extra) ||
            !std::all_of(expected.begin(), expected.end(), [](unsigned char c) { return std::isxdigit(c) != 0; })) {
            result.ok = false;
            result.reason = L"invalid manifest";
            return result;
        }
        hasEntries = true;

        std::wstring wname(name.begin(), name.end());
        std::wstring wexpected(expected.begin(), expected.end());
        std::transform(wexpected.begin(), wexpected.end(), wexpected.begin(), towlower);

        std::wstring full = baseDir + L"\\" + wname;
        std::wstring actual = sha256File(full);
        if (actual.empty()) {
            result.ok = false; result.failedFile = wname; result.reason = L"missing";
            return result;
        }
        if (actual != wexpected) {
            result.ok = false; result.failedFile = wname; result.reason = L"hash mismatch";
            Logger::error(L"Integrity: " + wname + L" expected " + wexpected + L" but got " + actual);
            return result;
        }
        Logger::debug(L"Integrity OK: " + wname);
    }
    if (in.bad() || !hasEntries) {
        result.ok = false;
        result.reason = in.bad() ? L"manifest read failed" : L"empty manifest";
    }
    return result;
}

} // namespace soju
