// Fast Mine — Increases block mining speed.
// category: block   descriptor: 0x2f1d90   settings block: 0x2f1f88
// functions: gui fast_mine_gui, register fast_mine_register, status fast_mine_status
// implementation: src/logic/block/fast_mine.cpp
#include "slinky/api.h"

S(1544a6); // "%.2fx"
S(1544ac); // "Main"
S(1544b1); // "speed"
S(1544b7); // "break_delay"
S(1544c3); // "decr_block_hit_delay"

D(2f1fa0);                    // setting num "break_delay"
D(2f1fd8);                    // setting bool "decr_block_hit_delay"
D(2f1ff8);                    // setting num "speed"
extern "C" float data_2f2028; // current value of setting "speed" (+0x30)

extern "C" void fast_mine_gui(void *a, void *b) {
    W_label(a, b, rdata_1544ac);
    W_slider(b, a, data_2f1ff8);
    W_slider(b, a, data_2f1fa0);
    W_bool(b, a, data_2f1fd8);
}

extern "C" void fast_mine_register(void *m) {
    R_num(m, rdata_1544b1, data_2f1ff8);
    R_num(m, rdata_1544b7, data_2f1fa0);
    R_bool(m, rdata_1544c3, data_2f1fd8);
}

extern "C" void fast_mine_status(void *self) {
    log_fmt(self, 0x20, rdata_1544a6, (double)data_2f2028);

}
