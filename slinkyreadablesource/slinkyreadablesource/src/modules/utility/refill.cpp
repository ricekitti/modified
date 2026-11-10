// Refill — Automatically moves items into your hotbar.
// category: utility   descriptor: 0x2eed90   settings block: 0x2eef88
// functions: gui refill_gui, register refill_register
// implementation: src/logic/utility/refill.cpp
#include "slinky/api.h"

S(152bae); // "Items"
S(152bb4); // "Clicks"
S(152bbb); // "refill_potion"
S(152bc9); // "refill_potion_non_health"
S(152be2); // "refill_soup"
S(152bee); // "simulate_button"
S(152bfe); // "delay_after_open"
S(152c0f); // "delay_before_close"
S(152c22); // "speed"
S(152c28); // "smart_speed"

D(2eefa0); // setting num "delay_before_close"
D(2eefd8); // setting num "delay_after_open"
D(2ef010); // setting num "speed"
D(2ef048); // setting bool "refill_soup"
D(2ef068); // setting bool "refill_potion"
D(2ef080); // current value of setting "refill_potion" (+0x18)
D(2ef088); // setting bool "refill_potion_non_health"
D(2ef0a8); // setting bool "smart_speed"
D(2ef0e8); // setting enum "simulate_button"

extern "C" void refill_gui(void *a, void *b) {
    W_label(a, b, rdata_152bae);
    W_bool(b, a, data_2ef068);
    if (*(unsigned char *)data_2ef080 == 1) {
        W_bool(b, a, data_2ef088);
    }
    W_bool(b, a, data_2ef048);
    W_label(a, b, rdata_152bb4);
    W_enumw(b, a, data_2ef0e8);
    W_slider(b, a, data_2eefd8);
    W_slider(b, a, data_2eefa0);
    W_slider(b, a, data_2ef010);
    W_bool(b, a, data_2ef0a8);
}

extern "C" void refill_register(void *m) {
    R_bool(m, rdata_152bbb, data_2ef068);
    R_bool(m, rdata_152bc9, data_2ef088);
    R_bool(m, rdata_152be2, data_2ef048);
    R_enum(m, rdata_152bee, data_2ef0e8);
    R_num(m, rdata_152bfe, data_2eefd8);
    R_num(m, rdata_152c0f, data_2eefa0);
    R_num(m, rdata_152c22, data_2ef010);
    R_bool(m, rdata_152c28, data_2ef0a8);
}
