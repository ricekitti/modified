// Criticals — Increases chance of landing critical hits.
// category: combat   descriptor: 0x2ec010   settings block: 0x2ec208
// functions: gui criticals_gui, register criticals_register, status criticals_status
// implementation: src/logic/combat/criticals.cpp
#include "slinky/api.h"

S(1518c4); // "Main"
S(1518c9); // "Conditions"
S(1518d4); // "mode"
S(1518d9); // "chance"
S(1518e0); // "max_delay"
S(1518ea); // "timer_speed"
S(1518f6); // "boost"
S(1518fc); // "require_weapon"
S(15190b); // "require_mouse_pressed"
S(151921); // "%s"

D(2ec220);                       // setting num "max_delay"
D(2ec258);                       // setting num "chance"
D(2ec290);                       // setting bool "require_mouse_pressed"
D(2ec2b0);                       // setting bool "require_weapon"
D(2ec2d0);                       // enum option labels (const char *[])
D(2ec2e0);                       // setting enum "mode"
extern "C" unsigned data_2ec310; // current value of setting "mode" (+0x30)
D(2ec320);                       // setting num "timer_speed"
D(2ec358);                       // setting num "boost"

extern "C" void criticals_gui(void *a, void *b) {
    W_label(a, b, rdata_1518c4);
    W_enumw(b, a, data_2ec2e0);
    W_slider(b, a, data_2ec258);
    if (data_2ec310 != 0) {
        W_slider(b, a, data_2ec320);
        W_slider(b, a, data_2ec358);
    } else {
        W_slider(b, a, data_2ec220);
    }
    W_label(a, b, rdata_1518c9);
    W_bool(b, a, data_2ec2b0);
    W_bool(b, a, data_2ec290);
}

extern "C" void criticals_register(void *m) {
    R_enum(m, rdata_1518d4, data_2ec2e0);
    R_num(m, rdata_1518d9, data_2ec258);
    R_num(m, rdata_1518e0, data_2ec220);
    R_num(m, rdata_1518ea, data_2ec320);
    R_num(m, rdata_1518f6, data_2ec358);
    R_bool(m, rdata_1518fc, data_2ec2b0);
    R_bool(m, rdata_15190b, data_2ec290);
}

extern "C" void criticals_status(void *self) {
    log_fmt(self, 0x20, rdata_151921, ((const char **)data_2ec2d0)[data_2ec310]);

}
