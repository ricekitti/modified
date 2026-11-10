// 0x40000: 24-byte entry vector accessors, comparators
#include "slinky/engine.h"
struct Ent { void *name; long long q; int d; int key; };
struct Vec { unsigned long long size, cap; Ent *data; };
extern "C" unsigned long long deref_u64_2(unsigned long long *p) { return *p; }
extern "C" unsigned long long vec24_key_2(Vec *v, long long i) { return *(unsigned long long *)((char *)v->data + i * 24); }
extern "C" void *vec24_name_2(Vec *v, long long i) { return (char *)v->data + i * 24 + 8; }
extern "C" int compare_vec24_key(const int *k, const Ent *e) { return *k - e->key; }
extern "C" int compare_int_ids(const int *a, const int *b) {
    int r = a[0] - b[0];
    if (r == 0) r = a[1] - b[1];

    return r;
}

FN(void, libc_memmove, (void *dst, const void *src, unsigned long long n));
FN(void, libc_free, (void *p));
struct V3 { unsigned long long size, cap; void *data; };
struct Lists { char pad[8]; V3 a, b, c; };
struct LVec { unsigned long long size, cap; Lists *data; };
extern "C" LVec data_3020f0;
extern "C" unsigned char data_302108;
static inline __attribute__((always_inline)) void clear_lists(Lists *l) {
    l->a.size = 0; l->a.cap = 0; libc_free(l->a.data); l->a.data = 0;
    l->b.size = 0; l->b.cap = 0; libc_free(l->b.data); l->b.data = 0;
    l->c.size = 0; l->c.cap = 0; libc_free(l->c.data); l->c.data = 0;
}
static inline __attribute__((always_inline)) void clear_all() {
    for (unsigned long long i = 0; i < data_3020f0.size; i++) clear_lists(&data_3020f0.data[i]);
    data_3020f0.size = 0; data_3020f0.cap = 0;
    libc_free(data_3020f0.data);
    data_3020f0.data = 0;
    data_302108 = 1;
}
extern "C" void vec24_erase_2(Vec *v, long long i) {
    v->size--;
    libc_memmove(&v->data[i], &v->data[i + 1], (v->size - i) * sizeof(Ent));
    clear_all();
}
extern "C" void vec24_clear_2(Vec *v) {
    v->size = 0; v->cap = 0;
    libc_free(v->data);
    v->data = 0;
    clear_all();
}

typedef char v16c __attribute__((vector_size(16)));
typedef char v16cu __attribute__((vector_size(16), aligned(1)));
FN(int, key_name_to_code, (void *key));
FN(void *, key_code_to_index, (int idx));
FN(unsigned char, vector_grow, (void *vec, unsigned long long elem, unsigned long long n, void *cap));
FN(void *, libc_bsearch, (const void *key, const void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
extern "C" unsigned long long data_302110, data_302118;
extern "C" Ent *data_302120;
// insert a block-name entry into the sorted search list (and drop all cached meshes)
extern "C" unsigned char insert_block_name_entry_sorted(Vec *v, void *name, long long *src) {
    int id = key_name_to_code(name);
    if (id == -1) return 0;
    if (data_302120 && libc_bsearch(&id, data_302120, data_302110, 0x18, (int (*)(const void *, const void *))compare_vec24_key))
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
    clear_all();
    unsigned char ok = vector_grow(&data_302120, 0x18, data_302110, &data_302118);
    if (!ok) return ok;
    Ent *base = data_302120;
    libc_memmove(base + idx + 1, base + idx, (data_302110 - idx) * 0x18);
    base[idx].name = nm;
    base[idx].q = q;
    base[idx].d = d;
    base[idx].key = k;
    data_302110++;
    return ok;
}

struct Blk { unsigned char x, y, z, p3, p4, p5, p6, p7; Ent *ent; };
struct BMesh { char pad[8]; unsigned long long n, cap; Blk *b; };
FN(BMesh *, find_insert_mesh_record_chunk, (unsigned long long key));
// scan a 16x16x16 chunk section for every searched block id and append the hits to the section mesh
extern "C" void scan_16x16x16_chunk_section_searched(int cx, int sy, unsigned cz, const unsigned char *ids, BMesh **out) {
    if (!data_302110) return;
    unsigned long long key = (unsigned)cx | ((unsigned long long)cz << 32);
    int ybase = sy << 4;
    for (unsigned long long e = 0; e < data_302110; e++) {
        Ent *ent = &data_302120[e];
        char kb = (char)ent->key;
        v16c want = {kb, kb, kb, kb, kb, kb, kb, kb, kb, kb, kb, kb, kb, kb, kb, kb};
        for (unsigned long long r = 0; r != 0x100; r++) {
            v16c row = *(const v16cu *)(ids + r * 16);
            unsigned mask = __builtin_ia32_pmovmskb128((v16c)(row == want));
            if (!mask) continue;
            unsigned char y = (unsigned char)(((unsigned)(r << 4) >> 8) + ybase);
            unsigned char z = (unsigned char)(r & 0xf);
            for (int x = 0; x != 0x10; x++) {
                if (!((mask >> x) & 1)) continue;
                BMesh *m = *out;
                if (!m) {
                    m = find_insert_mesh_record_chunk(key);
                    *out = m;
                    if (!m) return;
                }
                if (!vector_grow(&m->b, 0x10, m->n, &m->cap)) return;
                Blk *b = &m->b[m->n];
                b->x = (unsigned char)x; b->y = y; b->z = z;
                b->p3 = 0; b->p4 = 0; b->p5 = 0; b->p6 = 0; b->p7 = 0;
                b->ent = ent;
                m->n++;
            }
        }
    }
}

typedef short v8s __attribute__((vector_size(16)));
typedef short v8su __attribute__((vector_size(16), aligned(1)));
// same scan for 16-bit block data (id << 4 | meta)
extern "C" void scan_16_bit_block_data(int cx, int sy, unsigned cz, const unsigned short *ids, BMesh **out) {
    if (!data_302110) return;
    unsigned long long key = (unsigned)cx | ((unsigned long long)cz << 32);
    int ybase = sy << 4;
    for (unsigned long long e = 0; e < data_302110; e++) {
        Ent *ent = &data_302120[e];
        short kw = (short)ent->key;
        v8s want = {kw, kw, kw, kw, kw, kw, kw, kw};
#pragma clang loop unroll(disable) vectorize(disable)
        for (unsigned long long r = 0; r != 0x200; r++) {
            v8s row = *(const v8su *)(ids + r * 8);
            unsigned mask = __builtin_ia32_pmovmskb128((v16c)((row >> 4) == want));
            if (!mask) continue;
            unsigned char x = (unsigned char)((r & 1) << 3);
            unsigned char y = (unsigned char)(((unsigned)(r * 8) >> 8) + ybase);
            unsigned char z = (unsigned char)((r >> 1) & 0xf);
#pragma clang loop unroll(disable) vectorize(disable)
            for (int w = 0; w != 0x10; w += 2, x++) {
                if (!((mask >> w) & 3)) continue;
                BMesh *m = *out;
                if (!m) {
                    m = find_insert_mesh_record_chunk(key);
                    *out = m;
                    if (!m) return;
                }
                if (!vector_grow(&m->b, 0x10, m->n, &m->cap)) return;
                Blk *b = &m->b[m->n];
                b->x = x; b->y = y; b->z = z;
                b->p3 = 0; b->p4 = 0; b->p5 = 0; b->p6 = 0; b->p7 = 0;
                b->ent = ent;
                m->n++;
            }
        }
    }
}

extern "C" unsigned long long data_3020f8;
// find or insert the mesh record of chunk section `key` (sorted by x, then z)
extern "C" BMesh *find_insert_mesh_record_chunk(unsigned long long key) {
    Lists tmp = {};
    *(unsigned long long *)&tmp = key;
    if (data_3020f0.data) {
        void *hit = libc_bsearch(&tmp, data_3020f0.data, data_3020f0.size, 0x50,
                              (int (*)(const void *, const void *))compare_int_ids);
        if (hit) return (BMesh *)hit;
    }
    unsigned long long idx = 0;
    if (data_3020f0.size) {
        for (idx = 0; idx < data_3020f0.size; idx++) {
            const int *e = (const int *)&data_3020f0.data[idx];
            int c = (int)key - e[0];
            if (c == 0) c = (int)(key >> 32) - e[1];
            if (c <= 0) break;
        }
    }
    if (!vector_grow(&data_3020f0.data, 0x50, data_3020f0.size, &data_3020f8)) return 0;
    Lists *base = data_3020f0.data;
    libc_memmove(base + idx + 1, base + idx, (data_3020f0.size - idx) * 0x50);
    *(unsigned long long *)&base[idx] = key;
    base[idx].a = V3{}; base[idx].b = V3{}; base[idx].c = V3{};
    data_3020f0.size++;
    return (BMesh *)&data_3020f0.data[idx];
}

// ---- world events: chunk unload (0) / load (1) rebuild the chunk's meshes, block change (2) edits one ----
#include "slinky/jids.h"
struct WEv { Jvm *j; jobject world; unsigned type; int a, b, c, id; };
FN(jobject, java_world_get_chunk_from_chunk_coords, (Jvm *j, jobject w, int cx, int cz));
FN(jobject, java_get_chunk_storage_arrays, (Jvm *j, jobject chunk));
FN(jobject, java_get_id_b20, (Jvm *j, jobject sec));
FN(int, chunk_coordinate, (int x));
FN(int, block_coordinate, (int x));
FN(void, clear_mesh, (BMesh *m));
FN(void, clear_list_set, (Lists *l));
extern "C" void (*data_302128)(int cx, int sy, int cz, void *ids, BMesh **out);
static inline __attribute__((always_inline)) void drop_rec(Lists *r) {
    unsigned long long i = r - data_3020f0.data;
    data_3020f0.size--;
    libc_memmove(r, r + 1, (data_3020f0.size - i) * 0x50);
}
extern "C" void equipment_event(WEv *ev) {
    if (ev->type >= 2) {
        if (ev->type != 2) return;
        int id = ev->id;
        Ent *ent = data_302120 ? (Ent *)libc_bsearch(&id, data_302120, data_302110, 0x18,
                                                   (int (*)(const void *, const void *))compare_vec24_key)
                               : 0;
        int cx = chunk_coordinate(ev->a), cz = chunk_coordinate(ev->c);
        unsigned long long key = (unsigned)cx | (unsigned long long)(unsigned)cz << 32;
        Lists tmp = {};
        *(unsigned long long *)&tmp = key;
        BMesh *m = data_3020f0.data ? (BMesh *)libc_bsearch(&tmp, data_3020f0.data, data_3020f0.size, 0x50,
                                                         (int (*)(const void *, const void *))compare_int_ids)
                                    : 0;
        unsigned char lx = ev->a - block_coordinate(cx), lz = ev->c - block_coordinate(cz), y = (unsigned char)ev->b;
        if (m) {
            unsigned long long n = m->n;
            for (unsigned long long i = 0; i < n; i++) {
                Blk *b = &m->b[i];
                if (b->x != lx || b->y != y || b->z != lz) continue;
                if (ent) {
                    b->ent = ent;
                } else {
                    m->n = --n;
                    libc_memmove(b, b + 1, (n - i) * sizeof(Blk));
                    if (!m->n) {
                        clear_list_set((Lists *)m);
                        drop_rec((Lists *)m);
                        return;
                    }
                }
                clear_mesh(m);
                return;
            }
            if (!ent) return;
        } else {
            if (!ent) return;
            unsigned long long idx = 0;
            if (data_3020f0.size) {
                for (idx = 0; idx < data_3020f0.size; idx++) {
                    const int *e = (const int *)&data_3020f0.data[idx];
                    int c = cx - e[0];
                    if (c == 0) c = cz - e[1];
                    if (c <= 0) break;
                }
            }
            if (!vector_grow(&data_3020f0.data, 0x50, data_3020f0.size, &data_3020f8)) return;
            Lists *base = data_3020f0.data;
            libc_memmove(base + idx + 1, base + idx, (data_3020f0.size - idx) * 0x50);
            *(unsigned long long *)&base[idx] = key;
            base[idx].a = V3{}; base[idx].b = V3{}; base[idx].c = V3{};
            data_3020f0.size++;
            if (!data_3020f0.data) return;
            m = (BMesh *)&data_3020f0.data[idx];
        }
        Blk nb = {lx, y, lz, 0, 0, 0, 0, 0, ent};
        if (!vector_grow(&m->b, 0x10, m->n, &m->cap)) return;
        m->b[m->n] = nb;
        m->n++;
        clear_mesh(m);
        return;
    }
    Lists tmp = {};
    *(unsigned long long *)&tmp = *(unsigned long long *)&ev->a;
    if (data_3020f0.data) {
        Lists *r = (Lists *)libc_bsearch(&tmp, data_3020f0.data, data_3020f0.size, 0x50,
                                      (int (*)(const void *, const void *))compare_int_ids);
        if (r) {
            clear_lists(r);
            drop_rec(r);
        }
    }
    if (ev->type != 1) return;
    Jvm *j = ev->j;
    int cx = ev->a, cz = ev->b;
    jobject chunk = java_world_get_chunk_from_chunk_coords(j, ev->world, cx, cz);
    if (!chunk) return;
    BMesh *out = 0;
    JNIEnv *env = j->env;
    jobjectArray secs = (jobjectArray)java_get_chunk_storage_arrays(j, chunk);
    for (int i = 0; i != 16; i++) {
        jobject s = (*env)->GetObjectArrayElement(env, secs, i);
        if (!s) continue;
        jarray ids = (jarray)java_get_id_b20(j, s);
        void *p = (*env)->GetPrimitiveArrayCritical(env, ids, 0);
        data_302128(cx, i, cz, p, &out);
        (*env)->ReleasePrimitiveArrayCritical(env, ids, p, 0);
    }
    if (out) clear_mesh(out);
    return;
}
