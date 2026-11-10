/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.ScaledResolution
 */
package com.snowify.gui;

import com.snowify.Snowify;
import com.snowify.gui.Draw;
import com.snowify.gui.Theme;
import com.snowify.util.Gradient;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public final class ProgressBar {
    private static final float W = 100.0f;
    private static final float H = 4.0f;
    private static final float STEP = 16.0f;
    private static int drawn;

    private ProgressBar() {
    }

    public static void newFrame() {
        drawn = 0;
    }

    public static void draw(ScaledResolution res, String text, float done) {
        done = Math.max(0.0f, Math.min(1.0f, done));
        Theme.refresh();
        int[] stops = Theme.stops();
        float x = (float)res.getScaledWidth() / 2.0f - 50.0f;
        float y = (float)res.getScaledHeight() / 2.0f + 16.0f + 16.0f * (float)drawn++;
        boolean glow = Snowify.instance.getGuiConfig().glow();
        if (glow) {
            Draw.glow(x, y, 100.0f, 4.0f, 2.0f, 6.0f, 0x60000000, null);
        }
        Draw.round(x, y, 100.0f, 4.0f, 2.0f, -1879048192);
        if (done > 0.0f) {
            if (glow) {
                Draw.glow(x, y, Math.max(4.0f, 100.0f * done), 4.0f, 2.0f, 4.0f, 0x60FFFFFF, stops);
            }
            Draw.roundGradient(x, y, Math.max(4.0f, 100.0f * done), 4.0f, 2.0f, stops, 255);
        }
        FontRenderer fr = Snowify.instance.getGuiConfig().noticeFont();
        int colour = 0xFF000000 | Gradient.at(stops, done) & 0xFFFFFF;
        fr.drawStringWithShadow(text, (float)res.getScaledWidth() / 2.0f - (float)fr.getStringWidth(text) / 2.0f, y + 6.0f, colour);
    }
}
