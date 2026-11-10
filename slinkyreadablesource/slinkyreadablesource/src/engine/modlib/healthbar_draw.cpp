// 0x2f000: health-bar style line drawing (immediate-mode GL), comparator, hurt-time hook
#include "slinky/engine.h"
typedef void (*GlColor3f)(float, float, float);
typedef void (*GlBegin)(int);
typedef void (*GlVertex2f)(float, float);
typedef void (*GlEnd)();
extern "C" GlColor3f *rdata_150ca0;
extern "C" GlBegin *rdata_150c98;
extern "C" GlVertex2f *rdata_150ce0;
extern "C" GlEnd *rdata_150ca8;
extern "C" float rdata_150a24, rdata_150a28;
FN(float, lerp_unclamped_float, (float a, float b, float t));   // lerp

struct Bar { char pad[0x44]; float f44, f48; };

extern "C" void draw_health_bar(Bar *b, float x, float y0, float y1) {
    float m = lerp_unclamped_float(y1, y0, b->f44);
    GlColor3f *color = rdata_150ca0;
    (*color)(0.0f, 0.0f, 0.0f);
    GlBegin *begin = rdata_150c98;
    (*begin)(1);
    GlVertex2f *vertex = rdata_150ce0;
    (*vertex)(x, y0);
    (*vertex)(x, m);
    GlEnd *end = rdata_150ca8;
    (*end)();
    if (b->f48 > 0.0f) {
        float m2 = lerp_unclamped_float(m, y1, b->f48);
        float c0 = rdata_150a24;

        (*color)(c0, rdata_150a28, 0.0f);
        (*begin)(1);
        (*vertex)(x, m);
        (*vertex)(x, m2);
        (*end)();
        m = m2;
    }
    GlColor3f cf = *color;
    float t = b->f44;
    float c = rdata_150a24;
    float r = lerp_unclamped_float(c, 0.0f, t);
    float g = lerp_unclamped_float(0.0f, c, b->f44);
    cf(r, g, 0.0f);
    (*begin)(1);
    (*vertex)(x, m);
    (*vertex)(x, y1);
    (*end)();
}

extern "C" int compare_healthbar_height(char *a, char *b) { return (int)(*(float *)(b + 0x3c) - *(float *)(a + 0x3c)); }

extern "C" int data_2f9d80, data_2f9d84;
extern "C" unsigned char data_2ea7e8;
extern "C" float data_2ea7c8, data_2ea790;
FN(unsigned char, java_call_entity_is_sprinting, (void *a, void *b));
FN(int, java_get_entity_living_base_last_attacker_time, (void *a, void *b));
struct Ev { char pad[8]; Ctx *ctx; unsigned char on; char pad2[7]; double d18; };
extern "C" void healthbar_render(Ev *ev) {
    int n = data_2f9d84;
    if (n > 0) data_2f9d84 = --n;
    Ctx *c = ev->ctx;
    if (data_2ea7e8 == 1) {
        if (!java_call_entity_is_sprinting(c->a, c->b)) return;
        n = data_2f9d84;
    }
    if (n == 0) {
        int t = java_get_entity_living_base_last_attacker_time(c->a, c->b);
        if (t == data_2f9d80) {
            n = data_2f9d84;
        } else {
            data_2f9d80 = t;
            data_2f9d84 = 9;
            n = 9;
        }

    }
    if (data_2ea7c8 > (float)n) {
        ev->on = 1;
        ev->d18 = data_2ea790;
    }
}
