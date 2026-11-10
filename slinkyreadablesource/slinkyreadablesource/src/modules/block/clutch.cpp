// Clutch — Clutches for you when falling.
// category: block   descriptor: 0x2f5580   settings block: 0x2f5778
// functions: gui clutch_gui, register clutch_register
// implementation: src/logic/block/clutch.cpp
#include "slinky/api.h"

S(1564bc); // "Main"
S(1564c1); // "Aim"
S(1564c5); // "Snap-back"
S(1564cf); // "Conditions"
S(1564da); // "range"
S(1564e0); // "max_aim_dist"
S(1564ed); // "min_height"
S(1564f8); // "click_speed"
S(156504); // "randomization"
S(156512); // "select_blocks"
S(156520); // "sideways_only"
S(15652e); // "aim_speed"
S(156538); // "aim_mult"
S(156541); // "aim_mult_strength"
S(156553); // "aim_multipoint"
S(156562); // "snapback_delay"
S(156571); // "snapback_duration"
S(156583); // "snapback_instant_on_jump"
S(15659c); // "snapback_disable_after"
S(1565b3); // "require_air"
S(1565bf); // "require_damage"
S(1565ce); // "require_backwards"

D(2f5790); // setting num "click_speed"
D(2f57c8); // setting num "snapback_duration"
D(2f5800); // setting num "snapback_delay"
D(2f5838); // setting num "randomization"
D(2f5870); // setting num "aim_speed"
D(2f58a8); // setting num "aim_mult"
D(2f58e0); // setting num "aim_mult_strength"
D(2f5918); // setting bool "snapback_instant_on_jump"
D(2f5938); // setting bool "require_air"
D(2f5958); // setting bool "require_damage"
D(2f5978); // setting bool "require_backwards"
D(2f5998); // setting num "min_height"
D(2f59d0); // setting num "range"
D(2f5a08); // setting num "max_aim_dist"
D(2f5a40); // setting bool "sideways_only"
D(2f5a60); // setting bool "aim_multipoint"
D(2f5a98); // setting enum "select_blocks"
D(2f5ad8); // setting bool "snapback_disable_after"

extern "C" void clutch_gui(void *a, void *b) {
    W_label(a, b, rdata_1564bc);
    W_slider(b, a, data_2f59d0);
    W_slider(b, a, data_2f5a08);
    W_slider(b, a, data_2f5998);
    W_slider(b, a, data_2f5790);
    W_slider(b, a, data_2f5838);
    W_enumw(b, a, data_2f5a98);
    W_bool(b, a, data_2f5a40);
    W_label(a, b, rdata_1564c1);
    W_slider(b, a, data_2f5870);
    W_slider(b, a, data_2f58a8);
    W_slider(b, a, data_2f58e0);
    W_bool(b, a, data_2f5a60);
    W_label(a, b, rdata_1564c5);
    W_slider(b, a, data_2f5800);
    W_slider(b, a, data_2f57c8);
    W_bool(b, a, data_2f5918);
    W_bool(b, a, data_2f5ad8);
    W_label(a, b, rdata_1564cf);
    W_bool(b, a, data_2f5938);
    W_bool(b, a, data_2f5958);
    W_bool(b, a, data_2f5978);
}

extern "C" void clutch_register(void *m) {
    R_num(m, rdata_1564da, data_2f59d0);
    R_num(m, rdata_1564e0, data_2f5a08);
    R_num(m, rdata_1564ed, data_2f5998);
    R_num(m, rdata_1564f8, data_2f5790);
    R_num(m, rdata_156504, data_2f5838);
    R_enum(m, rdata_156512, data_2f5a98);
    R_bool(m, rdata_156520, data_2f5a40);
    R_num(m, rdata_15652e, data_2f5870);
    R_num(m, rdata_156538, data_2f58a8);
    R_num(m, rdata_156541, data_2f58e0);
    R_bool(m, rdata_156553, data_2f5a60);
    R_num(m, rdata_156562, data_2f5800);
    R_num(m, rdata_156571, data_2f57c8);
    R_bool(m, rdata_156583, data_2f5918);
    R_bool(m, rdata_15659c, data_2f5ad8);
    R_bool(m, rdata_1565b3, data_2f5938);
    R_bool(m, rdata_1565bf, data_2f5958);
    R_bool(m, rdata_1565ce, data_2f5978);
}
