// Velocity - knockback modification: save the motion at hit time and apply the
// configured reduction.
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
typedef float v4f __attribute__((vector_size(16), aligned(16)));
typedef int v4i __attribute__((vector_size(16), aligned(16)));
struct Rot { float yaw, pitch; };
struct Range { float lo, hi; };
struct PhaseHook { char pad0[8]; Ctx *ctx; unsigned char phase; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" char data_2ea7f0[];           // Velocity module descriptor
extern "C" unsigned char data_2f9d89;    // tick motion saved
extern "C" Vec3 data_2f9d90;             // motion at the last tick
extern "C" Vec3 data_2f9dc0;             // motion at our last hit
extern "C" unsigned data_2eaa50;         // mode
extern "C" float data_2eaa90;            // "chance"
extern "C" unsigned data_2eab68;         // keep vertical: 1 = in air, 2 = always
extern "C" unsigned char data_2f9d88;    // own velocity packet seen (atomic)
extern "C" char data_2f9da8[];           // rng state
extern "C" int data_2f9db8;              // own entity id
extern "C" unsigned char data_2f9dbc;    // strong knockback (atomic)
extern "C" const float rdata_150e20;
extern "C" unsigned char data_2eaab0;    // "require_moving"
extern "C" unsigned char data_2eaaf0;    // "require_weapon"
extern "C" unsigned char data_2eab90;    // "every_hit"
extern "C" unsigned char data_2f9dd8;    // reduced this combo
extern "C" const double rdata_150e50;    // reduce factor
extern "C" double rdata_150e28, rdata_150e30, rdata_150e38, rdata_150e40, rdata_150e48;
struct PktHook { char pad0[8]; void *ctx; void *ev; char pad18[12]; int verdict; };

FN(unsigned char, velocity_shared_conditions_moving, (Ctx *c));
extern "C" unsigned char data_2eaad0, data_2eab10, data_2eacc8;
extern "C" float data_2eabc8, data_2eac00, data_2eac38, data_2eac70, data_2eaca8;
extern "C" int data_2f9ddc;              // delay ticks
extern "C" unsigned char data_2f9de0;    // jump key pressed by us
extern "C" const float rdata_150e58;
extern "C" const double rdata_150e60, rdata_150e68, rdata_150e70, rdata_150e78, rdata_150e80, rdata_150e88, rdata_150eb0;
extern "C" const v4i rdata_150ec0;
extern "C" const float rdata_150ed0;
typedef unsigned char (*EntityPred)(void *self, void *player, void *entity);
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(void *, java_get_world, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(Rot, rot_look_at, (Vec3 from, Vec3 to));
FN(float, wrap_angle, (float a));
FN(int, java_get_entity_entity_id, (void *a, void *p));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, get_held_item, (void *a, void *x));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(unsigned char, is_key_down, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(void *, java_potion_potion_types, (void *a, int id));
FN(void *, java_call_entity_living_base_get_active_potion_effect, (void *a, void *p, void *effect));
FN(double, libc_hypot, (double x, double z));
FN(float, libc_sinf, (float v));
FN(float, libc_cosf, (float v));
FN(double, libc_acos, (double v));
FN(float, java_get_entity_living_base_move_forward, (void *a, void *p));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(Range, rot_make_floats, (float lo, float hi));
FN(unsigned char, walk_should_change, (void *st, Range r, double p));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(void, java_set_entity_motion_x, (void *a, void *p, Vec3 v));
FN(void *, java_jni_is_instance_of_s12_packet_entity_velocity, (void *ctx, void *ev));
FN(int, java_get_s12_packet_entity_velocity_entity_id, (void *ctx, void *p));
FN(void *, java_jni_is_instance_of_s27_packet_explosion, (void *ctx, void *ev));
FN(float, java_get_s27_packet_explosion_f, (void *ctx, void *p));
FN(float, java_get_s27_packet_explosion_h, (void *ctx, void *p));
FN(void *, java_jni_is_instance_of_s08_packet_player_pos_look, (void *ctx, void *ev));

FN(Vec3, java_get_entity_motion_x, (void *a, void *p));

extern "C" void velocity_disable(EvHook *h) {
    if (h->ev == data_2ea7f0)
        data_2f9d89 = 0;
}

extern "C" void velocity_attack(PhaseHook *h) {
    if (h->phase != 1)
        return;
    Ctx *c = h->ctx;
    if (c->b)
        data_2f9dc0 = java_get_entity_motion_x(c->a, c->b);
}

extern "C" void velocity_tick(Hook *h) {
    Ctx *c = h->ctx;
    if (c->b) {
        data_2f9d90 = java_get_entity_motion_x(c->a, c->b);
        data_2f9d89 = 1;
    }
}

// mode 3 ("delayed"): after our own velocity packet, restore the motion of
// the previous tick (optionally keeping the new vertical motion)
extern "C" void velocity_mode_3_delayed(Hook *h) {
    if (data_2eaa50 != 3)
        return;
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    if (__builtin_expect(__atomic_load_n(&data_2f9d88, __ATOMIC_SEQ_CST) & 1, 1)) {
        __atomic_exchange_n(&data_2f9d88, (unsigned char)0, __ATOMIC_SEQ_CST);
        return;
    }
    if (data_2f9d89 != 1 || !velocity_shared_conditions_moving(c))
        return;
    float ch = data_2eaa90;
    if (!(ch > rdata_150e20)) {
        double p = (float)((double)ch * rdata_150e28);
        if (!walk_should_change(data_2f9da8, rot_make_floats((float)(rdata_150e30 + p), (float)(p + rdata_150e38)), rdata_150e40))
            return;
    }
    Vec3 v = java_get_entity_motion_x(c->a, c->b);
    v.x = data_2f9d90.x;
    unsigned keep = data_2eab68;
    if (keep == 2 || (keep == 1 && !java_get_entity_on_ground(c->a, c->b)))
        v.y = data_2f9d90.y;
    v.z = data_2f9d90.z;
    java_set_entity_motion_x(c->a, c->b, v);
    data_2f9d89 = 0;
}

// packet hook: watch our own velocity packet / explosions / strong knockback
extern "C" void velocity_packet_hook_watch(PktHook *h) {
    if (__builtin_expect(h->verdict != 0, 1))
        goto end;
    {
        void *p = java_jni_is_instance_of_s12_packet_entity_velocity(h->ctx, h->ev);
        if (p) {
            if (java_get_s12_packet_entity_velocity_entity_id(h->ctx, p) == data_2f9db8)
                __atomic_exchange_n(&data_2f9dbc, (unsigned char)1, __ATOMIC_SEQ_CST);
            goto end;
        }
        void *q = java_jni_is_instance_of_s27_packet_explosion(h->ctx, h->ev);
        if (q) {
            float x = java_get_s27_packet_explosion_f(h->ctx, q);
            float z = java_get_s27_packet_explosion_h(h->ctx, q);
            __atomic_exchange_n(&data_2f9dbc, (unsigned char)((double)(x * x + z * z) > rdata_150e48), __ATOMIC_SEQ_CST);
            goto end;
        }
        if (java_jni_is_instance_of_s08_packet_player_pos_look(h->ctx, h->ev))
            __atomic_exchange_n(&data_2f9d88, (unsigned char)1, __ATOMIC_SEQ_CST);
    }
end:

}

// mode 2 ("reduce"): on our hit while the target is hurt, scale the saved motion
extern "C" void velocity_mode_2_reduce(PhaseHook *h) {
    if (data_2eaa50 != 2)
        return;
    if (data_2f9dd8 == 1 && !data_2eab90)
        return;
    if (h->phase != 1)
        return;
    Ctx *c = h->ctx;
    if (!c->b || *((unsigned char *)c->p38 + 0x18) != 1)
        return;
    if (data_2eaab0 == 1 && 0.0f >= java_get_entity_living_base_move_forward(c->a, c->b))
        return;
    if (data_2eaaf0 == 1 && !click_with_helper(c->a, c->b))
        return;
    if (java_get_entity_ticks_existed(c->a, c->b) != java_get_entity_living_base_last_attacker_time(c->a, c->b))
        return;
    double k = rdata_150e50;
    data_2f9dc0.x *= k;
    data_2f9dc0.z *= k;
    void *a = c->a, *b = c->b;
       // original reloads the motion from memory here
    java_set_entity_motion_x(a, b, data_2f9dc0);
    data_2f9dd8 = 1;
}

// shared conditions (moving / mouse / weapon / facing a target in range)
extern "C" unsigned char velocity_shared_conditions_moving_2(Ctx *c) {
    if (data_2eaab0 == 1 && 0.0f >= java_get_entity_living_base_move_forward(c->a, c->b))
        return 0;
    if (data_2eaad0 == 1 && !vertex_pointer_current(c))
        return 0;
    if (data_2eaaf0 == 1 && !click_with_helper(c->a, c->b))
        return 0;
    if (data_2eab10 != 1)
        return 1;
    Vec3 eye = java_get_entity_pos_x(c->a, c->b);
    Rot rot = get_entity_rotation(c->a, c->b);
    void *list = java_get_world(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *obj = ((void **)c->a)[1];
        if (((EntityPred)(*(void ***)obj)[0xc0 / 8])(obj, c->b, e))
            continue;
        Vec3 p = java_get_entity_pos_x(c->a, e);
        if (vec3_distance_sq(eye, p) > rdata_150eb0)
            continue;
        v4f d = {wrap_angle(rot_look_at(eye, p).yaw - rot.yaw), 0, 0, 0};
        d = (v4f)((v4i)d & rdata_150ec0);
        if (d[0] > rdata_150ed0)
            continue;
        return 1;
    }
    return 0;
}

static inline __attribute__((always_inline)) bool roll(float ch) {
    if (ch > rdata_150e58)
        return true;
    double p = (float)((double)ch * rdata_150e60);
    return walk_should_change(data_2f9da8, rot_make_floats((float)(rdata_150e68 + p), (float)(p + rdata_150e70)), rdata_150e78);
}

// tick: mode 0 = cancel horizontal knockback after a delay when hit from the
// front, mode 1 = jump reset
extern "C" void velocity_tick_mode_0(Hook *h) {
    data_2f9dd8 = 0;
    if (data_2eaa50 == 2)
        return;
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    __atomic_exchange_n(&data_2f9db8, java_get_entity_entity_id(c->a, c->b), __ATOMIC_SEQ_CST);
    unsigned mode = data_2eaa50;
    if (mode == 1) {
        if (data_2f9de0 == 1) {
            void *jk = get_held_item(c->a, get_net_handler(c->a, c->c18));
            void *a = c->a;
            java_set_key_binding_pressed(a, jk, java_get_key_binding_key_code(a, jk));
            data_2f9de0 = 0;
            return;
        }
        if (!velocity_shared_conditions_moving_2(c) || !roll(data_2eaca8))
            return;
        if (data_2eacc8 == 1 && !java_call_entity_is_sprinting(c->a, c->b))
            return;
        if (!java_get_entity_on_ground(c->a, c->b))
            return;
        if (0.0 >= java_get_entity_motion_x(c->a, c->b).y)
            return;
        if (java_call_entity_living_base_get_active_potion_effect(c->a, c->b, java_potion_potion_types(c->a, 8)))
            return;
        void *jk = get_held_item(c->a, get_net_handler(c->a, c->c18));
        if (is_key_down(c->a, jk))
            return;
        java_set_key_binding_pressed(c->a, jk, 1);
        data_2f9de0 = 1;
        return;
    }
    if (mode != 0)
        return;
    int n = data_2f9ddc;
    if (n <= 0) {
        unsigned char expected = 1;
        if (!__atomic_compare_exchange_n(&data_2f9dbc, &expected, 0, false, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST))
            return;
        if (!velocity_shared_conditions_moving_2(c) || !roll(data_2eabc8))
            return;
        Vec3 v = java_get_entity_motion_x(c->a, c->b);
        double d = libc_hypot(v.x, v.z);
        if (d == 0.0)
            return;
        float yaw = get_entity_rotation(c->a, c->b).yaw;
        float ang = (float)((double)yaw * rdata_150e80 / rdata_150e88 + rdata_150e80);
        float sn = libc_sinf(ang);
        float cs = libc_cosf(ang);
        double q = libc_acos(((double)sn * v.x + (double)cs * v.z) / d);
        if (!((double)data_2eac70 >= q))
            return;
        n = (int)data_2eac00;
        data_2f9ddc = n;
        if (n <= 0)
            goto apply;
    }
    data_2f9ddc = n - 1;
    if (n - 1 != 0)
        return;
apply:
    {
        Vec3 v = java_get_entity_motion_x(c->a, c->b);
        double f = data_2eac38;
        v.x *= f;
        v.z *= f;
        java_set_entity_motion_x(c->a, c->b, v);
    }
}
