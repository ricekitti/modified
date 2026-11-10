// 0x28000: module manager - per-event handler lists, dispatch, small item helpers
#include "slinky/engine.h"
#include "slinky/jcall.h"
typedef void (*Handler)(void *event);
struct Module;
struct Reg { Module **list; unsigned long long n; };
struct Mgr {
    Module **mods; unsigned long long count; Reg cat[5];   // +0x10: modules per category
    Reg reg[27];                        // +0x060: modules that implement each event type
    Handler *lists[27];                 // +0x210: NULL-terminated handler list per event type
    Module **en_list; unsigned long long en_count;    // +0x2e8 pending enables
    Module **dis_list; unsigned long long dis_count;  // +0x2f8 pending disables
    void (*set308)(void *mod, int v);   // +0x308
};
struct HSlot { Handler f; void *x; };
struct Module { char pad[0x10]; unsigned cat; unsigned char enabled; char pad2[0x33]; HSlot h[27]; };
// dispatch: call every handler registered for the event's type (first dword of the event)
extern "C" void dispatch_event(Mgr *m, unsigned *e) {
    Handler *h = m->lists[*e];
    for (Handler f = *h; f; f = *++h) f(e);
}
extern "C" bool is_event_handled(Mgr *m, unsigned type) { return *m->lists[type] == 0; }
extern "C" bool has_queued_modules(Mgr *m) { return m->dis_count != 0 || m->en_count != 0; }
extern "C" void apply_module_state(Mgr *m, void *mod, int v) {
    auto f = m->set308;

    f(mod, v);
}

FN(void *, java_get_entity_player_2, (void *a, void *b));
FN(int, java_get_inventory_player_current_item, (void *a, void *x));
FN(void *, java_get_inventory_player_main_inventory, (void *a, void *x));
FN(void *, java_call_item_stack_get_item, (void *a, void *x));
FN(int, java_call_static_item_get_id_from_item, (void *a, void *x));
// item held in a hotbar slot (0..8) of the entity's inventory
extern "C" void *item_held_hotbar_slot(Jvm *j, void *ent) {
    void *inv = java_get_entity_player_2(j, ent);
    unsigned slot = java_get_inventory_player_current_item(j, inv);
    if (slot > 8) return 0;
    void *arr = java_get_inventory_player_main_inventory(j, inv);
    jobject st = (*j->env)->GetObjectArrayElement(j->env, (jobjectArray)arr, slot);
    if (!st) return 0;
    return java_call_item_stack_get_item(j, st);
}
// is the item a sword (ids 267, 268, 272, 276, 283)?
extern "C" bool item_sword(Jvm *j, void *item) {
    unsigned char r;
    if (!item) { r = 0; goto out; }
    {
    unsigned k = java_call_static_item_get_id_from_item(j, item) - 0x10b;
    if (k <= 0x10) {  r = (unsigned char)(0x10223u >> k); }
    else r = 0;
    }
out:

    return r & 1;
}

static inline __attribute__((always_inline)) bool is_code(unsigned short c) {
    return (unsigned short)(c - 0x61) < 6 | (unsigned short)(c - 0x30) < 10 | c == 0x72;
}
// colour code (0-9, a-f, r) in effect before the first name character of a display string, 0 if none
static inline __attribute__((always_inline)) unsigned prefix_code(Jvm *j, jstring s) {
    JNIEnv *env = j->env;
    const jchar *ch = (const jchar *)(*env)->GetStringCritical(env, s, 0);
    unsigned r = 0;
    if ((*env)->GetStringLength(env, s) > 0) {
        bool prev = false;
        r = 0;
        long long i = 0;
        for (;;) {
            bool p = prev;
            unsigned short c = ch[i];
            prev = c == 0xa7;
            if (!prev) {
                if (p) {
                    if ((unsigned short)(c - 0x30) < 10) r = c;
                    if ((unsigned short)(c - 0x61) < 6) r = c;
                    if (c == 0x72) r = c;
                } else {
                    if (c == 0x5f || (unsigned short)(c - 0x30) < 10 || (unsigned short)((c & 0xffdf) - 0x41) < 0x1a)
                        goto done;
                }
            }
            i++;
            if (i >= (long long)(*env)->GetStringLength(env, s)) { r = 0; break; }
        }
    }
done:
    (*env)->ReleaseStringCritical(env, s, ch);
    return r;
}
// last colour code (0-9, a-f, r) of a display string, 'f' if none
static inline __attribute__((always_inline)) unsigned short last_code(Jvm *j, jstring s) {
    unsigned short res = 0x66;
    JNIEnv *env = j->env;
    jsize len = (*env)->GetStringLength(env, s);
    if (!len) return res;
    const jchar *ch = (const jchar *)(*env)->GetStringCritical(env, s, 0);
    if (len > 0) {
        unsigned cand = 0;
        unsigned char valid = 0;
        for (unsigned long long k = (unsigned)len + 1; ; ) {
            unsigned short c = ch[k - 2];
            if (c != 0xa7) {
                valid = is_code(c);
                if (valid) cand = c;
            } else {
                if (valid & 1) { res = (unsigned short)cand; break; }
                valid = 0;
            }
            k--;
            if (k <= 1) break;
        }
    }
    (*env)->ReleaseStringCritical(env, s, ch);
    return res;
}
extern "C" unsigned name_prefix_code(Jvm *j, jstring s) { return prefix_code(j, s); }
FN(jstring, java_get_score_player_team, (Jvm *j, void *x));
extern "C" unsigned short name_last_code(Jvm *j, void *x) {
    jstring s = java_get_score_player_team(j, x);
    if (!s) return 0x66;
    return last_code(j, s);
}
FN(jstring, java_call_entity_get_command_sender_name, (Jvm *j, void *ent));
FN(void *, java_jni_is_instance_of_entity_living_base, (Jvm *j, void *ent));
FN(void *, java_call_entity_living_base_get_team, (Jvm *j, void *x));
FN(void *, java_jni_is_instance_of_score_player_team, (Jvm *j, void *x));
// entity name colour: prefix code of the display name, else the team colour, else 'f'
extern "C" unsigned short entity_name_colour(Jvm *j, void *ent) {
    unsigned short r = (unsigned short)prefix_code(j, java_call_entity_get_command_sender_name(j, ent));
    if (r) return r;
    void *t = java_jni_is_instance_of_entity_living_base(j, ent);
    if (!t) return 0x66;
    void *x = java_call_entity_living_base_get_team(j, t);
    if (!x) return 0x66;
    void *y = java_jni_is_instance_of_score_player_team(j, x);
    if (!y) return 0x66;
    jstring s = java_get_score_player_team(j, y);
    if (!s) return 0x66;
    return last_code(j, s);
}

// manager constructor: copy the static module table, bucket modules by category and by implemented event
FN(void *, libc_memset, (void *d, int c, unsigned long long n));          // memset
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));  // calloc
FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));  // memcpy
FN(void, libc_qsort, (void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));   // qsort
extern "C" Module *const rdata_14f810[0x42];
extern "C" unsigned data_2f7a4c;
FN(int, compare_module_slots, (const void *a, const void *b));
extern "C" void *stasis_descriptor();
extern "C" void displace_install();
extern "C" void *bow_aimbot_descriptor();
extern "C" void *fog_descriptor();
extern "C" void *shaders_descriptor();
extern "C" void *scaffold_native_descriptor();
extern "C" void *bedplates_descriptor();
extern "C" void *rice_anticheat_descriptor();
extern "C" void hud_editor_install();
extern "C" void *anti_fireball_descriptor();
extern "C" void *fireball_pearl_descriptor();
extern "C" void *fireball_boost_descriptor();
extern "C" void *auto_pearl_descriptor();
extern "C" void *fireball_pearl_clutch_descriptor();
extern "C" void *bridge_egg_descriptor();
extern "C" void heavy_glow_extend_modes();
extern "C" int create_module_manager(Mgr *m, void (*setter)(void *mod, int v)) {
    heavy_glow_extend_modes();
    displace_install();
    hud_editor_install();
    libc_memset(m->cat, 0, 0x300);
    m->count = 0x4e; // Retire regular Fireball Pearl; keep Fireball Pearl Clutch.
    m->mods = (Module **)libc_calloc(m->count, 8);
    if (!m->mods) return 0x3ed;
    libc_memcpy_alt(m->mods, rdata_14f810, 0x210);
    m->mods[0x42] = (Module *)stasis_descriptor();
    m->mods[0x43] = (Module *)anti_fireball_descriptor();
    m->mods[0x44] = (Module *)bow_aimbot_descriptor();
    m->mods[0x45] = (Module *)fog_descriptor();
    m->mods[0x46] = (Module *)shaders_descriptor();
    m->mods[0x47] = (Module *)scaffold_native_descriptor();
    m->mods[0x48] = (Module *)bedplates_descriptor();
    m->mods[0x49] = (Module *)rice_anticheat_descriptor();
    m->mods[0x4a] = (Module *)fireball_boost_descriptor();
    m->mods[0x4b] = (Module *)auto_pearl_descriptor();
    m->mods[0x4c] = (Module *)fireball_pearl_clutch_descriptor();
    m->mods[0x4d] = (Module *)bridge_egg_descriptor();
#pragma clang loop unroll(disable)
    for (unsigned long long i = 0; i != m->count; i++) {
        Module *mod = m->mods[i];
        *(unsigned long long *)((char *)m + 0x18 + ((unsigned long long)mod->cat << 4)) += 1;
#pragma clang loop unroll_count(2)
        for (unsigned t = 0; t < 27; t++)
            if (mod->h[t].f) m->reg[t].n++;
    }
    for (int c = 0; c < 5; c++) {
        m->cat[c].list = (Module **)libc_calloc(m->cat[c].n, 8);
        if (!m->cat[c].list) return 0x3ed;
        m->cat[c].n = 0;
    }
#pragma clang loop unroll(disable)
    for (int t = 0; t < 27; t++) {
        unsigned long long n = m->reg[t].n;
        m->reg[t].list = (Module **)libc_calloc(n, 8);
        if (!m->reg[t].list) return 0x3ed;
        m->lists[t] = (Handler *)libc_calloc(n + 1, 8);
        if (!m->lists[t]) return 0x3ed;
        m->reg[t].n = 0;
    }
    m->en_list = (Module **)libc_calloc(m->count, 8);
    if (!m->en_list) return 0x3ed;
    m->dis_list = (Module **)libc_calloc(m->count, 8);
    if (!m->dis_list) return 0x3ed;
#pragma clang loop unroll(disable)
    for (unsigned long long i = 0; i != m->count; i++) {
        Module *mod = m->mods[i];
        Reg *cr = &m->cat[mod->cat];
        unsigned long long cn = cr->n;
        cr->list[cn] = mod;
        cr->n = cn + 1;
#pragma clang loop unroll_count(2)
        for (unsigned t = 0; t < 27; t++)
            if (mod->h[t].f) {
                unsigned long long rn = m->reg[t].n;
                m->reg[t].list[rn] = mod;
                m->reg[t].n = rn + 1;
            }
    }
#pragma clang loop unroll(disable)
    for (unsigned t = 0; t != 27; t++) {
        data_2f7a4c = t;
        libc_qsort(m->reg[t].list, m->reg[t].n, 8, compare_module_slots);
    }
    m->set308 = setter;
    return 0;
}

// queue a module enable/disable (applied by apply_module_toggles); a disable is announced first (event 4) and can be
// cancelled by a handler setting the event's flag
struct QEvent { int type; int pad; Module *mod; unsigned char cancel; char rest[0x37]; };
extern "C" bool queue_module_toggle(Mgr *m, Module *mod, unsigned char on) {
    if (mod->enabled == on) return true;
    Module ***list;
    if (on) {
        list = &m->en_list;
    } else {
        QEvent ev = {};
        ev.type = 4;
        ev.mod = mod;
        Handler *h = m->lists[4];
        if (*h) {
            for (Handler f = *h; f; f = *++h) f((unsigned *)&ev);
            if (ev.cancel & 1) return false;
        }
        list = &m->dis_list;
    }
    unsigned long long *n = (unsigned long long *)(list + 1);
    (*list)[(*n)++] = mod;
    return true;
}
FN(void *, get_block_state_id, (Jvm *j, void *a1));
FN(jint, get_block_id, (Jvm *j, void *a1));
extern "C" bool block_entity_id(Jvm *j, void *ent) {
    bool r = false;
    if (ent) {
        void *x = get_block_state_id(j, ent);
        if (x) r = get_block_id(j, x) != 0;
    }

    return r;
}

// ---- gap-scan functions of the manager block ----
// sort comparator: descending by the int in slot `data_2f7a4c` (slots are 16 bytes, starting at +0x50)
struct SortSlot { int v; int pad[3]; };
struct SortRec { char pad[0x50]; SortSlot k[1]; };
extern "C" int compare_module_slots(const void *pa, const void *pb) {
    SortRec **a = (SortRec **)pa, **b = (SortRec **)pb;
    SortRec *rb = *b;
    unsigned i = data_2f7a4c;
    return rb->k[i].v - (*a)->k[i].v;
}

// module manager destructor: free every list the constructor (create_module_manager) allocated
FN(void, libc_free, (void *p));
#define RELEASE(p)         \
    do {                   \
        if (p) {           \
            libc_free(p);  \
            p = 0;         \
        }                  \
    } while (0)
extern "C" void destroy_module_manager(Mgr *m) {
    RELEASE(m->mods);
    for (int i = 0; i < 5; i++) RELEASE(m->cat[i].list);
#pragma clang loop unroll(disable)
    for (int i = 0; i < 27; i++) RELEASE(m->reg[i].list);
#pragma clang loop unroll(disable)
    for (int i = 0; i < 27; i++) RELEASE(m->lists[i]);
    RELEASE(m->en_list);
    RELEASE(m->dis_list);
}

// Minecraft formatting codes in a (modified UTF-8) name: returns the last colour code (0-9, a-f; 'r' resets to 'f')
// seen before the first visible character (letter, digit or '_'); *hasText tells whether there was one.
// "§" arrives as the byte pair C2 A7.
FN(unsigned long long, libc_strlen, (const char *s));
extern "C" int last_text_color_code(const char *s, int colour, bool *hasText) {
    *hasText = false;
    unsigned long long n = libc_strlen(s);
    bool sawC2 = false, afterSign = false;
    for (unsigned long long i = 0; i != n; i++) {
        signed char c = s[i];
        if (afterSign) {
            signed char code = c == 'r' ? 'f' : c;
            if ((unsigned short)(c - '0') < 10) colour = code;
            if ((unsigned short)(c - 'a') < 6) colour = code;
            if (c == 'r') colour = code;
            afterSign = false;
        } else if (sawC2) {
            if ((unsigned char)c == 0xa7) {
                afterSign = true;
                sawC2 = false;
            } else {
                sawC2 = (unsigned char)c == 0xc2;
            }
        } else {
            if ((unsigned char)c == 0xc2) {
                sawC2 = true;
                continue;
            }
            if (c == '_' || (unsigned short)(c - '0') < 10 || (unsigned short)((c & 0xdf) - 'A') <= 25) {
                *hasText = true;
                return colour;
            }
        }
    }
    return colour;
}
