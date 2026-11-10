// Hit Select — Perfectly times your hits.
// category: combat   descriptor: 0x2ee4f0   settings block: 0x2ee6e8
// functions: gui hit_select_gui, register hit_select_register, status hit_select_status
// implementation: src/logic/combat/hit_select.cpp
#include "slinky/api.h"

S(15282d); // "Main"
S(152832); // "Cancel rate"
S(15283e); // "Conditions"
S(152849); // "mode"
S(15284e); // "cancel_rate"
S(15285a); // "cancel_rate_air"
S(15286a); // "pause_dur"
S(152874); // "wait_for_damage_dur"
S(152888); // "trades_delay"
S(152895); // "use_server_time"
S(1528a5); // "fake_swing"
S(1528b0); // "crits_allow_reduce"
S(1528c3); // "require_damage"
S(1528d2); // "require_sprint"
S(1528e1); // "require_weapon"
S(1528f0); // "%s"

D(2ee700);                       // setting num "cancel_rate_air"
D(2ee738);                       // setting num "pause_dur"
D(2ee770);                       // setting num "wait_for_damage_dur"
D(2ee7a8);                       // setting num "trades_delay"
D(2ee7e0);                       // setting num "cancel_rate"
D(2ee818);                       // setting bool "require_sprint"
D(2ee838);                       // setting bool "require_weapon"
D(2ee860);                       // enum option labels (const char *[])
D(2ee870);                       // setting enum "mode"
extern "C" unsigned data_2ee8a0; // current value of setting "mode" (+0x30)
D(2ee8b0);                       // setting bool "use_server_time"
D(2ee8d0);                       // setting bool "crits_allow_reduce"
D(2ee8f0);                       // setting bool "require_damage"
D(2ee910);                       // setting bool "fake_swing"

extern "C" void hit_select_gui(void *a, void *b) {
    W_label(a, b, rdata_15282d);
    W_enumw(b, a, data_2ee870);
    W_slider(b, a, data_2ee738);
    if (data_2ee8a0 == 0) {
        W_slider(b, a, data_2ee770);
        W_slider(b, a, data_2ee7a8);
    }
    W_bool(b, a, data_2ee8b0);
    W_bool(b, a, data_2ee910);
    if (data_2ee8a0 == 1) {
        W_bool(b, a, data_2ee8d0);
        W_bool(b, a, data_2ee8f0);
    }
    W_label(a, b, rdata_152832);
    W_slider(b, a, data_2ee7e0);
    W_slider(b, a, data_2ee700);
    W_label(a, b, rdata_15283e);
    W_bool(b, a, data_2ee818);
    W_bool(b, a, data_2ee838);
}

extern "C" void hit_select_register(void *m) {
    R_enum(m, rdata_152849, data_2ee870);
    R_num(m, rdata_15284e, data_2ee7e0);
    R_num(m, rdata_15285a, data_2ee700);
    R_num(m, rdata_15286a, data_2ee738);
    R_num(m, rdata_152874, data_2ee770);
    R_num(m, rdata_152888, data_2ee7a8);
    R_bool(m, rdata_152895, data_2ee8b0);
    R_bool(m, rdata_1528a5, data_2ee910);
    R_bool(m, rdata_1528b0, data_2ee8d0);
    R_bool(m, rdata_1528c3, data_2ee8f0);
    R_bool(m, rdata_1528d2, data_2ee818);
    R_bool(m, rdata_1528e1, data_2ee838);
}

extern "C" void hit_select_status(void *self) {
    log_fmt(self, 0x20, rdata_1528f0, ((const char **)data_2ee860)[data_2ee8a0]);

}
