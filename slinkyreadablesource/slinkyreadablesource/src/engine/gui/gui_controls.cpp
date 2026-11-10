// GUI: controls - sliders, ranges, numeric fields, key pickers.
#include "slinky/engine.h"

FN(void *, libc_memcpy, (void *d, const void *s, unsigned long long n));
extern "C" void set_slider_text(char *o, void *, const char *src, int a, int b) {
    o[0x268] = 1;
    libc_memcpy(o + 0x26a, src, 100);
    float *f = (float *)(o + 0x2d0);
    f[0] = (float)a;
    f[1] = (float)b;
}

// ---- slider: drag along the track or type a value (after a double click), rounded to step, clamped ----
struct R16g { float x, y, w, h; };
FN(unsigned char, clicked_inside_r_frame, (char *g, R16g r));
FN(void, start_dragging_item_b, (char *g, void *a, void *b));
FN(bool, dragged_item_is, (char *g, void *x));
FN(const char *, take_dragged_item, (char *g));
FN(void, numeric_text_filter, (char *buf, char c));
FN(float, libc_roundf, (float x));   // roundf
extern "C" const float rdata_140cd8;
extern "C" const double rdata_140ce0;
extern "C" unsigned char draw_slider_track(char *g, const R16g *track, const R16g *hit, float lo, float hi, float step,
                                  float scale, unsigned char *active, float *val) {
    if (clicked_inside_r_frame(g, *hit)) {
        *active = 1;
        if (g[0x4ca] == 1) start_dragging_item_b(g, (void *)numeric_text_filter, val);
    } else if (g[0x4c8] == 0 && !dragged_item_is(g, val)) {
        *active = 0;
    }
    unsigned char a = *active;
    if (a != 1) return a;
    float v;
    if (dragged_item_is(g, val)) {
        const char *s = take_dragged_item(g);
        if (!s) return a;
        int neg = *s == '-';
        const char *p = s + neg;
        int sign = -neg | 1;
        int ip = 0;
        char c = *p;
        while ((unsigned)(c - '0') <= 9) {
            ip = ip * 10 + (c - '0');
            c = *++p;
        }
        if ((unsigned char)(c | 2) == '.') {
            float f = 0.0f;
            float m = rdata_140cd8;
            ++p;
            for (int d; (unsigned)(d = *p - '0') < 10; p++) {
                f += (float)d * m;
                m = (float)((double)m * rdata_140ce0);
            }
            v = ((float)ip + f) * (float)sign;
        } else {
            v = (float)(ip * sign);
        }
        if (scale != 0.0f) v /= scale;
    } else {
        float t = (*(float *)(g + 0x4a8) - track->x) / track->w;
        v = (hi - lo) * t + lo;
    }
    if (step != 0.0f) v = libc_roundf(v / step) * step;
    *val = v;
    if (lo >= v) *val = lo;
    else if (!(v < hi)) *val = hi;
    return a;
}

// ---- range slider: two knobs (each grabs on its side of the midpoint), they push each other ----
struct V4g { float x, y, z, w; };
struct GW14 { void *ig; double t; };
struct RAnim { unsigned char a0, a1; char pad[2]; float p0, p1; int pad2; double last; };
FN(R16g, add_cell, (char *g, float h));
FN(V4g, nvg_rgba_f, (float r, float g, float b, float a));
FN(void, nvg_FillColor, (void *ig, V4g *c));
FN(void, nvg_FontSize, (void *ig, float size));
FN(float, draw_text, (GW14 *w, R16g r, int align, int col, const char *text));
FN(R16g, make_rect, (float x, float y, float w, float h));
FN(bool, mouse_over_rect_r, (char *g, R16g r));
FN(void, draw_rect_rounded, (GW14 *w, R16g r, float round));
FN(float, limit_rotation_delta, (float lo, float hi, float x));
FN(float, lerp_clamped_float, (float a, float b, float t));
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(void, nvg_BeginPath, (void *ig));
FN(void, nvg_Circle, (void *ig, float x, float y, float rad));
FN(void, nvg_Fill, (void *ig));
FN(unsigned char, draw_slider_track_local, (char *g, R16g track, R16g hit, float lo, float hi, float step, float scale,
                           unsigned char *active, float *val)) __asm__("draw_slider_track");
FN(int, libc_snprintf, (char *buf, unsigned long long n, const char *fmt, ...));
extern "C" const float rdata_140ce8, rdata_140cec, rdata_140cf0, rdata_140cf4, rdata_140d00, rdata_140d10,
    rdata_140d14, rdata_140d18, rdata_140d1c, rdata_140d28, rdata_140d2c, rdata_140d30;
extern "C" const double rdata_140cf8, rdata_140d08, rdata_140d20;
extern "C" const char rdata_140e53[], rdata_140e2c[];   // "%0.2f", "%0.2f - %0.2f"
extern "C" char data_2f7670[], data_2f7630[];
static inline __attribute__((always_inline)) const char *val_text(char *g, float *v, const char *fmt, float scale) {
    if (dragged_item_is(g, v)) return g + 0x378;
    float x = *v;
    float s = scale != 0.0f ? scale * x : x;
    libc_snprintf(data_2f7670, 0x40, fmt ? fmt : rdata_140e53, (double)s);
    return data_2f7670;
}
extern "C" bool draw_slider_with_label(char *g, GW14 *w, const char *label, const char *fmt, const char *rfmt, RAnim *an, float lo,
                         float hi, float step, float scale, float *vlo, float *vhi) {
    void *ig = w->ig;
    R16g r = add_cell(g, rdata_140ce8);
    V4g tc = nvg_rgba_f(rdata_140cec, rdata_140cec, rdata_140cec, 1.0f);
    nvg_FillColor(ig, &tc);
    nvg_FontSize(ig, rdata_140cf0);
    draw_text(w, r, 0, 0, label);
    float y = r.y + rdata_140cf4 + rdata_140d00;
    double half = (double)r.w * rdata_140cf8;
    float x0 = (float)((double)r.x + half);
    float tw = (float)(half + rdata_140d08);
    R16g track = make_rect(x0, y, tw, 4.0f);
    float k = mouse_over_rect_r(g, r) ? rdata_140d14 : rdata_140d10;
    V4g kc = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &kc);
    float rnd = rdata_140d18;
    draw_rect_rounded(w, track, rnd);
    float range = hi - lo;
    float t0 = limit_rotation_delta(0.0f, rdata_140d1c, (*vlo - lo) / range);
    float t1 = limit_rotation_delta(0.0f, rdata_140d1c, (*vhi - lo) / range);
    float dt = (float)((double)(float)(w->t - an->last) / rdata_140d20);
    an->p0 = lerp_clamped_float(an->p0, t0, dt);
    an->p1 = lerp_clamped_float(an->p1, t1, dt);
    an->last = w->t;
    float x1 = x0 + tw;
    float a = lerp_unclamped_float(x0, x1, an->p0);
    float b = lerp_unclamped_float(x0, x1, an->p1);
    R16g fill = make_rect(a, y, b - a, 4.0f);
    V4g acc = *(V4g *)(g + 0x360);
    nvg_FillColor(ig, &acc);
    draw_rect_rounded(w, fill, rnd);
    float ky = rnd + y, kr = rdata_140d28;
    nvg_BeginPath(ig);
    nvg_Circle(ig, a, ky, kr);
    nvg_Fill(ig);
    nvg_BeginPath(ig);
    nvg_Circle(ig, b, ky, kr);
    nvg_Fill(ig);
    float mid = (float)(int)((double)(a + b) * rdata_140cf8);
    float d = mid - x0;
    float pad = rdata_140d30;
    R16g hl = make_rect(rdata_140d2c + x0, r.y, d + pad, r.h);
    R16g hh = make_rect(mid, r.y, tw - d + pad, r.h);
    const char *text;
    int tx;
    bool changed;
    if (draw_slider_track_local(g, track, hl, lo, hi, step, scale, &an->a0, vlo)) {
        if (*vlo > *vhi) *vhi = *vlo;
        text = val_text(g, vlo, fmt, scale);
        tx = (int)a;
        changed = true;
    } else if (draw_slider_track_local(g, track, hh, lo, hi, step, scale, &an->a1, vhi)) {
        if (*vlo > *vhi) *vlo = *vhi;
        text = val_text(g, vhi, fmt, scale);
        tx = (int)b;
        changed = true;
    } else {
        if (!mouse_over_rect_r(g, r)) return false;
        if (g[0x4c8] == 1 && !an->a0 && an->a1 != 1) return false;
        float l = *vlo, h = *vhi;
        float ls = scale == 0.0f ? l : scale * l;
        float hs = scale == 0.0f ? h : scale * h;
        libc_snprintf(data_2f7630, 0x40, rfmt ? rfmt : rdata_140e2c, (double)ls, (double)hs);
        text = data_2f7630;
        tx = (int)((double)(hl.x + hl.w + hh.x) * rdata_140cf8);
        changed = false;
    }
    g[0x268] = 1;
    libc_memcpy(g + 0x26a, text, 0x64);
    *(float *)(g + 0x2d0) = (float)tx;
    *(float *)(g + 0x2d4) = (float)(int)y;
    return changed;
}

// ---- dropdown: expands in place (< 6 items) or opens the "Select item" search popup; optional dropdowns
// (index 0 = none) get a clear cross ----
struct DDrop { char pad[8]; double last; float h; unsigned char open; };
FN(void, nvg_StrokeColor, (void *ig, V4g *c));
FN(void, nvg_StrokeWidth, (void *ig, float w));
FN(void, draw_rect_stroked, (GW14 *w, R16g r, float round));
FN(V4g, nvg_trans_rgba_f, (V4g c, float k));
FN(void, nvg_MoveTo, (void *ig, float x, float y));
FN(void, nvg_LineTo, (void *ig, float x, float y));
FN(void, nvg_Stroke, (void *ig));
FN(void, open_list_popup, (char *g, const char *title, void *sel, void *filter, void *pick, void *items, unsigned long long n));
FN(void, text_filter_alnum, (char *buf, char c));
FN(void, select_index, (int *out, int i, void *sel));
extern "C" const float rdata_140d38, rdata_140d48, rdata_140d4c, rdata_140d58, rdata_140d5c, rdata_140d60,
    rdata_140d64, rdata_140d68, rdata_140d6c, rdata_140d78, rdata_140d7c, rdata_140d80;
extern "C" const double rdata_140d40, rdata_140d50, rdata_140d70;
extern "C" const char rdata_140e3a[];   // "Select item"
static inline __attribute__((always_inline)) V4g grey14(float k) { return nvg_rgba_f(k, k, k, 1.0f); }
extern "C" unsigned char draw_keybind_control(char *g, GW14 *w, const char *label, DDrop *dd, unsigned n, const char **items,
                                  int *sel, bool opt) {
    void *ig = w->ig;
    float dt = (float)(w->t - dd->last);
    float target = dd->open ? (float)(28 * n) : rdata_140d38;
    float h = lerp_clamped_float(dd->h, target, (float)((double)dt / rdata_140d40));
    dd->h = h;
    dd->last = w->t;
    R16g r = add_cell(g, h);
    R16g lr = r;
    lr.h = 20.0f;
    V4g tc = grey14(rdata_140d48);
    nvg_FillColor(ig, &tc);
    nvg_FontSize(ig, rdata_140d4c);
    draw_text(w, lr, 0, 0, label);
    R16g box = make_rect((float)((double)r.w * rdata_140d50 + (double)r.x), r.y, r.w * rdata_140d58, r.h);
    V4g oc = grey14(rdata_140d5c);
    nvg_StrokeColor(ig, &oc);
    nvg_StrokeWidth(ig, rdata_140d60);
    draw_rect_stroked(w, box, rdata_140d64);
    if (dd->open) {
        float rh = box.h;
        V4g sc = nvg_trans_rgba_f(grey14(rdata_140d5c), rdata_140d58);
        nvg_StrokeColor(ig, &sc);
        if (!n) return 0;
        rh /= (float)n;
        bool notopt = !opt;
        unsigned char done = 0;
        for (unsigned long long i = 0; i != n; i++) {
            R16g row = make_rect(box.x, (float)(unsigned)i * rh + box.y, box.w, rh);
            const char *name = items[i];
            bool hov = mouse_over_rect_r(g, row);
            unsigned s = (unsigned)*sel;
            V4g c;
            if (i == s) c = *(V4g *)(g + 0x360);
            else if (hov) c = grey14(i == 0 || notopt ? rdata_140d78 : rdata_140d80);
            else c = grey14(i != 0 || notopt ? rdata_140d48 : rdata_140d7c);
            nvg_FillColor(ig, &c);
            draw_text(w, row, 1, 0, name);
            if (!(done & 1)) {
                if (clicked_inside_r_frame(g, row)) {
                    *sel = (int)i;
                    dd->open = 0;
                    done = 1;
                } else {
                    done = 0;
                }
            } else {
                done = 1;
            }
            if (i) {
                nvg_BeginPath(ig);
                nvg_MoveTo(ig, row.x, row.y);
                nvg_LineTo(ig, row.x + row.w, row.y);
                nvg_Stroke(ig);
            }
        }
        return done;
    }
    bool small = n < 6;
    bool hov = mouse_over_rect_r(g, box);
    int s = *sel;
    if (opt && !small) {
        if (s) {
            float y = box.y, xh = box.h, x = box.w - xh + box.x;
            R16g xr = {x, y, xh, xh};
            bool xhov = mouse_over_rect_r(g, xr);
            V4g xc = xhov ? nvg_rgba_f(rdata_140d68, rdata_140d6c, rdata_140d6c, 1.0f) : grey14(rdata_140d5c);
            nvg_StrokeColor(ig, &xc);
            float in = (float)((double)xh * rdata_140d70);
            nvg_BeginPath(ig);
            float x0 = x + in, y0 = y + in, x1 = xh + x - in, y1 = y + xh - in;
            nvg_MoveTo(ig, x0, y0);
            nvg_LineTo(ig, x1, y1);
            nvg_MoveTo(ig, x1, y0);
            nvg_LineTo(ig, x0, y1);
            nvg_Stroke(ig);
            if (clicked_inside_r_frame(g, xr)) {
                *sel = 0;
                s = 0;
            } else {
                s = *sel;
            }
            hov = hov & !xhov;
        } else {
            s = 0;
        }
    }
    V4g c;
    if (opt && s == 0) c = grey14(hov ? rdata_140d80 : rdata_140d7c);
    else c = grey14(hov ? rdata_140d78 : rdata_140d48);
    nvg_FillColor(ig, &c);
    draw_text(w, box, 1, 0, items[(unsigned)*sel]);
    if (clicked_inside_r_frame(g, box)) {
        if (n >= 6) open_list_popup(g, rdata_140e3a, sel, (void *)text_filter_alnum, (void *)select_index, items + 1, n - 1);
        else dd->open = 1;
    }
    return 0;
}
