// Bridge Assist - sneaks at block edges while bridging backwards.
//   0x2de50 disable: release the sneak key if we were holding it
//   0x2e5a0 tick:    keep the module's keybind state in sync
//   0x2e260 tick:    hold / release sneak
//   0x2dec0 place:   allow placing only when aimed at the edge below us
//   0x2e7b0 helper:  is the ground under the next position air?
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
struct Range { float lo, hi; };
struct Box { double x0, y0, z0, x1, y1, z1; };
struct PlaceEvt { char pad0[8]; Ctx *ctx; unsigned char cancelled; unsigned char allow; };
typedef double v2d __attribute__((vector_size(16), aligned(16)));

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" char data_2e9ac0[];           // Bridge Assist module descriptor
extern "C" int data_2e9d78;              // activation mode
extern "C" unsigned char data_2f7dc8;    // sneak held by us
extern "C" unsigned char data_2f7dd1;
extern "C" unsigned char data_2f7dd0;    // restrict placing to the edge
extern "C" int data_2f7dcc;              // cooldown after sneaking ticks
extern "C" int data_2f7dd8;              // ticks left to hold
extern "C" float data_2f7dd4;            // release timer
extern "C" char data_2f7de0[], data_2f7df0[], data_2f7e00[];   // RNG states
extern "C" unsigned char data_2e9ce8;    // only while sneak held
extern "C" unsigned char data_2e9d08;    // only when looking down
extern "C" unsigned char data_2e9d28;    // only while not rising
extern "C" unsigned char data_2e9da0;    // only for a few ticks after landing
extern "C" unsigned char data_2e9dc0;    // also in the air while falling
extern "C" unsigned char data_2e9e18;    // randomise
extern "C" unsigned char data_2e9e38;    // edge-only placing
extern "C" float data_2e9df8;            // edge margin
extern "C" float data_2e9e70;            // release delay
extern "C" const double rdata_150760, rdata_1507a0, rdata_1507a8, rdata_1507b0, rdata_1507b8, rdata_1507d0, rdata_1507e0,
                        rdata_1507f0, rdata_1507f8, rdata_150808;
extern "C" const float rdata_150780, rdata_150784, rdata_1507c0, rdata_1507c4, rdata_1507c8, rdata_1507cc,
                       rdata_1507d8, rdata_1507dc, rdata_1507e8, rdata_150800, rdata_150810, rdata_150814;
extern "C" const v2d rdata_150790;       // abs mask (double)
extern "C" const v2d rdata_150770;       // abs mask (float)

FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_3, (void *a, void *x));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(void, track_owner, (void *ctx, void *mod, unsigned char toggle));
FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *hit));
FN(unsigned, java_get_variant, (void *a, void *hit));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *gs));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *gs));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *gs));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *gs));
FN(unsigned char, is_key_down, (void *a, void *k));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(unsigned char, block_entity_id, (void *a, void *x));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(double, libc_fmod, (double x, double y));
FN(double, libc_floor, (double x));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height_sneak, (Vec3 v));
FN(void *, java_get_moving_object_position, (void *a, void *hit));
FN(Vec3, java_get_vec3_x_coord, (void *a, void *bp));
FN(Vec3, java_get_variant_2, (void *a, void *hit));
FN(float, java_get_entity_living_base_move_forward, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(void *, java_get_entity, (void *a, void *p));
FN(Box, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(float, java_get_entity_living_base_land_movement_factor, (void *a, void *p));
FN(Vec3, horizontal_motion, (Rot r, float fwd, float strafe, float speed));
FN(void *, java_call_variant, (void *a, void *world, int x, int y, int z));
FN(int, get_block_id, (void *a, void *block));
FN(Range, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, Range r, double d));
FN(unsigned char, walk_should_change, (void *st, Range r, double d));
FN(float, random_rotation, (Range r));

static inline __attribute__((always_inline)) double dabs(double x) {
    x = __builtin_fabs(x);
    return x;
}
static inline __attribute__((always_inline)) float fabs_(float x) {
    x = __builtin_fabsf(x);
    return x;
}

extern "C" void bridge_assist_disable(EvHook *h) {
    if (h->ev == data_2e9ac0 && !h->on && (data_2f7dc8 & 1)) {
        Ctx *c = h->ctx;
        void *k = java_get_game_settings_key_bindings_3(c->a, get_net_handler(c->a, c->c18));
        java_set_key_binding_pressed(c->a, k, java_get_key_binding_key_code(c->a, k));
        data_2f7dc8 = 0;
    }

}

extern "C" void bridge_assist_tick_2(Hook *h) {
    if (data_2f7dd1 == 1) {
        Ctx *c = h->ctx;

        track_owner(c, data_2e9ac0, data_2e9d78 == 2);
        return;
    }

}

extern "C" unsigned char bridge_assist_part(void *a, void *gs, void *world, void *player) {
    Box bb = java_get_axis_aligned_bb_min_x(a, java_get_entity(a, player));
    Vec3 base = vec3_make((bb.x0 + bb.x1) * rdata_1507f0, bb.y0 + rdata_1507f8, (bb.z0 + bb.z1) * rdata_1507f0);
    Vec3 motion = java_get_entity_motion_x(a, player);
    double m = data_2e9df8;
    if (data_2e9e18 == 1) {
        m -= walk_update(data_2f7de0, rot_make_floats(0.0f, rdata_150800), rdata_150808);
        if (0.0 > m)
            m = 0.0;
    }
    void *k0 = java_get_game_settings_key_bindings_4(a, gs), *k1 = java_get_game_settings_key_bindings_5(a, gs), *k2 = java_get_game_settings_key_bindings_6(a, gs), *k3 = java_get_game_settings_key_bindings_7(a, gs);
    float fwd = is_key_down(a, k0) ? rdata_150810 : 0.0f;
    if (is_key_down(a, k1))
        fwd += rdata_150814;
    float str = is_key_down(a, k2) ? rdata_150810 : 0.0f;
    if (is_key_down(a, k3))
        str += rdata_150814;
    Rot r = get_entity_rotation(a, player);
    Vec3 dir = horizontal_motion(r, fwd, str, java_get_entity_living_base_land_movement_factor(a, player));
    Vec3 p = vec3_add(base, vec3_add(motion, dir));
    int x0 = (int)libc_floor(p.x - m);
    int z0 = (int)libc_floor(p.z - m);
    int y = (int)libc_floor(p.y);
    double x1d = libc_floor(p.x + m);
    double z1d = libc_floor(m + p.z);
    if (get_block_id(a, java_call_variant(a, world, x0, y, z0)))
        return 0;
    int z1 = (int)z1d;
    if (z0 != z1 && get_block_id(a, java_call_variant(a, world, x0, y, z1)))
        return 0;
    int x1 = (int)x1d;
    if (x0 == x1)
        return 1;
    if (get_block_id(a, java_call_variant(a, world, x1, y, z0)))
        return 0;
    if (z0 == z1)
        return 1;
    return !get_block_id(a, java_call_variant(a, world, x1, y, z1));
}

extern "C" void bridge_assist_tick(Hook *h) {
    data_2f7dd0 = 0;
    data_2f7dd1 = 0;
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    void *gs = get_net_handler(c->a, c->c18);
    if (data_2f7dcc > 0)
        data_2f7dcc -= 1;
    void *key = java_get_game_settings_key_bindings_3(c->a, gs);
    unsigned char held = java_get_key_binding_key_code(c->a, key);
    if (data_2e9ce8 != held)
        goto release;
    if (data_2e9d08 == 1 && rdata_1507c0 > get_entity_rotation(c->a, c->b).pitch)
        goto release;
    if (data_2e9d28 == 1 && java_get_entity_living_base_move_forward(c->a, c->b) > 0.0f)
        goto release;
    data_2f7dd1 = data_2e9d78 != 0;
    if (data_2e9da0 == 1) {
        int n;
        if (block_entity_id(c->a, item_held_hotbar_slot(c->a, c->b))) {
            n = 2;
        } else {
            n = data_2f7dd8;
            if (n <= 0)
                goto release;
            n -= 1;
        }
        data_2f7dd8 = n;
    }
    data_2f7dc8 = 1;
    if (is_key_down(c->a, key)) {
        if (data_2e9ce8 == 1) {
            if (java_get_entity_living_base_move_forward(c->a, c->b) >= 0.0f || data_2f7dcc <= 0)
                goto keep;
            goto check;
        }
        if (java_get_entity_on_ground(c->a, c->b))
            goto check;
        if (data_2e9dc0 == 1 && !(0.0 > java_get_entity_motion_x(c->a, c->b).y))
            goto check;
        goto timer;
    }
    if (!java_get_entity_on_ground(c->a, c->b)) {
        if (data_2e9dc0 != 1 || 0.0 > java_get_entity_motion_x(c->a, c->b).y)
            return;
    }
    if (!bridge_assist_part(c->a, gs, c->p20, c->b))
        return;
    java_set_key_binding_pressed(c->a, key, 1);
    goto keep;
check:
    if (bridge_assist_part(c->a, gs, c->p20, c->b))
        goto keep;
timer:
    data_2f7dd4 += rdata_1507c4;
    if (data_2f7dd4 > 0.0f)
        goto keep;
    java_set_key_binding_pressed(c->a, key, 0);
    {
        float d = data_2e9e70;
        if (data_2e9e18 == 1) {
            d += walk_update(data_2f7df0, rot_make_floats(rdata_1507c8, rdata_1507cc), rdata_1507d0);
            if (walk_should_change(data_2f7e00, rot_make_floats(rdata_1507d8, rdata_1507dc), rdata_1507e0))
                d += random_rotation(rot_make_floats(0.0f, rdata_1507e8));
        }
        data_2f7dd4 = d + data_2f7dd4;
    }
    return;
keep:
    data_2f7dd0 = data_2e9e38;
    return;
release:
    if (data_2f7dc8 == 1)
        java_set_key_binding_pressed(c->a, key, held);
    data_2f7dc8 = 0;
}

extern "C" void bridge_assist_place(PlaceEvt *h) {
    if (h->cancelled || data_2f7dcc == 0x14)
        return;
    Ctx *c = h->ctx;
    if (data_2f7dd0 == 1) {
        void *hit = java_get_minecraft_5(c->a, c->c18);
        if (hit && java_get_moving_object_position_3(c->a, hit) == 1 && java_get_variant(c->a, hit) >= 2 && java_get_entity_on_ground(c->a, c->b)) {
            void *gs = get_net_handler(c->a, c->c18);
            void *back = java_get_game_settings_key_bindings_5(c->a, gs), *left = java_get_game_settings_key_bindings_6(c->a, gs), *right = java_get_game_settings_key_bindings_7(c->a, gs);
            if (is_key_down(c->a, back) && !is_key_down(c->a, left) && !is_key_down(c->a, right)) {
                float yaw = fabs_((float)libc_fmod(get_entity_rotation(c->a, c->b).yaw, rdata_150760));
                if (!(rdata_150780 > yaw) && !(yaw > rdata_150784)) {
                    Vec3 pos = java_get_entity_pos_x(c->a, c->b);
                    if (java_variant(c->a))
                        pos = vec3_add_eye_height_sneak(pos);
                    Vec3 bp = java_get_vec3_x_coord(c->a, java_get_moving_object_position(c->a, hit));
                    double dy = bp.y - pos.y;
                    if (!(dabs(bp.x - pos.x) > rdata_1507a0) && !(rdata_1507a8 > dy) && !(dy > 0.0) &&
                        !(dabs(bp.z - pos.z) > rdata_1507a0)) {
                        Vec3 hv = java_get_variant_2(c->a, hit);
                        const double one = rdata_1507a0;
                        bool in_z = hv.z < pos.z && pos.z < hv.z + one;
                        bool in_x = hv.x < pos.x && pos.x < hv.x + one;
                        double fx = dabs(libc_fmod(bp.x, one));
                        double fz = libc_fmod(bp.z, one);
                        double edge = in_x && in_z ? rdata_1507b0 : rdata_1507b8;
                        double far = one - edge;
                        bool ok = !(far < fx) && !(fx < edge);
                        if (!ok) {
                            fz = dabs(fz);
                            ok = !(far < fz) && !(fz < edge);
                        }
                        if (ok) {
                            h->allow = 1;
                            return;
                        }
                    }
                }
            }
        }
    }
    if (block_entity_id(c->a, item_held_hotbar_slot(c->a, c->b)))
        data_2f7dcc = 0x14;
}
