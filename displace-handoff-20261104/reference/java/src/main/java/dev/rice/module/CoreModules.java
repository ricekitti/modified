package dev.rice.module;

import dev.rice.module.impl.AimAssistModule;
import dev.rice.module.impl.AntiBotModule;
import dev.rice.module.impl.AntiCheatModule;
import dev.rice.module.impl.TargetHudModule;
import dev.rice.module.impl.AntiFireballModule;
import dev.rice.module.impl.AntiDebuffModule;
import dev.rice.module.impl.AutoBlockModule;
import dev.rice.module.impl.AutoClickerModule;
import dev.rice.module.impl.RightClickerModule;
import dev.rice.module.impl.AutoPotModule;
import dev.rice.module.impl.AutoRodModule;
import dev.rice.module.impl.AutoSoupModule;
import dev.rice.module.impl.AutoToolModule;
import dev.rice.module.impl.AutoWeaponModule;
import dev.rice.module.impl.BacktrackModule;
import dev.rice.module.impl.BlinkModule;
import dev.rice.module.impl.BlockInModule;
import dev.rice.module.impl.BlockEspModule;
import dev.rice.module.impl.BridgeAssistModule;
import dev.rice.module.impl.ChamsModule;
import dev.rice.module.impl.ChestEspModule;
import dev.rice.module.impl.ChestStealerModule;
import dev.rice.module.impl.AutoClutchModule;
import dev.rice.module.impl.BowAimbotModule;
import dev.rice.module.impl.BedNukerModule;
import dev.rice.module.impl.CriticalsModule;
import dev.rice.module.impl.DebugModule;
import dev.rice.module.impl.FakeLagModule;
import dev.rice.module.impl.FastAccelModule;
import dev.rice.module.impl.FastMineModule;
import dev.rice.module.impl.FastPlaceModule;
import dev.rice.module.impl.FreeLookModule;
import dev.rice.module.impl.FriendsModule;
import dev.rice.module.impl.GhostHandModule;
import dev.rice.module.impl.GuiModule;
import dev.rice.module.impl.HitSelectModule;
import dev.rice.module.impl.HitboxesModule;
import dev.rice.module.impl.IndicatorsModule;
import dev.rice.module.impl.InstantStopModule;
import dev.rice.module.impl.InvManagerModule;
import dev.rice.module.impl.InvWalkModule;
import dev.rice.module.impl.ItemEspModule;
import dev.rice.module.impl.ItemUseFixModule;
import dev.rice.module.impl.KeepSprintModule;
import dev.rice.module.impl.KillAuraDisplaceModule;
import dev.rice.module.impl.KillAuraModule;
import dev.rice.module.impl.StasisModule;
import dev.rice.module.impl.KnockbackDelayModule;
import dev.rice.module.impl.KnockbackDisplaceModule;
import dev.rice.module.impl.LagRangeModule;
import dev.rice.module.impl.ModOverlayModule;
import dev.rice.module.impl.NameTagsModule;
import dev.rice.module.impl.NoHitDelayModule;
import dev.rice.module.impl.NoHurtCamModule;
import dev.rice.module.impl.NoItemReleaseModule;
import dev.rice.module.impl.NoJumpDelayModule;
import dev.rice.module.impl.NoSlowModule;
import dev.rice.module.impl.NoUseDelayModule;
import dev.rice.module.impl.NotificationsModule;
import dev.rice.module.impl.NullMoveModule;
import dev.rice.module.impl.PiercingModule;
import dev.rice.module.impl.PingFixModule;
import dev.rice.module.impl.PlayerEspModule;
import dev.rice.module.impl.PointersModule;
import dev.rice.module.impl.ReachModule;
import dev.rice.module.impl.RefillModule;
import dev.rice.module.impl.SafeWalkModule;
import dev.rice.module.impl.ShaderModule;

import dev.rice.module.impl.OpsecModule;
import dev.rice.module.impl.BedDefenderModule;
import dev.rice.module.impl.FireballBoostModule;
import dev.rice.module.impl.FireballEspModule;
import dev.rice.module.impl.FireballPearlModule;
import dev.rice.module.impl.FogModule;
import dev.rice.module.impl.SpeedModule;
import dev.rice.module.impl.SprintModule;
import dev.rice.module.impl.SprintResetModule;
import dev.rice.module.impl.StrafeModule;
import dev.rice.module.impl.TeamsModule;
import dev.rice.module.impl.TimerModule;
import dev.rice.module.impl.TimerRangeModule;
import dev.rice.module.impl.TrajectoriesModule;
import dev.rice.module.impl.VelocityModule;
import dev.rice.module.impl.WeaponsModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CoreModules {
   private CoreModules() {
   }

   public static void register(ModuleManager manager) {
      WeaponsModule weapons = new WeaponsModule();
      List<Module> implemented = new ArrayList<>();
      implemented.add(weapons);
      implemented.add(new TimerModule());
      implemented.add(new AntiDebuffModule());
      implemented.add(new NoJumpDelayModule());
      implemented.add(new NullMoveModule());
      implemented.add(new NoHitDelayModule());
      implemented.add(new FastMineModule());
      implemented.add(new NoUseDelayModule());
      implemented.add(new AutoToolModule());
      implemented.add(new AutoWeaponModule(weapons));
      implemented.add(new NoHurtCamModule());
      implemented.add(new SprintModule());
      implemented.add(new NoSlowModule());
      implemented.add(new ItemUseFixModule());
      implemented.add(new ReachModule());
      implemented.add(new HitboxesModule());
      implemented.add(new SafeWalkModule());
      implemented.add(new FastAccelModule(weapons));
      implemented.add(new AntiBotModule());
      implemented.add(new TeamsModule());
      implemented.add(new FriendsModule());
      implemented.add(new PiercingModule(weapons));
      implemented.add(new GhostHandModule());
      implemented.add(new NoItemReleaseModule());
      implemented.add(new PingFixModule());
      implemented.add(new AimAssistModule(weapons));
      implemented.add(new FreeLookModule());
      implemented.add(new StrafeModule(weapons));
      implemented.add(new InstantStopModule());
      implemented.add(new KeepSprintModule());
      implemented.add(new SprintResetModule(weapons));
      implemented.add(new VelocityModule(weapons, manager));
      implemented.add(new FakeLagModule(weapons));
      implemented.add(new CriticalsModule(weapons));
      implemented.add(new BlinkModule(manager));
      implemented.add(new BacktrackModule(weapons));
      implemented.add(new LagRangeModule(weapons));
      implemented.add(new TimerRangeModule(weapons));
      implemented.add(new KnockbackDelayModule(weapons));
      implemented.add(new DebugModule());
      implemented.add(new KnockbackDisplaceModule(weapons, manager));
      implemented.add(new AutoSoupModule(manager));
      implemented.add(new AutoPotModule(manager));
      implemented.add(new AutoRodModule(manager, weapons));
      implemented.add(new RefillModule(manager));
      implemented.add(new InvWalkModule());
      implemented.add(new InvManagerModule());
      implemented.add(new AutoClickerModule(manager, weapons));
      implemented.add(new AutoBlockModule(manager));
      implemented.add(new BridgeAssistModule());
      implemented.add(new FastPlaceModule());
      implemented.add(new HitSelectModule(weapons));
      implemented.add(new BlockInModule(manager));
      implemented.add(new PlayerEspModule());
      implemented.add(new ChestEspModule());
      implemented.add(new NameTagsModule());
      implemented.add(new PointersModule());
      implemented.add(new IndicatorsModule());
      implemented.add(new BlockEspModule());
      implemented.add(new ItemEspModule());
      implemented.add(new TrajectoriesModule());
      implemented.add(new ChamsModule());
      implemented.add(new ModOverlayModule(manager));
      implemented.add(new NotificationsModule(manager));
      implemented.add(new GuiModule(manager));
      Map<String, Module> byName = new LinkedHashMap<>();

      for (Module module : implemented) {
         if (byName.put(module.getName(), module) != null) {
            throw new IllegalStateException("Duplicate implementation " + module.getName());
         }
      }

      for (String name : EvidenceCatalog.moduleNames()) {
         if (EvidenceCatalog.descriptorCategory(name) >= 0) {
            Module modulex = byName.remove(name);
            if (modulex == null) {
               throw new IllegalStateException("Missing implementation " + name);
            }

            manager.register(modulex);
         }
      }

      if (!byName.isEmpty()) {
         throw new IllegalStateException("Unmatched implementations " + byName.keySet());
      }

      // KillAura and KillAura Displace are REMOVED from the client (not registered). Their classes stay compiled
      // because other modules reference the types (AntiFireball reads KillAuraModule.published by reflection and
      // find("killaura"); TargetHUD renders OpenB4KillAuraBackend's target) and both reference paths are null-safe
      // when no KillAura is registered. Re-add the two register calls below to restore them.
      manager.register(new StasisModule());
      // Right Clicker: the Auto Clicker for the right mouse button; a source extension, not in the recovered catalogue.
      manager.register(new RightClickerModule());
      // Source extensions, not part of the recovered catalogue. Mayu Scaffold and BedNuker (byte-for-byte from
      // Downloads/meow, tools/port-mayu.py) share Mayu's own event bus.
      manager.register(new ShaderModule(manager));
      // Continuous Scaffold removed; the independent OG Clutch remains registered below.

      manager.register(new BedNukerModule(manager));
      manager.register(new OpsecModule(manager));
      manager.register(new FireballPearlModule(manager));
      // v159: Bed Defender and Fireball Boost removed from the client (unregistered).
      manager.register(new FogModule());
      // OpenB4-u20 AntiFireball. Registered last so its event-17 rotation request runs after KillAura's
      // (OpenB4 RotationManager polls AntiFireball after every other provider).
      manager.register(new AntiFireballModule(manager));
      // Sam's Raven script FireballESP.java as a native module (tools/port-raven-fireballesp.py).
      manager.register(new FireballEspModule());
      // Complete Snowify Anticheat detectors, blacklist, HUD and markers.
      manager.register(new AntiCheatModule(manager));
      // OpenB4-u20 TargetHUD, generated verbatim (tools/port-openb4-targethud.py); drawn by KillAura's restored HUD lines.
      manager.register(new TargetHudModule(manager));
      // v159: Auto Clutch removed from the client (unregistered). The restored OG Clutch (id "clutch") stays.
      // Bow Aimbot: predictive bow + rod aimbot, backend byte-for-byte.
      manager.register(new BowAimbotModule());
      // "Clutch" â€” the original Slinky fall-clutch scaffold-aim module, restored from OpenSlinky and ported onto
      // Rice (dev.rice.module.impl.ClutchModule). Replaces the removed Vape V4.21 Block-In clutch; id "clutch",
      // the 18 original config keys. Does not touch the AutoClutch or the existing Block In. Owns placement + rotations.
      manager.register(new dev.rice.module.impl.ClutchModule(manager));
   }

   public static List<Module> sourceOnlyIntervals() {
      return Collections.unmodifiableList(Arrays.asList(new Module[]{new ChestStealerModule(), new SpeedModule()}));
   }
}

