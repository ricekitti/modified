// Name Tags - health / name / distance labels with armour and enchantments.
//   0x3c210 disable: release caches and the global font ref
//   0x3ca20 tick: build the label strings for up to 100 players, sorted
//   0x3c280 render: project and draw them
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Pair { float x, y; };
struct Slot { unsigned char has; char pad1[3]; int count; void **refs; };   // armour piece + enchant labels
struct Tag {                        // 0xa0 bytes at 0x2fc920
    Vec3 prev, pos;
    void *ent;                      // global ref
    void *name;                     // global ref to the label
    int width;
    float prev_dist, dist;
    int armor;                      // pieces worn
    Slot slot[5];
};
struct Builder { alignas(8) char raw[0x20]; };
struct Enchant { unsigned short id, lvl; };
struct RenderEvt { char pad0[8]; Vec3 cam; Ctx *ctx; char pad28[0x18]; float partial; unsigned char shader; };

struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };
typedef void (*DeleteRef)(void *env, void *ref);

extern "C" char data_2f02d0[];           // Name Tags module descriptor
extern "C" void *data_3007a0;

FN(void, entity_list_for_render, (void *a));

extern "C" unsigned long long data_2fc910;     // count
extern "C" Tag data_2fc920[100];
extern "C" char data_3007b0[0x80];             // label buffer
extern "C" Enchant data_300830[25];
extern "C" char data_300894[6];                // enchant label buffer
extern "C" float data_2f0510;                  // scale
extern "C" unsigned char data_2f0530;          // distance scaling
extern "C" unsigned char data_2f0550;          // background
extern "C" unsigned char data_2f0570;          // text shadow
extern "C" unsigned char data_2f0590;          // armour
extern "C" unsigned char data_2f05b0;          // enchantments
extern "C" float data_2f05e8;                  // range
extern "C" unsigned char data_2f0608;          // own name tag
extern "C" unsigned char data_2f0628;          // display name
extern "C" unsigned char data_2f0648;          // health
extern "C" unsigned char data_2f0668;          // distance
extern "C" int data_2f06b0;                    // health format: 1 = hearts
extern "C" const char *const rdata_153870[63]; // enchantment abbreviations
S(1537bb);   // "id"
S(1537be);   // "lvl"
S(1537e7);   // "\xa7%c%.*f"
S(1537f0);   // "\xa76+%.*f"
S(1537f9);   // heart, "\xa7r "
S(153801);   // "\xa7"
S(153804);   // "\xa7r \xa7%c%.0fm"
S(153864);   // "%s %d"
extern "C" const float rdata_15371c, rdata_153720, rdata_153724, rdata_153728, rdata_15372c, rdata_153730, rdata_153734;
extern "C" const float rdata_1536d0, rdata_1536d4, rdata_1536e0, rdata_1536e4, rdata_1536e8, rdata_153700, rdata_153710,
                       rdata_153714, rdata_153718;
extern "C" const double rdata_1536d8, rdata_1536f0, rdata_1536f8, rdata_153708;
extern "C" void (**rdata_150cc0)();                                 // glPushMatrix
extern "C" void (**rdata_150cb8)();                                 // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);              // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);              // glScalef
extern "C" void (**rdata_150ca0)(float, float, float);              // glColor3f
extern "C" void (**rdata_13e448)(float, float, float, float);       // glColor4f
extern "C" void (**rdata_13e458)(unsigned);                         // glEnable
extern "C" void (**rdata_13e450)(unsigned);                         // glDisable
extern "C" void (**rdata_13e438)(unsigned, unsigned);               // glBlendFunc
extern "C" void (**rdata_150c98)(unsigned);                         // glBegin
extern "C" void (**rdata_150ce0)(float, float);                     // glVertex2f
extern "C" void (**rdata_150ca8)();                                 // glEnd

FN(void *, java_get_minecraft_2, (void *a, void *x));
FN(void *, java_new_render_item, (void *a));
FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(Vec3, java_get_entity_prev_pos_x, (void *a, void *p));
FN(void *, java_get_world, (void *a, void *world));
FN(void *, java_get_minecraft_6, (void *a, void *x));
FN(void *, java_get_render_manager, (void *a, void *mc));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(void *, get_net_handler, (void *a, void *x));
FN(int, java_get_game_settings_third_person_view, (void *a, void *gs));
FN(int, entity_flags_get, (void *a, void *e));
FN(unsigned char, java_get_entity_is_dead, (void *a, void *e));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(double, vec3_distance, (Vec3 a, Vec3 b));
FN(void *, java_call_entity_get_formatted_command_sender_name, (void *a, void *e));
FN(void *, java_call_ichat_component_get_formatted_text, (void *a, void *x));
FN(void *, java_call_entity_get_command_sender_name, (void *a, void *e));
FN(void, buffer_init, (Builder *b, char *buf, int cap));
FN(unsigned char, buffer_append, (Builder *b, const char *s));
FN(unsigned char, buffer_printf, (Builder *b, const char *fmt, ...));
FN(unsigned char, buffer_push_char, (Builder *b, char ch));
FN(unsigned char, buffer_terminate, (Builder *b));
FN(float, java_call_entity_living_base_get_health, (void *a, void *e));
FN(float, java_call_entity_player_get_absorption_amount, (void *a, void *e));
FN(float, limit_rotation_delta, (float lo, float hi, float v));
FN(double, libc_sqrt, (double x));
FN(float, libc_sqrtf, (float x));
FN(unsigned char, java_variant, (void *a));
FN(Vec3, vec3_add_eye_height_sneak, (Vec3 v));
FN(int, java_call_font_renderer_get_string_width, (void *a, void *font, void *str));
FN(void *, java_call_entity_player_get_equipment_in_slot, (void *a, void *e, int slot));
FN(void *, java_call_item_stack_get_enchantment_tag_list, (void *a, void *stack));
FN(int, java_call_nbttag_list_tag_count, (void *a, void *list));
FN(void *, java_call_nbttag_list_get_compound_tag_at, (void *a, void *list, int i));
FN(short, java_jni_call_short_method_nbttag_compound_get_short, (void *a, void *tag, void *key));
FN(void, libc_qsort, (void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(int, compare_key_table, (const void *a, const void *b));
FN(int, compare_module_height, (const void *a, const void *b));
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));
FN(void *, java_get_minecraft_3, (void *a, void *x));
FN(float, java_get_render_item_z_level, (void *a, void *ri));
FN(void, java_set_render_item_z_level, (void *a, void *ri, float z));
FN(void *, java_get_minecraft, (void *a, void *x));
FN(float, java_call_entity_renderer_get_fovmodifier, (void *a, void *er, float partial));
FN(Vec3, vec3_lerp, (Vec3 a, Vec3 b, double t));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(unsigned char, project_point_ex, (void *view, Vec3 rel, Pair *screen, float *depth));
FN(void, gl_push_state, (void *a));
FN(void, gl_reset_state_2, (void *a));
FN(void, gl_pop_state, (void *a));
FN(void, gl_bind_texture, (void *a));
FN(void, gl_clear_color, (void *a));
FN(void, java_call_static_gl_state_manager_disable_alpha, (void *a));
FN(void, gl_blend_mode, (void *a, unsigned src, unsigned dst));
FN(void, gl_set_color, (void *a, float r, float g, float b, float al));
FN(void, java_variant_2, (void *a, void *font, void *str, float x, float y, unsigned color, unsigned char shadow));
FN(void, java_call_static_render_helper_enable_standard_item_lighting, (void *a));
FN(void, java_call_static_render_helper_disable_standard_item_lighting, (void *a));
FN(void, java_render_item_init, (void *a, void *ri, void *font, void *ir, void *stack, int x, int y));
FN(void, java_render_item_render_item_overlay_into_gui, (void *a, void *ri, void *font, void *ir, void *stack, int x, int y, const char *text));

typedef unsigned char (*IsSameObject)(void *env, void *a, void *b);
typedef void *(*NewGlobalRef)(void *env, void *o);
typedef void *(*NewString)(void *env, const char *s);
typedef int (*StringLength)(void *env, void *s);
typedef const char *(*StringChars)(void *env, void *s, unsigned char *copy);
typedef void (*ReleaseChars)(void *env, void *s, const char *chars);

#define JNI(env, off, T) ((T)(*(void ***)(env))[(off) / 8])

static inline __attribute__((always_inline)) char tier(float v, float low, float mid) {
    if (low > v)
        return 'c';
    return mid > v ? 'e' : 'a';
}

extern "C" void name_tags_disable(EvHook *h) {
    if (!h->on && h->ev == data_2f02d0) {
        entity_list_for_render(h->ctx->a);
        void *ref = data_3007a0;
        if (ref) {
            void *env = ((void **)h->ctx->a)[1];
            ((DeleteRef)(*(void ***)env)[0xb0 / 8])(env, ref);
            data_3007a0 = nullptr;
        }
    }

}

extern "C" void name_tags_tick(Hook *h) {
    Ctx *c = h->ctx;
    entity_list_for_render(c->a);
    if (!c->p20)
        return;
    if (!data_3007a0) {
        void *env = ((void **)c->a)[1];
        void *ri;
        if (**(int **)c->a) {
            ri = java_get_minecraft_2(c->a, c->c18);
        } else {
            ri = java_new_render_item(c->a);
            if (!ri)
                goto have_ri;
        }
        data_3007a0 = JNI(env, 0xa8, NewGlobalRef)(env, ri);
    }
have_ri:
    Vec3 me = java_get_entity_pos_x(c->a, c->b);
    Vec3 me_prev = java_get_entity_prev_pos_x(c->a, c->b);
    void *list = java_get_world(c->a, c->p20);
    void *env = ((void **)c->a)[1];
    void *font = java_get_render_manager(c->a, java_get_minecraft_6(c->a, c->c18));
    if (!font)
        return;
    void *key_id = JNI(env, 0x538, NewString)(env, rdata_1537bb);
    void *key_lvl = JNI(env, 0x538, NewString)(env, rdata_1537be);
    if (!key_id || !key_lvl)
        return;
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *e = java_call_list_get(c->a, list, i);
        unsigned char self = JNI(env, 0xc0, IsSameObject)(env, c->b, e);
        if (self && (data_2f0608 != 1 || !java_get_game_settings_third_person_view(c->a, get_net_handler(c->a, c->c18))))
            continue;                           // own tag only in third person
        int flags = entity_flags_get(c->a, e);
        if (flags & 2)
            continue;
        if (java_get_entity_is_dead(c->a, e))
            continue;
        Vec3 pos = java_get_entity_pos_x(c->a, e);
        double d2 = vec3_distance_sq(pos, me);
        if (d2 > (double)(data_2f05e8 * data_2f05e8))
            continue;
        void *env2 = ((void **)c->a)[1];
        void *label;
        void *name;
        if (data_2f0628 == 1) {
            name = java_call_ichat_component_get_formatted_text(c->a, java_call_entity_get_formatted_command_sender_name(c->a, e));
            label = name;
            if (!data_2f0648 && !data_2f0668)
                goto have_label;
        } else {
            name = java_call_entity_get_command_sender_name(c->a, e);
        }
        {
            label = name;
            int len = JNI(env2, 0x540, StringLength)(env2, name);
            int n = len < 0x40 ? len : 0x40;
            const char *chars = JNI(env2, 0x548, StringChars)(env2, name, nullptr);
            Builder sb;
            buffer_init(&sb, data_3007b0, 0x80);
            if (data_2f0648 == 1) {
                float hp = limit_rotation_delta(0.0f, rdata_15371c, java_call_entity_living_base_get_health(c->a, e));
                float abs = limit_rotation_delta(0.0f, rdata_153720, java_call_entity_player_get_absorption_amount(c->a, e));
                char col = tier(hp, rdata_153728, rdata_153724);
                int hearts = data_2f06b0 == 1;
                if (hearts) {
                    hp *= rdata_15372c;
                    abs *= rdata_15372c;
                }
                if (!buffer_printf(&sb, rdata_1537e7, col, hearts, (double)hp))
                    goto have_label;
                if (abs > 0.0f && !buffer_printf(&sb, rdata_1537f0, hearts, (double)abs))
                    goto have_label;
                if (!buffer_append(&sb, rdata_1537f9))
                    goto have_label;
            }
            if (data_2f0628 == 1) {
                if (!buffer_append(&sb, chars))
                    goto have_label;
            } else {
                if (!buffer_append(&sb, rdata_153801))
                    goto have_label;
                if (!buffer_push_char(&sb, (flags & 5) == 0 ? 'c' : 'a'))
                    goto have_label;
                for (int k = 0; len > 0 && k < n;) {
                    if ((unsigned char)chars[k] == 0xc2) {   // drop colour codes
                        k += 3;
                        continue;
                    }
                    if (!buffer_push_char(&sb, chars[k]))
                        goto have_label;
                    k += 1;
                }
            }
            if (data_2f0668 == 1 && !JNI(env, 0xc0, IsSameObject)(env, c->b, e)) {
                double d;
                if (d2 >= 0.0)
                    d = __builtin_sqrt(d2);
                else
                    d = libc_sqrt(d2);
                if (!buffer_printf(&sb, rdata_153804, tier((float)d, rdata_153734, rdata_153730), d))
                    goto have_label;
            }
            JNI(env2, 0x550, ReleaseChars)(env2, name, chars);
            if (!buffer_terminate(&sb))
                goto have_label;
            label = JNI(env2, 0x538, NewString)(env2, data_3007b0);
        }
    have_label:
        if (!label)
            break;
        {
            void *name_ref = JNI(env2, 0xa8, NewGlobalRef)(env2, label);
            if (!name_ref)
                break;
            void *ent_ref = JNI(env, 0xa8, NewGlobalRef)(env, e);
            if (!ent_ref) {
                JNI(env, 0xb0, DeleteRef)(env, name_ref);
                break;
            }
            Vec3 prev = java_get_entity_prev_pos_x(c->a, e);
            if (self && java_variant(c->a)) {
                pos = vec3_add_eye_height_sneak(pos);
                prev = vec3_add_eye_height_sneak(prev);
            }
            Tag *t = &data_2fc920[data_2fc910++];
            t->pos = pos;
            t->prev = prev;
            t->ent = ent_ref;
            float f2 = (float)d2;

            float dist;
            if (f2 >= 0.0f)
                dist = __builtin_sqrtf(f2);
            else
                dist = libc_sqrtf(f2);
            t->dist = dist;
            t->prev_dist = (float)vec3_distance(me_prev, t->prev);
            t->name = name_ref;
            t->width = java_call_font_renderer_get_string_width(c->a, font, name_ref);
            t->armor = 0;
            for (int k = 0; k < 5; k++) {
                unsigned char has = java_call_entity_player_get_equipment_in_slot(c->a, e, k) != nullptr;
                if (has)
                    t->armor += 1;
                t->slot[k].has = has;
            }
            if (data_2f05b0 == 1) {
                void *env3 = ((void **)c->a)[1];
                for (int k = 0; k < 5; k++) {
                    Slot *sl = &t->slot[k];
                    sl->refs = nullptr;
                    sl->count = 0;
                    void *stack = java_call_entity_player_get_equipment_in_slot(c->a, e, k);
                    if (!stack)
                        continue;
                    void *tags = java_call_item_stack_get_enchantment_tag_list(c->a, stack);
                    if (!tags)
                        continue;
                    int m = java_call_nbttag_list_tag_count(c->a, tags);
                    int cnt = m < 0x19 ? m : 0x19;
                    for (int j = 0; m > 0 && j < cnt; j++) {
                        void *tag = java_call_nbttag_list_get_compound_tag_at(c->a, tags, j);
                        unsigned short id = java_jni_call_short_method_nbttag_compound_get_short(c->a, tag, key_id);
                        if (id > 0x3e || (0x1ff0fff0ffc0fe00ull >> id & 1))
                            continue;           // unknown enchantment
                        short lvl = java_jni_call_short_method_nbttag_compound_get_short(c->a, tag, key_lvl);
                        lvl = lvl > 0 ? lvl : 0;
                        data_300830[j].id = id;
                        data_300830[j].lvl = lvl < 0x63 ? lvl : 0x63;
                    }
                    libc_qsort(data_300830, (long long)cnt, 4, compare_key_table);
                    int shown = m < 4 ? m : 4;
                    sl->refs = (void **)libc_calloc((long long)shown, 8);
                    if (!sl->refs)
                        goto next;
                    for (int j = 0; m > 0 && j < shown; j++) {
                        unsigned id = data_300830[j].id;
                        const char *abbr = id <= 0x3e ? rdata_153870[id] : nullptr;
                        log_fmt(data_300894, 6, rdata_153864, abbr, (int)(short)data_300830[j].lvl);
                        void *str = JNI(env3, 0x538, NewString)(env3, data_300894);
                        if (!str)
                            goto next;
                        void *ref = JNI(env3, 0xa8, NewGlobalRef)(env3, str);
                        if (!ref)
                            goto next;
                        sl->refs[j] = ref;
                        sl->count += 1;
                    }
                }
            }
        }
    next:
        if (data_2fc910 == 100)
            break;
    }
    libc_qsort(data_2fc920, data_2fc910, sizeof(Tag), compare_module_height);
}

extern "C" void name_tags_render(RenderEvt *h) {
    if (!data_2fc910 || !data_3007a0)
        return;
    Ctx *c = h->ctx;
    void *font = java_get_render_manager(c->a, java_get_minecraft_6(c->a, c->c18));
    void *ir = java_get_minecraft_3(c->a, c->c18);
    float saved_z = java_get_render_item_z_level(c->a, data_3007a0);
    java_set_render_item_z_level(c->a, data_3007a0, rdata_1536d0);
    float k = rdata_1536d4 / java_call_entity_renderer_get_fovmodifier(c->a, java_get_minecraft(c->a, c->c18), h->partial);
    const float one = rdata_1536e8;
    for (unsigned long long i = 0; i < data_2fc910; i++) {
        Tag *t = &data_2fc920[i];
        Vec3 p = vec3_lerp(t->prev, t->pos, (double)h->partial);
        p.y += rdata_1536d8;
        Pair s;
        float depth;
        if (!project_point_ex(&h->cam, vec3_sub(p, h->cam), &s, &depth))
            continue;
        (*rdata_150cc0)();
        (*rdata_150cd8)(s.x, s.y, depth * rdata_1536e0);
        float sc = depth * rdata_1536e4;
        if (data_2f0530)
            sc = sc > one ? sc : one;
        float scale = data_2f0510 * k * sc;
        (*rdata_150cd0)(scale, scale, scale);
        double x0d = (double)(-t->width) * rdata_1536f0 + rdata_1536f8;
        if (h->shader == 1)
            gl_push_state(c->a);
        else
            (*rdata_13e450)(0xb71);             // GL_DEPTH_TEST
        float x0 = (float)x0d;
        if (data_2f0550 == 1) {
            if (h->shader == 1) {
                gl_clear_color(c->a);
                gl_set_color(c->a, 0.0f, 0.0f, 0.0f, 0.5f);
            } else {
                (*rdata_13e450)(0xde1);
                (*rdata_13e448)(0.0f, 0.0f, 0.0f, rdata_153700);
            }
            float x1 = (float)((double)t->width * rdata_1536f0 + rdata_153708);
            (*rdata_150c98)(5);                 // GL_TRIANGLE_STRIP
            (*rdata_150ce0)(x0, 0.0f);
            (*rdata_150ce0)(x1, 0.0f);
            (*rdata_150ce0)(x0, rdata_153710);
            (*rdata_150ce0)(x1, rdata_153710);
            (*rdata_150ca8)();
        }
        if (h->shader == 1) {
            gl_bind_texture(c->a);
            gl_set_color(c->a, one, one, one, 1.0f);
        } else {
            (*rdata_13e458)(0xde1);
            (*rdata_150ca0)(one, one, one);
        }
        java_variant_2(c->a, font, t->name, x0 + rdata_153714, -10.0f, 0xffffffffu, data_2f0570);
        if (data_2f0590 == 1) {
            if (h->shader == 1)
                gl_reset_state_2(c->a);
            else
                (*rdata_13e458)(0xb71);
            java_call_static_render_helper_enable_standard_item_lighting(c->a);
            (*rdata_150cd0)(one, one, rdata_153718);
            int x = -(t->armor * 8);
            for (int k2 = 0; k2 < 5; k2++) {
                if (t->slot[k2].has != 1)
                    continue;
                void *stack = java_call_entity_player_get_equipment_in_slot(c->a, t->ent, k2);
                if (!stack)
                    continue;
                java_render_item_init(c->a, data_3007a0, font, ir, stack, x, -32);
                java_render_item_render_item_overlay_into_gui(c->a, data_3007a0, font, ir, stack, x, -32, nullptr);
                x += 16;
            }
            java_call_static_render_helper_disable_standard_item_lighting(c->a);
            if (h->shader) {
                gl_pop_state(c->a);
                java_call_static_gl_state_manager_disable_alpha(c->a);
                gl_blend_mode(c->a, 0x302, 0x303);
            } else {
                (*rdata_13e458)(0xbe2);
                (*rdata_13e450)(0xbc0);
                (*rdata_13e438)(0x302, 0x303);
            }
            (*rdata_150ca0)(one, one, one);
            (*rdata_150cd0)(rdata_153700, rdata_153700, rdata_153700);
            if (data_2f05b0 == 1) {
                int bx = -(t->armor * 8);
                for (int k2 = 0; k2 < 5; k2++) {
                    Slot *sl = &t->slot[k2];
                    if (sl->has != 1)
                        continue;
                    if (sl->refs && sl->count > 0) {
                        float lx = (float)(bx + bx);
                        int y = -64;
                        for (int j = 0; j < sl->count; j++, y += 8)
                            java_variant_2(c->a, font, sl->refs[j], lx, (float)y, 0xffffffffu, 1);
                    }
                    bx += 16;
                }
            }
        }
        (*rdata_150cb8)();
    }
    if (h->shader) {
        gl_clear_color(c->a);
        gl_push_state(c->a);
    } else {
        (*rdata_13e450)(0xde1);
        (*rdata_13e450)(0xb71);
    }
    java_set_render_item_z_level(c->a, data_3007a0, saved_z);
}
