/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.animation;

public class ToggleAnimation {
    private float current;
    private float target;
    private long lastTimeNs;
    private boolean targetInitialised;
    private final float responsiveness;

    public ToggleAnimation(long l) {
        this.responsiveness = Math.max(1.0f, 3000.0f / (float)Math.max(1L, l));
        this.lastTimeNs = System.nanoTime();
    }

    public void setTarget(boolean bl) {
        float f = this.target = bl ? 1.0f : 0.0f;
        if (!this.targetInitialised) {
            this.targetInitialised = true;
            this.current = this.target;
            this.lastTimeNs = System.nanoTime();
        }
    }

    public void snap(boolean bl) {
        this.current = this.target = bl ? 1.0f : 0.0f;
        this.targetInitialised = true;
        this.lastTimeNs = System.nanoTime();
    }

    public float getValue() {
        long l = System.nanoTime();
        float f = Math.min((float)(l - this.lastTimeNs) / 1.0E9f, 0.05f);
        this.lastTimeNs = l;
        if (Math.abs(this.target - this.current) < 0.001f) {
            this.current = this.target;
        } else {
            float f2 = 1.0f - (float)Math.exp(-this.responsiveness * f);
            this.current += (this.target - this.current) * f2;
        }
        return this.current * this.current * (3.0f - 2.0f * this.current);
    }

    public static float getKnobWidth(float f) {
        return 9.0f + 8.0f * f * (1.0f - f);
    }

    public static float getKnobHeight(float f) {
        return 9.0f - 3.2f * f * (1.0f - f);
    }

    public boolean isAnimating() {
        return Math.abs(this.target - this.current) >= 0.001f;
    }
}

