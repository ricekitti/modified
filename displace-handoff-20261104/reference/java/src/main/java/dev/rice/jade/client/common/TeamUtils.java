package dev.rice.jade.client.common;

import dev.rice.client.RelationshipService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

/** Rice host shim for OpenJade's TeamUtils: teammates come from Rice's Teams module (relationship flag 1). */
public final class TeamUtils {
   private TeamUtils() {
   }

   public static boolean isTeammate(Minecraft mc, Entity entity) {
      return entity != null && (RelationshipService.flags(entity) & 1) != 0;
   }
}
