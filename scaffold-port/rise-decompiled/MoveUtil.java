/*
 * Decompiled with CFR 0.152.
 */
package com.alan.clients.util.player;

import com.alan.clients.Client;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.other.MoveEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.util.value.ConstantManager;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.util.vector.Vector3d;
import java.util.Arrays;
import javax.vecmath.Vector2d;
import lombok.Generated;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.world.World;

public final class MoveUtil
implements InstanceAccess {
    private static float aOp;
    private static float aOq;
    public static final double aOr = 0.221;
    public static final double aOs = 0.66;
    public static final double aOt = (double)1.3f;
    public static final double aOu = (double)0.3f;
    public static final double aOv = 2.5;
    public static final double aOw = 0.4751131221719457;
    public static final double aOx = (double)0.42f;
    public static final double aOy = (double)159.9f;
    public static final double aOz = 1.0E-5;
    public static final double aOA = 0.0626;
    public static final double aOB = 0.015625;
    public static final double aOC = (double)0.98f;
    public static final double aOD = (double)0.8f;
    public static final double aOE = 0.5;
    public static final double aOF = 0.5203620003898759;
    public static final double[] MOD_DEPTH_STRIDER;
    public static final double aOH = -0.09800000190735147;
    public static final double aOI = -0.0784000015258789;

    public static boolean isMoving() {
        return MoveUtil.aEg.thePlayer.moveForward != 0.0f || MoveUtil.aEg.thePlayer.moveStrafing != 0.0f;
    }

    public static boolean enoughMovementForSprinting() {
        return Math.abs(MoveUtil.aEg.thePlayer.moveForward) >= 0.8f || Math.abs(MoveUtil.aEg.thePlayer.moveStrafing) >= 0.8f;
    }

    public static boolean canSprint(boolean var0) {
        return var0 ? !(!(MoveUtil.aEg.thePlayer.moveForward >= 0.8f) || MoveUtil.aEg.thePlayer.isCollidedHorizontally || MoveUtil.aEg.thePlayer.getFoodStats().getFoodLevel() <= 6 && !MoveUtil.aEg.thePlayer.capabilities.allowFlying || MoveUtil.aEg.thePlayer.isPotionActive(Potion.blindness) || MoveUtil.aEg.thePlayer.isUsingItem() || MoveUtil.aEg.thePlayer.isSneaking()) : MoveUtil.enoughMovementForSprinting();
    }

    public static double movementDelta() {
        return Math.hypot(MoveUtil.aEg.thePlayer.posX - MoveUtil.aEg.thePlayer.prevPosX, MoveUtil.aEg.thePlayer.posZ - MoveUtil.aEg.thePlayer.prevPosZ);
    }

    public static double speedPotionAmp(double var0) {
        return MoveUtil.aEg.thePlayer.isPotionActive(Potion.moveSpeed) ? (double)(MoveUtil.aEg.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * var0 : 0.0;
    }

    public static double jumpMotion() {
        return MoveUtil.jumpBoostMotion(0.42f);
    }

    public static double jumpBoostMotion(double var0) {
        return MoveUtil.aEg.thePlayer.isPotionActive(Potion.jump) ? var0 + (double)((float)(MoveUtil.aEg.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f) : var0;
    }

    public static Vector2d getMotion(double var0) {
        MovementInput movementinput = MoveUtil.aEg.thePlayer.movementInput;
        double d0 = movementinput.moveForward;
        double d1 = movementinput.moveStrafe;
        double d2 = MoveUtil.aEg.thePlayer.pp;
        if (d0 == 0.0 && d1 == 0.0) {
            return new Vector2d(0.0, 0.0);
        }
        if (d1 > 0.0) {
            d1 = 1.0;
        } else if (d1 < 0.0) {
            d1 = -1.0;
        }
        if (d0 != 0.0) {
            if (d1 > 0.0) {
                d2 += d0 > 0.0 ? -45.0 : 45.0;
            } else if (d1 < 0.0) {
                d2 += d0 > 0.0 ? 45.0 : -45.0;
            }
            d1 = 0.0;
            if (d0 > 0.0) {
                d0 = 1.0;
            } else if (d0 < 0.0) {
                d0 = -1.0;
            }
        }
        double cos = Math.cos(Math.toRadians(d2 + (double)ConstantManager.J));
        double sin = Math.sin(Math.toRadians(d2 + (double)ConstantManager.J));
        return new Vector2d(d0 * var0 * cos + d1 * var0 * sin, d0 * var0 * sin - d1 * var0 * cos);
    }

    public static int depthStriderLevel() {
        return EnchantmentHelper.getDepthStriderModifier((Entity)MoveUtil.aEg.thePlayer);
    }

    public static float fallDistanceForDamage() {
        float f = 3.0f;
        if (MoveUtil.aEg.thePlayer.isPotionActive(Potion.jump)) {
            int i = MoveUtil.aEg.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            f += (float)(i + 1);
        }
        return f;
    }

    public static double roundToGround(double var0) {
        return (double)Math.round(var0 / 0.015625) * 0.015625;
    }

    public static double predictedMotion(double var0) {
        return (var0 - 0.08) * (double)0.98f;
    }

    public static void forward(double var0) {
        double d0 = MoveUtil.direction();
        MoveUtil.aEg.thePlayer.motionX = -Math.sin(d0) * var0;
        MoveUtil.aEg.thePlayer.motionZ = Math.cos(d0) * var0;
    }

    public static double predictedMotion(double var0, int var2) {
        if (var2 == 0) {
            return var0;
        }
        double d0 = var0;
        for (int i = 0; i < var2; ++i) {
            d0 = (d0 - 0.08) * (double)0.98f;
        }
        return d0;
    }

    public static float getMoveX(float var0) {
        return (float)(-Math.sin(Math.toRadians(var0)));
    }

    public static float getMoveZ(float var0) {
        return (float)Math.cos(Math.toRadians(var0));
    }

    public static float getMoveX() {
        return MoveUtil.getMoveX(MoveUtil.aEg.thePlayer.pp);
    }

    public static float getMoveZ() {
        return MoveUtil.getMoveZ(MoveUtil.aEg.thePlayer.pp);
    }

    public static double getLastVelocityDelta() {
        return Math.hypot(MoveUtil.aEg.thePlayer.crI, MoveUtil.aEg.thePlayer.crI);
    }

    public static double vd() {
        double d0 = 0.2873;
        if (MoveUtil.aEg.thePlayer.isPotionActive(Potion.moveSpeed)) {
            d0 *= 1.0 + 0.2 * (double)(MoveUtil.aEg.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return d0;
    }

    public static double getAllowedHorizontalDistance() {
        double d0;
        boolean flag = false;
        if (MoveUtil.aEg.thePlayer.isInWeb) {
            d0 = 0.105;
        } else if (PlayerUtil.vj()) {
            d0 = 0.11500000208616258;
            int i = MoveUtil.depthStriderLevel();
            if (i > 0) {
                d0 *= MOD_DEPTH_STRIDER[i];
                flag = true;
            }
        } else if (MoveUtil.aEg.thePlayer.isSneaking()) {
            d0 = 0.0663000026345253;
        } else {
            d0 = 0.221;
            flag = true;
        }
        if (flag) {
            if (MoveUtil.canSprint(false)) {
                d0 *= (double)1.3f;
            }
            Scaffold scaffold = (Scaffold)Client.a.g().c(Scaffold.class);
            if (!(!MoveUtil.aEg.thePlayer.isPotionActive(Potion.moveSpeed) || MoveUtil.aEg.thePlayer.getActivePotionEffect((Potion)Potion.moveSpeed).duration <= 0 || scaffold.isEnabled() && ((Boolean)scaffold.agt.wo()).booleanValue())) {
                d0 *= 1.0 + 0.2 * (double)(MoveUtil.aEg.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
            }
            if (MoveUtil.aEg.thePlayer.isPotionActive(Potion.moveSlowdown)) {
                d0 = 0.29;
            }
        }
        return d0;
    }

    public static void jumpRandom(double var0) {
        MoveUtil.aEg.thePlayer.motionY = var0 + Math.random() / 500.0;
    }

    public static void strafe() {
        MoveUtil.a(MoveUtil.speed(), (Entity)MoveUtil.aEg.thePlayer);
    }

    public static void u(Entity entity) {
        MoveUtil.a(MoveUtil.speed(), entity);
    }

    public static void strafe(double var0) {
        MoveUtil.a(var0, (Entity)MoveUtil.aEg.thePlayer);
    }

    public static void a(double var0, Entity entity) {
        if (MoveUtil.isMoving()) {
            double d0 = MoveUtil.direction();
            entity.motionX = (double)(-MathHelper.sin((float)((float)d0))) * var0;
            entity.motionZ = (double)MathHelper.cos((float)((float)d0)) * var0;
        }
    }

    public static void a(double var0, float var2) {
        if (MoveUtil.isMoving()) {
            var2 = (float)Math.toRadians(var2);
            MoveUtil.aEg.thePlayer.motionX = (double)(-MathHelper.sin((float)var2)) * var0;
            MoveUtil.aEg.thePlayer.motionZ = (double)MathHelper.cos((float)var2) * var0;
        }
    }

    public static void stop() {
        MoveUtil.aEg.thePlayer.motionX = 0.0;
        MoveUtil.aEg.thePlayer.motionZ = 0.0;
    }

    public static double direction() {
        float f = MoveUtil.aEg.thePlayer.pp;
        if (MoveUtil.aEg.thePlayer.moveForward < 0.0f) {
            f += 180.0f;
        }
        float f1 = 1.0f;
        if (MoveUtil.aEg.thePlayer.moveForward < 0.0f) {
            f1 = -0.5f;
        } else if (MoveUtil.aEg.thePlayer.moveForward > 0.0f) {
            f1 = 0.5f;
        }
        if (MoveUtil.aEg.thePlayer.moveStrafing > 0.0f) {
            f -= 90.0f * f1;
        }
        if (MoveUtil.aEg.thePlayer.moveStrafing < 0.0f) {
            f += 90.0f * f1;
        }
        return Math.toRadians(f);
    }

    public static float moveYaw(double var0, double var2) {
        return var0 == 0.0 && var2 == 0.0 ? MoveUtil.aEg.thePlayer.pl : (float)(Math.toDegrees(Math.atan2(var2, var0)) - 90.0);
    }

    public static double m(MoveInputEvent event) {
        float f = MoveUtil.aEg.thePlayer.pp;
        if (event.getForward() < 0.0f) {
            f += 180.0f;
        }
        float f1 = 1.0f;
        if (event.getForward() < 0.0f) {
            f1 = -0.5f;
        } else if (event.getForward() > 0.0f) {
            f1 = 0.5f;
        }
        if (event.getStrafe() > 0.0f) {
            f -= 70.0f * f1;
        }
        if (event.getStrafe() < 0.0f) {
            f += 70.0f * f1;
        }
        return Math.toRadians(f);
    }

    public static double g(float var0, float var1) {
        float f = MoveUtil.aEg.thePlayer.pp;
        if (var0 < 0.0f) {
            f += 180.0f;
        }
        float f1 = 1.0f;
        if (var0 < 0.0f) {
            f1 = -0.5f;
        } else if (var0 > 0.0f) {
            f1 = 0.5f;
        }
        if (var1 > 0.0f) {
            f -= 70.0f * f1;
        }
        if (var1 < 0.0f) {
            f += 70.0f * f1;
        }
        return Math.toRadians(f);
    }

    public static double wrappedDirection() {
        float f = MoveUtil.aEg.thePlayer.pp;
        if (MoveUtil.aEg.thePlayer.moveForward < 0.0f && MoveUtil.aEg.thePlayer.moveStrafing == 0.0f) {
            f += 180.0f;
        }
        if (MoveUtil.aEg.thePlayer.moveStrafing > 0.0f) {
            f -= 90.0f;
        }
        if (MoveUtil.aEg.thePlayer.moveStrafing < 0.0f) {
            f += 90.0f;
        }
        return Math.toRadians(f);
    }

    public static double direction(float var0, double var1, double var3) {
        if (var1 < 0.0) {
            var0 += 180.0f;
        }
        float f = 1.0f;
        if (var1 < 0.0) {
            f = -0.5f;
        } else if (var1 > 0.0) {
            f = 0.5f;
        }
        if (var3 > 0.0) {
            var0 -= 90.0f * f;
        }
        if (var3 < 0.0) {
            var0 += 90.0f * f;
        }
        return Math.toRadians(var0);
    }

    public static double speed() {
        return Math.hypot(MoveUtil.aEg.thePlayer.motionX, MoveUtil.aEg.thePlayer.motionZ);
    }

    public static void setMoveEvent(MoveEvent moveEvent, double var1) {
        MoveUtil.setMoveEvent(moveEvent, var1, MoveUtil.aEg.thePlayer.pp, MoveUtil.aEg.thePlayer.movementInput.moveStrafe, MoveUtil.aEg.thePlayer.movementInput.moveForward);
    }

    public static void setMoveEvent(MoveEvent moveEvent, double var1, float var3, double var4, double var6) {
        double d0 = var6;
        double d1 = var4;
        float f = var3;
        if (d0 != 0.0) {
            if (d1 > 0.0) {
                f += d0 > 0.0 ? -45.0f : 45.0f;
            } else if (d1 < 0.0) {
                f += d0 > 0.0 ? 45.0f : -45.0f;
            }
            d1 = 0.0;
            if (d0 > 0.0) {
                d0 = 1.0;
            } else if (d0 < 0.0) {
                d0 = -1.0;
            }
        }
        double cos = Math.cos(Math.toRadians(f + ConstantManager.J));
        double sin = Math.sin(Math.toRadians(f + ConstantManager.J));
        moveEvent.setPosX(d0 * var1 * cos + d1 * var1 * sin);
        moveEvent.setPosZ(d0 * var1 * sin - d1 * var1 * cos);
    }

    public static void fixMovement(MoveInputEvent moveInputEvent, float var1) {
        float f = moveInputEvent.getForward();
        float strafe = moveInputEvent.getStrafe();
        double d0 = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(MoveUtil.direction(MoveUtil.aEg.thePlayer.pl, f, strafe)));
        if (f != 0.0f || strafe != 0.0f) {
            float f2 = 0.0f;
            float f3 = 0.0f;
            float f4 = Float.MAX_VALUE;
            for (float f5 = -1.0f; f5 <= 1.0f; f5 += 1.0f) {
                for (float f6 = -1.0f; f6 <= 1.0f; f6 += 1.0f) {
                    double d1;
                    double d2;
                    if (f6 == 0.0f && f5 == 0.0f || !((d2 = MathUtil.n((double)d0, (double)(d1 = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(MoveUtil.direction(var1, f5, f6)))))) < (double)f4)) continue;
                    f4 = (float)d2;
                    f2 = f5;
                    f3 = f6;
                }
            }
            if (f3 == -1.0f) {
                // empty if block
            }
            aOp = f3;
            aOq = f2;
            moveInputEvent.setForward(f2);
            moveInputEvent.setStrafe(f3);
        }
    }

    public static double getMCFriction() {
        float f = 0.91f;
        if (MoveUtil.aEg.thePlayer.onGround) {
            f = MoveUtil.aEg.theWorld.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)MoveUtil.aEg.thePlayer.posX), (int)(MathHelper.floor_double((double)MoveUtil.aEg.thePlayer.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)MoveUtil.aEg.thePlayer.posZ))).getBlock().slipperiness * 0.91f;
        }
        return f;
    }

    public static double[] moveFlying(float var0, float var1, boolean var2, float var3, boolean var4) {
        float f5;
        float f = 0.02f;
        float f1 = MoveUtil.aEg.thePlayer.getAIMoveSpeed();
        if (var2) {
            f = f1 / 2.0f * 0.9999998f;
        }
        if (var4) {
            f = (float)((double)f + (double)(var2 ? f1 / 2.0f : 0.02f) * 0.3);
        }
        if ((f5 = var0 * var0 + var1 * var1) >= 1.0E-4f) {
            if ((f5 = MathHelper.sqrt_float((float)f5)) < 1.0f) {
                f5 = 1.0f;
            }
            f5 = f / f5;
            float sin = MathHelper.sin((float)(var3 * (float)ConstantManager.aHb / 180.0f));
            float cos = MathHelper.cos((float)(var3 * (float)ConstantManager.aHb / 180.0f));
            double d0 = (var0 *= f5) * cos - (var1 *= f5) * sin;
            double d1 = var1 * cos + var0 * sin;
            return new double[]{d0, d1};
        }
        return null;
    }

    public static com.alan.clients.util.vector.Vector2d moveFlyingVec(float var0, float var1, boolean var2, float var3, boolean var4) {
        double[] adouble = MoveUtil.moveFlying(var0, var1, var2, var3, var4);
        return adouble == null ? null : new com.alan.clients.util.vector.Vector2d(adouble[0], adouble[1]);
    }

    public static Double moveFlyingSpeed(float var0, float var1, boolean var2, float var3, boolean var4) {
        double[] adouble = MoveUtil.moveFlying(var0, var1, var2, var3, var4);
        return adouble == null ? null : Double.valueOf(Math.hypot(adouble[0], adouble[1]));
    }

    public static Double moveFlyingSpeed(boolean var0) {
        double[] adouble = MoveUtil.moveFlying(0.98f, 0.98f, MoveUtil.aEg.thePlayer.onGround, 180.0f, var0);
        return adouble == null ? null : Double.valueOf(Math.hypot(adouble[0], adouble[1]));
    }

    public static void partialStrafeMax(double var0) {
        MoveUtil.b(var0, (Entity)MoveUtil.aEg.thePlayer);
    }

    public static void b(double var0, Entity entity) {
        double d0 = entity.motionX;
        double d1 = entity.motionZ;
        MoveUtil.u(entity);
        entity.motionX = d0 + Math.max(-var0, Math.min(var0, entity.motionX - d0));
        entity.motionZ = d1 + Math.max(-var0, Math.min(var0, entity.motionZ - d1));
    }

    public static void partialStrafePercent(double var0) {
        var0 /= 100.0;
        var0 = Math.min(1.0, Math.max(0.0, var0));
        double d0 = MoveUtil.aEg.thePlayer.motionX;
        double d1 = MoveUtil.aEg.thePlayer.motionZ;
        MoveUtil.strafe();
        MoveUtil.aEg.thePlayer.motionX = d0 + (MoveUtil.aEg.thePlayer.motionX - d0) * var0;
        MoveUtil.aEg.thePlayer.motionZ = d1 + (MoveUtil.aEg.thePlayer.motionZ - d1) * var0;
    }

    public static double moveMaxFlying(boolean var0) {
        float f = 0.02f;
        float f1 = MoveUtil.aEg.thePlayer.getAIMoveSpeed() / 2.0f;
        float f2 = 0.98f;
        float f3 = 0.98f;
        float f4 = 180.0f;
        if (var0) {
            f = f1 * 0.9999998f;
        }
        f = (float)((double)f + (double)(var0 ? f1 : 0.02f) * 0.3);
        float f8 = f2 * f2 + f3 * f3;
        if (f8 >= 1.0E-4f) {
            if ((f8 = (float)Math.sqrt(f8)) < 1.0f) {
                f8 = 1.0f;
            }
            f8 = f / f8;
            float sin = MathHelper.sin((float)(f4 * (float)ConstantManager.aHb / 180.0f));
            float cos = MathHelper.cos((float)(f4 * (float)ConstantManager.aHb / 180.0f));
            double d0 = (f2 *= f8) * cos - (f3 *= f8) * sin;
            double d1 = f3 * cos + f2 * sin;
            return Math.hypot(d0, d1);
        }
        return 0.0;
    }

    public static float simulationStrafeAngle(float var0, float var1) {
        float degrees = (float)Math.toDegrees(MoveUtil.direction());
        var0 = Math.abs(var0 - degrees) <= var1 ? degrees : (var0 > degrees ? (var0 -= var1) : (var0 += var1));
        float f = var0;
        MoveUtil.a(MoveUtil.speed(), f);
        return f;
    }

    public static float simulationStrafe(float var0) {
        double d0 = 0.02599999835384377;
        double d1 = 0.91f;
        double d2 = MoveUtil.aEg.thePlayer.He * d1;
        double d3 = MoveUtil.aEg.thePlayer.Hg * d1;
        float f = var0;
        float degrees = (float)Math.toDegrees(MoveUtil.direction());
        for (int i = 0; i <= 360; ++i) {
            MoveUtil.a(MoveUtil.speed(), var0);
            double d4 = Math.abs(MoveUtil.aEg.thePlayer.motionX);
            double d5 = Math.abs(MoveUtil.aEg.thePlayer.motionZ);
            double d6 = d2 - d0;
            double d7 = d3 - d0;
            if (var0 == degrees || d4 < d6 || d5 < d7) break;
            f = var0;
            if (Math.abs(var0 - degrees) <= 1.0f) {
                var0 = degrees;
                continue;
            }
            if (var0 > degrees) {
                var0 -= 1.0f;
                continue;
            }
            var0 += 1.0f;
        }
        MoveUtil.a(MoveUtil.speed(), f);
        return f;
    }

    public static void preventDiagonalSpeed() {
        boolean flag;
        KeyBinding[] akeybinding = new KeyBinding[]{MoveUtil.aEg.gameSettings.keyBindForward, MoveUtil.aEg.gameSettings.keyBindRight, MoveUtil.aEg.gameSettings.keyBindBack, MoveUtil.aEg.gameSettings.keyBindLeft};
        int[] aint = new int[]{0};
        Arrays.stream(akeybinding).forEach(var1x -> {
            aint[0] = aint[0] + (var1x.isKeyDown() ? 1 : 0);
        });
        boolean bl = flag = aint[0] == 1;
        if (!flag) {
            double d0 = 0.0026000750109401644;
            double d1 = 5.199896488849598E-4;
            double d2 = MoveUtil.aEg.thePlayer.onGround ? d0 : d1;
            MoveUtil.moveFlying(-d2);
        }
    }

    public static void useDiagonalSpeed() {
        boolean flag;
        KeyBinding[] akeybinding = new KeyBinding[]{MoveUtil.aEg.gameSettings.keyBindForward, MoveUtil.aEg.gameSettings.keyBindRight, MoveUtil.aEg.gameSettings.keyBindBack, MoveUtil.aEg.gameSettings.keyBindLeft};
        int[] aint = new int[]{0};
        Arrays.stream(akeybinding).forEach(var1x -> {
            aint[0] = aint[0] + (var1x.isKeyDown() ? 1 : 0);
        });
        boolean bl = flag = aint[0] == 1;
        if (flag) {
            double d0 = 0.0026000750109401644;
            double d1 = 5.199896488849598E-4;
            double d2 = MoveUtil.aEg.thePlayer.onGround ? d0 : d1;
            MoveUtil.moveFlying(d2);
        }
    }

    public static void c(double var0, Entity entity) {
        if (MoveUtil.isMoving()) {
            double d0 = MoveUtil.getMCFriction();
            double d1 = d0 > 1.0E-6 ? var0 / d0 : var0;
            double d2 = MoveUtil.direction();
            entity.motionX = (double)(-MathHelper.sin((float)((float)d2))) * d1;
            entity.motionZ = (double)MathHelper.cos((float)((float)d2)) * d1;
        }
    }

    public static void strafeNoFriction(double var0) {
        MoveUtil.c(var0, (Entity)MoveUtil.aEg.thePlayer);
    }

    public static void moveFlying(double var0) {
        if (MoveUtil.isMoving()) {
            double d0 = MoveUtil.direction();
            MoveUtil.aEg.thePlayer.motionX += (double)(-MathHelper.sin((float)((float)d0))) * var0;
            MoveUtil.aEg.thePlayer.motionZ += (double)MathHelper.cos((float)((float)d0)) * var0;
        }
    }

    public static void moveFlying2(double var0, float var2) {
        MoveUtil.aEg.thePlayer.motionX += (double)(-MathHelper.sin((float)((float)Math.toRadians(var2)))) * var0;
        MoveUtil.aEg.thePlayer.motionZ += (double)MathHelper.cos((float)((float)Math.toRadians(var2))) * var0;
    }

    public static Vector3d a(Entity entity, Vector2f vec2, int var2, boolean var3) {
        double dx = entity.posX - entity.lastTickPosX;
        double dz = entity.posZ - entity.lastTickPosZ;
        return MoveUtil.a(new Vector3d(entity.posX, entity.posY, entity.posZ), new Vector3d(dx, 0.0, dz), entity.pl, vec2, var2, var3);
    }

    public static Vector3d a(Vector3d var0, Vector3d var1, float var2, Vector2f vec2) {
        return MoveUtil.a(var0, var1, var2, vec2, 1, vec2.x != 0.0f || vec2.y != 0.0f);
    }

    public static Vector3d a(Vector3d var0, Vector3d var1, float var2, Vector2f vec2, int var4, boolean var5) {
        for (int i = 0; i <= var4; ++i) {
            var1.z *= 0.546000063419342;
            var1.x *= 0.546000063419342;
            double[] adouble = MoveUtil.moveFlying(vec2.x, vec2.y, true, var2, var5);
            if (adouble == null) {
                return var0;
            }
            var1.x += adouble[0];
            var1.y = 0.0;
            var1.z += adouble[1];
            var0 = var0.e(var1);
        }
        return var0;
    }

    public static Vector3d ve() {
        EntityPlayerSP entityplayersp = new EntityPlayerSP(aEg, (World)MoveUtil.aEg.theWorld, new NetHandlerPlayClient(aEg, MoveUtil.aEg.currentScreen, new NetworkManager(MoveUtil.aEg.getNetHandler().getNetworkManager().direction), MoveUtil.aEg.thePlayer.getGameProfile()), MoveUtil.aEg.thePlayer.getStatFileWriter());
        MoveUtil.aEg.theWorld.addEntityToWorld(-99999, (Entity)entityplayersp);
        entityplayersp.motionX = MoveUtil.aEg.thePlayer.motionX;
        entityplayersp.motionY = MoveUtil.aEg.thePlayer.motionY;
        entityplayersp.motionZ = MoveUtil.aEg.thePlayer.motionZ;
        entityplayersp.setPosition(MoveUtil.aEg.thePlayer.posX, MoveUtil.aEg.thePlayer.posY, MoveUtil.aEg.thePlayer.posZ);
        entityplayersp.pl = entityplayersp.pp = MoveUtil.aEg.thePlayer.pp;
        entityplayersp.onGround = MoveUtil.aEg.thePlayer.onGround;
        entityplayersp.setSprinting(MoveUtil.aEg.thePlayer.isSprinting());
        entityplayersp.bjQ = MoveUtil.aEg.thePlayer.bjQ;
        entityplayersp.movementInput = new MovementInputFromOptions(MoveUtil.aEg.gameSettings);
        entityplayersp.movementInput.updatePlayerMoveState();
        entityplayersp.moveForward = entityplayersp.movementInput.moveForward;
        entityplayersp.moveStrafing = entityplayersp.movementInput.moveStrafe;
        entityplayersp.onUpdate();
        MoveUtil.aEg.theWorld.removeEntityFromWorld(-99999);
        if (MoveUtil.aEg.gameSettings.keyBindJump.isKeyDown() && MoveUtil.aEg.thePlayer.onGround && MoveUtil.aEg.thePlayer.jumpTicks <= 1) {
            entityplayersp.setPosition(entityplayersp.posX, entityplayersp.posY + MoveUtil.jumpMotion(), entityplayersp.posZ);
        }
        return new Vector3d(entityplayersp.posX, entityplayersp.posY, entityplayersp.posZ);
    }

    @Generated
    private MoveUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        MOD_DEPTH_STRIDER = new double[]{1.0, 1.4304347400741908, 1.7347825295420372, 1.9217390955733897};
    }
}
