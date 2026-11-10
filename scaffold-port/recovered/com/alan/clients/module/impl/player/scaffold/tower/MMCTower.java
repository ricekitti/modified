/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  com.alan.clients.value.Mode
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.util.BlockPos
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class MMCTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var0 -> {
        Packet packet = var0.dq();
        if (MMCTower.aEg.gameSettings.keyBindJump.isKeyDown() && packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement)packet).getPosition().equals((Object)new BlockPos(MMCTower.aEg.thePlayer.posX, MMCTower.aEg.thePlayer.posY - 1.4, MMCTower.aEg.thePlayer.posZ))) {
            MMCTower.aEg.gameSettings.cgG.setPressed(false);
            MMCTower.aEg.thePlayer.setSprinting(false);
            MMCTower.aEg.thePlayer.motionY = 0.42f;
        }
    };

    public MMCTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}
