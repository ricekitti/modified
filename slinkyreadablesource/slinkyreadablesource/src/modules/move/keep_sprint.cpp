// Keep Sprint — Keeps your sprint speed when attacking players.
// category: move   descriptor: 0x2ef700   settings block: 0x2ef8f8
// functions: gui keep_sprint_gui, register keep_sprint_register, status keep_sprint_status
// implementation: src/logic/move/keep_sprint.cpp
#include "slinky/api.h"

extern "C" float rdata_152e1c; // 100
S(152e63);                     // "%.0f%%"
S(152e92);                     // "Main"
S(152e97);                     // "Conditions"
S(152ea2);                     // "chance"
S(152ea9);                     // "motion_mult"
S(152eb5);                     // "allow_reduce"
S(152ec2);                     // "require_on_ground"

D(2ef910);                    // setting num "chance"
D(2ef948);                    // setting num "motion_mult"
extern "C" float data_2ef978; // current value of setting "motion_mult" (+0x30)
D(2ef980);                    // setting bool "require_on_ground"
D(2ef9a0);                    // setting bool "allow_reduce"

extern "C" void keep_sprint_gui(void *a, void *b) {
    W_label(a, b, rdata_152e92);
    W_slider(b, a, data_2ef910);
    W_slider(b, a, data_2ef948);
    W_bool(b, a, data_2ef9a0);
    W_label(a, b, rdata_152e97);
    W_bool(b, a, data_2ef980);
}

extern "C" void keep_sprint_register(void *m) {
    R_num(m, rdata_152ea2, data_2ef910);
    R_num(m, rdata_152ea9, data_2ef948);
    R_bool(m, rdata_152eb5, data_2ef9a0);
    R_bool(m, rdata_152ec2, data_2ef980);
}

extern "C" void keep_sprint_status(void *self) {
    log_fmt(self, 0x20, rdata_152e63, (double)(data_2ef978 * rdata_152e1c));

}
