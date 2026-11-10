// Jade recovery: original class: jade.deps.eLz.bj3B9yb6
package dev.rice.jade.client.common;

import dev.rice.jade.client.module.client.Rendering;
import java.util.ArrayList;
import java.util.List;

public final class ExternalChatOverlay {
   private static final List<ExternalChatOverlay$2> XTQhR = new ArrayList<>();

   public static boolean WBGA(String var0) {
      if (Rendering.isExternalOutput() && var0 != null && !var0.isEmpty()) {
         if (ChatUtils.hasFlowTag(var0)) {
            ChatUtils.resolveFlowText(var0);
         }

         synchronized (XTQhR) {
            XTQhR.add(new ExternalChatOverlay$2(var0, System.currentTimeMillis()));

            while (XTQhR.size() > 100) {
               XTQhR.remove(0);
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
