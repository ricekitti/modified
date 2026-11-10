// Strafe — Allows faster strafing.
// category: move   descriptor: 0x2f24e8   settings block: 0x2f26e0
// functions: gui strafe_gui, register strafe_register
// implementation: src/logic/move/strafe.cpp
#include "slinky/api.h"

S(15469f); // "Strength"
S(1546a8); // "Conditions"
S(1546b3); // "strength_jump"
S(1546c1); // "strength_ground"
S(1546d1); // "strength_air"
S(1546de); // "max_hurt_time"
S(1546ec); // "require_weapon"

D(2f26f8); // setting num "max_hurt_time"
D(2f2730); // setting num "strength_air"
D(2f2768); // setting num "strength_jump"
D(2f27a0); // setting num "strength_ground"
D(2f27d8); // setting bool "require_weapon"

extern "C" void strafe_gui(void *a, void *b) {
    W_label(a, b, rdata_15469f);
    W_slider(b, a, data_2f2768);
    W_slider(b, a, data_2f27a0);
    W_slider(b, a, data_2f2730);
    W_label(a, b, rdata_1546a8);
    W_slider(b, a, data_2f26f8);
    W_bool(b, a, data_2f27d8);
}

extern "C" void strafe_register(void *m) {
    R_num(m, rdata_1546b3, data_2f2768);
    R_num(m, rdata_1546c1, data_2f27a0);
    R_num(m, rdata_1546d1, data_2f2730);
    R_num(m, rdata_1546de, data_2f26f8);
    R_bool(m, rdata_1546ec, data_2f27d8);
}
