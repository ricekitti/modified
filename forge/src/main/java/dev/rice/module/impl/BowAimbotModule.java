package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.MinecraftFields;
import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.client.rotation.RotationService;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.impl.bowaimbot.AimResult;
import dev.rice.module.impl.bowaimbot.AimResult$0;
import dev.rice.module.impl.bowaimbot.AimSolver;
import dev.rice.module.impl.bowaimbot.AimSolver$2;
import dev.rice.module.impl.bowaimbot.EntityPositionTracker;
import dev.rice.module.impl.bowaimbot.MotionPredictor;
import dev.rice.module.impl.bowaimbot.MotionPredictor$1;
import dev.rice.module.impl.bowaimbot.ProjectileMotion;
import dev.rice.module.impl.bowaimbot.TargetPrediction;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Bow Aimbot: native Rice port of OpenJade's Auto Aim ("jade.deps.eLz.qFcqqGTsq"), byte-faithful to the
 * original aiming behaviour. The prediction/solve/tracking backend ({@code dev.rice.module.impl.bowaimbot.*})
 * is a 1:1 copy of OpenJade's; this class is the Rice module shell that routes SemanticEvents and registers
 * settings. Rotation publish mirrors Block In / AutoClutch (claim at pre-update 17, finalize at 13).
 */
public final class BowAimbotModule extends Module {
   private static final double TARGET_SWITCH_MARGIN = 1.25;
   private static final int ROTATION_PRIORITY = 800;
   private static BowAimbotModule instance;
   private static final Method SYNC_CURRENT_ITEM =
      ReflectionHelper.findMethod(PlayerControllerMP.class, (PlayerControllerMP)null, new String[]{"syncCurrentPlayItem", "func_78750_j"});

   private final Minecraft mc = Minecraft.getMinecraft();

   private final NumberSetting fov;
   private final NumberSetting range;
   private final BooleanSetting bow;
   private final BooleanSetting rod;
   private final BooleanSetting players;
   private final BooleanSetting teammates;
   private final BooleanSetting invisible;
   private final NumberSetting bowFlight;
   private final NumberSetting minimumConfidence;
   private final ChoiceSetting rodMode;
   private final NumberSetting minimumRodRange;
   private final NumberSetting maximumRodRange;
   private final NumberSetting cooldown;
   private final BooleanSetting notWhilstHoldingLeft;
   private final BooleanSetting notWhilstHoldingRight;
   private final BooleanSetting holdingWeapon;
   private final BooleanSetting opponentLooking;
   private final BooleanSetting visualise;

   private final MotionPredictor motionPredictor = new MotionPredictor();
   private final EntityPositionTracker entityPositionTracker = new EntityPositionTracker();
   private final AimSolver aimSolver = new AimSolver(this.motionPredictor);
   private final Set<Integer> live = new HashSet<>();
   private final Set<Integer> dead = new HashSet<>();
   private final Map<Integer, EntityPlayer> trackedPlayersById = new HashMap<>();
   private EntityPlayer entityPlayer;
   private AimResult aimSolution;
   private PendingAction pendingAction = PendingAction.NONE;
   private RodState rodState = RodState.READY;
   private int hitStreak;
   private int cooldownTicks;
   private int castStartTick;
   private int missStreak;
   private double smoothedLatencyMillis;
   private boolean rodCastActive;
   private boolean swappedToRod;
   private int storedSwordSlot = -1;
   private int visualisedEntityId = -1;
   private long lastVisualisationNanos;
   private double smoothedEntityX;
   private double smoothedEntityY;
   private double smoothedEntityZ;
   private double smoothedAimX;
   private double smoothedAimY;
   private double smoothedAimZ;
   private boolean aimIndicatorInitialised;
   private boolean smoothingInitialised;
   private float smoothedYaw;
   private float smoothedPitch;

   private boolean hasRotation;
   private float publishYaw;
   private float publishPitch;

   private enum PendingAction {
      NONE, RELEASE_BOW, SWAP_TO_ROD, CAST_ROD, RECYCLE_ROD, REEL_ROD_HIT, REEL_ROD_MISS;
   }

   private enum RodState {
      READY, CAST, COOLDOWN;
   }

   public BowAimbotModule() {
      super("bowaimbot", "Bow Aimbot", "Predictive bow and rod aimbot", ModuleCategory.COMBAT);
      this.bow = this.add(new BooleanSetting("bow", "Bow", true));
      this.rod = this.add(new BooleanSetting("rod", "Rod", true));
      this.fov = this.add(new NumberSetting("fov", "FOV", 8.0, 1.0, 360.0, 1.0));
      this.range = this.add(new NumberSetting("range", "Range", 80.0, 8.0, 160.0, 1.0));
      this.players = this.add(new BooleanSetting("players", "Players", true));
      this.teammates = this.add(new BooleanSetting("teammates", "Teammates", false));
      this.invisible = this.add(new BooleanSetting("invisible", "Invisible", false));
      this.bowFlight = this.add(new NumberSetting("bow_flight", "Bow Flight", 1.5, 0.2, 3.5, 0.1));
      this.minimumConfidence = this.add(new NumberSetting("minimum_confidence", "Minimum Confidence", 40.0, 20.0, 95.0, 5.0));
      this.rodMode = this.add(new ChoiceSetting("rod_mode", "Rod Mode", 0, "Manual", "Automatic"));
      this.minimumRodRange = this.add(new NumberSetting("minimum_rod_range", "Minimum Rod Range", 2.8, 1.0, 10.0, 0.1));
      this.maximumRodRange = this.add(new NumberSetting("maximum_rod_range", "Maximum Rod Range", 4.5, 1.0, 12.0, 0.1));
      this.cooldown = this.add(new NumberSetting("cooldown", "Cooldown", 0.5, 0.05, 1.5, 0.05));
      this.notWhilstHoldingLeft = this.add(new BooleanSetting("not_whilst_holding_left", "Not whilst holding left", true));
      this.notWhilstHoldingRight = this.add(new BooleanSetting("not_whilst_holding_right", "Not whilst holding right", true));
      this.holdingWeapon = this.add(new BooleanSetting("holding_weapon", "Holding weapon", true));
      this.opponentLooking = this.add(new BooleanSetting("opponent_looking", "Opponent looking", true));
      this.visualise = this.add(new BooleanSetting("visualise", "Visualise", false));
      instance = this;
   }

   @Override
   public boolean settingVisible(String key) {
      if (key.equals("minimum_rod_range") || key.equals("maximum_rod_range")) {
         return this.rod.isEnabled() && this.isAutomaticRod();
      }
      return true;
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.resetModuleState();
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.resetModuleState();
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      this.resetModuleState();
   }

   @Override
   public boolean listens(int type) {
      // 2 inbound packet, 17 pre-update brain + action, 13 rotation finalize, 26 world unload.
      return type == 2 || type == 13 || type == 17 || type == 26;
   }

   @Override
   public void onEvent(ClientContext context, SemanticEvent event) {
      if (event.type == 2) {
         if (event.packet != null && this.mc.theWorld != null) {
            this.entityPositionTracker.onPacketReceived(event.packet, this.mc.theWorld);
         }
      } else if (event.type == 26) {
         this.resetModuleState();
      } else if (event.type == 17) {
         this.runBrain();
         this.executePending();
      } else if (event.type == 13 && this.hasRotation) {
         event.active = true;
         event.overrideRotation = true;
         event.yaw = this.publishYaw;
         event.pitch = this.publishPitch;
      }
   }

   @Override
   public void onClientTickStart(ClientContext context) {
      if (!this.inWorld()) {
         this.resetCombatState();
      } else {
         this.entityPositionTracker.flushToPredictor(this.motionPredictor);
         this.motionPredictor.setCurrentTime(System.nanoTime() / 5.0E7);
         this.purgeRemovedPlayers();
         this.updatePlayerTracking();
         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            if (this.cooldownTicks == 0) {
               this.rodState = RodState.READY;
            }
         }

         this.tickFishingRod();
      }
   }

   // ---- rotation brain (OpenJade AutoAim.onRotation), publishes this.publishYaw/Pitch for event 13 ----
   private void runBrain() {
      this.hasRotation = false;
      if (!this.canOperate()) {
         if (this.mc.thePlayer == null || this.mc.thePlayer.fishEntity == null) {
            this.restoreHotbarSlot();
         }

         this.clearAimSolution(false);
         this.clearRotationSmoothing();
         return;
      }

      boolean usingBow = this.isUsingBow();
      boolean bowDrawn = usingBow && this.isBowFullyDrawn();
      boolean autoRod = this.isAutomaticRod() && this.rod.isEnabled() && !usingBow;
      boolean manualRod = !this.isAutomaticRod() && this.rod.isEnabled() && this.isHoldingRod();
      if (!usingBow && !manualRod && !autoRod) {
         this.clearAimSolution(true);
         this.clearRotationSmoothing();
         return;
      }

      if (autoRod && !this.areAutomaticConditionsMet()) {
         if (this.swappedToRod) {
            this.restoreHotbarSlot();
         }

         this.clearAimSolution(true);
         return;
      }

      EntityPlayer previous = this.entityPlayer;
      this.entityPlayer = autoRod ? this.selectRodTarget(this.entityPlayer) : this.selectBowTarget(this.entityPlayer);
      if (this.entityPlayer != previous) {
         this.aimSolution = null;
         this.hitStreak = 0;
         this.pendingAction = PendingAction.NONE;
         this.resetAimVisualisation();
      }

      if (this.entityPlayer == null) {
         if (this.swappedToRod && this.mc.thePlayer.fishEntity == null) {
            this.restoreHotbarSlot();
         }

         this.aimSolution = null;
         this.hitStreak = 0;
         if (usingBow) {
            this.driftTowardPlayerAngles();
         }

         return;
      }

      if (autoRod && this.rodState == RodState.READY && this.mc.thePlayer.fishEntity == null && !this.isHoldingRod()) {
         int rodSlot = this.findHotbarSlot(Items.fishing_rod);
         int swordSlot = this.findSwordHotbarSlot();
         if (rodSlot < 0 || swordSlot < 0) {
            this.clearAimSolution(true);
            return;
         }

         this.storedSwordSlot = swordSlot;
      }

      if (autoRod && this.isHoldingRod() && !this.swappedToRod) {
         this.storedSwordSlot = this.findSwordHotbarSlot();
         if (this.storedSwordSlot < 0) {
            return;
         }

         this.swappedToRod = true;
      }

      double confidence = this.minimumConfidence.doubleValue() / 100.0;
      double latencyTicks = this.smoothedLatencyMillis / 100.0;
      if (usingBow) {
         boolean recentVelocity = this.motionPredictor.hasRecentVelocity(this.entityPlayer.getEntityId());
         if (recentVelocity) {
            this.aimSolution = null;
            this.hitStreak = 0;
            this.pendingAction = PendingAction.NONE;
            this.applySmoothedRotation();
         } else {
            double drawProgress = this.getBowDrawProgress();
            double lead = latencyTicks + this.getLateralLeadCorrection(this.entityPlayer);
            this.aimSolution = this.aimSolver.UMZR(this.mc.theWorld, this.mc.thePlayer, this.entityPlayer, AimSolver$2.BOW, 3.0, lead, confidence);
            this.updateAimOutcome(this.aimSolution);
            if (this.isConfirmedHit(this.aimSolution)
               && this.isTargetReachable(this.aimSolution)
               && bowDrawn
               && drawProgress >= 1.0
               && this.aimSolution.getTicks() <= this.getBowFlightTicks()
               && this.isWithinAngleTolerance(this.aimSolution, 1.5, 1.5)
               && this.hitStreak >= 2) {
               this.pendingAction = PendingAction.RELEASE_BOW;
            }
         }
      } else if (this.rodState == RodState.READY && this.mc.thePlayer.fishEntity == null) {
         this.aimSolution = this.aimSolver.UMZR(this.mc.theWorld, this.mc.thePlayer, this.entityPlayer, AimSolver$2.FISHING_ROD, 1.5, latencyTicks, confidence);
         this.updateAimOutcome(this.aimSolution);
         if (this.isConfirmedHit(this.aimSolution) && this.isWithinAngleTolerance(this.aimSolution, 3.0, 3.0) && this.hitStreak >= 2) {
            this.pendingAction = autoRod && !this.isHoldingRod() ? PendingAction.SWAP_TO_ROD : PendingAction.CAST_ROD;
         }
      }

      boolean reachable = !usingBow
         || this.aimSolution != null && this.aimSolution.getTicks() <= this.getBowFlightTicks() && this.isTargetReachable(this.aimSolution);
      if (this.isViableSolution(this.aimSolution) && reachable) {
         if (usingBow) {
            this.smoothTowardTargetAngles(this.aimSolution.getYaw(), this.aimSolution.getPitch());
         } else {
            this.publish(this.aimSolution.getYaw(), this.aimSolution.getPitch());
         }
      } else if (usingBow) {
         this.applySmoothedRotation();
      }
   }

   // ---- pending action execution (OpenJade AutoAim.onPreUpdate) ----
   private void executePending() {
      if (this.pendingAction == PendingAction.NONE || !this.canOperate()) {
         return;
      }

      PendingAction action = this.pendingAction;
      this.pendingAction = PendingAction.NONE;
      if (action == PendingAction.RELEASE_BOW
         && this.isUsingBow()
         && this.isBowFullyDrawn()
         && this.entityPlayer != null
         && !this.entityPositionTracker.hasPendingTeleport(this.entityPlayer.getEntityId())) {
         this.mc.playerController.onStoppedUsingItem(this.mc.thePlayer);
         if (Mouse.isButtonDown(1)) {
            MinecraftFields.setRightClickDelayTimer(this.mc, 0);
         }

         this.hitStreak = 0;
      } else if (action == PendingAction.SWAP_TO_ROD && this.storedSwordSlot >= 0) {
         int rodSlot = this.findHotbarSlot(Items.fishing_rod);
         if (rodSlot >= 0) {
            this.swappedToRod = true;
            this.selectHotbarSlot(rodSlot);
            ItemStack held = this.mc.thePlayer.getHeldItem();
            if (held != null && held.getItem() == Items.fishing_rod && this.mc.thePlayer.fishEntity == null) {
               this.mc.playerController.sendUseItem(this.mc.thePlayer, this.mc.theWorld, held);
               this.rodState = RodState.CAST;
               this.castStartTick = this.mc.thePlayer.ticksExisted;
               this.rodCastActive = true;
               this.hitStreak = 0;
            }
         }
      } else if (this.isRodAction(action) && this.isHoldingRod()) {
         ItemStack held = this.mc.thePlayer.getHeldItem();
         boolean casting = this.mc.thePlayer.fishEntity != null;
         if (held != null) {
            if (action == PendingAction.CAST_ROD && !casting) {
               this.mc.playerController.sendUseItem(this.mc.thePlayer, this.mc.theWorld, held);
               this.rodState = RodState.CAST;
               this.castStartTick = this.mc.thePlayer.ticksExisted;
               this.rodCastActive = true;
            } else if (action == PendingAction.RECYCLE_ROD) {
               this.rodCastActive = false;
               this.rodState = RodState.READY;
               this.cooldownTicks = 0;
               this.aimSolution = null;
               if (this.swappedToRod) {
                  this.restoreHotbarSlot();
               } else if (casting) {
                  this.mc.playerController.sendUseItem(this.mc.thePlayer, this.mc.theWorld, held);
               }
            } else if (action == PendingAction.REEL_ROD_HIT || action == PendingAction.REEL_ROD_MISS) {
               this.rodCastActive = false;
               if (this.swappedToRod) {
                  this.beginRodCooldown();
                  this.restoreHotbarSlot();
               } else {
                  if (casting) {
                     this.mc.playerController.sendUseItem(this.mc.thePlayer, this.mc.theWorld, held);
                  }

                  this.beginRodCooldown();
               }
            }

            this.hitStreak = 0;
         }
      }
   }

   private void tickFishingRod() {
      if (!this.rod.isEnabled()) {
         this.restoreHotbarSlot();
         this.rodState = RodState.READY;
      } else if (!this.isHoldingRod()) {
         if (this.rodState != RodState.COOLDOWN && (!this.swappedToRod || this.pendingAction != PendingAction.SWAP_TO_ROD)) {
            this.swappedToRod = false;
            this.storedSwordSlot = -1;
            this.rodState = RodState.READY;
         }
      } else {
         EntityFishHook hook = this.mc.thePlayer.fishEntity;
         if (hook == null) {
            int timeout = 10 + (int)Math.ceil(this.smoothedLatencyMillis / 50.0);
            if (this.rodState == RodState.CAST && this.mc.thePlayer.ticksExisted - this.castStartTick > timeout) {
               this.beginRodCooldown();
            }
         } else if (this.rodCastActive) {
            this.rodState = RodState.CAST;
            boolean hit = this.entityPlayer != null && (hook.caughtEntity == this.entityPlayer || this.hookIntersectsTarget(hook, this.entityPlayer));
            boolean caughtOther = hook.caughtEntity != null && hook.caughtEntity != this.entityPlayer;
            int flightTicks = this.aimSolution == null ? 20 : this.aimSolution.getTicks();
            boolean overTime = hook.ticksExisted > flightTicks + 10;
            boolean missedPath = hook.ticksExisted > 3 && !this.hookOnTarget(hook);
            boolean recycle = this.shouldRecycleHook(hook, flightTicks);
            if (hit) {
               this.pendingAction = PendingAction.REEL_ROD_HIT;
            } else if (recycle) {
               this.pendingAction = PendingAction.RECYCLE_ROD;
            } else if (caughtOther || overTime || missedPath || hook.isDead) {
               this.pendingAction = PendingAction.REEL_ROD_MISS;
            }
         }
      }
   }

   private boolean shouldRecycleHook(EntityFishHook hook, int flightTicks) {
      if (this.entityPlayer == null) {
         return true;
      } else {
         double extra = Math.max(1.0, (double)(flightTicks - hook.ticksExisted));
         TargetPrediction prediction =
            this.motionPredictor.ygp3(this.entityPlayer.getEntityId(), this.smoothedLatencyMillis / 100.0 + extra, this.createCollisionCheck(this.entityPlayer));
         return prediction == null || prediction.getConfidence() < this.minimumConfidence.doubleValue() / 100.0;
      }
   }

   private boolean hookIntersectsTarget(EntityFishHook hook, EntityPlayer target) {
      AxisAlignedBB box = target.getEntityBoundingBox().expand(0.35, 0.35, 0.35);
      AxisAlignedBB hookBox = hook.getEntityBoundingBox();
      return hookBox != null && box.intersectsWith(hookBox);
   }

   private boolean hookOnTarget(EntityFishHook hook) {
      if (this.entityPlayer != null && this.isValidTarget(this.entityPlayer, 360.0)) {
         double x = hook.posX;
         double y = hook.posY;
         double z = hook.posZ;
         double mx = hook.motionX;
         double my = hook.motionY;
         double mz = hook.motionZ;
         double latency = this.smoothedLatencyMillis / 100.0;
         MotionPredictor$1 collision = this.createCollisionCheck(this.entityPlayer);

         for (int tick = 1; tick <= 24; tick++) {
            Vec3 from = new Vec3(x, y, z);
            Vec3 to = new Vec3(x + mx, y + my, z + mz);
            MovingObjectPosition block = this.mc.theWorld.rayTraceBlocks(from, to, false, true, false);
            TargetPrediction prediction = this.motionPredictor.ygp3(this.entityPlayer.getEntityId(), latency + tick, collision);
            if (prediction == null) {
               return true;
            }

            double half = this.entityPlayer.width * 0.5;
            AxisAlignedBB predicted = new AxisAlignedBB(
                  prediction.RAYFZ() - half,
                  prediction.fmmQ(),
                  prediction.getZ() - half,
                  prediction.RAYFZ() + half,
                  prediction.fmmQ() + this.entityPlayer.height,
                  prediction.getZ() + half
               )
               .expand(0.3, 0.3, 0.3);
            MovingObjectPosition targetHit = predicted.calculateIntercept(from, to);
            if (targetHit != null && (block == null || from.squareDistanceTo(targetHit.hitVec) <= from.squareDistanceTo(block.hitVec))) {
               return true;
            }

            if (block != null) {
               return false;
            }

            x += mx;
            y += my;
            z += mz;
            mx *= 0.92;
            my = my * 0.92 - 0.04;
            mz *= 0.92;
         }

         return false;
      } else {
         return false;
      }
   }

   private void beginRodCooldown() {
      this.rodState = RodState.COOLDOWN;
      this.cooldownTicks = this.swappedToRod ? Math.max(1, (int)Math.round(this.cooldown.doubleValue() * 20.0)) : 1;
      this.aimSolution = null;
      this.hitStreak = 0;
      this.rodCastActive = false;
   }

   private void updateAimOutcome(AimResult result) {
      if (this.isConfirmedHit(result)) {
         this.hitStreak++;
         this.missStreak = 0;
      } else if (this.isViableSolution(result)) {
         this.hitStreak = 0;
         this.missStreak = 0;
      } else {
         this.hitStreak = 0;
         this.missStreak++;
         if (this.missStreak > 20) {
            this.entityPlayer = null;
         }
      }
   }

   private boolean isViableSolution(AimResult result) {
      return result != null && (result.WYqjD() == AimResult$0.HIT || result.WYqjD() == AimResult$0.LOW_CONFIDENCE);
   }

   private boolean isConfirmedHit(AimResult result) {
      return result != null && result.WYqjD() == AimResult$0.HIT;
   }

   private boolean isRodAction(PendingAction action) {
      return action == PendingAction.CAST_ROD
         || action == PendingAction.RECYCLE_ROD
         || action == PendingAction.REEL_ROD_HIT
         || action == PendingAction.REEL_ROD_MISS;
   }

   private boolean isWithinAngleTolerance(AimResult result, double yawTolerance, double pitchTolerance) {
      if (result != null) {
         float[] last = this.lastSentRotation();
         double yawDiff = Math.abs(MathHelper.wrapAngleTo180_float(result.getYaw() - last[0]));
         double pitchDiff = Math.abs(result.getPitch() - last[1]);
         return yawDiff <= yawTolerance && pitchDiff <= pitchTolerance;
      } else {
         return false;
      }
   }

   private boolean isTargetReachable(AimResult result) {
      TargetPrediction prediction = result == null ? null : result.getPrediction();
      if (prediction != null && !(prediction.getConfidence() < 0.18)) {
         double base = this.entityPlayer == null ? prediction.fmmQ() : this.entityPlayer.serverPosY / 32.0;
         double delta = prediction.fmmQ() - base;
         PotionEffect jump = this.entityPlayer == null ? null : this.entityPlayer.getActivePotionEffect(Potion.jump);
         double ceiling = 1.5 + (jump == null ? 0.0 : (jump.getAmplifier() + 1) * 0.75);
         return delta >= -1.5 && delta <= ceiling;
      } else {
         return false;
      }
   }

   private void updatePlayerTracking() {
      this.live.clear();

      for (EntityPlayer player : this.mc.theWorld.playerEntities) {
         if (player != this.mc.thePlayer) {
            int id = player.getEntityId();
            EntityPlayer prior = this.trackedPlayersById.put(id, player);
            if (!player.isDead && player.deathTime <= 0 && !(player.getHealth() <= 0.0F)) {
               this.live.add(id);
               double sx = player.serverPosX / 32.0;
               double sy = player.serverPosY / 32.0;
               double sz = player.serverPosZ / 32.0;
               boolean reacquired = this.dead.remove(id) || prior != null && prior != player;
               if (reacquired) {
                  this.motionPredictor.AVgoK(id);
                  this.entityPositionTracker.removeEntity(id);
                  this.entityPositionTracker.MVkfPjx(id, sx, sy, sz);
                  if (this.entityPlayer != null && this.entityPlayer.getEntityId() == id) {
                     this.clearAimSolution(true);
                     this.resetAimVisualisation();
                  }
               }

               PotionEffect jump = player.getActivePotionEffect(Potion.jump);
               ItemStack using = player.getItemInUse();
               ItemStack held = player.getHeldItem();
               boolean blocking = player.isBlocking() || player.isUsingItem() && using != null && using.getItem() instanceof ItemBow;
               boolean placing = held != null && held.getItem() instanceof ItemBlock && player.rotationPitch > 70.0F;
               this.motionPredictor
                  .updateMotionStateInternal(id, player.rotationYawHead, blocking, player.isSprinting(), player.onGround, jump == null ? -1 : jump.getAmplifier(), placing);
               if (this.motionPredictor.hasSamples(id) && this.motionPredictor.computeSquaredDistanceToLastSample(id, sx, sy, sz) > 2.25) {
                  this.motionPredictor.AVgoK(id);
                  this.entityPositionTracker.MVkfPjx(id, sx, sy, sz);
               } else if (!this.motionPredictor.hasSamples(id)) {
                  this.entityPositionTracker.trackInitialPosition(id, sx, sy, sz);
               }
            } else {
               this.dead.add(id);
               this.motionPredictor.AVgoK(id);
               this.entityPositionTracker.removeEntity(id);
               if (this.entityPlayer != null && this.entityPlayer.getEntityId() == id) {
                  this.clearAimSolution(true);
                  this.resetAimVisualisation();
               }
            }
         }
      }

      if (this.entityPlayer != null && !this.live.contains(this.entityPlayer.getEntityId())) {
         this.motionPredictor.AVgoK(this.entityPlayer.getEntityId());
         this.entityPlayer = null;
      }

      NetworkPlayerInfo info = this.mc.getNetHandler().getPlayerInfo(this.mc.thePlayer.getUniqueID());
      if (info != null && info.getResponseTime() >= 0) {
         double ping = Math.min(1000.0, (double)info.getResponseTime());
         this.smoothedLatencyMillis = this.smoothedLatencyMillis == 0.0 ? ping : this.smoothedLatencyMillis * 0.85 + ping * 0.15;
      }
   }

   private void purgeRemovedPlayers() {
      int[] removed = this.entityPositionTracker.Thdhe();

      for (int id : removed) {
         this.motionPredictor.AVgoK(id);
         this.dead.add(id);
         this.trackedPlayersById.remove(id);
         if (this.entityPlayer != null && this.entityPlayer.getEntityId() == id) {
            this.clearAimSolution(true);
            this.resetAimVisualisation();
         }
      }
   }

   private EntityPlayer selectBowTarget(EntityPlayer current) {
      double fov = this.fov.doubleValue();
      boolean currentValid = current != null && this.isValidTarget(current, fov);
      double currentAngle = currentValid ? this.getCrosshairAngle(current) : Double.MAX_VALUE;
      EntityPlayer best = null;
      double bestAngle = fov;

      for (EntityPlayer player : this.mc.theWorld.playerEntities) {
         if (this.isValidTarget(player, fov)) {
            double angle = this.getCrosshairAngle(player);
            if (angle < bestAngle) {
               bestAngle = angle;
               best = player;
            }
         }
      }

      return currentValid && best != current && !exceedsSwitchMargin(currentAngle, bestAngle) ? current : best;
   }

   private EntityPlayer selectRodTarget(EntityPlayer current) {
      double fov = this.fov.doubleValue();
      boolean currentValid = current != null && this.isValidRodTarget(current, fov);
      double currentAngle = currentValid ? this.getCrosshairAngle(current) : Double.MAX_VALUE;
      EntityPlayer best = null;
      double bestAngle = fov;

      for (EntityPlayer player : this.mc.theWorld.playerEntities) {
         if (this.isValidRodTarget(player, fov)) {
            double angle = this.getCrosshairAngle(player);
            if (angle < bestAngle) {
               bestAngle = angle;
               best = player;
            }
         }
      }

      return currentValid && best != current && !exceedsSwitchMargin(currentAngle, bestAngle) ? current : best;
   }

   private static boolean exceedsSwitchMargin(double currentAngle, double bestAngle) {
      return bestAngle + TARGET_SWITCH_MARGIN < currentAngle;
   }

   private boolean isValidRodTarget(EntityPlayer player, double fov) {
      if (!this.isValidTarget(player, fov)) {
         return false;
      } else if (this.opponentLooking.isEnabled() && !this.isOpponentLookingAtUs(player)) {
         return false;
      } else {
         double min = Math.min(this.minimumRodRange.doubleValue(), this.maximumRodRange.doubleValue());
         double max = Math.max(this.minimumRodRange.doubleValue(), this.maximumRodRange.doubleValue());
         double distance = this.mc.thePlayer.getDistanceSqToEntity(player);
         return distance >= min * min && distance <= max * max;
      }
   }

   private boolean isOpponentLookingAtUs(EntityPlayer player) {
      double dx = this.mc.thePlayer.posX - player.posX;
      double dz = this.mc.thePlayer.posZ - player.posZ;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      if (horizontal < 1.0E-6) {
         return true;
      } else {
         double yaw = Math.toRadians(player.rotationYawHead);
         double fx = -Math.sin(yaw);
         double fz = Math.cos(yaw);
         double dot = fx * (dx / horizontal) + fz * (dz / horizontal);
         return dot >= Math.cos(Math.toRadians(80.0));
      }
   }

   private boolean isValidTarget(EntityPlayer player, double fov) {
      if (player == null || player == this.mc.thePlayer || player.isDead || player.deathTime != 0 || player.getHealth() <= 0.0F) {
         return false;
      } else if (this.mc.theWorld == null
         || this.mc.theWorld.getEntityByID(player.getEntityId()) != player
         || !this.mc.theWorld.playerEntities.contains(player)) {
         return false;
      } else if (RelationshipService.hostile(player) || this.teammates.isEnabled()) {
         if (this.teammates.isEnabled() ? !RelationshipService.hostile(player) : this.players.isEnabled()) {
            if (!this.invisible.isEnabled() && player.isInvisible()) {
               return false;
            } else if (this.mc.thePlayer.getDistanceSqToEntity(player) > this.range.doubleValue() * this.range.doubleValue()) {
               return false;
            } else {
               return this.mc.thePlayer.canEntityBeSeen(player) && this.getCrosshairAngle(player) <= fov;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private double getCrosshairAngle(EntityPlayer player) {
      double dx = player.posX - this.mc.thePlayer.posX;
      double dz = player.posZ - this.mc.thePlayer.posZ;
      double dy = player.posY + player.getEyeHeight() * 0.8 - (this.mc.thePlayer.posY + this.mc.thePlayer.getEyeHeight());
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizontal)));
      double yawDiff = MathHelper.wrapAngleTo180_float(yaw - this.mc.thePlayer.rotationYaw);
      double pitchDiff = pitch - this.mc.thePlayer.rotationPitch;
      return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
   }

   private boolean canOperate() {
      return this.inWorld() && this.mc.currentScreen == null && this.mc.inGameHasFocus && !this.mc.thePlayer.isDead;
   }

   private boolean inWorld() {
      return this.mc.thePlayer != null && this.mc.theWorld != null;
   }

   private boolean isBowFullyDrawn() {
      ItemStack held = this.mc.thePlayer == null ? null : this.mc.thePlayer.getHeldItem();
      ItemStack using = this.mc.thePlayer == null ? null : this.mc.thePlayer.getItemInUse();
      return held != null
         && held.getItem() instanceof ItemBow
         && using != null
         && using.getItem() instanceof ItemBow
         && this.mc.thePlayer.getItemInUseCount() > 0;
   }

   private boolean isUsingBow() {
      if (this.bow.isEnabled() && this.mc.thePlayer != null && Mouse.isButtonDown(1)) {
         ItemStack held = this.mc.thePlayer.getHeldItem();
         return held != null && held.getItem() instanceof ItemBow;
      } else {
         return false;
      }
   }

   private boolean isHoldingRod() {
      ItemStack held = this.mc.thePlayer == null ? null : this.mc.thePlayer.getHeldItem();
      return held != null && held.getItem() == Items.fishing_rod;
   }

   private boolean isAutomaticRod() {
      return this.rodMode.index() == 1;
   }

   private boolean areAutomaticConditionsMet() {
      if (this.notWhilstHoldingLeft.isEnabled() && Mouse.isButtonDown(0)) {
         return false;
      } else if (this.notWhilstHoldingRight.isEnabled() && Mouse.isButtonDown(1)) {
         return false;
      } else {
         return !this.holdingWeapon.isEnabled() || this.isHoldingWeapon();
      }
   }

   private boolean isHoldingWeapon() {
      ItemStack held = this.mc.thePlayer == null ? null : this.mc.thePlayer.getHeldItem();
      return held != null && (held.getItem() instanceof ItemSword || held.getItem() instanceof ItemBow);
   }

   private int getBowFlightTicks() {
      return Math.max(1, (int)Math.round(this.bowFlight.doubleValue() * 20.0));
   }

   private int findHotbarSlot(Item item) {
      if (this.mc.thePlayer == null) {
         return -1;
      } else {
         for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() == item) {
               return slot;
            }
         }

         return -1;
      }
   }

   private int findSwordHotbarSlot() {
      if (this.mc.thePlayer == null) {
         return -1;
      } else {
         int current = this.mc.thePlayer.inventory.currentItem;
         ItemStack held = this.mc.thePlayer.inventory.getStackInSlot(current);
         if (held != null && held.getItem() instanceof ItemSword) {
            return current;
         } else {
            for (int slot = 0; slot < 9; slot++) {
               ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(slot);
               if (stack != null && stack.getItem() instanceof ItemSword) {
                  return slot;
               }
            }

            return -1;
         }
      }
   }

   private void selectHotbarSlot(int slot) {
      if (this.inWorld() && slot >= 0 && slot <= 8 && slot != this.mc.thePlayer.inventory.currentItem) {
         this.mc.thePlayer.inventory.currentItem = slot;
         syncCurrentItem(this.mc.playerController);
      }
   }

   private void restoreHotbarSlot() {
      if (this.swappedToRod) {
         if (this.inWorld() && this.isHoldingRod()) {
            this.selectHotbarSlot(this.storedSwordSlot);
         }

         this.swappedToRod = false;
         this.storedSwordSlot = -1;
      }
   }

   private double getBowDrawProgress() {
      return ProjectileMotion.OYIzkT(72000 - this.mc.thePlayer.getItemInUseCount());
   }

   private double getLateralLeadCorrection(EntityPlayer player) {
      if (player != null && this.mc.thePlayer != null) {
         double[] velocity = this.motionPredictor.TjaA(player.getEntityId());
         double dx = player.serverPosX / 32.0 - this.mc.thePlayer.posX;
         double dz = player.serverPosZ / 32.0 - this.mc.thePlayer.posZ;
         return 4.0 * squaredSineBetween(dx, dz, velocity[0], velocity[1]);
      } else {
         return 0.0;
      }
   }

   private static double squaredSineBetween(double ax, double az, double bx, double bz) {
      double aLength = Math.sqrt(ax * ax + az * az);
      double bLength = Math.sqrt(bx * bx + bz * bz);
      if (!(aLength < 1.0E-6) && !(bLength < 0.04)) {
         double cross = Math.abs(ax * bz - az * bx);
         double sine = cross / (aLength * bLength);
         return Math.max(0.0, Math.min(1.0, sine * sine));
      } else {
         return 0.0;
      }
   }

   private void publish(float yaw, float pitch) {
      this.publishYaw = yaw;
      this.publishPitch = pitch;
      this.hasRotation = true;
      if (RotationService.claim(ROTATION_PRIORITY) && this.mc.thePlayer != null) {
         this.mc.thePlayer.rotationYaw = yaw;
         this.mc.thePlayer.rotationPitch = pitch;
      }
   }

   private void smoothTowardTargetAngles(float yaw, float pitch) {
      if (!this.smoothingInitialised) {
         float[] last = this.lastSentRotation();
         this.smoothedYaw = last[0];
         this.smoothedPitch = last[1];
         this.smoothingInitialised = true;
      }

      this.smoothedYaw = stepYawToward(this.smoothedYaw, yaw);
      this.smoothedPitch = stepPitchToward(this.smoothedPitch, pitch);
      this.applySmoothedRotation();
   }

   private void applySmoothedRotation() {
      if (this.smoothingInitialised) {
         this.publish(this.smoothedYaw, this.smoothedPitch);
      }
   }

   private void driftTowardPlayerAngles() {
      if (this.smoothingInitialised && this.mc.thePlayer != null) {
         this.smoothedYaw = stepYawToward(this.smoothedYaw, this.mc.thePlayer.rotationYaw);
         this.smoothedPitch = stepPitchToward(this.smoothedPitch, this.mc.thePlayer.rotationPitch);
         this.applySmoothedRotation();
      }
   }

   private static float stepYawToward(float current, float target) {
      float delta = MathHelper.wrapAngleTo180_float(target - current);
      return current + boundedStep(delta, 40.0F);
   }

   private static float stepPitchToward(float current, float target) {
      float delta = target - current;
      return MathHelper.clamp_float(current + boundedStep(delta, 15.0F), -90.0F, 90.0F);
   }

   private static float boundedStep(float delta, float bound) {
      return Math.abs(delta) <= 0.08F ? delta : MathHelper.clamp_float(delta * 0.45F, -bound, bound);
   }

   private float[] lastSentRotation() {
      if (this.mc.thePlayer != null) {
         return new float[]{this.mc.thePlayer.rotationYaw, this.mc.thePlayer.rotationPitch};
      } else {
         return new float[]{0.0F, 0.0F};
      }
   }

   private void clearRotationSmoothing() {
      this.smoothingInitialised = false;
   }

   private void clearAimSolution(boolean clearTarget) {
      this.aimSolution = null;
      this.hitStreak = 0;
      this.pendingAction = PendingAction.NONE;
      if (clearTarget) {
         this.entityPlayer = null;
      }
   }

   private void resetCombatState() {
      this.restoreHotbarSlot();
      this.entityPlayer = null;
      this.aimSolution = null;
      this.pendingAction = PendingAction.NONE;
      this.rodState = RodState.READY;
      this.hitStreak = 0;
      this.cooldownTicks = 0;
      this.missStreak = 0;
      this.rodCastActive = false;
      this.swappedToRod = false;
      this.storedSwordSlot = -1;
      this.hasRotation = false;
      this.clearRotationSmoothing();
      this.resetAimVisualisation();
   }

   private void resetAimVisualisation() {
      this.visualisedEntityId = -1;
      this.lastVisualisationNanos = 0L;
      this.aimIndicatorInitialised = false;
   }

   private void resetModuleState() {
      this.resetCombatState();
      this.motionPredictor.resetAll();
      this.entityPositionTracker.ELFwOy();
      this.live.clear();
      this.dead.clear();
      this.trackedPlayersById.clear();
      this.smoothedLatencyMillis = 0.0;
   }

   private MotionPredictor$1 createCollisionCheck(final EntityPlayer target) {
      return new MotionPredictor$1() {
         @Override
         public boolean isPositionClear(double x, double y, double z) {
            double half = target.width * 0.5;
            AxisAlignedBB box = new AxisAlignedBB(x - half, y, z - half, x + half, y + target.height, z + half);
            return BowAimbotModule.this.mc.theWorld != null && BowAimbotModule.this.mc.theWorld.getCollidingBoundingBoxes(target, box).isEmpty();
         }
      };
   }

   /** Called from RenderHooks at the RenderWorldLast point (mirrors AutoClutchModule.renderWorld). */
   public static void renderWorld(float partialTicks) {
      if (instance != null && instance.isEnabled()) {
         instance.renderVisualisation(partialTicks);
      }
   }

   private void renderVisualisation(float partialTicks) {
      if (!this.visualise.isEnabled() || this.entityPlayer == null || !this.inWorld()) {
         return;
      }

      TargetPrediction prediction = this.aimSolution == null ? null : this.aimSolution.getPrediction();
      if (prediction == null) {
         prediction = this.motionPredictor
            .ygp3(this.entityPlayer.getEntityId(), this.smoothedLatencyMillis / 100.0 + 10.0, this.createCollisionCheck(this.entityPlayer));
      }

      if (prediction == null) {
         return;
      }

      double[] point = this.predictedRenderPoint(prediction);
      long now = System.nanoTime();
      double delta = this.lastVisualisationNanos == 0L ? 1.0 : Math.min(0.1, Math.max(0.0, (now - this.lastVisualisationNanos) / 1.0E9));
      double lerp = 1.0 - Math.exp(-delta * 14.0);
      double drift = square(this.smoothedEntityX - point[0]) + square(this.smoothedEntityY - point[1]) + square(this.smoothedEntityZ - point[2]);
      if (this.visualisedEntityId == this.entityPlayer.getEntityId() && this.lastVisualisationNanos != 0L && !(drift > 36.0)) {
         this.smoothedEntityX = this.smoothedEntityX + (point[0] - this.smoothedEntityX) * lerp;
         this.smoothedEntityY = this.smoothedEntityY + (point[1] - this.smoothedEntityY) * lerp;
         this.smoothedEntityZ = this.smoothedEntityZ + (point[2] - this.smoothedEntityZ) * lerp;
      } else {
         this.visualisedEntityId = this.entityPlayer.getEntityId();
         this.smoothedEntityX = point[0];
         this.smoothedEntityY = point[1];
         this.smoothedEntityZ = point[2];
         this.aimIndicatorInitialised = false;
      }

      this.lastVisualisationNanos = now;
      float red = 0.0F;
      float green = 0.85F;
      float blue = 1.0F;
      double viewX = this.mc.getRenderManager().viewerPosX;
      double viewY = this.mc.getRenderManager().viewerPosY;
      double viewZ = this.mc.getRenderManager().viewerPosZ;
      double half = this.entityPlayer.width * 0.5;
      AxisAlignedBB box = new AxisAlignedBB(
         this.smoothedEntityX - half,
         this.smoothedEntityY,
         this.smoothedEntityZ - half,
         this.smoothedEntityX + half,
         this.smoothedEntityY + this.entityPlayer.height,
         this.smoothedEntityZ + half
      );
      GL11.glPushAttrib(1048575);
      GL11.glPushMatrix();

      try {
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glDepthMask(false);
         GL11.glLineWidth(2.0F);
         drawBox(box.offset(-viewX, -viewY, -viewZ), red, green, blue, 0.25F);
         Vec3 aimVec = this.aimSolution == null ? null : this.aimSolution.kahN();
         if (aimVec != null) {
            double clampX = Math.max(this.smoothedEntityX - half, Math.min(this.smoothedEntityX + half, aimVec.xCoord));
            double clampY = Math.max(this.smoothedEntityY, Math.min(this.smoothedEntityY + this.entityPlayer.height, aimVec.yCoord));
            double clampZ = Math.max(this.smoothedEntityZ - half, Math.min(this.smoothedEntityZ + half, aimVec.zCoord));
            if (!this.aimIndicatorInitialised) {
               this.smoothedAimX = clampX;
               this.smoothedAimY = clampY;
               this.smoothedAimZ = clampZ;
               this.aimIndicatorInitialised = true;
            } else {
               this.smoothedAimX = this.smoothedAimX + (clampX - this.smoothedAimX) * lerp;
               this.smoothedAimY = this.smoothedAimY + (clampY - this.smoothedAimY) * lerp;
               this.smoothedAimZ = this.smoothedAimZ + (clampZ - this.smoothedAimZ) * lerp;
            }

            double marker = 0.14;
            AxisAlignedBB markerBox = new AxisAlignedBB(
               this.smoothedAimX - marker,
               this.smoothedAimY - marker,
               this.smoothedAimZ - marker,
               this.smoothedAimX + marker,
               this.smoothedAimY + marker,
               this.smoothedAimZ + marker
            );
            drawBox(markerBox.offset(-viewX, -viewY, -viewZ), red, green, blue, 0.5F);
         }
      } finally {
         GL11.glDepthMask(true);
         GL11.glEnable(2929);
         GL11.glDisable(3042);
         GL11.glEnable(3553);
         GL11.glPopMatrix();
         GL11.glPopAttrib();
      }
   }

   private static void drawBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
      GL11.glColor4f(red, green, blue, alpha);
      GL11.glBegin(GL11.GL_QUADS);
      GL11.glVertex3d(box.minX, box.minY, box.minZ);
      GL11.glVertex3d(box.maxX, box.minY, box.minZ);
      GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
      GL11.glVertex3d(box.minX, box.minY, box.maxZ);
      GL11.glVertex3d(box.minX, box.maxY, box.minZ);
      GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
      GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
      GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
      GL11.glVertex3d(box.minX, box.minY, box.minZ);
      GL11.glVertex3d(box.minX, box.maxY, box.minZ);
      GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
      GL11.glVertex3d(box.maxX, box.minY, box.minZ);
      GL11.glVertex3d(box.minX, box.minY, box.maxZ);
      GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
      GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
      GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
      GL11.glEnd();
   }

   private double[] predictedRenderPoint(TargetPrediction prediction) {
      double sx = this.entityPlayer.serverPosX / 32.0;
      double sy = this.entityPlayer.serverPosY / 32.0;
      double sz = this.entityPlayer.serverPosZ / 32.0;
      double lead = this.smoothedLatencyMillis / 100.0 + (this.aimSolution == null ? 10.0 : this.aimSolution.getTicks());
      double clampRadius = Math.max(2.0, Math.min(8.0, 1.25 + lead * 0.38));
      double dx = prediction.RAYFZ() - sx;
      double dz = prediction.getZ() - sz;
      double distance = Math.sqrt(dx * dx + dz * dz);
      if (distance > clampRadius && distance > 1.0E-6) {
         dx *= clampRadius / distance;
         dz *= clampRadius / distance;
      }

      PotionEffect jump = this.entityPlayer.getActivePotionEffect(Potion.jump);
      double ceiling = 1.5 + (jump == null ? 0.0 : (jump.getAmplifier() + 1) * 0.75);
      double y = Math.max(sy - 1.5, Math.min(sy + ceiling, prediction.fmmQ()));
      return new double[]{sx + dx, y, sz + dz};
   }

   private static double square(double value) {
      return value * value;
   }

   private static void syncCurrentItem(PlayerControllerMP controller) {
      if (SYNC_CURRENT_ITEM != null) {
         try {
            SYNC_CURRENT_ITEM.invoke(controller);
         } catch (ReflectiveOperationException ignored) {
         }
      }
   }
}
