// 0x42000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
FN(void *, java_get_minecraft_4, (void *a, void *x));
FN(unsigned char, java_get_player_controller_mp_is_hitting_block, (void *a, void *item));
FN(void, java_set_player_controller_mp_is_hitting_block, (void *a, void *item, int on));
extern "C" unsigned char data_3043d4, data_2f3174, data_3043dc;
extern "C" int data_3043d8;
struct Ev { char pad[8]; Ctx *ctx; unsigned char on; };
extern "C" void item_use_fix_on_attack(Ev *ev) {
    if (__builtin_expect(ev->on != 0, 0)) return;
    Ctx *c = ev->ctx;
    void *s = java_get_minecraft_4(c->a, c->c18);
    unsigned char r = java_get_player_controller_mp_is_hitting_block(c->a, s);
    data_3043d4 = r != 0;
    if (r) java_set_player_controller_mp_is_hitting_block(c->a, s, 0);
}
extern "C" void item_use_fix_on_use(Ev *ev) {
    if (data_3043d4 != 1 || ev->on != 0) return;
    Ctx *c = ev->ctx;
    void *s = java_get_minecraft_4(c->a, c->c18);
    void *a = c->a;

    java_set_player_controller_mp_is_hitting_block(a, s, data_3043d4);
}
extern "C" bool pending_action_flag() {
    unsigned char r;
    if (data_2f3174 == 1) {  r = (data_3043d8 > 0) | data_3043dc; }
    else r = 0;

    return r & 1;
}

FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *b));
FN(void *, item_held_hotbar_slot, (void *a, void *b));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *x));
extern "C" float data_2f2f08, data_2f2ed0, data_2f2f40;
extern "C" double rdata_154800;
struct Ev2 { char pad[8]; Ctx *ctx; float x, y; };
extern "C" void item_speed_by_type(Ev2 *ev) {
    Ctx *c = ev->ctx;
    if (!java_call_entity_player_is_using_item(c->a, c->b)) return;
    void *item = item_held_hotbar_slot(c->a, c->b);
    if (!item) return;
    float *f;
    switch (java_call_static_item_get_id_from_item(c->a, item)) {
    case 0x10b: case 0x10c: case 0x110: case 0x114: case 0x11b: f = &data_2f2f08; break;   // swords
    case 0x105: f = &data_2f2ed0; break;
    default: f = &data_2f2f40; break;
    }
    double s = (double)*f / rdata_154800;
    ev->x = (float)(s * ev->x);
    ev->y = (float)(s * ev->y);
}
