package dev.rice.mayu;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

public final class MovementUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    public static boolean hasMovementInput() {
        return MovementUtils.minecraft.gameSettings.keyBindForward.isKeyDown() != MovementUtils.minecraft.gameSettings.keyBindBack.isKeyDown() || MovementUtils.minecraft.gameSettings.keyBindLeft.isKeyDown() != MovementUtils.minecraft.gameSettings.keyBindRight.isKeyDown();
    }

    public static int forwardInput() {
        int input = 0;
        if (MovementUtils.minecraft.gameSettings.keyBindForward.isKeyDown()) {
            ++input;
        }
        if (MovementUtils.minecraft.gameSettings.keyBindBack.isKeyDown()) {
            --input;
        }
        return input;
    }

    public static int strafeInput() {
        int input = 0;
        if (MovementUtils.minecraft.gameSettings.keyBindLeft.isKeyDown()) {
            ++input;
        }
        if (MovementUtils.minecraft.gameSettings.keyBindRight.isKeyDown()) {
            --input;
        }
        return input;
    }

    public static float movementYaw() {
        float baseYaw = RotationState.isActive() ? RotationState.movementYaw() : MovementUtils.minecraft.thePlayer.rotationYaw;
        return MovementUtils.movementYawForInput(baseYaw, MovementUtils.minecraft.thePlayer.movementInput.moveForward, MovementUtils.minecraft.thePlayer.movementInput.moveStrafe);
    }

    public static float movementYawForInput(float yaw, float forward, float strafe) {
        float strafeYaw = Compatibility.targetStrafeYaw();
        if (!Float.isNaN(strafeYaw)) return strafeYaw;
        if (forward == 0.0f && strafe == 0.0f) return MathHelper.wrapAngleTo180_float(yaw);
        return MathHelper.wrapAngleTo180_float(yaw - (float)Math.toDegrees(Math.atan2(strafe, forward)));
    }

    public static float motionYaw() {
        if (MovementUtils.horizontalSpeed() == 0.0) {
            return MathHelper.wrapAngleTo180_float((float)MovementUtils.minecraft.thePlayer.rotationYaw);
        }
        return MathHelper.wrapAngleTo180_float((float)((float)Math.toDegrees(Math.atan2(MovementUtils.minecraft.thePlayer.motionZ, MovementUtils.minecraft.thePlayer.motionX)) - 90.0f));
    }

    public static double horizontalSpeed() {
        return MovementUtils.horizontalMagnitude(MovementUtils.minecraft.thePlayer.motionX, MovementUtils.minecraft.thePlayer.motionZ);
    }

    public static double horizontalMagnitude(double motionX, double motionZ) {
        return Math.hypot(motionX, motionZ);
    }

    public static void setSpeedAlongMotion(double speed) {
        MovementUtils.setHorizontalMotion(speed, MovementUtils.motionYaw());
    }

    public static void setHorizontalMotion(double speed, float yaw) {
        MovementUtils.minecraft.thePlayer.motionX = -Math.sin(Math.toRadians(yaw)) * speed;
        MovementUtils.minecraft.thePlayer.motionZ = Math.cos(Math.toRadians(yaw)) * speed;
    }

    public static int speedPotionLevel() {
        if (!MovementUtils.minecraft.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 0;
        }
        return MovementUtils.minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
    }

    public static void correctMovementForYaw(float yaw) {
        MovementInput input = minecraft.thePlayer.movementInput;
        float viewAngle = minecraft.thePlayer.rotationYaw * (float)Math.PI / 180.0f;
        float targetAngle = yaw * (float)Math.PI / 180.0f;
        double viewSin = MathHelper.sin(viewAngle), viewCos = MathHelper.cos(viewAngle);
        double targetSin = MathHelper.sin(targetAngle), targetCos = MathHelper.cos(targetAngle);
        double worldX = input.moveStrafe * viewCos - input.moveForward * viewSin;
        double worldZ = input.moveForward * viewCos + input.moveStrafe * viewSin;
        double magnitude = targetSin * targetSin + targetCos * targetCos;
        float forward = (float)((worldZ * targetCos - worldX * targetSin) / magnitude);
        float strafe = (float)((worldX * targetCos + worldZ * targetSin) / magnitude);
        setMovementInput(Math.abs(forward) < 1.0E-6f ? 0.0f : forward,
                         Math.abs(strafe) < 1.0E-6f ? 0.0f : strafe);
    }

    private static void setMovementInput(float forward, float strafe) {
        MovementInput input = MovementUtils.minecraft.thePlayer.movementInput;
        input.moveForward = forward;
        input.moveStrafe = strafe;
    }
}
