// Free Look — Freely move the camera around.
// category: visual   descriptor: 0x2edef0   settings block: 0x2ee0e8
// functions: gui free_look_gui, register free_look_register
// implementation: src/logic/visual/free_look.cpp
#include "slinky/api.h"

S(152405); // "Main"
S(15240a); // "target_perspective"

D(2ee118); // setting enum "target_perspective"

extern "C" void free_look_gui(void *a, void *b) {
    W_label(a, b, rdata_152405);
    W_enumw(b, a, data_2ee118);
}

extern "C" void free_look_register(void *m) {
    R_enum(m, rdata_15240a, data_2ee118);
}
