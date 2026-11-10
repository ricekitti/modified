// macOS file + timer layer: the POSIX counterpart of src/engine/protected/files.cpp (Windows).
// Same exported symbols, signatures and error codes, so the shared engine links and behaves identically.
//
// The engine builds its paths as UTF-16 wchar_t with '\\' separators (see keycodes_strings.cpp,
// settings_store.cpp) because that is what the original Windows image did. The Mac dylib is compiled with
// -fshort-wchar (wchar_t == 2 bytes, matching the UTF-16 data tables), and these leaf operations translate
// each path to UTF-8 with '/' separators at the open()/mkdir()/opendir() boundary. That keeps every path
// producer shared and confines the OS difference to this file. Settings still live under ~/.slinky/.
//
// Error codes (shared with the profile code in math/e_b.cpp):
//   0x3ec no user home dir   0x3ed out of memory       0x3ee path too long
//   0x3ef file size failed   0x3f0 open failed         0x3f1 read failed
//   0x3f2 write failed       0x3f3 delete failed       0x3f4 file exists
//   0x3f5 list failed        0x3f6 mkdir failed         0x3f7 list next failed
//   0x3fa short read         0xfa0 open folder failed
#include <dirent.h>
#include <fcntl.h>
#include <spawn.h>
#include <sys/stat.h>
#include <time.h>
#include <unistd.h>
#include <errno.h>
#include <stdio.h>

static_assert(sizeof(wchar_t) == 2, "build the macOS dylib with -fshort-wchar so wchar_t matches the UTF-16 data tables");

extern char **environ;

// the engine's allocator: buffers returned to callers are freed by the engine with libc_free, so they must
// come from the same allocator (init.cpp load_settings frees read_text_file's buffer; free_path frees paths).
extern "C" void *libc_malloc(unsigned long long n);
extern "C" void *libc_calloc(unsigned long long n, unsigned long long size);

// ---- path translation (UTF-16 '\\' path -> UTF-8 '/' path) ----
static bool to_posix(const wchar_t *w, char *out, unsigned long outsz) {
    unsigned long o = 0;
    for (unsigned long i = 0; w[i]; i++) {
        unsigned int cp = (unsigned short)w[i];
        if (cp >= 0xD800 && cp <= 0xDBFF) {                       // UTF-16 high surrogate
            unsigned int lo = (unsigned short)w[i + 1];
            if (lo >= 0xDC00 && lo <= 0xDFFF) { cp = 0x10000 + ((cp - 0xD800) << 10) + (lo - 0xDC00); i++; }
        }
        if (cp == '\\') cp = '/';                                  // the engine's separator -> POSIX
        if (cp < 0x80) {
            if (o + 1 >= outsz) return false;
            out[o++] = (char)cp;
        } else if (cp < 0x800) {
            if (o + 2 >= outsz) return false;
            out[o++] = (char)(0xC0 | (cp >> 6));
            out[o++] = (char)(0x80 | (cp & 0x3F));
        } else if (cp < 0x10000) {
            if (o + 3 >= outsz) return false;
            out[o++] = (char)(0xE0 | (cp >> 12));
            out[o++] = (char)(0x80 | ((cp >> 6) & 0x3F));
            out[o++] = (char)(0x80 | (cp & 0x3F));
        } else {
            if (o + 4 >= outsz) return false;
            out[o++] = (char)(0xF0 | (cp >> 18));
            out[o++] = (char)(0x80 | ((cp >> 12) & 0x3F));
            out[o++] = (char)(0x80 | ((cp >> 6) & 0x3F));
            out[o++] = (char)(0x80 | (cp & 0x3F));
        }
    }
    if (o >= outsz) return false;
    out[o] = 0;
    return true;
}

// UTF-8 -> UTF-16, allocated with the engine allocator (for settings_path to hand back a wchar_t home dir)
static int from_utf8(const char *s, wchar_t **out) {
    auto decode = [](const char *p, unsigned int &cp) -> int {
        unsigned char c = (unsigned char)p[0];
        int len; if (c < 0x80) { cp = c; len = 1; }
        else if ((c >> 5) == 0x6) { cp = c & 0x1F; len = 2; }
        else if ((c >> 4) == 0xE) { cp = c & 0x0F; len = 3; }
        else if ((c >> 3) == 0x1E) { cp = c & 0x07; len = 4; }
        else { cp = 0xFFFD; return 1; }
        for (int k = 1; k < len; k++) { if ((p[k] & 0xC0) != 0x80) { cp = 0xFFFD; return 1; } cp = (cp << 6) | (p[k] & 0x3F); }
        return len;
    };
    unsigned long units = 0;
    for (unsigned long i = 0; s[i];) { unsigned int cp; i += decode(s + i, cp); units += (cp >= 0x10000) ? 2 : 1; }
    wchar_t *w = (wchar_t *)libc_calloc(units + 1, sizeof(wchar_t));
    if (!w) return 0x3ed;
    unsigned long o = 0;
    for (unsigned long i = 0; s[i];) {
        unsigned int cp; i += decode(s + i, cp);
        if (cp >= 0x10000) { cp -= 0x10000; w[o++] = (wchar_t)(0xD800 + (cp >> 10)); w[o++] = (wchar_t)(0xDC00 + (cp & 0x3FF)); }
        else w[o++] = (wchar_t)cp;
    }
    w[o] = 0;
    *out = w;
    return 0;
}

// ---- seconds since an arbitrary start (monotonic; QueryPerformanceCounter on Windows) ----
extern "C" double time_now() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (double)ts.tv_sec + (double)ts.tv_nsec / 1e9;
}

// ---- *path = copy of $HOME (the %USERPROFILE% counterpart; settings live under $HOME/.slinky) ----
extern "C" int settings_path(wchar_t **path) {
    const char *home = getenv("HOME");
    if (!home || !home[0]) return 0x3ec;
    return from_utf8(home, path);
}

// ---- create the directory *path (already existing is fine) ----
extern "C" int settings_path_ensure(wchar_t **path) {
    char p[4096];
    if (!to_posix(*path, p, sizeof p)) return 0x3ee;
    if (mkdir(p, 0755) == 0) return 0;
    return errno == EEXIST ? 0 : 0x3f6;
}

// ---- delete the file *path ----
extern "C" int delete_file_path(wchar_t **path) {
    char p[4096];
    if (!to_posix(*path, p, sizeof p)) return 0x3ee;
    return unlink(p) == 0 ? 0 : 0x3f3;
}

// ---- reveal *path in Finder (the "open in Explorer" counterpart) ----
extern "C" int open_path_explorer(wchar_t **path) {
    char p[4096];
    if (!to_posix(*path, p, sizeof p)) return 0x3ee;
    char *argv[] = { (char *)"open", p, nullptr };
    pid_t pid;
    if (posix_spawn(&pid, "/usr/bin/open", nullptr, nullptr, argv, environ) != 0) return 0xfa0;
    return 0;
}

// ---- call cb(&info, ctx) for every file (not directory) in *dir; info.age = seconds since last write
// (-1 when the write time is in the future). A non-zero cb result stops and is returned. ----
struct FileInfo { const char *name; long long age; };
typedef int (*FileCb)(FileInfo *info, void *ctx);
extern "C" int find_files(wchar_t **dir, void *ctx, FileCb cb) {
    char d[4096];
    if (!to_posix(*dir, d, sizeof d)) return 0x3ee;
    DIR *dp = opendir(d);
    if (!dp) return errno == ENOENT ? 0 : 0x3f5;                  // empty / absent folder is fine
    long long t_now = (long long)time(nullptr);
    int r = 0;
    char full[4352];
    struct dirent *ent;
    while ((ent = readdir(dp))) {
        const char *n = ent->d_name;
        if (n[0] == '.' && (n[1] == 0 || (n[1] == '.' && n[2] == 0))) continue;
        int fn = snprintf(full, sizeof full, "%s/%s", d, n);
        if (fn <= 0 || fn >= (int)sizeof full) continue;
        struct stat st;
        if (stat(full, &st) != 0) continue;
        if (S_ISDIR(st.st_mode)) continue;
        FileInfo info;
        info.name = n;
        long long mt = (long long)st.st_mtime;
        info.age = (mt - 1 < t_now) ? (t_now - mt) : -1;
        r = cb(&info, ctx);
        if (r) break;
    }
    closedir(dp);
    return r;
}

// ---- *data = whole file *path, NUL-terminated (engine allocator) ----
extern "C" int read_text_file(wchar_t **path, char **data) {
    char p[4096];
    if (!to_posix(*path, p, sizeof p)) return 0x3ee;
    int fd = open(p, O_RDONLY);
    if (fd < 0) return 0x3f0;
    struct stat st;
    int r;
    if (fstat(fd, &st) != 0) {
        r = 0x3ef;
    } else {
        unsigned long size = (unsigned long)st.st_size;
        char *buf = (char *)libc_malloc(size + 1);
        if (!buf) { close(fd); return 0x3ed; }
        r = 0;
        for (unsigned long got = 0; got < size;) {
            ssize_t n = read(fd, buf + got, size - got);
            if (n < 0) { r = 0x3f1; break; }
            if (n == 0) { r = 0x3fa; break; }
            got += (unsigned long)n;
        }
        if (r) { /* leak-free: free the buffer the engine will never see */ extern "C" void libc_free(void *); libc_free(buf); }
        else { buf[size] = 0; *data = buf; }
    }
    close(fd);
    return r;
}

// ---- write text to *path (overwrite = replace; else fail 0x3f4 if it exists) ----
extern "C" int write_text_file(wchar_t **path, const char *text, bool overwrite) {
    char p[4096];
    if (!to_posix(*path, p, sizeof p)) return 0x3ee;
    int fd = open(p, O_WRONLY | O_CREAT | (overwrite ? O_TRUNC : O_EXCL), 0644);
    if (fd < 0) return errno == EEXIST ? 0x3f4 : 0x3f0;
    unsigned long len = 0; while (text[len]) len++;
    int r = 0;
    for (unsigned long done = 0; done < len;) {
        ssize_t n = write(fd, text + done, len - done);
        if (n < 0) { r = 0x3f2; break; }
        done += (unsigned long)n;
    }
    close(fd);
    return r;
}
