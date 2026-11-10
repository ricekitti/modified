// Ghost Hand — Allows interacting with blocks through entities.
// category: block   descriptor: 0x2f48b0   settings block: 0x2f4aa8
// functions: gui ghost_hand_gui, register ghost_hand_register
// implementation: src/logic/block/ghost_hand.cpp
#include "slinky/api.h"

S(15549b); // "Interact through"
S(1554ac); // "Allow while using"
S(1554be); // "Conditions"
S(1554c9); // "non_players"
S(1554d5); // "friendlies"
S(1554e0); // "bots"
S(1554e5); // "enemies"
S(1554ed); // "allow_using_sword"
S(1554ff); // "allow_using_tools"
S(155511); // "allow_using_fists"
S(155523); // "allow_using_buckets"
S(155537); // "allow_using_fns"
S(155547); // "allow_using_cobweb"
S(15555a); // "allow_using_other"
S(15556c); // "require_mouse_pressed"

D(2f4ac0); // setting bool "require_mouse_pressed"
D(2f4ae0); // setting bool "allow_using_fists"
D(2f4b00); // setting bool "allow_using_sword"
D(2f4b20); // setting bool "allow_using_tools"
D(2f4b40); // setting bool "allow_using_buckets"
D(2f4b60); // setting bool "allow_using_fns"
D(2f4b80); // setting bool "allow_using_cobweb"
D(2f4ba0); // setting bool "allow_using_other"
D(2f4bc0); // setting bool "non_players"
D(2f4be0); // setting bool "bots"
D(2f4c00); // setting bool "friendlies"
D(2f4c20); // setting bool "enemies"

extern "C" void ghost_hand_gui(void *a, void *b) {
    W_label(a, b, rdata_15549b);
    W_bool(b, a, data_2f4bc0);
    W_bool(b, a, data_2f4c00);
    W_bool(b, a, data_2f4be0);
    W_bool(b, a, data_2f4c20);
    W_label(a, b, rdata_1554ac);
    W_bool(b, a, data_2f4b00);
    W_bool(b, a, data_2f4b20);
    W_bool(b, a, data_2f4ae0);
    W_bool(b, a, data_2f4b40);
    W_bool(b, a, data_2f4b60);
    W_bool(b, a, data_2f4b80);
    W_bool(b, a, data_2f4ba0);
    W_label(a, b, rdata_1554be);
    W_bool(b, a, data_2f4ac0);
}

extern "C" void ghost_hand_register(void *m) {
    R_bool(m, rdata_1554c9, data_2f4bc0);
    R_bool(m, rdata_1554d5, data_2f4c00);
    R_bool(m, rdata_1554e0, data_2f4be0);
    R_bool(m, rdata_1554e5, data_2f4c20);
    R_bool(m, rdata_1554ed, data_2f4b00);
    R_bool(m, rdata_1554ff, data_2f4b20);
    R_bool(m, rdata_155511, data_2f4ae0);
    R_bool(m, rdata_155523, data_2f4b40);
    R_bool(m, rdata_155537, data_2f4b60);
    R_bool(m, rdata_155547, data_2f4b80);
    R_bool(m, rdata_15555a, data_2f4ba0);
    R_bool(m, rdata_15556c, data_2f4ac0);
}
