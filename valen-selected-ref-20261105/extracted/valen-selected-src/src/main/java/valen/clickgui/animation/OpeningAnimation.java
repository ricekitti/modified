/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.animation;

public final class OpeningAnimation {
    private static final double DURATION_NS = 1.8E8;
    private long startedAt;
    private boolean started;
    private float progress = 1.0f;

    public void update(long l) {
        if (!this.started) {
            this.started = true;
            this.startedAt = l;
        }
        double d = Math.max(0.0, Math.min(1.0, (double)(l - this.startedAt) / 1.8E8));
        double d2 = 1.0 - d;
        this.progress = (float)(1.0 - d2 * d2 * d2);
    }

    public float getProgress() {
        return this.progress;
    }

    public double getScale() {
        return 0.98 + 0.02 * (double)this.progress;
    }

    public boolean isAnimating() {
        return this.progress < 1.0f;
    }

    public void reset() {
        this.started = false;
        this.progress = 1.0f;
    }
}

