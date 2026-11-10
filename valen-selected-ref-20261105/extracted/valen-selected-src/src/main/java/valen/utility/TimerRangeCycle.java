/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.util.function.DoubleConsumer;
import valen.utility.TimerController;

public final class TimerRangeCycle {
    private static final double MAX_TICKS_PER_LOOP = 10.0;
    private final TimerController timer;
    private final TimerController.Owner owner;
    private final int maxTicks;
    private final DoubleConsumer catchUp;
    private Phase phase = Phase.IDLE;
    private boolean legit;
    private boolean coolingDown;
    private long freezeUntil;
    private long cooldownUntil;
    private long cooldownNanos;
    private double ticks;
    private double remainingTicks;

    public TimerRangeCycle(TimerController timerController, DoubleConsumer doubleConsumer) {
        this(timerController, TimerController.Owner.TIMER_RANGE, doubleConsumer);
    }

    public TimerRangeCycle(TimerController timerController, TimerController.Owner owner, DoubleConsumer doubleConsumer) {
        this(timerController, owner, 10, doubleConsumer);
    }

    public TimerRangeCycle(TimerController timerController, TimerController.Owner owner, int n, DoubleConsumer doubleConsumer) {
        if (n < 1) {
            throw new IllegalArgumentException("maxTicks must be positive");
        }
        this.timer = timerController;
        this.owner = owner;
        this.maxTicks = n;
        this.catchUp = doubleConsumer;
    }

    public boolean begin(long l, boolean bl, double d, double d2) {
        if (this.active() || this.coolingDown && l < this.cooldownUntil || this.timer.isOverridden(this.owner) || !Double.isFinite(d) || !Double.isFinite(d2) || d <= 0.0) {
            return false;
        }
        this.legit = bl;
        this.ticks = Math.min((double)this.maxTicks, d / 50.0);
        this.freezeUntil = l + (long)(this.ticks * 5.0E7);
        this.cooldownNanos = (long)(Math.max(0.0, Math.min(10000.0, d2)) * 1000000.0);
        this.timer.request(this.owner, 0.0);
        if (bl) {
            this.phase = Phase.FROZEN;
        } else {
            this.burst(l);
        }
        return true;
    }

    public void update(long l) {
        if (!this.active()) {
            return;
        }
        if (!this.timer.isOwner(this.owner)) {
            this.abort(l);
            return;
        }
        if (this.phase == Phase.BURST) {
            if (this.remainingTicks > 0.0) {
                this.emitBurstTicks();
                return;
            }
            if (this.legit) {
                this.finish();
                return;
            }
            this.phase = Phase.FROZEN;
        }
        if (l >= this.freezeUntil) {
            if (this.legit) {
                this.burst(l);
            } else {
                this.finish();
            }
        }
    }

    public void abort(long l) {
        if (this.active()) {
            this.coolingDown = true;
            this.cooldownUntil = Math.max(this.cooldownUntil, l + this.cooldownNanos);
        }
        this.finish();
    }

    public void reset() {
        this.finish();
        this.coolingDown = false;
        this.cooldownUntil = 0L;
        this.cooldownNanos = 0L;
    }

    public boolean active() {
        return this.phase != Phase.IDLE;
    }

    public boolean legit() {
        return this.legit;
    }

    public boolean blockWhileFrozen() {
        return this.phase == Phase.FROZEN && !this.legit;
    }

    private void burst(long l) {
        this.phase = Phase.BURST;
        this.coolingDown = true;
        this.cooldownUntil = l + this.cooldownNanos;
        this.remainingTicks = this.ticks;
        this.emitBurstTicks();
    }

    private void emitBurstTicks() {
        double d = Math.min(10.0, this.remainingTicks);
        this.remainingTicks -= d;
        this.catchUp.accept(d);
    }

    private void finish() {
        this.phase = Phase.IDLE;
        this.freezeUntil = 0L;
        this.ticks = 0.0;
        this.remainingTicks = 0.0;
        this.timer.release(this.owner);
    }

    private static enum Phase {
        IDLE,
        BURST,
        FROZEN;

    }
}

