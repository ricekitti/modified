// 0x10000: GUI window helpers
#include "slinky/engine.h"
#include "slinky/hud_editor.h"
FN(void, nvg_FontFaceId, (void *imgui, unsigned col));
struct GCol { void *imgui; char pad[0x38]; unsigned col[1]; };
extern "C" void set_draw_color(GCol *g, unsigned i) {
    void *ig = g->imgui;

    nvg_FontFaceId(ig, g->col[i]);
}
extern "C" void reset_layout_columns(char *g) { *(long long *)(g + 0x258) = 0; *(int *)(g + 0x260) = 0; }

FN(void, nvg_BeginPath, (void *ig));
FN(void, nvg_Rect, (void *ig, float x, float y, float w, float h));
FN(void, nvg_Fill, (void *ig));
extern "C" void draw_rect_filled(void **w, const float *r) {
    void *ig = w[0];
    nvg_BeginPath(ig);
    float x = r[0];
    float y = r[1];
    float w2 = r[2];
    float h = r[3];
    nvg_Rect(ig, x, y, w2, h);
    nvg_Fill(ig);
}

FN(void, nvg_RoundedRect, (void *ig, float x, float y, float w, float h, float round));
FN(void, nvg_Stroke, (void *ig));
extern "C" void draw_rect_rounded(void **w, const float *r, float round) {
    float x = r[0], y = r[1], ww = r[2], h = r[3];
    void *ig = w[0];
    nvg_BeginPath(ig);
    nvg_RoundedRect(ig, x, y, ww, h, round);
    nvg_Fill(ig);
}
extern "C" void draw_rect_stroked(void **w, const float *r, float round) {
    float x = r[0], y = r[1], ww = r[2], h = r[3];
    void *ig = w[0];
    nvg_BeginPath(ig);
    nvg_RoundedRect(ig, x, y, ww, h, round);
    nvg_Stroke(ig);
}

struct R16e { float x, y, w, h; };
struct V4e { float x, y, z, w; };
FN(void, push_layout_row, (char *g, R16e *r, float sp, float pad));
FN(R16e, round_rect, (R16e r));
FN(void, nvg_Save, (void *ig));
FN(void, nvg_IntersectScissor, (void *ig, float x, float y, float w, float h));
typedef int v4ie __attribute__((vector_size(16)));
typedef float v4fe __attribute__((vector_size(16)));
extern "C" const v4ie rdata_140810;   // sign mask
// begin a child region: push it, round the rect to whole pixels and clip to its padded inside
extern "C" void begin_content(void **w, char *g, R16e *r, float pad, float f) {
    void *ig = w[0];
    // the original round-trips a stack copy of the rect: push_layout_row clips and insets the copy to build
    // the pushed row while the caller's rect stays raw, then the raw rect is rounded and reused for
    // the scissor - whose left/top edge sits at the card edge, not at the already-inset row. Scissoring
    // the inset row shaved the first glyph off every label ("mode" -> "ode").
    R16e local = *r;
    push_layout_row(g, &local, pad, f);
    g[*(long long *)(g + 0x258) * 0x78 - 0x60] = 1;
    *r = round_rect(*r);
    nvg_Save(ig);
    v4fe v = {pad, 0, 0, 0};
    float np = ((v4fe)((v4ie)v ^ rdata_140810))[0] - pad;
    nvg_IntersectScissor(ig, r->x + pad, r->y + pad, r->w + np, np + r->h);
}
FN(float, libc_sinf, (float x));   // sinf
FN(V4e, nvg_trans_rgba_f, (V4e c, float k));
FN(void, nvg_StrokeColor, (void *ig, V4e *c));
FN(void, nvg_StrokeWidth, (void *ig, float w));
FN(void, nvg_MoveTo, (void *ig, float x, float y));
FN(void, nvg_LineTo, (void *ig, float x, float y));
extern "C" const float rdata_140858;
struct WT { void *ig; double t; };
// pulsing vertical accent line at the left edge of r
extern "C" void draw_accent_line(WT *w, R16e *r, V4e *col, float speed, float width) {
    void *ig = w->ig;
    float s = libc_sinf((float)((double)speed * w->t));
    float k = rdata_140858;
    k = s * k + k;
    nvg_BeginPath(ig);
    V4e c = nvg_trans_rgba_f(*col, k);
    nvg_StrokeColor(ig, &c);
    nvg_StrokeWidth(ig, width);
    nvg_MoveTo(ig, r->x, r->y);
    nvg_LineTo(ig, r->x, r->y + r->h);
    nvg_Stroke(ig);
}

extern "C" const float rdata_140824;
// set the column count of the current layout row (finishing the previous row's height)
struct Cell { float x, y, w, h; };
struct Row { char pad[4]; float top; float width; int padw; float y0; int pad14; unsigned char clip; char pad2[7]; float bottom; int pad3; long long cols; Cell cell[4]; unsigned long long items; };
static_assert(__builtin_offsetof(Row, cell) == 0x30 && __builtin_offsetof(Row, items) == 0x70, "row layout");
extern "C" void pop_layout_row(char *g, int n) {
    Row *row = (Row *)(g + *(long long *)(g + 0x258) * 0x78 - 0x78);
    unsigned long long k = row->items;
    if (k) {
        k--;
        // the compiled binary here reloads only one cell, (items-1)&3 i.e. the last added one: its
        // "max over the last visual row" loop re-maxes that same value into a register constant and
        // moves it - dead straight-lining. Two columns therefore never cross-balance their bottoms.
        Cell *c = &row->cell[k & 3];
        float m = c->y + c->h;
        m = m > rdata_140824 ? m : rdata_140824;
        if (m != rdata_140824) row->bottom = m + row->y0;
    }
    int cols = n < 4 ? n : 4;
    if (!n) cols = 1;
    row->cols = cols;
    row->items = 0;
}
FN(void, nvg_TextAlign, (void *ig, unsigned font));
FN(float, nvg_text, (void *ig, float x, float y, void *text, void *end));
extern "C" const unsigned rdata_14085c[];
extern "C" const float rdata_140848;
extern "C" const double rdata_140850;   // 0.5
// text in rect r: align 0 = left, 1 = centre, 2 = right; vertically centred
extern "C" void draw_text_sized(GCol *g, R16e *r, unsigned align, unsigned col, void *text, void *end) {
    set_draw_color(g, col);
    void *ig = g->imgui;
    nvg_TextAlign(ig, rdata_14085c[align]);
    float x = r->x;
    float y = r->y + rdata_140848;
    r->y = y;
    if (align != 0) {
        if (align == 2) x = x + r->w;
        else x = (float)((double)r->w * rdata_140850 + (double)x);
    }
    float yy = (float)((double)r->h * rdata_140850 + (double)y);
    nvg_text(ig, x, yy, text, end);
}

extern "C" const float rdata_140828;
// pick the column count so that cells of width w fit the row
extern "C" void choose_columns(char *g, float w) {
    Row *row = (Row *)(g + *(long long *)(g + 0x258) * 0x78 - 0x78);
    float avail = row->width, sp = row->y0;
    unsigned long long k = row->items;
    if (k) {
        k--;
        // same single-cell finalizer as end_layout_row/pop_layout_row, see the note there
        Cell *c = &row->cell[k & 3];
        float m = c->y + c->h;
        m = m > rdata_140828 ? m : rdata_140828;
        if (m != rdata_140828) row->bottom = m + sp;
    }
    int n = (int)((avail + sp) / (w + sp));
    int cols = n < 4 ? n : 4;
    if (!n) cols = 1;
    row->cols = cols;
    row->items = 0;
}
extern "C" const float rdata_140840;
extern "C" const double rdata_140838;
// text in rect r (read-only), returns its width
extern "C" float draw_text(GCol *g, const R16e *r, unsigned align, unsigned col, void *text) {
    float x = r->x, y = r->y, w = r->w, h = r->h;
    set_draw_color(g, col);
    void *ig = g->imgui;
    nvg_TextAlign(ig, rdata_14085c[align]);
    if (align != 0) {
        if (align == 2) x = x + w;
        else x = (float)((double)w * rdata_140838 + (double)x);
    }
    y += rdata_140840;
    float yy = (float)((double)h * rdata_140838 + (double)y);
    return nvg_text(ig, x, yy, text, 0);
}

extern "C" const float rdata_140820;
FN(void, nvg_Restore, (void *ig));
// end the current layout row: finish its height, pop its clip, grow the window height; returns the used height
extern "C" float end_layout_row(GCol *w, char *g) {
    Row *row = (Row *)(g + *(long long *)(g + 0x258) * 0x78 - 0x78);
    unsigned long long k = row->items;
    if (k) {
        k--;
        // only the last added cell ((items-1)&3) reaches the finalizer in the compiled original
        Cell *c = &row->cell[k & 3];
        float m = c->y + c->h;
        m = m > rdata_140820 ? m : rdata_140820;
        if (m != rdata_140820) row->bottom = m + row->y0;
    }
    if (row->clip == 1) nvg_Restore(w->imgui);
    float bottom = row->bottom;
    if (bottom > (float)*(int *)(g + 0x260)) *(int *)(g + 0x260) = (int)bottom;
    *(long long *)(g + 0x258) -= 1;
    return (row->y0 + row->y0) + (bottom - row->top);
}

FN(R16e, inset_rect, (R16e r, float d));
FN(unsigned long long, offset_rect, (R16e r));
// push a layout row: clip r to the parent row, inset it by pad, start the cursor at its corner
extern "C" void push_layout_row(char *g, R16e *r, float sp, float pad) {
    long long n = *(long long *)(g + 0x258);
    if (n) {
        float *p = (float *)(g + n * 0x78 - 0x78);
        float px = p[0], py = p[1], pw = p[2], ph = p[3];
        float x = r->x;
        if (px > x) { r->x = px; x = px; }
        float y = r->y;
        if (py > y) { r->y = py; y = py; }
        float right = px + pw;
        if (r->w + x > right) {
            r->w = right - x;
            if (0.0f > r->w) r->w = 0.0f;
        }
        float bottom = py + ph;
        if (r->h + y > bottom) {
            r->h = bottom - y;
            if (0.0f > r->h) r->h = 0.0f;
        }
    }
    char *row = g + n * 0x78;
    *(long long *)(g + 0x258) = n + 1;
    *r = inset_rect(*r, pad);
    __builtin_memset(row, 0, 0x78);
    *(R16e *)row = *r;
    *(float *)(row + 0x10) = sp;
    *(float *)(row + 0x14) = pad;
    *(unsigned long long *)(row + 0x1c) = offset_rect(*r);
}
struct Img { char b[0x4c]; };
struct Off2 { float x, y; };
FN(Img, nvg_image_pattern, (void *ig, float x, float y, float w, float h, int z, int tex, float f));
FN(void, nvg_FillPaint, (void *ig, Img im));
extern "C" const float rdata_140830;
extern "C" Off2 *rdata_140868;
struct GImg { void *imgui; char pad[0x40]; int tex; float tw, th; };
// draw texture tile `idx` scaled into r, clipped to r
extern "C" void draw_texture(GImg *w, R16e *r, unsigned idx, float f) {
    void *ig = w->imgui;
    float k = rdata_140830;
    float sx = r->w / k, sy = r->h / k;
    Off2 *t = rdata_140868;
    float ox = r->x - t[idx].x * sx;
    float oy = r->y - t[idx].y * sy;
    Img im = nvg_image_pattern(ig, ox, oy, sx * w->tw, sy * w->th, 0, w->tex, f);
    nvg_BeginPath(ig);
    nvg_Rect(ig, r->x, r->y, r->w, r->h);
    nvg_FillPaint(ig, im);
    nvg_Fill(ig);
}

// next cell of the current layout row (height h), rounded to whole pixels
extern "C" R16e add_cell(char *g, float h) {
    long long top = *(long long *)(g + 0x258);
    Row *row = (Row *)(g + top * 0x78 - 0x78);
    unsigned long long cols = (unsigned long long)row->cols;
    unsigned long long n = row->items;
    row->items = n + 1;
    Cell *above = &row->cell[(n - cols) & 3];
    Cell *c = &row->cell[n & 3];
    if (n % cols) {
        Cell *prev = &row->cell[(n - 1) & 3];
        float pw = prev->w;
        c->x = prev->x + pw + row->y0;
        c->w = pw;
        c->y = n < cols ? row->bottom : above->y + above->h + row->y0;
    } else {
        c->x = *(float *)((char *)row + 0x1c);
        if (n < cols) {
            c->y = row->bottom;
            c->w = (row->width - (float)(cols - 1) * row->y0) / (float)cols;
        } else {
            c->y = above->y + above->h + row->y0;
            c->w = above->w;
        }
    }
    c->h = h;
    return round_rect(*(R16e *)c);
}

// ---- input events: 1 mouse move, 2 key/button (+ up to 4 typed chars), 3 scroll; returns "consumed" ----
FN(bool, popup_is_open, (char *g));
FN(unsigned long long, libc_strlen, (const char *s));
FN(void, close_popup, (char *g));
extern "C" const float rdata_140870;
struct InEv { int type; union { int key; float wheel; }; union { int y; unsigned char ch[4]; }; unsigned char down; };
typedef void (*CharFilter)(char *buf, char c, void *arg);
extern "C" bool handle_gui_input(char *g, InEv *in) {
    if(hud_editor_input(in))return true;
    int st;
    switch (in->type) {
    case 1:
        *(float *)(g + 0x4b0) = (float)in->key;
        *(float *)(g + 0x4b4) = (float)in->y;
        return false;
    case 3:
        if (!popup_is_open(g)) *(float *)(g + 0x4bc) += in->wheel * rdata_140870;
        return false;
    case 2:
        if (in->down != 1) break;
        st = *(int *)(g + 0x370);
        if ((unsigned)(st - 2) < 2) {           // text field active
            int k = in->key;
            if (k > 0x54) {
                if (k == 0x55) {                 // enter
                    if (g[0x378]) g[0x3f0] = 1;
                    return true;
                }
                if (k == 0x57) {                 // escape
                    *(int *)(g + 0x370) = 0;
                    return true;
                }
            } else if (k == 1) {
                if (st != 3) {
                    *(int *)(g + 0x370) = 0;
                    return true;
                }
                break;
            } else if (k == 0x53) {              // backspace
                unsigned long long n = libc_strlen(g + 0x378);
                if (n) g[0x378 + n - 1] = 0;
                return true;
            }
            CharFilter f = *(CharFilter *)(g + 0x3e0);
            for (int i = 0; i < 4; i++) {
                char c = (char)in->ch[i];
                if (!c) return true;
                f = *(CharFilter *)(g + 0x3e0);
                f(g + 0x378, c, *(void **)(g + 0x3e8));
            }
            return true;
        }
        if (st == 1) {                           // key-bind capture
            int k = in->key;
            int key = (k == 1 || k == 0x57) ? 0 : k;
            (*(void (**)(void *, int))(g + 0x378))(*(void **)(g + 0x380), key);
            *(int *)(g + 0x370) = 0;
            return true;
        }
        if (st == 0 && popup_is_open(g) && in->key == 0x57) {
            close_popup(g);
            return true;
        }
        break;
    default:
        return false;
    }
    {
        int k = in->key;
        if ((unsigned)(k - 100) < 2) g[0x4ca] = in->down;
        else if (k == 1) g[0x4c8] = in->down;
    }
    return false;
}

// ---- GUI render target: ImGui frame into an offscreen framebuffer scaled by `scale` ----
struct FBuf { char pad[0x14]; int tex; };
struct Rend { void *ig; double t; int w, h; float sw, sh; float scale; int pad; FBuf *fb; float clear[4]; };
FN(void, nvg_wrap_delete_framebuffer, (FBuf *fb));                                    // destroy
FN(FBuf *, nvglu_create_framebuffer, (void *ig, int w, int h, int flags));         // create
FN(void, nvg_wrap_bind_framebuffer, (FBuf *fb));                                    // bind (0 = default)
FN(void, nvg_BeginFrame, (void *ig, float w, float h, float dpi));       // begin frame
FN(void, nvg_EndFrame, (void *ig));                                    // end frame
FN(void, nvg_Scale, (void *ig, float x, float y));
extern "C" void (**const rdata_13e460)(unsigned pname, float *out);   // glGetFloatv
extern "C" void (**const rdata_140800)(float r, float g, float b, float a);   // glClearColor
extern "C" void (**const rdata_13e440)(unsigned mask);   // glClear
extern "C" float rdata_140780, rdata_140784;
extern "C" void set_render_scale(Rend *r, float scale) {
    r->scale = scale;
    r->sw = (float)r->w / scale;
    r->sh = (float)r->h / scale;
}
// (re)create the framebuffer when the window size changed
extern "C" bool resize_render_target(Rend *r, int w, int h) {
    bool changed;
    if (w == r->w && h == r->h) {
        changed = false;
    } else {
        if (r->fb) nvg_wrap_delete_framebuffer(r->fb);
        r->fb = nvglu_create_framebuffer(r->ig, w, h, 0);
        changed = true;
    }
    r->w = w;
    r->h = h;
    return changed;
}
extern "C" void begin_frame(Rend *r, double t) {
    float fw = (float)r->w;

    void *ig = r->ig;
    float fh = (float)r->h;

    nvg_BeginFrame(ig, fw, fh, rdata_140780);
    nvg_Scale(r->ig, r->scale, r->scale);
    r->t = t;
}
// begin a frame into the framebuffer (saves and clears the GL clear colour)
extern "C" void begin_frame_framebuffer(Rend *r, double t) {
    nvg_wrap_bind_framebuffer(r->fb);
    (*rdata_13e460)(0xc22, r->clear);   // GL_COLOR_CLEAR_VALUE
    (*rdata_140800)(0.0f, 0.0f, 0.0f, 0.0f);
    (*rdata_13e440)(0x4400);            // colour | stencil
    float fw = (float)r->w;

    void *ig = r->ig;
    float fh = (float)r->h;

    nvg_BeginFrame(ig, fw, fh, rdata_140784);
    nvg_Scale(r->ig, r->scale, r->scale);
    r->t = t;
}
// draw the framebuffer texture over the screen
extern "C" void draw_framebuffer_texture_over_screen(Rend *r) {
    void *ig = r->ig;
    float sw = r->sw, sh = r->sh;

    Img g = nvg_image_pattern(ig, 0.0f, 0.0f, sw, sh, 0, r->fb->tex, 1.0f);
    nvg_BeginPath(ig);
    nvg_Rect(ig, 0.0f, 0.0f, r->sw, r->sh);
    nvg_FillPaint(ig, g);
    nvg_Fill(ig);
}
extern "C" void end_frame(Rend *r) { nvg_EndFrame(r->ig); }
extern "C" void clear_frame(Rend *r) {
    nvg_EndFrame(r->ig);
    nvg_wrap_bind_framebuffer(0);
    void (*cc)(float, float, float, float) = *rdata_140800;

    float c0 = r->clear[0], c1 = r->clear[1], c2 = r->clear[2], c3 = r->clear[3];

    cc(c0, c1, c2, c3);
}
