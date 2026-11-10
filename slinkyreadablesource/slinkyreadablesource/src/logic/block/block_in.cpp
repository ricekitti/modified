// Block In - surrounds the player with blocks (roof first, then the sides).
//   0x4abf0 (no-op hook)   0x4ad50 rotation override for the placement packet
//   0x4ae50 tick: pick the next free spot, aim at it and place
#include "slinky/engine.h"

typedef float v4f __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct Box { Vec3 lo, hi; };
struct Range { float lo, hi; };
struct Pair { float x, y; };
struct Offset3 { signed char dx, dy, dz; };
struct Offset2 { signed char dx, dz; };

struct Rot { float yaw, pitch; };
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct RotHook { char pad0[8]; unsigned char override_yaw, override_pitch; char pad0a[2]; Rot rot; };   // +0x08 / +0x0c

extern "C" unsigned char data_304b1c;    // active
extern "C" int data_304b20;              // rotation mode
extern "C" Rot data_304b28;              // placement rotation

extern "C" char data_2f6108[];           // Block In module descriptor
extern "C" void *data_304b10;
extern "C" int data_304b18;              // facing quadrant (0..3)
extern "C" const float rdata_156870;     // 90.0
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(float, libc_roundf, (float v));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_4, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_5, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_6, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings_7, (void *a, void *x));
FN(void, java_set_key_binding_pressed, (void *a, void *k, unsigned char on));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height_sneak, (Vec3 v));
FN(double, libc_floor, (double x));
FN(double, libc_fmod, (double x, double y));
FN(void *, java_block_pos_init, (void *a, void *x, int y, int z));
FN(void *, java_call_id_328, (void *a, void *world, void *pos, int x, int y, int z));
FN(int, get_block_id, (void *a, void *block));
FN(void *, java_get_entity, (void *a, void *p));
FN(Box, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(Box, aabb_make, (Vec3 lo, Vec3 hi));
FN(unsigned char, aabb_intersects, (Box a, Box b));
FN(Range, rot_make_floats, (float lo, float hi));
FN(float, walk_update, (void *st, Range r, double d));
FN(unsigned char, increment_wrap, (int ms));
FN(void, switch_best_hotbar_slot, (Ctx *c, void *mod, unsigned char on, void (*cb)(void *)));
FN(void, key_category, (void *));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(unsigned char, block_entity_id, (void *a, void *x));
FN(Rot, get_render_state, ());
FN(Rot, rot_add, (Rot a, Rot b));
FN(void *, java_call_entity_living_base_get_position, (void *a, void *p));
FN(Vec3, java_get_vec3_x_coord, (void *a, void *bp));
FN(Rot, rot_look_at, (Vec3 from, Vec3 to));
FN(Pair, rot_sub, (Rot a, Rot b));
FN(float, wrap_angle, (float deg));
FN(Pair, rot_scale, (Pair p, float k));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(Pair, rot_lerp, (Pair a, Pair b, float k));
FN(void, apply_silent_rotation, (Pair r));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *gs));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(void, queue_module_toggle, (void *mods, void *mod, unsigned char on));

extern "C" unsigned char data_2f6330;    // require ground
extern "C" float data_2f6368;            // aim speed
extern "C" double data_304b30;           // last y
extern "C" char data_304b38[], data_304b48[], data_304b58[], data_304b68[], data_304b78[], data_304b88[];   // RNG states
extern "C" const Offset3 rdata_156b80[10];   // roof spots, relative to the block below the feet
extern "C" const Offset2 rdata_156b9e[4];    // side directions
extern "C" const v4f rdata_156930;           // abs mask (float)
extern "C" const v4f rdata_156890;           // abs mask (double)
extern "C" const float rdata_1568a8, rdata_1568ac, rdata_1568b0, rdata_1568b4, rdata_1568d8, rdata_1568dc, rdata_1568e0,
                       rdata_1568e4, rdata_1568e8, rdata_1568ec, rdata_1568f0, rdata_1568f4, rdata_156900, rdata_156920, rdata_156940;
extern "C" const double rdata_156880, rdata_1568a0, rdata_1568b8, rdata_1568c0, rdata_1568c8, rdata_1568d0, rdata_1568f8;

extern "C" void block_in_part(void *) {
}

extern "C" void block_in_rotation_override_placement(RotHook *h) {
    if (data_304b1c != 1)
        return;
    *(unsigned short *)&h->override_yaw = 0x101;
    if (data_304b20 == 1)
        h->rot = data_304b28;
}

// tick while placing: hold forward only in mode 1, release back / left / right
extern "C" void block_in_tick_while_placing(Hook *h) {
    if (data_304b20 != 0) {
        Ctx *c = h->ctx;
        void *obj = get_net_handler(c->a, c->c18);
        void *fwd = java_get_game_settings_key_bindings_4(c->a, obj);
        void *back = java_get_game_settings_key_bindings_5(c->a, obj);
        void *left = java_get_game_settings_key_bindings_6(c->a, obj);
        void *right = java_get_game_settings_key_bindings_7(c->a, obj);
        void *a = c->a;

        java_set_key_binding_pressed(a, fwd, data_304b20 == 1);
        java_set_key_binding_pressed(c->a, back, 0);
        java_set_key_binding_pressed(c->a, left, 0);
        java_set_key_binding_pressed(c->a, right, 0);
        return;
    }

}

// air, water and tall grass can be built into
static inline __attribute__((always_inline)) bool open_at(Ctx *c, void *pos, int x, int y, int z) {
    unsigned id = get_block_id(c->a, java_call_id_328(c->a, c->p20, pos, x, y, z));
    return id <= 0x1f && (0x80000301u >> id & 1);
}

static inline __attribute__((always_inline)) double fabs_(double x) {
    x = __builtin_fabs(x);
    return x;
}

extern "C" void block_in_tick(Hook *h) {
    unsigned long long &placed = *(unsigned long long *)&data_304b10;
    data_304b1c = 0;
    data_304b20 = 0;
    Ctx *c = h->ctx;
    Vec3 me, target;
    void *pos;
    int bx, by, bz;
    unsigned char bit;
    unsigned char ground;
    if (c->f40 != 1)
        goto off;
    ground = java_get_entity_on_ground(c->a, c->b);
    if (data_2f6330 && !ground)
        goto off;
    me = java_get_entity_pos_x(c->a, c->b);
    if (java_variant(c->a))
        me = vec3_add_eye_height_sneak(me);
    bx = (int)libc_floor(me.x);
    by = (int)(libc_floor(me.y) + rdata_156880);
    bz = (int)libc_floor(me.z);
    if (fabs_(me.y - data_304b30) > rdata_1568a0)
        placed = 0;
    data_304b30 = me.y;
    pos = **(int **)c->a ? java_block_pos_init(c->a, nullptr, 0, 0) : nullptr;

    // roof: the first occupied spot decides where the next block goes
    for (int i = 0; i < 10; i++) {
        const Offset3 &o = rdata_156b80[i];
        if (!(placed >> 32 >> i & 1) && open_at(c, pos, bx + o.dx, by + o.dy, bz + o.dz))
            continue;
        if (i == 0)
            break;                              // standing under a roof already: do the sides
        int slot = i >= 5 && i <= 8 ? i - 4 : 0;
        bit = (unsigned char)(slot + 0x20);
        float fx = (float)bx, fy = (float)by, fz = (float)bz;
        Range up = rot_make_floats(rdata_1568e4, rdata_1568e8);
        switch (i == 9 ? 4 : (i - 1) & 3) {
        case 0:
            target.x = bx;
            target.y = fy + walk_update(data_304b38, up, rdata_1568b8);
            target.z = fz + walk_update(data_304b48, rot_make_floats(rdata_1568dc, rdata_1568e0), rdata_1568b8);
            break;
        case 1:
            target.x = fx + walk_update(data_304b38, rot_make_floats(rdata_1568ec, rdata_1568f0), rdata_1568b8);
            target.y = fy + walk_update(data_304b48, up, rdata_1568b8);
            target.z = bz;
            break;
        case 2:
            target.x = bx + 1;
            target.y = fy + walk_update(data_304b38, up, rdata_1568b8);
            target.z = fz + walk_update(data_304b48, rot_make_floats(rdata_1568ec, rdata_1568f0), rdata_1568b8);
            break;
        case 3:
            target.x = fx + walk_update(data_304b38, rot_make_floats(rdata_1568dc, rdata_1568e0), rdata_1568b8);
            target.y = fy + walk_update(data_304b48, up, rdata_1568b8);
            target.z = bz + 1;
            break;
        default:
            target.x = fx + walk_update(data_304b38, rot_make_floats(rdata_1568b4, rdata_1568d8), rdata_1568b8);
            target.y = by + 4;
            target.z = fz + walk_update(data_304b48, rot_make_floats(rdata_1568b4, rdata_1568d8), rdata_1568b8);
            break;
        }
        goto place;
    }

    // sides: one block in each direction, at feet, head or above
    for (int d = 0; d < 4; d++) {
        int dir = (data_304b18 + d) & 3;
        int dx = rdata_156b9e[dir].dx, dz = rdata_156b9e[dir].dz;
        int nx = bx + dx, nz = bz + dz;
        Box mine = java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, c->b));
        Vec3 lo = {(double)nx, (double)by, (double)nz};
        Box cell = aabb_make(lo, vec3_add(lo, Vec3{1.0, 3.0, 1.0}));
        if (aabb_intersects(mine, cell))
            continue;
        int base = dir * 3;
        int k;
        Range r;
        if (!(placed >> base & 1) && open_at(c, pos, nx, by, nz) && !open_at(c, pos, nx, by - 1, nz)) {
            k = 0;
            r = rot_make_floats(rdata_1568a8, rdata_1568ac);
        } else if (!(placed >> (base + 1) & 1) && open_at(c, pos, nx, by + 1, nz) && !open_at(c, pos, nx, by, nz)) {
            k = 1;
            r = rot_make_floats(rdata_1568b0, rdata_1568b4);
        } else if (!(placed >> (base + 2) & 1) && open_at(c, pos, nx, by + 2, nz) && !open_at(c, pos, nx, by + 1, nz)) {
            k = 2;
            r = rot_make_floats(rdata_1568b0, rdata_1568b4);
        } else {
            continue;
        }
        target.x = (rdata_1568b8 - (double)(walk_update(data_304b58, r, rdata_1568b8) * (float)dx)) + nx;
        target.z = (rdata_1568b8 - (double)(walk_update(data_304b68, r, rdata_1568b8) * (float)dz)) + nz;
        target.y = by + k;
        bit = (unsigned char)(base + k);
        goto place;
    }

    // nothing left to place: stop once we stand in the middle of the block
    if (!ground)
        return;
    {
        double fx = fabs_(libc_fmod(me.x, rdata_1568c0));
        double fz = fabs_(libc_fmod(me.z, rdata_1568c0));
        if (rdata_1568c8 >= fx && fx >= rdata_1568d0 && fz >= rdata_1568d0 && rdata_1568c8 >= fz)
            goto off;
    }
    return;

place:
    if (!increment_wrap(500))
        goto off;
    switch_best_hotbar_slot(c, data_2f6108, 1, key_category);
    if (!block_entity_id(c->a, item_held_hotbar_slot(c->a, c->b)))
        goto off;
    {
        Rot cur = rot_add(get_entity_rotation(c->a, c->b), get_render_state());
        Vec3 eye = java_get_vec3_x_coord(c->a, java_call_entity_living_base_get_position(c->a, c->b));
        Pair d = rot_sub(rot_look_at(eye, target), cur);
        float dyaw = wrap_angle(d.x);
        float dpitch = d.y;
        Pair step = rot_scale(Pair{dyaw, dpitch}, (float)((double)data_2f6368 * rdata_1568b8 + rdata_1568b8));
        float j1 = walk_update(data_304b78, rot_make_floats(rdata_1568f4, rdata_1568f0), rdata_1568f8);
        float j2 = walk_update(data_304b88, rot_make_floats(rdata_1568f4, rdata_1568f0), rdata_1568f8);
        float my = j1 * rdata_156900 * data_2f6368;
        float mp = rdata_156900 * j2 * data_2f6368;
        Pair lim = {limit_rotation_delta(-my, my, dyaw), limit_rotation_delta(-mp, mp, dpitch)};
        Pair rot = rot_lerp(step, lim, rdata_156920);
        apply_silent_rotation(rot);
        v4f err = {dyaw - rot.x, dpitch - rot.y, 0, 0};
        for (int e = 0; e < 4; e++) err[e] = __builtin_fabsf(err[e]);
        if (rdata_156940 > err[1] + err[0]) {   // on target: place
            java_get_key_binding_press_time_2(c->a, java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18)));
            placed |= 1ull << (bit & 63);
        }
        data_304b1c = 1;
        double fx = fabs_(libc_fmod(me.x, rdata_1568c0));
        double fz = libc_fmod(me.z, rdata_1568c0);
        if (rdata_1568c8 >= fx && fx >= rdata_1568d0 && fabs_(fz) >= rdata_1568d0 && rdata_1568c8 >= fabs_(fz)) {
            data_304b20 = 2;
            return;
        }
        data_304b20 = 1;                        // walk to the middle of the block first
        Vec3 mid = {libc_floor(me.x) + rdata_1568b8, me.y, libc_floor(me.z) + rdata_1568b8};
        data_304b28 = rot_look_at(me, mid);
    }
    return;

off:
    queue_module_toggle(c->p08, data_2f6108, 0);
}
