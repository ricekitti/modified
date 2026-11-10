package dev.rice.jade.client.module.combat;

import dev.rice.jade.client.module.Category;
import dev.rice.jade.client.module.Module;
import dev.rice.jade.client.setting.BooleanSetting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

/**
 * Rice host stub (type only). Jade's AutoClicker is not part of Rice, so it is never registered and
 * {@code getModule(AutoClicker.class)} is null; the members exist only so the verbatim Jade hooks compile.
 */
public final class AutoClicker extends Module {
   public BooleanSetting whilstShiftingInInventory;

   private AutoClicker() {
      super("AutoClicker", Category.combat);
   }

   public void setInventoryTarget(GuiContainer container, Slot slot) {
      throw new IllegalStateException("Jade AutoClicker is not part of Rice");
   }
}
