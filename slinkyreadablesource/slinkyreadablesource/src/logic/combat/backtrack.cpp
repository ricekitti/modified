// Backtrack - hold the target's movement packets for up to a budget and release
// them on our own motion, explosions or a transaction mismatch.
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
struct Target { void *pad; void *ent; double dist2; };
struct RenderHook { char pad0[8]; Vec3 cam; char pad20[8]; float partial; };
struct PktHook { char pad0[8]; void *ctx; void *ev; long long budget; int seq; int verdict; };

extern "C" int data_2fc450;              // last released sequence
extern "C" int data_2fc454;              // last held sequence
extern "C" unsigned char data_2fc458;    // spin lock
extern "C" long long data_2fc460;        // max hold budget
extern "C" int data_2fc468;              // target entity id
extern "C" int data_2fc46c;              // own entity id
extern "C" unsigned char data_2fc470;    // active
extern "C" unsigned char data_2fc471;    // "stop_on_velocity"

extern "C" Vec3 data_2fc478;             // accumulated offset
extern "C" unsigned char data_2fc490, data_2fc498;
extern "C" float data_2fc494;
extern "C" unsigned char data_2fc4a0;    // render box
extern "C" Vec3 data_2fc4a8, data_2fc4c0, data_2fc4e8;   // previous / current / server position
extern "C" float data_2fc4d8, data_2fc4dc, data_2fc500;  // yaws
extern "C" int data_2fc4e0;              // cooldown ticks
extern "C" float data_2edd10;            // "line_width"
extern "C" float data_2edd20[3];         // "color"
extern "C" unsigned char data_2edd50;    // rotate box
extern "C" unsigned char data_2edd70;    // "filled"
extern "C" float data_2edda8;            // max target hurt time
extern "C" float data_2eddf0, data_2eddf4;   // min / max range
extern "C" float data_2ede30;            // max hold (ms)
extern "C" float data_2ede68;            // cooldown
extern "C" unsigned char data_2ede88, data_2edea8, data_2edec8, data_2edee8;
extern "C" void (**rdata_150cb0)(float);
extern "C" void (**rdata_150cc0)();
extern "C" void (**rdata_150cb8)();
extern "C" void (**rdata_150cd8)(float, float, float);
extern "C" void (**rdata_150cd0)(float, float, float);
extern "C" void (**rdata_150cc8)(float, float, float, float);
extern "C" void (**rdata_150ca0)(float, float, float);
extern "C" void (**rdata_13e448)(float, float, float, float);
extern "C" const double rdata_152178, rdata_152190;
extern "C" const float rdata_152180, rdata_152184, rdata_152188, rdata_15218c;

FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Vec3, vec3_add_eye_height_sneak, (Vec3 v));
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(void, gl_array_xyz_float, ());
FN(void, gl_array_color1, ());
FN(void, gl_array_color2, ());
FN(void, enable_vertex_array, ());
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, java_get_entity_other_player_mp_other_player_mpx, (void *a, void *e));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(int, java_get_entity_entity_id, (void *a, void *p));
FN(unsigned char, entity_reach_check, (Ctx *c, void *e, Vec3 eye));
FN(double, segment_distance, (Vec3 eye, Vec3 pos, Vec3 off));
FN(void, is_same_position, (Vec3 off, float yaw));
FN(void *, java_jni_is_instance_of_s19_packet_entity_status, (void *ctx, void *ev));
FN(int, java_get_s19_packet_entity_status_entity_id, (void *ctx, void *p));
FN(void *, java_jni_is_instance_of_s06_packet_update_health, (void *ctx, void *ev));
FN(void *, java_jni_is_instance_of_s29_packet_sound_effect, (void *ctx, void *ev));
FN(void *, java_jni_is_instance_of_s12_packet_entity_velocity, (void *ctx, void *ev));
FN(int, java_get_s12_packet_entity_velocity_entity_id, (void *ctx, void *p));
FN(void *, java_jni_is_instance_of_s08_packet_player_pos_look, (void *ctx, void *ev));
FN(void, check_entity_distance, (void **hook_ctx, unsigned char hold));

extern "C" void backtrack_packet_hook_under(PktHook *h) {
    volatile bool busy;
    while ((busy = __atomic_exchange_n(&data_2fc458, (unsigned char)1, __ATOMIC_SEQ_CST)))
        __builtin_ia32_pause();
    if (data_2fc470 == 1) {
        void *p = java_jni_is_instance_of_s19_packet_entity_status(h->ctx, h->ev);
        if (p) {
            if (java_get_s19_packet_entity_status_entity_id(h->ctx, p) == data_2fc468)
                goto pass;
        } else if (java_jni_is_instance_of_s06_packet_update_health(h->ctx, h->ev) || java_jni_is_instance_of_s29_packet_sound_effect(h->ctx, h->ev)) {
            goto pass;
        }
        {
            void *q = java_jni_is_instance_of_s12_packet_entity_velocity(h->ctx, h->ev);
            if (!q) {
                if (java_jni_is_instance_of_s08_packet_player_pos_look(h->ctx, h->ev))
                    goto stop;
            } else if (data_2fc471 && java_get_s12_packet_entity_velocity_entity_id(h->ctx, q) == data_2fc46c) {
                goto stop;
            }
        }
        if (h->budget <= data_2fc460) {
            h->verdict = 1;
            int seq = h->seq;
            if (seq > data_2fc450) {
                data_2fc450 = seq;
                check_entity_distance(&h->ctx, 0);
            }
        } else if (h->verdict == 0 && h->seq > data_2fc454) {
            check_entity_distance(&h->ctx, 1);
            data_2fc454 = h->seq;
        }
        goto unlock;
pass:
        h->verdict = 3;
        goto unlock;
stop:
        data_2fc470 = 0;
    }
unlock:
    data_2fc458 = 0;
}

static inline __attribute__((always_inline)) void lock() {
    while (__atomic_exchange_n(&data_2fc458, (unsigned char)1, __ATOMIC_SEQ_CST))
        __builtin_ia32_pause();
}

// render: box at the held (server-side) position of the target
extern "C" void backtrack_render_box_held(RenderHook *h) {
    if (data_2fc4a0 != 1)
        return;
    Vec3 rel = vec3_sub(vec3_lerp(data_2fc4a8, data_2fc4c0, (double)h->partial), h->cam);
    gl_array_xyz_float();
    (*rdata_150cb0)(data_2edd10);
    (*rdata_150cc0)();
    (*rdata_150cd8)((float)rel.x, (float)(rel.y + rdata_152178), (float)rel.z);
    float w = rdata_152184;
    (*rdata_150cd0)(w, rdata_152180, w);
    if (data_2edd50 == 1) {
        float yaw = lerp_unclamped_float(data_2fc4d8, data_2fc4dc, h->partial);
        (*rdata_150cc8)(rdata_152188 - yaw, 0.0f, rdata_152180, 0.0f);
    }
    if (data_2edd70 == 1) {
        (*rdata_13e448)(data_2edd20[0], data_2edd20[1], data_2edd20[2], rdata_15218c);
        gl_array_color1();
    }
    (*rdata_150ca0)(data_2edd20[0], data_2edd20[1], data_2edd20[2]);
    gl_array_color2();
    (*rdata_150cb8)();
    enable_vertex_array();
}

// tick: decide whether to start / keep backtracking the current target
extern "C" void backtrack_tick_decide_whether(Hook *h) {
    Ctx *c = h->ctx;
    int n = data_2fc4e0;
    if (n > 0) {
        data_2fc4e0 = n - 1;
        if (n != 1)
            return;
    }
    if (c->f40 != 1)
        goto reset;
    if (data_2ede88 == 1 && !vertex_pointer_current(c))
        goto reset;
    if (data_2edea8 == 1 && !click_with_helper(c->a, c->b))
        goto reset;
    {
        int ht = java_get_entity_living_base_last_attacker_time(c->a, c->b);
        if (java_get_entity_ticks_existed(c->a, c->b) - ht > 20)
            goto reset;
    }
    {
        Target *t = (Target *)c->p38;
        if (!t->ent)
            goto reset;
        if ((float)java_get_entity_living_base_hurt_time(c->a, t->ent) > data_2edda8)
            goto reset;
        double d2 = t->dist2;
        if ((double)(data_2eddf0 * data_2eddf0) > d2 || d2 > (double)(data_2eddf4 * data_2eddf4))
            goto reset;
        Vec3 eye = java_get_entity_pos_x(c->a, c->b);
        if (java_variant(c->a))
            eye = vec3_add_eye_height_sneak(eye);
        Vec3 tp = java_get_entity_other_player_mp_other_player_mpx(c->a, t->ent);
        float yaw = get_entity_rotation(c->a, t->ent).yaw;
        int tid = java_get_entity_entity_id(c->a, t->ent);
        int own = java_get_entity_entity_id(c->a, c->b);
        lock();
        data_2fc460 = (long long)data_2ede30;
        int old_tid = data_2fc468;
        data_2fc468 = tid;
        data_2fc46c = own;
        data_2fc471 = data_2edec8;
        if (data_2fc470 != 1) {
            if (!entity_reach_check(c, t->ent, eye)) {
                data_2fc4a0 = 0;
                data_2fc458 = 0;
                return;
            }
            data_2fc470 = 1;
            data_2fc478 = vec3_make(0.0, 0.0, 0.0);
            data_2fc490 = 0;
            data_2fc498 = 0;
            Vec3 p = java_get_entity_pos_x(c->a, t->ent);
            data_2fc4a8 = p;
            data_2fc4c0 = p;
            data_2fc4e8 = p;
            data_2fc4d8 = yaw;
            data_2fc4dc = yaw;
            data_2fc500 = yaw;
            data_2fc4a0 = data_2edee8;
            data_2fc458 = 0;
            return;
        }
        Vec3 off = data_2fc490 == 1 ? data_2fc478 : vec3_add(data_2fc478, tp);
        double d = segment_distance(eye, tp, off);
        if (rdata_152190 > d || old_tid != tid) {
            data_2fc478 = vec3_make(0.0, 0.0, 0.0);
            data_2fc490 = 0;
            data_2fc498 = 0;
            data_2fc4a0 = 0;
            data_2fc470 = 0;
            data_2fc4e0 = (int)data_2ede68;
            data_2fc458 = 0;
            return;
        }
        is_same_position(off, data_2fc498 ? data_2fc494 : yaw);
        data_2fc458 = 0;
        return;
    }
reset:
    lock();
    if (data_2fc470 == 1) {
        data_2fc4e0 = (int)data_2ede68;
        data_2fc470 = 0;
    }
    data_2fc458 = 0;
    data_2fc4a0 = 0;
}
