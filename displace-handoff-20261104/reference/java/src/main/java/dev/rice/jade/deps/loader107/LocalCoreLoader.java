package dev.rice.jade.deps.loader107;

/**
 * Rice host shim: upstream boots Jade's whole client core on first tick. Rice registers only the ported
 * modules (done by JadeRuntime), so there is nothing further to boot.
 */
public final class LocalCoreLoader {
   private LocalCoreLoader() {
   }

   public static synchronized void bootLocalCore(ClassLoader loader) throws Exception {
   }
}
