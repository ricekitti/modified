package com.snowy.rotation;

import net.minecraft.util.MathHelper;

import java.util.Random;

/**
 * Jade's rotation shaping, shared by the modules that turn silently (Aim Assist, Clutch Assist). Two parts,
 * both of which an anticheat looks for if they are missing:
 *
 * <ul>
 * <li><b>eased step</b> - move towards the target by {@code (speed/30)^2} of 180 degrees a tick (scaled by a
 *     tick fraction, varied by a Randomization %, never overshooting), so the look never teleports onto the
 *     target in one tick;</li>
 * <li><b>GCD quantize</b> - round each delta onto the grid of angles a real mouse at the player's sensitivity
 *     produces, so the rotation looks like a hand moved it.</li>
 * </ul>
 */
public final class AngleUtils {

    private AngleUtils() {
    }

    /**
     * One tick of an eased turn from the current angle towards the target. speed 1-30 (30 = land at once);
     * {@code frames} is the tick fraction (a whole tick = 1); {@code randomization} 0-100 varies the step and
     * slows it near the target. {@code rng} may be null when randomization is 0.
     */
    public static float[] step(float curYaw, float curPitch, float tgtYaw, float tgtPitch,
                               int speed, float frames, int randomization, Random rng) {
        if (speed >= 30) {
            return new float[] {curYaw + MathHelper.wrapAngleTo180_float(tgtYaw - curYaw), tgtPitch};
        }
        float dYaw = MathHelper.wrapAngleTo180_float(tgtYaw - curYaw);
        float dPitch = tgtPitch - curPitch;
        float dist = (float) Math.sqrt(dYaw * dYaw + dPitch * dPitch);
        if (dist < 0.001F) {
            return new float[] {curYaw, curPitch};
        }
        float t = speed / 30.0F;
        float base = t * t * 180.0F;
        float rand = randomization;
        float jitter = 0.6F * (rand / 100.0F);
        if (jitter > 0.001F && rng != null) {
            base *= 1.0F - jitter / 2.0F + (float) (rng.nextDouble() * jitter);
        }
        float closeness = Math.min(1.0F, dist / 180.0F);
        float ease = (float) Math.pow(closeness, 0.7D);
        float r = rand / 100.0F;
        float factor = Math.max(0.8F, 1.0F - r * (1.0F - ease));
        float step = Math.min(base * factor * frames, dist) / dist;
        return new float[] {curYaw + dYaw * step, curPitch + dPitch * step};
    }

    /** Round {@code angle} onto the rotation grid the player's mouse sensitivity produces, from {@code prev}. */
    public static float quantize(float angle, float prev, float sensitivity, boolean yaw) {
        float a = yaw ? prev + MathHelper.wrapAngleTo180_float(angle - prev) : angle;
        float f = sensitivity * 0.6F + 0.2F;
        double g = f * f * f * 1.2D;
        if (g <= 0.0D) {
            return a;
        }
        return prev + (float) (Math.round((a - prev) / g) * g);
    }

    public static float clampPitch(float pitch) {
        return MathHelper.clamp_float(pitch, -90.0F, 90.0F);
    }
}
