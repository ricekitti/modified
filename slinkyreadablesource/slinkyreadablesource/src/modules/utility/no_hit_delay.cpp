// No Hit Delay — Removes 1.8 swing delay.
// category: utility   descriptor: 0x2f0b70   settings block: 0x2f0d68
// functions: gui no_hit_delay_gui, register no_hit_delay_register, status no_hit_delay_status
// implementation: src/logic/utility/no_hit_delay.cpp
#include "slinky/api.h"

S(153ee0); // "In-game"
S(153ee8); // "mode"
S(153eed); // "%s"

D(2f0d80);                       // enum option labels (const char *[])
D(2f0d90);                       // setting enum "mode"
extern "C" unsigned data_2f0dc0; // current value of setting "mode" (+0x30)

extern "C" void no_hit_delay_gui(void *a, void *b) {
    W_label(a, b, rdata_153ee0);
    W_enumw(b, a, data_2f0d90);
}

extern "C" void no_hit_delay_register(void *m) {
    R_enum(m, rdata_153ee8, data_2f0d90);
}

extern "C" void no_hit_delay_status(void *self) {
    log_fmt(self, 0x20, rdata_153eed, ((const char **)data_2f0d80)[data_2f0dc0]);

}
