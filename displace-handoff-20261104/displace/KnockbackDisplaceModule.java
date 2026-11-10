package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.client.StatusText;
import dev.rice.client.combat.RavenBsVoidScanner;
import dev.rice.setting.NumberSetting;
import dev.rice.setting.BooleanSetting;
import dev.rice.client.packet.TimingAccess;
import dev.rice.client.rotation.RecoveredRandom;
import dev.rice.client.rotation.RotationMath;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.EvidenceModule;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public final strictfp class KnockbackDisplaceModule extends EvidenceModule {
   private final WeaponsModule weapons;
   private final ModuleManager modules;
   private boolean packetPending;
   private boolean active;
   private boolean wasActive;
   private boolean inputSeen;
   private int inputTicks;
   private int activationTicks;
   private int movementDirection;
   private float retainedYaw;
   private float retainedPitch;
   private boolean sprintOwned;

   public KnockbackDisplaceModule(WeaponsModule weapons, ModuleManager modules) {
      super("knockbackdisplace", "Knockback Displace", ModuleCategory.COMBAT);
      this.weapons = weapons;
      this.modules = modules;
      NumberSetting radius = add(new NumberSetting("void_scan_radius", "Void scan radius", 6.0, 1.0, 12.0, 0.5));
      radius.set(6.0);
      radius.saveCurrent();
      BooleanSetting wall = add(new BooleanSetting("void_ignore_one_block_wall", "Void ignore one block wall", false));
      wall.set(false);
      wall.saveCurrent();
      BooleanSetting thirdPerson = add(new BooleanSetting("third_person_rotations", "Third person rotations", true));
      thirdPerson.set(true);
      thirdPerson.saveCurrent();
   }

   @Override
   public boolean settingVisible(String key) {
      if (key.equals("void_scan_radius") || key.equals("void_ignore_one_block_wall")) return this.choice("direction") == 5;
      return super.settingVisible(key);
   }

   @Override
   public strictfp boolean listens(int t) {
      return t == 3 || t == 13 || t == 14 || t == 15 || t == 18;
   }

   public static strictfp int direction(int mode, int movement, boolean random) {
      switch (mode) {
         case 0:
            return -1;
         case 1:
            return 1;
         case 2:
            return (random ? 1 : 0) - 1 | 1;
         case 3:
            return movement;
         case 4:
            return -movement;
         case 5:
            return movement;
         default:
            throw new IllegalArgumentException("direction");
      }
   }

   @Override
   public strictfp synchronized void onEvent(ClientContext c, SemanticEvent e) {
      if (e.type == 3) {
         if (this.packetPending) {
            e.dispatchMode = 1;
         }
      } else if (e.type == 13) {
         if (this.active || this.wasActive) {
            e.remapMovement = false;
         }
      } else if (e.type == 15) {
         if (e.active && Integer.compareUnsigned(this.inputTicks, 10) >= 0 && this.inputSeen) {
            this.inputTicks = 0;
         }
         if (e.active) {
            this.releaseSprint(c);
         }
      } else if (e.type == 18) {
         this.update(c);
      } else if (!c.inputReady() || c.minecraft().currentScreen != null) {
         this.releaseSprint(c);
         this.clear();
      } else if (this.active) {
         e.cancelled = true;
      } else if (e.active && c.ready()) {
         MovingObjectPosition hit = c.minecraft().objectMouseOver;
         if (hit != null
            && hit.typeOfHit == MovingObjectType.ENTITY
            && hit.entityHit instanceof EntityOtherPlayerMP
            && RelationshipService.hostile(hit.entityHit)) {
            this.inputSeen = true;
            if (!this.wasActive
               && Integer.compareUnsigned(this.inputTicks, 9) >= 0
               && !(this.number("cooldown") > (float)Integer.toUnsignedLong(this.activationTicks))
               && !c.player().isRiding()
               && !c.player().isSpectator()
               && c.minecraft().getRenderViewEntity() == c.player()) {
               Module timer = this.modules.find("timerrange");
               if (!(timer instanceof TimerRangeModule) || !((TimerRangeModule)timer).frozenOrAdvancing()) {
                  ItemStack held = c.player().inventory.getCurrentItem();
                  if ((!this.flag("require_no_kb") || !e.reducing)
                     && (!this.flag("require_weapon") || this.weapons.isAllowed(held, c.player().inventory.currentItem))) {
                     boolean enchant = (WeaponsModule.enchantmentGroups(held) & 2) != 0;
                     if (enchant || !this.flag("require_kb_enchant")) {
                        this.retainedYaw = c.player().rotationYaw;
                        this.retainedPitch = c.player().rotationPitch;
                        float targetYaw = (float)TimingAccess.serverYaw(hit.entityHit);
                        if (!this.flag("require_facing_away") || 100.0F > RotationMath.abs(RotationMath.wrap(targetYaw - this.retainedYaw))) {
                           int mode = this.choice("direction");
                           int sign = 0;
                           Float voidYaw = null;
                           float angle = Math.max(45.0F, Math.min(180.0F, (float)this.number("angle")));
                           if (mode == 5) {
                              voidYaw = RavenBsVoidScanner.findYaw(c, hit.entityHit, this.retainedYaw,
                                 angle, this.number("void_scan_radius"), this.flag("void_ignore_one_block_wall"));
                           } else {
                              sign = direction(mode, this.movementDirection, mode == 2 && RecoveredRandom.SHARED.chance(0.5F));
                           }
                           if ((voidYaw != null || sign != 0) && RotationService.claim(200)) {
                              if (!enchant && !c.player().isSprinting()) {
                                 c.player().setSprinting(true);
                                 this.sprintOwned = true;
                              }
                              RotationService.retain(c, false, this.retainedYaw, this.retainedPitch);
                              this.active = true;
                              // Raven offsets yaw by angle +/-5 degrees and preserves pitch.
                              float yaw = voidYaw != null ? RotationMath.wrap(voidYaw - this.retainedYaw)
                                 : angle * (float)sign + RecoveredRandom.SHARED.interval(-5.0F, 5.0F);
                              RotationService.add(yaw, 0.0F);
                              RotationService.applyPending(c);
                              if (this.flag("third_person_rotations")) {
                                 // Head-flick on the third-person model; movementYaw = the spoofed yaw keeps the
                                 // shared Mayu move hooks a no-op, so movement and camera are unchanged.
                                 myau.RotationState.update(true, c.player().rotationYaw, c.player().rotationPitch, c.player().rotationYaw, 3);
                              }
                              if (this.flag("hide_rotation")) {
                                 this.packetPending = true;
                              }

                              e.cancelled = true;
                              this.activationTicks = 0;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private strictfp void update(ClientContext c) {
      this.inputTicks++;
      this.activationTicks++;
      this.wasActive = this.active;
      this.inputSeen = false;
      if (!c.inputReady() || c.minecraft().currentScreen != null) {
         this.releaseSprint(c);
         this.clear();
         return;
      } else {
         float movement = c.player().moveStrafing;
         int sign = (movement > 0.0F ? 1 : 0) - (movement < 0.0F ? 1 : 0);
         if (!this.flag("use_last_strafe") || sign != 0) {
            this.movementDirection = sign;
         }

         if (this.active) {
            if (RotationService.claim(200)) {
               float yaw = this.retainedYaw - (c.player().rotationYaw + RotationService.pendingYaw());
               float pitch = this.retainedPitch - (c.player().rotationPitch + RotationService.pendingPitch());
               RotationService.add(yaw, pitch);
               RotationService.applyPending(c);
               MinecraftFields.setLeftClickCounter(c.minecraft(), 0);
               KeyBinding.onTick(c.minecraft().gameSettings.keyBindAttack.getKeyCode());
            }

            this.active = false;
         }
      }

      this.packetPending = this.wasActive && this.flag("hide_rotation");
   }

   @Override
   protected synchronized void onDisable(ClientContext c) {
      this.releaseSprint(c);
      this.clear();
   }

   @Override
   public strictfp synchronized void onWorldUnload(ClientContext c) {
      this.releaseSprint(c);
      this.clear();
   }

   private void releaseSprint(ClientContext c) {
      if (this.sprintOwned) {
         if (c.ready() && c.player().isSprinting()) {
            c.player().setSprinting(false);
         }
         this.sprintOwned = false;
      }
   }

   private strictfp void clear() {
      this.packetPending = this.active = this.wasActive = this.inputSeen = this.sprintOwned = false;
      this.inputTicks = this.activationTicks = this.movementDirection = 0;
      this.retainedYaw = this.retainedPitch = 0.0F;
   }

   @Override
   public strictfp String statusText() {
      return !this.findSetting("angle").isConfigured() ? null : StatusText.number(this.getName(), (double)this.number("angle"));
   }
}
