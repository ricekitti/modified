// Inv Manager — Organises your inventory for you.
// category: utility   descriptor: 0x2f4c40   settings block: 0x2f4e38
// functions: gui inv_manager_gui, register inv_manager_register
// implementation: src/logic/utility/inv_manager.cpp
#include "slinky/api.h"

S(155df9); // "Main"
S(155dfe); // "Equip armor"
S(155e0a); // "Sort hotbar"
S(155e16); // "delay_after_open"
S(155e27); // "speed"
S(155e2d); // "smart_speed"
S(155e39); // "randomize"
S(155e43); // "equip_armor"
S(155e4f); // "sort_hotbar"
S(155e5b); // "sort_hotbar_fallback"

D(2f4e50); // setting num "delay_after_open"
D(2f4e88); // setting bool "equip_armor"
D(2f4ea8); // setting bool "sort_hotbar"
D(2f4ec0); // current value of setting "sort_hotbar" (+0x18)
D(2f4fb0); // setting enum (slot)
D(2f4ff0); // setting enum (slot)
D(2f5030); // setting enum (slot)
D(2f5070); // setting enum (slot)
D(2f50b0); // setting enum (slot)
D(2f50f0); // setting enum (slot)
D(2f5130); // setting enum (slot)
D(2f5170); // setting enum (slot)
D(2f51b0); // setting enum (slot)
D(2f51f0); // setting bool "sort_hotbar_fallback"
D(2f5210); // setting bool "randomize"
D(2f5230); // setting num "speed"
D(2f5268); // setting bool "smart_speed"

extern "C" void inv_manager_gui(void *a, void *b) {
    W_label(a, b, rdata_155df9);
    W_slider(b, a, data_2f4e50);
    W_slider(b, a, data_2f5230);
    W_bool(b, a, data_2f5268);
    W_bool(b, a, data_2f5210);
    W_label(a, b, rdata_155dfe);
    W_bool(b, a, data_2f4e88);
    W_label(a, b, rdata_155e0a);
    W_bool(b, a, data_2f4ea8);
    if (*(unsigned char *)data_2f4ec0 == 1) {
        W_bool(b, a, data_2f51f0);
        W_enumw(b, a, data_2f4fb0);
        W_enumw(b, a, data_2f4ff0);
        W_enumw(b, a, data_2f5030);
        W_enumw(b, a, data_2f5070);
        W_enumw(b, a, data_2f50b0);
        W_enumw(b, a, data_2f50f0);
        W_enumw(b, a, data_2f5130);
        W_enumw(b, a, data_2f5170);
        W_enumw(b, a, data_2f51b0);
    } else {

    }
}

extern "C" void inv_manager_register(void *m) {
    char slot[2];
    R_num(m, rdata_155e16, data_2f4e50);
    R_num(m, rdata_155e27, data_2f5230);
    R_bool(m, rdata_155e2d, data_2f5268);
    R_bool(m, rdata_155e39, data_2f5210);
    R_bool(m, rdata_155e43, data_2f4e88);
    R_bool(m, rdata_155e4f, data_2f4ea8);
    R_bool(m, rdata_155e5b, data_2f51f0);
    *(__int16 *)slot = 0x31;
    R_enum(m, slot, data_2f4fb0);
    *(__int16 *)slot = 0x32;
    R_enum(m, slot, data_2f4ff0);
    *(__int16 *)slot = 0x33;
    R_enum(m, slot, data_2f5030);
    *(__int16 *)slot = 0x34;
    R_enum(m, slot, data_2f5070);
    *(__int16 *)slot = 0x35;
    R_enum(m, slot, data_2f50b0);
    *(__int16 *)slot = 0x36;
    R_enum(m, slot, data_2f50f0);
    *(__int16 *)slot = 0x37;
    R_enum(m, slot, data_2f5130);
    *(__int16 *)slot = 0x38;
    R_enum(m, slot, data_2f5170);
    *(__int16 *)slot = 0x39;
    R_enum(m, slot, data_2f51b0);
}
