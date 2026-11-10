// Chams — Renders players behind blocks.
// category: visual   descriptor: 0x2f0fe0   settings block: 0x2f11d8
// functions: gui chams_gui, register chams_register
// implementation: src/logic/visual/chams.cpp
#include "slinky/api.h"

S(153f61); // "Main"
S(153f66); // "hide_friendlies"

D(2f11f0); // setting bool "hide_friendlies"

extern "C" void chams_gui(void *a, void *b) {
    W_label(a, b, rdata_153f61);
    W_bool(b, a, data_2f11f0);
}

extern "C" void chams_register(void *m) {
    R_bool(m, rdata_153f66, data_2f11f0);
}
