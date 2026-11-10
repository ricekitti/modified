/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class ClientLookEvent
extends Event {
    public float yaw;
    public float pitch;

    public ClientLookEvent(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}

