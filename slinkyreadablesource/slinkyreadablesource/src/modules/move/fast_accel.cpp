// Fast Accel — Speeds up acceleration.
// category: move   descriptor: 0x2f27f8   settings block: 0x2f29f0
// functions: gui fast_accel_gui, register fast_accel_register
// implementation: src/logic/move/fast_accel.cpp
#include "slinky/api.h"

S(154763); // "Main"
S(154768); // "Conditions"
S(154773); // "acceleration"
S(154780); // "require_not_damaged"
S(154794); // "require_weapon"

D(2f2a08); // setting num "acceleration"
D(2f2a40); // setting bool "require_weapon"
D(2f2a60); // setting bool "require_not_damaged"

extern "C" void fast_accel_gui(void *a, void *b) {
    W_label(a, b, rdata_154763);
    W_slider(b, a, data_2f2a08);
    W_label(a, b, rdata_154768);
    W_bool(b, a, data_2f2a60);
    W_bool(b, a, data_2f2a40);
}

extern "C" void fast_accel_register(void *m) {
    R_num(m, rdata_154773, data_2f2a08);
    R_bool(m, rdata_154780, data_2f2a60);
    R_bool(m, rdata_154794, data_2f2a40);
}
