/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class PrePlayerInputEvent
extends Event {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private double sneakSlowDownMultiplier;

    public PrePlayerInputEvent(float f, float f2, boolean bl, boolean bl2, double d) {
        this.forward = f;
        this.strafe = f2;
        this.jump = bl;
        this.sneak = bl2;
        this.sneakSlowDownMultiplier = d;
    }

    public float getForward() {
        return this.forward;
    }

    public void setForward(float f) {
        this.forward = f;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public void setStrafe(float f) {
        this.strafe = f;
    }

    public boolean isJump() {
        return this.jump;
    }

    public void setJump(boolean bl) {
        this.jump = bl;
    }

    public boolean isSneak() {
        return this.sneak;
    }

    public void setSneak(boolean bl) {
        this.sneak = bl;
    }

    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
    }

    public void setSneakSlowDownMultiplier(double d) {
        this.sneakSlowDownMultiplier = d;
    }
}

