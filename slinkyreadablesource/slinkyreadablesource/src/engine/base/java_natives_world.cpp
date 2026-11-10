// 0x8000: Java native entry points (SlinkyHooks13): wrap the JNIEnv in a Jvm handle and forward
#include "slinky/engine.h"
#include "slinky/rotation_override.h"
#include "slinky/jids.h"
extern "C" JIds *data_2f6ef0;
extern "C" unsigned char data_2f7259;   // set by the unload sequence: every entry below is inert once it is 1
struct Rot { float yaw, pitch; };
FN(unsigned char, is_silent_rotation_active, ());
FN(Rot, get_silent_rotation, ());
FN(void *, should_render_player_name, (Jvm *j, void *a, void *b));
FN(void, java_set_render_manager_player_view_x, (Jvm *j, void *o, float v));
FN(void, java_set_render_manager_player_view_y, (Jvm *j, void *o, float v));
FN(unsigned char, handle_chat_packet, (Jvm *j, void *a, void *b));
FN(void, send_packet_with_id, (Jvm *j, void *a, void *b, void *c));
FN(void, send_packet, (Jvm *j, void *a, void *b));
// renderPlayer is a static native with the class-file descriptor (Object,Object,DDDFF)V: the JVM passes
// the three doubles and two floats in registers. The module receives them unchanged and forwards them
// to RendererLivingEntity.doRender.
FN(void, chams_part, (Jvm *j, void *a, void *b, double x, double d0, double d1, float f0, float f1));
extern "C" jobjectArray source_scaffold_visual_begin(JNIEnv*,jobject);
extern "C" void source_scaffold_visual_end(JNIEnv*,jobject,jobjectArray);
extern "C" void *hook_can_render_name(JNIEnv *env, jclass, void *a, void *b) {
    if (data_2f7259) return 0;
    Jvm j = {data_2f6ef0, env};
    return should_render_player_name(&j, a, b);
}
extern "C" void hook_render_player(JNIEnv *env, jclass, void *a, void *b, double x, double d0, double d1, float f0, float f1) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    struct VisualGuard{
        JNIEnv* env;jobject entity;jobjectArray saved;
        ~VisualGuard(){source_scaffold_visual_end(env,entity,saved);}
    } visual{env,(jobject)b,source_scaffold_visual_begin(env,(jobject)b)};
    chams_part(&j, a, b, x, d0, d1, f0, f1);
}
// silent-rotation aware pitch / yaw setters
extern "C" void hook_set_render_x(JNIEnv *env, jclass, void *o, float pitch) {
    if (data_2f7259) return;
    if (is_silent_rotation_active()) pitch = get_silent_rotation().pitch;
    Jvm j = {data_2f6ef0, env};
    java_set_render_manager_player_view_x(&j, o, pitch);
}
extern "C" void hook_set_render_y(JNIEnv *env, jclass, void *o, float yaw) {
    if (data_2f7259) return;
    if (is_silent_rotation_active()) yaw = get_silent_rotation().yaw;
    Jvm j = {data_2f6ef0, env};
    java_set_render_manager_player_view_y(&j, o, yaw);
}
extern "C" void hook_send_packet_1_7(JNIEnv *env, jclass, void *a, void *b, void *c) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    if (!handle_chat_packet(&j, a, b)) send_packet_with_id(&j, a, b, c);
}
extern "C" void hook_send_packet_1_8(JNIEnv *env, jclass, void *a, void *b) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    if (!handle_chat_packet(&j, a, b)) send_packet(&j, a, b);
}

FN(void *, get_minecraft, (Jvm *j));
FN(void *, get_world, (Jvm *j, void *mc));
FN(void, set_block_meta, (Jvm *j, void *o, int a, int b));
struct Event { long long type; Jvm *j; void *world; int z; int a; int b; int zz[9]; };
extern "C" char data_2f6f08[];
FN(void, dispatch_event, (void *mgr, Event *e));
extern "C" void hook_unload_chunk(JNIEnv *env, jclass, void *o, int a, int b) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    void *w = get_world(&j, get_minecraft(&j));
    set_block_meta(&j, o, a, b);
    Event e = {0x14, &j, w, 0, a, b, {}};
    dispatch_event(data_2f6f08, &e);
}

// ---- world-change hooks: report the change to the modules (event 0x14), then call the game method ----
FN(int, get_block_id, (Jvm *j, void *blockOrState));
FN(void *, set_block_state, (Jvm *j, void *o, int a, int b, int c, void *blk, int d, int e));
struct Pos3 { int x, y, z; };
FN(Pos3, get_block_position, (Jvm *j, void *pos));
FN(void *, get_block_from_pos, (Jvm *j, void *blk));
FN(void *, set_block_at_pos, (Jvm *j, void *o, void *pos, void *blk, int f));
FN(void, update_chunk, (Jvm *j, void *o, int p4, int p5, int p6, int p7, int p8, int p9));
FN(int, chunk_coordinate, (int v));
struct Ev14 { long long type; Jvm *j; void *o; int kind; int v[4]; int z[7]; };
extern "C" void *hook_set_block_1_7(JNIEnv *env, jclass, void *o, int a, int b, int c, void *blk, int d, int e) {
    if (data_2f7259) return 0;
    Jvm j = {data_2f6ef0, env};
    int id = get_block_id(&j, blk);
    Ev14 ev = {0x14, &j, o, 2, {a, b, c, id}, {}};
    dispatch_event(data_2f6f08, (Event *)&ev);
    return set_block_state(&j, o, a, b, c, blk, d, e);
}
extern "C" void *hook_set_block_1_8(JNIEnv *env, jclass, void *o, void *pos, void *blk, int f) {
    if (data_2f7259) return 0;
    Jvm j = {data_2f6ef0, env};
    Pos3 p = get_block_position(&j, pos);
    int id = get_block_id(&j, get_block_from_pos(&j, blk));
    Ev14 ev = {0x14, &j, o, 2, {p.x, p.y, p.z, id}, {}};
    dispatch_event(data_2f6f08, (Event *)&ev);
    return set_block_at_pos(&j, o, pos, blk, f);
}
extern "C" void hook_invalidate_chunk(JNIEnv *env, jclass, void *o, int p4, int p5, int p6, int p7, int p8, int p9) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    update_chunk(&j, o, p4, p5, p6, p7, p8, p9);
    int cx = chunk_coordinate(p4);
    int cz = chunk_coordinate(p6);
    Ev14 ev = {0x14, &j, o, 1, {cx, cz, 0, 0}, {}};
    dispatch_event(data_2f6f08, (Event *)&ev);
}
extern "C" char *rdata_13e4b0;
FN(void *, get_local_player, (Jvm *j, void *mc));
FN(unsigned char, get_render_flag, ());
FN(unsigned char, is_sneaking, (Jvm *j, void *o));
// isSneaking hook: report true for the local player while the "sneak" module asks for it
extern "C" unsigned char hook_should_clip_ledge(JNIEnv *env, jclass, void *o) {
    if (data_2f7259) return 0;
    Jvm j = {data_2f6ef0, env};
    if (rdata_13e4b0[0x14] == 1) {
        void *pl = get_local_player(&j, get_minecraft(&j));
        if (pl && (*j.env)->IsSameObject(j.env, (jobject)pl, (jobject)o) && get_render_flag()) return 1;
    }
    return is_sneaking(&j, o);
}

// ---- player update / velocity hooks ----
extern "C" char data_2f7260[];
extern "C" void *data_2f7240, *data_2f7218;
extern "C" char *rdata_13e498;
struct HCtx { Jvm *a; void *mgr; void *p10; void *mc; void *world; void *player; void *p30; void *p38;
              unsigned char f40; unsigned char f41[3]; int f44; };
struct HEvent { int type; int pad; HCtx *ctx; char rest[0x38]; };
struct RotEvent { int type; int pad; unsigned char a, b, d, e; Rot rot; char rest[0x34]; };   // event 0xd
FN(unsigned char, is_in_game, (Jvm *j, void *mc));
FN(void, reset_player_rotation, (HCtx *c));
FN(void, update_entities, (Jvm *j, void *o));
FN(Rot, get_entity_rotation, (Jvm *j, void *p));
FN(unsigned char, is_event_handled, (void *mgr, int type));
FN(void, apply_player_rotation, (HCtx *c, unsigned char a, unsigned char b, unsigned char d, Rot r));
FN(void, set_rotation_active, (Jvm *j, void *pl, bool on));
extern "C" void hook_update_entities(JNIEnv *env, jclass, void *o) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    void *mc = get_minecraft(&j);
    void *pl = get_local_player(&j, mc);
    HCtx c;
    c.a = &j; c.mgr = data_2f6f08; c.p10 = data_2f7240; c.mc = mc; c.world = o; c.player = pl;
    c.p30 = data_2f7218; c.p38 = data_2f7260;
    c.f40 = rdata_13e498[0x14] ? 0 : is_in_game(&j, mc);
    *(int *)&c.f41[0] = 0;
    c.f44 = 0;
    HEvent e = {};
    e.type = 0x10;
    e.ctx = &c;
    dispatch_event(data_2f6f08, (Event *)&e);
    reset_player_rotation(&c);
    e.type = 0x11;
    dispatch_event(data_2f6f08, (Event *)&e);
    update_entities(&j, c.world);
    e.type = 0x12;
    dispatch_event(data_2f6f08, (Event *)&e);
    Rot r = is_silent_rotation_active() ? get_silent_rotation() : get_entity_rotation(&j, pl);
    unsigned char a, b, d;
    if (is_event_handled(data_2f6f08, 0xd)) {
        a = 0; b = 0; d = 0;
    } else {
        RotEvent m;
        __builtin_memset(&m, 0, sizeof m);

        m.type = 0xd;
        m.rot = r;
        dispatch_event(data_2f6f08, (Event *)&m);
        a = m.a; b = m.b; d = m.d;
        r = m.rot;
        if (m.a && (m.e & kApplyRotationOverride))
            queue_rotation_override(&j, pl, {r.yaw, r.pitch});
    }
    apply_player_rotation(&c, a, b, d, r);
    set_rotation_active(&j, pl, true);
}

FN(void, set_motion, (Jvm *j, void *o, float x, float y, float z));
extern "C" bool source_mayu_move(JNIEnv*,jobject,float,float,float);
FN(void *, get_net_handler, (Jvm *j, void *mc));
FN(void *, get_held_item, (Jvm *j, void *x));
FN(int, get_hurt_time, (Jvm *j, void *pl));
FN(unsigned char, is_key_down, (Jvm *j, void *x));
struct VelEvent { int type; int pad; HCtx *ctx; float x, y, z; unsigned char flag; char rest[0x2b]; };
extern "C" void hook_move_flying(JNIEnv *env, jclass, void *o, float x, float y, float z) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    if (!is_event_handled(data_2f6f08, 0x15) || !is_event_handled(data_2f6f08, 0x16)) {
        void *mc = get_minecraft(&j);
        void *pl = get_local_player(&j, mc);
        if ((*j.env)->IsSameObject(j.env, (jobject)o, (jobject)pl)) {
            void *w = get_world(&j, mc);
            HCtx c;
            c.a = &j; c.mgr = data_2f6f08; c.p10 = data_2f7240; c.mc = mc; c.world = w; c.player = pl;
            c.p30 = data_2f7218; c.p38 = data_2f7260;
            c.f40 = rdata_13e498[0x14] ? 0 : is_in_game(&j, mc);
            *(int *)&c.f41[0] = 0;
            c.f44 = 0;
            void *it = get_held_item(&j, get_net_handler(&j, mc));
            unsigned char flag = get_hurt_time(&j, pl) > 0 ? is_key_down(&j, it) : 0;
            VelEvent e = {0x15, 0, &c, x, y, z, flag, {}};
            dispatch_event(data_2f6f08, (Event *)&e);
            if(!source_mayu_move(env,(jobject)pl,e.x,e.y,e.z))set_motion(&j, pl, e.x, e.y, e.z);
            e.type = 0x16;
            dispatch_event(data_2f6f08, (Event *)&e);
            return;
        }
    }
    set_motion(&j, o, x, y, z);
}

// GUI tick: let modules ask for key pass-through, then forward movement keys into the open screen
FN(void, gui_key_passthrough, (Jvm *j, void *o));
FN(void *, get_current_screen, (Jvm *j, void *mc));
FN(void *, get_game_screen, (Jvm *j, void *x));
FN(int, get_event_key, (Jvm *j, void *x));
FN(unsigned char, keyboard_has_events, (Jvm *j));
FN(unsigned char, keyboard_has_next, (Jvm *j));
FN(unsigned char, keyboard_can_repeat, (Jvm *j));
FN(int, keyboard_next_key, (Jvm *j));
FN(void, keyboard_send_key, (Jvm *j, void *scr, int x, int y));
struct FlagEvent { int type; int pad; HCtx *ctx; unsigned char flag; char rest[0x37]; };
extern "C" void hook_update_controller(JNIEnv *env, jclass, void *o) {
    if (data_2f7259) return;
    Jvm j = {data_2f6ef0, env};
    gui_key_passthrough(&j, o);
    void *mc = get_minecraft(&j);
    void *pl = get_local_player(&j, mc);
    void *w = get_world(&j, mc);
    HCtx c;
    c.a = &j; c.mgr = data_2f6f08; c.p10 = data_2f7240; c.mc = mc; c.world = w; c.player = pl;
    c.p30 = data_2f7218; c.p38 = data_2f7260;
    c.f40 = rdata_13e498[0x14] ? 0 : is_in_game(&j, mc);
    *(int *)&c.f41[0] = 0;
    c.f44 = 0;
    FlagEvent e = {};
    e.type = 0x19;
    e.ctx = &c;
    dispatch_event(data_2f6f08, (Event *)&e);
    if (!(e.flag & 1)) return;
    void *scr = get_current_screen(&j, mc);
    int key = get_event_key(&j, get_game_screen(&j, get_net_handler(&j, mc)));
    while (keyboard_has_events(&j)) {
    }
    while (keyboard_has_next(&j)) {
        if (!scr || !keyboard_can_repeat(&j)) continue;
        int k = keyboard_next_key(&j);
        if (k == 1 || k == key) keyboard_send_key(&j, scr, 0, k);
    }
}

// ---- glClear(GL_DEPTH_BUFFER_BIT) hook: fire the 3D (6) and 2D (7) render events with GL state set up ----
struct Vec3 { double x, y, z; };
FN(void *, get_render_view_entity, (Jvm *j, void *mc));
FN(void *, get_timer, (Jvm *j, void *mc));
FN(float, get_partial_ticks, (Jvm *j, void *timer));
FN(Vec3, java_get_entity_pos_x, (Jvm *j, void *e));
FN(Vec3, java_get_entity_prev_pos_x, (Jvm *j, void *e));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(void, gl_push_state, (Jvm *j));
FN(void, gl_clear_color, (Jvm *j));
FN(void, gl_reset_state_1, (Jvm *j));
FN(void, gl_pop_state, (Jvm *j));
FN(void, gl_blend_mode, (Jvm *j, unsigned src, unsigned dst));
FN(void, gl_bind_texture, (Jvm *j));
FN(void, gl_reset_state_2, (Jvm *j));
FN(void, gl_begin_2d, (Jvm *j));
FN(void, gl_depth_mask, (Jvm *j));
FN(void, gl_set_color, (Jvm *j, float r, float g, float b, float a));
FN(void, set_silent_rotation_active, (Jvm *j, void *pl, bool on));
typedef void (*GlCap)(unsigned);
typedef void (*GlBlend)(unsigned, unsigned);
typedef void (*GlGetD)(unsigned, double *);
typedef void (*GlVoid)();
typedef void (*GlOrtho)(double, double, double, double, double, double);
typedef void (*GlColor4)(float, float, float, float);
extern "C" GlCap *rdata_13e450, *rdata_13e458, *rdata_13e440, *rdata_13e470;
extern "C" GlBlend *rdata_13e438;
extern "C" GlGetD *rdata_13e460;
extern "C" GlVoid *rdata_13e468;
extern "C" GlOrtho *rdata_13e478;
extern "C" GlColor4 *rdata_13e448;
extern "C" const float rdata_13e038;
extern "C" void *data_2f7280;
extern "C" int data_2f7294, data_2f7298;
extern "C" double data_2f72a0[16], data_2f72e0[16];
struct R3D { long long type; Vec3 cam; HCtx *ctx; float pt; unsigned char flag; char rest[0x1b]; };
struct R2D { long long type; Vec3 cam; HCtx *ctx; float w, h; double *proj, *mv; float pt; unsigned char flag, z5, z6, z7; };
extern "C" void render_world_events(JNIEnv *env, unsigned mask, unsigned char flag) {
    if (data_2f7259) return;
    if (!data_2f7280) return;
    if (mask == 0x100) {
        Jvm j = {data_2f6ef0, env};
        void *mc = get_minecraft(&j);
        void *pl = get_local_player(&j, mc);
        unsigned char silent = is_silent_rotation_active();
        if (silent) set_silent_rotation_active(&j, pl, true);
        unsigned char no3d = is_event_handled(data_2f6f08, 6);
        unsigned char no2d = is_event_handled(data_2f6f08, 7);
        if (!(no3d & no2d)) {
            void *w = get_world(&j, mc);
            void *view = get_render_view_entity(&j, mc);
            if (pl && w && view) {
                HCtx c;
                c.a = &j; c.mgr = data_2f6f08; c.p10 = data_2f7240; c.mc = mc; c.world = w; c.player = pl;
                c.p30 = data_2f7218; c.p38 = data_2f7260;
                c.f40 = rdata_13e498[0x14] ? 0 : is_in_game(&j, mc);
                *(int *)&c.f41[0] = 0;
                c.f44 = 0;
                float pt = get_partial_ticks(&j, get_timer(&j, mc));
                Vec3 cur = java_get_entity_pos_x(&j, view);
                Vec3 prev = java_get_entity_prev_pos_x(&j, view);
                Vec3 cam = vec3_lerp(prev, cur, (double)pt);
                if (flag) {
                    gl_push_state(&j); gl_clear_color(&j); gl_reset_state_1(&j); gl_pop_state(&j);
                    gl_blend_mode(&j, 0x302, 0x303);
                }
                GlCap *dis = rdata_13e450;
                (*dis)(0xb71); (*dis)(0xde1); (*dis)(0xb50);
                GlCap *en = rdata_13e458;
                (*en)(0xbe2);
                (*rdata_13e438)(0x302, 0x303);
                (*en)(0xb20);
                if (!no3d) {
                    R3D e = {6, cam, &c, pt, flag, {}};
                    dispatch_event(data_2f6f08, (Event *)&e);
                }
                if (!(no2d & 1)) {
                    GlGetD *get = rdata_13e460;
                    (*get)(0xba6, data_2f72a0);
                    (*get)(0xba7, data_2f72e0);
                    GlCap *mode = rdata_13e470;
                    (*mode)(0x1701);
                    GlVoid *push = rdata_13e468;
                    (*push)();
                    (*rdata_13e478)(0.0, (double)data_2f7294, (double)data_2f7298, 0.0, -3000.0, 3000.0);
                    (*mode)(0x1700);
                    (*push)();
                    R2D e = {7, cam, &c, (float)data_2f7294, (float)data_2f7298, data_2f72a0, data_2f72e0, pt, flag, 0, 0, 0};
                    dispatch_event(data_2f6f08, (Event *)&e);
                }
                if (flag) {
                    gl_bind_texture(&j); gl_reset_state_2(&j); gl_begin_2d(&j); gl_depth_mask(&j);
                    float one = rdata_13e038;
                    gl_set_color(&j, one, one, one, 1.0f);
                }
                (*en)(0xde1); (*en)(0xb71); (*en)(0xb44); (*en)(0xbc0);
                float one = rdata_13e038;
                (*rdata_13e448)(one, one, one, one);
            }
        }
        if (silent) set_silent_rotation_active(&j, pl, false);
    }
    (*rdata_13e440)(mask);
}

// ---- the client's input handler (installed in data_304ba8 for the game-window hook, see protected/input.cpp) ----
// Keeps the key-state table (byte per key: bit0 = down, bit1 = toggled by synthetic events), attaches to the JVM,
// lets the key-bind logic handle the event, and passes keys/buttons on to the registered key listeners.
// Returns true when the event was consumed.
struct InEvt { int type; int code; int pad8; unsigned char down, synthetic; };
struct VmFns { void *fn[8]; };
extern "C" VmFns **data_2f6ee0;                       // JavaVM*
extern "C" void *data_2f7218, *data_2f7230, *data_2f7238;   // key states, listener maps (typed / always)
extern "C" char data_2f6f08[];                        // module manager
extern "C" char *rdata_13e498, *rdata_13e490;          // settings blocks (+0x14: flag)
FN(unsigned char, gui_part_2, (InEvt *e, unsigned char unchanged, void *mgr));
FN(void *, get_minecraft, (Jvm *j));
FN(void, friends_friends_friend_list, (Jvm *j, void *mgr));
FN(bool, key_map_dispatch, (void *map, int id, int arg));
extern "C" bool on_input_event(InEvt *e) {
    bool unchanged = true;
    if (e->type == 2) {
        unsigned char *keys = (unsigned char *)data_2f7218;
        unsigned code = e->code;
        unsigned char cur = keys[code];
        unsigned char was = cur & 1;
        unsigned char down = e->down;
        if (!(e->synthetic == 1 && was == down)) {
            if (e->synthetic == 1) down = (down | (cur & 2)) ^ 2;
            keys[code] = down;
            unchanged = was == e->down;
        }
    }
    void *env;
    if (((int (*)(void *, void **, int))(*data_2f6ee0)->fn[6])(data_2f6ee0, &env, 0x10008)) return false;
    Jvm j = {data_2f6ef0, (JNIEnv *)env};
    bool r = gui_part_2(e, unchanged, data_2f6f08);
    if (r) return r;
    if (e->type != 2 || unchanged) return r;
    if (e->code == 0) return r;
    if (!is_in_game(&j, get_minecraft(&j))) return r;
    if (rdata_13e498[0x14] != 0) return r;
    if (rdata_13e490[0x14] == 1 && e->type == 2 && e->down == 1 && e->code == 3) friends_friends_friend_list(&j, data_2f6f08);
    if (key_map_dispatch(data_2f7230, e->code, e->down)) return r;
    key_map_dispatch(data_2f7238, e->code, e->down);
    return r;
}
