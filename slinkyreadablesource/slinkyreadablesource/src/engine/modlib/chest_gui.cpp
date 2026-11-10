// 0x48000: chest-GUI check (localized title compare) and randomized click delay
#include "slinky/engine.h"
#include "slinky/jids.h"
struct Rot { float yaw, pitch; };
FN(void *, get_current_screen, (void *a, void *x));
FN(unsigned char, java_jni_is_instance_of_gui_chest, (void *a, void *s));
FN(void *, java_get_gui_chest, (void *a, void *s));
FN(void *, java_jni_is_instance_of_inventory_basic, (void *a, void *x));
FN(void *, java_get_inventory_basic, (void *a, void *x));
FN(void *, java_call_static_stat_collector_translate_to_local, (void *a, void *s));
FN(Rot, rot_make_floats, (float a, float b));
FN(float, random_rotation, (Rot r));
FN(void, slot_index, (int id));
FN(int, libc_strncmp_alt, (const void *a, const void *b, unsigned long long n));
extern "C" unsigned char data_2f54e8;
extern "C" float data_2f54c8, data_304a10, rdata_155f60, rdata_155f64;
extern "C" const char rdata_155f9a[], rdata_155faa[];

// does the Java string equal the translation of `key`?
extern "C" bool java_string_equal_translation_key(Jvm *j, jstring s, const char *key) {
    JNIEnv *env = j->env;
    jstring t = (jstring)java_call_static_stat_collector_translate_to_local(j, (*env)->NewStringUTF(env, key));
    jsize len = (*env)->GetStringLength(env, s);
    if ((*env)->GetStringLength(env, t) != len) return false;
    const jchar *a = (const jchar *)(*env)->GetStringCritical(env, s, 0);
    const jchar *b = (const jchar *)(*env)->GetStringCritical(env, t, 0);
    bool eq = libc_strncmp_alt(a, b, (long long)len * 2) == 0;
    (*env)->ReleaseStringCritical(env, s, a);
    (*env)->ReleaseStringCritical(env, t, b);
    return eq;
}

struct Ev { char pad[8]; Ctx *ctx; };
extern "C" void chest_gui_tick(Ev *ev) {
    Ctx *c = ev->ctx;
    void *s = get_current_screen(c->a, c->c18);
    if (s && java_jni_is_instance_of_gui_chest(c->a, s)) {
        if (data_2f54e8 != 1) return;
        void *a = c->a;
        void *ct = java_jni_is_instance_of_inventory_basic(a, java_get_gui_chest(a, s));
        if (ct) {
            void *name = java_get_inventory_basic(c->a, ct);
            if (java_string_equal_translation_key((Jvm *)c->a, (jstring)name, rdata_155f9a)) return;
            if (java_string_equal_translation_key((Jvm *)c->a, (jstring)name, rdata_155faa)) return;
        }
    }
    data_304a10 = data_2f54c8;
    float lo = rdata_155f60;

    data_304a10 = random_rotation(rot_make_floats(lo, rdata_155f64)) * data_304a10;
    slot_index(0xd);
}

// item id -> {tool kind, material tier}  (kind 1/14/16/22 = axe/shovel/pickaxe/sword, 15 = shears)
struct ToolInfo { int kind, tier; };
extern "C" ToolInfo chest_gui_title_id(int id) {
    switch (id) {
    case 0x100: return ToolInfo{16, 3};
    case 0x101: return ToolInfo{14, 3};
    case 0x102: return ToolInfo{1, 3};
    case 0x10b: return ToolInfo{22, 3};
    case 0x10c: return ToolInfo{22, 1};
    case 0x10d: return ToolInfo{16, 1};
    case 0x10e: return ToolInfo{14, 1};
    case 0x10f: return ToolInfo{1, 1};
    case 0x110: return ToolInfo{22, 2};
    case 0x111: return ToolInfo{16, 2};
    case 0x112: return ToolInfo{14, 2};
    case 0x113: return ToolInfo{1, 2};
    case 0x114: return ToolInfo{22, 5};
    case 0x115: return ToolInfo{16, 5};
    case 0x116: return ToolInfo{14, 5};
    case 0x117: return ToolInfo{1, 5};
    case 0x11b: return ToolInfo{22, 4};
    case 0x11c: return ToolInfo{16, 4};
    case 0x11d: return ToolInfo{14, 4};
    case 0x11e: return ToolInfo{1, 4};
    case 0x167: return ToolInfo{15, 1};
    default: return ToolInfo{0, 0};
    }
}

FN(int, java_call_nbttag_list_tag_count, (void *a, void *list));
FN(void *, java_call_nbttag_list_get_compound_tag_at, (void *a, void *list, int i));
FN(short, java_jni_call_short_method_nbttag_compound_get_short, (void *a, void *tag, void *key));
extern "C" const char rdata_155b94[], rdata_155b97[];
// weighted sum of enchantment levels (weights indexed by enchantment id 0..62)
extern "C" unsigned short weighted_sum_enchantment_levels(Ctx *c, void *list, const unsigned char *weights) {
    if (!list) return 0;
    JNIEnv *env = ((Jvm *)c->a)->env;
    jstring idS = (*env)->NewStringUTF(env, rdata_155b94);
    if (!idS) return 0;
    jstring lvlS = (*env)->NewStringUTF(env, rdata_155b97);
    if (!lvlS) return 0;
    int n = java_call_nbttag_list_tag_count(c->a, list);
    if (n <= 0) return 0;
    unsigned short sum = 0;
    for (int i = 0; i != n; i++) {
        void *tag = java_call_nbttag_list_get_compound_tag_at(c->a, list, i);
        unsigned short id = (unsigned short)java_jni_call_short_method_nbttag_compound_get_short(c->a, tag, idS);
        if (id > 0x3e) continue;
        unsigned short w = weights[id];
        if (w == 0) continue;
        short lvl = java_jni_call_short_method_nbttag_compound_get_short(c->a, tag, lvlS);
        sum += lvl < 0 ? 0 : (unsigned short)(w * lvl);
    }
    return sum;
}

extern "C" const unsigned char rdata_155d70[], rdata_155d30[], rdata_155ce0[], rdata_155d1e[];
// score of an item for an inventory-sorter category (higher = better); cases decoded from the image jump tables
extern "C" int score_item_inventory_sorter_category(Ctx *c, int cat, int id, unsigned meta, void *nbt, unsigned char b) {
    switch (cat) {
    case 1: case 14: case 15: case 16: {
        ToolInfo ti = chest_gui_title_id(id);
        if (cat != ti.kind) return 0;
        int base = ti.tier * 100;
        return weighted_sum_enchantment_levels(c, nbt, rdata_155d70) + base;
    }
    case 2:
        switch (id) {
        case 0x1: case 0x2: case 0x3: case 0x4: case 0x5: case 0x11: case 0x12: case 0x14: case 0x18: case 0x23:
        case 0x2d: case 0x31: case 0x4e: case 0x57: case 0x5f: case 0x62: case 0x79: case 0x7d: case 0x85:
        case 0x9b: case 0x9f: case 0xa2:
            return 1;
        default:
            return 0;
        }
    case 3:
        if (id != 0x105) return 0;
        return weighted_sum_enchantment_levels(c, nbt, rdata_155d30) + 1;
    case 4: return id == 0x36;
    case 5: return id == 0x170 ? 2 - b : 0;
    case 6: return id == 0x181;
    case 7: return id == 0x15a;
    case 8: return id == 0x118;
    case 9:
        if (id != 0x142) return 0;
        return (meta == 1) + 1;
    case 10: case 11: case 19: case 21: {
        if (id != 0x175) return 0;
        unsigned short base;
        switch (meta & 0xf) {
        case 1: base = 1; if (cat != 10) return 0; break;
        case 2: base = 1; if (cat != 19) return 0; break;
        case 5: base = 2; if (cat != 10) return 0; break;
        case 9: base = 1; if (cat != 21) return 0; break;
        case 11: base = 1; if (cat != 11) return 0; break;
        default: return 0;
        }
        return (unsigned short)(base + ((meta >> 6) & 1) + ((meta >> 5) & 1));
    }
    case 12: return id == 0x41;
    case 13: return id == 0x147;
    case 17: return id == 0x14c || id == 0x158;
    case 18: return id == 0x19d || id == 0x11a;
    case 20: return id == 0x103;
    case 22: {
        ToolInfo ti = chest_gui_title_id(id);
        if (ti.kind != 0x16) return 0;
        int base = rdata_155d1e[ti.tier] * 40;
        return weighted_sum_enchantment_levels(c, nbt, rdata_155ce0) + base + 0xa0;
    }
    case 23: return id == 0x2e;
    case 24: return id == 0x146;
    case 25: {
        ToolInfo ti = chest_gui_title_id(id);
        unsigned short k;
        switch (ti.kind) {
        case 1: k = 3; break;
        case 14: k = 2; break;
        case 16: k = 1; break;
        case 22: k = 4; break;
        default: k = 0; break;
        }
        int base = (k + rdata_155d1e[ti.tier]) * 40;
        return weighted_sum_enchantment_levels(c, nbt, rdata_155ce0) + base;
    }
    case 26: return id == 0x23;
    default: return 0;
    }
}
