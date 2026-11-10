#pragma once

namespace slinky {
struct StasisMotion { double x, y, z; };

// Behavior reference: OpenMyau-Plus Stasis.java, aca1afc77f695c85cd641c9008401f8fb164c2a8.
// Independent native implementation of the 45-tick freeze / 1-tick release cycle.
struct StasisState {
    StasisMotion saved;
    unsigned ticks;
    bool release;
    bool active;

    void enable(StasisMotion motion) {
        saved = motion;
        ticks = 0;
        release = false;
        active = true;
    }

    StasisMotion update(StasisMotion current) {
        if (!active) return current;
        ++ticks;
        if (!release && ticks >= 45) {
            release = true;
            ticks = 0;
            return saved;
        }
        if (release && ticks >= 1) {
            release = false;
            ticks = 0;
            saved = current;
        }
        return release ? current : StasisMotion{0, 0, 0};
    }

    StasisMotion disable() {
        active = false;
        ticks = 0;
        release = false;
        return saved;
    }

    static bool cancelMovementPacket(bool frozen, bool movementPacket, bool lookOnly, int hurtTime) {
        return frozen && movementPacket && !lookOnly && hurtTime == 0;
    }
};
}
