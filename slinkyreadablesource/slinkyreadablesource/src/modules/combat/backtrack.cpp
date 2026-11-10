// Backtrack — Uses lag to make players easier to hit.
// category: combat   descriptor: 0x2edad0   settings block: 0x2edcc8
// functions: gui backtrack_gui, register backtrack_register, status backtrack_status
// implementation: src/logic/combat/backtrack.cpp
#include "slinky/api.h"

S(1522bc); // "Main"
S(1522c1); // "Real position indicator"
S(1522d9); // "Conditions"
S(1522e4); // "target_dist"
S(1522f0); // "delay"
S(1522f6); // "max_hurt_time"
S(152304); // "cooldown"
S(15230d); // "disable_on_hit"
S(15231c); // "render_remote_pos"
S(15232e); // "indicator_color"
S(15233e); // "indicator_line_width"
S(152353); // "indicator_filled"
S(152364); // "indicator_rotate"
S(152375); // "require_mouse_pressed"
S(15238b); // "require_weapon"
S(15239a); // "%.0fms"

D(2edce0);                    // setting num "indicator_line_width"
D(2edd18);                    // setting key "indicator_color"
D(2edd38);                    // setting bool "indicator_rotate"
D(2edd58);                    // setting bool "indicator_filled"
D(2edd78);                    // setting num "max_hurt_time"
D(2eddb0);                    // setting rng "target_dist"
D(2ede00);                    // setting num "delay"
extern "C" float data_2ede30; // current value of setting "delay" (+0x30)
D(2ede38);                    // setting num "cooldown"
D(2ede70);                    // setting bool "require_mouse_pressed"
D(2ede90);                    // setting bool "require_weapon"
D(2edeb0);                    // setting bool "disable_on_hit"
D(2eded0);                    // setting bool "render_remote_pos"
D(2edee8);                    // current value of setting "render_remote_pos" (+0x18)

extern "C" void backtrack_gui(void *a, void *b) {
    W_label(a, b, rdata_1522bc);
    W_range(b, a, data_2eddb0);
    W_slider(b, a, data_2ede00);
    W_slider(b, a, data_2edd78);
    W_slider(b, a, data_2ede38);
    W_bool(b, a, data_2edeb0);
    W_label(a, b, rdata_1522c1);
    W_bool(b, a, data_2eded0);
    if (*(unsigned char *)data_2edee8 == 1) {
        W_color(b, a, data_2edd18);
        W_slider(b, a, data_2edce0);
        W_bool(b, a, data_2edd58);
        W_bool(b, a, data_2edd38);
    }
    W_label(a, b, rdata_1522d9);
    W_bool(b, a, data_2ede70);
    W_bool(b, a, data_2ede90);
}

extern "C" void backtrack_register(void *m) {
    R_rng(m, rdata_1522e4, data_2eddb0);
    R_num(m, rdata_1522f0, data_2ede00);
    R_num(m, rdata_1522f6, data_2edd78);
    R_num(m, rdata_152304, data_2ede38);
    R_bool(m, rdata_15230d, data_2edeb0);
    R_bool(m, rdata_15231c, data_2eded0);
    R_key(m, rdata_15232e, data_2edd18);
    R_num(m, rdata_15233e, data_2edce0);
    R_bool(m, rdata_152353, data_2edd58);
    R_bool(m, rdata_152364, data_2edd38);
    R_bool(m, rdata_152375, data_2ede70);
    R_bool(m, rdata_15238b, data_2ede90);
}

extern "C" void backtrack_status(void *self) {
    log_fmt(self, 0x20, rdata_15239a, (double)data_2ede30);

}
