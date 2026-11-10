// Friends — Lets you mark other players as friendly.
// category: utility   descriptor: 0x2f0038   settings block: 0x2f0230
// functions: gui friends_gui, register friends_register
// implementation: src/logic/utility/friends.cpp
#include "slinky/api.h"

S(153673); // "Main"
S(153678); // "middle_click_add"
S(153689); // "friends"

D(2f0248); // setting bool "middle_click_add"
D(2f0268); // setting kind2 "friends"

extern "C" void friends_gui(void *a, void *b) {
    W_label(a, b, rdata_153673);
    W_bool(b, a, data_2f0248);
}

extern "C" void friends_register(void *m) {
    R_bool(m, rdata_153678, data_2f0248);
    R_kind2(m, rdata_153689, data_2f0268);
}
