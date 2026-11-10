// Mod Overlay — Displays a list of all active mods.
// category: visual   descriptor: 0x2ecef0   settings block: 0x2ed0e8
// functions: gui mod_overlay_gui, register mod_overlay_register
// implementation: src/logic/visual/mod_overlay.cpp
#include "slinky/api.h"

S(151d5e); // "Visible categories"
S(151d71); // "Customization"
S(151d7f); // "Conditions"
S(151d8a); // "show_combat"
S(151d96); // "show_move"
S(151da0); // "show_block"
S(151dab); // "show_visual"
S(151db7); // "show_utility"
S(151dc4); // "text_size"
S(151dce); // "padding_x"
S(151dd8); // "padding_y"
S(151de2); // "bg_rounding"
S(151dee); // "name_color"
S(151df9); // "details_color"
S(151e07); // "lowercase"
S(151e11); // "show_details"
S(151e1e); // "require_bound"

D(2ed100); // setting bool "show_combat"
D(2ed120); // setting bool "show_move"
D(2ed140); // setting bool "show_block"
D(2ed160); // setting bool "show_visual"
D(2ed180); // setting bool "show_utility"
D(2ed1a0); // setting bool "require_bound"
D(2ed1c0); // setting bool "lowercase"
D(2ed1e0); // setting bool "show_details"
D(2ed200); // setting num "text_size"
D(2ed238); // setting key "name_color"
D(2ed258); // setting key "details_color"
D(2ed278); // setting num "bg_rounding"
D(2ed2b0); // setting num "padding_x"
D(2ed2e8); // setting num "padding_y"

extern "C" void mod_overlay_gui(void *a, void *b) {
    W_label(a, b, rdata_151d5e);
    W_bool(b, a, data_2ed100);
    W_bool(b, a, data_2ed120);
    W_bool(b, a, data_2ed140);
    W_bool(b, a, data_2ed160);
    W_bool(b, a, data_2ed180);
    W_label(a, b, rdata_151d71);
    W_slider(b, a, data_2ed200);
    W_slider(b, a, data_2ed2b0);
    W_slider(b, a, data_2ed2e8);
    W_slider(b, a, data_2ed278);
    W_color(b, a, data_2ed238);
    W_color(b, a, data_2ed258);
    W_bool(b, a, data_2ed1c0);
    W_bool(b, a, data_2ed1e0);
    W_label(a, b, rdata_151d7f);
    W_bool(b, a, data_2ed1a0);
}

extern "C" void mod_overlay_register(void *m) {
    R_bool(m, rdata_151d8a, data_2ed100);
    R_bool(m, rdata_151d96, data_2ed120);
    R_bool(m, rdata_151da0, data_2ed140);
    R_bool(m, rdata_151dab, data_2ed160);
    R_bool(m, rdata_151db7, data_2ed180);
    R_num(m, rdata_151dc4, data_2ed200);
    R_num(m, rdata_151dce, data_2ed2b0);
    R_num(m, rdata_151dd8, data_2ed2e8);
    R_num(m, rdata_151de2, data_2ed278);
    R_key(m, rdata_151dee, data_2ed238);
    R_key(m, rdata_151df9, data_2ed258);
    R_bool(m, rdata_151e07, data_2ed1c0);
    R_bool(m, rdata_151e11, data_2ed1e0);
    R_bool(m, rdata_151e1e, data_2ed1a0);
}
