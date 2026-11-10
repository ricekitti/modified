// Item Use Fix - descriptor +0x128 / +0x138.
// The first hook records whether the held item is usable (data_3043d4); the
// second re-applies that state on the following tick.
#include "slinky/engine.h"

extern "C" unsigned char data_3043d4;  // item-use flag

struct UseState { char pad0[8]; Ctx *ctx; unsigned char skip; };  // skip @ +0x10

FN(void *, java_get_minecraft_4, (void *a, void *x));
FN(unsigned char, java_get_player_controller_mp_is_hitting_block, (void *a, void *item));
FN(void, java_set_player_controller_mp_is_hitting_block, (void *a, void *item, int on));

extern "C" void item_use_fix_part(UseState *s) {
    if (__builtin_expect(s->skip != 0, 0))
        goto out;
    {
        Ctx *c = s->ctx;
        void *item = java_get_minecraft_4(c->a, c->c18);
        unsigned char usable = java_get_player_controller_mp_is_hitting_block(c->a, item);
        data_3043d4 = usable != 0;
        if (usable) {
            java_set_player_controller_mp_is_hitting_block(c->a, item, 0);
            return;
        }
    }
out:

}

extern "C" void item_use_fix_part_2(UseState *s) {
    if (data_3043d4 == 1 && s->skip == 0) {
        Ctx *c = s->ctx;
        void *item = java_get_minecraft_4(c->a, c->c18);
        void *a = c->a;
        unsigned char on = data_3043d4;
        java_set_player_controller_mp_is_hitting_block(a, item, on);
    }
}
