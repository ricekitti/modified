// Player ESP - boxes around players (3D or projected 2D) with health bars.
//   0x2f3f0 tick: collect up to 100 players in range, sorted
//   0x2eb20 render, mode 1: 3D boxes
//   0x2eea0 render, mode 0: 2D boxes from the projected corners
#include "slinky/engine.h"
#include "slinky/heavy_glow.h"
extern "C" bool heavy_glow_render(void* event);

typedef int v4i __attribute__((vector_size(16), aligned(16)));

struct Vec3 { double x, y, z; };
struct Color3 { float r, g, b; };
struct Pair { float x, y; };
struct Player {                     // 0x50 bytes at 0x2f7e20
    Vec3 prev, pos;
    Color3 color;
    float dist2;
    int hurt;                       // hurt ticks left
    float health;                   // (health + absorption) / (max + absorption)
    float absorb;                   // absorption / health
    unsigned char target;           // current combat target
};
struct TickEvt { char pad0[8]; Ctx *ctx; };
struct Render3d { char pad0[8]; Vec3 cam; Ctx *ctx; float partial; };
struct Render2d { char pad0[8]; Vec3 cam; char pad20[8]; float width, height; char pad30[0x10]; float partial; };

extern "C" unsigned long long data_2f7e10;     // count
extern "C" Player data_2f7e20[100];
extern "C" float data_2ea0c0;                  // line width
extern "C" int data_2ea110;                    // mode: 0 2D, 1 3D
extern "C" unsigned char data_2ea138;          // draw boxes for every player (else only the target)
extern "C" unsigned char data_2ea158;          // hurt effect
extern "C" unsigned char data_2ea178;          // filled
extern "C" unsigned char data_2ea198;          // health bar
extern "C" float data_2ea1d0;                  // range
extern "C" Color3 data_2ea1e0;                 // target colour
extern "C" Color3 data_2ea200, data_2ea220;    // friend / other colour
extern "C" unsigned char data_2ea250;          // also skip invisible
extern "C" unsigned char data_2ea270;          // highlight target
extern "C" int data_2ea2c0;                    // colour mode: 0 team, else friend / other
extern "C" const double rdata_1509e0;
extern "C" const float rdata_1509e8, rdata_1509ec, rdata_1509f0, rdata_1509f4, rdata_1509f8, rdata_1509fc,
                       rdata_150a00, rdata_150a04, rdata_150a08, rdata_150a0c, rdata_150a10, rdata_150a14,
                       rdata_150a18, rdata_150a1c, rdata_150a20;
extern "C" const v4i rdata_1509d0;             // sign mask
extern "C" const Vec3 rdata_150aa0[8];         // box corners around the feet
extern "C" void (**rdata_150cc0)();                                 // glPushMatrix
extern "C" void (**rdata_150cb8)();                                 // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);              // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);              // glScalef
extern "C" void (**rdata_150cc8)(float, float, float, float);       // glRotatef
extern "C" void (**rdata_150cb0)(float);                            // glLineWidth
extern "C" void (**rdata_150ca0)(float, float, float);              // glColor3f
extern "C" void (**rdata_13e448)(float, float, float, float);       // glColor4f
extern "C" void (**rdata_13e458)(unsigned);                         // glEnable

FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(void *, java_get_minecraft_6, (void *a, void *p));
FN(float, java_get_render_manager_player_view_y, (void *a, void *mc));
FN(void *, java_get_world, (void *a, void *world));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(int, entity_flags_get, (void *a, void *e));
FN(unsigned char, java_get_entity_is_dead, (void *a, void *e));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(void *, get_refill_state, ());
FN(int, entity_name_colour, (void *a, void *e));
FN(Color3, hex_digit_value, (int team));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *e));
FN(float, java_call_entity_living_base_get_health, (void *a, void *e));
FN(float, java_call_entity_player_get_absorption_amount, (void *a, void *e));
FN(float, java_call_entity_living_base_get_max_health, (void *a, void *e));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(void, libc_qsort, (void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(int, compare_healthbar_height, (const void *a, const void *b));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(unsigned char, project_point, (void *view, Vec3 p, Pair *out));
FN(void, draw_health_bar, (Player *p, float x, float y0, float y1));
FN(void, gl_array_xyz_float, ());
FN(void, gl_array_color1, ());
FN(void, gl_array_color2, ());
FN(void, gl_array_xy_float, ());
FN(void, gl_array_color3, ());
FN(void, gl_array_color4, ());
FN(void, enable_vertex_array, ());

typedef unsigned char (*IsSameObject)(void *env, void *a, void *b);

// fade the colour towards red while the player is hurt
static inline __attribute__((always_inline)) void hurt_tint(Player *p, float partial, float cap, float scale, float one,
                                                            float &r, float &g, float &b) {
    if (p->hurt <= 0 || !data_2ea158)
        return;
    float x = (float)p->hurt - partial;
    float k = (cap < x ? cap : x) * scale;
    r = lerp_unclamped_float(r, one, k);
    g = lerp_unclamped_float(g, 0.0f, k);
    b = lerp_unclamped_float(b, 0.0f, k);
}

extern "C" void player_esp_tick(TickEvt *h) {
    Ctx *c = h->ctx;
    if (!c->p20)
        return;
    data_2f7e10 = 0;
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    void *list = java_get_world(c->a, c->p20);
    int skip = data_2ea250 * 5 + 2;
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        void *env = ((void **)c->a)[1];
        if (((IsSameObject)(*(void ***)env)[0xc0 / 8])(env, c->b, e))
            continue;
        int flags = entity_flags_get(c->a, e);
        if (flags & skip)
            continue;
        if (java_get_entity_is_dead(c->a, e))
            continue;
        Vec3 pos = java_get_entity_pos_x(c->a, e);
        double d2 = vec3_distance_sq(pos, me);
        if (d2 > (double)(data_2ea1d0 * data_2ea1d0))
            continue;
        Color3 col;
        unsigned char target = 0;
        if (data_2ea270 == 1 && ((IsSameObject)(*(void ***)env)[0xc0 / 8])(env, e, get_refill_state())) {
            col = data_2ea1e0;
            target = 1;
        } else if (data_2ea2c0) {
            col = (flags & 5) ? data_2ea200 : data_2ea220;
        } else {
            col = hex_digit_value(entity_name_colour(c->a, e));
        }
        Player *p = &data_2f7e20[data_2f7e10++];
        p->pos = pos;
        p->prev = java_get_entity_prev_pos_x(c->a, e);
        p->color = col;
        p->dist2 = (float)d2;
        p->hurt = java_get_entity_living_base_hurt_time(c->a, e);
        p->target = target;
        float hp = java_call_entity_living_base_get_health(c->a, e);
        float abs = java_call_entity_player_get_absorption_amount(c->a, e);
        abs = 0.0f > abs ? 0.0f : abs;
        float total = java_call_entity_living_base_get_max_health(c->a, e) + abs;
        p->health = total != 0.0f ? limit_rotation_delta(0.0f, rdata_150a20, (hp + abs) / total) : 0.0f;
        p->absorb = hp != 0.0f ? limit_rotation_delta(0.0f, rdata_150a20, abs / hp) : 0.0f;
        if (data_2f7e10 == 100)
            break;
    }
    libc_qsort(data_2f7e20, data_2f7e10, sizeof(Player), compare_healthbar_height);
}

static void player_esp_render_classic_3d(Render3d *h);
extern "C" void player_esp_render_mode_1(Render3d *h) {
    if (data_2ea110 == 2) {
        if (!heavy_glow_render(h)) player_esp_render_classic_3d(h);
        return;
    }
    heavy_glow_release();
    if (data_2ea110 == 1) player_esp_render_classic_3d(h);
}
static void player_esp_render_classic_3d(Render3d *h) {
    if (!data_2f7e10 || !h->ctx || !h->ctx->p20 || !h->ctx->b)
        return;
    (*rdata_13e458)(0xbe2);
    (*rdata_150cb0)(data_2ea0c0);
    float partial = h->partial;
    Ctx *c = h->ctx;
    float yaw = java_get_render_manager_player_view_y(c->a, java_get_minecraft_6(c->a, c->c18));
    yaw = -yaw;
    const float one = rdata_1509e8;
    for (unsigned long long i = 0; i < data_2f7e10; i++) {
        Player *p = &data_2f7e20[i];
        Vec3 r = vec3_sub(vec3_lerp(p->prev, p->pos, partial), h->cam);
        (*rdata_150cc0)();
        (*rdata_150cd8)((float)r.x, (float)(r.y + rdata_1509e0), (float)r.z);
        (*rdata_150cd0)(rdata_1509ec, one, rdata_1509ec);
        if (data_2ea138 || p->target == 1) {
            gl_array_xyz_float();
            float cr = p->color.r, cg = p->color.g, cb = p->color.b;
            hurt_tint(p, partial, rdata_1509f0, rdata_1509f4, one, cr, cg, cb);
            if (data_2ea178 == 1) {
                (*rdata_13e448)(cr, cg, cb, rdata_1509f8);
                gl_array_color1();
            }
            (*rdata_150ca0)(cr, cg, cb);
            gl_array_color2();
            enable_vertex_array();
        }
        if (data_2ea198 == 1) {
            (*rdata_150cc8)(yaw, 0.0f, one, 0.0f);
            (*rdata_13e448)(0.0f, one, 0.0f, rdata_1509fc);
            draw_health_bar(p, rdata_150a00, one, rdata_150a04);
        }
        (*rdata_150cb8)();
    }
}

extern "C" void player_esp_render_mode_0(Render2d *h) {
    if (!data_2f7e10 || data_2ea110 != 0)
        return;
    (*rdata_13e458)(0xbe2);
    (*rdata_150cb0)(data_2ea0c0);
    float partial = h->partial;
    void *view = &h->cam;
    for (unsigned long long i = 0; i < data_2f7e10; i++) {
        Player *p = &data_2f7e20[i];
        Vec3 r = vec3_sub(vec3_lerp(p->prev, p->pos, partial), h->cam);
        Pair s;
        if (!project_point(view, vec3_add(r, rdata_150aa0[0]), &s))
            continue;
        float x0 = s.x, x1 = s.x, y0 = s.y, y1 = s.y;
        bool ok = true;
        for (int k = 1; k < 8; k++) {
            Pair q;
            if (!project_point(view, vec3_add(r, rdata_150aa0[k]), &q)) {
                ok = false;
                break;
            }
            if (x0 > q.x)
                x0 = q.x;
            else if (q.x > x1)
                x1 = q.x;
            if (y0 > q.y)
                y0 = q.y;
            else if (q.y > y1)
                y1 = q.y;
        }
        if (!ok)
            continue;
        bool min_on = !(0.0f > x0) && !(0.0f > y0) && !(x0 > h->width) && !(y0 > h->height);
        bool max_on = !(0.0f > x1) && !(x1 > h->width) && !(0.0f > y1) && !(y1 > h->height);
        if (!min_on && !max_on)
            continue;
        float cr = p->color.r, cg = p->color.g, cb = p->color.b;
        hurt_tint(p, partial, rdata_150a08, rdata_150a0c, rdata_150a10, cr, cg, cb);
        if (data_2ea138 || p->target == 1) {
            gl_array_xy_float();
            (*rdata_150cc0)();
            (*rdata_150cd8)(x0, y0, 0.0f);
            (*rdata_150cd0)(x1 - x0, y1 - y0, rdata_150a10);
            if (data_2ea178 == 1) {
                (*rdata_13e448)(cr, cg, cb, rdata_150a14);
                gl_array_color3();
            }
            (*rdata_150ca0)(cr, cg, cb);
            gl_array_color4();
            (*rdata_150cb8)();
            enable_vertex_array();
        }
        if (data_2ea198 == 1)
            draw_health_bar(p, x0 - data_2ea0c0 + rdata_150a18 + rdata_150a1c, y0, y1);
    }
}
