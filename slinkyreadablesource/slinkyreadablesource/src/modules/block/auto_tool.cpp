// Auto Tool — Selects the best tool when digging.
// category: block   descriptor: 0x2ee158   settings block: 0x2ee350
// functions: gui auto_tool_gui, register auto_tool_register
// implementation: src/logic/block/auto_tool.cpp
#include "slinky/api.h"

S(152658); // "Main"
S(15265d); // "Allow while using"
S(15266f); // "Conditions"
S(152695); // "activation_time"
S(1526a5); // "switch_back"
S(1526b1); // "allow_using_sword"
S(1526c3); // "allow_using_tools"
S(1526d5); // "allow_using_fists"
S(1526e7); // "allow_using_other"
S(1526f9); // "require_sneak"
S(152707); // "restrict_allowed_blocks"
S(15271f); // "allowed_blocks"

D(2ee370); // setting num "activation_time"
D(2ee3a8); // setting bool "allow_using_fists"
D(2ee3c8); // setting bool "allow_using_sword"
D(2ee3e8); // setting bool "allow_using_tools"
D(2ee408); // setting bool "allow_using_other"
D(2ee428); // setting bool "require_sneak"
D(2ee448); // setting bool "restrict_allowed_blocks"
D(2ee460); // current value of setting "restrict_allowed_blocks" (+0x18)
D(2ee468); // setting bool "switch_back"
D(2ee488); // setting kind2 "allowed_blocks"

extern "C" void auto_tool_gui(void *a, void *b) {
    W_label(a, b, rdata_152658);
    W_slider(b, a, data_2ee370);
    W_bool(b, a, data_2ee468);
    W_label(a, b, rdata_15265d);
    W_bool(b, a, data_2ee3c8);
    W_bool(b, a, data_2ee3e8);
    W_bool(b, a, data_2ee3a8);
    W_bool(b, a, data_2ee408);
    W_label(a, b, rdata_15266f);
    W_bool(b, a, data_2ee428);
    W_bool(b, a, data_2ee448);
    if (*(unsigned char *)data_2ee460 == 1) {
        W_items(b, a, data_2ee488);
    } else {

    }
}

extern "C" void auto_tool_register(void *m) {
    R_num(m, rdata_152695, data_2ee370);
    R_bool(m, rdata_1526a5, data_2ee468);
    R_bool(m, rdata_1526b1, data_2ee3c8);
    R_bool(m, rdata_1526c3, data_2ee3e8);
    R_bool(m, rdata_1526d5, data_2ee3a8);
    R_bool(m, rdata_1526e7, data_2ee408);
    R_bool(m, rdata_1526f9, data_2ee428);
    R_bool(m, rdata_152707, data_2ee448);
    R_kind2(m, rdata_15271f, data_2ee488);
}
