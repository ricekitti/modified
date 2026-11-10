// Item ESP — Highlights dropped items.
// category: visual   descriptor: 0x2f1a80   settings block: 0x2f1c78
// functions: gui item_esp_gui, register item_esp_register
// implementation: src/logic/visual/item_esp.cpp
#include "slinky/api.h"

S(1543ce); // "Main"
S(1543d3); // "Customization"
S(1543f8); // "range"
S(1543fe); // "show_count"
S(154409); // "allowed_items"
S(154417); // "highlight_all"
S(154425); // "default_color"

D(2f1c90); // setting bool "show_count"
D(2f1cb0); // setting num "range"
D(2f1ce8); // setting key "default_color"
D(2f1d08); // setting bool "highlight_all"
D(2f1d20); // current value of setting "highlight_all" (+0x18)
D(2f1d28); // setting kind2 "allowed_items"

extern "C" void item_esp_gui(void *a, void *b) {
    W_label(a, b, rdata_1543ce);
    W_slider(b, a, data_2f1cb0);
    W_label(a, b, rdata_1543d3);
    W_bool(b, a, data_2f1c90);
    W_items(b, a, data_2f1d28);
    W_bool(b, a, data_2f1d08);
    if (*(unsigned char *)data_2f1d20 == 1) {
        W_color(b, a, data_2f1ce8);
    } else {

    }
}

extern "C" void item_esp_register(void *m) {
    R_num(m, rdata_1543f8, data_2f1cb0);
    R_bool(m, rdata_1543fe, data_2f1c90);
    R_kind2(m, rdata_154409, data_2f1d28);
    R_bool(m, rdata_154417, data_2f1d08);
    R_key(m, rdata_154425, data_2f1ce8);
}
