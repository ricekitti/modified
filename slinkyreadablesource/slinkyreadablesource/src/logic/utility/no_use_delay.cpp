// No Use Delay - descriptor +0x168: shorten the item-use delay.
#include "slinky/engine.h"

extern "C" unsigned char data_2f1438;  // "allow_desync"
extern "C" int data_301e90;            // remaining use-delay ticks
extern "C" int data_301e94;            // last seen slot/state

struct DelayState { char pad0[8]; Ctx *ctx; };

FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(unsigned char, is_key_down, (void *a, void *x));
FN(void *, java_get_entity_player_2, (void *a, void *b));
FN(int, java_get_inventory_player_current_item, (void *a, void *x));
FN(int, java_get_key_binding_key_code, (void *a, void *x));
FN(void, java_set_key_binding_pressed, (void *a, void *x, int v));
FN(int, java_get_entity_player_item_in_use_count, (void *a, void *b));

extern "C" void no_use_delay_part(DelayState *s) {
    Ctx *c = s->ctx;
    if (c->f40 == 0) {
        data_301e90 = 0;
        return;
    }
    void *it = java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18));
    unsigned char busy = is_key_down(c->a, it);
    int slot = java_get_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b));
    if (slot != data_301e94) {
        data_301e90 = 0;
        data_301e94 = slot;
        void *a = c->a;
        java_set_key_binding_pressed(a, it, java_get_key_binding_key_code(a, it));
    }
    if (!busy)
        return;
    int w = java_get_entity_player_item_in_use_count(c->a, c->b);
    int cnt = data_301e90;
    if (__builtin_expect(cnt <= 0, 0)) {
        data_301e90 = w;
        return;
    }
    cnt--;
    data_301e90 = cnt;
    if (cnt != 0) {
        unsigned char ok = (w == 0) & data_2f1438;

        if (ok != 1)
            return;
    }
    java_set_key_binding_pressed(c->a, it, 0);
}
