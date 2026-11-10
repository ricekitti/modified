// Jade recovery: original class: jade.deps.eLz.m1vrmZURg8
package dev.rice.jade.client.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RotationUtils implements IMinecraft {
   private static final RayTracer rayTracer = new RayTracer(mc);
   public static float pN0;
   public static float previousPitch;
   public static float outgoingYaw;
   public static float IuZ;
   public static float[] lastSentRotation = new float[]{0.0F, 0.0F};
   public static Float[] gib;
   public static boolean hasPendingRotationOverride;

   public static void applyRenderedYaw(float var0) {
      FIdTNZFlk0.niSo8(var0, IuZ);
   }

   public static float[] QweciK(Entity var0, float var1, float var2) {
      return RotationTargets.lccPbxo(mc.thePlayer, var0, var1, var2, mc.gameSettings.mouseSensitivity);
   }

   private static float[] anglesToCoordinatesWithPitchOffset(double var0, double var2, double var4, float var6, float var7, float var8) {
      return RotationTargets.anglesToCoordinates(mc.thePlayer, var0, var2, var4, var6, var7, var8);
   }

   public static float[] anglesToEntity(Entity var0, double var1, double var3, float var5, float var6) {
      Vec3 var7 = getEntityHitVec(var0, var1, var3);
      if (var7 == null) {
         return null;
      } else {
         float var8 = hasHitOffset(var1, var3) ? 0.0F : 3.0F;
         return anglesToCoordinatesWithPitchOffset(var7.xCoord, var7.yCoord, var7.zCoord, var5, var6, var8);
      }
   }

   private static boolean hasHitOffset(double var0, double var2) {
      return var0 > 0.0 || var2 > 0.0;
   }

   public static Vec3 getEntityHitVec(Entity var0, double var1, double var3) {
      return HitVecUtils.getAimPointOnEntity(var0, mc.thePlayer, var1, var3);
   }

   private static RaytraceUtils$1 createEntityIgnoreFilter(final boolean var0, final boolean var1) {
      return var0 && var1 ? RaytraceUtils.KmB : new RaytraceUtils$1() {
         @Override
         public boolean shouldIgnoreEntity(Entity var1x) {
            boolean var2 = var1x instanceof EntityPlayer && (ClientUtils.isFriend((EntityPlayer)var1x) || ClientUtils.isTeammate((EntityPlayer)var1x));
            return var2 ? var0 : var1;
         }
      };
   }

   public static float[] anglesToEntityRayTracedFull(Entity var0, double var1, double var3, float var5, float var6, double var7, boolean var9, boolean var10, boolean var11) {
      float var12 = hasHitOffset(var1, var3) ? 0.0F : 3.0F;
      Vec3 var13 = EntityReachTracer.getHitVector(mc.theWorld, mc.thePlayer, var0, var1, var3, var7, var9, createEntityIgnoreFilter(var10, var11));
      return var13 == null ? null : anglesToCoordinatesWithPitchOffset(var13.xCoord, var13.yCoord, var13.zCoord, var5, var6, var12);
   }

   public static float[] smoothAnglesRandomized(float var0, float var1, float var2, float var3, int var4, float var5) {
      return AngleUtils.interpolateRotation(var0, var1, var2, var3, var4, var5, Math::random);
   }

   public static float[] NSsr(float var0, float var1, float var2, float var3) {
      return RotationQuantizer.quantizeRotation(var0, var1, var2, var3, mc.gameSettings.mouseSensitivity);
   }

   public static MovingObjectPosition traceBlockHit(double var0, float var2, float var3) {
      return rayTracer.juhtohK(var0, var2, var3);
   }

   public static MovingObjectPosition traceBlockThroughUncollidable(double var0, float var2, float var3) {
      return rayTracer.traceBlocks(var0, var2, var3, false, false, true);
   }

   public static Vec3 getLookVector(float var0, float var1) {
      return AngleUtils.getLookVector(var1, var0);
   }
}
