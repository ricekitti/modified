// Slinky client - shared declarations for every module.
// Symbols keep the names they had in the original binary (fn_<rva>, data_<rva>,
// rdata_<rva>), so the code can be compared with the decompiled image one
// function at a time.
#pragma once

// ---- widget API (GUI drawing), original RVAs ----
extern "C" void W_bool(void *a, void *ctx, void *cfg);    // 0x0cef0
extern "C" void W_slider(void *a, void *ctx, void *cfg);  // 0x0cf30
extern "C" void W_range(void *a, void *ctx, void *cfg);   // 0x0cfb0
extern "C" void W_enumw(void *a, void *ctx, void *cfg);   // 0x0d040
extern "C" void W_color(void *a, void *ctx, void *cfg);   // 0x0d0a0
extern "C" void W_items(void *a, void *ctx, void *cfg);   // 0x0d0e0
extern "C" void W_label(void *ctx, void *a, const char *s); // 0x13e90

// ---- settings registration API ----
extern "C" void R_bool(void *mgr, const char *name, void *cfg);  // 0x0c3b0
extern "C" void R_num(void *mgr, const char *name, void *cfg);   // 0x0c450
extern "C" void R_rng(void *mgr, const char *name, void *cfg);   // 0x0c550
extern "C" void R_enum(void *mgr, const char *name, void *cfg);  // 0x0c6f0
extern "C" void R_key(void *mgr, const char *name, void *cfg);   // 0x0c7e0
extern "C" void R_kind2(void *mgr, const char *name, void *cfg); // 0x0ca00

// ---- original .rdata strings (symbol = RVA) ----
#define S(n) extern "C" const char rdata_##n[]
#define D(n) extern "C" unsigned char data_##n[]

// module logging/format helper (0x0ab710): log_fmt(self, len, fmt, arg)
extern "C" void log_fmt(void *self, int len, const char *fmt, ...);
