package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.SemanticEvent;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.module.impl.fireball.FireballJumpTracker;
import dev.rice.module.impl.fireball.TimerController;
import dev.rice.module.impl.fireball.TimerRangeCycle;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

/**
 * Fireball Boost, adapted from valen.module.impl.movement.FireballBoost (CFR 0.152) onto the Rice
 * Slinky feature bridge. Detects a fireball-boost jump from the packet stream ({@link FireballJumpTracker})
 * and drives a freeze/burst client-timer cycle ({@link TimerRangeCycle} + shared {@link TimerController})
 * so the boost's airborne ticks resolve in one Regular burst or a Legit freeze-then-burst.
 *
 * Source settings/timing preserved exactly: Mode {Regular, Legit} (Legit recovered from source, not the
 * screenshot), Duration 200ms [50..2000 step 50], Cooldown 1000ms [0..5000 step 50], launch/impact
 * windows and tick math from the ported helpers. Distinguished from the pre-existing unregistered
 * dev.rice.module.impl.FireballBoostModule (native OpenJade boost-delay), which is left untouched.
 *
 * Two Lunar-specific behaviors do not map to this client and are intentionally dropped (documented in
 * the v1.8.12 changelog, not silent): the render-tick keybind re-press (Lunar hold-bind semantics; this
 * client toggles) and the ModuleManager.isBedMining() mutual-exclusion guard (no equivalent accessor is
 * exposed to feature modules). The core detection + timer cycle are unchanged.
 */
public final class FireballBoostFeatureModule extends Module {
   private final ChoiceSetting mode = this.add(new ChoiceSetting("mode", "Mode", 0, "Regular", "Legit"));
   private final NumberSetting duration = this.add(new NumberSetting("duration", "Duration", 200.0, 50.0, 2000.0, 50.0));
   private final NumberSetting cooldown = this.add(new NumberSetting("cooldown", "Cooldown", 1000.0, 0.0, 5000.0, 50.0));

   private final TimerController timer;
   private final TimerRangeCycle cycle;
   private final FireballJumpTracker jump = new FireballJumpTracker();
   private final AtomicBoolean correctionPending = new AtomicBoolean();
   private Object activeWorld;
   private Object activePlayer;

   public FireballBoostFeatureModule() {
      super("fireball_boost", "Fireball Boost", "Fireball boost timer burst after a fireball jump", ModuleCategory.MOVEMENT);
      this.timer = dev.rice.module.impl.pearl.RescueServices.timer();
      this.cycle = new TimerRangeCycle(this.timer, TimerController.Owner.FIREBALL_BOOST, 40, delta -> {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc != null) {
            MinecraftFields.timer(mc).elapsedPartialTicks += (float)delta;
         }
      });
   }

   @Override
   public boolean listens(int type) {
      return type == 1 || type == 2 || type == 3;
   }

   @Override
   public String statusText() {
      return this.mode.selectedLabel();
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.reset();
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.reset();
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      this.reset();
   }

   @Override
   public void onEvent(ClientContext context, SemanticEvent event) {
      switch (event.type) {
         case 3:
            this.onSendPacket(context, event);
            break;
         case 2:
            this.onReceivePacket(context, event);
            break;
         case 1:
            this.onGameLoop(context);
            break;
         default:
      }
   }

   /** type 3 - outgoing packets: arm the tracker when the local player uses a fire charge. */
   private void onSendPacket(ClientContext context, SemanticEvent event) {
      if (event.cancelled || !this.prepareContext(context) || !this.canRun(context) || this.cycle.active()) {
         return;
      }
      if (event.packet instanceof C08PacketPlayerBlockPlacement) {
         ItemStack stack = ((C08PacketPlayerBlockPlacement)event.packet).getStack();
         if (stack != null && stack.stackSize > 0 && stack.getItem() == Items.fire_charge) {
            this.jump.onUse(System.nanoTime());
         }
      }
   }

   /** type 2 - incoming packets: explosion / velocity feed the tracker; a pos-look is a server correction. */
   private void onReceivePacket(ClientContext context, SemanticEvent event) {
      if (event.packet instanceof S08PacketPlayerPosLook) {
         this.correctionPending.set(true);
         return;
      }
      if (event.cancelled || !this.prepareContext(context) || !this.canRun(context)) {
         return;
      }
      long now = System.nanoTime();
      EntityPlayerSP player = context.player();
      if (event.packet instanceof S27PacketExplosion) {
         S27PacketExplosion packet = (S27PacketExplosion)event.packet;
         this.jump.onExplosion(now,
            player.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()),
            packet.func_149149_c(), packet.func_149144_d(), packet.func_149147_e());
      } else if (event.packet instanceof S12PacketEntityVelocity) {
         S12PacketEntityVelocity packet = (S12PacketEntityVelocity)event.packet;
         if (packet.getEntityID() == player.getEntityId()) {
            this.jump.onVelocity(now,
               (double)packet.getMotionX() / 8000.0,
               (double)packet.getMotionY() / 8000.0,
               (double)packet.getMotionZ() / 8000.0);
         }
      }
   }

   /** type 1 - per-frame run loop: advance or begin the freeze/burst cycle, aborting on correction or conflict. */
   private void onGameLoop(ClientContext context) {
      if (!this.prepareContext(context)) {
         return;
      }
      long now = System.nanoTime();
      if (this.correctionPending.getAndSet(false)
            || !this.canRun(context)
            || this.timer.hasRequestOtherThan(TimerController.Owner.FIREBALL_BOOST)
            || NativeFeatureBridge.timerBlocked()) {
         this.abort(now);
         return;
      }
      boolean legit = this.mode.index() == 1;
      if (this.cycle.active()) {
         if (this.cycle.legit() != legit) {
            this.abort(now);
         } else {
            this.cycle.update(now);
         }
         return;
      }
      EntityPlayerSP player = context.player();
      if (this.jump.consumeJump(now, !player.onGround, player.motionY)) {
         this.cycle.begin(now, legit, this.duration.doubleValue(), this.cooldown.doubleValue());
      }
   }

   private boolean prepareContext(ClientContext context) {
      if (!context.ready()) {
         this.reset();
         return false;
      }
      if (this.activeWorld != context.world() || this.activePlayer != context.player()) {
         this.reset();
         this.activeWorld = context.world();
         this.activePlayer = context.player();
      }
      return true;
   }

   private boolean canRun(ClientContext context) {
      Minecraft mc = context.minecraft();
      EntityPlayerSP player = context.player();
      return mc.currentScreen == null
         && mc.inGameHasFocus
         && !player.isDead
         && player.getHealth() > 0.0F
         && !player.isRiding()
         && !player.capabilities.isFlying
         && !player.isSpectator()
         && !player.isInWater()
         && !player.isInLava()
         && !player.isOnLadder();
   }

   private void abort(long now) {
      this.cycle.abort(now);
      this.jump.reset();
   }

   private void reset() {
      this.cycle.reset();
      this.jump.reset();
      this.activePlayer = null;
      this.activeWorld = null;
      this.correctionPending.set(false);
   }
}
