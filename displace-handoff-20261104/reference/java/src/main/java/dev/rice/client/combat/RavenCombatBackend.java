package dev.rice.client.combat;

import dev.rice.client.ClientContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** Drop-side calculation retained for Knockback Displace; aura primitives were removed. */
public final strictfp class RavenCombatBackend {
   private RavenCombatBackend() {
   }

   /** Returns 0 for left, 1 for right, or -1 when neither side offers a meaningful drop. */
   public static int dropSide(ClientContext c, Entity entity, int previous) {
      return dropSide(c.player(), c.world(), entity, previous);
   }

   /** OpenB4 u20+-4 WorldUtils.dropSide geometry, shared by both Displace modules. */
   public static int dropSide(Entity player, World world, Entity entity, int previous) {
      double dx = entity.posX - player.posX;
      double dz = entity.posZ - player.posZ;
      double length = Math.sqrt(dx * dx + dz * dz);
      if (length < 0.001) return -1;
      dx /= length;
      dz /= length;
      double px = -dz;
      double pz = dx;
      int left = 0, right = 0, behind = 0;
      int[] distances = new int[]{1, 2, 3, 4, 6, 8};
      for (int distance : distances) {
         int weight = distance < 6 ? 2 : 1;
         behind += openBelow(world, entity.posX + dx * distance, entity.posY, entity.posZ + dz * distance, px, pz) * weight;
         left += openBelow(world, entity.posX - px * distance, entity.posY, entity.posZ - pz * distance, dx, dz) * weight;
         right += openBelow(world, entity.posX + px * distance, entity.posY, entity.posZ + pz * distance, dx, dz) * weight;
      }
      if (left == 0 && right == 0 || behind >= left && behind >= right) return -1;
      if (left == right && previous != -1) return previous;
      return left > right ? 0 : 1;
   }

   private static int openBelow(World world, double x, double y, double z, double dx, double dz) {
      int open = 0;
      for (int offset = -1; offset <= 1; ++offset) {
         double ox = x + dx * offset;
         double oz = z + dz * offset;
         if (world.rayTraceBlocks(new Vec3(ox, y, oz), new Vec3(ox, y - 6.0, oz), true) == null) ++open;
      }
      return open;
   }
}
