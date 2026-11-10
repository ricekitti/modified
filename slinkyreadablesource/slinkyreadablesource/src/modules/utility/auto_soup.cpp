// Auto Soup — Uses soups for you.
// category: utility   descriptor: 0x2e96b0   settings block: 0x2e98a8
// functions: gui auto_soup_gui, register auto_soup_register
// implementation: src/logic/utility/auto_soup.cpp
#include "slinky/api.h"

S(1506ab); // "Maximum health"
S(1506ba); // "Delays"
S(1506c1); // "Extra"
S(1506d9); // "single_soup_max_hp"
S(1506ec); // "double_soup_max_hp"
S(1506ff); // "randomization"
S(15070d); // "delay_before"
S(15071a); // "delay_between"
S(150728); // "delay_after"
S(150734); // "drop_bowls"
S(15073f); // "override_item"
S(15074d); // "allowed_items"

D(2e98c0);                            // setting num "single_soup_max_hp"
extern "C" float data_2e98f0;         // current value of setting "single_soup_max_hp" (+0x30)
D(2e98f8);                            // setting num "delay_before"
D(2e9930);                            // setting num "double_soup_max_hp"
extern "C" float data_2e9960;         // current value of setting "double_soup_max_hp" (+0x30)
D(2e9968);                            // setting num "delay_between"
D(2e99a0);                            // setting num "delay_after"
D(2e99d8);                            // setting num "randomization"
D(2e9a10);                            // setting bool "override_item"
extern "C" unsigned char data_2e9a28; // current value of setting "override_item" (+0x18)
D(2e9a30);                            // setting bool "drop_bowls"
D(2e9a50);                            // setting kind2 "allowed_items"

extern "C" void auto_soup_gui(void *a, void *b) {
    W_label(a, b, rdata_1506ab);
    W_slider(b, a, data_2e98c0);
    float lo = data_2e98f0;
    if (data_2e9960 > lo)
        data_2e9960 = lo;
    W_slider(b, a, data_2e9930);
    W_label(a, b, rdata_1506ba);
    W_slider(b, a, data_2e99d8);
    W_slider(b, a, data_2e98f8);
    W_slider(b, a, data_2e9968);
    W_slider(b, a, data_2e99a0);
    W_label(a, b, rdata_1506c1);
    W_bool(b, a, data_2e9a30);
    W_bool(b, a, data_2e9a10);
    if (data_2e9a28 == 1)
        W_items(b, a, data_2e9a50);

}

extern "C" void auto_soup_register(void *m) {
    R_num(m, rdata_1506d9, data_2e98c0);
    R_num(m, rdata_1506ec, data_2e9930);
    R_num(m, rdata_1506ff, data_2e99d8);
    R_num(m, rdata_15070d, data_2e98f8);
    R_num(m, rdata_15071a, data_2e9968);
    R_num(m, rdata_150728, data_2e99a0);
    R_bool(m, rdata_150734, data_2e9a30);
    R_bool(m, rdata_15073f, data_2e9a10);
    R_kind2(m, rdata_15074d, data_2e9a50);
}
