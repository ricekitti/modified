/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.IChatComponent
 */
package valen.lunar.forge.client.event;

import net.minecraft.util.IChatComponent;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class ClientChatReceivedEvent
extends Event {
    public final byte type;
    public IChatComponent message;

    public ClientChatReceivedEvent(byte by, IChatComponent iChatComponent) {
        this.type = by;
        this.message = iChatComponent;
    }
}

