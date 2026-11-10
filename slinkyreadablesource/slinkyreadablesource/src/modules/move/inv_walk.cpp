// Inv Walk — Allows moving in inventory.
// category: move   descriptor: 0x2ef3d0   settings block: 0x2ef5c8
// functions: gui inv_walk_gui, register inv_walk_register, status inv_walk_status
// implementation: src/logic/move/inv_walk.cpp
#include "slinky/api.h"

S(152dd3); // "Main"
S(152dd8); // "Other containers"
S(152de9); // "mode"
S(152dee); // "stop_on_click"
S(152dfc); // "stop_duration"
S(152e0a); // "stop_non_inv"
S(152e17); // "%s"

D(2ef5e0);                       // enum option labels (const char *[])
D(2ef5f0);                       // setting enum "mode"
extern "C" unsigned data_2ef620; // current value of setting "mode" (+0x30)
D(2ef648);                       // setting enum "stop_on_click"
extern "C" unsigned data_2ef678; // current value of setting "stop_on_click" (+0x30)
D(2ef688);                       // setting num "stop_duration"
D(2ef6c0);                       // setting enum "stop_non_inv"

extern "C" void inv_walk_gui(void *a, void *b) {
    W_label(a, b, rdata_152dd3);
    W_enumw(b, a, data_2ef5f0);
    if (data_2ef620 != 0) {
        W_enumw(b, a, data_2ef648);
        if (data_2ef678 != 2)
            W_slider(b, a, data_2ef688);
        W_label(a, b, rdata_152dd8);
        W_enumw(b, a, data_2ef6c0);
    } else {

    }
}

extern "C" void inv_walk_register(void *m) {
    R_enum(m, rdata_152de9, data_2ef5f0);
    R_enum(m, rdata_152dee, data_2ef648);
    R_num(m, rdata_152dfc, data_2ef688);
    R_enum(m, rdata_152e0a, data_2ef6c0);
}

extern "C" void inv_walk_status(void *self) {
    log_fmt(self, 0x20, rdata_152e17, ((const char **)data_2ef5e0)[data_2ef620]);

}
