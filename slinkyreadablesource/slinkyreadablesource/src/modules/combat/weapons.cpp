// Weapons — Configure which items are considered weapons.
// category: combat   descriptor: 0x2ee930   settings block: 0x2eeb28
// functions: gui weapons_gui, register weapons_register
// implementation: src/logic/combat/weapons.cpp
#include "slinky/api.h"

S(1529a0); // "Item types"
S(1529ab); // "Any item with"
S(1529b9); // "Any item in"
S(1529d7); // "fist"
S(1529dc); // "swords"
S(1529e3); // "axes"
S(1529e8); // "extra_items"
S(1529f4); // "enchant_sharpness"
S(152a06); // "enchant_knockback"
S(152a18); // "enchant_fire_aspect"

D(2eeb40); // setting bool "fist"
D(2eeb60); // setting bool (slot)
D(2eeb80); // setting bool (slot)
D(2eeba0); // setting bool (slot)
D(2eebc0); // setting bool (slot)
D(2eebe0); // setting bool (slot)
D(2eec00); // setting bool (slot)
D(2eec20); // setting bool (slot)
D(2eec40); // setting bool (slot)
D(2eec60); // setting bool (slot)
D(2eec80); // setting bool "enchant_sharpness"
D(2eeca0); // setting bool "enchant_knockback"
D(2eecc0); // setting bool "enchant_fire_aspect"
D(2eece0); // setting bool "axes"
D(2eed00); // setting bool "swords"
D(2eed20); // setting kind2 "extra_items"

extern "C" void weapons_gui(void *a, void *b) {
    W_label(a, b, rdata_1529a0);
    W_bool(b, a, data_2eeb40);
    W_bool(b, a, data_2eed00);
    W_bool(b, a, data_2eece0);
    W_items(b, a, data_2eed20);
    W_label(a, b, rdata_1529ab);
    W_bool(b, a, data_2eec80);
    W_bool(b, a, data_2eeca0);
    W_bool(b, a, data_2eecc0);
    W_label(a, b, rdata_1529b9);
    W_bool(b, a, data_2eeb60);
    W_bool(b, a, data_2eeb80);
    W_bool(b, a, data_2eeba0);
    W_bool(b, a, data_2eebc0);
    W_bool(b, a, data_2eebe0);
    W_bool(b, a, data_2eec00);
    W_bool(b, a, data_2eec20);
    W_bool(b, a, data_2eec40);
    W_bool(b, a, data_2eec60);
}

extern "C" void weapons_register(void *m) {
    char slot[2];
    R_bool(m, rdata_1529d7, data_2eeb40);
    R_bool(m, rdata_1529dc, data_2eed00);
    R_bool(m, rdata_1529e3, data_2eece0);
    R_kind2(m, rdata_1529e8, data_2eed20);
    R_bool(m, rdata_1529f4, data_2eec80);
    R_bool(m, rdata_152a06, data_2eeca0);
    R_bool(m, rdata_152a18, data_2eecc0);
    *(__int16 *)slot = 0x31;
    R_bool(m, slot, data_2eeb60);
    *(__int16 *)slot = 0x32;
    R_bool(m, slot, data_2eeb80);
    *(__int16 *)slot = 0x33;
    R_bool(m, slot, data_2eeba0);
    *(__int16 *)slot = 0x34;
    R_bool(m, slot, data_2eebc0);
    *(__int16 *)slot = 0x35;
    R_bool(m, slot, data_2eebe0);
    *(__int16 *)slot = 0x36;
    R_bool(m, slot, data_2eec00);
    *(__int16 *)slot = 0x37;
    R_bool(m, slot, data_2eec20);
    *(__int16 *)slot = 0x38;
    R_bool(m, slot, data_2eec40);
    *(__int16 *)slot = 0x39;
    R_bool(m, slot, data_2eec60);
}
