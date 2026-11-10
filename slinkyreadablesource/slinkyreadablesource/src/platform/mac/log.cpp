// macOS diagnostics sink for the portable client.
//   init.cpp:slinky_logf -> slinky_log_write (declared in slinky/platform.h) -> here.
// Mirrors the Windows entry's sink (OutputDebugString + process stderr + a temp log): on macOS we write to
// stderr and append to ~/Library/Logs/RiceSlinky/slinky.log, so an unload problem stays visible after the
// game exits. No newline is added by the caller; we add exactly one.
#include "slinky/platform.h"

#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>

static void append_log_file(const char *line, unsigned long n) {
    const char *home = getenv("HOME");
    if (!home || !home[0]) return;
    char dir[1024];
    int d = snprintf(dir, sizeof dir, "%s/Library/Logs/RiceSlinky", home);
    if (d <= 0 || d >= (int)sizeof dir) return;
    mkdir(dir, 0755);                                   // fine if it already exists
    char path[1100];
    int p = snprintf(path, sizeof path, "%s/slinky.log", dir);
    if (p <= 0 || p >= (int)sizeof path) return;
    int fd = open(path, O_WRONLY | O_CREAT | O_APPEND, 0644);
    if (fd < 0) return;
    (void)!write(fd, line, n);
    close(fd);
}

extern "C" void slinky_log_write(const char *line) {
    char buf[600];
    int n = snprintf(buf, sizeof buf, "Slinky: %s\n", line ? line : "");
    if (n <= 0) return;
    if (n >= (int)sizeof buf) n = (int)sizeof buf - 1;  // truncated but still newline-terminated by snprintf
    (void)!write(STDERR_FILENO, buf, (unsigned long)n);
    append_log_file(buf, (unsigned long)n);
}
