// Piercing — Allows hitting entities through obstructions.
// category: combat   descriptor: 0x2f3ab0   settings block: 0x2f3ca8
// functions: gui piercing_gui, register piercing_register
// implementation: src/logic/combat/piercing.cpp
#include "slinky/api.h"

S(154d7c); // "Always hit through"
S(154d8f); // "Conditions"
S(154d9a); // "sort_by"
S(154da2); // "non_players"
S(154dae); // "friendlies"
S(154db9); // "bots"
S(154dbe); // "enemies"
S(154dc6); // "blocks"
S(154dcd); // "require_weapon"
S(154ddc); // "require_inside_bb"

D(2f3cc0); // setting bool "require_inside_bb"
D(2f3ce0); // setting bool "require_weapon"
D(2f3d00); // setting bool "non_players"
D(2f3d20); // setting bool "friendlies"
D(2f3d40); // setting bool "bots"
D(2f3d60); // setting bool "enemies"
D(2f3d90); // setting enum "sort_by"
D(2f3dd0); // setting bool "blocks"

extern "C" void piercing_gui(void *a, void *b) {
    W_label(a, b, rdata_154d7c);
    W_enumw(b, a, data_2f3d90);
    W_bool(b, a, data_2f3d00);
    W_bool(b, a, data_2f3d20);
    W_bool(b, a, data_2f3d40);
    W_bool(b, a, data_2f3d60);
    W_bool(b, a, data_2f3dd0);
    W_label(a, b, rdata_154d8f);
    W_bool(b, a, data_2f3ce0);
    W_bool(b, a, data_2f3cc0);
}

extern "C" void piercing_register(void *m) {
    R_enum(m, rdata_154d9a, data_2f3d90);
    R_bool(m, rdata_154da2, data_2f3d00);
    R_bool(m, rdata_154dae, data_2f3d20);
    R_bool(m, rdata_154db9, data_2f3d40);
    R_bool(m, rdata_154dbe, data_2f3d60);
    R_bool(m, rdata_154dc6, data_2f3dd0);
    R_bool(m, rdata_154dcd, data_2f3ce0);
    R_bool(m, rdata_154ddc, data_2f3cc0);
}
