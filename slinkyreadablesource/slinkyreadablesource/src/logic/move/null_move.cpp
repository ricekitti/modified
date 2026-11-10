// Null Move - descriptor +0x148: per tick, drive four movement key states
// (forward/back/left/right style pairs) so that opposite keys cancel out.
// Each of the four states in 0x3049d0..0x3049dc follows the same pattern.
#include "slinky/engine.h"

extern "C" int data_3049d0;
extern "C" int data_3049d4;
extern "C" int data_3049d8;
extern "C" int data_3049dc;

FN(unsigned char, is_in_game, (void *a, void *x));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *x));
FN(unsigned char, is_key_down, (void *a, void *k));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));

// one key pair: `own` = key held, `opp` = opposite key held, `tgt` = key to release
#define PAIR(state, own, opp, tgt, ptype, zinit, cond)             \
    {                                                               \
        int s = state;                                              \
        unsigned char o = is_key_down(c->a, own);                      \
        ptype p = is_key_down(c->a, opp);                              \
        int n;                                                      \
        if (o) {                                                    \
            zinit                                                   \
            n = mode;                                               \
            if (cond)                                               \
                java_set_key_binding_pressed(c->a, tgt, 0);                             \
        } else if (s == 0) {                                        \
            n = 0;                                                  \
        } else {                                                    \
            unsigned char r = java_get_key_binding_key_code(c->a, tgt);                  \
            n = 0;                                                  \
            if (r)                                                  \
                java_set_key_binding_pressed(c->a, tgt, 1);                             \
        }                                                           \
        state = n;                                                  \
    }

extern "C" void null_move_part(Hook *h) {
    Ctx *c = h->ctx;
    if (!is_in_game(c->a, c->c18))
        return;
    int mode = java_get_entity_ticks_existed(c->a, c->b);
    void *obj = get_net_handler(c->a, c->c18);
    void *k0 = java_get_game_settings_key_bindings_4(c->a, obj);
    void *k1 = java_get_game_settings_key_bindings_6(c->a, obj);
    void *k2 = java_get_game_settings_key_bindings_5(c->a, obj);
    void *k3 = java_get_game_settings_key_bindings_7(c->a, obj);
    PAIR(data_3049d0, k0, k2, k2, bool, bool z = s == 0;, z & p)
    PAIR(data_3049d4, k1, k3, k3, unsigned char, unsigned char z = s == 0;, (z & p) == 1)
    PAIR(data_3049d8, k2, k0, k0, unsigned char, unsigned char z = s == 0;, (z & p) == 1)
    PAIR(data_3049dc, k3, k1, k1, unsigned char, unsigned char z = s == 0;, (z & p) == 1)
}
