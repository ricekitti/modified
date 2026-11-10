// Inv Manager - sorts the inventory with timed window clicks.
//   0x47090 tick: state machine (0 / 1 plan, 3 click, 4 / 5 drag out, 2 done)
//   0x479c0 plan step: pick the best stack for armour / hotbar slot `sub`
//   0x47f90 score an item stack for armour slot `slot` (falls back to the
//           mirrored slot when "swap_sides" is on and the item does not fit)
#include "slinky/engine.h"

extern "C" unsigned char data_2f5208;    // try the paired slot

FN(void *, java_call_item_stack_get_item, (void *a, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(int, java_get_item_stack_metadata, (void *a, void *stack));
FN(void *, java_call_item_stack_get_enchantment_tag_list, (void *a, void *stack));
FN(unsigned char, java_call_item_stack_has_display_name, (void *a, void *stack));
FN(unsigned short, score_item_inventory_sorter_category, (void **s, int slot, int id, int dmg, void *nbt, unsigned char flag));

extern "C" unsigned inv_manager_score_item_stack(void **s, void *stack, int slot) {
    void *item = java_call_item_stack_get_item(*s, stack);
    if (!item)
        return 0;
    int id = java_call_static_item_get_id_from_item(*s, item);
    int dmg = java_get_item_stack_metadata(*s, stack);
    void *nbt = java_call_item_stack_get_enchantment_tag_list(*s, stack);
    unsigned char flag = java_call_item_stack_has_display_name(*s, stack);
    unsigned r = score_item_inventory_sorter_category(s, slot, id, dmg, nbt, flag);
    if (data_2f5208 != 1)
        return r;
    if (r)
        return r << 16;
    int alt;
    switch (slot) {
    case 0x16: alt = 0x19; break;
    case 0x11: alt = 7; break;
    case 7: alt = 0x11; break;
    default: return 0;
    }
    return score_item_inventory_sorter_category(s, alt, id, dmg, nbt, flag);
}

struct Pair { float x, y; };
struct Range { float lo, hi; };
struct InvState {                   // 0x3049e0
    int state;
    int pad4;
    unsigned long long mask;        // slots already handled
    int action;                     // 0 shift-click, 1 swap with hotbar `slot`, 2 pick up
    unsigned char slot, b15, b16, b17;
    int saved;                      // state to resume after a click
    unsigned char sub;              // current target slot within the state
    unsigned char window;           // window slot to click
    unsigned char advance;          // move to the next target after the click
    unsigned char pad1f;
    float delay;
    int pad24;
    Pair pos;                       // last mouse position (humanised delay)
};
struct InvHook { char pad0[8]; Ctx *ctx; unsigned char busy; };
typedef unsigned char (*Step)(Ctx *c, InvState *st);
typedef void *(*GetElement)(void *env, void *arr, int i);

extern "C" InvState data_3049e0;
extern "C" char data_2f4fb0[];                   // per-slot settings, 0x40 bytes each (+0x30 wanted type)
extern "C" float data_2f4e80;                    // start delay
extern "C" float data_2f525c, data_2f5260;       // click delay = a / b
extern "C" unsigned char data_2f5228;            // randomise
extern "C" unsigned char data_2f5280;            // scale by mouse distance
extern "C" unsigned char *const rdata_155b40[2]; // state 0 / 1 enabled
extern "C" const unsigned char rdata_155b50[2];  // targets per state
extern "C" const Step rdata_155b60[2];           // plan step per state
extern "C" const float rdata_155590, rdata_155594, rdata_155598, rdata_15559c, rdata_1555a0, rdata_1555a4;

FN(void *, get_current_screen, (void *a, void *p));
FN(unsigned char, java_jni_is_instance_of_gui_inventory, (void *a, void *g));
FN(void, java_gui_container_handle_mouse_click, (void *a, void *g, int slot, int button, int mode));
FN(Pair, slot_index, (int slot));
FN(float, rot_delta_length, (Pair a, Pair b));
FN(Range, rot_make_floats, (float lo, float hi));
FN(float, random_rotation, (Range r));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(void *, java_get_inventory_player_main_inventory, (void *a, void *p));
FN(int, java_get_item_stack_stack_size, (void *a, void *stack));
FN(int, java_call_item_stack_get_max_stack_size, (void *a, void *stack));
FN(unsigned char, java_call_static_item_stack_are_item_stack_tags_equal, (void *a, void *x, void *y));

static inline __attribute__((always_inline)) void move_to(Pair p) {
    float d = data_2f525c / data_2f5260;
    if (data_2f5280 == 1) {
        float m = rot_delta_length(p, data_3049e0.pos) * rdata_155598;
        d *= m > rdata_15559c ? m : rdata_15559c;
    }
    if (data_2f5228 == 1)
        d *= random_rotation(rot_make_floats(rdata_15559c, rdata_1555a0));
    data_3049e0.delay = d + data_3049e0.delay;
    data_3049e0.pos = p;
}

extern "C" void inv_manager_tick(InvHook *h) {
    InvState &st = data_3049e0;
    Ctx *c = h->ctx;
    void *g = get_current_screen(c->a, c->c18);
    if (!g || !java_jni_is_instance_of_gui_inventory(c->a, g)) {
        __builtin_memset(&st, 0, 0x20);
        st.delay = data_2f4e80;
        if (data_2f5228 == 1)
            st.delay = random_rotation(rot_make_floats(rdata_15559c, rdata_1555a4)) * st.delay;
        st.pos = slot_index(0xd);
        return;
    }
    int s = st.state;
    if (s == 2)
        return;
    if (st.delay > 0.0f) {
        st.delay += rdata_155590;
        if (st.delay > 0.0f)
            return;
    }
    h->busy = 1;
    int button = 0, mode = 0;
    for (;;) {
        switch (s) {
        case 0:
        case 1:
            if (*rdata_155b40[s]) {
                for (unsigned char n = rdata_155b50[s]; st.sub < n; st.sub++) {
                    if (rdata_155b60[s](c, &st)) {
                        move_to(slot_index(st.window));
                        st.state = 3;
                        st.saved = s;
                        st.advance = s != 1;
                        return;
                    }
                }
            }
            st.state = ++s;
            __builtin_memset((char *)&st + 4, 0, 0x1c);
            continue;
        case 3: {
            int act = st.action;
            unsigned char slot = st.slot;
            if (act == 2) {
                button = 0;
                mode = 0;
            } else if (act == 1) {
                button = slot;
                mode = 2;
            } else if (act == 0) {
                button = 0;
                mode = 1;
            }
            java_gui_container_handle_mouse_click(c->a, g, st.window, button, mode);
            st.mask |= (long long)(1 << (st.sub & 31));
            if (st.advance == 1)
                st.sub++;
            if (act == 2) {
                move_to(slot_index(slot));
                st.state = 4;
                st.window = slot;
                return;
            }
            s = st.state = st.saved;
            continue;
        }
        case 4: {
            java_gui_container_handle_mouse_click(c->a, g, st.window, 0, 0);
            Pair p = st.pos;
            p.x += rdata_155594;
            move_to(p);
            st.state = 5;
            return;
        }
        case 5:
            java_gui_container_handle_mouse_click(c->a, g, -999, 0, 0);    // drop outside the window
            s = st.state = st.saved;
            continue;
        default:
            return;
        }
    }
}

static inline __attribute__((always_inline)) unsigned long long item_key(Ctx *c, void *stack) {
    void *item = java_call_item_stack_get_item(c->a, stack);
    if (!item)
        return 0;
    unsigned id = java_call_static_item_get_id_from_item(c->a, item);
    return ((unsigned long long)id << 32) | (unsigned long long)(long long)java_get_item_stack_metadata(c->a, stack);
}

extern "C" unsigned char inv_manager_plan_step(Ctx *c, InvState *st) {
    unsigned sub = st->sub;
    int want = *(int *)(data_2f4fb0 + sub * 0x40 + 0x30);
    if (!want)
        return 0;
    void *inv = java_get_inventory_player_main_inventory(c->a, java_get_entity_player_2(c->a, c->b));
    void *env = ((void **)c->a)[1];
    GetElement get = (GetElement)(*(void ***)env)[0x568 / 8];
    void *cur = get(env, inv, sub);
    int count = 0, max = 0;
    unsigned long long key = 0;
    unsigned score = 0;
    if (cur) {
        count = java_get_item_stack_stack_size(c->a, cur);
        max = java_call_item_stack_get_max_stack_size(c->a, cur);
        key = item_key(c, cur);
        score = inv_manager_score_item_stack((void **)c, cur, want);
    }
    void *last;
    int i = 9;
    for (;;) {                                  // any empty main-inventory slot?
        last = get(env, inv, i);
        if (!last || i == 0x23)
            break;
        i++;
    }
    if (count <= 0)
        count = 0;
    unsigned best = count * score;
    unsigned best_slot = sub;
    unsigned char merge = 0;
    for (unsigned j = 0; j < 0x24; j++) {
        if (j == sub || (st->mask >> j & 1))
            continue;
        void *s = get(env, inv, j);
        if (!s)
            continue;
        int n = java_get_item_stack_stack_size(c->a, s);
        unsigned long long k = item_key(c, s);
        unsigned merged = 0;
        if (count < max && n < max && k == key && java_call_static_item_stack_are_item_stack_tags_equal(c->a, cur, s)) {
            int total = count + n;
            merged = (total < max ? total : max) * score;
        }
        unsigned v = inv_manager_score_item_stack((void **)c, s, want) * n;
        bool use_merge = v - 1 < merged;
        if (use_merge)
            v = merged;
        if (v > best && (!last || j >= 9 || !use_merge)) {
            best = v;
            best_slot = j;
            merge = use_merge;
        }
    }
    if (best_slot != sub) {
        st->b15 = st->b16 = st->b17 = 0;
        st->action = !merge;
        st->slot = merge ? 0 : sub;
        st->window = (int)best_slot >= 9 ? best_slot : best_slot + 0x24;
        st->mask |= (long long)(1 << (sub & 31));
        return 1;
    }
    if (best)
        st->mask |= (long long)(1 << (sub & 31));
    return 0;
}
