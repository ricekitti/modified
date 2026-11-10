// Readable version of the native getMouseOver hook (0x7410) including its first-call GUI setup; notes on the
// protector-touched functions around it (0x6e10 hook control is in init.cpp, CRT 0x4cd50 is not client code).
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/platform.h"

struct HCtx { Jvm *a; void *mgr; void *p10; void *mc; void *world; void *player; void *p30; void *p38;
              unsigned char f40; unsigned char f41[3]; int f44; };
// event 8 ("reach"): handlers may set `use` and change reach / expand; reach_distance_check reads ctx..expand as ReachA
struct ReachEvent { long long type; HCtx *ctx; unsigned char use; char pad[7]; double reach, expand; char rest[0x20]; };
struct PlainEvent { long long type; HCtx *ctx; char rest[0x38]; };
struct Pair16 { long long a, b; };

extern "C" JIds *data_2f6ef0;
extern "C" char data_2f6f08[], data_2f7260[];   // module manager, target state
extern "C" void *data_2f7240, *data_2f7218;
extern "C" char *rdata_13e498;                 // client settings block (+0x14: GUI open)
extern "C" Pair16 data_2f7248;                 // netty handler class + ctor
extern "C" unsigned char data_2f725a;          // one-time initialisation done
extern "C" unsigned char data_2f7259;          // set by the unload sequence: the entry is inert once it is 1
extern "C" const double rdata_13e020[2];       // default reach 3.0, hitbox expand 0.1

FN(void, call_get_mouse_over, (Jvm *j, void *renderer, float partial_ticks));   // EntityRenderer.getMouseOver (original)
FN(void *, get_minecraft, (Jvm *j));                                       // Minecraft instance
FN(int, register_channel_class, (Jvm *j, Pair16 *handler));                          // keep "slinky_handler" in the netty pipeline
FN(void, player_tick, (Jvm *j, void *mc));
FN(void *, get_local_player, (Jvm *j, void *mc));                             // player
FN(void *, get_world, (Jvm *j, void *mc));                             // world
FN(unsigned char, is_in_game, (Jvm *j, void *mc));                      // in game (no screen open)
FN(bool, target_clear, (HCtx *c, void *target));
FN(void, dispatch_event, (void *mgr, void *event));                         // dispatch an event to the modules
FN(void, reach_distance_check, (void *reach_args));                                // entity mouse-over with module reach
FN(void, release_tracked_owner, ());

// ---- GUI setup, once, on the render thread of the game (needs its OpenGL context) ----
// New code (the original does this inside its virtualised first-call block): load the GL entry points, create the
// NanoVG context the GUI draws with, and the window object (gui/e_10.cpp "Rend") that carries it and the font ids.
extern "C" int slinky_load_gl(void *(*get)(const char *name));
extern "C" void *nvgCreateGL3(int flags);
enum { NVG_ANTIALIAS = 1, NVG_STENCIL_STROKES = 2 };   // inferred: the usual flags; the shader is the EDGE_AA variant
struct GuiWindow { void *ig; double t; int w, h; float sw, sh, scale; int pad; void *fb; float clear[4];
                   int font[2]; int tex; float tw, th; };       // +0x48 texture, +0x4c/+0x50 atlas size
extern "C" GuiWindow *data_2f7280;
extern "C" char data_2f9e30[];                                   // GUI state (gui/e_11.cpp, logic/visual/gui.cpp)
extern "C" const unsigned char rdata_15b7a0[], rdata_2050a0[];   // embedded TrueType: Inter Medium, Inter Bold
extern "C" const unsigned char rdata_15a360[];                   // 64x64 GUI icon atlas (PNG, 5129 bytes)
extern "C" int nvgCreateImageMem(void *ctx, int imageFlags, unsigned char *data, int ndata);
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));
FN(int, find_glfw_entrypoint, (void **ctx, unsigned slot, const char *name, const unsigned char *data, int size));
// The click GUI's tab bar: 7 entries {int kind; int pad; const char *label} at state+0x2e0, rendered by draw_tab_bar
// and indexed by the selected tab (state+0x350) in draw_gui_frame. The image leaves the array zeroed in .data and fills
// it during start-up (the virtualised block, not recovered) - without this the first GUI frame calls nvgText(NULL).
struct GuiTab { int kind; int pad; const char *label; };
static const char *const tab_label[7] = {"Combat", "Move", "Visual", "Block", "Utility", "Profiles", "Unload"};
static void gui_tabs_init() {
    GuiTab *t = (GuiTab *)(data_2f9e30 + 0x2e0);
    for (int i = 0; i < 7; i++) {
        t[i].kind = i;
        t[i].label = tab_label[i];
    }
}
static void gui_init() {
    gui_tabs_init();
    slinky_load_gl(slinky_gl_get_proc);
    GuiWindow *w = (GuiWindow *)libc_calloc(1, sizeof(GuiWindow));
    if (!w) return;
    w->ig = nvgCreateGL3(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
    w->scale = 1.0f;
    if (!w->ig || find_glfw_entrypoint((void **)w, 0, "Inter Medium", rdata_15b7a0, 694511) ||
        find_glfw_entrypoint((void **)w, 1, "Inter Bold", rdata_2050a0, 715203)) {
        slinky_logf("GUI setup failed");   // the GUI stays off (data_2f7280 is checked before every use)
        return;
    }
    // GUI icons: one 64x64 sheet of 20px tiles, used by draw_texture (profile load/save/folder/delete and
    // friends). The original start-up uploaded it and stored {image, 64, 64} at window+0x48/+0x4c/+0x50;
    // without it draw_texture used image 0 and every icon was a solid rectangle.
    int atlas = nvgCreateImageMem(w->ig, 0, (unsigned char *)rdata_15a360, 5129);
    if (atlas <= 0) {
        slinky_logf("GUI icon atlas failed to load");
    } else {
        w->tex = atlas;
        w->tw = 64.0f;
        w->th = 64.0f;
    }
    data_2f7280 = w;
}

// Both entry points run on the render thread with a current GL context.
// updateDisplay also runs on the title screen, before getMouseOver is called.
extern "C" void slinky_ensure_gui() {
    if (data_2f7259 || data_2f7280) return;
    if (__atomic_exchange_n(&data_2f725a, 1, __ATOMIC_SEQ_CST) == 0) gui_init();
}

// ---- native hook of EntityRenderer.getMouseOver(partialTicks) ----
extern "C" void hook_get_mouse_over(JNIEnv *env, jclass, void *renderer, float partial_ticks) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    call_get_mouse_over(&j, renderer, partial_ticks);   // vanilla ray trace first

    slinky_ensure_gui();

    void *mc = get_minecraft(&j);
    Pair16 handler = data_2f7248;
    if (register_channel_class(&j, &handler)) {   // pipeline handler could not be installed: print and clear the Java exception
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
    player_tick(&j, mc);
    void *player = get_local_player(&j, mc);
    void *world = get_world(&j, mc);

    HCtx c;
    c.a = &j;
    c.mgr = data_2f6f08;
    c.p10 = data_2f7240;
    c.mc = mc;
    c.world = world;
    c.player = player;
    c.p30 = data_2f7218;
    c.p38 = data_2f7260;
    c.f40 = rdata_13e498[0x14] ? 0 : is_in_game(&j, mc);
    c.f41[0] = c.f41[1] = c.f41[2] = 0;
    c.f44 = 0;
    target_clear(&c, data_2f7260);

    PlainEvent ev0 = {};   // event 0: mouse-over computed
    ev0.ctx = &c;
    dispatch_event(data_2f6f08, &ev0);

    if (!is_in_game(&j, mc)) return;
    ReachEvent ev = {};   // event 8: modules may extend the reach
    ev.type = 8;
    ev.ctx = &c;
    ev.reach = rdata_13e020[0];
    ev.expand = rdata_13e020[1];
    dispatch_event(data_2f6f08, &ev);
    if (ev.use == 1) reach_distance_check(&ev.ctx);
    release_tracked_owner();
}

// ---- set_java_hooks(JNIEnv *env, int enable): turn every game hook on (1) or off (0) -> init.cpp ----

// ---- crt_dll_attach / crt_dll_attach_helper: MSVC C runtime DLL attach/detach ----
// crt_dll_attach(HINSTANCE, DWORD reason, void *) matches the vcruntime DLL attach/detach dispatch: it branches on
// reason 1 / 0, keeps a process-attach counter (data_3065c4), takes the native-startup lock with `lock cmpxchg`
// on a TEB field and retries with a 1000 ms sleep, then calls into the CRT (crt_initterm). crt_dll_attach_helper is only
// called from it. This is Microsoft runtime code, not client logic (see vcruntime's dll_dllmain.cpp).

// ---- empty_placeholder(gui state): virtualised, NOT RECOVERED ----
// It prepares a message from a short string stored encrypted at rdata_140a7b (right behind "Unload Slinky?") and is
// only started through reset_key_state. The decrypted text and the effect are unknown, so this is a no-op placeholder.
extern "C" void empty_placeholder(void *gui) { (void)gui; }
