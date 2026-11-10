// 0x35000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
FN(void, java_set_entity_living_base_jump_ticks, (void *a, void *b, int c));
extern "C" void no_jump_delay_tick(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1) java_set_entity_living_base_jump_ticks(c->a, c->b, 0);
}
