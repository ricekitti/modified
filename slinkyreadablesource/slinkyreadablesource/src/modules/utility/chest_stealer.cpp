// Chest Stealer — Automatically loots chests for you.
// category: utility   descriptor: 0x2f5288   settings block: 0x2f5480
// functions: gui chest_stealer_gui, register chest_stealer_register
// implementation: src/logic/utility/chest_stealer.cpp
#include "slinky/api.h"

S(155fee); // "Main"
S(15601e); // "delay_after_open"
S(15602f); // "delay_before_close"
S(156042); // "speed"
S(156048); // "smart_speed"
S(156054); // "require_vanilla_title"

D(2f5498); // setting num "delay_after_open"
D(2f54d0); // setting bool "require_vanilla_title"
D(2f54f0); // setting num "delay_before_close"
D(2f5528); // setting num "speed"
D(2f5560); // setting bool "smart_speed"

extern "C" void chest_stealer_gui(void *a, void *b) {
    W_label(a, b, rdata_155fee);
    W_slider(b, a, data_2f5498);
    W_slider(b, a, data_2f54f0);
    W_slider(b, a, data_2f5528);
    W_bool(b, a, data_2f5560);
    W_label(a, b, rdata_155fee);
    W_bool(b, a, data_2f54d0);
}

extern "C" void chest_stealer_register(void *m) {
    R_num(m, rdata_15601e, data_2f5498);
    R_num(m, rdata_15602f, data_2f54f0);
    R_num(m, rdata_156042, data_2f5528);
    R_bool(m, rdata_156048, data_2f5560);
    R_bool(m, rdata_156054, data_2f54d0);
}
