// Name Tags — Makes player name tags easier to see.
// category: visual   descriptor: 0x2f02d0   settings block: 0x2f04c8
// functions: gui name_tags_gui, register name_tags_register
// implementation: src/logic/visual/name_tags.cpp
#include "slinky/api.h"

S(153bbc); // "Main"
S(153bc1); // "Extra info"
S(153bcc); // "range"
S(153bd2); // "scale"
S(153bd8); // "show_own_name_tag"
S(153bea); // "use_display_name"
S(153bfb); // "distance_scaling"
S(153c0c); // "background"
S(153c17); // "text_shadow"
S(153c23); // "show_health"
S(153c2f); // "health_fmt"
S(153c3a); // "show_distance"
S(153c48); // "show_equipment"
S(153c57); // "show_enchants"

D(2f04e0); // setting num "scale"
D(2f0518); // setting bool "distance_scaling"
D(2f0538); // setting bool "background"
D(2f0558); // setting bool "text_shadow"
D(2f0578); // setting bool "show_equipment"
D(2f0590); // current value of setting "show_equipment" (+0x18)
D(2f0598); // setting bool "show_enchants"
D(2f05b8); // setting num "range"
D(2f05f0); // setting bool "show_own_name_tag"
D(2f0610); // setting bool "use_display_name"
D(2f0630); // setting bool "show_health"
D(2f0648); // current value of setting "show_health" (+0x18)
D(2f0650); // setting bool "show_distance"
D(2f0680); // setting enum "health_fmt"

extern "C" void name_tags_gui(void *a, void *b) {
    W_label(a, b, rdata_153bbc);
    W_slider(b, a, data_2f05b8);
    W_slider(b, a, data_2f04e0);
    W_bool(b, a, data_2f05f0);
    W_bool(b, a, data_2f0610);
    W_bool(b, a, data_2f0518);
    W_bool(b, a, data_2f0538);
    W_bool(b, a, data_2f0558);
    W_label(a, b, rdata_153bc1);
    W_bool(b, a, data_2f0630);
    if (*(unsigned char *)data_2f0648 == 1) {
        W_enumw(b, a, data_2f0680);
    }
    W_bool(b, a, data_2f0650);
    W_bool(b, a, data_2f0578);
    if (*(unsigned char *)data_2f0590 == 1) {
        W_bool(b, a, data_2f0598);
    } else {

    }
}

extern "C" void name_tags_register(void *m) {
    R_num(m, rdata_153bcc, data_2f05b8);
    R_num(m, rdata_153bd2, data_2f04e0);
    R_bool(m, rdata_153bd8, data_2f05f0);
    R_bool(m, rdata_153bea, data_2f0610);
    R_bool(m, rdata_153bfb, data_2f0518);
    R_bool(m, rdata_153c0c, data_2f0538);
    R_bool(m, rdata_153c17, data_2f0558);
    R_bool(m, rdata_153c23, data_2f0630);
    R_enum(m, rdata_153c2f, data_2f0680);
    R_bool(m, rdata_153c3a, data_2f0650);
    R_bool(m, rdata_153c48, data_2f0578);
    R_bool(m, rdata_153c57, data_2f0598);
}
