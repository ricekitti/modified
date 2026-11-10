/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Packet
 */
package valen.event;

import net.minecraft.network.Packet;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class DispatchPacketEvent
extends Event {
    private Packet<?> packet;

    public DispatchPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }
}

