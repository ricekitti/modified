// Lag Range - attack only while the target's hurt time is inside the configured range.
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct RenderHook { char pad0[8]; Vec3 cam; char pad20[8]; float partial; };   // +0x08 / +0x28
struct Target { void *pad; void *ent; double dist2; };
struct PktHook { char pad0[8]; void *ctx; void *ev; long long budget; int seq; int verdict; };
struct Range { float lo, hi; };
struct PhaseHook { char pad0[8]; Ctx *ctx; unsigned char phase; };

extern "C" unsigned char data_2fc858;
extern "C" float data_2efc00;            // "line_width"
extern "C" float data_2efc10[3];         // "color"
extern "C" unsigned char data_2efc40;    // "filled"
extern "C" unsigned char data_2fc820;    // have a target position
extern "C" Vec3 data_2fc828;             // target position (previous tick)
extern "C" Vec3 data_2fc840;             // target position (this tick)
extern "C" void (**rdata_150cb0)(float);
extern "C" void (**rdata_150cc0)();
extern "C" void (**rdata_150cb8)();
extern "C" void (**rdata_150cd8)(float, float, float);
extern "C" void (**rdata_150cd0)(float, float, float);
extern "C" void (**rdata_150ca0)(float, float, float);
extern "C" void (**rdata_13e448)(float, float, float, float);
extern "C" const double rdata_152ed8;
extern "C" const float rdata_152ee0, rdata_152ee4, rdata_152ee8;

extern "C" int data_2fc7c0;              // last packet sequence
extern "C" unsigned char data_2fc7c8;    // spin lock
extern "C" long long data_2fc7d0;        // max hold budget
extern "C" unsigned char data_2fc7d8;    // target moved closer
extern "C" unsigned char data_2fc7d9, data_2fc7da, data_2fc7db;
extern "C" Vec3 data_2fc7e0;             // target position when the window started
extern "C" unsigned char data_2fc7f8;    // active
extern "C" Vec3 data_2fc808;             // latest target position from packets
extern "C" unsigned char data_2fc859;    // sprinting last tick
extern "C" int data_2fc85c;              // pause ticks (potion)
extern "C" char data_2fc860[];           // rng state
extern "C" float data_2efc78;            // max hold (ms)
extern "C" unsigned char data_2efc98, data_2efcb8, data_2efcd8, data_2efcf8;
extern "C" const float rdata_152ef0, rdata_152ef4;
extern "C" const double rdata_152ef8;
FN(void *, java_jni_is_instance_of_c03_packet_player, (void *ctx, void *ev));
FN(unsigned char, java_get_c03_packet_player_moving, (void *ctx, void *p));
FN(Vec3, java_get_c03_packet_player_a, (void *ctx, void *p));
FN(void *, java_jni_is_instance_of_c02_packet_use_entity, (void *ctx, void *ev));
FN(void *, java_jni_is_instance_of_c0_bpacket_entity_action, (void *ctx, void *ev));
FN(int, java_c0_bpacket_entity_action, (void *ctx, void *p));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(unsigned char, click_with_helper, (void *a, void *b));
FN(void *, java_get_entity_player_2, (void *a, void *p));
FN(int, java_get_inventory_player_current_item, (void *a, void *p));
FN(void *, java_call_iinventory_get_stack_in_slot, (void *a, void *p, int slot));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(unsigned, java_get_item_stack_metadata, (void *a, void *stack));
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *p));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height_sneak, (Vec3 v));
FN(Range, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, Range r, double p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(void *, get_net_handler, (void *a, void *x));
FN(int, java_get_game_settings_third_person_view, (void *a, void *x));
FN(Vec3, java_get_entity_other_player_mp_other_player_mpx, (void *a, void *e));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(void, gl_array_xyz_float, ());
FN(void, gl_array_color1, ());
FN(void, gl_array_color2, ());
FN(void, enable_vertex_array, ());

FN(int, java_get_entity_ticks_existed, (void *a, void *p));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *p));

extern "C" void lag_range_attack_hook(PhaseHook *h) {
    if (h->phase != 1)
        return;
    Ctx *c = h->ctx;
    int ht = java_get_entity_ticks_existed(c->a, c->b);
    data_2fc858 = ht == java_get_entity_living_base_last_attacker_time(c->a, c->b);
}

// render: box at the (interpolated) server-side target position
extern "C" void lag_range_render_box_interpolated(RenderHook *h) {
    if (data_2fc820 == 1) {
        Vec3 rel = vec3_sub(vec3_lerp(data_2fc828, data_2fc840, (double)h->partial), h->cam);
        gl_array_xyz_float();
        (*rdata_150cb0)(data_2efc00);
        (*rdata_150cc0)();
        (*rdata_150cd8)((float)rel.x, (float)(rel.y + rdata_152ed8), (float)rel.z);
        float w = rdata_152ee4;
        (*rdata_150cd0)(w, rdata_152ee0, w);
        if (data_2efc40 == 1) {
            (*rdata_13e448)(data_2efc10[0], data_2efc10[1], data_2efc10[2], rdata_152ee8);
            gl_array_color1();
        }
        (*rdata_150ca0)(data_2efc10[0], data_2efc10[1], data_2efc10[2]);
        gl_array_color2();
        (*rdata_150cb8)();
        enable_vertex_array();
    }
}

static inline __attribute__((always_inline)) void lock() {
    while (__atomic_exchange_n(&data_2fc7c8, (unsigned char)1, __ATOMIC_SEQ_CST))
        __builtin_ia32_pause();
}

// packet hook: hold outgoing packets while the target walks towards us
extern "C" void lag_range_packet_hook_hold(PktHook *h) {
    lock();
    unsigned char act = data_2fc7f8;
    if (h->seq > data_2fc7c0) {
        if (act) {
            void *p = java_jni_is_instance_of_c03_packet_player(h->ctx, h->ev);
            if (p && java_get_c03_packet_player_moving(h->ctx, p)) {
                Vec3 np = java_get_c03_packet_player_a(h->ctx, p);
                double before = vec3_distance_sq(data_2fc7e0, data_2fc808);
                data_2fc7d8 = before > vec3_distance_sq(data_2fc7e0, np);
            }
        }
        data_2fc7c0 = h->seq;
        act = data_2fc7f8;
    }
    if (act && data_2fc7d8 && !data_2fc7d9 && !data_2fc7db && !data_2fc7da && h->budget < data_2fc7d0) {
        h->verdict = 1;
    } else if (h->verdict == 0) {
        void *p = java_jni_is_instance_of_c03_packet_player(h->ctx, h->ev);
        if (p) {
            data_2fc7db = 0;
            if (java_get_c03_packet_player_moving(h->ctx, p))
                data_2fc808 = java_get_c03_packet_player_a(h->ctx, p);
        } else {
            void *q = java_jni_is_instance_of_c02_packet_use_entity(h->ctx, h->ev);
            if (q) {
                if (data_2fc7d9) {
                    data_2fc7d9 = 0;
                    data_2fc7db = 1;
                }
            } else {
                void *r = java_jni_is_instance_of_c0_bpacket_entity_action(h->ctx, h->ev);
                if (r && data_2fc7da && java_c0_bpacket_entity_action(h->ctx, r) == 4) {
                    data_2fc7da = 0;
                    data_2fc7db = 1;
                }
            }
        }
    }
    data_2fc7c8 = 0;
}

// tick: decide whether a lag window may be opened for the current target
extern "C" void lag_range_tick_decide_whether(Hook *h) {
    unsigned char hit_equal = data_2fc858;
    data_2fc858 = 0;
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        goto off;
    if (data_2efc98 == 1 && !click_with_helper(c->a, c->b))
        goto off;
    if (data_2efcb8 == 1) {
        void *inv = java_get_entity_player_2(c->a, c->b);
        void *st = java_call_iinventory_get_stack_in_slot(c->a, inv, java_get_inventory_player_current_item(c->a, inv));
        if (st) {
            void *held = item_held_hotbar_slot(c->a, c->b);
            if (held && java_call_static_item_get_id_from_item(c->a, held) == 0x175 && (java_get_item_stack_metadata(c->a, st) & 0x4000)) {
                data_2fc85c = 15;
                goto off;
            }
        }
        int n = data_2fc85c;
        if (n > 0) {
            data_2fc85c = n - 1;
            goto off;
        }
    }
    {
        unsigned char sprint = java_call_entity_is_sprinting(c->a, c->b);
        unsigned char stopped = (data_2fc859 & 1) ? data_2efcd8 : 0;
        if (sprint)
            stopped = 0;
        data_2fc859 = sprint;
        Vec3 eye = java_get_entity_pos_x(c->a, c->b);
        if (java_variant(c->a))
            eye = vec3_add_eye_height_sneak(eye);
        (void)eye;
        Target *t = (Target *)c->p38;
        if (!t->ent)
            goto off;
        double d2 = t->dist2;
        double r = walk_update(data_2fc860, rot_make_floats(rdata_152ef0, rdata_152ef4), rdata_152ef8);
        if (d2 > r * r)
            goto off;
        unsigned char low_hurt = java_get_entity_living_base_hurt_time(c->a, t->ent) < 5;
        unsigned char moving = 0;
        if (data_2efcf8 == 1)
            moving = java_get_game_settings_third_person_view(c->a, get_net_handler(c->a, c->c18)) != 0;
        hit_equal &= low_hurt;
        Vec3 tp = java_get_entity_other_player_mp_other_player_mpx(c->a, t->ent);
        lock();
        data_2fc7d0 = (long long)data_2efc78;
        data_2fc7d9 = hit_equal;
        data_2fc7da = stopped;
        data_2fc7f8 = 1;
        data_2fc7e0 = tp;
        data_2fc828 = data_2fc820 == 1 ? data_2fc840 : data_2fc808;
        data_2fc840 = data_2fc808;
        data_2fc820 = moving ? data_2fc7d8 : 0;
        data_2fc7c8 = 0;
        return;
    }
off:
    lock();
    data_2fc7db = 0;
    data_2fc7d9 = 0;
    data_2fc7da = 0;
    data_2fc7f8 = 0;
    data_2fc7c8 = 0;
    data_2fc820 = 0;
}
