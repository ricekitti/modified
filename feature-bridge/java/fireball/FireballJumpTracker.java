package dev.rice.module.impl.fireball;

/**
 * Faithful 1:1 reconstruction of {@code valen.utility.FireballJumpTracker}
 * (CFR 0.152 decompile; source sha256 7149f202793b402fbf53dce22d3f7ec5019f4020fdd5ded8d5a7d846a01e7bce,
 * recorded in ../../valen-selected-ref-20261105/SHA256SUMS.json).
 *
 * Detects a fireball-boost jump from the packet stream, on a nanosecond clock:
 *   1. the local player used a fire charge  ({@link #onUse})
 *   2. an explosion landed within 8 blocks   ({@link #onExplosion}; distanceSq <= 64)
 *   3. an upward velocity packet followed     ({@link #onVelocity}; motionY > 0.05)
 *   4. the player is airborne with upward motion inside the 0.25s impact window ({@link #consumeJump}).
 * The whole sequence must complete inside the 1.5s launch window.
 *
 * Behavior is preserved exactly from the supplied source; only identifiers are clarified and the
 * two private boolean/long fields keep their original semantics. No logic, constant, comparison or
 * window was changed. This class is pure state + arithmetic and has no framework dependencies, so it
 * is mapping-agnostic (named/SRG/official) and reusable by the Regular and Legit boost modes alike.
 */
public final class FireballJumpTracker {
   private static final long LAUNCH_WINDOW_NANOS = 1_500_000_000L;
   private static final long IMPACT_WINDOW_NANOS = 250_000_000L;
   private static final double EXPLOSION_RANGE_SQ = 64.0;

   private boolean armed;
   private boolean explosion;
   private boolean upwardImpulse;
   private long launchedAt;
   private long explodedAt;
   private long impulseAt;

   /** Local player sent a fire-charge block placement (threw a fireball). Arms the window. */
   public void onUse(long nowNanos) {
      this.reset();
      this.armed = true;
      this.launchedAt = nowNanos;
   }

   /**
    * Server explosion packet while a launch is recent and within range.
    * @param distanceSq player distance squared to the blast center (blocks^2)
    * @param motionX/motionY/motionZ the explosion's player-velocity component (blocks/tick)
    */
   public void onExplosion(long nowNanos, double distanceSq, double motionX, double motionY, double motionZ) {
      if (!this.recentLaunch(nowNanos) || !Double.isFinite(distanceSq) || distanceSq < 0.0 || distanceSq > EXPLOSION_RANGE_SQ) {
         return;
      }
      this.explosion = true;
      this.explodedAt = nowNanos;
      this.onVelocity(nowNanos, motionX, motionY, motionZ);
   }

   /** Server velocity packet for the local player; an upward component arms the impulse. */
   public void onVelocity(long nowNanos, double motionX, double motionY, double motionZ) {
      if (this.recentLaunch(nowNanos) && Double.isFinite(motionX) && Double.isFinite(motionY)
            && Double.isFinite(motionZ) && motionY > 0.05) {
         this.upwardImpulse = true;
         this.impulseAt = nowNanos;
      }
   }

   /**
    * Returns true exactly once, the game loop in which a boost jump is confirmed, and consumes the
    * armed state. Expires the sequence when the launch window lapses or the impact window is missed.
    * @param airborne the player's {@code !onGround} flag this loop
    * @param motionY  the player's current vertical motion (blocks/tick)
    */
   public boolean consumeJump(long nowNanos, boolean airborne, double motionY) {
      if (!this.recentLaunch(nowNanos)) {
         this.reset();
         return false;
      }
      if (this.explosion && nowNanos - this.explodedAt > IMPACT_WINDOW_NANOS) {
         this.reset();
         return false;
      }
      boolean confirmed = this.explosion && this.upwardImpulse
            && nowNanos - this.impulseAt >= 0L && nowNanos - this.impulseAt <= IMPACT_WINDOW_NANOS
            && airborne && Double.isFinite(motionY) && motionY > 0.01;
      if (!confirmed) {
         return false;
      }
      this.reset();
      return true;
   }

   public void reset() {
      this.armed = false;
      this.explosion = false;
      this.upwardImpulse = false;
      this.impulseAt = 0L;
      this.explodedAt = 0L;
      this.launchedAt = 0L;
   }

   private boolean recentLaunch(long nowNanos) {
      return this.armed && nowNanos - this.launchedAt >= 0L && nowNanos - this.launchedAt <= LAUNCH_WINDOW_NANOS;
   }
}
