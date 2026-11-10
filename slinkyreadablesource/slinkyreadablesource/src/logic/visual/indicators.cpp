// Indicators - off-screen arrows pointing at nearby dropped items.
//   0x3ea40 disable: release every cached global reference
//   0x3ef20 tick: collect up to 10 items with their distance labels
//   0x3ead0 render: rotate an arrow around the crosshair towards each item
#include "slinky/engine.h"

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
struct Cached { char pad0[0x30]; void *ref; };          // 0x38 bytes, array at 0x301ea8
typedef void (*DeleteRef)(void *env, void *ref);

extern "C" char data_2f1440[];                // Indicators module descriptor
extern "C" unsigned long long data_301ea0;    // cached count
extern "C" Cached data_301ea8[];

extern "C" void indicators_disable(EvHook *h) {
    if (h->on || h->ev != data_2f1440)
        return;
    unsigned long long n = data_301ea0;
    if (n) {
        void *env = ((void **)h->ctx->a)[1];
        for (unsigned long long i = 0; i < n; i++) {
            void *ref = data_301ea8[i].ref;
            if (!ref)
                continue;
            ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, ref);
            n = data_301ea0;
        }
    }
    data_301ea0 = 0;
}

struct Indicator {                  // 0x38 bytes at 0x301eb0 (same slots as Cached above)
    double x0, z0, x1, z1;          // previous / current position
    int kind;                       // 10, 12 or 14
    void *ref;                      // global ref to the distance label
    int width;                      // label width in pixels
    unsigned color;
};
typedef int v4i __attribute__((vector_size(16), aligned(16)));
typedef float v4f __attribute__((vector_size(16), aligned(16)));
extern "C" const v4i rdata_154010;      // sign mask
static inline __attribute__((always_inline)) float neg(float x) {
    v4f v = {x, 0, 0, 0};
    return ((v4f)((v4i)v ^ rdata_154010))[0];
}
struct Pair { float x, y; };
struct Vec3 { double x, y, z; };
struct TickEvt { char pad0[8]; Ctx *ctx; };
struct RenderEvt { char pad0[8]; double cam_x; char pad10[8]; double cam_z; Ctx *ctx; void *screen; char pad30[0x10]; float partial; unsigned char shader; };

extern "C" Indicator data_301eb0[10];
extern "C" char data_3020e0[10];                       // label buffer
extern "C" unsigned char data_2f16d8, data_2f16f8, data_2f1718;   // show kind 10 / 12 / 14
extern "C" unsigned char data_2f1738;                  // only approaching items
extern "C" float data_2f1680, data_2f16b8;             // arrow scale / radius
extern "C" const double rdata_153fe8, rdata_153ff0, rdata_153ff8, rdata_154000, rdata_154008, rdata_154028, rdata_154030;
extern "C" const float rdata_153fe0, rdata_153fe4, rdata_154020, rdata_154024;
extern "C" const float rdata_154110[], rdata_154124[], rdata_154138[];   // colour per kind - 10
S(1540a8);   // "%.0fm"
extern "C" void (**rdata_150cc0)();                                 // glPushMatrix
extern "C" void (**rdata_150cb8)();                                 // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);              // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);              // glScalef
extern "C" void (**rdata_150cc8)(float, float, float, float);       // glRotatef
extern "C" void (**rdata_150cb0)(float);                            // glLineWidth
extern "C" void (**rdata_13e458)(unsigned);                         // glEnable
extern "C" void (**rdata_13e450)(unsigned);                         // glDisable
extern "C" void (**rdata_13e448)(float, float, float, float);       // glColor4f

FN(void *, java_get_minecraft_6, (void *a, void *p));
FN(void *, java_get_render_manager, (void *a, void *p));
FN(float, java_get_render_manager_player_view_y, (void *a, void *p));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(void *, java_get_world_2, (void *a, void *p));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(int, java_call_static_entity_list_get_entity_id, (void *a, void *e));
FN(unsigned char, java_get_entity_arrow_in_ground, (void *a, void *e));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(int, java_call_font_renderer_get_string_width, (void *a, void *font, void *str));
FN(void, java_variant_2, (void *a, void *font, void *str, float x, float y, unsigned color, unsigned char shadow));
FN(void, gl_bind_texture, (void *a));
FN(void, gl_clear_color, (void *a));
FN(void, gl_set_color, (void *a, float r, float g, float b, float al));
FN(void, gl_array_xy_float2, ());
FN(void, gl_array_color5, ());
FN(void, gl_array_color6, ());
FN(void, enable_vertex_array, ());
FN(Pair, rot_scale, (void *screen, float f));
FN(double, time_now, ());
FN(double, libc_sin, (double x));
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(double, libc_atan2, (double y, double x));
FN(float, wrap_angle, (float deg));
FN(double, libc_sqrt, (double x));   // sqrt (negative input path)

typedef void *(*NewString)(void *env, const char *s);
typedef void *(*NewGlobalRef)(void *env, void *o);

extern "C" void indicators_tick(TickEvt *h) {
    Ctx *c = h->ctx;
    if (data_301ea0) {
        void *env = ((void **)c->a)[1];
        for (unsigned long long i = 0; i < data_301ea0; i++)
            if (data_301eb0[i].ref)
                ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, data_301eb0[i].ref);
    }
    data_301ea0 = 0;
    if (!c->p20)
        return;
    void *font = java_get_render_manager(c->a, java_get_minecraft_6(c->a, c->c18));
    if (!font)
        return;
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    void *list = java_get_world_2(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        int kind = java_call_static_entity_list_get_entity_id(c->a, e);
        if (kind == 14) {
            if (!data_2f1718)
                continue;
        } else if (kind == 12) {
            if (!data_2f16f8)
                continue;
        } else if (kind == 10) {
            if (!data_2f16d8 || java_get_entity_arrow_in_ground(c->a, e))
                continue;
        } else {
            continue;
        }
        Vec3 cur = java_get_entity_pos_x(c->a, e);
        Vec3 prev = java_get_entity_prev_pos_x(c->a, e);
        double d2 = vec3_distance_sq(cur, me);
        if (data_2f1738 == 1 && d2 > vec3_distance_sq(prev, me))
            continue;                           // moving away
        double dist;
        if (d2 >= 0.0)
            dist = __builtin_sqrt(d2);
        else
            dist = libc_sqrt(d2);
        Indicator *ind = &data_301eb0[data_301ea0++];
        ind->x0 = cur.x;
        ind->z0 = cur.z;
        ind->x1 = prev.x;
        ind->z1 = prev.z;
        ind->kind = kind;
        void *env = ((void **)c->a)[1];
        log_fmt(data_3020e0, 10, rdata_1540a8, dist);
        void *str = ((NewString)(*(void ***)env)[0x538 / 8])(env, data_3020e0);
        if (!str) {
            ind->ref = nullptr;
        } else {
            ind->ref = ((NewGlobalRef)(*(void ***)env)[0xa8 / 8])(env, str);
            if (ind->ref) {
                ind->width = java_call_font_renderer_get_string_width(c->a, font, ind->ref);
                unsigned col = dist > rdata_154028 ? 0xffffff00u : 0xffff0000u;
                ind->color = dist > rdata_154030 ? 0xffffffffu : col;
            }
        }
        if (data_301ea0 == 10)
            return;
    }
}

extern "C" void indicators_render(RenderEvt *h) {
    if (!data_301ea0)
        return;
    Ctx *c = h->ctx;
    float partial = h->partial;
    void *mc = java_get_minecraft_6(c->a, c->c18);
    void *font = java_get_render_manager(c->a, mc);
    float yaw = java_get_render_manager_player_view_y(c->a, mc);
    gl_array_xy_float2();
    Pair mid = rot_scale(h->screen, rdata_153fe0);
    (*rdata_150cc0)();
    (*rdata_150cd8)(mid.x, mid.y, 0.0f);
    (*rdata_150cb0)(rdata_153fe4);
    (*rdata_13e458)(0xb20);
    double wave = libc_sin(time_now() * rdata_153fe8);
    float pulse = (float)(wave * rdata_153ff0 + rdata_153ff0);
    for (unsigned long long i = 0; i < data_301ea0; i++) {
        Indicator *ind = &data_301eb0[i];
        double x = lerp_unclamped_float((float)ind->x1, (float)ind->x0, partial);
        double z = lerp_unclamped_float((float)ind->z1, (float)ind->z0, partial);
        double ang = libc_atan2(z - h->cam_z, x - h->cam_x);
        float a = wrap_angle((float)(ang * rdata_153ff8 / rdata_154000 + rdata_154008) - yaw);
        (*rdata_150cc0)();
        (*rdata_150cc8)(a, 0.0f, 0.0f, rdata_153fe4);
        float scale = data_2f1680;
        (*rdata_150cd8)(0.0f, neg(data_2f16b8), 0.0f);
        (*rdata_150cd0)(scale, scale, scale);
        if (ind->ref) {
            (*rdata_150cc0)();
            (*rdata_150cc8)(neg(a), 0.0f, 0.0f, rdata_153fe4);
            if (h->shader == 1) {
                gl_bind_texture(c->a);
                gl_set_color(c->a, rdata_153fe4, rdata_153fe4, rdata_153fe4, 1.0f);
            } else {
                (*rdata_13e458)(0xde1);
            }
            java_variant_2(c->a, font, ind->ref, (float)(-ind->width * rdata_153ff0), -5.0f, ind->color, 1);
            if (h->shader == 1)
                gl_clear_color(c->a);
            else
                (*rdata_13e450)(0xde1);
            (*rdata_150cb8)();
        }
        (*rdata_150cd8)(0.0f, rdata_154020, 0.0f);
        (*rdata_150cd0)(rdata_154024, rdata_154024, rdata_154024);
        int k = ind->kind - 10;
        (*rdata_13e448)(rdata_154110[k], rdata_154124[k], rdata_154138[k], pulse);
        gl_array_color5();
        (*rdata_13e448)(0.0f, 0.0f, 0.0f, pulse);
        gl_array_color6();
        (*rdata_150cb8)();
    }
    (*rdata_150cb8)();
    enable_vertex_array();
}
