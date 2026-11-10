/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  com.alan.clients.util.packet.PacketUtil
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.value.Mode
 *  com.alan.clients.value.impl.NumberValue
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.client.C0BPacketEntityAction
 *  net.minecraft.network.play.client.C0BPacketEntityAction$Action
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class VulcanSprint
extends Mode<Scaffold> {
    private final NumberValue speed = new NumberValue("Speed", (Mode)this, (Number)1.3, (Number)0.9, (Number)1.3, (Number)0.05);
    private int time;
    private int block;
    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = var1x -> {
        VulcanSprint.aEg.thePlayer.setSprinting(false);
        double d0 = ((Number)this.speed.wo()).doubleValue();
        if (!VulcanSprint.aEg.gameSettings.keyBindJump.isKeyDown() && d0 > 0.9 && VulcanSprint.aEg.thePlayer.cqL >= 2 && this.block <= 10) {
            MoveUtil.strafe((double)(MoveUtil.getAllowedHorizontalDistance() * d0));
            VulcanSprint.aEg.thePlayer.jump();
            VulcanSprint.aEg.thePlayer.motionY = 0.012500047683714;
        }
        if (VulcanSprint.aEg.thePlayer.onGround) {
            MoveUtil.strafe();
        }
        ++this.time;
        ++this.block;
        switch (this.time) {
            case 1: {
                PacketUtil.send((Packet)new C0BPacketEntityAction((Entity)VulcanSprint.aEg.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                break;
            }
            case 10: {
                this.time = 0;
            }
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var1x -> {
        Packet packet = var1x.dq();
        if (packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement)packet).getPlacedBlockDirection() != 255) {
            this.block = 0;
        }
    };

    public VulcanSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }

    public void onDisable() {
        if (this.time == 1) {
            PacketUtil.send((Packet)new C0BPacketEntityAction((Entity)VulcanSprint.aEg.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }
    }
}
