package dev.rice.mayu;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

public final class PlayerUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    public static boolean isUseItemKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindUseItem.getKeyCode());
    }

    private static boolean isGameplayKeyDown(int keyCode) {
        return PlayerUtils.minecraft.currentScreen == null && Compatibility.isKeyDown(keyCode);
    }

    public static boolean isOffsetCollisionFree(double offsetX, double offsetY, double offsetZ) {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ);
        return PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }

    public static boolean hasCollisionBelow() {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(0.0, -1.0, 0.0);
        return !PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }

    public static boolean hasCollisionAbove() {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);
        return !PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }
}
