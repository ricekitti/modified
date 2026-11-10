// macOS OpenGL entry-point resolver (slinky/platform.h: slinky_gl_get_proc).
// Minecraft 1.8.9 runs on LWJGL2 / legacy GL through the system OpenGL framework. Unlike Windows there is
// no wglGetProcAddress indirection: the framework exports the GL 1.1-2.1 entry points as ordinary dynamic
// symbols, so dlsym against the framework resolves them. The caller guarantees a current CGL context
// (the client only renders from the game's render-thread natives, which already have one bound).
#include "slinky/platform.h"

#include <dlfcn.h>

extern "C" void *slinky_gl_get_proc(const char *name) {
    static void *gl = nullptr;
    if (!gl) {
        gl = dlopen("/System/Library/Frameworks/OpenGL.framework/Versions/Current/OpenGL", RTLD_LAZY | RTLD_LOCAL);
        if (!gl) gl = dlopen("/System/Library/Frameworks/OpenGL.framework/OpenGL", RTLD_LAZY | RTLD_LOCAL);
        if (!gl) return nullptr;
    }
    return name ? dlsym(gl, name) : nullptr;
}
