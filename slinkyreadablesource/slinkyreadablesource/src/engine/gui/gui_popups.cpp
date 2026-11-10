// 0x16000
#include "slinky/engine.h"
FN(void, set_popup_open, (void *x, bool b));
extern "C" void close_popup(void *x) { set_popup_open(x, true); }
extern "C" bool popup_is_open(char *g) { return *(int *)(g + 0x3f8) != 0; }
extern "C" unsigned char popup_is_animating(char *g) { return (unsigned char)g[0x420]; }

struct Col3 { float r, g, b; };
struct Pick { void *ctx; char pad[0x18]; void (*add)(void *ctx, void *key, Col3 *c); };
extern "C" const float rdata_140df8, rdata_140dfc;
extern "C" void pick_add_highlight(Pick *p, void *, void *key) {
    if (!key) return;
    void *ctx = p->ctx;
    auto add = p->add;

    alignas(16) Col3 c = {rdata_140df8, 0, 0};
    add(ctx, key, &c);
}
extern "C" void pick_add_shadow(Pick *p, void *key) {
    if (!key) return;
    void *ctx = p->ctx;
    auto add = p->add;

    alignas(16) Col3 c = {rdata_140dfc, 0, 0};
    add(ctx, key, &c);
}
FN(unsigned long long, libc_strlen, (const char *s));
// numeric text filter: digits and , - .
extern "C" void numeric_text_filter(char *buf, char c) {
    unsigned long long n = libc_strlen(buf);
    if (n == 100) return;
    unsigned char d = (unsigned char)(c - 0x30);

    unsigned char nd = d >= 10;

    unsigned char e = (unsigned char)(c - 0x2c);

    unsigned char np = e >= 3;

    if (np & nd) return;
    buf[n] = c;
}
struct R16 { long long a, b; };
FN(unsigned char, clicked_inside_r_frame, (void *g, R16 r));
FN(void, start_dragging_item_b, (void *g, void *a, void *b));
FN(bool, dragged_item_is, (char *g, void *x));
extern "C" __attribute__((noinline)) bool drag_item(char *g, void *, R16 *r, void *a, void *b) {
    if (clicked_inside_r_frame(g, *r)) start_dragging_item_b(g, a, b);
    return dragged_item_is(g, b);
}

struct R16d { float x, y, w, h; };
struct V4b { float x, y, z, w; };
FN(float, draw_text, (void *w, R16d r, int a, int b, void *x));
FN(V4b, nvg_rgba_f, (float r, float g, float b, float a));
FN(void, draw_accent_line, (void *w, R16d r, V4b c, float a, float b));
extern "C" const float rdata_140e14, rdata_140e18, rdata_140e1c;
extern "C" __attribute__((noinline)) void draw_tracked_text(char *g, void *w, const R16d *rp) {
    R16d r = *rp;
    float f = draw_text(w, *rp, 1, 0, g + 0x378) + rdata_140e14;
    float k = rdata_140e18;
    V4b c = nvg_rgba_f(k, k, k, 1.0f);
    R16d n = {f, r.y, r.w, r.h};
    draw_accent_line(w, n, c, rdata_140e1c, 1.0f);
}

extern "C" const float rdata_140e20, rdata_140e24, rdata_140e28;
extern "C" __attribute__((noinline)) void draw_clipped_text(char *g, void *w, const R16d *rp, int a) {
    float f = draw_text(w, *rp, a, 0, g + 0x378);
    R16d r = *rp;
    f += rdata_140e20;
    float k = rdata_140e24;
    V4b c = nvg_rgba_f(k, k, k, 1.0f);
    R16d n = {f, r.y, r.w, r.h};
    draw_accent_line(w, n, c, rdata_140e28, 1.0f);
}
FN(void, set_popup_open, (void *x, bool b));
FN(double, time_now, ());
FN(void, start_dragging_item_b, (void *g, void *a, void *b));
// start a timed popup/drag payload
extern "C" void start_timed_popup_drag_payload(char *g, void *a, void *b, void *c, void *d) {
    set_popup_open(g, true);
    double now = time_now();
    *(long long *)(g + 0x3f8) = 1;
    *(void **)(g + 0x400) = b;
    *(void **)(g + 0x408) = a;
    *(double *)(g + 0x410) = now;
    __builtin_memset(g + 0x418, 0, 0x10);
    *(void **)(g + 0x428) = d;
    *(void **)(g + 0x430) = c;
    __builtin_memset(g + 0x438, 0, 0x60);
    start_dragging_item_b(g, c, g + 0x3f8);
}

#include <stdarg.h>
FN(int, libc_vsnprintf, (char *buf, unsigned long long n, const char *fmt, va_list ap));
FN(void, end_drag, (void *g));
extern "C" const char rdata_140f44[];   // "Are you sure?"
// confirmation popup with a formatted message
extern "C" void confirmation_popup_formatted_message(char *g, void *a, void *b, const char *fmt, ...) {
    set_popup_open(g, true);
    double now = time_now();
    *(long long *)(g + 0x3f8) = 3;
    *(void **)(g + 0x400) = a;
    *(const char **)(g + 0x408) = rdata_140f44;
    *(double *)(g + 0x410) = now;
    __builtin_memset(g + 0x418, 0, 0x10);
    *(void **)(g + 0x428) = b;
    __builtin_memset(g + 0x430, 0, 0x68);
    va_list ap;
    va_start(ap, fmt);
    libc_vsnprintf(g + 0x430, 100, fmt, ap);
}
extern "C" void open_list_popup(char *g, void *a, void *b, void *c, void *d0, void *d1, void *e) {
    set_popup_open(g, true);
    double now = time_now();
    *(long long *)(g + 0x3f8) = 4;
    *(void **)(g + 0x400) = b;
    *(void **)(g + 0x408) = a;
    *(double *)(g + 0x410) = now;
    __builtin_memset(g + 0x418, 0, 0x10);
    *(void **)(g + 0x428) = c;
    *(void **)(g + 0x430) = d0;
    *(void **)(g + 0x438) = d1;
    *(void **)(g + 0x440) = e;
    __builtin_memset(g + 0x448, 0, 0x50);
    start_dragging_item_b(g, c, g + 0x3f8);
    end_drag(g);
}

// ---- popups: 1 = callback, 2 = colour picker, 3 = confirm, 4 = list picker ----
struct Col3b { float r, g, b; };
extern "C" unsigned long long data_2f76b0;   // recent-colour ring position
extern "C" Col3b data_2f76c0[8];
struct ListV { unsigned long long n, cap; void *data; };
static inline __attribute__((always_inline)) bool same(const Col3b &a, const Col3b &b) {
    return a.r == b.r && a.g == b.g && a.b == b.b;
}
// close the open popup (apply = run its callback with the result)
FN(void, libc_free, (void *p));
extern "C" __attribute__((noinline)) void set_popup_open(void *gv, bool apply) {
    char *g = (char *)gv;
    *(int *)(g + 0x370) = 0;
    switch (*(unsigned *)(g + 0x3f8)) {
    case 0:
        return;
    case 1:
        if (apply) (*(void (**)(void *, void *))(g + 0x428))(*(void **)(g + 0x400), 0);
        break;
    case 2: {
        if (apply) {
            Col3b c = *(Col3b *)(g + 0x430);
            (*(void (**)(void *, Col3b *))(g + 0x428))(*(void **)(g + 0x400), &c);
        }
        Col3b c = *(Col3b *)(g + 0x430);
        unsigned long long p = data_2f76b0;
        for (unsigned j = 0; j < 8; j++)
            if (same(c, data_2f76c0[(p + 8 - j) & 7])) goto out;
        p = p ? p - 1 : 7;
        data_2f76b0 = p;
        data_2f76c0[p] = c;
        break;
    }
    case 3:
        break;
    case 4: {
        if (apply) (*(void (**)(void *, void *, void *))(g + 0x430))(*(void **)(g + 0x400), 0, 0);
        ListV *l = *(ListV **)(g + 0x450);
        if (l) {
            l->n = 0;
            l->cap = 0;
            libc_free(l->data);
            l->data = 0;
            libc_free(*(void **)(g + 0x450));
            *(void **)(g + 0x450) = 0;
        }
        break;
    }
    }
out:
    *(long long *)(g + 0x3f8) = 0;
}
extern "C" const float rdata_140e60, rdata_140e64, rdata_140e68, rdata_140e6c;   // 0.5, 2, 4, 6
// open the colour picker for colour c (keeps the RGB and its HSL)
extern "C" void open_colour_picker_colour_c(char *g, void *a, void *b, const Col3b *c, void *cb) {
    set_popup_open(g, true);
    double now = time_now();
    Col3b in = *c;
    float r = c->r, gg = c->g, bb = c->b;
    float mx = __builtin_fmaxf(r, __builtin_fmaxf(gg, bb));
    float mn = __builtin_fminf(r, __builtin_fminf(gg, bb));
    float sum = mx + mn;
    float l = rdata_140e60 * sum;
    float h = 0.0f, s = 0.0f;
    if (mx != mn) {
        float d = mx - mn;
        float denom = rdata_140e60 < l ? rdata_140e64 - mx - mn : sum;
        if (mx == r) h = (gg - bb) / d + (gg < bb ? rdata_140e6c : 0.0f);
        else if (mx == gg) h = (bb - r) / d + rdata_140e64;
        else if (mx == bb) h = (r - gg) / d + rdata_140e68;
        else h = l;
        s = d / denom;
        h = h / rdata_140e6c;
    }
    *(long long *)(g + 0x3f8) = 2;
    *(void **)(g + 0x400) = b;
    *(void **)(g + 0x408) = a;
    *(double *)(g + 0x410) = now;
    __builtin_memset(g + 0x418, 0, 0x10);
    *(void **)(g + 0x428) = cb;
    *(Col3b *)(g + 0x430) = in;
    *(float *)(g + 0x43c) = h;
    *(float *)(g + 0x440) = s;
    *(float *)(g + 0x444) = l;
    __builtin_memset(g + 0x448, 0, 0x50);
}

// ---- draw the open popup: title bar with close cross, then by kind (g+0x3f8): 1 text prompt, 2 colour
// picker (hex field, HSL sliders, recent colours), 3 confirm, 4 searchable list. Closes it when done. ----
struct GW16 { void *ig; double t; };
struct Grad { float v[19]; };
struct Hit { unsigned long long idx; const char *name; };
FN(void, pop_layout_row, (char *g, int n));
FN(R16d, add_cell, (char *g, float h));
FN(void, nvg_FillColor, (void *ig, V4b *c));
FN(void, nvg_StrokeColor, (void *ig, V4b *c));
FN(void, nvg_StrokeWidth, (void *ig, float w));
FN(void, nvg_FontSize, (void *ig, float size));
FN(void, draw_rect_rounded, (GW16 *w, R16d r, float round));
FN(void, draw_rect_stroked, (GW16 *w, R16d r, float round));
FN(void, begin_content, (GW16 *w, char *g, R16d r, float pad, float f));
FN(bool, mouse_over_rect_r, (char *g, R16d r));
FN(unsigned char, point_in_rect_local, (void *g, R16d r)) __asm__("clicked_inside_r_frame");
FN(void, nvg_BeginPath, (void *ig));
FN(void, nvg_MoveTo, (void *ig, float x, float y));
FN(void, nvg_LineTo, (void *ig, float x, float y));
FN(void, nvg_Stroke, (void *ig));
FN(void, nvg_Fill, (void *ig));
FN(R16d, inset_rect, (R16d r, float d));
FN(void *, take_dragged_item, (char *g));
FN(char *, dragged_payload, (char *g));
FN(R16d, make_rect, (float x, float y, float w, float h));
FN(V4b, nvg_rgb_f, (float r, float g, float b));
FN(V4b, nvg_hsl, (float h, float s, float l));
FN(V4b, nvg_lerp_rgba, (V4b a, V4b b, float t));
FN(Grad, nvg_linear_gradient, (void *ig, float x0, float y0, float x1, float y1, V4b c0, V4b c1));
FN(void, nvg_FillPaint, (void *ig, Grad g));
FN(void, nvg_RoundedRectVarying, (void *ig, float x, float y, float w, float h, float r0, float r1, float r2, float r3));
FN(void, nvg_Rect, (void *ig, float x, float y, float w, float h));
FN(void, nvg_RoundedRect, (void *ig, float x, float y, float w, float h, float r));
FN(float, libc_ceilf, (float x));
FN(float, libc_floorf, (float x));
FN(float, draw_label_va, (char *g, GW16 *w, const char *label));
FN(bool, is_packet_cancelled, (char *g, GW16 *w, R16d r, unsigned idx, V4b *col));
FN(void, hex_colour_text_filter, (char *buf, char c));
FN(float, end_layout_row, (GW16 *w, char *g));
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));
FN(unsigned char, vector_grow, (void *vec, unsigned long long elem, unsigned long long n, void *cap));
FN(int, libc_snprintf, (char *buf, unsigned long long n, const char *fmt, ...));
extern "C" const float rdata_140e90, rdata_140e94, rdata_140e98, rdata_140e9c, rdata_140ea0, rdata_140ea4, rdata_140ea8,
    rdata_140eac, rdata_140eb0, rdata_140eb4, rdata_140ec0, rdata_140ec4, rdata_140ec8, rdata_140ecc, rdata_140ed8,
    rdata_140edc, rdata_140ee0, rdata_140ee4, rdata_140ee8, rdata_140eec, rdata_140ef0, rdata_140f00, rdata_140f04,
    rdata_140f08, rdata_140f0c;
extern "C" const double rdata_140e88, rdata_140eb8, rdata_140ed0, rdata_140ef8;
extern "C" const char rdata_140f52[], rdata_140f56[], rdata_140f61[], rdata_140f6b[], rdata_140f79[], rdata_140f7b[],
    rdata_140f88[], rdata_140f91[];
extern "C" char data_2f7720[];
static inline __attribute__((always_inline)) unsigned char lower16(unsigned char c) {
    return (unsigned char)(c - 'A') < 26 ? (unsigned char)(c | 0x20) : c;
}
static inline __attribute__((always_inline)) unsigned char hexval(signed char c) {
    return (unsigned char)(c + (c >= 'a' ? 0xa9 : 0xd0));
}
// RGB at g+0x430 -> HSL at g+0x43c
static inline __attribute__((always_inline)) void rgb_to_hsl(char *g) {
    float r = *(float *)(g + 0x430), gg = *(float *)(g + 0x434), bb = *(float *)(g + 0x438);
    float mx = __builtin_fmaxf(__builtin_fmaxf(bb, gg), r);
    float mn = __builtin_fminf(__builtin_fminf(bb, gg), r);
    float sum = mx + mn;
    float l = rdata_140edc * sum;
    float h = 0.0f, s = 0.0f;
    if (mx != mn) {
        float d = mx - mn;
        float denom = rdata_140edc < l ? rdata_140eb4 - mx - mn : sum;
        if (mx == r) h = (gg - bb) / d + (gg < bb ? rdata_140f00 : 0.0f);
        else if (mx == gg) h = (bb - r) / d + rdata_140eb4;
        else if (mx == bb) h = (r - gg) / d + rdata_140ecc;
        else h = l;
        s = d / denom;
        h = h / rdata_140f00;
    }
    *(float *)(g + 0x43c) = h;
    *(float *)(g + 0x440) = s;
    *(float *)(g + 0x444) = l;
}
// one HSL slider: gradient from c(t=0) to c(t=1) with the knob at the current value
static inline __attribute__((always_inline)) bool hsl_bar(char *g, GW16 *w, unsigned idx, V4b c0, V4b c1, float t) {
    V4b mid = nvg_lerp_rgba(c0, c1, t);
    void *ig = w->ig;
    R16d r = add_cell(g, rdata_140ec4);
    nvg_FillPaint(ig, nvg_linear_gradient(ig, r.x, r.y, r.w + r.x, r.h + r.y, c0, c1));
    draw_rect_rounded(w, r, rdata_140ee4);
    return is_packet_cancelled(g, w, r, idx, &mid);
}
extern "C" void close_popup_animated(char *g, GW16 *w) {
    g[0x420] = 1;
    float op = (float)((double)*(float *)(g + 0x418) + (w->t - *(double *)(g + 0x410)) / rdata_140e88);
    *(float *)(g + 0x418) = rdata_140e90 < op ? rdata_140e90 : op;
    pop_layout_row(g, 1);
    R16d box = add_cell(g, *(float *)(g + 0x41c));
    void *ig = w->ig;
    V4b bg = nvg_rgba_f(rdata_140e94, rdata_140e94, rdata_140e94, 1.0f);
    nvg_FillColor(ig, &bg);
    draw_rect_rounded(w, box, rdata_140e98);
    begin_content(w, g, box, rdata_140e9c, 12.0f);
    pop_layout_row(g, 1);
    void *tig = w->ig;
    R16d t = add_cell(g, rdata_140e98);
    nvg_FontSize(tig, rdata_140ea0);
    V4b tc = nvg_rgba_f(rdata_140ea4, rdata_140ea4, rdata_140ea4, 1.0f);
    nvg_FillColor(tig, &tc);
    draw_text(w, t, 0, 0, *(void **)(g + 0x408));
    // close cross in the title bar's right square
    float cx = t.w - t.h + t.x;
    R16d xr = {cx, t.y, t.h, t.h};
    V4b xc = mouse_over_rect_r(g, xr) ? nvg_rgba_f(rdata_140eac, rdata_140eb0, rdata_140eb0, 1.0f)
                             : nvg_rgba_f(rdata_140ea8, rdata_140ea8, rdata_140ea8, 1.0f);
    nvg_StrokeColor(tig, &xc);
    nvg_StrokeWidth(tig, rdata_140eb4);
    float in = (float)((double)t.h * rdata_140eb8);
    nvg_BeginPath(tig);
    float x0 = cx + in, y0 = t.y + in;
    nvg_MoveTo(tig, x0, y0);
    float x1 = t.h + cx - in, y1 = t.y + t.h - in;
    nvg_LineTo(tig, x1, y1);
    nvg_MoveTo(tig, x1, y0);
    nvg_LineTo(tig, x0, y1);
    nvg_Stroke(tig);
    bool closed = point_in_rect_local(g, R16d{cx, t.y, t.h, t.h});
    int res = 1;   // 0 keep open, 1 close (cancel), 2 close (done)
    void *key = g + 0x3f8;
    switch (*(int *)(g + 0x3f8)) {
    case 1: {   // text prompt -> cb(ctx, text)
        void *fig = w->ig;
        R16d f = add_cell(g, rdata_140ec0);
        nvg_FontSize(fig, rdata_140ec4);
        V4b oc = nvg_rgba_f(rdata_140ec8, rdata_140ec8, rdata_140ec8, 1.0f);
        nvg_StrokeColor(fig, &oc);
        nvg_StrokeWidth(fig, rdata_140eb4);
        draw_rect_stroked(w, f, rdata_140e9c);
        res = 1;
        if (!dragged_item_is(g, key)) break;
        f = inset_rect(f, rdata_140ecc);
        draw_tracked_text(g, w, &f);
        void *s = take_dragged_item(g);
        if (!s) {
            res = 0;
            break;
        }
        (*(void (**)(void *, void *))(g + 0x428))(*(void **)(g + 0x400), s);
        res = 2;
        break;
    }
    case 2: {   // colour picker
        void *pig = w->ig;
        R16d f = add_cell(g, rdata_140ee8);
        bool hov = mouse_over_rect_r(g, f);
        float fh = f.h;
        V4b sw = nvg_rgb_f(*(float *)(g + 0x430), *(float *)(g + 0x434), *(float *)(g + 0x438));
        nvg_FillColor(pig, &sw);
        draw_rect_rounded(w, R16d{f.x, f.y, fh, fh}, rdata_140ee4);
        V4b oc = nvg_rgba_f(rdata_140ec8, rdata_140ec8, rdata_140ec8, 1.0f);
        nvg_StrokeColor(pig, &oc);
        nvg_StrokeWidth(pig, rdata_140eb4);
        nvg_Stroke(pig);
        nvg_FontSize(pig, rdata_140e98);
        V4b hc = nvg_rgba_f(rdata_140ea8, rdata_140ea8, rdata_140ea8, 1.0f);
        nvg_FillColor(pig, &hc);
        float hx = f.x + fh + rdata_140e9c;
        float hw = f.w - (f.x - hx);
        float ex = draw_text(w, R16d{hx, f.y, hw, f.h}, 0, 0, (void *)rdata_140f79 /* "#" */);
        hx -= ex;
        hw -= hx;
        R16d hr = {ex, f.y, hw, f.h};
        R16 ed;
        __builtin_memcpy(&ed, &f, sizeof ed);
        if (drag_item(g, w, &ed, (void *)hex_colour_text_filter, g + 0x428)) {
            V4b wc = nvg_rgba_f(rdata_140e90, rdata_140e90, rdata_140e90, 1.0f);
            nvg_FillColor(pig, &wc);
            draw_clipped_text(g, w, &hr, 0);
            const char *s = (const char *)take_dragged_item(g);
            if (s) {
                if (libc_strlen(s) > 5) {
                    *(float *)(g + 0x430) =
                        (float)((double)(hexval(s[0]) << 4 | hexval(s[1])) / rdata_140ef8);
                    *(float *)(g + 0x434) =
                        (float)((double)(hexval(s[2]) << 4 | hexval(s[3])) / rdata_140ef8);
                    *(float *)(g + 0x438) =
                        (float)((double)(hexval(s[4]) << 4 | hexval(s[5])) / rdata_140ef8);
                }
                rgb_to_hsl(g);
            }
        } else {
            float k = rdata_140eec;
            libc_snprintf(data_2f7720, 7, rdata_140f7b /* "%02x%02x%02x" */,
                     (unsigned char)(long long)(*(float *)(g + 0x430) * k),
                     (unsigned char)(long long)(*(float *)(g + 0x434) * k),
                     (unsigned char)(long long)(k * *(float *)(g + 0x438)));
            float c = hov ? rdata_140ef0 : rdata_140ea8;
            V4b tc2 = nvg_rgba_f(c, c, c, 1.0f);
            nvg_FillColor(pig, &tc2);
            draw_text(w, hr, 0, 0, data_2f7720);
        }
        // hue: six gradient segments, rounded at the ends
        draw_label_va(g, w, rdata_140f52 /* "Hue" */);
        void *hig = w->ig;
        R16d hb = add_cell(g, rdata_140ec4);
        float seg = hb.w / rdata_140f00;
        float sw2 = libc_ceilf(seg);
        for (unsigned long long k = 0; k != 6; k++) {
            float sx = libc_floorf((float)k * seg + hb.x);
            V4b c0 = nvg_hsl((float)k / rdata_140f00, *(float *)(g + 0x440), *(float *)(g + 0x444));
            V4b c1 = nvg_hsl((float)(k + 1) / rdata_140f00, *(float *)(g + 0x440), *(float *)(g + 0x444));
            Grad gr = nvg_linear_gradient(hig, sx, hb.y, seg + sx, hb.y, c0, c1);
            nvg_BeginPath(hig);
            if (k == 5) nvg_RoundedRectVarying(hig, sx, hb.y, sw2, hb.h, 0.0f, 5.0f, 5.0f, 0.0f);
            else if (k == 0) nvg_RoundedRectVarying(hig, sx, hb.y, sw2, hb.h, 5.0f, 0.0f, 0.0f, 5.0f);
            else nvg_Rect(hig, sx, hb.y, sw2, hb.h);
            nvg_FillPaint(hig, gr);
            nvg_Fill(hig);
        }
        V4b cur = nvg_hsl(*(float *)(g + 0x43c), *(float *)(g + 0x440), *(float *)(g + 0x444));
        bool ch = is_packet_cancelled(g, w, hb, 0, &cur);
        draw_label_va(g, w, rdata_140f56 /* "Saturation" */);
        bool cs = hsl_bar(g, w, 1, nvg_hsl(*(float *)(g + 0x43c), 0.0f, *(float *)(g + 0x444)),
                          nvg_hsl(*(float *)(g + 0x43c), rdata_140e90, *(float *)(g + 0x444)), *(float *)(g + 0x440));
        draw_label_va(g, w, rdata_140f61 /* "Lightness" */);
        bool cl = hsl_bar(g, w, 2, nvg_hsl(*(float *)(g + 0x43c), *(float *)(g + 0x440), 0.0f),
                          nvg_hsl(*(float *)(g + 0x43c), *(float *)(g + 0x440), rdata_140e90), *(float *)(g + 0x444));
        if (ch || cs || cl) {
            V4b c = nvg_hsl(*(float *)(g + 0x43c), *(float *)(g + 0x440), *(float *)(g + 0x444));
            __builtin_memcpy(g + 0x430, &c, 8);
            *(float *)(g + 0x438) = c.z;
        }
        // recently used colours (ring buffer, newest first)
        draw_label_va(g, w, rdata_140f6b /* "Recently used" */);
        void *rig = w->ig;
        R16d rr = add_cell(g, rdata_140ed8);
        float x = rr.x;
        float sz = (rr.w + rdata_140f04) * rdata_140f08;
        nvg_StrokeWidth(rig, rdata_140eb4);
        float step = rdata_140e9c + sz;
        unsigned long long pick = 8;
        for (long long i = -8; i; i++) {
            unsigned long long p = data_2f76b0 + i + 8;
            unsigned long long idx = p < 8 ? p : data_2f76b0 + i;
            R16d cell = make_rect(x, rr.y, sz, rr.h);
            float k = mouse_over_rect_r(g, cell) ? rdata_140f0c : rdata_140ec8;
            V4b oc2 = nvg_rgba_f(k, k, k, 1.0f);
            nvg_StrokeColor(rig, &oc2);
            Col3b *e = &data_2f76c0[idx];
            if (point_in_rect_local(g, cell)) pick = idx;
            nvg_BeginPath(rig);
            nvg_RoundedRect(rig, cell.x, cell.y, cell.w, cell.h, 5.0f);
            V4b fc = nvg_rgb_f(e->r, e->g, e->b);
            nvg_FillColor(rig, &fc);
            nvg_Fill(rig);
            nvg_Stroke(rig);
            x += step;
        }
        if (pick != 8) {
            *(Col3b *)(g + 0x430) = data_2f76c0[pick];
            rgb_to_hsl(g);
        }
        res = 0;
        break;
    }
    case 3: {   // confirm -> cb(ctx)
        void *cig = w->ig;
        R16d f = add_cell(g, rdata_140ec0);
        nvg_FontSize(cig, rdata_140ec4);
        V4b mc = nvg_rgba_f(rdata_140ea4, rdata_140ea4, rdata_140ea4, 1.0f);
        nvg_FillColor(cig, &mc);
        draw_text(w, f, 0, 0, g + 0x430);
        R16d b = add_cell(g, rdata_140ec0);
        V4b bc = mouse_over_rect_r(g, b) ? nvg_rgba_f(rdata_140eac, rdata_140eb0, rdata_140eb0, 1.0f)
                                : nvg_rgba_f(rdata_140ea4, rdata_140ee0, rdata_140ee0, 1.0f);
        nvg_FillColor(cig, &bc);
        draw_rect_rounded(w, b, rdata_140ee4);
        V4b wc = nvg_rgba_f(rdata_140e90, rdata_140e90, rdata_140e90, 1.0f);
        nvg_FillColor(cig, &wc);
        draw_text(w, b, 1, 0, (void *)rdata_140f88 /* "Continue" */);
        res = 0;
        if (!point_in_rect_local(g, b)) break;
        (*(void (**)(void *))(g + 0x428))(*(void **)(g + 0x400));
        res = 2;
        break;
    }
    case 4: {   // searchable list -> cb(ctx, index, name)
        R16d f = add_cell(g, rdata_140ec0);
        void *lig = w->ig;
        nvg_FontSize(lig, rdata_140ec4);
        V4b oc = nvg_rgba_f(rdata_140ec8, rdata_140ec8, rdata_140ec8, 1.0f);
        nvg_StrokeColor(lig, &oc);
        nvg_StrokeWidth(lig, rdata_140eb4);
        draw_rect_stroked(w, f, rdata_140e9c);
        if (!dragged_item_is(g, key)) break;
        f = inset_rect(f, rdata_140ecc);
        draw_tracked_text(g, w, &f);
        const char *q = dragged_payload(g);
        double now = w->t;
        if (!(rdata_140ed0 > now - *(double *)(g + 0x448))) {   // rebuild the matches at most every 0.25 s
            ListV *l = *(ListV **)(g + 0x450);
            if (!l) {
                l = (ListV *)libc_calloc(1, 0x18);
                *(ListV **)(g + 0x450) = l;
                if (!l) goto shown;
            }
            l->n = 0;
            l->cap = 0;
            libc_free(l->data);
            l->data = 0;
            *(unsigned long long *)(g + 0x458) = 0;
            for (unsigned long long k = 0; k < *(unsigned long long *)(g + 0x440); k++) {
                const char *it = (*(const char ***)(g + 0x438))[k];
                unsigned long long il = libc_strlen(it), ql = libc_strlen(q);
                if (il < ql) continue;
                if (ql) {   // case-insensitive substring
                    for (unsigned long long st = 0;; st++) {
                        unsigned long long p = 0;
                        while (lower16(it[st + p]) == lower16(q[p]))
                            if (++p == ql) goto match;
                        if (st + 1 > il - ql) goto next;
                    }
                }
            match : {
                ListV *m = *(ListV **)(g + 0x450);
                if (m->n >= 0x1e) {
                    (*(unsigned long long *)(g + 0x458))++;
                    continue;
                }
                if (!vector_grow(&m->data, 0x10, m->n, &m->cap)) continue;
                Hit *h = &((Hit *)m->data)[m->n];
                h->idx = k;
                h->name = it;
                m->n++;
            }
            next:;
            }
            *(double *)(g + 0x448) = now;
        }
    shown : {
        unsigned long long cnt = (*(ListV **)(g + 0x450))->n;
        if (!cnt) {
            res = 0;
            break;
        }
        void *tig2 = w->ig;
        nvg_FontSize(tig2, rdata_140ea0);
        const char *chosen = 0;
        unsigned long long cidx = 0;
        R16d row = {};
        for (unsigned long long i = 0; i != cnt; i++) {   // two columns
            float cx2, cw;
            if (i & 1) {
                cw = row.w * rdata_140edc;
                cx2 = row.x + cw;
            } else {
                row = add_cell(g, rdata_140ed8);
                cw = row.w * rdata_140edc;
                cx2 = row.x;
            }
            R16d cell = {cx2, row.y, cw, row.h};
            float k = mouse_over_rect_r(g, cell) ? rdata_140e90 : rdata_140ea4;
            V4b ic = nvg_rgba_f(k, k, k, 1.0f);
            nvg_FillColor(tig2, &ic);
            Hit *h = &((Hit *)(*(ListV **)(g + 0x450))->data)[i];
            unsigned long long hi = h->idx;
            const char *hn = h->name;
            draw_text(w, cell, 0, 0, (void *)hn);
            if (point_in_rect_local(g, cell)) {
                chosen = hn;
                cidx = hi;
            }
        }
        unsigned long long more = *(unsigned long long *)(g + 0x458);
        if (more) {
            char buf[0xe];
            libc_snprintf(buf, 0xe, rdata_140f91 /* "+%d more..." */, more < 0x270f ? more : 0x270f);
            R16d mr = add_cell(g, rdata_140ed8);
            V4b mc = nvg_rgba_f(rdata_140ea8, rdata_140ea8, rdata_140ea8, 1.0f);
            nvg_FillColor(tig2, &mc);
            nvg_FontSize(tig2, rdata_140ec4);
            draw_text(w, mr, 0, 0, buf);
        }
        res = 0;
        if (!chosen) break;
        (*(void (**)(void *, unsigned long long, const char *))(g + 0x430))(*(void **)(g + 0x400), cidx, chosen);
        res = 2;
        break;
    }
    }
    }
    *(float *)(g + 0x41c) = end_layout_row(w, g);
    g[0x420] = 0;
    *(double *)(g + 0x410) = w->t;
    if (res) set_popup_open(g, res == 1);
    else if (closed) set_popup_open(g, true);
}

// ---- selectable label: rounded box (highlighted when `hot`), text centred; returns whether it was clicked ----
extern "C" const float rdata_140e00, rdata_140e04, rdata_140e08, rdata_140e0c, rdata_140e10;
extern "C" bool draw_icon_button(char *g, GW16 *w, const char *text, bool selectable, const R16d *rect) {
    void *ig = w->ig;
    bool clicked = selectable ? point_in_rect_local(g, *rect) : false;
    V4b bg = nvg_rgba_f(rdata_140e00, rdata_140e00, rdata_140e00, 1.0f);
    nvg_FillColor(ig, &bg);
    draw_rect_rounded(w, *rect, rdata_140e04);
    float k = selectable ? rdata_140e0c : rdata_140e08;
    V4b fg = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &fg);
    nvg_FontSize(ig, rdata_140e10);
    draw_text(w, *rect, 1, 0, (void *)text);
    return clicked;
}
