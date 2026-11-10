/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.MathHelper
 */
package com.rice.antipearl.core;

import com.rice.antipearl.core.MixinProbe;
import com.rice.displace.WireRotation;
import com.rice.util.Claims;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public final class RotationManager {
    private static final RotationManager INSTANCE = new RotationManager();
    private boolean active;
    private boolean movementFix;
    private boolean engaged;
    private float yaw;
    private float pitch;
    private Object owner;
    private int priority = Integer.MIN_VALUE;
    private Object lastOwner;
    private boolean ended = true;
    public static final int PRIORITY_ANTIPEARL = 100;
    public static final int PRIORITY_CLUTCH = 95;
    public static final int PRIORITY_BED_DEFENDER = 90;
    public static final int PRIORITY_BLOCK_IN = 85;
    public static final int PRIORITY_COUNTER = 70;
    public static final int PRIORITY_DISPLACE = 60;
    public static final int PRIORITY_VELOCITY = 55;
    public static final int PRIORITY_AIMBOT = 50;
    public static final int PRIORITY_BED_NUKER = 1000;
    public static final int PRIORITY_SCRIPT = 20;
    public static final int PRIORITY_TURN_BACK = 10;
    private float sentYaw;
    private float sentPitch;
    private int recordedTicks;
    private int spoofedTicks;
    private int spoofedAt = -9999;
    private int updateTicks;
    private int ridingTicks;
    private int walkingTicks;
    private Boolean applied;
    private boolean inputCorrectedThisTick;
    private boolean yawSwapped;
    private float realYaw;
    private boolean placing;

    private RotationManager() {
    }

    public static RotationManager get() {
        return INSTANCE;
    }

    public void noteInputCorrected() {
        this.inputCorrectedThisTick = true;
    }

    public boolean takeInputCorrected() {
        boolean ran = this.inputCorrectedThisTick;
        this.inputCorrectedThisTick = false;
        return ran;
    }

    public void beginMovementYaw(Entity entity) {
        if (this.yawSwapped || !this.shouldFixMovement() || !this.isLocalPlayer(entity)) {
            return;
        }
        this.realYaw = entity.rotationYaw;
        entity.rotationYaw = this.yaw;
        this.yawSwapped = true;
    }

    public void endMovementYaw(Entity entity) {
        if (this.yawSwapped && this.isLocalPlayer(entity)) {
            entity.rotationYaw = this.realYaw;
            this.yawSwapped = false;
        }
    }

    public boolean aim(Object owner, int priority, float yaw, float pitch, boolean fixMovement) {
        if (Claims.blocked(owner, priority, Claims.Resource.LOOK)) {
            return false;
        }
        if (this.active && priority < this.priority) {
            return false;
        }
        this.owner = owner;
        this.priority = priority;
        this.yaw = yaw;
        this.pitch = pitch;
        this.active = true;
        this.movementFix = fixMovement;
        return true;
    }

    public boolean isOwner(Object owner) {
        return this.active && this.owner == owner;
    }

    public boolean sentBy(Object owner) {
        return owner != null && this.lastOwner == owner;
    }

    public boolean takenOver(Object owner) {
        return this.lastOwner != null && this.lastOwner != owner;
    }

    public void startTick() {
        this.ended = false;
    }

    public void endTick() {
        if (!this.ended) {
            this.lastOwner = this.active ? this.owner : null;
            this.ended = true;
        }
        this.active = false;
        this.placing = false;
        this.movementFix = false;
        this.owner = null;
        this.priority = Integer.MIN_VALUE;
    }

    public boolean isActive() {
        return this.active;
    }

    public void notePlacing() {
        this.placing = true;
        WireRotation.notePlacementLook(this.yaw, this.pitch);
    }

    public boolean isPlacing() {
        return this.active && this.placing;
    }

    public boolean isEngaged() {
        return this.engaged;
    }

    public void setEngaged(boolean engaged) {
        this.engaged = engaged;
    }

    public boolean shouldFixMovement() {
        return this.active && this.movementFix;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getSentYaw() {
        return this.sentYaw;
    }

    public float getSentPitch() {
        return this.sentPitch;
    }

    public void recordSent(float yaw, float pitch, boolean spoofed) {
        this.sentYaw = yaw;
        this.sentPitch = pitch;
        ++this.recordedTicks;
        if (spoofed) {
            ++this.spoofedTicks;
            this.spoofedAt = this.recordedTicks;
        }
    }

    public boolean isMixinApplied() {
        if (this.applied == null || !this.applied.booleanValue()) {
            if (MixinProbe.injectedIntoPlayer() || this.recordedTicks > 0 || this.walkingTicks > 0) {
                this.applied = Boolean.TRUE;
            } else if (this.applied == null) {
                this.applied = Boolean.FALSE;
            }
        }
        return this.applied;
    }

    public boolean isMixinAlive() {
        return this.recordedTicks > 0 || this.walkingTicks > 0;
    }

    public void noteUpdate(boolean riding) {
        ++this.updateTicks;
        if (riding) {
            ++this.ridingTicks;
        }
    }

    public int getUpdateTicks() {
        return this.updateTicks;
    }

    public int getRidingTicks() {
        return this.ridingTicks;
    }

    public void noteWalkingPlayer() {
        ++this.walkingTicks;
    }

    public int getWalkingTicks() {
        return this.walkingTicks;
    }

    public int getRecordedTicks() {
        return this.recordedTicks;
    }

    public int getSpoofedTicks() {
        return this.spoofedTicks;
    }

    public boolean spoofedLastPacket() {
        return this.spoofedAt == this.recordedTicks;
    }

    public boolean isLocalPlayer(Object entity) {
        return entity == Minecraft.getMinecraft().thePlayer;
    }

    public static float[] correctedInput(float realYaw, float spoofedYaw, float forward, float strafe, boolean sneaking) {
        if (forward == 0.0f && strafe == 0.0f) {
            return null;
        }
        float sneakMultiplier = sneaking ? 0.3f : 1.0f;
        double intended = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(RotationManager.direction(realYaw, forward, strafe)));
        float bestForward = 0.0f;
        float bestStrafe = 0.0f;
        double bestDifference = Double.MAX_VALUE;
        for (float f = -1.0f; f <= 1.0f; f += 1.0f) {
            for (float s = -1.0f; s <= 1.0f; s += 1.0f) {
                float candidateStrafe;
                float candidateForward;
                double candidate;
                double difference;
                if (f == 0.0f && s == 0.0f || !((difference = Math.abs(MathHelper.wrapAngleTo180_double((double)(intended - (candidate = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(RotationManager.direction(spoofedYaw, candidateForward = f * sneakMultiplier, candidateStrafe = s * sneakMultiplier)))))))) < bestDifference)) continue;
                bestDifference = difference;
                bestForward = candidateForward;
                bestStrafe = candidateStrafe;
            }
        }
        return new float[]{bestForward, bestStrafe};
    }

    private static double direction(float yaw, double forward, double strafe) {
        if (forward < 0.0) {
            yaw += 180.0f;
        }
        float sign = 1.0f;
        if (forward < 0.0) {
            sign = -0.5f;
        } else if (forward > 0.0) {
            sign = 0.5f;
        }
        if (strafe > 0.0) {
            yaw -= 90.0f * sign;
        }
        if (strafe < 0.0) {
            yaw += 90.0f * sign;
        }
        return Math.toRadians(yaw);
    }
}

