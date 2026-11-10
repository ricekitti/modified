/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.component.Component
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  net.minecraft.network.play.client.C07PacketPlayerDigging
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.client.C0EPacketClickWindow
 *  net.minecraft.network.play.client.C16PacketClientStatus
 *  net.minecraft.network.play.client.C16PacketClientStatus$EnumState
 *  net.minecraft.network.play.client.l
 *  net.minecraft.network.play.client.m
 *  net.minecraft.network.play.client.q
 */
package com.alan.clients.component.impl.player;

import com.alan.clients.component.Component;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.l;
import net.minecraft.network.play.client.m;
import net.minecraft.network.play.client.q;

public final class BadPacketsComponent
extends Component {
    private static boolean cI;
    private static boolean cJ;
    private static boolean cK;
    private static boolean cL;
    private static boolean cM;
    private static boolean cN;
    @EventLink(value=4)
    public final Listener<PacketSendEvent> onPacketSend = var0 -> {
        Packet packet = var0.dq();
        if (packet instanceof l) {
            cI = true;
        } else if (packet instanceof m) {
            cK = true;
        } else if (packet instanceof C02PacketUseEntity) {
            cJ = true;
        } else if (packet instanceof C08PacketPlayerBlockPlacement) {
            cL = true;
        } else if (packet instanceof C07PacketPlayerDigging) {
            cL = true;
            cN = true;
        } else if (packet instanceof C0EPacketClickWindow || packet instanceof C16PacketClientStatus && ((C16PacketClientStatus)packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT || packet instanceof q) {
            cM = true;
        } else if (packet instanceof C03PacketPlayer) {
            BadPacketsComponent.reset();
        }
    };

    public static boolean aW() {
        return BadPacketsComponent.a(true, true, true, true, true, false);
    }

    public static boolean bad(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4) {
        return BadPacketsComponent.a(var0, var1, var2, var3, var4, false);
    }

    public static boolean a(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5) {
        return cI && var0 || cJ && var1 || cK && var2 || cL && var3 || cM && var4 || cN && var5;
    }

    public static void reset() {
        cI = false;
        cK = false;
        cJ = false;
        cL = false;
        cM = false;
        cN = false;
    }
}
