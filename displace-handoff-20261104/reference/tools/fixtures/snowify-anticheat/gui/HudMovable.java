/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.FontRenderer
 */
package com.snowify.gui;

import com.snowify.gui.HudElement;
import net.minecraft.client.gui.FontRenderer;

public interface HudMovable {
    public double hudX();

    public double hudY();

    public void setHud(double var1, double var3);

    public int[] previewSize(FontRenderer var1);

    public void drawPreview(FontRenderer var1, int var2, int var3);

    default public boolean scalable() {
        return false;
    }

    default public float hudScale() {
        return 1.0f;
    }

    default public void setHudScale(float scale) {
    }

    default public int[] placeBox(int screenW, int screenH, int w, int h) {
        return HudElement.place(this, screenW, screenH, w, h);
    }

    default public void moveBox(int x, int y, int w, int h, int screenW, int screenH) {
        this.setHud(HudElement.clamp((double)x / (double)Math.max(1, screenW - w)), HudElement.clamp((double)y / (double)Math.max(1, screenH - h)));
    }
}
