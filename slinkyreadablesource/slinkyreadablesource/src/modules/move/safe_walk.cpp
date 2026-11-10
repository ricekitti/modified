// Safe Walk — Prevents you from walking off ledges.
// category: move   descriptor: 0x2f2030   settings block: 0x2f2228
// functions: gui safe_walk_gui, register safe_walk_register
// implementation: src/logic/move/safe_walk.cpp
#include "slinky/api.h"

S(15454c); // "Main"
S(154551); // "Conditions"
S(15455c); // "motion_multiplier"
S(15456e); // "require_holding_blocks"
S(154585); // "require_looking_down"
S(15459a); // "require_not_forward"

D(2f2240); // setting num "motion_multiplier"
D(2f2278); // setting bool "require_looking_down"
D(2f2298); // setting bool "require_not_forward"
D(2f22b8); // setting bool "require_holding_blocks"

extern "C" void safe_walk_gui(void *a, void *b) {
    W_label(a, b, rdata_15454c);
    W_slider(b, a, data_2f2240);
    W_label(a, b, rdata_154551);
    W_bool(b, a, data_2f22b8);
    W_bool(b, a, data_2f2278);
    W_bool(b, a, data_2f2298);
}

extern "C" void safe_walk_register(void *m) {
    R_num(m, rdata_15455c, data_2f2240);
    R_bool(m, rdata_15456e, data_2f22b8);
    R_bool(m, rdata_154585, data_2f2278);
    R_bool(m, rdata_15459a, data_2f2298);
}
