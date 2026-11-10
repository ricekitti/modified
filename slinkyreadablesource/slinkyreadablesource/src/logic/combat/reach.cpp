// Reach - extended attack range with sprint and cooldown requirements.
#include "slinky/api.h"

// ---- settings (see src/modules/combat/reach.cpp) ----
extern "C" float data_2ea790;          // "distance" current value
extern "C" float data_2ea7c8;          // "activate_time" current value
extern "C" unsigned char data_2ea7e8;  // "require_sprint" current value

// ---- module state ----
extern "C" int data_2f9d80;            // last value seen from java_get_entity_living_base_last_attacker_time
extern "C" int data_2f9d84;            // cooldown, ticks (re-armed to 9)

// ---- engine helpers ----
extern "C" bool java_call_entity_is_sprinting(void *a, void *b);   // sprint/ground check
extern "C" int java_get_entity_living_base_last_attacker_time(void *a, void *b);    // target/state id

struct Ctx { void *a; char pad[0x20]; void *b; };  // +0x00, +0x28

struct ReachState {
    char pad0[8];
    Ctx *ctx;       // +0x08
    bool active;    // +0x10
    char pad1[7];
    double reach;   // +0x18
};

extern "C" void reach_part(ReachState *s) {
    int c = data_2f9d84;
    if (c > 0) {
        c--;
        data_2f9d84 = c;
    }
    Ctx *p = s->ctx;
    if (data_2ea7e8 == 1) {
        if (!java_call_entity_is_sprinting(p->a, p->b))
            return;
        c = data_2f9d84;
    }
    if (c == 0) {
        int v = java_get_entity_living_base_last_attacker_time(p->a, p->b);
        if (v == data_2f9d80) {
            c = data_2f9d84;
        } else {
            data_2f9d80 = v;
            data_2f9d84 = 9;
            c = 9;
        }
    }
    if (data_2ea7c8 > (float)c) {
        s->active = true;
        s->reach = (double)data_2ea790;
    }
}
