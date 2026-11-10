// Auto Rod — Throws your fishing rod for you.
// category: utility   descriptor: 0x2f3520   settings block: 0x2f3718
// functions: gui auto_rod_gui, register auto_rod_register
// implementation: src/logic/utility/auto_rod.cpp
#include "slinky/api.h"

S(154bf5); // "Main"
S(154bfa); // "Throwables"
S(154c05); // "retract_speed"
S(154c13); // "delay"
S(154c19); // "randomize_delays"
S(154c2a); // "always_switch_to_weapon"
S(154c42); // "allow_throwables"
S(154c53); // "throw_speed"
S(154c5f); // "require_not_mouse_over"

D(2f3738); // setting bool "require_not_mouse_over"
D(2f3758); // setting bool "allow_throwables"
D(2f3778); // setting bool "always_switch_to_weapon"
D(2f3798); // setting num "delay"
D(2f37d0); // setting bool "randomize_delays"
D(2f37f0); // setting num "retract_speed"
D(2f3828); // setting num "throw_speed"

extern "C" void auto_rod_gui(void *a, void *b) {
    W_label(a, b, rdata_154bf5);
    W_slider(b, a, data_2f37f0);
    W_slider(b, a, data_2f3798);
    W_bool(b, a, data_2f37d0);
    W_bool(b, a, data_2f3778);
    W_label(a, b, rdata_154bfa);
    W_bool(b, a, data_2f3758);
    W_slider(b, a, data_2f3828);
    W_label(a, b, rdata_154bf5);
    W_bool(b, a, data_2f3738);
}

extern "C" void auto_rod_register(void *m) {
    R_num(m, rdata_154c05, data_2f37f0);
    R_num(m, rdata_154c13, data_2f3798);
    R_bool(m, rdata_154c19, data_2f37d0);
    R_bool(m, rdata_154c2a, data_2f3778);
    R_bool(m, rdata_154c42, data_2f3758);
    R_num(m, rdata_154c53, data_2f3828);
    R_bool(m, rdata_154c5f, data_2f3738);
}
