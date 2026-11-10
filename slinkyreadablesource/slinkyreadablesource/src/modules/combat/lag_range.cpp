// Lag Range — Uses lag to stay out of target range.
// category: combat   descriptor: 0x2ef9c0   settings block: 0x2efbb8
// functions: gui lag_range_gui, register lag_range_register, status lag_range_status
// implementation: src/logic/combat/lag_range.cpp
#include "slinky/api.h"

S(152f9e); // "Main"
S(152fa3); // "Flush if"
S(152fac); // "Real position indicator"
S(152fc4); // "Conditions"
S(152fcf); // "delay"
S(152fd5); // "flush_sprint_reset"
S(152fe8); // "flush_splash_potion"
S(152ffc); // "render_remote_pos"
S(15300e); // "indicator_color"
S(15301e); // "indicator_line_width"
S(153033); // "indicator_filled"
S(153044); // "require_weapon"
S(153053); // "%.0fms"

D(2efbd0);                    // setting num "indicator_line_width"
D(2efc08);                    // setting key "indicator_color"
D(2efc28);                    // setting bool "indicator_filled"
D(2efc48);                    // setting num "delay"
extern "C" float data_2efc78; // current value of setting "delay" (+0x30)
D(2efc80);                    // setting bool "require_weapon"
D(2efca0);                    // setting bool "flush_splash_potion"
D(2efcc0);                    // setting bool "flush_sprint_reset"
D(2efce0);                    // setting bool "render_remote_pos"
D(2efcf8);                    // current value of setting "render_remote_pos" (+0x18)

extern "C" void lag_range_gui(void *a, void *b) {
    W_label(a, b, rdata_152f9e);
    W_slider(b, a, data_2efc48);
    W_label(a, b, rdata_152fa3);
    W_bool(b, a, data_2efcc0);
    W_bool(b, a, data_2efca0);
    W_label(a, b, rdata_152fac);
    W_bool(b, a, data_2efce0);
    if (*(unsigned char *)data_2efcf8 == 1) {
        W_color(b, a, data_2efc08);
        W_slider(b, a, data_2efbd0);
        W_bool(b, a, data_2efc28);
    }
    W_label(a, b, rdata_152fc4);
    W_bool(b, a, data_2efc80);
}

extern "C" void lag_range_register(void *m) {
    R_num(m, rdata_152fcf, data_2efc48);
    R_bool(m, rdata_152fd5, data_2efcc0);
    R_bool(m, rdata_152fe8, data_2efca0);
    R_bool(m, rdata_152ffc, data_2efce0);
    R_key(m, rdata_15300e, data_2efc08);
    R_num(m, rdata_15301e, data_2efbd0);
    R_bool(m, rdata_153033, data_2efc28);
    R_bool(m, rdata_153044, data_2efc80);
}

extern "C" void lag_range_status(void *self) {
    log_fmt(self, 0x20, rdata_153053, (double)data_2efc78);

}
