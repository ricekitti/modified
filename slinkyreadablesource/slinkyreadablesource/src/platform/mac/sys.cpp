// macOS replacements for the two Win32 module helpers the shared engine resolves by name. On Windows they
// live in src/engine/protected/input.cpp as thin wrappers over GetModuleHandleA / GetProcAddress; that file
// is Windows-only and is replaced by src/platform/mac/ on this target, so the helpers are provided here.
//
// Users of these two symbols (all compiled unchanged on macOS):
//   * src/engine/math/math_helpers.cpp : get_java_vm() -> kernel32_lookups("jvm") + get_proc_address(.., "JNI_GetCreatedJavaVMs")
//   * the GL loader path for "opengl32"
// Because the client is loaded as a JVMTI agent, it is already inside the JVM process, so libjvm is a
// resident image (RTLD_NOLOAD finds it). Windows module names are mapped to their macOS equivalents; any
// other name is tried as-is and then as lib<name>.dylib.
#include <dlfcn.h>
#include <cstdio>
#include <cstring>

static void *open_resident(const char *path) {
    void *h = dlopen(path, RTLD_NOLOAD | RTLD_LAZY);    // already-loaded only: GetModuleHandle semantics
    if (!h) h = dlopen(path, RTLD_LAZY);                // otherwise load it
    return h;
}

extern "C" void *kernel32_lookups(const char *module) {
    if (!module) return nullptr;
    if (!strcmp(module, "jvm")) {
        void *h = open_resident("libjvm.dylib");
        return h ? h : RTLD_DEFAULT;                    // fall back to a global dlsym scope; the JVM exports JNI_GetCreatedJavaVMs
    }
    if (!strcmp(module, "opengl32"))
        return open_resident("/System/Library/Frameworks/OpenGL.framework/OpenGL");
    void *h = open_resident(module);
    if (h) return h;
    char named[256];
    int n = snprintf(named, sizeof named, "lib%s.dylib", module);
    if (n > 0 && n < (int)sizeof named) h = open_resident(named);
    return h;
}

extern "C" void *get_proc_address(void *module, const char *name) {
    if (!name) return nullptr;
    return dlsym(module ? module : RTLD_DEFAULT, name);
}
