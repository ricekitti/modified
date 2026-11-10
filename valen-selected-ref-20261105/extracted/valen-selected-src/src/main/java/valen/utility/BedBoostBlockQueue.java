/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.util.HashSet;
import java.util.Set;

public final class BedBoostBlockQueue {
    private static final double EPSILON = 1.0E-6;
    private final Set<Long> boosted = new HashSet<Long>();
    private long activeBlock;
    private double remainingTicks;
    private boolean active;

    public boolean select(long l, double d, double d2) {
        if (this.active && this.activeBlock == l) {
            return true;
        }
        this.clearActive();
        if (!Double.isFinite(d) || !Double.isFinite(d2) || d <= 1.0E-6 || d2 <= 1.0E-6 || !this.boosted.add(l)) {
            return false;
        }
        this.activeBlock = l;
        this.remainingTicks = Math.min(d2, d);
        this.active = true;
        return true;
    }

    public double allowance(long l, double d) {
        if (!this.active || this.activeBlock != l || !Double.isFinite(d) || d <= 0.0) {
            return 0.0;
        }
        return Math.min(this.remainingTicks, d);
    }

    public void consume(double d) {
        if (!this.active || !Double.isFinite(d) || d <= 0.0) {
            return;
        }
        this.remainingTicks -= Math.min(d, this.remainingTicks);
        if (this.remainingTicks <= 1.0E-6) {
            this.clearActive();
        }
    }

    public void clearActive() {
        this.active = false;
        this.activeBlock = 0L;
        this.remainingTicks = 0.0;
    }

    public void reset() {
        this.boosted.clear();
        this.clearActive();
    }

    public boolean active(long l) {
        return this.active && this.activeBlock == l;
    }

    public double getRemainingTicks() {
        return this.remainingTicks;
    }
}

