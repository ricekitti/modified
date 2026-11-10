package dev.rice.jade.client.module.player;

import dev.rice.jade.client.module.Category;
import dev.rice.jade.client.module.Module;

/** Rice host stub (type only): Jade's InventoryManager is not part of Rice, so getModule(InventoryManager.class) is null. */
public final class InventoryManager extends Module {
   private InventoryManager() {
      super("InventoryManager", Category.player);
   }

   public boolean isExecutingPlan() {
      return false;
   }
}
