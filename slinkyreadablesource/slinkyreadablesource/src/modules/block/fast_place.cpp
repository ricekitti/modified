// Fast Place — Allows placing blocks faster.
// category: block   descriptor: 0x2ea2d0   settings block: 0x2ea4c8
// functions: gui fast_place_gui, register fast_place_register, status fast_place_status
// implementation: src/logic/block/fast_place.cpp
#include "slinky/api.h"

S(150d5f); // "Main"
S(150d64); // "max_cps"
S(150d6c); // "activate_delay"
S(150d7b); // "%.1f"

D(2ea4e0);                    // setting num "max_cps"
extern "C" float data_2ea510; // current value of setting "max_cps" (+0x30)
D(2ea518);                    // setting num "activate_delay"

extern "C" void fast_place_gui(void *a, void *b) {
    W_label(a, b, rdata_150d5f);
    W_slider(b, a, data_2ea4e0);
    W_slider(b, a, data_2ea518);
}

extern "C" void fast_place_register(void *m) {
    R_num(m, rdata_150d64, data_2ea4e0);
    R_num(m, rdata_150d6c, data_2ea518);
}

extern "C" void fast_place_status(void *self) {
    log_fmt(self, 0x20, rdata_150d7b, (double)data_2ea510);

}
