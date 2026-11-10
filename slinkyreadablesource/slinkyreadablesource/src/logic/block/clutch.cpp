// Clutch - while falling, aims at a block next to / under the player and places on it,
// then snaps the view back.
//   0x488f0 tick: re-click the planned block when the crosshair is on it
//   0x48a30 rotation override flag
//   0x48a50 tick: plan the block, aim at it, snap back afterwards
//   0x49f40 helper: aim point and rotation cost for one candidate block
#include "slinky/engine.h"

typedef double v2d __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct BlockPos { int x, y, z; };
struct Rot { float yaw, pitch; };
struct Box { Vec3 lo, hi; };
struct Range { float lo, hi; };

extern "C" float data_2f57c0;            // clicks per second
extern "C" float data_304a18;            // click cooldown
extern "C" unsigned char data_304a1c;    // planned
extern "C" BlockPos data_304a20;         // planned block
extern "C" unsigned char data_304a2c;    // planned face is "up"
extern "C" unsigned char data_304a2d;    // rotating
extern "C" const float rdata_156070;
extern "C" const float rdata_156074;

FN(void *, java_get_minecraft_5, (void *a, void *x));
FN(int, java_get_moving_object_position_3, (void *a, void *x));
FN(BlockPos, java_get_moving_object_position_block_x_2, (void *a, void *hit));
FN(int, java_get_variant, (void *a, void *hit));
FN(void *, get_net_handler, (void *a, void *x));
FN(void *, java_get_game_settings_key_bindings, (void *a, void *x));
FN(void, java_get_key_binding_press_time_2, (void *a, void *k));
FN(Vec3, java_get_variant_2, (void *a, void *hit));

extern "C" int data_304a30;              // ticks since last hurt
extern "C" int data_304a34;              // ticks left for the jump snapback
extern "C" int data_304a38;              // snapback delay
extern "C" unsigned char data_304a3c;    // disable the module after the snapback
extern "C" int data_304a40;              // snapback ticks left
extern "C" Vec3 data_304a48;             // aim point
extern "C" double data_304a90;           // jump wiggle direction
extern "C" char data_304a60[], data_304a70[], data_304a80[], data_304a98[], data_304aa8[], data_304ab8[],
                data_304ac8[], data_304ad8[];   // RNG states
extern "C" char data_2f5580[];           // Clutch module descriptor
extern "C" float data_2f57f8;            // snapback duration
extern "C" float data_2f5830;            // snapback delay
extern "C" float data_2f5868;            // randomisation
extern "C" float data_2f58a0;            // aim speed
extern "C" float data_2f58d8;            // aim multiplier
extern "C" float data_2f5910;            // aim multiplier strength
extern "C" unsigned char data_2f5930;    // snapback instantly on jump
extern "C" unsigned char data_2f5950;    // require air
extern "C" unsigned char data_2f5970;    // require damage
extern "C" unsigned char data_2f5990;    // require moving backwards
extern "C" float data_2f59c8;            // min height
extern "C" float data_2f5a00;            // range
extern "C" float data_2f5a38;            // max aim distance
extern "C" unsigned char data_2f5a58;    // sideways only
extern "C" unsigned char data_2f5a78;    // multipoint aim
extern "C" int data_2f5ac8;              // select blocks: 1 switch, 2 switch back
extern "C" unsigned char data_2f5af0;    // disable after
extern "C" const double rdata_156158[2];
extern "C" const double rdata_156080, rdata_156088, rdata_156090, rdata_156098, rdata_1560a0, rdata_1560b0, rdata_1560c0,
                        rdata_1560c8, rdata_1560e0, rdata_1560e8, rdata_156110, rdata_156118, rdata_156128, rdata_156130,
                        rdata_156178, rdata_156188, rdata_1561d0, rdata_1561d8;
extern "C" const float rdata_1560a8, rdata_1560b8, rdata_1560d0, rdata_1560d4, rdata_1560d8, rdata_156120, rdata_156124,
                       rdata_156150, rdata_156168, rdata_15616c, rdata_156170, rdata_156180, rdata_156184, rdata_1561e0;
extern "C" const v2d rdata_1560f0, rdata_156100;
extern "C" const double rdata_1561a0, rdata_1561b0, rdata_1561c0;   // 0.5, -1.0, 1.0
extern "C" const v2d rdata_156190;       // abs mask (double)
extern "C" const v2d rdata_1561f0;       // abs mask (float)
extern "C" const v2d rdata_156140;       // sign mask (float)
extern "C" const unsigned short rdata_156280[8];   // candidates within distance n
extern "C" const unsigned short rdata_156290[];    // candidate offsets, 4 bits each (+8)

FN(unsigned char, java_get_entity_on_ground, (void *a, void *p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *p));
FN(unsigned char, is_silent_rotation_active, ());
FN(void *, get_held_item, (void *a, void *gs));
FN(unsigned char, java_get_key_binding_key_code, (void *a, void *k));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(Rot, get_render_state, ());
FN(Rot, get_render_rotation, ());
FN(Rot, rot_add, (Rot a, Rot b));
FN(Rot, rot_sub, (Rot a, Rot b));
FN(Rot, rot_scale, (Rot r, float k));
FN(float, wrap_angle, (float deg));
FN(void, apply_silent_rotation, (Rot r));
FN(Range, rot_make_floats, (float lo, float hi));
FN(float, random_rotation, (Range r));
FN(float, walk_update, (void *st, Range r, double d));
FN(float, walk_update_timed, (void *st, Range r, double a, double b));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(void *, java_get_entity, (void *a, void *p));
FN(Box, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(void *, java_call_entity_living_base_get_position, (void *a, void *p));
FN(Vec3, java_get_vec3_x_coord, (void *a, void *v));
FN(void *, java_get_entity_3, (void *a, void *p));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(double, libc_hypot, (double x, double y));   // hypot
FN(double, libc_acos, (double x));             // acos
FN(float, libc_sinf, (float x));                // sinf
FN(float, libc_cosf, (float x));                // cosf
FN(double, libc_floor, (double x));              // floor
FN(double, libc_fmod, (double x, double y));    // fmod
FN(void *, java_block_pos_init, (void *a, void *x, int y, int z));
FN(void *, java_call_id_328, (void *a, void *world, void *pos, int x, int y, int z));
FN(int, get_block_id, (void *a, void *block));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(void *, java_new_vec3_init, (void *a, Vec3 v));
FN(void *, java_world_ray_trace_blocks, (void *a, void *world, void *from, void *to, unsigned char liquids, unsigned char ignore_no_box, unsigned char last));
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Rot, rot_look_at, (Vec3 from, Vec3 to));
FN(void, track_owner, (Ctx *c, void *mod, unsigned char back));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(unsigned char, block_entity_id, (void *a, void *x));
FN(void, queue_module_toggle, (void *mods, void *mod, unsigned char on));
FN(unsigned char, increment_wrap, (int ms));
FN(Vec3, java_get_entity_rotation_pitch, (void *a, void *p));
FN(Vec3, vec3_scale, (Vec3 v, double k));
FN(unsigned char, aabb_ray_hit, (Box b, Vec3 origin, Vec3 dir, Vec3 *hit));

static inline __attribute__((always_inline)) int iabs(int v) {
    return v < 0 ? -v : v;
}
static inline __attribute__((always_inline)) double dabs(double x) {
    x = __builtin_fabs(x);
    return x;
}
static inline __attribute__((always_inline)) float fabs_(float x) {
    x = __builtin_fabsf(x);
    return x;
}
static inline __attribute__((always_inline)) float neg(float x) {
    x = -x;
    return x;
}

extern "C" void clutch_tick(Hook *h) {
    float t = data_304a18;
    if (t > 0.0f) {
        t += rdata_156070;
        data_304a18 = t;
        if (t > 0.0f)
            return;
    }
    data_304a18 = rdata_156074 / data_2f57c0 + t;
    Ctx *c = h->ctx;
    if (c->f40 != 1 || !data_304a1c)
        return;
    void *hit = java_get_minecraft_5(c->a, c->c18);
    if (!hit || java_get_moving_object_position_3(c->a, hit) != 1)
        return;
    BlockPos bp = java_get_moving_object_position_block_x_2(c->a, hit);
    if (bp.x != data_304a20.x || bp.y != data_304a20.y || bp.z != data_304a20.z)
        return;
    if ((java_get_variant(c->a, hit) != 1) == data_304a2c)
        return;
    java_get_key_binding_press_time_2(c->a, java_get_game_settings_key_bindings(c->a, get_net_handler(c->a, c->c18)));
    Vec3 v = java_get_variant_2(c->a, hit);
    int x = (int)v.x, y = (int)v.y, z = (int)v.z;
       // scalar conversions, no SLP (as in the original)
    data_304a20.x = x;
    data_304a20.y = y;
    data_304a20.z = z;
}

extern "C" void clutch_rotation_override_flag(char *h) {
    if (data_304a2d == 1)
        *(unsigned short *)(h + 8) = 0x101;
}

extern "C" float clutch_part(Vec3 *eye, Rot cur, Vec3 *center, BlockPos *base, BlockPos *rel, Vec3 *out) {
    int rx = rel->x, ry = rel->y, rz = rel->z;
    float j0 = walk_update(data_304ab8, rot_make_floats(0.0f, rdata_156170), rdata_156178);
    float rnd = data_2f5868;
    float j1 = walk_update(data_304ac8, rot_make_floats(rdata_156180, rdata_156184), rdata_156188);
    float j2 = walk_update(data_304ad8, rot_make_floats(rdata_156180, rdata_156184), rdata_156188);
    double lane_x = (double)(j1 * rnd), lane_z = (double)(j2 * rnd);
    if (!data_2f5a78) {
        // lean towards the side of the block the previous aim point was on
        double fx = dabs(libc_fmod(center->x, rdata_156178));
        double fz = dabs(libc_fmod(center->z, rdata_156178));
        fx = rdata_1561a0 < fx ? dabs(fx + rdata_1561b0) : fx;
        fz = rdata_1561a0 < fz ? dabs(fz + rdata_1561b0) : fz;
        lane_z += fx * (data_304a48.z < center->z ? rdata_1561b0 : rdata_1561c0);
        lane_x += fz * (data_304a48.x < center->x ? rdata_1561b0 : rdata_1561c0);
    }
    double k = (double)(rnd * j0) + rdata_1561d0;
    double far = rdata_156178 - k;
    double xlo, xhi, zlo, zhi;
    if ((unsigned)iabs(rx) > (unsigned)iabs(rz)) {
        xlo = xhi = (double)((unsigned)rx >> 31);  // the face pointing at us
        zlo = k;
        zhi = far;
    } else {
        xlo = k;
        xhi = far;
        zlo = zhi = (double)((unsigned)rz >> 31);
    }
    int nx = rx + base->x, ny = ry + base->y, nz = rz + base->z;
    double dist = (double)(iabs(rel->x) + iabs(rel->z));
    double ax = limit_rotation_delta((float)(xlo + nx), (float)(xhi + nx), (float)(lane_x + center->x));
    double ay = (double)limit_rotation_delta(0.0f, rdata_1561e0, (float)(dist * rdata_1561d8)) + ((double)ny + rdata_1561d8);
    double az = limit_rotation_delta((float)(zlo + nz), (float)(zhi + nz), (float)(lane_z + center->z));
    Vec3 aim = vec3_make(ax, ay, az);
    Rot d = rot_sub(rot_look_at(*eye, aim), cur);
    *out = aim;
    return fabs_(wrap_angle(d.yaw)) + fabs_(d.pitch);
}

extern "C" void clutch_tick_2(Hook *h) {
    data_304a1c = 0;
    data_304a2d = 0;
    Ctx *c = h->ctx;
    if (c->f40 != 1)
        return;
    unsigned char ground = java_get_entity_on_ground(c->a, c->b);
    data_304a30 = java_get_entity_living_base_hurt_time(c->a, c->b) == 0 ? data_304a30 + 1 : 0;
    if (data_304a34 > 0) {
        data_304a34 -= 1;
        if (ground && is_silent_rotation_active() && data_2f5930 &&
            java_get_key_binding_key_code(c->a, get_held_item(c->a, get_net_handler(c->a, c->c18)))) {
            // jumped: wiggle back to the real rotation at once
            Rot cur = rot_add(get_entity_rotation(c->a, c->b), get_render_state());
            Rot srv = get_render_rotation();
            double sgn = rdata_156158[0.0 > data_304a90];
            float r1 = (float)((double)random_rotation(rot_make_floats(rdata_156168, rdata_15616c)) * sgn);
            float r2 = (float)((double)random_rotation(rot_make_floats(rdata_156168, rdata_15616c)) * sgn);
            data_304a90 += r1;
            Rot d = rot_sub(rot_add(srv, rot_scale(Rot{r1, r2}, data_2f5868)), cur);
            d.yaw = wrap_angle(d.yaw);
            apply_silent_rotation(d);
            data_304a2d = 1;
            data_304a38 = (int)data_2f57f8;
            return;
        }
    }
    Box bb = java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, c->b));
    Vec3 eye = java_get_vec3_x_coord(c->a, java_call_entity_living_base_get_position(c->a, c->b));
    Rot cur = rot_add(get_entity_rotation(c->a, c->b), get_render_state());
    if (data_2f5950 & ground)
        goto idle;
    if (data_304a30 >= 11 && data_2f5970)
        goto idle;
    if (java_get_entity_3(c->a, c->b))
        goto idle;
    if (data_2f5990 == 1) {
        Vec3 m = java_get_entity_motion_x(c->a, c->b);
        double speed = libc_hypot(m.x, m.z);
        if (speed == 0.0)
            goto idle;
        float r = (float)((double)get_entity_rotation(c->a, c->b).yaw * rdata_156080 / rdata_156088 + rdata_156080);
        float sn = libc_sinf(r), cs = libc_cosf(r);
        if (rdata_156090 > libc_acos(((double)sn * m.x + (double)cs * m.z) / speed))
            goto idle;                          // moving forwards
    }
    {
        Vec3 center = {(bb.lo.x + bb.hi.x) * rdata_156098, bb.lo.y + rdata_1560a0, (bb.lo.z + bb.hi.z) * rdata_156098};
        data_304a2c = 0;
        void *pos = **(int **)c->a ? java_block_pos_init(c->a, nullptr, 0, 0) : nullptr;
        BlockPos base = {(int)libc_floor(center.x), (int)libc_floor(center.y), (int)libc_floor(center.z)};
        if (data_2f59c8 > 0.0f) {
            int y = base.y;
            for (int k = 1;; k++, y--) {
                if (get_block_id(c->a, java_call_id_328(c->a, c->p20, pos, base.x, y, base.z)))
                    goto idle;                  // ground too close
                if (!(data_2f59c8 > (float)k))
                    break;
            }
        }
        unsigned long long limit = rdata_156280[(int)data_2f5a00];
        long long best_rank = -3;
        float best = rdata_1560a8;
        Vec3 best_aim;
        BlockPos best_block;
        Vec3 aim;
        // keep the previous block while it still works
        if (!((double)data_304a20.y > center.y) &&
            get_block_id(c->a, java_call_id_328(c->a, c->p20, pos, data_304a20.x, data_304a20.y, data_304a20.z))) {
            BlockPos rel = {data_304a20.x - base.x, data_304a20.y - base.y, data_304a20.z - base.z};
            float cost = clutch_part(&eye, cur, &center, &base, &rel, &aim);
            if (!(cost > data_2f5a38) && rdata_1560b0 > vec3_distance_sq(eye, aim)) {
                void *hit = java_world_ray_trace_blocks(c->a, c->p20, java_new_vec3_init(c->a, eye), java_new_vec3_init(c->a, aim), 0, 1, 0);
                BlockPos hb = data_304a20;
                if (hit)
                    hb = java_get_moving_object_position_block_x_2(c->a, hit);
                if (hb.x == data_304a20.x && hb.y == data_304a20.y && hb.z == data_304a20.z) {
                    best_aim = aim;
                    best_block = hb;
                    best = rdata_1560a8;
                    if (cost != rdata_1560a8) {
                        best_rank = iabs(hb.x - base.x) + iabs(hb.y - base.y) + iabs(hb.z - base.z) - 2;
                        best = cost;
                    }
                }
            }
        }
        for (unsigned long long i = 0; i < limit; i++) {
            unsigned off = rdata_156290[i];
            int ox = (off & 0xf) - 8, oy = (off >> 4 & 0xf) - 8, oz = (off >> 8 & 0xf) - 8;
            int x = base.x + ox, y = base.y + oy, z = base.z + oz;
            if (!get_block_id(c->a, java_call_id_328(c->a, c->p20, pos, x, y, z)))
                continue;
            if (!ox && !oz) {                   // straight below: aim at its top face
                if (data_2f5a58)
                    goto idle;
                double inset = (double)(walk_update(data_304ab8, rot_make_floats(0.0f, rdata_1560b8), rdata_1560c0) * data_2f5868) + rdata_1560c8;
                double jx = walk_update(data_304a98, rot_make_floats(rdata_1560d0, rdata_1560d4), rdata_1560c0) * data_2f5868;
                double jz = walk_update(data_304aa8, rot_make_floats(rdata_1560d0, rdata_1560d4), rdata_1560c0) * data_2f5868;
                double ax = limit_rotation_delta((float)((double)x + inset), (float)((double)(x + 1) - inset), (float)(center.x + jx));
                double az = limit_rotation_delta((float)((double)z + inset), (float)((double)(z + 1) - inset), (float)(center.z + jz));
                data_304a48 = vec3_make(ax, rdata_1560c0, az);
                data_304a20 = BlockPos{x, y, z};
                data_304a2c = 1;
                goto planned;
            }
            long long rank = iabs(ox) + iabs(oy) + iabs(oz);
            limit = rdata_156280[rank];
            BlockPos rel = {ox, oy, oz};
            float cost = clutch_part(&eye, cur, &center, &base, &rel, &aim);
            if (cost > best && best_rank <= rank)
                continue;
            if (!(rdata_1560b0 > vec3_distance_sq(eye, aim)))
                continue;
            best_aim = aim;
            best_block = BlockPos{x, y, z};
            best = cost;
        }
        if (best > data_2f5a38)
            goto idle;
        data_304a48 = best_aim;
        data_304a20 = best_block;
    planned:
        if (data_2f5ac8 == 1)
            track_owner(c, data_2f5580, 0);
        else if (data_2f5ac8 == 2)
            track_owner(c, data_2f5580, 1);
        data_304a1c = block_entity_id(c->a, item_held_hotbar_slot(c->a, c->b));
        if (!data_304a1c)
            goto countdown;
        float r = random_rotation(rot_make_floats(rdata_1560d8, (float)((double)data_2f5868 * rdata_156098 + rdata_1560c0)));
        data_304a38 = (int)(r * data_2f5830);
        data_304a34 = 10;
        data_304a3c = data_2f5af0;
        goto act;
    }
idle:
    data_304a1c = 0;
countdown:
    if (data_304a38 > 0) {
        data_304a38 -= 1;
        data_304a40 = (int)data_2f57f8;
    } else if (data_304a40 > 0) {
        data_304a40 -= 1;
        data_304a1c = 0;
    } else {
        if (data_304a3c == 1) {
            queue_module_toggle(c->p08, data_2f5580, 0);
            data_304a3c = 0;
        }
        return;
    }
act:
    if (!increment_wrap(1000))
        return;
    data_304a2d = 1;
    if (data_304a1c) {
        // already looking at the top of the planned block?
        int bx = data_304a20.x, by = data_304a20.y, bz = data_304a20.z;
        Vec3 look = vec3_scale(java_get_entity_rotation_pitch(c->a, c->b), rdata_1560e8);
        Box top = {{bx + rdata_1560f0[0], by + rdata_1560f0[1], bz + rdata_156100[0]},
                   {bx + rdata_156100[1], (double)(by + 1), rdata_1560e0 + bz}};
        Vec3 hit;
        if (aabb_ray_hit(top, eye, look, &hit) && (double)by + rdata_156110 > hit.y)
            return;
        Rot want = rot_look_at(eye, data_304a48);
        Rot d = rot_sub(want, cur);
        float yaw;
        if ((double)want.pitch > rdata_156118)
            yaw = walk_update(data_304a60, rot_make_floats(rdata_156120, rdata_156124), rdata_156098) * data_2f5868;
        else
            yaw = wrap_angle(d.yaw);
        float speed = data_2f58a0;
        Range jr = rot_make_floats((float)((double)data_2f5868 * rdata_156128 + rdata_1560c0), rdata_1560d8);
        float ys = walk_update_timed(data_304a70, jr, rdata_156130, rdata_1560c0) * speed;
        jr = rot_make_floats((float)((double)data_2f5868 * rdata_156128 + rdata_1560c0), rdata_1560d8);
        float ps = speed * walk_update_timed(data_304a80, jr, rdata_156130, rdata_1560c0);
        float cy = limit_rotation_delta(neg(ys), ys, yaw);
        float cp = limit_rotation_delta(neg(ps), ps, d.pitch);
        float m1 = data_2f58d8 + walk_update_timed(data_304a70, rot_make_floats(rdata_156150, rdata_1560b8), rdata_156130, rdata_1560c0);
        float m2 = data_2f58d8 + walk_update_timed(data_304a80, rot_make_floats(rdata_156150, rdata_1560b8), rdata_156130, rdata_1560c0);
        float ry = lerp_unclamped_float(cy, m1 * yaw, data_2f5910);
        float rp = lerp_unclamped_float(cp, m2 * d.pitch, data_2f5910);
        apply_silent_rotation(Rot{ry, rp});
        return;
    }
    // snap back to the real rotation over the remaining ticks
    if (data_304a38 > 0)
        return;
    int left = data_304a40;
    if (left <= 0 || !is_silent_rotation_active())
        return;
    Rot now = rot_add(get_entity_rotation(c->a, c->b), get_render_state());
    Rot d = rot_sub(get_render_rotation(), now);
    d.yaw = wrap_angle(d.yaw);
    apply_silent_rotation(rot_scale(d, rdata_1560d8 / (float)left));
}
