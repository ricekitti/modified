// Keep Sprint - descriptor +0x128 / +0x138 / +0x158.
// +0x128: on the attack tick, roll "chance" and remember the player's motion.
// +0x138: if the roll succeeded and the player is still moving the same way,
//         re-apply the saved motion scaled by "motion_mult".
// +0x158: afterwards re-assert sprint on the player.
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct PhaseHook { char pad0[8]; Ctx *ctx; unsigned char phase; };   // +0x10: phase flag

extern "C" float data_2ef940;           // "chance"
extern "C" float data_2ef978;           // "motion_mult"
extern "C" unsigned char data_2ef998;   // "require_on_ground"
extern "C" unsigned char data_2ef9b8;   // "allow_reduce"
extern "C" Vec3 data_2fc7a0;            // saved motion
extern "C" unsigned char data_2fc7b8;   // roll succeeded
extern "C" unsigned char data_2fc7b9;   // motion pending re-apply

FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(unsigned char, random_chance, (float chance));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(void, java_set_entity_motion_x, (void *a, void *p, Vec3 v));
FN(void, java_call_entity_set_sprinting, (void *a, void *p, bool on));

extern "C" void keep_sprint_part(PhaseHook *h) {
    if (h->phase == 1) {
        Ctx *c = h->ctx;
        unsigned char ok = 0;
        if (c->f40 == 1 && java_call_entity_is_sprinting(c->a, c->b) && random_chance(data_2ef940)) {
            data_2fc7a0 = java_get_entity_motion_x(c->a, c->b);
            ok = 1;
        }
        data_2fc7b8 = ok;
    }
}

extern "C" void keep_sprint_part_2(PhaseHook *h) {
    if (data_2fc7b8 != 1 || h->phase != 1)
        return;
    Ctx *c = h->ctx;
    if (!c->b)
        return;
    if (data_2ef998 == 1 && !java_get_entity_on_ground(c->a, c->b))
        return;
    if (java_get_entity_ticks_existed(c->a, c->b) != java_get_entity_living_base_last_attacker_time(c->a, c->b))
        return;
    if (data_2ef9b8 == 1 && *((unsigned char *)c->p38 + 0x18))
        return;
    double m = data_2ef978;
    data_2fc7a0.x *= m;
    data_2fc7a0.z *= m;
    void *a = c->a, *b = c->b;
       // original reloads the saved motion from memory here
    java_set_entity_motion_x(a, b, data_2fc7a0);
    data_2fc7b9 = 1;
}

extern "C" void keep_sprint_part_3(Hook *h) {
    if (data_2fc7b9 == 1) {
        data_2fc7b9 = 0;
        Ctx *c = h->ctx;
        if (c->b) {
            java_call_entity_set_sprinting(c->a, c->b, true);
            return;
        }
    }

}
