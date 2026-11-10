/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.util.packet.PacketUtil
 *  com.alan.clients.util.player.PlayerUtil
 *  com.alan.clients.value.Mode
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class NCPTower
extends Mode<Scaffold> {
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = var0 -> {
        if (NCPTower.aEg.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.ay((int)2)) {
            PacketUtil.sendNoEvent((Packet)new C08PacketPlayerBlockPlacement(null));
            if (NCPTower.aEg.thePlayer.posY % 1.0 <= 0.00153598) {
                NCPTower.aEg.thePlayer.setPosition(NCPTower.aEg.thePlayer.posX, Math.floor(NCPTower.aEg.thePlayer.posY), NCPTower.aEg.thePlayer.posZ);
                NCPTower.aEg.thePlayer.motionY = 0.42f;
            } else if (NCPTower.aEg.thePlayer.posY % 1.0 < 0.1 && NCPTower.aEg.thePlayer.tR != 0) {
                NCPTower.aEg.thePlayer.motionY = 0.0;
                NCPTower.aEg.thePlayer.setPosition(NCPTower.aEg.thePlayer.posX, Math.floor(NCPTower.aEg.thePlayer.posY), NCPTower.aEg.thePlayer.posZ);
            }
        }
    };

    public NCPTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }
}
