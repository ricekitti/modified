package dev.rice.jade.client.module.minigames;

import dev.rice.jade.client.module.Category;
import dev.rice.jade.client.module.Module;
import net.minecraft.inventory.Slot;

/**
 * Rice host stub (type only). Jade's BedwarsUtils is not part of Rice, so {@code getModule(BedwarsUtils.class)}
 * is null and Opsec/the container hooks take upstream's fall-through {@code windowClick} path.
 */
public final class BedwarsUtils extends Module {
   private BedwarsUtils() {
      super("BedwarsUtils", Category.minigames);
   }

   public boolean handleQuickShopSlotClick(Slot slot, int slotId, int button, int mode) {
      throw new IllegalStateException("Jade BedwarsUtils is not part of Rice");
   }
}
