package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.openjade.JadeModuleAdapter;
import dev.rice.client.openjade.JadeRuntime;
import dev.rice.jade.client.setting.Setting;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;

/**
 * Rice GUI/config face of OpenJade's Opsec (Hypixel Bed Wars layout/ping privacy; ported verbatim under
 * dev.rice.jade). "Setup" is upstream's action button: Rice has no button control, so it is a momentary
 * toggle that runs the button's action and switches itself back off.
 */
public final class OpsecModule extends JadeModuleAdapter {
   private final BooleanSetting setup;

   public OpsecModule(ModuleManager modules) {
      super(modules, "opsec", "Opsec", "Hide identifying server-side Bed Wars layouts.", ModuleCategory.UTILITY);
      this.bind(this.add(new BooleanSetting("anti_quickbuy_identify", "Anti Quickbuy Identify", true)), "Anti Quickbuy Identify");
      this.bind(this.add(new BooleanSetting("anti_ping_identify", "Anti Ping Identify", false)), "Anti Ping Identify");
      this.bind(this.add(new NumberSetting("ping_boost", "Ping Boost (ms)", 100, 50, 1000, 50)), "Ping Boost");
      this.setup = this.add(new BooleanSetting("setup", "Setup", false));
   }

   @Override
   protected dev.rice.jade.client.module.Module jade() {
      this.ensureRuntime();
      return JadeRuntime.opsec();
   }

   @Override
   public int metadataCategory() {
      return 4;
   }

   @Override
   public boolean afterSettingControl(String key, ClientContext context) {
      super.afterSettingControl(key, context);
      if ("setup".equals(key) && this.setup.isEnabled()) {
         this.setup.set(false);
         for (Setting setting : this.jade().getSettings()) {
            if ("Setup".equals(setting.getName()) && setting instanceof dev.rice.jade.client.setting.BooleanSetting) {
               ((dev.rice.jade.client.setting.BooleanSetting)setting).runAction();
            }
         }

         return true;
      }

      return false;
   }
}
