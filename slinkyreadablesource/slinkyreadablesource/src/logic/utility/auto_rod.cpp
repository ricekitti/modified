// Auto Rod - cast and reel the fishing rod automatically.
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct AABB { Vec3 lo, hi; };
struct Target { void *pad; void *ent; double dist2; };
struct InHook { char pad0[8]; void *mod; unsigned char use; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct AttackHook { char pad0[8]; Ctx *ctx; unsigned char phase; unsigned char cancel; };

extern "C" char data_2f3520[];           // Auto Rod module descriptor
extern "C" unsigned char data_2f3540;
extern "C" unsigned char data_304400;    // input taken
extern "C" int data_304404;              // state
extern "C" unsigned char data_304415;    // rod swing seen

extern "C" float data_2f37c8;            // throw delay
extern "C" unsigned char data_2f37e8;    // randomise
extern "C" float data_2f3858;            // pull speed
extern "C" char data_304418[], data_304428[], data_304438[], data_304448[];   // rng states
extern "C" float rdata_154ab8, rdata_154ac8, rdata_154acc, rdata_154ad8, rdata_154af8,
                 rdata_154afc, rdata_154b00, rdata_154b10, rdata_154b14, rdata_154b20;
extern "C" double rdata_154ac0, rdata_154ad0, rdata_154b08, rdata_154b18;

extern "C" int data_2f3730;              // slot to switch back to (-1 none)
extern "C" unsigned char data_2f3750;    // abort when aiming at an entity
extern "C" unsigned char data_2f3770;    // prefer snowballs / eggs
extern "C" unsigned char data_2f3790;    // switch to best weapon afterwards
extern "C" float data_2f3820;            // projectile speed
extern "C" int data_304408;              // rod slot
extern "C" float data_30440c;            // step delay
extern "C" int data_304410;              // wait ticks
extern "C" unsigned char data_304414;    // using a fishing rod
extern "C" unsigned char data_304415;
extern "C" const float rdata_154a98;     // -1.0
extern "C" const double rdata_154ae0, rdata_154ae8;
extern "C" const float rdata_154af0;
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(void, java_set_inventory_player_current_item, (void *a, void *p, int slot));
FN(int, java_get_inventory_player_current_item, (void *a, void *p));
FN(void, release_owner, (void *timer));
FN(unsigned char, claim_release_hotbar_module, (void *timer));
FN(void, queue_module_toggle, (void *a, void *mod, int x));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *p, int slot));
FN(void *, java_call_item_stack_get_item, (void *a, void *stack));
FN(int, weapons_part_3, (Ctx *c, void *p));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(void, java_set_key_binding_press_time, (void *a, void *k, int n));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height, (Vec3 v));
FN(Vec3, java_get_entity_other_player_mp_other_player_mpx, (void *a, void *e));
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(AABB, aabb_make, (Vec3 lo, Vec3 hi));
FN(Vec3, aabb_clamp_point, (AABB box, Vec3 p));
FN(double, vec3_distance, (Vec3 a, Vec3 b));
FN(float, libc_ceilf, (float v));
FN(void *, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, void *rng, double p));
FN(unsigned char, walk_should_change, (void *st, void *rng, double p));
FN(float, random_rotation, (void *rng));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));

extern "C" void auto_rod_input_hook_0x44390(InHook *h) {
    if (__builtin_expect(data_304400 != 0, 0) || h->mod != data_2f3520)
        return;
    if (data_2f3540 == 1) {
        data_304400 = 1;
    } else {
        int st = data_304404;
        if (st == 6)
            return;
        if (st == 3)
            data_304404 = 7;
    }

    h->use = 1;
}

extern "C" void auto_rod_part_2(EvHook *h) {
    if (h->ev != data_2f3520 || h->on != 1)
        return;
    data_304404 = 0;
}

extern "C" void auto_rod_part_3(AttackHook *h) {
    if (__builtin_expect(h->phase == 0, 1)) {
        Ctx *c = h->ctx;
        void *held = item_held_hotbar_slot(c->a, c->b);
        if (held && java_call_static_item_get_id_from_item(c->a, held) == 0x15a) {
            if (data_304415)
                h->cancel = 1;
            else
                data_304415 = 1;
        }
    }

}

// randomised throw delay
extern "C" float auto_rod_randomised_throw_delay() {
    float d = data_2f37c8;
    if (data_2f37e8 == 1) {
        void *rng = rot_make_floats(0.0f, rdata_154ab8);
        char *st = data_304418;

        float m = d - walk_update(st, rng, rdata_154ac0);
        d = 0.0f;
        d = d > m ? d : m;
        float lo = rdata_154ac8;

        rng = rot_make_floats(lo, rdata_154acc);
        st = data_304428;

        if (walk_should_change(st, rng, rdata_154ad0)) {
            lo = rdata_154ab8;

            d += random_rotation(rot_make_floats(lo, rdata_154ad8));
        }
    }
    return d;
}

// randomised rod pull interval
extern "C" float auto_rod_randomised_rod_pull() {
    float base = data_2f3858;
    float v;
    if (data_2f37e8) {
        float lo = rdata_154afc;

        void *rng = rot_make_floats(lo, rdata_154b00);
        char *st = data_304438;

        float r = walk_update(st, rng, rdata_154b08);
        float br = base * r;

        v = rdata_154af8 / br;
        lo = rdata_154b10;

        rng = rot_make_floats(lo, rdata_154b14);
        st = data_304448;

        if (walk_should_change(st, rng, rdata_154b18)) {
            lo = rdata_154b00;

            v += random_rotation(rot_make_floats(lo, rdata_154b20));
        }
    } else {
        v = rdata_154af8 / base;
    }
    return v;
}

// ticks the bobber needs to reach the target (distance to its hitbox / speed)
extern "C" int auto_rod_ticks_bobber_needs(Ctx *c) {
    void *ent = ((Target *)c->p38)->ent;
    float t;
    if (!ent) {
        t = rdata_154af0 / data_2f3820;
    } else {
        Vec3 eye = java_get_entity_pos_x(c->a, c->b);
        if (!java_variant(c->a))
            eye = vec3_add_eye_height(eye);
        Vec3 pos = java_get_entity_other_player_mp_other_player_mpx(c->a, ent);
        double k = rdata_154ae0;
        Vec3 lo = vec3_sub(pos, vec3_make(k, 0.0, k));
        Vec3 hi = vec3_add(pos, vec3_make(k, rdata_154ae8, k));
        Vec3 hit = aabb_clamp_point(aabb_make(lo, hi), eye);
        t = (float)(vec3_distance(eye, hit) / (double)data_2f3820);
    }
    return (int)libc_ceilf(t);
}

static inline __attribute__((always_inline)) void restore_slot(Ctx *c) {
    if (data_2f3730 != -1) {
        java_set_inventory_player_current_item(c->a, java_get_entity_player_2(c->a, c->b), data_2f3730);
        data_2f3730 = -1;
    }
}

// main tick: 0 find rod -> 1 switch -> 2 throw -> 3 wait -> 4/5 switch back / reel -> 6 done, 7 cancel
extern "C" float auto_rod_randomised_throw_delay();
extern "C" float auto_rod_randomised_rod_pull();
extern "C" void auto_rod_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    if (data_304400) {
        restore_slot(c);
        release_owner(data_2f3520);
        queue_module_toggle(c->p08, data_2f3520, 0);
        data_304400 = 0;
        return;
    }
    for (int iter = 0; iter < 3; iter++) {
        if (data_2f3750 == 1) {
            void *t = java_get_minecraft_5(c->a, c->c18);
            if (t && java_get_moving_object_position_3(c->a, t) == 2) {
                restore_slot(c);
                release_owner(data_2f3520);
                data_304404 = 6;
                goto finished;
            }
        }
        if (data_304404 == 7) {
            restore_slot(c);
            release_owner(data_2f3520);
            data_304404 = 0;
            return;
        }
        if (data_304410)
            data_304410 -= 1;
        float d = data_30440c;
        if (d > 0.0f) {
            d += rdata_154a98;
            data_30440c = d;
            if (d > 0.0f)
                goto done;
        }
        void *inv = java_get_entity_player_2(c->a, c->b);
        int cur = java_get_inventory_player_current_item(c->a, inv);
        unsigned st = data_304404;
        int rs = data_304408;
        if (st >= 2 && cur != rs) {
            release_owner(data_2f3520);
            data_304404 = 6;
            goto finished;
        }
        int found = -1;
        switch (st) {
        case 0:
        case 2: {
            data_304414 = 0;
            bool pref = false;
            for (int i = 0; i < 9; i++) {
                void *it = java_call_iinventory_get_stack_in_slot(c->a, inv, i);
                if (!it)
                    continue;
                void *item = java_call_item_stack_get_item(c->a, it);
                if (!item)
                    continue;
                int id = java_call_static_item_get_id_from_item(c->a, item);
                if (id == 0x14c || id == 0x158) {
                    if (data_2f3770 == 1) {
                        data_304414 = 0;
                        found = i;
                        pref = true;
                        break;
                    }
                    continue;
                }
                if (id != 0x15a)
                    continue;
                data_304414 = 1;
                found = i;
                if (!data_2f3770) {
                    pref = true;
                    break;
                }
            }
            if (st == 0) {
                if (found == -1 || !claim_release_hotbar_module(data_2f3520)) {
                    data_304404 = 6;
                    goto finished;
                }
                data_304408 = found;
                data_2f3730 = cur;
                if (data_2f3790 == 1) {
                    int b = weapons_part_3(c, inv);
                    if (b != -1)
                        data_2f3730 = b;
                    cur = data_2f3730;
                }
                data_304404 = (data_304408 == cur) + 1;
                continue;
            }
            (void)pref;
            if (found == -1) {
                data_304404 = 5;
                return;
            }
            if (found == cur)
                goto throw_;
            data_304408 = found;
            data_304404 = 1;
            data_30440c += auto_rod_randomised_throw_delay();
            continue;
        }
        case 1:
            java_set_inventory_player_current_item(c->a, inv, rs);
            data_304404 = 2;
            data_304415 = 0;
            data_30440c += auto_rod_randomised_throw_delay();
            continue;
        case 3:
            if (data_304410 != 0)
                goto done;
            data_304404 = data_2f3730 == rs ? 4 : 5;
            data_30440c += auto_rod_randomised_throw_delay();
            continue;
        case 4:
            java_set_inventory_player_current_item(c->a, inv, (data_2f3730 + 1) % 9);
            data_304404 = 5;
            return;
        case 5:
            data_304404 = 6;
            restore_slot(c);
            release_owner(data_2f3520);
            data_30440c += auto_rod_randomised_throw_delay();
            goto done;
        default:
            goto done;
        }
throw_:
        java_set_key_binding_press_time(c->a, java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18)), 1);
        if (data_304414 == 1) {
            data_304404 = 3;
            data_304410 = auto_rod_ticks_bobber_needs(c);
            data_30440c += auto_rod_randomised_throw_delay();
        } else if (data_2f3540 == 1) {
            data_30440c += auto_rod_randomised_rod_pull();
        } else {
            data_304404 = 5;
            data_30440c += auto_rod_randomised_throw_delay();
        }
        goto done;
    }
done:
    if (data_304404 != 6)
        return;
finished:
    if (data_2f3540 == 1) {
        data_304404 = 0;
        return;
    }
    queue_module_toggle(c->p08, data_2f3520, 0);
}
