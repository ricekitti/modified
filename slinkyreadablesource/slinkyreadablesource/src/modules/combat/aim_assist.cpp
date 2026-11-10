// Aim Assist — Pulls your crosshair towards other players.
// category: combat   descriptor: 0x2e8cc0   settings block: 0x2e8eb8
// functions: gui aim_assist_gui, register aim_assist_register, status aim_assist_status
// implementation: src/logic/combat/aim_assist.cpp
#include "slinky/api.h"

S(1501b4); // "Aim"
S(1501b8); // "Multi-point"
S(1501c4); // "Target"
S(1501cb); // "Conditions"
S(1501d6); // "mode"
S(1501db); // "hor_speed"
S(1501e5); // "ver_speed"
S(1501ef); // "lock_on_vertical"
S(150200); // "silent_move_correct"
S(150214); // "silent_ignore_manual_aim"
S(15022d); // "predict"
S(150235); // "aim_randomization"
S(150247); // "hor_hitbox_correct"
S(15025a); // "ver_hitbox_correct"
S(15026d); // "mode_target"
S(150279); // "sort_by"
S(150281); // "allow_exceed_fov"
S(150292); // "range"
S(150298); // "fov"
S(15029c); // "require_sprint"
S(1502ab); // "require_mouse_pressed"
S(1502c1); // "require_mouse_moved"
S(1502d5); // "disable_on_block_break"
S(1502ec); // "require_weapon"
S(1502fb); // "ignore_invis"
S(150308); // "require_line_of_sight"
S(15031e); // "%s"

D(2e8ed0);                       // setting rng "fov"
D(2e8f20);                       // setting num "hor_speed"
D(2e8f58);                       // setting num "ver_speed"
D(2e8f90);                       // setting bool "lock_on_vertical"
D(2e8fb0);                       // setting bool "require_mouse_moved"
D(2e8fd0);                       // enum option labels (const char *[])
D(2e8ff0);                       // setting enum "mode"
extern "C" unsigned data_2e9020; // current value of setting "mode" (+0x30)
D(2e9030);                       // setting bool "silent_ignore_manual_aim"
D(2e9050);                       // setting bool "silent_move_correct"
D(2e9070);                       // setting rng "range"
D(2e90c0);                       // setting bool "require_sprint"
D(2e90e0);                       // setting bool "require_mouse_pressed"
D(2e9100);                       // setting bool "disable_on_block_break"
D(2e9120);                       // setting bool "require_weapon"
D(2e9150);                       // setting enum "mode_target"
extern "C" int data_2e9180;      // current value of setting "mode_target" (+0x30)
D(2e9190);                       // setting num "predict"
D(2e91c8);                       // setting num "hor_hitbox_correct"
D(2e9200);                       // setting num "ver_hitbox_correct"
D(2e9238);                       // setting num "aim_randomization"
D(2e9270);                       // setting bool "ignore_invis"
D(2e9290);                       // setting bool "allow_exceed_fov"
D(2e92b0);                       // setting bool "require_line_of_sight"
D(2e92f0);                       // setting enum "sort_by"

extern "C" void aim_assist_gui(void *a, void *b) {
    W_label(a, b, rdata_1501b4);
    W_enumw(b, a, data_2e8ff0);
    if (data_2e9020 == 2) {
        W_bool(b, a, data_2e8f90);
    } else if (data_2e9020 == 3) {
        W_bool(b, a, data_2e9050);
        W_bool(b, a, data_2e9030);
        W_slider(b, a, data_2e8f20);
        W_slider(b, a, data_2e8f58);
    } else {
        W_slider(b, a, data_2e8f20);
        W_slider(b, a, data_2e8f58);
    }
    W_slider(b, a, data_2e9190);
    W_slider(b, a, data_2e9238);
    W_label(a, b, rdata_1501b8);
    W_slider(b, a, data_2e91c8);
    W_slider(b, a, data_2e9200);
    W_label(a, b, rdata_1501c4);
    W_enumw(b, a, data_2e9150);
    if (data_2e9180 == 0) {
        W_bool(b, a, data_2e9290);
    } else if (data_2e9180 == 1) {
        W_enumw(b, a, data_2e92f0);
    }
    W_range(b, a, data_2e9070);
    W_range(b, a, data_2e8ed0);
    W_label(a, b, rdata_1501cb);
    if (data_2e9020 != 3)
        W_bool(b, a, data_2e90c0);
    W_bool(b, a, data_2e90e0);
    W_bool(b, a, data_2e8fb0);
    W_bool(b, a, data_2e9100);
    W_bool(b, a, data_2e9120);
    W_bool(b, a, data_2e9270);
    W_bool(b, a, data_2e92b0);
}

extern "C" void aim_assist_register(void *m) {
    R_enum(m, rdata_1501d6, data_2e8ff0);
    R_num(m, rdata_1501db, data_2e8f20);
    R_num(m, rdata_1501e5, data_2e8f58);
    R_bool(m, rdata_1501ef, data_2e8f90);
    R_bool(m, rdata_150200, data_2e9050);
    R_bool(m, rdata_150214, data_2e9030);
    R_num(m, rdata_15022d, data_2e9190);
    R_num(m, rdata_150235, data_2e9238);
    R_num(m, rdata_150247, data_2e91c8);
    R_num(m, rdata_15025a, data_2e9200);
    R_enum(m, rdata_15026d, data_2e9150);
    R_enum(m, rdata_150279, data_2e92f0);
    R_bool(m, rdata_150281, data_2e9290);
    R_rng(m, rdata_150292, data_2e9070);
    R_rng(m, rdata_150298, data_2e8ed0);
    R_bool(m, rdata_15029c, data_2e90c0);
    R_bool(m, rdata_1502ab, data_2e90e0);
    R_bool(m, rdata_1502c1, data_2e8fb0);
    R_bool(m, rdata_1502d5, data_2e9100);
    R_bool(m, rdata_1502ec, data_2e9120);
    R_bool(m, rdata_1502fb, data_2e9270);
    R_bool(m, rdata_150308, data_2e92b0);
}

extern "C" void aim_assist_status(void *self) {
    log_fmt(self, 0x20, rdata_15031e, ((const char **)data_2e8fd0)[data_2e9020]);

}
