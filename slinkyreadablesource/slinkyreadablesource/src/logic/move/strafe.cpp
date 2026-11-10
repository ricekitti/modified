// Strafe - descriptors +0x198 (remember ground state) / +0x1a8 (steer motion).
// +0x1a8 pulls the horizontal motion towards the direction the movement keys
// ask for, by "strength_air" / "strength_jump" / "strength_ground".
#include "slinky/engine.h"

typedef float __m128 __attribute__((vector_size(16), aligned(16)));
typedef int __m128i_ __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct MoveHook { char pad0[8]; Ctx *ctx; float strafe; float forward; float speed; unsigned char sprint; };  // +0x10/+0x14/+0x1c

extern "C" float data_2f2728;           // "max_hurt_time"
extern "C" float data_2f2760;           // "strength_air"
extern "C" float data_2f2798;           // "strength_jump"
extern "C" float data_2f27d0;           // "strength_ground"
extern "C" unsigned char data_2f27f0;   // "require_weapon"
extern "C" unsigned char data_3043cc;   // was on ground last tick
extern "C" const double rdata_1545f0;
extern "C" const double rdata_1545f8;
extern "C" const double rdata_154600;
extern "C" const double rdata_154608;
extern "C" const double rdata_154610;
extern "C" const double rdata_154618;
extern "C" const double rdata_154620;
extern "C" const double rdata_154628;
extern "C" const __m128 rdata_154630;      // sign mask

FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(double, libc_hypot, (double x, double z));
FN(unsigned, get_entity_rotation, (void *a, void *p));
FN(float, libc_sinf, (float v));
FN(float, libc_cosf, (float v));
FN(double, lerp_double, (double a, double b, double c));
FN(void, java_set_entity_motion_x, (void *a, void *p, Vec3 v));

extern "C" void strafe_part(Hook *h) {
    Ctx *c = h->ctx;
    data_3043cc = java_call_entity_is_sprinting(c->a, c->b);
}

extern "C" void strafe_part_2(MoveHook *h) {
    if (h->forward == 0.0f && h->strafe == 0.0f)
        return;
    Ctx *c = h->ctx;
    if (data_2f27f0 == 1 && !click_with_helper(c->a, c->b))
        return;
    if ((float)java_get_entity_living_base_hurt_time(c->a, c->b) > data_2f2728)
        return;
    const float *strength;
    unsigned char boost;
    if (java_get_entity_on_ground(c->a, c->b)) {
        if (h->sprint == 1) {
            boost = data_3043cc ^ 1;

            strength = &data_2f2798;
        } else {
            boost = 0;
            strength = &data_2f27d0;
        }
    } else {
        boost = 0;
        strength = &data_2f2760;
    }
    float str = *(volatile const float *)strength;
    if (str == 0.0f)
        return;
    Vec3 v = java_get_entity_motion_x(c->a, c->b);
    double d = libc_hypot(v.x, v.z);
    if (boost)
        d += rdata_1545f0;
    unsigned yawbits = get_entity_rotation(c->a, c->b);
    double a = 0.0;
    if (0.0f > h->strafe)
        a = rdata_1545f8;
    if (h->strafe > 0.0f)
        a = rdata_154600;
    double y = __builtin_bit_cast(float, yawbits);
    if (h->forward > 0.0f) {
        a *= rdata_154620;
    } else if (0.0f > h->forward) {
        y += rdata_154610;
        a *= rdata_154618;
    } else {
        a *= rdata_154608;
    }
    a += y;
    a *= rdata_154628;
    a /= rdata_154610;
    float ang = (float)a;
    double vx = v.x;
    __m128 t = {libc_sinf(ang), 0, 0, 0};
    __m128 nm = (__m128)((__m128i_)t ^ (__m128i_)rdata_154630);
    float nc = nm[0];
    v.x = lerp_double(vx, (double)nc * d, str);
    double vz = v.z;
    v.z = lerp_double(vz, (double)libc_cosf(ang) * d, str);
    java_set_entity_motion_x(c->a, c->b, v);
}
