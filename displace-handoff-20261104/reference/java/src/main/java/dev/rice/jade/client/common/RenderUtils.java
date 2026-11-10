// Jade recovery: original class: jade.deps.eLz.N1vLKkf
package dev.rice.jade.client.common;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class RenderUtils implements IMinecraft {
   private static final FloatBuffer lht = BufferUtils.createFloatBuffer(16);
   private static final FloatBuffer jgivP = BufferUtils.createFloatBuffer(16);

   public static RenderUtils$1 uyB6() {
      boolean var0 = GL11.glIsEnabled(2896);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      boolean var1 = GL11.glIsEnabled(3553);
      ((Buffer)jgivP).clear();
      GL11.glGetFloat(2819, jgivP);
      RenderUtils$1 var2 = new RenderUtils$1(var0, var1, jgivP.get(0), jgivP.get(1));
      resetLightmapState();
      return var2;
   }

   public static void resetLightmapState() {
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.enableTexture2D();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.disableLighting();
   }

   public static void restoreLightmapState(RenderUtils$1 var0) {
      if (var0 == null) {
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      } else {
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, RenderUtils$1.getLightmapU(var0), RenderUtils$1.getLightmapV(var0));
         if (RenderUtils$1.bGlc(var0)) {
            GlStateManager.enableTexture2D();
         } else {
            GlStateManager.disableTexture2D();
         }

         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         if (RenderUtils$1.isLightingEnabled(var0)) {
            GlStateManager.enableLighting();
         } else {
            GlStateManager.disableLighting();
         }
      }
   }

   public static void XNRNki(double var0, double var2, double var4, double var6, int var8) {
      float var9 = (var8 >> 24 & 0xFF) / 255.0F;
      float var10 = (var8 >> 16 & 0xFF) / 255.0F;
      float var11 = (var8 >> 8 & 0xFF) / 255.0F;
      float var12 = (var8 & 0xFF) / 255.0F;
      GlStateManager.pushMatrix();
      Tessellator var13 = Tessellator.getInstance();
      WorldRenderer var14 = var13.getWorldRenderer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.color(var10, var11, var12, var9);
      var14.begin(7, DefaultVertexFormats.POSITION);
      var14.pos(var0, var6, 0.0).endVertex();
      var14.pos(var4, var6, 0.0).endVertex();
      var14.pos(var4, var2, 0.0).endVertex();
      var14.pos(var0, var2, 0.0).endVertex();
      var13.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   public static void GIWbDs(int var0) {
      GL11.glColor4f((var0 >> 16 & 0xFF) / 255.0F, (var0 >> 8 & 0xFF) / 255.0F, (var0 & 0xFF) / 255.0F, (var0 >> 24 & 0xFF) / 255.0F);
   }

   public static void setAlphaThreshold(float var0) {
      GlStateManager.enableAlpha();
      GlStateManager.alphaFunc(516, (float)(var0 * 0.01));
   }

   public static void lTbf() {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void resetGlPipelineState() {
      int var0 = GL11.glGetInteger(34016);
      resetTextureUnitState(OpenGlHelper.defaultTexUnit);
      resetTextureUnitState(OpenGlHelper.lightmapTexUnit);
      rebindActiveTexture(var0);
      boolean var1 = GL11.glIsEnabled(3008);
      int var2 = GL11.glGetInteger(3009);
      float var3 = GL11.glGetFloat(3010);
      boolean var4 = GL11.glIsEnabled(2929);
      boolean var5 = GL11.glGetBoolean(2930);
      boolean var6 = GL11.glIsEnabled(2896);
      boolean var7 = GL11.glIsEnabled(32826);
      boolean var8 = GL11.glIsEnabled(3042);
      int var9 = GL11.glGetInteger(32969);
      int var10 = GL11.glGetInteger(32968);
      int var11 = GL11.glGetInteger(32971);
      int var12 = GL11.glGetInteger(32970);
      ((Buffer)lht).clear();
      GL11.glGetFloat(2816, lht);
      float var13 = lht.get(0);
      float var14 = lht.get(1);
      float var15 = lht.get(2);
      float var16 = lht.get(3);
      applyAlphaState(var1);
      GlStateManager.alphaFunc(519, 0.0F);
      GlStateManager.alphaFunc(var2, var3);
      applyDepthState(var4);
      GlStateManager.depthMask(!var5);
      GlStateManager.depthMask(var5);
      applyLightingState(var6);
      cIv778(var7);
      QvSfy6(var8);
      GlStateManager.tryBlendFuncSeparate(1, 0, 1, 0);
      GlStateManager.tryBlendFuncSeparate(var9, var10, var11, var12);
      GlStateManager.color(var13 == 0.0F ? 1.0F : 0.0F, var14 == 0.0F ? 1.0F : 0.0F, var15 == 0.0F ? 1.0F : 0.0F, var16 == 0.0F ? 1.0F : 0.0F);
      GlStateManager.color(var13, var14, var15, var16);
   }

   private static void resetTextureUnitState(int var0) {
      rebindActiveTexture(var0);
      boolean var1 = GL11.glIsEnabled(3553);
      int var2 = GL11.glGetInteger(32873);
      if (var1) {
         GlStateManager.disableTexture2D();
         GlStateManager.enableTexture2D();
      } else {
         GlStateManager.enableTexture2D();
         GlStateManager.disableTexture2D();
      }

      if (var2 != 0) {
         GlStateManager.bindTexture(0);
      }

      GlStateManager.bindTexture(var2);
   }

   private static void rebindActiveTexture(int var0) {
      int var1 = var0 == OpenGlHelper.defaultTexUnit ? OpenGlHelper.lightmapTexUnit : OpenGlHelper.defaultTexUnit;
      GlStateManager.setActiveTexture(var1);
      GlStateManager.setActiveTexture(var0);
   }

   private static void applyAlphaState(boolean var0) {
      if (var0) {
         GlStateManager.disableAlpha();
         GlStateManager.enableAlpha();
      } else {
         GlStateManager.enableAlpha();
         GlStateManager.disableAlpha();
      }
   }

   private static void applyDepthState(boolean var0) {
      if (var0) {
         GlStateManager.disableDepth();
         GlStateManager.enableDepth();
      } else {
         GlStateManager.enableDepth();
         GlStateManager.disableDepth();
      }
   }

   private static void applyLightingState(boolean var0) {
      if (var0) {
         GlStateManager.disableLighting();
         GlStateManager.enableLighting();
      } else {
         GlStateManager.enableLighting();
         GlStateManager.disableLighting();
      }
   }

   private static void cIv778(boolean var0) {
      if (var0) {
         GlStateManager.disableRescaleNormal();
         GlStateManager.enableRescaleNormal();
      } else {
         GlStateManager.enableRescaleNormal();
         GlStateManager.disableRescaleNormal();
      }
   }

   private static void QvSfy6(boolean var0) {
      if (var0) {
         GlStateManager.disableBlend();
         GlStateManager.enableBlend();
      } else {
         GlStateManager.enableBlend();
         GlStateManager.disableBlend();
      }
   }

   public static void jxyoE(float var0, float var1, float var2, float var3, float var4, int var5) {
      if (!(var2 <= var0)) {
         float var6 = var2 - var0;
         if (var6 < 3.0F) {
            var4 = Math.min(var4, var6 / 2.0F);
         }

         var0 = (float)(var0 * 2.0);
         var1 = (float)(var1 * 2.0);
         var2 = (float)(var2 * 2.0);
         var3 = (float)(var3 * 2.0);
         GL11.glPushMatrix();
         GL11.glPushAttrib(1048575);
         GL11.glScaled(0.5, 0.5, 0.5);
         GL11.glEnable(3042);
         GL11.glDisable(3553);
         GL11.glEnable(2848);
         GL11.glBegin(9);
         GIWbDs(var5);

         for (byte var7 = 0; var7 <= 90; var7 += 3) {
            double var8 = var7 * (float) (Math.PI / 180.0);
            GL11.glVertex2d(var0 + var4 + Math.sin(var8) * var4 * -1.0, var1 + var4 + Math.cos(var8) * var4 * -1.0);
         }

         for (int var14 = 90; var14 <= 180; var14 += 3) {
            double var17 = var14 * (float) (Math.PI / 180.0);
            GL11.glVertex2d(var0 + var4 + Math.sin(var17) * var4 * -1.0, var3 - var4 + Math.cos(var17) * var4 * -1.0);
         }

         if (var2 - var0 >= 4.5) {
            for (int var15 = 0; var15 <= 90; var15++) {
               double var18 = var15 * (float) (Math.PI / 180.0);
               GL11.glVertex2d(var2 - var4 + Math.sin(var18) * var4, var3 - var4 + Math.cos(var18) * var4);
            }

            for (int var16 = 90; var16 <= 180; var16++) {
               double var19 = var16 * (float) (Math.PI / 180.0);
               GL11.glVertex2d(var2 - var4 + Math.sin(var19) * var4, var1 + var4 + Math.cos(var19) * var4);
            }
         }

         GL11.glEnd();
         GL11.glEnable(3553);
         GL11.glDisable(3042);
         GL11.glDisable(2848);
         GL11.glEnable(3553);
         GL11.glPopAttrib();
         GL11.glPopMatrix();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
