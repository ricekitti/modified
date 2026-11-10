package dev.rice.mayu;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

public final class RotationUtils {
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

    public static float snapToMouseSensitivity(float targetAngle, float referenceAngle) {
        float change = MathHelper.wrapAngleTo180_float((float)(targetAngle - referenceAngle));
        float sensitivity = RotationUtils.minecraft.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float increment = sensitivity * sensitivity * sensitivity * 8.0f;
        double roundedChange = (double)((float)Math.round((double)change / ((double)increment * 0.15)) * increment) * 0.15;
        return (float)((double)referenceAngle + roundedChange);
    }

    public static float[] rotationsFromDelta(double deltaX, double deltaY, double deltaZ) {
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }

    public static MovingObjectPosition traceBlocks(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 eyePosition = RotationUtils.minecraft.thePlayer.getPositionEyes(partialTicks);
        return RotationUtils.traceBlocksFrom(eyePosition, yaw, pitch, distance);
    }

    public static MovingObjectPosition traceBlocksFrom(Vec3 position, float yaw, float pitch, double distance) {
        Vec3 direction = Compatibility.vectorForRotation(pitch, yaw);
        Vec3 end = position.addVector(direction.xCoord * distance, direction.yCoord * distance, direction.zCoord * distance);
        return RotationUtils.minecraft.theWorld.rayTraceBlocks(position, end);
    }
}
