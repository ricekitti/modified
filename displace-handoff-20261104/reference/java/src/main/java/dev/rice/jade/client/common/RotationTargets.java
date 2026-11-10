// Jade recovery: original class: jade.deps.eLz.R0SZtk8hoS
package dev.rice.jade.client.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public final class RotationTargets {

   public static float[] anglesToEntityAimPoint(EntityPlayer var0, Entity var1, AimPoint var2) {
      if (var1 == null) {
         return null;
      } else {
         double var3 = var1 instanceof EntityLivingBase
            ? var1.posY + var2.offset(var1) * 0.9
            : (var1.getEntityBoundingBox().minY + var1.getEntityBoundingBox().maxY) * 0.5;
         return AngleUtils.computeRotations(var1.posX - var0.posX, var3 - JIWq(var0), var1.posZ - var0.posZ, var0.rotationYaw, var0.rotationPitch, 3.0F);
      }
   }

   public static float[] lccPbxo(EntityPlayer var0, Entity var1, float var2, float var3, float var4) {
      float[] var5 = anglesToEntityAimPoint(var0, var1, AimPoint.NATURAL);
      return var5 == null ? null : RotationQuantizer.quantizeRotation(var5[0], var5[1], var2, var3, var4);
   }

   public static float[] anglesToCoordinates(EntityPlayer var0, double var1, double var3, double var5, float var7, float var8, float var9) {
      return AngleUtils.computeRotationsSafe(var1 - var0.posX, var3 - JIWq(var0), var5 - var0.posZ, var7, var8, var9);
   }

   private static double JIWq(EntityPlayer var0) {
      return var0.posY + var0.getEyeHeight();
   }
}
