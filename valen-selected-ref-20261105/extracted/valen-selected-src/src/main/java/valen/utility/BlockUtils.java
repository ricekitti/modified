/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockAnvil
 *  net.minecraft.block.BlockBasePressurePlate
 *  net.minecraft.block.BlockBed
 *  net.minecraft.block.BlockBrewingStand
 *  net.minecraft.block.BlockChest
 *  net.minecraft.block.BlockColored
 *  net.minecraft.block.BlockContainer
 *  net.minecraft.block.BlockDispenser
 *  net.minecraft.block.BlockDoor
 *  net.minecraft.block.BlockDropper
 *  net.minecraft.block.BlockEnchantmentTable
 *  net.minecraft.block.BlockEnderChest
 *  net.minecraft.block.BlockFence
 *  net.minecraft.block.BlockFenceGate
 *  net.minecraft.block.BlockFlowerPot
 *  net.minecraft.block.BlockHopper
 *  net.minecraft.block.BlockJukebox
 *  net.minecraft.block.BlockLadder
 *  net.minecraft.block.BlockNote
 *  net.minecraft.block.BlockTrapDoor
 *  net.minecraft.block.BlockWorkbench
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.EnumDyeColor
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.item.ItemStack
 *  net.minecraft.potion.Potion
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.World
 *  valen.utility.BlockUtils$1
 */
package valen.utility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockNote;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import valen.utility.BlockUtils;
import valen.utility.IMinecraftInstance;
import valen.utility.Utils;

public class BlockUtils
implements IMinecraftInstance {
    public static boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ();
    }

    public static boolean notFull(Block block) {
        return block instanceof BlockFenceGate || block instanceof BlockLadder || block instanceof BlockFlowerPot || block instanceof BlockBasePressurePlate || BlockUtils.isFluid(block) || block instanceof BlockFence || block instanceof BlockAnvil || block instanceof BlockEnchantmentTable || block instanceof BlockChest;
    }

    public static boolean isNormalBlock(Block block) {
        return block == Blocks.glass || block.isFullBlock() && block != Blocks.gravel && block != Blocks.sand && block != Blocks.soul_sand && block != Blocks.tnt && block != Blocks.crafting_table && block != Blocks.furnace && block != Blocks.dispenser && block != Blocks.dropper && block != Blocks.noteblock && block != Blocks.command_block;
    }

    public static BlockPos pos(double d, double d2, double d3) {
        return new BlockPos(d, d2, d3);
    }

    public static boolean isBlockPosEqual(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ();
    }

    public static BlockPos offsetPos(MovingObjectPosition movingObjectPosition) {
        return movingObjectPosition.getBlockPos().offset(movingObjectPosition.sideHit);
    }

    public static boolean isFluid(Block block) {
        return block.getMaterial() == Material.lava || block.getMaterial() == Material.water;
    }

    public static boolean isInteractable(Block block) {
        return block instanceof BlockTrapDoor || block instanceof BlockDoor || block instanceof BlockContainer || block instanceof BlockJukebox || block instanceof BlockFenceGate || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockEnchantmentTable || block instanceof BlockBrewingStand || block instanceof BlockBed || block instanceof BlockDropper || block instanceof BlockDispenser || block instanceof BlockHopper || block instanceof BlockAnvil || block instanceof BlockNote || block instanceof BlockWorkbench;
    }

    public static boolean isInteractable(MovingObjectPosition movingObjectPosition) {
        if (movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || movingObjectPosition.getBlockPos() == null) {
            return false;
        }
        if (!BlockUtils.mc.thePlayer.isSneaking() || BlockUtils.mc.thePlayer.getHeldItem() == null) {
            return BlockUtils.isInteractable(BlockUtils.getBlock(movingObjectPosition.getBlockPos()));
        }
        return false;
    }

    public static float getBlockHardness(Block block, ItemStack itemStack, boolean bl, boolean bl2) {
        float f = block.getBlockHardness((World)BlockUtils.mc.theWorld, null);
        if (f < 0.0f) {
            return 0.0f;
        }
        return block.getMaterial().isToolNotRequired() || itemStack != null && itemStack.canHarvestBlock(block) ? BlockUtils.getToolDigEfficiency(itemStack, block, bl, bl2) / f / 30.0f : BlockUtils.getToolDigEfficiency(itemStack, block, bl, bl2) / f / 100.0f;
    }

    public static float maxDigRateAcrossSlots(Block block, int n) {
        if (BlockUtils.mc.thePlayer == null || n <= 0) {
            return 0.0f;
        }
        int n2 = Math.min(n, BlockUtils.mc.thePlayer.inventory.getSizeInventory());
        float f = 0.0f;
        for (int i = 0; i < n2; ++i) {
            float f2 = BlockUtils.getBlockHardness(block, BlockUtils.mc.thePlayer.inventory.getStackInSlot(i), false, false);
            if (!(f2 > f)) continue;
            f = f2;
        }
        return f;
    }

    public static float getToolDigEfficiency(ItemStack itemStack, Block block, boolean bl, boolean bl2) {
        int n;
        float f;
        float f2 = f = itemStack == null ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (f > 1.0f && (n = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.efficiency.effectId, (ItemStack)itemStack)) > 0 && itemStack != null) {
            f += (float)(n * n + 1);
        }
        if (BlockUtils.mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            f *= 1.0f + (float)(BlockUtils.mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!bl) {
            if (BlockUtils.mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float f3;
                switch (BlockUtils.mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        f3 = 0.3f;
                        break;
                    }
                    case 1: {
                        f3 = 0.09f;
                        break;
                    }
                    case 2: {
                        f3 = 0.0027f;
                        break;
                    }
                    default: {
                        f3 = 8.1E-4f;
                    }
                }
                f *= f3;
            }
            if (BlockUtils.mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier((EntityLivingBase)BlockUtils.mc.thePlayer)) {
                f /= 5.0f;
            }
            if (!BlockUtils.mc.thePlayer.onGround && !bl2) {
                f /= 5.0f;
            }
        }
        return f;
    }

    public static Block getBlock(BlockPos blockPos) {
        return BlockUtils.getBlockState(blockPos).getBlock();
    }

    public static Block getBlock(double d, double d2, double d3) {
        return BlockUtils.getBlockState(new BlockPos(d, d2, d3)).getBlock();
    }

    public static Block getBlock(Vec3 vec3) {
        return BlockUtils.getBlockState(new BlockPos(vec3.xCoord, vec3.yCoord, vec3.zCoord)).getBlock();
    }

    public static IBlockState getBlockState(BlockPos blockPos) {
        if (BlockUtils.mc.theWorld == null || blockPos == null) {
            return Blocks.air.getDefaultState();
        }
        return BlockUtils.mc.theWorld.getBlockState(blockPos);
    }

    public static AxisAlignedBB getBlockSelectionBox(BlockPos blockPos) {
        if (BlockUtils.mc.theWorld == null || blockPos == null) {
            return null;
        }
        IBlockState iBlockState = BlockUtils.mc.theWorld.getBlockState(blockPos);
        Block block = iBlockState.getBlock();
        block.setBlockBoundsBasedOnState((IBlockAccess)BlockUtils.mc.theWorld, blockPos);
        AxisAlignedBB axisAlignedBB = block.getSelectedBoundingBox((World)BlockUtils.mc.theWorld, blockPos);
        if (axisAlignedBB == null) {
            axisAlignedBB = new AxisAlignedBB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)blockPos.getX() + 1.0, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 1.0);
        }
        return axisAlignedBB;
    }

    public static AxisAlignedBB getCollisionOrSelectionBox(BlockPos blockPos) {
        if (BlockUtils.mc.theWorld == null || blockPos == null) {
            return null;
        }
        IBlockState iBlockState = BlockUtils.mc.theWorld.getBlockState(blockPos);
        Block block = iBlockState.getBlock();
        AxisAlignedBB axisAlignedBB = block.getCollisionBoundingBox((World)BlockUtils.mc.theWorld, blockPos, iBlockState);
        if (axisAlignedBB == null) {
            axisAlignedBB = block.getSelectedBoundingBox((World)BlockUtils.mc.theWorld, blockPos);
        }
        if (axisAlignedBB == null) {
            axisAlignedBB = new AxisAlignedBB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)blockPos.getX() + 1.0, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 1.0);
        }
        return axisAlignedBB;
    }

    public static AxisAlignedBB getCollisionOrSelectedOnly(BlockPos blockPos) {
        if (BlockUtils.mc.theWorld == null || blockPos == null) {
            return null;
        }
        IBlockState iBlockState = BlockUtils.mc.theWorld.getBlockState(blockPos);
        Block block = iBlockState.getBlock();
        AxisAlignedBB axisAlignedBB = block.getCollisionBoundingBox((World)BlockUtils.mc.theWorld, blockPos, iBlockState);
        if (axisAlignedBB == null) {
            axisAlignedBB = block.getSelectedBoundingBox((World)BlockUtils.mc.theWorld, blockPos);
        }
        return axisAlignedBB;
    }

    public static AxisAlignedBB unionBlockBounds(BlockPos blockPos, BlockPos blockPos2) {
        AxisAlignedBB axisAlignedBB = BlockUtils.getCollisionOrSelectionBox(blockPos);
        AxisAlignedBB axisAlignedBB2 = BlockUtils.getCollisionOrSelectionBox(blockPos2);
        return axisAlignedBB.union(axisAlignedBB2);
    }

    public static EnumFacing facingFromBlockCenterToPoint(BlockPos blockPos, Vec3 vec3) {
        double d = vec3.xCoord - ((double)blockPos.getX() + 0.5);
        double d2 = vec3.yCoord - ((double)blockPos.getY() + 0.5);
        double d3 = vec3.zCoord - ((double)blockPos.getZ() + 0.5);
        double d4 = Math.abs(d);
        double d5 = Math.abs(d2);
        double d6 = Math.abs(d3);
        if (d4 > d5 && d4 > d6) {
            return d > 0.0 ? EnumFacing.EAST : EnumFacing.WEST;
        }
        if (d5 > d6) {
            return d2 > 0.0 ? EnumFacing.UP : EnumFacing.DOWN;
        }
        return d3 > 0.0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
    }

    public static boolean check(BlockPos blockPos, Block block) {
        return BlockUtils.getBlock(blockPos) == block;
    }

    public static boolean replaceable(BlockPos blockPos) {
        if (!Utils.nullCheck()) {
            return true;
        }
        return BlockUtils.getBlock(blockPos).isReplaceable((World)BlockUtils.mc.theWorld, blockPos);
    }

    public static boolean canSeeVecBlock(BlockPos blockPos, Vec3 vec3, Vec3 vec32) {
        BlockPos blockPos2;
        MovingObjectPosition movingObjectPosition = BlockUtils.mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, false);
        if (movingObjectPosition == null) {
            return true;
        }
        return movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && (blockPos2 = movingObjectPosition.getBlockPos()).getX() == blockPos.getX() && blockPos2.getY() == blockPos.getY() && blockPos2.getZ() == blockPos.getZ();
    }

    public static boolean canBlockBeSeen(BlockPos blockPos) {
        Vec3 vec3 = new Vec3(BlockUtils.mc.thePlayer.posX, BlockUtils.mc.thePlayer.posY + (double)BlockUtils.mc.thePlayer.getEyeHeight(), BlockUtils.mc.thePlayer.posZ);
        for (double d = 0.0; d <= 0.5; d += 0.5) {
            double d2 = (double)blockPos.getY() + d;
            Vec3 vec32 = new Vec3((double)(blockPos.getX() + 1), d2, (double)blockPos.getZ() + 0.5);
            if (BlockUtils.canSeeVecBlock(blockPos, vec3, vec32)) {
                return true;
            }
            vec32 = new Vec3((double)blockPos.getX(), d2, (double)blockPos.getZ() + 0.5);
            if (BlockUtils.canSeeVecBlock(blockPos, vec3, vec32)) {
                return true;
            }
            vec32 = new Vec3((double)blockPos.getX() + 0.5, d2, (double)(blockPos.getZ() + 1));
            if (BlockUtils.canSeeVecBlock(blockPos, vec3, vec32)) {
                return true;
            }
            vec32 = new Vec3((double)blockPos.getX() + 0.5, d2, (double)blockPos.getZ());
            if (!BlockUtils.canSeeVecBlock(blockPos, vec3, vec32)) continue;
            return true;
        }
        return false;
    }

    public static EnumDyeColor getWoolColor(IBlockState iBlockState) {
        return (EnumDyeColor)iBlockState.getProperties().get((Object)BlockColored.COLOR);
    }

    public static EnumFacing[] getVisibleFaces(Vec3 vec3, BlockPos blockPos) {
        EnumFacing enumFacing = Math.abs(vec3.yCoord - (double)(blockPos.getY() + 1)) < Math.abs(vec3.yCoord - (double)blockPos.getY()) ? EnumFacing.UP : EnumFacing.DOWN;
        EnumFacing enumFacing2 = Math.abs(vec3.zCoord - (double)(blockPos.getZ() + 1)) < Math.abs(vec3.zCoord - (double)blockPos.getZ()) ? EnumFacing.SOUTH : EnumFacing.NORTH;
        EnumFacing enumFacing3 = Math.abs(vec3.xCoord - (double)(blockPos.getX() + 1)) < Math.abs(vec3.xCoord - (double)blockPos.getX()) ? EnumFacing.EAST : EnumFacing.WEST;
        return new EnumFacing[]{enumFacing, enumFacing2, enumFacing3};
    }

    public static boolean containsFace(EnumFacing[] enumFacingArray, EnumFacing enumFacing) {
        for (EnumFacing enumFacing2 : enumFacingArray) {
            if (enumFacing2 != enumFacing) continue;
            return true;
        }
        return false;
    }

    public static Vec3 getFaceCenter(BlockPos blockPos, EnumFacing enumFacing) {
        double d = 0.001;
        double d2 = (double)blockPos.getX() + 0.5;
        double d3 = (double)blockPos.getY() + 0.5;
        double d4 = (double)blockPos.getZ() + 0.5;
        switch (1.$SwitchMap$net$minecraft$util$EnumFacing[enumFacing.ordinal()]) {
            case 1: {
                return new Vec3(d2, (double)(blockPos.getY() + 1) - d, d4);
            }
            case 2: {
                return new Vec3(d2, (double)blockPos.getY() + d, d4);
            }
            case 3: {
                return new Vec3(d2, d3, (double)blockPos.getZ() + d);
            }
            case 4: {
                return new Vec3(d2, d3, (double)(blockPos.getZ() + 1) - d);
            }
            case 5: {
                return new Vec3((double)(blockPos.getX() + 1) - d, d3, d4);
            }
            case 6: {
                return new Vec3((double)blockPos.getX() + d, d3, d4);
            }
        }
        return new Vec3(d2, d3, d4);
    }

    public static double dist2PointAABB(Vec3 vec3, BlockPos blockPos) {
        double d = Math.max((double)blockPos.getX(), Math.min((double)(blockPos.getX() + 1), vec3.xCoord));
        double d2 = Math.max((double)blockPos.getY(), Math.min((double)(blockPos.getY() + 1), vec3.yCoord));
        double d3 = Math.max((double)blockPos.getZ(), Math.min((double)(blockPos.getZ() + 1), vec3.zCoord));
        double d4 = vec3.xCoord - d;
        double d5 = vec3.yCoord - d2;
        double d6 = vec3.zCoord - d3;
        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public static boolean canPlaceBlockOnSide(ItemStack itemStack, BlockPos blockPos, EnumFacing enumFacing) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemBlock)) {
            return false;
        }
        return ((ItemBlock)itemStack.getItem()).canPlaceBlockOnSide((World)BlockUtils.mc.theWorld, blockPos, enumFacing, (EntityPlayer)BlockUtils.mc.thePlayer, itemStack);
    }

    public static float getFistBreakTicks(Block block) {
        float f = block.getBlockHardness((World)BlockUtils.mc.theWorld, null);
        if (f < 0.0f) {
            return Float.MAX_VALUE;
        }
        if (f == 0.0f) {
            return 0.0f;
        }
        return f * (block.getMaterial().isToolNotRequired() ? 30.0f : 100.0f);
    }

    public static boolean hasAirNeighbor(BlockPos blockPos, BlockPos ... blockPosArray) {
        for (EnumFacing enumFacing : EnumFacing.values()) {
            BlockPos blockPos2 = blockPos.offset(enumFacing);
            if (BlockUtils.mc.theWorld.getBlockState(blockPos2).getBlock() != Blocks.air) continue;
            boolean bl = false;
            for (BlockPos blockPos3 : blockPosArray) {
                if (!blockPos2.equals((Object)blockPos3)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            return true;
        }
        return false;
    }

    public static boolean isAdjacentToBed(BlockPos blockPos) {
        for (EnumFacing enumFacing : EnumFacing.values()) {
            if (!(BlockUtils.getBlock(blockPos.offset(enumFacing)) instanceof BlockBed)) continue;
            return true;
        }
        return false;
    }

    public static MovingObjectPosition traverseBlocksAlongRay(Vec3 vec3, Vec3 vec32, boolean bl, boolean bl2) {
        if (BlockUtils.mc.theWorld == null) {
            return null;
        }
        if (Double.isNaN(vec3.xCoord) || Double.isNaN(vec3.yCoord) || Double.isNaN(vec3.zCoord)) {
            return null;
        }
        if (Double.isNaN(vec32.xCoord) || Double.isNaN(vec32.yCoord) || Double.isNaN(vec32.zCoord)) {
            return null;
        }
        int n = MathHelper.floor_double((double)vec32.xCoord);
        int n2 = MathHelper.floor_double((double)vec32.yCoord);
        int n3 = MathHelper.floor_double((double)vec32.zCoord);
        int n4 = MathHelper.floor_double((double)vec3.xCoord);
        int n5 = MathHelper.floor_double((double)vec3.yCoord);
        int n6 = MathHelper.floor_double((double)vec3.zCoord);
        MovingObjectPosition movingObjectPosition = null;
        MovingObjectPosition movingObjectPosition2 = BlockUtils.getBlockCollisionHit(n4, n5, n6, vec3, vec32);
        if (movingObjectPosition2 != null) {
            if (BlockUtils.isBedOrAdjacentMatch(movingObjectPosition2.getBlockPos(), bl, bl2)) {
                return movingObjectPosition2;
            }
            movingObjectPosition = movingObjectPosition2;
        }
        Vec3 vec33 = vec3;
        int n7 = 200;
        while (n7-- >= 0) {
            EnumFacing enumFacing;
            if (Double.isNaN(vec33.xCoord) || Double.isNaN(vec33.yCoord) || Double.isNaN(vec33.zCoord)) {
                return movingObjectPosition;
            }
            if (n4 == n && n5 == n2 && n6 == n3) {
                return movingObjectPosition;
            }
            boolean bl3 = true;
            boolean bl4 = true;
            boolean bl5 = true;
            double d = 999.0;
            double d2 = 999.0;
            double d3 = 999.0;
            if (n > n4) {
                d = (double)n4 + 1.0;
            } else if (n < n4) {
                d = n4;
            } else {
                bl3 = false;
            }
            if (n2 > n5) {
                d2 = (double)n5 + 1.0;
            } else if (n2 < n5) {
                d2 = n5;
            } else {
                bl4 = false;
            }
            if (n3 > n6) {
                d3 = (double)n6 + 1.0;
            } else if (n3 < n6) {
                d3 = n6;
            } else {
                bl5 = false;
            }
            double d4 = vec32.xCoord - vec33.xCoord;
            double d5 = vec32.yCoord - vec33.yCoord;
            double d6 = vec32.zCoord - vec33.zCoord;
            double d7 = 999.0;
            double d8 = 999.0;
            double d9 = 999.0;
            if (bl3) {
                d7 = (d - vec33.xCoord) / d4;
            }
            if (bl4) {
                d8 = (d2 - vec33.yCoord) / d5;
            }
            if (bl5) {
                d9 = (d3 - vec33.zCoord) / d6;
            }
            if (d7 == -0.0) {
                d7 = -1.0E-4;
            }
            if (d8 == -0.0) {
                d8 = -1.0E-4;
            }
            if (d9 == -0.0) {
                d9 = -1.0E-4;
            }
            if (d7 < d8 && d7 < d9) {
                enumFacing = n > n4 ? EnumFacing.WEST : EnumFacing.EAST;
                vec33 = new Vec3(d, vec33.yCoord + d5 * d7, vec33.zCoord + d6 * d7);
            } else if (d8 < d9) {
                enumFacing = n2 > n5 ? EnumFacing.DOWN : EnumFacing.UP;
                vec33 = new Vec3(vec33.xCoord + d4 * d8, d2, vec33.zCoord + d6 * d8);
            } else {
                enumFacing = n3 > n6 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                vec33 = new Vec3(vec33.xCoord + d4 * d9, vec33.yCoord + d5 * d9, d3);
            }
            if ((movingObjectPosition2 = BlockUtils.getBlockCollisionHit(n4 = MathHelper.floor_double((double)vec33.xCoord) - (enumFacing == EnumFacing.EAST ? 1 : 0), n5 = MathHelper.floor_double((double)vec33.yCoord) - (enumFacing == EnumFacing.UP ? 1 : 0), n6 = MathHelper.floor_double((double)vec33.zCoord) - (enumFacing == EnumFacing.SOUTH ? 1 : 0), vec3, vec32)) == null) continue;
            if (BlockUtils.isBedOrAdjacentMatch(movingObjectPosition2.getBlockPos(), bl, bl2)) {
                return movingObjectPosition2;
            }
            if (movingObjectPosition != null) continue;
            movingObjectPosition = movingObjectPosition2;
        }
        return movingObjectPosition;
    }

    private static MovingObjectPosition getBlockCollisionHit(int n, int n2, int n3, Vec3 vec3, Vec3 vec32) {
        BlockPos blockPos = new BlockPos(n, n2, n3);
        IBlockState iBlockState = BlockUtils.mc.theWorld.getBlockState(blockPos);
        Block block = iBlockState.getBlock();
        if (!block.canCollideCheck(iBlockState, false)) {
            return null;
        }
        return block.collisionRayTrace((World)BlockUtils.mc.theWorld, blockPos, vec3, vec32);
    }

    private static boolean isBedOrAdjacentMatch(BlockPos blockPos, boolean bl, boolean bl2) {
        Block block = BlockUtils.getBlock(blockPos);
        boolean bl3 = block instanceof BlockBed;
        if (bl && bl3) {
            return true;
        }
        return bl2 && !bl3 && BlockUtils.isAdjacentToBed(blockPos);
    }
}

