/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.shader.Framebuffer
 *  org.lwjgl.opengl.EXTFramebufferObject
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 */
package valen.lunar.render;

import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class LunarFramebuffer
extends Framebuffer {
    public LunarFramebuffer(int n, int n2, boolean bl) {
        super(n, n2, bl);
    }

    public static Framebuffer resize(Framebuffer framebuffer) {
        return LunarFramebuffer.resize(framebuffer, false);
    }

    public static Framebuffer resize(Framebuffer framebuffer, boolean bl) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!(framebuffer instanceof LunarFramebuffer) || framebuffer.framebufferWidth != minecraft.displayWidth || framebuffer.framebufferHeight != minecraft.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new LunarFramebuffer(minecraft.displayWidth, minecraft.displayHeight, bl);
        }
        return framebuffer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createBindFramebuffer(int n, int n2) {
        int n3 = GL11.glGetInteger((int)36006);
        GL11.glPushAttrib((int)805120);
        try {
            this.deleteFramebuffer();
            this.framebufferWidth = this.framebufferTextureWidth = Math.max(1, n);
            this.framebufferHeight = this.framebufferTextureHeight = Math.max(1, n2);
            this.framebufferObject = EXTFramebufferObject.glGenFramebuffersEXT();
            this.framebufferTexture = GL11.glGenTextures();
            GL13.glActiveTexture((int)33984);
            GL11.glBindTexture((int)3553, (int)this.framebufferTexture);
            GL11.glTexImage2D((int)3553, (int)0, (int)32856, (int)this.framebufferWidth, (int)this.framebufferHeight, (int)0, (int)6408, (int)5121, (ByteBuffer)null);
            this.setFramebufferFilter(9729);
            LunarFramebuffer.bind(this.framebufferObject);
            EXTFramebufferObject.glFramebufferTexture2DEXT((int)36160, (int)36064, (int)3553, (int)this.framebufferTexture, (int)0);
            if (this.useDepth) {
                this.depthBuffer = EXTFramebufferObject.glGenRenderbuffersEXT();
                int n4 = GL11.glGetInteger((int)36007);
                EXTFramebufferObject.glBindRenderbufferEXT((int)36161, (int)this.depthBuffer);
                EXTFramebufferObject.glRenderbufferStorageEXT((int)36161, (int)33190, (int)this.framebufferWidth, (int)this.framebufferHeight);
                EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36096, (int)36161, (int)this.depthBuffer);
                EXTFramebufferObject.glBindRenderbufferEXT((int)36161, (int)n4);
            }
            this.checkFramebufferComplete();
            this.framebufferClear();
        }
        finally {
            LunarFramebuffer.bind(n3);
            GL11.glPopAttrib();
        }
    }

    public void createFramebuffer(int n, int n2) {
        this.createBindFramebuffer(n, n2);
    }

    public void checkFramebufferComplete() {
        int n = EXTFramebufferObject.glCheckFramebufferStatusEXT((int)36160);
        if (n != 36053) {
            throw new IllegalStateException("Incomplete Valen render buffer: " + n);
        }
    }

    public void deleteFramebuffer() {
        if (this.depthBuffer >= 0) {
            EXTFramebufferObject.glDeleteRenderbuffersEXT((int)this.depthBuffer);
        }
        if (this.framebufferTexture >= 0) {
            GL11.glDeleteTextures((int)this.framebufferTexture);
        }
        if (this.framebufferObject >= 0) {
            EXTFramebufferObject.glDeleteFramebuffersEXT((int)this.framebufferObject);
        }
        this.framebufferObject = -1;
        this.framebufferTexture = -1;
        this.depthBuffer = -1;
    }

    public void setFramebufferFilter(int n) {
        this.framebufferFilter = n;
        int n2 = GL11.glGetInteger((int)34016);
        GL13.glActiveTexture((int)33984);
        int n3 = GL11.glGetInteger((int)32873);
        GL11.glBindTexture((int)3553, (int)this.framebufferTexture);
        GL11.glTexParameteri((int)3553, (int)10241, (int)n);
        GL11.glTexParameteri((int)3553, (int)10240, (int)n);
        GL11.glTexParameteri((int)3553, (int)10242, (int)33648);
        GL11.glTexParameteri((int)3553, (int)10243, (int)33648);
        GL11.glBindTexture((int)3553, (int)n3);
        GL13.glActiveTexture((int)n2);
    }

    public void framebufferClear() {
        int n = GL11.glGetInteger((int)36006);
        GL11.glPushAttrib((int)540928);
        LunarFramebuffer.bind(this.framebufferObject);
        GL11.glDisable((int)3089);
        GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        GL11.glDepthMask((boolean)true);
        GL11.glClearColor((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f);
        GL11.glClearDepth((double)1.0);
        GL11.glClear((int)(0x4000 | (this.useDepth ? 256 : 0)));
        LunarFramebuffer.bind(n);
        GL11.glPopAttrib();
    }

    public void bindFramebuffer(boolean bl) {
        LunarFramebuffer.bind(this.framebufferObject);
        if (bl) {
            GL11.glViewport((int)0, (int)0, (int)this.framebufferWidth, (int)this.framebufferHeight);
        }
    }

    public void unbindFramebuffer() {
        LunarFramebuffer.bind(0);
    }

    public void bindFramebufferTexture() {
        GL11.glBindTexture((int)3553, (int)this.framebufferTexture);
    }

    public void unbindFramebufferTexture() {
        GL11.glBindTexture((int)3553, (int)0);
    }

    static void bind(int n) {
        EXTFramebufferObject.glBindFramebufferEXT((int)36160, (int)n);
    }
}

