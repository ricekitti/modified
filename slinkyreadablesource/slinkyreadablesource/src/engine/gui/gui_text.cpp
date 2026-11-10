// GUI: text rendering helpers - labels, wrapped descriptions, numeric filters.
#include "slinky/engine.h"

FN(unsigned long long, libc_strlen, (const char *s));
// file-name text filter: printable, no slashes, max 15 chars
extern "C" void file_name_text_filter(char *buf, char c) {
    unsigned long long n = libc_strlen(buf);
    unsigned char lng = n >= 0xf;

    unsigned char np = (unsigned char)(c - 0x20) >= 0x5f;

    if (np | lng) return;
    if (c == 0x2f || c == 0x5c) return;
    buf[n] = c;
}

struct R16c { float x, y, w, h; };
struct V4 { float x, y, z, w; };
FN(R16c, add_cell, (const void *src, float f));
FN(V4, nvg_rgba_f, (float r, float g, float b, float a));
FN(void, nvg_FillColor, (void *ig, V4 *c));
FN(void, nvg_FontSize, (void *ig, float f));
FN(float, draw_text, (void *w, R16c r, int a, int b, void *x));
extern "C" const float rdata_140c80, rdata_140c84, rdata_140c88, rdata_140c8c;
extern "C" float draw_label_va(const void *src, void **w, void *x) {
    void *ig = w[0];
    R16c r = add_cell(src, rdata_140c80);
    r.y += rdata_140c84;
    float g = rdata_140c88;
    V4 c = nvg_rgba_f(g, g, g, 1.0f);
    nvg_FillColor(ig, &c);
    nvg_FontSize(ig, rdata_140c8c);
    return draw_text(w, r, 0, 0, x);
}

FN(void *, libc_malloc, (unsigned long long n));
FN(void, libc_free, (void *p));
FN(void *, libc_memcpy, (void *d, const void *s, unsigned long long n));
FN(void, confirmation_popup_formatted_message, (void *g, void *a, void *b, const char *fmt, ...));
FN(void, run_confirmed_profile_action, (void *p));
extern "C" const int rdata_140c30[];
extern "C" const char rdata_140aca[];   // "Default"
struct Confirm { void *g, *a, *b; char *name; int kind; };
// ask "Are you sure?" before running action `kind` on profile `name`
extern "C" void ask_sure_before_running_action(void *g, void *a, void *b, const char *name, int kind) {
    Confirm *c = (Confirm *)libc_malloc(0x28);
    if (!c) return;
    c->g = g; c->a = a; c->b = b; c->name = 0; c->kind = kind;
    if (name) {
        unsigned long long n = libc_strlen(name);
        char *s = (char *)libc_malloc(n + 1);
        if (!s) { libc_free(c); return; }
        libc_memcpy(s, name, n);
        s[n] = 0;
        c->name = s;
    } else {
        name = rdata_140aca;
    }
    const char *fmt = (const char *)rdata_140c30 + rdata_140c30[(unsigned)kind];
    confirmation_popup_formatted_message(g, c, (void *)run_confirmed_profile_action, fmt, name);
}

FN(int, load_profile_name, (const char *name, void *mods));
FN(int, save_profile, (const char *name, bool text, void *mods));
FN(int, open_profiles_folder, ());
FN(int, delete_profile_file, (const char *name));
FN(void, dispatch_event, (void *mgr, void *e));
struct ProfEvent { int type; int pad; union { int code; const char *name; }; int kind; char rest[0x2c]; };
// run the confirmed profile action (0 load, 1 save, 2 open folder, 3 delete) and report it
extern "C" void run_confirmed_profile_action(void *cv) {
    Confirm *c = (Confirm *)cv;
    int r;
    switch ((unsigned)c->kind) {
    case 0: r = load_profile_name(c->name, c->a); break;
    case 1: r = save_profile(c->name, true, c->a); break;
    case 2: r = open_profiles_folder(); break;
    case 3: r = delete_profile_file(c->name); break;
    default: r = 0; goto ok;
    }
    if (r) {
        ProfEvent e = {};
        e.type = 0xc;
        e.code = r;
        dispatch_event(c->a, &e);
        goto done;
    }
ok:
    {
        ProfEvent e = {};
        e.type = 0xb;
        e.name = c->name;
        e.kind = c->kind;
        dispatch_event(c->a, &e);
    }
done:
    *(double *)((char *)c->g + 0x4a0) = -1.0;
    libc_free(c->name);
    libc_free(c);
}

// ---- animated toggle switch; returns true when clicked ----
struct Anim { float pos; int pad; double last; };
struct WT2 { void *ig; double t; };
FN(float, ease_curve, (float t));   // smoothstep
FN(float, lerp_unclamped_float, (float a, float b, float t));
FN(bool, mouse_over_rect_r, (char *g, const R16c *r));
FN(unsigned char, clicked_inside_r_frame, (char *g, R16c r));
FN(V4, nvg_lerp_rgba, (V4 a, V4 b, float t));
FN(void, draw_rect_rounded, (WT2 *w, const R16c *r, float round));
FN(R16c, inset_rect, (R16c r, float d));
FN(void, nvg_BeginPath, (void *ig));
FN(void, nvg_Circle, (void *ig, float x, float y, float rad));
FN(void, nvg_Fill, (void *ig));
extern "C" const double rdata_140c40, rdata_140c48;
extern "C" const float rdata_140c50, rdata_140c54, rdata_140c58, rdata_140c5c, rdata_140c60, rdata_140c64, rdata_140c68;
extern "C" bool draw_animated_description(char *g, WT2 *w, R16c *r, Anim *an, unsigned char *val) {
    double t = w->t;
    double dt = (double)(float)(t - an->last);
    unsigned char v = *val;
    float p;
    if (dt > rdata_140c40) {
        p = (float)v;
        an->pos = p;
    } else {
        p = an->pos;
        if (v) {
            if (rdata_140c50 > p) {
                p = (float)(dt / rdata_140c40 + (double)p);
                p = p < rdata_140c50 ? p : rdata_140c50;
                an->pos = p;
            }
        } else if (p > 0.0f) {
            p = (float)(dt / rdata_140c48 + (double)p);
            p = p > 0.0f ? p : 0.0f;
            an->pos = p;
        }
    }
    an->last = t;
    void *ig = w->ig;
    float e = ease_curve(p);
    float k = mouse_over_rect_r(g, r) ? rdata_140c58 : rdata_140c54;
    V4 col = nvg_rgba_f(k, k, k, 1.0f);
    V4 track = nvg_lerp_rgba(col, *(V4 *)(g + 0x360), e);
    nvg_FillColor(ig, &track);
    draw_rect_rounded(w, r, rdata_140c5c);
    bool clicked = clicked_inside_r_frame(g, *r);
    if (clicked) *val ^= 1;
    *r = inset_rect(*r, rdata_140c60);
    float h = r->h;
    float kk = rdata_140c64;
    V4 knob = nvg_rgba_f(kk, kk, kk, 1.0f);
    nvg_FillColor(ig, &knob);
    nvg_BeginPath(ig);
    float rad = (float)(int)h;
    float off = rad * rdata_140c68;
    float x = lerp_unclamped_float(off + r->x, r->x + r->w - off, e);
    nvg_Circle(ig, x, off + r->y, rad);
    nvg_Fill(ig);
    return clicked;
}

// ---- module header row: [+]/[-] expander box and name; returns the right edge of the clickable area ----
FN(R16c, make_rect, (float x, float y, float w, float h));
FN(float, nvg_text_bounds, (void *ig, float x, float y, const char *text, const char *end, const float *bounds));   // nvgTextBounds
FN(void, nvg_StrokeColor, (void *ig, V4 *c));
FN(void, nvg_MoveTo, (void *ig, float x, float y));
FN(void, nvg_LineTo, (void *ig, float x, float y));
FN(void, nvg_StrokeWidth, (void *ig, float w));
FN(void, nvg_Stroke, (void *ig));
extern "C" const float rdata_140a20, rdata_140a24, rdata_140a28, rdata_140a2c, rdata_140a30, rdata_140a34, rdata_140a38,
    rdata_140a3c, rdata_140a40;
struct ModHdr { char pad[0x40]; unsigned char expanded; char pad2[0x1b7]; void *settings; };
extern "C" float draw_module_header(char *g, WT2 *w, ModHdr *m, const R16c *r, float fsz, const V4 *col, const V4 *hcol,
                          const char *text) {
    void *ig = w->ig;
    if (!m->settings) {
        nvg_FontSize(ig, fsz);
        V4 c = *col;
        nvg_FillColor(ig, &c);
        return draw_text(w, *r, 0, 0, (void *)text);
    }
    R16c box = make_rect(r->x, r->y, r->h, r->h);
    float gap = rdata_140a20;
    float tx = box.x + box.w + gap;
    float tw = nvg_text_bounds(ig, tx, r->y, text, 0, (const float *)0);
    R16c lbl = make_rect(tx, r->y, tw, r->h);
    R16c hit = make_rect(r->x, r->y, box.w + lbl.w + gap, r->h);
    if (clicked_inside_r_frame(g, hit)) m->expanded ^= 1;
    bool hov = mouse_over_rect_r(g, &hit);
    nvg_FontSize(ig, fsz);
    V4 c = *(hov ? hcol : col);
    nvg_FillColor(ig, &c);
    draw_text(w, lbl, 0, 0, (void *)text);
    float k = hov ? rdata_140a28 : rdata_140a24;
    V4 bc = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &bc);
    draw_rect_rounded(w, &box, rdata_140a2c);
    R16c in = inset_rect(box, rdata_140a30);
    float k2 = hov ? rdata_140a38 : rdata_140a34;
    V4 sc = nvg_rgba_f(k2, k2, k2, 1.0f);
    nvg_StrokeColor(ig, &sc);
    nvg_BeginPath(ig);
    float half = rdata_140a3c;
    nvg_MoveTo(ig, in.x, in.h * half + in.y);
    nvg_LineTo(ig, in.x + in.w, in.h * half + in.y);
    if (!m->expanded) {
        nvg_MoveTo(ig, in.w * half + in.x, in.y);
        nvg_LineTo(ig, half * in.w + in.x, in.y + in.h);
    }
    nvg_StrokeWidth(ig, rdata_140a40);
    nvg_Stroke(ig);
    return hit.x + hit.w;
}

// ---- key-bind button ("PRESS A KEY..." while capturing) and optional "HOLD" toggle ----
FN(void, set_draw_color, (void *w, unsigned col));
FN(bool, drag_owner_is, (char *g, void *b));
FN(void, begin_drag, (char *g, void *a, void *b));
FN(float, libc_ceilf, (float x));   // ceilf
FN(const char *, mouse_button_name, (int key));
FN(V4, nvg_trans_rgba_f, (V4 c, float k));
extern "C" float data_2f7490[];
extern "C" const char rdata_140ab6[], rdata_140ac5[];   // "PRESS A KEY...", "HOLD"
extern "C" const float rdata_140a44, rdata_140a48, rdata_140a4c, rdata_140a50, rdata_140a54, rdata_140a58;
static inline __attribute__((always_inline)) V4 grey(float k) { return nvg_rgba_f(k, k, k, 1.0f); }
extern "C" void draw_module_description(char *g, WT2 *w, void *cbArg, void *target, int key, unsigned char *hold, const R16c *r) {
    void *ig = w->ig;
    nvg_FontSize(ig, rdata_140a44);
    set_draw_color(w, 1);
    bool capturing = drag_owner_is(g, target);
    float tw;
    if (capturing) {
        tw = libc_ceilf(nvg_text_bounds(ig, 0.0f, 0.0f, rdata_140ab6, 0, (const float *)0));
    } else {
        tw = data_2f7490[(unsigned)key];
        if (tw == 0.0f) {
            tw = libc_ceilf(nvg_text_bounds(ig, 0.0f, 0.0f, mouse_button_name(key), 0, (const float *)0));
            data_2f7490[(unsigned)key] = tw;
        }
    }
    R16c btn = make_rect(r->x, r->y, tw + rdata_140a44, r->h);
    V4 bg = key ? nvg_trans_rgba_f(*(V4 *)(g + 0x360), rdata_140a48) : nvg_trans_rgba_f(grey(rdata_140a4c), rdata_140a48);
    nvg_FillColor(ig, &bg);
    draw_rect_rounded(w, &btn, rdata_140a50);
    V4 tc;
    if (mouse_over_rect_r(g, &btn)) tc = grey(rdata_140a54);
    else if (key) tc = *(V4 *)(g + 0x360);
    else tc = grey(rdata_140a4c);
    nvg_FillColor(ig, &tc);
    draw_text(w, btn, 1, 1, (void *)(capturing ? rdata_140ab6 : mouse_button_name(key)));
    if (clicked_inside_r_frame(g, btn)) begin_drag(g, cbArg, target);
    if (!key) {
        if (hold) *hold = 0;
        return;
    }
    if (!hold) return;
    float hw = libc_ceilf(nvg_text_bounds(ig, 0.0f, 0.0f, rdata_140ac5, 0, (const float *)0));
    R16c hb = make_rect(btn.x + btn.w + rdata_140a50, btn.y, hw + rdata_140a44, btn.h);
    V4 hc;
    if (*hold) {
        hc = nvg_trans_rgba_f(grey(rdata_140a58), rdata_140a48);
    } else {
        if (!mouse_over_rect_r(g, r)) return;
        hc = *hold == 1 ? nvg_trans_rgba_f(grey(rdata_140a58), rdata_140a48) : nvg_trans_rgba_f(grey(rdata_140a4c), rdata_140a48);
    }
    nvg_FillColor(ig, &hc);
    draw_rect_rounded(w, &hb, rdata_140a50);
    V4 ht = mouse_over_rect_r(g, &hb) ? grey(rdata_140a58) : grey(rdata_140a4c);
    nvg_FillColor(ig, &ht);
    draw_text(w, hb, 1, 1, (void *)rdata_140ac5);
    if (clicked_inside_r_frame(g, hb)) *hold ^= 1;
}
// labelled toggle row: label text, then the switch right of it
extern "C" const float rdata_140c6c, rdata_140c70, rdata_140c74, rdata_140c78, rdata_140c7c;
FN(R16c, make_rect, (float x, float y, float w, float h));
FN(bool, draw_animated_description_local, (char *g, WT2 *w, R16c r, Anim *an, unsigned char *val)) __asm__("draw_animated_description");
extern "C" bool draw_label(char *g, WT2 *w, const char *label, Anim *an, unsigned char *val) {
    void *ig = w->ig;
    R16c r = add_cell(g, rdata_140c6c);
    float k = rdata_140c70;
    V4 c = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &c);
    nvg_FontSize(ig, rdata_140c74);
    draw_text(w, r, 0, 0, (void *)label);
    return draw_animated_description_local(g, w, make_rect(r.x + r.w + rdata_140c78, r.y, rdata_140c7c, r.h), an, val);
}

// ---- float slider: label, track, animated fill + knob, drag/typing via draw_slider_track, value tooltip ----
struct SAnim { unsigned char active; char pad[3]; float pos; double last; };
FN(unsigned char, draw_slider_track, (char *g, R16c track, R16c hit, float lo, float hi, float step, float scale, SAnim *an,
                          float *val));
FN(float, limit_rotation_delta, (float lo, float hi, float x));   // clamp
FN(float, lerp_clamped_float, (float a, float b, float t));
FN(bool, dragged_item_is, (char *g, void *x));
FN(int, libc_snprintf, (char *buf, unsigned long long n, const char *fmt, ...));   // snprintf
extern "C" const float rdata_140c90, rdata_140c94, rdata_140c98, rdata_140c9c, rdata_140ca8, rdata_140cb8,
    rdata_140cbc, rdata_140cc0, rdata_140cc4, rdata_140cd0;
extern "C" const double rdata_140ca0, rdata_140cb0, rdata_140cc8;
extern "C" const char rdata_140e53[];   // "%0.2f"
extern "C" char data_2f7670[];
extern "C" unsigned char draw_slider_label(char *g, WT2 *w, const char *label, const char *fmt, SAnim *an, float lo, float hi,
                                  float step, float scale, float *val) {
    void *ig = w->ig;
    R16c r = add_cell(g, rdata_140c90);
    V4 tc = nvg_rgba_f(rdata_140c94, rdata_140c94, rdata_140c94, 1.0f);
    nvg_FillColor(ig, &tc);
    nvg_FontSize(ig, rdata_140c98);
    draw_text(w, r, 0, 0, (void *)label);
    float y = r.y + rdata_140c9c + rdata_140ca8;
    double half = (double)r.w * rdata_140ca0;
    float x0 = (float)((double)r.x + half);
    float tw = (float)(half + rdata_140cb0);
    R16c track = make_rect(x0, y, tw, 4.0f);
    R16c hr = r;
    float k = mouse_over_rect_r(g, &hr) ? rdata_140cbc : rdata_140cb8;
    V4 kc = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &kc);
    float rnd = rdata_140cc0;
    R16c tr = track;
    draw_rect_rounded(w, &tr, rnd);
    float t = limit_rotation_delta(0.0f, rdata_140cc4, (*val - lo) / (hi - lo));
    float pos = lerp_clamped_float(an->pos, t, (float)((double)(float)(w->t - an->last) / rdata_140cc8));
    an->pos = pos;
    an->last = w->t;
    float fx = lerp_unclamped_float(x0, tw + x0, pos);
    R16c fill = make_rect(x0, y, fx - x0, 4.0f);
    V4 acc = *(V4 *)(g + 0x360);
    nvg_FillColor(ig, &acc);
    draw_rect_rounded(w, &fill, rnd);
    nvg_BeginPath(ig);
    nvg_Circle(ig, fx, rnd + y, rdata_140cd0);
    nvg_Fill(ig);
    unsigned char res = draw_slider_track(g, track, r, lo, hi, step, scale, an, val);
    if (!an->active) {
        R16c h2 = r;
        if (!mouse_over_rect_r(g, &h2) || g[0x4c8]) return res;
    }
    const char *text;
    if (dragged_item_is(g, val)) {
        text = g + 0x378;
    } else {
        float v = *val;
        float shown = scale != 0.0f ? scale * v : v;
        text = data_2f7670;
        libc_snprintf(data_2f7670, 0x40, fmt ? fmt : rdata_140e53, (double)shown);
    }
    g[0x268] = 1;
    libc_memcpy(g + 0x26a, text, 0x64);
    *(float *)(g + 0x2d0) = (float)(int)fx;
    *(float *)(g + 0x2d4) = (float)(int)y;
    return res;
}
