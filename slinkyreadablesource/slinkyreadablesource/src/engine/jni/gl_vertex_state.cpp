// 0x29000: GL vertex-array state helpers (immediate "pointer" setup via the loaded GL function table).
#include "slinky/engine.h"
#include "slinky/jcall.h"

typedef void (*GlPtrFn)(int size, int type, int stride, const void *p);   // glVertexPointer-style
typedef void (*GlEnableFn)(int cap);
extern "C" GlPtrFn *rdata_14fd10;     // cell -> fn ptr (size,type,stride,ptr)
extern "C" GlEnableFn *rdata_14fd08;  // cell -> glEnableClientState
extern "C" GlEnableFn *rdata_14fcf8;
extern "C" void (**rdata_14fd00)(int, int, int, const void *);
extern "C" char rdata_14fc10[], rdata_14fc70[], rdata_14fc80[], rdata_14fca0[], rdata_14fcc0[], rdata_14fcc4[],
    rdata_14fcd0[], rdata_14fcf0[], rdata_14fcf4[];

#define GL_VEC(name, tbl, n, cell, ptr) \
    extern "C" void name() { (*tbl)(n, 0x1406, 0, ptr); (*cell)(0x8074); }
#define GL_COL(name, n, size, ptr) \
    extern "C" void name() { auto f = *rdata_14fd00;  f(n, size, 0x1401, ptr); }

GL_VEC(gl_array_xyz_float, rdata_14fd10, 3, rdata_14fd08, rdata_14fc10)
GL_COL(gl_array_color1, 5, 0xe, rdata_14fc70)
GL_COL(gl_array_color2, 1, 0x18, rdata_14fc80)
GL_VEC(gl_array_xy_float, rdata_14fd10, 2, rdata_14fd08, rdata_14fca0)
GL_COL(gl_array_color3, 5, 4, rdata_14fcc0)
GL_COL(gl_array_color4, 2, 4, rdata_14fcc4)
GL_VEC(gl_array_xy_float2, rdata_14fd10, 2, rdata_14fd08, rdata_14fcd0)
GL_COL(gl_array_color5, 6, 4, rdata_14fcf0)
GL_COL(gl_array_color6, 2, 4, rdata_14fcf4)
extern "C" void enable_vertex_array() { (*rdata_14fcf8)(0x8074); }

struct Vec3 { double x, y, z; };
FN(void *, java_new_vec3_init, (void *a, Vec3 v));
FN(void *, java_world_ray_trace_blocks, (void *a, void *b, void *c, void *d, bool e, bool f, bool g));
FN(void *, get_net_handler, (void *a, void *b));
FN(void *, java_get_game_settings_key_bindings_8, (void *a, void *b));
FN(int, get_event_key, (void *a, void *b));
FN(Vec3, vec3_make, (double x, double y, double z));

extern "C" bool set_vertex_pointer(void *a, void *b, Vec3 *p, Vec3 *q) {
    void *r = java_new_vec3_init(a, *p);
    void *s = java_new_vec3_init(a, *q);
    return java_world_ray_trace_blocks(a, b, r, s, false, true, true) == nullptr;
}

struct Ctx3 { void *a; char pad[0x10]; void *c18; char pad2[0x10]; char *c30; };
extern "C" bool vertex_pointer_current(Ctx3 *c) {
    void *x = get_net_handler(c->a, c->c18);
    x = java_get_game_settings_key_bindings_8(c->a, x);
    int r = get_event_key(c->a, x);
    bool f = r == -0x63;

    return c->c30[f + 1] != 0;
}

// ---- rotation state (silent aim / mouse emulation)
struct Rot { float yaw, pitch; };
FN(Rot, rot_add, (Rot a, Rot b));
FN(Rot, rot_make, (float yaw, float pitch));
FN(float, wrap_angle, (float a));                  // wrap to [-180, 180)
FN(void *, get_net_handler, (void *j, void *x));
FN(float, java_get_game_settings_mouse_sensitivity, (void *j, void *settings)); // mouse sensitivity
FN(void, pack_flags, (void *j, bool on));
FN(Rot, get_entity_rotation, (void *j, void *p));
FN(Rot, get_entity_prev_rotation, (void *j, void *p));
FN(void, set_entity_rotation, (void *j, void *p, Rot r));
FN(void, set_entity_prev_rotation, (void *j, void *p, Rot r));
extern "C" unsigned data_2f7b50;
extern "C" Rot data_2f7b58, data_2f7b78, data_2f7b80, data_2f7b88;
extern "C" int data_2f7b60;
extern "C" float data_2f7b64;
extern "C" unsigned char data_2f7b68, data_2f7b69, data_2f7b90, data_2f7b91;
extern "C" Rot data_2f7b70;
extern "C" const float rdata_14fd20, rdata_14fd24, rdata_14fd28, rdata_14fd2c;

extern "C" bool increment_wrap(unsigned v) {
    unsigned old = data_2f7b50;
    if (v >= old) data_2f7b50 = v;
    return v >= old;
}
extern "C" void apply_silent_rotation(Rot r) {
    data_2f7b58 = rot_add(data_2f7b58, r);
    data_2f7b60 = 0;
}
extern "C" Rot get_render_state() { return data_2f7b58; }
// per-tick: recompute the mouse step from the sensitivity (f = s*0.6+0.2; step = f^3*8*0.15)
extern "C" void player_tick(void *j, void *x) {
    float f = java_get_game_settings_mouse_sensitivity(j, get_net_handler(j, x)) * rdata_14fd20 + rdata_14fd24;
    data_2f7b64 = f * f * f * rdata_14fd28 * rdata_14fd2c;
    if (data_2f7b60++ > 0) data_2f7b58 = rot_make(0.0f, 0.0f);
    data_2f7b50 = 0;
}
extern "C" void reset_player_rotation(void **j) {
    if (data_2f7b68 == 1) {
        pack_flags(*j, true);
        data_2f7b69 = 1;
    }
}
extern "C" void set_silent_rotation_active(void *j, void *p, bool save) {
    Rot *a, *b;
    if (save) {
        data_2f7b78 = get_entity_rotation(j, p);
        data_2f7b80 = get_entity_prev_rotation(j, p);
        a = b = &data_2f7b88;
    } else {
        b = &data_2f7b80;
        a = &data_2f7b78;
    }
    set_entity_rotation(j, p, *a);
    set_entity_prev_rotation(j, p, *b);
}
extern "C" unsigned char is_silent_rotation_active() { return data_2f7b90; }
extern "C" Rot get_silent_rotation() { return data_2f7b88; }
extern "C" void set_render_rotation(Rot r) {
    float d = wrap_angle(r.yaw - data_2f7b88.yaw);
    data_2f7b91 = 1;
    data_2f7b88.yaw = d + data_2f7b88.yaw;
    data_2f7b88.pitch = r.pitch;
}
extern "C" Rot get_render_rotation() { return data_2f7b70; }

// Minecraft formatting colour code ('0'-'9', 'a'-'e', anything else white) -> RGB
struct F2 { float a, b; };
struct Col { F2 rg; float b; };
extern "C" const F2 rdata_14fa20, rdata_14fa70, rdata_14fa80, rdata_14fac0, rdata_14fad0, rdata_14fae0, rdata_14faf0;
extern "C" const float rdata_14fa30, rdata_14fa50, rdata_14fa90;   // 0.33, 1.0, 0.67
extern "C" Col hex_digit_value(unsigned short ch) {
    Col c;
    F2 z = {0.0f, 0.0f};
    switch (ch) {
    case '0': c.rg = z; c.b = 0.0f; break;
    case '1': c.rg = z; c.b = rdata_14fa90; break;
    case '2': c.rg = rdata_14fae0; c.b = 0.0f; break;
    case '3': c.rg = rdata_14fae0; c.b = rdata_14fa90; break;
    case '4': c.rg.a = rdata_14fa90; c.rg.b = 0.0f; c.b = 0.0f; break;
    case '5': c.rg.a = rdata_14fa90; c.rg.b = 0.0f; c.b = rdata_14fa90; break;
    case '6': c.rg = rdata_14faf0; c.b = 0.0f; break;
    case '7': c.rg = rdata_14fa80; c.b = rdata_14fa90; break;
    case '8': c.rg = rdata_14fa70; c.b = rdata_14fa30; break;
    case '9': c.rg = rdata_14fa70; c.b = rdata_14fa50; break;
    case 'a': c.rg = rdata_14fad0; c.b = rdata_14fa30; break;
    case 'b': c.rg = rdata_14fad0; c.b = rdata_14fa50; break;
    case 'c': c.rg = rdata_14fac0; c.b = rdata_14fa30; break;
    case 'd': c.rg = rdata_14fac0; c.b = rdata_14fa50; break;
    case 'e': c.rg = rdata_14fa20; c.b = rdata_14fa30; break;
    default: c.rg = rdata_14fa20; c.b = rdata_14fa50; break;
    }
    return c;
}

// ---- chat output
FN(void *, get_minecraft, (void *j));
FN(void *, get_local_player, (void *j, void *mc));
FN(int, libc_vsnprintf, (char *buf, unsigned long long n, const char *fmt, char *va));   // vsnprintf
FN(void *, java_new_chat_component_text_init, (void *j, void *str));     // new ChatComponentText
FN(void, java_call_entity_player_sp_add_chat_message, (void *j, void *p, void *msg));
extern "C" char data_2f7a50[0x100];
extern "C" void jni_throw_format(Jvm *j, const char *fmt, ...) {
    void *mc = get_minecraft(j);
    void *p = get_local_player(j, mc);
    if (!p) return;
    __builtin_va_list va;
    __builtin_va_start(va, fmt);
    libc_vsnprintf(data_2f7a50, 0x100, fmt, va);
    JNIEnv *env = j->env;
    jstring s = (*env)->NewStringUTF(env, data_2f7a50);
    if (!s) return;
    void *t = java_new_chat_component_text_init(j, s);
    if (!t) return;
    java_call_entity_player_sp_add_chat_message(j, p, t);
}

// ---- movement helpers
FN(float, libc_sinf, (float x));   // sinf
FN(float, libc_cosf, (float x));   // cosf
extern "C" const float rdata_14fbd8, rdata_14fbdc, rdata_14fbe0, rdata_14fbe4;
extern "C" const double rdata_14fbe8, rdata_14fbf0;
// horizontal motion for (forward, strafe) input at the given yaw (EntityLivingBase.moveFlying)
extern "C" Vec3 horizontal_motion(Rot r, float fwd, float strafe, float speed) {
    fwd *= rdata_14fbd8;
    strafe *= rdata_14fbd8;
    float m = strafe * strafe + fwd * fwd;
    if (rdata_14fbdc > m) {
        Vec3 z = vec3_make(0.0, 0.0, 0.0);
        return z;
    }
    float d = __builtin_sqrtf(m);
    float k = speed * rdata_14fbe0 / (rdata_14fbe4 > d ? rdata_14fbe4 : d);
    strafe *= k;
    fwd *= k;
    float a = (float)((double)r.yaw * rdata_14fbe8 / rdata_14fbf0);
    float sn = libc_sinf(a);
    float cs = libc_cosf(a);
    Vec3 v;
    float t = fwd * sn;

    v.x = (double)(strafe * cs - t);
    v.y = 0.0;
    v.z = (double)(fwd * cs + strafe * sn);
    return v;
}

// ---- world -> screen projection with the captured GL matrices
struct View { char pad[0x20]; float w, h; const float *mv, *proj; };
extern "C" const float rdata_14fc00, rdata_14fc04;   // 1.0, 0.5
extern "C" bool project_point_ex(View *v, Vec3 p, F2 *out, float *depth) {
    float x = (float)p.x, y = (float)p.y, z = (float)p.z;
    const float *m = v->mv, *q = v->proj;
    float e0 = m[0] * x + m[4] * y + m[8] * z + m[12];
    float e1 = m[1] * x + m[5] * y + m[9] * z + m[13];
    float e2 = m[2] * x + m[6] * y + m[10] * z + m[14];
    float e3 = m[3] * x + m[7] * y + m[11] * z + m[15];
    float w = q[3] * e0 + q[7] * e1 + q[11] * e2 + q[15] * e3;
    if (w == 0.0f) return false;
    float k = rdata_14fc00 / w * rdata_14fc04;
    float zz = (q[2] * e0 + q[6] * e1 + q[10] * e2 + q[14] * e3) * k + rdata_14fc04;
    if (0.0f >= zz || zz >= rdata_14fc00) return false;
    float yy = (q[1] * e0 + q[5] * e1 + q[9] * e2 + q[13] * e3) * k + rdata_14fc04;
    float xx = (q[0] * e0 + q[4] * e1 + q[8] * e2 + q[12] * e3) * k + rdata_14fc04;
    out->a = xx * v->w;
    out->b = v->h - yy * v->h;
    if (depth) *depth = k;
    return true;
}
extern "C" bool project_point(View *v, Vec3 *p, F2 *out) { return project_point_ex(v, *p, out, nullptr); }

// ---- apply the pending mouse delta in whole sensitivity steps
FN(float, libc_truncf, (float v));                 // truncf
FN(void, java_entity_player_sp_render_arm_pitch, (void *j, void *p, Rot r));
FN(void, java_entity_player_sp_prev_render_arm_pitch, (void *j, void *p, Rot r));
typedef int v4i __attribute__((vector_size(16), aligned(16)));
typedef float v4f __attribute__((vector_size(16), aligned(16)));
extern "C" const v4i rdata_14fd40;           // sign mask
extern "C" void set_rotation_active(void *j, void *p, bool render) {
    float step = data_2f7b64;
    if (step == 0.0f) return;
    float neg = ((v4f)((v4i)(v4f){step} ^ rdata_14fd40))[0];
    float dx = 0.0f, dy = 0.0f;
    float mx = data_2f7b58.yaw;
    if (neg >= mx || mx >= step) dx = libc_truncf(mx / step) * step;
    data_2f7b58.yaw = mx - dx;
    float my = data_2f7b58.pitch;
    if (neg >= my || my >= step) dy = libc_truncf(my / step) * step;
    data_2f7b58.pitch = my - dy;
    if (dx == 0.0f && dy == 0.0f) return;
    Rot r = rot_add(get_entity_rotation(j, p), Rot{dx, dy});
    set_entity_rotation(j, p, r);
    set_entity_prev_rotation(j, p, r);
    if (!render) return;
    java_entity_player_sp_prev_render_arm_pitch(j, p, r);
    java_entity_player_sp_render_arm_pitch(j, p, r);
}

// ---- movement fix: remap the pressed WASD keys to the server-side yaw
FN(bool, is_in_game, (void *j, void *mc));
FN(void *, java_get_game_settings_key_bindings_4, (void *j, void *gs));      // keyBindForward
FN(void *, java_get_game_settings_key_bindings_5, (void *j, void *gs));      // keyBindBack
FN(void *, java_get_game_settings_key_bindings_6, (void *j, void *gs));      // keyBindLeft
FN(void *, java_get_game_settings_key_bindings_7, (void *j, void *gs));      // keyBindRight
FN(bool, is_key_down, (void *j, void *kb));        // pressed
FN(void, java_set_key_binding_pressed, (void *j, void *kb, bool pressed));
FN(float, libc_roundf, (float v));                 // roundf
extern "C" unsigned char data_2f7ba1;        // captured key bits: fwd, back, left, right
extern "C" const float rdata_14fd50;         // 45
extern "C" const unsigned short rdata_14fd60[8];   // octant -> (fwd, strafe) bytes
extern "C" void pack_flags(void *j, bool apply) {
    void *mc = get_minecraft(j);
    if (!is_in_game(j, mc)) return;
    void *gs = get_net_handler(j, mc);
    void *kf = java_get_game_settings_key_bindings_4(j, gs), *kb = java_get_game_settings_key_bindings_5(j, gs), *kl = java_get_game_settings_key_bindings_6(j, gs), *kr = java_get_game_settings_key_bindings_7(j, gs);
    if (!apply) {
        java_set_key_binding_pressed(j, kf, data_2f7ba1 & 1);
        java_set_key_binding_pressed(j, kb, (data_2f7ba1 >> 1) & 1);
        java_set_key_binding_pressed(j, kl, (data_2f7ba1 >> 2) & 1);
        java_set_key_binding_pressed(j, kr, (data_2f7ba1 >> 3) & 1);
        return;
    }
    unsigned char a = is_key_down(j, kf);
    unsigned char b = is_key_down(j, kb);
    unsigned char bits = (unsigned char)(b + b) | a;
    unsigned char c = is_key_down(j, kl);
    bits = (unsigned char)(c << 2) | bits;
    unsigned char d = is_key_down(j, kr);
    data_2f7ba1 = (unsigned char)(d << 3) | bits;
    void *p = get_local_player(j, mc);
    float delta = wrap_angle(get_entity_rotation(j, p).yaw - data_2f7b70.yaw);
    unsigned f = data_2f7ba1;
    signed char fwd = (signed char)((f & 1) + ((signed char)(f << 6) >> 7));
    signed char str = (signed char)(((signed char)(f << 4) >> 7) + ((f >> 2) & 1));
    unsigned short mv;
    if (fwd == 0 && str == 0) {
        mv = 0;
    } else {
        int dir;
        if (fwd == 0) dir = str == 1 ? 2 : (str == -1 ? 6 : 0);
        else if (fwd == -1) dir = 5 - (unsigned char)(str + 1);
        else if (str == 0) dir = 0;
        else dir = str == 1 ? 1 : (str == -1 ? 7 : 0);
        mv = rdata_14fd60[((int)libc_roundf(delta / rdata_14fd50) + dir) & 7];
    }
    unsigned hi = mv >> 8;
    java_set_key_binding_pressed(j, kf, (mv & 0xff) == 1);
    java_set_key_binding_pressed(j, kb, (mv & 0xff) == 0xff);
    java_set_key_binding_pressed(j, kl, hi == 1);
    java_set_key_binding_pressed(j, kr, hi == 0xff);
}

// ---- silent-rotation controller: start (save real rotation) / stop (restore smoothly)
extern "C" unsigned char data_2f7b6a;
extern "C" void apply_player_rotation(Ctx *c, bool on, unsigned char moveFix, unsigned char useTarget, Rot target) {
    if (data_2f7b69 == 1) {
        pack_flags(c->a, false);
        data_2f7b69 = 0;
        data_2f7b68 = 0;
    }
    if (on) {
        if (!(data_2f7b90 & 1) && c->b) {
            data_2f7b90 = 1;
            data_2f7b88 = get_entity_rotation(c->a, c->b);
        }
        data_2f7b6a = useTarget;
        data_2f7b70 = target;
        data_2f7b68 = moveFix;
        return;
    }
    if (!(data_2f7b90 & 1)) return;
    data_2f7b90 = 0;
    Rot *src = data_2f7b6a ? &data_2f7b70 : &data_2f7b88;
    float yaw = src->yaw, pitch = src->pitch;
    float cur = get_entity_rotation(c->a, c->b).yaw;
    Rot r = {wrap_angle(yaw - cur) + cur, pitch};
    set_entity_rotation(c->a, c->b, r);
    set_entity_prev_rotation(c->a, c->b, r);
    java_entity_player_sp_prev_render_arm_pitch(c->a, c->b, r);
    java_entity_player_sp_render_arm_pitch(c->a, c->b, r);
}

// wrap an angle into range when it lies outside (-limit, limit); limit = data_2f7b64
FN(float, libc_truncf, (float x));   // floorf
extern "C" void clamp_angle(float *angle) {
    float v = *angle, limit = data_2f7b64;
    float wrap = 0.0f;
    if (v <= -limit || v >= limit) wrap = libc_truncf(v / limit) * limit;
    *angle = v - wrap;
}
