// Trajectories - predicted flight path of bows, pearls and throwables.
//   0x44e40 disable: free the cached path buffers
//   0x45370 tick: simulate the flight, find the block / entity it lands on
//   0x44ea0 render: redraw the path from the interpolated position, plus the hit box
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Rot { float yaw, pitch; };
struct Box { Vec3 lo, hi; };
struct Point { float x, y, z; };
struct RenderEvt { char pad0[8]; Vec3 cam; Ctx *ctx; float partial; };

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" char data_2f3df0[];           // Trajectories module descriptor
extern "C" void *data_304478[2];
extern "C" void *data_304488[2];

FN(void, libc_free, (void *p));

extern "C" unsigned data_304484;         // tracked entity count (header at 0x304480)
extern "C" unsigned data_304494;         // tracked box count (header at 0x304490)
extern "C" char data_304480[], data_304490[];
extern "C" int data_304498;              // path length
extern "C" unsigned char data_30449c;    // hit colour
extern "C" Point data_3044a0[100];
extern "C" unsigned char data_304950;    // something hit
extern "C" Box data_304958;              // hit box
extern "C" Vec3 data_304460;             // hit entity movement this tick
extern "C" float data_304988;            // gravity
extern "C" float data_30498c, data_304990;   // launch power
extern "C" unsigned char data_2f4090;    // bows
extern "C" unsigned char data_2f40b0;    // uncharged bow
extern "C" unsigned char data_2f40d0;    // pearls
extern "C" unsigned char data_2f40f0;    // throwables
extern "C" float data_2f4030;            // line width
extern "C" float data_2f4040, data_2f4044, data_2f4048;   // hit colour
extern "C" float data_2f4060, data_2f4064, data_2f4068;   // miss colour
extern "C" const double rdata_154df0, rdata_154df8, rdata_154e00, rdata_154e10, rdata_154e20, rdata_154e28,
                        rdata_154e30, rdata_154e38, rdata_154e40, rdata_154e48;
extern "C" const float rdata_154e08, rdata_154e0c, rdata_154e18;
extern "C" void (**rdata_150cc0)();                                 // glPushMatrix
extern "C" void (**rdata_150cb8)();                                 // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);              // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);              // glScalef
extern "C" void (**rdata_150cb0)(float);                            // glLineWidth
extern "C" void (**rdata_150ca0)(float, float, float);              // glColor3f
extern "C" void (**rdata_13e458)(unsigned);                         // glEnable
extern "C" void (**rdata_14fd08)(unsigned);                         // glEnableClientState
extern "C" void (**rdata_14fd10)(int, unsigned, int, const void *); // glVertexPointer
extern "C" void (**rdata_154f98)(unsigned, int, int);               // glDrawArrays

FN(unsigned char, is_silent_rotation_active, ());
FN(void *, get_net_handler, (void *a, void *x));
FN(int, java_get_game_settings_third_person_view, (void *a, void *gs));
FN(void *, item_held_hotbar_slot, (void *a, void *p));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(unsigned char, java_call_entity_player_is_using_item, (void *a, void *p));
FN(int, java_get_entity_player_item_in_use_count, (void *a, void *p));
FN(Rot, get_entity_rotation, (void *a, void *p));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(void, throwable_velocity, (Ctx *c, Vec3 *start, Rot r, float partial, Vec3 *pos, Vec3 *vel));
FN(void *, java_get_world_2, (void *a, void *world));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(unsigned char, java_call_entity_can_be_collided_with, (void *a, void *e));
FN(void *, java_get_entity, (void *a, void *e));
FN(Box, java_get_axis_aligned_bb_min_x, (void *a, void *bb));
FN(Box, aabb_expand, (Box b, double grow));
FN(Box, aabb_union, (Vec3 a, Vec3 b));
FN(Box, aabb_make, (Vec3 lo, Vec3 hi));
FN(unsigned char, aabb_intersects, (Box a, Box b));
FN(unsigned char, aabb_ray_slab_test, (Box b, Vec3 origin, Vec3 dir));
FN(void, vector_push_back, (void *vec, unsigned elem, void *hdr, const void *item));
FN(void *, element_at, (void *vec, unsigned elem, void *hdr, unsigned i));
FN(void *, java_new_vec3_init, (void *a, Vec3 v));
FN(void *, java_world_ray_trace_blocks, (void *a, void *world, void *from, void *to, unsigned char liquids, unsigned char ignore_no_box, unsigned char last));
FN(void *, java_get_moving_object_position, (void *a, void *hit));
FN(Vec3, java_get_vec3_x_coord, (void *a, void *v));
FN(Vec3, java_get_moving_object_position_block_x, (void *a, void *hit));
FN(Vec3, vec3_make, (double x, double y, double z));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(Vec3, vec3_scale, (Vec3 v, double k));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Vec3, vec3_reciprocal, (Vec3 v));
FN(void, gl_array_xyz_float, ());
FN(void, gl_array_color2, ());
FN(void, enable_vertex_array, ());

typedef unsigned char (*IsSameObject)(void *env, void *a, void *b);

// bow power after `ticks` of drawing, 0 below the minimum
static inline __attribute__((always_inline)) float bow_power(int ticks) {
    float t = (float)ticks / rdata_154e08;
    float v = (t * t + (t + t)) / rdata_154e0c;
    if (rdata_154e10 > (double)v)
        return 0.0f;
    double p = (double)(rdata_154e18 < v ? rdata_154e18 : v) * rdata_154e20;
    return (float)(p + p);
}

extern "C" void trajectories_disable(EvHook *h) {
    if (!h->on && h->ev == data_2f3df0) {
        libc_free(data_304478[0]);
        data_304478[0] = data_304478[1] = nullptr;
        libc_free(data_304488[0]);
        data_304488[0] = data_304488[1] = nullptr;
    }

}

extern "C" void trajectories_tick(Hook *h) {
    Ctx *c = h->ctx;
    unsigned char pearl, thrown;
    if (c->f40 != 1 || is_silent_rotation_active() || java_get_game_settings_third_person_view(c->a, get_net_handler(c->a, c->c18)))
        goto none;
    {
        void *stack = item_held_hotbar_slot(c->a, c->b);
        if (!stack)
            goto none;
        int id = java_call_static_item_get_id_from_item(c->a, stack);
        if (id == 0x170) {                      // ender pearl
            if (data_2f40d0 != 1)
                goto none;
            data_30498c = 1.5f;
            data_304990 = 1.5f;
            data_304988 = 0.03f;
            pearl = 1;
            thrown = 0;
        } else if (id == 0x14c || id == 0x158) {   // snowball, egg
            if (data_2f40f0 != 1)
                goto none;
            data_30498c = 1.5f;
            data_304990 = 1.5f;
            data_304988 = 0.03f;
            thrown = 1;
            pearl = 0;
        } else if (id == 0x105) {               // bow
            if (data_2f4090 != 1)
                goto none;
            data_304988 = 0.05f;
            if (java_call_entity_player_is_using_item(c->a, c->b)) {
                int used = java_get_entity_player_item_in_use_count(c->a, c->b);
                float p = bow_power(72000 - used);
                data_304990 = p;
                if (p == 0.0f)
                    goto none;
                data_30498c = bow_power(71999 - used);
            } else {
                if (data_2f40b0 != 1)
                    goto none;
                data_30498c = 3.0f;
                data_304990 = 3.0f;
            }
            thrown = 1;
            pearl = 0;
        } else {
            goto none;
        }
    }
    {
        Rot rot = get_entity_rotation(c->a, c->b);
        Vec3 me = java_get_entity_pos_x(c->a, c->b);
        Vec3 pt, vel;
        throwable_velocity(c, &me, rot, 0.0f, &pt, &vel);
        data_304484 = 0;
        data_304494 = 0;
        // entities near the first stretch of the path
        void *list = java_get_world_2(c->a, c->p20);
        Vec3 lo = {pt.x, -100.0, pt.z};
        Vec3 hi = {vel.x * rdata_154e28 + pt.x, pt.y + rdata_154e30, rdata_154e28 * vel.z + pt.z};
        Box area = aabb_union(lo, hi);
        for (int i = 0; i < java_call_list_size(c->a, list); i++) {
            void *e = java_call_list_get(c->a, list, i);
            void *env = ((void **)c->a)[1];
            if (((IsSameObject)(*(void ***)env)[0xc0 / 8])(env, e, c->b))
                continue;
            if (!java_call_entity_can_be_collided_with(c->a, e))
                continue;
            Box bb = aabb_expand(java_get_axis_aligned_bb_min_x(c->a, java_get_entity(c->a, e)), rdata_154e38);
            if (!aabb_intersects(bb, area))
                continue;
            vector_push_back(data_304478, 8, data_304480, &e);
            vector_push_back(data_304488, 0x30, data_304490, &bb);
        }
        for (int n = 0; n < 100; n++) {
            Vec3 from = pt;
            pt = vec3_add(pt, vel);
            void *hit = java_world_ray_trace_blocks(c->a, c->p20, java_new_vec3_init(c->a, from), java_new_vec3_init(c->a, pt), 0, 1, 0);
            if (hit) {
                data_304498 = n + 1;
                data_304950 = 1;
                data_30449c = pearl;
                pt = java_get_vec3_x_coord(c->a, java_get_moving_object_position(c->a, hit));
                vel = vec3_sub(pt, from);
                Vec3 block = java_get_moving_object_position_block_x(c->a, hit);
                data_304958 = aabb_make(block, vec3_add(block, vec3_make(rdata_154e40, rdata_154e40, rdata_154e40)));
                data_304460 = vec3_make(0.0, 0.0, 0.0);
            }
            Vec3 dir = vec3_reciprocal(vel);
            for (unsigned j = 0; j < data_304494; j++) {
                Box b = *(Box *)element_at(data_304488[0], 0x30, data_304490, j);
                if (!aabb_ray_slab_test(b, from, dir))
                    continue;
                data_304498 = n + 1;
                data_304950 = 1;
                data_30449c = thrown;
                data_304958 = b;
                void *e = *(void **)element_at(data_304478[0], 8, data_304480, j);
                data_304460 = vec3_sub(java_get_entity_pos_x(c->a, e), java_get_entity_prev_pos_x(c->a, e));
                return;
            }
            if (hit)
                return;
            vel = vec3_scale(vel, rdata_154e48);
            vel.y -= data_304988;
        }
        data_304498 = 100;
        data_304950 = 0;
        data_30449c = 0;
    }
    return;
none:
    data_304498 = 0;
}

extern "C" void trajectories_render(RenderEvt *h) {
    if (!data_304498)
        return;
    Ctx *c = h->ctx;
    Rot rot = get_entity_rotation(c->a, c->b);
    Vec3 pos = java_get_entity_pos_x(c->a, c->b);
    Vec3 prev = java_get_entity_prev_pos_x(c->a, c->b);
    Vec3 start = vec3_lerp(prev, pos, (double)h->partial);
    Vec3 pt = prev, vel = pos;
    throwable_velocity(c, &start, rot, h->partial, &pt, &vel);
    for (unsigned i = 0; i < (unsigned)data_304498; i++) {
        pt = vec3_add(pt, vel);
        data_3044a0[i].x = (float)(pt.x - h->cam.x);
        data_3044a0[i].y = (float)(pt.y - h->cam.y);
        data_3044a0[i].z = (float)(pt.z - h->cam.z);
        vel = vec3_scale(vel, rdata_154df0);
        vel.y -= data_304988;
    }
    (*rdata_13e458)(0xbe2);
    (*rdata_150cb0)(data_2f4030);
    if (data_30449c)
        (*rdata_150ca0)(data_2f4040, data_2f4044, data_2f4048);
    else
        (*rdata_150ca0)(data_2f4060, data_2f4064, data_2f4068);
    (*rdata_14fd08)(0x8074);                    // GL_VERTEX_ARRAY
    (*rdata_14fd10)(3, 0x1406, 0, data_3044a0);
    (*rdata_154f98)(3, 0, data_304498);         // GL_LINE_STRIP
    if (data_304950 == 1) {
        Vec3 mid = vec3_lerp(data_304958.lo, data_304958.hi, rdata_154df8);
        Vec3 half = vec3_sub(data_304958.hi, mid);
        Vec3 back = vec3_scale(data_304460, rdata_154e00 - (double)h->partial);
        Vec3 at = vec3_sub(vec3_sub(mid, h->cam), back);
        (*rdata_150cc0)();
        (*rdata_150cd8)((float)at.x, (float)at.y, (float)at.z);
        (*rdata_150cd0)((float)half.x, (float)half.y, (float)half.z);
        gl_array_xyz_float();
        gl_array_color2();
        (*rdata_150cb8)();
    }
    enable_vertex_array();
}
