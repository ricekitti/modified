// Sprint Reset — Resets your sprint state to deal more knockback.
// category: combat   descriptor: 0x2efd00   settings block: 0x2efef8
// functions: gui sprint_reset_gui, register sprint_reset_register, status sprint_reset_status
// implementation: src/logic/combat/sprint_reset.cpp
#include "slinky/api.h"

S(153122); // "Main"
S(153127); // "Conditions"
S(153132); // "mode"
S(153137); // "delay"
S(15313d); // "duration"
S(153146); // "randomize"
S(153150); // "wait_for_damage"
S(153160); // "require_weapon"
S(15316f); // "%s"

D(2eff10);                       // enum option labels (const char *[])
D(2eff28);                       // setting enum "mode"
extern "C" unsigned data_2eff58; // current value of setting "mode" (+0x30)
D(2eff68);                       // setting num "delay"
D(2effa0);                       // setting bool "randomize"
D(2effc0);                       // setting bool "wait_for_damage"
D(2effe0);                       // setting bool "require_weapon"
D(2f0000);                       // setting num "duration"

extern "C" void sprint_reset_gui(void *a, void *b) {
    W_label(a, b, rdata_153122);
    W_enumw(b, a, data_2eff28);
    W_slider(b, a, data_2eff68);
    W_slider(b, a, data_2f0000);
    W_bool(b, a, data_2effa0);
    W_label(a, b, rdata_153127);
    W_bool(b, a, data_2effc0);
    W_bool(b, a, data_2effe0);
}

extern "C" void sprint_reset_register(void *m) {
    R_enum(m, rdata_153132, data_2eff28);
    R_num(m, rdata_153137, data_2eff68);
    R_num(m, rdata_15313d, data_2f0000);
    R_bool(m, rdata_153146, data_2effa0);
    R_bool(m, rdata_153150, data_2effc0);
    R_bool(m, rdata_153160, data_2effe0);
}

extern "C" void sprint_reset_status(void *self) {
    log_fmt(self, 0x20, rdata_15316f, ((const char **)data_2eff10)[data_2eff58]);

}
