// Jade recovery: module: Rendering (client); original class: jade.deps.eLz.Szyn3b
package dev.rice.jade.client.module.client;

import dev.rice.jade.client.Jade;
import dev.rice.jade.client.module.Category;
import dev.rice.jade.client.module.Module;
import dev.rice.jade.client.module.ModuleInfo;
import dev.rice.jade.client.setting.SliderSetting;

@ModuleInfo
public final class Rendering extends Module {
   // Rice host shell: only Rendering.isExternalOutput is ported. Rice has no Jade Rendering module, so
   // getModule(Rendering.class) is null and chat always goes to the vanilla chat (upstream's default).
   private Rendering() {
      super("Rendering", Category.client);
      this.output = null;
   }

   private final SliderSetting output;

   public static boolean isExternalOutput() {
      if (Jade.getModuleManager() == null) {
         return false;
      } else {
         Rendering var0 = Jade.getModuleManager().getModule(Rendering.class);
         return var0 != null && var0.output.getInput() == 1.0;
      }
   }
}
