// Block ESP - per-chunk line meshes of the "allowed_blocks" found around the
// player, rebuilt on demand and drawn with GL client-side arrays.
//   +0x48 tick, +0x98 enable/disable, +0xa8 render, +0x188 block update, +0x1e8 shutdown
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Buf { void *p; unsigned long long n, cap; };
struct Mesh {                    // 0x50 bytes
    int cx, cz;                  // chunk coordinates
    unsigned long long a, b;     // +0x08
    Buf lines;                   // +0x18
    Buf verts;                   // +0x30 (16-byte vertices: xyz float + rgba)
    unsigned short *idx;         // +0x48
};
struct EvHook { char pad0[8]; Ctx *ctx; void *ev; unsigned char on; };

extern "C" unsigned char data_2f19b0;     // allowed_blocks changed
extern "C" float data_2f19e8;             // "line_width"
extern "C" float data_2f1a20;             // "block_render_limit"
extern "C" float data_2f1a58;             // "range"
extern "C" unsigned char data_2f1a78;     // "should_limit_block_render"
extern "C" char data_2f1740[];            // Block ESP module descriptor
extern "C" unsigned long long data_3020f0[2];   // mesh count / capacity
extern "C" Mesh *data_302100;             // meshes
extern "C" unsigned char data_302108;     // rebuild requested
extern "C" void *data_302110[2];
extern "C" void *data_302120;
extern "C" void *data_302128;             // block scan callback
extern "C" void (**rdata_14fd08)(unsigned);                       // glEnableClientState
extern "C" void (**rdata_14fcf8)(unsigned);                       // glDisableClientState
extern "C" void (**rdata_150cb0)(float);                          // glLineWidth
extern "C" void (**rdata_150cc0)();                               // glPushMatrix
extern "C" void (**rdata_150cb8)();                               // glPopMatrix
extern "C" void (**rdata_150cd8)(float, float, float);            // glTranslatef
extern "C" void (**rdata_14fd10)(int, unsigned, int, const void *);   // glVertexPointer
extern "C" void (**rdata_154310)(int, unsigned, int, const void *);   // glColorPointer
extern "C" void (**rdata_14fd00)(unsigned, unsigned long long, unsigned, const void *);  // glDrawElements
extern "C" const double rdata_154150;     // 1/16
extern "C" const float rdata_154158;      // limit scale
extern "C" const double rdata_154160;     // 0.5

FN(void, clear_mesh, (Mesh *m));
FN(int, java_call_object_hash_code, (void *a, void *world));
FN(void, sort_float_entries, (Ctx *c));
FN(void, libc_free, (void *p));
FN(double, libc_hypot, (double x, double z));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(void, equipment_event, (void *x));
FN(void, scan_16x16x16_chunk_section_searched, ());
FN(void, scan_16_bit_block_data, ());

static inline __attribute__((always_inline)) void clear_meshes() {
    for (unsigned long long i = 0; i < data_3020f0[0]; i++) {
        Mesh *m = &data_302100[i];
        m->a = m->b = 0;
        libc_free(m->lines.p);
        m->lines.p = nullptr;
        m->lines.n = 0;
        m->lines.cap = 0;
        libc_free(m->verts.p);
        m->verts.p = nullptr;
        m->verts.n = 0;
        m->verts.cap = 0;
        libc_free(m->idx);
        m->idx = nullptr;
    }
    data_3020f0[0] = data_3020f0[1] = 0;
    libc_free(data_302100);
    data_302100 = nullptr;
}

extern "C" void block_esp_part(Hook *h) {
    Ctx *c = h->ctx;
    if (c->f40 == 1 && data_2f19b0) {
        for (unsigned long long i = 0; i < data_3020f0[0]; i++)
            clear_mesh(&data_302100[i]);
        data_2f19b0 = 0;
    }
    if (!c->p20) {
        clear_meshes();
        return;
    }
    java_call_object_hash_code(c->a, c->p20);
    if (data_302108 == 1) {
        clear_meshes();
        sort_float_entries(c);
        data_302108 = 0;
    }
}

extern "C" void block_esp_part_2(EvHook *h) {
    if (__builtin_expect(h->ev == data_2f1740, 1)) {
        if (h->on) {
            Ctx *c = h->ctx;
            data_302128 = **(int **)c->a == 0 ? (void *)scan_16x16x16_chunk_section_searched : (void *)scan_16_bit_block_data;
            if (c->p20) {
                sort_float_entries(c);
                return;
            }
        } else {
            clear_meshes();
        }
    }

}

extern "C" void block_esp_part_3(char *h) {
    if (!data_3020f0[0])
        return;
    void (**enable)(unsigned) = rdata_14fd08;
    (*enable)(0x8074);
    (*enable)(0x8076);
    (*rdata_150cb0)(data_2f19e8);
    unsigned long long lim = (unsigned long long)(data_2f1a20 * rdata_154158);
    unsigned long long limit = data_2f1a78 ? lim : 0x1000;
    unsigned long long n = data_3020f0[0];
    if (n) {
        Vec3 *cam = (Vec3 *)(h + 8);
        double k = rdata_154150;
        double cx = cam->x * k;
        double cz = k * cam->z;
        Mesh *arr = data_302100;
        float range = data_2f1a58;
        void (**pop)() = rdata_150cb8;
        double half = rdata_154160;
        for (unsigned long long i = 0; i < n; i++) {
            Mesh *m = &arr[i];
            double dx = cx - ((double)m->cx + half);
            double dz = cz - ((double)m->cz + half);
            if (libc_hypot(dx, dz) > (double)range)
                continue;
            (*rdata_150cc0)();
            Vec3 o = {(double)(m->cx << 4), 0.0, (double)(m->cz << 4)};
            Vec3 r = vec3_sub(o, *cam);
            (*rdata_150cd8)((float)r.x, (float)r.y, (float)r.z);
            (*rdata_14fd10)(3, 0x1406, 0x10, m->verts.p);
            (*rdata_154310)(3, 0x1401, 0x10, (char *)m->verts.p + 0xc);
            unsigned long long cnt = m->verts.n;
            (*rdata_14fd00)(1, limit < cnt ? limit : cnt, 0x1403, m->idx);
            (*pop)();
            arr = data_302100;
            range = data_2f1a58;
            n = data_3020f0[0];
        }
    }
    void (**disable)(unsigned) = rdata_14fcf8;
    (*disable)(0x8074);
    (*disable)(0x8076);
}

extern "C" void block_esp_part_4(char *h) {
    equipment_event(h + 8);
}

extern "C" void block_esp_part_5(Hook *h) {
    data_302110[0] = data_302110[1] = nullptr;
    libc_free(data_302120);
    data_302120 = nullptr;
    clear_meshes();
}
