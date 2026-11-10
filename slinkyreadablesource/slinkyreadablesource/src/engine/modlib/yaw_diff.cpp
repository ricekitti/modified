// 0x4a000: yaw-difference test
#include "slinky/engine.h"
struct Rot { float yaw, pitch; };
typedef float v4f __attribute__((vector_size(16)));
typedef int v4i __attribute__((vector_size(16)));
extern "C" v4i rdata_1566a0;   // fabs mask
extern "C" float rdata_1566b0;
FN(double, java_get_entity_other_player_mp_other_player_mpyaw, (void *a, void *e));
FN(float, wrap_angle, (float x));
extern "C" bool yaw_diff_check(void **c, Rot r, void *ent) {
    float d = wrap_angle((float)java_get_entity_other_player_mp_other_player_mpyaw(*c, ent) - r.yaw);
    v4f v = {d, 0, 0, 0};
    v = (v4f)((v4i)v & rdata_1566a0);
    return rdata_1566b0 > v[0];
}
