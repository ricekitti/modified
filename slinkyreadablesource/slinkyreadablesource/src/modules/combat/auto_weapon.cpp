// Auto Weapon — Selects your weapon when aiming on players.
// category: combat   descriptor: 0x2f5af8   settings block: 0x2f5cf0
// functions: gui auto_weapon_gui, register auto_weapon_register
// implementation: src/logic/combat/auto_weapon.cpp
#include "slinky/api.h"

S(15663e); // "Main"
S(156643); // "Conditions"
S(15664e); // "activation_time"
S(15665e); // "require_mouse_pressed"

D(2f5d08); // setting num "activation_time"
D(2f5d40); // setting bool "require_mouse_pressed"

extern "C" void auto_weapon_gui(void *a, void *b) {
    W_label(a, b, rdata_15663e);
    W_slider(b, a, data_2f5d08);
    W_label(a, b, rdata_156643);
    W_bool(b, a, data_2f5d40);
}

extern "C" void auto_weapon_register(void *m) {
    R_num(m, rdata_15664e, data_2f5d08);
    R_bool(m, rdata_15665e, data_2f5d40);
}
