package dev.rice.module.impl.pearl;
import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.Item;import net.minecraft.init.Items;import net.minecraft.util.Vec3;
public final class TrajectoryAim {
    private enum PhysicsModel {THROWABLE,FISH_HOOK}
    private static final class TrajectoryProps {
        final double gravity,drag,initialVelocity;final boolean requiresInitialTickCorrection;
        TrajectoryProps(PhysicsModel m,double gravity,double drag,double water,double width,double height,double radius,double velocity,boolean ignore,boolean correction){this.gravity=gravity;this.drag=drag;initialVelocity=velocity;requiresInitialTickCorrection=correction;}
    }
    public static float[] getThrowRotations(EntityPlayer entityPlayer, Item item, Vec3 vec3, boolean bl) {
        float[] fArray;
        float f;
        TrajectoryProps trajectoryProps = TrajectoryAim.getThrowableProperties(item);
        if (entityPlayer == null || trajectoryProps == null || vec3 == null) {
            return null;
        }
        double d = vec3.xCoord - entityPlayer.posX;
        double d2 = vec3.zCoord - entityPlayer.posZ;
        float f2 = f = d * d + d2 * d2 < 1.0E-8 ? entityPlayer.rotationYaw : (float)Math.toDegrees(Math.atan2(d2, d)) - 90.0f;
        float f3 = 90.0f;
        double d3 = Double.MAX_VALUE;
        for (int i = -2; i <= 2; ++i) {
            float f4 = f + (float)i * 4.0f;
            float f5 = TrajectoryAim.solveThrowPitch(entityPlayer, trajectoryProps, f4, vec3, bl);
            double d4 = TrajectoryAim.closestThrowApproach(entityPlayer, trajectoryProps, f4, f5, vec3, bl);
            if (!(d4 < d3)) continue;
            d3 = d4;
            f2 = f4;
            f3 = f5;
        }
        if (d3 <= 0.36) {
            float[] fArray2 = new float[2];
            fArray2[0] = TrajectoryAim.normalizeYaw(f2);
            fArray = fArray2;
            fArray2[1] = Math.max(0.0f, Math.min(90.0f, f3));
        } else {
            fArray = null;
        }
        return fArray;
    }

    private static TrajectoryProps getThrowableProperties(Item item) {
        if (item == Items.ender_pearl || item == Items.snowball || item == Items.egg) {
            return new TrajectoryProps(PhysicsModel.THROWABLE, 0.03, 0.99, 0.8, 0.25, 0.25, 0.25, 1.5, true, false);
        }
        if (item == Items.experience_bottle) {
            return new TrajectoryProps(PhysicsModel.THROWABLE, 0.07, 0.99, 0.8, 0.25, 0.25, 0.25, 0.7, true, false);
        }
        if (item == Items.potionitem) {
            return new TrajectoryProps(PhysicsModel.THROWABLE, 0.05, 0.99, 0.8, 0.25, 0.25, 0.25, 0.5, true, false);
        }
        if (item == Items.fishing_rod) {
            return new TrajectoryProps(PhysicsModel.FISH_HOOK, 0.04, 0.92, 0.92, 0.25, 0.25, 0.25, 1.5, false, false);
        }
        return null;
    }

    private static float solveThrowPitch(EntityPlayer entityPlayer, TrajectoryProps trajectoryProps, float f, Vec3 vec3, boolean bl) {
        float f2 = 90.0f;
        double d = Double.MAX_VALUE;
        for (float f3 = 0.0f; f3 <= 90.0f; f3 += 1.0f) {
            double d2 = TrajectoryAim.closestThrowApproach(entityPlayer, trajectoryProps, f, f3, vec3, bl);
            if (!(d2 < d)) continue;
            d = d2;
            f2 = f3;
        }
        double d3 = d;
        for (float f4 = f2 - 1.5f; f4 <= f2 + 1.5f; f4 += 0.05f) {
            double d4;
            if (f4 < 0.0f || f4 > 90.0f || !((d4 = TrajectoryAim.closestThrowApproach(entityPlayer, trajectoryProps, f, f4, vec3, bl)) < d3)) continue;
            d3 = d4;
            f2 = f4;
        }
        return f2;
    }

    private static double closestThrowApproach(EntityPlayer entityPlayer, TrajectoryProps trajectoryProps, float f, float f2, Vec3 vec3, boolean bl) {
        double d = Math.toRadians(f);
        double d2 = Math.toRadians(f2);
        double d3 = entityPlayer.posX - Math.cos(d) * 0.16;
        double d4 = entityPlayer.posY + (double)entityPlayer.getEyeHeight() - (double)0.1f;
        double d5 = entityPlayer.posZ - Math.sin(d) * 0.16;
        double d6 = -Math.sin(d) * Math.cos(d2) * trajectoryProps.initialVelocity;
        double d7 = -Math.sin(d2) * trajectoryProps.initialVelocity;
        double d8 = Math.cos(d) * Math.cos(d2) * trajectoryProps.initialVelocity;
        if (bl) {
            d6 += entityPlayer.motionX;
            d7 += entityPlayer.onGround ? 0.0 : entityPlayer.motionY;
            d8 += entityPlayer.motionZ;
        }
        if (trajectoryProps.requiresInitialTickCorrection) {
            d6 *= trajectoryProps.drag;
            d7 *= trajectoryProps.drag;
            d8 *= trajectoryProps.drag;
            d7 -= trajectoryProps.gravity;
        }
        double d9 = Double.MAX_VALUE;
        for (int i = 0; i < 160; ++i) {
            double d10 = d3 + d6;
            double d11 = d4 + d7;
            double d12 = d5 + d8;
            d9 = Math.min(d9, TrajectoryAim.segmentDistanceSq(d3, d4, d5, d10, d11, d12, vec3));
            d3 = d10;
            d4 = d11;
            d5 = d12;
            d6 *= trajectoryProps.drag;
            d7 *= trajectoryProps.drag;
            d8 *= trajectoryProps.drag;
            if ((d7 -= trajectoryProps.gravity) < 0.0 && d4 < vec3.yCoord - 16.0) break;
        }
        return d9;
    }

    private static double segmentDistanceSq(double d, double d2, double d3, double d4, double d5, double d6, Vec3 vec3) {
        double d7 = d4 - d;
        double d8 = d5 - d2;
        double d9 = d6 - d3;
        double d10 = d7 * d7 + d8 * d8 + d9 * d9;
        double d11 = 0.0;
        if (d10 > 1.0E-9) {
            d11 = ((vec3.xCoord - d) * d7 + (vec3.yCoord - d2) * d8 + (vec3.zCoord - d3) * d9) / d10;
            d11 = Math.max(0.0, Math.min(1.0, d11));
        }
        double d12 = d + d7 * d11 - vec3.xCoord;
        double d13 = d2 + d8 * d11 - vec3.yCoord;
        double d14 = d3 + d9 * d11 - vec3.zCoord;
        return d12 * d12 + d13 * d13 + d14 * d14;
    }

    private static float normalizeYaw(float f) {
        if ((f %= 360.0f) > 180.0f) {
            f -= 360.0f;
        }
        if (f <= -180.0f) {
            f += 360.0f;
        }
        return f;
    }

}
