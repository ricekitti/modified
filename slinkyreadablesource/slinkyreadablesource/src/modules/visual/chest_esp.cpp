// Chest ESP — Highlights chests.
// category: visual   descriptor: 0x2ec908   settings block: 0x2ecb00
// functions: gui chest_esp_gui, register chest_esp_register
// implementation: src/logic/visual/chest_esp.cpp
#include "slinky/api.h"

S(151b03); // "Main"
S(151b08); // "Target"
S(151b0f); // "Customization"
S(151b1d); // "range"
S(151b23); // "render_chest"
S(151b30); // "render_ender_chest"
S(151b43); // "render_trapped_chest"
S(151b58); // "render_beacon"
S(151b66); // "line_width"
S(151b71); // "color_chest"
S(151b7d); // "color_ender_chest"
S(151b8f); // "color_trapped_chest"
S(151ba3); // "color_beacon"
S(151bb0); // "filled"

D(2ecb18);                            // setting num "range"
D(2ecb50);                            // setting bool "render_chest"
extern "C" unsigned char data_2ecb68; // current value of setting "render_chest" (+0x18)
D(2ecb70);                            // setting bool "render_ender_chest"
extern "C" unsigned char data_2ecb88; // current value of setting "render_ender_chest" (+0x18)
D(2ecb90);                            // setting bool "render_trapped_chest"
extern "C" unsigned char data_2ecba8; // current value of setting "render_trapped_chest" (+0x18)
D(2ecbb0);                            // setting bool "render_beacon"
extern "C" unsigned char data_2ecbc8; // current value of setting "render_beacon" (+0x18)
D(2ecbd0);                            // setting num "line_width"
D(2ecc08);                            // setting key "color_ender_chest"
D(2ecc28);                            // setting key "color_trapped_chest"
D(2ecc48);                            // setting key "color_beacon"
D(2ecc68);                            // setting key "color_chest"
D(2ecc88);                            // setting bool "filled"

extern "C" void chest_esp_gui(void *a, void *b) {
    W_label(a, b, rdata_151b03);
    W_slider(b, a, data_2ecb18);
    W_label(a, b, rdata_151b08);
    W_bool(b, a, data_2ecb50);
    W_bool(b, a, data_2ecb70);
    W_bool(b, a, data_2ecb90);
    W_bool(b, a, data_2ecbb0);
    W_label(a, b, rdata_151b0f);
    W_slider(b, a, data_2ecbd0);
    if (data_2ecb68 == 1) goto L1;
    if (data_2ecb88 == 1) goto L2;
S2:
    if (data_2ecba8 == 1) goto L3;
S3:
    if (data_2ecbc8 == 1) goto L4;
    goto Lend;
L1:
    W_color(b, a, data_2ecc68);
    if (data_2ecb88 != 1) goto S2;
L2:
    W_color(b, a, data_2ecc08);
    if (data_2ecba8 != 1) goto S3;
L3:
    W_color(b, a, data_2ecc28);
    if (data_2ecbc8 == 1) goto L4;
    goto Lend;
L4:
    W_color(b, a, data_2ecc48);
Lend:
    W_bool(b, a, data_2ecc88);
}

extern "C" void chest_esp_register(void *m) {
    R_num(m, rdata_151b1d, data_2ecb18);
    R_bool(m, rdata_151b23, data_2ecb50);
    R_bool(m, rdata_151b30, data_2ecb70);
    R_bool(m, rdata_151b43, data_2ecb90);
    R_bool(m, rdata_151b58, data_2ecbb0);
    R_num(m, rdata_151b66, data_2ecbd0);
    R_key(m, rdata_151b71, data_2ecc68);
    R_key(m, rdata_151b7d, data_2ecc08);
    R_key(m, rdata_151b8f, data_2ecc28);
    R_key(m, rdata_151ba3, data_2ecc48);
    R_bool(m, rdata_151bb0, data_2ecc88);
}
