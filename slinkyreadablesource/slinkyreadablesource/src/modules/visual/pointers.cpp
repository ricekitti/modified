// Pointers — Points to other players.
// category: visual   descriptor: 0x2f06c0   settings block: 0x2f08b8
// functions: gui pointers_gui, register pointers_register, status pointers_status
// implementation: src/logic/visual/pointers.cpp
#include "slinky/api.h"

S(153e02); // "Main"
S(153e07); // "Customization"
S(153e15); // "range"
S(153e1b); // "ignore_fov"
S(153e26); // "hide_friendlies"
S(153e36); // "style"
S(153e3c); // "color_mode"
S(153e47); // "near_far_range"
S(153e56); // "color_near"
S(153e61); // "color_far"
S(153e6b); // "color_enemies"
S(153e79); // "color_friendlies"
S(153e8a); // "scale"
S(153e90); // "radius"
S(153e97); // "scale_radius"
S(153ea4); // "%s"

D(2f08d0);                            // setting rng "near_far_range"
D(2f0920);                            // setting num "scale"
D(2f0958);                            // setting num "radius"
D(2f0990);                            // setting key "color_near"
D(2f09b0);                            // setting key "color_far"
D(2f09d0);                            // enum option labels (const char *[])
D(2f09e0);                            // setting enum "style"
extern "C" unsigned data_2f0a10;      // current value of setting "style" (+0x30)
D(2f0a20);                            // setting bool "scale_radius"
D(2f0a58);                            // setting enum "color_mode"
extern "C" unsigned data_2f0a88;      // current value of setting "color_mode" (+0x30)
D(2f0a98);                            // setting num "range"
D(2f0ad0);                            // setting num "ignore_fov"
D(2f0b08);                            // setting key "color_friendlies"
D(2f0b28);                            // setting key "color_enemies"
D(2f0b48);                            // setting bool "hide_friendlies"
extern "C" unsigned char data_2f0b60; // current value of setting "hide_friendlies" (+0x18)

extern "C" void pointers_gui(void *a, void *b) {
    W_label(a, b, rdata_153e02);
    W_slider(b, a, data_2f0a98);
    W_slider(b, a, data_2f0ad0);
    W_bool(b, a, data_2f0b48);
    W_label(a, b, rdata_153e07);
    W_enumw(b, a, data_2f09e0);
    W_enumw(b, a, data_2f0a58);
    W_range(b, a, data_2f08d0);
    if (data_2f0a88 == 0) {
        W_color(b, a, data_2f0990);
        W_color(b, a, data_2f09b0);
    } else if (data_2f0a88 == 2) {
        W_color(b, a, data_2f0b28);
        if (data_2f0b60 == 0)
            W_color(b, a, data_2f0b08);
    }
    W_slider(b, a, data_2f0920);
    W_slider(b, a, data_2f0958);
    W_bool(b, a, data_2f0a20);
}

extern "C" void pointers_register(void *m) {
    R_num(m, rdata_153e15, data_2f0a98);
    R_num(m, rdata_153e1b, data_2f0ad0);
    R_bool(m, rdata_153e26, data_2f0b48);
    R_enum(m, rdata_153e36, data_2f09e0);
    R_enum(m, rdata_153e3c, data_2f0a58);
    R_rng(m, rdata_153e47, data_2f08d0);
    R_key(m, rdata_153e56, data_2f0990);
    R_key(m, rdata_153e61, data_2f09b0);
    R_key(m, rdata_153e6b, data_2f0b28);
    R_key(m, rdata_153e79, data_2f0b08);
    R_num(m, rdata_153e8a, data_2f0920);
    R_num(m, rdata_153e90, data_2f0958);
    R_bool(m, rdata_153e97, data_2f0a20);
}

extern "C" void pointers_status(void *self) {
    log_fmt(self, 0x20, rdata_153ea4, ((const char **)data_2f09d0)[data_2f0a10]);

}
