/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class StrafeEvent
extends Event {
    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public StrafeEvent(float f, float f2, float f3, float f4) {
        this.strafe = f;
        this.forward = f2;
        this.friction = f3;
        this.yaw = f4;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public void setStrafe(float f) {
        this.strafe = f;
    }

    public float getForward() {
        return this.forward;
    }

    public void setForward(float f) {
        this.forward = f;
    }

    public float getFriction() {
        return this.friction;
    }

    public void setFriction(float f) {
        this.friction = f;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float f) {
        this.yaw = f;
    }
}

