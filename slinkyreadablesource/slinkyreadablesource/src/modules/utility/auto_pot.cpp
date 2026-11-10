// Auto Pot — Uses healing potions for you.
// category: utility   descriptor: 0x2ef128   settings block: 0x2ef320
// functions: gui auto_pot_gui, register auto_pot_register
// implementation: src/logic/utility/auto_pot.cpp
#include "slinky/api.h"

S(152d22); // "Main"
S(152d27); // "delay"
S(152d2d); // "prevent_waste"
S(152d3b); // "double_splash"
S(152d49); // "use_non_health"

D(2ef338); // setting bool "use_non_health"
D(2ef358); // setting num "delay"
D(2ef390); // setting bool "prevent_waste"
D(2ef3b0); // setting bool "double_splash"

extern "C" void auto_pot_gui(void *a, void *b) {
    W_label(a, b, rdata_152d22);
    W_slider(b, a, data_2ef358);
    W_bool(b, a, data_2ef390);
    W_bool(b, a, data_2ef3b0);
    W_bool(b, a, data_2ef338);
}

extern "C" void auto_pot_register(void *m) {
    R_num(m, rdata_152d27, data_2ef358);
    R_bool(m, rdata_152d2d, data_2ef390);
    R_bool(m, rdata_152d3b, data_2ef3b0);
    R_bool(m, rdata_152d49, data_2ef338);
}
