/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreUpdateEvent
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  com.alan.clients.value.Mode
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  org.lwjgl.input.Keyboard
 */
package com.alan.clients.module.impl.player.scaffold.downwards;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.lwjgl.input.Keyboard;

public class WatchdogDownwards
extends Mode<Scaffold> {
    public static int fullBlockTicks;
    @EventLink(value=0)
    public final Listener<StrafeEvent> onStrafe = var0 -> {
        if (Keyboard.isKeyDown((int)WatchdogDownwards.aEg.gameSettings.keyBindSneak.getKeyCode())) {
            // empty if block
        }
    };
    @EventLink(value=3)
    public final Listener<PreUpdateEvent> onPreUpdate = var0 -> {
        if (!Keyboard.isKeyDown((int)WatchdogDownwards.aEg.gameSettings.keyBindSneak.getKeyCode()) || WatchdogDownwards.aEg.thePlayer.crH > 20) {
            // empty if block
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var0 -> {
        Packet packet = var0.dq();
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            // empty if block
        }
    };

    public WatchdogDownwards(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }

    public void onEnable() {
        fullBlockTicks = 0;
    }
}
