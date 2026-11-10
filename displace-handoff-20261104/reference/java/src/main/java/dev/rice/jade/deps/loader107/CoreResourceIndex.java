package dev.rice.jade.deps.loader107;

import java.io.InputStream;

/**
 * Rice host shim for Jade's resource index. Jade's bundled fonts are shipped byte-identical under
 * assets/riceopenjade/fonts; the shaders Jade's UI uses are shipped byte-identical under
 * assets/riceopenjade/jade (so Rice never overrides Minecraft's own assets/minecraft namespace).
 */
public final class CoreResourceIndex {
   private CoreResourceIndex() {
   }

   public static synchronized InputStream openResource(String path) {
      if (path == null) {
         return null;
      }

      String mapped;
      if (path.startsWith("/assets/jade/fonts/")) {
         mapped = "/assets/riceopenjade/fonts/" + path.substring("/assets/jade/fonts/".length());
      } else if (path.startsWith("/assets/jade/")) {
         mapped = "/assets/riceopenjade/jade/" + path.substring("/assets/jade/".length());
      } else if (path.startsWith("/assets/minecraft/shaders/")) {
         mapped = "/assets/riceopenjade/jade/minecraft/shaders/" + path.substring("/assets/minecraft/shaders/".length());
      } else {
         return null;
      }

      return CoreResourceIndex.class.getResourceAsStream(mapped);
   }
}
