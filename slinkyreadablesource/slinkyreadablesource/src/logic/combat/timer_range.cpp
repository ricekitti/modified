// Timer Range - freezes the game timer, then catches up with a burst of ticks that
// carries us into hit range of the target.
//   0x43950 packet hook, 0x43970 disable, 0x43bf0 start a timer burst
//   0x42c60 tick: count the burst down, decide whether a burst is possible
//   0x42f00 render tick: release the freeze; simulate ticks ahead to find one that reaches the target
#include "slinky/engine.h"

typedef int v4i __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
struct Box { Vec3 lo, hi; };
struct Target { char pad0[8]; void *ent; double dist2; };
struct FrameEvt { char pad0[8]; Ctx *ctx; float partial; int pad14; double dt; };

struct PktHook { char pad0[0x24]; int verdict; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" char data_2f3160[];           // Timer Range module descriptor
extern "C" int data_2f3410;              // mode
extern "C" unsigned char data_2f3438;    // block while charging
extern "C" int data_3043d8;
extern "C" unsigned char data_3043dc;    // timer modified
extern "C" unsigned char data_3043e8;    // holding packets
extern "C" float rdata_154938;           // normal timer speed
extern "C" unsigned char data_3043dd;    // a burst would reach the target
extern "C" float data_3043e0, data_3043e4;   // last forward / strafe input
extern "C" double data_3043f0;           // cooldown
extern "C" double data_3043f8;           // freeze time left
extern "C" unsigned char data_2f3388;    // require mouse pressed
extern "C" unsigned char data_2f33a8;    // require weapon
extern "C" unsigned char data_2f33c8;    // require not using item
extern "C" float data_2f34a8, data_2f34e0;   // max ticks (mode 0 / mode 1)
extern "C" float data_2f3518;            // min range
extern "C" float data_2f3470;            // cooldown after a burst
extern "C" const double rdata_1548c0, rdata_1548d8, rdata_1548e0, rdata_154900, rdata_154908, rdata_154910,
                        rdata_154918, rdata_154920, rdata_154928, rdata_154930;
extern "C" const float rdata_1548d0;
extern "C" const v4i rdata_1548f0;       // sign mask

FN(void *, get_timer, (void *a, void *x));
FN(void, java_set_timer_timer_speed, (void *a, void *timer, float speed));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(unsigned char, item_sword, (void *a, void *item));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *hit));
FN(void, java_set_minecraft_left_click_counter, (void *a, void *x, int n));
FN(void *, java_get_game_settings_key_bindings_8, (void *a, void *gs));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *e));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(unsigned char, click_with_helper, (void *a, void *p));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(void *, get_held_item, (void *a, void *gs));
FN(unsigned char, is_key_down, (void *a, void *k));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height, (Vec3 v));
FN(Vec3, java_get_entity_other_player_mp_other_player_mpx, (void *a, void *e));
FN(unsigned char, set_vertex_pointer, (void *a, void *world, Vec3 from, Vec3 to));
FN(void, armor_weights, (void *a, void *gs));
FN(void, java_set_timer_elapsed_partial_ticks, (void *a, void *timer, float ticks));
FN(int, java_get_entity_other_player_mp_other_player_mppos_rotation_increments, (void *a, void *e));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(float, libc_cosf, (float x));     // cosf
FN(float, libc_sinf, (float x));     // sinf
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(Vec3, vec3_scale, (Vec3 v, double k));
FN(Vec3, vec3_reciprocal, (Vec3 v));
FN(unsigned char, aabb_ray_slab_test, (Box b, Vec3 origin, Vec3 dir));
FN(Vec3, aabb_clamp_point, (Box b, Vec3 p));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(float, java_get_entity_living_base_land_movement_factor, (void *a, void *p));
FN(Vec3, horizontal_motion, (Rot r, float fwd, float strafe, float speed));
FN(double, libc_hypot, (double x, double y));   // hypot

extern "C" void timer_range_hypot(Ctx *c);

static inline __attribute__((always_inline)) float neg(float x) {
    x = -x;
    return x;
}

extern "C" void timer_range_packet_hook_0x43970(PktHook *h) {
    if (__atomic_load_n(&data_3043e8, __ATOMIC_SEQ_CST) & 1)
        h->verdict = 1;
}

extern "C" void timer_range_part(EvHook *h) {
    if (h->ev == data_2f3160 && !h->on) {
        Ctx *c = h->ctx;
        void *timer = get_timer(c->a, c->c18);
        void *a = c->a;
        java_set_timer_timer_speed(a, timer, rdata_154938);
        data_3043dc = 0;
        data_3043d8 = 0;
        __atomic_exchange_n(&data_3043e8, (unsigned char)0, __ATOMIC_SEQ_CST);
    }

}

extern "C" void timer_range_hypot(Ctx *c) {
    void *timer = get_timer(c->a, c->c18);
    void *a = c->a;
    java_set_timer_timer_speed(a, timer, 0.0f);
    data_3043dc = 1;
    if (data_2f3410 == 1) {
        __atomic_exchange_n(&data_3043e8, (unsigned char)1, __ATOMIC_SEQ_CST);
    } else if (data_2f3438 == 1) {
        a = c->a;
        if (item_sword(a, item_held_hotbar_slot(a, c->b))) {
            void *k = java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18));
            java_set_key_binding_pressed(c->a, k, 1);
            java_get_key_binding_press_time_2(c->a, k);
            return;
        }
    }

}

extern "C" void timer_range_tick(Hook *h) {
    Ctx *c = h->ctx;
    if (data_3043d8 > 0) {
        void *gs = get_net_handler(c->a, c->c18);
        int n = --data_3043d8;
        if (!(n | data_2f3410))
            timer_range_hypot(c);
        if (c->f40 != 1 || !((Target *)c->p38)->ent)
            return;
        void *hit = java_get_minecraft_5(c->a, c->c18);
        if (!hit || java_get_moving_object_position_3(c->a, hit) != 2)
            return;
        java_set_minecraft_left_click_counter(c->a, c->c18, 0);
        java_get_key_binding_press_time_2(c->a, java_get_game_settings_key_bindings_8(c->a, gs));       // attack
        return;
    }
    data_3043dd = 0;
    if (c->f40 != 1)
        return;
    Target *t = (Target *)c->p38;
    if (!t->ent || rdata_1548c0 > t->dist2)
        return;
    if (!java_get_entity_on_ground(c->a, c->b) || java_get_entity_living_base_hurt_time(c->a, t->ent) > 1)
        return;
    if (data_2f3388 == 1 && !vertex_pointer_current(c))
        return;
    if (data_2f33a8 == 1 && !click_with_helper(c->a, c->b))
        return;
    if (data_2f33c8 == 1 && java_call_entity_player_is_using_item(c->a, c->b))
        return;
    if (java_get_entity_motion_x(c->a, c->b).y > 0.0)
        return;
    void *gs = get_net_handler(c->a, c->c18);
    if (is_key_down(c->a, get_held_item(c->a, gs)))
        return;
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    if (!java_variant(c->a))
        me = vec3_add_eye_height(me);
    Vec3 tp = java_get_entity_other_player_mp_other_player_mpx(c->a, t->ent);
    if (!java_variant(c->a))
        tp = vec3_add_eye_height(tp);
    if (!set_vertex_pointer(c->a, c->p20, me, tp))
        return;
    armor_weights(c->a, gs);
    data_3043dd = data_3043e0 > 0.0f;
}

extern "C" void timer_range_render_tick(FrameEvt *h) {
    Ctx *c = h->ctx;
    void *timer = get_timer(c->a, c->c18);
    if (data_3043dc == 1) {
        data_3043f8 -= h->dt;
        if (data_3043f8 > 0.0) {
            java_set_timer_timer_speed(c->a, timer, 0.0f);
            return;
        }
        java_set_timer_timer_speed(c->a, get_timer(c->a, c->c18), rdata_1548d0);
        data_3043dc = 0;
        __atomic_exchange_n(&data_3043e8, (unsigned char)0, __ATOMIC_SEQ_CST);
        int mode = data_2f3410;
        if (mode != 1 && data_2f3438 == 1) {
            if (item_sword(c->a, item_held_hotbar_slot(c->a, c->b)))
                java_set_key_binding_pressed(c->a, java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18)), 0);
            mode = data_2f3410;
        }
        if (mode == 1)
            java_set_timer_elapsed_partial_ticks(c->a, timer, (float)data_3043d8);
    }
    if (data_3043f0 > 0.0) {
        data_3043f0 -= h->dt;
        return;
    }
    if (data_3043dd != 1 || c->f40 != 1)
        return;
    Target *t = (Target *)c->p38;
    if (!t->ent)
        return;
    Vec3 motion = java_get_entity_motion_x(c->a, c->b);
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    if (!java_variant(c->a))
        me = vec3_add_eye_height(me);
    Vec3 tpos = java_get_entity_pos_x(c->a, t->ent);
    Vec3 tserver = java_get_entity_other_player_mp_other_player_mpx(c->a, t->ent);
    int steps = java_get_entity_other_player_mp_other_player_mppos_rotation_increments(c->a, t->ent);         // interpolation steps left
    Rot rot = get_entity_rotation(c->a, c->b);
    float ry = (float)(rdata_1548e0 - (double)rot.yaw * rdata_1548d8);
    float cy = libc_cosf(ry), sy = libc_sinf(ry);
    float rp = (float)((double)neg(rot.pitch) * rdata_1548d8);
    float cp = neg(libc_cosf(rp)), sp = libc_sinf(rp);
    Vec3 look = {(double)(sy * cp), (double)sp, (double)(cy * cp)};
    int max = (int)(data_2f3410 == 0 ? data_2f34a8 : data_2f34e0);
    for (int i = 1; i <= max; i++) {
        Box box;
        box.lo = vec3_sub(tpos, vec3_make(rdata_154908, rdata_154900, rdata_154908));
        box.hi = vec3_add(tpos, vec3_make(rdata_154908, rdata_154910, rdata_154908));
        Vec3 dir = vec3_reciprocal(vec3_scale(look, rdata_154918));
        if (aabb_ray_slab_test(box, me, dir)) {
            double d2 = vec3_distance_sq(me, aabb_clamp_point(box, me));
            if (d2 > 0.0) {
                if ((double)(data_2f3518 * data_2f3518) > d2)
                    return;                     // already too close
                if (data_2f3410 == 1)
                    timer_range_hypot(c);
                else
                    java_set_timer_elapsed_partial_ticks(c->a, timer, (float)i);
                data_3043d8 = i;
                data_3043f8 = (double)((float)i - h->partial) * rdata_154920;
                data_3043f0 = data_2f3470;
                return;
            }
        }
        // advance one tick: our movement ...
        Vec3 dv = horizontal_motion(rot, data_3043e0, data_3043e4, java_get_entity_living_base_land_movement_factor(c->a, c->b));
        motion = vec3_add(motion, dv);
        me = vec3_add(me, motion);
        motion = vec3_scale(motion, rdata_154928);
        if (data_2f3410 == 1) {
            double speed = libc_hypot(motion.x, motion.z);
            Vec3 rel = vec3_sub(me, tserver);
            Vec3 step = vec3_scale(rel, speed / libc_hypot(rel.x, rel.z));
            step.y = 0.0;
            tserver = vec3_add(tserver, step);
        }
        // ... and the target's interpolation towards its server position
        if (steps > 0) {
            Vec3 d = vec3_sub(tserver, tpos);
            tpos = vec3_add(tpos, vec3_scale(d, rdata_154930 / (double)steps));
            steps -= 1;
        }
    }
}
