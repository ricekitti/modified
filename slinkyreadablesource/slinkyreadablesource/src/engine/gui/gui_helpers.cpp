// GUI: small helpers - text buffers and formatting.
#include "slinky/engine.h"
extern "C" void select_index(int *out, int i, void *sel) { if (sel) *out = i + 1; }
struct C12 { long long a; int b; };
extern "C" void copy_pair12(C12 *o, const C12 *i) { o->b = i->b; o->a = i->a; }

FN(unsigned long long, libc_strlen, (const char *s));
extern "C" void text_filter_alnum(char *buf, char c) {
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

// ---- colour setting row: label, swatch (colour fading to 25 % alpha), click opens the colour picker ----
struct R15 { float x, y, w, h; };
struct V15 { float x, y, z, w; };
struct Grad15 { float v[19]; };
struct Col15 { float r, g, b; };
struct GW15 { void *ig; double t; };
FN(R15, add_cell, (char *g, float h));
FN(V15, nvg_rgba_f, (float r, float g, float b, float a));
FN(V15, nvg_rgb_f, (float r, float g, float b));
FN(void, nvg_FillColor, (void *ig, V15 *c));
FN(void, nvg_StrokeColor, (void *ig, V15 *c));
FN(void, nvg_StrokeWidth, (void *ig, float w));
FN(void, nvg_FontSize, (void *ig, float size));
FN(float, draw_text, (GW15 *w, R15 r, int align, int col, const char *text));
FN(R15, make_rect, (float x, float y, float w, float h));
FN(void, draw_rect_stroked, (GW15 *w, R15 r, float round));
FN(void, draw_rect_rounded, (GW15 *w, R15 r, float round));
FN(Grad15, nvg_linear_gradient, (void *ig, float x0, float y0, float x1, float y1, V15 c0, V15 c1));
FN(void, nvg_FillPaint, (void *ig, Grad15 g));
FN(unsigned char, clicked_inside_r_frame, (char *g, R15 r));
FN(void, open_colour_picker_colour_c, (char *g, const char *label, Col15 *col, const Col15 *c, void *cb));
extern "C" const float rdata_140d88, rdata_140d8c, rdata_140d90, rdata_140d94, rdata_140d98, rdata_140d9c,
    rdata_140da0, rdata_140da4, rdata_140db0;
extern "C" const double rdata_140da8;
extern "C" unsigned char draw_checkbox(char *g, GW15 *w, const char *label, Col15 *col) {
    void *ig = w->ig;
    R15 r = add_cell(g, rdata_140d88);
    R15 lr = r;
    lr.h = 20.0f;
    V15 tc = nvg_rgba_f(rdata_140d8c, rdata_140d8c, rdata_140d8c, 1.0f);
    nvg_FillColor(ig, &tc);
    nvg_FontSize(ig, rdata_140d90);
    draw_text(w, lr, 0, 0, label);
    R15 sw = make_rect(r.x + r.w + rdata_140d94, r.y, rdata_140d98, 20.0f);
    V15 oc = nvg_rgba_f(rdata_140d9c, rdata_140d9c, rdata_140d9c, 1.0f);
    nvg_StrokeColor(ig, &oc);
    nvg_StrokeWidth(ig, rdata_140da0);
    float rnd = rdata_140da4;
    draw_rect_stroked(w, sw, rnd);
    float x0 = sw.x, y0 = sw.y, x1 = sw.w + sw.x;
    V15 c0 = nvg_rgb_f(col->r, col->g, col->b);
    V15 c1 = nvg_rgba_f(col->r, col->g, col->b, 0.25f);
    nvg_FillPaint(ig, nvg_linear_gradient(ig, x0, y0, x1, y0, c0, c1));
    draw_rect_rounded(w, sw, rnd);
    R15 cr = make_rect((float)((double)r.w * rdata_140da8 + (double)r.x), r.y, r.w * rdata_140db0, r.h);
    unsigned char clicked = clicked_inside_r_frame(g, cr);
    if (clicked) {
        Col15 c = *col;
        open_colour_picker_colour_c(g, label, col, &c, (void *)copy_pair12);
    }
    return clicked;
}

// ---- list setting: one row per entry (name, colour swatch for colour lists, remove cross), "Click to add" ----
struct ListOps {
    void *ctx;
    unsigned long long (*count)(void *ctx);
    const char *(*name)(void *ctx, unsigned long long i);
    Col15 *(*color)(void *ctx, unsigned long long i);
    void *p20;
    void (*remove)(void *ctx, unsigned long long i);
};
FN(float, draw_text_sized, (GW15 *w, R15 r, unsigned align, unsigned col, const char *text, const char *end));
FN(R15, inset_rect, (R15 r, float d));
FN(V15, nvg_trans_rgba_f, (V15 c, float k));
FN(bool, mouse_over_rect_r, (char *g, R15 r));
FN(void, nvg_BeginPath, (void *ig));
FN(void, nvg_MoveTo, (void *ig, float x, float y));
FN(void, nvg_LineTo, (void *ig, float x, float y));
FN(void, nvg_Stroke, (void *ig));
FN(void, pick_add_highlight, (void *p, void *, void *key));
FN(void, pick_add_shadow, (void *p, void *key));
FN(void, open_list_popup, (char *g, void *a, void *b, void *c, void *d0, void *d1, void *e));
FN(void, start_timed_popup_drag_payload, (char *g, void *a, void *b, void *c, void *d));
extern "C" const float rdata_140db8, rdata_140dbc, rdata_140dc8, rdata_140dcc, rdata_140dd0, rdata_140dd4,
    rdata_140dd8, rdata_140ddc, rdata_140de0, rdata_140de4, rdata_140df0, rdata_140df4;
extern "C" const double rdata_140dc0, rdata_140de8;
extern "C" const char rdata_140e46[];   // "Click to add"
extern "C" bool draw_colored_checkbox(char *g, GW15 *w, const char *label, void *a, void *b, int kind, ListOps *l, void *p8,
                         void *p9) {
    void *ig = w->ig;
    unsigned long long n = l->count(l->ctx);
    R15 r = add_cell(g, (float)(n + 27 * (n + 1) + 1));
    R15 lr = r;
    lr.h = 20.0f;
    V15 tc = nvg_rgba_f(rdata_140db8, rdata_140db8, rdata_140db8, 1.0f);
    nvg_FillColor(ig, &tc);
    nvg_FontSize(ig, rdata_140dbc);
    draw_text(w, lr, 0, 0, label);
    R15 box = make_rect((float)((double)r.w * rdata_140dc0 + (double)r.x), r.y, r.w * rdata_140dc8, r.h);
    V15 oc = nvg_rgba_f(rdata_140dcc, rdata_140dcc, rdata_140dcc, 1.0f);
    nvg_StrokeColor(ig, &oc);
    nvg_StrokeWidth(ig, rdata_140dd0);
    draw_rect_stroked(w, box, rdata_140dd4);
    float rh = box.h / (float)(n + 1);
    bool changed = false;
    if (n) {
        unsigned long long del = ~0ull;
        float dim = rdata_140dcc;
        for (unsigned long long i = 0; i != n; i++) {
            R15 row = make_rect(box.x, (float)i * rh + box.y, box.w, rh);
            const char *name = l->name(l->ctx, i);
            unsigned long long len = libc_strlen(name);
            if (len >= 0x14) len = 0x14;
            V15 nc = nvg_rgba_f(rdata_140db8, rdata_140db8, rdata_140db8, 1.0f);
            nvg_FillColor(ig, &nc);
            draw_text_sized(w, row, 1, 0, name, name + len);
            if (kind == 1) {
                R15 sw = inset_rect(R15{row.x, row.y, row.h, row.h}, rdata_140dd8);
                Col15 *col = l->color(l->ctx, i);
                V15 cc = nvg_rgb_f(col->r, col->g, col->b);
                nvg_FillColor(ig, &cc);
                draw_rect_rounded(w, sw, rdata_140ddc);
                if (clicked_inside_r_frame(g, sw)) {
                    Col15 c = *col;
                    open_colour_picker_colour_c(g, name, col, &c, (void *)copy_pair12);
                    changed = true;
                }
            }
            float y = row.y, h = row.h, x = row.w - h + row.x;
            R15 xr = {x, y, h, h};
            V15 xc = mouse_over_rect_r(g, xr) ? nvg_rgba_f(rdata_140de0, rdata_140de4, rdata_140de4, 1.0f)
                                     : nvg_rgba_f(dim, dim, dim, 1.0f);
            nvg_StrokeColor(ig, &xc);
            float in = (float)((double)h * rdata_140de8);
            nvg_BeginPath(ig);
            float x0 = x + in, y0 = y + in, x1 = h + x - in, y1 = y + h - in;
            nvg_MoveTo(ig, x0, y0);
            nvg_LineTo(ig, x1, y1);
            nvg_MoveTo(ig, x1, y0);
            nvg_LineTo(ig, x0, y1);
            nvg_Stroke(ig);
            if (clicked_inside_r_frame(g, xr)) del = i;
            V15 sc = nvg_trans_rgba_f(nvg_rgba_f(dim, dim, dim, 1.0f), rdata_140dc8);
            nvg_StrokeColor(ig, &sc);
            nvg_BeginPath(ig);
            nvg_MoveTo(ig, row.x, row.y + row.h);
            nvg_LineTo(ig, row.x + row.w, row.y + row.h);
            nvg_Stroke(ig);
        }
        if (del != ~0ull) {
            l->remove(l->ctx, del);
            changed = true;
        }
    }
    R15 add = make_rect(box.x, (float)n * rh + box.y, box.w, rh);
    float k = mouse_over_rect_r(g, add) ? rdata_140df4 : rdata_140df0;
    V15 ac = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &ac);
    draw_text(w, add, 1, 0, rdata_140e46);
    if (clicked_inside_r_frame(g, add)) {
        if (p8) open_list_popup(g, a, l, b, (void *)pick_add_highlight, p8, p9);
        else start_timed_popup_drag_payload(g, a, l, b, (void *)pick_add_shadow);
        changed = true;
    }
    return changed;
}
