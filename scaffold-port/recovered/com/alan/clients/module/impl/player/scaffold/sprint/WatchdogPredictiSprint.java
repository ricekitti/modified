/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.component.impl.player.RotationComponent
 *  com.alan.clients.component.impl.player.SlotComponent
 *  com.alan.clients.component.impl.player.rotationcomponent.MovementFix
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.input.MoveInputEvent
 *  com.alan.clients.newevent.impl.motion.PreUpdateEvent
 *  com.alan.clients.newevent.impl.packet.PacketReceiveEvent
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  com.alan.clients.util.chat.ChatUtil
 *  com.alan.clients.util.packet.PacketUtil
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.util.vector.Vector2f
 *  com.alan.clients.value.Mode
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  net.minecraft.network.play.client.C03PacketPlayer$C05PacketPlayerLook
 *  net.minecraft.network.play.client.C03PacketPlayer$C06PacketPlayerPosLook
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.server.S23PacketBlockChange
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class WatchdogPredictiSprint
extends Mode<Scaffold> {
    private float targetYaw;
    private boolean holdSneak;
    private boolean awaitingBlockChange;
    private BlockPos lastBlockPos;
    private int sneakUntilTick;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = var1x -> {
        MathHelper.wrapAngleTo180_float((float)this.targetYaw);
        RotationComponent.d((boolean)false);
        RotationComponent.setRotations((Vector2f)new Vector2f(this.targetYaw, 94.0f), (double)10.0, (MovementFix)MovementFix.NORMAL);
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var0 -> {
        C03PacketPlayer c03packetplayer;
        Packet patt0$temp = var0.dq();
        if (patt0$temp instanceof C03PacketPlayer && ((c03packetplayer = (C03PacketPlayer)patt0$temp) instanceof C03PacketPlayer.C05PacketPlayerLook || c03packetplayer instanceof C03PacketPlayer.C06PacketPlayerPosLook) && Math.abs(c03packetplayer.getPitch() - 90.0f) < 0.001f) {
            c03packetplayer.setPitch(94.0f);
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = var1x -> {
        Packet packet = var1x.getPacket();
        if (WatchdogPredictiSprint.aEg.thePlayer.ticksExisted >= this.sneakUntilTick) {
            this.holdSneak = false;
        }
        if (packet instanceof S23PacketBlockChange) {
            BlockPos blockpos = ((S23PacketBlockChange)packet).getBlockPosition();
            if (!blockpos.equals((Object)this.lastBlockPos)) {
                this.lastBlockPos = blockpos;
                this.sneakUntilTick = WatchdogPredictiSprint.aEg.thePlayer.ticksExisted + 10;
            }
            if (this.awaitingBlockChange) {
                this.holdSneak = true;
                this.awaitingBlockChange = false;
            }
        }
    };
    @EventLink
    public final Listener<MoveInputEvent> onMoveInput = var1x -> {
        if (this.holdSneak) {
            var1x.setSneak(true);
        }
        if (this.holdSneak) {
            var1x.setForward(0.0f);
        }
    };

    public WatchdogPredictiSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }

    public void onEnable() {
        this.sneakUntilTick = WatchdogPredictiSprint.aEg.thePlayer.ticksExisted + 10;
        this.awaitingBlockChange = true;
        this.holdSneak = true;
        SlotComponent slotcomponent = (SlotComponent)this.d(SlotComponent.class);
        PacketUtil.send((Packet)new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
        slotcomponent = (SlotComponent)this.d(SlotComponent.class);
        PacketUtil.send((Packet)new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
        slotcomponent = (SlotComponent)this.d(SlotComponent.class);
        PacketUtil.send((Packet)new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
        MathHelper.wrapAngleTo180_float((float)this.targetYaw);
        RotationComponent.d((boolean)false);
        double d0 = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(MoveUtil.direction()));
        MathHelper.wrapAngleTo180_double((double)(Math.toDegrees(Math.atan2(WatchdogPredictiSprint.aEg.thePlayer.motionZ, WatchdogPredictiSprint.aEg.thePlayer.motionX)) - 90.0));
        boolean flag = Math.abs(d0 % 90.0) <= 10.0 || Math.abs(d0 % 90.0) >= 80.0;
        double d1 = (double)Math.round(d0 / 45.0) * 45.0;
        WatchdogPredictiSprint.aEg.thePlayer.pl = (float)d1;
        ChatUtil.c((Object)d0, (Object[])new Object[0]);
        this.targetYaw = flag && d0 >= -10.0 && d0 <= 100.0 && WatchdogPredictiSprint.aEg.thePlayer.onGround ? WatchdogPredictiSprint.aEg.thePlayer.pl + 43.0f : (flag && WatchdogPredictiSprint.aEg.thePlayer.onGround ? WatchdogPredictiSprint.aEg.thePlayer.pl - 43.0f : WatchdogPredictiSprint.aEg.thePlayer.pl);
    }
}
