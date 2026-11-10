#include "Resources.h"
#include "NativePayload.h"
#include "Logger.h"
#include "resource.h"

#include <windows.h>
#include <shlobj.h>
#include <shlwapi.h>
#include <shellapi.h>
#include <vector>
#include <filesystem>
#include <fstream>
#include <cstring>
#include <cstdint>
#include <compressapi.h>

#pragma comment(lib, "shell32.lib")
#pragma comment(lib, "shlwapi.lib")

namespace soju {

bool Resources::fileExists(const std::wstring& path) {
    DWORD a = GetFileAttributesW(path.c_str());
    return a != INVALID_FILE_ATTRIBUTES && !(a & FILE_ATTRIBUTE_DIRECTORY);
}

bool Resources::dirExists(const std::wstring& path) {
    DWORD a = GetFileAttributesW(path.c_str());
    return a != INVALID_FILE_ATTRIBUTES && (a & FILE_ATTRIBUTE_DIRECTORY);
}

static std::wstring knownFolder(REFKNOWNFOLDERID id) {
    PWSTR raw = nullptr;
    std::wstring result;
    if (SUCCEEDED(SHGetKnownFolderPath(id, 0, nullptr, &raw)) && raw) result = raw;
    if (raw) CoTaskMemFree(raw);
    return result;
}

static void ensureDirectory(const std::wstring& path) {
    if (!path.empty()) CreateDirectoryW(path.c_str(), nullptr);
}

static std::wstring exeDirectory() {
    wchar_t buf[MAX_PATH];
    DWORD n = GetModuleFileNameW(nullptr, buf, MAX_PATH);
    std::wstring path(buf, n);
    size_t slash = path.find_last_of(L"\\/");
    return slash == std::wstring::npos ? L"." : path.substr(0, slash);
}

Paths Resources::resolve() {
    Paths p;
    p.exeDir = exeDirectory();
    std::wstring local = knownFolder(FOLDERID_LocalAppData);
    if (local.empty()) local = p.exeDir; // last resort
    p.dataRoot = local + L"\\RiceSlinky";
    p.logs    = p.dataRoot + L"\\logs";
    p.cache   = p.dataRoot + L"\\cache";
    p.configs = p.dataRoot + L"\\configs";
    p.data    = p.dataRoot + L"\\data";
    p.runtime = p.dataRoot + L"\\runtime";

    for (const std::wstring* d : { &p.dataRoot, &p.logs, &p.cache, &p.configs, &p.data, &p.runtime })
        ensureDirectory(*d);

    std::wstring roaming = knownFolder(FOLDERID_RoamingAppData);
    if (!roaming.empty()) {
        std::wstring clientRoot = roaming + L"\\.soju";
        ensureDirectory(clientRoot);
        for (const wchar_t* child : { L"profiles", L"fonts", L"scripts", L"packet-debug" }) {
            ensureDirectory(clientRoot + L"\\" + child);
        }
    }
    return p;
}

bool Resources::migrateFromJade(const Paths& p) {
    std::wstring local = knownFolder(FOLDERID_LocalAppData);
    if (local.empty()) return false;
    std::wstring oldRoot = local + L"\\Jade";
    if (!dirExists(oldRoot)) return false;

    // Only migrate if the new configs folder is still empty, so we never clobber newer settings.
    WIN32_FIND_DATAW fd;
    std::wstring probe = p.configs + L"\\*";
    HANDLE h = FindFirstFileW(probe.c_str(), &fd);
    bool configsEmpty = true;
    if (h != INVALID_HANDLE_VALUE) {
        do {
            if (wcscmp(fd.cFileName, L".") && wcscmp(fd.cFileName, L"..")) { configsEmpty = false; break; }
        } while (FindNextFileW(h, &fd));
        FindClose(h);
    }
    if (!configsEmpty) return false;

    // Non-destructive recursive copy old -> new\configs (SHFileOperation needs double-null-terminated paths).
    std::wstring from = oldRoot;
    from.push_back(L'\0'); from.push_back(L'\0');
    std::wstring to = p.configs;
    to.push_back(L'\0'); to.push_back(L'\0');

    SHFILEOPSTRUCTW op = {};
    op.wFunc = FO_COPY;
    op.pFrom = from.c_str();
    op.pTo = to.c_str();
    op.fFlags = FOF_NOCONFIRMATION | FOF_NOCONFIRMMKDIR | FOF_SILENT | FOF_NOERRORUI;
    int rc = SHFileOperationW(&op);
    if (rc == 0) {
        Logger::info(L"Migrated existing settings from %LOCALAPPDATA%\\Jade (left intact).");
        return true;
    }
    Logger::warn(L"Could not migrate old Jade settings (code " + std::to_wstring(rc) + L").");
    return false;
}

static bool extractPayload(const Paths& p) {
    HMODULE module = GetModuleHandleW(nullptr);
    HRSRC resource = FindResourceW(module, MAKEINTRESOURCEW(IDR_SOJU_PAYLOAD), RT_RCDATA);
    if (!resource) return false;
    HGLOBAL loaded = LoadResource(module, resource);
    const void* compressed = loaded ? LockResource(loaded) : nullptr;
    DWORD compressedSize = SizeofResource(module, resource);
    if (!compressed || !compressedSize) return false;
    DECOMPRESSOR_HANDLE decompressor = nullptr;
    if (!CreateDecompressor(COMPRESS_ALGORITHM_LZMS, nullptr, &decompressor)) return false;
    struct DecompressionHandle {
        DECOMPRESSOR_HANDLE handle;
        ~DecompressionHandle() { CloseDecompressor(handle); }
    } compressionHandle{decompressor};
    SIZE_T expandedSize = 0;
    BOOL measured = Decompress(decompressor, compressed, compressedSize, nullptr, 0, &expandedSize);
    if ((!measured && GetLastError() != ERROR_INSUFFICIENT_BUFFER) ||
        expandedSize < 12 || expandedSize > 256 * 1024 * 1024) return false;
    std::vector<unsigned char> expanded(expandedSize);
    SIZE_T written = 0;
    if (!Decompress(decompressor, compressed, compressedSize, expanded.data(), expanded.size(), &written) ||
        written != expanded.size()) return false;
    const auto* cursor = expanded.data();
    size_t remaining = expanded.size();
    if (!cursor || remaining < 12 || std::memcmp(cursor, "SJRT0001", 8) != 0) return false;
    cursor += 8;
    remaining -= 8;
    const auto readSize = [&](uint32_t& value) {
        if (remaining < sizeof(value)) return false;
        std::memcpy(&value, cursor, sizeof(value));
        cursor += sizeof(value);
        remaining -= sizeof(value);
        return true;
    };
    uint32_t count = 0;
    if (!readSize(count) || count == 0 || count > 10000) return false;
    std::filesystem::path root(p.runtime);
    for (uint32_t i = 0; i < count; ++i) {
        uint32_t pathLength = 0, fileLength = 0;
        if (!readSize(pathLength) || !pathLength || pathLength > 32767 || pathLength > remaining) return false;
        std::string name(reinterpret_cast<const char*>(cursor), pathLength);
        cursor += pathLength;
        remaining -= pathLength;
        if (name.find('\0') != std::string::npos || name.find(':') != std::string::npos ||
            name.find('\\') != std::string::npos) return false;
        auto relative = std::filesystem::u8path(name);
        if (relative.is_absolute() || relative.has_root_path()) return false;
        for (const auto& part : relative) if (part == "..") return false;
        if (!readSize(fileLength) || fileLength > remaining) return false;
        auto target = root / relative;
        std::error_code error;
        std::filesystem::create_directories(target.parent_path(), error);
        if (error) return false;
        std::ofstream output(target, std::ios::binary | std::ios::trunc);
        output.write(reinterpret_cast<const char*>(cursor), fileLength);
        output.close();
        if (!output) return false;
        cursor += fileLength;
        remaining -= fileLength;
    }
    return remaining == 0 && Resources::fileExists(root.wstring() + L"\\soju.jar") &&
        Resources::fileExists(root.wstring() + L"\\soju-bridge.dll");
}

bool Resources::materializeBundledRuntime(const Paths& p) {
    HANDLE mutex = CreateMutexW(nullptr, FALSE, L"Local\\rice-slinky-runtime-extraction");
    if (!mutex) return false;
    DWORD wait = WaitForSingleObject(mutex, 30000);
    if (wait != WAIT_OBJECT_0 && wait != WAIT_ABANDONED) { CloseHandle(mutex); return false; }
    struct Lock {
        HANDLE handle;
        ~Lock() { ReleaseMutex(handle); CloseHandle(handle); }
    } lock{mutex};
    CreateDirectoryW(p.runtime.c_str(), nullptr);
    HMODULE module = GetModuleHandleW(nullptr);
    HRSRC resource = FindResourceW(module, MAKEINTRESOURCEW(IDR_RICE_SLINKY_NATIVE), RT_RCDATA);
    HGLOBAL loaded = resource ? LoadResource(module, resource) : nullptr;
    const void* bytes = loaded ? LockResource(loaded) : nullptr;
    DWORD size = resource ? SizeofResource(module, resource) : 0;
    if (!bytes || !size) return false;
    std::vector<unsigned char> native;
    if (!decodeNativePayload(bytes, size, native)) return false;
    std::wstring target = p.runtime + L"\\RiceNative.dll";
    HANDLE file = CreateFileW(target.c_str(), GENERIC_WRITE, 0, nullptr, CREATE_ALWAYS,
                              FILE_ATTRIBUTE_NORMAL, nullptr);
    if (file == INVALID_HANDLE_VALUE) return false;
    DWORD written = 0;
    BOOL saved = WriteFile(file, native.data(), static_cast<DWORD>(native.size()), &written, nullptr);
    CloseHandle(file);
    bool ok = saved && written == native.size();
    if (ok) Logger::info(L"Rice Slinky runtime extracted to " + target);
    return ok;
}

std::wstring Resources::locate(const std::wstring& exeDir, const std::wstring& fileName) {
    const std::wstring candidates[] = {
        exeDir + L"\\" + fileName,
        exeDir + L"\\resources\\" + fileName,
        exeDir + L"\\required\\" + fileName,
    };
    for (const auto& c : candidates)
        if (fileExists(c)) return c;
    return L"";
}

} // namespace soju
