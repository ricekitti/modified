#pragma once
#include <stddef.h>

using Handler = void (*)(void*);
struct ModuleView;
struct ModuleRegistryView { ModuleView** list; unsigned long long n; };
struct ModuleSlotView { Handler f; void* priority; };
struct ModuleView {
    char pad[0x10];
    unsigned category;
    unsigned char enabled;
    char pad2[0x33];
    ModuleSlotView h[27];
};
struct ModuleManagerView {
    ModuleView** mods;
    unsigned long long count;
    ModuleRegistryView cat[5], reg[27];
    Handler* lists[27];
    ModuleView** en_list;
    unsigned long long en_count;
    ModuleView** dis_list;
    unsigned long long dis_count;
    void (*setter)(void*, int);
};
struct ModuleToggleContext {
    void* a;
    ModuleManagerView* mgr;
    void* profiles;
    void* minecraft;
    void* world;
    void* player;
};
struct ModuleToggleEvent {
    long long type;
    ModuleToggleContext* ctx;
    ModuleView* mod;
    unsigned char on;
    char rest[0x2f];
};
static_assert(offsetof(ModuleView, h) == 0x48, "Module handler ABI");
static_assert(offsetof(ModuleManagerView, lists) == 0x210, "Handler-list ABI");
static_assert(offsetof(ModuleManagerView, en_list) == 0x2e8, "Pending enable ABI");
static_assert(sizeof(ModuleManagerView) == 0x310, "Manager ABI");
static_assert(offsetof(ModuleToggleContext, world) == 0x20, "World context ABI");
static_assert(offsetof(ModuleToggleContext, player) == 0x28, "Player context ABI");
extern "C" void apply_module_toggles(ModuleToggleContext* context);
