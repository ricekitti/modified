/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

public final class FireballJumpTracker {
    private static final long LAUNCH_WINDOW = 1500000000L;
    private static final long IMPACT_WINDOW = 250000000L;
    private static final double EXPLOSION_RANGE_SQ = 64.0;
    private boolean armed;
    private boolean explosion;
    private boolean upwardImpulse;
    private long launchedAt;
    private long explodedAt;
    private long impulseAt;

    public void onUse(long l) {
        this.reset();
        this.armed = true;
        this.launchedAt = l;
    }

    public void onExplosion(long l, double d, double d2, double d3, double d4) {
        if (!this.recentLaunch(l) || !Double.isFinite(d) || d < 0.0 || d > 64.0) {
            return;
        }
        this.explosion = true;
        this.explodedAt = l;
        this.onVelocity(l, d2, d3, d4);
    }

    public void onVelocity(long l, double d, double d2, double d3) {
        if (this.recentLaunch(l) && Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && d2 > 0.05) {
            this.upwardImpulse = true;
            this.impulseAt = l;
        }
    }

    public boolean consumeJump(long l, boolean bl, double d) {
        if (!this.recentLaunch(l)) {
            this.reset();
            return false;
        }
        if (this.explosion && l - this.explodedAt > 250000000L) {
            this.reset();
            return false;
        }
        if (!(this.explosion && this.upwardImpulse && l - this.impulseAt >= 0L && l - this.impulseAt <= 250000000L && bl && Double.isFinite(d) && !(d <= 0.01))) {
            return false;
        }
        this.reset();
        return true;
    }

    public void reset() {
        this.armed = false;
        this.explosion = false;
        this.upwardImpulse = false;
        this.impulseAt = 0L;
        this.explodedAt = 0L;
        this.launchedAt = 0L;
    }

    private boolean recentLaunch(long l) {
        return this.armed && l - this.launchedAt >= 0L && l - this.launchedAt <= 1500000000L;
    }
}

