package dev.rice.module.impl.fireball;

import java.util.EnumMap;
import java.util.function.DoubleConsumer;

/**
 * Faithful 1:1 reconstruction of {@code valen.utility.TimerController} (CFR 0.152;
 * source sha256 recorded in ../../valen-selected-ref-20261105/SHA256SUMS.json).
 *
 * Shared client-timer-speed arbiter. Any number of owners may request a speed in [0,1]; the
 * highest-ordinal active owner wins (ordinal order == the {@link Owner} declaration order), and that
 * winning speed is pushed through the injected {@code applySpeed} consumer (which writes the live
 * {@code Timer.timerSpeed}). The full owner set is retained so the remaining ported rescue modules
 * (Stasis / Bridge Egg / Fireball Pearl Clutch / Auto Pearl) share one arbiter and never fight the
 * timer. Logic, clamping and the "apply only on change" rule are preserved exactly.
 */
public final class TimerController {
   private final EnumMap<Owner, Float> requests = new EnumMap<>(Owner.class);
   private final DoubleConsumer applySpeed;
   private Owner owner;
   private float speed = 1.0F;

   public TimerController(DoubleConsumer applySpeed) {
      this.applySpeed = applySpeed;
   }

   public void request(Owner owner, double speed) {
      if (!Double.isFinite(speed)) {
         this.release(owner);
         return;
      }
      this.requests.put(owner, (float)Math.max(0.0, Math.min(1.0, speed)));
      this.apply();
   }

   public void release(Owner owner) {
      this.requests.remove(owner);
      this.apply();
   }

   public boolean isOwner(Owner owner) {
      return this.owner == owner;
   }

   public boolean isRequested(Owner owner) {
      return this.requests.containsKey(owner);
   }

   public boolean hasRequests() {
      return !this.requests.isEmpty();
   }

   public boolean hasRequestOtherThan(Owner owner) {
      for (Owner other : this.requests.keySet()) {
         if (other != owner) {
            return true;
         }
      }
      return false;
   }

   public boolean isOverridden(Owner owner) {
      return this.owner != null && this.owner.ordinal() > owner.ordinal();
   }

   public float getSpeed() {
      return this.speed;
   }

   public void clear() {
      this.requests.clear();
      this.apply();
   }

   public void apply() {
      boolean had = this.owner != null;
      this.owner = null;
      this.speed = 1.0F;
      for (Owner candidate : Owner.values()) {
         Float requested = this.requests.get(candidate);
         if (requested == null) {
            continue;
         }
         this.owner = candidate;
         this.speed = requested;
      }
      if (this.owner != null || had) {
         this.applySpeed.accept(this.speed);
      }
   }

   public enum Owner {
      TIMER,
      CRITICALS,
      TIMER_RANGE,
      FIREBALL_BOOST,
      BED_BOOST,
      STASIS,
      BRIDGE_EGG,
      FIREBALL_PEARL_CLUTCH,
      AUTO_PEARL
   }
}
