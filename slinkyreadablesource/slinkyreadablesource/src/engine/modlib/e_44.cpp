// 0x44000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
FN(void *, java_potion_potion_types, (void *a, int id));
FN(void *, java_call_entity_living_base_get_active_potion_effect, (void *a, void *b, void *effect));
FN(void, java_set_potion_effect_duration, (void *a, void *effect, int c));
FN(void, java_set_entity_player_sp_time_in_portal, (void *a, void *b, float v));
FN(void, java_set_entity_player_sp_prev_time_in_portal, (void *a, void *b, float v));
extern "C" unsigned char data_2f3a88, data_2f3aa8;
struct Ev { char pad[8]; Ctx *ctx; };
extern "C" void zero_motion_tick(Ev *ev) {
    Ctx *c = ev->ctx;
    if (data_2f3a88 == 1) {
        void *e = java_potion_potion_types(c->a, 0xf);
        void *p = java_call_entity_living_base_get_active_potion_effect(c->a, c->b, e);
        if (p) java_set_potion_effect_duration(c->a, p, 0);
    }
    if (data_2f3aa8 == 1) {
        java_set_entity_player_sp_time_in_portal(c->a, c->b, 0.0f);
        java_set_entity_player_sp_prev_time_in_portal(c->a, c->b, 0.0f);
    }
}
