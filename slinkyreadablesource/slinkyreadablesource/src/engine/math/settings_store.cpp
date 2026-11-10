// 0xb000: settings profile load/save (cJSON), saved-string helper
#include "slinky/engine.h"
struct cJSON;
FN(cJSON *, cjson_parse, (const char *s));                     // cJSON_Parse
FN(cJSON *, cjson_get_object_item, (const cJSON *o, const char *name));  // cJSON_GetObjectItemCaseSensitive
FN(int, cjson_is_array, (const cJSON *o));                        // cJSON_IsArray
FN(char *, cjson_get_string_value, (const cJSON *o));                     // cJSON_GetStringValue
FN(int, cjson_is_number, (const cJSON *o));                        // cJSON_IsNumber
FN(void, cjson_delete, (cJSON *o));                             // cJSON_Delete
FN(void, cjson_free_string, (void *p));                              // cJSON_free
struct JNode { JNode *next, *prev, *child; int type; char *vs; int vi; double vd; char *key; };
struct Mod { char pad[0x18]; unsigned char flags; };
struct Mods { Mod **data; unsigned long long count; };
FN(unsigned char, load_module_settings, (cJSON *root, Mods *mods, Mod *m));
FN(void *, find_profile, (void *ctx, const char *name));
typedef void (*SetBind)(void *mod, int key);
FN(SetBind, profile_set_bind, (void *ctx));
extern "C" const char rdata_13ec52[], rdata_13ec47[], rdata_13ec60[], rdata_13ec08[], rdata_13ec10[];
extern "C" const double rdata_13eb90;

extern "C" bool load_module_states_from_text(const char *text, Mods *mods, void *ctx) {
    cJSON *root = cjson_parse(text);
    if (!root) return false;
    bool ok;
    for (unsigned long long i = 0; i < mods->count; i++) {
        Mod *m = mods->data[i];
        if (!(m->flags & 0x10)) continue;
        if (!load_module_settings(root, mods, m)) { ok = false; goto out; }
    }
    {
        cJSON *binds = cjson_get_object_item(root, rdata_13ec52);
        unsigned char notarr = !cjson_is_array(binds);

        unsigned char nul = binds == 0;

        ok = true;
        if (nul | notarr) goto out;
        for (JNode *e = ((JNode *)binds)->child; e; e = e->next) {
            cJSON *nm = cjson_get_object_item((cJSON *)e, rdata_13ec47);
            if (!nm) continue;
            char *name = cjson_get_string_value(nm);
            if (!name) continue;
            void *mod = find_profile(ctx, name);
            if (!mod) continue;
            JNode *b = (JNode *)cjson_get_object_item((cJSON *)e, rdata_13ec60);
            int key = 0;
            if (cjson_is_number((cJSON *)b)) {
                int v = (int)(b->vd + rdata_13eb90);
                key = 0;
                if ((unsigned)(v - 0x68) >= (unsigned)-0x67) {  key = v; }
            }
            profile_set_bind(ctx)(mod, key);
        }
    }
out:
    cjson_delete(root);
    return ok;
}

FN(int, settings_path, (char **path));
FN(int, settings_path_append, (char **path, const char *suffix));
FN(int, settings_path_ensure, (char **path));
FN(void, free_path, (char **path));
FN(int, write_text_file, (char **path, const char *data, bool text));
FN(char *, serialize_settings, (Mods *mods, void *ctx));
extern "C" int save_settings(void *a, void *b) {
    char *path;
    int r = settings_path(&path);
    if (r) return r;
    r = settings_path_append(&path, rdata_13ec08);
    if (!r) r = settings_path_ensure(&path);
    if (!r) r = settings_path_append(&path, rdata_13ec10);
    if (!r) {
        char *json = serialize_settings((Mods *)a, b);
        if (!json) {
            r = 0x1388;
        } else {
            r = write_text_file(&path, json, true);
            cjson_free_string(json);
        }
    }
    free_path(&path);
    return r;
}

extern "C" char *data_2f7340;
FN(void *, libc_malloc, (unsigned long long n));   // malloc
FN(void *, libc_memcpy, (void *d, const void *s, unsigned long long n));   // memcpy
FN(unsigned long long, libc_strlen, (const char *s));
FN(void, libc_free, (void *p));
extern "C" void set_saved_string(const char *s) {
    if (data_2f7340) libc_free(data_2f7340);
    if (!s) {
        data_2f7340 = 0;
        return;
    }
    unsigned long long n = libc_strlen(s);
    char *p = (char *)libc_malloc(n + 1);
    data_2f7340 = p;
    if (!p) return;
    libc_memcpy(p, s, n);
    p[n] = 0;
}

FN(cJSON *, cjson_create_object, ());                                                  // cJSON_CreateObject
FN(cJSON *, cjson_add_array_to_object, (cJSON *o, const char *name));                        // cJSON_AddArrayToObject
FN(cJSON *, cjson_add_string_to_object, (cJSON *o, const char *name, const char *s));         // cJSON_AddStringToObject
FN(cJSON *, cjson_add_number_to_object, (cJSON *o, const char *name, double v));              // cJSON_AddNumberToObject
FN(int, cjson_add_item_to_array, (cJSON *arr, cJSON *item));                               // cJSON_AddItemToArray
FN(char *, cjson_print_unformatted, (const cJSON *o));                                     // cJSON_PrintUnformatted
FN(unsigned char, save_module_settings, (cJSON *root, Mod *m));
struct BindEnt { const char *name; char pad[0x30]; unsigned bind; int pad2; };
FN(BindEnt *, profile_array, (void *ctx, unsigned long long *n));
// serialize all savable modules plus the key binds
extern "C" char *serialize_settings(Mods *mods, void *ctx) {
    cJSON *root = cjson_create_object();
    char *out = 0;
    if (!root) goto done;
    for (unsigned long long i = 0; i < mods->count; i++) {
        Mod *m = mods->data[i];
        if (!(m->flags & 0x10)) continue;
        if (!save_module_settings(root, m)) goto done;
    }
    {
        cJSON *binds = cjson_add_array_to_object(root, rdata_13ec52);
        if (!binds) { out = 0; goto done; }
        unsigned long long n;
        BindEnt *e = profile_array(ctx, &n);
        for (unsigned long long i = 0; i < n; i++, e++) {
            if (e->bind == 0) continue;
            cJSON *o = cjson_create_object();
            if (!o) goto done;
            if (!cjson_add_string_to_object(o, rdata_13ec47, e->name)) goto done;
            if (!cjson_add_number_to_object(o, rdata_13ec60, (double)e->bind)) goto done;
            if (!cjson_add_item_to_array(binds, o)) goto done;
        }
        out = cjson_print_unformatted(root);
    }
done:
    cjson_delete(root);
    return out;
}

FN(int, read_text_file, (char **path, char **data));
FN(unsigned char, load_module_states, (const char *data, Mods *mods));
FN(void, apply_module_state, (Mods *mods, Mod *m, int v));
FN(void, queue_module_toggle, (Mods *mods, Mod *m, unsigned char on));
extern "C" const char rdata_13ec30[];
extern "C" unsigned char data_2f7338;
struct Pair16 { long long a, b; };
extern "C" const Pair16 rdata_13ec78;
struct ModX { char pad[0x18]; unsigned char flags; char pad2[7]; unsigned char b20; char pad3[0x1df]; void (*cb200)(Pair16 *); };
// load profile `name` (NULL = reset every savable module to its defaults)
extern "C" int load_profile_name(const char *name, Mods *mods) {
    char *data;
    char *path;
    Pair16 p;
    if (!name) {
        for (unsigned long long i = 0; i < mods->count; i++) {
            ModX *m = (ModX *)mods->data[i];
            if (m->flags & 0x10) continue;
            apply_module_state(mods, (Mod *)m, 0);
            m->b20 = 0;
            if (!(m->flags & 2)) queue_module_toggle(mods, (Mod *)m, 0);
            if (m->cb200) {
                p = rdata_13ec78;
                m->cb200(&p);
            }
        }
        if (data_2f7340) libc_free(data_2f7340);
        data_2f7340 = 0;
        data_2f7338 = 0;
        return 0;
    }
    int r = settings_path(&path);
    if (r) return r;
    if ((r = settings_path_append(&path, rdata_13ec08)) != 0) goto clean;
    if ((r = settings_path_ensure(&path)) != 0) goto clean;
    if ((r = settings_path_append(&path, rdata_13ec30)) != 0) goto clean;
    if ((r = settings_path_ensure(&path)) != 0) goto clean;
    if ((r = settings_path_append(&path, name)) != 0) goto clean;
    if ((r = read_text_file(&path, &data)) != 0) goto clean;
    {
    unsigned char ok = load_module_states(data, mods);
    r = 0x1389;
    if (ok) {
        set_saved_string(name);
        data_2f7338 = 0;
        r = 0;
    }
    }
    libc_free(data);
clean:
    free_path(&path);
    return r;
}

// ---- call the shutdown callback (descriptor +0x200) of every module ----
struct ModList { struct ModCb { char pad[0x200]; void (*cb)(void *ev); } **data; unsigned long long count; };
struct Ev16 { long long a, b; };
extern "C" const Ev16 rdata_13ec20;
extern "C" void save_module_states(ModList *l) {
    for (unsigned long long i = 0; i < l->count; i++) {
        auto cb = l->data[i]->cb;
        if (cb) {
            Ev16 ev = rdata_13ec20;
            cb(&ev);
        }
    }
}
