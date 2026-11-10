package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.SemanticEvent;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;

public final class NoJumpDelayModule extends EvidenceModule {
   public NoJumpDelayModule() {
      super("nojumpdelay", "No Jump Delay", ModuleCategory.MOVEMENT);
   }

   @Override
   public boolean listens(int type) {
      return type == 17;
   }

   @Override
   public void onEvent(ClientContext context, SemanticEvent event) {
      if (context.inputReady()) {
         MinecraftFields.setJumpTicks(context.player(), 0);
      }
   }
}
