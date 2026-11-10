/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.value.Mode
 *  net.minecraft.potion.Potion
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.value.Mode;
import net.minecraft.potion.Potion;

public class WatchdogSprint
extends Mode<Scaffold> {
    private int blocks;
    @EventLink(value=0)
    public final Listener<StrafeEvent> onStrafe = var1x -> {
        double d0;
        ++this.blocks;
        WatchdogSprint.aEg.gameSettings.cgG.setPressed(true);
        WatchdogSprint.aEg.thePlayer.setSprinting(true);
        double d = d0 = WatchdogSprint.aEg.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.118 : 0.083;
        if (WatchdogSprint.aEg.thePlayer.onGround) {
            MoveUtil.strafe((double)(d0 - (WatchdogSprint.aEg.thePlayer.Zl <= 40 ? 0.1 : 0.0) - Math.random() * 1.0E-4));
        }
        if (MoveUtil.speed() >= d0 && !WatchdogSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
            MoveUtil.moveFlying((double)((MoveUtil.speed() - d0) * -1.0));
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var0 -> {};

    public WatchdogSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }

    public void onDisable() {
        WatchdogSprint.aEg.thePlayer.capabilities.isFlying = false;
        WatchdogSprint.aEg.timer.dzD = 1.0f;
    }
}
