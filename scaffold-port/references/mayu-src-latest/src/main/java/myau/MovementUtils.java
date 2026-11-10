package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;

public final class MovementUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();

    private MovementUtils() {
    }

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
        float strafeYaw;
        TargetStrafeModule targetStrafe = (TargetStrafeModule)MyauClient.moduleManager.modules.get(TargetStrafeModule.class);
        if (targetStrafe != null && targetStrafe.isEnabled() && !Float.isNaN(strafeYaw = targetStrafe.getStrafeYaw())) {
            return strafeYaw;
        }
        if (forward < 0.0f) {
            yaw += 180.0f;
        }
        if (strafe != 0.0f) {
            float forwardFactor = forward == 0.0f ? 1.0f : 0.5f * Math.signum(forward);
            yaw += -90.0f * forwardFactor * Math.signum(strafe);
        }
        return MathHelper.wrapAngleTo180_float((float)yaw);
    }

    public static float motionYaw() {
        if (MovementUtils.horizontalSpeed() == 0.0) {
            return MathHelper.wrapAngleTo180_float((float)MovementUtils.minecraft.thePlayer.rotationYaw);
        }
        return MathHelper.wrapAngleTo180_float((float)((float)Math.toDegrees(Math.atan2(MovementUtils.minecraft.thePlayer.motionZ, MovementUtils.minecraft.thePlayer.motionX)) - 90.0f));
    }

    public static double baseMovementSpeed() {
        double speed = 0.28015;
        if (MovementUtils.speedPotionTicks() > 0) {
            speed *= 1.0 + 0.15 * (double)MovementUtils.speedPotionLevel();
        }
        return speed;
    }

    public static double boostedSpeedForPotionLevel(int potionLevel) {
        double speed = 0.452;
        if (potionLevel == 1) {
            return speed * 1.1;
        }
        if (potionLevel >= 2) {
            speed *= 1.2;
        }
        return speed;
    }

    public static double boostedMovementSpeed() {
        int potionLevel = MovementUtils.speedPotionTicks() > 0 ? MovementUtils.speedPotionLevel() : 0;
        return MovementUtils.boostedSpeedForPotionLevel(potionLevel);
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

    public static void addHorizontalMotion(double speed, float yaw) {
        MovementUtils.minecraft.thePlayer.motionX += -Math.sin(Math.toRadians(yaw)) * speed;
        MovementUtils.minecraft.thePlayer.motionZ += Math.cos(Math.toRadians(yaw)) * speed;
    }

    public static int speedPotionLevel() {
        if (!MovementUtils.minecraft.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 0;
        }
        return MovementUtils.minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
    }

    public static int speedPotionTicks() {
        if (!MovementUtils.minecraft.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 0;
        }
        return MovementUtils.minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration();
    }

    public static float groundAcceleration() {
        float friction = MovementUtils.minecraft.thePlayer.worldObj.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)MovementUtils.minecraft.thePlayer.posX), (int)(MathHelper.floor_double((double)MovementUtils.minecraft.thePlayer.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)MovementUtils.minecraft.thePlayer.posZ))).getBlock().slipperiness * 0.91f;
        return MovementUtils.minecraft.thePlayer.getAIMoveSpeed() * (0.16277136f / (friction * friction * friction));
    }

    public static double[] inputAcceleration() {
        float forward;
        float strafe = (float)MovementUtils.strafeInput() * 0.98f;
        float magnitudeSquared = strafe * strafe + (forward = (float)MovementUtils.forwardInput() * 0.98f) * forward;
        if (magnitudeSquared < 1.0E-4f) {
            return new double[]{0.0, 0.0};
        }
        float magnitude = MathHelper.sqrt_float((float)magnitudeSquared);
        if (magnitude < 1.0f) {
            magnitude = 1.0f;
        }
        float acceleration = MovementUtils.groundAcceleration() / magnitude;
        float sinYaw = MathHelper.sin((float)(MovementUtils.minecraft.thePlayer.rotationYaw * (float)Math.PI / 180.0f));
        float cosYaw = MathHelper.cos((float)(MovementUtils.minecraft.thePlayer.rotationYaw * (float)Math.PI / 180.0f));
        return new double[]{(strafe *= acceleration) * cosYaw - (forward *= acceleration) * sinYaw, forward * cosYaw + strafe * sinYaw};
    }

    public static void correctMovementForYaw(float yaw) {
        float relativeYaw = MathHelper.wrapAngleTo180_float((float)(MovementUtils.movementYawForInput(MovementUtils.minecraft.thePlayer.rotationYaw, MovementUtils.forwardInput(), MovementUtils.strafeInput()) - yaw + 22.5f));
        int direction = (int)(relativeYaw + 180.0f) / 45 % 8;
        switch (direction) {
            case 0: {
                MovementUtils.setMovementInput(-1.0f, 0.0f);
                break;
            }
            case 1: {
                MovementUtils.setMovementInput(-1.0f, 1.0f);
                break;
            }
            case 2: {
                MovementUtils.setMovementInput(0.0f, 1.0f);
                break;
            }
            case 3: {
                MovementUtils.setMovementInput(1.0f, 1.0f);
                break;
            }
            case 4: {
                MovementUtils.setMovementInput(1.0f, 0.0f);
                break;
            }
            case 5: {
                MovementUtils.setMovementInput(1.0f, -1.0f);
                break;
            }
            case 6: {
                MovementUtils.setMovementInput(0.0f, -1.0f);
                break;
            }
            case 7: {
                MovementUtils.setMovementInput(-1.0f, -1.0f);
                break;
            }
        }
        if (MovementUtils.minecraft.thePlayer.movementInput.sneak) {
            MovementUtils.minecraft.thePlayer.movementInput.moveForward *= 0.3f;
            MovementUtils.minecraft.thePlayer.movementInput.moveStrafe *= 0.3f;
        }
    }

    private static void setMovementInput(float forward, float strafe) {
        MovementInput input = MovementUtils.minecraft.thePlayer.movementInput;
        input.moveForward = forward;
        input.moveStrafe = strafe;
    }
}

