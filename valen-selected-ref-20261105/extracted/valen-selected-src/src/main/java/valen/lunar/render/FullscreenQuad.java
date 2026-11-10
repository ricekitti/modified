/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 */
package valen.lunar.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public final class FullscreenQuad {
    public static void begin() {
        GL11.glPushAttrib((int)270337);
        int n = GL11.glGetInteger((int)34018);
        for (int i = 1; i < n; ++i) {
            GL13.glActiveTexture((int)(33984 + i));
            GL11.glDisable((int)3553);
        }
        GL13.glActiveTexture((int)33984);
        GL11.glEnable((int)3553);
        GL11.glTexEnvi((int)8960, (int)8704, (int)8448);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void end() {
        GL11.glPopAttrib();
    }
}

