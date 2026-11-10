/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.util.EnumMap;
import java.util.function.DoubleConsumer;

public final class TimerController {
    private final EnumMap<Owner, Float> requests = new EnumMap(Owner.class);
    private final DoubleConsumer applySpeed;
    private Owner owner;
    private float speed = 1.0f;

    public TimerController(DoubleConsumer doubleConsumer) {
        this.applySpeed = doubleConsumer;
    }

    public void request(Owner owner, double d) {
        if (!Double.isFinite(d)) {
            this.release(owner);
            return;
        }
        this.requests.put(owner, Float.valueOf((float)Math.max(0.0, Math.min(1.0, d))));
        this.apply();
    }

    public void release(Owner owner) {
        this.requests.remove((Object)owner);
        this.apply();
    }

    public boolean isOwner(Owner owner) {
        return this.owner == owner;
    }

    public boolean isRequested(Owner owner) {
        return this.requests.containsKey((Object)owner);
    }

    public boolean hasRequests() {
        return !this.requests.isEmpty();
    }

    public boolean hasRequestOtherThan(Owner owner) {
        for (Owner owner2 : this.requests.keySet()) {
            if (owner2 == owner) continue;
            return true;
        }
        return false;
    }

    public boolean isOverridden(Owner owner) {
        return this.owner != null && this.owner.ordinal() > owner.ordinal();
    }

    public float getSpeed() {
        return this.speed;
    }

    public void clear() {
        this.requests.clear();
        this.apply();
    }

    public void apply() {
        boolean bl = this.owner != null;
        this.owner = null;
        this.speed = 1.0f;
        for (Owner owner : Owner.values()) {
            Float f = this.requests.get((Object)owner);
            if (f == null) continue;
            this.owner = owner;
            this.speed = f.floatValue();
        }
        if (this.owner != null || bl) {
            this.applySpeed.accept(this.speed);
        }
    }

    public static enum Owner {
        TIMER,
        CRITICALS,
        TIMER_RANGE,
        FIREBALL_BOOST,
        BED_BOOST,
        STASIS,
        BRIDGE_EGG,
        FIREBALL_PEARL_CLUTCH,
        AUTO_PEARL;

    }
}

