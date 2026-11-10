package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.stasis.JadeStasisBackend;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.BooleanSetting;

/** OpenJade Stasis settings and lifecycle, backed by its movement/packet implementation. */
public final class StasisModule extends Module {
   private final BooleanSetting hypixelMode = this.add(new BooleanSetting("hypixel_mode", "Hypixel Mode", false));
   private final BooleanSetting shiftPlace = this.add(new BooleanSetting("shift_place_through_walls", "Shift place through walls", false));
   private JadeStasisBackend backend;

   public StasisModule() {
      super("stasis", "Stasis", "Freezes your position and movement", ModuleCategory.MOVEMENT);
   }

   @Override
   public int metadataCategory() {
      return 1;
   }

   @Override
   public boolean listens(int type) {
      return type == 2 || type == 14 || type == 16 || type == 18 || type == 23 || type == SemanticEvent.MOVEMENT_INPUT;
   }

   @Override
   public void onEvent(ClientContext c, SemanticEvent event) {
      if (this.backend != null) this.backend.dispatch(event);
   }

   @Override
   public String statusText() {
      return this.isHoldingPackets() ? "Stasis " + Math.round(this.getProgressFraction() * 100F) + "%"
         : this.hypixelMode.isEnabled() ? "Hypixel Mode" : "Stasis";
   }

   @Override public boolean settingVisible(String key) {
      return !key.equals("shift_place_through_walls") || this.hypixelMode.isEnabled();
   }
   @Override protected void onEnable(ClientContext c) {
      this.backend = new JadeStasisBackend(c, this.hypixelMode, this.shiftPlace);
      this.backend.onEnable();
   }
   @Override protected void onDisable(ClientContext c) {
      if (this.backend != null) this.backend.onDisable();
   }
   @Override public void onWorldUnload(ClientContext c) {
      if (this.backend != null) this.backend.onLoadWorld(new SemanticEvent(26));
   }
   @Override public void onClientTickStart(ClientContext c) {
      if (this.backend != null) this.backend.onTickStart(new SemanticEvent(0));
   }
   public boolean isHypixelModeActive() { return this.isEnabled() && this.hypixelMode.isEnabled(); }
   public boolean isHoldingPackets() { return this.isEnabled() && this.backend != null && this.backend.isHoldingPackets(); }
   public float getProgressFraction() { return this.backend == null ? 0F : this.backend.getProgressFraction(); }
}
