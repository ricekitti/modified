// Mod Overlay - the enabled-modules list in the screen corner.
//   0x34ce0 rebuild visibility from the module list
//   0x34080 tick: refresh names / suffixes when the filter settings change
//   0x34290 module toggled: add / show / hide its entry
//   0x34510 render: animate, measure, sort and draw the entries
//   0x34e90 append an entry (the image's import loads here are corrupted; the tolower
//           calls are done inline instead)
#include "slinky/engine.h"

struct Module {
    const char *name;
    char pad8[8];
    int category;                   // 0 combat, 1 move, 2 visual, 3 block, 4 utility
    unsigned char enabled;
    char pad15[3];
    int flags;                      // 4: hidden from the overlay
    int key;
    char pad20[0x1e8];
    void (*suffix)(char *out);
};
struct ModList { Module **mods; unsigned long long count; };
struct Entry {                      // 0x50 bytes at 0x2fb000
    Module *mod;
    char text[0x14];
    char suffix[0x20];
    float w, h;
    float anim;
    unsigned char alive;
    char pad49[7];
};
struct ToggleEvt { char pad0[8]; Ctx *ctx; Module *mod; unsigned char on; };
struct Ui { void *dl; char pad8[0x10]; float width; };
struct RenderEvt { char pad0[0x10]; Ui *ui; float dt; };
struct Color { float r, g, b, a; };
struct Measure { char pad0[8]; float w, h; };

struct FlagHook { char pad0[0x10]; unsigned flags; };

extern "C" unsigned char data_2ed1b8;
extern "C" unsigned char data_2faff4;
extern "C" unsigned char data_2faff5;    // animating
extern "C" float data_2fbfa0;            // fade
extern "C" unsigned data_2faff0;         // filter settings last applied
extern "C" unsigned long long data_2faff8;   // entry count
extern "C" Entry data_2fb000[];
extern "C" char data_2ecef0[];           // Mod Overlay module descriptor
extern "C" unsigned char data_2ed118, data_2ed138, data_2ed158, data_2ed178, data_2ed198;   // show per category
extern "C" unsigned char data_2ed1d8;    // lowercase
extern "C" unsigned char data_2ed1f8;    // show details
extern "C" float data_2ed230;            // font scale
extern "C" float data_2ed240, data_2ed244, data_2ed248;   // name colour
extern "C" float data_2ed260, data_2ed264, data_2ed268;   // suffix colour
extern "C" float data_2ed2a8;            // corner rounding
extern "C" float data_2ed2e0;            // horizontal padding
extern "C" float data_2ed318;            // vertical padding
extern "C" const double rdata_151c60;
extern "C" const float rdata_151c68, rdata_151c6c;
S(151cfc);   // "%s"

FN(void, mod_overlay_rebuild_visibility_module, (void *ctx));
FN(void, mod_overlay_append_entry_image, (Module *m));
FN(unsigned long long, libc_strlen, (const char *s));
FN(void, libc_memmove, (void *dst, const void *src, unsigned long long n));
FN(void, libc_qsort, (void *base, unsigned long long n, unsigned long long size, int (*cmp)(const void *, const void *)));
FN(int, compare_height_desc, (const void *a, const void *b));
FN(void, nvg_FontSize, (void *dl, float scale));
FN(void, nvg_TextAlign, (void *dl, int font));
FN(void, set_draw_color, (Ui *ui, int flags));
FN(void, nvg_text_bounds, (void *dl, float size, float wrap, const char *text, void *end, Measure *out));
FN(Color, nvg_rgba_f, (float r, float g, float b, float a));
FN(Color, nvg_rgb_f, (float r, float g, float b));
FN(float, ease_curve, (float t));
FN(void, nvg_GlobalAlpha, (void *dl, float alpha));
FN(void, nvg_FillColor, (void *dl, Color c));
FN(void, nvg_BeginPath, (void *dl));
FN(void, nvg_Fill, (void *dl));
FN(void, nvg_RoundedRectVarying, (void *dl, float x, float y, float w, float h, int a, int b, int c, float rounding));
FN(float, nvg_text, (void *dl, float x, float y, const char *text, void *end));

static inline __attribute__((always_inline)) char lower(char ch) {
    return ch >= 'A' && ch <= 'Z' ? ch + ('a' - 'A') : ch;
}

static bool category_shown(int cat) {
    switch (cat) {
    case 0: return data_2ed118;
    case 1: return data_2ed138;
    case 2: return data_2ed178;
    case 3: return data_2ed158;
    case 4: return data_2ed198 == 1;
    default: return true;
    }
}

static bool shown(Module *m) {
    if (m->flags & 4)
        return false;
    if (data_2ed1b8 == 1 && !m->key)
        return false;
    return category_shown(m->category);
}

static Entry *find(Module *m) {
    for (unsigned long long i = 0; i < data_2faff8; i++)
        if (data_2fb000[i].mod == m)
            return &data_2fb000[i];
    return nullptr;
}

extern "C" void mod_overlay_part(FlagHook *h) {
    unsigned f = h->flags;
    h->flags = f | 1;
    if (data_2faff5 || data_2fbfa0 > 0.0f)
        h->flags = f | 3;
}

extern "C" void mod_overlay_part_2(FlagHook *h) {
    if (h->flags == 0)
        data_2faff4 = 1;
}

extern "C" void mod_overlay_part_3(Hook *h) {
    if (data_2ed1b8 == 1) {
        mod_overlay_rebuild_visibility_module(h->ctx);
        data_2faff5 = 1;
    }
}

extern "C" void mod_overlay_rebuild_visibility_module_2(ModList *list) {
    for (unsigned long long i = 0; i < data_2faff8; i++)
        if (!shown(data_2fb000[i].mod))
            data_2fb000[i].alive = 0;
    for (unsigned long long i = 0; i < list->count; i++) {
        Module *m = list->mods[i];
        if (m->enabled != 1 || !shown(m))
            continue;
        if (Entry *e = find(m))
            e->alive = 1;
        else
            mod_overlay_append_entry_image(m);
    }
}

extern "C" void mod_overlay_tick(Hook *h) {
    Ctx *c = h->ctx;
    unsigned mask = data_2ed118 | data_2ed138 << 1 | data_2ed158 << 2 | data_2ed178 << 3 | data_2ed198 << 4 | data_2ed1b8 << 5;
    unsigned char dirty = data_2faff4;
    if (data_2faff0 != mask || (dirty & 1)) {
        mod_overlay_rebuild_visibility_module(c->p08);
        data_2faff0 = mask;
        data_2faff5 = 1;
        data_2faff4 = 0;
    }
    if (!(dirty & 1) && c->f40)
        return;
    for (unsigned long long i = 0; i < data_2faff8; i++) {
        Entry *e = &data_2fb000[i];
        Module *m = e->mod;
        if (data_2ed1d8 & 1) {
            unsigned long long n = libc_strlen(m->name);
            for (unsigned long long j = 0; j < n; j++)
                e->text[j] = lower(m->name[j]);
            e->text[n] = 0;
        } else {
            log_fmt(e->text, 0x14, rdata_151cfc, m->name);
        }
        if (!m->suffix || !data_2ed1f8) {
            e->suffix[0] = 0;
            continue;
        }
        m->suffix(e->suffix);
        if (data_2ed1d8 == 1)
            for (unsigned long long j = 0; j < libc_strlen(e->suffix); j++)
                e->suffix[j] = lower(e->suffix[j]);
    }
    data_2faff5 = 1;
}

extern "C" void mod_overlay_module_toggled(ToggleEvt *h) {
    Module *m = h->mod;
    data_2fbfa0 = 0.2f;
    if ((void *)m == data_2ecef0) {             // the overlay itself: rebuild from scratch
        ModList *list = (ModList *)h->ctx->p08;
        data_2faff8 = 0;
        if (h->on != 1)
            return;
        for (unsigned long long i = 0; i < list->count; i++) {
            Module *o = list->mods[i];
            if (o->enabled == 1 && shown(o))
                mod_overlay_append_entry_image(o);
        }
        data_2faff5 = 1;
        return;
    }
    if (Entry *e = find(m)) {
        if (!h->on)
            e->alive = 0;
        else if (shown(m))
            e->alive = 1;
        return;
    }
    if (h->on != 1 || !shown(m))
        return;
    mod_overlay_append_entry_image(m);
    data_2faff5 = 1;
}

extern "C" void mod_overlay_render(RenderEvt *h) {
    Ui *ui = h->ui;
    void *dl = ui->dl;
    nvg_FontSize(dl, data_2ed230);
    set_draw_color(ui, 0);
    if (data_2fbfa0 > 0.0f)
        data_2fbfa0 -= h->dt;
    double up = (double)h->dt * rdata_151c60;
    double down = -(double)h->dt * rdata_151c60;
    unsigned long long n = data_2faff8;
    for (unsigned long long i = 0; i < n; i++) {
        Entry *e = &data_2fb000[i];
        if (e->alive == 1) {
            if (e->anim != rdata_151c68) {
                float a = (float)(e->anim + up);
                e->anim = rdata_151c68 < a ? rdata_151c68 : a;
            }
            continue;
        }
        float a = (float)(e->anim + down);

        e->anim = a;
        if (a > 0.0f)
            continue;
        data_2faff8 = n - 1;                    // faded out: drop the entry
        libc_memmove(e, e + 1, (n - 1 - i) * sizeof(Entry));
        n -= 1;
    }
    if (data_2faff5 == 1) {
        nvg_TextAlign(dl, 9);
        Measure m;
        for (unsigned long long i = 0; i < data_2faff8; i++) {
            Entry *e = &data_2fb000[i];
            nvg_text_bounds(dl, 0.0f, 0.0f, e->text, nullptr, &m);
            e->w = m.w;
            e->h = m.h;
            if (!e->suffix[0])
                continue;
            nvg_text_bounds(dl, 0.0f, 0.0f, e->suffix, nullptr, &m);
            e->w = m.w + rdata_151c6c + e->w;
        }
        libc_qsort(data_2fb000, data_2faff8, sizeof(Entry), compare_height_desc);
        data_2faff5 = 0;
    }
    nvg_TextAlign(dl, 9);
    Color bg = nvg_rgba_f(0.0f, 0.0f, 0.0f, 0.7f);
    Color name = nvg_rgb_f(data_2ed240, data_2ed244, data_2ed248);
    Color detail = nvg_rgb_f(data_2ed260, data_2ed264, data_2ed268);
    float width = ui->width;
    float y = 0.0f;
    for (unsigned long long i = 0; i < data_2faff8; i++) {
        Entry *e = &data_2fb000[i];
        float t = e->anim == rdata_151c68 ? rdata_151c68 : ease_curve(e->anim);
        nvg_GlobalAlpha(dl, t);
        float round = data_2ed2a8;
        if (i + 1 < data_2faff8) {
            float d = e->w - data_2fb000[i + 1].w;
            round = d < round ? d : round;
        }
        nvg_FillColor(dl, bg);
        nvg_BeginPath(dl);
        float pad = data_2ed2e0 + data_2ed2e0;
        nvg_RoundedRectVarying(dl, width - pad - e->w * t, y, pad + e->w, data_2ed318 + data_2ed318 + e->h, 0, 0, 0, round);
        nvg_Fill(dl);
        nvg_FillColor(dl, name);
        float ty = data_2ed318 + y;
        float end = nvg_text(dl, width - data_2ed2e0 - e->w * t, ty, e->text, nullptr);
        if (e->suffix[0]) {
            nvg_FillColor(dl, detail);
            nvg_text(dl, end + rdata_151c6c, ty, e->suffix, nullptr);
        }
        y += t * (data_2ed318 + data_2ed318 + e->h);
    }
    nvg_GlobalAlpha(dl, rdata_151c68);
}

extern "C" void mod_overlay_append_entry_image_2(Module *m) {
    if (data_2faff8 >= 50)                // data_2fb000 is exactly 50 * sizeof(Entry) bytes
        return;
    Entry *e = &data_2fb000[data_2faff8];
    e->mod = m;
    e->anim = 0.0f;
    e->alive = 1;
    if (data_2ed1d8) {
        unsigned long long n = libc_strlen(m->name);
        if (n > 0x13) n = 0x13;           // text is 0x14 bytes including the terminator
        for (unsigned long long j = 0; j < n; j++)
            e->text[j] = lower(m->name[j]);
        e->text[n] = 0;
    } else {
        log_fmt(e->text, 0x14, rdata_151cfc, m->name);
        m = e->mod;
    }
    if (m->suffix && data_2ed1f8) {
        m->suffix(e->suffix);
        if (data_2ed1d8 == 1 && e->suffix[0])
            for (unsigned long long j = 0; j < libc_strlen(e->suffix); j++)
                e->suffix[j] = lower(e->suffix[j]);
    } else {
        e->suffix[0] = 0;
    }
    data_2faff8 += 1;
}
