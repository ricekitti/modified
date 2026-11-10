// Chest ESP - descriptor +0x48 (collect containers in range, max 100) and
// +0xa8 (render a box per container with the per-type colour).
#include "slinky/engine.h"

struct Vec3 { double x, y, z; };
struct Found { Vec3 pos; int id; int pad; };          // 0x20 bytes

extern "C" float data_2ecb48;            // "range"
extern "C" unsigned char data_2ecb68;    // "render_chest"
extern "C" unsigned char data_2ecb88;    // "render_ender_chest"
extern "C" unsigned char data_2ecba8;    // "render_trapped_chest"
extern "C" unsigned char data_2ecbc8;    // "render_beacon"
extern "C" float data_2ecc00;            // "line_width"
extern "C" float data_2ecc10[];          // "color_ender_chest"
extern "C" float data_2ecc30[];          // "color_trapped_chest"
extern "C" float data_2ecc50[];          // "color_beacon"
extern "C" float data_2ecc70[];          // "color_chest"
extern "C" unsigned char data_2ecca0;    // "filled"
extern "C" unsigned long long data_2fa360;   // found count
extern "C" Found data_2fa370[100];
extern "C" void (**rdata_150cb0)(float);                    // glLineWidth
extern "C" void (**rdata_150ca0)(float, float, float);      // glColor3f
extern "C" void (**rdata_150cb8)();                         // glPopMatrix
extern "C" void (**rdata_150cc0)();                         // glPushMatrix
extern "C" void (**rdata_150cd8)(float, float, float);      // glTranslatef
extern "C" void (**rdata_150cd0)(float, float, float);      // glScalef
extern "C" void (**rdata_13e448)(float, float, float, float);  // glColor4f
extern "C" const double rdata_151ad0[2];  // {0.5, 0.5}
extern "C" const float rdata_151a60;      // box scale
extern "C" const float rdata_151a64;      // fill alpha

FN(Vec3, java_get_entity_pos_x, (void *a, void *p));
FN(void *, java_get_world_3, (void *a, void *x));
FN(int, java_call_list_size, (void *a, void *list));
FN(void *, java_call_list_get, (void *a, void *list, int i));
FN(void *, java_get_tile_entity, (void *a, void *te));
FN(int, get_block_id, (void *a, void *blk));
FN(Vec3, java_get_block_pos_a_2, (void *a, void *te));
FN(double, vec3_distance_sq, (Vec3 a, Vec3 b));
FN(void, gl_array_xyz_float, (void *h));
FN(Vec3, vec3_sub, (Vec3 a, Vec3 b));
FN(Vec3, vec3_add, (Vec3 a, Vec3 b));
FN(void, gl_array_color1, ());
FN(void, gl_array_color2, ());
FN(void, enable_vertex_array, ());

extern "C" void chest_esp_part(Hook *h) {
    data_2fa360 = 0;
    Ctx *c = h->ctx;
    if (!c->p20)
        return;
    Vec3 eye = java_get_entity_pos_x(c->a, c->b);
    void *list = java_get_world_3(c->a, c->p20);
    for (int i = 0; i < java_call_list_size(c->a, list); i++) {
        void *te = java_call_list_get(c->a, list, i);
        void *blk = java_get_tile_entity(c->a, te);
        if (!blk)
            continue;
        int id = get_block_id(c->a, blk);
        if (id > 0x89) {
            if (id == 0x8a) {
                if (data_2ecbc8 != 1) continue;
            } else if (id == 0x92) {
                if (!data_2ecba8) continue;
            } else {
                continue;
            }
        } else if (id == 0x36) {
            if (!data_2ecb68) continue;
        } else if (id == 0x82) {
            if (!data_2ecb88) continue;
        } else {
            continue;
        }
        Vec3 p = java_get_block_pos_a_2(c->a, te);
        if (vec3_distance_sq(p, eye) > (double)(data_2ecb48 * data_2ecb48))
            continue;
        unsigned long long n = data_2fa360;
        data_2fa360 = n + 1;
        data_2fa370[n].pos = p;
        data_2fa370[n].id = id;
        if (n + 1 == 100)
            return;
    }
}

extern "C" void chest_esp_part_2(char *h) {
    gl_array_xyz_float(h);
    (*rdata_150cb0)(data_2ecc00);
    if (data_2fa360) {
        Vec3 *cam = (Vec3 *)(h + 8);
        Found *f = data_2fa370;
        for (unsigned long long i = 0; i < data_2fa360; i++, f++) {
            float *col = data_2ecc10;
            if (f->id == 0x82)
                ;
            else if (f->id == 0x8a)
                col = data_2ecc50;
            else if (f->id == 0x92)
                col = data_2ecc30;
            else
                col = data_2ecc70;
            Vec3 ctr = vec3_add(vec3_sub(f->pos, *cam), Vec3{rdata_151ad0[0], rdata_151ad0[1], 0.5});
            (*rdata_150cc0)();
            (*rdata_150cd8)((float)ctr.x, (float)ctr.y, (float)ctr.z);
            (*rdata_150cd0)(rdata_151a60, rdata_151a60, rdata_151a60);
            if (data_2ecca0 == 1) {
                (*rdata_13e448)(col[0], col[1], col[2], rdata_151a64);
                gl_array_color1();
            }
            (*rdata_150ca0)(col[0], col[1], col[2]);
            gl_array_color2();
            (*rdata_150cb8)();
        }
    }
    enable_vertex_array();
}
