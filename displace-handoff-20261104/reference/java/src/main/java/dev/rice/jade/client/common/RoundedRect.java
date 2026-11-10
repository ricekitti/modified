// Jade recovery: original class: jade.deps.eLz.Igem8aRq
package dev.rice.jade.client.common;

import dev.rice.jade.client.module.render.shared.ShaderProgram;
import java.awt.Color;

public final class RoundedRect {
   public static ShaderProgram ABgU = new ShaderProgram("roundedRect");
   public static ShaderProgram outlineShader = new ShaderProgram("roundRectOutline");

   public static void drawRoundedRect(float var0, float var1, float var2, float var3, float var4, Color var5) {
      RoundedShader.Fzsi8(ABgU, var0, var1, var2, var3, var4, false, var5.getRGB());
   }

   public static void drawRoundedOutline(float var0, float var1, float var2, float var3, float var4, float var5, Color var6, Color var7) {
      RoundedShader.drawRoundedOutline(outlineShader, var0, var1, var2, var3, var4, var5, var6.getRGB(), var7.getRGB());
   }
}
