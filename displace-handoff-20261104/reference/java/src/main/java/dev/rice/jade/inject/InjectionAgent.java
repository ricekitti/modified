package dev.rice.jade.inject;

/**
 * Rice host shim for Jade's injection agent diagnostics. Rice is not Jade's agent: diagnostics are off,
 * signals are not recorded, and the Badlion-specific paths are disabled (Rice is not a Badlion runtime).
 */
public final class InjectionAgent {
   private InjectionAgent() {
   }

   public static void setHookEnabled(String hook, String descriptor, boolean enabled) {
   }

   public static void markReady() {
   }

   public static void markFailed(String message) {
   }

   public static boolean isBadlionRuntime() {
      return false;
   }

   public static boolean diagnosticsEnabled() {
      return false;
   }

   public static void reportDiagnostic(String message) {
   }

   public static void recordRuntimeSignal(String category, String name) {
   }

   public static void recordRuntimeFailure(String category, String name, Throwable failure) {
   }

   public static void recordBadlionGradient(String stage, String detail) {
   }

   public static String describeFailure(Throwable failure) {
      return failure == null ? "unknown" : failure.getClass().getName() + ": " + failure.getMessage();
   }
}
