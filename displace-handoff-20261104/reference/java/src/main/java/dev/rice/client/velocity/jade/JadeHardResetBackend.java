package dev.rice.client.velocity.jade;

import dev.rice.client.ClientContext;
import dev.rice.client.ItemPolicyService;
import dev.rice.client.KeyState;
import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.client.rotation.RotationMath;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.StasisModule;
import dev.rice.module.impl.VelocityModule;
import dev.rice.setting.ChoiceSetting;
import java.util.ArrayDeque;
import java.util.function.LongSupplier;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * OpenJade Velocity HARD_RESET adapted to Rice's events and packet ownership.
 * Source: Tewxx/OpenJade, jade/client/module/combat/Velocity.java.
 * Only the Hard Reset path is used. Rice calls it Watchdog; no server bypass is implied.
 */
public class JadeHardResetBackend {
   private final ClientContext context;
   private final VelocityModule settings;
   private final ModuleManager modules;
   private final LongSupplier clock;
   private final ArrayDeque<HeldPacket> held = new ArrayDeque<>();
   private TimeoutCondition timeout;
   private WorldClient heldWorld;
   private NetHandlerPlayClient heldHandler;
   private long inboundDelayMillis;
   private boolean delayApplied;
   private boolean pendingReset;
   private long resetDueAtMillis;
   private double pendingKnockbackSpeed;
   private boolean jumpOwned;
   private boolean forwardOwned;
   private long forwardHoldUntilMillis;
   private boolean ignoreFallDamage;
   private long fallDamageIgnoreUntil;
   private double previousFallDistance;
   private double maximumFallDistance;
   private boolean delaySetting;

   public JadeHardResetBackend(ClientContext context, VelocityModule settings, ModuleManager modules) {
      this(context, settings, modules, System::currentTimeMillis);
   }
   public JadeHardResetBackend(ClientContext context, VelocityModule settings, ModuleManager modules, LongSupplier clock) {
      this.context = context; this.settings = settings; this.modules = modules; this.clock = clock;
      this.delaySetting = flag("delay");
   }
   private boolean flag(String key) { return this.settings.watchdogFlag(key); }
   private double number(String key) { return this.settings.watchdogNumber(key); }
   private boolean ready() {
      return this.context.inputReady() && this.context.minecraft().currentScreen == null && !this.context.player().isDead;
   }
   private boolean movingForwardAllowed() {
      if (!flag("require_forward")) return true;
      if (this.context.player() == null) return false;
      float forward = this.context.player().movementInput == null
         ? this.context.player().moveForward : this.context.player().movementInput.moveForward;
      return forward > 0.0F;
   }
   private boolean conditionsAllowed() {
      return movingForwardAllowed()
         && (!flag("require_mouse") || this.context.minecraft().gameSettings.keyBindAttack.isKeyDown())
         && (!flag("require_aim") || aimingPlayer(false) && withinKnockbackFov())
         && (!flag("require_weapon") || this.settings.watchdogWeaponHeld(this.context));
   }
   private boolean conflict() {
      if (this.modules == null) return false;
      Module blink = this.modules.find("blink");
      if (blink != null && blink.isEnabled() && ((ChoiceSetting)blink.findSetting("direction")).index() != 1) return true;
      Module stasis = this.modules.find("stasis");
      if (stasis instanceof StasisModule && ((StasisModule)stasis).isHypixelModeActive()) return true;
      Module buffer = this.modules.find("buffer");
      return buffer != null && buffer.isEnabled();
   }
   private boolean valid(Entity entity, double rangeSq) {
      if (!(entity instanceof EntityPlayer) || entity == this.context.player() || entity.isDead
         || ((EntityPlayer)entity).deathTime != 0 || !RelationshipService.hostile(entity)) return false;
      AxisAlignedBB box = entity.getEntityBoundingBox();
      Vec3 eye = this.context.player().getPositionEyes(1F);
      double x = Math.max(box.minX, Math.min(box.maxX, eye.xCoord)) - eye.xCoord;
      double y = Math.max(box.minY, Math.min(box.maxY, eye.yCoord)) - eye.yCoord;
      double z = Math.max(box.minZ, Math.min(box.maxZ, eye.zCoord)) - eye.zCoord;
      return x*x + y*y + z*z <= rangeSq;
   }
   private boolean hasTarget() {
      double range = number("range"); double rangeSq = range * range;
      MovingObjectPosition hit = this.context.minecraft().objectMouseOver;
      if (hit != null && valid(hit.entityHit, rangeSq)) return true;
      for (Object value : this.context.world().playerEntities) if (valid((Entity)value, rangeSq)) return true;
      return false;
   }
   private boolean aimingPlayer(boolean rangeLimited) {
      MovingObjectPosition hit = this.context.minecraft().objectMouseOver;
      if (hit == null || !(hit.entityHit instanceof EntityPlayer)) return false;
      double range = number("range");
      return !rangeLimited || valid(hit.entityHit, range*range);
   }
   private boolean potionBlocked() {
      return this.context.player().isPotionActive(Potion.jump) || this.context.player().isPotionActive(Potion.poison)
         || this.context.player().isPotionActive(Potion.wither);
   }
   private boolean fallIgnored(long now) {
      return this.ignoreFallDamage && now <= this.fallDamageIgnoreUntil
         || this.context.player().onGround && !this.context.player().capabilities.allowFlying
            && (this.previousFallDistance > 3.0 || this.maximumFallDistance > 3.0);
   }
   private void trackFall(long now) {
      if (this.context.player().capabilities.allowFlying) {
         this.ignoreFallDamage = false; this.maximumFallDistance = 0; return;
      }
      this.maximumFallDistance = Math.max(this.maximumFallDistance, this.context.player().fallDistance);
      if (this.context.player().onGround && Math.max(this.previousFallDistance, this.maximumFallDistance) > 3.0) {
         this.ignoreFallDamage = true; this.fallDamageIgnoreUntil = now + 750L; this.maximumFallDistance = 0;
      }
      if (this.ignoreFallDamage && now > this.fallDamageIgnoreUntil) {
         this.ignoreFallDamage = false; this.maximumFallDistance = 0;
      }
      if (this.context.player().onGround && !this.ignoreFallDamage) this.maximumFallDistance = 0;
      this.previousFallDistance = this.context.player().fallDistance;
   }
   private boolean resetAllowed(long now) {
      if (fallIgnored(now) || this.context.player().isBurning() || potionBlocked()) return false;
      if (flag("require_mouse") && !this.context.minecraft().gameSettings.keyBindAttack.isKeyDown()) return false;
      if (flag("require_aim") && (!aimingPlayer(false) || !withinKnockbackFov())) return false;
      // Jade passes allow-air and override-forward=true for Hard Reset's packet trigger.
      double chance = number("reset_chance");
      return chance >= 100.0 || Math.random()*100.0 < chance;
   }
   private boolean withinKnockbackFov() {
      float forward = this.context.player().movementInput == null ? 0 : this.context.player().movementInput.moveForward;
      float strafe = this.context.player().movementInput == null ? 0 : this.context.player().movementInput.moveStrafe;
      float factor = forward < 0 ? -.5F : forward > 0 ? .5F : 1F;
      float yaw = forward < 0 ? this.context.player().rotationYaw + 180F : this.context.player().rotationYaw;
      if (strafe > 0) yaw -= 90F*factor; else if (strafe < 0) yaw += 90F*factor;
      // Preserve Jade's helper return units (radians), including its comparison to the motion yaw.
      float inputAngle = yaw * (float)(Math.PI / 180.0);
      float motionYaw = (float)(-Math.atan2(this.context.player().motionX, this.context.player().motionZ) * (float)(180.0/Math.PI));
      float delta = RotationMath.wrap((inputAngle - motionYaw) % 360F);
      return delta > 0 ? delta < 165F : delta > -165F;
   }
   private boolean delayAllowed() {
      if (!hasTarget() || flag("in_air") && this.context.player().onGround
         || flag("looking_at_player") && !aimingPlayer(true)
         || flag("require_left_mouse") && !KeyState.physical(this.context.minecraft().gameSettings.keyBindAttack)) return false;
      if (flag("restrict_held_item")) {
         ItemStack held = this.context.player().getHeldItem();
         if (held == null || !ItemPolicyService.contains(this.settings.watchdogItems(), Item.getIdFromItem(held.getItem()))) return false;
      }
      return true;
   }
   private void velocity(S12PacketEntityVelocity packet, long now) {
      if (!resetAllowed(now) || !hasTarget()) return;
      double x = packet.getMotionX()/8000.0, z = packet.getMotionZ()/8000.0;
      this.pendingKnockbackSpeed = Math.sqrt(x*x+z*z);
      this.pendingReset = true;
      this.resetDueAtMillis = now + (flag("delay") ? Math.max(0L, (long)number("max_delay")-50L) : 0L);
      if (flag("delay") && this.timeout == null) {
         double chance = number("delay_chance");
         this.delayApplied = delayAllowed() && (chance >= 100.0 || Math.random()*100.0 < chance);
         this.inboundDelayMillis = this.delayApplied ? (long)number("max_delay") : 50L;
         this.timeout = new TimeoutCondition(now, this.inboundDelayMillis + 50L);
         this.heldWorld = this.context.world(); this.heldHandler = this.context.minecraft().getNetHandler();
      }
   }
   public synchronized void packet(SemanticEvent event) {
      if (event.packet == null) return;
      Packet<?> packet = event.packet;
      if (packet instanceof S01PacketJoinGame || packet instanceof S07PacketRespawn || packet instanceof S40PacketDisconnect) {
         reset(false); return;
      }
      if (!ready() || !conditionsAllowed()) { reset(true); return; }
      if (packet instanceof S08PacketPlayerPosLook) { clearPendingReset(); releaseHeld(true); return; }
      if (conflict()) { reset(true); return; }
      if (event.dispatchMode != 0) return;
      long now = this.clock.getAsLong();
      maintainDelay(now);
      // Own hurt status remains immediate, as Jade's markHandled path does.
      if (this.timeout != null && packet instanceof S19PacketEntityStatus) {
         S19PacketEntityStatus status = (S19PacketEntityStatus)packet;
         if (status.getOpCode() == 2 && status.getEntity(this.context.world()) == this.context.player()) return;
      }
      if (packet instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity)packet).getEntityID() == this.context.player().getEntityId()) {
         velocity((S12PacketEntityVelocity)packet, now);
      }
      if (this.timeout != null) {
         this.held.addLast(new HeldPacket(packet, now));
         event.dispatchMode = 2; event.cancelled = true;
      }
   }
   public synchronized void tickStart() {
      if (!ready() || conflict() || !conditionsAllowed()) { reset(true); return; }
      boolean delay = flag("delay");
      if (delay != this.delaySetting) { clearPendingReset(); releaseHeld(true); this.delaySetting = delay; }
      maintainDelay(this.clock.getAsLong());
   }
   private void maintainDelay(long now) {
      if (this.timeout == null) return;
      if (this.context.world() != this.heldWorld || this.context.minecraft().getNetHandler() != this.heldHandler) { reset(false); return; }
      if (!hasTarget() || this.delayApplied && !delayAllowed()) { clearPendingReset(); releaseHeld(true); return; }
      if (this.timeout.isOpen(now)) { releaseHeld(true); return; }
      while (!this.held.isEmpty() && now - this.held.peekFirst().added >= this.inboundDelayMillis) deliver(this.held.removeFirst().packet);
   }
   public synchronized void preUpdate() {
      if (!ready() || conflict() || !conditionsAllowed()) { reset(true); return; }
      long now = this.clock.getAsLong();
      if (this.pendingReset && now >= this.resetDueAtMillis) {
         if (hasTarget() && !this.context.player().isBurning() && !potionBlocked()) {
            KeyState.set(this.context.minecraft().gameSettings.keyBindJump, true); this.jumpOwned = true;
            this.forwardHoldUntilMillis = Math.max(this.forwardHoldUntilMillis, now + forwardHoldMillis(this.pendingKnockbackSpeed));
            // With the forward condition enabled, never manufacture forward input:
            // releasing forward must be able to stop this operation.
            if (!flag("require_forward") && !KeyState.physical(this.context.minecraft().gameSettings.keyBindForward)) {
               KeyState.set(this.context.minecraft().gameSettings.keyBindForward, true); this.forwardOwned = true;
            }
         }
         clearPendingReset();
      }
      trackFall(now);
   }
   public synchronized void postUpdate() {
      if (!ready() || !conditionsAllowed()) { reset(true); return; }
      if (this.jumpOwned) { KeyState.restore(this.context.minecraft().gameSettings.keyBindJump); this.jumpOwned = false; }
      if (this.forwardOwned && this.clock.getAsLong() >= this.forwardHoldUntilMillis) {
         KeyState.restore(this.context.minecraft().gameSettings.keyBindForward); this.forwardOwned = false;
      }
   }
   public static long forwardHoldMillis(double speed) {
      if (speed <= .45) return 180L;
      if (speed >= .75) return 360L;
      return 180L + Math.round(180.0 * ((speed-.45)/.3));
   }
   private void clearPendingReset() { this.pendingReset = false; this.resetDueAtMillis = 0L; this.pendingKnockbackSpeed = 0; }
   private void releaseHeld(boolean apply) {
      boolean sameWorld = this.context.ready() && this.context.world() == this.heldWorld
         && this.context.minecraft().getNetHandler() == this.heldHandler;
      this.timeout = null;
      while (!this.held.isEmpty()) {
         Packet<?> packet = this.held.removeFirst().packet;
         if (apply && sameWorld) deliver(packet);
      }
      this.heldWorld = null; this.heldHandler = null; this.inboundDelayMillis = 0; this.delayApplied = false;
   }
   @SuppressWarnings({"rawtypes", "unchecked"})
   protected void deliver(Packet<?> packet) {
      final NetHandlerPlayClient handler = this.context.minecraft().getNetHandler();
      final WorldClient world = this.context.world();
      if (handler == null || world == null) return;
      this.context.minecraft().addScheduledTask(() -> {
         if (this.context.world() == world && this.context.minecraft().getNetHandler() == handler) {
            try { ((Packet)packet).processPacket(handler); }
            catch (ThreadQuickExitException alreadyScheduled) { /* Vanilla owns the scheduled processing. */ }
         }
      });
   }
   public synchronized void reset(boolean release) {
      clearPendingReset(); releaseHeld(release);
      if (this.context.minecraft().gameSettings != null) {
         if (this.jumpOwned) KeyState.restore(this.context.minecraft().gameSettings.keyBindJump);
         if (this.forwardOwned) KeyState.restore(this.context.minecraft().gameSettings.keyBindForward);
      }
      this.jumpOwned = this.forwardOwned = this.ignoreFallDamage = false;
      this.fallDamageIgnoreUntil = this.forwardHoldUntilMillis = 0;
      this.previousFallDistance = this.maximumFallDistance = 0;
   }
   public synchronized boolean isInboundDelaying() { return this.timeout != null; }
   public synchronized int retained() { return this.held.size(); }
   private static final class HeldPacket {
      final Packet<?> packet; final long added;
      HeldPacket(Packet<?> packet, long added) { this.packet = packet; this.added = added; }
   }
}
