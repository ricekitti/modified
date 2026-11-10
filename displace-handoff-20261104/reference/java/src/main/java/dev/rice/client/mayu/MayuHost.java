package dev.rice.client.mayu;

import dev.rice.client.rotation.RotationService;
import dev.rice.module.ModuleManager;
import java.util.Objects;
import myau.BedNukerModule;
import myau.Module;
import myau.MyauClient;
import myau.InjectScaffoldView;
import myau.Setting;

/**
 * Rice side of the Mayu port (Scaffold + BedNuker, java/src/main/java/myau, tools/port-mayu.py).
 *
 * <ul>
 *   <li>{@link #install} runs upstream's {@code MyauClient.initialize} (host version) before any hook fires; Mayu's event
 *       bus is then live for the whole session, exactly as in Mayu, and every hook RicePatches places posts to it.</li>
 *   <li>The Rice Scaffold/BedNuker modules only mirror their enabled state and settings into the Mayu modules; Mayu's
 *       own {@code Module.setEnabled} runs their onEnable/onDisable.</li>
 *   <li>{@link #rotationAllowed}: Rice's per-frame rotation arbitration. While a Mayu rotating module is enabled, Mayu
 *       claims priority 750 (above KillAura 500, Displace 200, AimAssist 100, AntiFireball 50; below Block In 1000, which
 *       stands down while Scaffold is on). If the claim fails the RotationUpdateEvent is not posted that tick.</li>
 * </ul>
 */
public final class MayuHost {
   public static final int ROTATION_CLAIM = 750;

   private MayuHost() {
   }

   public static void install(ModuleManager modules) {
      MyauClient.initialize();
   }

   public static InjectScaffoldView scaffold() {
      MyauClient.initialize();
      return (InjectScaffoldView)MyauClient.moduleManager.modules.get(InjectScaffoldView.class);
   }

   public static BedNukerModule bedNuker() {
      MyauClient.initialize();
      return (BedNukerModule)MyauClient.moduleManager.modules.get(BedNukerModule.class);
   }

   public static boolean rotationAllowed() {
      if (!MyauClient.initialized()) {
         return true;
      }

      boolean rotating = scaffold().isEnabled() || bedNuker().isEnabled();
      return !rotating || RotationService.claim(ROTATION_CLAIM);
   }

   /** Rice enabled state into the Mayu module through upstream's own setEnabled (onEnable/onDisable). */
   public static void setEnabled(Module module, boolean enabled) {
      module.setEnabled(enabled);
   }

   /** Writes a Rice setting value into a Mayu setting only when it differs (Mayu's setValue notifies its module). */
   public static <T> void sync(Setting<T> setting, T value) {
      if (!Objects.equals(setting.getValue(), value)) {
         setting.setValue(value);
      }
   }

   /** Mayu BedNuker owns rotations/interactions (upstream KillAura stands down while it does). */
   public static boolean bedNukerControlling() {
      return MyauClient.initialized() && bedNuker().isEnabled() && bedNuker().isControllingInteractions();
   }
}
