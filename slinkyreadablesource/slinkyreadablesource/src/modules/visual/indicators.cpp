// Indicators — Points to incoming projectiles.
// category: visual   descriptor: 0x2f1440   settings block: 0x2f1638
// functions: gui indicators_gui, register indicators_register
// implementation: src/logic/visual/indicators.cpp
#include "slinky/api.h"

S(1540ae); // "Main"
S(1540b3); // "Warn for"
S(1540bc); // "Conditions"
S(1540c7); // "scale"
S(1540cd); // "radius"
S(1540d4); // "show_arrow"
S(1540df); // "show_fireball"
S(1540ed); // "show_pearl"
S(1540f8); // "require_coming_closer"

D(2f1650); // setting num "scale"
D(2f1688); // setting num "radius"
D(2f16c0); // setting bool "show_arrow"
D(2f16e0); // setting bool "show_fireball"
D(2f1700); // setting bool "show_pearl"
D(2f1720); // setting bool "require_coming_closer"

extern "C" void indicators_gui(void *a, void *b) {
    W_label(a, b, rdata_1540ae);
    W_slider(b, a, data_2f1650);
    W_slider(b, a, data_2f1688);
    W_label(a, b, rdata_1540b3);
    W_bool(b, a, data_2f16c0);
    W_bool(b, a, data_2f16e0);
    W_bool(b, a, data_2f1700);
    W_label(a, b, rdata_1540bc);
    W_bool(b, a, data_2f1720);
}

extern "C" void indicators_register(void *m) {
    R_num(m, rdata_1540c7, data_2f1650);
    R_num(m, rdata_1540cd, data_2f1688);
    R_bool(m, rdata_1540d4, data_2f16c0);
    R_bool(m, rdata_1540df, data_2f16e0);
    R_bool(m, rdata_1540ed, data_2f1700);
    R_bool(m, rdata_1540f8, data_2f1720);
}
