/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.animation;

public class RevealAnimation {
    private final float durationMs;
    private float phase;
    private boolean open;
    private long lastTimeNs = System.nanoTime();
    private static final float CONTENT_LAG = 0.18f;

    public RevealAnimation(float f) {
        this.durationMs = Math.max(1.0f, f);
    }

    public void setTarget(boolean bl) {
        this.open = bl;
    }

    public void snap(boolean bl) {
        this.open = bl;
        this.phase = bl ? 1.0f : 0.0f;
        this.lastTimeNs = System.nanoTime();
    }

    public void update() {
        long l = System.nanoTime();
        float f = Math.min((float)(l - this.lastTimeNs) / 1000000.0f, 50.0f);
        this.lastTimeNs = l;
        float f2 = f / this.durationMs;
        this.phase = this.open ? Math.min(1.0f, this.phase + f2) : Math.max(0.0f, this.phase - f2);
    }

    public float getValue() {
        return RevealAnimation.easeOutCubic(this.phase);
    }

    public float getContentValue() {
        float f = (this.phase - 0.18f) / 0.82f;
        return RevealAnimation.easeOutCubic(Math.max(0.0f, Math.min(1.0f, f)));
    }

    public boolean isAnimating() {
        return this.open ? this.phase < 1.0f : this.phase > 0.0f;
    }

    public boolean isOpen() {
        return this.open;
    }

    private static float easeOutCubic(float f) {
        float f2 = 1.0f - f;
        return 1.0f - f2 * f2 * f2;
    }
}

