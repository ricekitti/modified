// Jade recovery: original class: jade.deps.eLz.UhFBqmumF
package dev.rice.jade.client.common;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockUtils implements IMinecraft {

   public static boolean isInteractiveBlock(Block var0) {
      return BlockTypes.DSLVc(var0);
   }

   public static Block iepjdt(BlockPos var0) {
      return WorldUtils.getBlock(mc.theWorld, var0);
   }

   public static AxisAlignedBB getSelectedBounds(BlockPos var0) {
      return WorldUtils.getSelectionBounds(mc.theWorld, var0);
   }

   public static boolean isReplaceableAt(BlockPos var0) {
      return PlaceUtils.isBlockReplaceable(mc, var0);
   }

   public static boolean canPlaceItemOnSide(ItemStack var0, BlockPos var1, EnumFacing var2) {
      return PlaceUtils.canPlaceItemOnSide(mc, var0, var1, var2);
   }
}
