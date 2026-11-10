package dev.rice.client.stasis;

import net.minecraft.util.MovementInput;

/** OpenJade's two-input-update lock remains active after Stasis is disabled. */
public final class StasisInputLock {
   private static int lockedTicks;
   public static void reset(StasisInputLock lock) { lockedTicks = 0; }
   public static void lockInput(StasisInputLock lock) { lockedTicks = 2; }
   public static void clear() { lockedTicks = 0; }
   public static void apply(MovementInput input) {
      if (input != null && lockedTicks > 0) {
         input.moveForward = input.moveStrafe = 0F;
         input.jump = input.sneak = false;
         lockedTicks--;
      }
   }
}
