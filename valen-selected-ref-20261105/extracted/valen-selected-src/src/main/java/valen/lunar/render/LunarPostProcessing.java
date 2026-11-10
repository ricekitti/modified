/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.shader.Framebuffer
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 */
package valen.lunar.render;

import java.nio.IntBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import valen.lunar.render.LunarFramebuffer;
import valen.utility.shader.BlurUtils;

public final class LunarPostProcessing {
    private static final IntBuffer viewport = BufferUtils.createIntBuffer((int)16);
    private static LunarFramebuffer scene;
    private static int output;

    public static void prepareBlur() {
        Minecraft minecraft = Minecraft.getMinecraft();
        LunarPostProcessing.begin(minecraft.displayWidth, minecraft.displayHeight);
        BlurUtils.prepareBlur();
    }

    public static void prepareBloom() {
        Minecraft minecraft = Minecraft.getMinecraft();
        LunarPostProcessing.begin(minecraft.displayWidth, minecraft.displayHeight, false);
        BlurUtils.prepareBloom();
    }

    public static void blurEnd(int n, float f) {
        try {
            BlurUtils.blurEnd(n, f);
        }
        finally {
            LunarPostProcessing.end();
        }
    }

    public static void bloomEnd(int n, float f) {
        try {
            BlurUtils.bloomEnd(n, f);
        }
        finally {
            LunarPostProcessing.end();
        }
    }

    public static void begin(int n, int n2) {
        LunarPostProcessing.begin(n, n2, true);
    }

    private static void begin(int n, int n2, boolean bl) {
        output = GL11.glGetInteger((int)36006);
        viewport.clear();
        GL11.glGetInteger((int)2978, (IntBuffer)viewport);
        if (scene == null) {
            scene = new LunarFramebuffer(n, n2, false){

                @Override
                public void bindFramebuffer(boolean bl) {
                    LunarFramebuffer.bind(output);
                    if (bl) {
                        GL11.glViewport((int)viewport.get(0), (int)viewport.get(1), (int)viewport.get(2), (int)viewport.get(3));
                    }
                }
            };
        } else if (LunarPostProcessing.scene.framebufferWidth != n || LunarPostProcessing.scene.framebufferHeight != n2) {
            scene.createBindFramebuffer(n, n2);
        }
        if (bl) {
            GL11.glPushAttrib((int)262144);
            GL13.glActiveTexture((int)33984);
            GL11.glBindTexture((int)3553, (int)LunarPostProcessing.scene.framebufferTexture);
            GL11.glCopyTexSubImage2D((int)3553, (int)0, (int)0, (int)0, (int)0, (int)0, (int)n, (int)n2);
            GL11.glPopAttrib();
        }
    }

    public static Framebuffer target(Minecraft minecraft) {
        return scene;
    }

    public static void end() {
        LunarFramebuffer.bind(output);
        GL11.glViewport((int)viewport.get(0), (int)viewport.get(1), (int)viewport.get(2), (int)viewport.get(3));
    }
}

