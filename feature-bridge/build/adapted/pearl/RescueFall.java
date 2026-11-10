package dev.rice.module.impl.pearl;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public final class RescueFall {
    private static final int MAX_TICKS = 160;
    public final boolean lands;
    public final Vec3 catchPosition;

    private RescueFall(boolean bl, Vec3 vec3) {
        this.lands = bl;
        this.catchPosition = vec3;
    }

    public static RescueFall predict(EntityPlayer entityPlayer, double d) {
        return RescueFall.predict(entityPlayer, d, null);
    }

    public static RescueFall predict(EntityPlayer entityPlayer, double d, Vec3 vec3) {
        if (entityPlayer instanceof EntityPlayerSP && Minecraft.getMinecraft().thePlayer == entityPlayer && ((EntityPlayerSP)entityPlayer).movementInput != null) {
            return RescueFall.predictControlled((EntityPlayerSP)entityPlayer, d, vec3);
        }
        return RescueFall.predictBallistic(entityPlayer, d, vec3);
    }

    private static RescueFall predictControlled(EntityPlayerSP entityPlayerSP, double d, Vec3 vec3) {
        SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(entityPlayerSP.movementInput);
        if (vec3 != null) {
            simulatedPlayer.motionX = vec3.xCoord;
            simulatedPlayer.motionY = vec3.yCoord;
            simulatedPlayer.motionZ = vec3.zCoord;
        }
        Vec3 vec32 = null;
        for (int i = 0; i < 160 && simulatedPlayer.box.minY > -8.0; ++i) {
            AxisAlignedBB axisAlignedBB = simulatedPlayer.box;
            if (!RescueFall.loaded((EntityPlayer)entityPlayerSP, axisAlignedBB)) {
                return new RescueFall(true, null);
            }
            simulatedPlayer.tick();
            AxisAlignedBB axisAlignedBB2 = simulatedPlayer.box;
            if (entityPlayerSP.worldObj.isMaterialInBB(axisAlignedBB2, Material.water)) {
                return new RescueFall(true, vec32);
            }
            if (simulatedPlayer.onGround) {
                return new RescueFall(true, vec32);
            }
            if (vec32 != null || !(axisAlignedBB.minY >= d) || !(axisAlignedBB2.minY <= d)) continue;
            vec32 = RescueFall.crossing(axisAlignedBB, axisAlignedBB2, d);
        }
        return new RescueFall(simulatedPlayer.box.minY > -8.0, vec32);
    }

    private static RescueFall predictBallistic(EntityPlayer entityPlayer, double d, Vec3 vec3) {
        AxisAlignedBB axisAlignedBB = entityPlayer.getEntityBoundingBox();
        double d2 = vec3 == null ? entityPlayer.motionX : vec3.xCoord;
        double d3 = vec3 == null ? entityPlayer.motionY : vec3.yCoord;
        double d4 = vec3 == null ? entityPlayer.motionZ : vec3.zCoord;
        Vec3 vec32 = null;
        for (int i = 0; i < 160 && axisAlignedBB.minY > -8.0; ++i) {

            if (!RescueFall.loaded(entityPlayer, axisAlignedBB)) {
                return new RescueFall(true, null);
            }
            List<AxisAlignedBB> list = entityPlayer.worldObj.getCollidingBoundingBoxes((Entity)entityPlayer, axisAlignedBB.addCoord(d2, d3, d4));
            double d5 = d3;
            double d6 = d2;
            double d7 = d4;
            for (AxisAlignedBB object2 : list) {
                d5 = object2.calculateYOffset(axisAlignedBB, d5);
            }
            AxisAlignedBB axisAlignedBB2 = axisAlignedBB;
            axisAlignedBB = axisAlignedBB.offset(0.0, d5, 0.0);
            for (AxisAlignedBB axisAlignedBB3 : list) {
                d6 = axisAlignedBB3.calculateXOffset(axisAlignedBB, d6);
            }
            axisAlignedBB = axisAlignedBB.offset(d6, 0.0, 0.0);
            java.util.Iterator<AxisAlignedBB> object2 = list.iterator();
            while (object2.hasNext()) {
                AxisAlignedBB axisAlignedBB3;
                axisAlignedBB3 = (AxisAlignedBB)object2.next();
                d7 = axisAlignedBB3.calculateZOffset(axisAlignedBB, d7);
            }
            if (entityPlayer.worldObj.isMaterialInBB(axisAlignedBB = axisAlignedBB.offset(0.0, 0.0, d7), Material.water)) {
                return new RescueFall(true, vec32);
            }
            if (d3 < 0.0 && d5 != d3) {
                return new RescueFall(true, vec32);
            }
            if (vec32 == null && axisAlignedBB2.minY >= d && axisAlignedBB.minY <= d) {
                vec32 = RescueFall.crossing(axisAlignedBB2, axisAlignedBB, d);
            }
            d2 = d6 == d2 ? d2 * 0.91 : 0.0;
            d4 = d7 == d4 ? d4 * 0.91 : 0.0;
            d3 = (d3 - 0.08) * 0.98;
        }
        return new RescueFall(axisAlignedBB.minY > -8.0, vec32);
    }

    private static boolean loaded(EntityPlayer entityPlayer, AxisAlignedBB axisAlignedBB) {
        return entityPlayer.worldObj.isBlockLoaded(new BlockPos((axisAlignedBB.minX + axisAlignedBB.maxX) * 0.5, 64.0, (axisAlignedBB.minZ + axisAlignedBB.maxZ) * 0.5));
    }

    private static Vec3 crossing(AxisAlignedBB axisAlignedBB, AxisAlignedBB axisAlignedBB2, double d) {
        double d2 = (axisAlignedBB.minY - d) / Math.max(1.0E-9, axisAlignedBB.minY - axisAlignedBB2.minY);
        double d3 = (axisAlignedBB.minX + axisAlignedBB.maxX) * 0.5;
        double d4 = (axisAlignedBB.minZ + axisAlignedBB.maxZ) * 0.5;
        double d5 = (axisAlignedBB2.minX + axisAlignedBB2.maxX) * 0.5;
        double d6 = (axisAlignedBB2.minZ + axisAlignedBB2.maxZ) * 0.5;
        return new Vec3(d3 + (d5 - d3) * d2, d, d4 + (d6 - d4) * d2);
    }
}

