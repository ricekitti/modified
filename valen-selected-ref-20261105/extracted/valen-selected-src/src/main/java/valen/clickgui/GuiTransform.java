/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui;

import valen.clickgui.GuiAppearance;

public final class GuiTransform {
    public static final double DESIGN_WIDTH = 720.0;
    public static final double DESIGN_HEIGHT = 428.0;
    private double scale = 1.0;
    private double left;
    private double top;

    public void update(double d, double d2, double d3, double d4) {
        this.scale = GuiTransform.fitScale(d, d2, d3) * Math.max(0.98, Math.min(1.0, d4));
        this.left = (d / this.scale - 720.0) / 2.0;
        this.top = (d2 / this.scale - 428.0) / 2.0;
    }

    public static double fitScale(double d, double d2, double d3) {
        if (d <= 0.0 || d2 <= 0.0) {
            return 1.0;
        }
        return Math.min(d * 0.95 / 720.0, d2 * 0.95 / 428.0) * GuiAppearance.clampScale(d3) / 100.0;
    }

    public double scale() {
        return this.scale;
    }

    public float left() {
        return (float)this.left;
    }

    public float top() {
        return (float)this.top;
    }

    public int toLogical(double d) {
        return (int)Math.floor(d / this.scale);
    }
}

