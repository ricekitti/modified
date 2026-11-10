// Pointers - arrows around the crosshair towards players outside the view.
//   0x3df10 tick: collect up to 100 players in range but out of view, sorted
//   0x3da80 render: one coloured arrow per player
#include "slinky/engine.h"

typedef int v4i __attribute__((vector_size(16), aligned(16)));

struct Vec3 { double x, y, z; };
struct Color3 { float r, g, b; };
struct Pair { float x, y; };
struct Pointer {                    // 0x38 bytes at 0x3008b0
    double x, z, px, pz;            // current / previous position
    Color3 color;
    float prev_dist, dist;
    int pad;
};
struct TickEvt { char pad0[8]; Ctx *ctx; };
struct RenderEvt { char pad0[8]; double cam_x; char pad10[8]; double cam_z; Ctx *ctx; void *screen; char pad30[0x10]; float partial; };

extern "C" unsigned long long data_3008a0;     // count
extern "C" Pointer data_3008b0[100];
extern "C" float data_2f0ac8;                  // range
extern "C" float data_2f0b00;                  // skip when within this many degrees of the view
extern "C" unsigned char data_2f0b60;          // also skip invisible
extern "C" int data_2f0a88;                    // colour mode: 0 distance gradient, 1 team, 2 friend
extern "C" Color3 data_2f0b10, data_2f0b30;    // friend / other colour
extern "C" int data_2f0a10;                    // 3d tilt
extern "C" float data_2f0910, data_2f0914;     // gradient distance range
extern "C" float data_2f0950;                  // size
extern "C" float data_2f0988;                  // radius
extern "C" unsigned char data_2f0a38;          // pulse radius with distance
extern "C" float data_2f0998, data_2f099c, data_2f09a0;   // near colour
extern "C" float data_2f09b8, data_2f09bc, data_2f09c0;   // far colour
extern "C" const double rdata_153cc0, rdata_153cc8, rdata_153cd0, rdata_153c88, rdata_153c90, rdata_153c98;
extern "C" const float rdata_153c70, rdata_153c74, rdata_153c78, rdata_153c7c, rdata_153c80, rdata_153ca0;
extern "C" const v4i rdata_153ce0;             // abs mask
extern "C" const v4i rdata_153cb0;             // sign mask
extern "C" void (**rdata_150cc0)();                                 // glPushMatrix
extern "C" void (**rdata_150cb8)();                                 // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);              // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);              // glScalef
extern "C" void (**rdata_150cc8)(float, float, float, float);       // glRotatef
extern "C" void (**rdata_150cb0)(float);                            // glLineWidth
extern "C" void (**rdata_150ca0)(float, float, float);              // glColor3f
extern "C" void (**rdata_13e458)(unsigned);                         // glEnable

FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(void *, java_get_minecraft_6, (void *a, void *p));
FN(float, java_get_render_manager_player_view_x, (void *a, void *mc));
FN(float, java_get_render_manager_player_view_y, (void *a, void *mc));
FN(void *, java_get_world, (void *a, void *world));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(unsigned char, java_get_entity_is_dead, (void *a, void *e));
FN(int, entity_flags_get, (void *a, void *e));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(double, vec3_distance, (Vec3 a, Vec3 b));
FN(double, libc_atan2, (double y, double x));
FN(float, wrap_angle, (float deg));
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(float, libc_sqrtf, (float x));                  // sqrtf (negative input path)
FN(int, entity_name_colour, (void *a, void *e));
FN(Color3, hex_digit_value, (int team));
FN(void, libc_qsort, (void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(int, compare_hurt_time, (const void *a, const void *b));
FN(Pair, rot_scale, (void *screen, float f));
FN(void, gl_array_xy_float2, ());
FN(void, gl_array_color5, ());
FN(void, gl_array_color6, ());
FN(void, enable_vertex_array, ());

typedef unsigned char (*IsSameObject)(void *env, void *a, void *b);

static inline __attribute__((always_inline)) float vabs(float x) {
    x = __builtin_fabsf(x);
    return x;
}
static inline __attribute__((always_inline)) float neg(float x) {
    x = -x;
    return x;
}

extern "C" void pointers_tick(TickEvt *h) {
    Ctx *c = h->ctx;
    if (!c->p20)
        return;
    data_3008a0 = 0;
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    Vec3 me_prev = java_get_entity_prev_pos_x(c->a, c->b);
    float yaw = java_get_render_manager_player_view_y(c->a, java_get_minecraft_6(c->a, c->c18));
    void *list = java_get_world(c->a, c->p20);
    int skip = data_2f0b60 * 5 + 2;
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *env = ((void **)c->a)[1];
        if (((IsSameObject)(*(void ***)env)[0xc0 / 8])(env, c->b, e))
            continue;
        if (java_get_entity_is_dead(c->a, e))
            continue;
        int flags = entity_flags_get(c->a, e);
        if (flags & skip)
            continue;
        Vec3 pos = java_get_entity_pos_x(c->a, e);
        double d2 = vec3_distance_sq(pos, me);
        if (d2 > (double)(data_2f0ac8 * data_2f0ac8))
            continue;
        double ang = libc_atan2(pos.z - me.z, pos.x - me.x);
        float off = vabs(wrap_angle((float)(ang * rdata_153cc0 / rdata_153cc8 + rdata_153cd0) - yaw));
        if (data_2f0b00 > off)
            continue;                           // already on screen
        Vec3 prev = java_get_entity_prev_pos_x(c->a, e);
        Pointer *p = &data_3008b0[data_3008a0++];
        p->x = pos.x;
        p->z = pos.z;
        p->px = prev.x;
        p->pz = prev.z;
        float f2 = (float)d2;

        float dist;
        if (f2 >= 0.0f)
            dist = __builtin_sqrtf(f2);
        else
            dist = libc_sqrtf(f2);
        p->dist = dist;
        p->prev_dist = (float)vec3_distance(me_prev, prev);
        if (data_2f0a88 == 2)
            p->color = (flags & 5) ? data_2f0b10 : data_2f0b30;
        else if (data_2f0a88 == 1)
            p->color = hex_digit_value(entity_name_colour(c->a, e));
        if (data_3008a0 == 100)
            break;
    }
    libc_qsort(data_3008b0, data_3008a0, 0x38, compare_hurt_time);
}

extern "C" void pointers_render(RenderEvt *h) {
    if (!data_3008a0)
        return;
    Ctx *c = h->ctx;
    float partial = h->partial;
    void *mc = java_get_minecraft_6(c->a, c->c18);
    float fov = java_get_render_manager_player_view_x(c->a, mc);
    float yaw = java_get_render_manager_player_view_y(c->a, mc);
    gl_array_xy_float2();
    Pair mid = rot_scale(h->screen, rdata_153c70);
    (*rdata_150cc0)();
    const float one = rdata_153c7c;
    if (data_2f0a10) {
        float m = rdata_153c74 > fov ? rdata_153c74 : fov;
        (*rdata_150cd8)(mid.x, mid.y, 0.0f);
        (*rdata_150cc8)(rdata_153c78 - m, one, 0.0f, 0.0f);
    } else {
        (*rdata_150cd8)(mid.x, mid.y, 0.0f);
    }
    (*rdata_150cb0)(one);
    (*rdata_13e458)(0xb20);
    float range = data_2f0914 - data_2f0910;
    if (range == 0.0f)
        range = rdata_153c80;
    for (unsigned long long i = 0; i < data_3008a0; i++) {
        Pointer *p = &data_3008b0[i];
        double x = lerp_unclamped_float((float)p->px, (float)p->x, partial);
        double z = lerp_unclamped_float((float)p->pz, (float)p->z, partial);
        double ang = libc_atan2(z - h->cam_z, x - h->cam_x);
        float a = wrap_angle((float)(ang * rdata_153c88 / rdata_153c90 + rdata_153c98) - yaw);
        (*rdata_150cc0)();
        (*rdata_150cc8)(a, 0.0f, 0.0f, one);
        float s = data_2f0950 * rdata_153ca0;
        (*rdata_150cd0)(s, s, s);
        float d = lerp_unclamped_float(p->prev_dist, p->dist, partial);
        float k = limit_rotation_delta(0.0f, one, (d - data_2f0910) / range);
        float r = data_2f0988;
        if (data_2f0a38)
            r *= k + one;
        (*rdata_150cd8)(0.0f, neg(r), 0.0f);
        if (data_2f0a88) {
            (*rdata_150ca0)(p->color.r, p->color.g, p->color.b);
        } else {
            float cr = lerp_unclamped_float(data_2f0998, data_2f09b8, k);
            float cg = lerp_unclamped_float(data_2f099c, data_2f09bc, k);
            float cb = lerp_unclamped_float(data_2f09a0, data_2f09c0, k);
            (*rdata_150ca0)(cr, cg, cb);
        }
        gl_array_color5();
        (*rdata_150ca0)(0.0f, 0.0f, 0.0f);
        gl_array_color6();
        (*rdata_150cb8)();
    }
    (*rdata_150cb8)();
    enable_vertex_array();
}
