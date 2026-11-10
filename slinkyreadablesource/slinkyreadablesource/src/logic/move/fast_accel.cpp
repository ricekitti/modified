// Fast Accel - descriptor +0x1a8: while moving on the ground, rescale the
// horizontal motion towards the speed the current input could reach after 20
// ticks of acceleration (capped by "acceleration").
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct MoveHook { char pad0[8]; Ctx *ctx; float strafe; float forward; float speed; unsigned char sprint; };  // +0x10/+0x14/+0x18/+0x1c

extern "C" float data_2f2a38;           // "acceleration"
extern "C" unsigned char data_2f2a58;   // "require_weapon"
extern "C" unsigned char data_2f2a78;   // "require_not_damaged"
extern "C" const float rdata_154700;    // minimum input length
extern "C" const double rdata_154708;   // per-tick friction factor
extern "C" const double rdata_154710;   // sprint bonus

FN(unsigned char, click_with_helper, (void *a, void *b));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(double, libc_hypot, (double x, double z));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(float, libc_hypotf_alt, (float x, float y));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(double, lerp_double, (double a, double b, double c));
FN(void, java_set_entity_motion_x, (void *a, void *p, Vec3 v));

extern "C" void fast_accel_part(MoveHook *h) {
    if (h->forward == 0.0f && h->strafe == 0.0f)
        return;
    Ctx *c = h->ctx;
    if (data_2f2a58 == 1 && !click_with_helper(c->a, c->b))
        return;
    if (data_2f2a78 == 1 && java_get_entity_living_base_hurt_time(c->a, c->b) > 0)
        return;
    Vec3 v = java_get_entity_motion_x(c->a, c->b);
    double d = libc_hypot(v.x, v.z);
    if (d == 0.0)
        return;
    if (java_get_entity_on_ground(c->a, c->b)) {
        float s = h->strafe, f = h->forward, sp = h->speed;
        float len = libc_hypotf_alt(s, f);
        float scale = sp / (len > rdata_154700 ? len : rdata_154700);
        s *= scale;
        scale *= f;
        double a = libc_hypotf_alt(s, scale);
        double t = 0.0;
        t += a;
        for (int i = 0; i < 19; i++) {
            t *= rdata_154708;
            t += a;
        }
        if (h->sprint == 1 && java_call_entity_is_sprinting(c->a, c->b))
            t += rdata_154710;
        if (d > t)
            return;
        double k = lerp_double(d, t, data_2f2a38);
        v.x = v.x / d * k;
           // keep the two components scalar (no SLP vectorisation)
        v.z = v.z / d * k;
        java_set_entity_motion_x(c->a, c->b, v);
    }
}
