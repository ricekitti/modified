/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class PreMotionEvent
extends Event {
    private double posX;
    public double posY;
    private double posZ;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private static boolean setRenderYaw;
    private boolean isSprinting;
    private boolean isSneaking;
    public static boolean setRotations;
    public static float preMotionYaw;

    public PreMotionEvent(double d, double d2, double d3, float f, float f2, boolean bl, boolean bl2, boolean bl3) {
        this.posX = d;
        this.posY = d2;
        this.posZ = d3;
        this.yaw = f;
        this.pitch = f2;
        this.onGround = bl;
        this.isSprinting = bl2;
        this.isSneaking = bl3;
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setPosX(double d) {
        this.posX = d;
    }

    public void setPosY(double d) {
        this.posY = d;
    }

    public void setPosZ(double d) {
        this.posZ = d;
    }

    public void setYaw(float f) {
        this.yaw = f;
        setRenderYaw = true;
        setRotations = true;
        preMotionYaw = f;
    }

    public void setRotations(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
        setRenderYaw = true;
        setRotations = true;
        preMotionYaw = f;
    }

    public void setPitch(float f) {
        this.pitch = f;
        setRotations = true;
    }

    public void setOnGround(boolean bl) {
        this.onGround = bl;
    }

    public static boolean setRenderYaw() {
        return setRenderYaw;
    }

    public static void setRenderYaw(boolean bl) {
        setRenderYaw = bl;
    }

    public boolean isSprinting() {
        return this.isSprinting;
    }

    public void setSprinting(boolean bl) {
        this.isSprinting = bl;
    }

    public boolean isSneaking() {
        return this.isSneaking;
    }

    public void setSneaking(boolean bl) {
        this.isSneaking = bl;
    }
}

