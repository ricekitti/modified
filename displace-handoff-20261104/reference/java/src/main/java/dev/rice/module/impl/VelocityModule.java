package dev.rice.module.impl;

import dev.rice.client.AttackInput;
import dev.rice.client.ClientContext;
import dev.rice.client.KeyState;
import dev.rice.client.RecoveredMath;
import dev.rice.client.SemanticEvent;
import dev.rice.client.StatusText;
import dev.rice.client.packet.TimingAccess;
import dev.rice.client.rotation.RecoveredRandom;
import dev.rice.client.rotation.RotationMath;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.client.velocity.jade.JadeHardResetBackend;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.setting.Setting;
import dev.rice.setting.StringListSetting;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;

public final strictfp class VelocityModule extends EvidenceModule {
   public static final int WATCHDOG = 4;
   private final ModuleManager modules;
   private JadeHardResetBackend watchdog;
   private int previousMode = -1;
   private final WeaponsModule weapons;
   private final RecoveredRandom.Smooth probability = new RecoveredRandom.Smooth();
   private boolean resetPending;
   private boolean knockbackPending;
   private boolean storedPending;
   private boolean attackApplied;
   private boolean reducedThisTick;
   private boolean jumpOwned;
   private int entityId;
   private int countdown;
   private double storedX;
   private double storedY;
   private double storedZ;
   private double inputX;
   private double inputY;
   private double inputZ;

   public VelocityModule(WeaponsModule weapons) { this(weapons, null); }

   public VelocityModule(WeaponsModule weapons, ModuleManager modules) {
      super("velocity", "Velocity", ModuleCategory.COMBAT);
      this.weapons = weapons;
      this.modules = modules;
      this.watchdogSetting(new NumberSetting("watchdog_reset_chance", "Reset Chance", 100, 0, 100, 1));
      this.watchdogSetting(new BooleanSetting("watchdog_require_mouse", "Mouse pressed", false));
      this.watchdogSetting(new BooleanSetting("watchdog_require_aim", "Looking at player", false));
      this.watchdogSetting(new BooleanSetting("watchdog_require_forward", "Only while moving forward", false));
      this.watchdogSetting(new BooleanSetting("watchdog_require_weapon", "Holding weapon", false));
      this.watchdogSetting(new BooleanSetting("watchdog_delay", "Delay", true));
      this.watchdogSetting(new NumberSetting("watchdog_delay_chance", "Delay Chance", 100, 0, 100, 1));
      this.watchdogSetting(new NumberSetting("watchdog_range", "Distance to target", 5.3, 3, 12, .1));
      this.watchdogSetting(new NumberSetting("watchdog_max_delay", "Maximum delay (ms)", 200, 50, 1000, 10));
      this.watchdogSetting(new BooleanSetting("watchdog_in_air", "In air", true));
      this.watchdogSetting(new BooleanSetting("watchdog_looking_at_player", "Looking at player", true));
      this.watchdogSetting(new BooleanSetting("watchdog_require_left_mouse", "Require Left mouse", true));
      this.watchdogSetting(new BooleanSetting("watchdog_restrict_held_item", "Restrict held item", false));
      this.watchdogSetting(new StringListSetting("watchdog_whitelisted_items", "Whitelisted items", Collections.emptyList()));
      this.itemPolicy("watchdog_whitelisted_items");
   }

   private <T> void watchdogSetting(Setting<T> setting) {
      this.add(setting); setting.set(setting.get()); setting.saveCurrent();
   }
   public boolean watchdogFlag(String key) { return this.flag("watchdog_" + key); }
   public double watchdogNumber(String key) { return this.number("watchdog_" + key); }
   public boolean watchdogWeaponHeld(ClientContext c) { return this.weapons.isWeapon(c.player()); }
   public List<String> watchdogItems() { return this.names("watchdog_whitelisted_items"); }

   private void syncMode(ClientContext c) {
      int mode = this.choice("mode");
      if (mode != this.previousMode) {
         if (this.watchdog != null) { this.watchdog.reset(true); this.watchdog = null; }
         if (this.previousMode != -1 && (mode == WATCHDOG || this.previousMode == WATCHDOG)) this.reset(c);
         this.previousMode = mode;
      }
      if (mode == WATCHDOG && this.watchdog == null) this.watchdog = new JadeHardResetBackend(c, this, this.modules);
   }
   @Override public boolean settingVisible(String key) {
      if (!key.startsWith("watchdog_")) return true;
      if (this.choice("mode") != WATCHDOG) return false;
      if (key.equals("watchdog_reset_chance") || key.equals("watchdog_require_mouse")
         || key.equals("watchdog_require_aim") || key.equals("watchdog_require_forward") || key.equals("watchdog_require_weapon") || key.equals("watchdog_delay")) return true;
      return this.watchdogFlag("delay") && (!key.equals("watchdog_whitelisted_items") || this.watchdogFlag("restrict_held_item"));
   }
   @Override public synchronized boolean afterSettingControl(String key, ClientContext c) {
      if (key.equals("mode")) this.syncMode(c);
      if ((key.equals("watchdog_delay") || key.equals("watchdog_require_forward")
         || key.equals("watchdog_require_mouse") || key.equals("watchdog_require_aim") || key.equals("watchdog_require_weapon")) && this.watchdog != null) this.watchdog.reset(true);
      return false;
   }
   @Override public synchronized void onClientTickStart(ClientContext c) {
      this.reducedThisTick = false;
      this.syncMode(c);
      if (this.watchdog != null) this.watchdog.tickStart();
   }

   @Override
   public strictfp boolean listens(int t) {
      return t == 0 || t == 2 || t == 14 || t == 15 || t == 17 || t == 18;
   }

   private strictfp boolean eligible(ClientContext c) {
      return (!this.flag("require_forward") || !(0.0F >= c.player().moveForward))
         && (!this.flag("require_mouse") || AttackInput.pressed(c))
         && (!this.flag("require_weapon") || this.weapons.isWeapon(c.player()))
         && (!this.flag("require_target") || TimingAccess.facingPlayer(c));
   }

   private strictfp boolean chance(float percent) {
      if (percent > 99.0F) {
         return true;
      } else {
         double fraction = (double)((float)((double)percent * 0.01));
         return this.probability.gate((float)(-0.05 + fraction), (float)(fraction + 0.05), 60.0, RecoveredRandom.now(), RecoveredRandom.SHARED);
      }
   }

   @Override
   public strictfp synchronized void onEvent(ClientContext c, SemanticEvent e) {
      this.syncMode(c);
      if (this.watchdog != null) {
         if (e.type == 2) this.watchdog.packet(e);
         else if (e.type == 17) this.watchdog.preUpdate();
         else if (e.type == 18) this.watchdog.postUpdate();
         return;
      }
      if (e.type == 2) {
         if (e.dispatchMode == 0) {
            if (e.packet instanceof S12PacketEntityVelocity) {
               if (((S12PacketEntityVelocity)e.packet).getEntityID() == this.entityId) {
                  this.knockbackPending = true;
               }
            } else if (e.packet instanceof S27PacketExplosion) {
               S27PacketExplosion p = (S27PacketExplosion)e.packet;
               float x = p.func_149149_c();
               float z = p.func_149147_e();
               this.knockbackPending = (double)(x * x + z * z) > 0.05;
            } else if (e.packet instanceof S08PacketPlayerPosLook) {
               this.resetPending = true;
            }
         }
      } else if (c.player() != null) {
         if (e.type == 14) {
            if (e.active) {
               this.inputX = c.player().motionX;
               this.inputY = c.player().motionY;
               this.inputZ = c.player().motionZ;
            }
         } else if (e.type == 15) {
            net.minecraft.util.MovingObjectPosition hit = c.minecraft().objectMouseOver;
            if (e.active && e.reducing && hit != null && hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.ENTITY
               && hit.entityHit instanceof net.minecraft.entity.player.EntityPlayer) this.reducedThisTick = true;
            if (this.choice("mode") == 2 && (!this.attackApplied || this.flag("reduce_multiple_per_tick")) && e.active && e.reducing) {
               if ((!this.flag("require_forward") || !(0.0F >= c.player().moveForward)) && (!this.flag("require_weapon") || this.weapons.isWeapon(c.player()))) {
                  if (c.player().ticksExisted == c.player().getLastAttackerTime()) {
                     this.inputX *= 0.6;
                     this.inputZ = 0.6 * this.inputZ;
                     c.player().motionX = this.inputX;
                     c.player().motionY = this.inputY;
                     c.player().motionZ = this.inputZ;
                     this.attackApplied = true;
                  }
               }
            }
         } else if (e.type == 17) {
            this.storedX = c.player().motionX;
            this.storedY = c.player().motionY;
            this.storedZ = c.player().motionZ;
            this.storedPending = true;
         } else if (e.type == 0) {
            if (this.choice("mode") == 3 && c.inputReady()) {
               if (this.resetPending) {
                  this.resetPending = false;
               } else if (this.storedPending && this.eligible(c) && this.chance(this.number("ignore_chance"))) {
                  c.player().motionX = this.storedX;
                  c.player().motionZ = this.storedZ;
                  if (this.choice("ignore_vertical") == 2 || this.choice("ignore_vertical") == 1 && !c.player().onGround) {
                     c.player().motionY = this.storedY;
                  }

                  this.storedPending = false;
               }
            }
         } else {
            this.attackApplied = false;
            int mode = this.choice("mode");
            if (mode != 2 && c.inputReady()) {
               this.entityId = c.player().getEntityId();
               if (mode == 1) {
                  if (this.jumpOwned) {
                     KeyState.restore(c.minecraft().gameSettings.keyBindJump);
                     this.jumpOwned = false;
                  } else if (this.eligible(c)
                     && this.chance(this.number("jump_chance"))
                     && (!this.flag("require_sprinting_jump") || c.player().isSprinting() && !this.reducedThisTick)
                     && c.player().onGround
                     && !(0.0 >= c.player().motionY)
                     && !c.player().isPotionActive(Potion.jump)
                     && !c.minecraft().gameSettings.keyBindJump.isKeyDown()) {
                     KeyState.set(c.minecraft().gameSettings.keyBindJump, true);
                     this.jumpOwned = true;
                  }
               } else if (mode == 0) {
                  if (this.countdown > 0) {
                     if (--this.countdown == 0) {
                        this.scale(c);
                     }
                  } else if (this.knockbackPending) {
                     this.knockbackPending = false;
                     if (this.eligible(c) && this.chance(this.number("reduce_chance"))) {
                        double x = c.player().motionX;
                        double z = c.player().motionZ;
                        double length = RotationMath.hypot(x, z);
                        if (length != 0.0) {
                           float angle = (float)((double)c.player().rotationYaw * -Math.PI / 180.0 + -Math.PI);
                           double projection = ((double)RotationMath.sine(angle) * x + (double)RotationMath.cosine(angle) * z) / length;
                           if ((double)this.number("fov") >= StrictMath.acos(projection)) {
                              this.countdown = RecoveredMath.truncateInt(this.number("reduce_delay"));
                              if (this.countdown <= 0 || --this.countdown == 0) {
                                 this.scale(c);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private strictfp void scale(ClientContext c) {
      double factor = (double)this.number("reduce_amount");
      c.player().motionX *= factor;
      c.player().motionZ = factor * c.player().motionZ;
   }

   @Override
   protected strictfp synchronized void onEnable(ClientContext c) {
      this.storedPending = false;
      this.reducedThisTick = false;
      this.syncMode(c);
   }

   @Override
   protected strictfp synchronized void onDisable(ClientContext c) {
      if (this.watchdog != null) { this.watchdog.reset(true); this.watchdog = null; }
      this.previousMode = -1;
      this.storedPending = false;
      if (this.jumpOwned) {
         KeyState.restore(c.minecraft().gameSettings.keyBindJump);
         this.jumpOwned = false;
      }
   }

   @Override
   public strictfp synchronized void onWorldUnload(ClientContext c) {
      if (this.watchdog != null) { this.watchdog.reset(false); this.watchdog = null; }
      this.previousMode = -1;
      this.reset(c);
   }

   private strictfp void reset(ClientContext c) {
      if (this.jumpOwned) {
         KeyState.restore(c.minecraft().gameSettings.keyBindJump);
      }

      this.jumpOwned = this.resetPending = this.knockbackPending = this.storedPending = this.attackApplied = false;
      this.reducedThisTick = false;
      this.countdown = this.entityId = 0;
      this.storedX = this.storedY = this.storedZ = this.inputX = this.inputY = this.inputZ = 0.0;
   }

   @Override
   public strictfp String statusText() {
      if (!this.findSetting("mode").isConfigured()) {
         return null;
      } else {
         int index = this.choice("mode");
         if (index == WATCHDOG) return "Watchdog";
         if (index != 0) {
            return StatusText.label(this.getName(), index);
         } else if (!this.findSetting("reduce_amount").isConfigured()) {
            return null;
         } else {
            float percent = this.number("reduce_amount") * 100.0F;
            return StatusText.number(this.getName(), 1, (double)percent);
         }
      }
   }
}
