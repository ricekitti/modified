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
 *  com.alan.clients.util.vector.Vector3d
 *  com.alan.clients.value.Mode
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.util.EnumFacing
 *  org.lwjgl.input.Keyboard
 */
package com.alan.clients.module.impl.player.scaffold.downwards;

import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

public class VerusDownwards
extends Mode<Scaffold> {
    int sneakTicks;
    @EventLink(value=0)
    public final Listener<StrafeEvent> onStrafe = var0 -> {
        if (Keyboard.isKeyDown((int)VerusDownwards.aEg.gameSettings.keyBindSneak.getKeyCode())) {
            VerusDownwards.aEg.gameSettings.cgG.setPressed(false);
            VerusDownwards.aEg.thePlayer.setSprinting(false);
        }
    };
    @EventLink(value=3)
    public final Listener<PreUpdateEvent> onPreUpdate = var1x -> {
        if (Keyboard.isKeyDown((int)VerusDownwards.aEg.gameSettings.keyBindSneak.getKeyCode()) && VerusDownwards.aEg.thePlayer.crH <= 20) {
            ++this.sneakTicks;
            if (VerusDownwards.aEg.thePlayer.posY % 1.0 != 0.0) {
                this.sneakTicks = 0;
            }
            if (VerusDownwards.aEg.thePlayer.motionY <= 0.0 && this.sneakTicks >= 15) {
                ((Scaffold)this.getParent()).placeOffset = ((Scaffold)this.getParent()).placeOffset.v(0.0, -1.0, 0.0);
            }
        } else {
            this.sneakTicks = 1000;
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var1x -> {
        C08PacketPlayerBlockPlacement c08packetplayerblockplacement;
        Packet patt0$temp = var1x.dq();
        if (patt0$temp instanceof C08PacketPlayerBlockPlacement && !(c08packetplayerblockplacement = (C08PacketPlayerBlockPlacement)patt0$temp).getPosition().h(new Vector3d(-1.0, -1.0, -1.0))) {
            c08packetplayerblockplacement.getPlacedBlockDirection();
            EnumFacing.DOWN.getIndex();
        }
    };

    public VerusDownwards(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }

    public void onEnable() {
        this.sneakTicks = 1000;
    }
}
