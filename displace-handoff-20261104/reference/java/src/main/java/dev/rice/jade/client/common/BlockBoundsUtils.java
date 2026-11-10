// Jade recovery: original class: jade.deps.eLz.HfVLWb
package dev.rice.jade.client.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public final class BlockBoundsUtils {

   public static IBlockState getBlockStateSafe(World var0, BlockPos var1) {
      return var0 != null && var1 != null ? var0.getBlockState(var1) : Blocks.air.getDefaultState();
   }

   public static AxisAlignedBB sBdmu(World var0, BlockPos var1) {
      if (var0 != null && var1 != null) {
         IBlockState var2 = var0.getBlockState(var1);
         Block var3 = var2.getBlock();
         var3.setBlockBoundsBasedOnState(var0, var1);
         AxisAlignedBB var4 = var3.getSelectedBoundingBox(var0, var1);
         return var4 == null ? getFullCubeBounds(var1) : var4;
      } else {
         return null;
      }
   }

   private static AxisAlignedBB getFullCubeBounds(BlockPos var0) {
      return new AxisAlignedBB(var0.getX(), var0.getY(), var0.getZ(), var0.getX() + 1.0, var0.getY() + 1.0, var0.getZ() + 1.0);
   }
}
