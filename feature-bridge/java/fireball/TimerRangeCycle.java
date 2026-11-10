package dev.rice.module.impl.fireball;

import java.util.function.DoubleConsumer;

/**
 * Faithful 1:1 reconstruction of {@code valen.utility.TimerRangeCycle} (CFR 0.152;
 * source sha256 recorded in ../../valen-selected-ref-20261105/SHA256SUMS.json).
 *
 * Drives a single freeze/burst timer cycle, owned through {@link TimerController}:
 *   - Regular (legit=false): request speed 0 then immediately emit the catch-up tick burst
 *     (the catch-up consumer adds to {@code Timer.elapsedPartialTicks}).
 *   - Legit  (legit=true):  freeze (speed 0) until {@code freezeUntil}, then burst.
 * duration(ms)/50 = ticks, capped at {@code maxTicks}; cooldown(ms) clamped to [0,10000]. A burst emits
 * at most 10 ticks per loop ({@link #emitBurstTicks}). Nanosecond clock throughout. No constant,
 * comparison, phase transition or cooldown rule was changed.
 */
public final class TimerRangeCycle {
   private final TimerController timer;
   private final TimerController.Owner owner;
   private final int maxTicks;
   private final DoubleConsumer catchUp;
   private Phase phase = Phase.IDLE;
   private boolean legit;
   private boolean coolingDown;
   private long freezeUntil;
   private long cooldownUntil;
   private long cooldownNanos;
   private double ticks;
   private double remainingTicks;

   public TimerRangeCycle(TimerController timer, DoubleConsumer catchUp) {
      this(timer, TimerController.Owner.TIMER_RANGE, catchUp);
   }

   public TimerRangeCycle(TimerController timer, TimerController.Owner owner, DoubleConsumer catchUp) {
      this(timer, owner, 10, catchUp);
   }

   public TimerRangeCycle(TimerController timer, TimerController.Owner owner, int maxTicks, DoubleConsumer catchUp) {
      if (maxTicks < 1) {
         throw new IllegalArgumentException("maxTicks must be positive");
      }
      this.timer = timer;
      this.owner = owner;
      this.maxTicks = maxTicks;
      this.catchUp = catchUp;
   }

   public boolean begin(long now, boolean legit, double durationMs, double cooldownMs) {
      if (this.active()
            || (this.coolingDown && now < this.cooldownUntil)
            || this.timer.isOverridden(this.owner)
            || !Double.isFinite(durationMs)
            || !Double.isFinite(cooldownMs)
            || durationMs <= 0.0) {
         return false;
      }
      this.legit = legit;
      this.ticks = Math.min((double)this.maxTicks, durationMs / 50.0);
      this.freezeUntil = now + (long)(this.ticks * 5.0E7);
      this.cooldownNanos = (long)(Math.max(0.0, Math.min(10000.0, cooldownMs)) * 1000000.0);
      this.timer.request(this.owner, 0.0);
      if (legit) {
         this.phase = Phase.FROZEN;
      } else {
         this.burst(now);
      }
      return true;
   }

   public void update(long now) {
      if (!this.active()) {
         return;
      }
      if (!this.timer.isOwner(this.owner)) {
         this.abort(now);
         return;
      }
      if (this.phase == Phase.BURST) {
         if (this.remainingTicks > 0.0) {
            this.emitBurstTicks();
            return;
         }
         if (this.legit) {
            this.finish();
            return;
         }
         this.phase = Phase.FROZEN;
      }
      if (now >= this.freezeUntil) {
         if (this.legit) {
            this.burst(now);
         } else {
            this.finish();
         }
      }
   }

   public void abort(long now) {
      if (this.active()) {
         this.coolingDown = true;
         this.cooldownUntil = Math.max(this.cooldownUntil, now + this.cooldownNanos);
      }
      this.finish();
   }

   public void reset() {
      this.finish();
      this.coolingDown = false;
      this.cooldownUntil = 0L;
      this.cooldownNanos = 0L;
   }

   public boolean active() {
      return this.phase != Phase.IDLE;
   }

   public boolean legit() {
      return this.legit;
   }

   public boolean blockWhileFrozen() {
      return this.phase == Phase.FROZEN && !this.legit;
   }

   private void burst(long now) {
      this.phase = Phase.BURST;
      this.coolingDown = true;
      this.cooldownUntil = now + this.cooldownNanos;
      this.remainingTicks = this.ticks;
      this.emitBurstTicks();
   }

   private void emitBurstTicks() {
      double emit = Math.min(10.0, this.remainingTicks);
      this.remainingTicks -= emit;
      this.catchUp.accept(emit);
   }

   private void finish() {
      this.phase = Phase.IDLE;
      this.freezeUntil = 0L;
      this.ticks = 0.0;
      this.remainingTicks = 0.0;
      this.timer.release(this.owner);
   }

   private enum Phase {
      IDLE,
      BURST,
      FROZEN
   }
}
