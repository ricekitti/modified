// Auto Soup - eat mushroom soup when health is low.
#include "slinky/engine.h"

struct InHook { char pad0[8]; void *mod; unsigned char use; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" char data_2e96b0[];           // Auto Soup module descriptor
extern "C" float data_2e9a08;            // delay scale
extern "C" unsigned char data_2f7d68;    // input consumed
extern "C" int data_2f7d6c;              // state
extern "C" unsigned char data_2f7d70;
extern "C" float data_2f7d80;           // step delay
extern "C" char data_2f7d88[];           // rng states
extern "C" char data_2f7d98[];
extern "C" float rdata_1505e0;
extern "C" const double rdata_1505e8;
extern "C" const double rdata_1505f0;
extern "C" const float rdata_1505f8;
extern "C" const float rdata_1505fc;
extern "C" const float rdata_150600;

extern "C" float data_2e98f0;            // "health"
extern "C" float data_2e9928, data_2e9960, data_2e9998, data_2e99d0;   // delays / refill health
extern "C" unsigned char data_2e9a28;    // custom item list
extern "C" unsigned char data_2e9a48;    // "drop_bowls"
extern "C" int data_2f7d74;              // soup slot
extern "C" int data_2f7d78;              // original slot
extern "C" unsigned char data_2f7d7c;    // eat another one
extern "C" unsigned char data_2f7d7d;    // item leaves a bowl
extern "C" char data_2f7da8[];           // rng state
extern "C" char data_2f7db8[];           // custom item set
extern "C" const float rdata_1505a8, rdata_1505ac, rdata_1505b0, rdata_1505c0;
extern "C" const double rdata_1505b8;
FN(void, queue_module_toggle, (void *a, void *mod, int x));
FN(float, java_call_entity_living_base_get_health, (void *a, void *p));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(int, java_get_inventory_player_current_item, (void *a, void *p));
FN(void *, java_get_inventory_player_main_inventory, (void *a, void *inv));
FN(void *, java_call_item_stack_get_item, (void *a, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(unsigned char, id_vector_contains, (void *set, int id));
FN(unsigned char, claim_release_hotbar_module, (void *timer));
FN(void, release_owner, (void *timer));
FN(void, java_set_inventory_player_current_item, (void *a, void *p, int slot));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_2, (void *a, void *x));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(void *, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, void *rng, double p));
FN(unsigned char, walk_should_change, (void *st, void *rng, double p));
FN(float, random_rotation, (void *rng));

extern "C" void auto_soup_input_hook(InHook *h) {
    if (h->mod != data_2e96b0)
        return;
    int st = data_2f7d6c;
    if (st == 0 || st == 6)
        return;
    h->use = 1;
    data_2f7d68 = 1;
}

extern "C" void auto_soup_enable(EvHook *h) {
    if (h->ev != data_2e96b0 || h->on != 1)
        return;
    data_2f7d6c = 0;
    data_2f7d70 = 0;
    data_2f7d80 = 0.0f;
    data_2f7d68 = 0;
}

extern "C" float auto_soup_asm_pins_keep(float base) {
    float k = rdata_1505e0;
    void *rng = rot_make_floats(0.0f, k);
    char *st = data_2f7d88;

    double p = rdata_1505e8;

    float a = walk_update(st, rng, p);
    rng = rot_make_floats(0.0f, k);
    float s = walk_update(st, rng, rdata_1505f0) + a;
    float lo = rdata_1505f8;

    rng = rot_make_floats(lo, rdata_1505fc);
    if (walk_should_change(data_2f7d98, rng, p)) {
        lo = rdata_1505e0;

        s += random_rotation(rot_make_floats(lo, rdata_150600));
    }
    return s * data_2e9a08 + base;
}

typedef void *(*GetElement)(void *env, void *array, int index);

// main tick: 0 check health -> 1 find soup -> 2 switch -> 3 eat -> 4 drop bowl
// -> 5 switch back (or eat again) -> 6 cool down
extern "C" float auto_soup_asm_pins_keep(float base);
extern "C" void auto_soup_part(Hook *h) {
    Ctx *c = h->ctx;
    if (data_2f7d68 == 1 && (data_2f7d6c == 6 || data_2f7d6c == 0)) {
        queue_module_toggle(c->p08, data_2e96b0, 0);
        return;
    }
    if (c->f40 != 1) {
        data_2f7d70 = 0;
        data_2f7d6c = 0;
        return;
    }
    for (int iter = 0; iter < 3; iter++) {
        float t = data_2f7d80;
        if (t > 0.0f) {
            data_2f7d80 = t + rdata_1505c0;
            return;
        }
        switch (data_2f7d6c) {
        case 0: {
            float hp = java_call_entity_living_base_get_health(c->a, c->b);
            if (hp > data_2e98f0)
                return;
            data_2f7d80 = auto_soup_asm_pins_keep(data_2e9928) + data_2f7d80;
            data_2f7d7c = data_2e9960 >= hp;
            data_2f7d6c = 1;
            break;
        }
        case 1: {
            void *inv = java_get_entity_player_2(c->a, c->b);
            unsigned char keep = data_2f7d70;
            int cur = java_get_inventory_player_current_item(c->a, inv);
            void *hotbar = java_get_inventory_player_main_inventory(c->a, inv);
            int found = -1;
            for (int i = 0; i < 9; i++) {
                int s = (cur + i) % 9;
                if (keep & (s == cur))
                    continue;
                void *a = c->a;
                void *env = ((void **)a)[1];
                void *stack = ((GetElement)(*(void ***)env)[0x568 / 8])(env, hotbar, s);
                int id = stack ? java_call_static_item_get_id_from_item(a, java_call_item_stack_get_item(a, stack)) : 0;
                if (data_2e9a28 == 1) {
                    if (!id_vector_contains(data_2f7db8, id))
                        continue;
                } else if (id != 0x19d && id != 0x11a) {
                    continue;
                }
                data_2f7d7d = (id == 0x11a) | (id == 0x19d);
                found = s;
                if (!walk_should_change(data_2f7da8, rot_make_floats(rdata_1505ac, rdata_1505b0), rdata_1505b8))
                    break;
            }
            if (found == -1) {
                data_2f7d6c = 6 - data_2f7d70;
                data_2f7d70 = 0;
                return;
            }
            data_2f7d74 = found;
            int orig;
            if (data_2f7d70 == 1) {
                orig = data_2f7d78;
            } else {
                orig = java_get_inventory_player_current_item(c->a, inv);
                data_2f7d78 = orig;
                found = data_2f7d74;
            }
            data_2f7d70 = 0;
            data_2f7d6c = (found == orig) | 2;
            break;
        }
        case 2:
            if (!claim_release_hotbar_module(data_2e96b0)) {
                data_2f7d6c = 0;
                return;
            }
            java_set_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b), data_2f7d74);
            data_2f7d6c = 3;
            data_2f7d80 = auto_soup_asm_pins_keep(data_2e9998) + data_2f7d80;
            if (java_call_entity_player_is_using_item(c->a, c->b)) {
                float x = data_2f7d80;
                if (0.0f >= x)
                    data_2f7d80 = x + rdata_1505a8;
            }
            break;
        case 3:
            java_get_key_binding_press_time_2(c->a, java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18)));
            if (data_2e9a48 == 1 && data_2f7d7d) {
                data_2f7d6c = 4;
                float x = auto_soup_asm_pins_keep(data_2e9998) + data_2f7d80;
                data_2f7d80 = x > 0.0f ? x : x + rdata_1505a8;
                break;
            }
            goto eaten;
        case 4:
            java_get_key_binding_press_time_2(c->a, java_get_game_settings_key_bindings_2(c->a, get_net_handler(c->a, c->c18)));
        eaten:
            data_2f7d6c = 5;
            if (data_2f7d74 != data_2f7d78) {
                float x = auto_soup_asm_pins_keep(data_2e9998) + data_2f7d80;
                data_2f7d80 = x;
                if (0.0f >= x)
                    data_2f7d80 = x + rdata_1505a8;
            }
            break;
        case 5:
            release_owner(data_2e96b0);
            if (data_2f7d7c == 1) {
                data_2f7d7c = 0;
                data_2f7d70 = 1;
                data_2f7d6c = 1;
                break;
            }
            java_set_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b), data_2f7d78);
            data_2f7d6c = 6;
            break;
        case 6:
            data_2f7d70 = 0;
            data_2f7d80 = auto_soup_asm_pins_keep(data_2e99d0) + data_2f7d80;
            data_2f7d6c = 0;
            break;
        }
    }
}
