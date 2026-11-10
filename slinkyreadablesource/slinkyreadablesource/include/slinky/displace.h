#pragma once

namespace slinky {
struct DisplaceVec { double x, y, z; };
struct DisplaceBox { DisplaceVec min, max; };
struct DisplaceQuery {
    void* context;
    bool (*isVoid)(void*, double, double, double);
    bool (*collision)(void*, DisplaceBox);
};
struct DisplaceAim { bool found, left; float yaw; };
DisplaceAim displaceScan(DisplaceQuery, DisplaceVec player, DisplaceVec target,
                        DisplaceBox box, bool dynamic);
double displaceDistanceSquared(DisplaceVec eye, DisplaceBox box);
struct DisplaceWindow {
    unsigned start;
    bool initialized;
    bool allows(unsigned tick, float delayMs);
};
// The reference combines the two enabled item conditions with OR, including
// the permissive result of an unchecked condition. Keep that behavior explicit.
bool displaceItemPass(bool requireKB, bool whitelist, bool hasKB, bool matches);
struct DisplaceCycle {
    bool active, phase, compensate, last, left, hasKB;
    void clear();
    bool advance(bool eligible, bool allowed, bool side, bool knockback);
    void input(float& forward, float& strafe, bool moving);
};
}
