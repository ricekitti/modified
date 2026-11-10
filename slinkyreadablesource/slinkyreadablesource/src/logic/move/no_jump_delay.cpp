// No Jump Delay - descriptor +0x158: clear the jump cooldown while the flag is set.
#include "slinky/engine.h"

FN(void, java_set_entity_living_base_jump_ticks, (void *a, void *b, int c));

extern "C" void no_jump_delay_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1) {
        java_set_entity_living_base_jump_ticks(c->a, c->b, 0);
        return;
    }

}
