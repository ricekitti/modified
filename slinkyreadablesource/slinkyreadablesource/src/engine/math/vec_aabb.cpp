// 0x9000: Vec3 / AABB helpers (MS ABI: 24/48-byte structs travel by pointer, results via sret)
#include "slinky/engine.h"
struct Vec3 { double x, y, z; };
struct AABB { Vec3 min, max; };
extern "C" double rdata_13e850, rdata_13e858;

extern "C" Vec3 vec3_make(double x, double y, double z) { return Vec3{x, y, z}; }
extern "C" Vec3 vec3_sub(Vec3 a, Vec3 b) { return Vec3{a.x - b.x, a.y - b.y, a.z - b.z}; }
extern "C" Vec3 vec3_add(Vec3 a, Vec3 b) { return Vec3{b.x + a.x, b.y + a.y, b.z + a.z}; }
extern "C" Vec3 vec3_scale(Vec3 a, double s) { return Vec3{s * a.x, s * a.y, s * a.z}; }
extern "C" Vec3 vec3_lerp(Vec3 a, Vec3 b, double t) {
    return Vec3{a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t};
}
extern "C" double vec3_distance_sq(Vec3 *a, Vec3 *b) {
    double dx = a->x - b->x, dy = a->y - b->y, dz = a->z - b->z;
    return dz * dz + (dx * dx + dy * dy);
}
extern "C" double vec3_distance(Vec3 *a, Vec3 *b) {
    double ax = a->x, ay = a->y, az = a->z;
    double dx = ax - b->x, dy = ay - b->y, dz = az - b->z;
    return __builtin_sqrt(dz * dz + (dx * dx + dy * dy));
}
extern "C" Vec3 vec3_add_eye_height(Vec3 a) { return Vec3{a.x, a.y + rdata_13e850, a.z}; }
extern "C" Vec3 vec3_add_eye_height_sneak(Vec3 a) { return Vec3{a.x, a.y + rdata_13e858, a.z}; }
extern "C" Vec3 vec3_reciprocal(Vec3 a) { return Vec3{1.0 / a.x, 1.0 / a.y, 1.0 / a.z}; }
extern "C" AABB aabb_make(Vec3 a, Vec3 b) { return AABB{a, b}; }
extern "C" AABB *aabb_expand(AABB *out, AABB *b, double d) {
    b->min.x -= d; b->min.y -= d; b->min.z -= d;
    b->max.x += d; b->max.y += d; b->max.z += d;
    *out = *b;
    return out;
}
extern "C" bool aabb_intersects(AABB *a, AABB *b) {
    return b->max.x > a->min.x && a->max.x > b->min.x && b->max.y > a->min.y &&
           a->max.y > b->min.y && b->max.z > a->min.z && a->max.z > b->min.z;
}
extern "C" bool aabb_contains_point(AABB *a, Vec3 *p) {
    return p->x > a->min.x && a->max.x > p->x && p->y > a->min.y && a->max.y > p->y &&
           p->z > a->min.z && a->max.z > p->z;
}

// ---- Rot (yaw, pitch) packed in one register ----
struct Rot { float yaw, pitch; };
FN(float, libc_hypotf_alt, (float a, float b));
FN(float, libc_hypotf, (float a, float b));
extern "C" double libc_atan2(double y, double x);
extern "C" double rdata_13e8a0, rdata_13e8a8, rdata_13e8b0, rdata_13e8b8;

extern "C" Rot rot_make(float a, float b) { return Rot{a, b}; }
extern "C" Rot rot_add(Rot a, Rot b) { return Rot{b.yaw + a.yaw, b.pitch + a.pitch}; }
extern "C" Rot rot_sub(Rot a, Rot b) { return Rot{a.yaw - b.yaw, a.pitch - b.pitch}; }
extern "C" Rot rot_scale(Rot a, float s) { return Rot{a.yaw * s, a.pitch * s}; }
extern "C" Rot rot_lerp(Rot a, Rot b, float t) {
    float y = (b.yaw - a.yaw) * t + a.yaw;

    return Rot{y, (b.pitch - a.pitch) * t + a.pitch};
}
extern "C" float rot_delta_length(Rot a, Rot b) { return libc_hypotf_alt(a.yaw - b.yaw, a.pitch - b.pitch); }
extern "C" float rot_distance(Rot a, Rot b) { return libc_hypotf(b.pitch - a.pitch, b.yaw - a.yaw); }
extern "C" float rot_length(Rot a) { return libc_hypotf_alt(a.yaw, a.pitch); }
extern "C" Rot rot_look_at(Vec3 *from, Vec3 *to) {
    double dx = to->x - from->x;

    double dy = to->y - from->y;

    double dz = to->z - from->z;
    double hor = __builtin_sqrt(dx * dx + dz * dz);
    float yaw = (float)(libc_atan2(dz, dx) * rdata_13e8a0 / rdata_13e8a8 + rdata_13e8b0);
    float pitch = (float)(libc_atan2(dy, hor) * rdata_13e8b8 / rdata_13e8a8);
    return Rot{yaw, pitch};
}

// key code -> name (clang turns the switch into a relative string table)
extern "C" const char rdata_13e692[], rdata_13e4c0[], rdata_13e4c7[], rdata_13e4ce[], rdata_13e4d5[], rdata_13e4dc[], rdata_13e4e3[], rdata_13e4ed[], rdata_13e4f1[], rdata_13e4f7[], rdata_13e4fc[], rdata_13e500[], rdata_13e506[], rdata_13e50b[], rdata_13e510[], rdata_13e514[], rdata_13e519[], rdata_13e51e[], rdata_13e521[], rdata_13e527[], rdata_13e52c[], rdata_13e534[], rdata_13e538[], rdata_13e53c[], rdata_13e544[], rdata_13e54b[], rdata_13e552[], rdata_13e559[], rdata_13e562[], rdata_13e56b[], rdata_13e571[], rdata_13e577[], rdata_13e57d[], rdata_13e582[], rdata_13e588[], rdata_13e58e[], rdata_13e595[], rdata_13e59b[], rdata_13e5a1[], rdata_13e5aa[], rdata_13e5b4[], rdata_13e5bd[], rdata_13e5c3[], rdata_13e5c5[], rdata_13e5c7[], rdata_13e5c9[], rdata_13e5cb[], rdata_13e5cd[], rdata_13e5cf[], rdata_13e5d1[], rdata_13e5d3[], rdata_13e5d5[], rdata_13e5d7[], rdata_13e5d9[], rdata_13e5db[], rdata_13e5dd[], rdata_13e5df[], rdata_13e5e1[], rdata_13e5e3[], rdata_13e5e5[], rdata_13e5e7[], rdata_13e5e9[], rdata_13e5eb[], rdata_13e5ed[], rdata_13e5ef[], rdata_13e5f1[], rdata_13e5f3[], rdata_13e5f5[], rdata_13e5f7[], rdata_13e5f9[], rdata_13e5fb[], rdata_13e5fd[], rdata_13e5ff[], rdata_13e601[], rdata_13e603[], rdata_13e605[], rdata_13e607[], rdata_13e609[], rdata_13e60b[], rdata_13e613[], rdata_13e61b[], rdata_13e623[], rdata_13e62b[], rdata_13e633[], rdata_13e63b[], rdata_13e643[], rdata_13e64b[], rdata_13e653[], rdata_13e65b[], rdata_13e65e[], rdata_13e661[], rdata_13e664[], rdata_13e667[], rdata_13e66a[], rdata_13e66d[], rdata_13e670[], rdata_13e673[], rdata_13e676[], rdata_13e67a[], rdata_13e67e[], rdata_13e682[], rdata_13e686[], rdata_13e68a[], rdata_13e68e[];
extern "C" const char *mouse_button_name(int v) {
    switch (v) {
    case 1: return rdata_13e4c0;   // "MOUSE1"
    case 2: return rdata_13e4c7;   // "MOUSE2"
    case 3: return rdata_13e4ce;   // "MOUSE3"
    case 4: return rdata_13e4d5;   // "MOUSE4"
    case 5: return rdata_13e4dc;   // "MOUSE5"
    case 6: return rdata_13e5c3;   // "0"
    case 7: return rdata_13e5c5;   // "1"
    case 8: return rdata_13e5c7;   // "2"
    case 9: return rdata_13e5c9;   // "3"
    case 10: return rdata_13e5cb;   // "4"
    case 11: return rdata_13e5cd;   // "5"
    case 12: return rdata_13e5cf;   // "6"
    case 13: return rdata_13e5d1;   // "7"
    case 14: return rdata_13e5d3;   // "8"
    case 15: return rdata_13e5d5;   // "9"
    case 16: return rdata_13e60b;   // "NUMPAD0"
    case 17: return rdata_13e613;   // "NUMPAD1"
    case 18: return rdata_13e61b;   // "NUMPAD2"
    case 19: return rdata_13e623;   // "NUMPAD3"
    case 20: return rdata_13e62b;   // "NUMPAD4"
    case 21: return rdata_13e633;   // "NUMPAD5"
    case 22: return rdata_13e63b;   // "NUMPAD6"
    case 23: return rdata_13e643;   // "NUMPAD7"
    case 24: return rdata_13e64b;   // "NUMPAD8"
    case 25: return rdata_13e653;   // "NUMPAD9"
    case 26: return rdata_13e5d7;   // "A"
    case 27: return rdata_13e5d9;   // "B"
    case 28: return rdata_13e5db;   // "C"
    case 29: return rdata_13e5dd;   // "D"
    case 30: return rdata_13e5df;   // "E"
    case 31: return rdata_13e5e1;   // "F"
    case 32: return rdata_13e5e3;   // "G"
    case 33: return rdata_13e5e5;   // "H"
    case 34: return rdata_13e5e7;   // "I"
    case 35: return rdata_13e5e9;   // "J"
    case 36: return rdata_13e5eb;   // "K"
    case 37: return rdata_13e5ed;   // "L"
    case 38: return rdata_13e5ef;   // "M"
    case 39: return rdata_13e5f1;   // "N"
    case 40: return rdata_13e5f3;   // "O"
    case 41: return rdata_13e5f5;   // "P"
    case 42: return rdata_13e5f7;   // "Q"
    case 43: return rdata_13e5f9;   // "R"
    case 44: return rdata_13e5fb;   // "S"
    case 45: return rdata_13e5fd;   // "T"
    case 46: return rdata_13e5ff;   // "U"
    case 47: return rdata_13e601;   // "V"
    case 48: return rdata_13e603;   // "W"
    case 49: return rdata_13e605;   // "X"
    case 50: return rdata_13e607;   // "Y"
    case 51: return rdata_13e609;   // "Z"
    case 52: return rdata_13e577;   // "COLON"
    case 53: return rdata_13e57d;   // "PLUS"
    case 54: return rdata_13e582;   // "COMMA"
    case 55: return rdata_13e588;   // "MINUS"
    case 56: return rdata_13e58e;   // "PERIOD"
    case 57: return rdata_13e595;   // "SLASH"
    case 58: return rdata_13e59b;   // "GRAVE"
    case 59: return rdata_13e5a1;   // "LBRACKET"
    case 60: return rdata_13e5aa;   // "BACKSLASH"
    case 61: return rdata_13e5b4;   // "RBRACKET"
    case 62: return rdata_13e5bd;   // "QUOTE"
    case 63: return rdata_13e519;   // "LEFT"
    case 64: return rdata_13e51e;   // "UP"
    case 65: return rdata_13e521;   // "RIGHT"
    case 66: return rdata_13e527;   // "DOWN"
    case 67: return rdata_13e65b;   // "F1"
    case 68: return rdata_13e65e;   // "F2"
    case 69: return rdata_13e661;   // "F3"
    case 70: return rdata_13e664;   // "F4"
    case 71: return rdata_13e667;   // "F5"
    case 72: return rdata_13e66a;   // "F6"
    case 73: return rdata_13e66d;   // "F7"
    case 74: return rdata_13e670;   // "F8"
    case 75: return rdata_13e673;   // "F9"
    case 76: return rdata_13e676;   // "F10"
    case 77: return rdata_13e67a;   // "F11"
    case 78: return rdata_13e67e;   // "F12"
    case 79: return rdata_13e682;   // "F13"
    case 80: return rdata_13e686;   // "F14"
    case 81: return rdata_13e68a;   // "F15"
    case 82: return rdata_13e68e;   // "F16"
    case 83: return rdata_13e4e3;   // "BACKSPACE"
    case 84: return rdata_13e4ed;   // "TAB"
    case 85: return rdata_13e4f1;   // "ENTER"
    case 86: return rdata_13e4f7;   // "CAPS"
    case 87: return rdata_13e4fc;   // "ESC"
    case 88: return rdata_13e500;   // "SPACE"
    case 89: return rdata_13e506;   // "PREV"
    case 90: return rdata_13e50b;   // "NEXT"
    case 91: return rdata_13e510;   // "END"
    case 92: return rdata_13e514;   // "HOME"
    case 93: return rdata_13e52c;   // "PRNTSCR"
    case 94: return rdata_13e534;   // "INS"
    case 95: return rdata_13e538;   // "DEL"
    case 96: return rdata_13e53c;   // "NUMLOCK"
    case 97: return rdata_13e544;   // "SCROLL"
    case 98: return rdata_13e54b;   // "LSHIFT"
    case 99: return rdata_13e552;   // "RSHIFT"
    case 100: return rdata_13e559;   // "LCONTROL"
    case 101: return rdata_13e562;   // "RCONTROL"
    case 102: return rdata_13e56b;   // "LMENU"
    case 103: return rdata_13e571;   // "RMENU"
    default: return rdata_13e692;   // "NONE"
    }
}

// ---- AABB union / clamp / ray slab tests (double fmin/fmax on float slab values, like the C source) ----
extern "C" AABB aabb_union(Vec3 a, Vec3 b) {
    AABB r;
    r.min.z = __builtin_fmin(a.z, b.z);
    r.max.z = __builtin_fmax(a.z, b.z);
    r.min.x = __builtin_fmin(a.x, b.x); r.min.y = __builtin_fmin(a.y, b.y);
    r.max.x = __builtin_fmax(a.x, b.x); r.max.y = __builtin_fmax(a.y, b.y);
    return r;
}
extern "C" Vec3 aabb_clamp_point(AABB b, Vec3 p) {
    Vec3 r;
    r.x = p.x < b.min.x ? b.min.x : (b.max.x < p.x ? b.max.x : p.x);
    r.y = p.y < b.min.y ? b.min.y : (b.max.y < p.y ? b.max.y : p.y);
    r.z = p.z < b.min.z ? b.min.z : (b.max.z < p.z ? b.max.z : p.z);
    return r;
}
extern "C" const double rdata_13e878, rdata_13e888, rdata_13e890;
extern "C" const float rdata_13e880, rdata_13e898;
extern "C" bool aabb_ray_slab_test(const AABB *b, const Vec3 *o, const Vec3 *inv) {
    const double *mn = &b->min.x, *mx = &b->max.x, *po = &o->x, *pi = &inv->x;
    float tmin = 0.0f, tmax = (float)rdata_13e878;
#pragma clang loop unroll(full)
    for (int i = 0; i < 3; i++) {
        float t1 = (float)((mn[i] - po[i]) * pi[i]);
        float t2 = (float)((mx[i] - po[i]) * pi[i]);
        tmin = (float)__builtin_fmin(__builtin_fmax(t1, tmin), __builtin_fmax(t2, tmin));
        tmax = (float)__builtin_fmax(__builtin_fmin(t1, tmax), __builtin_fmin(t2, tmax));
    }
    return tmin < tmax && tmin <= rdata_13e880;
}
extern "C" bool aabb_ray_hit(const AABB *b, const Vec3 *o, const Vec3 *dir, Vec3 *hit) {
    double inv[3] = {rdata_13e888 / dir->x, rdata_13e888 / dir->y, rdata_13e888 / dir->z};
    const double *mn = &b->min.x, *mx = &b->max.x, *po = &o->x;
    float tmin = 0.0f, tmax = (float)rdata_13e890;
#pragma clang loop unroll(full)
    for (int i = 0; i < 3; i++) {
        float t1 = (float)((mn[i] - po[i]) * inv[i]);
        float t2 = (float)((mx[i] - po[i]) * inv[i]);
        tmin = (float)__builtin_fmin(__builtin_fmax(t1, tmin), __builtin_fmax(t2, tmin));
        tmax = (float)__builtin_fmax(__builtin_fmin(t1, tmax), __builtin_fmin(t2, tmax));
    }
    bool ok = !(tmax <= tmin) & !(rdata_13e898 < tmin);
    if (ok) {
        hit->x = o->x + dir->x * tmin;
        hit->y = o->y + dir->y * tmin;
        hit->z = o->z + dir->z * tmin;
    }
    return ok;
}

// ---- packet hook: GUI-chat commands first, then the packet event (0x17); returns "cancel" ----
#include "slinky/jids.h"
struct HCtx { Jvm *a; void *mgr; void *p10; void *mc; void *world; void *player; void *p30; void *p38;
              unsigned char f40; unsigned char f41[3]; int f44; };
extern "C" char data_2f6f08[], data_2f7260[];
extern "C" void *data_2f7240, *data_2f7218;
extern "C" char *rdata_13e498;
FN(void *, get_minecraft, (Jvm *j));
FN(void *, get_local_player, (Jvm *j, void *mc));
FN(void *, get_world, (Jvm *j, void *mc));
FN(unsigned char, is_in_game, (Jvm *j, void *mc));
FN(void *, java_jni_is_instance_of_c01_packet_chat_message, (Jvm *j, void *pkt));
FN(void *, java_jni_is_instance_of_c14_packet_tab_complete, (Jvm *j, void *pkt));
FN(unsigned char, handle_chat_message, (HCtx *c, void *x));
FN(unsigned char, text_starts_with_bang, (HCtx *c, void *x));
struct PktEvent { long long type; Jvm *j; void *a; void *pkt; unsigned char cancel; char rest[0x27]; };
FN(void, dispatch_event, (void *mgr, PktEvent *e));
extern "C" unsigned char handle_chat_packet(Jvm *j, void *a, void *pkt) {
    void *mc = get_minecraft(j);
    void *pl = get_local_player(j, mc);
    void *w = get_world(j, mc);
    HCtx c;
    c.a = j; c.mgr = data_2f6f08; c.p10 = data_2f7240; c.mc = mc; c.world = w; c.player = pl;
    c.p30 = data_2f7218; c.p38 = data_2f7260;
    c.f40 = rdata_13e498[0x14] ? 0 : is_in_game(j, mc);
    *(int *)&c.f41[0] = 0;
    c.f44 = 0;
    void *x = java_jni_is_instance_of_c01_packet_chat_message(j, pkt);
    unsigned char r;
    if (x) {
        r = handle_chat_message(&c, x);
    } else {
        void *y = java_jni_is_instance_of_c14_packet_tab_complete(j, pkt);
        if (!y) goto send;
        r = text_starts_with_bang(&c, y);
    }
    if (r) return 1;
send:
    PktEvent e = {0x17, j, a, pkt, 0, {}};
    dispatch_event(data_2f6f08, &e);
    return e.cancel;
}

// ---- gap-scan functions ----
// client key code -> ASCII for text fields (a-z, 0-9, space, '.', '-'); 0 = not printable. Inverse of windows_key_to_client_key.
extern "C" char key_code_to_char(int key) {
    if ((unsigned)(key - 0x1a) <= 0x19) return (char)(key + 0x47);
    if ((unsigned)(key - 6) <= 9) return (char)(key + 0x2a);
    switch (key) {
    case 0x58: return ' ';
    case 0x38: return '.';
    case 0x37: return '-';
    default: return 0;
    }
}

// horizontal distance (*horiz) and absolute height difference (*vert) between two points
typedef long long v2l9 __attribute__((vector_size(16)));
typedef double v2d9 __attribute__((vector_size(16)));
extern "C" const v2l9 rdata_13e840;   // fabs mask
extern "C" double libc_hypot(double x, double z);   // hypot
extern "C" void vec3_horizontal_distance(const Vec3 *a, const Vec3 *b, double *horiz, double *vert) {
    double ax = a->x, az = a->z;

    *horiz = libc_hypot(ax - b->x, az - b->z);
    v2d9 d = {a->y - b->y, 0};
    *vert = ((v2d9)((v2l9)d & rdata_13e840))[0];
}

extern "C" Vec3 vec3_copy(Vec3 v) {   // normalize
    double len = __builtin_sqrt((v.x * v.x + v.y * v.y) + v.z * v.z);
    v.x /= len;
    v.y /= len;
    v.z /= len;
       // the original normalises its by-value copy in memory and copies it out
    return v;
}
extern "C" double vec3_length(const Vec3 *v) { return __builtin_sqrt((v->x * v->x + v->y * v->y) + v->z * v->z); }

struct V2f9 { float x, y; };
extern "C" float vec2_length_sq(V2f9 v) { return v.x * v.x + v.y * v.y; }
