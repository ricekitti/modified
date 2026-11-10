/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderGlobal
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.MovingObjectPosition
 */
package valen.lunar.forge.client.event;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class DrawBlockHighlightEvent
extends Event {
    public final RenderGlobal context;
    public final EntityPlayer player;
    public final MovingObjectPosition target;
    public final int subID;
    public final ItemStack currentItem;
    public final float partialTicks;

    public DrawBlockHighlightEvent(RenderGlobal renderGlobal, EntityPlayer entityPlayer, MovingObjectPosition movingObjectPosition, int n, ItemStack itemStack, float f) {
        this.context = renderGlobal;
        this.player = entityPlayer;
        this.target = movingObjectPosition;
        this.subID = n;
        this.currentItem = itemStack;
        this.partialTicks = f;
    }
}

