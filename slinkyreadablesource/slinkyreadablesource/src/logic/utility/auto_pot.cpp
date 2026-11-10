// Auto Pot - throw splash healing potions when low.
//   +0x98 (event): on low-health trigger work out how many potions to throw
//   +0x48 (tick):  state machine 0 find potion -> 1 switch slot -> 2 throw
//                  -> 3 next/switch back -> 4 done (module disables itself)
//   +0x88 (input): arm an extra throw for "double_splash"
//   0x396e0:       randomised delay between steps
#include "slinky/engine.h"

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct InHook { char pad0[8]; void *mod; unsigned char use; };

extern "C" char data_2ef128[];           // Auto Pot module descriptor (timer at +0)
extern "C" unsigned char data_2ef350;    // "use_non_health"
extern "C" float data_2ef388;            // "delay"
extern "C" unsigned char data_2ef3a8;    // "prevent_waste"
extern "C" unsigned char data_2ef3c8;    // "double_splash"
extern "C" int data_2fc640;              // state
extern "C" int data_2fc644;              // potion slot
extern "C" int data_2fc648;              // original slot
extern "C" unsigned char data_2fc64c;    // keep original slot
extern "C" float data_2fc650;            // step delay
extern "C" int data_2fc654;              // potions left to throw
extern "C" char data_2fc658[];           // rng states
extern "C" char data_2fc668[];
extern "C" int data_2fc678;              // extra throws
extern "C" const float rdata_152c38;     // 1.0
extern "C" float rdata_152c3c;     // -1.0
extern "C" double rdata_152c58;
extern "C" double rdata_152c60;
extern "C" float rdata_152c68;
extern "C" double rdata_152c70;
extern "C" float rdata_152c78;
extern "C" float rdata_152c7c;
extern "C" double rdata_152c80;
extern "C" float rdata_152c88;

FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *p, int slot));
FN(void *, java_call_item_stack_get_item, (void *a, void *item));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(unsigned, java_get_item_stack_metadata, (void *a, void *stack));
FN(int, java_get_inventory_player_current_item, (void *a, void *p));
FN(void, java_set_inventory_player_current_item, (void *a, void *p, int slot));
FN(unsigned char, claim_release_hotbar_module, (void *timer));
FN(void, release_owner, (void *timer));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(void, queue_module_toggle, (void *a, void *mod, int x));
FN(float, java_call_entity_living_base_get_health, (void *a, void *p));
FN(float, java_call_entity_living_base_get_max_health, (void *a, void *p));
FN(void *, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, void *rng, double p));
FN(unsigned char, walk_should_change, (void *st, void *rng, double p));
FN(float, random_rotation, (void *rng));
extern "C" float auto_pot_asm_r_pins();

extern "C" void auto_pot_part(Hook *h) {
    Ctx *c = h->ctx;
    #pragma clang loop unroll(disable)
    for (int iter = 0; iter < 4; iter++) {
        float t = data_2fc650;
        if (t > 0.0f) {
            data_2fc650 = t + rdata_152c3c;
            goto end;
        }
        if (!c->f40) {
            data_2fc64c = 0;
            goto stop;
        }
        switch (data_2fc640) {
        case 0: {
            void *p = java_get_entity_player_2(c->a, c->b);
            int slot = 0;
            #pragma clang loop unroll(disable)
            for (;; slot++) {
                if (slot == 9) {
                    data_2fc654 = 0;
                    int st = 4 - data_2fc64c;
                    data_2fc640 = st;
                    data_2fc64c = 0;
                    if (st != 4)
                        return;
                    goto disable;
                }
                void *it = java_call_iinventory_get_stack_in_slot(c->a, p, slot);
                if (!it)
                    continue;
                void *item = java_call_item_stack_get_item(c->a, it);
                if (!item || java_call_static_item_get_id_from_item(c->a, item) != 0x175)
                    continue;
                unsigned dmg = java_get_item_stack_metadata(c->a, it);
                if (!(dmg & 0x4000))
                    continue;
                if ((dmg & 0xf) != 5 && !data_2ef350)
                    continue;
                break;
            }
            data_2fc644 = slot;
            int orig;
            if (data_2fc64c == 1) {
                orig = data_2fc648;
            } else {
                orig = java_get_inventory_player_current_item(c->a, p);
                data_2fc648 = orig;
                slot = data_2fc644;
            }
            data_2fc64c = 0;
            data_2fc640 = (slot == orig) + 1;
            continue;
        }
        case 1:
            if (!claim_release_hotbar_module(data_2ef128))
                goto stop;
            java_set_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b), data_2fc644);
            data_2fc640 = 2;
            t = auto_pot_asm_r_pins();
            break;
        case 2:
            if (java_call_entity_player_is_using_item(c->a, c->b))
                goto end;
            java_get_key_binding_press_time_2(c->a, java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18)));
            data_2fc654 += -1;
            data_2fc640 = 3;
            if (data_2fc644 == data_2fc648)
                continue;
            t = auto_pot_asm_r_pins() + rdata_152c38;
            break;
        case 3:
            if (data_2fc654 > 0) {
                data_2fc64c = 1;
                data_2fc640 = 0;
            } else {
                java_set_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b), data_2fc648);
                data_2fc640 = 4;
            }
            continue;
        case 4:
            release_owner(data_2ef128);
            goto end;
        default:
            __builtin_unreachable();
        }
        data_2fc650 = t + data_2fc650;
    }
end:
    if (data_2fc640 != 4)
        return;
    goto disable;
stop:
    data_2fc640 = 4;
disable:
    queue_module_toggle(c->p08, data_2ef128, 0);
}

extern "C" void auto_pot_part_2(InHook *h) {
    // the original evaluates all three conditions with setcc and ORs them
    unsigned char other = h->mod != data_2ef128;

    unsigned char done = data_2fc640 == 4;

    done |= other;
    int extra = data_2fc678;
    unsigned char none = extra <= 0;

    none |= done;
    if (!none) {
        h->use = 1;
        data_2fc654 += 1;
        data_2fc678 = extra - 1;
    }

}

extern "C" void auto_pot_part_3(EvHook *h) {
    if (h->ev != data_2ef128)
        goto end;
    if (h->on != 1) {
        data_2fc640 = 0;
        data_2fc654 = 0;
        goto end;
    }
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        goto end;
    float hp = java_call_entity_living_base_get_health(c->a, c->b);
    int n = (int)(((double)(java_call_entity_living_base_get_max_health(c->a, c->b) - hp) + rdata_152c58) * rdata_152c60);
    if (n == 0) {
        n = 1;
        if (data_2ef3a8 == 1) {
            data_2fc640 = 4;
            goto end;
        }
    }
    if (data_2ef3c8 == 1) {
        data_2fc654 = n;
        data_2fc678 = 0;
    } else {
        data_2fc654 = 1;
        data_2fc678 = data_2ef3a8 ? n - 1 : 5;
    }
end:

}

extern "C" float auto_pot_asm_r_pins() {
    float d = data_2ef388;
    float r = 0.0f;

    float lo = 0.0f, hi = rdata_152c68;
    void *rng = rot_make_floats(lo, hi);
    char *st = data_2fc658;

    double k = rdata_152c70;
    float m = d - walk_update(st, rng, k);
    r = r > m ? r : m;
    lo = rdata_152c78;
    hi = rdata_152c7c;
    rng = rot_make_floats(lo, hi);
    st = data_2fc668;

    k = rdata_152c80;
    if (walk_should_change(st, rng, k)) {
        lo = rdata_152c68;
        hi = rdata_152c88;
        r += random_rotation(rot_make_floats(lo, hi));
    }
    return r;
}
