// Hit Select - cancel attacks that would be "bad trades" (only hit when the
// target can be comboed / we are not about to take damage).
//   +0x48  tick:   count ground ticks without being hurt
//   +0x128 attack: decide (once per tick) whether to cancel the attack
//   +0x158 tick:   track the target's hurt time / server-side hit timing
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct AttackHook { char pad0[8]; Ctx *ctx; unsigned char phase; unsigned char cancel; };

extern "C" float data_2ee730;            // "cancel_rate_air"
extern "C" float data_2ee768;            // "pause_dur"
extern "C" float data_2ee7a0;            // "wait_for_damage_dur"
extern "C" float data_2ee7d8;            // "trades_delay"
extern "C" float data_2ee810;            // "cancel_rate"
extern "C" unsigned char data_2ee830;    // "require_sprint"
extern "C" unsigned char data_2ee850;    // "require_weapon"
extern "C" unsigned data_2ee8a0;         // "mode"
extern "C" unsigned char data_2ee8c8;    // "use_server_time"
extern "C" unsigned char data_2ee8e8;    // "crits_allow_reduce"
extern "C" unsigned char data_2ee908;    // "require_damage"
extern "C" unsigned char data_2ee928;    // "fake_swing"
extern "C" unsigned char data_2fc538;    // decided this tick
extern "C" int data_2fc53c;              // calm ticks
extern "C" unsigned char data_2fc540;    // cancel
extern "C" unsigned data_2fc544;         // ticks since target was hit
extern "C" unsigned data_2fc548;         // wait-for-damage counter
extern "C" unsigned char data_2fc54c;    // target just got hit
extern "C" int data_2fc550;              // last target id

FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void, java_jni_call_nonvirtual_void_method_entity_living_base_swing_item, (void *a, void *p));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void *, java_get_moving_object_position_2, (void *a, void *x));
FN(unsigned char, random_chance, (float chance));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(float, java_get_entity_fall_distance, (void *a, void *p));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height_sneak, (Vec3 v));
FN(double, libc_floor, (double v));
FN(void *, java_call_variant, (void *a, void *world, int x, int y, int z));
FN(int, get_block_id, (void *a, void *blk));
FN(int, java_get_entity_entity_id, (void *a, void *p));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));

// whole part of `v` plus a chance roll on the fractional part
static inline unsigned rand_round(float v) {
    unsigned whole = (unsigned)(long long)v;
    return random_chance(v - (float)(unsigned long long)whole) + whole;
}

extern "C" void hit_select_part(Hook *h) {
    data_2fc538 = 0;
    Ctx *c = h->ctx;
    int n = 0;
    if (c->f40 == 1 && java_get_entity_living_base_hurt_time(c->a, c->b) == 0)
        n = data_2fc53c + 1;
    data_2fc53c = n;
}

extern "C" void hit_select_part_2(AttackHook *h) {
    if (h->phase != 1)
        return;
    Ctx *c = h->ctx;
    if (data_2fc538 == 1) {
        if (!data_2fc540)
            return;
        goto cancel;
    }
    {
        if (data_2ee830 == 1 && !java_call_entity_is_sprinting(c->a, c->b))
            goto allow;
        if (data_2ee850 == 1 && !click_with_helper(c->a, c->b))
            goto allow;
        unsigned char rising = 1, no_ent = 1;
        void *t = java_get_minecraft_5(c->a, c->c18);
        if (t) {
            switch (java_get_moving_object_position_3(c->a, t)) {
            case 2:
                no_ent = java_get_moving_object_position_2(c->a, t) == nullptr;
                break;
            case 1:
                goto allow;
            case 0:
                if (random_chance(data_2ee730))
                    goto do_cancel;
                break;
            }
        }
        unsigned limit = rand_round(data_2ee768);
        unsigned char ground = java_get_entity_on_ground(c->a, c->b);
        if (ground)
            rising = java_get_entity_motion_x(c->a, c->b).y > 0.0;
        rising &= *((unsigned char *)c->p38 + 0x18);
        if (data_2ee8a0 == 1) {
            unsigned char early = (data_2fc544 - 9 < (unsigned)-7) | data_2ee8c8;
            unsigned char falling;
            if (ground) {
                falling = 0;
            } else {
                falling = java_get_entity_fall_distance(c->a, c->b) > 0.0f;
                if (falling & early)
                    goto allow;
            }
            if (rising && (!early || data_2ee8e8))
                goto allow;
            Vec3 eye = java_get_entity_pos_x(c->a, c->b);
            if (java_variant(c->a))
                eye = vec3_add_eye_height_sneak(eye);
            double ey = eye.y;
            int x = (int)libc_floor(eye.x);
            int y = (int)libc_floor(ey);
            int z = (int)libc_floor(eye.z);
            if (get_block_id(c->a, java_call_variant(c->a, c->p20, x, y, z)) != 0)
                goto allow;
            if (ground | falling)
                goto check;
            if (data_2ee908 != 1 || java_get_entity_living_base_hurt_time(c->a, c->b) != 0)
                goto do_cancel;
            goto check;
        }
        if (data_2ee8a0 != 0 || rising)
            goto allow;
        if (data_2fc544 < 15) {
            data_2fc548 = 0;
        } else if (!no_ent) {
            unsigned s = data_2fc548 + 1;
            data_2fc548 = s;
            if (s <= rand_round(data_2ee7a0))
                goto do_cancel;
        }
        if ((unsigned)data_2fc53c <= 9)
            limit += rand_round(data_2ee7d8);
        if (data_2fc54c == 1) {
            data_2fc54c = 0;
            goto allow;
        }
check:
        if (limit <= data_2fc544 + 1)
            goto allow;
        unsigned char r = random_chance(data_2ee810);
        data_2fc540 = r;
        data_2fc538 = 1;
        if (r)
            goto cancel;
        return;
    }
do_cancel:
    data_2fc540 = 1;
    data_2fc538 = 1;
cancel:
    h->cancel = 1;
    if (data_2ee928 == 1 && c->b && !java_call_entity_player_is_using_item(c->a, c->b))
        java_jni_call_nonvirtual_void_method_entity_living_base_swing_item(c->a, c->b);
    return;
allow:
    data_2fc540 = 0;
    data_2fc538 = 1;
}

extern "C" void hit_select_part_3(Hook *h) {
    Ctx *c = h->ctx;
    void *tgt = ((void **)c->p38)[1];
    if (data_2ee8c8 == 1) {
        if (tgt && java_get_entity_living_base_hurt_time(c->a, tgt) == 10) {
            data_2fc544 = 0;
            data_2fc54c = 1;
            return;
        }
        data_2fc544 += 1;
        return;
    }
    int id = tgt ? java_get_entity_entity_id(c->a, tgt) : 0;
    if (id == data_2fc550) {
        unsigned v = data_2fc544 + 1;
        data_2fc544 = v;
        if (v < 10)
            return;
    } else {
        data_2fc550 = id;
        data_2fc544 = 1001;
    }
    if (java_get_entity_ticks_existed(c->a, c->b) == java_get_entity_living_base_last_attacker_time(c->a, c->b))
        data_2fc544 = 0;
}
