// Fake Lag — Adds an artificial delay to your packets.
// category: utility   descriptor: 0x2ebcc0   settings block: 0x2ebeb8
// functions: gui fake_lag_gui, register fake_lag_register, status fake_lag_status
// implementation: src/logic/utility/fake_lag.cpp
#include "slinky/api.h"

S(15179c); // "Delay"
S(1517a2); // "Conditions"
S(1517ad); // "mode"
S(1517b2); // "inbound"
S(1517ba); // "outbound"
S(1517c3); // "realtime_damage"
S(1517d3); // "require_weapon"
S(1517e2); // "require_attack"
S(1517f1); // "require_ingame"
S(151800); // "%s"

D(2ebed0);                       // setting num "inbound"
D(2ebf08);                       // setting num "outbound"
D(2ebf40);                       // setting bool "realtime_damage"
D(2ebf60);                       // enum option labels (const char *[])
D(2ebf70);                       // setting enum "mode"
extern "C" unsigned data_2ebfa0; // current value of setting "mode" (+0x30)
D(2ebfb0);                       // setting bool "require_ingame"
D(2ebfd0);                       // setting bool "require_weapon"
D(2ebff0);                       // setting bool "require_attack"

extern "C" void fake_lag_gui(void *a, void *b) {
    W_label(a, b, rdata_15179c);
    W_enumw(b, a, data_2ebf70);
    W_slider(b, a, data_2ebed0);
    W_slider(b, a, data_2ebf08);
    W_bool(b, a, data_2ebf40);
    W_label(a, b, rdata_1517a2);
    W_bool(b, a, data_2ebfd0);
    W_bool(b, a, data_2ebff0);
    W_bool(b, a, data_2ebfb0);
}

extern "C" void fake_lag_register(void *m) {
    R_enum(m, rdata_1517ad, data_2ebf70);
    R_num(m, rdata_1517b2, data_2ebed0);
    R_num(m, rdata_1517ba, data_2ebf08);
    R_bool(m, rdata_1517c3, data_2ebf40);
    R_bool(m, rdata_1517d3, data_2ebfd0);
    R_bool(m, rdata_1517e2, data_2ebff0);
    R_bool(m, rdata_1517f1, data_2ebfb0);
}

extern "C" void fake_lag_status(void *self) {
    log_fmt(self, 0x20, rdata_151800, ((const char **)data_2ebf60)[data_2ebfa0]);

}
