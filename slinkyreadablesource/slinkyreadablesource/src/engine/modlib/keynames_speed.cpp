// 0x32000: key-name table helpers and a "speed" module's callbacks
#include "slinky/engine.h"
struct Vec3 { double x, y, z; };

extern "C" int data_2f9e2c;
extern "C" char data_2f9e28[], data_2f9e30[];
extern "C" void *data_2f9e20;
extern "C" long long data_2eb728; extern "C" int data_2eb730;
extern "C" const char *data_2eb6a0[16];
extern "C" char data_2eb740[], data_2eba10[];
extern "C" float data_2ebc50, rdata_151668, rdata_15166c;
extern "C" unsigned char data_2ebc70, data_2ebc90, data_2ebcb0;

extern "C" int libc_strcmp(const char *a, const char *b);
FN(unsigned long long, libc_strlen, (const char *s));
FN(void **, element_at, (void *vec, int elem, void *hdr, void *key));
FN(void *, vector_find_insert, (void *t, int elem, void *hdr, void *e, void *cmp));
FN(void, vector_erase, (void *vec, int elem, void *hdr, void *key));
FN(unsigned char, is_unload_requested, (void *x));
FN(void *, get_timer, (void *a, void *x));
FN(void, java_set_timer_timer_speed, (void *a, void *timer, float speed));
FN(void, queue_module_toggle, (void *a, void *mod, unsigned char on));
FN(unsigned char, vertex_pointer_current, (Ctx *c));
FN(unsigned char, java_get_entity_on_ground, (void *a, void *b));
FN(Vec3, java_get_entity_motion_x, (void *a, void *p));
FN(int, java_get_entity_living_base_hurt_time, (void *a, void *b));

extern "C" int compare_signed_chars(const signed char *, const signed char *a, const signed char *b) { return *a - *b; }
extern "C" void key_filter(char *buf, char c) {
    unsigned long long n = libc_strlen(buf);
    if (n > 99) return;
    unsigned char l = (unsigned char)(c - 0x61);

    if (l >= 0x1a) {
        unsigned char u = (unsigned char)(c - 0x41);

        unsigned char notupper = u >= 0x1a;

        unsigned char notspace = c != 0x20;

        if (notupper & notspace) return;
    }
    buf[n] = c;
}
extern "C" int get_key_name_index() { return data_2f9e2c; }
extern "C" const char *key_name_for(void *, void *key) {
    signed char *p = (signed char *)element_at(data_2f9e20, 1, data_2f9e28, key);
    return data_2eb6a0[*p];
}
extern "C" void remove_key_name(void *, void *key) { vector_erase(data_2f9e20, 1, data_2f9e28, key); }
extern "C" void clear_key_name() { data_2f9e2c = 0; }
extern "C" void key_name_table(const char ***a, long long *n) { *a = data_2eb6a0; *n = 0x10; }
extern "C" unsigned char is_unload_requested_flag() { return is_unload_requested(data_2f9e30); }
struct Pair { long long a; int b; };
extern "C" Pair get_speed_pair() { Pair r; r.b = data_2eb730; r.a = data_2eb728; return r; }
extern "C" bool enable_hud_editor(Ctx **h) { queue_module_toggle(h[1], data_2eb740, 1); return true; }

struct Hk { char pad[8]; Ctx *ctx; };
extern "C" bool key_name_matches(void *, const char *name) {
    int i;
    for (i = 0; i < 16; i++)
        if (libc_strcmp(name, data_2eb6a0[i]) == 0) break;
    if (i == 16) return false;
    signed char idx = (signed char)i;
    vector_find_insert(&data_2f9e20, 1, data_2f9e28, &idx, (void *)compare_signed_chars);
    return true;
}
extern "C" void speed_hook_tick(Hk *h) {
    Ctx *c = h->ctx;
    void *t = get_timer(c->a, c->c18);
    float speed;
    if (data_2ebc70 == 1 && !vertex_pointer_current(c)) goto slow;
    if (data_2ebc90 == 1) {
        if (java_get_entity_on_ground(c->a, c->b)) goto slow;
        Vec3 v = java_get_entity_motion_x(c->a, c->b);
        if (v.y >= 0.0) goto slow;
    }
    if (data_2ebcb0 == 1) {
        if (c->f40 != 1) goto slow;
        if (!java_get_entity_living_base_hurt_time(c->a, c->b)) goto slow;
    }
    speed = data_2ebc50;
    goto go;
slow:
    speed = rdata_151668;
go:
    java_set_timer_timer_speed(c->a, t, speed);
}
extern "C" void speed_hook_enable(Hk *h) {
    if (*(void **)((char *)h + 0x10) == data_2eba10 && *((char *)h + 0x18) == 0) {
        Ctx *c = h->ctx;
        void *t = get_timer(c->a, c->c18);
        void *w = c->a;

        java_set_timer_timer_speed(w, t, rdata_15166c);
    }
}

FN(void, empty_placeholder, (void *x));
extern "C" void reset_key_state() { empty_placeholder(data_2f9e30); }
