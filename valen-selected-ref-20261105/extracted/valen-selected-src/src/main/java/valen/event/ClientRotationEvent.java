/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class ClientRotationEvent
extends Event {
    public Float yaw;
    public Float pitch;
    public boolean scriptRotations;

    public ClientRotationEvent(Float f, Float f2) {
        this.yaw = f;
        this.pitch = f2;
    }

    public void setYaw(Float f) {
        this.yaw = f;
    }

    public void setPitch(Float f) {
        this.pitch = f;
    }
}

