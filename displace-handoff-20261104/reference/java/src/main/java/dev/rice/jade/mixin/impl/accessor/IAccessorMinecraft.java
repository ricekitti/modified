package dev.rice.jade.mixin.impl.accessor;

import dev.rice.client.MinecraftFields;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

/**
 * Rice host replacement for Jade's Minecraft mixin accessor; {@link #of(Object)} replaces upstream's cast and
 * the getters/setters are backed by Rice's MinecraftFields.
 */
public interface IAccessorMinecraft {
   void setRightClickDelayTimer(int ticks);

   int getRightClickDelayTimer();

   Timer getTimer();

   static IAccessorMinecraft of(Object minecraft) {
      final Minecraft target = (Minecraft)minecraft;
      return new IAccessorMinecraft() {
         @Override
         public void setRightClickDelayTimer(int ticks) {
            MinecraftFields.setRightClickDelayTimer(target, ticks);
         }

         @Override
         public int getRightClickDelayTimer() {
            return MinecraftFields.rightClickDelayTimer(target);
         }

         @Override
         public Timer getTimer() {
            return MinecraftFields.timer(target);
         }
      };
   }
}
