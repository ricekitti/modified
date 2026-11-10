// No Use Delay — Removes vanilla delay when consuming items.
// category: utility   descriptor: 0x2f1210   settings block: 0x2f1408
// functions: gui no_use_delay_gui, register no_use_delay_register
// implementation: src/logic/utility/no_use_delay.cpp
#include "slinky/api.h"

S(153fcc); // "Main"
S(153fd1); // "allow_desync"

D(2f1420); // setting bool "allow_desync"

extern "C" void no_use_delay_gui(void *a, void *b) {
    W_label(a, b, rdata_153fcc);
    W_bool(b, a, data_2f1420);
}

extern "C" void no_use_delay_register(void *m) {
    R_bool(m, rdata_153fd1, data_2f1420);
}
