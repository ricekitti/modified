package myau;

import myau.mixin.AccessorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RotationUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();

    public static float nearestEquivalentAngle(float targetAngle, float referenceAngle) {
        return referenceAngle + MathHelper.wrapAngleTo180_float((float)(targetAngle - referenceAngle));
    }

    public static float clampAngleChange(float change, float maximumChange) {
        if (change > (maximumChange = MathHelper.clamp_float((float)maximumChange, (float)0.0f, (float)180.0f))) {
            return maximumChange;
        }
        if (change < -maximumChange) {
            change = -maximumChange;
        }
        return change;
    }

    public static float scaleAngleChange(float change, float factor) {
        return change * (0.5f + 0.5f * (1.0f - MathHelper.clamp_float((float)factor, (float)0.0f, (float)1.0f)));
    }

    public static float snapToMouseSensitivity(float targetAngle, float referenceAngle) {
        float change = MathHelper.wrapAngleTo180_float((float)(targetAngle - referenceAngle));
        float sensitivity = RotationUtils.minecraft.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float increment = sensitivity * sensitivity * sensitivity * 8.0f;
        double roundedChange = (double)((float)Math.round((double)change / ((double)increment * 0.15)) * increment) * 0.15;
        return (float)((double)referenceAngle + roundedChange);
    }

    public static float[] aimAtBounds(AxisAlignedBB bounds) {
        Vec3 eyePosition = RotationUtils.minecraft.thePlayer.getPositionEyes(1.0f);
        Vec3 center = RotationUtils.boundsCenter(bounds);
        Vec3 closestPoint = RotationUtils.closestPoint(eyePosition, bounds);
        double deltaX = MathUtils.lerpDouble(center.xCoord, closestPoint.xCoord, 0.875) - eyePosition.xCoord;
        double deltaY = closestPoint.yCoord - eyePosition.yCoord;
        double deltaZ = MathUtils.lerpDouble(center.zCoord, closestPoint.zCoord, 0.875) - eyePosition.zCoord;
        float[] rotation = RotationUtils.rotationsFromDelta(deltaX, deltaY, deltaZ);
        rotation[1] = rotation[1] + 10.0f * MathHelper.clamp_float((float)((float)((eyePosition.yCoord - center.yCoord) / (center.yCoord - bounds.minY))), (float)-0.5f, (float)0.5f);
        return rotation;
    }

    public static float[] rotationsFromDelta(double deltaX, double deltaY, double deltaZ) {
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }

    public static Vec3 boundsCenter(AxisAlignedBB bounds) {
        return new Vec3((bounds.minX + bounds.maxX) / 2.0, (bounds.minY + bounds.maxY) / 2.0, (bounds.minZ + bounds.maxZ) / 2.0);
    }

    public static Vec3 closestPoint(Vec3 position, AxisAlignedBB bounds) {
        double[] coordinates = new double[]{position.xCoord, position.yCoord, position.zCoord};
        double[] minimum = new double[]{bounds.minX, bounds.minY, bounds.minZ};
        double[] maximum = new double[]{bounds.maxX, bounds.maxY, bounds.maxZ};
        for (int axis = 0; axis < 3; ++axis) {
            if (coordinates[axis] > maximum[axis]) {
                coordinates[axis] = maximum[axis];
                continue;
            }
            if (!(coordinates[axis] < minimum[axis])) continue;
            coordinates[axis] = minimum[axis];
        }
        return new Vec3(coordinates[0], coordinates[1], coordinates[2]);
    }

    public static double distanceToEntity(Entity entity) {
        float collisionBorder = entity.getCollisionBorderSize();
        AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);
        return RotationUtils.distanceToBounds(bounds);
    }

    public static double distanceToEntityFrom(Entity entity, Vec3 position) {
        float collisionBorder = entity.getCollisionBorderSize();
        AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);
        return RotationUtils.distanceToBoundsFrom(bounds, position);
    }

    public static double distanceToBounds(AxisAlignedBB bounds) {
        return RotationUtils.distanceToBoundsFrom(bounds, RotationUtils.minecraft.thePlayer.getPositionEyes(1.0f));
    }

    public static double distanceToBoundsFrom(AxisAlignedBB bounds, Vec3 position) {
        if (bounds.isVecInside(position)) {
            return 0.0;
        }
        Vec3 closestPoint = RotationUtils.closestPoint(position, bounds);
        double deltaX = closestPoint.xCoord - position.xCoord;
        double deltaY = closestPoint.yCoord - position.yCoord;
        double deltaZ = closestPoint.zCoord - position.zCoord;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static float entityAimError(Entity entity) {
        Vec3 eyePosition = RotationUtils.minecraft.thePlayer.getPositionEyes(1.0f);
        float collisionBorder = entity.getCollisionBorderSize();
        AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);
        return RotationUtils.boundsAimError(bounds, eyePosition);
    }

    public static float boundsAimError(AxisAlignedBB bounds, Vec3 position) {
        if (bounds.isVecInside(position)) {
            return 0.0f;
        }
        double traceDistance = 30.0 + bounds.maxX - bounds.minX + bounds.maxZ - bounds.minZ + bounds.maxY - bounds.minY;
        MovingObjectPosition hit = RotationUtils.intersectBoundsFrom(bounds, position, RotationUtils.minecraft.thePlayer.rotationYaw, RotationUtils.minecraft.thePlayer.rotationPitch, traceDistance);
        if (hit != null) {
            return 0.0f;
        }
        float[] rotation = RotationUtils.aimAtBounds(bounds);
        float yawError = Math.abs(MathHelper.wrapAngleTo180_float((float)(rotation[0] - RotationUtils.minecraft.thePlayer.rotationYaw)));
        float pitchError = Math.abs(rotation[1] - RotationUtils.minecraft.thePlayer.rotationPitch);
        return Math.max(yawError * 2.0f, pitchError * 2.0f);
    }

    public static float horizontalYawError(double startX, double startZ, double endX, double endZ) {
        float yaw = (float)(Math.atan2(endZ - startZ, endX - startX) * 180.0 / Math.PI) - 90.0f;
        return MathHelper.wrapAngleTo180_float((float)(yaw - RotationUtils.minecraft.thePlayer.rotationYaw));
    }

    public static MovingObjectPosition traceBlocks(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 eyePosition = RotationUtils.minecraft.thePlayer.getPositionEyes(partialTicks);
        return RotationUtils.traceBlocksFrom(eyePosition, yaw, pitch, distance);
    }

    public static MovingObjectPosition traceBlocksFrom(Vec3 position, float yaw, float pitch, double distance) {
        Vec3 direction = ((AccessorEntity)RotationUtils.minecraft.thePlayer).callGetVectorForRotation(pitch, yaw);
        Vec3 end = position.addVector(direction.xCoord * distance, direction.yCoord * distance, direction.zCoord * distance);
        return RotationUtils.minecraft.theWorld.rayTraceBlocks(position, end);
    }

    public static MovingObjectPosition traceToEntity(Entity entity) {
        Vec3 eyePosition = RotationUtils.minecraft.thePlayer.getPositionEyes(1.0f);
        float collisionBorder = entity.getCollisionBorderSize();
        Vec3 closestPoint = RotationUtils.closestPoint(eyePosition, entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder));
        return RotationUtils.minecraft.theWorld.rayTraceBlocks(eyePosition, closestPoint);
    }

    public static MovingObjectPosition intersectBounds(AxisAlignedBB bounds, float yaw, float pitch, double distance) {
        Vec3 eyePosition = RotationUtils.minecraft.thePlayer.getPositionEyes(1.0f);
        return RotationUtils.intersectBoundsFrom(bounds, eyePosition, yaw, pitch, distance);
    }

    public static MovingObjectPosition intersectBoundsFrom(AxisAlignedBB bounds, Vec3 position, float yaw, float pitch, double distance) {
        Vec3 direction = ((AccessorEntity)RotationUtils.minecraft.thePlayer).callGetVectorForRotation(pitch, yaw);
        Vec3 end = position.addVector(direction.xCoord * distance, direction.yCoord * distance, direction.zCoord * distance);
        return bounds.calculateIntercept(position, end);
    }

    public static boolean isBoundsRayObstructed(AxisAlignedBB bounds, Vec3 position, float yaw, float pitch, double distance) {
        if (bounds.isVecInside(position)) {
            return false;
        }
        MovingObjectPosition boxHit = RotationUtils.intersectBoundsFrom(bounds, position, yaw, pitch, distance);
        if (boxHit == null) {
            return true;
        }
        MovingObjectPosition blockHit = RotationUtils.traceBlocksFrom(position, yaw, pitch, distance);
        if (blockHit == null) {
            return false;
        }
        return position.distanceTo(boxHit.hitVec) > position.distanceTo(blockHit.hitVec);
    }
}

