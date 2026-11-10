package dev.rice.client.action;

import dev.rice.client.ClientContext;
import dev.rice.client.RelationshipService;
import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Unfiltered interaction ray: combat filters must not hide a shop from use input. */
public final class InteractionTarget {
   private InteractionTarget() { }

   public static MovingObjectPosition find(ClientContext c) {
      if (!c.ready()) return null;
      Entity viewer = c.minecraft().getRenderViewEntity();
      if (viewer == null) viewer = c.player();
      double reach = c.minecraft().playerController.getBlockReachDistance();
      MovingObjectPosition selected = viewer.rayTrace(reach, 1.0F);
      Vec3 origin = viewer.getPositionEyes(1.0F);
      Vec3 look = viewer.getLook(1.0F);
      Vec3 end = origin.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
      double distance = selected == null ? reach : origin.distanceTo(selected.hitVec);
      double entityReach = c.minecraft().playerController.extendedReach() ? 6.0D : 3.0D;
      for (Object value : c.world().loadedEntityList) {
         Entity entity = (Entity)value;
         if (entity == viewer || !entity.canBeCollidedWith()) continue;
         float border = entity.getCollisionBorderSize();
         AxisAlignedBB bounds = entity.getEntityBoundingBox().expand(border, border, border);
         MovingObjectPosition intercept = bounds.calculateIntercept(origin, end);
         boolean inside = bounds.isVecInside(origin);
         if (!inside && intercept == null) continue;
         Vec3 point = inside ? origin : intercept.hitVec;
         double candidate = origin.distanceTo(point);
         if (entity == viewer.ridingEntity && !inside) continue;
         if (candidate <= entityReach && (candidate < distance || selected == null)) {
            distance = candidate;
            selected = new MovingObjectPosition(entity, point);
         }
      }
      return selected;
   }

   public static boolean isNpc(ClientContext c, Entity entity) {
      if (entity instanceof EntityVillager || entity instanceof EntityArmorStand) return true;
      if (!(entity instanceof EntityPlayer) || entity == c.player()) return false;
      if ((RelationshipService.flags(entity) & 2) != 0) return true;
      if (shopLabel(entity.getName()) || shopLabel(entity.getDisplayName().getUnformattedText())) return true;
      // Hypixel lobby/game NPCs (the game selectors, shop keepers, etc.) are player-model entities spawned with
      // version-2 UUIDs; genuine Mojang players are version-4. Treat version-2 as an NPC so interaction yields.
      java.util.UUID uuid = entity.getUniqueID();
      if (uuid != null && uuid.version() == 2) return true;
      return c.minecraft().getNetHandler() != null
         && c.minecraft().getNetHandler().getPlayerInfo(entity.getUniqueID()) == null;
   }

   private static boolean shopLabel(String label) {
      if (label == null) return false;
      String text = EnumChatFormatting.getTextWithoutFormattingCodes(label).trim().toLowerCase(Locale.ROOT);
      return text.contains("shop") || text.contains("upgrades") || text.contains("merchant");
   }
}
