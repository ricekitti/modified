// Refill - move selected items into the hotbar and keep them stacked.
#include "slinky/engine.h"

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" char data_2eed90[];           // Refill module descriptor
extern "C" unsigned char data_2ef060;    // soup class offset
extern "C" unsigned char data_2ef080;    // potions enabled
extern "C" unsigned char data_2ef0a0;    // non-health potion class offset
extern "C" int data_2fc580;              // state
extern "C" unsigned char data_2fc5e0[27];   // slot classes

struct InvHook { char pad0[8]; Ctx *ctx; unsigned char handled; };
extern "C" float data_2eefd0;            // "close_delay"
extern "C" float data_2ef008;            // "open_delay"
extern "C" float data_2ef040;            // "speed"
extern "C" unsigned char data_2ef0c0;    // distance based delay
extern "C" unsigned data_2ef118;         // click button mode (0/1 fixed, 2 random)
extern "C" char data_2fc588[], data_2fc598[], data_2fc5a8[], data_2fc5b8[], data_2fc5c8[], data_2fc610[];
extern "C" float data_2fc5d0;            // step delay
extern "C" int data_2fc5fc;              // last slot
extern "C" float data_2fc600;
extern "C" int data_2fc604;              // slot being moved
extern "C" int data_2fc608;              // random button
extern "C" const float rdata_152a30, rdata_152a34, rdata_152a40, rdata_152a44, rdata_152a60, rdata_152a64,
                       rdata_152a68, rdata_152a6c, rdata_152a70, rdata_152a74, rdata_152a78, rdata_152a7c;
extern "C" const double rdata_152a38, rdata_152a48, rdata_152a50, rdata_152a58;
FN(void *, get_current_screen, (void *a, void *x));
FN(unsigned char, java_jni_is_instance_of_gui_inventory, (void *a, void *screen));
FN(void *, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, void *rng, double p));
FN(unsigned char, walk_should_change, (void *st, void *rng, double p));
FN(float, random_rotation, (void *rng));
FN(float, gauss_random, (void *st));
FN(int, find_hotbar_cell, (int last, float bias, float *dist));
FN(void, close_screen_and_release, (Ctx *c));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, get_game_screen, (void *a, void *x));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(void, java_gui_container_handle_mouse_click, (void *a, void *screen, int slot, int button, int mode));
FN(void, queue_module_toggle, (void *a, void *mod, int x));
FN(void, keyboard_send_key, (void *a, void *screen, int x, int y));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *inv, int slot));
FN(void *, java_call_item_stack_get_item, (void *a, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(unsigned, java_get_item_stack_metadata, (void *a, void *stack));

extern "C" void refill_enable(EvHook *h) {
    if (h->ev != data_2eed90 || h->on != 1)
        return;
    data_2fc580 = 0;
}

// class: 0 = empty, 1 = other / soup-less, 2 = splash heal potion,
//        data_2ef0a0+1 = other splash potion, data_2ef060+1 = mushroom soup
extern "C" void refill_classify_27_main(Ctx *c) {
    void *inv = java_get_entity_player_2(c->a, c->b);
    for (long i = 0; i != 27; i++) {
        unsigned char cls;
        void *it = java_call_iinventory_get_stack_in_slot(c->a, inv, (int)i + 9);
        if (!it)
            goto empty;
        {
            void *a = c->a;
            void *item = java_call_item_stack_get_item(a, it);
            if (!item)
                goto empty;
            int id = java_call_static_item_get_id_from_item(a, item);
            if (!id)
                goto empty;
            switch (id) {
            case 0x175:
                cls = 1;
                if (data_2ef080 == 1) {
                    unsigned dmg = java_get_item_stack_metadata(a, it);
                    if (dmg & 0x4000) {
                        cls = 2;
                        if ((dmg & 0xf) != 5)
                            cls = data_2ef0a0 + 1;
                    }
                }
                break;
            case 0x11a:
                cls = data_2ef060 + 1;
                break;
            default:
                cls = 1;
                break;
            }
            goto store;
        }
empty:
        cls = 0;
store:
        data_2fc5e0[i] = cls;
    }
}

static inline __attribute__((always_inline)) void add_delay(float base, char *st) {
    double d = (double)base - (double)walk_update(st, rot_make_floats(0.0f, rdata_152a70), rdata_152a48);
    d = d > 0.0 ? d : 0.0;
    data_2fc5d0 = (float)((double)data_2fc5d0 + d);
}

// tick: open the inventory, shift-click soups / potions into the hotbar, close it
//   state 0 open, 1 pick timing, 2 choose slot, 3 click, 4 done
extern "C" void refill_classify_27_main(Ctx *c);
extern "C" void refill_part(InvHook *h) {
    Ctx *c = h->ctx;
    void *scr = get_current_screen(c->a, c->c18);
    if (data_2fc580 != 0) {
        if (!scr || !java_jni_is_instance_of_gui_inventory(c->a, scr))
            goto disable;
        h->handled = 1;
    }
    {
        float t = data_2fc5d0;
        if (t > 0.0f) {
            data_2fc5d0 = t + rdata_152a7c;
            return;
        }
        if (!(0.0f >= t))
            return;
    }
    {
        int btn = 0;
        for (;;) {
            if (data_2fc580 == 4)
                goto disable;
            if (!c->b)
                goto finish;
            {
                void *inv = java_get_entity_player_2(c->a, c->b);
                int i = 0;
                for (; i < 9; i++) {
                    void *it = java_call_iinventory_get_stack_in_slot(c->a, inv, i);
                    if (!it)
                        break;
                    void *item = java_call_item_stack_get_item(c->a, it);
                    if (!item || !java_call_static_item_get_id_from_item(c->a, item))
                        break;
                }
                if (i == 9)
                    goto finish;
            }
            switch (data_2fc580) {
            case 0: {
                data_2fc580 = 1;
                if (scr)
                    return;
                refill_classify_27_main(c);
                float dist;
                if (find_hotbar_cell(data_2fc5fc, data_2fc600, &dist) == -1) {
                    close_screen_and_release(c);
                    return;
                }
                java_get_key_binding_press_time_2(c->a, get_game_screen(c->a, get_net_handler(c->a, c->c18)));
                return;
            }
            case 1:
                data_2fc5fc = (int)random_rotation(rot_make_floats(0.0f, rdata_152a74));
                data_2fc600 = random_rotation(rot_make_floats(0.0f, rdata_152a78));
                data_2fc580 = 2;
                add_delay(data_2ef008, data_2fc588);
                break;
            case 2: {
                float dist;
                int r = find_hotbar_cell(data_2fc5fc, data_2fc600, &dist);
                data_2fc604 = r;
                if (r == -1) {
                    add_delay(data_2eefd0, data_2fc598);
                    data_2fc580 = 4;
                    return;
                }
                float q = walk_update(data_2fc5a8, rot_make_floats(rdata_152a40, rdata_152a44), rdata_152a48) * data_2ef040;
                q = (float)((double)q * ((double)gauss_random(data_2fc5c8) * rdata_152a50 + rdata_152a58));
                if (walk_should_change(data_2fc5b8, rot_make_floats(rdata_152a60, rdata_152a64), rdata_152a48))
                    q /= random_rotation(rot_make_floats(rdata_152a68, rdata_152a6c));
                float num = data_2ef0c0 ? dist : rdata_152a34;
                data_2fc5d0 = num / q + data_2fc5d0;
                data_2fc5fc = data_2fc604;
                data_2fc580 = 3;
                break;
            }
            case 3: {
                unsigned mode = data_2ef118;
                if (mode < 2) {
                    btn = (int)mode;
                } else if (mode == 2) {
                    btn = data_2fc608;
                    if (walk_should_change(data_2fc610, rot_make_floats(rdata_152a30, rdata_152a34), rdata_152a38))
                        data_2fc608 = data_2fc608 == 0;
                }
                java_gui_container_handle_mouse_click(c->a, scr, data_2fc604 + 9, btn, 1);
                data_2fc5e0[data_2fc604] = 0;
                data_2fc580 = 2;
                break;
            }
            }
            if (!(0.0f >= data_2fc5d0))
                return;
        }
    }
finish:
    add_delay(data_2eefd0, data_2fc598);
    data_2fc580 = 4;
    if (!(0.0f >= data_2fc5d0))
        return;
disable:
    queue_module_toggle(c->p08, data_2eed90, 0);
    {
        void *s2 = get_current_screen(c->a, c->c18);
        if (s2)
            keyboard_send_key(c->a, s2, 0, 1);
    }
}
