// Timer — Modifies the speed the game runs at.
// category: utility   descriptor: 0x2eba10   settings block: 0x2ebc08
// functions: gui timer_gui, register timer_register, status timer_status
// implementation: src/logic/utility/timer.cpp
#include "slinky/api.h"

S(1516c5); // "Main"
S(1516ca); // "Conditions"
S(1516d5); // "speed"
S(1516db); // "require_mouse"
S(1516e9); // "require_damage"
S(1516f8); // "require_falling"
S(151708); // "%.2fx"

D(2ebc20);                    // setting num "speed"
extern "C" float data_2ebc50; // current value of setting "speed" (+0x30)
D(2ebc58);                    // setting bool "require_mouse"
D(2ebc78);                    // setting bool "require_falling"
D(2ebc98);                    // setting bool "require_damage"

extern "C" void timer_gui(void *a, void *b) {
    W_label(a, b, rdata_1516c5);
    W_slider(b, a, data_2ebc20);
    W_label(a, b, rdata_1516ca);
    W_bool(b, a, data_2ebc58);
    W_bool(b, a, data_2ebc98);
    W_bool(b, a, data_2ebc78);
}

extern "C" void timer_register(void *m) {
    R_num(m, rdata_1516d5, data_2ebc20);
    R_bool(m, rdata_1516db, data_2ebc58);
    R_bool(m, rdata_1516e9, data_2ebc98);
    R_bool(m, rdata_1516f8, data_2ebc78);
}

extern "C" void timer_status(void *self) {
    log_fmt(self, 0x20, rdata_151708, (double)data_2ebc50);

}
