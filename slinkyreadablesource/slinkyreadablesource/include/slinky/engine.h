// Engine-side types and helpers shared by the recoded module logic.
// Everything is named after its original RVA until its purpose is understood.
#pragma once
#include "slinky/api.h"

// Per-hook context handed to every module callback (rcx at +0x08 points here).
struct Ctx {
    void *a;            // +0x00  world / client handle
    void *p08, *p10;
    void *c18;          // +0x18  (second argument of several world calls)
    void *p20;
    void *b;            // +0x28  local player / entity handle
    void *p30, *p38;
    unsigned char f40;  // +0x40
};

// Hook argument block (rcx of every descriptor callback).
struct Hook {
    char pad0[8];
    Ctx *ctx;           // +0x08
};

#define FN(ret, name, args) extern "C" ret name args
