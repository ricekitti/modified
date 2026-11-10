package dev.rice.client.render.openb4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
/** Only the OpenB4 drawing/color primitives used by PlayerESP. */
public final class RenderUtils {
    public static double scaledWidthD, scaledHeightD;
    public static int scaledWidth, scaledHeight;
    public static void updateResolution() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        scaledWidthD = sr.getScaledWidth_double(); scaledHeightD = sr.getScaledHeight_double();
        scaledWidth = sr.getScaledWidth(); scaledHeight = sr.getScaledHeight();
    }
    public static int rainbow(long speed, long... offset) {
        long l2 = System.currentTimeMillis() + (offset.length > 0 ? offset[0] : 0L);
        return Color.getHSBColor((float) (l2 % (15000L / speed)) / (15000.0f / (float) speed), 0.75f, 1.0f).getRGB();
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void rect(float x, float y, float x2, float y2, int color) {
        beginRect();
        rawRect(x, y, x2, y2, color);
        endRect();
    }

    public static void beginRect() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    public static void rawRect(float f, float f2, float f3, float f4, int n) {
        float t;
        if (f < f3) { t = f; f = f3; f3 = t; }
        if (f2 < f4) { t = f2; f2 = f4; f4 = t; }
        float a = (float) (n >>> 24) / 255.0f;
        float r = (float) (n >> 16 & 0xFF) / 255.0f;
        float g = (float) (n >> 8 & 0xFF) / 255.0f;
        float b = (float) (n & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, a);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION);
        wr.pos(f, f4, 0.0).endVertex();
        wr.pos(f3, f4, 0.0).endVertex();
        wr.pos(f3, f2, 0.0).endVertex();
        wr.pos(f, f2, 0.0).endVertex();
        tessellator.draw();
    }

    public static void endRect() {
        resetColor();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void quad(double x, double y, double w, double h) {
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2d(x, y + h);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2d(x + w, y + h);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2d(x + w, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void screenQuad() {
        quad(0.0, 0.0, scaledWidth, scaledHeight);
    }

    public static int blendInt(int n, int n2, double d) {
        int n3 = (int) (d * 255.0);
        int n4 = 255 - n3;
        int a = (n >> 24 & 0xFF) * n3 + (n2 >> 24 & 0xFF) * n4 >> 8;
        int r = (n >> 16 & 0xFF) * n3 + (n2 >> 16 & 0xFF) * n4 >> 8;
        int g = (n >> 8 & 0xFF) * n3 + (n2 >> 8 & 0xFF) * n4 >> 8;
        int b = (n & 0xFF) * n3 + (n2 & 0xFF) * n4 >> 8;
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void gradientRect(float f, float f2, float f3, float f4, int n, int n2) {
        float t;
        if (f < f3) { t = f; f = f3; f3 = t; }
        if (f2 < f4) { t = f2; f2 = f4; f4 = t; }
        float a = (float) (n >> 24 & 0xFF) / 255.0f;
        float r = (float) (n >> 16 & 0xFF) / 255.0f;
        float g = (float) (n >> 8 & 0xFF) / 255.0f;
        float b = (float) (n & 0xFF) / 255.0f;
        float a2 = (float) (n2 >> 24 & 0xFF) / 255.0f;
        float r2 = (float) (n2 >> 16 & 0xFF) / 255.0f;
        float g2 = (float) (n2 >> 8 & 0xFF) / 255.0f;
        float b2 = (float) (n2 & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(f3, f2, 0.0).color(r, g, b, a).endVertex();
        wr.pos(f, f2, 0.0).color(r, g, b, a).endVertex();
        wr.pos(f, f4, 0.0).color(r2, g2, b2, a2).endVertex();
        wr.pos(f3, f4, 0.0).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    public static int healthColor(double d) {
        return d < 0.3 ? -55770 : (d < 0.5 ? -12250 : (d < 0.7 ? -218 : -14221530));
    }
}
