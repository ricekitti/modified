/*
 * Decompiled with CFR 0.152.
 */
package com.alan.clients.util.player;

import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.player.EnumFacingOffset;
import com.alan.clients.util.player.GoodPotions;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.util.vector.Vector2i;
import com.alan.clients.util.vector.Vector3d;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import lombok.Generated;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public final class PlayerUtil
implements InstanceAccess {
    private static final HashMap<Integer, Integer> GOOD_POTIONS = new GoodPotions();

    public static Block o(double var0, double var2, double var4) {
        return PlayerUtil.aEg.theWorld.getBlockState(new BlockPos(var0, var2, var4)).getBlock();
    }

    public static Block block(BlockPos pos) {
        return PlayerUtil.aEg.theWorld.getBlockState(pos).getBlock();
    }

    public static Block a(Vec3i vec) {
        return PlayerUtil.block(new BlockPos(vec));
    }

    public static Block c(Vector3d var0) {
        return PlayerUtil.o(var0.getX(), var0.getY(), var0.getZ());
    }

    public static Block a(net.minecraft.util.Vector3d vec3) {
        return PlayerUtil.block(new BlockPos(new Vec3i(vec3.x, vec3.y, vec3.z)));
    }

    public static double distance(BlockPos pos, BlockPos var1) {
        double dx = pos.getX() - var1.getX();
        double dy = pos.getY() - var1.getY();
        double dz = pos.getZ() - var1.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public static boolean vg() {
        return PlayerUtil.av(0);
    }

    public static boolean av(int var0) {
        if (PlayerUtil.aEg.thePlayer.onGround && PlayerUtil.aEg.thePlayer.isCollidedHorizontally) {
            for (Vector2i ajz : new Vector2i[]{new Vector2i(0, 1), new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(-1, 0)}) {
                if (PlayerUtil.p(ajz.ald, var0, ajz.ale) instanceof BlockAir || !(PlayerUtil.p(ajz.ald, 1 + var0, ajz.ale) instanceof BlockAir)) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static Block p(double var0, double var2, double var4) {
        return PlayerUtil.o(PlayerUtil.aEg.thePlayer.posX + var0, PlayerUtil.aEg.thePlayer.posY + var2, PlayerUtil.aEg.thePlayer.posZ + var4);
    }

    public static Block blockAheadOfPlayer(double var0, double var2) {
        return PlayerUtil.p(-Math.sin(MoveUtil.direction()) * var0, var2, Math.cos(MoveUtil.direction()) * var0);
    }

    public static String g(EntityPlayer player) {
        return player.getName();
    }

    public static String name() {
        return PlayerUtil.aEg.thePlayer.getName();
    }

    public static boolean sameTeam(EntityLivingBase living) {
        if (living.getTeam() != null && PlayerUtil.aEg.thePlayer.getTeam() != null) {
            char c1;
            char c0 = living.getDisplayName().getFormattedText().charAt(1);
            return c0 == (c1 = PlayerUtil.aEg.thePlayer.getDisplayName().getFormattedText().charAt(1));
        }
        return false;
    }

    public static boolean ad(double var0) {
        return PlayerUtil.a(var0, true);
    }

    public static boolean a(double var0, boolean var2) {
        if (var2) {
            AxisAlignedBB axisalignedbb = PlayerUtil.aEg.thePlayer.getEntityBoundingBox().offset(0.0, -var0, 0.0);
            if (!PlayerUtil.aEg.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtil.aEg.thePlayer, axisalignedbb).isEmpty()) {
                return true;
            }
        } else {
            int i = 0;
            while ((double)i < var0) {
                if (PlayerUtil.p(0.0, -i, 0.0).isFullBlock()) {
                    return true;
                }
                ++i;
            }
        }
        return false;
    }

    public static boolean b(double var0, boolean var2) {
        AxisAlignedBB axisalignedbb = PlayerUtil.aEg.thePlayer.getEntityBoundingBox().offset(0.0, var0 / 2.0, 0.0).expand(0.0, var0 - (double)PlayerUtil.aEg.thePlayer.height, 0.0);
        return !PlayerUtil.aEg.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtil.aEg.thePlayer, axisalignedbb).isEmpty();
    }

    public static boolean vh() {
        return PlayerUtil.ad(10.0);
    }

    public static boolean ae(double var0) {
        AxisAlignedBB axisalignedbb = PlayerUtil.aEg.thePlayer.getEntityBoundingBox().offset(0.0, var0, 0.0);
        return !PlayerUtil.aEg.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtil.aEg.thePlayer, axisalignedbb).isEmpty();
    }

    public static double vi() {
        AxisAlignedBB axisalignedbb = PlayerUtil.aEg.thePlayer.getEntityBoundingBox();
        int i = MathHelper.floor_double((double)(axisalignedbb.minX + 1.0E-7));
        int j = MathHelper.floor_double((double)(axisalignedbb.maxX - 1.0E-7));
        int k = MathHelper.floor_double((double)(axisalignedbb.minZ + 1.0E-7));
        int l = MathHelper.floor_double((double)(axisalignedbb.maxZ - 1.0E-7));
        double d0 = axisalignedbb.minY;
        double d1 = Double.POSITIVE_INFINITY;
        for (int i1 = i; i1 <= j; ++i1) {
            block1: for (int j1 = k; j1 <= l; ++j1) {
                for (int k1 = MathHelper.floor_double((double)d0) - 1; k1 >= 0; --k1) {
                    BlockPos blockpos = new BlockPos(i1, k1, j1);
                    IBlockState iblockstate = PlayerUtil.aEg.theWorld.getBlockState(blockpos);
                    AxisAlignedBB axisalignedbb1 = iblockstate.getBlock().getCollisionBoundingBox((World)PlayerUtil.aEg.theWorld, blockpos, iblockstate);
                    if (axisalignedbb1 == null) continue;
                    double d2 = axisalignedbb1.maxY;
                    if (d0 >= d2) {
                        double d3 = d0 - d2;
                        if (!(d3 < d1)) continue block1;
                        d1 = d3;
                        continue block1;
                    }
                    d1 = 0.0;
                    continue block1;
                }
            }
        }
        return d1 == Double.POSITIVE_INFINITY ? -1.0 : d1;
    }

    public static boolean aw(int var0) {
        return GOOD_POTIONS.containsKey(var0);
    }

    public static int potionRanking(int var0) {
        return GOOD_POTIONS.getOrDefault(var0, -1);
    }

    public static boolean vj() {
        return PlayerUtil.aEg.thePlayer.isInWater() || PlayerUtil.aEg.thePlayer.isInLava();
    }

    public static void fakeDamage() {
        PlayerUtil.aEg.thePlayer.handleStatusUpdate((byte)2);
        PlayerUtil.aEg.ingameGUI.healthUpdateCounter = PlayerUtil.aEg.ingameGUI.updateCounter + 20;
    }

    public static boolean ay(int var0) {
        for (int i = -var0; i <= var0; ++i) {
            for (int j = -var0; j <= var0; ++j) {
                for (int k = -var0; k <= var0; ++k) {
                    if (PlayerUtil.p(i, j, k) instanceof BlockAir) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean a(int var0, Material material) {
        for (int i = -var0; i <= var0; ++i) {
            for (int j = -var0; j <= var0; ++j) {
                for (int k = -var0; k <= var0; ++k) {
                    if (!PlayerUtil.p(i, j, k).getMaterial().equals(material)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean vk() {
        if (PlayerUtil.aEg.thePlayer.ticksExisted < 5) {
            return false;
        }
        EntityPlayerSP entityplayersp = PlayerUtil.aEg.thePlayer;
        WorldClient worldclient = PlayerUtil.aEg.theWorld;
        AxisAlignedBB axisalignedbb = entityplayersp.getEntityBoundingBox();
        for (int i = MathHelper.floor_double((double)axisalignedbb.minX); i < MathHelper.floor_double((double)axisalignedbb.maxX) + 1; ++i) {
            for (int j = MathHelper.floor_double((double)axisalignedbb.minY); j < MathHelper.floor_double((double)axisalignedbb.maxY) + 1; ++j) {
                for (int k = MathHelper.floor_double((double)axisalignedbb.minZ); k < MathHelper.floor_double((double)axisalignedbb.maxZ) + 1; ++k) {
                    AxisAlignedBB axisalignedbb1;
                    Block block = worldclient.getBlockState(new BlockPos(i, j, k)).getBlock();
                    if (block == null || block instanceof BlockAir || (axisalignedbb1 = block.getCollisionBoundingBox((World)worldclient, new BlockPos(i, j, k), worldclient.getBlockState(new BlockPos(i, j, k)))) == null || !entityplayersp.getEntityBoundingBox().intersectsWith(axisalignedbb1)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static void sendClick(int var0, boolean var1) {
        int i = var0 == 0 ? PlayerUtil.aEg.gameSettings.cgK.getKeyCode() : PlayerUtil.aEg.gameSettings.cgI.getKeyCode();
        KeyBinding.setKeyBindState((int)i, (boolean)var1);
        if (var1) {
            KeyBinding.onTick((int)i);
        }
    }

    public static boolean vl() {
        boolean flag = false;
        AxisAlignedBB axisalignedbb = PlayerUtil.aEg.thePlayer.getEntityBoundingBox();
        WorldClient worldclient = PlayerUtil.aEg.theWorld;
        int i = (int)axisalignedbb.offset((double)0.0, (double)-0.01, (double)0.0).minY;
        for (int j = MathHelper.floor_double((double)axisalignedbb.minX); j < MathHelper.floor_double((double)axisalignedbb.maxX) + 1; ++j) {
            for (int k = MathHelper.floor_double((double)axisalignedbb.minZ); k < MathHelper.floor_double((double)axisalignedbb.maxZ) + 1; ++k) {
                Block block = worldclient.getBlockState(new BlockPos(j, i, k)).getBlock();
                if (block == null || block instanceof BlockAir) continue;
                if (!(block instanceof BlockLiquid)) {
                    return false;
                }
                flag = true;
            }
        }
        return flag;
    }

    public static EnumFacingOffset getEnumFacing(Vec3 vec) {
        return PlayerUtil.a(vec, false);
    }

    public static EnumFacingOffset a(Vec3 vec, boolean var1) {
        ArrayList<Object> arraylist = new ArrayList<Object>();
        for (int b0 = -1; b0 <= 1; b0 = (int)((byte)(b0 + 2))) {
            if (PlayerUtil.o(vec.xCoord, vec.yCoord, vec.zCoord + (double)b0).isReplaceable((World)PlayerUtil.aEg.theWorld, new BlockPos(vec.xCoord, vec.yCoord, vec.zCoord + (double)b0))) continue;
            if (b0 < 0) {
                arraylist.add(new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0.0, 0.0, (double)b0)));
                continue;
            }
            arraylist.add(new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0.0, 0.0, (double)b0)));
        }
        for (int b1 = -1; b1 <= 1; b1 = (int)((byte)(b1 + 2))) {
            if (PlayerUtil.o(vec.xCoord + (double)b1, vec.yCoord, vec.zCoord).isReplaceable((World)PlayerUtil.aEg.theWorld, new BlockPos(vec.xCoord + (double)b1, vec.yCoord, vec.zCoord))) continue;
            if (b1 > 0) {
                arraylist.add(new EnumFacingOffset(EnumFacing.WEST, new Vec3((double)b1, 0.0, 0.0)));
                continue;
            }
            arraylist.add(new EnumFacingOffset(EnumFacing.EAST, new Vec3((double)b1, 0.0, 0.0)));
        }
        arraylist.sort(Comparator.comparingDouble(var0x -> {
            double degrees = Math.toDegrees(Math.atan2(((EnumFacingOffset)var0x).getOffset().zCoord, ((EnumFacingOffset)var0x).getOffset().xCoord)) % 360.0;
            double d1 = RotationComponent.fk.x % 360.0f + 90.0f;
            return Math.abs(MathUtil.n((double)degrees, (double)d1));
        }));
        if (!arraylist.isEmpty()) {
            return (EnumFacingOffset)arraylist.get(0);
        }
        for (int b2 = -1; b2 <= 1; b2 = (int)((byte)(b2 + 2))) {
            if (PlayerUtil.o(vec.xCoord, vec.yCoord + (double)b2, vec.zCoord).isReplaceable((World)PlayerUtil.aEg.theWorld, new BlockPos(vec.xCoord, vec.yCoord + (double)b2, vec.zCoord))) continue;
            if (b2 < 0) {
                return new EnumFacingOffset(EnumFacing.UP, new Vec3(0.0, (double)b2, 0.0));
            }
            if (!var1) continue;
            return new EnumFacingOffset(EnumFacing.DOWN, new Vec3(0.0, (double)b2, 0.0));
        }
        return null;
    }

    public static Vec3 getPlacePossibility(double var0, double var2, double var4) {
        return PlayerUtil.a(var0, var2, var4, null);
    }

    public static Vec3 a(double var0, double var2, double var4, Integer var6) {
        ArrayList<Object> arraylist = new ArrayList<Object>();
        int i = (int)(5.0 + (Math.abs(var0) + Math.abs(var4)));
        for (int j = -i; j <= i; ++j) {
            for (int k = -i; k <= i; ++k) {
                for (int l = -i; l <= i; ++l) {
                    BlockPos blockpos = new BlockPos(PlayerUtil.aEg.thePlayer.posX + (double)j, PlayerUtil.aEg.thePlayer.posY + (double)k, PlayerUtil.aEg.thePlayer.posZ + (double)l);
                    Block block = PlayerUtil.aEg.theWorld.getBlockState(blockpos).getBlock();
                    if (block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.anvil || block == Blocks.crafting_table || block == Blocks.furnace || block == Blocks.lit_furnace || block == Blocks.hopper || block == Blocks.dropper || block == Blocks.dispenser || block == Blocks.noteblock || block == Blocks.jukebox) continue;
                    if (!(block.isFullBlock() && block.isNormalCube() || block instanceof BlockStairs || !(block instanceof BlockSlab) || !PlayerUtil.aEg.thePlayer.onGround)) {
                        double d = Math.abs(PlayerUtil.aEg.thePlayer.posY - (double)Math.round(PlayerUtil.aEg.thePlayer.posY)) - 0.03;
                    }
                    if (block.isReplaceable((World)PlayerUtil.aEg.theWorld, blockpos)) continue;
                    for (int b0 = -1; b0 <= 1; b0 = (int)((byte)(b0 + 2))) {
                        arraylist.add(new Vec3(PlayerUtil.aEg.thePlayer.posX + (double)j + (double)b0, PlayerUtil.aEg.thePlayer.posY + (double)k, PlayerUtil.aEg.thePlayer.posZ + (double)l));
                    }
                    for (int b1 = -1; b1 <= 1; b1 = (int)((byte)(b1 + 2))) {
                        arraylist.add(new Vec3(PlayerUtil.aEg.thePlayer.posX + (double)j, PlayerUtil.aEg.thePlayer.posY + (double)k + (double)b1, PlayerUtil.aEg.thePlayer.posZ + (double)l));
                    }
                    for (int b2 = -1; b2 <= 1; b2 = (int)((byte)(b2 + 2))) {
                        arraylist.add(new Vec3(PlayerUtil.aEg.thePlayer.posX + (double)j, PlayerUtil.aEg.thePlayer.posY + (double)k, PlayerUtil.aEg.thePlayer.posZ + (double)l + (double)b2));
                    }
                }
            }
        }
        arraylist.removeIf(var0x -> PlayerUtil.aEg.thePlayer.getDistance(((Vec3)var0x).xCoord, ((Vec3)var0x).yCoord, ((Vec3)var0x).zCoord) > 5.0 || !PlayerUtil.o(((Vec3)var0x).xCoord, ((Vec3)var0x).yCoord, ((Vec3)var0x).zCoord).isReplaceable((World)PlayerUtil.aEg.theWorld, new BlockPos(((Vec3)var0x).xCoord, ((Vec3)var0x).yCoord, ((Vec3)var0x).zCoord)));
        if (arraylist.isEmpty()) {
            return null;
        }
        if (var6 != null) {
            arraylist.removeIf(var1 -> Math.floor(((Vec3)var1).yCoord + 1.0) != (double)var6.intValue());
        }
        arraylist.sort(Comparator.comparingDouble(var6x -> {
            double d1 = PlayerUtil.aEg.thePlayer.posX + var0 - ((Vec3)var6x).xCoord;
            double d2 = PlayerUtil.aEg.thePlayer.posY - 1.0 + var2 - ((Vec3)var6x).yCoord;
            double d3 = PlayerUtil.aEg.thePlayer.posZ + var4 - ((Vec3)var6x).zCoord;
            return MathHelper.sqrt_double((double)(d1 * d1 + d2 * d2 + d3 * d3));
        }));
        return arraylist.isEmpty() ? null : (Vec3)arraylist.getFirst();
    }

    public static EntityOtherPlayerMP c(EntityOtherPlayerMP other) {
        EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(other.getEntityWorld(), other.getGameProfile());
        entityotherplayermp.motionX = other.motionX;
        entityotherplayermp.motionY = other.motionY;
        entityotherplayermp.motionZ = other.motionZ;
        entityotherplayermp.pl = other.pl;
        entityotherplayermp.setEntityId(other.getEntityId());
        entityotherplayermp.lastTickPosX = other.lastTickPosX;
        entityotherplayermp.lastTickPosY = other.lastTickPosY;
        entityotherplayermp.lastTickPosZ = other.lastTickPosZ;
        entityotherplayermp.setPosition(other.posX, other.posY, other.posZ);
        return entityotherplayermp;
    }

    public static double v(Entity entity) {
        if (entity != null && PlayerUtil.aEg.thePlayer != null) {
            AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
            if (axisalignedbb != null && !axisalignedbb.hasNaN() && PlayerUtil.w(entity)) {
                Vec3 vec3 = PlayerUtil.aEg.thePlayer.getPositionEyes(1.0f);
                if (PlayerUtil.af(vec3.xCoord) && PlayerUtil.af(vec3.yCoord) && PlayerUtil.af(vec3.zCoord)) {
                    Vector2f vector2f = RotationUtil.y((Entity)entity);
                    if (vector2f != null && PlayerUtil.af(vector2f.getX()) && PlayerUtil.af(vector2f.getY())) {
                        Vec3 vec = PlayerUtil.aEg.thePlayer.getVectorForRotation(vector2f.getY(), vector2f.getX());
                        if (PlayerUtil.af(vec.xCoord) && PlayerUtil.af(vec.yCoord) && PlayerUtil.af(vec.zCoord)) {
                            MovingObjectPosition movingobjectposition = axisalignedbb.expand(0.1, 0.1, 0.1).calculateIntercept(vec3, vec3.addVector(vec.xCoord * 1000.0, vec.yCoord * 1000.0, vec.zCoord * 1000.0));
                            if (movingobjectposition != null && movingobjectposition.hitVec != null) {
                                double d0 = movingobjectposition.hitVec.distanceTo(vec3);
                                return PlayerUtil.af(d0) ? d0 : (double)PlayerUtil.aEg.thePlayer.getDistanceToEntity(entity);
                            }
                            return PlayerUtil.aEg.thePlayer.getDistanceToEntity(entity);
                        }
                        return PlayerUtil.aEg.thePlayer.getDistanceToEntity(entity);
                    }
                    return PlayerUtil.aEg.thePlayer.getDistanceToEntity(entity);
                }
                return PlayerUtil.aEg.thePlayer.getDistanceToEntity(entity);
            }
            return PlayerUtil.aEg.thePlayer.getDistanceToEntity(entity);
        }
        return Double.MAX_VALUE;
    }

    private static boolean w(Entity entity) {
        return PlayerUtil.af(entity.posX) && PlayerUtil.af(entity.posY) && PlayerUtil.af(entity.posZ);
    }

    private static boolean af(double var0) {
        return !Double.isNaN(var0) && !Double.isInfinite(var0);
    }

    @Generated
    private PlayerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
