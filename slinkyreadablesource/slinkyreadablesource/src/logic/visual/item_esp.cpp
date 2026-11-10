// Item ESP - boxes and stack counts on dropped items.
//   0x416c0 shutdown: free the item list
//   0x41250 tick: collect up to 100 listed items in range, merge nearby stacks
//   0x40de0 render: boxes, then the count labels
#include "slinky/engine.h"

typedef int v4i __attribute__((vector_size(16), aligned(16)));
struct Vec3 { double x, y, z; };
struct Color3 { float r, g, b; };
struct Listed { int id; int pad; Color3 color; int pad14; };   // 0x18 bytes, sorted by id
struct Drop {                       // 0x58 bytes at 0x302140
    Vec3 prev, pos;
    Color3 color;
    int pad3c;
    void *ref;                      // global ref to the count label
    int width;
    int id;
    unsigned char count;
};
struct TickEvt { char pad0[8]; Ctx *ctx; };
struct RenderEvt { char pad0[8]; Vec3 cam; Ctx *ctx; float partial; unsigned char shader; };

extern "C" unsigned long long data_302130;   // count
extern "C" Drop data_302140[100];
extern "C" char data_3043b8[5];              // label buffer
extern "C" Listed data_2f1ce8;               // colour for unlisted items
extern "C" float data_2f1ce0;                // range
extern "C" unsigned char data_2f1d20;        // show unlisted items
extern "C" unsigned char data_2f1ca8;        // merge stacks + labels
extern "C" const double rdata_154320, rdata_154358, rdata_154360;
extern "C" const float rdata_154328, rdata_15432c, rdata_154330, rdata_154350;
extern "C" const v4i rdata_154340;           // sign mask
S(1543cb);   // "%u"
extern "C" void (**rdata_150cc0)();                                 // glPushMatrix
extern "C" void (**rdata_150cb8)();                                 // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);              // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);              // glScalef
extern "C" void (**rdata_150cc8)(float, float, float, float);       // glRotatef
extern "C" void (**rdata_150ca0)(float, float, float);              // glColor3f
extern "C" void (**rdata_13e448)(float, float, float, float);       // glColor4f
extern "C" void (**rdata_13e458)(unsigned);                         // glEnable
extern "C" void (**rdata_13e450)(unsigned);                         // glDisable

FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(void *, java_get_world_2, (void *a, void *world));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(void *, java_jni_is_instance_of_entity_item, (void *a, void *e));
FN(unsigned char, java_get_entity_is_dead, (void *a, void *e));
FN(void *, java_call_entity_item_get_entity_item, (void *a, void *item));
FN(int, java_get_item_stack_stack_size, (void *a, void *stack));
FN(void *, java_call_item_stack_get_item, (void *a, void *stack));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *item));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(void *, libc_bsearch, (const void *key, const void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(int, compare_name_key, (const void *a, const void *b));
FN(void *, java_get_minecraft_6, (void *a, void *p));
FN(void *, java_get_render_manager, (void *a, void *mc));
FN(float, java_get_render_manager_player_view_x, (void *a, void *mc));
FN(float, java_get_render_manager_player_view_y, (void *a, void *mc));
FN(int, java_call_font_renderer_get_string_width, (void *a, void *font, void *str));
FN(void, java_variant_2, (void *a, void *font, void *str, float x, float y, unsigned color, unsigned char shadow));
FN(void, gl_bind_texture, (void *a));
FN(void, gl_clear_color, (void *a));
FN(void, gl_set_color, (void *a, float r, float g, float b, float al));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(void, gl_array_xyz_float, ());
FN(void, gl_array_color1, ());
FN(void, enable_vertex_array, ());

typedef void (*DeleteRef)(void *env, void *ref);
typedef void *(*NewString)(void *env, const char *s);
typedef void *(*NewGlobalRef)(void *env, void *o);

extern "C" void *data_3043a0[2];
extern "C" void *data_3043b0;

FN(void, libc_free, (void *p));

extern "C" void item_esp_shutdown(Hook *h) {
    data_3043a0[0] = data_3043a0[1] = nullptr;
    libc_free(data_3043b0);
    data_3043b0 = nullptr;
}

extern "C" void item_esp_tick(TickEvt *h) {
    Ctx *c = h->ctx;
    if (data_302130) {
        void *env = ((void **)c->a)[1];
        for (unsigned long long i = 0; i < data_302130; i++) {
            if (!data_302140[i].ref)
                continue;
            ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, data_302140[i].ref);
            data_302140[i].ref = nullptr;
        }
    }
    data_302130 = 0;
    if (!c->p20)
        return;
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    void *list = java_get_world_2(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *item = java_jni_is_instance_of_entity_item(c->a, java_call_list_get(c->a, list, i));
        if (!item || java_get_entity_is_dead(c->a, item))
            continue;
        Vec3 pos = java_get_entity_pos_x(c->a, item);
        if (vec3_distance_sq(pos, me) > (double)(data_2f1ce0 * data_2f1ce0))
            continue;
        void *stack = java_call_entity_item_get_entity_item(c->a, item);
        if (!stack)
            continue;
        int count = java_get_item_stack_stack_size(c->a, stack);
        if (count <= 0)
            continue;
        void *it = java_call_item_stack_get_item(c->a, stack);
        if (!it)
            continue;
        int id = java_call_static_item_get_id_from_item(c->a, it);
        Listed *l = data_3043b0 ? (Listed *)libc_bsearch(&id, data_3043b0, (unsigned long long)data_3043a0[0], 0x18, compare_name_key) : nullptr;
        if (!l) {
            if (!data_2f1d20)
                continue;
            l = &data_2f1ce8;
        }
        Drop *d = &data_302140[data_302130];
        d->color = l->color;
        d->id = id;
        d->count = (unsigned char)count;
        d->pos = pos;
        d->prev = java_get_entity_prev_pos_x(c->a, item);
        if (++data_302130 == 100)
            break;
    }
    if (data_2f1ca8 != 1 || !data_302130)
        return;
    void *font = java_get_render_manager(c->a, java_get_minecraft_6(c->a, c->c18));
    if (!font)
        return;
    for (unsigned long long i = 0; i < data_302130; i++) {
        Drop *d = &data_302140[i];
        unsigned long long n = d->count;
        if (!n)
            continue;
        for (unsigned long long j = 0; j < data_302130; j++) {
            Drop *o = &data_302140[j];
            if (i == j || d->id != o->id)
                continue;
            if (rdata_154360 > vec3_distance_sq(d->pos, o->pos)) {
                n += o->count;
                o->count = 0;
            }
        }
        unsigned long long shown = n < 0x63 ? n : 0x63;
        d->count = (unsigned char)shown;
        if (n < 2) {
            d->ref = nullptr;
            continue;
        }
        log_fmt(data_3043b8, 5, rdata_1543cb, (unsigned)shown);
        void *env = ((void **)c->a)[1];
        void *str = ((NewString)(*(void ***)env)[0x538 / 8])(env, data_3043b8);
        if (!str) {
            d->ref = nullptr;
            continue;
        }
        d->ref = ((NewGlobalRef)(*(void ***)env)[0xa8 / 8])(env, str);
        if (d->ref)
            d->width = java_call_font_renderer_get_string_width(c->a, font, d->ref);
    }
}

extern "C" void item_esp_render(RenderEvt *h) {
    if (!data_302130)
        return;
    (*rdata_13e458)(0xbe2);
    double t = h->partial;
    gl_array_xyz_float();
    for (unsigned long long i = 0; i < data_302130; i++) {
        Drop *d = &data_302140[i];
        Vec3 r = vec3_sub(vec3_lerp(d->prev, d->pos, t), h->cam);
        (*rdata_150cc0)();
        (*rdata_150cd8)((float)r.x, (float)(r.y + rdata_154320), (float)r.z);
        (*rdata_150cd0)(rdata_154328, rdata_154328, rdata_154328);
        (*rdata_13e448)(d->color.r, d->color.g, d->color.b, rdata_15432c);
        gl_array_color1();
        (*rdata_150cb8)();
    }
    enable_vertex_array();
    if (data_2f1ca8 != 1)
        return;
    Ctx *c = h->ctx;
    void *mc = java_get_minecraft_6(c->a, c->c18);
    void *font = java_get_render_manager(c->a, mc);
    float pitch = java_get_render_manager_player_view_x(c->a, mc);
    float yaw = java_get_render_manager_player_view_y(c->a, mc);
    t = h->partial;
    if (h->shader == 1) {
        gl_bind_texture(c->a);
        gl_set_color(c->a, rdata_154330, rdata_154330, rdata_154330, 1.0f);
    } else {
        (*rdata_13e458)(0xde1);
        (*rdata_150ca0)(rdata_154330, rdata_154330, rdata_154330);
    }
    yaw = -yaw;
    for (unsigned long long i = 0; i < data_302130; i++) {
        Drop *d = &data_302140[i];
        if (!d->ref)
            continue;
        Vec3 r = vec3_sub(vec3_lerp(d->prev, d->pos, t), h->cam);
        (*rdata_150cc0)();
        (*rdata_150cd8)((float)r.x, (float)(r.y + rdata_154320), (float)r.z);
        (*rdata_150cc8)(yaw, 0.0f, rdata_154330, 0.0f);
        (*rdata_150cc8)(pitch, rdata_154330, 0.0f, 0.0f);
        (*rdata_150cd0)(rdata_154350, rdata_154350, rdata_154350);
        java_variant_2(c->a, font, d->ref, (float)(-d->width * rdata_154358), -4.0f, 0xffffffffu, 1);
        (*rdata_150cb8)();
    }
    if (h->shader)
        gl_clear_color(c->a);
    else
        (*rdata_13e450)(0xde1);
}
