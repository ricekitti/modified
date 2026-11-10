package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.combat.openb4.OpenB4AntiFireballBackend;
import dev.rice.client.combat.openb4.OpenB4AntiFireballHost;
import dev.rice.client.openjade.JadeRuntime;
import dev.rice.client.rotation.RotationService;
import dev.rice.inject.ReflectionHelper;
import dev.rice.jade.client.common.RotationHandler;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import java.lang.reflect.Field;
import net.minecraft.entity.Entity;

/**
 * Rice lifecycle/settings adapter for the generated OpenB4-u20 AntiFireball backend.
 *
 * <p>OpenB4 timing as Rice events:
 * <ul>
 *   <li>Hooks.preUpdate (Hypixel.tick, onPreUpdateAlways, then the rotation request) runs at event 17.</li>
 *   <li>Module.onUpdate (ClientTick END) runs at onClientTick.</li>
 *   <li>EntityJoinWorldEvent comes from the World.spawnEntityInWorld head hook.</li>
 * </ul>
 *
 * <p>OpenB4's RotationManager polls AntiFireball last, so it only rotates when no earlier provider rotates.
 * The earlier providers in Rice are:
 * <ul>
 *   <li>Mayu Scaffold and BedNuker (claim 750)</li>
 *   <li>Knockback Displace</li>
 *   <li>Block In</li>
 *   <li>Aim Assist (claims 100 per frame)</li>
 *   <li>KillAura, when it actually published a rotation this tick</li>
 * </ul>
 * KillAura claims 500 every tick it is enabled, whether or not it has a target. Its {@code published} field is read
 * (never written) to tell those cases apart, the same way KillAura reads Knockback Displace's {@code active} field.
 */
public final class AntiFireballModule extends Module {
   private static final Field MANUAL_DISPLACE_ACTIVE = ReflectionHelper.findField(KnockbackDisplaceModule.class, "active");
   private static final Field KILLAURA_PUBLISHED = ReflectionHelper.findField(KillAuraModule.class, "published");
   private static final Field ROTATION_PRIORITY = ReflectionHelper.findField(RotationService.class, "priority");
   private static final int CLAIM = 50;
   private static final int KILLAURA_CLAIM = 500;
   private final OpenB4AntiFireballBackend backend = new OpenB4AntiFireballBackend();
   private final ModuleManager modules;
   private final OpenB4AntiFireballHost.ButtonSetting[] buttons;
   private float[] published;
   private boolean frameOpen;
   private boolean jadeRotatedLastTick;

   public AntiFireballModule(ModuleManager modules) {
      super("antifireball", "Anti Fireball", "Hits incoming fireballs back", ModuleCategory.WORLD);
      this.modules = modules;
      OpenB4AntiFireballBackend.fov = new OpenB4AntiFireballHost.SliderSetting(this.add(new NumberSetting("fov", "FOV", 360.0, 20.0, 360.0, 5.0)));
      OpenB4AntiFireballBackend.range = new OpenB4AntiFireballHost.SliderSetting(this.add(new NumberSetting("range", "Range", 10.0, 3.0, 20.0, 0.5)));
      OpenB4AntiFireballBackend.disableWhileFlying = new OpenB4AntiFireballHost.ButtonSetting(this.add(new BooleanSetting("disable_while_flying", "Disable while flying", true)));
      OpenB4AntiFireballBackend.disableWhileScaffold = new OpenB4AntiFireballHost.ButtonSetting(this.add(new BooleanSetting("disable_while_scaffold", "Disable while scaffold", false)));
      OpenB4AntiFireballBackend.rotateWithBlocks = new OpenB4AntiFireballHost.ButtonSetting(this.add(new BooleanSetting("rotate_with_blocks", "Rotate with blocks", false)));
      OpenB4AntiFireballBackend.rotateWithProjectiles = new OpenB4AntiFireballHost.ButtonSetting(this.add(new BooleanSetting("rotate_with_projectiles", "Rotate with projectiles", false)));
      OpenB4AntiFireballBackend.silentSwing = new OpenB4AntiFireballHost.ButtonSetting(this.add(new BooleanSetting("silent_swing", "Silent swing", false)));
      this.buttons = new OpenB4AntiFireballHost.ButtonSetting[]{
         OpenB4AntiFireballBackend.disableWhileFlying, OpenB4AntiFireballBackend.disableWhileScaffold, OpenB4AntiFireballBackend.rotateWithBlocks,
         OpenB4AntiFireballBackend.rotateWithProjectiles, OpenB4AntiFireballBackend.silentSwing
      };
      this.backend.guiUpdate();
   }

   @Override
   public int metadataCategory() {
      return 3;
   }

   @Override
   public boolean listens(int type) {
      return type == 16 || type == 17 || type == 13;
   }

   /** OpenB4 settings are read live; Rice settings may change from the GUI or a config load. */
   private void sync() {
      for (OpenB4AntiFireballHost.ButtonSetting button : this.buttons) {
         button.sync();
      }

      Module scaffold = this.modules.find("scaffold");
      OpenB4AntiFireballHost.ScaffoldModule.active = scaffold != null && scaffold.isEnabled();
      this.backend.guiUpdate();
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.published = null;
      this.sync();
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.backend.onDisable();
      this.published = null;
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      this.published = null;
   }

   @Override
   public boolean afterSettingControl(String key, ClientContext context) {
      this.sync();
      return super.afterSettingControl(key, context);
   }

   /** OpenB4 ClickGuiMod.onTick (ClientTickEvent END) -> Module.onUpdate. */
   @Override
   public void onClientTick(ClientContext context) {
      if (context.ready()) {
         this.sync();
         this.backend.onUpdate();
      }
   }

   /** Forge EntityJoinWorldEvent while the module is enabled (OpenB4 registers @SubscribeEvent handlers on enable). */
   public void entityJoined(ClientContext context, Entity entity) {
      if (context.ready()) {
         this.sync();
         this.backend.onJoin(new OpenB4AntiFireballHost.EntityJoinWorldEvent(entity));
      }
   }

   @Override
   public void onEvent(ClientContext context, SemanticEvent event) {
      if (!context.ready()) {
         this.published = null;
         return;
      }

      if (event.type == 16) {
         // Claims that outlive a tick (Aim Assist claims 100 every frame) are only visible before this tick's own
         // claims. A 500 here can only be KillAura's per-tick claim from an earlier tick of this same frame.
         int priority = priority();
         this.frameOpen = priority <= CLAIM || priority == KILLAURA_CLAIM && this.frameOpen;
      } else if (event.type == 17) {
         this.sync();
         this.published = null;
         // Hooks.preUpdate: Hypixel.tick() (state = detect()) ... AntiFireball.onPreUpdateAlways()
         OpenB4AntiFireballBackend.Hypixel.state = OpenB4AntiFireballBackend.Hypixel.detect();
         this.backend.onPreUpdateAlways();
         // Hooks.onUpdate: RotationManager.request(lastYaw, lastPitch). The rotating/server fields hold the state
         // left by the previous walking update.
         boolean jadeRotating = JadeRuntime.active() && RotationHandler.getInstance().abxpJn();
         OpenB4AntiFireballBackend.RotationManager.rotating = RotationService.storedRotationValid() || this.jadeRotatedLastTick;
         OpenB4AntiFireballBackend.RotationManager.wasRotating = OpenB4AntiFireballBackend.RotationManager.rotating;
         OpenB4AntiFireballBackend.RotationManager.serverYaw = OpenB4AntiFireballBackend.RotationManager.lastYaw;
         OpenB4AntiFireballBackend.RotationManager.serverPitch = OpenB4AntiFireballBackend.RotationManager.lastPitch;
         this.jadeRotatedLastTick = jadeRotating;
         if (!this.earlierProviderIdle(jadeRotating)) {
            return;
         }

         float[] rotations = this.backend.getRotations(OpenB4AntiFireballBackend.RotationManager.lastYaw, OpenB4AntiFireballBackend.RotationManager.lastPitch);
         if (rotations == null || priority() <= CLAIM && !RotationService.claim(CLAIM)) {
            return;
         }

         // Same silent publish path as the Rice KillAura port: the camera is kept by RotationService, and
         // rotationYaw carries the server rotation through this tick's movement (OpenB4 movement fix) and C03.
         RotationService.retain(context, false, context.player().rotationYaw, context.player().rotationPitch);
         context.player().rotationYaw = rotations[0];
         context.player().rotationPitch = rotations[1];
         OpenB4AntiFireballBackend.RotationManager.serverYaw = rotations[0];
         OpenB4AntiFireballBackend.RotationManager.serverPitch = rotations[1];
         this.published = rotations;
      } else if (event.type == 13 && this.published != null) {
         event.active = true;
         event.overrideRotation = false;
      }
   }

   /** RotationManager.PROVIDERS order: every provider before AntiFireball must have returned null this tick. */
   private boolean earlierProviderIdle(boolean jadeRotating) {
      if (!this.frameOpen || jadeRotating || this.manualDisplaceActive() || this.blockInActive()) {
         return false;
      }

      int priority = priority();
      return priority <= CLAIM || priority == KILLAURA_CLAIM && this.killAuraIdle();
   }

   private boolean killAuraIdle() {
      Module m = this.modules.find("killaura");
      if (!(m instanceof KillAuraModule) || !m.isEnabled()) {
         return false;
      }

      try {
         return KILLAURA_PUBLISHED.get(m) == null;
      } catch (IllegalAccessException ex) {
         throw new IllegalStateException(ex);
      }
   }

   private boolean manualDisplaceActive() {
      Module m = this.modules.find("knockbackdisplace");
      if (!(m instanceof KnockbackDisplaceModule) || !m.isEnabled()) {
         return false;
      }

      try {
         return MANUAL_DISPLACE_ACTIVE.getBoolean(m);
      } catch (IllegalAccessException ex) {
         throw new IllegalStateException(ex);
      }
   }

   private boolean blockInActive() {
      Module m = this.modules.find("blockin");
      return m instanceof BlockInModule && m.isEnabled() && ((BlockInModule)m).active;
   }

   private static int priority() {
      try {
         return ROTATION_PRIORITY.getInt(null);
      } catch (IllegalAccessException ex) {
         throw new IllegalStateException(ex);
      }
   }
}
