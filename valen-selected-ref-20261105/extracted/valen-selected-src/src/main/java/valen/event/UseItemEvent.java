/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 */
package valen.event;

import net.minecraft.item.ItemStack;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class UseItemEvent
extends Event {
    public ItemStack usedItemStack;

    public UseItemEvent(ItemStack itemStack) {
        this.usedItemStack = itemStack;
    }
}

