/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.lunar.render;

import net.minecraft.client.renderer.GlStateManager;

public final class LunarMatrices {
    public static void rotate(double d, double d2, double d3, double d4) {
        GlStateManager.rotate((float)((float)d), (float)((float)d2), (float)((float)d3), (float)((float)d4));
    }
}

