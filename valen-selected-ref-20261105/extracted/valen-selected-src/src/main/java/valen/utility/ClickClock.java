/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

public final class ClickClock {
    private boolean armed;
    private long nextClick;

    public boolean poll(long l, long l2, long l3) {
        if (!this.armed) {
            this.armed = true;
            this.nextClick = l + Math.max(0L, l2);
        }
        if (l < this.nextClick) {
            return false;
        }
        this.nextClick = l + Math.max(1L, l3);
        return true;
    }

    public void schedule(long l, long l2) {
        this.armed = true;
        this.nextClick = l + Math.max(1L, l2);
    }

    public void reset() {
        this.armed = false;
        this.nextClick = 0L;
    }
}

