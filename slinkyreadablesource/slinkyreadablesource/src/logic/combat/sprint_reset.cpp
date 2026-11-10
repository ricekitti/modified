// Sprint Reset - descriptor +0x158 (state machine) and helper 0x3b150.
// After a hit the module waits "delay" (randomised), then releases / blocks the
// sprint (or forward/back) key for "duration" and restores it.
//   state 0 = idle, 1 = waiting for the delay, 2 = key released.
#include "slinky/engine.h"

extern "C" unsigned data_2eff58;         // "mode"
extern "C" float data_2eff98;            // "delay"
extern "C" unsigned char data_2effb8;    // "randomize"
extern "C" unsigned char data_2effd8;    // "wait_for_damage"
extern "C" unsigned char data_2efff8;    // "require_weapon"
extern "C" float data_2f0030;            // "duration"
extern "C" int data_2fc870;              // state
extern "C" float data_2fc874;            // countdown
extern "C" double data_2fc878;           // time of last own hit
extern "C" unsigned char data_2fc880;    // hit seen
extern "C" int data_2fc884;              // ticks since hurt
extern "C" char data_2fc888[];           // rng states
extern "C" char data_2fc898[];
extern "C" char data_2fc8a8[];
extern "C" const double rdata_153060;
extern "C" const float rdata_153068;
extern "C" const float rdata_15306c;
extern "C" const double rdata_153070;
extern "C" const float rdata_153078;
extern "C" const float rdata_15307c;
extern "C" const float rdata_153080;
extern "C" const double rdata_153088;

FN(double, time_now, ());
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_3, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_9, (void *a, void *x));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(void, java_call_entity_set_sprinting, (void *a, void *p, bool on));
FN(void *, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, void *rng, double p));
FN(unsigned char, walk_should_change, (void *st, void *rng, double p));
FN(float, random_rotation, (void *rng));
extern "C" void sprint_reset_part_2(Ctx *c, unsigned char on);

extern "C" void sprint_reset_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1) {
        double now = time_now();
        if (!(now - data_2fc878 < rdata_153060)) {
            if (java_get_entity_ticks_existed(c->a, c->b) == java_get_entity_living_base_last_attacker_time(c->a, c->b)) {
                data_2fc878 = now;
                data_2fc880 = 1;
            }
        }
        data_2fc884 += 1;
        if (java_get_entity_living_base_hurt_time(c->a, c->b) == 10)
            data_2fc884 = 0;
        float t = data_2fc874;
        if (t > 0.0f) {
            t += rdata_153068;
            data_2fc874 = t;
            if (t > 0.0f) {
                if (data_2fc870 == 2) {
                    sprint_reset_part_2(c, 1);
                    return;
                }
                return;
            }
        }
        if (data_2fc870 == 2) {
            sprint_reset_part_2(c, 0);
            data_2fc870 = 0;
            return;
        }
        if (data_2fc870 == 1) {
            sprint_reset_part_2(c, 1);
            data_2fc870 = 2;
            data_2fc880 = 0;
            float d = data_2f0030;
            if (data_2effb8 == 1) {
                float x = walk_update(data_2fc898, rot_make_floats(0.0f, rdata_15306c), rdata_153070);
                float m = d - x;
                d = 0.0f > m ? 0.0f : m;
                if (walk_should_change(data_2fc8a8, rot_make_floats(rdata_153078, rdata_15307c), rdata_153070))
                    d += random_rotation(rot_make_floats(rdata_15306c, rdata_153080));
            }
            data_2fc874 = d + data_2fc874;
            return;
        }
        float dl = data_2eff98;
        if (data_2effb8 == 1)
            dl += walk_update(data_2fc888, rot_make_floats(0.0f, rdata_153080), rdata_153088);
        if (data_2fc880 == 1) {
            if (data_2effd8 == 1 && data_2fc884 != 0)
                return;
            if (data_2efff8 == 1 && !click_with_helper(c->a, c->b))
                return;
            data_2fc874 = dl + data_2fc874;
            data_2fc870 = 1;
        }
        return;
    }
    if (data_2fc870 == 2)
        sprint_reset_part_2(c, 0);
    data_2fc870 = 0;
    data_2fc880 = 0;
    *(int *)&data_2fc874 = 0;
}

extern "C" void sprint_reset_part_2(Ctx *c, unsigned char on) {
    void *obj = get_net_handler(c->a, c->c18);
    void *key;
    if (data_2eff58 == 2)
        key = java_get_game_settings_key_bindings_9(c->a, obj);
    else if (data_2eff58 == 1)
        key = java_get_game_settings_key_bindings_3(c->a, obj);
    else
        key = java_get_game_settings_key_bindings_4(c->a, obj);
    if (data_2eff58 == 2)
        java_call_entity_set_sprinting(c->a, c->b, (on ^ 1) != 0);
    unsigned char st = on ? (data_2eff58 == 1) : java_get_key_binding_key_code(c->a, key);
    java_set_key_binding_pressed(c->a, key, st);
}
