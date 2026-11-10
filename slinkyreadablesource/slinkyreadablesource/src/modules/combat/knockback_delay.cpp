// Knockback Delay — Delays your knockback in certain scenarios.
// category: combat   descriptor: 0x2f40f8   settings block: 0x2f42f0
// functions: gui knockback_delay_gui, register knockback_delay_register, status knockback_delay_status
// implementation: src/logic/combat/knockback_delay.cpp
#include "slinky/api.h"

S(15509d); // "Main"
S(1550a2); // "Conditions"
S(1550ad); // "range"
S(1550b3); // "chance"
S(1550ba); // "max_delay"
S(1550c4); // "require_air"
S(1550d0); // "require_target"
S(1550df); // "require_mouse"
S(1550ed); // "require_weapon"
S(1550fc); // "%.0fms"

D(2f4308);                    // setting num "max_delay"
extern "C" float data_2f4338; // current value of setting "max_delay" (+0x30)
D(2f4340);                    // setting bool "require_air"
D(2f4360);                    // setting num "range"
D(2f4398);                    // setting bool "require_weapon"
D(2f43b8);                    // setting bool "require_mouse"
D(2f43d8);                    // setting bool "require_target"
D(2f43f8);                    // setting num "chance"

extern "C" void knockback_delay_gui(void *a, void *b) {
    W_label(a, b, rdata_15509d);
    W_slider(b, a, data_2f4360);
    W_slider(b, a, data_2f43f8);
    W_slider(b, a, data_2f4308);
    W_label(a, b, rdata_1550a2);
    W_bool(b, a, data_2f4340);
    W_bool(b, a, data_2f43d8);
    W_bool(b, a, data_2f43b8);
    W_bool(b, a, data_2f4398);
}

extern "C" void knockback_delay_register(void *m) {
    R_num(m, rdata_1550ad, data_2f4360);
    R_num(m, rdata_1550b3, data_2f43f8);
    R_num(m, rdata_1550ba, data_2f4308);
    R_bool(m, rdata_1550c4, data_2f4340);
    R_bool(m, rdata_1550d0, data_2f43d8);
    R_bool(m, rdata_1550df, data_2f43b8);
    R_bool(m, rdata_1550ed, data_2f4398);
}

extern "C" void knockback_delay_status(void *self) {
    log_fmt(self, 0x20, rdata_1550fc, (double)data_2f4338);

}
