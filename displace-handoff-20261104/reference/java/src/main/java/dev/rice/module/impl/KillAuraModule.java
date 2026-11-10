package dev.rice.module.impl;

import dev.rice.client.*;
import dev.rice.client.combat.openb4.*;
import dev.rice.client.combat.ravenbs.*;
import dev.rice.client.rotation.RotationService;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.*;
import java.lang.reflect.Field;

/** Rice lifecycle/settings adapter for the Raven bS (keystrokesmod) KillAura reference. */
public final class KillAuraModule extends Module {
   /** Read-only view of the v72 Knockback Displace manual activation; that module's code is never modified. */
   private static final Field MANUAL_DISPLACE_ACTIVE = ReflectionHelper.findField(KnockbackDisplaceModule.class, "active");
   private final OpenB4KillAuraBackend legacyBackend = new OpenB4KillAuraBackend();
   private final RavenBsKillAuraBackend backend = new RavenBsKillAuraBackend();
   private float[] published;
   private boolean initialized;
   private final ModuleManager modules;
   private boolean calculated;
   private boolean pendingDisplaced;
   private boolean raySwapped;
   private boolean rayAllowed;
   private float rayYaw, rayPitch, rayPrevYaw, rayPrevPitch;
   public KillAuraModule(WeaponsModule weapons, ModuleManager modules) {
      super("killaura", "Kill Aura", "Raven bS targeting, rotations and vanilla click attacks", ModuleCategory.COMBAT);
      this.modules = modules;
      B4AuraHost.weapons = weapons;
      B4AuraHost.modules = modules;
      OpenB4KillAuraBackend.aps = new B4AuraHost.SliderSetting(add(new NumberSetting("target_cps", "Target CPS", 10.0, 1.0, 20.0, 0.5)));
      OpenB4KillAuraBackend.attackOnDamage = new B4AuraHost.SliderSetting(add(new NumberSetting("attack_on_damage", "Attack on damage", 3.0, 0.0, 10.0, 1.0)));
      OpenB4KillAuraBackend.autoBlockSetting = new B4AuraHost.SliderSetting(add(new ChoiceSetting("autoblock", "AutoBlock", 0, "Manual", "Vanilla", "Damage", "Fake", "Interact A", "Interact B", "Interact C", "Interact D", "Swap", "Dual")));
      OpenB4KillAuraBackend.autoBlockAps = new B4AuraHost.SliderSetting(add(new NumberSetting("autoblock_aps", "AutoBlock APS", 10.0, 1.0, 20.0, 0.5)));
      OpenB4KillAuraBackend.autoBlockLag = new B4AuraHost.SliderSetting(add(new NumberSetting("autoblock_lag", "AutoBlock lag", 100.0, 0.0, 100.0, 2.0)));
      OpenB4KillAuraBackend.fov = new B4AuraHost.SliderSetting(add(new NumberSetting("fov", "FOV", 360.0, 30.0, 360.0, 4.0)));
      OpenB4KillAuraBackend.attackRangeSetting = new B4AuraHost.SliderSetting(add(new NumberSetting("attack_range", "Range (attack)", 3.0, 3.0, 6.0, 0.05)));
      OpenB4KillAuraBackend.swingRangeSetting = new B4AuraHost.SliderSetting(add(new NumberSetting("swing_range", "Range (swing)", 4.5, 3.0, 8.0, 0.05)));
      OpenB4KillAuraBackend.blockRangeSetting = new B4AuraHost.SliderSetting(add(new NumberSetting("range_block", "Range (block)", 6.0, 3.0, 10.0, 0.1)));
      OpenB4KillAuraBackend.rotationSetting = new B4AuraHost.SliderSetting(add(new ChoiceSetting("rotation_mode", "Rotation mode", 0, "Silent", "Lock view", "None")));
      OpenB4KillAuraBackend.rotationSmoothing = new B4AuraHost.SliderSetting(add(new NumberSetting("rotation_smoothing", "Rotation smoothing", 0.0, 0.0, 8.0, 0.1)));
      OpenB4KillAuraBackend.sortSetting = new B4AuraHost.SliderSetting(add(new ChoiceSetting("sort_mode", "Sort mode", 0, "Distance", "Health", "Hurt time", "Yaw")));
      OpenB4KillAuraBackend.switchDelay = new B4AuraHost.SliderSetting(add(new NumberSetting("switch_delay", "Switch delay", 200.0, 50.0, 1000.0, 25.0)));
      OpenB4KillAuraBackend.targets = new B4AuraHost.SliderSetting(add(new NumberSetting("targets", "Targets", 3.0, 1.0, 10.0, 1.0)));
      OpenB4KillAuraBackend.aimInvis = new B4AuraHost.ButtonSetting(add(new BooleanSetting("target_invis", "Target invis", true)));
      OpenB4KillAuraBackend.attackFireballs = new B4AuraHost.ButtonSetting(add(new BooleanSetting("attack_fireballs", "Attack fireballs", false)));
      OpenB4KillAuraBackend.attackMobs = new B4AuraHost.ButtonSetting(add(new BooleanSetting("attack_mobs", "Attack mobs", false)));
      OpenB4KillAuraBackend.attackPlayers = new B4AuraHost.ButtonSetting(add(new BooleanSetting("attack_players", "Attack players", true)));
      OpenB4KillAuraBackend.disableInInventory = new B4AuraHost.ButtonSetting(add(new BooleanSetting("disable_in_inventory", "Disable in inventory", true)));
      OpenB4KillAuraBackend.disableWhileBlocking = new B4AuraHost.ButtonSetting(add(new BooleanSetting("disable_while_blocking", "Disable while blocking", true)));
      OpenB4KillAuraBackend.disableWhileMining = new B4AuraHost.ButtonSetting(add(new BooleanSetting("disable_while_mining", "Disable while mining", false)));
      OpenB4KillAuraBackend.disableSprintMidair = new B4AuraHost.ButtonSetting(add(new BooleanSetting("disable_sprint_midair", "Disable sprint midair", false)));
      OpenB4KillAuraBackend.hitThroughBlocks = new B4AuraHost.ButtonSetting(add(new BooleanSetting("hit_through_walls", "Hit through blocks", false)));
      OpenB4KillAuraBackend.ignoreTeammates = new B4AuraHost.ButtonSetting(add(new BooleanSetting("ignore_teammates", "Ignore teammates", true)));
      OpenB4KillAuraBackend.manualBlock = new B4AuraHost.ButtonSetting(add(new BooleanSetting("manual_block", "Manual block", true)));
      OpenB4KillAuraBackend.prioritizeEnemies = new B4AuraHost.ButtonSetting(add(new BooleanSetting("prioritize_enemies", "Prioritize enemies", false)));
      OpenB4KillAuraBackend.requireMouseDown = new B4AuraHost.ButtonSetting(add(new BooleanSetting("require_mouse_down", "Require mouse down", false)));
      OpenB4KillAuraBackend.silentSwingWhileBlocking = new B4AuraHost.ButtonSetting(add(new BooleanSetting("silent_swing_while_blocking", "Silent swing while blocking", false)));
      OpenB4KillAuraBackend.weaponOnly = new B4AuraHost.ButtonSetting(add(new BooleanSetting("weapon_only", "Weapon only", false)));
      B4AuraHost.aimClosestPoint = new B4AuraHost.SliderSetting(add(new NumberSetting("aim_closest_point", "Aim closest point", 90.0, 0.0, 100.0, 2.0)));
      B4AuraHost.jitterYaw = new B4AuraHost.SliderSetting(add(new NumberSetting("jitter_yaw", "Jitter yaw", 3.0, 0.0, 50.0, 1.0)));
      legacyBackend.guiUpdate();
      backend.targetCPS = OpenB4KillAuraBackend.aps;
      backend.fov = OpenB4KillAuraBackend.fov;
      backend.attackRange = OpenB4KillAuraBackend.attackRangeSetting;
      backend.swingRange = OpenB4KillAuraBackend.swingRangeSetting;
      backend.aimRange = new B4AuraHost.SliderSetting(add(new NumberSetting("aim_range", "Range (aim)", 4.5, 3.0, 8.0, 0.05)));
      backend.rotationMode = OpenB4KillAuraBackend.rotationSetting;
      backend.speed = new B4AuraHost.SliderSetting(add(new NumberSetting("speed", "Speed", 10.0, 1.0, 30.0, 1.0)));
      backend.sortMode = OpenB4KillAuraBackend.sortSetting;
      backend.switchDelay = OpenB4KillAuraBackend.switchDelay;
      backend.targets = OpenB4KillAuraBackend.targets;
      backend.targetInvis = OpenB4KillAuraBackend.aimInvis;
      backend.attackMobs = OpenB4KillAuraBackend.attackMobs;
      backend.aimThroughBlocks = OpenB4KillAuraBackend.hitThroughBlocks;
      backend.aimThroughEntities = new B4AuraHost.ButtonSetting(add(new BooleanSetting("hit_through_entities", "Hit through entities", false)));
      backend.disableInInventory = OpenB4KillAuraBackend.disableInInventory;
      backend.disableWhileMining = OpenB4KillAuraBackend.disableWhileMining;
      backend.ignoreTeammates = OpenB4KillAuraBackend.ignoreTeammates;
      backend.notUsingItem = new B4AuraHost.ButtonSetting(add(new BooleanSetting("not_using_item", "Not using item", false)));
      backend.prioritizeEnemies = OpenB4KillAuraBackend.prioritizeEnemies;
      backend.enemies = add(new StringListSetting("enemies", "Enemies", java.util.Collections.emptyList()));
      backend.requireMouseDown = OpenB4KillAuraBackend.requireMouseDown;
      backend.weaponOnly = OpenB4KillAuraBackend.weaponOnly;
   }
   @Override public int metadataCategory() { return 0; }
   @Override public boolean settingVisible(String key) {
      return key.equals("target_cps") || key.equals("fov") || key.equals("attack_range") || key.equals("swing_range")
            || key.equals("aim_range") || key.equals("rotation_mode") || key.equals("speed") || key.equals("sort_mode")
            || key.equals("switch_delay") || key.equals("targets") || key.equals("target_invis") || key.equals("attack_mobs")
            || key.equals("hit_through_walls") || key.equals("hit_through_entities") || key.equals("disable_in_inventory")
            || key.equals("disable_while_mining") || key.equals("ignore_teammates") || key.equals("not_using_item")
            || key.equals("enemies") || key.equals("prioritize_enemies") || key.equals("require_mouse_down") || key.equals("weapon_only");
   }
   @Override public boolean listens(int type) {
      return type == 23 || type == 8 || type == 13 || type == 17 || type == 18 || type == SemanticEvent.PRE_PLAYER_INTERACT || type == SemanticEvent.POST_MOUSE_OVER || type == SemanticEvent.PRE_MOUSE_OVER;
   }
   @Override protected void onEnable(ClientContext c) {
      B4AuraHost.context = c;
      backend.enabled = legacyBackend.enabled = true;
      if (c.ready()) initialize(c);
   }
   private void initialize(ClientContext c) {
      B4AuraHost.reset();
      RavenBsRotations.serverRotations[0] = c.player().rotationYaw;
      RavenBsRotations.serverRotations[1] = c.player().rotationPitch;
      backend.onEnable(); initialized = true;
   }
   /** OpenB4 RotationManager lists the Displace provider before KillAura: a manual displace owns that tick's rotation. */
   private boolean manualDisplaceActive() {
      Module m = this.modules.find("knockbackdisplace");
      if (!(m instanceof KnockbackDisplaceModule) || !m.isEnabled()) return false;
      try { return MANUAL_DISPLACE_ACTIVE.getBoolean(m); }
      catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
   }
   private void restoreRay(ClientContext c) {
      if (raySwapped && c.player() != null) {
         c.player().rotationYaw=rayYaw; c.player().rotationPitch=rayPitch;
         c.player().prevRotationYaw=rayPrevYaw; c.player().prevRotationPitch=rayPrevPitch;
      }
      raySwapped=false;
   }
   @Override public void onClientTickStart(ClientContext c) {
      B4AuraHost.context=c; restoreRay(c); backend.restoreSyntheticButton(); calculated=false;
   }
   @Override public void onClientTick(ClientContext c) {
      B4AuraHost.context=c;
      if (c.ready() && initialized && c.inputReady()) backend.onUpdate();
   }
   private void calculate(ClientContext c) {
      calculated=true;
      backend.refreshHostiles();
      float[] r=backend.onClientRotation(RavenBsRotations.serverRotations[0],RavenBsRotations.serverRotations[1]);
      OpenB4KillAuraBackend.publishExternalTarget(RavenBsKillAuraBackend.target);
      if (backend.inSwingRange()) OpenB4KillAuraBackend.publishExternalHit(RavenBsKillAuraBackend.target);
      if (manualDisplaceActive()) { published=null; pendingDisplaced=false; return; }
      // LOWEST bS Displace receives the aura's proposal before helper mouse quantization.
      r=KillAuraDisplaceModule.applyRotations(c,r);
      pendingDisplaced=KillAuraDisplaceModule.displacedRotation();
      if (r!=null && !pendingDisplaced) r=RavenBsRotations.fixRotation(r[0],r[1],RavenBsRotations.serverRotations[0],RavenBsRotations.serverRotations[1]);
      published=r;
   }
   @Override public void onEvent(ClientContext c, SemanticEvent e) {
      B4AuraHost.context=c;
      if (!c.ready()) { backend.resetWorld(); OpenB4KillAuraBackend.publishExternalTarget(null); published=null; initialized=false; raySwapped=false; return; }
      if (!initialized) initialize(c);
      if (!c.inputReady()) {
         restoreRay(c); backend.setTarget(null); backend.restoreSyntheticButton();
         OpenB4KillAuraBackend.publishExternalTarget(null); published=null; return;
      }
      if (e.type==23) {
         if (!e.cancelled && e.packet instanceof net.minecraft.network.play.client.C02PacketUseEntity) {
            net.minecraft.network.play.client.C02PacketUseEntity packet=(net.minecraft.network.play.client.C02PacketUseEntity)e.packet;
            if (packet.getAction()==net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK)
               OpenB4KillAuraBackend.publishExternalHit(packet.getEntityFromWorld(c.world()));
         }
      } else if (e.type==SemanticEvent.PRE_MOUSE_OVER) {
         if (!calculated) calculate(c);
      } else if (e.type==SemanticEvent.PRE_PLAYER_INTERACT) {
         backend.onPrePlayerInteract();
      } else if (e.type==8) {
         restoreRay(c);
         rayAllowed=RotationService.claim(pendingDisplaced?600:500);
         if (published!=null && rayAllowed) {
            rayYaw=c.player().rotationYaw; rayPitch=c.player().rotationPitch;
            rayPrevYaw=c.player().prevRotationYaw; rayPrevPitch=c.player().prevRotationPitch;
            raySwapped=true;
            c.player().rotationYaw=c.player().prevRotationYaw=published[0];
            c.player().rotationPitch=c.player().prevRotationPitch=published[1];
         }
      } else if (e.type==SemanticEvent.POST_MOUSE_OVER) {
         try { if (rayAllowed) backend.modifyMouseOverFromGetMouseOver(e.timerRenderPartialTicks); }
         finally { restoreRay(c); }
      } else if (e.type==17) {
         if (!calculated) calculate(c);
         if (manualDisplaceActive()) { published=null; return; }
         if (published!=null && RotationService.claim(pendingDisplaced?600:500)) {
            float cameraYaw=c.player().rotationYaw, cameraPitch=c.player().rotationPitch;
            if ((int)backend.rotationMode.getInput()!=1 || pendingDisplaced) RotationService.retain(c,false,cameraYaw,cameraPitch);
            c.player().rotationYaw=published[0]; c.player().rotationPitch=published[1];
            B4AuraHost.RotationManager.serverYaw=published[0]; B4AuraHost.RotationManager.serverPitch=published[1];
            RavenBsRotations.serverRotations[0]=published[0]; RavenBsRotations.serverRotations[1]=published[1];
         } else if (published==null) {
            RavenBsRotations.serverRotations[0]=c.player().rotationYaw;
            RavenBsRotations.serverRotations[1]=c.player().rotationPitch;
         }
      } else if (e.type==18) {
         backend.restoreSyntheticButton();
      } else if (e.type==13 && published!=null) {
         e.active=true; e.overrideRotation=false;
         if ((int)backend.rotationMode.getInput()==1 && !pendingDisplaced) RotationService.restoreCamera(published[0],published[1]);
      }
   }
   @Override protected void onDisable(ClientContext c) {
      B4AuraHost.context = c; restoreRay(c); backend.enabled = legacyBackend.enabled = false;
      backend.resetWorld(); OpenB4KillAuraBackend.publishExternalTarget(null);
      published = null; initialized = false; calculated=false; pendingDisplaced=false;
   }
   @Override public void onWorldUnload(ClientContext c) {
      B4AuraHost.context = c; restoreRay(c); backend.resetWorld(); OpenB4KillAuraBackend.publishExternalTarget(null);
      published = null; initialized = false; calculated=false; pendingDisplaced=false;
   }
   @Override public String statusText() { return backend.getInfo(); }
}
