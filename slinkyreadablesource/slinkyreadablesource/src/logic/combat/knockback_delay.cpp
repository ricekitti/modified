// Knockback Delay - descriptors +0x68 (packet hook) / +0x168 (tick).
// +0x168 decides each tick whether delaying is allowed (air / weapon / mouse /
// facing a target in range / chance roll) and publishes it atomically.
// +0x68 holds incoming packets while our own velocity packet is pending, up to
// "max_delay" ms, then releases them.
#include "slinky/engine.h"

typedef float v4f __attribute__((vector_size(16), aligned(16)));
typedef int v4i __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
struct PktHook { char pad0[8]; void *ctx; void *ev; long long budget; int id; int verdict; };  // +0x18/+0x20/+0x24

extern "C" float data_2f4338;            // "max_delay"
extern "C" unsigned char data_2f4358;    // "require_air"
extern "C" float data_2f4390;            // "range"
extern "C" unsigned char data_2f43b0;    // "require_weapon"
extern "C" unsigned char data_2f43d0;    // "require_mouse"
extern "C" unsigned char data_2f43f0;    // "require_target"
extern "C" float data_2f4428;            // "chance"
extern "C" unsigned char data_304998;    // holding
extern "C" int data_30499c;              // held transaction id
extern "C" unsigned char data_3049a0;    // in air
extern "C" unsigned char data_3049a1;    // allowed
extern "C" unsigned short data_3049a2;   // max delay (ms)
extern "C" int data_3049a4;              // own entity id
extern "C" char data_3049a8[];           // rng state
extern "C" const double rdata_154fa0;    // max target distance^2
extern "C" const v4f rdata_154fb0;       // abs mask
extern "C" const float rdata_154fc0;     // max yaw difference
extern "C" const double rdata_154fc8;
extern "C" const double rdata_154fd0;
extern "C" const double rdata_154fd8;
extern "C" const double rdata_154fe0;

typedef unsigned char (*EntityPred)(void *self, void *player, void *entity);

FN(void *, java_jni_is_instance_of_s12_packet_entity_velocity, (void *ctx, void *ev));
FN(int, java_get_s12_packet_entity_velocity_entity_id, (void *ctx, void *pkt));
FN(void *, java_jni_is_instance_of_s19_packet_entity_status, (void *ctx, void *ev));
FN(void *, java_jni_is_instance_of_s06_packet_update_health, (void *ctx, void *ev));
FN(void *, java_jni_is_instance_of_s29_packet_sound_effect, (void *ctx, void *ev));
FN(int, java_get_entity_entity_id, (void *a, void *p));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(void *, java_get_world, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(Rot, rot_look_at, (Vec3 from, Vec3 to));
FN(float, wrap_angle, (float a));
FN(void *, rot_make_floats, (float lo, float hi));
FN(unsigned char, walk_should_change, (void *st, void *rng, double p));

#define ALOAD(x) __atomic_load_n(&(x), __ATOMIC_SEQ_CST)
#define AXCHG(x, v) __atomic_exchange_n(&(x), (v), __ATOMIC_SEQ_CST)

extern "C" void knockback_delay_part(PktHook *h) {
    if (h->verdict != 0)
        return;
    bool mine = false;
    void *vel = java_jni_is_instance_of_s12_packet_entity_velocity(h->ctx, h->ev);
    if (vel)
        mine = java_get_s12_packet_entity_velocity_entity_id(h->ctx, vel) == ALOAD(data_3049a4);
    unsigned char act;
    if (ALOAD(data_3049a0) & 1) {
        act = h->budget == 0 ? (ALOAD(data_3049a1) & 1) : 0;
        data_304998 = act;
    } else if ((ALOAD(data_3049a1) & mine) & 1) {
        data_304998 = 1;
        act = 1;
    } else {
        act = data_304998;
    }
    if ((mine & act) == 1) {
        int st = data_30499c;
        int id = h->id;
        if (st == 0)
            data_30499c = id;
        else if (st != id)
            goto reset;
    } else if (!act) {
        return;
    }
    if (h->budget > (long long)ALOAD(data_3049a2)) {
reset:
        data_304998 = 0;
        AXCHG(data_3049a1, (unsigned char)0);
        data_30499c = 0;
        return;
    }
    h->verdict = (java_jni_is_instance_of_s19_packet_entity_status(h->ctx, h->ev) || java_jni_is_instance_of_s06_packet_update_health(h->ctx, h->ev) || java_jni_is_instance_of_s29_packet_sound_effect(h->ctx, h->ev)) ? 3 : 1;
}

extern "C" void knockback_delay_part_2(Hook *h) {
    Ctx *c = h->ctx;
    if (!c->b)
        return;
    AXCHG(data_3049a4, java_get_entity_entity_id(c->a, c->b));
    unsigned char air;
    if (data_2f4358 == 1)
        air = java_get_entity_on_ground(c->a, c->b);
    else
        air = 0;
    AXCHG(data_3049a0, air);
    unsigned char ok = 0;
    struct Target { void *pad; void *ent; double dist2; } *t = (Target *)c->p38;
    if (!t->ent || t->dist2 > (double)(data_2f4390 * data_2f4390))
        goto out;
    if (data_2f43b0 == 1 && !click_with_helper(c->a, c->b))
        goto out;
    if (data_2f43d0 == 1 && !vertex_pointer_current(c))
        goto out;
    if (data_2f43f0 == 1) {
        Vec3 eye = java_get_entity_pos_x(c->a, c->b);
        Rot rot = get_entity_rotation(c->a, c->b);
        void *list = java_get_world(c->a, c->p20);
        int i = 0;
        for (;; i++) {
            if (i >= java_call_list_size(c->a, list))
                goto out;
            void *e = java_call_list_get(c->a, list, i);
            void *obj = ((void **)c->a)[1];
            EntityPred pred = (EntityPred)(*(void ***)obj)[0xc0 / 8];
            if (pred(obj, c->b, e))
                continue;
            Vec3 p = java_get_entity_pos_x(c->a, e);
            if (vec3_distance_sq(eye, p) > rdata_154fa0)
                continue;
            v4f d = {wrap_angle(rot_look_at(eye, p).yaw - rot.yaw), 0, 0, 0};
            d = (v4f)((v4i)d & (v4i)rdata_154fb0);
            if (d[0] > rdata_154fc0)
                continue;
            break;
        }
    }
    {
        double ch = data_2f4428;
        ok = 1;
        if (!(ch > rdata_154fc8))
            ok = walk_should_change(data_3049a8, rot_make_floats((float)(rdata_154fd0 + ch), (float)(ch + rdata_154fd8)), rdata_154fe0);
    }
out:
    AXCHG(data_3049a1, ok);
    AXCHG(data_3049a2, (unsigned short)(int)data_2f4338);
}
