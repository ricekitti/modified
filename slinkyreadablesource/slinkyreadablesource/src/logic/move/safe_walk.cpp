// Safe Walk - descriptor +0x48: scale horizontal motion while sneaking at an edge.
#include "slinky/engine.h"

extern "C" float data_2f2270;           // "motion_multiplier"
extern "C" unsigned char data_2f2290;   // "require_looking_down"
extern "C" unsigned char data_2f22b0;   // "require_not_forward"
extern "C" unsigned char data_2f22d0;   // "require_holding_blocks"
extern "C" unsigned char data_3043c8;   // safe-walk active

struct V3 { double x, y, z; };
struct WalkState { char pad0[8]; Ctx *ctx; };

FN(unsigned long long, get_entity_rotation, (void *a, void *b));   // packed rotation (pitch in high half)
FN(float, java_get_entity_living_base_move_forward, (void *a, void *b));
FN(void *, item_held_hotbar_slot, (void *a, void *b));
FN(unsigned char, block_entity_id, (void *a, void *item));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *b));
FN(void, java_get_entity_motion_x, (V3 *out, void *a, void *b));
FN(void, java_set_entity_motion_x, (void *a, void *b, V3 v));

extern "C" void safe_walk_part(WalkState *s) {
    Ctx *c = s->ctx;
    if (c->f40 != 1)
        goto out;
    if (data_2f2290 == 1) {
        float pitch = __builtin_bit_cast(float, (unsigned)(get_entity_rotation(c->a, c->b) >> 32));
        if (pitch < 65.0f)
            goto clear;
    }
    if (data_2f22b0 == 1) {
        if (java_get_entity_living_base_move_forward(c->a, c->b) > 0.0f)
            goto clear;
    }
    if (data_2f22d0 == 1) {
        void *item = item_held_hotbar_slot(c->a, c->b);
        if (!block_entity_id(c->a, item))
            goto clear;
    }
    data_3043c8 = 1;
    if (!java_get_entity_on_ground(c->a, c->b))
        goto out;
    if (data_2f2270 == 1.0f)
        goto out;
    {
        V3 v;
        java_get_entity_motion_x(&v, c->a, c->b);
        double m = data_2f2270;
        v.x *= m;
        v.z *= m;
        void *wa = c->a, *wb = c->b;

        java_set_entity_motion_x(wa, wb, v);
    }
    goto out;
clear:
    data_3043c8 = 0;
out:

}
