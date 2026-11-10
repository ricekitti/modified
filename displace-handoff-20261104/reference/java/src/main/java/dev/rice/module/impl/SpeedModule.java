package dev.rice.module.impl;

import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;

public final class SpeedModule extends EvidenceModule {
   public SpeedModule() {
      super("speed", "Speed", ModuleCategory.MOVEMENT);
      this.reorderSettings(new String[]{"speed", "require_ground"});
   }
}
