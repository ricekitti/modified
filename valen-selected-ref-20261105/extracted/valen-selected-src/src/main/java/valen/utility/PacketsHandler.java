/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.player.InventoryPlayer
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C09PacketHeldItemChange
 *  net.minecraft.network.play.server.S09PacketHeldItemChange
 *  net.minecraft.network.play.server.S0CPacketSpawnPlayer
 */
package valen.utility;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import valen.event.ReceivePacketEvent;
import valen.event.SendPacketEvent;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.utility.IMinecraftInstance;

public class PacketsHandler
implements IMinecraftInstance {
    public AtomicInteger playerSlot = new AtomicInteger(-1);
    public AtomicInteger serverSlot = new AtomicInteger(-1);
    private final boolean handleSlots = true;

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent sendPacketEvent) {
        if (sendPacketEvent.isCanceled()) {
            return;
        }
        Packet<?> packet = sendPacketEvent.getPacket();
        if (packet instanceof C09PacketHeldItemChange) {
            C09PacketHeldItemChange c09PacketHeldItemChange = (C09PacketHeldItemChange)packet;
            int n = c09PacketHeldItemChange.getSlotId();
            this.playerSlot.set(n);
            this.serverSlot.set(n);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
        if (receivePacketEvent.getPacket() instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange s09PacketHeldItemChange = (S09PacketHeldItemChange)receivePacketEvent.getPacket();
            int n = s09PacketHeldItemChange.getHeldItemHotbarIndex();
            if (n >= 0 && n < InventoryPlayer.getHotbarSize()) {
                this.serverSlot.set(n);
            }
        } else if (receivePacketEvent.getPacket() instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
            S0CPacketSpawnPlayer s0CPacketSpawnPlayer = (S0CPacketSpawnPlayer)receivePacketEvent.getPacket();
            if (s0CPacketSpawnPlayer.getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                return;
            }
            this.playerSlot.set(-1);
        }
    }
}

