// Timer Range — Abuses timer lag to teleport you.
// category: combat   descriptor: 0x2f3160   settings block: 0x2f3358
// functions: gui timer_range_gui, register timer_range_register, status timer_range_status
// implementation: src/logic/combat/timer_range.cpp
#include "slinky/api.h"

S(154a05); // "Main"
S(154a0a); // "Conditions"
S(154a15); // "mode"
S(154a1a); // "min_dist"
S(154a23); // "duration"
S(154a2c); // "duration_legit"
S(154a3b); // "cooldown"
S(154a44); // "block_while_frozen"
S(154a57); // "require_mouse_pressed"
S(154a6d); // "require_weapon"
S(154a7c); // "require_not_using_item"
S(154a93); // "%s"

D(2f3370);                       // setting bool "require_mouse_pressed"
D(2f3390);                       // setting bool "require_weapon"
D(2f33b0);                       // setting bool "require_not_using_item"
D(2f33d0);                       // enum option labels (const char *[])
D(2f33e0);                       // setting enum "mode"
extern "C" unsigned data_2f3410; // current value of setting "mode" (+0x30)
D(2f3420);                       // setting bool "block_while_frozen"
D(2f3440);                       // setting num "cooldown"
D(2f3478);                       // setting num "duration"
D(2f34b0);                       // setting num "duration_legit"
D(2f34e8);                       // setting num "min_dist"

extern "C" void timer_range_gui(void *a, void *b) {
    W_label(a, b, rdata_154a05);
    W_enumw(b, a, data_2f33e0);
    W_slider(b, a, data_2f34e8);
    W_slider(b, a, data_2f3410 == 0 ? data_2f3478 : data_2f34b0);
    W_slider(b, a, data_2f3440);
    if (data_2f3410 == 0)
        W_bool(b, a, data_2f3420);
    W_label(a, b, rdata_154a0a);
    W_bool(b, a, data_2f3370);
    W_bool(b, a, data_2f3390);
    W_bool(b, a, data_2f33b0);
}

extern "C" void timer_range_register(void *m) {
    R_enum(m, rdata_154a15, data_2f33e0);
    R_num(m, rdata_154a1a, data_2f34e8);
    R_num(m, rdata_154a23, data_2f3478);
    R_num(m, rdata_154a2c, data_2f34b0);
    R_num(m, rdata_154a3b, data_2f3440);
    R_bool(m, rdata_154a44, data_2f3420);
    R_bool(m, rdata_154a57, data_2f3370);
    R_bool(m, rdata_154a6d, data_2f3390);
    R_bool(m, rdata_154a7c, data_2f33b0);
}

extern "C" void timer_range_status(void *self) {
    log_fmt(self, 0x20, rdata_154a93, ((const char **)data_2f33d0)[data_2f3410]);

}
