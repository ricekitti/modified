package dev.rice.client.combat.ravenbs;

import com.google.common.base.Predicates;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;

/** Raven bS RotationUtils aim-point, backup visibility, smoothing and mouse-quantization dependency closure.
 * Arithmetic and branch ordering come from tools/fixtures/ravenbs/RotationUtils.java.txt.
 */
public final class RavenBsRotations {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static float[] serverRotations = new float[]{0.0F, 0.0F};
   public static boolean setFakeRotations;
   public static float renderYaw;
   private static final double BACKUP_FACE_INSET = 0.05;
   private static final int BACKUP_TARGET_TOTAL = 30;
   private static final float FAR_THRESHOLD = 180.0F;
   private RavenBsRotations() {}
   private static boolean canRiderInteract(Entity entity) {
      // Forge adds this optional method; vanilla 1.8.9 mounts do not allow rider interaction.
      try { return (Boolean)entity.getClass().getMethod("canRiderInteract").invoke(entity); }
      catch (NoSuchMethodException vanilla) { return false; }
      catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
   }
   public static float unwrapYaw(float yaw, float prevYaw) {
      return prevYaw + (((yaw - prevYaw + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F);
   }
   /** RotationHelper's full-multipoint overload used by Raven bS KillAura. */
   public static float[] toTarget(Entity target, float baseYaw, float basePitch, int speed,
         double range, boolean throughBlocks, boolean throughEntities) {
      boolean backup = !throughBlocks || !throughEntities;
      float[] rot = backup ? getRotationsWithBackup(target, 100.0, 100.0, baseYaw, basePitch,
            range, throughBlocks, throughEntities, true)
            : getRotations(target, 100.0, 100.0, baseYaw, basePitch);
      return rot == null ? null : smoothRotation(baseYaw, basePitch, rot[0], rot[1], speed, 0.0F);
   }
   public static double distanceFromYaw(Entity entity, boolean b) {
      return Math.abs(MathHelper.wrapAngleTo180_double((double)(i(entity.posX, entity.posZ) - (b && setFakeRotations ? renderYaw : mc.thePlayer.rotationYaw))));
   }

   public static float i(double n, double n2) {
      return (float)(Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * (double)57.29578F * (double)-1.0F);
   }


   public static float[] getRotationsToPoint(double x, double y, double z) {
      return getRotationsToPoint(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
   }

   public static float[] getRotationsToPoint(double x, double y, double z, float baseYaw, float basePitch) {
      double deltaX = x - mc.thePlayer.posX;
      double deltaZ = z - mc.thePlayer.posZ;
      double deltaY = y - (mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight());
      double horizDistSq = deltaX * deltaX + deltaZ * deltaZ;
      float yaw;
      float targetPitch;
      if (horizDistSq < 1.0E-12) {
         yaw = baseYaw;
         targetPitch = (float)(-(Math.atan2(deltaY, (double)0.0F) * (double)57.29578F));
      } else {
         float targetYaw = (float)(Math.atan2(deltaZ, deltaX) * (double)57.29578F) - 90.0F;
         yaw = baseYaw + MathHelper.wrapAngleTo180_float(targetYaw - baseYaw);
         double horizDist = (double)MathHelper.sqrt_double(horizDistSq);
         targetPitch = (float)(-(Math.atan2(deltaY, horizDist) * (double)57.29578F));
      }

      float pitch = basePitch + MathHelper.wrapAngleTo180_float(targetPitch - basePitch) + 3.0F;
      return new float[]{yaw, clampPitch(pitch)};
   }

   public static float[] getRotations(Entity entity, double horizontalMultipoint, double verticalMultipoint, float baseYaw, float basePitch) {
      Vec3 aimPoint = getAimPoint(entity, horizontalMultipoint, verticalMultipoint);
      return aimPoint == null ? null : getRotationsToPoint(aimPoint.xCoord, aimPoint.yCoord, aimPoint.zCoord, baseYaw, basePitch);
   }

   public static Vec3 getAimPoint(Entity entity, double horizontalMultipoint, double verticalMultipoint) {
      if (entity != null && mc.thePlayer != null) {
         float borderSize = entity.getCollisionBorderSize();
         AxisAlignedBB bb = entity.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
         double centerX = (bb.minX + bb.maxX) / (double)2.0F;
         double centerY;
         if (entity instanceof EntityLivingBase) {
            centerY = entity.posY + (double)((EntityLivingBase)entity).getEyeHeight();
         } else {
            centerY = (bb.minY + bb.maxY) / (double)2.0F;
         }

         double centerZ = (bb.minZ + bb.maxZ) / (double)2.0F;
         Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
         if (bb.isVecInside(eye)) {
            return new Vec3(centerX, eye.yCoord, centerZ);
         } else {
            Vec3 cl = closestPointOnAabb(bb, eye);
            double tH = Math.max((double)0.0F, Math.min((double)1.0F, horizontalMultipoint / (double)100.0F));
            double tV = Math.max((double)0.0F, Math.min((double)1.0F, verticalMultipoint / (double)100.0F));
            double targetX = centerX + (cl.xCoord - centerX) * tH;
            double targetY = centerY + (cl.yCoord - centerY) * tV;
            double targetZ = centerZ + (cl.zCoord - centerZ) * tH;
            return new Vec3(targetX, targetY, targetZ);
         }
      } else {
         return null;
      }
   }

   public static Vec3 closestPointOnAabb(AxisAlignedBB box, Vec3 point) {
      double x = Math.max(box.minX, Math.min(box.maxX, point.xCoord));
      double y = Math.max(box.minY, Math.min(box.maxY, point.yCoord));
      double z = Math.max(box.minZ, Math.min(box.maxZ, point.zCoord));
      return new Vec3(x, y, z);
   }

   public static List<Vec3> buildBackupPoints(Entity entity, Vec3 eye) {
      if (entity != null && mc.thePlayer != null) {
         float borderSize = entity.getCollisionBorderSize();
         AxisAlignedBB bb = entity.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
         double sizeX = bb.maxX - bb.minX;
         double sizeY = bb.maxY - bb.minY;
         double sizeZ = bb.maxZ - bb.minZ;
         boolean xPos = eye.xCoord > bb.maxX;
         boolean xNeg = eye.xCoord < bb.minX;
         boolean yPos = eye.yCoord > bb.maxY;
         boolean yNeg = eye.yCoord < bb.minY;
         boolean zPos = eye.zCoord > bb.maxZ;
         boolean zNeg = eye.zCoord < bb.minZ;
         int visibleFaceCount = (!xPos && !xNeg ? 0 : 1) + (!yPos && !yNeg ? 0 : 1) + (!zPos && !zNeg ? 0 : 1);
         if (visibleFaceCount == 0) {
            return new ArrayList();
         } else {
            int pointsPerFace = 30 / visibleFaceCount;
            List<Vec3> points = new ArrayList(36);
            if (xPos || xNeg) {
               double fixedX = xPos ? bb.maxX - 0.05 : bb.minX + 0.05;
               addFaceGrid(points, 0, fixedX, bb.minY + 0.05, bb.maxY - 0.05, bb.minZ + 0.05, bb.maxZ - 0.05, pointsPerFace, sizeY, sizeZ);
            }

            if (yPos || yNeg) {
               double fixedY = yPos ? bb.maxY - 0.05 : bb.minY + 0.05;
               addFaceGrid(points, 1, fixedY, bb.minX + 0.05, bb.maxX - 0.05, bb.minZ + 0.05, bb.maxZ - 0.05, pointsPerFace, sizeX, sizeZ);
            }

            if (zPos || zNeg) {
               double fixedZ = zPos ? bb.maxZ - 0.05 : bb.minZ + 0.05;
               addFaceGrid(points, 2, fixedZ, bb.minX + 0.05, bb.maxX - 0.05, bb.minY + 0.05, bb.maxY - 0.05, pointsPerFace, sizeX, sizeY);
            }

            return points;
         }
      } else {
         return new ArrayList();
      }
   }

   private static void addFaceGrid(List<Vec3> out, int fixedAxis, double fixedVal, double uMin, double uMax, double vMin, double vMax, int targetPoints, double dimU, double dimV) {
      if (!(dimU < 1.0E-4) && !(dimV < 1.0E-4)) {
         double ratio = dimU / dimV;
         int gridU = Math.max(2, (int)Math.round(Math.sqrt((double)targetPoints * ratio)));
         int gridV = Math.max(2, (int)Math.round(Math.sqrt((double)targetPoints / ratio)));

         for(int i = 0; i < gridU; ++i) {
            double u = uMin + (uMax - uMin) * (double)i / (double)(gridU - 1);

            for(int j = 0; j < gridV; ++j) {
               double v = vMin + (vMax - vMin) * (double)j / (double)(gridV - 1);
               switch (fixedAxis) {
                  case 0:
                     out.add(new Vec3(fixedVal, u, v));
                     break;
                  case 1:
                     out.add(new Vec3(u, fixedVal, v));
                     break;
                  case 2:
                     out.add(new Vec3(u, v, fixedVal));
               }
            }
         }

      } else {
         double uMid = (uMin + uMax) / (double)2.0F;
         double vMid = (vMin + vMax) / (double)2.0F;
         switch (fixedAxis) {
            case 0:
               out.add(new Vec3(fixedVal, uMid, vMid));
               break;
            case 1:
               out.add(new Vec3(uMid, fixedVal, vMid));
               break;
            case 2:
               out.add(new Vec3(uMid, vMid, fixedVal));
         }

      }
   }

   public static double distanceSqFromEyeToClosestOnAABB(Entity entity) {
      if (entity != null && mc.thePlayer != null) {
         Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
         float borderSize = entity.getCollisionBorderSize();
         AxisAlignedBB bb = entity.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
         Vec3 closest = closestPointOnAabb(bb, eye);
         double dx = eye.xCoord - closest.xCoord;
         double dy = eye.yCoord - closest.yCoord;
         double dz = eye.zCoord - closest.zCoord;
         return dx * dx + dy * dy + dz * dz;
      } else {
         return Double.MAX_VALUE;
      }
   }

   public static double distanceFromEyeToClosestOnAABB(Entity entity) {
      double dSq = distanceSqFromEyeToClosestOnAABB(entity);
      return dSq == Double.MAX_VALUE ? Double.MAX_VALUE : Math.sqrt(dSq);
   }

   public static double distanceFromEyeToClosestOnAABB(Entity entity, Vec3 position) {
      double dSq = distanceSqFromEyeToClosestOnAABB(entity, position);
      return dSq == Double.MAX_VALUE ? Double.MAX_VALUE : Math.sqrt(dSq);
   }

   public static double distanceSqFromEyeToClosestOnAABB(Entity entity, Vec3 position) {
      if (entity != null && position != null && mc.thePlayer != null) {
         Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
         float borderSize = entity.getCollisionBorderSize();
         double offsetX = position.xCoord - entity.posX;
         double offsetY = position.yCoord - entity.posY;
         double offsetZ = position.zCoord - entity.posZ;
         AxisAlignedBB bb = entity.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ).expand((double)borderSize, (double)borderSize, (double)borderSize);
         Vec3 closest = closestPointOnAabb(bb, eye);
         double dx = eye.xCoord - closest.xCoord;
         double dy = eye.yCoord - closest.yCoord;
         double dz = eye.zCoord - closest.zCoord;
         return dx * dx + dy * dy + dz * dz;
      } else {
         return Double.MAX_VALUE;
      }
   }

   public static boolean canAimAtPoint(Vec3 eye, Vec3 point, Entity target, double range) {
      return canAimAtPoint(eye, point, target, range, false, true);
   }

   public static boolean canAimAtPoint(Vec3 eye, Vec3 point, Entity target, double range, boolean allowThroughBlocks, boolean allowThroughEntities) {
      return canAimAtPoint(eye, point, target, range, allowThroughBlocks, allowThroughEntities, false);
   }

   public static boolean canAimAtPoint(Vec3 eye, Vec3 point, Entity target, double range, boolean allowThroughBlocks, boolean allowThroughEntities, boolean ignoreOpenFenceGates) {
      if (target == null) {
         return false;
      } else {
         double dx = point.xCoord - eye.xCoord;
         double dy = point.yCoord - eye.yCoord;
         double dz = point.zCoord - eye.zCoord;
         double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (len < 1.0E-6) {
            return false;
         } else {
            double scale = range / len;
            Vec3 end = new Vec3(eye.xCoord + dx * scale, eye.yCoord + dy * scale, eye.zCoord + dz * scale);
            float borderSize = target.getCollisionBorderSize();
            AxisAlignedBB aabb = target.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
            MovingObjectPosition entityHit = aabb.calculateIntercept(eye, end);
            if (entityHit == null) {
               return false;
            } else {
               double entityDistSq = eye.squareDistanceTo(entityHit.hitVec);
               if (!allowThroughBlocks) {
                  MovingObjectPosition blockHit = ignoreOpenFenceGates ? rayTraceBlocksIgnoringOpenFenceGates(eye, end, false, false, false) : mc.theWorld.rayTraceBlocks(eye, end, false, false, false);
                  if (blockHit != null && blockHit.typeOfHit == MovingObjectType.BLOCK) {
                     double blockDistSq = eye.squareDistanceTo(blockHit.hitVec);
                     if (blockDistSq < entityDistSq) {
                        return false;
                     }
                  }
               }

               return allowThroughEntities || !hasEntityBlockingPath(eye, end, target, entityDistSq);
            }
         }
      }
   }

   public static MovingObjectPosition rayTraceBlocksIgnoringOpenFenceGates(Vec3 start, Vec3 end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
      if (mc.theWorld != null && start != null && end != null) {
         Vec3 traceStart = start;

         for(int ignoredGates = 0; ignoredGates < 200; ++ignoredGates) {
            MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(traceStart, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
            if (!isOpenFenceGateHit(hit)) {
               return hit;
            }

            traceStart = movePastBlock(hit.hitVec, end, hit.getBlockPos());
            if (traceStart == null) {
               return null;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean isOpenFenceGateHit(MovingObjectPosition hit) {
      if (hit != null && hit.typeOfHit == MovingObjectType.BLOCK && hit.getBlockPos() != null) {
         IBlockState state = mc.theWorld.getBlockState(hit.getBlockPos());
         return state.getBlock() instanceof BlockFenceGate && (Boolean)state.getValue(BlockFenceGate.OPEN);
      } else {
         return false;
      }
   }

   private static Vec3 movePastBlock(Vec3 hitVec, Vec3 end, BlockPos blockPos) {
      if (hitVec != null && end != null && blockPos != null) {
         double dx = end.xCoord - hitVec.xCoord;
         double dy = end.yCoord - hitVec.yCoord;
         double dz = end.zCoord - hitVec.zCoord;
         double remainingDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (remainingDistance < 1.0E-7) {
            return null;
         } else {
            dx /= remainingDistance;
            dy /= remainingDistance;
            dz /= remainingDistance;
            double distanceToExit = Double.POSITIVE_INFINITY;
            if (dx > 1.0E-7) {
               distanceToExit = Math.min(distanceToExit, ((double)blockPos.getX() + (double)1.0F - hitVec.xCoord) / dx);
            } else if (dx < -1.0E-7) {
               distanceToExit = Math.min(distanceToExit, ((double)blockPos.getX() - hitVec.xCoord) / dx);
            }

            if (dy > 1.0E-7) {
               distanceToExit = Math.min(distanceToExit, ((double)blockPos.getY() + (double)1.0F - hitVec.yCoord) / dy);
            } else if (dy < -1.0E-7) {
               distanceToExit = Math.min(distanceToExit, ((double)blockPos.getY() - hitVec.yCoord) / dy);
            }

            if (dz > 1.0E-7) {
               distanceToExit = Math.min(distanceToExit, ((double)blockPos.getZ() + (double)1.0F - hitVec.zCoord) / dz);
            } else if (dz < -1.0E-7) {
               distanceToExit = Math.min(distanceToExit, ((double)blockPos.getZ() - hitVec.zCoord) / dz);
            }

            double advance = Math.max((double)0.0F, distanceToExit) + 1.0E-5;
            return !Double.isInfinite(distanceToExit) && !Double.isNaN(distanceToExit) && !(advance >= remainingDistance) ? hitVec.addVector(dx * advance, dy * advance, dz * advance) : null;
         }
      } else {
         return null;
      }
   }

   private static boolean hasEntityBlockingPath(Vec3 eye, Vec3 end, Entity target, double targetDistSq) {
      if (mc.thePlayer != null && mc.theWorld != null) {
         Vec3 delta = end.subtract(eye);
         AxisAlignedBB searchBox = mc.thePlayer.getEntityBoundingBox().addCoord(delta.xCoord, delta.yCoord, delta.zCoord).expand((double)1.0F, (double)1.0F, (double)1.0F);

         for(Entity entity : mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, searchBox, Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith))) {
            if (entity != null && entity != target && !entity.isDead) {
               float border = entity.getCollisionBorderSize();
               AxisAlignedBB bb = entity.getEntityBoundingBox().expand((double)border, (double)border, (double)border);
               MovingObjectPosition hit = bb.calculateIntercept(eye, end);
               if (bb.isVecInside(eye)) {
                  return true;
               }

               if (hit != null) {
                  double entityDistSq = eye.squareDistanceTo(hit.hitVec);
                  if (entityDistSq < targetDistSq - 1.0E-7) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isPathBlockedByEntity(Vec3 eye, Vec3 hitVec, Entity target) {
      if (eye != null && hitVec != null && target != null) {
         double targetDistSq = eye.squareDistanceTo(hitVec);
         return hasEntityBlockingPath(eye, hitVec, target, targetDistSq);
      } else {
         return false;
      }
   }

   public static boolean isEyeInsideEntityAABB(Entity entity) {
      if (entity != null && mc.thePlayer != null) {
         Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
         float borderSize = entity.getCollisionBorderSize();
         AxisAlignedBB bb = entity.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
         return bb.isVecInside(eye);
      } else {
         return false;
      }
   }

   private static boolean mainRayHitsTargetAABB(Vec3 eye, Vec3 point, Entity target, double range) {
      double dx = point.xCoord - eye.xCoord;
      double dy = point.yCoord - eye.yCoord;
      double dz = point.zCoord - eye.zCoord;
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len < 1.0E-6) {
         return false;
      } else {
         double scale = range / len;
         Vec3 end = new Vec3(eye.xCoord + dx * scale, eye.yCoord + dy * scale, eye.zCoord + dz * scale);
         float borderSize = target.getCollisionBorderSize();
         AxisAlignedBB aabb = target.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
         return aabb.calculateIntercept(eye, end) != null;
      }
   }

   public static boolean hasValidAimPoint(Entity entity, double hMult, double vMult, double range) {
      return hasValidAimPoint(entity, hMult, vMult, range, false, true);
   }

   public static boolean hasValidAimPoint(Entity entity, double hMult, double vMult, double range, boolean allowThroughBlocks, boolean allowThroughEntities) {
      return hasValidAimPoint(entity, hMult, vMult, range, allowThroughBlocks, allowThroughEntities, false);
   }

   public static boolean hasValidAimPoint(Entity entity, double hMult, double vMult, double range, boolean allowThroughBlocks, boolean allowThroughEntities, boolean ignoreOpenFenceGates) {
      if (entity != null && mc.thePlayer != null) {
         Vec3 mainPoint = getAimPoint(entity, hMult, vMult);
         if (mainPoint == null) {
            return false;
         } else {
            Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
            if (eye.squareDistanceTo(mainPoint) < 1.0E-6) {
               return true;
            } else if (!mainRayHitsTargetAABB(eye, mainPoint, entity, range)) {
               return false;
            } else if (canAimAtPoint(eye, mainPoint, entity, range, allowThroughBlocks, allowThroughEntities, ignoreOpenFenceGates)) {
               return true;
            } else {
               List<Vec3> backups = buildBackupPoints(entity, eye);
               Collections.sort(backups, Comparator.comparingDouble((px) -> {
                  double dx = px.xCoord - eye.xCoord;
                  double dy = px.yCoord - eye.yCoord;
                  double dz = px.zCoord - eye.zCoord;
                  return dx * dx + dy * dy + dz * dz;
               }));

               for(Vec3 p : backups) {
                  if (canAimAtPoint(eye, p, entity, range, allowThroughBlocks, allowThroughEntities, ignoreOpenFenceGates)) {
                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static float[] getRotationsWithBackup(Entity entity, double horizontalMultipoint, double verticalMultipoint, float baseYaw, float basePitch, double range) {
      return getRotationsWithBackup(entity, horizontalMultipoint, verticalMultipoint, baseYaw, basePitch, range, false, true);
   }

   public static float[] getRotationsWithBackup(Entity entity, double horizontalMultipoint, double verticalMultipoint, float baseYaw, float basePitch, double range, boolean allowThroughBlocks, boolean allowThroughEntities) {
      return getRotationsWithBackup(entity, horizontalMultipoint, verticalMultipoint, baseYaw, basePitch, range, allowThroughBlocks, allowThroughEntities, false);
   }

   public static float[] getRotationsWithBackup(Entity entity, double horizontalMultipoint, double verticalMultipoint, float baseYaw, float basePitch, double range, boolean allowThroughBlocks, boolean allowThroughEntities, boolean ignoreOpenFenceGates) {
      if (entity != null && mc.thePlayer != null) {
         Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
         float borderSize = entity.getCollisionBorderSize();
         AxisAlignedBB bb = entity.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
         if (bb.isVecInside(eye)) {
            double centerX = (bb.minX + bb.maxX) / (double)2.0F;
            double centerZ = (bb.minZ + bb.maxZ) / (double)2.0F;
            return getRotationsToPoint(centerX, eye.yCoord, centerZ, baseYaw, basePitch);
         } else {
            Vec3 mainPoint = getAimPoint(entity, horizontalMultipoint, verticalMultipoint);
            if (mainPoint == null) {
               return null;
            } else if (eye.squareDistanceTo(mainPoint) < 1.0E-6) {
               return null;
            } else if (!mainRayHitsTargetAABB(eye, mainPoint, entity, range)) {
               return getRotationsToPoint(mainPoint.xCoord, mainPoint.yCoord, mainPoint.zCoord, baseYaw, basePitch);
            } else if (canAimAtPoint(eye, mainPoint, entity, range, allowThroughBlocks, allowThroughEntities, ignoreOpenFenceGates)) {
               return getRotationsToPoint(mainPoint.xCoord, mainPoint.yCoord, mainPoint.zCoord, baseYaw, basePitch);
            } else {
               List<Vec3> backups = buildBackupPoints(entity, eye);
               Collections.sort(backups, Comparator.comparingDouble((px) -> {
                  double dx = px.xCoord - eye.xCoord;
                  double dy = px.yCoord - eye.yCoord;
                  double dz = px.zCoord - eye.zCoord;
                  return dx * dx + dy * dy + dz * dz;
               }));

               for(Vec3 p : backups) {
                  if (canAimAtPoint(eye, p, entity, range, allowThroughBlocks, allowThroughEntities, ignoreOpenFenceGates)) {
                     return getRotationsToPoint(p.xCoord, p.yCoord, p.zCoord, baseYaw, basePitch);
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }


   public static float[] smoothRotation(float baseYaw, float basePitch, float targetYaw, float targetPitch, int speed) {
      return smoothRotation(baseYaw, basePitch, targetYaw, targetPitch, speed, 0.0F);
   }

   public static float[] smoothRotation(float baseYaw, float basePitch, float targetYaw, float targetPitch, int speed, float randomizationPercent) {
      if (speed <= 0) {
         return new float[]{baseYaw, clampPitch(basePitch)};
      } else if (speed >= 30) {
         return new float[]{targetYaw, clampPitch(targetPitch)};
      } else {
         float deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - baseYaw);
         float deltaPitch = targetPitch - basePitch;
         float magnitude = MathHelper.sqrt_double((double)(deltaYaw * deltaYaw + deltaPitch * deltaPitch));
         if (magnitude < 0.001F) {
            return new float[]{targetYaw, clampPitch(targetPitch)};
         } else {
            float t = (float)speed / 30.0F;
            float stepSize = t * t * 180.0F;
            float range = 0.6F * (float)((double)randomizationPercent / (double)100.0F);
            float multiplier = range <= 0.001F ? 1.0F : 1.0F - range / 2.0F + (float)(Math.random() * (double)range);
            stepSize *= multiplier;
            float proximityFactor = Math.min(1.0F, magnitude / 180.0F);
            proximityFactor = (float)Math.pow((double)proximityFactor, 0.7);
            float maxSlowdown = (float)((double)randomizationPercent / (double)100.0F);
            float proximityMult = Math.max(0.8F, 1.0F - maxSlowdown * (1.0F - proximityFactor));
            stepSize *= proximityMult;
            float stepLength = Math.min(stepSize, magnitude);
            float scale = stepLength / magnitude;
            float stepYaw = deltaYaw * scale;
            float stepPitch = deltaPitch * scale;
            float yaw = baseYaw + stepYaw;
            float pitch = basePitch + stepPitch;
            return new float[]{yaw, clampPitch(pitch)};
         }
      }
   }

   public static float clampPitch(float n) {
      return MathHelper.clamp_float(n, -90.0F, 90.0F);
   }

   public static float[] fixRotation(float targetYaw, float targetPitch, float yaw, float pitch) {
      targetYaw = unwrapYaw(targetYaw, yaw);
      float n5 = targetYaw - yaw;
      float abs = Math.abs(n5);
      float n7 = targetPitch - pitch;
      float n8 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
      double n9 = (double)(n8 * n8 * n8) * 1.2;
      float n10 = (float)((double)Math.round((double)n5 / n9) * n9);
      float n11 = (float)((double)Math.round((double)n7 / n9) * n9);
      targetYaw = yaw + n10;
      targetPitch = pitch + n11;
      return new float[]{targetYaw, clampPitch(targetPitch)};
   }


   public static Vec3 getVectorForRotation(float pitch, float yaw) {
      float f = MathHelper.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
      float f1 = MathHelper.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
      float f2 = -MathHelper.cos(-pitch * ((float)Math.PI / 180F));
      float f3 = MathHelper.sin(-pitch * ((float)Math.PI / 180F));
      return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
   }

   public static MovingObjectPosition rayCastBlock(double distance, float yaw, float pitch) {
      Vec3 eyeVec = mc.thePlayer.getPositionEyes(1.0F);
      Vec3 lookVec = getVectorForRotation(pitch, yaw);
      Vec3 sumVec = eyeVec.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
      MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(eyeVec, sumVec, false, false, false);
      return mop != null && mop.typeOfHit == MovingObjectType.BLOCK ? mop : null;
   }

   public static MovingObjectPosition rayTraceCustom(double blockReachDistance, float yaw, float pitch) {
      Vec3 vec3 = mc.thePlayer.getPositionEyes(1.0F);
      Vec3 vec31 = getVectorForRotation(pitch, yaw);
      Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
      return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
   }

   public static MovingObjectPosition rayTrace(double range, float partialTicks, float[] rotations, EntityLivingBase ignoreCollision) {
      if (ignoreCollision != null) {
         MovingObjectPosition target = rayTraceIgnore(range, partialTicks, rotations, ignoreCollision);
         if (target != null) {
            return target;
         }
      }

      Entity targetEntity = null;
      double d0 = range;
      if (rotations == null) {
         rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
      }

      MovingObjectPosition hitObject = rayTraceCustom(range, rotations[0], rotations[1]);
      double distanceTo = range;
      Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);
      if (mc.playerController.extendedReach()) {
         d0 = (double)6.0F;
         distanceTo = (double)6.0F;
      }

      if (hitObject != null) {
         distanceTo = hitObject.hitVec.distanceTo(vec3);
      }

      Vec3 vec31 = getVectorForRotation(rotations[1], rotations[0]);
      Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
      Vec3 vec33 = null;
      float f = 1.0F;
      List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f, (double)f, (double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
      double d2 = distanceTo;

      for(int j = 0; j < list.size(); ++j) {
         Entity entity1 = (Entity)list.get(j);
         float f1 = entity1.getCollisionBorderSize();
         AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
         MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
         if (axisalignedbb.isVecInside(vec3)) {
            if (d2 >= (double)0.0F) {
               targetEntity = entity1;
               vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
               d2 = (double)0.0F;
            }
         } else if (movingobjectposition != null) {
            double d3 = vec3.distanceTo(movingobjectposition.hitVec);
            if (d3 < d2 || d2 == (double)0.0F) {
               if (entity1 == mc.thePlayer.ridingEntity && !canRiderInteract(mc.thePlayer)) {
                  if (d2 == (double)0.0F) {
                     targetEntity = entity1;
                     vec33 = movingobjectposition.hitVec;
                  }
               } else {
                  targetEntity = entity1;
                  vec33 = movingobjectposition.hitVec;
                  d2 = d3;
               }
            }
         }
      }

      if (targetEntity != null && d2 < distanceTo) {
         return new MovingObjectPosition(targetEntity, vec33);
      } else {
         return null;
      }
   }

   public static MovingObjectPosition rayTraceIgnore(double range, float partialTicks, float[] rotations, EntityLivingBase ignoreCollision) {
      MovingObjectPosition blockHit = rayTraceCustom(range, rotations[0], rotations[1]);
      Vec3 start = mc.thePlayer.getPositionEyes(partialTicks);
      double blockDistance = range;
      if (blockHit != null) {
         blockDistance = blockHit.hitVec.distanceTo(start);
      }

      if (ignoreCollision != null) {
         if (rotations == null) {
            rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
         }

         Vec3 lookVec = getVectorForRotation(rotations[1], rotations[0]);
         Vec3 end = start.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);
         float f1 = ignoreCollision.getCollisionBorderSize();
         AxisAlignedBB aabb = ignoreCollision.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
         MovingObjectPosition ignoreMOP = aabb.calculateIntercept(start, end);
         if (aabb.isVecInside(start)) {
            return new MovingObjectPosition(ignoreCollision, start);
         }

         if (ignoreMOP != null) {
            double ignoreDist = start.distanceTo(ignoreMOP.hitVec);
            if (ignoreDist < blockDistance) {
               return new MovingObjectPosition(ignoreCollision, ignoreMOP.hitVec);
            }
         }
      }

      return blockHit != null ? blockHit : null;
   }

}
