// 0xe000: comparators, static tables
#include "slinky/engine.h"
extern "C" int libc_strcmp(const char *a, const char *b);
extern "C" int compare_cstr_ptr(const char **a, const char **b) {
    const char *x = *a;

    return libc_strcmp(x, *b);
}
extern "C" int compare_int_field(const int *a, const char *b) { return *a - *(const int *)(b + 8); }
extern "C" int compare_int(const int *a, const int *b) { return *a - *b; }
extern "C" int compare_cstr_desc(const char **a, const char **b) {
    const char *x = *a;

    return libc_strcmp(x, *b);
}
extern "C" char data_2e5010[], data_2e55e8[];
extern "C" void default_name_list(void **p, unsigned long long *n) { *p = data_2e5010; *n = 0x181; }
extern "C" void default_key_list(void **p, unsigned long long *n) { *p = data_2e55e8; *n = 0xc6; }

extern "C" int compare_long_field(const char **a, const char **b) {
    long long y = *(const long long *)((const char *)b + 0x30);
    if (*(const long long *)((const char *)a + 0x30) < y) return -1;
    if (*(const long long *)((const char *)a + 0x30) > y) return 1;
    const char *x = *a;

    return libc_strcmp(x, *b);
}

FN(void *, libc_bsearch, (const void *key, const void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
struct MEnt { int id; int pad; unsigned long long n; void *pad2; void **list; };
struct IdMap { char pad[0x10]; unsigned long long count; char pad2[8]; MEnt *data; unsigned char multi; };
// lookup id -> value (multi-maps: test membership of val)
extern "C" void *key_map_get(IdMap *m, int id, void *val) {
    if (!id) return 0;
    MEnt k = {};
    k.id = id;
    if (!m->data) return 0;
    MEnt *e = (MEnt *)libc_bsearch(&k, m->data, m->count, 0x20, (int (*)(const void *, const void *))compare_int);
    if (!e) return 0;
    unsigned long long n = e->n;
    if (m->multi == 1) {
        for (unsigned long long i = 0; i < n; i++) {
            void *x = e->list[i];
            if (x == val) return x;
        }
        return 0;
    }
    if (!n) return 0;
    return e->list[0];
}

struct CbMap { unsigned char (*fn)(void *item, int id, int arg); char pad[8]; unsigned long long count; char pad2[8]; MEnt *data; };
// call fn on every item registered under id; true if any returned true
extern "C" bool key_map_dispatch(CbMap *m, int id, int arg) {
    MEnt k = {};
    k.id = id;
    unsigned char r = 0;
    if (m->data) {
        MEnt *e = (MEnt *)libc_bsearch(&k, m->data, m->count, 0x20, (int (*)(const void *, const void *))compare_int);
        if (e && e->n) {
            for (unsigned long long i = 0; i < e->n; i++) r |= m->fn(e->list[i], id, arg);
        }
    }
    return r & 1;
}

// ---- id -> value(s) map (sorted 32-byte entries; "multi" maps keep a list per id) ----
FN(unsigned char, vector_grow, (void *data, unsigned long long elem, unsigned long long n, void *cap));
FN(void, libc_free, (void *p));
FN(void *, libc_memmove, (void *d, const void *s, unsigned long long n));
struct IdMap2 { void *fn; void (*release)(void *v); unsigned long long count, cap; MEnt *data; unsigned char multi; };
extern "C" bool key_map_add(IdMap2 *m, int id, void *val) {
    if (!id) return true;
    MEnt k = {};
    k.id = id;
    MEnt *e = m->data ? (MEnt *)libc_bsearch(&k, m->data, m->count, 0x20, (int (*)(const void *, const void *))compare_int) : 0;
    if (!e) {
        unsigned long long idx = 0;
        if (m->count) {
            for (idx = 0; idx < m->count; idx++)
                if (id - m->data[idx].id <= 0) break;
        }
        if (!vector_grow(&m->data, 0x20, m->count, &m->cap)) return false;
        MEnt *base = m->data;
        libc_memmove(base + idx + 1, base + idx, (m->count - idx) * 0x20);
        base[idx] = MEnt{};
        base[idx].id = id;
        m->count++;
        if (!m->data) return false;
        e = m->data + idx;
    }
    unsigned long long n = e->n;
    if (m->multi == 1) {
        for (unsigned long long i = 0; i < n; i++)
            if (e->list[i] == val) return true;
    } else if (n) {
        if (m->release) m->release(e->list[0]);
        e->n = 0;
        e->pad2 = 0;
        libc_free(e->list);
        e->list = 0;
        n = e->n;
    }
    if (!vector_grow(&e->list, 8, n, &e->pad2)) return false;
    e->list[e->n] = val;
    e->n++;
    return true;
}
extern "C" void key_map_remove(IdMap2 *m, int id, void *val) {
    if (!id) return;
    MEnt k = {};
    k.id = id;
    if (!m->data) return;
    MEnt *e = (MEnt *)libc_bsearch(&k, m->data, m->count, 0x20, (int (*)(const void *, const void *))compare_int);
    if (!e) return;
    unsigned long long n0 = e->n;
    if (!n0) return;
    if (m->multi == 1) {
        void **list = e->list;
        unsigned long long i;
        for (i = 0; i < n0; i++)
            if (list[i] == val) break;
        if (i == n0) return;
        unsigned long long n = n0;
        if (m->release) {
            m->release(list[i]);
            n = e->n;
            list = e->list;
        }
        e->n = --n;
        libc_memmove(&list[i], &list[i + 1], (n - i) * 8);
        if (n0 != 1) return;
    } else {
        if (m->release) m->release(e->list[0]);
    }
    e->n = 0;
    e->pad2 = 0;
    libc_free(e->list);
    e->list = 0;
    unsigned long long idx = (unsigned long long)(e - m->data);
    unsigned long long c = m->count;
    m->count = c - 1;
    libc_memmove(e, e + 1, (c - idx - 1) * 0x20);
}

// ---- profile list (names + "modified" label + bind), refreshed from the profiles folder ----
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));   // calloc
FN(void *, libc_memcpy, (void *d, const void *s, unsigned long long n));
FN(unsigned long long, libc_strlen, (const char *s));
FN(int, libc_snprintf, (char *buf, unsigned long long n, const char *fmt, ...));   // snprintf
FN(void, libc_qsort, (void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(int, list_profiles, (void *ctx, int (*cb)(void *fi, void *ctx)));
extern "C" const char rdata_13ff7e[], rdata_13ffb1[], rdata_13ffc9[], rdata_13ff99[];
struct ProfEnt { char *name; char *end; char desc[0x20]; long long age; int bind; int pad; };
struct ProfList { unsigned long long count, cap; ProfEnt *data; };
struct OldEnt { char *name; int bind; int pad; };
struct OldList { unsigned long long count, cap; OldEnt *data; };
struct FileInfo { const char *name; long long age; };
struct ScanCtx { ProfList *list; OldList *old; };
extern "C" int scan_next_file(FileInfo *fi, ScanCtx *ctx) {
    const char *name = fi->name;
    unsigned long long n = libc_strlen(name);
    char *s = (char *)libc_calloc(n + 1, 1);
    if (!s) return 0x3ed;
    libc_memcpy(s, name, n);
    ProfEnt e = {};
    e.name = s;
    e.end = s + (n < 0xf ? n : 0xf);
    e.age = fi->age;
    OldEnt key = {s, 0, 0};
    OldList *old = ctx->old;
    if (old->data) {
        OldEnt *f = (OldEnt *)libc_bsearch(&key, old->data, old->count, 0x10, (int (*)(const void *, const void *))compare_cstr_desc);
        if (f) e.bind = f->bind;
    }
    long long age = fi->age;
    if (age == -1) {
        __builtin_memcpy(e.desc, rdata_13ff7e, 0x18);
    } else {
        long long days = age / 86400;
        if (days == 1) __builtin_memcpy(e.desc, rdata_13ffb1, 0x18);
        else if (days == 0) { __builtin_memcpy(e.desc, rdata_13ffc9, 0x10); *(long long *)(e.desc + 0x10) = 0; }
        else libc_snprintf(e.desc, 0x19, rdata_13ff99, days);
    }
    ProfList *l = ctx->list;
    if (!vector_grow(&l->data, 0x40, l->count, &l->cap)) {
        libc_free(s);
        return 0x3ed;
    }
    l->data[l->count] = e;
    l->count++;
    return 0;
}
extern "C" int load_profiles(ProfList *l) {
    OldList old = {0, 0, 0};
    int r;
    if (l->count) {
        unsigned long long left = l->count;
        for (ProfEnt *e = l->data; ; ) {
            if (e->bind) {
                const char *nm = e->name;
                unsigned long long n = libc_strlen(nm);
                char *s = (char *)libc_calloc(n + 1, 1);
                if (!s) { r = 0x3ed; goto cleanup; }
                libc_memcpy(s, nm, n);
                int bind = e->bind;
                if (vector_grow(&old.data, 0x10, old.count, &old.cap)) {
                    old.data[old.count].name = s;
                    old.data[old.count].bind = bind;
                    old.data[old.count].pad = 0;
                    old.count++;
                }
            }
            if (!--left) break;
            e = l->data + (l->count - left);
        }
        if (old.data) libc_qsort(old.data, old.count, 0x10, (int (*)(const void *, const void *))compare_cstr_desc);
        for (unsigned long long i = 0; i < l->count; i++) libc_free(l->data[i].name);
        l->count = 0;
        l->cap = 0;
        libc_free(l->data);
        l->data = 0;
    }
    {
        ScanCtx ctx = {l, &old};
        r = list_profiles(&ctx, (int (*)(void *, void *))scan_next_file);
        if (l->count && l->data) libc_qsort(l->data, l->count, 0x40, (int (*)(const void *, const void *))compare_long_field);
    }
cleanup:
    for (unsigned long long i = 0; i < old.count; i++) libc_free(old.data[i].name);
    old.count = 0;
    old.cap = 0;
    libc_free(old.data);
    return r;
}

// ---- generic dynamic arrays (as 0xdd0..0xdf90): {u32 cap, u32 count} header, element size passed in ----
struct AHdr { unsigned cap, count; };
typedef int (*ACmp)(void *ctx, const void *key, const void *elem);
FN(void *, libc_realloc, (void *p, unsigned long long n));   // realloc
extern "C" __attribute__((noreturn)) void libc_abort();   // abort
FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));   // memcpy
// sorted find-or-insert: the existing element equal to key, or key inserted in order
extern "C" void *vector_find_insert(void **data, unsigned elem, AHdr *h, const void *key, ACmp cmp) {
    unsigned hi = h->count, idx = 0, cnt = 0;
    if (hi) {
        char *base = (char *)*data;
        unsigned lo = 0;
        for (;;) {
            unsigned mid = lo + ((hi - lo) >> 1);
            int r = cmp(0, key, base + mid * elem);
            if (r < 0) {
                hi = mid;
                if (lo >= hi) break;
            } else if (r == 0) {
                return (char *)*data + mid * elem;
            } else {
                lo = mid + 1;
                if (lo >= hi) break;
            }
        }
        cnt = h->count;
        idx = hi;
    }
    char *base = (char *)*data;
    if (cnt >= h->cap) {
        unsigned cap = h->cap ? h->cap * 2 : 8;
        h->cap = cap;
        void *p = libc_realloc(base, cap * elem);
        if (!p) libc_abort();
        *data = p;
        base = (char *)p;
        cnt = h->count;
    }
    char *at = base + idx * elem;
    libc_memmove(base + (idx + 1) * elem, at, (cnt - idx) * elem);
    libc_memcpy_alt(at, key, elem);
    h->count++;
    return at;
}
// erase index i
extern "C" void *vector_erase(void *base, unsigned elem, AHdr *h, unsigned i) {
    unsigned n = --h->count;
    return libc_memmove((char *)base + i * elem, (char *)base + (i + 1) * elem, (n - i) * elem);
}
// erase the element at p
extern "C" void *vector_erase_value(void *base, unsigned elem, AHdr *h, void *p) {
    unsigned i = (unsigned)((unsigned long long)((char *)p - (char *)base) / elem);
    unsigned n = --h->count;
    return libc_memmove((char *)base + i * elem, (char *)base + (i + 1) * elem, (n - i) * elem);
}
// make room for element n (64-bit sizes): starts at 8, doubles
extern "C" unsigned char vector_grow(void *datav, unsigned long long elem, unsigned long long n, void *capv) {
    void **data = (void **)datav;
    unsigned long long *cap = (unsigned long long *)capv;
    unsigned long long c = *cap;
    if (n < c) return true;
    void *p;
    if (*data) {
        c += c;
        p = libc_realloc(*data, elem * c);
    } else {
        c = 8;
        p = libc_calloc(8, elem);
    }
    if (!p) return false;
    *data = p;
    *cap = c;
    return true;
}

// ---- block name <-> id tables (sorted by name / by id, 0x181 entries) ----
struct NameId { const char *name; int id; int pad; };
extern "C" NameId data_2e5c20[], data_2e7430[];
extern "C" int key_name_to_code(const char *name) {
    const char *k = name;
    NameId *e = (NameId *)libc_bsearch(&k, data_2e5c20, 0x181, 0x10, (int (*)(const void *, const void *))compare_cstr_ptr);
    int r;
    if (e) r = e->id;
    else r = -1;

    return r;
}
extern "C" const char *key_code_to_index(int id) {
    int k = id;
    NameId *e = (NameId *)libc_bsearch(&k, data_2e7430, 0x181, 0x10, (int (*)(const void *, const void *))compare_int_field);
    const char *r;
    if (e) r = e->name;
    else r = 0;

    return r;
}
// call fn(item, id, 0) for every registered item
extern "C" void invoke_callback(CbMap *m) {
    for (unsigned long long i = 0; i < m->count; i++) {
        MEnt *e = &m->data[i];
        if (!e->n) continue;
        unsigned long long j = 0;
        do {
            void *it = e->list[j];

            m->fn(it, e->id, 0);
        } while (++j < e->n);
    }
}

// ---- profile list accessors ----
struct Profs { unsigned long long count, cap; ProfEnt *data; void *bind; };
extern "C" void *profile_set_bind(Profs *p) { return p->bind; }
extern "C" ProfEnt *profile_array(Profs *p, unsigned long long *n) {
    *n = p->count;
    return p->count ? p->data : 0;
}
extern "C" ProfEnt *find_profile(Profs *p, const char *name) {
    unsigned long long n = p->count;
    if (!n) return 0;
    ProfEnt *e = p->data;
    for (; n; n--, e++)
        if (libc_strcmp(name, e->name) == 0) return e;
    return 0;
}

// ---- gap-scan functions of the 0xe000 block ----
// grow a ring buffer when full: capacity doubles (16 first); if the contents wrap around, the wrapped part
// [0, tail) is copied behind the old end. Indices are free-running, taken modulo the (power of two) capacity.
struct Ring { unsigned long long cap, head, tail; };
extern "C" void vector_reserve(void **data, unsigned long long elem, Ring *r) {
    unsigned long long cap = r->cap;
    if (r->tail - r->head < cap) return;
    unsigned long long ncap = cap ? cap * 2 : 16;
    r->cap = ncap;
    void *p = libc_realloc(*data, ncap * elem);
    if (!p) libc_abort();
    *data = p;
    unsigned long long mask = cap - 1;
    unsigned long long h = r->head & mask, t = r->tail & mask;
    if (h < t) return;
    libc_memcpy_alt((char *)p + cap * elem, p, t * elem);
}

// small zeroed records
struct Node30 { void *a, *b; char pad[0x18]; unsigned char flag; char pad2[7]; };   // 0x30
extern "C" Node30 *find_entry_index(void *a, void *b, bool flag) {
    Node30 *n = (Node30 *)libc_calloc(1, sizeof(Node30));
    if (n) {
        n->a = a;
        n->b = b;
        n->flag = flag;
    }
    return n;
}
struct Node20 { char pad[0x18]; void *p; };   // 0x20
extern "C" Node20 *count_matching(void *p) {
    Node20 *n = (Node20 *)libc_calloc(1, sizeof(Node20));
    n->p = p;   // (the original does not check the allocation)
    return n;
}

// destroy a callback map (see CbMap): run dtor on every registered item, free the lists, the entry array and the map
struct DtorMap { void *fn; void (*dtor)(void *item); unsigned long long count; unsigned long long cap; MEnt *data; };
extern "C" void key_map_destroy(DtorMap *m) {
    if (!m) return;
    if (m->count) {
        void (*dtor)(void *) = m->dtor;
        for (unsigned long long i = 0; i < m->count; i++) {
            MEnt *e = &m->data[i];
            if (dtor && e->n)
#pragma clang loop unroll(disable)
                for (unsigned long long j = 0; j < e->n; j++) dtor(e->list[j]);
            e->n = 0;
            e->pad2 = 0;
            libc_free(e->list);
            e->list = 0;
        }
    }
    m->count = 0;
    m->cap = 0;
    libc_free(m->data);
    libc_free(m);
}

// free a profile list: every entry's name, the entry array and the list itself
extern "C" void destroy_profiles(ProfList *l) {
    if (!l) return;
#pragma clang loop unroll(disable)
    for (unsigned long long i = 0; i < l->count; i++) libc_free(l->data[i].name);
    l->count = 0;
    l->cap = 0;
    libc_free(l->data);
    libc_free(l);
}

// look up the GLFW entry points the client hooks from the already loaded "glfw" module
// (0x3e8: module not loaded, 0x3e9: a function is missing)
FN(void *, kernel32_lookups, (const char *module));
FN(void *, get_proc_address, (void *module, const char *name));
extern "C" void *data_2f7350, *data_2f7358, *data_2f7360, *data_2f7368, *data_2f7370, *data_2f7378, *data_2f7380, *data_2f7388;
extern "C" const char rdata_13ffe1[], rdata_13ffe6[], rdata_13fffc[], rdata_140013[], rdata_140032[], rdata_14004d[],
    rdata_140060[], rdata_140074[], rdata_14008d[];
extern "C" int init_glfw_entrypoints() {
    void *glfw = kernel32_lookups(rdata_13ffe1 /* "glfw" */);
    if (!glfw) return 0x3e8;
    if (!(data_2f7350 = get_proc_address(glfw, rdata_13ffe6 /* glfwGetCurrentContext */))) return 0x3e9;
    if (!(data_2f7358 = get_proc_address(glfw, rdata_13fffc /* glfwGetFramebufferSize */))) return 0x3e9;
    if (!(data_2f7360 = get_proc_address(glfw, rdata_140013 /* glfwSetFramebufferSizeCallback */))) return 0x3e9;
    if (!(data_2f7368 = get_proc_address(glfw, rdata_140032 /* glfwSetMouseButtonCallback */))) return 0x3e9;
    if (!(data_2f7370 = get_proc_address(glfw, rdata_14004d /* glfwSetKeyCallback */))) return 0x3e9;
    if (!(data_2f7378 = get_proc_address(glfw, rdata_140060 /* glfwSetCharCallback */))) return 0x3e9;
    if (!(data_2f7380 = get_proc_address(glfw, rdata_140074 /* glfwSetCursorPosCallback */))) return 0x3e9;
    if (!(data_2f7388 = get_proc_address(glfw, rdata_14008d /* glfwSetScrollCallback */))) return 0x3e9;
    return 0;
}
