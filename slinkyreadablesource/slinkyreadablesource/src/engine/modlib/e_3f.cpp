// 0x3f000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
FN(void, libc_free, (void *p));
FN(unsigned long long, libc_strlen, (const char *s));
struct Vec { unsigned long long size, cap; void *data; };
struct Lists { char pad[8]; Vec a, b, c; };
extern "C" void clear_list_set(Lists *l) {
    l->a.size = 0; l->a.cap = 0; libc_free(l->a.data); l->a.data = 0;
    l->b.size = 0; l->b.cap = 0; libc_free(l->b.data); l->b.data = 0;
    l->c.size = 0; l->c.cap = 0; libc_free(l->c.data); l->c.data = 0;
}
extern "C" void text_filter_upper(char *buf, char c) {
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

FN(void *, java_get_chunk_provider_client_2, (void *a, void *p));
FN(void *, java_get_chunk_provider_client, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(int, java_get_chunk_x_position, (void *a, void *e));
FN(int, java_get_chunk_z_position, (void *a, void *e));
struct Rec { void *a; void *p; int one; int id; int lvl; int z0, z1, z2; };
FN(void, equipment_event, (Rec *r));
struct S3f { void *a; char pad[0x18]; void *p20; };
extern "C" void sort_float_entries(S3f *s) {
    void *x = java_get_chunk_provider_client_2(s->a, s->p20);
    if (!x) return;
    void *list = java_get_chunk_provider_client(s->a, x);
    int n = java_call_list_size(s->a, list);
    if (n <= 0) return;
    for (int i = 0; i != n; i++) {
        void *e = java_call_list_get(s->a, list, i);
        int id = java_get_chunk_x_position(s->a, e);
        int lvl = java_get_chunk_z_position(s->a, e);
        Rec r;
        r.a = s->a;
        r.p = s->p20;
        r.one = 1;
        r.id = id;
        r.lvl = lvl;
        r.z0 = 0; r.z1 = 0; r.z2 = 0;
        equipment_event(&r);
    }
}

// rebuild a coloured box mesh: 8 corner vertices + 24 line indices per block
FN(unsigned char, vector_grow, (void *data, unsigned long long elem, unsigned long long size, void *cap));
struct Vtx { float x, y, z; unsigned char r, g, b, a; };
struct Corner { float x, y, z; };
struct Blk { unsigned char x, y, z, pad; char pad2[4]; float *color; };
struct Mesh {
    char pad[8]; unsigned long long nblocks; char pad2[8]; Blk *blocks;
    unsigned long long nv, capv; Vtx *v;
    unsigned long long ni, capi; unsigned short *idx;
};
extern "C" const float rdata_154168;
extern "C" const Corner rdata_1541b0[8];
extern "C" const unsigned short rdata_154210[24];
extern "C" void clear_mesh(Mesh *m) {
    m->nv = 0;
    m->ni = 0;
    if (!m->nblocks) return;
    for (unsigned long long b = 0; b < m->nblocks; b++) {
        Blk *bk = &m->blocks[b];
        float *col = bk->color;
        unsigned char r = (unsigned char)(int)(col[2] * rdata_154168);
        unsigned char g = (unsigned char)(int)(col[3] * rdata_154168);
        unsigned char bl = (unsigned char)(int)(col[4] * rdata_154168);
        unsigned long long base = m->nv;
#pragma clang loop unroll(disable)
        for (int k = 0; k < 8; k++) {
            if (!vector_grow(&m->v, 0x10, m->nv, &m->capv)) return;
            Vtx *v = &m->v[m->nv];
            v->x = rdata_1541b0[k].x + (float)bk->x;
            v->y = rdata_1541b0[k].y + (float)bk->y;
            v->z = rdata_1541b0[k].z + (float)bk->z;
            v->r = r; v->g = g; v->b = bl; v->a = 0xff;
            m->nv++;
        }
#pragma clang loop unroll(disable)
        for (int k = 0; k < 24; k++) {
            if (!vector_grow(&m->idx, 2, m->ni, &m->capi)) return;
            m->idx[m->ni] = (unsigned short)(rdata_154210[k] + base);
            m->ni++;
        }
    }
}

// ---- free the whole block-search mesh cache: every section's three lists, then the section array ----
struct LVec3f { unsigned long long size, cap; Lists *data; };
extern "C" LVec3f data_3020f0;
extern "C" void free_allocator_pages() {
    for (unsigned long long i = 0; i < data_3020f0.size; i++) {
        Lists *l = &data_3020f0.data[i];
        l->a.size = 0; l->a.cap = 0; libc_free(l->a.data); l->a.data = 0;
        l->b.size = 0; l->b.cap = 0; libc_free(l->b.data); l->b.data = 0;
        l->c.size = 0; l->c.cap = 0; libc_free(l->c.data); l->c.data = 0;
    }
    data_3020f0.size = 0;
    data_3020f0.cap = 0;
    libc_free(data_3020f0.data);
    data_3020f0.data = 0;
}
