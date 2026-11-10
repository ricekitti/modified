/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.world.World
 */
package valen.utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.world.World;
import valen.event.DispatchPacketEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.utility.IMinecraftInstance;
import valen.utility.Utils;

public final class RescueItemUse
implements IMinecraftInstance {
    private final Item item;
    private boolean sent;

    private RescueItemUse(Item item) {
        this.item = item;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean use(int n) {
        if (!Utils.nullCheck() || RescueItemUse.mc.playerController == null || n < 0 || n > 8) {
            return false;
        }
        ItemStack itemStack = RescueItemUse.mc.thePlayer.inventory.mainInventory[n];
        if (itemStack == null || itemStack.stackSize <= 0) {
            return false;
        }
        RescueItemUse rescueItemUse = new RescueItemUse(itemStack.getItem());
        MinecraftForge.EVENT_BUS.register(rescueItemUse);
        try {
            RescueItemUse.mc.thePlayer.inventory.currentItem = n;
            RescueItemUse.mc.playerController.updateController();
            ItemStack itemStack2 = RescueItemUse.mc.thePlayer.getHeldItem();
            boolean bl = itemStack2 != null && RescueItemUse.mc.playerController.sendUseItem((EntityPlayer)RescueItemUse.mc.thePlayer, (World)RescueItemUse.mc.theWorld, itemStack2);
            boolean bl2 = bl || rescueItemUse.sent;
            return bl2;
        }
        finally {
            MinecraftForge.EVENT_BUS.unregister(rescueItemUse);
        }
    }

    public static void restore(int n) {
        if (RescueItemUse.mc.thePlayer != null && RescueItemUse.mc.playerController != null && n >= 0 && n < 9) {
            RescueItemUse.mc.thePlayer.inventory.currentItem = n;
            RescueItemUse.mc.playerController.updateController();
        }
    }

    @SubscribeEvent
    public void onDispatch(DispatchPacketEvent dispatchPacketEvent) {
        ItemStack itemStack;
        if (dispatchPacketEvent.getPacket() instanceof C08PacketPlayerBlockPlacement && (itemStack = ((C08PacketPlayerBlockPlacement)dispatchPacketEvent.getPacket()).getStack()) != null && itemStack.getItem() == this.item) {
            this.sent = true;
        }
    }
}

