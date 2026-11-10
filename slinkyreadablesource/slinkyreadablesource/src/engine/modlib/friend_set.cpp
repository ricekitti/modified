// 0x3b000: friend-list style 32-byte key set (sorted vector) with chat feedback
#include "slinky/engine.h"
struct Key { unsigned long long hi, lo; };
struct Ent { char b[0x20]; };
extern "C" void *data_2fc8c0; extern "C" char data_2fc8c8[]; extern "C" int data_2fc8cc;
extern "C" const char rdata_153590[], rdata_153603[], rdata_153640[], rdata_153641[], rdata_15365a[];
FN(unsigned char, friend_resolve, (void *c, Key k, Ent *e, char *name));
FN(void *, vector_find_insert, (void *t, int elem, void *hdr, void *e, void *cmp));
FN(void *, binary_search, (void *t, int elem, void *hdr, void *e, void *cmp));
FN(void, vector_erase_value, (void *t, int elem, void *hdr, void *at));
extern "C" void jni_throw_format(void *a, const char *fmt, ...);

extern "C" int compare_friend_key(void *, unsigned long long *a, unsigned long long *b) {
    unsigned long long x = __builtin_bswap64(a[0]);

    unsigned long long y = __builtin_bswap64(b[0]);
    if (x == y) {
        unsigned long long a1 = a[1], b1 = b[1];

        x = __builtin_bswap64(a1);
        y = __builtin_bswap64(b1);

        if (x == y) return 0;
    }
    return x >= y ? 1 : -1;
}
extern "C" unsigned char friend_set_insert(void **c, Key k) {
    Ent e; char name[0x20];
    unsigned char ok = friend_resolve(c, k, &e, name);
    if (!ok) return ok;
    vector_find_insert(&data_2fc8c0, 0x20, data_2fc8c8, &e, (void *)compare_friend_key);
    jni_throw_format(*c, rdata_153590, name);
    return ok;
}
extern "C" unsigned char friend_set_erase(void **c, Key k) {
    Ent e; char name[0x20];
    unsigned char ok = friend_resolve(c, k, &e, name);
    if (!ok) return ok;
    void *at = binary_search(data_2fc8c0, 0x20, data_2fc8c8, &e, (void *)compare_friend_key);
    if (at) vector_erase_value(data_2fc8c0, 0x20, data_2fc8c8, at);
    jni_throw_format(*c, rdata_153603, name);
    return ok;
}
extern "C" bool friend_set_clear(void **c) {
    unsigned long long n = (unsigned)data_2fc8cc;

    void *a = *c;
    jni_throw_format(a, rdata_15365a, n, n == 1 ? rdata_153640 : rdata_153641);
    data_2fc8cc = 0;
    return true;
}
extern "C" int friend_set_dirty() { return data_2fc8cc; }

#include "slinky/jids.h"
FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));
FN(void *, java_get_world, (void *a, void *w));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(jstring, java_call_entity_get_command_sender_name, (void *a, void *p));
FN(void *, java_get_entity_2, (void *a, void *p));
FN(unsigned long long, java_jni_call_long_method_uuid_get_most_significant_bits, (void *a, void *uuid));
FN(unsigned long long, java_jni_call_long_method_uuid_get_least_significant_bits, (void *a, void *uuid));
extern "C" const char rdata_1535a7[], rdata_1535c0[], rdata_1535ee[];
struct Str { const char *s; unsigned long long len; };
struct Uid { unsigned long long hi, lo, z0, z1; };
struct C3b { Jvm *a; char pad[0x18]; void *world; void *self; };
static inline __attribute__((always_inline)) unsigned char fold(unsigned char ch) {
    return (unsigned char)(((unsigned char)(ch - 0x41) < 0x1a) << 5) + ch;
}
// resolve a (unique, case-insensitive) player-name prefix to its UUID; prints the reason on failure
extern "C" unsigned char friend_resolve(void *cv, Key kk, Ent *eo, char *name) {
    C3b *c = (C3b *)cv;
    Str k = __builtin_bit_cast(Str, kk);
    Uid *out = (Uid *)eo;
    unsigned long long len = k.len;
    if (len >= 0x11) { jni_throw_format(c->a, rdata_1535a7); return false; }
    const char *s = k.s;
    JNIEnv *env = c->a->env;
    char buf[0x18];
    libc_memcpy_alt(buf, s, len);
    buf[len] = 0;
    if (!(*env)->NewStringUTF(env, buf)) return false;
    void *list = java_get_world(c->a, c->world);
    if (java_call_list_size(c->a, list) <= 0) { jni_throw_format(c->a, rdata_1535ee); return false; }
    void *found = 0;
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *p = java_call_list_get(c->a, list, i);
        JNIEnv *e2 = c->a->env;
        if ((*e2)->IsSameObject(e2, (jobject)p, (jobject)c->self)) continue;
        jstring nm = java_call_entity_get_command_sender_name(c->a, p);
        int L = (*env)->GetStringUTFLength(env, nm);
        if (len > (unsigned long long)(long long)L) continue;
        const char *chars = (*env)->GetStringUTFChars(env, nm, 0);
        unsigned long long j;
        for (j = 0; j < len; j++)
            if (fold(s[j]) != fold(chars[j])) break;
        if (j == len) {
            if (found) { jni_throw_format(c->a, rdata_1535c0, (int)len, s); return false; }
            int n2 = L >= 0x10 ? 0x10 : L;
            libc_memcpy_alt(name, chars, (long long)n2);
            name[n2] = 0;
            found = p;
        }
        (*env)->ReleaseStringUTFChars(env, nm, chars);
    }
    if (!found) { jni_throw_format(c->a, rdata_1535ee); return false; }
    void *uuid = java_get_entity_2(c->a, found);
    unsigned long long hi = java_jni_call_long_method_uuid_get_most_significant_bits(c->a, uuid);
    out->lo = java_jni_call_long_method_uuid_get_least_significant_bits(c->a, uuid);
    out->hi = hi;
    out->z0 = 0; out->z1 = 0;
    return true;
}

// hex string of friend entry i (first 24 bytes: the UUID + name hash), in a static buffer
FN(unsigned char *, element_at, (void *vec, unsigned elem, void *hdr, unsigned i));
extern "C" const char rdata_1536b0[];   // "0123456789abcdef"
extern "C" char data_2fc8d0[];
extern "C" char *friend_set_count(void *, unsigned i) {
    const unsigned char *e = element_at(data_2fc8c0, 0x20, data_2fc8c8, i);
    for (int k = 0; k < 0x18; k++) {
        data_2fc8d0[2 * k] = rdata_1536b0[e[k] >> 4];
        data_2fc8d0[2 * k + 1] = rdata_1536b0[e[k] & 0xf];
    }
    data_2fc8d0[0x30] = 0;
    return data_2fc8d0;
}

// add every player within `radius` blocks as a friend (optionally clearing the list first)
struct V3b { double x, y, z; };
FN(V3b, java_get_entity_pos_x, (void *a, void *e));
FN(unsigned char, entity_flags_has, (void *a, void *e, int mask));
FN(double, vec3_distance_sq, (V3b a, V3b b));
extern "C" const char rdata_15361c[], rdata_153643[];
struct NearArgs { unsigned long long radius; char pad[8]; unsigned char clear; };
typedef int v4i3 __attribute__((vector_size(16)));
typedef double v2d3 __attribute__((vector_size(16)));
extern "C" const v4i3 rdata_153180;   // {0x43300000, 0x45300000, 0, 0}
extern "C" const v2d3 rdata_153190;   // {2^52, 2^84}
// uint64 -> double with the image's constants (the original's lowering)
static inline __attribute__((always_inline)) double u64_to_d(unsigned long long x) {
    v4i3 v = {(int)x, (int)(x >> 32), 0, 0};
    v4i3 u = __builtin_shufflevector(v, rdata_153180, 0, 4, 1, 5);
    v2d3 d = (v2d3)u - rdata_153190;
    return d[1] + d[0];
}
extern "C" bool friend_set_contains(C3b *c, NearArgs *a) {
    unsigned long long radius = a->radius;
    unsigned char clear = a->clear;
    unsigned long long old = (unsigned)data_2fc8cc;
    if (clear == 1) data_2fc8cc = 0;
    V3b eye = java_get_entity_pos_x(c->a, c->self);
    void *list = java_get_world(c->a, c->world);
    unsigned long long added = 0;
    if (java_call_list_size(c->a, list) > 0) {
        double r = u64_to_d(radius);
        r *= r;
        for (int i = 0; i < java_call_list_size(c->a, list); i++) {
            void *e = java_call_list_get(c->a, list, i);
            JNIEnv *env = c->a->env;
            if ((*env)->IsSameObject(env, (jobject)e, (jobject)c->self)) continue;
            if (entity_flags_has(c->a, e, 2)) continue;
            V3b p = java_get_entity_pos_x(c->a, e);
            if (vec3_distance_sq(eye, p) > r) continue;
            void *uuid = java_get_entity_2(c->a, e);
            Uid k;
            k.hi = java_jni_call_long_method_uuid_get_most_significant_bits(c->a, uuid);
            k.lo = java_jni_call_long_method_uuid_get_least_significant_bits(c->a, uuid);
            k.z0 = 0;
            k.z1 = 0;
            vector_find_insert(&data_2fc8c0, 0x20, data_2fc8c8, &k, (void *)compare_friend_key);
            added++;
        }
    }
    const char *s = added == 1 ? rdata_153640 : rdata_153641;
    if (clear) jni_throw_format(c->a, rdata_15361c, old, added, s);
    else jni_throw_format(c->a, rdata_153643, added, s);
    return true;
}

// add a friend entry from its hex string (inverse of friend_set_count); a bad digit stops that part of the parse
FN(unsigned long long, libc_strlen, (const char *s));
static inline __attribute__((always_inline)) bool hexdig(unsigned char c, unsigned char *v) {
    if ((unsigned char)(c - '0') < 10) { *v = c - '0'; return true; }
    if ((unsigned char)(c - 'a') < 26) { *v = c - 'a' + 10; return true; }
    return false;
}
static inline __attribute__((always_inline)) void unhex(unsigned char *d, const char *s, int n) {
    for (int i = 0; i < n; i++) {
        unsigned char h, l;
        if (!hexdig(s[i * 2], &h) || !hexdig(s[i * 2 + 1], &l)) return;
        d[i] = h << 4 | l;
    }
}
extern "C" bool friend_name_matches(void *, const char *s) {
    Ent e = {};
    unsigned long long n = libc_strlen(s);
    if (n >= 0x30) {
        unhex((unsigned char *)e.b, s, 16);
        unhex((unsigned char *)e.b + 16, s + 32, 8);
        vector_find_insert(&data_2fc8c0, 0x20, data_2fc8c8, &e, (void *)compare_friend_key);
    }
    return n >= 0x30;
}
