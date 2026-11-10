// 0x20000: class-file patching helpers (member refs, name compares)
#include "slinky/engine.h"
#include "slinky/classfile.h"
FN(int, constant_class_name, (CP *cp, unsigned short idx, Utf8 *out));
FN(int, constant_name_and_type, (CP *cp, unsigned short idx, NameType *out));
FN(int, libc_strncmp_alt, (const void *a, const void *b, unsigned long long n));
// CONSTANT_Fieldref/Methodref (tag 9) -> class, name, descriptor
extern "C" int constant_pool_member_ref(CP *cp, unsigned short idx, MemberRef *out) {
    const unsigned char *e, *lim;
    int r = 0x2afd;
    CP_ENTRY(cp, idx, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    r = 0x2afe;
    if (*e != 9) goto out_;
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
// compare a constant-pool string with a C string (strncmp semantics, bounded by len + 1);
// `k` is the compile-time copy of `s` used for the length (lets a literal fold like the original)
static inline __attribute__((always_inline)) int utf8_cmpk(const Utf8 *u, const char *s, const char *k) {
    unsigned long long n;
    if (!k[0]) {
        n = 0;
    } else {
        unsigned long long lim = (unsigned long long)u->len + 1;
        unsigned long long i = 0;
        for (;;) {
            if (lim == i) { n = lim; break; }
            if (!k[i + 1]) { n = i + 1; break; }
            i++;
        }
    }
    return libc_strncmp_alt(u->p, s, n);
}
static inline __attribute__((always_inline)) int utf8_cmp(const Utf8 *u, const char *s) {
    unsigned long long n;
    if (!s[0]) {
        n = 0;
    } else {
        unsigned long long lim = (unsigned long long)u->len + 1;
        unsigned long long i = 0;
        for (;;) {
            if (lim == i) { n = lim; break; }
            if (!s[i + 1]) { n = i + 1; break; }
            i++;
        }
    }
    return libc_strncmp_alt(u->p, s, n);
}
extern "C" int compare_utf8(const Utf8 *u, const char *s) { return utf8_cmp(u, s); }
// does the NameAndType match {name, desc}?
extern "C" bool compare_member_name(const NameType *nt, const char *const *want) {
    if (utf8_cmp(&nt->name, want[0]) != 0) return false;
    return utf8_cmp(&nt->desc, want[1]) == 0;
}

// ---- class-file writer: bounded output cursor, every put fails with 0x2af9 when it would overflow ----
struct W { unsigned char *p, *end; };
extern "C" __attribute__((pure)) unsigned long long libc_strlen(const char *s);   // strlen
FN(void *, libc_memcpy_alt, (void *d, const void *s, unsigned long long n));
#define W_PUT8(w, v)                                     \
    do {                                                 \
        unsigned char *q_ = (w)->p + 1;                  \
        if (q_ > (w)->end) return 0x2af9;                \
        *(w)->p = (unsigned char)(v);                    \
        (w)->p = q_;                                     \
    } while (0)
#define W_PUT16(w, v)                                    \
    do {                                                 \
        unsigned char *q_ = (w)->p + 2;                  \
        if (q_ > (w)->end) return 0x2af9;                \
        *(w)->p = (unsigned char)((v) >> 8);             \
        (w)->p[1] = (unsigned char)(v);                  \
        (w)->p = q_;                                     \
    } while (0)
// append CONSTANT_Utf8 `s` and a CONSTANT_Class naming it; *cls = the class entry's index
extern "C" int append_constant_utf8_s_constant(W *w, const char *s, unsigned short *count, unsigned short *cls) {
    unsigned short u = (*count)++;
    W_PUT8(w, 1);
    unsigned char *q = w->p + 2;
    if (q > w->end) return 0x2af9;
    unsigned short n = (unsigned short)libc_strlen(s);
    *w->p = (unsigned char)(n >> 8);
    w->p[1] = (unsigned char)n;
    w->p = q;

    unsigned char *e = q + n;
    if (e > w->end) return 0x2af9;
    libc_memcpy_alt(q, s, n);
    w->p = e;
    unsigned c = *count;
    *count = (unsigned short)(c + 1);
    *cls = (unsigned short)c;
    unsigned char *t = e + 1;
    if (t > w->end) return 0x2af9;
    *e = 7;
    w->p = t;
    q = e + 3;
    if (q > w->end) return 0x2af9;
    *t = (unsigned char)(u >> 8);
    w->p[1] = (unsigned char)u;
    w->p = q;
    return 0;
}
// resolve two CONSTANT_Utf8 entries into {name, desc} and hand them to cb(a, &nt, b, c)
typedef int (*NtCb)(void *a, NameType *nt, void *b, void *c);
extern "C" int constant_pool_name_and_type(CP *cp, unsigned short ni, unsigned short di, NtCb cb, void *a, void *b, void *c) {
    const unsigned char *e, *lim, *u, *ulim;
    int r = 0x2afd;
    CP_ENTRY(cp, ni, e, lim, out_);
    if (e + 1 > lim) goto trunc;
    if (*e != 1) goto tag;

    {
        const unsigned char *p = e + 3;
        if (p > lim) goto trunc;
        unsigned short len = be16(e + 1);
        if (p + len > lim) goto trunc;
        CP_ENTRY(cp, di, u, ulim, out_);
        if (u + 1 > ulim) goto trunc;
        r = 0x2afe;
        if (*u != 1) goto out_;

        const unsigned char *q = u + 3;
        if (q > ulim) goto trunc;
        unsigned short dl = be16(u + 1);
        r = 0x2af8;
        if (q + dl > ulim) goto out_;
        NameType nt = {{p, len}, {q, dl}};
        return cb(a, &nt, b, c);
    }
tag:
    r = 0x2afe;
    goto out_;
trunc:
    r = 0x2af8;
out_:
    return r;
}
// is constant-pool entry idx the Utf8 "Code"?
extern "C" const char rdata_14d380[];   // "Code"
extern "C" int constant_pool_utf8_member(CP *cp, unsigned short idx, bool *out) {
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
        Utf8 u = {p, len};
        *out = utf8_cmpk(&u, rdata_14d380, "Code") == 0;
        r = 0;
        goto out_;
    }
trunc:
    r = 0x2af8;
out_:

    return r;
}

// ---- bytecode walker: copies each instruction's span to `out` and hands both spans to cb ----
struct Span { const unsigned char *p, *end; };
typedef int (*InsCb)(void *ctx, void *a, Span *ins, Span *dst);
extern "C" const unsigned char rdata_14d390[];   // opcode -> instruction length (0 = invalid)
#define IN_SKIP(in, n)                                   \
    do {                                                 \
        const unsigned char *q_ = (in)->p + (n);         \
        if (q_ > (in)->end) return 0x2af8;               \
        (in)->p = q_;                                    \
    } while (0)
static inline __attribute__((always_inline)) unsigned be32(const unsigned char *p) {
    return __builtin_bswap32(*(const unsigned *)p);
}
extern "C" int rewrite_span(Span *in, W *out, const unsigned char *code, void *a, InsCb cb, void *ctx) {
    for (;;) {
        const unsigned char *p = in->p;
        if (p >= in->end) return 0;
        unsigned char *dst = out->p;
        unsigned char op = *p;
        in->p = p + 1;
        const unsigned char *cur, *nx;
        switch (op) {
        case 0xc4: {   // wide: opcode + u16 index (+ s16 constant for iinc)
            const unsigned char *s = in->p;
            cur = s + 1;
            if (cur > in->end) return 0x2af8;
            nx = cur + (*s == 0x84 ? 4 : 2);
            goto skip;
        }
        case 0xab: {   // lookupswitch: pad, default, npairs, npairs * {match, offset}
            unsigned pad = (unsigned)(in->p - code) & 3;
            if (pad) IN_SKIP(in, 4 - pad);
            IN_SKIP(in, 4);
            const unsigned char *l = in->p;
            cur = l + 4;
            if (cur > in->end) return 0x2af8;
            nx = cur + (unsigned)(be32(l) << 3);
            goto skip;
        }
        case 0xaa: {   // tableswitch: pad, default, low, high, (high - low + 1) offsets
            unsigned pad = (unsigned)(in->p - code) & 3;
            if (pad) IN_SKIP(in, 4 - pad);
            IN_SKIP(in, 4);
            const unsigned char *l = in->p;
            if (l + 4 > in->end) return 0x2af8;
            unsigned lo = (unsigned)l[0] << 24 | (unsigned)l[1] << 16 | (unsigned)l[2] << 8 | l[3];
            in->p = l + 4;
            cur = l + 8;
            if (cur > in->end) return 0x2af8;
            nx = cur + (unsigned)((be32(l + 4) - lo) * 4 + 4);
            goto skip;
        }
        default:
            if (op > 0xc9) return 0x2aff;
            IN_SKIP(in, (unsigned)(rdata_14d390[op] - 1));
            break;
        skip:
            in->p = nx <= in->end ? nx : cur;
            if (nx > in->end) return 0x2af8;
            break;
        }
        const unsigned char *e = in->p;
        unsigned n = (unsigned)(e - p);
        unsigned char *o = out->p + n;
        if (o > out->end) return 0x2af9;
        out->p = o;
        Span ins = {p, e}, os = {dst, o};
        int r = cb(ctx, a, &ins, &os);
        if (r) return r;
    }
}
// copy a Code attribute's u4 code_length + code through the walker (the length word is copied verbatim)
extern "C" int copy_code_attribute_s_u4(Span *in, W *out, void *a, InsCb cb, void *ctx) {
    const unsigned char *p = in->p, *iend = in->end;
    const unsigned char *l = p + 4;
    if (l > iend) return 0x2af8;
    in->p = l;
    const unsigned char *c = p + 8;
    if (c > iend) return 0x2af8;
    unsigned char b0 = l[0], b1 = l[1], b2 = l[2], b3 = l[3];
    in->p = c;
    unsigned char *op = out->p, *oend = out->end;
    unsigned char *o = op + 8;
    if (o > oend) return 0x2af9;
    unsigned long long n = (unsigned)(b1 << 16 | b0 << 24 | b2 << 8) | (unsigned long long)b3;
    out->p = o;
    Span src = {c, c + n};
    W dst = {o, o + n};
    if (src.end > iend) return 0x2af8;
    in->p = src.end;
    if (dst.end > oend) return 0x2af9;
    out->p = dst.end;
    return rewrite_span(&src, &dst, c, a, cb, ctx);
}

// copy the methods table; when a constant pool is given, `pick` chooses per method an instruction
// callback (+ ctx) and the method's Code attribute is rewritten through it
extern "C" int copy_methods_table_constant_pool(Span *in, W *out, CP *cp, NtCb pick, void *pctx) {
    const unsigned char *p = in->p;
    if (p + 2 > in->end) return 0x2af8;
    unsigned char c0 = p[0], c1 = p[1];
    in->p = p + 2;
    unsigned char *o = out->p;
    if (o + 2 > out->end) return 0x2af9;
    *o = c0;
    out->p[1] = c1;
    out->p = o + 2;
    unsigned short cnt = (unsigned short)(c0 << 8 | c1);
    if (!cnt) return 0;
    unsigned short m = 0;
    do {
        const unsigned char *h = in->p, *hend = in->end;
        if (h + 2 > hend) return 0x2af8;
        in->p = h + 2;
        if (h + 4 > hend) return 0x2af8;
        unsigned short ni = *(const unsigned short *)(h + 2);
        in->p = h + 4;
        if (h + 6 > hend) return 0x2af8;
        unsigned short di = *(const unsigned short *)(h + 4);
        in->p = h + 6;
        if (h + 8 > hend) return 0x2af8;
        unsigned char a0 = h[6], a1 = h[7];
        in->p = h + 8;
        unsigned char *w = out->p;
        if (w + 8 > out->end) return 0x2af9;
        __builtin_memcpy(w, h, 8);
        out->p = w + 8;
        InsCb cb = 0;
        void *cbctx = 0;
        if (cp) {
            int r = constant_pool_name_and_type(cp, (unsigned short)(ni << 8 | ni >> 8), (unsigned short)(di << 8 | di >> 8), pick,
                             pctx, &cb, &cbctx);
            if (r) return r;
        }
        unsigned short na = (unsigned short)((a0 << 8) + a1);
        for (; na; na--) {
            const unsigned char *q = in->p, *qend = in->end;
            if (q + 2 > qend) return 0x2af8;
            InsCb acb = cb;
            void *actx = cbctx;
            unsigned short an = *(const unsigned short *)q;
            in->p = q + 2;
            if (q + 6 > qend) return 0x2af8;
            unsigned long long len = (unsigned)((unsigned)q[2] << 24 | (unsigned)q[3] << 16 | (unsigned)q[4] << 8) | (unsigned long long)q[5];
            in->p = q + 6;
            unsigned char *ah = out->p;
            unsigned char *body = ah + 6;
            if (body > out->end) return 0x2af9;
            __builtin_memcpy(ah, q, 6);
            out->p = body;
            const unsigned char *src = in->p;
            if (src + len > in->end) return 0x2af8;
            in->p = src + len;
            unsigned char *bend = body + len;
            if (bend > out->end) return 0x2af9;
            if (src) libc_memcpy_alt(body, src, len);
            out->p = bend;
            if (!acb) continue;
            bool code;
            int r = constant_pool_utf8_member(cp, (unsigned short)(an << 8 | an >> 8), &code);
            if (!r && code) {
                Span s2 = {src, in->p};
                W w2 = {body, bend};
                r = copy_code_attribute_s_u4(&s2, &w2, cp, acb, actx);
            }
            if (r) return r;
        }
    } while (++m != cnt);
    return 0;
}

// ---- class transformer: rewrite a class file, adding per hook a Utf8 name/desc, NameAndType and a
// Methodref on the class itself (hooks[i].ref = its index), and rewriting methods through `pick` ----
struct MHook { const char *name, *desc; unsigned short ref; };
FN(void *, libc_malloc, (long long n));                                         // malloc
FN(void *, libc_calloc, (unsigned long long n, unsigned long long size));   // calloc
FN(void, libc_free, (void *p));                                              // free
extern "C" int rewrite_class_file(const unsigned char *cls, int len, unsigned char **outBuf, int *outLen, const char *clsName,
                        MHook *hooks, int nh, NtCb pick, void *pctx) {
    long long cap = len + 0x1000;
    unsigned char *buf = (unsigned char *)libc_malloc(cap);
    if (!buf) return 0x3ed;
    const unsigned char *end = cls + len;
    unsigned char *oend = buf + cap;
    const unsigned char **ents;
    int r = 0x2af8;
    if (len < 4) goto free_buf;
    r = 0x2afa;
    if (be32(cls) != 0xcafebabe) goto free_buf;
    r = 0x2af8;
    if ((unsigned)len < 8) goto free_buf;
    r = 0x2afb;
    if (be16(cls + 6) > 0x45) goto free_buf;   // newer than Java 1 (major 69)
    __builtin_memcpy(buf, cls, 8);
    r = 0x2af8;
    if ((unsigned)len < 10) goto free_buf;
    {
        unsigned short cpn = be16(cls + 8);
        Span in = {cls + 10, end};
        unsigned nc = cpn + nh * 4 + 2;
        buf[8] = (unsigned char)(nc >> 8);
        buf[9] = (unsigned char)nc;
        W out = {buf + 10, oend};
        unsigned short n1 = cpn - 1;
        ents = (const unsigned char **)libc_calloc(n1, 8);
        if (!ents) {
            r = 0x3ed;
            goto free_buf;
        }
        // index the constant pool (long/double take two slots)
        const unsigned char *p = cls + 10;
        for (unsigned short i = 0; i < n1;) {
            ents[i] = p;
            const unsigned char *q = p + 1;
            if (q > end) { r = 0x2af8; goto free_both; }
            unsigned char tag = *p;
            in.p = q;
            r = 0x2afc;
            unsigned long long sz;
            switch (tag) {
            case 3: case 4: case 9: case 10: case 11: case 12: case 17: case 18: sz = 4; break;
            case 7: case 8: case 16: case 19: case 20: sz = 2; break;
            case 5: case 6: sz = 8; break;
            case 15: sz = 3; break;
            case 1:
                q = p + 3;
                if (q > end) { r = 0x2af8; goto free_both; }
                q += (p[1] << 8) + p[2];
                r = 0x2af8;
                if (q > end) goto free_both;
                in.p = q;
                i++;
                p = q;
                continue;
            default: goto free_both;
            }
            q += sz;
            r = 0x2af8;
            if (q > end) goto free_both;
            in.p = q;
            i += (unsigned char)(tag - 5) < 2;
            i++;
            p = q;
        }
        unsigned cpl = (unsigned)(p - (cls + 10));
        unsigned char *ce = buf + 10 + cpl;
        if (ce > oend) { r = 0x2af9; goto free_both; }
        libc_memcpy_alt(buf + 10, cls + 10, cpl);
        out.p = ce;
        unsigned short count = cpn, clsIdx;
        r = append_constant_utf8_s_constant(&out, clsName, &count, &clsIdx);
        if (r) goto free_both;
        if (nh > 0) {
            unsigned char *w = out.p;
            if (w + 1 > oend) { r = 0x2af9; goto free_both; }
            unsigned c = count;
            for (unsigned long long k = 0;;) {
                const char *nm = hooks[k].name;
                w[0] = 1;
                unsigned char *s = w + 3;
                if (s > oend) { r = 0x2af9; goto free_both; }
                unsigned short l = (unsigned short)libc_strlen(nm);
                w[1] = (unsigned char)(l >> 8);
                w[2] = (unsigned char)l;
                unsigned char *t = s + l;
                if (t > oend) { r = 0x2af9; goto free_both; }
                libc_memcpy_alt(s, nm, l);
                if (t + 1 > oend) { r = 0x2af9; goto free_both; }
                const char *ds = hooks[k].desc;
                t[0] = 1;
                s = t + 3;
                if (s > oend) { r = 0x2af9; goto free_both; }
                l = (unsigned short)libc_strlen(ds);
                *(unsigned short *)(t + 1) = __builtin_bswap16(l);
                unsigned char *u = s + l;
                if (u > oend) { r = 0x2af9; goto free_both; }
                libc_memcpy_alt(s, ds, l);
                if (u + 1 > oend) { r = 0x2af9; goto free_both; }
                u[0] = 0xc;   // NameAndType {c, c + 1}
                if (u + 3 > oend) { r = 0x2af9; goto free_both; }
                *(unsigned short *)(u + 1) = __builtin_bswap16((unsigned short)c);
                if (u + 5 > oend) { r = 0x2af9; goto free_both; }
                u[3] = (unsigned char)((c + 1) >> 8);
                u[4] = (unsigned char)(c + 1);
                hooks[k].ref = (unsigned short)(c + 3);
                if (u + 6 > oend) { r = 0x2af9; goto free_both; }
                u[5] = 0xa;   // Methodref {clsIdx, c + 2}
                if (u + 8 > oend) { r = 0x2af9; goto free_both; }
                u[6] = (unsigned char)(clsIdx >> 8);
                u[7] = (unsigned char)clsIdx;
                w = u + 10;
                if (w > oend) { r = 0x2af9; goto free_both; }
                u[8] = (unsigned char)((c + 2) >> 8);
                u[9] = (unsigned char)(c + 2);
                if (++k == (unsigned)nh) break;
                c += 4;
                if (w + 1 > oend) { r = 0x2af9; goto free_both; }
            }
            out.p = w;
        }
        CP cp;
        cp.e = ents;
        cp.n = n1;
        cp.end = p;
        // access, this, super, interfaces
        const unsigned char *h = p;
        r = 0x2af8;
        if (h + 6 > end) goto free_cp;
        in.p = h + 6;
        if (h + 8 > end) goto free_cp;
        {
            unsigned long long ic = (unsigned long long)(h[6] << 9) + (unsigned long long)(h[7] * 2);
            const unsigned char *ie = h + 8 + ic;
            in.p = ie > end ? h + 8 : ie;
            if (ie > end) goto free_cp;
            unsigned char *we = out.p + ic + 8;
            r = 0x2af9;
            if (we > oend) goto free_cp;
            libc_memcpy_alt(out.p, h, ic + 8);
            out.p = we;
        }
        {
            int e2 = copy_methods_table_constant_pool(&in, &out, 0, 0, 0);   // fields
            if (e2) { r = e2; goto free_cp; }
            e2 = copy_methods_table_constant_pool(&in, &out, &cp, pick, pctx);   // methods
            if (e2) { r = e2; goto free_cp; }
        }
        {
            const unsigned char *rest = in.p;
            unsigned rn = (unsigned)(end - rest);
            unsigned char *fe = out.p + rn;
            if (fe > oend) {
                fe = out.p;
            } else {
                r = 0;
                if (rest) libc_memcpy_alt(out.p, rest, rn);
            }
            *outBuf = buf;
            *outLen = (int)(fe - buf);
        }
    free_cp:
        libc_free(cp.e);
        if (!r) return 0;
        goto free_buf;
    }
free_both:
    libc_free(ents);
free_buf:
    libc_free(buf);
    return r;
}

// ---- gap-scan functions of the class transformer ----
// size of what rewrite_class_file appends to the constant pool: the class entries plus, per hook, a Utf8 name, a Utf8 descriptor,
// a NameAndType and a Methodref
extern "C" int gap_scan_functions_class_transformer(const char *clsName, MHook *hooks, int nh) {
    int n = (int)libc_strlen(clsName) + 6;
    for (int i = 0; i < nh; i++) n += (int)libc_strlen(hooks[i].name) + (int)libc_strlen(hooks[i].desc) + 0x10;
    return n;
}
