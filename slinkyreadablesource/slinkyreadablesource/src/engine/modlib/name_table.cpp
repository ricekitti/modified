// 0x41000: sorted name table (24-byte entries) helpers
#include "slinky/engine.h"
struct Ent { void *name; long long q; int d; int key; };
struct Vec { unsigned long long size, cap; Ent *data; };

extern "C" unsigned long long data_3043a0, data_3043a8;
extern "C" Ent *data_3043b0;
extern "C" unsigned char data_3043c8;

FN(unsigned long long, libc_strlen, (const char *s));
FN(int, key_name_to_code, (void *key));
FN(void *, key_code_to_index, (int idx));
FN(unsigned char, vector_grow, (void *vec, unsigned long long elem, unsigned long long n, void *cap));
FN(void *, libc_bsearch, (const void *key, const void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(void, libc_memmove, (void *dst, const void *src, unsigned long long n));
FN(void, libc_free, (void *p));
FN(void *, get_minecraft, (void *a));
FN(void *, get_local_player, (void *j, void *mc));
FN(void, java_set_entity_living_base_max_hurt_time, (void *a, void *b, int c));

extern "C" int compare_name_key(const int *k, const Ent *e) { return *k - e->key; }
extern "C" void text_filter_name(char *buf, char c) {
    unsigned long long n = libc_strlen(buf);
    if (n > 99) return;
    unsigned char u = (unsigned char)((c & 0xdf) - 0x41);

    if (u >= 0x1a) {
        unsigned char d = (unsigned char)(c - 0x30);

        unsigned char notdigit = d >= 10;

        unsigned char notspace = c != 0x20;

        if (notdigit & notspace) return;
    }
    buf[n] = c;
}
extern "C" unsigned long long deref_u64(unsigned long long *p) { return *p; }
extern "C" unsigned long long vec24_key(Vec *v, long long i) { return *(unsigned long long *)((char *)v->data + i * 24); }
extern "C" void *vec24_name(Vec *v, long long i) { return (char *)v->data + i * 24 + 8; }
extern "C" unsigned char name_table_find(Vec *v, void *name, long long *src) {
    int id = key_name_to_code(name);
    if (id == -1) return 0;
    if (data_3043b0 && libc_bsearch(&id, data_3043b0, data_3043a0, 0x18, (int (*)(const void *, const void *))compare_name_key))
        return 0;
    unsigned long long idx = 0;
    if (v->size) {
        for (idx = 0; idx < v->size; idx++)
            if (id <= v->data[idx].key) break;
    }
    void *nm = key_code_to_index(id);
    long long q = src[0];
    int d = (int)src[1];
    int k = id;
    unsigned char ok = vector_grow(&data_3043b0, 0x18, data_3043a0, &data_3043a8);
    if (!ok) return ok;
    Ent *base = data_3043b0;
    libc_memmove(base + idx + 1, base + idx, (data_3043a0 - idx) * 0x18);
    base[idx].name = nm;
    base[idx].q = q;
    base[idx].d = d;
    base[idx].key = k;
    data_3043a0++;
    return ok;
}
extern "C" void vec24_erase(Vec *v, long long i) {
    v->size--;
    libc_memmove(&v->data[i], &v->data[i + 1], (v->size - i) * sizeof(Ent));
}
extern "C" void vec24_clear(Vec *v) {
    v->size = 0; v->cap = 0;
    libc_free(v->data);
    v->data = 0;
}
extern "C" unsigned char get_render_flag() { return data_3043c8; }
extern "C" void get_local_player_ref(void *a) {
    void *p = get_local_player(a, get_minecraft(a));
    if (p) java_set_entity_living_base_max_hurt_time(a, p, 0x7fffffff);
}
