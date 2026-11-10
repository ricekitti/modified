// 0x7000: Java native entry points (SlinkyHooks13): wrap the JNIEnv in a Jvm handle and forward
#include "slinky/engine.h"
#include "slinky/jids.h"
#include "slinky/injection_notice.h"
extern "C" JIds *data_2f6ef0;
extern "C" void slinky_logf(const char *fmt, ...);
extern "C" char *rdata_13e4a0;   // -> module settings block
FN(void, render_world_events, (JNIEnv *env, int key, bool down));
FN(void, tick_rotation, (Jvm *j));
FN(void, get_local_player_ref, (Jvm *j));
FN(unsigned char, world_is_loaded, (Jvm *j));
// The unload sequence sets data_2f7259 ("client is shutting down") before slinky_teardown frees every
// global about a second later. The JVM can still dispatch any of these natives in that window (the
// retransform is not guaranteed to have unwired every patched call site: see the java_set_render_manager_player_view_y/serialize_settings
// unload crashes in hs_err pid6708/pid2260), so every entry reachable through the hooks checks the flag
// first and does nothing.
extern "C" unsigned char data_2f7259;
extern "C" void hook_post_world_clear_1_7(JNIEnv *env, jclass, int key) { if (!data_2f7259) render_world_events(env, key, false); }
extern "C" void hook_post_world_clear_1_8(JNIEnv *env, jclass, int key) { if (!data_2f7259) render_world_events(env, key, true); }
extern "C" void hook_is_display_active_prerender(JNIEnv *env, jclass) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    tick_rotation(&j);
    if (rdata_13e4a0[0x14] == 1) get_local_player_ref(&j);
    world_is_loaded(&j);
}

struct Rot { float yaw, pitch; };
FN(unsigned char, is_silent_rotation_active, ());
FN(Rot, get_silent_rotation, ());
FN(void *, get_minecraft, (Jvm *j));
FN(void *, get_local_player, (Jvm *j, void *mc));
FN(void *, get_render_manager, (Jvm *j, void *mc));
FN(void, set_render_manager_boolean, (Jvm *j, void *x, bool on));
FN(void, apply_rotation_to_player, (Jvm *j, void *pl));
FN(void, set_silent_rotation_active, (Jvm *j, void *pl, bool on));
FN(void, call_orient_camera, (Jvm *j, void *o, float t));
FN(Rot, get_entity_rotation, (Jvm *j, void *p));
FN(Rot, get_entity_prev_rotation, (Jvm *j, void *p));
FN(void, set_entity_rotation, (Jvm *j, void *p, Rot r));
FN(void, set_entity_prev_rotation, (Jvm *j, void *p, Rot r));
FN(void, set_active_render_info, (Jvm *j, void *o, bool b));
// camera orientation hook: with silent rotations active, orient using the server rotation
extern "C" void hook_orient_camera(JNIEnv *env, jclass, void *o, float t) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    if (is_silent_rotation_active()) {
        void *mc = get_minecraft(&j);
        void *pl = get_local_player(&j, mc);
        set_render_manager_boolean(&j, get_render_manager(&j, mc), true);
        apply_rotation_to_player(&j, pl);
        set_silent_rotation_active(&j, pl, true);
        call_orient_camera(&j, o, t);
        set_silent_rotation_active(&j, pl, false);
    } else {
        call_orient_camera(&j, o, t);
    }
}
// same for the other hook: temporarily swap in the silent rotation around the call
extern "C" void hook_update_active_render_info(JNIEnv *env, jclass, void *o, unsigned char b) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    if (is_silent_rotation_active()) {
        void *mc = get_minecraft(&j);
        void *pl = get_local_player(&j, mc);
        Rot r1 = get_entity_rotation(&j, pl);
        Rot r2 = get_entity_prev_rotation(&j, pl);
        Rot s = get_silent_rotation();
        set_entity_rotation(&j, pl, s);
        set_entity_prev_rotation(&j, pl, s);
        set_active_render_info(&j, o, b != 0);
        set_entity_rotation(&j, pl, r1);
        set_entity_prev_rotation(&j, pl, r2);
    } else {
        set_active_render_info(&j, o, b != 0);
    }
}

// ---- tick hooks: build the module Ctx, dispatch pre-event (0xe), run the game method, dispatch post (0xf) ----
extern "C" char data_2f6f08[], data_2f7260[];
extern "C" void *data_2f7240, *data_2f7218;
extern "C" char *rdata_13e498;
struct HCtx { Jvm *a; void *mgr; void *p10; void *mc; void *world; void *player; void *p30; void *p38;
              unsigned char f40; unsigned char f41[3]; int f44; };
struct HEvent { int type; int pad; HCtx *ctx; unsigned char flag, cancel; char rest[0x36]; };
FN(void *, get_world, (Jvm *j, void *mc));
FN(unsigned char, is_in_game, (Jvm *j, void *mc));
FN(void, tick_pre_click, (HCtx *c, void *state));
FN(void, dispatch_event, (void *mgr, HEvent *e));
FN(void, java_call_minecraft_click_mouse, (Jvm *j, void *mc));
FN(void, java_call_minecraft_right_click_mouse, (Jvm *j, void *mc));
FN(void, tick_click, (HCtx *c));
FN(void, tick_click_done, (HCtx *c));
static inline __attribute__((always_inline)) void make_ctx(HCtx *c, Jvm *j, void *mc) {
    void *w = get_world(j, mc);
    void *pl = get_local_player(j, mc);
    c->a = j;
    c->mgr = data_2f6f08;
    c->p10 = data_2f7240;
    c->mc = mc;
    c->world = w;
    c->player = pl;
    c->p30 = data_2f7218;
    c->p38 = data_2f7260;
    c->f40 = rdata_13e498[0x14] ? 0 : is_in_game(j, mc);
    c->f41[0] = 0; c->f41[1] = 0; c->f41[2] = 0;
    *(int *)&c->f41[0] = 0;
    c->f44 = 0;
}
extern "C" void hook_click_mouse(JNIEnv *env, jclass, void *mc) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    HCtx c;
    make_ctx(&c, &j, mc);
    tick_pre_click(&c, data_2f7260);
    HEvent e = {};
    e.type = 0xe;
    e.ctx = &c;
    e.flag = 1;
    dispatch_event(data_2f6f08, &e);
    if (e.cancel) return;
    java_call_minecraft_click_mouse(&j, mc);
    e.type = 0xf;
    dispatch_event(data_2f6f08, &e);
}
extern "C" void hook_right_click_mouse(JNIEnv *env, jclass, void *mc) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    HCtx c;
    make_ctx(&c, &j, mc);
    HEvent e = {};
    e.type = 0xe;
    e.ctx = &c;

    e.cancel = 0;
    dispatch_event(data_2f6f08, &e);
    tick_click(&c);
    if (e.cancel) return;
    java_call_minecraft_right_click_mouse(&j, mc);
    e.type = 0xf;
    dispatch_event(data_2f6f08, &e);
    tick_click_done(&c);
}

// ---- main per-frame hook: context, GUI frame (events 9/10), module ticks, unload ----
FN(void, hook_game_window, ());
FN(void *, get_timer, (Jvm *j, void *mc));
FN(void, invoke_callback, (void *x));
FN(void, apply_module_state, (void *mgr, void *mod, int v));
FN(double, time_now, ());
FN(int, display_width, (Jvm *j));
FN(int, display_height, (Jvm *j));
FN(unsigned char, is_event_handled, (void *mgr, int type));
FN(unsigned char, resize_render_target, (void *gui, int w, int h));
FN(float, gui_part, ());
FN(void, set_render_scale, (void *gui, float t));
FN(void, begin_frame_framebuffer, (void *gui, double t));
FN(void, clear_frame, (void *gui));
FN(void, begin_frame, (void *gui, double t));
FN(void, draw_framebuffer_texture_over_screen, (void *gui));
FN(void, end_frame, (void *gui));
FN(unsigned char, has_queued_modules, (void *mgr));
FN(void, apply_module_toggles, (HCtx *c));
FN(void, tick_modules, (HCtx *c, void *timer, double now));
FN(void, tick_netty, (Jvm *j));
FN(unsigned char, is_unload_requested_flag, ());
FN(int, save_settings, (void *mgr, void *b));
FN(void, queue_module_toggle, (void *mgr, void *mod, unsigned char on));
struct Pair16 { long long a, b; };
FN(void, unregister_channel_class, (Jvm *j, Pair16 *p));
FN(void, tick_post_frame, (HCtx *c, void *state));
FN(int, set_java_hooks, (JNIEnv *env, int x));
extern "C" char data_2f9e30[];            // GUI state; [0x4cb] = unload requested
FN(void, restore_game_window, ());
FN(void, start_unload_thread, (void *x));
typedef void (*GlPushAttrib)(unsigned);
typedef void (*GlPopAttrib)();
extern "C" GlPushAttrib *rdata_13e488;
extern "C" GlPopAttrib *rdata_13e480;
extern "C" const double rdata_13e030;
extern "C" double data_2e5000;
extern "C" unsigned char data_2f7288, data_2f7290, data_2f7291, data_2f7258, data_2f7259;
extern "C" int data_2f728c, data_2f7294, data_2f7298;
extern "C" void *data_2f7238, *data_2f7280, *data_2f6ed8;
extern "C" void slinky_ensure_gui();
extern "C" Pair16 data_2f7248;
struct ModEnt { char pad[0x1e8]; void (*cb1e8)(HEvent *); };
struct Mgr { ModEnt **data; unsigned long long count; };
struct GuiEvent { int type; int pad; HCtx *ctx; int flags; float dt; char rest[0x30]; };
struct GuiEvent2 { long long type; HCtx *ctx; void *gui; float dt; char rest[0x2c]; };
static inline __attribute__((always_inline)) void spin_lock() {
    while (__atomic_exchange_n(&data_2f7258, 1, __ATOMIC_SEQ_CST)) __builtin_ia32_pause();
}
extern "C" void source_scaffold_render_hud(JNIEnv*,double);
extern "C" void hook_update_display(JNIEnv *env) {
    if (data_2f7259) return;   // the client is shutting down; this frame must not touch anything the
                               // unload thread is about to free (save_settings of a second pass crashed here)
    slinky_ensure_gui();
    hook_game_window();
    Jvm j = {data_2f6ef0, env};
    void *mc = get_minecraft(&j);
    void *pl = get_local_player(&j, mc);
    void *w = get_world(&j, mc);
    void *timer = get_timer(&j, mc);
    HCtx c;
    c.a = &j; c.mgr = data_2f6f08; c.p10 = data_2f7240; c.mc = mc; c.world = w; c.player = pl;
    c.p30 = data_2f7218; c.p38 = data_2f7260;
    char *cfg = rdata_13e498;
    c.f40 = cfg[0x14] ? 0 : is_in_game(&j, mc);
    *(int *)&c.f41[0] = 0;
    c.f44 = 0;
    if (!c.f40 && data_2f7288) {
        invoke_callback(data_2f7238);
        if (!world_is_loaded(&j)) __builtin_memset(data_2f7218, 0, 0x68);
    }
    data_2f7288 = c.f40;
    if (*(int *)(cfg + 0x1c) == 0) apply_module_state(data_2f6f08, rdata_13e498, 0x63);
    double now = time_now();
    int W = display_width(&j);
    int H = display_height(&j);
    if(c.f40&&W>0&&H>0)source_scaffold_render_hud(env,now);
    if (0.0 > data_2e5000) data_2e5000 = now;
    if (data_2f7280 && W > 0 && H > 0 && !is_event_handled(data_2f6f08, 0xa)) {
        data_2f7294 = W;
        data_2f7298 = H;
        if (resize_render_target(data_2f7280, W, H)) {
            data_2f728c = 0xa;
        } else if (data_2f728c > 0) {
            data_2f728c = data_2f728c - 1;
        }
        void *gui = data_2f7280;
        set_render_scale(gui, gui_part());
        double dt = now - data_2e5000;
        if (dt > rdata_13e030) {
            float fdt = (float)dt;
            GuiEvent e = {};
            e.type = 9;
            e.ctx = &c;
            e.dt = fdt;
            dispatch_event(data_2f6f08, (HEvent *)&e);
            int flags = e.flags;
            data_2f7290 = flags & 1;
            if (flags & 1) {
                if ((flags & 2) || data_2f7291 || data_2f728c > 0) {
                    unsigned char again = (unsigned)(flags & 2) >> 1;
                    (*rdata_13e488)(0xffffffff);
                    begin_frame_framebuffer(data_2f7280, now);
                    GuiEvent2 e2 = {0xa, &c, data_2f7280, fdt, {}};
                    dispatch_event(data_2f6f08, (HEvent *)&e2);
                    clear_frame(data_2f7280);
                    (*rdata_13e480)();
                    data_2f7291 = again;
                }
            }
            data_2e5000 = now;
        }
        if (data_2f7290) {
            (*rdata_13e488)(0xffffffff);
            begin_frame(data_2f7280, now);
            draw_framebuffer_texture_over_screen(data_2f7280);
            end_frame(data_2f7280);
            (*rdata_13e480)();
        } else {
            data_2f7291 = 1;
        }
    }
    if (has_queued_modules(data_2f6f08)) {
        spin_lock();
        apply_module_toggles(&c);
        data_2f7258 = 0;
    }
    // Render the startup key hint independently of world-bound module handlers.
    // This path is available on Lunar's main menu and does not open the GUI.
    if (W > 0 && H > 0 && injection_notice_visible((Ctx*)&c, now)) {
        void* gui = data_2f7280;
        resize_render_target(gui, W, H);
        set_render_scale(gui, gui_part());
        (*rdata_13e488)(0xffffffff);
        begin_frame(gui, now);
        injection_notice_draw(gui, now);
        end_frame(gui);
        (*rdata_13e480)();
    }
    tick_modules(&c, timer, now);
    tick_netty(&j);
    if (!is_unload_requested_flag()) return;
    data_2f9e30[0x4cb] = 0;   // consume the click immediately: one sequence per click, no repass if
                              // the user clicks again while this one is still running
    // Take the Java hooks out before touching any state. If removal fails the game will keep calling
    // these natives; the old sequence still freed the module manager from the unload thread and the
    // next frame crashed in save_settings -> serialize_settings on the freed module list. In that case keep the client
    // resident (everything stays allocated) and cancel the unload request instead. The native entries
    // check data_2f7259, so a partial removal followed by the real teardown can no longer fault
    // either: everything the game still reaches goes inert the moment this branch commits.
    int hoff = set_java_hooks(env, 0);
    slinky_logf("unload: hooks-off=%d", hoff);
    if (hoff) return;
    // unload: save settings, notify and disable every module, tear down
    int saved = save_settings(data_2f6f08, data_2f7240);
    slinky_logf("unload: settings saved=%d", saved);
    HEvent e3 = {};
    e3.type = 0x1a;
    e3.ctx = &c;
    Mgr *mgr = (Mgr *)data_2f6f08;
    for (unsigned long long i = 0; i < mgr->count; i++)
        if (mgr->data[i]->cb1e8) mgr->data[i]->cb1e8(&e3);
    for (unsigned long long i = 0; i < mgr->count; i++) queue_module_toggle(data_2f6f08, mgr->data[i], 0);
    spin_lock();
    data_2f7259 = 1;
    Pair16 p = data_2f7248;
    unregister_channel_class(&j, &p);
    data_2f7258 = 0;
    apply_module_toggles(&c);
    tick_post_frame(&c, data_2f7260);
    restore_game_window();
    slinky_logf("unload: starting unload thread");
    start_unload_thread(data_2f6ed8);
}

// ---- set the key bind of a module: re-register it in the bind map, then tell the modules (event 0x18) ----
FN(void, key_map_remove, (void *map, int key, void *mod));   // unregister
FN(void, key_map_add, (void *map, int key, void *mod));   // register
struct BindEvent { int type; int pad; void *mgr; void *mod; char rest[0x30]; int tail; };
extern "C" void set_module_bind(void *mod, int key) {
    int *slot = (int *)((char *)mod + 0x1c);
    key_map_remove(data_2f7238, *slot, mod);
    *slot = key;
    key_map_add(data_2f7238, key, mod);
    BindEvent ev = {};
    ev.type = 0x18;
    ev.mgr = data_2f6f08;
    ev.mod = mod;
    dispatch_event(data_2f6f08, (HEvent *)&ev);
}

// ---- key handlers of the two key maps (data_2f7230: key -> profile name, data_2f7238: key -> module) ----
// Both are called from the input handler on_input_event through key_map_dispatch as handler(item, key, down).
FN(int, load_profile_name, (const char *name, void *mgr));              // load profile `name`; 0 = ok
FN(void *, find_profile, (void *profiles, const char *name));       // profile entry by name (+0x38: key bind)
FN(void *, key_map_get, (void *map, int key, void *val));          // item registered under `key`
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));
FN(unsigned long long, libc_strlen, (const char *s));
FN(void *, libc_memcpy, (void *d, const void *s, unsigned long long n));
extern "C" void *data_2f7230;
extern "C" unsigned char data_2f7258, data_2f7259;
extern "C" const char rdata_13e370[];   // "Failed to load profile"
struct Ev48 { int type; int pad; union { const char *name; struct { int err; int pad2; const char *msg; }; }; char rest[0x38]; };
extern "C" const char rdata_13e3a0[];   // prepared "Failed to set bind" event (0x48 bytes)
// profile bind pressed: load the profile; a failure unbinds it and is reported (event 0xc), success is event 0xb
extern "C" unsigned char on_profile_bind(const char *name, int key, bool down) {
    if (!down) return 0;
    int err = load_profile_name(name, data_2f6f08);
    if (!err) {
        Ev48 e = {};
        e.type = 0xb;
        e.name = name;
        dispatch_event(data_2f6f08, (HEvent *)&e);
        return 1;
    }
    char *prof = (char *)find_profile(data_2f7240, name);
    if (prof) *(int *)(prof + 0x38) = 0;
    Ev48 e = {};
    e.type = 0xc;
    e.err = err;
    e.msg = rdata_13e370;
    dispatch_event(data_2f6f08, (HEvent *)&e);
    key_map_remove(data_2f7230, key, 0);
    return 0;
}

// module bind pressed (hold mode: enabled while the key is down; otherwise toggles on press)
extern "C" unsigned char on_module_bind(char *mod, int key, bool down) {
    if (mod[0x20] == 1) {
        queue_module_toggle(data_2f6f08, mod, down);
        mod[0x21] = down;
    } else if (down) {
        queue_module_toggle(data_2f6f08, mod, mod[0x14] ^ 1);
    }
    return 1;
}

// bind profile `p` to `key` (0 = unbind): a key serves one profile, the previous owner loses its bind
extern "C" void bind_profile_to_key(char *p, int key) {
    void *map = data_2f7230;

    key_map_remove(map, *(int *)(p + 0x38), 0);
    void *other = key_map_get(data_2f7230, key, 0);
    if (other) {
        char *o = (char *)find_profile(data_2f7240, (const char *)other);
        if (o) {
            *(int *)(o + 0x38) = 0;
            key_map_remove(data_2f7230, key, 0);
        }
    }
    if (key) {
        const char *name = *(const char **)p;
        unsigned long long n = libc_strlen(name);
        char *copy = (char *)libc_calloc(n + 1, 1);
        if (!copy) {
            Ev48 e;
            __builtin_memcpy(&e, rdata_13e3a0, 0x48);
            dispatch_event(data_2f6f08, (HEvent *)&e);
            return;
        }
        libc_memcpy(copy, name, n);
        key_map_add(data_2f7230, key, copy);
    }
    *(int *)(p + 0x38) = key;
}

// ---- SlinkyChannelHandler.onPacket(Object packet, boolean outbound, long time, int id): int ----
// Netty thread -> client: serialised with the frame code through the lock at data_2f7258, ignored once the
// client is shutting down (data_2f7259). The modules see it as event 2 (inbound) / 3 (outbound) and may change
// `result` (checked at +0x24 by the handler).
struct PktEv { int type; int pad; Jvm *j; void *pkt; long long time; int id; int result; char rest[0x20]; int tail; };
extern "C" unsigned char source_consume_silent_packet(JNIEnv*,jobject);
extern "C" int on_packet(JNIEnv *env, jobject, void *pkt, unsigned char outbound, long long time, int id) {
    if(outbound&&source_consume_silent_packet(env,(jobject)pkt))return 0;
    bool busy;
    for (;;) {
        busy = __atomic_exchange_n(&data_2f7258, 1, __ATOMIC_SEQ_CST) != 0;

        if (!busy) break;
        __builtin_ia32_pause();
    }
    int r = 0;
    if (!data_2f7259) {
        Jvm j = {data_2f6ef0, env};
        PktEv e = {};
        e.type = outbound ? 3 : 2;
        e.j = &j;
        e.pkt = pkt;
        e.time = time;
        e.id = id;
        dispatch_event(data_2f6f08, (HEvent *)&e);
        r = e.result;
    }
    data_2f7258 = 0;
    return r;
}
