/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL20
 */
package valen.clickgui.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

public final class SceneSnapshot {
    private int texture;
    private int textureWidth;
    private int textureHeight;

    public void capture(int n, int n2) {
        if (n <= 0 || n2 <= 0) {
            return;
        }
        if (this.texture == 0) {
            this.texture = GL11.glGenTextures();
        }
        GL11.glPushAttrib((int)262144);
        try {
            GL13.glActiveTexture((int)33984);
            GL11.glBindTexture((int)3553, (int)this.texture);
            if (this.textureWidth != n || this.textureHeight != n2) {
                GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
                GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
                GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
                GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
                GL11.glCopyTexImage2D((int)3553, (int)0, (int)32849, (int)0, (int)0, (int)n, (int)n2, (int)0);
                this.textureWidth = n;
                this.textureHeight = n2;
            } else {
                GL11.glCopyTexSubImage2D((int)3553, (int)0, (int)0, (int)0, (int)0, (int)0, (int)n, (int)n2);
            }
        }
        finally {
            GL11.glPopAttrib();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void draw(double d, double d2, float f) {
        if (this.texture == 0 || f <= 0.0f) {
            return;
        }
        int n = GL11.glGetInteger((int)35725);
        GL11.glPushAttrib((int)1048575);
        try {
            GL20.glUseProgram((int)0);
            int n2 = GL11.glGetInteger((int)34018);
            for (int i = 1; i < n2; ++i) {
                GL13.glActiveTexture((int)(33984 + i));
                GL11.glDisable((int)3553);
            }
            GL13.glActiveTexture((int)33984);
            GL11.glEnable((int)3553);
            GL11.glBindTexture((int)3553, (int)this.texture);
            GL11.glTexEnvi((int)8960, (int)8704, (int)8448);
            GL11.glDisable((int)2929);
            GL11.glDisable((int)3008);
            GL11.glDisable((int)3089);
            GL11.glDisable((int)2960);
            GL11.glDisable((int)2896);
            GL11.glDisable((int)2912);
            GL11.glDisable((int)2884);
            GL11.glDepthMask((boolean)false);
            GL11.glEnable((int)3042);
            GL14.glBlendEquation((int)32774);
            GL14.glBlendFuncSeparate((int)770, (int)771, (int)0, (int)1);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, f));
            GL11.glBegin((int)7);
            GL11.glTexCoord2d((double)0.0, (double)1.0);
            GL11.glVertex2d((double)0.0, (double)0.0);
            GL11.glTexCoord2d((double)0.0, (double)0.0);
            GL11.glVertex2d((double)0.0, (double)d2);
            GL11.glTexCoord2d((double)1.0, (double)0.0);
            GL11.glVertex2d((double)d, (double)d2);
            GL11.glTexCoord2d((double)1.0, (double)1.0);
            GL11.glVertex2d((double)d, (double)0.0);
            GL11.glEnd();
        }
        finally {
            GL20.glUseProgram((int)n);
            GL11.glPopAttrib();
        }
    }

    public void release() {
        if (this.texture != 0) {
            GL11.glDeleteTextures((int)this.texture);
            this.texture = 0;
            this.textureWidth = 0;
            this.textureHeight = 0;
        }
    }
}

