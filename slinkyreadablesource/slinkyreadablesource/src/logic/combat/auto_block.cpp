// Auto Block - hold the use (block) key with a sword while fighting, with
// optional "lag" (packet hold) windows and a client-side fake block animation.
//   +0x48 tick, +0x78 packet hook, +0x98 disable, +0x128 attack hook, +0x168 render
#include "slinky/engine.h"

struct AttackHook { char pad0[8]; Ctx *ctx; unsigned char phase; unsigned char cancel; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct PktHook { char pad0[0x24]; int verdict; };
struct Target { void *pad; void *ent; double dist2; };
struct Mouse { unsigned char pad, lmb, rmb; };

extern "C" float data_2eb238;            // "max_hold_time"
extern "C" float data_2eb270;            // "lag_chance"
extern "C" float data_2eb2a8;            // "lag_duration"
extern "C" float data_2eb2e0;            // "max_hurt_time"
extern "C" unsigned char data_2eb300;    // "require_left_mouse_pressed"
extern "C" unsigned char data_2eb320;    // "require_right_mouse_pressed"
extern "C" unsigned char data_2eb340;    // "require_damage"
extern "C" unsigned char data_2eb360;    // "fake_block_animation"
extern "C" unsigned char data_2eb380;    // "fake_block_animation_require_target"
extern "C" float data_2eb3b8;            // "range"
extern "C" unsigned char data_2eb3d8;    // "lag_flush_attack"
extern "C" unsigned char data_2eb3f8;    // "lag_reblock"
extern "C" char data_2eaff8[];           // Auto Block module descriptor
extern "C" int data_2f9e00;              // re-block cooldown
extern "C" int data_2f9e04;              // ticks held
extern "C" unsigned data_2f9e08;         // ticks since target hit
extern "C" int data_2f9e0c;              // last target id
extern "C" unsigned char data_2f9e10;    // fake animation this tick
extern "C" unsigned char data_2f9e11;    // animation started
extern "C" unsigned char data_2f9e12;    // right mouse required
extern "C" int data_2f9e14;              // lag ticks left (atomic)
extern "C" unsigned char data_2f9e18;    // blocking
extern "C" unsigned char data_2f9e19;    // use key forced
extern "C" int data_2f9e1c;              // animation counter

FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(void, java_set_entity_player_item_in_use_count, (void *a, void *p, int n));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(unsigned char, item_sword, (void *a, void *item));
FN(unsigned char, pending_action_flag, ());
FN(unsigned char, random_chance, (float chance));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));
FN(int, java_get_entity_entity_id, (void *a, void *p));

#define AXCHG(x, v) __atomic_exchange_n(&(x), (v), __ATOMIC_SEQ_CST)
#define ALOAD(x) __atomic_load_n(&(x), __ATOMIC_SEQ_CST)

static inline __attribute__((always_inline)) int rand_round(float v) {
    int whole = (int)v;
    return random_chance(v - (float)(int)v) + whole;
}

static inline __attribute__((always_inline)) void *use_key(Ctx *c) {
    return java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18));
}

extern "C" void auto_block_part(Hook *h) {
    data_2f9e10 = 0;
    Ctx *c = h->ctx;
    if (data_2f9e11 == 1) {
        if (c->b && !java_call_entity_player_is_using_item(c->a, c->b))
            java_set_entity_player_item_in_use_count(c->a, c->b, 0);
        data_2f9e11 = 0;
    }
    if (data_2f9e00 != 0)
        data_2f9e00 -= 1;
    if (!c->f40) {
        if (data_2f9e19 == 1) {
            void *k = use_key(c);
            java_set_key_binding_pressed(c->a, k, java_get_key_binding_key_code(c->a, k));
            data_2f9e19 = 0;
        }
        data_2f9e12 = 0;
        goto stop_lag;
    }
    {
        int ht = java_get_entity_living_base_hurt_time(c->a, c->b);
        void *a = c->a;
        unsigned char sword = item_sword(a, item_held_hotbar_slot(a, c->b));
        data_2f9e12 = 0;
        unsigned char want = 0, forced = 0, fake = 0;
        if (!sword) {
            if (data_2f9e19 != 1)
                goto publish;
            goto release;
        }
        if (data_2eb300 == 1 && !((Mouse *)c->p30)->lmb)
            goto release;
        if (pending_action_flag())
            goto release;
        {
            unsigned char rmb = data_2eb320;
            data_2f9e12 = rmb;
            if (rmb != ((Mouse *)c->p30)->rmb)
                goto release;
        }
        want = 1;
        if (ht == 0 && data_2eb340)
            goto release;
        if (ht > rand_round(data_2eb2e0))
            goto release;
        {
            unsigned char blocking = data_2f9e18;
            void *k = use_key(c);
            java_set_key_binding_pressed(c->a, k, blocking);
            forced = 1;
            if (blocking == 1)
                java_get_key_binding_press_time_2(c->a, k);
            want = 1;
            goto forced_set;
        }
release:
        {
            void *k = use_key(c);
            void *a3 = c->a;
            java_set_key_binding_pressed(a3, k, data_2f9e12 ? 0 : java_get_key_binding_key_code(a3, k));
            forced = 0;
        }
forced_set:
        data_2f9e19 = forced;
        if (want && data_2eb360) {
            fake = 1;
            if (data_2eb380 == 1) {
                Target *t = (Target *)c->p38;
                fake = t->ent ? (double)(data_2eb3b8 * data_2eb3b8) > t->dist2 : 0;
            }
        }
publish:
        data_2f9e10 = fake;
        if (ht == 10)
            data_2f9e00 = 0;
        unsigned ticks = data_2f9e08 + 1;
        data_2f9e08 = ticks;
        unsigned char fresh = 0;
        if (ticks >= 10) {
            fresh = java_get_entity_ticks_existed(c->a, c->b) == java_get_entity_living_base_last_attacker_time(c->a, c->b) + 1;
            if (fresh)
                data_2f9e08 = 0;
        }
        void *te = ((Target *)c->p38)->ent;
        int id = te ? java_get_entity_entity_id(c->a, te) : 0;
        if (id != data_2f9e0c) {
            data_2f9e0c = id;
            data_2f9e08 = 0;
            fresh = 1;
        }
        if (ALOAD(data_2f9e14) != 0) {
            if ((fresh & data_2eb3d8) == 1) {
                AXCHG(data_2f9e14, 0);
                if (data_2eb3f8 != 1)
                    goto hold;
            } else {
                __atomic_fetch_sub(&data_2f9e14, 1, __ATOMIC_SEQ_CST);
                if (data_2eb3f8 != 1)
                    goto hold;
            }
            if (ALOAD(data_2f9e14) == 0) {
                data_2f9e00 = 0;
            }
        }
hold:
        if (data_2f9e18) {
            int s = data_2f9e04 + 1;
            float mx = data_2eb238;
            int whole = (int)mx;

            data_2f9e04 = s;
            int lim = random_chance(mx - (float)(int)mx) + whole;
            unsigned char under = s < lim;

            if (forced & under)
                return;
            java_set_key_binding_pressed(c->a, use_key(c), 0);
            data_2f9e19 = 1;
            if (random_chance(data_2eb270))
                AXCHG(data_2f9e14, rand_round(data_2eb2a8));
            data_2f9e18 = 0;
            return;
        }
        if (!(forced & (data_2f9e00 == 0)))
            return;
        Target *t = (Target *)c->p38;
        if (!t->ent || !((double)(data_2eb3b8 * data_2eb3b8) > t->dist2))
            return;
        void *k = use_key(c);
        java_set_key_binding_pressed(c->a, k, 1);
        java_get_key_binding_press_time_2(c->a, k);
        data_2f9e19 = 1;
        data_2f9e18 = 1;
        data_2f9e00 = 10;
        data_2f9e04 = 0;
    }
stop_lag:
    AXCHG(data_2f9e14, 0);
}

extern "C" void auto_block_part_2(PktHook *h) {
    if (ALOAD(data_2f9e14) != 0)
        h->verdict = 1;
}

extern "C" void auto_block_part_3(EvHook *h) {
    if (h->ev == data_2eaff8 && !h->on && (data_2f9e18 & 1)) {
        if (data_2f9e19 == 1) {
            Ctx *c = h->ctx;
            void *k = use_key(c);
            java_set_key_binding_pressed(c->a, k, java_get_key_binding_key_code(c->a, k));
            data_2f9e19 = 0;
        }
        data_2f9e18 = 0;
        AXCHG(data_2f9e14, 0);
    }

}

extern "C" void auto_block_part_4(AttackHook *h) {
    if (h->phase == 0) {
        void *a = h->ctx->a;
        if (!item_sword(a, item_held_hotbar_slot(a, h->ctx->b)))
            AXCHG(data_2f9e14, 0);

        if (data_2f9e12 == 1 && !(data_2f9e18 & 1))
            h->cancel = 1;
    }

}

extern "C" void auto_block_part_5(Hook *h) {
    if (data_2f9e10) {
        Ctx *c = h->ctx;
        if (c->b) {
            data_2f9e11 = 1;
            int n = data_2f9e1c - 1;
            data_2f9e1c = n;
            java_set_entity_player_item_in_use_count(c->a, c->b, n);
        }
        return;
    }
    data_2f9e1c = 0x11940;
}
