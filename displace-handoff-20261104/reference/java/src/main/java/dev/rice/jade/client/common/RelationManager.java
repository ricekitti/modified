package dev.rice.jade.client.common;

import dev.rice.client.RelationshipService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/** Rice host shim for OpenJade's RelationManager: friends come from Rice's Friends module (relationship flag 4). */
public final class RelationManager {
   public boolean isFriend(String name) {
      Minecraft mc = Minecraft.getMinecraft();
      if (name == null || mc.theWorld == null) {
         return false;
      }

      for (EntityPlayer player : mc.theWorld.playerEntities) {
         if (name.equalsIgnoreCase(player.getName()) && (RelationshipService.flags(player) & 4) != 0) {
            return true;
         }
      }

      return false;
   }
}
