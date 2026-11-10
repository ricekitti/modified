/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.render;

import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.utility.RenderUtils;
import valen.utility.shader.ShaderUtils;

public final class Shape {
    private static final float MARGIN = 2.0f;
    private static final int MODE_GRADIENT = 0;
    private static final int MODE_HUE = 1;
    private static final int MODE_CHECKER = 2;
    private static final int MODE_CONIC = 3;
    private static ShaderUtils program;

    private Shape() {
    }

    public static void surface(float f, float f2, float f3, float f4, int n) {
        Shape.fill(f, f2, f3, f4, 12.0f, n);
    }

    public static void capsule(float f, float f2, float f3, float f4, int n) {
        Shape.fill(f, f2, f3, f4, Shape.capsuleRadius(f, f2, f3, f4), n);
    }

    public static void circle(float f, float f2, float f3, int n) {
        float f4 = f3 / 2.0f;
        Shape.capsule(f - f4, f2 - f4, f + f4, f2 + f4, n);
    }

    public static void fill(float f, float f2, float f3, float f4, float f5, int n) {
        Shape.draw(f, f2, f3, f4, f5, 0.0f, 0.0f, 0, 0.0f, 0.0f, n, n, n, n);
    }

    public static void verticalGradient(float f, float f2, float f3, float f4, float f5, int n, int n2) {
        Shape.draw(f, f2, f3, f4, f5, 0.0f, 0.0f, 0, 0.0f, 0.0f, n, n, n2, n2);
    }

    public static void gradient(float f, float f2, float f3, float f4, float f5, int n, int n2, int n3, int n4) {
        Shape.draw(f, f2, f3, f4, f5, 0.0f, 0.0f, 0, 0.0f, 0.0f, n, n2, n3, n4);
    }

    public static void insetGradient(float f, float f2, float f3, float f4, float f5, float f6, int n, int n2, int n3, int n4) {
        Shape.draw(f, f2, f3, f4, f5, 0.0f, 0.0f, 0, 0.0f, f6, n, n2, n3, n4);
    }

    public static void conicRing(float f, float f2, float f3, float f4, float f5) {
        float f6 = f3 / 2.0f;
        int n = ClickGuiStyle.withAlpha(-1, Math.round(255.0f * Shape.clamp01(f5)));
        Shape.draw(f - f6, f2 - f6, f + f6, f2 + f6, f6, 0.0f, Math.max(1.0E-4f, f4), 3, 0.0f, 0.0f, n, n, n, n);
    }

    public static void ring(float f, float f2, float f3, float f4, float f5, float f6, int n) {
        Shape.draw(f, f2, f3, f4, f5, 0.0f, Math.max(1.0E-4f, f6), 0, 0.0f, 0.0f, n, n, n, n);
    }

    public static void capsuleRing(float f, float f2, float f3, float f4, float f5, int n) {
        Shape.ring(f, f2, f3, f4, Shape.capsuleRadius(f, f2, f3, f4), f5, n);
    }

    public static void circleRing(float f, float f2, float f3, float f4, int n) {
        float f5 = f3 / 2.0f;
        Shape.capsuleRing(f - f5, f2 - f5, f + f5, f2 + f5, f4, n);
    }

    public static void shadow(float f, float f2, float f3, float f4, float f5, float f6, int n) {
        Shape.draw(f, f2, f3, f4, f5, Math.max(1.0E-4f, f6), 0.0f, 0, 0.0f, 0.0f, n, n, n, n);
    }

    public static void hueCapsule(float f, float f2, float f3, float f4, float f5, float f6) {
        int n = ClickGuiStyle.withAlpha(-1, Math.round(255.0f * Shape.clamp01(f6)));
        Shape.draw(f, f2, f3, f4, Shape.capsuleRadius(f, f2, f3, f4), 0.0f, 0.0f, 1, 0.0f, f5, n, n, n, n);
    }

    public static void checker(float f, float f2, float f3, float f4, float f5, float f6, float f7, int n, int n2, int n3, int n4) {
        Shape.draw(f, f2, f3, f4, f5, 0.0f, 0.0f, 2, Math.max(0.5f, f6), f7, n, n2, n3, n4);
    }

    public static void scrollEdge(float f, float f2, float f3, float f4, boolean bl, int n) {
        float f5 = 2.0f;
        int n2 = ClickGuiStyle.withAlpha(n, 0);
        if (bl) {
            Shape.fill(f, f2 - 1.0f, f3, f2 + f5, 0.0f, n);
            Shape.verticalGradient(f, f2 + f5, f3, f2 + f5 + f4, 0.0f, n, n2);
        } else {
            Shape.fill(f, f2 - f5, f3, f2 + 1.0f, 0.0f, n);
            Shape.verticalGradient(f, f2 - f5 - f4, f3, f2 - f5, 0.0f, n2, n);
        }
    }

    public static float capsuleRadius(float f, float f2, float f3, float f4) {
        return Math.min(f3 - f, f4 - f2) / 2.0f;
    }

    private static void draw(float f, float f2, float f3, float f4, float f5, float f6, float f7, int n, float f8, float f9, int n2, int n3, int n4, int n5) {
        boolean bl;
        float f10 = f3 - f;
        float f11 = f4 - f2;
        boolean bl2 = bl = (n2 | n3 | n4 | n5) >>> 24 == 0 && n != 2;
        if (f10 <= 0.0f || f11 <= 0.0f || bl) {
            return;
        }
        float f12 = Math.max(0.0f, Math.min(f5, Math.min(f10, f11) / 2.0f));
        float f13 = 2.0f + f6;
        ShaderUtils shaderUtils = Shape.program();
        RenderUtils.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)771);
        RenderUtils.setAlphaLimit(0.0f);
        shaderUtils.init();
        shaderUtils.setUniformf("size", f10, f11);
        shaderUtils.setUniformf("margin", f13, f13);
        shaderUtils.setUniformf("radius", f12);
        shaderUtils.setUniformf("softness", f6);
        shaderUtils.setUniformf("stroke", f7);
        shaderUtils.setUniformf("cell", f8);
        shaderUtils.setUniformf("inset", f9);
        shaderUtils.setUniformi("mode", n);
        Shape.color(shaderUtils, "color1", n2);
        Shape.color(shaderUtils, "color2", n3);
        Shape.color(shaderUtils, "color3", n4);
        Shape.color(shaderUtils, "color4", n5);
        ShaderUtils.drawQuads(f - f13, f2 - f13, f10 + f13 * 2.0f, f11 + f13 * 2.0f);
        shaderUtils.unload();
        GlStateManager.disableBlend();
    }

    private static void color(ShaderUtils shaderUtils, String string, int n) {
        shaderUtils.setUniformf(string, (float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f, (float)(n >>> 24 & 0xFF) / 255.0f);
    }

    private static ShaderUtils program() {
        if (program == null) {
            program = new ShaderUtils("guiShape");
        }
        return program;
    }

    private static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }
}

