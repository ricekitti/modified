// Readable reimplementations of protector-obfuscated engine functions (0x4c270 - 0x4cc40):
// the timer and the profile-folder file helpers. In the image these call Windows through the protector's
// encrypted import slots; the logic around those calls is intact and is reproduced here 1:1, the API behind
// each slot is named from its arguments, the strings it uses and the error codes compared afterwards.
//
// Error codes (shared with the profile code in math/e_b.cpp):
//   0x3ec no user profile dir   0x3ed out of memory        0x3ee path too long
//   0x3ef file size failed      0x3f0 open failed          0x3f1 read failed
//   0x3f2 write failed          0x3f3 delete failed        0x3f4 file exists
//   0x3f5 list failed           0x3f6 mkdir failed         0x3f7 list next failed
//   0x3fa short read            0xfa0 open folder failed
#include "slinky/engine.h"
#include "slinky/win32.h"

FN(unsigned long long, libc_wcslen, (const wchar_t *s));    // wcslen
FN(unsigned long long, libc_strlen, (const char *s));        // strlen
extern "C" int libc_snprintf(char *buf, unsigned long long n, const char *fmt, ...);   // snprintf
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));   // calloc
FN(void *, libc_malloc, (unsigned long long n));            // malloc
FN(void, libc_free, (void *p));                           // free
FN(int, format_string, (wchar_t *buf, unsigned long long n, const wchar_t *fmt, ...));     // _snwprintf
FN(int, snwprintf_s, (char *buf, unsigned long long n, unsigned long long cnt, const char *fmt, ...));   // _snprintf_s, "%S" = wide arg
extern "C" int wcsncpy_s(wchar_t *dst, size_t dst_size, const wchar_t *src, size_t count);

// ---- seconds since an arbitrary start (QueryPerformanceCounter / cached frequency) ----
extern "C" double data_304bd0;   // cached counter frequency
extern "C" double time_now() {
    long long v;
    if (data_304bd0 == 0.0) {
        QueryPerformanceFrequency(&v);
        data_304bd0 = (double)v;
    }
    QueryPerformanceCounter(&v);
    return (double)v / data_304bd0;
}

// ---- *path = copy of %USERPROFILE% ----
extern "C" int settings_path(wchar_t **path) {
    const wchar_t *home = _wgetenv(L"USERPROFILE");
    if (!home || !home[0]) return 0x3ec;
    unsigned long long n = libc_wcslen(home);
    wchar_t *copy = (wchar_t *)libc_calloc(n + 1, sizeof(wchar_t));
    if (!copy) return 0x3ed;
    wcsncpy_s(copy, n + 1, home, n);
    *path = copy;
    return 0;
}

// ---- create the directory *path (already existing is fine) ----
extern "C" int settings_path_ensure(wchar_t **path) {
    if (CreateDirectoryW(*path, 0)) return 0;
    return GetLastError() == ERROR_ALREADY_EXISTS ? 0 : 0x3f6;
}

// ---- delete the file *path ----
extern "C" int delete_file_path(wchar_t **path) { return DeleteFileW(*path) ? 0 : 0x3f3; }

// ---- open *path in Explorer ----
extern "C" int open_path_explorer(wchar_t **path) {
    long long r = (long long)ShellExecuteW(0, L"explore", *path, 0, 0, SW_SHOWNORMAL);
    return r >= 0x21 ? 0 : 0xfa0;
}

// ---- call cb(&info, ctx) for every file (not directory) in *dir; info.age = seconds since last write
// (-1 when the write time is in the future). A non-zero cb result stops and is returned. ----
struct FileInfo { const char *name; long long age; };
typedef int (*FileCb)(FileInfo *info, void *ctx);
extern "C" int find_files(wchar_t **dir, void *ctx, FileCb cb) {
    char name[0x104];   // the image keeps this buffer in its TLS block; the callback copies the name at once
    unsigned long long dlen = libc_wcslen(*dir);
    wchar_t *pattern = (wchar_t *)libc_calloc(dlen + 3, sizeof(wchar_t));
    if (!pattern) return 0x3ed;
    for (unsigned long long i = 0; i < dlen; i++) pattern[i] = (*dir)[i];
    pattern[dlen] = L'\\';
    pattern[dlen + 1] = L'*';
    pattern[dlen + 2] = 0;
    int r;
    WIN32_FIND_DATAW fd;
    HANDLE h = FindFirstFileW(pattern, &fd);
    if (h == INVALID_HANDLE) {
        r = GetLastError() == ERROR_FILE_NOT_FOUND ? 0 : 0x3f5;   // empty folder is fine
        libc_free(pattern);
        return r;
    }
    FILETIME now;
    GetSystemTimeAsFileTime(&now);
    unsigned long long t_now = (unsigned long long)now.hi << 32 | now.lo;
    for (;;) {
        if (!(fd.attributes & FILE_ATTRIBUTE_DIRECTORY)) {
            unsigned long long ni = 0;
            for (; fd.name[ni] && ni < 0x103; ni++) name[ni] = (char)fd.name[ni];
            name[ni] = 0;
            FileInfo info;
            info.name = name;
            info.age = -1;
            unsigned long long t = (unsigned long long)fd.written.hi << 32 | fd.written.lo;
            if (t - 1 < t_now) info.age = (long long)((t_now - t) / 10000000);   // 100 ns ticks -> seconds
            r = cb(&info, ctx);
            if (r) break;
        }
        if (!FindNextFileW(h, &fd)) {
            r = GetLastError() == ERROR_NO_MORE_FILES ? 0 : 0x3f7;
            break;
        }
    }
    FindClose(h);
    libc_free(pattern);
    return r;
}

// ---- *data = whole file *path, NUL-terminated (malloc) ----
extern "C" int read_text_file(wchar_t **path, char **data) {
    HANDLE h = CreateFileW(*path, GENERIC_READ, FILE_SHARE_READ, 0, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
    if (h == INVALID_HANDLE) return 0x3f0;
    int r;
    DWORD size = GetFileSize(h, 0);
    if (size == INVALID_FILE_SIZE) {
        r = 0x3ef;
    } else {
        char *buf = (char *)libc_malloc(size + 1);
        if (!buf) return 0x3ed;   // (the image returns here without closing the handle)
        r = 0;
        for (unsigned long long got = 0; got < size;) {
            DWORD n;
            if (!ReadFile(h, buf + got, (DWORD)(size - got), &n, 0)) { r = 0x3f1; break; }
            if (!n) { r = 0x3fa; break; }
            got += n;
        }
        if (r) {
            libc_free(buf);
        } else {
            buf[size] = 0;
            *data = buf;
        }
    }
    CloseHandle(h);
    return r;
}

// ---- write text to *path (overwrite = replace an existing file, else fail with 0x3f4 if it exists) ----
extern "C" int write_text_file(wchar_t **path, const char *text, bool overwrite) {
    HANDLE h = CreateFileW(*path, GENERIC_WRITE, 0, 0, overwrite ? CREATE_ALWAYS : CREATE_NEW,
                           FILE_ATTRIBUTE_NORMAL, 0);
    if (h == INVALID_HANDLE) return GetLastError() == ERROR_FILE_EXISTS ? 0x3f4 : 0x3f0;
    unsigned long long len = libc_strlen(text), done = 0;
    int r = 0;
    while (done < len) {
        DWORD n;
        BOOL ok = WriteFile(h, text + done, (DWORD)(len - done), &n, 0);
        done += n;
        if (!ok) {
            r = 0x3f2;
            break;
        }
    }
    CloseHandle(h);
    return r;
}
