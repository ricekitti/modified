/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.MathHelper
 */
package valen.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import valen.event.ClientRotationEvent;
import valen.event.GameTickEvent;
import valen.event.JumpEvent;
import valen.event.PostPlayerInputEvent;
import valen.event.PreMotionEvent;
import valen.event.PreUpdateEvent;
import valen.event.StrafeEvent;
import valen.lunar.forge.client.event.RenderWorldLastEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.module.ModuleManager;
import valen.module.impl.client.Settings;
import valen.utility.RotationUtils;
import valen.utility.Utils;

public class RotationHelper {
    private static RotationHelper INSTANCE = new RotationHelper();
    private Float serverYaw = null;
    private Float serverPitch = null;
    private boolean setRotations = false;
    public boolean forceMovementFix = false;
    private boolean serverRelativeMovementInputs = false;
    private float savedYaw;
    private float savedPitch;
    private float savedPrevYaw;
    private float savedPrevPitch;
    public boolean swappedForMouseOver;
    private boolean rotationsUpdatedThisTick = false;
    private boolean needsArmYawUpdate = false;
    private Minecraft mc = Minecraft.getMinecraft();

    public static float unwrapYaw(float f, float f2) {
        return f2 + (((f - f2 + 180.0f) % 360.0f + 360.0f) % 360.0f - 180.0f);
    }

    public float[] getRotationsToTarget(Entity entity, ClientRotationEvent clientRotationEvent, float f) {
        float f2;
        if (entity == null || this.mc.thePlayer == null) {
            return null;
        }
        float f3 = clientRotationEvent.yaw != null ? clientRotationEvent.yaw.floatValue() : RotationUtils.serverRotations[0];
        float[] fArray = RotationUtils.getRotations(entity, f3, f2 = clientRotationEvent.pitch != null ? clientRotationEvent.pitch.floatValue() : RotationUtils.serverRotations[1]);
        if (fArray == null) {
            return null;
        }
        float f4 = Math.max(1.0f, f);
        float f5 = f3 + MathHelper.wrapAngleTo180_float((float)(fArray[0] - f3)) / f4;
        float f6 = f2 + (fArray[1] - f2) / f4;
        return new float[]{f5, f6};
    }

    public float[] getRotationsToTarget(Entity entity, float f) {
        if (entity == null || this.mc.thePlayer == null) {
            return null;
        }
        float f2 = this.mc.thePlayer.rotationYaw;
        float f3 = this.mc.thePlayer.rotationPitch;
        float[] fArray = RotationUtils.getRotations(entity, f2, f3);
        if (fArray == null) {
            return null;
        }
        float f4 = Math.max(1.0f, f);
        float f5 = f2 + MathHelper.wrapAngleTo180_float((float)(fArray[0] - f2)) / f4;
        float f6 = f3 + (fArray[1] - f3) / f4;
        return new float[]{f5, f6};
    }

    public float[] getRotationsToTarget(Entity entity, ClientRotationEvent clientRotationEvent, int n) {
        return this.getRotationsToTarget(entity, clientRotationEvent, n, 0.0, 0.0, 0.0f);
    }

    public float[] getRotationsToTarget(Entity entity, ClientRotationEvent clientRotationEvent, int n, double d, double d2, float f) {
        return this.getRotationsToTarget(entity, clientRotationEvent, n, d, d2, f, false, 10.0);
    }

    public float[] getRotationsToTarget(Entity entity, ClientRotationEvent clientRotationEvent, int n, double d, double d2, float f, boolean bl, double d3) {
        return this.getRotationsToTarget(entity, clientRotationEvent, n, d, d2, f, bl, d3, false, true);
    }

    public float[] getRotationsToTarget(Entity entity, ClientRotationEvent clientRotationEvent, int n, double d, double d2, float f, boolean bl, double d3, boolean bl2, boolean bl3) {
        float[] fArray;
        if (entity == null || this.mc.thePlayer == null) {
            return null;
        }
        float f2 = clientRotationEvent.yaw != null ? clientRotationEvent.yaw.floatValue() : RotationUtils.serverRotations[0];
        float f3 = clientRotationEvent.pitch != null ? clientRotationEvent.pitch.floatValue() : RotationUtils.serverRotations[1];
        float[] fArray2 = fArray = bl ? RotationUtils.getRotationsWithBackup(entity, d, d2, f2, f3, d3, bl2, bl3) : RotationUtils.getRotations(entity, d, d2, f2, f3);
        if (fArray == null) {
            return null;
        }
        return RotationUtils.smoothRotation(f2, f3, fArray[0], fArray[1], n, f);
    }

    public float[] getRotationsToTargetFov(Entity entity, ClientRotationEvent clientRotationEvent, float f, double d, double d2, float f2, boolean bl, double d3, boolean bl2, boolean bl3) {
        float[] fArray;
        if (entity == null || this.mc.thePlayer == null) {
            return null;
        }
        float f3 = clientRotationEvent.yaw != null ? clientRotationEvent.yaw.floatValue() : RotationUtils.serverRotations[0];
        float f4 = clientRotationEvent.pitch != null ? clientRotationEvent.pitch.floatValue() : RotationUtils.serverRotations[1];
        float[] fArray2 = fArray = bl ? RotationUtils.getRotationsWithBackup(entity, d, d2, f3, f4, d3, bl2, bl3) : RotationUtils.getRotations(entity, d, d2, f3, f4);
        if (fArray == null) {
            return null;
        }
        float[] fArray3 = RotationUtils.smoothRotationFov(f3, f4, fArray[0], fArray[1], f, f2);
        return RotationUtils.settleOnTarget(f3, f4, fArray3, fArray[0], fArray[1], entity, d3, bl2, bl3);
    }

    public float[] getRotationsToTargetFov(Entity entity, float f, double d, double d2, float f2, boolean bl, double d3, boolean bl2, boolean bl3) {
        float[] fArray;
        if (entity == null || this.mc.thePlayer == null) {
            return null;
        }
        float f3 = this.mc.thePlayer.rotationYaw;
        float f4 = this.mc.thePlayer.rotationPitch;
        float[] fArray2 = fArray = bl ? RotationUtils.getRotationsWithBackup(entity, d, d2, f3, f4, d3, bl2, bl3) : RotationUtils.getRotations(entity, d, d2, f3, f4);
        if (fArray == null) {
            return null;
        }
        float[] fArray3 = RotationUtils.smoothRotationFov(f3, f4, fArray[0], fArray[1], f, f2);
        return RotationUtils.settleOnTarget(f3, f4, fArray3, fArray[0], fArray[1], entity, d3, bl2, bl3);
    }

    public float[] getRotationsToTarget(Entity entity, int n) {
        return this.getRotationsToTarget(entity, n, 0.0, 0.0, 0.0f);
    }

    public float[] getRotationsToTarget(Entity entity, int n, double d, double d2, float f) {
        return this.getRotationsToTarget(entity, n, d, d2, f, false, 10.0);
    }

    public float[] getRotationsToTarget(Entity entity, int n, double d, double d2, float f, boolean bl, double d3) {
        return this.getRotationsToTarget(entity, n, d, d2, f, bl, d3, false, true);
    }

    public float[] getRotationsToTarget(Entity entity, int n, double d, double d2, float f, boolean bl, double d3, boolean bl2, boolean bl3) {
        float[] fArray;
        if (entity == null || this.mc.thePlayer == null) {
            return null;
        }
        float f2 = this.mc.thePlayer.rotationYaw;
        float f3 = this.mc.thePlayer.rotationPitch;
        float[] fArray2 = fArray = bl ? RotationUtils.getRotationsWithBackup(entity, d, d2, f2, f3, d3, bl2, bl3) : RotationUtils.getRotations(entity, d, d2, f2, f3);
        if (fArray == null) {
            return null;
        }
        return RotationUtils.smoothRotation(f2, f3, fArray[0], fArray[1], n, f);
    }

    public void updateServerRotations() {
        int n;
        if (this.mc.thePlayer == null) {
            return;
        }
        if (this.rotationsUpdatedThisTick) {
            return;
        }
        this.rotationsUpdatedThisTick = true;
        ClientRotationEvent clientRotationEvent = new ClientRotationEvent(this.serverYaw, this.serverPitch);
        MinecraftForge.EVENT_BUS.post(clientRotationEvent);
        this.serverYaw = clientRotationEvent.yaw;
        this.serverPitch = clientRotationEvent.pitch;
        if (this.serverYaw == null && this.serverPitch == null) {
            return;
        }
        if (this.serverYaw != null && Math.abs(this.serverYaw.floatValue() - this.mc.thePlayer.rotationYaw) >= 1.0f && (n = (int)Settings.randomYawFactor.getInput()) != 0) {
            int n2 = n * 100 + Utils.randomizeInt(-30, 30);
            RotationHelper rotationHelper = this;
            rotationHelper.serverYaw = Float.valueOf(rotationHelper.serverYaw.floatValue() + (float)Utils.randomizeInt(-n2, n2) / 100.0f);
        }
        float[] fArray = RotationUtils.fixRotation(this.serverYaw == null ? this.mc.thePlayer.rotationYaw : this.serverYaw.floatValue(), this.serverPitch == null ? this.mc.thePlayer.rotationPitch : this.serverPitch.floatValue(), RotationUtils.serverRotations[0], RotationUtils.serverRotations[1]);
        this.serverYaw = Float.valueOf(fArray[0]);
        this.serverPitch = Float.valueOf(fArray[1]);
        if (!(this.serverYaw.floatValue() == this.mc.thePlayer.rotationYaw || clientRotationEvent.yaw != null && clientRotationEvent.yaw.isNaN())) {
            this.setRotations = true;
        }
        if (!(this.serverPitch.floatValue() == this.mc.thePlayer.rotationPitch || clientRotationEvent.pitch != null && clientRotationEvent.pitch.isNaN())) {
            this.setRotations = true;
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onPreUpdate(PreUpdateEvent preUpdateEvent) {
        this.updateServerRotations();
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent preMotionEvent) {
        if (!this.setRotations) {
            return;
        }
        if (this.serverYaw != null && !this.serverYaw.isNaN()) {
            preMotionEvent.setYaw(this.serverYaw.floatValue());
        }
        if (this.serverPitch != null && !this.serverPitch.isNaN()) {
            preMotionEvent.setPitch(this.serverPitch.floatValue());
        }
    }

    @SubscribeEvent
    public void onRunTick(GameTickEvent gameTickEvent) {
        if (this.setRotations && this.serverYaw != null && this.mc.thePlayer != null) {
            float f;
            float f2 = RotationUtils.serverRotations[0];
            this.mc.thePlayer.rotationYaw = f = RotationHelper.unwrapYaw(MathHelper.wrapAngleTo180_float((float)this.mc.thePlayer.rotationYaw), f2);
            this.mc.thePlayer.prevRotationYaw = f;
        }
        this.serverPitch = null;
        this.serverYaw = null;
        this.forceMovementFix = false;
        this.setRotations = false;
        this.serverRelativeMovementInputs = false;
        this.rotationsUpdatedThisTick = false;
        this.swappedForMouseOver = false;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onRenderWorld(RenderWorldLastEvent renderWorldLastEvent) {
        if (this.setRotations && this.mc.thePlayer != null) {
            this.mc.thePlayer.prevRenderArmYaw = this.mc.thePlayer.rotationYaw;
            this.mc.thePlayer.renderArmYaw = this.mc.thePlayer.rotationYaw;
        }
    }

    public boolean isActive() {
        return this.setRotations && (this.serverYaw != null || this.serverPitch != null);
    }

    public void beginSwap(Entity entity, float f, float f2, boolean bl) {
        this.savedYaw = entity.rotationYaw;
        this.savedPrevYaw = entity.prevRotationYaw;
        this.savedPitch = entity.rotationPitch;
        this.savedPrevPitch = entity.prevRotationPitch;
        entity.rotationYaw = f;
        entity.prevRotationYaw = f;
        if (bl) {
            entity.rotationPitch = f2;
            entity.prevRotationPitch = f2;
        }
    }

    public void endSwap(Entity entity) {
        entity.rotationYaw = this.savedYaw;
        entity.prevRotationYaw = this.savedPrevYaw;
        entity.rotationPitch = this.savedPitch;
        entity.prevRotationPitch = this.savedPrevPitch;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onPostInput(PostPlayerInputEvent postPlayerInputEvent) {
        if (!this.fixMovement()) {
            return;
        }
        if (this.serverRelativeMovementInputs) {
            return;
        }
        float f = this.mc.thePlayer.movementInput.sneak ? 0.3f : 1.0f;
        float f2 = this.serverYaw.floatValue();
        float f3 = this.mc.thePlayer.movementInput.moveForward;
        float f4 = this.mc.thePlayer.movementInput.moveStrafe;
        if (f3 == 0.0f && f4 == 0.0f) {
            return;
        }
        double d = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(RotationHelper.getDirection(this.mc.thePlayer.rotationYaw, f3, f4)));
        float f5 = 0.0f;
        float f6 = 0.0f;
        float f7 = Float.MAX_VALUE;
        for (float f8 = -1.0f; f8 <= 1.0f; f8 += 1.0f) {
            for (float f9 = -1.0f; f9 <= 1.0f; f9 += 1.0f) {
                float f10;
                float f11;
                double d2;
                double d3;
                if (f8 == 0.0f && f9 == 0.0f || !((d3 = Math.abs(d - (d2 = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(RotationHelper.getDirection(f2, f11 = f8 * f, f10 = f9 * f)))))) < (double)f7)) continue;
                f7 = (float)d3;
                f5 = f11;
                f6 = f10;
            }
        }
        this.mc.thePlayer.movementInput.moveForward = f5;
        this.mc.thePlayer.movementInput.moveStrafe = f6;
    }

    @SubscribeEvent
    public void onStrafe(StrafeEvent strafeEvent) {
        if (this.fixMovement()) {
            strafeEvent.setYaw(this.serverYaw.floatValue());
        }
    }

    @SubscribeEvent
    public void onJump(JumpEvent jumpEvent) {
        if (this.fixMovement()) {
            jumpEvent.setYaw(this.serverYaw.floatValue());
        }
    }

    public boolean fixMovement() {
        return (ModuleManager.movementFix != null && ModuleManager.movementFix.isEnabled() || this.forceMovementFix) && this.setRotations;
    }

    public static double getDirection(float f, double d, double d2) {
        if (d < 0.0) {
            f += 180.0f;
        }
        float f2 = 1.0f;
        if (d < 0.0) {
            f2 = -0.5f;
        } else if (d > 0.0) {
            f2 = 0.5f;
        }
        if (d2 > 0.0) {
            f -= 90.0f * f2;
        }
        if (d2 < 0.0) {
            f += 90.0f * f2;
        }
        return Math.toRadians(f);
    }

    public static RotationHelper get() {
        return INSTANCE;
    }

    public void setRotations(float f, float f2) {
        this.serverYaw = Float.valueOf(f);
        this.serverPitch = Float.valueOf(f2);
        this.setRotations = true;
    }

    public void setYaw(float f) {
        this.serverYaw = Float.valueOf(f);
        this.setRotations = true;
    }

    public void setPitch(float f) {
        this.serverPitch = Float.valueOf(f);
        this.setRotations = true;
    }

    public void setServerRelativeMovementInputs(boolean bl) {
        this.serverRelativeMovementInputs = bl;
    }

    public Float getServerYaw() {
        return this.serverYaw;
    }

    public Float getServerPitch() {
        return this.serverPitch;
    }
}

