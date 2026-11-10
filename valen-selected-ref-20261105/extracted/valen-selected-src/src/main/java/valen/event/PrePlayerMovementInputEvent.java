/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class PrePlayerMovementInputEvent
extends Event {
    public float forward;
    public float strafe;

    public PrePlayerMovementInputEvent(float f, float f2) {
        this.forward = f;
        this.strafe = f2;
    }
}

