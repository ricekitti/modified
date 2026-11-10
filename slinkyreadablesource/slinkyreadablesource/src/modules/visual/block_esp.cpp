// Block ESP — Highlights blocks.
// category: visual   descriptor: 0x2f1740   settings block: 0x2f1938
// functions: gui block_esp_gui, register block_esp_register
// implementation: src/logic/visual/block_esp.cpp
#include "slinky/api.h"

S(154298); // "Main"
S(15429d); // "Block render limit"
S(1542b0); // "Customization"
S(1542be); // "range"
S(1542c4); // "allowed_blocks"
S(1542d3); // "should_limit_block_render"
S(1542ed); // "block_render_limit"
S(154300); // "line_width"

D(2f1950); // setting kind2 "allowed_blocks"
D(2f19b8); // setting num "line_width"
D(2f19f0); // setting num "block_render_limit"
D(2f1a28); // setting num "range"
D(2f1a60); // setting bool "should_limit_block_render"
D(2f1a78); // current value of setting "should_limit_block_render" (+0x18)

extern "C" void block_esp_gui(void *a, void *b) {
    W_label(a, b, rdata_154298);
    W_slider(b, a, data_2f1a28);
    W_items(b, a, data_2f1950);
    W_label(a, b, rdata_15429d);
    W_bool(b, a, data_2f1a60);
    if (*(unsigned char *)data_2f1a78 == 1) {
        W_slider(b, a, data_2f19f0);
    }
    W_label(a, b, rdata_1542b0);
    W_slider(b, a, data_2f19b8);
}

extern "C" void block_esp_register(void *m) {
    R_num(m, rdata_1542be, data_2f1a28);
    R_kind2(m, rdata_1542c4, data_2f1950);
    R_bool(m, rdata_1542d3, data_2f1a60);
    R_num(m, rdata_1542ed, data_2f19f0);
    R_num(m, rdata_154300, data_2f19b8);
}
