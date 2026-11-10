// 0x1f000: class-file constant pool getters (single exit, like the original)
#include "slinky/engine.h"
#include "slinky/classfile.h"
// CONSTANT_Utf8
extern "C" __attribute__((noinline)) int constant_utf8(CP *cp, unsigned short idx, Utf8 *out) {
    const unsigned char *e, *lim;
    int r = 0x2afd;
    CP_ENTRY(cp, idx, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    r = 0x2afe;
    if (*e != 1) goto out_;
    {
        const unsigned char *p = e + 3;
        if (p > lim) goto trunc;
        unsigned short len = be16(e + 1);
        r = 0x2af8;
        if (p + len > lim) goto out_;
        r = 0;
        if (out) {
            out->len = len;
            out->p = p;
        }
    }
    goto out_;
trunc:
    r = 0x2af8;
out_:

    return r;
}
// CONSTANT_Class -> its name
extern "C" __attribute__((noinline)) int constant_class_name(CP *cp, unsigned short idx, Utf8 *out) {
    const unsigned char *e, *lim, *u, *ulim;
    int r = 0x2afd;
    CP_ENTRY(cp, idx, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    if (*e != 7) goto tag;
    if (e + 3 > lim) goto trunc;
    {
        unsigned short ni = be16(e + 1);
        CP_ENTRY(cp, ni, u, ulim, out_);
        if (u + 1 > ulim) goto trunc;
        r = 0x2afe;
        if (*u != 1) goto out_;
        const unsigned char *p = u + 3;
        if (p > ulim) goto trunc;
        unsigned short len = be16(u + 1);
        r = 0x2af8;
        if (p + len > ulim) goto out_;
        r = 0;
        if (out) {
            out->len = len;
            out->p = p;
        }
    }
    goto out_;
tag:
    r = 0x2afe;
    goto out_;
trunc:
    r = 0x2af8;
out_:

    return r;
}
// CONSTANT_NameAndType -> name + descriptor
extern "C" __attribute__((noinline)) int constant_name_and_type(CP *cp, unsigned short idx, NameType *out) {
    const unsigned char *e, *lim, *u, *ulim;
    int r = 0x2afd;
    CP_ENTRY(cp, idx, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    if (*e != 0xc) goto tag;
    if (e + 3 > lim || e + 5 > lim) goto trunc;
    {
        unsigned short ni = be16(e + 1);
        CP_ENTRY(cp, ni, u, ulim, out_);
        if (u + 1 > ulim) goto trunc;
        r = 0x2afe;
        if (*u != 1) goto out_;
        const unsigned char *p = u + 3;
        if (p > ulim) goto trunc;
        unsigned short len = be16(u + 1);
        r = 0x2af8;
        if (p + len > ulim) goto out_;
        unsigned short di = be16(e + 3);
        if (out) {
            out->name.len = len;
            out->name.p = p;
        }
        return constant_utf8(cp, di, &out->desc);
    }
tag:
    r = 0x2afe;
    goto out_;
trunc:
    r = 0x2af8;
out_:

    return r;
}

struct WBuf { unsigned char *p, *end; };
extern "C" int classfile_write_u1(WBuf *w, unsigned char b) {
    unsigned char *p = w->p;
    if (p + 1 > w->end) return 0x2af9;
    *p = b;
    w->p = p + 1;
    return 0;
}

struct RBuf { const unsigned char *p, *end; };
extern "C" int classfile_read_u1(RBuf *r, unsigned char *out) {
    const unsigned char *p = r->p;
    if (p + 1 > r->end) return 0x2af8;
    if (out) *out = *p;
    r->p = p + 1;
    return 0;
}
extern "C" int classfile_read_u2(RBuf *r, unsigned short *out) {
    const unsigned char *p = r->p;
    if (p + 2 > r->end) return 0x2af8;
    if (out) *out = be16(p);
    r->p = p + 2;
    return 0;
}
extern "C" int classfile_write_u2(WBuf *w, unsigned short v) {
    unsigned char *p = w->p;
    if (p + 2 > w->end) return 0x2af9;
    p[0] = (unsigned char)(v >> 8);
    w->p[1] = (unsigned char)v;
    w->p = p + 2;
    return 0;
}

#include "slinky/jnienv.h"
extern "C" const char rdata_14d24a[], rdata_14d254[];   // "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;"
struct LoaderRef { jobject loader; jmethodID loadClass; };
extern "C" int make_secure_class_loader(LoaderRef *o, JNIEnv *env, jobject loader) {
    jclass cls = (*env)->GetObjectClass(env, loader);
    jmethodID mid = (*env)->GetMethodID(env, cls, rdata_14d24a, rdata_14d254);
    if (!mid) return 0x7d4;
    jobject ref = (*env)->NewGlobalRef(env, loader);
    if (!ref) return 0x7d5;
    o->loader = ref;
    o->loadClass = mid;
    return 0;
}

FN(int, constant_class_name, (CP *cp, unsigned short idx, Utf8 *out));
// CONSTANT_Methodref / InterfaceMethodref (tags 10, 11) -> class, name, descriptor
extern "C" int constant_pool_member_ref2(CP *cp, unsigned short idx, MemberRef *out) {
    const unsigned char *e, *lim;
    int r = 0x2afd;
    CP_ENTRY(cp, idx, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    r = 0x2afe;
    if ((*e & 0xfe) != 10) goto out_;
    if (e + 3 > lim) goto trunc;
    r = 0x2af8;
    if (e + 5 > lim) goto out_;
    {
        unsigned short ci = be16(e + 1);
        unsigned char hi = e[3], lo = e[4];
        r = constant_class_name(cp, ci, &out->cls);
        if (r) goto out_;
        return constant_name_and_type(cp, (unsigned short)((hi << 8) | lo), &out->nt);
    }
trunc:
    r = 0x2af8;
out_:
    return r;
}

FN(jclass, find_class, (jobject loader, JNIEnv *env, const char *name));
extern "C" const char rdata_14d27a[], rdata_14d2b4[], rdata_14d2c2[], rdata_14d2f5[], rdata_14d322[],
    rdata_14d299[], rdata_14d29d[], rdata_14d29f[], rdata_14d2ae[], rdata_14d2b2[];
struct Mapping { int player; int names; unsigned char b8; };   // player: 0 = ClientPlayerMP, 1 = PlayerSP; names: 0/1 obf, 1 MCP, 2 SRG
// detect which mapping set the game uses (SRG / MCP / obfuscated 1.7 / 1.8)
extern "C" int fill_jni_ids(Mapping *out, jobject loader, JNIEnv *env) {
    jclass mc = find_class(loader, env, rdata_14d27a);   // net.minecraft.client.Minecraft
    if (mc) {
        if ((*env)->GetFieldID(env, mc, rdata_14d2b4, rdata_14d2c2)) { *(long long *)out = 0x200000000ll; return 0; }
        (*env)->ExceptionClear(env);
        if ((*env)->GetFieldID(env, mc, rdata_14d2b4, rdata_14d2f5)) { *(long long *)out = 0x200000001ll; return 0; }
        (*env)->ExceptionClear(env);
        if ((*env)->GetFieldID(env, mc, rdata_14d322, rdata_14d2c2)) { *(long long *)out = 0x100000000ll; out->b8 = 0; return 0; }
        (*env)->ExceptionClear(env);
        if ((*env)->GetFieldID(env, mc, rdata_14d322, rdata_14d2f5)) { *(long long *)out = 0x100000001ll; out->b8 = 0; return 0; }
        (*env)->ExceptionClear(env);
        return 0x3eb;
    }
    (*env)->ExceptionClear(env);
    jclass c = find_class(loader, env, rdata_14d299);   // "bao" (1.7 obf)
    if (c && (*env)->GetFieldID(env, c, rdata_14d29d, rdata_14d29f)) { *(long long *)out = 0; return 0; }
    (*env)->ExceptionClear(env);
    c = find_class(loader, env, rdata_14d2ae);          // "ave" (1.8 obf)
    if (c && (*env)->GetFieldID(env, c, rdata_14d2b2, rdata_14d29f)) { *(long long *)out = 1; return 0; }
    (*env)->ExceptionClear(env);
    return 0x3eb;
}

extern "C" const char rdata_14d160[], rdata_14d1cf[], rdata_14d1e1[], rdata_14d1f3[], rdata_14d201[], rdata_14d208[],
    rdata_14d171[], rdata_14d186[], rdata_14d18e[], rdata_14d1a4[], rdata_14d1ac[], rdata_14d1c1[], rdata_14d21a[], rdata_14d230[];
extern "C" int libc_strcmp(const char *a, const char *b);   // strcmp
// find the game's "Client thread" and keep a global ref to its context class loader (+ loadClass)
extern "C" int find_game_class_loader(LoaderRef *out, JNIEnv *env) {
    jclass T = (*env)->FindClass(env, rdata_14d160);
    if (!T) return 0x7d0;
    jmethodID all = (*env)->GetStaticMethodID(env, T, rdata_14d1cf, rdata_14d1e1);
    if (!all) return 0x7d0;
    jclass M = (*env)->FindClass(env, rdata_14d1f3);
    if (!M) return 0x7d0;
    jmethodID keySet = (*env)->GetMethodID(env, M, rdata_14d201, rdata_14d208);
    if (!keySet) return 0x7d0;
    jobject map = (*env)->CallStaticObjectMethod(env, T, all);
    jobject set = (*env)->CallObjectMethod(env, map, keySet);
    jclass C = (*env)->FindClass(env, rdata_14d171);
    if (!C) return 0x7d0;
    jmethodID toArray = (*env)->GetMethodID(env, C, rdata_14d186, rdata_14d18e);
    if (!toArray) return 0x7d0;
    jmethodID getName = (*env)->GetMethodID(env, T, rdata_14d1a4, rdata_14d1ac);
    if (!getName) return 0x7d0;
    jobjectArray arr = (jobjectArray)(*env)->CallObjectMethod(env, set, toArray);
    jsize n = (*env)->GetArrayLength(env, arr);
    if (n <= 0) return 0x3ea;
    jobject th;
    for (int i = 0;; i++) {
        th = (*env)->GetObjectArrayElement(env, arr, i);
        jstring name = (jstring)(*env)->CallObjectMethod(env, th, getName);
        const char *s = (*env)->GetStringUTFChars(env, name, 0);
        int c = libc_strcmp(s, rdata_14d1c1);   // "Client thread"
        (*env)->ReleaseStringUTFChars(env, name, s);
        if (c == 0) break;
        if (n == i + 1) return 0x3ea;
    }
    jmethodID gccl = (*env)->GetMethodID(env, T, rdata_14d21a, rdata_14d230);
    if (!gccl) return 0x7d0;
    jobject loader = (*env)->CallObjectMethod(env, th, gccl);
    jclass lc = (*env)->GetObjectClass(env, loader);
    jmethodID load = (*env)->GetMethodID(env, lc, rdata_14d24a, rdata_14d254);
    if (!load) return 0x7d4;
    jobject ref = (*env)->NewGlobalRef(env, loader);
    if (!ref) return 0x7d5;
    out->loader = ref;
    out->loadClass = load;
    return 0;
}
extern "C" void *class_loader_ref(void **p) { return *p; }
// load class `name` (slashes or dots) through the game's class loader: loader.loadClass(name with dots)
FN(unsigned long long, libc_strlen, (const char *s));
FN(void *, libc_malloc, (unsigned long long n));   // malloc
FN(void, libc_free, (void *p));                   // free
extern "C" __attribute__((noinline)) jclass find_class(jobject lr, JNIEnv *env, const char *name) {
    LoaderRef *o = (LoaderRef *)lr;
    unsigned long long n = libc_strlen(name);
    char *buf = (char *)libc_malloc(n + 1);
    if (!buf) return 0;
#pragma clang loop vectorize(disable) interleave(disable)
    for (unsigned long long i = 0; i < n; i++) buf[i] = name[i] == '/' ? '.' : name[i];
    buf[n] = 0;
    jstring s = (*env)->NewStringUTF(env, buf);
    libc_free(buf);
    return (jclass)(*env)->CallObjectMethod(env, o->loader, o->loadClass, s);
}

// ---- gap-scan functions of the class-file reader block ----
// release a global ref: DeleteGlobalRef(env, *ref); *ref = 0
extern "C" void class_file_scan(jobject *ref, JNIEnv *env) {
    if (*ref) {
        (*env)->DeleteGlobalRef(env, *ref);
        *ref = 0;
    }
}

// which Minecraft is it? the obfuscated main class "bao" (field I: File) or "ave" (field N: File)  ->  *kind 0 / 1
// (0x3eb: neither found)
extern "C" const char rdata_14d299[], rdata_14d29d[], rdata_14d29f[], rdata_14d2ae[], rdata_14d2b2[];
extern "C" int class_define(long long *kind, jobject loader, JNIEnv *env) {
    jclass c = find_class(loader, env, rdata_14d299 /* "bao" */);
    if (c && (*env)->GetFieldID(env, c, rdata_14d29d /* "I" */, rdata_14d29f /* "Ljava/io/File;" */)) {
        *kind = 0;
        return 0;
    }
    (*env)->ExceptionClear(env);
    c = find_class(loader, env, rdata_14d2ae /* "ave" */);
    if (c && (*env)->GetFieldID(env, c, rdata_14d2b2 /* "N" */, rdata_14d29f)) {
        *kind = 1;
        return 0;
    }
    (*env)->ExceptionClear(env);
    return 0x3eb;
}

FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));
// read n bytes into dst (may be NULL to skip) / a big-endian u32
extern "C" int classfile_read_bytes(RBuf *r, unsigned n, void *dst) {
    const unsigned char *p = r->p, *e = p + n;
    if (e > r->end) return 0x2af8;
    if (dst) libc_memcpy_alt(dst, p, n);
    r->p = e;
    return 0;
}
extern "C" int classfile_read_constant(RBuf *r, unsigned *out) {
    const unsigned char *p = r->p, *e = p + 4;
    if (e > r->end) return 0x2af8;
    if (out) *out = __builtin_bswap32(*(const unsigned *)p);
    r->p = e;
    return 0;
}

// Fieldref/Methodref-style entry whose tag satisfies `pred` -> class, name, descriptor (general form of constant_pool_member_ref/constant_pool_member_ref2)
FN(int, constant_name_and_type, (CP *cp, unsigned short idx, NameType *out));
extern "C" int constant_pool_find_if(CP *cp, bool (*pred)(unsigned char), unsigned short idx, MemberRef *out) {
    const unsigned char *e, *lim;
    int r = 0x2afd;
    CP_ENTRY(cp, idx, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    {
        unsigned char tag = *e;
        r = 0x2afe;
        if (!pred(tag)) goto out_;
    }
    if (e + 3 > lim) goto trunc;
    r = 0x2af8;
    if (e + 5 > lim) goto out_;
    {
        unsigned short ci = be16(e + 1);
        unsigned char hi = e[3], lo = e[4];
        r = constant_class_name(cp, ci, &out->cls);
        if (r) goto out_;
        return constant_name_and_type(cp, (unsigned short)((hi << 8) | lo), &out->nt);
    }
trunc:
    r = 0x2af8;
out_:
    return r;
}
