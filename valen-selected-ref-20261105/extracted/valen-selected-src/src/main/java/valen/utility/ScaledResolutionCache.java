/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.ScaledResolution
 */
package valen.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class ScaledResolutionCache {
    private static ScaledResolution cached;
    private static int lastDisplayWidth;
    private static int lastDisplayHeight;
    private static int lastGuiScale;
    private static boolean lastUnicode;

    private ScaledResolutionCache() {
    }

    public static ScaledResolution get() {
        Minecraft minecraft = Minecraft.getMinecraft();
        int n = minecraft.displayWidth;
        int n2 = minecraft.displayHeight;
        int n3 = minecraft.gameSettings != null ? minecraft.gameSettings.guiScale : 0;
        boolean bl = minecraft.isUnicode();
        if (cached == null || n != lastDisplayWidth || n2 != lastDisplayHeight || n3 != lastGuiScale || bl != lastUnicode) {
            cached = new ScaledResolution(minecraft);
            lastDisplayWidth = n;
            lastDisplayHeight = n2;
            lastGuiScale = n3;
            lastUnicode = bl;
        }
        return cached;
    }

    static {
        lastDisplayWidth = -1;
        lastDisplayHeight = -1;
        lastGuiScale = -1;
    }
}

