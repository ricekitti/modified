package dev.rice.jade.client.module.player;

import dev.rice.jade.client.module.Category;
import dev.rice.jade.client.module.Module;
import dev.rice.jade.client.setting.BooleanSetting;

/** Rice host stub (type only): Jade's DelayRemover is not part of Rice, so getModule(DelayRemover.class) is null. */
public final class DelayRemover extends Module {
   public BooleanSetting legacyHitreg;

   private DelayRemover() {
      super("DelayRemover", Category.player);
   }
}
