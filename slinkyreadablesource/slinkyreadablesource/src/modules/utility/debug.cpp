// Debug — Prints debug information in chat.
// category: utility   descriptor: 0x2f4430   settings block: 0x2f4628
// functions: gui debug_gui, register debug_register
// implementation: src/logic/utility/debug.cpp
#include "slinky/api.h"

S(1551db); // "Main"
S(1551e0); // "clicks"
S(1551e7); // "hurt_time"
S(1551f1); // "item_use_state"

D(2f4640); // setting bool "hurt_time"
D(2f4660); // setting bool "item_use_state"
D(2f4680); // setting bool "clicks"

extern "C" void debug_gui(void *a, void *b) {
    W_label(a, b, rdata_1551db);
    W_bool(b, a, data_2f4680);
    W_bool(b, a, data_2f4640);
    W_bool(b, a, data_2f4660);
}

extern "C" void debug_register(void *m) {
    R_bool(m, rdata_1551e0, data_2f4680);
    R_bool(m, rdata_1551e7, data_2f4640);
    R_bool(m, rdata_1551f1, data_2f4660);
}
