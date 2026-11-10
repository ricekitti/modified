// Velocity — Reduces the amount of knockback you take.
// category: combat   descriptor: 0x2ea7f0   settings block: 0x2ea9e8
// functions: gui velocity_gui, register velocity_register, status velocity_status
// implementation: src/logic/combat/velocity.cpp
#include "slinky/api.h"

extern "C" float rdata_150ea0; // 100
S(150f6c);                     // "%.0f%%"
S(150fed);                     // "Main"
S(150ff2);                     // "Conditions"
S(150ffd);                     // "mode"
S(151002);                     // "reduce_chance"
S(151010);                     // "fov"
S(151014);                     // "reduce_amount"
S(151022);                     // "reduce_delay"
S(15102f);                     // "jump_chance"
S(15103b);                     // "reduce_multiple_per_tick"
S(151054);                     // "ignore_vertical"
S(151064);                     // "ignore_chance"
S(151072);                     // "require_target"
S(151081);                     // "require_mouse"
S(15108f);                     // "require_forward"
S(15109f);                     // "require_weapon"
S(1510ae);                     // "require_sprinting_jump"
S(1510c5);                     // "%s"

D(2eaa00);                       // enum option labels (const char *[])
D(2eaa20);                       // setting enum "mode"
extern "C" unsigned data_2eaa50; // current value of setting "mode" (+0x30)
D(2eaa60);                       // setting num "ignore_chance"
D(2eaa98);                       // setting bool "require_forward"
D(2eaab8);                       // setting bool "require_mouse"
D(2eaad8);                       // setting bool "require_weapon"
D(2eaaf8);                       // setting bool "require_target"
D(2eab38);                       // setting enum "ignore_vertical"
D(2eab78);                       // setting bool "reduce_multiple_per_tick"
D(2eab98);                       // setting num "reduce_chance"
D(2eabd0);                       // setting num "reduce_delay"
D(2eac08);                       // setting num "reduce_amount"
extern "C" float data_2eac38;    // current value of setting "reduce_amount" (+0x30)
D(2eac40);                       // setting num "fov"
D(2eac78);                       // setting num "jump_chance"
D(2eacb0);                       // setting bool "require_sprinting_jump"

extern "C" void velocity_gui(void *a, void *b) {
    W_label(a, b, rdata_150fed);
    W_enumw(b, a, data_2eaa20);
    switch (data_2eaa50) {
    case 0:
        W_slider(b, a, data_2eab98);
        W_slider(b, a, data_2eac40);
        W_slider(b, a, data_2eac08);
        W_slider(b, a, data_2eabd0);
        break;
    case 1:
        W_slider(b, a, data_2eac78);
        break;
    case 2:
        W_bool(b, a, data_2eab78);
        break;
    case 3:
        W_enumw(b, a, data_2eab38);
        W_slider(b, a, data_2eaa60);
        break;
    }
    W_label(a, b, rdata_150ff2);
    if (data_2eaa50 != 2) {
        W_bool(b, a, data_2eaaf8);
        W_bool(b, a, data_2eaab8);
    }
    W_bool(b, a, data_2eaa98);
    W_bool(b, a, data_2eaad8);
    if (data_2eaa50 == 1)
        W_bool(b, a, data_2eacb0);

}

extern "C" void velocity_register(void *m) {
    R_enum(m, rdata_150ffd, data_2eaa20);
    R_num(m, rdata_151002, data_2eab98);
    R_num(m, rdata_151010, data_2eac40);
    R_num(m, rdata_151014, data_2eac08);
    R_num(m, rdata_151022, data_2eabd0);
    R_num(m, rdata_15102f, data_2eac78);
    R_bool(m, rdata_15103b, data_2eab78);
    R_enum(m, rdata_151054, data_2eab38);
    R_num(m, rdata_151064, data_2eaa60);
    R_bool(m, rdata_151072, data_2eaaf8);
    R_bool(m, rdata_151081, data_2eaab8);
    R_bool(m, rdata_15108f, data_2eaa98);
    R_bool(m, rdata_15109f, data_2eaad8);
    R_bool(m, rdata_1510ae, data_2eacb0);
}

extern "C" void velocity_status(void *self) {
    if (data_2eaa50)
        log_fmt(self, 0x20, rdata_1510c5, ((const char **)data_2eaa00)[data_2eaa50]);
    else
        log_fmt(self, 0x20, rdata_150f6c, (double)(data_2eac38 * rdata_150ea0));

}
