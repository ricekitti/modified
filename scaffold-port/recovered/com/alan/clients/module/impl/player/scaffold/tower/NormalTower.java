/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
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
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class NormalTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = var0 -> {
        if (NormalTower.aEg.gameSettings.keyBindJump.isKeyDown() && NormalTower.aEg.thePlayer.onGround) {
            NormalTower.aEg.thePlayer.motionY = 0.42f;
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var0 -> {
        Packet packet = var0.dq();
        if (NormalTower.aEg.thePlayer.motionY > -0.0784000015258789 && packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement)packet).getPosition().equals((Object)new BlockPos(NormalTower.aEg.thePlayer.posX, Math.floor(NormalTower.aEg.thePlayer.posY) - 2.0, NormalTower.aEg.thePlayer.posZ))) {
            NormalTower.aEg.thePlayer.motionY = -0.0784000015258789;
        }
    };

    public NormalTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}
