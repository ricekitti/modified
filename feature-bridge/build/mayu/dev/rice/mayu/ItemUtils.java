package dev.rice.mayu;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

public final class ItemUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    public static boolean isPlaceableBlockStack(ItemStack stack) {
        return stack != null && stack.stackSize >= 1 && stack.getItem() instanceof ItemBlock && ItemUtils.isSuitablePlacementBlock((ItemBlock)stack.getItem());
    }

    public static boolean isSuitablePlacementBlock(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        return !BlockUtils.isInteractable(block) && BlockUtils.isSuitableFullBlock(block);
    }

    public static boolean isHoldingPlaceableBlock() {
        return ItemUtils.isPlaceableBlockStack(ItemUtils.minecraft.thePlayer.getHeldItem());
    }
}
