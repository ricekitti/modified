/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import valen.utility.TimerController;

public final class BedBoostCycle {
    private static final double MAX_SAMPLE_MS = 250.0;
    private static final double EPSILON = 1.0E-6;
    private final TimerController timer;
    private Phase phase = Phase.IDLE;
    private long session;
    private long lastSampleNanos;
    private double bankDurationMs;
    private double bankedMs;
    private double bankedTicks;
    private double speed;

    public BedBoostCycle(TimerController timerController) {
        this.timer = timerController;
    }

    public boolean begin(long l, long l2, double d, double d2) {
        if (this.active() || this.timer.hasRequestOtherThan(TimerController.Owner.BED_BOOST) || !Double.isFinite(d) || !Double.isFinite(d2) || d2 <= 0.0) {
            return false;
        }
        this.session = l;
        this.speed = Math.max(0.0, Math.min(1.0, d));
        this.bankDurationMs = d2;
        this.bankedMs = 0.0;
        this.bankedTicks = 0.0;
        this.lastSampleNanos = l2;
        this.phase = Phase.BANKING;
        this.timer.request(TimerController.Owner.BED_BOOST, this.speed);
        if (!this.timer.isOwner(TimerController.Owner.BED_BOOST)) {
            this.reset();
            return false;
        }
        return true;
    }

    public boolean update(long l, long l2) {
        if (this.phase != Phase.BANKING || !this.matches(l)) {
            return this.active();
        }
        if (this.timer.hasRequestOtherThan(TimerController.Owner.BED_BOOST) || !this.timer.isOwner(TimerController.Owner.BED_BOOST)) {
            this.reset();
            return false;
        }
        this.sample(l2);
        return this.active();
    }

    public boolean beginDrain(long l, long l2) {
        if (!this.matches(l) || this.phase == Phase.DRAINING || this.phase == Phase.IDLE) {
            return this.phase == Phase.DRAINING && this.matches(l);
        }
        if (this.timer.hasRequestOtherThan(TimerController.Owner.BED_BOOST)) {
            this.reset();
            return false;
        }
        if (this.phase == Phase.BANKING) {
            if (!this.timer.isOwner(TimerController.Owner.BED_BOOST)) {
                this.reset();
                return false;
            }
            this.sample(l2);
        }
        this.timer.release(TimerController.Owner.BED_BOOST);
        if (this.bankedTicks <= 1.0E-6) {
            this.reset();
            return false;
        }
        this.phase = Phase.DRAINING;
        return true;
    }

    public double drain(long l, double d) {
        if (this.phase != Phase.DRAINING || !this.matches(l) || !Double.isFinite(d) || d <= 0.0) {
            return 0.0;
        }
        double d2 = Math.min(d, this.bankedTicks);
        this.bankedTicks -= d2;
        if (this.bankedTicks <= 1.0E-6) {
            this.finish();
        }
        return d2;
    }

    public void reset() {
        this.finish();
    }

    public boolean active() {
        return this.phase != Phase.IDLE;
    }

    public boolean banking() {
        return this.phase == Phase.BANKING;
    }

    public boolean ready() {
        return this.phase == Phase.READY;
    }

    public boolean draining() {
        return this.phase == Phase.DRAINING;
    }

    public boolean matches(long l) {
        return this.active() && this.session == l;
    }

    public double getBankedTicks() {
        return this.bankedTicks;
    }

    public double getBankedMs() {
        return this.bankedMs;
    }

    private void sample(long l) {
        double d = Math.max(0.0, Math.min(250.0, (double)(l - this.lastSampleNanos) / 1000000.0));
        this.lastSampleNanos = l;
        double d2 = Math.min(d, Math.max(0.0, this.bankDurationMs - this.bankedMs));
        this.bankedMs += d2;
        this.bankedTicks += d2 * (1.0 - this.speed) / 50.0;
        if (this.bankedMs + 1.0E-6 >= this.bankDurationMs) {
            this.phase = Phase.READY;
            this.timer.release(TimerController.Owner.BED_BOOST);
        }
    }

    private void finish() {
        boolean bl = this.phase != Phase.IDLE || this.timer.isRequested(TimerController.Owner.BED_BOOST);
        this.phase = Phase.IDLE;
        this.session = 0L;
        this.lastSampleNanos = 0L;
        this.bankDurationMs = 0.0;
        this.bankedMs = 0.0;
        this.bankedTicks = 0.0;
        this.speed = 1.0;
        if (bl) {
            this.timer.release(TimerController.Owner.BED_BOOST);
        }
    }

    private static enum Phase {
        IDLE,
        BANKING,
        READY,
        DRAINING;

    }
}

