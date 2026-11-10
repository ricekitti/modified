// 0xc000: named profiles (non-global modules) load/save via cJSON
#include "slinky/engine.h"
struct cJSON;
struct Mod { char pad[0x18]; unsigned char flags; };
struct Mods { Mod **data; unsigned long long count; };
FN(cJSON *, cjson_parse, (const char *s));     // cJSON_Parse
FN(void, cjson_delete, (cJSON *o));             // cJSON_Delete
FN(void, cjson_free_string, (void *p));              // cJSON_free
FN(cJSON *, cjson_create_object, ());                  // cJSON_CreateObject
FN(char *, cjson_print_unformatted, (const cJSON *o));     // cJSON_PrintUnformatted
FN(unsigned char, load_module_settings, (cJSON *root, Mods *mods, Mod *m));
FN(unsigned char, save_module_settings, (cJSON *root, Mod *m));
FN(int, settings_path, (char **path));
FN(int, settings_path_append, (char **path, const char *suffix));
FN(int, settings_path_ensure, (char **path));
FN(void, free_path, (char **path));
FN(int, write_text_file, (char **path, const char *data, bool text));
FN(void, set_saved_string, (const char *s));
extern "C" const char rdata_13ec08[], rdata_13ec30[];
extern "C" unsigned char data_2f7338;

extern "C" bool load_module_states(const char *text, Mods *mods) {
    cJSON *root = cjson_parse(text);
    if (!root) return false;
    bool ok = true;
    for (unsigned long long i = 0; i < mods->count; i++) {
        Mod *m = mods->data[i];
        if (m->flags & 0x10) continue;
        if (!load_module_settings(root, mods, m)) { ok = false; break; }
    }
    cjson_delete(root);
    return ok;
}
extern "C" char *serialize_module_states(Mods *mods) {
    cJSON *root = cjson_create_object();
    char *out = 0;
    if (!root) goto done;
    for (unsigned long long i = 0; i < mods->count; i++) {
        Mod *m = mods->data[i];
        if (m->flags & 0x10) continue;
        if (!save_module_settings(root, m)) goto done;
    }
    out = cjson_print_unformatted(root);
done:
    cjson_delete(root);
    return out;
}
extern "C" int save_profile(const char *name, bool text, Mods *mods) {
    char *path;
    int r = settings_path(&path);
    if (r) return r;
    if ((r = settings_path_append(&path, rdata_13ec08)) != 0) goto clean;
    if ((r = settings_path_ensure(&path)) != 0) goto clean;
    if ((r = settings_path_append(&path, rdata_13ec30)) != 0) goto clean;
    if ((r = settings_path_append(&path, name)) != 0) goto clean;
    {
        char *json = serialize_module_states(mods);
        if (!json) {
            r = 0x1388;
        } else {
            r = write_text_file(&path, json, text);
            if (!r) {
                set_saved_string(name);
                data_2f7338 = 0;
            }
            cjson_free_string(json);
        }
    }
clean:
    free_path(&path);
    return r;
}

// ---- setting visitors: mode 0 = load from json, 1 = save to json, 2 = remember, 3 = restore ----
// cflags-free: the original used a 4-entry jump table for the mode switch
struct Visit { cJSON *json; int mode; unsigned char failed; };
FN(cJSON *, cjson_get_object_item, (const cJSON *o, const char *name));      // cJSON_GetObjectItemCaseSensitive
FN(int, cjson_is_bool, (const cJSON *o));                            // cJSON_IsBool
FN(int, cjson_is_true, (const cJSON *o));                            // cJSON_IsTrue
FN(int, cjson_is_number, (const cJSON *o));                            // cJSON_IsNumber
FN(double, cjson_get_number_value, (const cJSON *o));                         // cJSON_GetNumberValue
FN(int, cjson_is_object, (const cJSON *o));                            // cJSON_IsObject
FN(cJSON *, cjson_add_item_to_object, (cJSON *o, const char *name, int b));     // cJSON_AddBoolToObject
FN(cJSON *, cjson_add_number_to_object, (cJSON *o, const char *name, double v));  // cJSON_AddNumberToObject
FN(cJSON *, cjson_add_bool_to_object, (cJSON *o, const char *name));            // cJSON_AddObjectToObject
extern "C" const char rdata_13ec39[], rdata_13ec3d[];   // "min", "max"
struct BoolSet { char pad[0x18]; unsigned char value, saved; };
extern "C" void visit_bool_setting(Visit *v, const char *name, BoolSet *s) {
    if (v->failed) return;
    switch (v->mode) {
    case 0: {
        unsigned char b = s->saved;
        cJSON *it = cjson_get_object_item(v->json, name);
        if (cjson_is_bool(it)) b = cjson_is_true(it) != 0;
        s->value = b;
        break;
    }
    case 2: s->saved = s->value; break;
    case 3: s->value = s->saved; break;
    case 1: {
        cJSON *j = v->json;

        v->failed |= cjson_add_item_to_object(j, name, s->value) == 0;
        break;
    }
    }
}
struct FloatSet { char pad[0x28]; float min, max, value, saved; };
extern "C" void visit_float_setting(Visit *v, const char *name, FloatSet *s) {
    if (v->failed) return;
    int mode = v->mode;
    if (mode == 0) {
        cJSON *it = cjson_get_object_item(v->json, name);
        float r = s->saved, lo = s->min, hi = s->max;
        if (cjson_is_number(it)) {
            float d = (float)cjson_get_number_value(it);
            if (d == d) {
                r = lo;
                if (!(lo > d)) {
                    r = d;
                    if (d > hi) r = hi;
                }
            }
        }
        s->value = r;
    } else {

    if (mode == 1) {
        cJSON *j = v->json;

        v->failed |= cjson_add_number_to_object(j, name, (double)s->value) == 0;
    } else if (mode == 2) {
        s->saved = s->value;
    } else if (mode == 3) {
        s->value = s->saved;
    }
    }
}
struct Range { float min, max; };
struct RangeSet { char pad[0x38]; float lo, hi; Range cur, saved; };
static inline __attribute__((always_inline)) float clampf(float x, float lo, float hi) {
    float r = lo;
    if (!(lo > x)) {
        r = x;
        if (x > hi) r = hi;
    }
    return r;
}
extern "C" void visit_range_setting(Visit *v, const char *name, RangeSet *s) {
    if (v->failed) return;
    int mode = v->mode;
    if (mode == 0) {
        cJSON *it = cjson_get_object_item(v->json, name);
        Range r = s->saved;
        float lo = s->lo, hi = s->hi;
        if (cjson_is_object(it)) {
            float mn = (float)cjson_get_number_value(cjson_get_object_item(it, rdata_13ec39));
            if (mn == mn) {
                float mx = (float)cjson_get_number_value(cjson_get_object_item(it, rdata_13ec3d));
                if (mx == mx) {
                    float a = mx < mn ? mx : mn;
                    r.min = clampf(a, lo, hi);
                    r.max = clampf(mx, lo, hi);
                }
            }
        }
        s->cur = r;
    } else {

    if (mode == 1) {
        cJSON *o = cjson_add_bool_to_object(v->json, name);
        if (!o) { v->failed = 1; return; }
        v->failed |= cjson_add_number_to_object(o, rdata_13ec39, (double)s->cur.min) == 0;
        v->failed |= cjson_add_number_to_object(o, rdata_13ec3d, (double)s->cur.max) == 0;
    } else if (mode == 2) {
        s->saved = s->cur;
    } else if (mode == 3) {
        s->cur = s->saved;
    }
    }
}

// ---- current profile name / dirty flag, profile file helpers ----
extern "C" char *data_2f7340;
extern "C" char *get_saved_string() { return data_2f7340; }
extern "C" unsigned char get_profile_dirty() { return data_2f7338; }
extern "C" __attribute__((noinline)) void set_profile_dirty(unsigned char v) { data_2f7338 = v; }
FN(int, delete_file_path, (char **path));
FN(int, open_path_explorer, (char **path));
FN(int, find_files, (char **path, void *a, void *b));
extern "C" int delete_profile_file(const char *name) {   // delete profile file
    char *path;
    int r = settings_path(&path);
    if (r) return r;
    if ((r = settings_path_append(&path, rdata_13ec08)) == 0 && (r = settings_path_append(&path, rdata_13ec30)) == 0 &&
        (r = settings_path_append(&path, name)) == 0)
        r = delete_file_path(&path);
    free_path(&path);
    return r;
}
extern "C" int open_profiles_folder() {   // open the profiles folder
    char *path;
    int r = settings_path(&path);
    if (r) return r;
    if ((r = settings_path_append(&path, rdata_13ec08)) == 0 && (r = settings_path_append(&path, rdata_13ec30)) == 0)
        r = open_path_explorer(&path);
    free_path(&path);
    return r;
}
extern "C" int list_profiles(void *a, void *b) {   // list profiles
    char *path;
    int r = settings_path(&path);
    if (r) return r;
    if ((r = settings_path_append(&path, rdata_13ec08)) == 0 && (r = settings_path_ensure(&path)) == 0 &&
        (r = settings_path_append(&path, rdata_13ec30)) == 0 && (r = settings_path_ensure(&path)) == 0)
        r = find_files(&path, a, b);
    free_path(&path);
    return r;
}

FN(int, cjson_is_string, (const cJSON *o));                            // cJSON_IsString
FN(cJSON *, cjson_add_string_to_object, (cJSON *o, const char *name, const char *s));
extern "C" int libc_strcmp(const char *a, const char *b);      // strcmp
struct JStr { char pad[0x20]; const char *valuestring; };
struct ChoiceSet { char pad[0x20]; unsigned count; char pad2[4]; const char **names; unsigned value, saved; };
extern "C" void visit_choice_setting(Visit *v, const char *name, ChoiceSet *s) {
    if (v->failed) return;
    int mode = v->mode;
    if (mode == 0) {
        unsigned r = s->saved;
        cJSON *it = cjson_get_object_item(v->json, name);
        if (cjson_is_string(it)) {
            unsigned long long n = s->count;
            for (unsigned long long i = 0; i < n; i++)
                if (libc_strcmp(s->names[i], ((JStr *)it)->valuestring) == 0) { r = (unsigned)i; break; }
        }
        s->value = r;
    } else {

        if (mode == 1) v->failed |= cjson_add_string_to_object(v->json, name, s->names[s->value]) == 0;
        else if (mode == 2) s->saved = s->value;
        else if (mode == 3) s->value = s->saved;
    }
}
struct Color { float r, g, b; };
extern "C" const float rdata_13ebe8;   // 1.0
extern "C" const char rdata_13ec41[], rdata_13ec43[], rdata_13ec45[];
static inline __attribute__((always_inline)) bool unit(float x) {

    return x >= 0.0f && !(x > rdata_13ebe8);
}
extern "C" Color parse_color(const cJSON *o, const Color *def) {
    if (cjson_is_object(o)) {
        float r = (float)cjson_get_number_value(cjson_get_object_item(o, rdata_13ec41));
        if (unit(r)) {
            float g = (float)cjson_get_number_value(cjson_get_object_item(o, rdata_13ec43));
            if (unit(g)) {
                float b = (float)cjson_get_number_value(cjson_get_object_item(o, rdata_13ec45));
                if (unit(b)) return Color{r, g, b};
            }
        }
    }
    return *def;
}
struct ColorSet { char pad[8]; Color cur, saved; };
extern "C" void visit_color_setting(Visit *v, const char *name, ColorSet *s) {
    if (v->failed) return;
    int mode = v->mode;
    if (mode == 0) {
        cJSON *it = cjson_get_object_item(v->json, name);
        Color def = s->saved;
        s->cur = parse_color(it, &def);
    } else {

        if (mode == 1) {
            cJSON *o = cjson_add_bool_to_object(v->json, name);
            if (!o) { v->failed = 1; return; }
            v->failed |= cjson_add_number_to_object(o, rdata_13ec41, (double)s->cur.r) == 0;
            v->failed |= cjson_add_number_to_object(o, rdata_13ec43, (double)s->cur.g) == 0;
            v->failed |= cjson_add_number_to_object(o, rdata_13ec45, (double)s->cur.b) == 0;
        } else if (mode == 2) {
            s->saved = s->cur;
        } else if (mode == 3) {
            s->cur = s->saved;
        }
    }
}

// ---- per-module json: "__on", "__bind", "__hold" and the module's own settings visitor ----
struct MCfg { const char *name; char pad[0xc]; unsigned char enabled; char pad2[3]; unsigned flags; unsigned bind;
              unsigned char hold; char pad3[0x1df]; void (*visit)(Visit *); };
FN(void, queue_module_toggle, (Mods *mods, MCfg *m, unsigned char on));
FN(void, apply_module_state, (Mods *mods, MCfg *m, int key));
extern "C" const char rdata_13ec65[], rdata_13ec6a[], rdata_13ec71[];
extern "C" const double rdata_13ec00;   // 0.5
struct JNum { char pad[0x30]; double vd; };
extern "C" unsigned char load_module_settings(cJSON *root, Mods *mods, Mod *mm) {
    MCfg *m = (MCfg *)mm;
    cJSON *o = cjson_get_object_item(root, m->name);
    unsigned f = m->flags;
    if (!(f & 2)) {
        queue_module_toggle(mods, m, cjson_is_true(cjson_get_object_item(o, rdata_13ec65)) != 0);
        f = m->flags;
    }
    if (!(f & 1)) {
        cJSON *b = cjson_get_object_item(o, rdata_13ec6a);
        int key;
        if (cjson_is_number(b)) {
            int k = (int)(((JNum *)b)->vd + rdata_13ec00);
            key = 0;
            if ((unsigned)(k - 0x68) >= (unsigned)-0x67) key = k;
        } else {
            key = 0;
        }
        apply_module_state(mods, m, key);
        m->hold = (m->flags & 8) ? 0 : cjson_is_true(cjson_get_object_item(o, rdata_13ec71)) != 0;
    }
    if (!m->visit) return true;
    Visit vv = {o, 0, 0};
    m->visit(&vv);
    return !vv.failed;
}
extern "C" unsigned char save_module_settings(cJSON *root, Mod *mm) {
    MCfg *m = (MCfg *)mm;
    cJSON *o = cjson_add_bool_to_object(root, m->name);
    unsigned f = m->flags;
    if (!(f & 2) && m->enabled == 1) {
        if (!cjson_add_item_to_object(o, rdata_13ec65, 1)) return false;
        f = m->flags;
    }
    if (!(f & 1)) {
        if (m->bind) {
            if (!cjson_add_number_to_object(o, rdata_13ec6a, (double)m->bind)) return false;
            f = m->flags;
        }
        if (!(f & 8) && m->hold == 1 && !cjson_add_item_to_object(o, rdata_13ec71, 1)) return false;
    }
    if (!m->visit) return true;
    Visit vv = {o, 1, 0};
    m->visit(&vv);
    return !vv.failed;
}

// ---- ImGui widget wrappers: mark the profile dirty when a widget changed its setting ----
FN(unsigned char, draw_label, (void *b, void *a, const char *label, void *s, void *value));
FN(unsigned char, draw_slider_label, (void *b, void *a, void *p10, void *p18, void *s, float f28, float f2c, float f20, float f24, void *value));
FN(unsigned char, draw_slider_with_label, (void *b, void *a, void *p18, void *p20, void *p28, void *s, float f38, float f3c, float f30,
                          float f34, void *v40, void *v44));
struct Wid { char pad[0x10]; void *p10, *p18, *p20, *p28; float f30, f34, f38, f3c; char v40[4], v44[4]; };
extern "C" void widget_bool(void *a, void *b, Wid *s) {
    if (draw_label(b, a, (const char *)s->p10, s, &s->p18)) set_profile_dirty(1);
}
extern "C" void widget_slider(void *a, void *b, Wid *s) {
    float *f = (float *)&s->p20;
    if (draw_slider_label(b, a, s->p10, s->p18, s, f[2], f[3], f[0], f[1], &s->f30)) set_profile_dirty(1);
}
extern "C" void widget_range(void *a, void *b, Wid *s) {
    if (draw_slider_with_label(b, a, s->p18, s->p20, s->p28, s, s->f38, s->f3c, s->f30, s->f34, s->v40, s->v44)) set_profile_dirty(1);
}

// ---- list setting (names, optionally with a colour each) ----
FN(cJSON *, cjson_add_array_to_object, (cJSON *o, const char *name));           // cJSON_AddArrayToObject
FN(cJSON *, cjson_create_string, (const char *s));                        // cJSON_CreateString
FN(int, cjson_add_item_to_array, (cJSON *arr, cJSON *item));                  // cJSON_AddItemToArray
FN(int, cjson_is_array, (const cJSON *o));                           // cJSON_IsArray
extern "C" const char rdata_13ec47[], rdata_13ec4c[];      // "name", "color"
extern "C" const double rdata_13ebf0;                       // {1.0f, 1.0f}
struct JNode2 { JNode2 *next, *prev, *child; int type; const char *vs; };
struct ListSet {
    char pad[0x18]; int kind; int pad2; void *ctx;
    unsigned long long (*count)(void *ctx);
    const char *(*name)(void *ctx, unsigned long long i);
    const float *(*color)(void *ctx, unsigned long long i);
    void (*add)(void *ctx, const char *name, Color *c);
    void *p48;
    void (*clear)(void *ctx);
};
extern "C" void visit_list_setting(Visit *v, const char *name, ListSet *s) {
    if (v->failed) return;
    int mode = v->mode;
    if (mode == 0) {
        s->clear(s->ctx);
        JNode2 *arr = (JNode2 *)cjson_get_object_item(v->json, name);
        unsigned char notarr = !cjson_is_array((cJSON *)arr);
        if ((arr == 0) | notarr) return;
        for (JNode2 *e = arr->child; e; e = e->next) {
            if (s->kind == 1) {
                if (!cjson_is_object((cJSON *)e)) continue;
                JNode2 *nm = (JNode2 *)cjson_get_object_item((cJSON *)e, rdata_13ec47);
                if (!cjson_is_string((cJSON *)nm)) continue;
                cJSON *col = cjson_get_object_item((cJSON *)e, rdata_13ec4c);
                void *ctx = s->ctx;
                auto add = s->add;
                const char *str = nm->vs;
                Color def;
                *(double *)&def = rdata_13ebf0;
                def.b = 1.0f;
                Color c = parse_color(col, &def);
                add(ctx, str, &c);
            } else if (s->kind == 0) {
                if (!cjson_is_string((cJSON *)e)) continue;
                Color c = {0, 0, 0};
                s->add(s->ctx, e->vs, &c);
            }
        }
    } else if (mode == 1) {
        cJSON *arr = cjson_add_array_to_object(v->json, name);
        if (!arr) { v->failed = 1; return; }
        if (!s->count(s->ctx)) return;
        for (unsigned long long i = 0; i < s->count(s->ctx); i++) {
            unsigned char bad;
            if (s->kind == 1) {
                bad = 1;
                cJSON *o = cjson_create_object();
                if (!o) goto fail;
                if (!cjson_add_string_to_object(o, rdata_13ec47, s->name(s->ctx, i))) goto fail;
                {
                    cJSON *c = cjson_add_bool_to_object(o, rdata_13ec4c);
                    if (!c) goto fail;
                    const float *col = s->color(s->ctx, i);
                    if (!cjson_add_number_to_object(c, rdata_13ec41, (double)col[0]) || !cjson_add_number_to_object(c, rdata_13ec43, (double)col[1]) ||
                        !cjson_add_number_to_object(c, rdata_13ec45, (double)col[2]))
                        goto fail;
                    bad = cjson_add_item_to_array(arr, o) == 0;
                }
            } else if (s->kind == 0) {
                cJSON *str = cjson_create_string(s->name(s->ctx, i));
                bad = str ? cjson_add_item_to_array(arr, str) == 0 : 1;
            } else {
                continue;
            }
        fail:
            v->failed |= bad;
        }
    } else if (mode == 3) {
        s->clear(s->ctx);
    }
}

FN(void, libc_free, (void *p));   // free
extern "C" void clear_saved_string() {
    if (data_2f7340) libc_free(data_2f7340);
    data_2f7340 = 0;
}
