// GUI - ClickGUI helper callbacks (scale table, input filter, open/close, layout).
#include "slinky/engine.h"

struct V4 { float x, y, z, w; };
struct InputEvt { int type; int code; int pad8; unsigned char down; };   // +0x00 / +0x04 / +0x0c
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };  // +0x10 / +0x18
struct Notice { int kind; int pad4; int value; char rest[0x3c]; };

extern "C" unsigned data_2eb9a0;         // "scale"
extern "C" const float rdata_1514f0[];   // scale table
extern "C" unsigned char data_2eb754;    // gui open
extern "C" char data_2eb740[];           // GUI module descriptor
extern "C" int data_2eb75c;              // toggle key
extern "C" unsigned char data_2eb9c8;    // "allow_input"
extern "C" unsigned char data_2eb9e8;    // "compact"
extern "C" unsigned char data_2eba08;    // "wide"
extern "C" char data_2f9e30[];           // gui state
extern "C" V4 data_2fa190;
extern "C" unsigned char data_2fa2fc;
extern "C" unsigned char data_2fa2fd;
extern "C" unsigned char data_2fa300;    // shutting down
extern "C" float data_2eb728;
extern "C" float data_2eb72c;
extern "C" float data_2eb730;

FN(unsigned char, handle_gui_input, (void *state, void *evt));
FN(void, queue_module_toggle, (void *a, void *mod, int x));
FN(unsigned char, java_call_static_mouse_is_grabbed, (void *a));
FN(void, java_call_static_mouse_set_grabbed, (void *a, int x));
FN(int, java_get_static_display_width, (void *a));
FN(int, java_get_static_display_height, (void *a));
FN(void, java_call_static_mouse_set_cursor_position, (void *a, int x, int y));
FN(double, time_now, ());                 // high-resolution seconds
FN(void, set_tab_animation, (void *state, double v));   // stores a double at state + 0x498
FN(unsigned char, is_in_game, (void *a, void *x));
FN(void, close_popup_now, (void *state));
FN(void, reset_drag_state, (void *state));
FN(int, save_settings, (void *x, void *y));
FN(void, dispatch_event, (void *a, Notice *n));
FN(V4, nvg_rgb_f, (float a, float b, float c));
FN(void, draw_gui_frame, (void *state, void *ev, void *x, void *y));

extern "C" float gui_part_3() {
    return rdata_1514f0[data_2eb9a0];
}

extern "C" unsigned char gui_part_2_2(InputEvt *e, unsigned char shift, void *a) {
    if (data_2eb754 == 1 && handle_gui_input(data_2f9e30, e))
        return 1;
    int type = e->type;
    if (type == 2 && e->down == 1 && e->code != 0) {
        unsigned char open = data_2eb754;
        int code = e->code;
        if (!shift && code == data_2eb75c) {
            queue_module_toggle(a, data_2eb740, open ^ 1);
            return 1;
        }
        if (code == 0x57 && open) {
            queue_module_toggle(a, data_2eb740, 0);
            return 1;
        }
    }
    if (data_2eb754 != 1)
        return 0;
    if (type != 2)
        return 1;
    if (!data_2eb9c8)
        return 1;
    if ((unsigned)e->code <= 5)
        return 1;
    return 0;
}

extern "C" void gui_part_3_2(Hook *h) {
    Ctx *c = h->ctx;
    if (java_call_static_mouse_is_grabbed(c->a)) {
        java_call_static_mouse_set_grabbed(c->a, 0);
        return;
    }

}

extern "C" void gui_part_4(EvHook *h) {
    if (h->ev != data_2eb740)
        return;
    Ctx *c = h->ctx;
    void *a0 = c->a;
    if (h->on == 1) {
        if (java_call_static_mouse_is_grabbed(a0)) {
            int w = java_get_static_display_width(c->a);
            int hgt = java_get_static_display_height(c->a);
            java_call_static_mouse_set_cursor_position(c->a, w / 2, hgt / 2);
            java_call_static_mouse_set_grabbed(c->a, 0);
        }
        set_tab_animation(data_2f9e30, time_now());
        return;
    }
    if (is_in_game(a0, c->c18))
        java_call_static_mouse_set_grabbed(c->a, 1);
    close_popup_now(data_2f9e30);
    reset_drag_state(data_2f9e30);
    if (data_2fa300)
        return;
    int code = save_settings(c->p08, c->p10);
    if (!code)
        return;
    Notice n = {};
    n.kind = 0xc;
    n.value = code;
    dispatch_event(((Ctx *)c)->p08, &n);
}

extern "C" void gui_part_5(unsigned char *h) {
    h[0x10] |= 3;
}

extern "C" void gui_part_6(EvHook *h) {
    Ctx *c = h->ctx;
    float x = data_2eb728, y = data_2eb72c, z = data_2eb730;
    data_2fa190 = nvg_rgb_f(x, y, z);
    data_2fa2fc = data_2eb9e8;
    data_2fa2fd = data_2eba08;
    void *ev = h->ev;
    void *p8 = c->p08;
    void *p10 = c->p10;
    draw_gui_frame(data_2f9e30, ev, p8, p10);
}

extern "C" void gui_part_7(Hook *h) {
    data_2fa300 = 1;
}
