// Jade recovery: module: Arraylist (render); original class: jade.deps.eLz.MbsVIT
package dev.rice.jade.client.module.render;

import dev.rice.jade.client.module.ModuleInfo;
import dev.rice.jade.client.module.render.arraylist.ColorTheme;
import dev.rice.jade.client.setting.SliderSetting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModuleInfo(aliases = "HUD")
public final class Arraylist {
   // Rice host shell: only Arraylist's static theme-gradient members are ported (Rice draws its own module list).

   private static List<Integer> AOGM = Collections.emptyList();
   public static SliderSetting waveSpeed;

   public static int xQec0(double var0) {
      if (AOGM.size() > 1) {
         return sampleMultiColorGradient(var0);
      } else {
         return AOGM.isEmpty() ? -12326778 : AOGM.get(0);
      }
   }

   public static void setGradientColors(List<Integer> var0) {
      AOGM = (List<Integer>)(var0 == null ? Collections.emptyList() : new ArrayList<>(var0));
   }

   public static List<Integer> getGradientColors() {
      return new ArrayList<>(AOGM);
   }

   private static int sampleMultiColorGradient(double var0) {
      int var2 = AOGM.size() - 1;
      double var3 = (double)System.currentTimeMillis() / (7500L * var2) * YUzc() + var0 * 0.12 / ((Math.PI * 2) * var2);
      var3 -= Math.floor(var3);
      double var5 = var3 * var2 * 2.0;
      double var7 = var5 <= var2 ? var5 : var2 * 2.0 - var5;
      int var9 = Math.min(var2 - 1, (int)Math.floor(var7));
      double var10 = var7 - var9;
      Color var12 = new Color(AOGM.get(var9), true);
      Color var13 = new Color(AOGM.get(var9 + 1), true);
      return ColorTheme.convert(var13, var12, var10).getRGB();
   }

   private static double YUzc() {
      return waveSpeed == null ? 1.0 : Math.max(0.1, waveSpeed.getInput());
   }
}
