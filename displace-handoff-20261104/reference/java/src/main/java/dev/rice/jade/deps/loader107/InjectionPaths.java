package dev.rice.jade.deps.loader107;

import java.io.File;

/**
 * Rice host shim: Jade keeps its data (opsec.json, the custom-font folder, dangerous-module
 * acknowledgements) in its own directory; Rice keeps it in .minecraft/rice/openjade.
 */
public final class InjectionPaths {
   private InjectionPaths() {
   }

   public static synchronized File dataDirectory(File minecraftDataDirectory) {
      File directory = new File(new File(minecraftDataDirectory, "rice"), "openjade");
      if (!directory.isDirectory()) {
         directory.mkdirs();
      }

      return directory;
   }
}
