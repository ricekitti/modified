// macOS agent entry for the client dylib.
//
// On Windows the client is a DLL injected with LoadLibrary and started from DllMain
// (src/platform/win/entry.cpp). On macOS it is a JVMTI agent that Lunar's JVM loads through the supported
// Attach API (VirtualMachine.attach(pid).loadAgentPath(dylib, runDir)) or -agentpath at start, so the entry
// points are Agent_OnAttach / Agent_OnLoad instead. The JVM hands us the JavaVM*, but start-up discovers it
// itself through JNI_GetCreatedJavaVMs (math_helpers.cpp + the dlsym shim in sys.cpp), so the shared engine
// (init.cpp slinky_setup/teardown) is used unchanged. Start-up runs on its own pthread because it performs
// JNI calls and file I/O that must not run on the JVM's attach thread.
#include "slinky/platform.h"

#include <pthread.h>
#include <dlfcn.h>
#include <cstring>
#include <cstdio>
#include <fcntl.h>
#include <unistd.h>

typedef struct JavaVM_ JavaVM;                 // opaque: start-up re-acquires the VM itself, we never deref it here

extern "C" void injection_notice_ready();      // src/engine/jni/injection_notice.cpp (portable, JNI-only)
extern "C" void start_unload_thread(void *self);

// The launcher polls a per-run status file beside the agent, exactly as on Windows (the loader waits for
// "FINAL STATUS: RUNNING" / "FAILED" in rice-native.log). Keep the contract so the Mac launcher reuses it.
static void report_startup(bool failed) {
    Dl_info info;
    if (!dladdr((void *)&report_startup, &info) || !info.dli_fname) return;
    char path[2048];
    int n = snprintf(path, sizeof path, "%s", info.dli_fname);
    if (n <= 0 || n >= (int)sizeof path) return;
    char *slash = strrchr(path, '/');
    if (!slash) return;
    slash[1] = 0;
    if (strlen(path) + sizeof("rice-native.log") > sizeof path) return;
    strncat(path, "rice-native.log", sizeof path - strlen(path) - 1);
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) return;
    const char *msg = failed ? "FINAL STATUS: FAILED\n" : "FINAL STATUS: RUNNING\n";
    (void)!write(fd, msg, strlen(msg));
    close(fd);
}

static void *agent_self() {
    Dl_info info;
    if (dladdr((void *)&agent_self, &info) && info.dli_fname)
        return dlopen(info.dli_fname, RTLD_NOLOAD | RTLD_LAZY);   // a handle to our own image (token; we stay resident)
    return nullptr;
}

static void *startup_thread(void *self) {
    bool failed = slinky_setup(self) != 0;
    report_startup(failed);
    if (!failed) injection_notice_ready();
    else start_unload_thread(self);            // start-up failed: tear back down (the dylib itself stays resident)
    return nullptr;
}

static void spawn(void *(*fn)(void *), void *arg) {
    pthread_t t;
    if (pthread_create(&t, nullptr, fn, arg) == 0) pthread_detach(t);
}

// Runtime attach: VirtualMachine.loadAgentPath() -> Agent_OnAttach.
extern "C" __attribute__((visibility("default")))
int Agent_OnAttach(JavaVM * /*vm*/, char * /*options*/, void * /*reserved*/) {
    spawn(startup_thread, agent_self());
    return 0;
}

// -agentpath at JVM start -> Agent_OnLoad (same start-up path).
extern "C" __attribute__((visibility("default")))
int Agent_OnLoad(JavaVM * /*vm*/, char * /*options*/, void * /*reserved*/) {
    spawn(startup_thread, agent_self());
    return 0;
}

// ---- unload: wait for the game's in-flight hook calls to return, tear down, then stay resident ----
// The Linux reference agent (Slinky-cross-platform-source/native/payload.cpp) keeps its library resident
// rather than dlclose-ing itself from inside a JVM callback; we do the same so the launcher's "Unload, then
// relaunch in the same session" reuse works and there is no use-after-free if a game thread is still in a hook.
static void *unload_thread(void * /*self*/) {
    slinky_logf("unload thread: waiting for the hooks to be out");
    usleep(1000 * 1000);
    slinky_logf("unload thread: teardown");
    slinky_teardown();                         // releases what slinky_setup built (init.cpp)
    slinky_logf("unload thread: done (agent stays resident for reload)");
    return nullptr;
}

extern "C" void start_unload_thread(void *self) { spawn(unload_thread, self); }
