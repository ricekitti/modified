// Sprint — Automatically sprints for you.
// category: move   descriptor: 0x2ed840   settings block: 0x2eda38
// functions: gui sprint_gui, register sprint_register
// implementation: src/logic/move/sprint.cpp
#include "slinky/api.h"

S(152129); // "Allow while"
S(152135); // "while_using_item"
S(152146); // "while_backwards"
S(152156); // "while_sideways"
S(152165); // "while_in_gui"

D(2eda50); // setting bool "while_in_gui"
D(2eda70); // setting bool "while_using_item"
D(2eda90); // setting bool "while_backwards"
D(2edab0); // setting bool "while_sideways"

extern "C" void sprint_gui(void *a, void *b) {
    W_label(a, b, rdata_152129);
    W_bool(b, a, data_2eda70);
    W_bool(b, a, data_2eda90);
    W_bool(b, a, data_2edab0);
    W_bool(b, a, data_2eda50);
}

extern "C" void sprint_register(void *m) {
    R_bool(m, rdata_152135, data_2eda70);
    R_bool(m, rdata_152146, data_2eda90);
    R_bool(m, rdata_152156, data_2edab0);
    R_bool(m, rdata_152165, data_2eda50);
}
