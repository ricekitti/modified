package com.snowy.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/**
 * Silent aim plumbing.
 *
 * <p>The player's own {@code rotationYaw}/{@code rotationPitch} are never touched. Instead the
 * mixins ask this class, once per tick, whether the rotation leaving the client in the movement
 * packet should be replaced. Because {@code EntityPlayerSP.onUpdateWalkingPlayer} reads the
 * rotation several times (to decide whether anything rotated, to fill the packet, and to record
 * what was last reported) the mixin redirects <em>every</em> read, so all three stay consistent
 * and a look-only change still forces a C05/C06 to go out.
 *
 * <p>Spoofing the packet alone would leave the server with a yaw that disagrees with where the
 * player is actually travelling, so when movement fix is armed the movement math is moved onto
 * the spoofed yaw as well and the WASD input is re-mapped to cancel that out
 * ({@link #correctedInput}).
 *
 * <p>State is armed for a single tick and cleared at the end of it, exactly like the script's
 * per-tick {@code client.enableMovementFix()} call.
 */
public final class RotationManager {

    private static final RotationManager INSTANCE = new RotationManager();

    private boolean active;
    private boolean movementFix;
    /**
     * Whether an engagement is under way, which outlives the per-tick arming above. Input is read
     * earlier in the tick than the aim is armed, so the thing that decides whether to hold a click
     * has to be the lock itself, not this tick's spoof.
     */
    private boolean engaged;
    private float yaw;
    private float pitch;

    /**
     * Who has this tick's rotation, and how much it matters: several modules may want to turn the
     * same tick - the one that matters most has it, whatever order they ask in - and what each does
     * with the rotation (a shot, a hit, a placement) waits until the tick its own went out.
     */
    private Object owner;
    private int priority = Integer.MIN_VALUE;
    /** Who had last tick's rotation (null: the player's own); set once a tick, when it ends. */
    private Object lastOwner;
    private boolean ended = true;

    public static final int PRIORITY_ANTIPEARL = 100;
    public static final int PRIORITY_CLUTCH = 95;
    public static final int PRIORITY_BED_DEFENDER = 90;
    public static final int PRIORITY_BLOCK_IN = 85;
    public static final int PRIORITY_COUNTER = 70;
    public static final int PRIORITY_DISPLACE = 60;
    /** Velocity's Reduce: below Displace, whose turn carries a hit of our own. */
    public static final int PRIORITY_VELOCITY = 55;
    public static final int PRIORITY_AIMBOT = 50;
    /** Bed Nuker, breaking: above all. */
    public static final int PRIORITY_BED_NUKER = 1000;
    public static final int PRIORITY_SCRIPT = 20;
    /** Turning back to the player's own look after: anything else that wants the rotation has it. */
    public static final int PRIORITY_TURN_BACK = 10;

    /** What actually went out in the last movement packet. */
    private float sentYaw;
    private float sentPitch;

    /**
     * Proof of life from the mixin. {@code recordedTicks} counts every tick the injection at the
     * end of {@code onUpdateWalkingPlayer} ran at all, so zero means the mixin never applied -
     * which is silent otherwise, because the packet then simply carries the real rotation and
     * nothing anywhere throws. {@code spoofedTicks} counts the subset where it found the spoof
     * armed and put our rotation in the packet instead.
     */
    private int recordedTicks;
    private int spoofedTicks;
    private int spoofedAt = -9999;

    /**
     * How often {@code EntityPlayerSP.onUpdate} ran, and how much of that was spent riding
     * something. The method we spoof from, {@code onUpdateWalkingPlayer}, is only reached on the
     * not-riding branch, so these two turn "the injection never ran" into an actual reason.
     */
    private int updateTicks;
    private int ridingTicks;
    /** Ticks the rotation redirects inside {@code onUpdateWalkingPlayer} actually ran on. */
    private int walkingTicks;

    /** Cached answer to "did {@link com.snowy.mixin.MixinEntityPlayerSP} actually apply". */
    private Boolean applied;

    private RotationManager() {
    }

    public static RotationManager get() {
        return INSTANCE;
    }

    /**
     * Whether the input correction in MovementInputFromOptions ran this tick. Another client can
     * overwrite that method whole (Jade does), taking our injection with it; then the correction
     * is made on the movement values instead (MixinEntityPlayerSP).
     */
    private boolean inputCorrectedThisTick;
    /** The player's own yaw while the spoofed one stands in for it around the movement. */
    private boolean yawSwapped;
    private float realYaw;

    public void noteInputCorrected() {
        inputCorrectedThisTick = true;
    }

    /** @return whether the input correction ran since the last call */
    public boolean takeInputCorrected() {
        boolean ran = inputCorrectedThisTick;
        inputCorrectedThisTick = false;
        return ran;
    }

    /**
     * Puts the spoofed yaw in the player's own rotationYaw until {@link #endMovementYaw}, so that
     * whatever moveFlying or jump runs - vanilla, or another client's overwrite of it, which our
     * redirects inside them do not survive - moves the player along the yaw the server is told.
     */
    public void beginMovementYaw(Entity entity) {
        if (yawSwapped || !shouldFixMovement() || !isLocalPlayer(entity)) {
            return;
        }
        realYaw = entity.rotationYaw;
        entity.rotationYaw = yaw;
        yawSwapped = true;
    }

    public void endMovementYaw(Entity entity) {
        if (yawSwapped && isLocalPlayer(entity)) {
            entity.rotationYaw = realYaw;
            yawSwapped = false;
        }
    }

    /**
     * Arms the spoof for this tick, for this owner, unless one that matters more already has it.
     * Must be called every tick the rotation should apply.
     *
     * @return whether it has the rotation (so far: one that matters more may still take it this tick)
     */
    public boolean aim(Object owner, int priority, float yaw, float pitch, boolean fixMovement) {
        if (com.snowy.util.Claims.blocked(owner, priority, com.snowy.util.Claims.Resource.LOOK)) {
            return false; // one that matters more has the look now (asked before or after us this tick)
        }
        if (active && priority < this.priority) {
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

    /** Whether this owner has this tick's rotation (asked once every module has had its say). */
    public boolean isOwner(Object owner) {
        return active && this.owner == owner;
    }

    /** Whether last tick's rotation - the one the server has now - was this owner's. */
    public boolean sentBy(Object owner) {
        return owner != null && lastOwner == owner;
    }

    /** Whether another module had last tick's rotation: one turning back leaves it to that one. */
    public boolean takenOver(Object owner) {
        return lastOwner != null && lastOwner != owner;
    }

    /** Called at the start of every client tick: this tick's owner is noted when it ends. */
    public void startTick() {
        ended = false;
    }

    /** Called at the end of every client tick, after the movement packet has been sent. */
    public void endTick() {
        if (!ended) {
            lastOwner = active ? owner : null;
            ended = true;
        }
        this.active = false;
        this.placing = false;
        this.movementFix = false;
        this.owner = null;
        this.priority = Integer.MIN_VALUE;
    }

    public boolean isActive() {
        return active;
    }

    /** A module places with this tick's look: the movement packet must carry it, whoever builds it. */
    private boolean placing;

    public void notePlacing() {
        placing = true;
        com.snowy.module.combat.displace.WireRotation.notePlacementLook(yaw, pitch);
    }

    public boolean isPlacing() {
        return active && placing;
    }

    /** True from the tick an engagement is locked until the tick the shot leaves. */
    public boolean isEngaged() {
        return engaged;
    }

    public void setEngaged(boolean engaged) {
        this.engaged = engaged;
    }

    public boolean shouldFixMovement() {
        return active && movementFix;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    /** The rotation the server has actually been told about. */
    public float getSentYaw() {
        return sentYaw;
    }

    public float getSentPitch() {
        return sentPitch;
    }

    public void recordSent(float yaw, float pitch, boolean spoofed) {
        this.sentYaw = yaw;
        this.sentPitch = pitch;
        this.recordedTicks++;
        if (spoofed) {
            this.spoofedTicks++;
            this.spoofedAt = this.recordedTicks;
        }
    }

    /** The last-sent look dropped to this, so it does not read as a stale rotation from a world you have left. */
    public void resetSent(float yaw, float pitch) {
        this.sentYaw = yaw;
        this.sentPitch = pitch;
    }

    /**
     * Whether the mixin is present on the loaded {@code EntityPlayerSP}, asked of the class itself
     * rather than inferred from whether its code has run yet.
     *
     * <p>Worth doing directly. In a pack this size the mixin can fail to apply for several
     * unrelated reasons - another jar winning the {@code MixinTweaker} race, the winner bringing a
     * Mixin too old for our config, a second launch pass with a different Mixin - and every one of
     * them fails the same silent way, because an unspoofed movement packet is perfectly valid. The
     * injected handlers keep the mixin's own method names, so their presence is a direct answer
     * where a behaviour counter is only ever circumstantial evidence.
     */
    public boolean isMixinApplied() {
        // only cache a yes: on the injected builds the mixin is never present and recordedTicks is 0
        // until the first walking-player tick runs, so a cached no (from an early call) would wrongly
        // disable silent aim for good. Keep re-checking until the injected handlers have run once.
        if (applied == null || !applied.booleanValue()) {
            // walkingTicks proves our onUpdateWalkingPlayer injection is live - the one place the
            // rotation spoof is written. On Badlion the player is an obfuscated subclass and the
            // outbound-packet hook may not fire, so recordedTicks can stay 0 while the spoof still
            // works; the walking-player hook running is the real proof that silent aim is installed.
            if (MixinProbe.injectedIntoPlayer() || recordedTicks > 0 || walkingTicks > 0) {
                applied = Boolean.TRUE;
            } else if (applied == null) {
                applied = Boolean.FALSE;
            }
        }
        return applied.booleanValue();
    }

    /**
     * Whether any of the injected code has run.
     *
     * <p>Counts the walking-player injection as well as sent packets, because a look packet only
     * goes out when the rotation has changed since the last one. Stand still and none does, so
     * {@code recordedTicks} alone would say the mixin was dead when it is simply idle.
     */
    public boolean isMixinAlive() {
        return recordedTicks > 0 || walkingTicks > 0;
    }

    public void noteUpdate(boolean riding) {
        updateTicks++;
        if (riding) {
            ridingTicks++;
        }
    }

    public int getUpdateTicks() {
        return updateTicks;
    }

    public int getRidingTicks() {
        return ridingTicks;
    }

    public void noteWalkingPlayer() {
        walkingTicks++;
    }

    public int getWalkingTicks() {
        return walkingTicks;
    }

    public int getRecordedTicks() {
        return recordedTicks;
    }

    public int getSpoofedTicks() {
        return spoofedTicks;
    }

    /** Whether the most recent packet the mixin handled carried our rotation rather than the real one. */
    public boolean spoofedLastPacket() {
        return spoofedAt == recordedTicks;
    }

    public boolean isLocalPlayer(Object entity) {
        return entity == Minecraft.getMinecraft().thePlayer;
    }

    /**
     * Re-maps a movement input so that, when it is applied under {@code spoofedYaw}, the player
     * still travels the direction {@code realYaw} + the original input would have produced.
     *
     * <p>Only the eight legitimate forward/strafe combinations are candidates, so the corrected
     * heading is quantised to 45 degree steps and can sit up to 22.5 degrees off the intended
     * one. That is inherent: the input has to stay something a keyboard could have produced.
     *
     * @return {@code { forward, strafe }}, or {@code null} when there is nothing to correct.
     */
    public static float[] correctedInput(float realYaw, float spoofedYaw, float forward, float strafe,
                                         boolean sneaking) {
        if (forward == 0f && strafe == 0f) {
            return null;
        }

        float sneakMultiplier = sneaking ? 0.3f : 1f;
        double intended = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(realYaw, forward, strafe)));

        float bestForward = 0f;
        float bestStrafe = 0f;
        double bestDifference = Double.MAX_VALUE;

        for (float f = -1f; f <= 1f; f += 1f) {
            for (float s = -1f; s <= 1f; s += 1f) {
                if (f == 0f && s == 0f) {
                    continue;
                }
                float candidateForward = f * sneakMultiplier;
                float candidateStrafe = s * sneakMultiplier;
                double candidate = MathHelper.wrapAngleTo180_double(
                        Math.toDegrees(direction(spoofedYaw, candidateForward, candidateStrafe)));
                // the difference has to be wrapped too, or a candidate 2 degrees away across the
                // +/-180 boundary scores as 358 and loses to one that is genuinely 43 degrees off
                double difference = Math.abs(MathHelper.wrapAngleTo180_double(intended - candidate));
                if (difference < bestDifference) {
                    bestDifference = difference;
                    bestForward = candidateForward;
                    bestStrafe = candidateStrafe;
                }
            }
        }
        return new float[] { bestForward, bestStrafe };
    }

    /** World-space heading, in radians, that this yaw and input combination moves towards. */
    private static double direction(float yaw, double forward, double strafe) {
        if (forward < 0.0) {
            yaw += 180f;
        }
        float sign = 1f;
        if (forward < 0.0) {
            sign = -0.5f;
        } else if (forward > 0.0) {
            sign = 0.5f;
        }
        if (strafe > 0.0) {
            yaw -= 90f * sign;
        }
        if (strafe < 0.0) {
            yaw += 90f * sign;
        }
        return Math.toRadians(yaw);
    }
}
