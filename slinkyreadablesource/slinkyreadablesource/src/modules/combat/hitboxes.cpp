// Hitboxes — Expands hitboxes of other players.
// category: combat   descriptor: 0x2ecca8   settings block: 0x2ecea0
// functions: gui hitboxes_gui, register hitboxes_register, status hitboxes_status
// implementation: src/logic/combat/hitboxes.cpp
#include "slinky/api.h"

S(151bf7); // "Main"
S(151bfc); // "expand"
S(151c03); // "%.2f"

D(2eceb8);                    // setting num "expand"
extern "C" float data_2ecee8; // current value of setting "expand" (+0x30)

extern "C" void hitboxes_gui(void *a, void *b) {
    W_label(a, b, rdata_151bf7);
    W_slider(b, a, data_2eceb8);
}

extern "C" void hitboxes_register(void *m) {
    R_num(m, rdata_151bfc, data_2eceb8);
}

extern "C" void hitboxes_status(void *self) {
    log_fmt(self, 0x20, rdata_151c03, (double)data_2ecee8);

}
