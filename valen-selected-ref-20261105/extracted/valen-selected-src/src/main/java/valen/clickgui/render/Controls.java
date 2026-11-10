/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.render;

import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Shape;

public final class Controls {
    public static final float TOGGLE_WIDTH = 26.0f;
    public static final float TOGGLE_HEIGHT = 13.0f;
    public static final float TRACK_HEIGHT = 4.0f;
    public static final float THUMB_SIZE = 10.0f;
    public static final float THUMB_GROWTH = 2.0f;
    public static final float FIELD_HEIGHT = 16.0f;
    public static final float CHIP_HEIGHT = 14.0f;
    private static final int SHADOW = -16777216;

    private Controls() {
    }

    public static void toggle(float f, float f2, float f3, float f4) {
        int n = ClickGuiStyle.blend(ClickGuiStyle.TOGGLE_OFF, ClickGuiStyle.TOGGLE_ON, f3);
        Shape.capsule(f, f2, f + 26.0f, f2 + 13.0f, Controls.fade(n, f4));
        float f5 = ToggleAnimation.getKnobWidth(f3);
        float f6 = ToggleAnimation.getKnobHeight(f3);
        float f7 = f + 2.0f + (26.0f - f5 - 4.0f) * f3;
        float f8 = f2 + (13.0f - f6) / 2.0f;
        Shape.shadow(f7, f8 + 0.6f, f7 + f5, f8 + f6 + 0.6f, f6 / 2.0f, 1.2f, Controls.fade(ClickGuiStyle.withAlpha(-16777216, 70), f4));
        Shape.capsule(f7, f8, f7 + f5, f8 + f6, Controls.fade(ClickGuiStyle.knobColorForToggle(f3), f4));
    }

    public static float thumbCenter(float f, float f2, float f3) {
        float f4 = 5.0f;
        return f + f4 + Math.max(0.0f, f2 - f - f4 * 2.0f) * Controls.clamp01(f3);
    }

    public static float fractionAt(float f, float f2, float f3) {
        float f4 = 5.0f;
        float f5 = f2 - f - f4 * 2.0f;
        return f5 <= 0.0f ? 0.0f : Controls.clamp01((f3 - f - f4) / f5);
    }

    public static float travel(float f, float f2) {
        return Math.max(1.0f, f2 - f - 10.0f);
    }

    public static void slider(float f, float f2, float f3, float f4, boolean bl, float f5, float f6) {
        float f7 = f3 - 2.0f;
        float f8 = f3 + 2.0f;
        Shape.capsule(f, f7, f2, f8, Controls.fade(ClickGuiStyle.TRACK, f6));
        float f9 = Controls.thumbCenter(f, f2, f4);
        if (bl) {
            Shape.capsule(f, f7, Math.max(f + 4.0f, f9), f8, Controls.fade(ClickGuiStyle.SLIDER_FILL, f6));
        }
        Controls.thumb(f9, f3, 10.0f + 2.0f * Controls.clamp01(f5), ClickGuiStyle.SWITCH_KNOB, f6);
    }

    public static void thumb(float f, float f2, float f3, int n, float f4) {
        float f5 = f3 / 2.0f;
        Shape.shadow(f - f5, f2 - f5 + 0.8f, f + f5, f2 + f5 + 0.8f, f5, 1.6f, Controls.fade(ClickGuiStyle.withAlpha(-16777216, 110), f4));
        Shape.circle(f, f2, f3, Controls.fade(n, f4));
        Shape.circleRing(f, f2, f3, 0.6f, Controls.fade(ClickGuiStyle.withAlpha(-16777216, 40), f4));
    }

    public static void colorThumb(float f, float f2, float f3, int n, float f4) {
        float f5 = f3 / 2.0f;
        Shape.shadow(f - f5, f2 - f5 + 0.8f, f + f5, f2 + f5 + 0.8f, f5, 1.6f, Controls.fade(ClickGuiStyle.withAlpha(-16777216, 120), f4));
        Shape.circle(f, f2, f3, Controls.fade(-1, f4));
        Shape.circle(f, f2, f3 - 4.0f, Controls.fade(n | 0xFF000000, f4));
    }

    public static void button(String string, float f, float f2, float f3, float f4, int n, int n2, float f5, float f6) {
        int n3 = ClickGuiStyle.blend(n, ClickGuiStyle.hovered(n, 1.0f), f5);
        Shape.capsule(f, f2, f3, f4, Controls.fade(n3, f6));
        PageUI.smallCentred(string, f, f3, f2, f4, Controls.fade(n2, f6));
    }

    public static void closeButton(float f, float f2, float f3, float f4, float f5) {
        int n = ClickGuiStyle.blend(ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.hovered(ClickGuiStyle.SURFACE_ACTIVE, 1.0f), f4);
        Shape.circle(f, f2, f3, Controls.fade(n, f5));
        int n2 = ClickGuiStyle.blend(ClickGuiStyle.TEXT_SECONDARY, ClickGuiStyle.TEXT_PRIMARY, f4);
        float f6 = f3 * 0.2f;
        Controls.stroke(f - f6, f2 - f6, f + f6, f2 + f6, 1.4f, Controls.fade(n2, f5));
        Controls.stroke(f - f6, f2 + f6, f + f6, f2 - f6, 1.4f, Controls.fade(n2, f5));
    }

    public static void chevron(float f, float f2, float f3, float f4, int n) {
        Controls.chevron(f, f2, f3, f4, 1.3f, n);
    }

    public static void chevron(float f, float f2, float f3, float f4, float f5, int n) {
        float f6 = f3 / 2.0f;
        float f7 = f3 * 0.28f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)f, (float)f2, (float)0.0f);
        GlStateManager.rotate((float)f4, (float)0.0f, (float)0.0f, (float)1.0f);
        Controls.stroke(-f6, -f7, 0.0f, f7, f5, n);
        Controls.stroke(0.0f, f7, f6, -f7, f5, n);
        GlStateManager.popMatrix();
    }

    public static void popUpIndicator(float f, float f2, int n) {
        Controls.chevron(f, f2 - 2.1f, 4.2f, 180.0f, 1.1f, n);
        Controls.chevron(f, f2 + 2.1f, 4.2f, 0.0f, 1.1f, n);
    }

    public static void stroke(float f, float f2, float f3, float f4, float f5, int n) {
        float f6 = f3 - f;
        float f7 = f4 - f2;
        float f8 = (float)Math.sqrt(f6 * f6 + f7 * f7);
        float f9 = f5 / 2.0f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)((f + f3) / 2.0f), (float)((f2 + f4) / 2.0f), (float)0.0f);
        GlStateManager.rotate((float)((float)Math.toDegrees(Math.atan2(f7, f6))), (float)0.0f, (float)0.0f, (float)1.0f);
        Shape.capsule(-f8 / 2.0f - f9, -f9, f8 / 2.0f + f9, f9, n);
        GlStateManager.popMatrix();
    }

    public static int fade(int n, float f) {
        if (f >= 1.0f) {
            return n;
        }
        int n2 = n >>> 24 & 0xFF;
        return ClickGuiStyle.withAlpha(n, Math.round((float)n2 * Controls.clamp01(f)));
    }

    public static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }
}

