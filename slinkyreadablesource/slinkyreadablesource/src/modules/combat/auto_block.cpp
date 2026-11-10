// Auto Block — Blocks your sword for you in combat.
// category: combat   descriptor: 0x2eaff8   settings block: 0x2eb1f0
// functions: gui auto_block_gui, register auto_block_register
// implementation: src/logic/combat/auto_block.cpp
#include "slinky/api.h"

S(1512da); // "Main"
S(1512df); // "Force block animation"
S(1512f5); // "Lag"
S(1512f9); // "Conditions"
S(151304); // "range"
S(15130a); // "max_hurt_time"
S(151318); // "max_hold_time"
S(151326); // "fake_block_animation"
S(15133b); // "fake_block_animation_require_target"
S(15135f); // "lag_chance"
S(15136a); // "lag_duration"
S(151377); // "lag_flush_attack"
S(151388); // "lag_reblock"
S(151394); // "require_left_mouse_pressed"
S(1513af); // "require_right_mouse_pressed"
S(1513cb); // "require_damage"

D(2eb208); // setting num "max_hold_time"
D(2eb240); // setting num "lag_chance"
D(2eb278); // setting num "lag_duration"
D(2eb2b0); // setting num "max_hurt_time"
D(2eb2e8); // setting bool "require_left_mouse_pressed"
D(2eb308); // setting bool "require_right_mouse_pressed"
D(2eb328); // setting bool "require_damage"
D(2eb348); // setting bool "fake_block_animation"
D(2eb360); // current value of setting "fake_block_animation" (+0x18)
D(2eb368); // setting bool "fake_block_animation_require_target"
D(2eb388); // setting num "range"
D(2eb3c0); // setting bool "lag_flush_attack"
D(2eb3e0); // setting bool "lag_reblock"

extern "C" void auto_block_gui(void *a, void *b) {
    W_label(a, b, rdata_1512da);
    W_slider(b, a, data_2eb388);
    W_slider(b, a, data_2eb2b0);
    W_slider(b, a, data_2eb208);
    W_label(a, b, rdata_1512df);
    W_bool(b, a, data_2eb348);
    if (*(unsigned char *)data_2eb360 == 1) {
        W_bool(b, a, data_2eb368);
    }
    W_label(a, b, rdata_1512f5);
    W_slider(b, a, data_2eb240);
    W_slider(b, a, data_2eb278);
    W_bool(b, a, data_2eb3c0);
    W_bool(b, a, data_2eb3e0);
    W_label(a, b, rdata_1512f9);
    W_bool(b, a, data_2eb2e8);
    W_bool(b, a, data_2eb308);
    W_bool(b, a, data_2eb328);
}

extern "C" void auto_block_register(void *m) {
    R_num(m, rdata_151304, data_2eb388);
    R_num(m, rdata_15130a, data_2eb2b0);
    R_num(m, rdata_151318, data_2eb208);
    R_bool(m, rdata_151326, data_2eb348);
    R_bool(m, rdata_15133b, data_2eb368);
    R_num(m, rdata_15135f, data_2eb240);
    R_num(m, rdata_15136a, data_2eb278);
    R_bool(m, rdata_151377, data_2eb3c0);
    R_bool(m, rdata_151388, data_2eb3e0);
    R_bool(m, rdata_151394, data_2eb2e8);
    R_bool(m, rdata_1513af, data_2eb308);
    R_bool(m, rdata_1513cb, data_2eb328);
}
