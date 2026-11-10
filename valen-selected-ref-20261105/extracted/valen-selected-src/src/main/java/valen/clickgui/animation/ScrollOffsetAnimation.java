/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.animation;

public class ScrollOffsetAnimation {
    private static final long MOMENTUM_WINDOW_MS = 140L;
    private static final float MAX_MOMENTUM_MULTIPLIER = 1.9f;
    private static final float MOMENTUM_GROWTH_PER_CHAIN = 0.16f;
    private float current;
    private float target;
    private long lastTimeNs;
    private final float responsiveness;
    private long lastExtendTimeNs;
    private int extendChainLength;

    public ScrollOffsetAnimation(long l) {
        this.responsiveness = Math.max(1.0f, 3000.0f / (float)Math.max(1L, l));
        this.current = 0.0f;
        this.target = 0.0f;
        this.lastTimeNs = System.nanoTime();
    }

    public void reset(float f) {
        this.current = f;
        this.target = f;
        this.lastTimeNs = System.nanoTime();
        this.extendChainLength = 0;
    }

    public void setTarget(float f) {
        this.target = f;
        this.lastTimeNs = System.nanoTime();
    }

    public void shiftBy(float f) {
        this.current += f;
        this.target += f;
    }

    public void extend(float f) {
        long l = System.nanoTime();
        this.extendChainLength = l - this.lastExtendTimeNs <= 140000000L ? ++this.extendChainLength : 0;
        this.lastExtendTimeNs = l;
        float f2 = 1.0f + Math.min((float)this.extendChainLength * 0.16f, 0.9f);
        this.target += f * f2;
        this.lastTimeNs = l;
    }

    public void clampTarget(float f, float f2) {
        this.target = Math.max(f, Math.min(f2, this.target));
    }

    public float getValue() {
        long l = System.nanoTime();
        float f = Math.min((float)(l - this.lastTimeNs) / 1.0E9f, 0.05f);
        this.lastTimeNs = l;
        if (Math.abs(this.target - this.current) < 0.01f) {
            this.current = this.target;
            return this.target;
        }
        float f2 = 1.0f - (float)Math.exp(-this.responsiveness * f);
        this.current += (this.target - this.current) * f2;
        return this.current;
    }

    public boolean isAnimating() {
        return Math.abs(this.target - this.current) >= 0.05f;
    }

    public float getTarget() {
        return this.target;
    }
}

