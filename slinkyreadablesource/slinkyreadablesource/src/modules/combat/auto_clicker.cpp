// Auto Clicker — Clicks for you when holding down left-click.
// category: combat   descriptor: 0x2e9330   settings block: 0x2e9528
// functions: gui auto_clicker_gui, register auto_clicker_register, status auto_clicker_status
// implementation: src/logic/combat/auto_clicker.cpp
#include "slinky/api.h"

S(1504f8); // "In-game"
S(150500); // "Inventory"
S(15050a); // "Conditions"
S(150515); // "pattern"
S(15051d); // "target_cps"
S(150528); // "randomize_cps"
S(150536); // "exhaust"
S(15053e); // "allow_block_break"
S(150550); // "allow_inventory"
S(150560); // "inventory_randomize"
S(150574); // "require_weapon"
S(150583); // "require_not_using_item"
S(15059a); // "%s"

D(2e9548);                       // setting bool "allow_inventory"
D(2e9560);                       // current value of setting "allow_inventory" (+0x18)
D(2e9568);                       // setting bool "inventory_randomize"
D(2e9588);                       // setting bool "allow_block_break"
D(2e95a8);                       // setting bool "require_weapon"
D(2e95c8);                       // setting bool "require_not_using_item"
D(2e95e8);                       // setting num "target_cps"
D(2e9620);                       // setting bool "randomize_cps"
D(2e9640);                       // enum option labels (const char *[])
D(2e9650);                       // setting enum "pattern"
extern "C" unsigned data_2e9680; // current value of setting "pattern" (+0x30)
D(2e9690);                       // setting bool "exhaust"

extern "C" void auto_clicker_gui(void *a, void *b) {
    W_label(a, b, rdata_1504f8);
    W_enumw(b, a, data_2e9650);
    W_slider(b, a, data_2e95e8);
    W_bool(b, a, data_2e9620);
    W_bool(b, a, data_2e9690);
    W_bool(b, a, data_2e9588);
    W_label(a, b, rdata_150500);
    W_bool(b, a, data_2e9548);
    if (*(unsigned char *)data_2e9560 == 1) {
        W_bool(b, a, data_2e9568);
    }
    W_label(a, b, rdata_15050a);
    W_bool(b, a, data_2e95a8);
    W_bool(b, a, data_2e95c8);
}

extern "C" void auto_clicker_register(void *m) {
    R_enum(m, rdata_150515, data_2e9650);
    R_num(m, rdata_15051d, data_2e95e8);
    R_bool(m, rdata_150528, data_2e9620);
    R_bool(m, rdata_150536, data_2e9690);
    R_bool(m, rdata_15053e, data_2e9588);
    R_bool(m, rdata_150550, data_2e9548);
    R_bool(m, rdata_150560, data_2e9568);
    R_bool(m, rdata_150574, data_2e95a8);
    R_bool(m, rdata_150583, data_2e95c8);
}

extern "C" void auto_clicker_status(void *self) {
    log_fmt(self, 0x20, rdata_15059a, ((const char **)data_2e9640)[data_2e9680]);

}
