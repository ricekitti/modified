// Jade recovery: original class: jade.deps.eLz.rRiiO2d2
package dev.rice.jade.client.common;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public final class GameStateUtils {

   public static boolean isInWorld(Minecraft var0) {
      return var0.thePlayer != null && var0.theWorld != null;
   }

   public static boolean isInGameWithFocus(Minecraft var0) {
      return var0.currentScreen == null && var0.inGameHasFocus;
   }

   public static boolean isOnHypixel(Minecraft var0) {
      return !var0.isSingleplayer() && var0.getCurrentServerData() != null && var0.getCurrentServerData().serverIP.contains("hypixel.net");
   }

   public static boolean yrvB(Minecraft var0) {
      return Keyboard.isKeyDown(var0.gameSettings.keyBindJump.getKeyCode());
   }
}
