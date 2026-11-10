/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.page;

import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.render.Shape;
import valen.module.impl.client.Gui;
import valen.utility.font.ValenFontRenderer;

public final class PageUI {
    public static final float SMALL_SCALE = 0.5f;

    private PageUI() {
    }

    public static ValenFontRenderer labelFont() {
        return Gui.getClickGuiHeaderFontRenderer();
    }

    public static ValenFontRenderer smallFont() {
        return Gui.getClickGuiSettingFontRenderer();
    }

    public static void small(String string, float f, float f2, int n) {
        ValenFontRenderer valenFontRenderer = PageUI.smallFont();
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)0.5, (double)0.5, (double)0.5);
        valenFontRenderer.drawString(string, f / 0.5f, f2 / 0.5f, n, false);
        GlStateManager.popMatrix();
    }

    public static float smallWidth(String string) {
        return (float)PageUI.smallFont().getStringWidth(string) * 0.5f;
    }

    public static float smallHeight() {
        return (float)PageUI.smallFont().getFontHeight() * 0.5f;
    }

    public static float textY(ValenFontRenderer valenFontRenderer, float f, float f2, float f3) {
        return (f2 + f3) / 2.0f - valenFontRenderer.getCapCenter() * f;
    }

    public static float smallY(float f, float f2) {
        return PageUI.textY(PageUI.smallFont(), 0.5f, f, f2);
    }

    public static float labelY(float f, float f2) {
        return PageUI.textY(PageUI.labelFont(), 1.0f, f, f2);
    }

    public static void smallCentred(String string, float f, float f2, float f3, float f4, int n) {
        PageUI.small(string, f + (f2 - f - PageUI.smallWidth(string)) / 2.0f, PageUI.smallY(f3, f4), n);
    }

    public static void label(String string, float f, float f2, int n) {
        PageUI.labelFont().drawString(string, f, f2, n, false);
    }

    public static float labelWidth(String string) {
        return PageUI.labelFont().getStringWidth(string);
    }

    public static float labelHeight() {
        return PageUI.labelFont().getFontHeight();
    }

    public static String truncateLabel(String string, float f) {
        if (string == null || string.isEmpty() || PageUI.labelWidth(string) <= f) {
            return string == null ? "" : string;
        }
        String string2 = "...";
        float f2 = PageUI.labelWidth(string2);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            if (PageUI.labelWidth(stringBuilder.toString() + string.charAt(i)) + f2 > f) break;
            stringBuilder.append(string.charAt(i));
        }
        return stringBuilder.append(string2).toString();
    }

    public static String truncateSmall(String string, float f) {
        if (string == null || string.isEmpty() || PageUI.smallWidth(string) <= f) {
            return string == null ? "" : string;
        }
        String string2 = "...";
        float f2 = PageUI.smallWidth(string2);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            if (PageUI.smallWidth(stringBuilder.toString() + string.charAt(i)) + f2 > f) break;
            stringBuilder.append(string.charAt(i));
        }
        return stringBuilder.append(string2).toString();
    }

    public static void surface(float f, float f2, float f3, float f4, int n) {
        Shape.surface(f, f2, f3, f4, n);
    }

    public static void pill(float f, float f2, float f3, float f4, int n) {
        Shape.capsule(f, f2, f3, f4, n);
    }

    public static void shadow(float f, float f2, float f3, float f4, float f5) {
        if (f5 <= 0.01f) {
            return;
        }
        Shape.shadow(f, f2 + 2.0f * f5, f3, f4 + 3.0f * f5, 12.0f, 6.0f * f5, ClickGuiStyle.withAlpha(-16777216, Math.round((ClickGuiStyle.DARK_THEME ? 110.0f : 45.0f) * f5)));
    }

    public static float chip(String string, float f, float f2, int n, int n2) {
        float f3 = 5.0f;
        float f4 = PageUI.smallHeight() + 5.0f;
        float f5 = PageUI.smallWidth(string) + f3 * 2.0f;
        PageUI.pill(f, f2, f + f5, f2 + f4, n);
        PageUI.small(string, f + f3, PageUI.smallY(f2, f2 + f4), n2);
        return f5;
    }

    public static float chipHeight() {
        return PageUI.smallHeight() + 5.0f;
    }

    public static void button(String string, float f, float f2, float f3, float f4, int n, int n2, float f5, boolean bl) {
        int n3 = bl ? ClickGuiStyle.blend(n, ClickGuiStyle.hovered(n, 1.0f), f5) : ClickGuiStyle.SURFACE_ALT;
        int n4 = bl ? n2 : ClickGuiStyle.TEXT_MUTED;
        PageUI.pill(f, f2, f3, f4, n3);
        PageUI.smallCentred(string, f, f3, f2, f4, n4);
    }

    public static void gradientV(float f, float f2, float f3, float f4, int n, int n2) {
        Shape.verticalGradient(f, f2, f3, f4, 0.0f, n, n2);
    }

    public static boolean inside(float f, float f2, float f3, float f4, float f5, float f6) {
        return f >= f3 && f <= f5 && f2 >= f4 && f2 <= f6;
    }
}

