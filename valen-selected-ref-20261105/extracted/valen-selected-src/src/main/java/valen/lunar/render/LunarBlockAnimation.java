/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.ItemRenderer
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.EnumAction
 *  net.minecraft.item.ItemStack
 */
package valen.lunar.render;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import valen.lunar.mixin.ItemRendererAccess;
import valen.mixin.interfaces.IMixinItemRenderer;

public final class LunarBlockAnimation {
    private static final ThreadLocal<EntityPlayer> forcedPlayer = new ThreadLocal();

    private LunarBlockAnimation() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(ItemRenderer itemRenderer, EntityPlayer entityPlayer, float f) {
        ItemStack itemStack;
        EntityPlayer entityPlayer2 = forcedPlayer.get();
        ItemStack itemStack2 = ((ItemRendererAccess)itemRenderer).valen$lunar$getItemToRender();
        ItemStack itemStack3 = itemStack = entityPlayer == null ? null : entityPlayer.getHeldItem();
        if (entityPlayer != null && ((IMixinItemRenderer)itemRenderer).isRenderItemInUse() && itemStack2 != null && itemStack2.getItemUseAction() == EnumAction.BLOCK && itemStack != null && itemStack.getItemUseAction() == EnumAction.BLOCK) {
            forcedPlayer.set(entityPlayer);
        } else {
            forcedPlayer.remove();
        }
        try {
            itemRenderer.renderItemInFirstPerson(f);
        }
        finally {
            if (entityPlayer2 == null) {
                forcedPlayer.remove();
            } else {
                forcedPlayer.set(entityPlayer2);
            }
        }
    }

    public static int itemUseCount(EntityPlayer entityPlayer, int n) {
        return n <= 0 && entityPlayer != null && forcedPlayer.get() == entityPlayer ? 1 : n;
    }
}

