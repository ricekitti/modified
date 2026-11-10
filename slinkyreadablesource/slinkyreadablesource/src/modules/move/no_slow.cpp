// No Slow — Removes slowdown while using items.
// category: move   descriptor: 0x2f2c90   settings block: 0x2f2e88
// functions: gui no_slow_gui, register no_slow_register
// implementation: src/logic/move/no_slow.cpp
#include "slinky/api.h"

S(154851); // "Retained speed"
S(154860); // "mult_sword"
S(15486b); // "mult_bow"
S(154874); // "mult_consumables"

D(2f2ea0); // setting num "mult_bow"
D(2f2ed8); // setting num "mult_sword"
D(2f2f10); // setting num "mult_consumables"

extern "C" void no_slow_gui(void *a, void *b) {
    W_label(a, b, rdata_154851);
    W_slider(b, a, data_2f2ed8);
    W_slider(b, a, data_2f2ea0);
    W_slider(b, a, data_2f2f10);
}

extern "C" void no_slow_register(void *m) {
    R_num(m, rdata_154860, data_2f2ed8);
    R_num(m, rdata_15486b, data_2f2ea0);
    R_num(m, rdata_154874, data_2f2f10);
}
