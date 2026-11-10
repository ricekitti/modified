package myau;

/**
 * Rice host stand-in for Mayu's TargetStrafeModule (not part of the port). Always disabled: MovementUtils only reads
 * {@code getStrafeYaw()} after {@code isEnabled()}.
 */
public class TargetStrafeModule extends Module {
   public TargetStrafeModule() {
      super("TargetStrafe", false, 0);
   }

   @Override
   public void setEnabled(boolean enabled) {
   }

   public float getStrafeYaw() {
      return Float.NaN;
   }
}
