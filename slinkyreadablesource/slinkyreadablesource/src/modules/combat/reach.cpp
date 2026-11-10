// Reach — Increases your melee range.
// category: combat   descriptor: 0x2ea550   settings block: 0x2ea748
// functions: gui reach_gui, register reach_register, status reach_status
// implementation: src/logic/combat/reach.cpp
#include "slinky/api.h"

S(150ddc); // "Main"
S(150de1); // "Advanced"
S(150dea); // "Conditions"
S(150df5); // "distance"
S(150dfe); // "activate_time"
S(150e0c); // "require_sprint"
S(150e1b); // "%.2f"

D(2ea760);                    // setting num "distance"
extern "C" float data_2ea790; // current value of setting "distance" (+0x30)
D(2ea798);                    // setting num "activate_time"
D(2ea7d0);                    // setting bool "require_sprint"

extern "C" void reach_gui(void *a, void *b) {
    W_label(a, b, rdata_150ddc);
    W_slider(b, a, data_2ea760);
    W_label(a, b, rdata_150de1);
    W_slider(b, a, data_2ea798);
    W_label(a, b, rdata_150dea);
    W_bool(b, a, data_2ea7d0);
}

extern "C" void reach_register(void *m) {
    R_num(m, rdata_150df5, data_2ea760);
    R_num(m, rdata_150dfe, data_2ea798);
    R_bool(m, rdata_150e0c, data_2ea7d0);
}

extern "C" void reach_status(void *self) {
    log_fmt(self, 0x20, rdata_150e1b, (double)data_2ea790);

}
