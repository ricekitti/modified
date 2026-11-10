// 0x38000: helper block (no descriptive name recovered).
#include "slinky/engine.h"
FN(unsigned char, weapons_part, (void *a, void *b, unsigned *flags));
FN(void, queue_module_toggle, (void *mods, void *mod, unsigned char on));
FN(void *, get_current_screen, (void *a, void *x));
FN(void, keyboard_send_key, (void *a, void *screen, int x, int y));
extern "C" char data_2eed90[];
extern "C" unsigned char click_with_helper(void *a, void *b) { return weapons_part(a, b, 0); }
extern "C" void close_screen_and_release(Ctx *c) {
    queue_module_toggle(c->p08, data_2eed90, 0);
    void *s = get_current_screen(c->a, c->c18);
    if (s) keyboard_send_key(c->a, s, 0, 1);
}

// pick the target cell (of the 9x3 grid) closest to `cell`, scored by distance and yaw difference.
// type-2 cells always count, type-0 cells only when a random check passes; the best type-0 cell or the
// runner-up type-2 cell can win instead after a random walk check. *out = score, data_2fc600 = angle.
struct Rot { float yaw, pitch; };
struct Walk { double t0; float a, b; };
FN(Rot, rot_make, (float a, float b));
FN(float, rot_delta_length, (Rot a, Rot b));
FN(float, rot_distance, (Rot a, Rot b));
FN(float, wrap_angle, (float a));
FN(bool, random_chance, (float p));
FN(Rot, rot_make_floats, (float a, float b));
FN(bool, walk_should_change, (Walk *w, Rot r, double period));
extern "C" unsigned char data_2fc5e0[27];
extern "C" Walk data_2fc620, data_2fc630;
extern "C" float data_2fc600;
extern "C" const float rdata_152aa0, rdata_152aa4, rdata_152ac0, rdata_152ac4, rdata_152ac8;
typedef int v4i __attribute__((vector_size(16)));
typedef float v4f __attribute__((vector_size(16)));
extern "C" const v4i rdata_152ab0;
extern "C" const double rdata_152ad0, rdata_152ad8, rdata_152ae0;
extern "C" int find_hotbar_cell(int cell, float yaw, float *out) {
    Rot cur = rot_make((float)(cell % 9), (float)(cell / 9));
    float best2 = rdata_152aa0, ang2 = 0, prev2 = rdata_152aa0, prevAng2 = 0;
    int idx2 = -1, prevIdx2 = -1;
    float best0 = rdata_152aa0, ang0 = 0;
    int idx0 = -1;
    for (unsigned long long i = 0; i < 27; i++) {
        unsigned char t = data_2fc5e0[i];
        if (t != 2) {
            if (t != 0) continue;
            if (random_chance(rdata_152aa4)) continue;
        }
        unsigned char b = (unsigned char)i;
        Rot r = rot_make((float)(signed char)(b % 9), (float)(b / 9));
        float d = rot_delta_length(cur, r);
        float a = rot_distance(cur, r);
        float s = d * rdata_152ac0 + ((v4f)((v4i)(v4f){wrap_angle(a - yaw), 0, 0, 0} & rdata_152ab0))[0] * rdata_152aa4;
        if (t) {
            if (!(s > best2)) { prev2 = best2; prevIdx2 = idx2; prevAng2 = ang2; best2 = s; idx2 = (int)i; ang2 = a; }
        } else if (!(s > best0)) { best0 = s; idx0 = (int)i; ang0 = a; }
    }
    float rs = best2, ra = ang2;
    int ri = idx2;
    if (best2 > best0 && walk_should_change(&data_2fc620, rot_make_floats(rdata_152ac4, rdata_152ac8), rdata_152ad0)) {
        rs = best0; ra = ang0; ri = idx0;
    } else if (prevIdx2 != -1 && (double)best2 * rdata_152ad8 > (double)prev2 &&
               walk_should_change(&data_2fc630, rot_make_floats(rdata_152ac4, rdata_152ac8), rdata_152ae0)) {
        rs = prev2; ra = prevAng2; ri = prevIdx2;
    }
    *out = rs;
    data_2fc600 = ra;
    return ri;
}
