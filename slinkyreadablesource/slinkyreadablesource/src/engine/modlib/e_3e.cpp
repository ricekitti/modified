// 0x3e000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
extern "C" int data_2f0dc0, data_301e90, data_301e94; extern "C" unsigned char data_2f1438;
struct Ev { char pad[8]; Ctx *ctx; void *p10; unsigned char on; int f24; };
struct Ev2 { char pad[8]; void *a; void *p; char pad2[0xc]; int f24; };
FN(int, java_get_minecraft_left_click_counter, (void *a, void *x));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void, java_set_minecraft_left_click_counter, (void *a, void *x, int c));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *b));
FN(void, java_jni_call_nonvirtual_void_method_entity_living_base_swing_item, (void *a, void *b));
FN(void *, java_jni_is_instance_of_s00_packet_keep_alive, (void *a, void *b));
FN(void *, java_jni_is_instance_of_c00_packet_keep_alive, (void *a, void *b));
FN(void *, get_net_handler, (void *a, void *b));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(unsigned char, is_key_down, (void *a, void *x));
FN(void *, java_get_entity_player_2, (void *a, void *b));
FN(int, java_get_inventory_player_current_item, (void *a, void *x));
FN(int, java_get_key_binding_key_code, (void *a, void *x));
FN(void, java_set_key_binding_pressed, (void *a, void *x, int v));
FN(int, java_get_entity_player_item_in_use_count, (void *a, void *b));
extern "C" int compare_hurt_time(char *a, char *b) { return (int)(*(float *)(b + 0x30) - *(float *)(a + 0x30)); }
extern "C" void ping_fix_toggle(Ev *ev) {
    if (ev->on != 1) return;
    Ctx *c = ev->ctx;
    if (**(int **)c == 0) return;
    if (java_get_minecraft_left_click_counter(c->a, c->c18) <= 0) return;
    if (data_2f0dc0 == 1) {
        void *t = java_get_minecraft_5(c->a, c->c18);
        if (t && java_get_moving_object_position_3(c->a, t) != 0) goto sw;
        if (!java_call_entity_player_is_using_item(c->a, c->b)) java_jni_call_nonvirtual_void_method_entity_living_base_swing_item(c->a, c->b);
        return;
    }
sw:
    java_set_minecraft_left_click_counter(c->a, c->c18, 0);
}
extern "C" void ping_fix_packet_in(Ev2 *e) { if (java_jni_is_instance_of_s00_packet_keep_alive(e->a, e->p)) e->f24 = 3; }
extern "C" void ping_fix_packet_out(Ev2 *e) { if (java_jni_is_instance_of_c00_packet_keep_alive(e->a, e->p)) e->f24 = 3; }
extern "C" void ping_fix_tick(Ev *ev) {
    Ctx *c = ev->ctx;
    if (c->f40 == 0) { data_301e90 = 0; return; }
    void *item = java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18));
    unsigned char b = is_key_down(c->a, item);
    int slot = java_get_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b));
    if (slot != data_301e94) {
        data_301e90 = 0;
        data_301e94 = slot;
        void *w = c->a;
        java_set_key_binding_pressed(w, item, java_get_key_binding_key_code(w, item));
    }
    if (!b) return;
    int r = java_get_entity_player_item_in_use_count(c->a, c->b);
    if (data_301e90 <= 0) { data_301e90 = r; return; }
    if (--data_301e90 != 0 && !((r == 0) & data_2f1438)) return;
    java_set_key_binding_pressed(c->a, item, 0);
}
