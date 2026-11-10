// Teams — Ignores teammates for specific modules.
// category: utility   descriptor: 0x2eb400   settings block: 0x2eb5f8
// functions: gui teams_gui, register teams_register
// implementation: src/logic/utility/teams.cpp
#include "slinky/api.h"

S(15141a); // "Main"
S(1514cd); // "match_self_color"
S(1514de); // "team_colors"

D(2eb610); // setting bool "match_self_color"
D(2eb630); // setting kind2 "team_colors"

extern "C" void teams_gui(void *a, void *b) {
    W_label(a, b, rdata_15141a);
    W_bool(b, a, data_2eb610);
    W_items(b, a, data_2eb630);
}

extern "C" void teams_register(void *m) {
    R_bool(m, rdata_1514cd, data_2eb610);
    R_kind2(m, rdata_1514de, data_2eb630);
}
