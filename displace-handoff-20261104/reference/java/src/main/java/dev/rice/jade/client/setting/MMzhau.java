package dev.rice.jade.client.setting;

import dev.rice.jade.client.module.Module;

/**
 * Rice host shim: upstream JZYRBC re-lays out Jade's ClickGui panels after a setting's visibility changes.
 * Rice shows these modules in its own GUI (visibility comes from each adapter's settingVisible), so there
 * is no Jade panel to refresh.
 */
public final class MMzhau {
   private MMzhau() {
   }

   public static void JZYRBC(Module module) {
   }
}
