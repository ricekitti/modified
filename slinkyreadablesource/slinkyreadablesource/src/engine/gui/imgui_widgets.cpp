// 0xd000: ImGui widget wrappers (mark profile dirty on change), bounded string builder
#include "slinky/engine.h"
#include <stdarg.h>
FN(void, set_profile_dirty, (unsigned char v));
FN(unsigned char, draw_keybind_control, (void *b, void *a, void *p18, void *s, int i20, void *p28, void *v30, unsigned char b38));
FN(unsigned char, draw_checkbox, (void *b, void *a, void *p0, void *v8));
FN(unsigned char, draw_colored_checkbox, (void *b, void *a, void *p0, void *p8, void *p10, int i18, void *v20, void *x, void *y));
struct W1 { char pad[0x18]; void *p18; int i20; int pad2; void *p28; char v30[8]; unsigned char b38; };
extern "C" void widget_enum(void *a, void *b, W1 *s) {
    if (draw_keybind_control(b, a, s->p18, s, s->i20, s->p28, s->v30, s->b38)) set_profile_dirty(1);
}
struct W2 { void *p0; char v8[8]; };
extern "C" void widget_color(void *a, void *b, W2 *s) {
    if (draw_checkbox(b, a, s->p0, s->v8)) set_profile_dirty(1);
}
struct W3 { void *p0, *p8, *p10; int i18; int pad; char v20[0x38]; void (*cb58)(void **x, void **y); unsigned char changed; };
extern "C" void widget_items(void *a, void *b, W3 *s) {
    void *x = 0, *y = 0;
    if (s->cb58) s->cb58(&x, &y);
    if (draw_colored_checkbox(b, a, s->p0, s->p8, s->p10, s->i18, s->v20, x, y)) {
        s->changed = 1;
        set_profile_dirty(1);
    }
}

struct Buf { char *p; unsigned long long cap, len; };
FN(unsigned long long, libc_strlen, (const char *s));
FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));
FN(int, libc_vsnprintf, (char *buf, unsigned long long n, const char *fmt, va_list ap));   // vsnprintf
extern "C" void buffer_init(Buf *b, char *p, unsigned long long cap) { b->p = p; b->cap = cap; b->len = 0; }
extern "C" bool buffer_append(Buf *b, const char *s) {
    unsigned long long len = b->len;
    if (b->cap <= len) return false;
    unsigned long long avail = b->cap - len;
    unsigned long long n = libc_strlen(s);
    if (n >= avail) return false;
    libc_memcpy_alt(b->p + len, s, n);
    b->len += n;
    return true;
}
extern "C" bool buffer_printf(Buf *b, const char *fmt, ...) {
    if (b->cap <= b->len) return false;
    unsigned long long avail = b->cap - b->len;
    va_list ap;
    va_start(ap, fmt);
    int r = libc_vsnprintf(b->p + b->len, avail, fmt, ap);
    unsigned long long nl;
    bool ok;
    if (r < 0 || avail <= (unsigned)r) { nl = b->cap; ok = false; }
    else { nl = (unsigned)r + b->len; ok = true; }
    b->len = nl;
    return ok;
}
extern "C" bool buffer_push_char(Buf *b, char c) {
    unsigned long long len = b->len, cap = b->cap;
    if (len < cap) {
        char *p = b->p;
        b->len = len + 1;
        p[len] = c;
    }
    return len < cap;
}
extern "C" bool buffer_terminate(Buf *b) {
    unsigned long long len = b->len, cap = b->cap;
    if (len < cap) {
        b->p[len] = 0;
        b->len++;
    }
    return len < cap;
}

// ---- generic dynamic array: data pointer + {u32 cap, u32 count} header, element size passed in ----
struct AHdr { unsigned cap, count; };
typedef int (*ACmp)(void *ctx, const void *key, const void *elem);
FN(void *, libc_realloc, (void *p, unsigned long long n));   // realloc
extern "C" __attribute__((noreturn)) void libc_abort();   // abort
FN(void *, libc_memmove, (void *d, const void *s, unsigned long long n));   // memmove
extern "C" void *element_at(void *base, unsigned elem, AHdr *, unsigned i) { return (char *)base + elem * i; }
static inline __attribute__((always_inline)) void agrow(void **data, unsigned elem, AHdr *h) {
    unsigned cap = h->cap ? h->cap * 2 : 8;
    h->cap = cap;
    void *p = libc_realloc(*data, cap * elem);
    if (!p) libc_abort();
    *data = p;
}
// push_back
extern "C" void *vector_push_back(void **data, unsigned elem, AHdr *h, const void *src) {
    if (h->count >= h->cap) agrow(data, elem, h);
    unsigned i = h->count;
    h->count = i + 1;
    return libc_memcpy_alt((char *)*data + i * elem, src, elem);
}
// insert at index (shares the manifest range of vector_push_back)
extern "C" void *insert_index(void **data, unsigned elem, AHdr *h, const void *src, unsigned idx) {
    if (h->count >= h->cap) agrow(data, elem, h);
    char *base = (char *)*data;
    char *at = base + idx * elem;
    libc_memmove(base + (idx + 1) * elem, at, (h->count - idx) * elem);
    libc_memcpy_alt(at, src, elem);
    h->count++;
    return at;
}
// binary search
extern "C" void *binary_search(void *base, unsigned elem, AHdr *h, const void *key, ACmp cmp) {
    unsigned hi = h->count;
    if (!hi) return 0;
    unsigned lo = 0;
    do {
        unsigned mid = lo + ((hi - lo) >> 1);
        char *p = (char *)base + mid * elem;
        int r = cmp(0, key, p);
        if (r < 0) hi = mid;
        else if (r == 0) return p;
        else lo = mid + 1;
    } while (lo < hi);
    return 0;
}
// sorted insert (returns the new element)
extern "C" void *sorted_insert(void **data, unsigned elem, AHdr *h, const void *key, ACmp cmp) {
    unsigned hi = h->count, idx;
    if (!hi) {
        idx = 0;
    } else {
        char *base = (char *)*data;
        unsigned lo = 0;
        for (;;) {
            unsigned mid = lo + ((hi - lo) >> 1);
            int r = cmp(0, key, base + mid * elem);
            if (r < 0) {
                hi = mid;
                if (lo >= hi) break;
            } else if (r == 0) {
                hi = mid;
                break;
            } else {
                lo = mid + 1;
                if (lo >= hi) break;
            }
        }
        idx = hi;
    }
    if (h->count >= h->cap) agrow(data, elem, h);
    char *base = (char *)*data;
    char *at = base + idx * elem;
    libc_memmove(base + (idx + 1) * elem, at, (h->count - idx) * elem);
    libc_memcpy_alt(at, key, elem);
    h->count++;
    return at;
}

// ---- install / remove "slinky_handler" in the client's netty pipeline ----
#include "slinky/jids.h"
FN(void *, get_minecraft, (Jvm *j));
FN(void *, java_call_minecraft_get_net_handler, (Jvm *j, void *mc));
FN(void *, java_get_net_handler_play_client_2, (Jvm *j, void *x));
FN(void *, java_get_network_manager, (Jvm *j, void *x));
FN(void *, java_call_channel_pipeline, (Jvm *j, void *x));
FN(void *, java_call_channel_pipeline_get, (Jvm *j, void *pipe, void *cls));
FN(void, java_call_channel_pipeline_add_before, (Jvm *j, void *pipe, const char *before, const char *name, jobject h));
FN(void, java_call_channel_pipeline_remove, (Jvm *j, void *pipe, void *cls));
extern "C" const char rdata_13ecf0[], rdata_13ecff[];
struct HClass { jclass cls; jmethodID ctor; };
static inline __attribute__((always_inline)) int pipeline(Jvm *j, void **out) {
    void *mc = get_minecraft(j);
    if (!mc) return 0x3fc;
    void *nh = java_call_minecraft_get_net_handler(j, mc);
    if (!nh) return 0x3fd;
    void *nm = java_get_network_manager(j, java_get_net_handler_play_client_2(j, nh));
    if (!nm) return 0x3fd;
    void *pipe = java_call_channel_pipeline(j, nm);
    if (!pipe) return 0x3fd;
    *out = pipe;
    return 0;
}
extern "C" int register_channel_class(Jvm *j, HClass *h) {
    void *pipe;
    int r = pipeline(j, &pipe);
    if (r) return r;
    if (java_call_channel_pipeline_get(j, pipe, h->cls)) return 0;
    JNIEnv *env = j->env;
    jobject o = (*env)->NewObject(env, h->cls, h->ctor);
    if (!o) return 0x7d5;
    java_call_channel_pipeline_add_before(j, pipe, rdata_13ecf0, rdata_13ecff, o);
    return (*env)->ExceptionCheck(env) ? 0x7d4 : 0;
}
extern "C" int unregister_channel_class(Jvm *j, HClass *h) {
    void *pipe;
    int r = pipeline(j, &pipe);
    if (r) return r;
    if (!java_call_channel_pipeline_get(j, pipe, h->cls)) return 0;
    JNIEnv *env = j->env;
    java_call_channel_pipeline_remove(j, pipe, h->cls);
    return (*env)->ExceptionOccurred(env) ? 0x7d4 : 0;
}

// ---- reach: entity mouse-over with the module's reach / hitbox expansion (piercing, ghost hand aware) ----
struct V3d { double x, y, z; };
struct Box6 { double v[6]; };
struct ReachA { Ctx *c; void *p8; double reach, expand; };
FN(bool, piercing_piercing_decide_which_2, (Ctx *c));                                      // piercing: blocks
FN(bool, ghost_hand_part, (Ctx *c, jobject e));                           // ghost hand: may hit through e
FN(int, piercing_piercing_decide_which, (Ctx *c, jobject e1, jobject e2, bool inside));  // piercing: 0/1 preferred, 2 none
FN(jobject, java_call_id_500, (Jvm *j, jobject e, double d, float pt));    // rayTrace
FN(jobject, java_call_entity_living_base_get_position, (Jvm *j, jobject e));                        // eye position
FN(V3d, java_get_vec3_x_coord, (Jvm *j, jobject v));
FN(jobject, java_get_moving_object_position, (Jvm *j, jobject mop));                      // hitVec
FN(double, vec3_distance, (V3d a, V3d b));
FN(V3d, java_get_entity_rotation_pitch, (Jvm *j, jobject e));                            // look vector
FN(V3d, vec3_scale, (V3d a, double s));
FN(V3d, vec3_add, (V3d a, V3d b));
FN(jobject, java_new_vec3_init, (Jvm *j, V3d v));
FN(jobject, java_get_world_2, (Jvm *j, void *w));                          // loaded entity list
FN(int, java_call_list_size, (Jvm *j, jobject l));
FN(int, java_get_moving_object_position_3, (Jvm *j, jobject mop));                          // typeOfHit ordinal
FN(jobject, java_call_list_get, (Jvm *j, jobject l, int i));
FN(bool, java_call_entity_can_be_collided_with, (Jvm *j, jobject e));                           // canBeCollidedWith
FN(jobject, java_get_entity, (Jvm *j, jobject e));                        // bounding box
FN(Box6, java_get_axis_aligned_bb_min_x, (Jvm *j, jobject bb));
FN(Box6, aabb_expand, (Box6 b, double d));                             // expand
FN(jobject, java_new_axis_aligned_bb_init, (Jvm *j, Box6 b));
FN(jobject, java_axis_aligned_bb_calculate_intercept, (Jvm *j, jobject bb, jobject from, jobject to));   // calculateIntercept
FN(bool, aabb_contains_point, (Box6 b, V3d p));                                // isVecInside
FN(V3d, aabb_clamp_point, (Box6 b, V3d p));                                 // clamp into box
FN(jobject, java_moving_object_position_init, (Jvm *j, jobject e, jobject v));             // new MovingObjectPosition
FN(void, java_set_minecraft, (Jvm *j, void *mc, jobject mop));               // objectMouseOver =
extern "C" const double rdata_13ed20, rdata_13ed30;
extern "C" const float rdata_13ed28;
#define RJ ((Jvm *)c->a)
extern "C" void reach_distance_check(ReachA *a) {
    Ctx *c = a->c;
    double reach = a->reach;
    double d = rdata_13ed20 + reach;
    bool pierce = piercing_piercing_decide_which_2(c);
    jobject mop = java_call_id_500(RJ, c->b, d, rdata_13ed28);
    jobject eyes = java_call_entity_living_base_get_position(RJ, c->b);
    V3d eye = java_get_vec3_x_coord(RJ, eyes);
    if (!pierce && mop) d = vec3_distance(eye, java_get_vec3_x_coord(RJ, java_get_moving_object_position(RJ, mop)));
    V3d endv = vec3_add(eye, vec3_scale(java_get_entity_rotation_pitch(RJ, c->b), d));
    jobject endo = java_new_vec3_init(RJ, endv);
    jobject list = java_get_world_2(RJ, c->p20);
    int n = java_call_list_size(RJ, list);
    bool blockHit = mop && java_get_moving_object_position_3(RJ, mop) == 1;
    if (n <= 0) {
        if (blockHit) java_set_minecraft(RJ, c->c18, mop);
        return;
    }
    jobject pointed = 0, hitv = 0;
    double closest = reach;
    bool found = false;
    for (int i = 0; i != n; i++) {
        jobject e = java_call_list_get(RJ, list, i);
        JNIEnv *env = RJ->env;
        if ((*env)->IsSameObject(env, (jobject)c->b, e)) continue;
        if (!java_call_entity_can_be_collided_with(RJ, e)) continue;
        Box6 bb = aabb_expand(java_get_axis_aligned_bb_min_x(RJ, java_get_entity(RJ, e)), a->expand);
        jobject ic = java_axis_aligned_bb_calculate_intercept(RJ, java_new_axis_aligned_bb_init(RJ, bb), eyes, endo);
        jobject np;
        double nd;
        if (aabb_contains_point(bb, eye)) {   // eye inside the (expanded) box
            found = true;
            np = pointed;
            nd = closest;
            if (closest >= 0.0 && !(pointed && piercing_piercing_decide_which(c, pointed, e, true) == 1)) {
                hitv = ic ? java_get_moving_object_position(RJ, ic) : eyes;
                np = e;
                nd = 0.0;
            }
        } else {
            if (!ic) continue;
            jobject hv = java_get_moving_object_position(RJ, ic);
            double dist = vec3_distance(eye, java_get_vec3_x_coord(RJ, hv));
            int r = piercing_piercing_decide_which(c, pointed, e, found);
            bool take = r == 2 ? closest == 0.0 || closest > dist : r == 0 && reach > dist;
            np = pointed;
            nd = closest;
            if (take) {
                hitv = hv;
                np = e;
                nd = dist;
            }
        }
        bool through = ghost_hand_part(c, np);
        blockHit = blockHit & through;
        closest = nd;
        pointed = np;
    }
    if (blockHit) {
        java_set_minecraft(RJ, c->c18, mop);
        return;
    }
    if (!pointed) return;
    if (!(pierce | !mop | !(closest > d))) return;
    Box6 bb = aabb_expand(java_get_axis_aligned_bb_min_x(RJ, java_get_entity(RJ, pointed)), rdata_13ed30);
    V3d p = aabb_clamp_point(bb, java_get_vec3_x_coord(RJ, hitv));
    java_set_minecraft(RJ, c->c18, java_moving_object_position_init(RJ, pointed, java_new_vec3_init(RJ, p)));
}
#undef RJ

// ---- counting allocator (the allocation counter at data_2f7348 is checked for leaks on unload) ----
FN(void *, libc_malloc, (unsigned long long n));                          // malloc
FN(void, libc_free, (void *p));                                         // free
FN(void *, libc_memset, (void *p, int c, unsigned long long n));          // memset
extern "C" long long data_2f7348;
extern "C" void *debug_alloc(unsigned long long n) {   // allocate n bytes filled with 0xaa
    void *p = libc_malloc(n);
    libc_memset(p, 0xaa, n);
    if (p) __atomic_fetch_add(&data_2f7348, 1, __ATOMIC_SEQ_CST);
    return p;
}
extern "C" void debug_free(void *p) {
    if (p) {
        __atomic_fetch_sub(&data_2f7348, 1, __ATOMIC_SEQ_CST);
        libc_free(p);
    }
}

// ---- define the netty channel handler class "SlinkyChannelHandler" from the embedded class bytes ----
// registers its native `onPacket(Object, boolean, long, int)` (implemented by `onPacket`), looks up the
// constructor and keeps a global ref of the class. 0x7d0: no constructor, 0x7d5: no global ref.
FN(int, define_class, (JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jint len, const JNINativeMethod *methods,
               jclass *out));
extern "C" const jbyte *rdata_13ed10;   // class file bytes of SlinkyChannelHandler
extern "C" const jint *rdata_13ed18;    // their length
extern "C" const char rdata_13ecae[], rdata_13ecb7[], rdata_13ecd0[], rdata_13ece5[], rdata_13ecec[];
extern "C" int install_packet_channel(Jvm *j, jobject loader, HClass *out, void *onPacket) {
    JNINativeMethod native = {(char *)rdata_13ecae /* "onPacket" */, (char *)rdata_13ecb7 /* "(Ljava/lang/Object;ZJI)I" */,
                              onPacket};
    JNIEnv *env = j->env;
    jclass cls;
    int r = define_class(env, rdata_13ecd0 /* "SlinkyChannelHandler" */, loader, rdata_13ed10, *rdata_13ed18, &native, &cls);
    if (r) return r;
    out->ctor = (*env)->GetMethodID(env, cls, rdata_13ece5 /* "<init>" */, rdata_13ecec /* "()V" */);
    if (!out->ctor) return 0x7d0;
    out->cls = (jclass)(*env)->NewGlobalRef(env, cls);
    return out->cls ? 0 : 0x7d5;
}
// drop the class again: unregister its natives and release the global ref
extern "C" void release_packet_channel(JNIEnv *env, jclass *cls) {
    if (!*cls) return;
    (*env)->UnregisterNatives(env, *cls);
    (*env)->DeleteGlobalRef(env, *cls);
    *cls = 0;
}

// grow a dynamic array when it is full (the capacity step of vector_push_back)
extern "C" void grow_dynamic_array_full(void **data, unsigned elem, AHdr *h) {
    if (__builtin_expect(h->count >= h->cap, 0)) agrow(data, elem, h);
}

// calloc / realloc with the same allocation counter
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));
FN(void *, libc_realloc, (void *p, unsigned long long n));
extern "C" void *debug_calloc(unsigned long long n, unsigned long long size) {
    void *p = libc_calloc(n, size);
    if (p) __atomic_fetch_add(&data_2f7348, 1, __ATOMIC_SEQ_CST);
    return p;
}
extern "C" void *debug_realloc(void *old, unsigned long long n) {
    bool had = old != 0;
    void *p = libc_realloc(old, n);
    if (!(p == 0 || had)) __atomic_fetch_add(&data_2f7348, 1, __ATOMIC_SEQ_CST);
    return p;
}
