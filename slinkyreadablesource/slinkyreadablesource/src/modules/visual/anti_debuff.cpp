// Anti Debuff — Removes negative visual effects.
// category: visual   descriptor: 0x2f3860   settings block: 0x2f3a58
// functions: gui anti_debuff_gui, register anti_debuff_register
// implementation: src/logic/visual/anti_debuff.cpp
#include "slinky/api.h"

S(154cb4); // "Effects"
S(154cbc); // "blindness"
S(154cc6); // "nausea"

D(2f3a70); // setting bool "blindness"
D(2f3a90); // setting bool "nausea"

extern "C" void anti_debuff_gui(void *a, void *b) {
    W_label(a, b, rdata_154cb4);
    W_bool(b, a, data_2f3a70);
    W_bool(b, a, data_2f3a90);
}

extern "C" void anti_debuff_register(void *m) {
    R_bool(m, rdata_154cbc, data_2f3a70);
    R_bool(m, rdata_154cc6, data_2f3a90);
}
