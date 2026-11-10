import dev.rice.module.impl.fireball.FireballJumpTracker;
import dev.rice.module.impl.fireball.TimerController;
import dev.rice.module.impl.fireball.TimerRangeCycle;

/**
 * Headless behavior test for the Fireball Boost adaptation's pure logic (no Minecraft classes):
 * jump detection windows, timer-owner arbitration, and the Regular/Legit freeze-burst cycle with
 * cooldown and tick-cap. Nanosecond constants mirror the supplied source exactly. Static results
 * only — this does not exercise live packet dispatch or the client timer (those are live-game items).
 */
public final class FireballBoostLogicTest {
   private static int checks;

   private static void check(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAIL: " + what);
      }
      checks++;
   }

   private static final long MS = 1_000_000L;

   public static void main(String[] args) {
      jumpTracker();
      timerController();
      cycleRegular();
      cycleLegit();
      cycleCooldownAndCap();
      System.out.println("Fireball Boost logic: " + checks + " detection, arbitration, cycle, cooldown and cap assertions passed");
   }

   private static void jumpTracker() {
      // Full valid sequence fires exactly once, then disarms.
      FireballJumpTracker t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 10.0, 0.1, 0.3, 0.1);
      check(t.consumeJump(150 * MS, true, 0.5), "valid sequence confirms a jump");
      check(!t.consumeJump(160 * MS, true, 0.5), "a confirmed jump disarms (no second fire)");

      // Explosion out of range (distSq > 64) never arms the explosion flag.
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 65.0, 0.1, 0.3, 0.1);
      check(!t.consumeJump(150 * MS, true, 0.5), "explosion beyond 64 blocks^2 is ignored");

      // Upward velocity without an explosion does not confirm.
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onVelocity(100 * MS, 0.1, 0.3, 0.1);
      check(!t.consumeJump(150 * MS, true, 0.5), "velocity without explosion does not confirm");

      // A non-upward explosion impulse (motionY <= 0.05) does not arm the impulse.
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 10.0, 0.1, 0.04, 0.1);
      check(!t.consumeJump(150 * MS, true, 0.5), "non-upward impulse (<=0.05) does not confirm");

      // Launch window: 1.5s after onUse, the sequence has expired.
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 10.0, 0.1, 0.3, 0.1);
      check(!t.consumeJump(1_600 * MS, true, 0.5), "launch window expires after 1.5s");

      // Impact window: >0.25s after the explosion, the jump is missed.
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 10.0, 0.1, 0.3, 0.1);
      check(!t.consumeJump(400 * MS, true, 0.5), "impact window expires after 0.25s");

      // Must be airborne with upward motion.
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 10.0, 0.1, 0.3, 0.1);
      check(!t.consumeJump(150 * MS, false, 0.5), "grounded player does not confirm");
      t = new FireballJumpTracker();
      t.onUse(0);
      t.onExplosion(100 * MS, 10.0, 0.1, 0.3, 0.1);
      check(!t.consumeJump(150 * MS, true, 0.005), "motionY <= 0.01 does not confirm");
   }

   private static void timerController() {
      final float[] applied = {Float.NaN};
      TimerController c = new TimerController(s -> applied[0] = (float)s);

      c.request(TimerController.Owner.FIREBALL_BOOST, 0.0);
      check(c.isOwner(TimerController.Owner.FIREBALL_BOOST), "single request takes ownership");
      check(applied[0] == 0.0f, "applySpeed receives the requested freeze speed");

      c.request(TimerController.Owner.TIMER_RANGE, 0.5);
      check(c.isOwner(TimerController.Owner.FIREBALL_BOOST), "higher-ordinal owner wins arbitration");
      check(c.hasRequestOtherThan(TimerController.Owner.FIREBALL_BOOST), "sees the other owner's request");
      check(c.isOverridden(TimerController.Owner.TIMER_RANGE), "lower owner reports overridden");
      check(!c.isOverridden(TimerController.Owner.FIREBALL_BOOST), "winning owner is not overridden");

      c.release(TimerController.Owner.FIREBALL_BOOST);
      check(c.isOwner(TimerController.Owner.TIMER_RANGE) && applied[0] == 0.5f, "release hands off to the remaining owner");
      c.release(TimerController.Owner.TIMER_RANGE);
      check(c.getSpeed() == 1.0f && applied[0] == 1.0f, "last release restores speed 1.0");
   }

   private static void cycleRegular() {
      final double[] catchUp = {0.0};
      TimerController timer = new TimerController(s -> {});
      TimerRangeCycle cycle = new TimerRangeCycle(timer, TimerController.Owner.FIREBALL_BOOST, 40, d -> catchUp[0] += d);

      check(cycle.begin(0, false, 200.0, 1000.0), "Regular begin succeeds");
      check(catchUp[0] == 4.0, "Regular bursts duration/50 = 4 ticks immediately");
      check(cycle.active() && !cycle.legit(), "Regular cycle is active and not legit");
      check(timer.isOwner(TimerController.Owner.FIREBALL_BOOST), "cycle owns the timer during the burst");
      cycle.update(0);
      check(cycle.active(), "stays frozen until the freeze window elapses");
      cycle.update(200 * MS);
      check(!cycle.active(), "releases once the 200ms freeze window elapses");
      check(!timer.isOwner(TimerController.Owner.FIREBALL_BOOST), "timer ownership released at finish");
   }

   private static void cycleLegit() {
      final double[] catchUp = {0.0};
      TimerController timer = new TimerController(s -> {});
      TimerRangeCycle cycle = new TimerRangeCycle(timer, TimerController.Owner.FIREBALL_BOOST, 40, d -> catchUp[0] += d);

      check(cycle.begin(0, true, 200.0, 1000.0), "Legit begin succeeds");
      check(catchUp[0] == 0.0, "Legit does not burst immediately (freeze first)");
      cycle.update(100 * MS);
      check(catchUp[0] == 0.0 && cycle.active(), "still frozen mid-window");
      cycle.update(200 * MS);
      check(catchUp[0] == 4.0, "Legit bursts after the freeze window");
      cycle.update(200 * MS);
      check(!cycle.active(), "Legit finishes after the post-freeze burst");
   }

   private static void cycleCooldownAndCap() {
      final double[] catchUp = {0.0};
      TimerController timer = new TimerController(s -> {});
      TimerRangeCycle cycle = new TimerRangeCycle(timer, TimerController.Owner.FIREBALL_BOOST, 40, d -> catchUp[0] += d);

      // Tick cap: 3000ms/50 = 60 ticks, capped to maxTicks 40, drained 10 per loop.
      check(cycle.begin(0, false, 3000.0, 1000.0), "long-duration begin succeeds");
      check(catchUp[0] == 10.0, "first burst loop emits at most 10 ticks");
      cycle.update(0);
      cycle.update(0);
      cycle.update(0);
      check(catchUp[0] == 40.0, "total emitted ticks capped at maxTicks (40)");
      cycle.update(3000 * MS);
      check(!cycle.active(), "cycle finishes after the capped burst and freeze");

      // Cooldown gate: cannot begin again until cooldownNanos elapse.
      check(!cycle.begin(500 * MS, false, 200.0, 1000.0), "cannot re-begin during cooldown");
      check(cycle.begin(1_100 * MS, false, 200.0, 1000.0), "can begin once cooldown elapses");
   }
}
