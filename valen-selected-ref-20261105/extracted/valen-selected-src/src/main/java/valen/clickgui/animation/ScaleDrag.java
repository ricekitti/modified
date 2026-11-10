/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.animation;

import valen.clickgui.GuiAppearance;

public final class ScaleDrag {
    private boolean active;
    private double startPointer;
    private double startValue;
    private double trackPixels;

    public void begin(double d, double d2, double d3) {
        this.active = true;
        this.startPointer = d;
        this.startValue = GuiAppearance.clampScale(d2);
        this.trackPixels = Math.max(1.0, d3);
    }

    public double valueAt(double d) {
        return GuiAppearance.clampScale(this.startValue + (d - this.startPointer) / this.trackPixels * 50.0);
    }

    public boolean isActive() {
        return this.active;
    }

    public void end() {
        this.active = false;
    }
}

