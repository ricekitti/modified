// Jade recovery: original class: jade.deps.eLz.rmxXxM
package dev.rice.jade.client.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockNote;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWorkbench;

public final class BlockTypes {
   private static final Class<?>[] INTERACTIVE_BLOCK_TYPES = new Class[]{
      BlockTrapDoor.class,
      BlockDoor.class,
      BlockContainer.class,
      BlockJukebox.class,
      BlockFenceGate.class,
      BlockChest.class,
      BlockEnderChest.class,
      BlockEnchantmentTable.class,
      BlockBrewingStand.class,
      BlockBed.class,
      BlockDropper.class,
      BlockDispenser.class,
      BlockHopper.class,
      BlockAnvil.class,
      BlockNote.class,
      BlockWorkbench.class
   };

   public static boolean DSLVc(Block var0) {
      return hczi(var0, INTERACTIVE_BLOCK_TYPES);
   }

   private static boolean hczi(Block var0, Class<?>[] var1) {
      for (Class var5 : var1) {
         if (var5.isInstance(var0)) {
            return true;
         }
      }

      return false;
   }
}
