/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicate
 *  com.google.common.base.Predicates
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EntitySelectors
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  valen.utility.RotationUtils$PLAYER_OFFSETS
 */
package valen.utility;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import valen.event.PreMotionEvent;
import valen.helper.RotationHelper;
import valen.lunar.VanillaHooks;
import valen.module.impl.client.Settings;
import valen.utility.BlockUtils;
import valen.utility.IMinecraftInstance;
import valen.utility.RotationUtils;
import valen.utility.Utils;

public class RotationUtils
implements IMinecraftInstance {
    public static float renderPitch;
    public static float prevRenderPitch;
    public static float renderYaw;
    public static float prevRenderYaw;
    public static float[] serverRotations;
    public static Float[] fakeRotations;
    public static boolean setFakeRotations;
    private static final double BACKUP_FACE_INSET = 0.05;
    private static final int BACKUP_TARGET_TOTAL = 30;
    private static final double AIM_POINT_INSET = 0.05;
    private static Entity stickyEntity;
    private static double stickyFracX;
    private static double stickyFracY;
    private static double stickyFracZ;
    private static boolean hasStickyPoint;
    private static long stickyPointTimeNs;
    private static final long MULTIPOINT_REROLL_NS = 500000000L;
    private static final double MULTIPOINT_TIE_EPSILON = 0.15;
    private static final float FAR_THRESHOLD = 180.0f;
    private static final float AIM_EASE_MIN = 0.06f;
    private static final float AIM_FLICK_REF = 120.0f;
    private static final float AIM_MIN_STEP = 2.0f;
    private static final float AIM_FULL_SNAP = 0.985f;
    private static final float[] SETTLE_STEPS;

    public static void setFakeRotations(float f, float f2) {
        fakeRotations = new Float[]{Float.valueOf(f), Float.valueOf(f2)};
        setFakeRotations = true;
    }

    public static void setRenderYaw(float f) {
        RotationUtils.mc.thePlayer.rotationYawHead = f;
        if (Settings.rotateBody.isToggled() && Settings.fullBody.isToggled()) {
            RotationUtils.mc.thePlayer.prevRenderYawOffset = prevRenderYaw;
            RotationUtils.mc.thePlayer.renderYawOffset = f;
        }
    }

    public static float[] getRotations(BlockPos blockPos, float f, float f2) {
        float[] fArray = RotationUtils.getRotations(blockPos);
        return RotationUtils.fixRotation(fArray[0], fArray[1], f, f2);
    }

    public static float[] getRotationsToBlock(BlockPos blockPos, EnumFacing enumFacing, float f, float f2) {
        float[] fArray = RotationUtils.getRotationsToBlock(blockPos, enumFacing);
        return RotationUtils.fixRotation(fArray[0], fArray[1], f, f2);
    }

    public static float[] getRotations(BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.45 - RotationUtils.mc.thePlayer.posX;
        double d2 = (double)blockPos.getY() + 0.45 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        double d3 = (double)blockPos.getZ() + 0.45 - RotationUtils.mc.thePlayer.posZ;
        float f = (float)(Math.atan2(d3, d) * 57.29577951308232) - 90.0f;
        float f2 = MathHelper.wrapAngleTo180_float((float)(f - RotationUtils.mc.thePlayer.rotationYaw));
        float f3 = RotationUtils.mc.thePlayer.rotationYaw + f2;
        double d4 = MathHelper.sqrt_double((double)(d * d + d3 * d3));
        float f4 = (float)(-(Math.atan2(d2, d4) * 57.29577951308232));
        float f5 = MathHelper.wrapAngleTo180_float((float)(f4 - RotationUtils.mc.thePlayer.rotationPitch));
        float f6 = RotationUtils.mc.thePlayer.rotationPitch + f5;
        f6 = RotationUtils.clampPitch(f6);
        return new float[]{f3, f6};
    }

    public static float[] getRotations(double d, double d2, double d3) {
        double d4 = d + 1.0 - RotationUtils.mc.thePlayer.posX;
        double d5 = d2 + 1.0 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        double d6 = d3 + 1.0 - RotationUtils.mc.thePlayer.posZ;
        float f = (float)(Math.atan2(d6, d4) * 57.29577951308232) - 90.0f;
        float f2 = MathHelper.wrapAngleTo180_float((float)(f - RotationUtils.mc.thePlayer.rotationYaw));
        float f3 = RotationUtils.mc.thePlayer.rotationYaw + f2;
        double d7 = MathHelper.sqrt_double((double)(d4 * d4 + d6 * d6));
        float f4 = (float)(-(Math.atan2(d5, d7) * 57.29577951308232));
        float f5 = MathHelper.wrapAngleTo180_float((float)(f4 - RotationUtils.mc.thePlayer.rotationPitch));
        float f6 = RotationUtils.mc.thePlayer.rotationPitch + f5;
        f6 = RotationUtils.clampPitch(f6);
        return new float[]{f3, f6};
    }

    public static float[] getRotations(Vec3 vec3) {
        double d = vec3.xCoord + 1.0 - RotationUtils.mc.thePlayer.posX;
        double d2 = vec3.yCoord + 1.0 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        double d3 = vec3.zCoord + 1.0 - RotationUtils.mc.thePlayer.posZ;
        float f = (float)(Math.atan2(d3, d) * 57.29577951308232) - 90.0f;
        float f2 = MathHelper.wrapAngleTo180_float((float)(f - RotationUtils.mc.thePlayer.rotationYaw));
        float f3 = RotationUtils.mc.thePlayer.rotationYaw + f2;
        double d4 = MathHelper.sqrt_double((double)(d * d + d3 * d3));
        float f4 = (float)(-(Math.atan2(d2, d4) * 57.29577951308232));
        float f5 = MathHelper.wrapAngleTo180_float((float)(f4 - RotationUtils.mc.thePlayer.rotationPitch));
        float f6 = RotationUtils.mc.thePlayer.rotationPitch + f5;
        f6 = RotationUtils.clampPitch(f6);
        return new float[]{f3, f6};
    }

    public static float[] getRotations(Entity entity, float f, float f2) {
        float[] fArray = RotationUtils.getRotations(entity);
        if (fArray == null) {
            return null;
        }
        return RotationUtils.fixRotation(fArray[0], fArray[1], f, f2);
    }

    public static float[] getRotationsToBlock(BlockPos blockPos, EnumFacing enumFacing) {
        double d = (double)blockPos.getX() + 0.45 - RotationUtils.mc.thePlayer.posX;
        double d2 = (double)blockPos.getY() + 0.45 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        double d3 = (double)blockPos.getZ() + 0.45 - RotationUtils.mc.thePlayer.posZ;
        if (enumFacing != null) {
            d += (double)enumFacing.getDirectionVec().getX() * 0.5;
            d2 += (double)enumFacing.getDirectionVec().getY() * 0.5;
            d3 += (double)enumFacing.getDirectionVec().getZ() * 0.5;
        }
        double d4 = MathHelper.sqrt_double((double)(d * d + d3 * d3));
        float f = (float)(Math.atan2(d3, d) * (double)57.29578f) - 90.0f;
        float f2 = (float)(-(Math.atan2(d2, d4) * (double)57.29578f));
        return new float[]{RotationUtils.mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(f - RotationUtils.mc.thePlayer.rotationYaw)), RotationUtils.clampPitch(RotationUtils.mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(f2 - RotationUtils.mc.thePlayer.rotationPitch)))};
    }

    public static double distanceFromYaw(Entity entity, boolean bl) {
        return Math.abs(MathHelper.wrapAngleTo180_double((double)(RotationUtils.i(entity.posX, entity.posZ) - (bl && PreMotionEvent.setRenderYaw() ? renderYaw : RotationUtils.mc.thePlayer.rotationYaw))));
    }

    public static float i(double d, double d2) {
        return (float)(Math.atan2(d - RotationUtils.mc.thePlayer.posX, d2 - RotationUtils.mc.thePlayer.posZ) * (double)57.29578f * -1.0);
    }

    public static boolean isPossibleToHit(Entity entity, double d, float[] fArray) {
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f = fArray[0];
        float f2 = fArray[1];
        float f3 = -f * ((float)Math.PI / 180) - (float)Math.PI;
        float f4 = -f2 * ((float)Math.PI / 180);
        float f5 = MathHelper.cos((float)f3);
        float f6 = MathHelper.sin((float)f3);
        float f7 = -MathHelper.cos((float)f4);
        float f8 = MathHelper.sin((float)f4);
        Vec3 vec32 = new Vec3((double)(f6 * f7), (double)f8, (double)(f5 * f7));
        double d2 = vec32.xCoord * d;
        double d3 = vec32.yCoord * d;
        double d4 = vec32.zCoord * d;
        Vec3 vec33 = vec3.addVector(d2, d3, d4);
        Entity entity2 = mc.getRenderViewEntity();
        AxisAlignedBB axisAlignedBB = entity2.getEntityBoundingBox().addCoord(d2, d3, d4).expand(1.0, 1.0, 1.0);
        List list = RotationUtils.mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity2, axisAlignedBB);
        for (Entity entity3 : list) {
            if (entity3 != entity || !entity3.canBeCollidedWith()) continue;
            float f9 = entity3.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB2 = entity3.getEntityBoundingBox().expand((double)f9, (double)f9, (double)f9);
            MovingObjectPosition movingObjectPosition = axisAlignedBB2.calculateIntercept(vec3, vec33);
            return movingObjectPosition != null;
        }
        return false;
    }

    public static boolean inRange(BlockPos blockPos, double d) {
        Vec3 vec3;
        MovingObjectPosition movingObjectPosition;
        AxisAlignedBB axisAlignedBB;
        float[] fArray = RotationUtils.getRotations(blockPos);
        Vec3 vec32 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f = -fArray[0] * ((float)Math.PI / 180);
        float f2 = -fArray[1] * ((float)Math.PI / 180);
        float f3 = MathHelper.cos((float)(f - (float)Math.PI));
        float f4 = MathHelper.sin((float)(f - (float)Math.PI));
        float f5 = -MathHelper.cos((float)f2);
        Vec3 vec33 = new Vec3((double)(f4 * f5), (double)MathHelper.sin((float)f2), (double)(f3 * f5));
        Block block = BlockUtils.getBlock(blockPos);
        IBlockState iBlockState = BlockUtils.getBlockState(blockPos);
        return block != null && iBlockState != null && (axisAlignedBB = block.getCollisionBoundingBox((World)RotationUtils.mc.theWorld, blockPos, iBlockState)) != null && (movingObjectPosition = axisAlignedBB.calculateIntercept(vec32, vec3 = vec32.addVector(vec33.xCoord * d, vec33.yCoord * d, vec33.zCoord * d))) != null;
    }

    public static float[] getRotations(Entity entity) {
        return RotationUtils.getRotations(entity, PLAYER_OFFSETS.NONE);
    }

    public static float[] getRotations(Entity entity, PLAYER_OFFSETS pLAYER_OFFSETS) {
        double d;
        if (entity == null) {
            return null;
        }
        double d2 = entity.posX - RotationUtils.mc.thePlayer.posX;
        double d3 = entity.posZ - RotationUtils.mc.thePlayer.posZ;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
            d = entityLivingBase.posY + pLAYER_OFFSETS.getHeightOffset((Entity)entityLivingBase) * 0.9 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        } else {
            d = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        }
        return new float[]{RotationUtils.mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)((float)(Math.atan2(d3, d2) * (double)57.29578f) - 90.0f - RotationUtils.mc.thePlayer.rotationYaw)), RotationUtils.clampPitch(RotationUtils.mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)((float)(-(Math.atan2(d, MathHelper.sqrt_double((double)(d2 * d2 + d3 * d3))) * (double)57.29578f)) - RotationUtils.mc.thePlayer.rotationPitch)) + 3.0f)};
    }

    public static float[] getRotationsToPoint(double d, double d2, double d3) {
        return RotationUtils.getRotationsToPoint(d, d2, d3, RotationUtils.mc.thePlayer.rotationYaw, RotationUtils.mc.thePlayer.rotationPitch);
    }

    public static float[] getRotationsToPoint(double d, double d2, double d3, float f, float f2) {
        float f3;
        float f4;
        float f5;
        double d4 = d - RotationUtils.mc.thePlayer.posX;
        double d5 = d3 - RotationUtils.mc.thePlayer.posZ;
        double d6 = d2 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        double d7 = d4 * d4 + d5 * d5;
        if (d7 < 1.0E-12) {
            f5 = f;
            f4 = (float)(-(Math.atan2(d6, 0.0) * (double)57.29578f));
        } else {
            f3 = (float)(Math.atan2(d5, d4) * (double)57.29578f) - 90.0f;
            f5 = f + MathHelper.wrapAngleTo180_float((float)(f3 - f));
            double d8 = MathHelper.sqrt_double((double)d7);
            f4 = (float)(-(Math.atan2(d6, d8) * (double)57.29578f));
        }
        f3 = f2 + MathHelper.wrapAngleTo180_float((float)(f4 - f2));
        return new float[]{f5, RotationUtils.clampPitch(f3)};
    }

    public static float[] getRotations(Entity entity, double d, double d2, float f, float f2) {
        Vec3 vec3 = RotationUtils.getAimPoint(entity, d, d2);
        if (vec3 == null) {
            return null;
        }
        return RotationUtils.getRotationsToPoint(vec3.xCoord, vec3.yCoord, vec3.zCoord, f, f2);
    }

    public static Vec3 getAimPoint(Entity entity, double d, double d2) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return null;
        }
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        AxisAlignedBB axisAlignedBB2 = RotationUtils.getAimPointBounds(entity);
        double d3 = (axisAlignedBB2.minX + axisAlignedBB2.maxX) / 2.0;
        double d4 = entity instanceof EntityLivingBase ? entity.posY + (double)((EntityLivingBase)entity).getEyeHeight() : (axisAlignedBB2.minY + axisAlignedBB2.maxY) / 2.0;
        double d5 = (axisAlignedBB2.minZ + axisAlignedBB2.maxZ) / 2.0;
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        if (axisAlignedBB.isVecInside(vec3)) {
            return new Vec3(d3, vec3.yCoord, d5);
        }
        Vec3 vec32 = RotationUtils.closestPointOnAabb(axisAlignedBB2, vec3);
        double d6 = Math.max(0.0, Math.min(1.0, d / 100.0));
        double d7 = Math.max(0.0, Math.min(1.0, d2 / 100.0));
        double d8 = d3 + (vec32.xCoord - d3) * d6;
        double d9 = d4 + (vec32.yCoord - d4) * d7;
        double d10 = d5 + (vec32.zCoord - d5) * d6;
        return new Vec3(d8, d9, d10);
    }

    public static Vec3 closestPointOnAabb(AxisAlignedBB axisAlignedBB, Vec3 vec3) {
        double d = Math.max(axisAlignedBB.minX, Math.min(axisAlignedBB.maxX, vec3.xCoord));
        double d2 = Math.max(axisAlignedBB.minY, Math.min(axisAlignedBB.maxY, vec3.yCoord));
        double d3 = Math.max(axisAlignedBB.minZ, Math.min(axisAlignedBB.maxZ, vec3.zCoord));
        return new Vec3(d, d2, d3);
    }

    private static AxisAlignedBB getAimPointBounds(Entity entity) {
        return RotationUtils.insetAimBounds(entity.getEntityBoundingBox());
    }

    private static AxisAlignedBB insetAimBounds(AxisAlignedBB axisAlignedBB) {
        double d = Math.min(0.05, (axisAlignedBB.maxX - axisAlignedBB.minX) * 0.25);
        double d2 = Math.min(0.05, (axisAlignedBB.maxY - axisAlignedBB.minY) * 0.25);
        double d3 = Math.min(0.05, (axisAlignedBB.maxZ - axisAlignedBB.minZ) * 0.25);
        return new AxisAlignedBB(axisAlignedBB.minX + d, axisAlignedBB.minY + d2, axisAlignedBB.minZ + d3, axisAlignedBB.maxX - d, axisAlignedBB.maxY - d2, axisAlignedBB.maxZ - d3);
    }

    public static List<Vec3> buildBackupPoints(Entity entity, Vec3 vec3) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return new ArrayList<Vec3>();
        }
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        return RotationUtils.buildBackupPoints(axisAlignedBB, vec3);
    }

    public static List<Vec3> buildBackupPoints(AxisAlignedBB axisAlignedBB, Vec3 vec3) {
        double d;
        if (RotationUtils.mc.thePlayer == null) {
            return new ArrayList<Vec3>();
        }
        double d2 = axisAlignedBB.maxX - axisAlignedBB.minX;
        double d3 = axisAlignedBB.maxY - axisAlignedBB.minY;
        double d4 = axisAlignedBB.maxZ - axisAlignedBB.minZ;
        boolean bl = vec3.xCoord > axisAlignedBB.maxX;
        boolean bl2 = vec3.xCoord < axisAlignedBB.minX;
        boolean bl3 = vec3.yCoord > axisAlignedBB.maxY;
        boolean bl4 = vec3.yCoord < axisAlignedBB.minY;
        boolean bl5 = vec3.zCoord > axisAlignedBB.maxZ;
        boolean bl6 = vec3.zCoord < axisAlignedBB.minZ;
        int n = (bl || bl2 ? 1 : 0) + (bl3 || bl4 ? 1 : 0) + (bl5 || bl6 ? 1 : 0);
        if (n == 0) {
            return new ArrayList<Vec3>();
        }
        int n2 = 30 / n;
        ArrayList<Vec3> arrayList = new ArrayList<Vec3>(36);
        if (bl || bl2) {
            d = bl ? axisAlignedBB.maxX - 0.05 : axisAlignedBB.minX + 0.05;
            RotationUtils.addFaceGrid(arrayList, 0, d, axisAlignedBB.minY + 0.05, axisAlignedBB.maxY - 0.05, axisAlignedBB.minZ + 0.05, axisAlignedBB.maxZ - 0.05, n2, d3, d4);
        }
        if (bl3 || bl4) {
            d = bl3 ? axisAlignedBB.maxY - 0.05 : axisAlignedBB.minY + 0.05;
            RotationUtils.addFaceGrid(arrayList, 1, d, axisAlignedBB.minX + 0.05, axisAlignedBB.maxX - 0.05, axisAlignedBB.minZ + 0.05, axisAlignedBB.maxZ - 0.05, n2, d2, d4);
        }
        if (bl5 || bl6) {
            d = bl5 ? axisAlignedBB.maxZ - 0.05 : axisAlignedBB.minZ + 0.05;
            RotationUtils.addFaceGrid(arrayList, 2, d, axisAlignedBB.minX + 0.05, axisAlignedBB.maxX - 0.05, axisAlignedBB.minY + 0.05, axisAlignedBB.maxY - 0.05, n2, d2, d3);
        }
        return arrayList;
    }

    private static void addFaceGrid(List<Vec3> list, int n, double d, double d2, double d3, double d4, double d5, int n2, double d6, double d7) {
        if (d6 < 1.0E-4 || d7 < 1.0E-4) {
            double d8 = (d2 + d3) / 2.0;
            double d9 = (d4 + d5) / 2.0;
            switch (n) {
                case 0: {
                    list.add(new Vec3(d, d8, d9));
                    break;
                }
                case 1: {
                    list.add(new Vec3(d8, d, d9));
                    break;
                }
                case 2: {
                    list.add(new Vec3(d8, d9, d));
                }
            }
            return;
        }
        double d10 = d6 / d7;
        int n3 = Math.max(2, (int)Math.round(Math.sqrt((double)n2 * d10)));
        int n4 = Math.max(2, (int)Math.round(Math.sqrt((double)n2 / d10)));
        for (int i = 0; i < n3; ++i) {
            double d11 = d2 + (d3 - d2) * (double)i / (double)(n3 - 1);
            block11: for (int j = 0; j < n4; ++j) {
                double d12 = d4 + (d5 - d4) * (double)j / (double)(n4 - 1);
                switch (n) {
                    case 0: {
                        list.add(new Vec3(d, d11, d12));
                        continue block11;
                    }
                    case 1: {
                        list.add(new Vec3(d11, d, d12));
                        continue block11;
                    }
                    case 2: {
                        list.add(new Vec3(d11, d12, d));
                    }
                }
            }
        }
    }

    public static double distanceSqFromEyeToClosestOnAABB(Entity entity) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return Double.MAX_VALUE;
        }
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        Vec3 vec32 = RotationUtils.closestPointOnAabb(axisAlignedBB, vec3);
        double d = vec3.xCoord - vec32.xCoord;
        double d2 = vec3.yCoord - vec32.yCoord;
        double d3 = vec3.zCoord - vec32.zCoord;
        return d * d + d2 * d2 + d3 * d3;
    }

    public static double distanceFromEyeToClosestOnAABB(Entity entity) {
        double d = RotationUtils.distanceSqFromEyeToClosestOnAABB(entity);
        return d == Double.MAX_VALUE ? Double.MAX_VALUE : Math.sqrt(d);
    }

    public static boolean canAimAtPoint(Vec3 vec3, Vec3 vec32, Entity entity, double d) {
        return RotationUtils.canAimAtPoint(vec3, vec32, entity, d, false, true);
    }

    public static boolean canAimAtPoint(Vec3 vec3, Vec3 vec32, Entity entity, double d, boolean bl, boolean bl2) {
        double d2;
        MovingObjectPosition movingObjectPosition;
        if (entity == null) {
            return false;
        }
        double d3 = vec32.xCoord - vec3.xCoord;
        double d4 = vec32.yCoord - vec3.yCoord;
        double d5 = vec32.zCoord - vec3.zCoord;
        double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
        if (d6 < 1.0E-6) {
            return false;
        }
        double d7 = d / d6;
        Vec3 vec33 = new Vec3(vec3.xCoord + d3 * d7, vec3.yCoord + d4 * d7, vec3.zCoord + d5 * d7);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        MovingObjectPosition movingObjectPosition2 = axisAlignedBB.calculateIntercept(vec3, vec33);
        if (movingObjectPosition2 == null) {
            return false;
        }
        double d8 = vec3.squareDistanceTo(movingObjectPosition2.hitVec);
        if (!bl && (movingObjectPosition = RotationUtils.mc.theWorld.rayTraceBlocks(vec3, vec33, false, false, false)) != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && (d2 = vec3.squareDistanceTo(movingObjectPosition.hitVec)) < d8) {
            return false;
        }
        return bl2 || !RotationUtils.hasEntityBlockingPath(vec3, vec33, entity, d8);
    }

    private static boolean hasEntityBlockingPath(Vec3 vec3, Vec3 vec32, Entity entity, double d) {
        if (RotationUtils.mc.thePlayer == null || RotationUtils.mc.theWorld == null) {
            return false;
        }
        Vec3 vec33 = vec32.subtract(vec3);
        AxisAlignedBB axisAlignedBB = RotationUtils.mc.thePlayer.getEntityBoundingBox().addCoord(vec33.xCoord, vec33.yCoord, vec33.zCoord).expand(1.0, 1.0, 1.0);
        List list = RotationUtils.mc.theWorld.getEntitiesInAABBexcluding((Entity)RotationUtils.mc.thePlayer, axisAlignedBB, Predicates.and((Predicate)EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
        for (Entity entity2 : list) {
            double d2;
            if (entity2 == null || entity2 == entity || entity2.isDead) continue;
            float f = entity2.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB2 = entity2.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
            MovingObjectPosition movingObjectPosition = axisAlignedBB2.calculateIntercept(vec3, vec32);
            if (axisAlignedBB2.isVecInside(vec3)) {
                return true;
            }
            if (movingObjectPosition == null || !((d2 = vec3.squareDistanceTo(movingObjectPosition.hitVec)) < d - 1.0E-7)) continue;
            return true;
        }
        return false;
    }

    public static boolean isPathBlockedByEntity(Vec3 vec3, Vec3 vec32, Entity entity) {
        if (vec3 == null || vec32 == null || entity == null) {
            return false;
        }
        double d = vec3.squareDistanceTo(vec32);
        return RotationUtils.hasEntityBlockingPath(vec3, vec32, entity, d);
    }

    public static boolean isEyeInsideEntityAABB(Entity entity) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return false;
        }
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        return axisAlignedBB.isVecInside(vec3);
    }

    private static boolean mainRayHitsTargetAABB(Vec3 vec3, Vec3 vec32, Entity entity, double d) {
        double d2 = vec32.xCoord - vec3.xCoord;
        double d3 = vec32.yCoord - vec3.yCoord;
        double d4 = vec32.zCoord - vec3.zCoord;
        double d5 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
        if (d5 < 1.0E-6) {
            return false;
        }
        double d6 = d / d5;
        Vec3 vec33 = new Vec3(vec3.xCoord + d2 * d6, vec3.yCoord + d3 * d6, vec3.zCoord + d4 * d6);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        return axisAlignedBB.calculateIntercept(vec3, vec33) != null;
    }

    public static boolean hasValidAimPoint(Entity entity, double d, double d2, double d3) {
        return RotationUtils.hasValidAimPoint(entity, d, d2, d3, false, true);
    }

    public static boolean hasValidAimPoint(Entity entity, double d, double d2, double d3, boolean bl, boolean bl2) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return false;
        }
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
        if (axisAlignedBB.isVecInside(vec3)) {
            return true;
        }
        AxisAlignedBB axisAlignedBB2 = RotationUtils.getAimPointBounds(entity);
        double d4 = Math.max(0.0, Math.min(1.0, d / 100.0));
        double d5 = Math.max(0.0, Math.min(1.0, d2 / 100.0));
        return RotationUtils.hasAnyHittablePoint(entity, axisAlignedBB2, vec3, d4, d5, d3, bl, bl2);
    }

    private static boolean hasAnyHittablePoint(Entity entity, AxisAlignedBB axisAlignedBB, Vec3 vec3, double d, double d2, double d3, boolean bl, boolean bl2) {
        AxisAlignedBB axisAlignedBB2 = RotationUtils.computeAimRegion(entity, axisAlignedBB, d, d2);
        Vec3 vec32 = RotationUtils.closestPointOnAabb(axisAlignedBB2, vec3);
        if (RotationUtils.mainRayHitsTargetAABB(vec3, vec32, entity, d3) && RotationUtils.canAimAtPoint(vec3, vec32, entity, d3, bl, bl2)) {
            return true;
        }
        Vec3 vec33 = new Vec3((axisAlignedBB2.minX + axisAlignedBB2.maxX) / 2.0, (axisAlignedBB2.minY + axisAlignedBB2.maxY) / 2.0, (axisAlignedBB2.minZ + axisAlignedBB2.maxZ) / 2.0);
        if (RotationUtils.mainRayHitsTargetAABB(vec3, vec33, entity, d3) && RotationUtils.canAimAtPoint(vec3, vec33, entity, d3, bl, bl2)) {
            return true;
        }
        List<Vec3> list = RotationUtils.buildBackupPoints(axisAlignedBB2, vec3);
        for (int i = 0; i < list.size(); ++i) {
            Vec3 vec34 = list.get(i);
            if (!RotationUtils.mainRayHitsTargetAABB(vec3, vec34, entity, d3) || !RotationUtils.canAimAtPoint(vec3, vec34, entity, d3, bl, bl2)) continue;
            return true;
        }
        List<Vec3> list2 = RotationUtils.buildBackupPoints(axisAlignedBB, vec3);
        for (int i = 0; i < list2.size(); ++i) {
            if (!RotationUtils.canAimAtPoint(vec3, list2.get(i), entity, d3, bl, bl2)) continue;
            return true;
        }
        return false;
    }

    public static float[] getRotationsWithBackup(Entity entity, double d, double d2, float f, float f2, double d3) {
        return RotationUtils.getRotationsWithBackup(entity, d, d2, f, f2, d3, false, true);
    }

    public static float[] getRotationsWithBackup(Entity entity, double d, double d2, float f, float f2, double d3, boolean bl, boolean bl2) {
        double d4;
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return null;
        }
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f3 = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f3, (double)f3, (double)f3);
        AxisAlignedBB axisAlignedBB2 = RotationUtils.getAimPointBounds(entity);
        if (axisAlignedBB.isVecInside(vec3)) {
            double d5 = (axisAlignedBB2.minX + axisAlignedBB2.maxX) / 2.0;
            double d6 = (axisAlignedBB2.minZ + axisAlignedBB2.maxZ) / 2.0;
            double d7 = Math.max(axisAlignedBB2.minY, Math.min(axisAlignedBB2.maxY, vec3.yCoord));
            return RotationUtils.getRotationsToPoint(d5, d7, d6, f, f2);
        }
        double d8 = Math.max(0.0, Math.min(1.0, d / 100.0));
        Vec3 vec32 = RotationUtils.findBestHittablePoint(entity, axisAlignedBB2, vec3, d8, d4 = Math.max(0.0, Math.min(1.0, d2 / 100.0)), d3, bl, bl2, true);
        if (vec32 == null) {
            return null;
        }
        return RotationUtils.getRotationsToPoint(vec32.xCoord, vec32.yCoord, vec32.zCoord, f, f2);
    }

    private static Vec3 findBestHittablePoint(Entity entity, AxisAlignedBB axisAlignedBB, Vec3 vec3, double d, double d2, double d3, boolean bl, boolean bl2, boolean bl3) {
        Object object32;
        Vec3 vec33;
        AxisAlignedBB axisAlignedBB2 = RotationUtils.computeAimRegion(entity, axisAlignedBB, d, d2);
        if (bl3 && hasStickyPoint && stickyEntity == entity) {
            boolean bl4;
            vec33 = RotationUtils.fracToPoint(axisAlignedBB, stickyFracX, stickyFracY, stickyFracZ);
            boolean bl5 = bl4 = System.nanoTime() - stickyPointTimeNs < 500000000L;
            if (bl4 && RotationUtils.isInside(axisAlignedBB2, vec33) && RotationUtils.mainRayHitsTargetAABB(vec3, vec33, entity, d3) && RotationUtils.canAimAtPoint(vec3, vec33, entity, d3, bl, bl2)) {
                return vec33;
            }
        }
        vec33 = RotationUtils.buildRegionCandidates(axisAlignedBB2, vec3);
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Object object2 : vec33) {
            if (!RotationUtils.mainRayHitsTargetAABB(vec3, (Vec3)object2, entity, d3) || !RotationUtils.canAimAtPoint(vec3, (Vec3)object2, entity, d3, bl, bl2)) continue;
            arrayList.add(object2);
        }
        if (arrayList.isEmpty()) {
            Object object2;
            List<Vec3> list = RotationUtils.buildBackupPoints(axisAlignedBB, vec3);
            Collections.sort(list, Comparator.comparingDouble(vec32 -> RotationUtils.sqDist(vec3, vec32)));
            object2 = list.iterator();
            while (object2.hasNext()) {
                Vec3 vec34 = (Vec3)object2.next();
                if (!RotationUtils.canAimAtPoint(vec3, vec34, entity, d3, bl, bl2)) continue;
                if (bl3) {
                    RotationUtils.setSticky(entity, axisAlignedBB, vec34);
                }
                return vec34;
            }
            if (bl3) {
                RotationUtils.clearSticky();
            }
            return null;
        }
        double d4 = Double.MAX_VALUE;
        for (Object object32 : arrayList) {
            d4 = Math.min(d4, RotationUtils.dist(vec3, (Vec3)object32));
        }
        ArrayList arrayList2 = new ArrayList();
        for (Vec3 vec35 : arrayList) {
            if (!(RotationUtils.dist(vec3, vec35) <= d4 + 0.15)) continue;
            arrayList2.add(vec35);
        }
        object32 = (Vec3)arrayList2.get(RotationUtils.randomIndex(arrayList2.size()));
        if (bl3) {
            RotationUtils.setSticky(entity, axisAlignedBB, (Vec3)object32);
        }
        return object32;
    }

    private static AxisAlignedBB computeAimRegion(Entity entity, AxisAlignedBB axisAlignedBB, double d, double d2) {
        double d3 = (axisAlignedBB.minX + axisAlignedBB.maxX) / 2.0;
        double d4 = (axisAlignedBB.minZ + axisAlignedBB.maxZ) / 2.0;
        double d5 = entity instanceof EntityLivingBase ? entity.posY + (double)((EntityLivingBase)entity).getEyeHeight() : (axisAlignedBB.minY + axisAlignedBB.maxY) / 2.0;
        if (d5 < axisAlignedBB.minY) {
            d5 = axisAlignedBB.minY;
        }
        if (d5 > axisAlignedBB.maxY) {
            d5 = axisAlignedBB.maxY;
        }
        double d6 = d3 - (d3 - axisAlignedBB.minX) * d;
        double d7 = d3 + (axisAlignedBB.maxX - d3) * d;
        double d8 = d5 - (d5 - axisAlignedBB.minY) * d2;
        double d9 = d5 + (axisAlignedBB.maxY - d5) * d2;
        double d10 = d4 - (d4 - axisAlignedBB.minZ) * d;
        double d11 = d4 + (axisAlignedBB.maxZ - d4) * d;
        return new AxisAlignedBB(d6, d8, d10, d7, d9, d11);
    }

    private static List<Vec3> buildRegionCandidates(AxisAlignedBB axisAlignedBB, Vec3 vec3) {
        List<Vec3> list = RotationUtils.buildBackupPoints(axisAlignedBB, vec3);
        list.add(RotationUtils.closestPointOnAabb(axisAlignedBB, vec3));
        double d = (axisAlignedBB.minX + axisAlignedBB.maxX) / 2.0;
        double d2 = (axisAlignedBB.minY + axisAlignedBB.maxY) / 2.0;
        double d3 = (axisAlignedBB.minZ + axisAlignedBB.maxZ) / 2.0;
        list.add(new Vec3(d, d2, d3));
        return list;
    }

    private static boolean isInside(AxisAlignedBB axisAlignedBB, Vec3 vec3) {
        return vec3.xCoord >= axisAlignedBB.minX - 1.0E-6 && vec3.xCoord <= axisAlignedBB.maxX + 1.0E-6 && vec3.yCoord >= axisAlignedBB.minY - 1.0E-6 && vec3.yCoord <= axisAlignedBB.maxY + 1.0E-6 && vec3.zCoord >= axisAlignedBB.minZ - 1.0E-6 && vec3.zCoord <= axisAlignedBB.maxZ + 1.0E-6;
    }

    private static Vec3 fracToPoint(AxisAlignedBB axisAlignedBB, double d, double d2, double d3) {
        return new Vec3(axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) * d, axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) * d2, axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) * d3);
    }

    private static double invLerp(double d, double d2, double d3) {
        double d4 = d2 - d;
        if (d4 < 1.0E-9) {
            return 0.5;
        }
        return (d3 - d) / d4;
    }

    private static double sqDist(Vec3 vec3, Vec3 vec32) {
        double d = vec3.xCoord - vec32.xCoord;
        double d2 = vec3.yCoord - vec32.yCoord;
        double d3 = vec3.zCoord - vec32.zCoord;
        return d * d + d2 * d2 + d3 * d3;
    }

    private static double dist(Vec3 vec3, Vec3 vec32) {
        return Math.sqrt(RotationUtils.sqDist(vec3, vec32));
    }

    private static int randomIndex(int n) {
        return n <= 1 ? 0 : (int)(Math.random() * (double)n);
    }

    private static void setSticky(Entity entity, AxisAlignedBB axisAlignedBB, Vec3 vec3) {
        stickyEntity = entity;
        stickyFracX = RotationUtils.invLerp(axisAlignedBB.minX, axisAlignedBB.maxX, vec3.xCoord);
        stickyFracY = RotationUtils.invLerp(axisAlignedBB.minY, axisAlignedBB.maxY, vec3.yCoord);
        stickyFracZ = RotationUtils.invLerp(axisAlignedBB.minZ, axisAlignedBB.maxZ, vec3.zCoord);
        hasStickyPoint = true;
        stickyPointTimeNs = System.nanoTime();
    }

    private static void clearSticky() {
        hasStickyPoint = false;
        stickyEntity = null;
    }

    public static float[] getRotationsPredicated(Entity entity, int n) {
        if (entity == null) {
            return null;
        }
        if (n == 0) {
            return RotationUtils.getRotations(entity);
        }
        double d = entity.posX;
        double d2 = entity.posY;
        double d3 = entity.posZ;
        double d4 = d - entity.lastTickPosX;
        double d5 = d3 - entity.lastTickPosZ;
        for (int i = 0; i < n; ++i) {
            d += d4;
            d3 += d5;
        }
        double d6 = d - RotationUtils.mc.thePlayer.posX;
        double d7 = entity instanceof EntityLivingBase ? d2 + (double)entity.getEyeHeight() * 0.9 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight()) : (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight());
        double d8 = d3 - RotationUtils.mc.thePlayer.posZ;
        return new float[]{RotationUtils.applyVanilla(RotationUtils.mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)((float)(Math.atan2(d8, d6) * (double)57.29578f) - 90.0f - RotationUtils.mc.thePlayer.rotationYaw))), RotationUtils.clampPitch(RotationUtils.mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)((float)(-(Math.atan2(d7, MathHelper.sqrt_double((double)(d6 * d6 + d8 * d8))) * (double)57.29578f)) - RotationUtils.mc.thePlayer.rotationPitch)) + 3.0f)};
    }

    public static float[] smoothRotation(float f, float f2, float f3, float f4, int n) {
        return RotationUtils.smoothRotation(f, f2, f3, f4, n, 0.0f);
    }

    public static float[] smoothRotation(float f, float f2, float f3, float f4, int n, float f5) {
        float f6;
        if (n <= 0) {
            return new float[]{f, RotationUtils.clampPitch(f2)};
        }
        if (n >= 30) {
            return new float[]{f3, RotationUtils.clampPitch(f4)};
        }
        float f7 = MathHelper.wrapAngleTo180_float((float)(f3 - f));
        float f8 = MathHelper.sqrt_double((double)(f7 * f7 + (f6 = f4 - f2) * f6));
        if (f8 < 0.001f) {
            return new float[]{f3, RotationUtils.clampPitch(f4)};
        }
        float f9 = (float)n / 30.0f;
        float f10 = f9 * f9 * 180.0f;
        float f11 = 0.6f * (float)((double)f5 / 100.0);
        float f12 = f11 <= 0.001f ? 1.0f : 1.0f - f11 / 2.0f + (float)(Math.random() * (double)f11);
        f10 *= f12;
        float f13 = Math.min(1.0f, f8 / 180.0f);
        f13 = (float)Math.pow(f13, 0.7);
        float f14 = (float)((double)f5 / 100.0);
        float f15 = Math.max(0.8f, 1.0f - f14 * (1.0f - f13));
        float f16 = Math.min(f10 *= f15, f8);
        float f17 = f16 / f8;
        float f18 = f7 * f17;
        float f19 = f6 * f17;
        float f20 = f + f18;
        float f21 = f2 + f19;
        return new float[]{f20, RotationUtils.clampPitch(f21)};
    }

    public static float[] smoothRotationFov(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7;
        float f8;
        float f9;
        float f10 = f5 / 100.0f;
        if (f10 <= 0.0f) {
            return new float[]{f, RotationUtils.clampPitch(f2)};
        }
        if (f10 >= 0.985f) {
            return new float[]{f3, RotationUtils.clampPitch(f4)};
        }
        float f11 = MathHelper.wrapAngleTo180_float((float)(f3 - f));
        float f12 = MathHelper.sqrt_double((double)(f11 * f11 + (f9 = f4 - f2) * f9));
        if (f12 < 0.001f) {
            return new float[]{f3, RotationUtils.clampPitch(f4)};
        }
        float f13 = 0.06f + 0.94f * (f10 * f10);
        float f14 = Math.min(1.0f, f12 / 120.0f);
        float f15 = f13 + (1.0f - f13) * f14 * f10;
        float f16 = 0.6f * (f6 / 100.0f);
        float f17 = f16 <= 0.001f ? 1.0f : 1.0f - f16 / 2.0f + (float)(Math.random() * (double)f16);
        if ((f15 *= f17) > 1.0f) {
            f15 = 1.0f;
        }
        if ((f8 = f12 * f15) < (f7 = 2.0f * f10)) {
            f8 = f7;
        }
        if (f8 > f12) {
            f8 = f12;
        }
        float f18 = f8 / f12;
        float f19 = f + f11 * f18;
        float f20 = f2 + f9 * f18;
        return new float[]{f19, RotationUtils.clampPitch(f20)};
    }

    public static boolean rotationHitsTarget(float f, float f2, Entity entity, double d) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return false;
        }
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 vec32 = Utils.getLookVec(f, f2);
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        float f3 = entity.getCollisionBorderSize();
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f3, (double)f3, (double)f3);
        return axisAlignedBB.calculateIntercept(vec3, vec33) != null;
    }

    public static boolean rotationCanHitTarget(float f, float f2, Entity entity, double d, boolean bl, boolean bl2) {
        if (entity == null || RotationUtils.mc.thePlayer == null) {
            return false;
        }
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 vec32 = Utils.getLookVec(f, f2);
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        return RotationUtils.canAimAtPoint(vec3, vec33, entity, d, bl, bl2);
    }

    public static float[] settleOnTarget(float f, float f2, float[] fArray, float f3, float f4, Entity entity, double d, boolean bl, boolean bl2) {
        float f5;
        if (fArray == null || entity == null || RotationUtils.mc.thePlayer == null) {
            return fArray;
        }
        if (RotationUtils.rotationCanHitTarget(fArray[0], fArray[1], entity, d, bl, bl2)) {
            return fArray;
        }
        if (!RotationUtils.rotationCanHitTarget(f3, f4, entity, d, bl, bl2)) {
            return fArray;
        }
        float f6 = MathHelper.wrapAngleTo180_float((float)(f3 - f));
        float f7 = f6 * f6 + (f5 = f4 - f2) * f5;
        if (f7 < 1.0E-8f) {
            return new float[]{f3, RotationUtils.clampPitch(f4)};
        }
        float f8 = (MathHelper.wrapAngleTo180_float((float)(fArray[0] - f)) * f6 + (fArray[1] - f2) * f5) / f7;
        for (float f9 : SETTLE_STEPS) {
            float f10;
            float f11;
            if (f9 <= f8 || !RotationUtils.rotationCanHitTarget(f11 = f + f6 * f9, f10 = RotationUtils.clampPitch(f2 + f5 * f9), entity, d, bl, bl2)) continue;
            return new float[]{f11, f10};
        }
        return new float[]{f3, RotationUtils.clampPitch(f4)};
    }

    public static float clampPitch(float f) {
        return MathHelper.clamp_float((float)f, (float)-90.0f, (float)90.0f);
    }

    public static float[] fixRotation(float f, float f2, float f3, float f4) {
        f = RotationHelper.unwrapYaw(f, f3);
        float f5 = f - f3;
        float f6 = Math.abs(f5);
        float f7 = f2 - f4;
        float f8 = RotationUtils.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        double d = (double)(f8 * f8 * f8) * 1.2;
        float f9 = (float)((double)Math.round((double)f5 / d) * d);
        float f10 = (float)((double)Math.round((double)f7 / d) * d);
        f = f3 + f9;
        f2 = f4 + f10;
        return new float[]{f, RotationUtils.clampPitch(f2)};
    }

    public static float angle(double d, double d2) {
        return (float)(Math.atan2(d - RotationUtils.mc.thePlayer.posX, d2 - RotationUtils.mc.thePlayer.posZ) * (double)57.29578f * -1.0);
    }

    public static float deltaAngle(double d, double d2) {
        return (float)(Math.atan2(d, d2) * (double)57.29578f * -1.0);
    }

    public static MovingObjectPosition rayCast(double d, float f, float f2, boolean bl) {
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        float f3 = -f * ((float)Math.PI / 180);
        float f4 = -f2 * ((float)Math.PI / 180);
        float f5 = MathHelper.cos((float)(f3 - (float)Math.PI));
        float f6 = MathHelper.sin((float)(f3 - (float)Math.PI));
        float f7 = -MathHelper.cos((float)f4);
        Vec3 vec32 = new Vec3((double)(f6 * f7), (double)MathHelper.sin((float)f4), (double)(f5 * f7));
        return RotationUtils.mc.theWorld.rayTraceBlocks(vec3, vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d), true, bl, true);
    }

    public static MovingObjectPosition rayCastBlock(double d, float f, float f2) {
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 vec32 = Utils.getLookVec(f, f2);
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        MovingObjectPosition movingObjectPosition = RotationUtils.mc.theWorld.rayTraceBlocks(vec3, vec33, false, false, false);
        if (movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return null;
        }
        return movingObjectPosition;
    }

    public static MovingObjectPosition rayTraceCustom(double d, float f, float f2) {
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 vec32 = RotationUtils.getVectorForRotation(f2, f);
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        return RotationUtils.mc.theWorld.rayTraceBlocks(vec3, vec33, false, false, true);
    }

    public static MovingObjectPosition rayTraceBlockIfNoEntityInFront(double d, float f, float f2) {
        if (RotationUtils.mc.thePlayer == null || RotationUtils.mc.theWorld == null) {
            return null;
        }
        MovingObjectPosition movingObjectPosition = RotationUtils.rayTraceCustom(d, f, f2);
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(1.0f);
        double d2 = d;
        if (movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return null;
        }
        d2 = movingObjectPosition.hitVec.distanceTo(vec3);
        Vec3 vec32 = RotationUtils.getVectorForRotation(f2, f);
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
        List list = RotationUtils.mc.theWorld.getEntitiesInAABBexcluding((Entity)RotationUtils.mc.thePlayer, RotationUtils.mc.thePlayer.getEntityBoundingBox().addCoord(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d).expand(1.0, 1.0, 1.0), Predicates.and((Predicate)EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
        for (Entity entity : list) {
            double d3;
            float f3 = entity.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f3, (double)f3, (double)f3);
            MovingObjectPosition movingObjectPosition2 = axisAlignedBB.calculateIntercept(vec3, vec33);
            if (axisAlignedBB.isVecInside(vec3)) {
                return null;
            }
            if (movingObjectPosition2 == null || !((d3 = vec3.distanceTo(movingObjectPosition2.hitVec)) < d2)) continue;
            return null;
        }
        return movingObjectPosition;
    }

    public static Vec3 getVectorForRotation(float f, float f2) {
        float f3 = MathHelper.cos((float)(-f2 * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = MathHelper.sin((float)(-f2 * ((float)Math.PI / 180) - (float)Math.PI));
        float f5 = -MathHelper.cos((float)(-f * ((float)Math.PI / 180)));
        float f6 = MathHelper.sin((float)(-f * ((float)Math.PI / 180)));
        return new Vec3((double)(f4 * f5), (double)f6, (double)(f3 * f5));
    }

    public static float applyVanilla(float f, boolean bl) {
        if (bl) {
            return f;
        }
        int n = (int)Math.floor(serverRotations[0] / 360.0f);
        float f2 = f + (float)(360 * n);
        if (f2 < serverRotations[0] - 180.0f) {
            f2 += 360.0f;
        } else if (f2 > serverRotations[0] + 180.0f) {
            f2 -= 360.0f;
        }
        float f3 = f2 - serverRotations[0];
        return serverRotations[0] + f3;
    }

    public static MovingObjectPosition rayTrace(double d, float f, float[] fArray, EntityLivingBase entityLivingBase) {
        MovingObjectPosition movingObjectPosition;
        if (entityLivingBase != null && (movingObjectPosition = RotationUtils.rayTraceIgnore(d, f, fArray, entityLivingBase)) != null) {
            return movingObjectPosition;
        }
        movingObjectPosition = null;
        double d2 = d;
        if (fArray == null) {
            fArray = new float[]{RotationUtils.mc.thePlayer.rotationYaw, RotationUtils.mc.thePlayer.rotationPitch};
        }
        MovingObjectPosition movingObjectPosition2 = RotationUtils.rayTraceCustom(d2, fArray[0], fArray[1]);
        double d3 = d2;
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(f);
        if (RotationUtils.mc.playerController.extendedReach()) {
            d2 = 6.0;
            d3 = 6.0;
        }
        if (movingObjectPosition2 != null) {
            d3 = movingObjectPosition2.hitVec.distanceTo(vec3);
        }
        Vec3 vec32 = RotationUtils.getVectorForRotation(fArray[1], fArray[0]);
        Vec3 vec33 = vec3.addVector(vec32.xCoord * d2, vec32.yCoord * d2, vec32.zCoord * d2);
        Vec3 vec34 = null;
        float f2 = 1.0f;
        List list = RotationUtils.mc.theWorld.getEntitiesInAABBexcluding((Entity)RotationUtils.mc.thePlayer, RotationUtils.mc.thePlayer.getEntityBoundingBox().addCoord(vec32.xCoord * d2, vec32.yCoord * d2, vec32.zCoord * d2).expand((double)f2, (double)f2, (double)f2), Predicates.and((Predicate)EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
        double d4 = d3;
        for (int i = 0; i < list.size(); ++i) {
            double d5;
            Entity entity = (Entity)list.get(i);
            float f3 = entity.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand((double)f3, (double)f3, (double)f3);
            MovingObjectPosition movingObjectPosition3 = axisAlignedBB.calculateIntercept(vec3, vec33);
            if (axisAlignedBB.isVecInside(vec3)) {
                if (!(d4 >= 0.0)) continue;
                movingObjectPosition = entity;
                vec34 = movingObjectPosition3 == null ? vec3 : movingObjectPosition3.hitVec;
                d4 = 0.0;
                continue;
            }
            if (movingObjectPosition3 == null || !((d5 = vec3.distanceTo(movingObjectPosition3.hitVec)) < d4) && d4 != 0.0) continue;
            if (entity == RotationUtils.mc.thePlayer.ridingEntity && !VanillaHooks.canRiderInteract((Entity)RotationUtils.mc.thePlayer)) {
                if (d4 != 0.0) continue;
                movingObjectPosition = entity;
                vec34 = movingObjectPosition3.hitVec;
                continue;
            }
            movingObjectPosition = entity;
            vec34 = movingObjectPosition3.hitVec;
            d4 = d5;
        }
        if (movingObjectPosition != null && d4 < d3) {
            return new MovingObjectPosition((Entity)movingObjectPosition, vec34);
        }
        return null;
    }

    public static MovingObjectPosition rayTraceIgnore(double d, float f, float[] fArray, EntityLivingBase entityLivingBase) {
        MovingObjectPosition movingObjectPosition = RotationUtils.rayTraceCustom(d, fArray[0], fArray[1]);
        Vec3 vec3 = RotationUtils.mc.thePlayer.getPositionEyes(f);
        double d2 = d;
        if (movingObjectPosition != null) {
            d2 = movingObjectPosition.hitVec.distanceTo(vec3);
        }
        if (entityLivingBase != null) {
            double d3;
            if (fArray == null) {
                fArray = new float[]{RotationUtils.mc.thePlayer.rotationYaw, RotationUtils.mc.thePlayer.rotationPitch};
            }
            Vec3 vec32 = RotationUtils.getVectorForRotation(fArray[1], fArray[0]);
            Vec3 vec33 = vec3.addVector(vec32.xCoord * d, vec32.yCoord * d, vec32.zCoord * d);
            float f2 = entityLivingBase.getCollisionBorderSize();
            AxisAlignedBB axisAlignedBB = entityLivingBase.getEntityBoundingBox().expand((double)f2, (double)f2, (double)f2);
            MovingObjectPosition movingObjectPosition2 = axisAlignedBB.calculateIntercept(vec3, vec33);
            if (axisAlignedBB.isVecInside(vec3)) {
                return new MovingObjectPosition((Entity)entityLivingBase, vec3);
            }
            if (movingObjectPosition2 != null && (d3 = vec3.distanceTo(movingObjectPosition2.hitVec)) < d2) {
                return new MovingObjectPosition((Entity)entityLivingBase, movingObjectPosition2.hitVec);
            }
        }
        if (movingObjectPosition != null) {
            return movingObjectPosition;
        }
        return null;
    }

    public static float applyVanilla(float f) {
        return RotationUtils.applyVanilla(f, false);
    }

    public static float[] getRotationsFromEye(Vec3 vec3, double d, double d2, double d3) {
        double d4 = d - vec3.xCoord;
        double d5 = d2 - vec3.yCoord;
        double d6 = d3 - vec3.zCoord;
        double d7 = Math.sqrt(d4 * d4 + d6 * d6);
        float f = (float)Math.toDegrees(Math.atan2(d6, d4)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d5, d7)));
        return new float[]{f, f2};
    }

    static {
        serverRotations = new float[]{0.0f, 0.0f};
        SETTLE_STEPS = new float[]{0.4f, 0.6f, 0.8f, 0.92f};
    }
}

