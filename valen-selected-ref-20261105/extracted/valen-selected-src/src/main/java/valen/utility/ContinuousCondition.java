/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

public final class ContinuousCondition {
    private boolean active;
    private long startedAt;

    public boolean update(boolean bl, long l, long l2) {
        if (!bl) {
            this.reset();
            return false;
        }
        if (!this.active || l < this.startedAt) {
            this.active = true;
            this.startedAt = l;
        }
        return l - this.startedAt >= Math.max(0L, l2);
    }

    public void reset() {
        this.active = false;
        this.startedAt = 0L;
    }
}

