// GUI — Graphical interface for the cheat.
// category: visual   descriptor: 0x2eb740   settings block: 0x2eb938
// functions: gui gui_gui, register gui_register
// implementation: src/logic/visual/gui.cpp
#include "slinky/api.h"

S(151630); // "Main"
S(151635); // "Layout"
S(15163c); // "scale"
S(151642); // "accent_color"
S(15164f); // "allow_input"
S(15165b); // "compact"
S(151663); // "wide"

D(2eb720); // setting key "accent_color"
D(2eb970); // setting enum "scale"
D(2eb9b0); // setting bool "allow_input"
D(2eb9d0); // setting bool "compact"
D(2eb9f0); // setting bool "wide"

extern "C" void gui_gui(void *a, void *b) {
    W_label(a, b, rdata_151630);
    W_enumw(b, a, data_2eb970);
    W_color(b, a, data_2eb720);
    W_bool(b, a, data_2eb9b0);
    W_label(a, b, rdata_151635);
    W_bool(b, a, data_2eb9d0);
    W_bool(b, a, data_2eb9f0);
}

extern "C" void gui_register(void *m) {
    R_enum(m, rdata_15163c, data_2eb970);
    R_key(m, rdata_151642, data_2eb720);
    R_bool(m, rdata_15164f, data_2eb9b0);
    R_bool(m, rdata_15165b, data_2eb9d0);
    R_bool(m, rdata_151663, data_2eb9f0);
}
