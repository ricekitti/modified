/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.client.shader.Framebuffer
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 */
package valen.utility.shader;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import valen.lunar.render.LunarFramebuffer;
import valen.lunar.render.LunarPostProcessing;
import valen.utility.RenderUtils;
import valen.utility.shader.ShaderUtils;

public class KawaseBlur {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static ShaderUtils kawaseDown = new ShaderUtils("kawaseDown");
    public static ShaderUtils kawaseUp = new ShaderUtils("kawaseUp");
    public static Framebuffer framebuffer = new LunarFramebuffer(1, 1, false);
    private static int currentIterations;
    private static final List<Framebuffer> framebufferList;

    private static void initFrameBuffers(float f) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();
        framebuffer = LunarFramebuffer.resize(null);
        framebufferList.add(framebuffer);
        int n = 1;
        while ((float)n <= f) {
            Framebuffer framebuffer;
            framebuffer = new LunarFramebuffer((int)((double)KawaseBlur.mc.displayWidth / Math.pow(3.0, n)), (int)((double)KawaseBlur.mc.displayHeight / Math.pow(3.0, n)), false);
            framebuffer.setFramebufferFilter(9729);
            GlStateManager.bindTexture((int)framebuffer.framebufferTexture);
            GL11.glTexParameteri((int)3553, (int)10242, (int)33648);
            GL11.glTexParameteri((int)3553, (int)10243, (int)33648);
            GlStateManager.bindTexture((int)0);
            framebufferList.add(framebuffer);
            ++n;
        }
    }

    public static void renderBlur(int n, int n2, float f) {
        int n3;
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        if (currentIterations != n2 || KawaseBlur.framebuffer.framebufferWidth != KawaseBlur.mc.displayWidth || KawaseBlur.framebuffer.framebufferHeight != KawaseBlur.mc.displayHeight) {
            KawaseBlur.initFrameBuffers(n2);
            currentIterations = n2;
        }
        GlStateManager.matrixMode((int)5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        GlStateManager.ortho((double)0.0, (double)scaledResolution.getScaledWidth_double(), (double)scaledResolution.getScaledHeight_double(), (double)0.0, (double)1000.0, (double)3000.0);
        GlStateManager.matrixMode((int)5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.translate((float)0.0f, (float)0.0f, (float)-2000.0f);
        KawaseBlur.renderFBO(framebufferList.get(1), LunarPostProcessing.target((Minecraft)KawaseBlur.mc).framebufferTexture, kawaseDown, f);
        for (n3 = 1; n3 < n2; ++n3) {
            KawaseBlur.renderFBO(framebufferList.get(n3 + 1), KawaseBlur.framebufferList.get((int)n3).framebufferTexture, kawaseDown, f);
        }
        for (n3 = n2; n3 > 1; --n3) {
            KawaseBlur.renderFBO(framebufferList.get(n3 - 1), KawaseBlur.framebufferList.get((int)n3).framebufferTexture, kawaseUp, f);
        }
        Framebuffer framebuffer = framebufferList.get(0);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", f, f);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / (float)framebuffer.framebufferWidth, 1.0f / (float)framebuffer.framebufferHeight);
        kawaseUp.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        GL13.glActiveTexture((int)34000);
        RenderUtils.bindTexture(n);
        GL13.glActiveTexture((int)33984);
        RenderUtils.bindTexture(KawaseBlur.framebufferList.get((int)1).framebufferTexture);
        ShaderUtils.drawQuads();
        kawaseUp.unload();
        GL13.glActiveTexture((int)34000);
        GL11.glBindTexture((int)3553, (int)0);
        GL13.glActiveTexture((int)33984);
        LunarPostProcessing.target(mc).bindFramebuffer(true);
        RenderUtils.bindTexture(KawaseBlur.framebufferList.get((int)0).framebufferTexture);
        RenderUtils.setAlphaLimit(0.0f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc((int)770, (int)771);
        ShaderUtils.drawQuads();
        GlStateManager.bindTexture((int)0);
        GlStateManager.disableBlend();
        GlStateManager.matrixMode((int)5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode((int)5888);
        GlStateManager.popMatrix();
        if (bl) {
            GL11.glEnable((int)3089);
        }
    }

    private static void renderFBO(Framebuffer framebuffer, int n, ShaderUtils shaderUtils, float f) {
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        shaderUtils.init();
        RenderUtils.bindTexture(n);
        shaderUtils.setUniformf("offset", f, f);
        shaderUtils.setUniformi("inTexture", 0);
        shaderUtils.setUniformi("check", 0);
        shaderUtils.setUniformf("halfpixel", 1.0f / (float)framebuffer.framebufferWidth, 1.0f / (float)framebuffer.framebufferHeight);
        shaderUtils.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        ShaderUtils.drawQuads();
        shaderUtils.unload();
        if (bl) {
            GL11.glEnable((int)3089);
        }
    }

    static {
        framebufferList = new ArrayList<Framebuffer>();
    }
}

