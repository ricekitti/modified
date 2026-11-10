// Blink — Simulates teleporting using lag.
// category: utility   descriptor: 0x2ec600   settings block: 0x2ec7f8
// functions: gui blink_gui, register blink_register, status blink_status
// implementation: src/logic/utility/blink.cpp
#include "slinky/api.h"

S(1519f0); // "Main"
S(1519f5); // "Disable on"
S(151a00); // "direction"
S(151a0a); // "max_duration"
S(151a17); // "disable_on_attack"
S(151a29); // "disable_on_block_interact"
S(151a43); // "disable_on_block_dig"
S(151a58); // "%s"

D(2ec810);                       // setting bool "disable_on_attack"
D(2ec830);                       // setting num "max_duration"
D(2ec870);                       // enum option labels (const char *[])
D(2ec888);                       // setting enum "direction"
extern "C" unsigned data_2ec8b8; // current value of setting "direction" (+0x30)
D(2ec8c8);                       // setting bool "disable_on_block_dig"
D(2ec8e8);                       // setting bool "disable_on_block_interact"

extern "C" void blink_gui(void *a, void *b) {
    W_label(a, b, rdata_1519f0);
    W_enumw(b, a, data_2ec888);
    W_slider(b, a, data_2ec830);
    W_label(a, b, rdata_1519f5);
    W_bool(b, a, data_2ec810);
    W_bool(b, a, data_2ec8e8);
    W_bool(b, a, data_2ec8c8);
}

extern "C" void blink_register(void *m) {
    R_enum(m, rdata_151a00, data_2ec888);
    R_num(m, rdata_151a0a, data_2ec830);
    R_bool(m, rdata_151a17, data_2ec810);
    R_bool(m, rdata_151a29, data_2ec8e8);
    R_bool(m, rdata_151a43, data_2ec8c8);
}

extern "C" void blink_status(void *self) {
    log_fmt(self, 0x20, rdata_151a58, ((const char **)data_2ec870)[data_2ec8b8]);

}
