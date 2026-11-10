package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.mayu.MayuHost;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;

/**
 * Rice ClickGUI/config face of Mayu's BedNuker, including Rice's NUKER PATCH layered-defense route mode. The adapter
 * mirrors enabled state and all 12 backend settings while preserving the existing LEGIT and SWAP indices.
 */
public final class BedNukerModule extends Module {
   public BedNukerModule(ModuleManager modules) {
      super("bednuker", "BedNuker", "Breaks beds (and their cover) in range", ModuleCategory.WORLD);
      add(new ChoiceSetting("mode", "Mode", 0, "LEGIT", "SWAP", "NUKER PATCH"));
      add(new NumberSetting("range", "Range", 4.5, 3.0, 6.0, 0.1));
      add(new NumberSetting("speed", "Speed (%)", 0, 0, 100, 1));
      add(new BooleanSetting("ground_spoof", "Ground Spoof", false));
      add(new ChoiceSetting("ignore_velocity", "Ignore Velocity", 0, "NONE", "CANCEL", "DELAY"));
      add(new BooleanSetting("surroundings", "Surroundings", true));
      add(new BooleanSetting("tool_check", "Tool Check", true));
      add(new BooleanSetting("whitelist", "Whitelist", true));
      add(new BooleanSetting("swing", "Swing", true));
      add(new ChoiceSetting("move_fix", "Move Fix", 1, "NONE", "SILENT", "STRICT"));
      add(new ChoiceSetting("show_target", "Show Target", 1, "NONE", "DEFAULT", "HUD"));
      add(new ChoiceSetting("show_progress", "Show Progress", 1, "NONE", "DEFAULT", "HUD"));
   }

   private int mode(String key) {
      return ((ChoiceSetting)findSetting(key)).index();
   }

   private double number(String key) {
      return ((NumberSetting)findSetting(key)).doubleValue();
   }

   private boolean flag(String key) {
      return ((BooleanSetting)findSetting(key)).isEnabled();
   }

   /** Rice settings into myau.BedNukerModule's own settings (only values that differ are written). */
   public void sync() {
      myau.BedNukerModule mayu = MayuHost.bedNuker();
      MayuHost.sync(mayu.mode, mode("mode"));
      MayuHost.sync(mayu.range, (float)number("range"));
      MayuHost.sync(mayu.speed, (int)number("speed"));
      MayuHost.sync(mayu.groundSpoof, flag("ground_spoof"));
      MayuHost.sync(mayu.ignoreVelocity, mode("ignore_velocity"));
      MayuHost.sync(mayu.surroundings, flag("surroundings"));
      MayuHost.sync(mayu.toolCheck, flag("tool_check"));
      MayuHost.sync(mayu.whitelist, flag("whitelist"));
      MayuHost.sync(mayu.swing, flag("swing"));
      MayuHost.sync(mayu.moveFix, mode("move_fix"));
      MayuHost.sync(mayu.showTarget, mode("show_target"));
      MayuHost.sync(mayu.showProgress, mode("show_progress"));
   }

   @Override
   protected void onEnable(ClientContext context) {
      sync();
      MayuHost.setEnabled(MayuHost.bedNuker(), true);
   }

   @Override
   protected void onDisable(ClientContext context) {
      MayuHost.setEnabled(MayuHost.bedNuker(), false);
   }

   /** Keeps Mayu's settings equal to Rice's after a config/profile load (no-op when nothing changed). */
   @Override
   public void onClientTickStart(ClientContext context) {
      sync();
   }

   @Override
   public boolean afterSettingControl(String key, ClientContext context) {
      sync();
      return false;
   }

   @Override
   public int metadataCategory() {
      return 3;
   }
}
