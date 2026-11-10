/*
 * Decompiled with CFR 0.152.
 */
package valen.lag.timeout;

public abstract class AbstractTimeout {
    private volatile boolean forcefullyTimedOut = false;

    public long getRemainingMs() {
        return Math.max(0L, this.getTimeoutDurationMs() - (System.currentTimeMillis() - this.getStartTimeMs()));
    }

    protected abstract long getTimeoutDurationMs();

    protected abstract long getStartTimeMs();

    protected abstract boolean shouldHaveTimedOut();

    public final boolean isTimedOut() {
        return this.forcefullyTimedOut || this.shouldHaveTimedOut();
    }

    public final void forceTimeOut() {
        this.forcefullyTimedOut = true;
    }
}

