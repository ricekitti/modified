package dev.rice.module.impl.pearl;

/**
 * Faithful 1:1 reconstruction of {@code valen.utility.ContinuousCondition} (CFR 0.152;
 * source sha256 in ../../valen-selected-ref-20261105/SHA256SUMS.json). Pure logic, no dependencies.
 *
 * A latch that returns true only once a condition has held continuously for at least {@code holdNanos}.
 * Used by Auto Pearl / Fireball Pearl Clutch for the "In void for" timed trigger. A non-held sample, or a
 * clock that moves backwards, resets the latch. Behavior preserved exactly.
 */
public final class ContinuousCondition {
   private boolean active;
   private long startedAt;

   public boolean update(boolean held, long nowNanos, long holdNanos) {
      if (!held) {
         this.reset();
         return false;
      }
      if (!this.active || nowNanos < this.startedAt) {
         this.active = true;
         this.startedAt = nowNanos;
      }
      return nowNanos - this.startedAt >= Math.max(0L, holdNanos);
   }

   public void reset() {
      this.active = false;
      this.startedAt = 0L;
   }
}
