// Bridge Assist — Sneaks and unsneaks for you while bridging.
// category: block   descriptor: 0x2e9ac0   settings block: 0x2e9cb8
// functions: gui bridge_assist_gui, register bridge_assist_register
// implementation: src/logic/block/bridge_assist.cpp
#include "slinky/api.h"

S(150914); // "Main"
S(150919); // "Conditions"
S(150924); // "edge_offset"
S(150930); // "unsneak_delay"
S(15093e); // "select_blocks"
S(15094c); // "randomize"
S(150956); // "sneak_on_jump"
S(150964); // "avoid_double_sneak"
S(150977); // "require_sneak"
S(150985); // "require_holding_blocks"
S(15099c); // "require_looking_down"
S(1509b1); // "require_not_forward"

D(2e9cd0); // setting bool "require_sneak"
D(2e9cf0); // setting bool "require_looking_down"
D(2e9d10); // setting bool "require_not_forward"
D(2e9d48); // setting enum "select_blocks"
D(2e9d88); // setting bool "require_holding_blocks"
D(2e9da8); // setting bool "sneak_on_jump"
D(2e9dc8); // setting num "edge_offset"
D(2e9e00); // setting bool "randomize"
D(2e9e20); // setting bool "avoid_double_sneak"
D(2e9e40); // setting num "unsneak_delay"

extern "C" void bridge_assist_gui(void *a, void *b) {
    W_label(a, b, rdata_150914);
    W_slider(b, a, data_2e9dc8);
    W_slider(b, a, data_2e9e40);
    W_enumw(b, a, data_2e9d48);
    W_bool(b, a, data_2e9e00);
    W_bool(b, a, data_2e9da8);
    W_bool(b, a, data_2e9e20);
    W_label(a, b, rdata_150919);
    W_bool(b, a, data_2e9cd0);
    W_bool(b, a, data_2e9d88);
    W_bool(b, a, data_2e9cf0);
    W_bool(b, a, data_2e9d10);
}

extern "C" void bridge_assist_register(void *m) {
    R_num(m, rdata_150924, data_2e9dc8);
    R_num(m, rdata_150930, data_2e9e40);
    R_enum(m, rdata_15093e, data_2e9d48);
    R_bool(m, rdata_15094c, data_2e9e00);
    R_bool(m, rdata_150956, data_2e9da8);
    R_bool(m, rdata_150964, data_2e9e20);
    R_bool(m, rdata_150977, data_2e9cd0);
    R_bool(m, rdata_150985, data_2e9d88);
    R_bool(m, rdata_15099c, data_2e9cf0);
    R_bool(m, rdata_1509b1, data_2e9d10);
}
