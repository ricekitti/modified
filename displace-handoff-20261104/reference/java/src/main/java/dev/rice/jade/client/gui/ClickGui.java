package dev.rice.jade.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/**
 * Rice host shim for OpenJade's ClickGui. Rice never shows Jade's ClickGui, so the verbatim
 * getActiveRenderScale() below always takes upstream's "not in ClickGui" branch (1.0).
 */
public class ClickGui extends GuiScreen {
   public double getRenderScale() {
      return 1.0;
   }

   public static double getActiveRenderScale() {
      Minecraft var0 = Minecraft.getMinecraft();
      return var0.currentScreen instanceof ClickGui ? ((ClickGui)var0.currentScreen).getRenderScale() : 1.0;
   }
}
