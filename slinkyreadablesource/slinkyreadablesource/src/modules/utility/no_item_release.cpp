// No Item Release — Hides item release from server.
// category: utility   descriptor: 0x2ec390   settings block: 0x2ec588
// functions: gui no_item_release_gui, register no_item_release_register, status no_item_release_status
// implementation: src/logic/utility/no_item_release.cpp
#include "slinky/api.h"

S(15196e); // "Main"
S(151973); // "mode"
S(151978); // "%s"

D(2ec5a0);                       // enum option labels (const char *[])
D(2ec5b8);                       // setting enum "mode"
extern "C" unsigned data_2ec5e8; // current value of setting "mode" (+0x30)

extern "C" void no_item_release_gui(void *a, void *b) {
    W_label(a, b, rdata_15196e);
    W_enumw(b, a, data_2ec5b8);
}

extern "C" void no_item_release_register(void *m) {
    R_enum(m, rdata_151973, data_2ec5b8);
}

extern "C" void no_item_release_status(void *self) {
    log_fmt(self, 0x20, rdata_151978, ((const char **)data_2ec5a0)[data_2ec5e8]);

}
