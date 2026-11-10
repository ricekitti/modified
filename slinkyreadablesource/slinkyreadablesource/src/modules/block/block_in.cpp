// Block In — Places blocks around you.
// category: block   descriptor: 0x2f6108   settings block: 0x2f6300
// functions: gui block_in_gui, register block_in_register
// implementation: src/logic/block/block_in.cpp
#include "slinky/api.h"

S(156bcd); // "Main"
S(156bd2); // "Conditions"
S(156bdd); // "speed"
S(156be3); // "require_ground"

D(2f6318); // setting bool "require_ground"
D(2f6338); // setting num "speed"

extern "C" void block_in_gui(void *a, void *b) {
    W_label(a, b, rdata_156bcd);
    W_slider(b, a, data_2f6338);
    W_label(a, b, rdata_156bd2);
    W_bool(b, a, data_2f6318);
}

extern "C" void block_in_register(void *m) {
    R_num(m, rdata_156bdd, data_2f6338);
    R_bool(m, rdata_156be3, data_2f6318);
}
