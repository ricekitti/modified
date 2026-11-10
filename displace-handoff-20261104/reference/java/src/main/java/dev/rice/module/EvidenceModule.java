package dev.rice.module;

import dev.rice.client.ItemPolicyService;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.ColorSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.setting.RangeSetting;
import dev.rice.setting.Setting;
import dev.rice.setting.StringListSetting;
import java.util.List;

public abstract class EvidenceModule extends Module {
   protected EvidenceModule(String id, String name, ModuleCategory category) {
      super(id, name, "Recovered " + name + " behavior", category);

      for (Setting<?> setting : EvidenceCatalog.settings(name)) {
         this.add(setting);
      }
   }

   protected final boolean flag(String key) {
      return ((BooleanSetting)this.findSetting(key)).isEnabled();
   }

   protected final float number(String key) {
      return ((NumberSetting)this.findSetting(key)).floatValue();
   }

   protected final int choice(String key) {
      return ((ChoiceSetting)this.findSetting(key)).index();
   }

   protected final RangeSetting.Value range(String key) {
      return ((RangeSetting)this.findSetting(key)).get();
   }

   protected final List<String> names(String key) {
      return ((StringListSetting)this.findSetting(key)).get();
   }

   protected final ColorSetting.Value color(String key) {
      return ((ColorSetting)this.findSetting(key)).get();
   }

   protected final void itemPolicy(String key) {
      ((StringListSetting)this.findSetting(key)).normalizeWith(ItemPolicyService::canonicalize);
   }
}
