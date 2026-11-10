/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.EnumAction
 *  net.minecraft.item.ItemStack
 */
package valen.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import valen.mixin.interfaces.IMixinItemRenderer;

public final class BlockAnimationUtils {
    private static EntityPlayer renderingPlayer;
    private static int renderingDepth;

    private BlockAnimationUtils() {
    }

    public static void beginRender(EntityPlayer entityPlayer) {
        if (entityPlayer == Minecraft.getMinecraft().thePlayer && renderingDepth++ == 0) {
            renderingPlayer = entityPlayer;
        }
    }

    public static void endRender(EntityPlayer entityPlayer) {
        if (entityPlayer != renderingPlayer || renderingDepth <= 0) {
            return;
        }
        if (--renderingDepth == 0) {
            renderingPlayer = null;
        }
    }

    public static boolean shouldForceBlockAnimation(EntityPlayer entityPlayer, ItemStack itemStack) {
        if (entityPlayer == null || entityPlayer != renderingPlayer || itemStack == null || itemStack.getItemUseAction() != EnumAction.BLOCK) {
            return false;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.getItemRenderer() != null && ((IMixinItemRenderer)minecraft.getItemRenderer()).isRenderItemInUse();
    }
}

