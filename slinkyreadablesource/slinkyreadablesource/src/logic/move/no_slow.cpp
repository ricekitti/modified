// No Slow - descriptor +0x198: rescale the movement slow-down while using an item.
#include "slinky/engine.h"

extern "C" float data_2f2ed0;  // "mult_bow"
extern "C" float data_2f2f08;  // "mult_sword"
extern "C" float data_2f2f40;  // "mult_consumables"

struct SlowState { char pad0[8]; Ctx *ctx; float move[2]; };  // +0x10: forward/strafe

FN(bool, java_call_entity_player_is_using_item, (void *a, void *b));
FN(void *, item_held_hotbar_slot, (void *a, void *b));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));

extern "C" void no_slow_part(SlowState *s) {
    Ctx *c = s->ctx;
    if (!java_call_entity_player_is_using_item(c->a, c->b))
        return;
    void *item = item_held_hotbar_slot(c->a, c->b);
    if (!item)
        return;
    // item id - 261 (bow); swords are ids 267, 268, 272, 276, 283
    unsigned k = java_call_static_item_get_id_from_item(c->a, item) - 261;
    const float *mult;
    if (__builtin_expect(k > 22, 0))
        mult = &data_2f2f40;
    else if (__builtin_expect((0x4088c0u >> k) & 1, 1))
        mult = &data_2f2f08;
    else if (__builtin_expect(k != 0, 0))
        mult = &data_2f2f40;
    else
        mult = &data_2f2ed0;
    double m = *mult / 0.2;
    s->move[0] = (float)(s->move[0] * m);
    s->move[1] = (float)(s->move[1] * m);
}
