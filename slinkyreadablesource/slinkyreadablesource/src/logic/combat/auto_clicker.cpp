// Auto Clicker - scheduled left clicks (attack key) with configurable CPS and
// randomised timing, released while a GUI is open.
#include "slinky/engine.h"

struct Rot { float yaw, pitch; };
struct Range { float lo, hi; };

extern "C" Rot data_2f7c88;              // rotation at the last tick
extern "C" unsigned char data_2f7c90;    // attack bound to mouse (-99 = button 1)
extern "C" int data_2f7cd0;              // hit type under the crosshair
extern "C" unsigned char *const rdata_1505a0;   // client state (+0x14: screen open)

extern "C" unsigned char data_2e96a8;    // "burst"
extern "C" char data_2f7d38[], data_2f7d48[], data_2f7d58[];   // rng states
extern "C" int data_2f7d60;              // burst clicks left
extern "C" double rdata_1503d8, rdata_1503e0, rdata_1503e8, rdata_150400, rdata_150410;
extern "C" float rdata_1503f0, rdata_1503f4, rdata_1503f8, rdata_150408, rdata_15040c;
FN(float, gauss_random, (void *st));
FN(Range, rot_make_floats, (float lo, float hi));
FN(unsigned char, walk_should_change, (void *st, Range r, double p));
FN(float, walk_update, (void *st, Range r, double p));
FN(float, random_rotation, (Range r));
extern "C" double data_2f7c98;           // time until the next click edge
extern "C" double data_2f7ca0;           // last edge delay
extern "C" char data_2f7ca8[], data_2f7cc0[], data_2f7cd8[], data_2f7ce8[], data_2f7cf8[],
                data_2f7d08[], data_2f7d18[], data_2f7d28[];          // rng states
extern "C" unsigned char data_2f7cb8;    // lmb was down in the GUI
extern "C" unsigned char data_2f7d00;    // double-click pending
extern "C" int data_2e9540;              // last inventory slot clicked
extern "C" unsigned char data_2e9560;    // "inventory_fill"
extern "C" unsigned char data_2e9580;    // "inventory_random_delay"
extern "C" unsigned char data_2e95a0;    // "break_blocks"
extern "C" unsigned char data_2e95c0;    // "require_weapon"
extern "C" unsigned char data_2e95e0;    // "pause_when_using"
extern "C" float data_2e9618;            // "cps"
extern "C" unsigned char data_2e9638;    // "randomize"
extern "C" unsigned data_2e9680;         // "mode"
extern "C" double rdata_150328, rdata_150330, rdata_150340, rdata_150348, rdata_150368, rdata_150378,
                  rdata_150388, rdata_150398, rdata_1503b0, rdata_1503b8, rdata_1503c0, rdata_1503c8;
extern "C" float rdata_150338, rdata_15033c, rdata_150350, rdata_150354, rdata_150358, rdata_15035c,
                 rdata_150360, rdata_150364, rdata_150370, rdata_150374, rdata_150380, rdata_150384,
                 rdata_150390, rdata_150394, rdata_1503a0, rdata_1503a4, rdata_1503a8, rdata_1503d0, rdata_1503d4;
struct Target { void *pad; void *ent; double dist2; };
struct TickHook { char pad0[8]; Ctx *ctx; char pad10[8]; double dt; };
FN(void *, get_timer, (void *a, void *x));
FN(float, java_get_timer_timer_speed, (void *a, void *timer));
FN(void, post_synthetic_click, (int button, unsigned char down));
FN(float, rot_delta_length, (Rot a, Rot b));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void *, get_current_screen, (void *a, void *x));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(void *, java_get_gui_container, (void *a, void *screen));
FN(void *, java_get_inventory_player, (void *a, void *inv));
FN(int, java_get_slot_slot_number, (void *a, void *slot));
FN(void *, java_call_slot_get_stack, (void *a, void *slot));
FN(unsigned char, random_chance, (float chance));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_8, (void *a, void *x));
FN(int, get_event_key, (void *a, void *k));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(void, java_set_minecraft_left_click_counter, (void *a, void *x, int y));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));

extern "C" void auto_clicker_tick(Hook *h) {
    Ctx *c = h->ctx;
    data_2f7c90 = get_event_key(c->a, java_get_game_settings_key_bindings_8(c->a, get_net_handler(c->a, c->c18))) == -99;
    if (c->f40 != 1)
        return;
    data_2f7c88 = get_entity_rotation(c->a, c->b);
    if (rdata_1505a0[0x14])
        return;
    java_set_minecraft_left_click_counter(c->a, c->c18, 0);
}

extern "C" void auto_clicker_tick_2(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1) {
        void *t = java_get_minecraft_5(c->a, c->c18);
        data_2f7cd0 = t ? java_get_moving_object_position_3(c->a, t) : 0;
    }
}

// extra "burst" clicks: returns true (and pushes *delay out) while a burst is running
extern "C" unsigned char auto_clicker_extra_burst_clicks(float *delay, float base) {
    int n = data_2f7d60;
    if (n <= 0) {
        if (data_2e96a8 != 1)
            return 0;
        double bd = base;
        Range r = {(float)(rdata_1503d8 + bd), (float)(bd + rdata_1503e0)};

        char *st = data_2f7d38;

        if (!walk_should_change(st, r, rdata_1503e8))
            return 0;
        float v = random_rotation(rot_make_floats(0.0f, rdata_1503f0));
        float lo = rdata_1503f4;

        Range r1 = rot_make_floats(lo, rdata_1503f8);
        char *st1 = data_2f7d48;

        n = 3;
        if (!(walk_update(st1, r1, rdata_150400) > v)) {
            lo = rdata_150408;

            Range r2 = rot_make_floats(lo, rdata_15040c);
            n = (walk_update(data_2f7d48, r2, rdata_150400) > v) + 1;
        }
    }
    data_2f7d60 = n - 1;
    double add = (double)gauss_random(data_2f7d58) * rdata_150410 + rdata_1503d8;
    *delay = (float)((double)*delay + add);
    return 1;
}

// aim-change bonus: clicking slows down when the crosshair moves fast / no target in reach
static inline __attribute__((always_inline)) float aim_bonus(Ctx *c) {
    float x = rot_delta_length(get_entity_rotation(c->a, c->b), data_2f7c88) / rdata_1503a8 + 0.0f;
    Target *t = (Target *)c->p38;
    if (!t->ent || t->dist2 > rdata_1503b0)
        x = (float)((double)x + rdata_1503b8);
    return x;
}

// tick: generate press / release edges of the attack button at the configured cps
extern "C" unsigned char auto_clicker_extra_burst_clicks(float *delay, float base);
extern "C" void auto_clicker_part(TickHook *h) {
    Ctx *c = h->ctx;
    double dt = h->dt;
    double acc = data_2f7c98 - (double)java_get_timer_timer_speed(c->a, get_timer(c->a, c->c18)) * dt;
    acc = rdata_150328 > acc ? rdata_150328 : acc;
    data_2f7c98 = acc;
    if (!(0.0 >= acc))
        return;
    unsigned char *mouse = (unsigned char *)c->p30;
    unsigned char held = mouse[data_2f7c90 + 1];
    unsigned char edge = (held & 2) >> 1;
    double reset = rdata_150330;
    for (;;) {
        if (c->f40 == 0)
            goto gui;
        if (!((held | edge) & 1))
            goto stop;
        if (data_2f7cd0 == 1 && data_2e95a0)
            goto cancel;
        if (data_2e95c0 == 1 && !click_with_helper(c->a, c->b))
            goto cancel;
        if (data_2e95e0 == 1) {
            if (java_call_entity_player_is_using_item(c->a, c->b) || ((held & edge) & 1))
                goto cancel;
        } else if ((held & edge) & 1) {
            held = 1;
            goto release;
        }
        {
            int btn = data_2f7c90 ^ 1;
            if (held & 1) {
                post_synthetic_click(btn, 0);
                data_2f7c98 = data_2f7ca0 + data_2f7c98;
            } else {
                post_synthetic_click(btn, 1);
                float cps = data_2e9618;
                if (data_2e9638 == 1) {
                    float r15 = walk_update(data_2f7ce8, rot_make_floats(rdata_150370, rdata_150374), rdata_150378);
                    float k = rdata_150380;
                    float r6 = walk_update(data_2f7d08, rot_make_floats(k, rdata_150384), rdata_150388);
                    float r7 = walk_update(data_2f7d18, rot_make_floats(k, rdata_150384), rdata_150378);
                    float r0 = walk_update(data_2f7d28, rot_make_floats(rdata_150390, rdata_150394), rdata_150398);
                    cps *= r6 + rdata_1503a0 + r7 + r0;
                    cps *= gauss_random(data_2f7cf8) * r15 + rdata_1503a0;
                }
                float delay;
                if (data_2e9680 == 1) {
                    if (data_2f7d00 == 1) {
                        delay = __builtin_bit_cast(float, 0x3c656042u);
                        data_2f7d00 = 0;
                    } else {
                        delay = (float)(rdata_1503c0 / (double)cps + rdata_1503c8);
                        if (!auto_clicker_extra_burst_clicks(&delay, aim_bonus(c)))
                            data_2f7d00 = random_chance((float)walk_should_change(data_2f7cd8, rot_make_floats(rdata_1503d0, rdata_1503d4), rdata_150378));
                    }
                } else {
                    delay = rdata_1503a4 / cps;
                    auto_clicker_extra_burst_clicks(&delay, aim_bonus(c));
                }
                double dd = delay;
                data_2f7c98 = data_2f7c98 + dd;
                data_2f7ca0 = dd;
            }
        }
        held ^= 1;
        edge ^= 1;
        if (0.0 < data_2f7c98)
            return;
    }
cancel:
    reset = 0.0;
    if (edge & 1) {
release:
        post_synthetic_click(data_2f7c90 ^ 1, (~held) & 1);
        reset = 0.0;
    }
stop:
    data_2f7c98 = reset;
    data_2e9540 = -1;
    return;
gui:
    {
        if (edge & 1)
            post_synthetic_click(data_2f7c90 ^ 1, (~held) & 1);
        if (!get_current_screen(c->a, c->c18))
            goto stop;
        unsigned char *m = (unsigned char *)c->p30;
        unsigned char lmb = m[1];
        double v;
        if (lmb == 1 && data_2f7cb8 == 1) {
            if (data_2e9560 != 1 || (!(m[0x62] & 1) && !(m[0x63] & 1)))
                goto plain;
            void *a = c->a;
            void *scr = get_current_screen(a, c->c18);
            void *inv = java_get_entity_player_2(a, c->b);
            void *slot = java_get_gui_container(a, scr);
            if (!slot || java_get_inventory_player(a, inv))
                goto plain;
            int idx = java_get_slot_slot_number(a, slot);
            if (idx == data_2e9540)
                goto plain;
            data_2e9540 = idx;
            if (!java_call_slot_get_stack(a, slot) && walk_should_change(data_2f7cc0, rot_make_floats(rdata_150338, rdata_15033c), rdata_150340))
                goto plain;
            post_synthetic_click(1, 1);
            if (data_2e9580 != 1) {
                v = rdata_150348;
                goto add;
            }
            Range r = walk_should_change(data_2f7ca8, rot_make_floats(rdata_150350, rdata_150354), rdata_150340)
                          ? rot_make_floats(rdata_150360, rdata_150364) : rot_make_floats(rdata_150358, rdata_15035c);
            v = random_rotation(r);
            goto add;
        }
        data_2f7cb8 = lmb == 1;
plain:
        v = data_2e9580 ? rdata_150368 : rdata_150348;
add:
        data_2f7c98 = v + data_2f7c98;
    }
}
