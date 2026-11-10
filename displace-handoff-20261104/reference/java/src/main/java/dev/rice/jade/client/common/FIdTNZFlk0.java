// Jade recovery: original class: jade.deps.eLz.FIdTNZFlk0
package dev.rice.jade.client.common;

import dev.rice.jade.client.module.client.Settings;
import net.minecraft.client.Minecraft;

public final class FIdTNZFlk0 {

   public static void niSo8(float var0, float var1) {
      Minecraft.getMinecraft().thePlayer.rotationYawHead = var0;
      if (Settings.rotateBody.isToggled() && Settings.fullBody.isToggled()) {
         Minecraft.getMinecraft().thePlayer.prevRenderYawOffset = var1;
         Minecraft.getMinecraft().thePlayer.renderYawOffset = var0;
      }
   }
}
