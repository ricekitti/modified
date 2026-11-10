// Aim Assist - picks a target and turns the view towards a randomised point on it.
//   0x2b5b0 disable: drop the cached global ref   0x2b610 input flags
//   0x2b630 tick: choose / keep the target and its aim point
//   0x2b200 frame: turn towards the interpolated aim point
//   0x2bf40 apply one frame of rotation (deadzone, speed, randomisation, mouse gating)
//   0x2c3b0 test one entity, updating the best-so-far scores
#include "slinky/engine.h"

typedef float v4f __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
struct Box { Vec3 lo, hi; };
struct Range { float lo, hi; };
struct FrameEvt { char pad0[8]; Ctx *ctx; float partial; int pad14; double dt; };

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
typedef void (*DeleteRef)(void *env, void *ref);

extern "C" char data_2e8cc0[];           // Aim Assist module descriptor
extern "C" unsigned char data_2e9068;
extern "C" unsigned char data_2f7bd8;
extern "C" void *data_2f7be0;            // cached target ref
extern "C" unsigned char data_2f7c20;    // aiming
extern "C" Vec3 data_2f7be8, data_2f7c00;    // previous / current aim point
extern "C" float data_2f7c18, data_2f7c1c;   // mouse movement since the last tick
extern "C" int data_2f7c78;              // clicks to wait before aiming
extern "C" unsigned long long data_2f7c80;   // tie-break counter
extern "C" char data_2f7c28[], data_2f7c38[], data_2f7c48[], data_2f7c58[], data_2f7c68[];   // RNG states
extern "C" int data_2e9020;              // mode (1 clamp, 2 instant, 3 silent)
extern "C" unsigned char data_2e8fa8;    // lock on vertical
extern "C" unsigned char data_2e8fc8;    // require mouse moved
extern "C" unsigned char data_2e9048;    // silent: ignore manual aim
extern "C" unsigned char data_2e90d8;    // require sprint
extern "C" unsigned char data_2e90f8;    // require mouse pressed
extern "C" unsigned char data_2e9118;    // disable on block break
extern "C" unsigned char data_2e9138;    // require weapon
extern "C" int data_2e9180;              // re-pick the target every tick
extern "C" unsigned char data_2e9288;    // ignore invisible
extern "C" unsigned char data_2e92a8;    // allow exceeding the fov
extern "C" unsigned char data_2e92c8;    // require line of sight
extern "C" int data_2e9320;              // sort by: distance, health, hurt time, fov
extern "C" float data_2e8f10;            // deadzone
extern "C" float data_2e8f14;            // fov
extern "C" float data_2e8f50, data_2e8f88;   // horizontal / vertical speed
extern "C" float data_2e90b0;            // near distance
extern "C" float data_2e90b4;            // range
extern "C" float data_2e91c0;            // predict
extern "C" float data_2e91f8, data_2e9230;   // horizontal / vertical hitbox correction
extern "C" float data_2e9268;            // aim randomisation
extern "C" const float rdata_14fef0, rdata_14fef4, rdata_14ff00, rdata_14ff04, rdata_14ff30, rdata_14ff34,
                       rdata_14ff38, rdata_14ff3c, rdata_14ff88, rdata_14ff8c;
extern "C" const double rdata_14ff08, rdata_14ff20, rdata_14ff28, rdata_14ff40, rdata_14ff60, rdata_14ff70,
                        rdata_14ff78, rdata_14ff80, rdata_14ffa0, rdata_14ffa8;
extern "C" const double rdata_14ff90[2];
extern "C" const v4f rdata_14ff10;       // abs mask
extern "C" const v4f rdata_14ff50;       // sign mask

FN(unsigned char, increment_wrap, (int ms));
FN(unsigned char, is_silent_rotation_active, ());
FN(Rot, get_render_rotation, ());
FN(Rot, get_render_state, ());
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(Rot, rot_add, (Rot a, Rot b));
FN(Rot, rot_sub, (Rot a, Rot b));
FN(Rot, rot_scale, (Rot r, float k));
FN(float, rot_length, (Rot r));
FN(float, wrap_angle, (float deg));
FN(void, apply_silent_rotation, (Rot r));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height, (Vec3 v));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(Vec3, vec3_scale, (Vec3 v, double k));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Rot, rot_look_at, (Vec3 from, Vec3 to));
FN(Range, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, Range r, double d));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(unsigned char, random_chance, (float chance));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *hit));
FN(unsigned char, click_with_helper, (void *a, void *p));
FN(void *, java_get_world, (void *a, void *world));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(unsigned char, java_get_entity_is_dead, (void *a, void *e));
FN(unsigned char, java_get_entity_player_sleeping, (void *a, void *e));
FN(unsigned char, java_call_entity_player_is_invisible_to_player, (void *a, void *e, void *viewer));
FN(unsigned char, entity_flags_has, (void *a, void *e, int checks));
FN(Vec3, java_get_entity_other_player_mp_other_player_mpx, (void *a, void *e));
FN(void *, java_get_entity, (void *a, void *e));
FN(Box, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(Box, aabb_expand, (Box b, double grow));
FN(Vec3, aabb_clamp_point, (Box b, Vec3 p));
FN(double, vec3_distance, (Vec3 a, Vec3 b));
FN(double, lerp_double, (double a, double b, double t));
FN(float, noise_1d, (float x));             // noise
FN(float, java_call_entity_living_base_get_health, (void *a, void *e));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *e));
FN(unsigned char, set_vertex_pointer, (void *a, void *world, Vec3 from, Vec3 to));
FN(int, java_get_entity_entity_id, (void *a, void *e));

typedef unsigned char (*IsSameObject)(void *env, void *a, void *b);
typedef void *(*NewGlobalRef)(void *env, void *o);

static inline __attribute__((always_inline)) float fabs_(float x) {
    x = __builtin_fabsf(x);
    return x;
}
static inline __attribute__((always_inline)) float neg(float x) {
    x = -x;
    return x;
}
static inline __attribute__((always_inline)) Rot wrap(Rot r) {
    r.yaw = wrap_angle(r.yaw);
    return r;
}

extern "C" unsigned char aim_assist_test_entity_updating(Ctx *c, void *e, Vec3 *eye, Rot rot, unsigned char sticky, double *range,
                                     float *best_hp, int *best_hurt, unsigned *best_tie, float *fov,
                                     Vec3 *aim_out, unsigned char *far_out);
extern "C" void aim_assist_apply_frame_rotation(Rot d, float dt);
extern "C" void aim_assist_disable(EvHook *h) {
    if (h->ev == data_2e8cc0 && !h->on) {
        void *ref = data_2f7be0;
        if (ref) {
            void *env = ((void **)h->ctx->a)[1];
            ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, ref);
        }
        data_2f7be0 = nullptr;
        data_2f7bd8 = 0;
    }

}

extern "C" void aim_assist_part(char *h) {
    if (data_2f7c20 == 1) {
        h[8] = 1;
        if (data_2e9068 == 1)
            h[9] = 1;
    }
}

extern "C" unsigned char aim_assist_test_entity_updating(Ctx *c, void *e, Vec3 *eye, Rot rot, unsigned char sticky, double *range,
                                     float *best_hp, int *best_hurt, unsigned *best_tie, float *fov,
                                     Vec3 *aim_out, unsigned char *far_out) {
    if (java_get_entity_is_dead(c->a, e) || java_get_entity_player_sleeping(c->a, e))
        return 0;
    if (data_2e9288 == 1 && java_call_entity_player_is_invisible_to_player(c->a, e, c->b))
        return 0;
    if (entity_flags_has(c->a, e, 7))
        return 0;
    Vec3 pos = java_get_entity_pos_x(c->a, e);
    if (data_2e91c0 > 0.0f)
        pos = vec3_lerp(pos, java_get_entity_other_player_mp_other_player_mpx(c->a, e), data_2e91c0);
    Box bb = aabb_expand(java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, e)), rdata_14ff70);
    Vec3 near = aabb_clamp_point(bb, *eye);
    double dist = vec3_distance(*eye, near);
    unsigned char far = !((double)data_2e90b0 > dist);
    if (dist > *range)
        return 0;
    Vec3 base = vec3_add_eye_height(pos);
    double ax, az;
    float hc = data_2e91f8;
    if (hc > 0.0f) {
        if (rdata_14ff78 > dist)
            hc = (float)(hc * (dist / rdata_14ff78));
        Vec3 p = vec3_lerp(base, near, hc);
        ax = p.x;
        az = p.z;
    } else {
        ax = base.x;
        az = base.z;
    }
    double ay = lerp_double(base.y, near.y, data_2e9230);
    float seed = (float)((eye->x + eye->y + eye->z + ax + ay + az) * rdata_14ff80);
    float s1 = rdata_14ff88 + seed, s2 = rdata_14ff8c + seed;
    float n1 = noise_1d(seed), n2 = noise_1d(s1);
    double n3 = (double)noise_1d(s2) * rdata_14ffa0;
    float rnd = data_2e9268;
    if (rdata_14ff78 > dist)
        rnd = (float)(rnd * (dist / rdata_14ff78 * rdata_14ffa8 + rdata_14ff80));
    Vec3 jitter = vec3_scale(Vec3{(double)n1 * rdata_14ff90[0], (double)n2 * rdata_14ff90[1], n3}, rnd);
    Vec3 aim = vec3_add(Vec3{ax, ay, az}, jitter);
    float off = fabs_(wrap_angle(rot_sub(rot_look_at(*eye, aim), rot).yaw));
    if (off > *fov) {
        if (!sticky)
            return 0;
        far = data_2e92a8;
    }
    float hp = java_call_entity_living_base_get_health(c->a, e);
    if (hp > *best_hp)
        return 0;
    int hurt = java_get_entity_living_base_hurt_time(c->a, e);
    if (hurt > *best_hurt)
        return 0;
    if (data_2e92c8 == 1 && !set_vertex_pointer(c->a, c->p20, *eye, aim))
        return 0;
    if (!data_2e9180) {
        *fov = off;
    } else {
        switch (data_2e9320) {
        case 0:
            *range = dist;
            break;
        case 1:
            *best_hp = hp;
            break;
        case 2: {
            unsigned x = java_get_entity_entity_id(c->a, e) + (unsigned)data_2f7c80;
            x = (x >> 16 ^ x) * 0x45d9f3b;
            x = (x >> 16 ^ x) * 0x45d9f3b;
            x = x >> 16 ^ x;
            data_2f7c80 += 1;
            if (hurt == *best_hurt && x < *best_tie)
                return 0;
            *best_hurt = hurt;
            *best_tie = x;
            break;
        }
        case 3:
            *fov = off;
            break;
        }
    }
    *aim_out = aim;
    *far_out = far;
    return 1;
}

extern "C" void aim_assist_apply_frame_rotation(Rot d, float dt) {
    float y = d.yaw, p = d.pitch;
    const float dead = data_2e8f10;
    if (y > 0.0f) {
        y -= dead;
        y = y > 0.0f ? y : 0.0f;
    } else {
        y += dead;
        y = y < 0.0f ? y : 0.0f;
    }
    if (p > 0.0f) {
        p -= dead;
        p = p > 0.0f ? p : 0.0f;
    } else {
        p += dead;
        p = p < 0.0f ? p : 0.0f;
    }
    if (data_2e9020 == 2) {
        if (!data_2e8fa8)
            p = 0.0f;
    } else {
        // pitch follows less while the yaw error is large
        float r = walk_update(data_2f7c28, rot_make_floats(rdata_14ff00, rdata_14ff04), rdata_14ff08);
        float k = (float)(rdata_14ff20 / (double)r * (double)fabs_(y) + rdata_14ff28);
        p *= k > 0.0f ? k : 0.0f;
        float hs = data_2e8f50, vs = data_2e8f88;
        if (data_2e9020 == 3) {
            const float m = rdata_14ff30;
            hs = m > hs ? m : hs;
            vs = m > vs ? m : vs;
        }
        hs *= rdata_14ff34;
        vs *= rdata_14ff34;
        float ry = walk_update(data_2f7c48, rot_make_floats(rdata_14ff38, rdata_14ff3c), rdata_14ff28) * (hs * hs);
        float rp = walk_update(data_2f7c58, rot_make_floats(rdata_14ff38, rdata_14ff3c), rdata_14ff28) * (vs * vs);
        float rs = walk_update(data_2f7c68, Range{0.95f, 1.05f}, rdata_14ff40);
        if (data_2e9020 == 1) {
            float ly = ry * rdata_14ff30 * rs * dt;
            y = limit_rotation_delta(neg(ly), ly, y);
            float lp = rp * rdata_14ff30 * rs * dt;
            p = limit_rotation_delta(neg(lp), lp, p);
        } else {
            Rot step = rot_scale(rot_scale(Rot{ry * y, rp * p}, rs), dt);
            if (!(fabs_(step.yaw) > fabs_(y)))
                y = step.yaw;
            if (!(fabs_(step.pitch) > fabs_(p)))
                p = step.pitch;
        }
        if (data_2e9020 != 2) {
            float j = (float)((double)fabs_(y) * rdata_14ff60);
            p += walk_update(data_2f7c38, rot_make_floats(neg(j), j), rdata_14ff28);
        }
    }
    if (data_2e8fc8 == 1) {                     // never turn faster than the mouse moved
        float ay = fabs_(y), ap = fabs_(p);
        float mx = fabs_(data_2f7c18), my = fabs_(data_2f7c1c);
        bool cut_y = mx < ay && y != 0.0f;
        float ny = mx / ay * y;
        if (my < ap && p != 0.0f)
            p *= my / ap;
        data_2f7c18 = 0.0f;
        data_2f7c1c = 0.0f;
        if (cut_y)
            y = ny;
    }
    apply_silent_rotation(Rot{y, p});
}

extern "C" void aim_assist_frame(FrameEvt *h) {
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    if (!data_2f7bd8) {
        if (data_2f7c20 != 1)
            return;
        if (!is_silent_rotation_active() || !increment_wrap(100)) {
            data_2f7c20 = 0;
            return;
        }
        Rot cur = rot_add(get_entity_rotation(c->a, c->b), get_render_state());
        Rot d = wrap(rot_sub(get_render_rotation(), cur));  // hand the silent rotation back
        if (rdata_14fef0 > rot_length(d))
            data_2f7c20 = 0;
        aim_assist_apply_frame_rotation(d, (float)h->dt);
        return;
    }
    if (!increment_wrap(100))
        return;
    if (!data_2f7c20 && data_2e9020 == 3)
        data_2f7c20 = 1;
    if (data_2f7c20 && !is_silent_rotation_active())
        return;
    float partial = h->partial;
    Vec3 prev = java_get_entity_prev_pos_x(c->a, c->b);
    Vec3 pos = java_get_entity_pos_x(c->a, c->b);
    Vec3 eye = vec3_add(prev, vec3_scale(vec3_sub(pos, prev), partial));
    if (!java_variant(c->a))
        eye = vec3_add_eye_height(eye);
    Vec3 aim = vec3_lerp(data_2f7be8, data_2f7c00, h->partial);
    Rot want = rot_look_at(eye, aim);
    Rot cur = rot_add(get_entity_rotation(c->a, c->b), get_render_state());
    Rot d = wrap(rot_sub(want, cur));
    if (data_2e9020 == 3 && !data_2e9048) {
        Rot back = wrap(rot_sub(get_render_rotation(), cur));
        Rot rest = wrap(rot_sub(want, get_render_rotation()));
        if (rdata_14fef0 > rot_length(back) && rot_length(d) > rot_length(rest)) {
            apply_silent_rotation(back);
            return;
        }
    }
    aim_assist_apply_frame_rotation(d, (float)h->dt);
}

extern "C" void aim_assist_tick(Hook *h) {
    *(unsigned long long *)&data_2f7c18 = 0;
    Ctx *c = h->ctx;
    void *env;
    void *best = nullptr;
    Vec3 eye, aim;
    Rot rot;
    double range;
    unsigned char far;
    struct { float fov, hp; int hurt; unsigned tie; } s;
    if (c->f40 != 1)
        goto clear;
    if (data_2e90d8 == 1 && data_2e9020 != 3 && !java_call_entity_is_sprinting(c->a, c->b))
        goto lost;
    {
        unsigned char pressed = vertex_pointer_current(c);
        if (data_2e90f8 == 1) {
            if (!pressed) {
                data_2f7c78 = random_chance(rdata_14fef4) + 1;
                goto clear;
            }
            if ((unsigned)data_2e9020 < 2 && data_2f7c78) {
                data_2f7c78 -= 1;
                goto clear;
            }
        }
        if ((pressed & data_2e9118) == 1) {
            void *hit = java_get_minecraft_5(c->a, c->c18);
            if (hit && java_get_moving_object_position_3(c->a, hit) == 1)
                goto lost;                      // breaking a block
        }
    }
    if (data_2e9138 == 1 && !click_with_helper(c->a, c->b))
        goto clear;
    eye = java_get_entity_pos_x(c->a, c->b);
    if (!java_variant(c->a))
        eye = vec3_add_eye_height(eye);
    rot = is_silent_rotation_active() ? get_render_rotation() : get_entity_rotation(c->a, c->b);
    range = data_2e90b4;
    s.fov = data_2e8f14;
    *(unsigned *)&s.hp = 0x7f7fffff;            // FLT_MAX
    s.hurt = 0x7fffffff;
    s.tie = 0;
    if (!data_2e9180 && data_2f7be0) {          // keep the current target while it stays valid
        double r = range;
        struct { float fov, hp; int hurt; unsigned tie; } k;
        k.fov = data_2e8f14;
        *(unsigned *)&k.hp = 0x7f7fffff;
        k.hurt = 0x7fffffff;
        k.tie = 0;
        if (aim_assist_test_entity_updating(c, data_2f7be0, &eye, rot, 1, &r, &k.hp, &k.hurt, &k.tie, &k.fov, &aim, &data_2f7bd8)) {
            data_2f7be8 = data_2f7c00;
            data_2f7c00 = aim;
            return;
        }
        env = ((void **)c->a)[1];
        ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, data_2f7be0);
        data_2f7be0 = nullptr;
        data_2f7bd8 = 0;
    }
    {
        void *list = java_get_world(c->a, c->p20);
        for (int i = 0; i < java_call_list_size(c->a, list); i++) {
            void *e = java_call_list_get(c->a, list, i);
            env = ((void **)c->a)[1];
            if (((IsSameObject)(*(void ***)env)[0xc0 / 8])(env, c->b, e))
                continue;
            if (aim_assist_test_entity_updating(c, e, &eye, rot, 0, &range, &s.hp, &s.hurt, &s.tie, &s.fov, &aim, &far))
                best = e;
        }
    }
    if (!best)
        goto clear;
    env = ((void **)c->a)[1];
    if (((IsSameObject)(*(void ***)env)[0xc0 / 8])(env, data_2f7be0, best)) {
        data_2f7be8 = data_2f7c00;
        data_2f7c00 = aim;
    } else {
        data_2f7c00 = aim;
        data_2f7be8 = aim;
    }
    data_2f7bd8 = far;
    if (data_2f7be0)
        ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, data_2f7be0);
    if (void *ref = ((NewGlobalRef)(*(void ***)env)[0xa8 / 8])(env, best))
        data_2f7be0 = ref;
    return;
clear:
    if (data_2f7be0) {
        env = ((void **)c->a)[1];
        ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, data_2f7be0);
    }
    data_2f7be0 = nullptr;
lost:
    data_2f7bd8 = 0;
}
