package myau;

/**
 * Rice host stand-in for Mayu's KillAuraModule (not part of the port; Rice's KillAura is OpenB4's). Always disabled:
 * the verbatim MixinEntityRenderer injector only calls {@code shouldRenderBlocking()} after {@code isEnabled()}.
 */
public class KillAuraModule extends Module {
   public KillAuraModule() {
      super("KillAura", false, 0);
   }

   @Override
   public void setEnabled(boolean enabled) {
   }

   public boolean shouldRenderBlocking() {
      return false;
   }
}
