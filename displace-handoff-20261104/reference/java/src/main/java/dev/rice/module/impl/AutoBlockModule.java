package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.action.ActionAccess;
import dev.rice.client.action.InteractionTarget;
import dev.rice.client.rotation.RecoveredRandom;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Mouse;

/** OpenB4 Slinky-style Auto Block, adapted only to the recovered Slinky API/settings. */
public final class AutoBlockModule extends EvidenceModule {
   private boolean blocking;
   private boolean canBlock;
   private boolean interacting;
   private boolean hasEnemyNearby;
   private boolean lagActive;
   private boolean reblockPending;
   private boolean fakeBlocking;
   private boolean animationOwned;
   private boolean manualHandoff;
   private boolean useOwned;
   private ItemStack animationStack;
   private long blockEnd;
   private long lagEndTime;

   public AutoBlockModule(ModuleManager modules) {
      super("autoblock", "Auto Block", ModuleCategory.COMBAT);
   }

   private static int ticks(float millis) {
      int value = (int)Math.ceil((double)millis / 50.0D);
      return value < 1 ? 1 : value;
   }

   private static boolean holdingSword(ClientContext c) {
      return c.player() != null && ActionAccess.canBlock(c.player().getHeldItem());
   }

   private static boolean usingRangedItem(ClientContext c) {
      ItemStack held = c.player() == null ? null : c.player().getHeldItem();
      return held != null && (held.getItem() instanceof ItemBow || held.getItem() instanceof ItemFishingRod);
   }

   private EntityPlayer findNearestEnemy(ClientContext c) {
      EntityPlayer self = c.player();
      if (self == null || c.world() == null) return null;
      float range = this.number("range");
      double nearestDistance = (double)range * (double)range;
      EntityPlayer nearest = null;
      List<EntityPlayer> players = c.world().playerEntities;
      for (int i = 0; i < players.size(); ++i) {
         EntityPlayer target = players.get(i);
         if (target == self || target == null || target.isDead || target.getHealth() <= 0.0F || InteractionTarget.isNpc(c, target)) continue;
         double distance = self.getDistanceSqToEntity(target);
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearest = target;
         }
      }
      return nearest;
   }

   private boolean aimingInteractable(ClientContext c) {
      MovingObjectPosition hit = InteractionTarget.find(c);
      if (hit == null || hit.typeOfHit != MovingObjectType.BLOCK) return false;
      Block block = c.world().getBlockState(hit.getBlockPos()).getBlock();
      Object key = block == null ? null : Block.blockRegistry.getNameForObject(block);
      if (key == null) return false;
      String name = key.toString();
      return name.contains("chest") || name.contains("furnace") || name.contains("crafting")
         || name.contains("anvil") || name.contains("enchant") || name.contains("brewing")
         || name.contains("hopper") || name.contains("dropper") || name.contains("dispenser")
         || name.contains("beacon") || name.contains("door") || name.contains("gate")
         || name.contains("lever") || name.contains("button") || name.contains("trap");
   }

   private boolean aimingNpc(ClientContext c) {
      MovingObjectPosition hit = InteractionTarget.find(c);
      return hit != null && hit.typeOfHit == MovingObjectType.ENTITY && InteractionTarget.isNpc(c, hit.entityHit);
   }

   /**
    * Lobby/social right-click: a real (tab-list) player is not an NPC, so AutoBlock would otherwise own RMB and
    * swallow the interaction - and a crowded lobby makes every nearby player count as an "enemy", so the
    * no-enemy manual-block hand-off never fires. Yield RMB when the crosshair is directly on a player AND the
    * local player is standing still. Combat blocking is unaffected: it happens while moving/strafing, and the
    * shop-interaction check's enemy-block case moves (moveForward=1), so it still owns RMB.
    */
   private boolean aimingPlayerStationary(ClientContext c) {
      MovingObjectPosition hit = InteractionTarget.find(c);
      if (hit == null || hit.typeOfHit != MovingObjectType.ENTITY
         || !(hit.entityHit instanceof EntityPlayer) || hit.entityHit == c.player()) return false;
      return c.player().movementInput == null
         || c.player().movementInput.moveForward == 0.0F
            && c.player().movementInput.moveStrafe == 0.0F
            && !c.player().movementInput.jump;
   }

   private boolean yieldInteraction(ClientContext c) {
      if (!this.aimingInteractable(c) && !this.aimingNpc(c) && !this.aimingPlayerStationary(c)) return false;
      this.interacting = true;
      this.yieldUse(c);
      return true;
   }

   private void yieldUse(ClientContext c) {
      boolean owned = this.useOwned || this.blocking || this.lagActive || this.animationOwned;
      this.resetOwnedState(c);
      if (owned && holdingSword(c) && c.player().isUsingItem()) {
         c.minecraft().playerController.onStoppedUsingItem(c.player());
      }
      if (owned) KeyBinding.setKeyBindState(c.minecraft().gameSettings.keyBindUseItem.getKeyCode(), Mouse.isButtonDown(1));
   }

   @Override
   public boolean listens(int type) {
      return type == 3 || type == 5 || type == 14 || type == 23 || type == 26;
   }

   @Override
   protected void onEnable(ClientContext c) {
      this.reset(c, false);
   }

   @Override
   protected void onDisable(ClientContext c) {
      this.reset(c, true);
   }

   @Override
   public void onWorldUnload(ClientContext c) {
      this.reset(c, true);
   }

   /** AutoBlock owns RMB except for stationary, out-of-range vanilla blocking. */
   public boolean swallowMouseEvent(ClientContext c) {
      if (!this.isEnabled() || !c.ready() || c.minecraft().currentScreen != null
         || Mouse.getEventButton() != 1 || !holdingSword(c) || this.yieldInteraction(c)) return false;
      if (this.manualBlockAllowed(c, this.findNearestEnemy(c) != null)) {
         this.manualHandoff = Mouse.getEventButtonState();
         return false;
      }
      this.manualHandoff = false;
      return true;
   }

   public boolean isActive() {
      return this.isEnabled() && (this.blocking || this.lagActive);
   }

   /** Release the real item-use state before vanilla evaluates an attack press. */
   public void prepareAttackInput(ClientContext c) {
      if (!this.isEnabled() || !c.ready() || c.minecraft().currentScreen != null
         || !holdingSword(c) || ActionAccess.presses(c.minecraft().gameSettings.keyBindAttack) <= 0) return;
      if (this.manualBlockAllowed(c, this.findNearestEnemy(c) != null)) return;
      this.releaseUse(c);
      if (c.player().isUsingItem()) c.minecraft().playerController.onStoppedUsingItem(c.player());
   }

   @Override
   public void onEvent(ClientContext c, SemanticEvent e) {
      if (e.type == 3) {
         this.classifyLagPacket(e);
      } else if (e.type == 23) {
         this.observeSentPacket(e);
      } else if (e.type == 14) {
         if (!e.active && this.isEnabled() && c.ready() && holdingSword(c)
            && c.minecraft().currentScreen == null && !this.yieldInteraction(c)
            && !this.manualBlockAllowed(c, this.findNearestEnemy(c) != null)) e.cancelled = true;
      } else if (e.type == 26 || e.type == 5 && e.module == this && !e.enabled) {
         this.reset(c, true);
      }
   }

   private void classifyLagPacket(SemanticEvent e) {
      if (!this.lagActive) return;
      if (e.packet instanceof C0EPacketClickWindow || e.packet instanceof C0DPacketCloseWindow) {
         this.stopLag();
         return;
      }
      if (e.packet instanceof C02PacketUseEntity) {
         C02PacketUseEntity packet = (C02PacketUseEntity)e.packet;
         if (packet.getAction() != Action.ATTACK) {
            this.stopLag();
            return;
         }
         if (this.flag("lag_flush_attack")) {
            this.stopLag();
            if (this.flag("lag_reblock") && this.hasEnemyNearby) this.reblockPending = true;
            return;
         }
      }
      e.dispatchMode = 1;
   }

   private void observeSentPacket(SemanticEvent e) {
      if (!this.lagActive || !(e.packet instanceof C02PacketUseEntity)) return;
      C02PacketUseEntity packet = (C02PacketUseEntity)e.packet;
      if (packet.getAction() != Action.ATTACK || this.flag("lag_flush_attack")) {
         this.stopLag();
         if (packet.getAction() == Action.ATTACK && this.flag("lag_reblock") && this.hasEnemyNearby) {
            this.reblockPending = true;
         }
      }
   }

   @Override
   public void onClientTickStart(ClientContext c) {
      if (!c.ready() || c.player().isDead || c.minecraft().currentScreen != null) {
         this.reset(c, true);
         return;
      }

      // Hand back only state we own; ordinary item use must survive repeated ticks.
      if (this.yieldInteraction(c)) return;
      this.interacting = false;
      this.hasEnemyNearby = this.findNearestEnemy(c) != null;
      if (!holdingSword(c)) {
         this.yieldUse(c);
         return;
      }

      long now = System.currentTimeMillis();
      if (this.lagActive && now >= this.lagEndTime) this.stopLag();
      if (this.manualBlockAllowed(c, this.hasEnemyNearby)) {
         this.canBlock = false;
         this.fakeBlocking = false;
         this.reblockPending = false;
         if (this.animationOwned) this.setItemInUse(c, false);
         this.driveManualBlock(c);
         return;
      }
      this.manualHandoff = false;
      boolean conditions = this.passesConditions(c);
      // The fake-block pose must obey the same left/right-mouse gating as a real block;
      // otherwise the animation shows a held block even while the required button is up.
      this.fakeBlocking = this.flag("fake_block_animation") && conditions
         && (!this.flag("fake_block_animation_require_target") || this.hasEnemyNearby);

      // Match OpenB4: native RMB never bypasses the script's block/release cycle.
      if (!this.blocking && !this.lagActive) this.releaseUse(c);

      if (!conditions || this.interacting || !this.hasEnemyNearby) {
         if (this.blocking) this.stopBlockingAndLag(c, now);
         else if (!this.lagActive) this.releaseUse(c);
         this.canBlock = false;
         return;
      }

      if (this.lagActive) {
         if (now >= this.lagEndTime) {
            this.stopLag();
            this.canBlock = true;
         }
         return;
      }

      this.canBlock = true;
      if (this.reblockPending) {
         this.reblockPending = false;
         this.startBlockingNow(c, now);
      } else if (!this.blocking) {
         if (this.shouldStartBlock(c)) this.startBlockingNow(c, now);
      } else if (now >= this.blockEnd) {
         this.stopBlockingAndLag(c, now);
      } else {
         this.pressUse(c);
      }
   }

   private boolean passesConditions(ClientContext c) {
      if (this.flag("require_left_mouse_pressed") && !Mouse.isButtonDown(0)) return false;
      if (this.flag("require_right_mouse_pressed") && !Mouse.isButtonDown(1)) return false;
      return !usingRangedItem(c);
   }

   private boolean manualBlockAllowed(ClientContext c, boolean enemyNearby) {
      if (enemyNearby || this.blocking || this.lagActive || !holdingSword(c)) return false;
      return c.player().movementInput == null
         || c.player().movementInput.moveForward == 0.0F
            && c.player().movementInput.moveStrafe == 0.0F
            && !c.player().movementInput.jump;
   }

   private void driveManualBlock(ClientContext c) {
      int key = c.minecraft().gameSettings.keyBindUseItem.getKeyCode();
      boolean right = Mouse.isButtonDown(1);
      KeyBinding.setKeyBindState(key, right);
      if (right && !this.manualHandoff) KeyBinding.onTick(key);
      this.manualHandoff = right;
   }

   private boolean shouldStartBlock(ClientContext c) {
      if (!this.flag("require_damage")) return true;
      int hurt = c.player().hurtTime;
      return hurt > 0 && hurt <= ticks(this.number("max_hurt_time"));
   }

   private void startBlockingNow(ClientContext c, long now) {
      if (!this.canBlock) return;
      this.pressUse(c);
      this.blocking = true;
      this.blockEnd = now + (long)this.number("max_hold_time");
   }

   private void stopBlockingAndLag(ClientContext c, long now) {
      this.blocking = false;
      this.canBlock = false;
      if (this.rollLag(c)) {
         this.lagActive = true;
         this.lagEndTime = now + (long)this.number("lag_duration");
      }
      this.releaseUse(c);
   }

   private boolean rollLag(ClientContext c) {
      if (c.minecraft().isSingleplayer()) return false;
      float chance = this.number("lag_chance");
      return chance > 0.0F && (chance >= 1.0F || RecoveredRandom.SHARED.chance(chance));
   }

   private void stopLag() {
      this.lagActive = false;
      this.lagEndTime = 0L;
   }

   private void pressUse(ClientContext c) {
      int key = c.minecraft().gameSettings.keyBindUseItem.getKeyCode();
      KeyBinding.setKeyBindState(key, true);
      KeyBinding.onTick(key);
      this.useOwned = true;
   }

   private void releaseUse(ClientContext c) {
      this.useOwned = false;
      if (c.minecraft() != null && c.minecraft().gameSettings != null) {
         KeyBinding.setKeyBindState(c.minecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
      }
   }

   @Override
   public void onRenderTickStart(ClientContext c) {
      if (!c.ready()) return;
      this.setItemInUse(c, holdingSword(c) && (this.blocking || this.lagActive || this.fakeBlocking));
   }

   private void setItemInUse(ClientContext c, boolean show) {
      if (show) {
         ActionAccess.useCount(c.player(), 72000);
         this.animationOwned = true;
         this.animationStack = c.player().getHeldItem();
      } else if (this.animationOwned) {
         // The forced itemInUseCount (72000) drives the first-person block pose and never
         // decays on its own: vanilla only decrements it while getItemInUse() != null. If the
         // player swaps slots or the fake-block stack identity shifts, the old guard bailed
         // here WITHOUT zeroing, leaking the sentinel and freezing the block hold on screen.
         // Clear it whenever no foreign vanilla use is live (use is null or still our stack);
         // a genuine use of a different item is left for vanilla to wind down.
         if (c.player().getItemInUse() == null || c.player().getItemInUse() == this.animationStack) {
            ActionAccess.useCount(c.player(), 0);
         }
         this.animationOwned = false;
         this.animationStack = null;
      }
   }

   private void resetOwnedState(ClientContext c) {
      this.blocking = false;
      this.canBlock = false;
      this.stopLag();
      this.reblockPending = false;
      this.fakeBlocking = false;
      this.manualHandoff = false;
      if (this.useOwned) this.releaseUse(c);
      if (this.animationOwned && c.player() != null) this.setItemInUse(c, false);
   }

   private void reset(ClientContext c, boolean release) {
      this.resetOwnedState(c);
      this.interacting = false;
      this.hasEnemyNearby = false;
      this.blockEnd = 0L;
      if (release) this.releaseUse(c);
   }
}
