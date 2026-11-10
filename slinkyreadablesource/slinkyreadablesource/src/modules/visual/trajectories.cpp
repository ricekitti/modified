// Trajectories — Predicts the trajectory of the item in your hand.
// category: visual   descriptor: 0x2f3df0   settings block: 0x2f3fe8
// functions: gui trajectories_gui, register trajectories_register
// implementation: src/logic/visual/trajectories.cpp
#include "slinky/api.h"

S(154f1f); // "Main"
S(154f24); // "Render when holding"
S(154f38); // "line_width"
S(154f43); // "color_hit"
S(154f4d); // "color_miss"
S(154f58); // "render_throwables"
S(154f6a); // "render_pearls"
S(154f78); // "render_bow"
S(154f83); // "render_uncharged_bow"

D(2f4000); // setting num "line_width"
D(2f4038); // setting key "color_hit"
D(2f4058); // setting key "color_miss"
D(2f4078); // setting bool "render_bow"
D(2f4090); // current value of setting "render_bow" (+0x18)
D(2f4098); // setting bool "render_uncharged_bow"
D(2f40b8); // setting bool "render_pearls"
D(2f40d8); // setting bool "render_throwables"

extern "C" void trajectories_gui(void *a, void *b) {
    W_label(a, b, rdata_154f1f);
    W_slider(b, a, data_2f4000);
    W_color(b, a, data_2f4038);
    W_color(b, a, data_2f4058);
    W_label(a, b, rdata_154f24);
    W_bool(b, a, data_2f40d8);
    W_bool(b, a, data_2f40b8);
    W_bool(b, a, data_2f4078);
    if (*(unsigned char *)data_2f4090 == 1) {
        W_bool(b, a, data_2f4098);
    } else {

    }
}

extern "C" void trajectories_register(void *m) {
    R_num(m, rdata_154f38, data_2f4000);
    R_key(m, rdata_154f43, data_2f4038);
    R_key(m, rdata_154f4d, data_2f4058);
    R_bool(m, rdata_154f58, data_2f40d8);
    R_bool(m, rdata_154f6a, data_2f40b8);
    R_bool(m, rdata_154f78, data_2f4078);
    R_bool(m, rdata_154f83, data_2f4098);
}
