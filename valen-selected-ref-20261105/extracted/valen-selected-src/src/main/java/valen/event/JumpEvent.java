/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class JumpEvent
extends Event {
    private float motionY;
    private float yaw;
    private boolean applySprint;

    public JumpEvent(float f, float f2, boolean bl) {
        this.motionY = f;
        this.yaw = f2;
        this.applySprint = bl;
    }

    public float getMotionY() {
        return this.motionY;
    }

    public void setMotionY(float f) {
        this.motionY = f;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float f) {
        this.yaw = f;
    }

    public boolean applySprint() {
        return this.applySprint;
    }

    public void setSprint(boolean bl) {
        this.applySprint = bl;
    }
}

