// No Hit Delay - descriptor +0x128.
#include "slinky/engine.h"

extern "C" unsigned data_2f0dc0;  // "mode" current value

struct World { int *state; };
struct HitState { char pad0[8]; Ctx *ctx; unsigned char on; };

FN(int, java_get_minecraft_left_click_counter, (void *a, void *x));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void, java_set_minecraft_left_click_counter, (void *a, void *x, int c));
FN(bool, java_call_entity_player_is_using_item, (void *a, void *b));
FN(void, java_jni_call_nonvirtual_void_method_entity_living_base_swing_item, (void *a, void *b));

extern "C" void no_hit_delay_part(HitState *s) {
    if (s->on != 1)
        return;
    Ctx *c = s->ctx;
    if (*((World *)c->a)->state == 0)
        return;
    if (java_get_minecraft_left_click_counter(c->a, c->c18) <= 0)
        return;
    if (data_2f0dc0 == 1) {
        void *t = java_get_minecraft_5(c->a, c->c18);
        if (!t || java_get_moving_object_position_3(c->a, t) == 0)
            goto next;
    }
    java_set_minecraft_left_click_counter(c->a, c->c18, 0);
    return;
next:
    if (java_call_entity_player_is_using_item(c->a, c->b))
        return;
    java_jni_call_nonvirtual_void_method_entity_living_base_swing_item(c->a, c->b);
}
