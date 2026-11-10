// Anti Debuff - descriptor +0x168: strip blindness / nausea effects.
#include "slinky/engine.h"

extern "C" unsigned char data_2f3a88;  // "blindness"
extern "C" unsigned char data_2f3aa8;  // "nausea"

FN(void *, java_potion_potion_types, (void *a, int effect_id));
FN(void *, java_call_entity_living_base_get_active_potion_effect, (void *a, void *b, void *effect));
FN(void, java_set_potion_effect_duration, (void *a, void *effect, int c));
FN(void, java_set_entity_player_sp_time_in_portal, (void *a, void *b, float v));
FN(void, java_set_entity_player_sp_prev_time_in_portal, (void *a, void *b, float v));

extern "C" void anti_debuff_part(Hook *h) {
    Ctx *c = h->ctx;
    if (data_2f3a88 == 1) {
        void *fx = java_call_entity_living_base_get_active_potion_effect(c->a, c->b, java_potion_potion_types(c->a, 15));
        if (fx)
            java_set_potion_effect_duration(c->a, fx, 0);
    }
    if (data_2f3aa8 == 1) {
        java_set_entity_player_sp_time_in_portal(c->a, c->b, 0.0f);
        java_set_entity_player_sp_prev_time_in_portal(c->a, c->b, 0.0f);
        return;
    }

}
