package myau;

/**
 * Rice host stand-in for Mayu's LongJumpModule (not part of the port). Always disabled: Scaffold only asks
 * {@code isEnabled() && isAutomaticFireballMode() && !hasJumpStarted()}, which short-circuits on {@code isEnabled()}.
 */
public class LongJumpModule extends Module {
   public LongJumpModule() {
      super("LongJump", false, 0);
   }

   @Override
   public void setEnabled(boolean enabled) {
   }

   public boolean isAutomaticFireballMode() {
      return false;
   }

   public boolean hasJumpStarted() {
      return false;
   }
}
