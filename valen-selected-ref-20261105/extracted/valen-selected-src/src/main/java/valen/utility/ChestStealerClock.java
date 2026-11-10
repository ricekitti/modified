/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

public final class ChestStealerClock {
    private long remaining;

    public void start(long l) {
        this.remaining = Math.max(0L, l);
    }

    public boolean ready() {
        if (this.remaining <= 0L) {
            return true;
        }
        --this.remaining;
        return this.remaining <= 0L;
    }

    public void reset() {
        this.remaining = 0L;
    }
}

