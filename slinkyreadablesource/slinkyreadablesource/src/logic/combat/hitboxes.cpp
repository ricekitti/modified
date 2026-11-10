// Hitboxes - descriptor +0xc8: publish the hitbox expansion each tick.
#include "slinky/engine.h"

extern "C" float data_2ecee8;  // "expand" current value

struct HitboxState {
    char pad0[0x10];
    bool active;   // +0x10
    char pad1[0xf];
    double expand; // +0x20
};

extern "C" void hitboxes_part(HitboxState *s) {
    s->active = true;
    s->expand = (double)data_2ecee8;
}
