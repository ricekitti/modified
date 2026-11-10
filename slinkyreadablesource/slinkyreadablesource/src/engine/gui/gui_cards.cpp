// GUI: module cards - header, settings body, per-card open/close animation.
#include "slinky/engine.h"
#include "slinky/gui_viewport.h"
FN(void, close_popup, (void *x));
extern "C" void close_popup_now(void *x) { close_popup(x); }
extern "C" void reset_drag_state(char *g) { *(int *)(g + 0x370) = 0; g[0x4c8] = 0; }

struct R16f { float x, y, w, h; };
struct V4f { float x, y, z, w; };
FN(float, libc_powf, (float x, float y));   // powf
FN(void, nvg_GlobalAlpha, (void *ig, float alpha));
FN(V4f, nvg_rgba_f, (float r, float g, float b, float a));
FN(V4f, nvg_trans_rgba_f, (V4f c, float k));
FN(void, nvg_FillColor, (void *ig, V4f *c));
FN(R16f, make_rect, (float x, float y, float w, float h));
FN(void, draw_rect_filled, (void *w, R16f *r));
FN(void, nvg_Translate, (void *ig, float a, float b));
extern "C" const float rdata_1409c4, rdata_1409c8, rdata_1409cc, rdata_1409d0, rdata_1409d4, rdata_1409d8;
struct WinA { void *ig; char pad[0x10]; float w, h; };
// open/close animation: ease-out alpha, dimmed backdrop, slide offset
extern "C" void card_open_animation(WinA *w, float t) {
    float one = rdata_1409c4;
    float v = one - libc_powf(one - t, rdata_1409c8);
    void *ig = w->ig;
    nvg_GlobalAlpha(ig, v);
    float k = rdata_1409cc;
    V4f c = nvg_rgba_f(k, k, k, 1.0f);
    V4f c2 = nvg_trans_rgba_f(c, rdata_1409d0);
    nvg_FillColor(ig, &c2);
    R16f r = make_rect(0.0f, 0.0f, w->w, w->h);
    draw_rect_filled(w, &r);
    nvg_Translate(ig, 0.0f, rdata_1409d4);
    nvg_Translate(ig, 0.0f, v * rdata_1409d8);
}

// ---- module cards of one category: header (name, bind button, toggle), description, settings ----
struct Reg2 { void **list; unsigned long long n; };
struct Mgr2 { void *mods; unsigned long long count; Reg2 cat[5]; char pad[0x308 - 0x60]; void *set308; };
struct ModC { const char *name; const char *desc; char pad[4]; unsigned char enabled; char pad2[3]; unsigned flags;
              int bind; unsigned char hold; char pad3[7]; char anim[0x10]; float height; char pad4[4];
              unsigned char expanded; char pad5[0x1b7]; void (*settings)(char *g, void *w); };
FN(void, choose_columns, (char *g, float w));
FN(R16f, add_cell, (char *g, float h));
FN(bool, rects_intersect, (const R16f *a, const R16f *b));
FN(void, draw_rect_rounded, (void *w, const R16f *r, float round));
FN(void, nvg_Scissor, (void *ig, float x, float y, float w, float h));
FN(void, begin_content, (void *w, char *g, R16f *r, float pad, float f));
FN(void, pop_layout_row, (char *g, int n));
FN(float, draw_module_header, (char *g, void *w, void *m, const R16f *r, float fsz, const V4f *col, const V4f *hcol, const char *text));
FN(void, nvg_FontSize, (void *ig, float size));
FN(float, draw_text, (void *w, R16f r, int align, int col, const char *text));
FN(void, draw_module_description, (char *g, void *w, void *cbArg, void *target, int key, unsigned char *hold, const R16f *r));
FN(bool, draw_animated_description, (char *g, void *w, R16f *r, void *anim, unsigned char *val));
FN(void, queue_module_toggle, (void *mgr, void *mod, unsigned char on));
FN(float, end_layout_row, (void *w, char *g));
FN(float, lerp_clamped_float, (float a, float b, float t));
extern "C" const float rdata_1409e0, rdata_1409f0, rdata_1409f4, rdata_1409f8, rdata_1409fc, rdata_140a00, rdata_140a04,
    rdata_140a08, rdata_140a0c, rdata_140a10, rdata_140a14, rdata_140a18, rdata_140a1c;
extern "C" const double rdata_1409e8;
static inline __attribute__((always_inline)) V4f grey12(float k) { return nvg_rgba_f(k, k, k, 1.0f); }
extern "C" void draw_module_cards(char *g, WinA *w, Mgr2 *mgr, unsigned cat, float dt) {
    void *ig = w->ig;
    choose_columns(g, rdata_1409e0);
    Reg2 *list = &mgr->cat[cat];
    if (!list->n) return;
    float speed = (float)((double)dt / rdata_1409e8);
    for (unsigned long long i = 0; i < list->n; i++) {
        ModC *m = (ModC *)list->list[i];
        R16f card = add_cell(g, m->height);
        // the content height grows through the per-card end_layout_row finalizers alone, exactly like the
        // original: a card outside the viewport is only measured once it scrolls into view
        R16f view = make_rect(0.0f, *(float *)(g + 0x4bc), w->w, guiviewport::contentHeight(w->h));
        if (!rects_intersect(&view, &card)) continue;
        V4f cc;
        volatile float ck = rdata_1409f0;
        cc.x = cc.y = cc.z = ck;
        cc.w = 1.0f;
        nvg_FillColor(ig, &cc);
        draw_rect_rounded(w, &card, rdata_1409f4);
        R16f inner = card;
        begin_content(w, g, &inner, rdata_1409f8, 12.0f);
        pop_layout_row(g, 1);
        void *ig2 = w->ig;
        R16f hdr = add_cell(g, rdata_1409f4);
        float x;
        if (g[0x4cc] == 1) {
            V4f c1 = grey12(rdata_140a00), c2 = grey12(rdata_140a04);
            x = draw_module_header(g, w, m, &hdr, 16.0f, &c1, &c2, m->name);
        } else {
            nvg_FontSize(ig2, rdata_1409fc);
            V4f c = grey12(rdata_140a00);
            nvg_FillColor(ig2, &c);
            x = draw_text(w, hdr, 0, 0, m->name);
        }
        unsigned f = m->flags;
        if (!(f & 1)) {
            R16f kb = make_rect(x + rdata_140a08, hdr.y, hdr.w - x + hdr.x, hdr.h);
            draw_module_description(g, w, mgr->set308, m, m->bind, (m->flags & 8) ? 0 : &m->hold, &kb);
            f = m->flags;
        }
        if (!(f & 2)) {
            R16f tg = make_rect(hdr.x + hdr.w + rdata_140a0c, hdr.y, rdata_140a10, hdr.h);
            unsigned char on = m->enabled;
            draw_animated_description(g, w, &tg, m->anim, &on);
            if (on != m->enabled) queue_module_toggle(mgr, m, on);
        }
        if (g[0x4cc] == 0) {
            R16f d = add_cell(g, rdata_1409f4);
            V4f c1 = grey12(rdata_140a14), c2 = grey12(rdata_140a18);
            draw_module_header(g, w, m, &d, 14.0f, &c1, &c2, m->desc);
        }
        if (m->expanded == 1 && m->settings) m->settings(g, w);
        float used = end_layout_row(w, g);
        long long ia = (long long)used;
        long long ib = (long long)(used - rdata_140a1c);   // float -> uint64 like the original (no constant pool)
        unsigned long long u = (unsigned long long)((ib & (ia >> 63)) | ia);
        float target = (float)u;
        if (m->height != 0.0f) target = lerp_clamped_float(m->height, target, speed);
        m->height = target;
    }
}
extern "C" unsigned char is_unload_requested(char *g) { return (unsigned char)g[0x4cb]; }

// is rect r inside the scrolled viewport (0, scroll, width, height)?
extern "C" bool rect_in_viewport(char *g, WinA *w, const R16f *r) {
    R16f view = make_rect(0.0f, *(float *)(g + 0x4bc), w->w, guiviewport::contentHeight(w->h));
    R16f rr = *r;
    return rects_intersect(&view, &rr);
}
