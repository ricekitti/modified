// Fast Place - repeats the use key while it is held, at a fixed rate.
//   0x2fbc0 tick: after the activation delay, click whenever the timer allows and the
//           placement would not land inside the player
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Box { Vec3 lo, hi; };
struct Range { float lo, hi; };

extern "C" float data_2f9d60;            // click timer
extern "C" unsigned char data_2f9d64;    // use key held last tick
extern "C" int data_2f9d68;              // activation delay left
extern "C" char data_2f9d70[];           // RNG state
extern "C" float data_2ea510;            // clicks per second
extern "C" float data_2ea548;            // activation delay
extern "C" const float rdata_150ce8, rdata_150d00, rdata_150d04;
extern "C" const double rdata_150cf0, rdata_150cf8;

FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *gs));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(int, java_get_key_binding_press_time, (void *a, void *k));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(unsigned char, block_entity_id, (void *a, void *x));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *hit));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(Vec3, java_get_variant_2, (void *a, void *hit));
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(Box, aabb_make, (Vec3 lo, Vec3 hi));
FN(void *, java_get_entity, (void *a, void *p));
FN(Box, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(unsigned char, aabb_intersects, (Box a, Box b));
FN(Range, rot_make_floats, (float lo, float hi));
FN(unsigned char, walk_should_change, (void *st, Range r, double d));

extern "C" void fast_place_tick(Hook *h) {
    float t = data_2f9d60;
    if (t > 0.0f) {
        t += rdata_150ce8;
        data_2f9d60 = t;
        if (t > 0.0f)
            return;
    }
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    void *key = java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18));
    unsigned char was_held = data_2f9d64;
    unsigned char held = java_get_key_binding_key_code(c->a, key);
    data_2f9d64 = held;
    if (!held) {
        data_2f9d68 = (int)data_2ea548;
        return;
    }
    if (data_2f9d68 > 0) {
        data_2f9d68 -= 1;
        return;
    }
    if (java_get_key_binding_press_time(c->a, key) > 0)
        return;
    t = data_2f9d60;
    if (!(0.0f >= t))
        return;
    bool first = !was_held;                     // the first press clicks by itself
    for (;;) {
        t = (float)((double)t + rdata_150cf0 / (double)data_2ea510);
        data_2f9d60 = t;
        if (!first && block_entity_id(c->a, item_held_hotbar_slot(c->a, c->b))) {
            bool click;
            void *hit = java_get_minecraft_5(c->a, c->c18);
            if (hit && java_get_moving_object_position_3(c->a, hit) == 1) {
                void *stack = item_held_hotbar_slot(c->a, c->b);
                if (stack && java_call_static_item_get_id_from_item(c->a, stack) == 0x41) {
                    click = true;               // ladders always
                } else {
                    // only when the new block would not overlap the player
                    Vec3 at = java_get_variant_2(c->a, hit);
                    Box cell = aabb_make(at, vec3_add(at, vec3_make(rdata_150cf8, rdata_150cf8, rdata_150cf8)));
                    Box me = java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, c->b));
                    click = !aabb_intersects(cell, me) ||
                            walk_should_change(data_2f9d70, rot_make_floats(rdata_150d00, rdata_150d04), rdata_150cf0);
                }
            } else {
                click = walk_should_change(data_2f9d70, rot_make_floats(rdata_150d00, rdata_150d04), rdata_150cf0);
            }
            if (click)
                java_get_key_binding_press_time_2(c->a, key);
            t = data_2f9d60;
        }
        first = false;
        if (!(0.0f >= t))
            return;
    }
}
