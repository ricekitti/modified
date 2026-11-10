// Player ESP — Highlights other players.
// category: visual   descriptor: 0x2e9e80   settings block: 0x2ea078
// functions: gui player_esp_gui, register player_esp_register, status player_esp_status
// implementation: src/logic/visual/player_esp.cpp
#include "slinky/api.h"
#include "slinky/heavy_glow.h"

S(150bd9); // "Main"
S(150bde); // "Customization"
S(150bec); // "mode"
S(150bf1); // "range"
S(150bf7); // "hide_friendlies"
S(150c07); // "color_mode"
S(150c12); // "color_enemies"
S(150c20); // "color_friendlies"
S(150c31); // "override_target_color"
S(150c47); // "color_target"
S(150c54); // "line_width"
S(150c5f); // "hurt_effect"
S(150c6b); // "filled"
S(150c72); // "show_2d_box"
S(150c7e); // "show_health_bar"
S(150c8e); // "%s"

D(2ea090);                            // setting num "line_width"
D(2ea0d0);                            // enum option labels (const char *[])
D(2ea0e0);                            // setting enum "mode"
extern "C" unsigned data_2ea110;      // current value of setting "mode" (+0x30)
D(2ea120);                            // setting bool "show_2d_box"
extern "C" unsigned char data_2ea138; // current value of setting "show_2d_box" (+0x18)
D(2ea140);                            // setting bool "hurt_effect"
D(2ea160);                            // setting bool "filled"
D(2ea180);                            // setting bool "show_health_bar"
D(2ea1a0);                            // setting num "range"
D(2ea1d8);                            // setting key "color_target"
D(2ea1f8);                            // setting key "color_friendlies"
D(2ea218);                            // setting key "color_enemies"
D(2ea238);                            // setting bool "hide_friendlies"
extern "C" unsigned char data_2ea250; // current value of setting "hide_friendlies" (+0x18)
D(2ea258);                            // setting bool "override_target_color"
extern "C" unsigned char data_2ea270; // current value of setting "override_target_color" (+0x18)
D(2ea290);                            // setting enum "color_mode"
extern "C" unsigned data_2ea2c0;      // current value of setting "color_mode" (+0x30)

extern "C" void player_esp_gui(void *a, void *b) {
    W_label(a, b, rdata_150bd9);
    W_slider(b, a, data_2ea1a0);
    W_bool(b, a, data_2ea238);
    W_label(a, b, rdata_150bde);
    W_enumw(b, a, data_2ea0e0);
    W_enumw(b, a, data_2ea290);
    if (data_2ea2c0 == 1) {
        W_color(b, a, data_2ea218);
        if (data_2ea250 == 0)
            W_color(b, a, data_2ea1f8);
    }
    W_bool(b, a, data_2ea258);
    if (data_2ea270 == 1)
        W_color(b, a, data_2ea1d8);
    if (data_2ea110 == 2) {
        W_bool(b, a, data_2ea140);
        return;
    }
    W_slider(b, a, data_2ea090);
    W_bool(b, a, data_2ea120);
    if (data_2ea138 == 1) {
        W_bool(b, a, data_2ea160);
        W_bool(b, a, data_2ea140);
    }
    W_bool(b, a, data_2ea180);
}

extern "C" void player_esp_register(void *m) {
    R_enum(m, rdata_150bec, data_2ea0e0);
    R_num(m, rdata_150bf1, data_2ea1a0);
    R_bool(m, rdata_150bf7, data_2ea238);
    R_enum(m, rdata_150c07, data_2ea290);
    R_key(m, rdata_150c12, data_2ea218);
    R_key(m, rdata_150c20, data_2ea1f8);
    R_bool(m, rdata_150c31, data_2ea258);
    R_key(m, rdata_150c47, data_2ea1d8);
    R_num(m, rdata_150c54, data_2ea090);
    R_bool(m, rdata_150c5f, data_2ea140);
    R_bool(m, rdata_150c6b, data_2ea160);
    R_bool(m, rdata_150c72, data_2ea120);
    R_bool(m, rdata_150c7e, data_2ea180);
}

extern "C" void player_esp_status(void *self) {
    log_fmt(self, 0x20, rdata_150c8e, heavy_glow_mode_name(data_2ea110));

}
