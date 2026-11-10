// Anti Bot — Ignores NPCs for specific modules.
// category: utility   descriptor: 0x2eacd0   settings block: 0x2eaec8
// functions: gui anti_bot_gui, register anti_bot_register
// implementation: src/logic/utility/anti_bot.cpp
#include "slinky/api.h"

S(15117b); // "Main"
S(151180); // "invalid_uuid"
S(15118d); // "invalid_username"
S(15119e); // "always_invis"
S(1511ab); // "always_stationary"
S(1511bd); // "sleeping"
S(1511c6); // "min_age"

D(2eaee0); // setting num "min_age"
D(2eaf18); // setting bool "always_stationary"
D(2eaf58); // setting enum "invalid_uuid"
D(2eaf98); // setting bool "sleeping"
D(2eafb8); // setting bool "always_invis"
D(2eafd8); // setting bool "invalid_username"

extern "C" void anti_bot_gui(void *a, void *b) {
    W_label(a, b, rdata_15117b);
    W_enumw(b, a, data_2eaf58);
    W_bool(b, a, data_2eafd8);
    W_bool(b, a, data_2eafb8);
    W_bool(b, a, data_2eaf18);
    W_bool(b, a, data_2eaf98);
    W_slider(b, a, data_2eaee0);
}

extern "C" void anti_bot_register(void *m) {
    R_enum(m, rdata_151180, data_2eaf58);
    R_bool(m, rdata_15118d, data_2eafd8);
    R_bool(m, rdata_15119e, data_2eafb8);
    R_bool(m, rdata_1511ab, data_2eaf18);
    R_bool(m, rdata_1511bd, data_2eaf98);
    R_num(m, rdata_1511c6, data_2eaee0);
}
