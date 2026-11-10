/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.shader.Framebuffer
 *  org.lwjgl.opengl.GL11
 *  valen.utility.shader.KawaseBloom
 */
package valen.utility.shader;

import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import valen.lunar.render.LunarFramebuffer;
import valen.utility.shader.KawaseBloom;
import valen.utility.shader.KawaseBlur;

public class BlurUtils {
    private static Framebuffer stencilFrameBufferBlur = new LunarFramebuffer(1, 1, false);
    private static Framebuffer stencilFrameBufferBloom = new LunarFramebuffer(1, 1, false);

    public static void prepareBlur() {
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        stencilFrameBufferBlur = LunarFramebuffer.resize(stencilFrameBufferBlur);
        stencilFrameBufferBlur.framebufferClear();
        stencilFrameBufferBlur.bindFramebuffer(true);
        if (bl) {
            GL11.glEnable((int)3089);
        }
    }

    public static void prepareBloom() {
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        stencilFrameBufferBloom = LunarFramebuffer.resize(stencilFrameBufferBloom);
        stencilFrameBufferBloom.framebufferClear();
        stencilFrameBufferBloom.bindFramebuffer(true);
        if (bl) {
            GL11.glEnable((int)3089);
        }
    }

    public static void blurEnd(int n, float f) {
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        stencilFrameBufferBlur.unbindFramebuffer();
        KawaseBlur.renderBlur(BlurUtils.stencilFrameBufferBlur.framebufferTexture, n, f);
        if (bl) {
            GL11.glEnable((int)3089);
        }
    }

    public static void bloomEnd(int n, float f) {
        boolean bl = GL11.glIsEnabled((int)3089);
        if (bl) {
            GL11.glDisable((int)3089);
        }
        stencilFrameBufferBloom.unbindFramebuffer();
        KawaseBloom.renderBlur((int)BlurUtils.stencilFrameBufferBloom.framebufferTexture, (int)n, (float)f);
        if (bl) {
            GL11.glEnable((int)3089);
        }
    }
}

