// 0x45000: throwable start position and velocity (vanilla EntityThrowable maths)
#include "slinky/engine.h"
struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
FN(float, libc_sinf, (float x));   // sinf
FN(float, libc_cosf, (float x));   // cosf
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(Vec3, vec3_scale, (Vec3 v, double k));
FN(Vec3, vec3_add_eye_height, (Vec3 v));
FN(unsigned char, java_variant, (void *a));
FN(unsigned char, is_sneaking, (void *a, void *b));
typedef int v4i __attribute__((vector_size(16)));
typedef float v4f __attribute__((vector_size(16)));
extern "C" v4i rdata_154e60;   // sign mask
extern "C" const double rdata_154e50, rdata_154e70, rdata_154e78, rdata_154e80;
extern "C" float data_30498c, data_304990;
static inline __attribute__((always_inline)) float neg(float x) {
    v4f v = {x, 0, 0, 0};
    return ((v4f)((v4i)v ^ rdata_154e60))[0];
}
extern "C" void throwable_velocity(Ctx *c, Vec3 *pos, Rot r, float t, Vec3 *outPos, Vec3 *outVel) {
    double k = rdata_154e50;
    float yaw = (float)((double)r.yaw * k);
    float cy = libc_cosf(yaw);
    float pitch = (float)((double)r.pitch * k);
    float cp = libc_cosf(pitch);
    float sy = libc_sinf(yaw);
    float sp = libc_sinf(pitch);
    Vec3 dir = {(double)(neg(sy) * cp), (double)neg(sp), (double)(cp * cy)};
    Vec3 vel = vec3_scale(dir, (double)lerp_unclamped_float(data_30498c, data_304990, t));
    if (!java_variant(c->a)) {
        *pos = vec3_add_eye_height(*pos);
        if (is_sneaking(c->a, c->b)) pos->y += rdata_154e70;
    }
    pos->x -= (double)cy * rdata_154e78;
    pos->y += rdata_154e80;
    pos->z -= (double)sy * rdata_154e78;
    *outPos = *pos;
    *outVel = vel;
}
