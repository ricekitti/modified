// Notifications - toast messages (ticker 0x351a0, renderer 0x354b0).
// Messages go into a 10-entry ring buffer (0x70 bytes each).
//   0x35960 profile loaded / saved   0x35a30 error   0x35af0 friend added / removed
#include "slinky/engine.h"

struct Module { const char *name; char pad8[8]; int category; int pad14; int flags; int pad1c; unsigned char hidden; };
struct ToggleEvt { char pad0[0x10]; Module *mod; unsigned char on; };
struct Recent { const char *name; int count; };
struct Note { int kind; char text[0x68]; float age; };     // 0x70 bytes
struct ProfileEvt { char pad0[8]; const char *name; unsigned status; };
struct ErrorEvt { char pad0[8]; int code; int pad; const char *msg; };
struct FriendEvt { char pad0[8]; unsigned char added; };

S(151f66);   // "%s %s"
S(151fb0);   // "Default"
S(151fe5);   // "%s"
S(151fe8);   // "Error code %d"
extern "C" const char *const rdata_151f90[2];   // "Loaded profile", "Saved profile"
extern "C" char data_2ed320[];                 // GUI module descriptor
extern "C" unsigned char data_2ed548, data_2ed568;            // hide flagged / hidden modules
extern "C" unsigned char data_2ed588, data_2ed5a8, data_2ed5c8, data_2ed5e8, data_2ed608;   // per category
extern "C" unsigned char data_2fbfb0;            // GUI hint shown
extern "C" Recent data_2fbfb8;                   // last enabled
extern "C" Recent data_2fbfc8;                   // last disabled
extern "C" int *const rdata_13e498;              // client config (+0x1c: GUI key)
S(151f02);
S(151f6c);   // "Enabled"
S(151f74);   // "Disabled"
S(151f7d);   // "%s %u mods"
S(151f88);   // "SLINKY"
struct Color { float r, g, b, a; };
struct Rect { float x, y, w, h; };
struct Ui { void *dl; };
struct TickHook { char pad0[0x10]; unsigned flags; float dt; };
struct DrawHook { char pad0[0x10]; Ui *ui; };
extern "C" unsigned char data_2ed628;    // only while the GUI is open
extern "C" const float rdata_151e48, rdata_151e60, rdata_151e64, rdata_151e70, rdata_151e88, rdata_151e8c,
                       rdata_151e90, rdata_151e94, rdata_151e98, rdata_151e9c, rdata_151ea0, rdata_151ea4,
                       rdata_151ea8, rdata_151eac, rdata_151eb0, rdata_151eb4, rdata_151eb8, rdata_151ebc, rdata_151ec0;
extern "C" const double rdata_151e50, rdata_151e58, rdata_151e68, rdata_151e78, rdata_151e80;
FN(void, nvg_FontSize, (void *dl, float v));
FN(void, nvg_GlobalAlpha, (void *dl, float alpha));
FN(float, ease_curve, (float t));
FN(Color, nvg_rgba_f, (float r, float g, float b, float a));
FN(Color, nvg_rgb_f, (float r, float g, float b));
FN(Color, get_speed_pair, ());
FN(Color, nvg_trans_rgba_f, (Color c, float mul));
FN(float, nvg_text_bounds, (void *dl, float size, float wrap, const char *text, void *end, void *extra));
FN(Rect, make_rect, (float x, float y, float w, float h));
FN(void, nvg_FillColor, (void *dl, Color c));
FN(void, draw_rect_rounded, (Ui *ui, Rect r, float rounding));
FN(void, draw_text, (Ui *ui, Rect r, int align, int flags, const char *text));   // "Press %s to open GUI"
FN(const char *, mouse_button_name, (int key));
extern "C" unsigned long long data_2fbfd8;      // count
extern "C" Note data_2fbfe0[10];
extern "C" unsigned long long data_2fc440;      // head

static inline __attribute__((always_inline)) Note *push(int kind) {
    unsigned long long head = data_2fc440, n = data_2fbfd8;
    Note *e = &data_2fbfe0[(n + head) % 10];
    e->kind = kind;
    e->age = 0;
    unsigned long long *slot = &data_2fbfd8, v = n;
    if (n == 10) {
        slot = &data_2fc440;
        v = head;
    }
    *slot = v + 1;
    return e;
}

extern "C" void notifications_profile_loaded_saved(ProfileEvt *p) {
    unsigned st = p->status;
    if (st == 2)
        return;
    const char *label = rdata_151f90[st];
    const char *name = p->name ? p->name : rdata_151fb0;
    Note *e = push(0);
    log_fmt(e->text, 0x65, rdata_151f66, label, name);
}

extern "C" void notifications_injected() {
    Note *e = push(1);
    log_fmt(e->text, 0x65, "%s", "Slinky injected successfully");
}

extern "C" void notifications_part(ErrorEvt *p) {
    Note *e = push(2);
    if (p->msg)
        log_fmt(e->text, 0x65, rdata_151fe5, p->msg);
    else
        log_fmt(e->text, 0x65, rdata_151fe8, p->code);
}

extern "C" void notifications_part_2(FriendEvt *p) {
    Note *e = push(1);
    if (p->added == 1)
        __builtin_memcpy(e->text, "Friend added", 13);
    else
        __builtin_memcpy(e->text, "Friend removed", 15);
}

// module toggled: first GUI open shows the key hint, other modules are counted
extern "C" void notifications_module_toggled_gui(ToggleEvt *h) {
    Module *m = h->mod;
    if ((void *)m == data_2ed320) {
        if (!h->on) {
            data_2fbfd8 = 0;
            goto end;
        }
        unsigned char expected = 0;
        if (!__atomic_compare_exchange_n(&data_2fbfb0, &expected, 1, false, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST))
            goto end;
        Note *e = push(1);
        char *text = e->text;

        log_fmt(text, 0x65, rdata_151f02, mouse_button_name(rdata_13e498[0x1c / 4]));
        goto end;
    }
    {
    int fl = m->flags;
    if (fl & 0x10)
        goto end;
    if ((fl & 2) && data_2ed548)
        goto end;
    if (m->hidden == 1 && data_2ed568)
        goto end;
    switch ((unsigned)m->category) {
    case 0: if (!data_2ed588) goto end; break;
    case 1: if (!data_2ed5a8) goto end; break;
    case 2: if (!data_2ed5e8) goto end; break;
    case 3: if (!data_2ed5c8) goto end; break;
    case 4: if (data_2ed608 != 1) goto end; break;
    }
    {
    Recent *r = h->on ? &data_2fbfb8 : &data_2fbfc8;
    r->name = m->name;
    r->count += 1;
    }
    }
end:

}

// tick: age the messages, drop expired ones, post the batched enable / disable messages
extern "C" void notifications_tick_age_messages(TickHook *h) {
    if (data_2ed628 == 1 && !((unsigned char *)rdata_13e498)[0x14]) {
        data_2fbfd8 = 0;
        data_2fbfb8.count = 0;
        data_2fbfc8.count = 0;
        return;
    }
    unsigned long long n = data_2fbfd8;
    while (n) {
        if (!(data_2fbfe0[data_2fc440 % 10].age > rdata_151e48))
            break;
        data_2fc440 += 1;
        data_2fbfd8 = --n;
    }
    if (data_2fbfb8.count) {
        Note *e = push(1);
        if (data_2fbfb8.count == 1)
            log_fmt(e->text, 0x65, rdata_151f66, rdata_151f6c, data_2fbfb8.name);
        else
            log_fmt(e->text, 0x65, rdata_151f7d, rdata_151f6c, data_2fbfb8.count);
        data_2fbfb8.count = 0;
        n = data_2fbfd8;
    }
    if (data_2fbfc8.count) {
        Note *e = push(1);
        if (data_2fbfc8.count == 1)
            log_fmt(e->text, 0x65, rdata_151f66, rdata_151f74, data_2fbfc8.name);
        else
            log_fmt(e->text, 0x65, rdata_151f7d, rdata_151f74, data_2fbfc8.count);
        data_2fbfc8.count = 0;
        n = data_2fbfd8;
    }
    if (!n)
        return;
    unsigned f = h->flags;
    h->flags = f | 1;
    float dt = h->dt;
    unsigned char animating = 0;
    for (unsigned long long i = data_2fc440; n; i++, n--) {
        Note *e = &data_2fbfe0[i % 10];
        float age = e->age + dt;
        e->age = age;
        animating |= (rdata_151e50 < (double)age) | ((double)age < rdata_151e58);
    }
    if (animating & 1)
        h->flags = f | 3;
}

// draw: slide-in boxes, one per message, coloured by kind
extern "C" void notifications_draw_slide_boxes(DrawHook *h) {
    if (!data_2fbfd8)
        return;
    if (data_2ed628 == 1 && !((unsigned char *)rdata_13e498)[0x14])
        return;
    Ui *ui = h->ui;
    void *dl = ui->dl;
    nvg_FontSize(dl, rdata_151e60);
    float y = rdata_151e64;
    const float x0 = rdata_151e64;
    for (unsigned long long i = 0; i < data_2fbfd8; i++) {
        Note *e = &data_2fbfe0[(data_2fc440 + i) % 10];
        double age = e->age;
        float a = rdata_151e70;
        if (rdata_151e68 > age)
            a = ease_curve((float)(age / rdata_151e68));
        else if (age > rdata_151e78)
            a = ease_curve((float)((rdata_151e80 - age) / rdata_151e68));
        float t = rdata_151e70 < a ? rdata_151e70 : a;
        nvg_GlobalAlpha(dl, t);
        float x = (rdata_151e70 - t) * rdata_151e88 + x0;
        void *d2 = ui->dl;
        Color c;
        switch (e->kind) {
        case 2: c = nvg_rgba_f(rdata_151e8c, rdata_151e90, rdata_151e90, 1.0f); break;
        case 1: { Color acc = get_speed_pair(); c = nvg_rgb_f(acc.r, acc.g, acc.b); break; }
        case 0: c = nvg_rgba_f(rdata_151e94, rdata_151e98, rdata_151e9c, 1.0f); break;
        default: c = nvg_rgba_f(rdata_151e8c, rdata_151e8c, rdata_151e8c, 1.0f); break;
        }
        const char *text = e->text;
        float w = nvg_text_bounds(d2, 0.0f, 0.0f, text, nullptr, nullptr) + rdata_151ea0;
        Rect box = make_rect(x, y, w, 42.0f);
        nvg_FillColor(d2, nvg_rgba_f(rdata_151ea4, rdata_151ea4, rdata_151ea4, 1.0f));
        draw_rect_rounded(ui, box, rdata_151ea8);
        Rect bar = make_rect(box.x + rdata_151eac, box.y + rdata_151eac, rdata_151eb4, box.h + rdata_151eb0);
        nvg_FillColor(d2, nvg_trans_rgba_f(c, rdata_151eb8));
        draw_rect_rounded(ui, bar, rdata_151ebc);
        nvg_FillColor(d2, c);
        draw_text(ui, bar, 1, 0, rdata_151f88);
        Rect body = make_rect(bar.x + bar.w + rdata_151eac, box.y, box.w - bar.w + rdata_151eb0, box.h);
        nvg_FillColor(d2, nvg_rgba_f(rdata_151e8c, rdata_151e8c, rdata_151e8c, 1.0f));
        draw_text(ui, body, 0, 0, text);
        y += t * rdata_151ec0;
    }
    nvg_GlobalAlpha(dl, rdata_151e70);
}
