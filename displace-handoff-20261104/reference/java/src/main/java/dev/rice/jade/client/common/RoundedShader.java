// Jade recovery: original class: jade.deps.eLz.lhuNLhkzBU
package dev.rice.jade.client.common;

import dev.rice.jade.client.gui.ClickGui;
import dev.rice.jade.client.module.render.shared.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public final class RoundedShader {

   public static void Fzsi8(ShaderProgram var0, float var1, float var2, float var3, float var4, float var5, boolean var6, int var7) {
      beginBlendPass(true);
      var0.eBml();
      applyRectUniforms(var0, var1, var2, var3, var4, var5);
      var0.lyik("blur", var6 ? 1 : 0);
      setUniformColor(var0, "color", var7);
      yJdrG(var1, var2, var3, var4);
      endShaderPass(var0);
   }

   public static void drawRoundedOutline(ShaderProgram var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8) {
      beginBlendPass(true);
      var0.eBml();
      applyRectUniforms(var0, var1, var2, var3, var4, var5);
      var0.setUniform("outlineThickness", var6 * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
      setUniformColor(var0, "color", var7);
      setUniformColor(var0, "outlineColor", var8);
      float[] var9 = RectUtils.expandRectByPadding(var1, var2, var3, var4, var6);
      ShaderProgram.drawQuad(var9[0], var9[1], var9[2], var9[3]);
      endShaderPass(var0);
   }

   private static void applyRectUniforms(ShaderProgram var0, float var1, float var2, float var3, float var4, float var5) {
      Minecraft var6 = Minecraft.getMinecraft();
      float var7 = (float)(new ScaledResolution(var6).getScaleFactor() * ClickGui.getActiveRenderScale());
      float[] var8 = RectUtils.toScaledPosition(var1, var2, var4, var7, var6.displayHeight);
      var0.setUniform("location", var8[0], var8[1]);
      var0.setUniform("rectSize", var3 * var7, var4 * var7);
      var0.setUniform("radius", var5 * var7);
   }

   private static void setUniformColor(ShaderProgram var0, String var1, int var2) {
      var0.setUniform(var1, ArgbColorUtils.toRgbaArray(var2));
   }

   private static void yJdrG(float var0, float var1, float var2, float var3) {
      float[] var4 = RectUtils.expandRect(var0, var1, var2, var3);
      ShaderProgram.drawQuad(var4[0], var4[1], var4[2], var4[3]);
   }

   private static void beginBlendPass(boolean var0) {
      RenderUtils.lTbf();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      if (var0) {
         GL11.glBlendFunc(770, 771);
      }

      RenderUtils.setAlphaThreshold(0.0F);
   }

   private static void endShaderPass(ShaderProgram var0) {
      var0.unbindProgram();
      GlStateManager.disableBlend();
      restoreGlState();
   }

   private static void restoreGlState() {
      RenderUtils.setAlphaThreshold(10.0F);
      RenderUtils.lTbf();
   }
}
