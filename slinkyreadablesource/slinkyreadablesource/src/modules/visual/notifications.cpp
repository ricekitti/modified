// Notifications — Informs you when certain actions are performed.
// category: visual   descriptor: 0x2ed320   settings block: 0x2ed518
// functions: gui notifications_gui, register notifications_register
// implementation: src/logic/visual/notifications.cpp
#include "slinky/api.h"

S(152012); // "Hide if"
S(15201a); // "Visible mod categories"
S(152031); // "hide_ingame"
S(15203d); // "hide_hold_notifs"
S(15204e); // "hide_bind_only"
S(15205d); // "show_combat"
S(152069); // "show_move"
S(152073); // "show_block"
S(15207e); // "show_visual"
S(15208a); // "show_utility"

D(2ed530); // setting bool "hide_bind_only"
D(2ed550); // setting bool "hide_hold_notifs"
D(2ed570); // setting bool "show_combat"
D(2ed590); // setting bool "show_move"
D(2ed5b0); // setting bool "show_block"
D(2ed5d0); // setting bool "show_visual"
D(2ed5f0); // setting bool "show_utility"
D(2ed610); // setting bool "hide_ingame"

extern "C" void notifications_gui(void *a, void *b) {
    W_label(a, b, rdata_152012);
    W_bool(b, a, data_2ed610);
    W_bool(b, a, data_2ed550);
    W_bool(b, a, data_2ed530);
    W_label(a, b, rdata_15201a);
    W_bool(b, a, data_2ed570);
    W_bool(b, a, data_2ed590);
    W_bool(b, a, data_2ed5b0);
    W_bool(b, a, data_2ed5d0);
    W_bool(b, a, data_2ed5f0);
}

extern "C" void notifications_register(void *m) {
    R_bool(m, rdata_152031, data_2ed610);
    R_bool(m, rdata_15203d, data_2ed550);
    R_bool(m, rdata_15204e, data_2ed530);
    R_bool(m, rdata_15205d, data_2ed570);
    R_bool(m, rdata_152069, data_2ed590);
    R_bool(m, rdata_152073, data_2ed5b0);
    R_bool(m, rdata_15207e, data_2ed5d0);
    R_bool(m, rdata_15208a, data_2ed5f0);
}
