/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.play.server.S12PacketEntityVelocity
 */
package valen.event;

import net.minecraft.network.play.server.S12PacketEntityVelocity;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class PreEntityVelocityEvent
extends Event {
    public S12PacketEntityVelocity packet;

    public PreEntityVelocityEvent(S12PacketEntityVelocity s12PacketEntityVelocity) {
        this.packet = s12PacketEntityVelocity;
    }
}

