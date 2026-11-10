/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 *  valen.utility.font.ValenFontRenderer$GlyphColorProvider
 */
package valen.utility.font;

import net.minecraft.client.renderer.GlStateManager;
import valen.utility.font.ValenFontRenderer;

public interface ValenFontRenderer {
    public int drawString(String var1, float var2, float var3, int var4, boolean var5);

    public int drawGlyphString(String var1, float var2, float var3, GlyphColorProvider var4, boolean var5);

    default public int drawString(String string, float f, float f2, int n) {
        return this.drawString(string, f, f2, n, false);
    }

    default public int drawGlyphString(String string, float f, float f2, GlyphColorProvider glyphColorProvider) {
        return this.drawGlyphString(string, f, f2, glyphColorProvider, false);
    }

    default public int drawStringWithShadow(String string, float f, float f2, int n) {
        return this.drawString(string, f, f2, n, true);
    }

    default public int drawScaledString(String string, float f, float f2, float f3, int n, boolean bl) {
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)f3, (double)f3, (double)f3);
        int n2 = this.drawString(string, f / f3, f2 / f3, n, bl);
        GlStateManager.popMatrix();
        return n2;
    }

    default public int drawScaledString(String string, float f, float f2, float f3, int n) {
        return this.drawScaledString(string, f, f2, f3, n, false);
    }

    default public float getScaledStringWidth(String string, float f) {
        return (float)this.getStringWidth(string) * f;
    }

    default public float getScaledFontHeight(float f) {
        return (float)this.getFontHeight() * f;
    }

    public int getStringWidth(String var1);

    public int getFontHeight();

    default public int getLineHeight() {
        return this.getFontHeight();
    }

    default public int getTextTopOffset() {
        return 0;
    }

    default public int getTextBottomOffset() {
        return this.getFontHeight();
    }

    default public float getCapCenter() {
        return (float)this.getFontHeight() / 2.0f;
    }

    default public void destroy() {
    }
}

