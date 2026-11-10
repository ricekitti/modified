// 0x43000: armor-piece flags -> two weights
#include "slinky/engine.h"
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *e));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *e));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *e));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *e));
FN(unsigned char, is_key_down, (void *a, void *x));
extern "C" float rdata_15493c, rdata_154940, data_3043e0, data_3043e4;
extern "C" void armor_weights(void *a, void *ent) {
    void *i0 = java_get_game_settings_key_bindings_4(a, ent);
    void *i1 = java_get_game_settings_key_bindings_5(a, ent);
    void *i2 = java_get_game_settings_key_bindings_6(a, ent);
    void *i3 = java_get_game_settings_key_bindings_7(a, ent);
    float c1 = rdata_15493c;
    float x = c1;
    if (!is_key_down(a, i0)) x = 0.0f;
    float c2 = rdata_154940;
    if (is_key_down(a, i1)) x += c2;
    float y = c1;
    if (!is_key_down(a, i2)) y = 0.0f;
    if (is_key_down(a, i3)) y += c2;
    data_3043e0 = x;
    data_3043e4 = y;
}
