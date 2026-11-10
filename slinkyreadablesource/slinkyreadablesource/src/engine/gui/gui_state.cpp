// 0x11000: GUI state helpers (drag state at +0x370, flags at +0x4c8..)
#include "slinky/engine.h"
#include "slinky/hud_editor.h"
#include "slinky/gui_viewport.h"
static bool contentViewportActive=false;
static float contentViewportHeight=0;
static bool outsideContent(char* g){return contentViewportActive&&!guiviewport::contains(*(float*)(g+0x4ac)-*(float*)(g+0x4bc),contentViewportHeight);}
extern "C" __attribute__((noinline)) void mark_click_consumed(char *g) {
    if (g[0x4c8] == 1 && g[0x4c9] == 0) {  g[0x4c9] = 1; }

}
extern "C" void begin_drag(char *g, void *a, void *b) {
    *(int *)(g + 0x370) = 1;
    *(void **)(g + 0x378) = a;
    *(void **)(g + 0x380) = b;
}
extern "C" bool drag_owner_is(char *g, void *b) {
    if (*(int *)(g + 0x370) != 1) return false;
    return *(void **)(g + 0x380) == b;
}
extern "C" void end_drag(char *g) {
    if (*(int *)(g + 0x370) == 2) *(int *)(g + 0x370) = 3;
}
extern "C" void *dragged_payload(char *g) { return (*(unsigned *)(g + 0x370) & ~1u) == 2 ? g + 0x378 : 0; }
extern "C" void request_unload(char *g) { g[0x4cb] = 1; }
extern "C" void set_tab_animation(char *g, double t) {
    *(int *)(g + 0x4b8) = 0;
    *(double *)(g + 0x498) = t;
}

extern "C" bool dragged_item_is(char *g, void *x) {
    if ((*(unsigned *)(g + 0x370) & ~1u) != 2) return false;
    return *(void **)(g + 0x3e8) == x;
}
extern "C" __attribute__((noinline)) void *take_dragged_item(char *g) {
    if ((*(unsigned *)(g + 0x370) & ~1u) != 2 || g[0x3f0] != 1) return 0;
    void *r = g + 0x378;
    *(int *)(g + 0x370) = 0;
    g[0x3f0] = 0;
    return r;
}

struct R16b { long long a, b; };
FN(bool, popup_is_open, (char *g));
FN(unsigned char, popup_is_animating, (char *g));
FN(bool, point_in_rect, (R16b r, unsigned long long mouse));
// is the mouse over rect r (and no drag in progress)?
extern "C" __attribute__((noinline)) bool mouse_over_rect_r(char *g, const R16b *r) {
    if (outsideContent(g)) return false;
    if (popup_is_open(g) && !popup_is_animating(g)) return false;
    if (!point_in_rect(*r, *(unsigned long long *)(g + 0x4a8))) return false;
    int st = *(int *)(g + 0x370);
    return (st == 0) | (st == 3);
}
// clicked inside r this frame (consumes the click)
extern "C" bool clicked_inside_r_frame(char *g, R16b r) {
    R16b c = r;

    if (outsideContent(g)) return false;
    if (popup_is_open(g) && !popup_is_animating(g)) return false;
    if (!point_in_rect(c, *(unsigned long long *)(g + 0x4a8))) return false;
    int st = *(int *)(g + 0x370);
    if (st != 3 && st != 0) return false;
    if (g[0x4c8] != 1 || g[0x4c9] != 0) return false;
    g[0x4c9] = 1;
    return true;
}
// start dragging item b (owner a)
extern "C" void start_dragging_item_b(char *g, void *a, void *b) {
    if ((*(unsigned *)(g + 0x370) & ~1u) == 2 && *(void **)(g + 0x3e8) == b) return;
    *(int *)(g + 0x370) = 2;
    __builtin_memset(g + 0x378, 0, 0x80);
    *(void **)(g + 0x3e0) = a;
    *(void **)(g + 0x3e8) = b;
}

// ---- category tab bar: 7 tabs with a sliding highlight; tab kind 6 = "Unload Slinky?" ----
struct R16t { float x, y, w, h; };
struct V4t { float x, y, z, w; };
struct WT3 { void *ig; double t; };
FN(R16t, add_cell, (char *g, float h));
FN(V4t, nvg_rgba_f, (float r, float g, float b, float a));
FN(void, nvg_FillColor, (void *ig, V4t *c));
FN(void, draw_rect_rounded, (WT3 *w, const R16t *r, float round));
FN(float, lerp_clamped_float, (float a, float b, float t));
FN(R16t, make_rect, (float x, float y, float w, float h));
FN(R16t, inset_rect, (R16t r, float d));
FN(void, nvg_FontSize, (void *ig, float size));
FN(unsigned char, clicked_inside_r_frame_local, (char *g, R16t r)) __asm__("clicked_inside_r_frame");
FN(float, draw_text, (WT3 *w, R16t r, int align, int col, void *text));
FN(void, confirmation_popup_formatted_message, (void *g, void *a, void *b, const char *fmt, ...));
FN(void, request_unload, (char *g));
extern "C" const float rdata_140880, rdata_140884, rdata_140888, rdata_14088c, rdata_140898, rdata_14089c, rdata_1408a0,
    rdata_1408a4, rdata_1408a8, rdata_1408ac, rdata_1408b0, rdata_1408b4, rdata_1408b8;
extern "C" const double rdata_140890;
extern "C" const char rdata_140a6c[];   // "Unload Slinky?"
struct Tab { int kind; int pad; const char *label; };
extern "C" void draw_tab_bar(char *g, WT3 *w, float dt) {
    void *ig = w->ig;
    R16t bar = add_cell(g, rdata_140880);
    float k = rdata_140884;
    V4t c = nvg_rgba_f(k, k, k, 1.0f);
    nvg_FillColor(ig, &c);
    draw_rect_rounded(w, &bar, rdata_140888);
    float tw = bar.w / rdata_14088c;
    float pos = lerp_clamped_float(*(float *)(g + 0x358), (float)*(unsigned long long *)(g + 0x350),
                        (float)((double)dt / rdata_140890));
    *(float *)(g + 0x358) = pos;
    float inset = rdata_140898;
    R16t sel = inset_rect(make_rect(bar.x + pos * tw, bar.y, tw, bar.h), inset);
    float ks = rdata_14089c;
    V4t sc = nvg_rgba_f(ks, ks, ks, 1.0f);
    nvg_FillColor(w->ig, &sc);
    draw_rect_rounded(w, &sel, rdata_1408a0);
    nvg_FontSize(ig, rdata_1408a4);
    Tab *tab = (Tab *)(g + 0x2e0);
    for (unsigned long long i = 0; i != 7; i++, tab++) {
        float cw = bar.w / rdata_14088c;
        R16t tr = make_rect((float)(long long)i * cw + bar.x, bar.y, cw, bar.h);
        if (clicked_inside_r_frame_local(g, tr)) {
            if (tab->kind == 6) {
                confirmation_popup_formatted_message(g, g, (void *)request_unload, rdata_140a6c);
            } else {
                *(unsigned long long *)(g + 0x350) = i;
                *(int *)(g + 0x370) = 0;
            }
        }
        R16t in = inset_rect(tr, inset);
        V4t tc;
        bool hov = mouse_over_rect_r(g, (const R16b *)&in);
        if (tab->kind == 6) tc = hov ? nvg_rgba_f(rdata_1408b4, rdata_1408b8, rdata_1408b8, 1.0f)
                                     : nvg_rgba_f(rdata_1408a8, rdata_1408b0, rdata_1408b0, 1.0f);
        else tc = hov ? nvg_rgba_f(rdata_1408ac, rdata_1408ac, rdata_1408ac, 1.0f)
                      : nvg_rgba_f(rdata_1408a8, rdata_1408a8, rdata_1408a8, 1.0f);
        nvg_FillColor(w->ig, &tc);
        draw_text(w, in, 1, 0, (void *)tab->label);
    }
}

// ---- click GUI frame: window rect + open animation, scroll, tab bar, category page (module cards or the
// profile list), toast, modal popup ----
struct GWin { void *ig; double t; void *p10; float w, h, scale; };
struct R2f { float x, y; };
struct Prof { const char *name, *end; char desc[0x28]; int key; int pad; };
struct GEv { int type, pad; union { void *p; int i; } a, b; char rest[0x30]; };
typedef void (*SetBindF)(void *mod, int key);
typedef int v4i11 __attribute__((vector_size(16)));
typedef float v4f11 __attribute__((vector_size(16)));
FN(void, nvg_Save, (void *ig));
FN(void, nvg_Restore, (void *ig));
FN(void, nvg_GlobalAlpha, (void *ig, float a));
FN(void, nvg_Translate, (void *ig, float x, float y));
FN(void, nvg_Scale, (void *ig, float x, float y));
FN(R2f, rot_scale, (R2f a, float s));
FN(void, card_open_animation, (GWin *w, float t));
FN(void, reset_layout_columns, (char *g));
FN(void, push_layout_row, (char *g, R16t r, float sp, float pad));
FN(void, pop_layout_row, (char *g, int n));
FN(void, draw_module_cards, (char *g, GWin *w, void *mgr, unsigned cat, float dt));
FN(int, load_profiles, (void *profiles));
FN(void, dispatch_event, (void *mgr, GEv *e));
FN(void, choose_columns, (char *g, float w));
FN(Prof *, profile_array, (void *profiles, unsigned long long *n));
FN(bool, rects_intersect, (R16t a, R16t b));
FN(void, begin_content, (GWin *w, char *g, R16t r, float pad, float f));
FN(float, draw_text_sized, (GWin *w, R16t r, unsigned align, unsigned col, const char *text, const char *end));
FN(SetBindF, profile_set_bind, (void *profiles));
FN(void, draw_module_description, (char *g, GWin *w, SetBindF cb, void *target, int key, unsigned char *hold, R16t r));
FN(void, ask_sure_before_running_action, (char *g, void *mgr, void *profiles, const char *name, int kind));
FN(void, draw_texture, (GWin *w, R16t r, unsigned icon, float alpha));
FN(void, set_slider_text, (char *g, GWin *w, const char *tip, int x, int y));
FN(bool, get_profile_dirty, ());
FN(const char *, get_saved_string, ());
FN(int, libc_strcmp, (const char *a, const char *b));   // strcmp
FN(int, load_profile_name, (const char *name, void *mgr));       // load profile
FN(int, save_profile, (const char *name, bool text, void *mgr));   // save profile
FN(V4t, nvg_trans_rgba_f, (V4t c, float k));
FN(void, file_name_text_filter, (char *buf, char c));
FN(bool, drag_item, (char *g, GWin *w, R16t r, void (*filter)(char *, char), void *b));
FN(void, draw_tracked_text, (char *g, GWin *w, const R16t *r));
FN(float, end_layout_row, (GWin *w, char *g));
FN(float, ease_curve, (float t));
FN(float, nvg_text_bounds, (void *ig, float x, float y, const char *text, const char *end, const float *bounds));   // nvgTextBounds
FN(void, close_popup_animated, (char *g, GWin *w));
FN(bool, mouse_over_rect_r_local, (char *g, R16t r)) __asm__("mouse_over_rect_r");
FN(void, draw_rect_rounded_local, (WT3 *w, R16t r, float round)) __asm__("draw_rect_rounded");
extern "C" const float rdata_1408c0, rdata_1408c4, rdata_1408c8, rdata_1408cc, rdata_1408d0, rdata_1408f8,
    rdata_1408fc, rdata_140920, rdata_140924, rdata_140928, rdata_14092c, rdata_140930, rdata_140934, rdata_140940,
    rdata_140944, rdata_140948, rdata_140964, rdata_140988, rdata_14098c, rdata_140990, rdata_140994, rdata_140998;
extern "C" const double rdata_1408f0, rdata_140910, rdata_140918, rdata_140938, rdata_140950, rdata_140970,
    rdata_140978, rdata_140980;
extern "C" const v4i11 rdata_1408e0, rdata_140900;   // sign / abs masks
extern "C" const char rdata_140aca[], rdata_140b00[], rdata_140b65[], rdata_140b87[], rdata_140c06[], rdata_140c1d[];
extern "C" const int rdata_140ad4[];   // tooltip strings as offsets from the table ("Click to load", ...)
extern "C" const unsigned rdata_140af0[];   // button icons
static inline __attribute__((always_inline)) float mask11(float x, v4i11 m, bool x_or) {
    v4f11 v = {x, 0, 0, 0};
    return ((v4f11)(x_or ? ((v4i11)v ^ m) : ((v4i11)v & m)))[0];
}
static inline __attribute__((always_inline)) void prof_event(void *mgr, int type, GEv *ev) {
    ev->type = type;
    dispatch_event(mgr, ev);
}
FN(void, nvg_Scissor, (void *ig, float x, float y, float w, float h));
extern "C" void draw_gui_frame(char *g, GWin *w, void *mgr, void *profiles) {
    if(hud_editor_frame(g,w,mgr,profiles))return;
    void *ig = w->ig;
    nvg_Save(ig);
    card_open_animation(w, *(float *)(g + 0x4b8));
    float ww = w->w;
    float pw = rdata_1408c0 + ww;
    float lim = g[0x4cd] ? rdata_1408c4 : rdata_1408c8;
    pw = pw < lim ? pw : lim;
    float px = (ww - pw) * rdata_1408cc;
    float *scroll = (float *)(g + 0x4bc);
    float s = *scroll;
    if (0.0f > s) *scroll = 0.0f;
    else if (s > *(float *)(g + 0x4c0)) *scroll = *(float *)(g + 0x4c0);
    float one = rdata_1408d0;
    R2f m = rot_scale(*(R2f *)(g + 0x4b0), one / w->scale);
    *(R2f *)(g + 0x4a8) = m;
    *(float *)(g + 0x4ac) = m.y + *scroll;
    nvg_Translate(w->ig, 0.0f, mask11(*scroll, rdata_1408e0, true));
    reset_layout_columns(g);
    R16t win = make_rect(px, 0.0f, pw, w->h);
    float dt = (float)(w->t - *(double *)(g + 0x498));
    double dtd = dt;
    float op = (float)((double)*(float *)(g + 0x4b8) + dtd / rdata_1408f0);
    *(float *)(g + 0x4b8) = one < op ? one : op;
    contentViewportHeight=w->h;
    contentViewportActive=true;
    nvg_Save(ig);
    nvg_Scissor(ig,px,*scroll,pw,guiviewport::contentHeight(w->h));
    push_layout_row(g, make_rect(px,0.0f,pw,guiviewport::contentHeight(w->h)), rdata_1408f8, rdata_1408fc);
    pop_layout_row(g, 1);
    draw_tab_bar(g, (WT3 *)w, dt);
    unsigned long long cat = *(unsigned long long *)(g + 0x350);
    float off = (float)cat - *(float *)(g + 0x358);
    if (off != 0.0f) {   // page slides while the tab highlight moves
        void *sig = w->ig;
        float d = mask11(off, rdata_140900, false);
        nvg_GlobalAlpha(sig, rdata_1408cc < d ? 0.0f : one - (d + d));
        nvg_Translate(sig, (float)((double)off * ((double)w->w * rdata_140910)), 0.0f);
        cat = *(unsigned long long *)(g + 0x350);
    }
    switch (*(unsigned *)(g + 0x2e0 + cat * 16)) {
    case 0: draw_module_cards(g, w, mgr, 0, dt); break;
    case 4: draw_module_cards(g, w, mgr, 4, dt); break;
    case 2: draw_module_cards(g, w, mgr, 2, dt); break;
    case 3: draw_module_cards(g, w, mgr, 3, dt); break;
    case 1: draw_module_cards(g, w, mgr, 1, dt); break;
    case 5: {   // profiles
        if (*(int *)(g + 0x370) == 0 && w->t > *(double *)(g + 0x4a0) + rdata_140918) {
            int e = load_profiles(profiles);
            if (e) {
                GEv ev = {};
                ev.a.i = e;
                prof_event(mgr, 0xc, &ev);
            }
            *(double *)(g + 0x4a0) = w->t;
        }
        void *pig = w->ig;
        choose_columns(g, rdata_140920);
        unsigned long long n;
        Prof *list = profile_array(profiles, &n);
        for (unsigned long long i = 0; i <= n; i++) {
            R16t row = add_cell(g, *(float *)(g + 0x4c4));
            if (!rects_intersect(make_rect(0.0f, *(float *)(g + 0x4bc), w->w, guiviewport::contentHeight(w->h)), row)) continue;
            Prof *ent = i ? &list[i - 1] : 0;
            V4t bg = nvg_rgba_f(rdata_140924, rdata_140924, rdata_140924, 1.0f);
            nvg_FillColor(pig, &bg);
            draw_rect_rounded_local((WT3 *)w, row, rdata_140928);
            begin_content(w, g, row, rdata_1408f8, 12.0f);
            pop_layout_row(g, 1);
            void *tig = w->ig;
            R16t t = add_cell(g, rdata_140928);
            nvg_FontSize(tig, rdata_14092c);
            V4t tc = nvg_rgba_f(rdata_140930, rdata_140930, rdata_140930, 1.0f);
            nvg_FillColor(tig, &tc);
            if (ent) {
                float tw = draw_text_sized(w, t, 0, 0, ent->name, ent->end);
                R16t br = make_rect(tw + rdata_140934, t.y, t.w - tw + t.x, t.h);
                draw_module_description(g, w, profile_set_bind(profiles), ent, ent->key, 0, br);
            } else {
                draw_text_sized(w, t, 0, 0, rdata_140aca /* "Default" */, 0);
            }
            for (unsigned j = 0; j != 4; j++) {   // load, save, open folder, delete
                R16t b = make_rect(t.x + t.w - (float)(int)(20 * (4 - j)) - (float)(int)(12 * (3 - j)), t.y,
                                 rdata_140928, 20.0f);
                float alpha = rdata_1408cc;
                if (ent || !j) {
                    alpha = one;
                    if (mouse_over_rect_r_local(g, b)) {
                        set_slider_text(g, w, (const char *)rdata_140ad4 + rdata_140ad4[j],
                                 (int)((double)b.x + (double)b.w * rdata_140938), (int)b.y);
                        if (clicked_inside_r_frame_local(g, b)) {
                            const char *name = ent ? ent->name : 0;
                            int r;
                            const char *msg;
                            switch (j) {
                            case 0:
                                if (get_profile_dirty()) {
                                    ask_sure_before_running_action(g, mgr, profiles, name, 0);
                                    goto icon;
                                }
                                r = load_profile_name(name, mgr);
                                goto result;
                            case 1:
                                if (!ent) {
                                    r = 0x1773;
                                    msg = rdata_140b87;   // "Default profile cannot be overwritten"
                                    goto error;
                                }
                                if (get_saved_string() && libc_strcmp(name, get_saved_string()) == 0) {
                                    r = save_profile(name, true, mgr);
                                    goto result;
                                }
                                ask_sure_before_running_action(g, mgr, profiles, name, 1);
                                goto icon;
                            case 2:
                                ask_sure_before_running_action(g, mgr, profiles, name, 2);
                                goto icon;
                            case 3:
                                if (ent) {
                                    ask_sure_before_running_action(g, mgr, profiles, name, 3);
                                    goto icon;
                                }
                                r = 0x1774;
                                msg = rdata_140b65;   // "Default profile cannot be deleted"
                                goto error;
                            }
                        result:
                            if (!r) {
                                GEv ev = {};
                                ev.a.p = (void *)name;
                                ev.b.i = (int)j;
                                prof_event(mgr, 0xb, &ev);
                                *(double *)(g + 0x4a0) = -1.0;
                                goto icon;
                            }
                            msg = r == 0x1774 ? rdata_140b65 : r == 0x1773 ? rdata_140b87 : 0;
                        error : {
                            GEv ev = {};
                            ev.a.i = r;
                            ev.b.p = (void *)msg;
                            prof_event(mgr, 0xc, &ev);
                        }
                        }
                    }
                }
            icon:
                draw_texture(w, b, rdata_140af0[j], alpha);
            }
            if (!g[0x4cc]) {   // description line
                R16t d = add_cell(g, rdata_140928);
                nvg_FontSize(tig, rdata_140940);
                V4t dc = nvg_rgba_f(rdata_140944, rdata_140944, rdata_140944, 1.0f);
                nvg_FillColor(tig, &dc);
                draw_text((WT3 *)w, d, 0, 0, ent ? (void *)ent->desc : (void *)rdata_140b00);
            }
            *(float *)(g + 0x4c4) = end_layout_row(w, g);
        }
        // "CREATE NEW PROFILE": a text field once clicked
        R16t cr = add_cell(g, *(float *)(g + 0x4c4));
        void *cig = w->ig;
        V4t c0 = nvg_rgba_f(rdata_140924, rdata_140924, rdata_140924, 1.0f);
        V4t c1 = nvg_trans_rgba_f(c0, rdata_1408cc);
        nvg_FillColor(cig, &c1);
        draw_rect_rounded_local((WT3 *)w, cr, rdata_140928);
        if (drag_item(g, w, cr, file_name_text_filter, 0)) {
            nvg_FontSize(cig, rdata_14092c);
            V4t wc = nvg_rgba_f(one, one, one, 1.0f);
            nvg_FillColor(cig, &wc);
            R16t ir = {cr.x, (float)((double)cr.h * rdata_140938 + (double)cr.y + rdata_140950), cr.w, rdata_140964};
            draw_tracked_text(g, w, &ir);
            const char *nm = (const char *)take_dragged_item(g);
            if (nm) {
                int r = save_profile(nm, false, mgr);
                GEv ev = {};
                if (!r) {
                    ev.a.p = (void *)nm;
                    ev.b.i = 1;
                    prof_event(mgr, 0xb, &ev);
                    *(double *)(g + 0x4a0) = -1.0;
                } else {
                    ev.a.i = r;
                    ev.b.p = r == 0x3f4 ? (void *)rdata_140c06 : 0;   // "Profile name is taken."
                    prof_event(mgr, 0xc, &ev);
                }
            }
        } else {
            nvg_FontSize(cig, rdata_140948);
            float k = mouse_over_rect_r_local(g, cr) ? one : rdata_140930;
            V4t tc2 = nvg_rgba_f(k, k, k, 1.0f);
            nvg_FillColor(cig, &tc2);
            draw_text((WT3 *)w, cr, 1, 1, (void *)rdata_140c1d);   // "CREATE NEW PROFILE"
        }
        break;
    }
    }
    end_layout_row(w, g);
    contentViewportActive=false;
    nvg_Restore(ig);
    if (g[0x268]) {   // toast at the top, fades in
        *(float *)(g + 0x2d8) = (float)((double)*(float *)(g + 0x2d8) + dtd / rdata_140970);
        void *tig = w->ig;
        nvg_Save(tig);
        nvg_Translate(tig, *(float *)(g + 0x2d0), *(float *)(g + 0x2d4));
        if (*(float *)(g + 0x2d8) > one) {
            *(float *)(g + 0x2d8) = 1.0f;
        } else {
            float e = ease_curve(*(float *)(g + 0x2d8));
            nvg_GlobalAlpha(tig, e);
            nvg_Scale(tig, e, e);
        }
        nvg_FontSize(tig, rdata_140940);
        float tw = nvg_text_bounds(tig, 0.0f, 0.0f, g + 0x26a, 0, (const float *)0);
        R16t tr = make_rect((float)((double)tw * rdata_140978 + rdata_140980), rdata_14098c, tw + rdata_140988, 20.0f);
        V4t bc = nvg_rgba_f(rdata_140990, rdata_140990, rdata_140990, 1.0f);
        nvg_FillColor(tig, &bc);
        draw_rect_rounded_local((WT3 *)w, tr, rdata_140934);
        V4t fc = nvg_rgba_f(rdata_140930, rdata_140930, rdata_140930, 1.0f);
        nvg_FillColor(tig, &fc);
        draw_text((WT3 *)w, tr, 1, 0, g + 0x26a);
        nvg_Restore(tig);
        g[0x268] = 0;
    } else {
        *(float *)(g + 0x2d8) = 0.0f;
    }
    nvg_Restore(ig);
    float mx = (float)*(int *)(g + 0x260) - guiviewport::contentHeight(w->h) + rdata_1408fc;
    *(float *)(g + 0x4c0) = 0.0f > mx ? 0.0f : mx;
    if (popup_is_open(g)) {   // modal popup over everything (unscrolled)
        nvg_Save(ig);
        *(float *)(g + 0x4ac) -= *(float *)(g + 0x4bc);
        card_open_animation(w, *(float *)(g + 0x418));
        reset_layout_columns(g);
        R16t pr = win;
        if (pr.w > rdata_140994) {
            pr.x = (float)((double)(pr.w + rdata_140998) * rdata_140938 + (double)pr.x);
            pr.w = rdata_140994;
        }
        push_layout_row(g, pr, rdata_1408f8, rdata_1408fc);
        close_popup_animated(g, w);
        end_layout_row(w, g);
        nvg_Restore(ig);
    }
    mark_click_consumed(g);
    g[0x4c9] = g[0x4c8];
    *(double *)(g + 0x498) = w->t;
    hud_editor_button(g,w);
}
