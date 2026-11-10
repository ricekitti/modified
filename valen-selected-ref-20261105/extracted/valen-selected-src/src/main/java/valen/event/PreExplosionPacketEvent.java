/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.play.server.S27PacketExplosion
 */
package valen.event;

import net.minecraft.network.play.server.S27PacketExplosion;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class PreExplosionPacketEvent
extends Event {
    public S27PacketExplosion packet;

    public PreExplosionPacketEvent(S27PacketExplosion s27PacketExplosion) {
        this.packet = s27PacketExplosion;
    }
}

