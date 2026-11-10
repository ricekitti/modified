package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.RecoveredMath;
import dev.rice.client.KeyState;
import dev.rice.client.SemanticEvent;
import dev.rice.client.action.ActionAccess;
import dev.rice.client.action.ActionConstants;
import dev.rice.client.numeric.DoubleNumeric;
import dev.rice.client.numeric.NumericServices;
import dev.rice.client.placement.PlacementFrame;
import dev.rice.client.placement.PlacementGeometry;
import dev.rice.client.placement.PlacementSelection;
import dev.rice.client.rotation.RecoveredRandom;
import dev.rice.client.rotation.RotationMath;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Mouse;

/**
 * "Clutch" — the original Slinky fall-clutch scaffold-aim module, restored from OpenSlinky
 * ({@code dev.slinky.module.world.ClutchModule}) and ported onto Rice. The recovered search/face/update
 * placement-aim and snapback behaviour is reproduced byte-for-byte against the OpenSlinky source; only the
 * Rice-side names differ ({@link RecoveredRandom}/{@link RecoveredMath}, {@code dev.rice} packages).
 *
 * <p>Carried as a self-contained {@link Module} (not a catalog EvidenceModule): the 18 original config keys and
 * their observed defaults/ranges are declared here directly so old {@code clutch} profiles load unchanged, while its
 * ClickGUI section layout (Main/Aim/Snap-back/Conditions) is the OG one in {@code ricesemantic/ModuleUi.json}. Category
 * WORLD (the block-building tab), description as upstream Slinky. SemanticEvents:
 * 0 placement-tick, 18 pre-aim brain, 13 movement remap, 26 world/shutdown cleanup.
 */
public final strictfp class ClutchModule extends Module {
   private final ClutchModule.State state = new ClutchModule.State();
   private final ModuleManager modules;
   private int cooldownTicks;
   private int prevHurtTime;
   private float lastFallDistance;
   private boolean lastOnGround;
   private boolean hitTriggered;
   private long lastHitTime;
   private int groundTicksAfterHit;
   private boolean fbed;
   private int fireballIgnoreTicks;
   private boolean groundDelayActive;
   private int groundDelayTicks;
   private double prevMotionX;
   private double prevMotionZ;

   public ClutchModule(ModuleManager modules) {
      super("clutch", "Clutch", "Clutches for you when falling.", ModuleCategory.WORLD);
      this.modules = modules;
      this.add(new NumberSetting("range", "Range", 7.0, 1.0, 7.0, 0.0));
      this.add(new NumberSetting("max_aim_dist", "FOV", 360.0, 50.0, 360.0, 0.0));
      this.add(new NumberSetting("min_height", "Minimum height", 4.0, 1.0, 10.0, 0.0));
      this.add(new NumberSetting("click_speed", "Click speed", 18.0, 10.0, 20.0, 0.0));
      this.add(new NumberSetting("randomization", "Randomization", 0.5, 0.0, 1.0, 0.0));
      this.add(new ChoiceSetting("select_blocks", "Select blocks", 2, "No", "On depletion", "Always"));
      this.add(new BooleanSetting("sideways_only", "Only place sideways", false));
      this.add(new NumberSetting("aim_speed", "Base speed", 30.0, 20.0, 180.0, 0.0));
      this.add(new NumberSetting("aim_mult", "Acceleration", 0.5, 0.3, 1.0, 0.0));
      this.add(new NumberSetting("aim_mult_strength", "Acceleration strength", 0.3, 0.0, 1.0, 0.0));
      this.add(new BooleanSetting("aim_multipoint", "Multipoint", false));
      this.add(new NumberSetting("snapback_delay", "Delay", 6.0, 1.0, 10.0, 0.0));
      this.add(new NumberSetting("snapback_duration", "Duration", 4.0, 1.0, 10.0, 0.0));
      this.add(new BooleanSetting("snapback_instant_on_jump", "Keep jump direction", false));
      this.add(new BooleanSetting("snapback_disable_after", "Disable afterwards", false));
      this.add(new BooleanSetting("require_air", "In air", true));
      this.add(new BooleanSetting("require_damage", "Damaged", false));
      this.add(new BooleanSetting("require_backwards", "Moving backwards", false));
      this.add(new ChoiceSetting("toggle_on", "Toggle On", 0, "Void", "Void Hit", "Void Hit (Ignore FB)"));
      this.add(new ChoiceSetting("toggle_off", "Toggle Off", 2, "None", "Movement", "Forward", "Ground", "Ground (Delay)", "Ground + Forward"));
      this.add(new BooleanSetting("recovery_check", "Recovery Check", true));
      this.add(new NumberSetting("clutch_cooldown", "Clutch Cooldown", 0.0, 0.0, 60.0, 1.0));
      this.add(new ChoiceSetting("max_fall", "Max Fall", 4, maxFallOptions()));
   }

   private strictfp boolean flag(String key) {
      return ((BooleanSetting)this.findSetting(key)).isEnabled();
   }

   private strictfp float number(String key) {
      return ((NumberSetting)this.findSetting(key)).floatValue();
   }

   private strictfp int choice(String key) {
      return ((ChoiceSetting)this.findSetting(key)).index();
   }

   private strictfp ClutchModule.Settings settings() {
      ClutchModule.Settings s = new ClutchModule.Settings();
      s.range = this.number("range");
      s.fov = this.number("max_aim_dist");
      s.height = this.number("min_height");
      s.clickSpeed = this.number("click_speed");
      s.randomization = this.number("randomization");
      s.aimSpeed = this.number("aim_speed");
      s.aimMult = this.number("aim_mult");
      s.aimStrength = this.number("aim_mult_strength");
      s.snapDelay = this.number("snapback_delay");
      s.snapDuration = this.number("snapback_duration");
      s.select = this.choice("select_blocks");
      s.sideways = this.flag("sideways_only");
      s.multipoint = this.flag("aim_multipoint");
      s.instantJump = this.flag("snapback_instant_on_jump");
      s.disableAfter = this.flag("snapback_disable_after");
      s.air = this.flag("require_air");
      s.damage = this.flag("require_damage");
      s.backwards = this.flag("require_backwards");
      return s;
   }

   @Override
   public strictfp boolean listens(int type) {
      return type == 0 || type == 13 || type == 18 || type == 26;
   }

   @Override
   public strictfp void onEvent(ClientContext c, SemanticEvent e) {
      PlacementFrame i = new PlacementFrame.Game(c, this.modules, this);
      if (e.type == 26) {
         this.cleanup();
      } else {
         if (e.type == 0) {
            this.state.placementTick(i, this.number("click_speed"));
         } else if (e.type == 18) {
            ClutchModule.Settings s = this.settings();
            this.evaluateToggles(c, s);
            this.state.update(i, s, RecoveredRandom.SHARED, RecoveredRandom.now());
         } else if (this.state.correctMovement) {
            e.active = true;
            e.remapMovement = true;
         }
      }
   }

   private strictfp void cleanup() {
      this.state.clear();
      PlacementSelection.release(this);
      this.resetToggleState();
   }

   @Override
   protected strictfp void onDisable(ClientContext c) {
      this.cleanup();
   }

   @Override
   public strictfp void onWorldUnload(ClientContext c) {
      this.cleanup();
   }

   private void resetToggleState() {
      this.cooldownTicks = 0;
      this.prevHurtTime = 0;
      this.lastFallDistance = 0.0F;
      this.lastOnGround = false;
      this.hitTriggered = false;
      this.lastHitTime = 0L;
      this.groundTicksAfterHit = 0;
      this.fbed = false;
      this.fireballIgnoreTicks = 0;
      this.groundDelayActive = false;
      this.groundDelayTicks = 0;
      this.prevMotionX = 0.0;
      this.prevMotionZ = 0.0;
   }

   /**
    * AutoClutch's Toggle On (arm) / Toggle Off (disarm) conditions, ported from
    * {@code dev.rice.client.clutch.AutoClutchBackend} onto the live Clutch. Writes {@code armed}/{@code forceOff}
    * onto the per-tick {@link Settings}; the recovered placement/aim {@link State} machine reads them at its
    * engage gate (and is otherwise untouched). Uses live player/world state so no stale damage events are read.
    */
   private void evaluateToggles(ClientContext c, ClutchModule.Settings s) {
      s.armed = false;
      s.forceOff = false;
      EntityPlayerSP p = c.player();
      if (p == null || c.world() == null) {
         return;
      }

      int toggleOn = this.choice("toggle_on");
      int toggleOff = this.choice("toggle_off");
      boolean recoveryCheck = this.flag("recovery_check");
      int cooldownMax = (int) this.number("clutch_cooldown");
      int maxFall = this.choice("max_fall");

      boolean onGround = p.onGround;
      boolean flying = p.capabilities != null && p.capabilities.isFlying;
      boolean forwardDown = KeyState.physical(c.minecraft().gameSettings.keyBindForward);
      boolean jumpDown = KeyState.physical(c.minecraft().gameSettings.keyBindJump);
      long now = System.currentTimeMillis();

      if (this.cooldownTicks > 0) {
         this.cooldownTicks--;
      }

      // Hit detection: combat damage only. A landing fall-hit (onGround and fell >= 3 blocks) is excluded so
      // "Void Hit" never arms off a routine landing.
      int hurt = p.hurtTime;
      float fallDist = p.fallDistance;
      if (hurt > this.prevHurtTime && hurt >= 8) {
         boolean landingDamage = (onGround || this.lastOnGround) && (this.lastFallDistance >= 3.0F || fallDist >= 3.0F);
         if (!landingDamage && !this.fbed) {
            this.hitTriggered = true;
            this.lastHitTime = now;
         }
      }
      this.prevHurtTime = hurt;
      this.lastFallDistance = fallDist;
      this.lastOnGround = onGround;
      if (onGround) {
         this.groundTicksAfterHit++;
         if (this.groundTicksAfterHit > 12) {
            this.hitTriggered = false;
         }
      } else {
         this.groundTicksAfterHit = 0;
      }

      // Fireball-ignore window (Toggle On = "Void Hit (Ignore FB)"): a held fire charge + right-click, or a
      // nearby fireball entity, opens a 20-tick window where hits do NOT arm, so intentional fireball jumps are
      // ignored while legitimate non-fireball hits still arm once the window lapses.
      if (this.fireballIgnoreTicks > 0) {
         this.fireballIgnoreTicks--;
         if (this.fireballIgnoreTicks == 0) {
            this.fbed = false;
         }
      }
      if (toggleOn == 2) {
         boolean fb = isFireballItem(p.getHeldItem()) && Mouse.isButtonDown(1);
         if (!fb) {
            for (Object o : c.world().loadedEntityList) {
               if (o instanceof EntityFireball) {
                  Entity ent = (Entity) o;
                  double dx = ent.posX - p.posX;
                  double dy = ent.posY - p.posY;
                  double dz = ent.posZ - p.posZ;
                  if (dx * dx + dy * dy + dz * dz < 64.0) {
                     fb = true;
                     break;
                  }
               }
            }
         }
         if (fb) {
            this.fbed = true;
            this.fireballIgnoreTicks = 20;
         }
         if (onGround) {
            this.fbed = false;
            this.fireballIgnoreTicks = 0;
         }
      } else {
         this.fbed = false;
         this.fireballIgnoreTicks = 0;
      }

      boolean hitActive;
      if (toggleOn == 0) {
         hitActive = true;
      } else if (toggleOn == 2 && this.fbed) {
         hitActive = false;
      } else {
         hitActive = this.hitTriggered && now - this.lastHitTime < 3500L;
      }

      int fall = fallBlocks(c);
      boolean overVoid = fall == -1 || (maxFall > 0 && fall > maxFall);
      boolean recoverable = recoveryCheck && this.canRecoverToPlatform(c);
      boolean baseArm = !onGround && !flying && overVoid && !hasSolidBelow(c) && this.cooldownTicks <= 0 && !recoverable;
      s.armed = baseArm && hitActive;

      switch (toggleOff) {
         case 1:
            if (onGround && isMoving(p)) {
               s.forceOff = true;
            }
            break;
         case 2:
            if (onGround && forwardDown) {
               s.forceOff = true;
            }
            break;
         case 3:
            if (onGround) {
               s.forceOff = true;
            }
            break;
         case 4:
            if (onGround) {
               if (!this.groundDelayActive) {
                  this.groundDelayTicks = 20;
                  this.groundDelayActive = true;
               }
            } else if (!jumpDown) {
               this.groundDelayActive = false;
            }
            if (this.groundDelayActive && this.groundDelayTicks > 0) {
               this.groundDelayTicks--;
            }
            if (this.groundDelayActive && this.groundDelayTicks == 0) {
               this.groundDelayActive = false;
               s.forceOff = true;
            }
            break;
         case 5:
            if (onGround && forwardDown) {
               s.forceOff = true;
            }
            break;
         default:
            break;
      }

      if (s.forceOff) {
         s.armed = false;
         if (cooldownMax > 0) {
            this.cooldownTicks = cooldownMax;
         }
      }

      this.prevMotionX = p.motionX;
      this.prevMotionZ = p.motionZ;
   }

   private static boolean isMoving(EntityPlayerSP p) {
      if (p.movementInput != null && (p.movementInput.moveForward != 0.0F || p.movementInput.moveStrafe != 0.0F)) {
         return true;
      }
      return Math.hypot(p.motionX, p.motionZ) > 0.05;
   }

   private static boolean isFireballItem(ItemStack item) {
      return item != null && item.getItem() instanceof ItemFireball;
   }

   /** AutoClutch's "Max Fall" option labels: index 0 = "Void only", index N = fall of more than N blocks. */
   private static String[] maxFallOptions() {
      String[] options = new String[51];
      options[0] = "Void only";
      options[1] = "1 block";
      for (int i = 2; i <= 50; i++) {
         options[i] = i + " blocks";
      }
      return options;
   }

   private static boolean solid(ClientContext c, int x, int y, int z) {
      Block b = c.world().getBlockState(new BlockPos(x, y, z)).getBlock();
      if (b == null || b == Blocks.air || b == Blocks.fire || b instanceof BlockLiquid) {
         return false;
      }
      return !b.getMaterial().isReplaceable();
   }

   private static int fallBlocks(ClientContext c) {
      EntityPlayerSP p = c.player();
      int px = (int) Math.floor(p.posX);
      int pz = (int) Math.floor(p.posZ);
      int y = (int) Math.floor(p.posY) - 1;
      for (int i = y; i >= 0; i--) {
         if (solid(c, px, i, pz)) {
            return y - i;
         }
      }
      return -1;
   }

   private static boolean hasSolidBelow(ClientContext c) {
      EntityPlayerSP p = c.player();
      int curY = (int) Math.floor(p.posY);
      double[] ox = {0.0, -0.29, 0.29, -0.29, 0.29};
      double[] oz = {0.0, -0.29, -0.29, 0.29, 0.29};
      for (int k = 0; k < ox.length; k++) {
         int px = (int) Math.floor(p.posX + ox[k]);
         int pz = (int) Math.floor(p.posZ + oz[k]);
         for (int dy = 1; dy <= 3; dy++) {
            if (solid(c, px, curY - dy, pz)) {
               return true;
            }
         }
      }
      return false;
   }

   private boolean canRecoverToPlatform(ClientContext c) {
      EntityPlayerSP p = c.player();
      if (p.onGround) {
         return true;
      }
      double mx = p.motionX;
      double mz = p.motionZ;
      double horiz = Math.hypot(mx, mz);
      boolean forwardDown = KeyState.physical(c.minecraft().gameSettings.keyBindForward);
      if (horiz <= 0.05 && !forwardDown) {
         return false;
      }
      double dirX;
      double dirZ;
      if (horiz > 0.04) {
         dirX = mx / horiz;
         dirZ = mz / horiz;
      } else {
         float yawRad = (float) Math.toRadians(p.rotationYaw);
         dirX = -Math.sin(yawRad);
         dirZ = Math.cos(yawRad);
      }
      int footY = (int) Math.floor(p.posY);
      int maxFall = this.choice("max_fall");
      for (double dist = 0.6; dist <= 5.0; dist += 0.5) {
         int cx = (int) Math.floor(p.posX + dirX * dist);
         int cz = (int) Math.floor(p.posZ + dirZ * dist);
         double toCx = (cx + 0.5) - p.posX;
         double toCz = (cz + 0.5) - p.posZ;
         if (toCx * dirX + toCz * dirZ <= 0.0) {
            continue;
         }
         for (int dy = 1; dy >= -4; dy--) {
            int cy = footY + dy;
            if (cy < 0 || cy > 255) {
               continue;
            }
            if (solid(c, cx, cy, cz) && !solid(c, cx, cy + 1, cz) && !solid(c, cx, cy + 2, cz)) {
               double landingTopY = cy + 1.0;
               double drop = p.posY - landingTopY;
               if (maxFall > 0 && drop > maxFall) {
                  continue;
               }
               double actualHoriz = Math.hypot(dirX * dist, dirZ * dist);
               double reach = this.computeReachAtY(c, p.posY, landingTopY);
               if (reach > 0.05 && actualHoriz <= reach + 0.45 && mx * dirX + mz * dirZ > 0.02) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   private double computeReachAtY(ClientContext c, double startY, double landingY) {
      EntityPlayerSP p = c.player();
      double velY = p.motionY;
      double velX = p.motionX;
      double velZ = p.motionZ;
      double simY = startY;
      double totalX = 0.0;
      double totalZ = 0.0;
      double accelX = 0.0;
      double accelZ = 0.0;
      if (this.prevMotionX != 0.0 || this.prevMotionZ != 0.0) {
         accelX = velX / 0.91 - this.prevMotionX;
         accelZ = velZ / 0.91 - this.prevMotionZ;
      }
      boolean reachedAbove = startY >= landingY;
      for (int tick = 0; tick < 80; tick++) {
         velX += accelX;
         velZ += accelZ;
         simY += velY;
         totalX += velX;
         totalZ += velZ;
         velY = (velY - 0.08) * 0.98;
         velX *= 0.91;
         velZ *= 0.91;
         if (simY >= landingY) {
            reachedAbove = true;
         }
         if (reachedAbove && simY <= landingY) {
            return Math.sqrt(totalX * totalX + totalZ * totalZ);
         }
      }
      return 0.0;
   }

   public static final strictfp class Face {
      public final Vec3 point;
      public final float score;

      Face(Vec3 point, float score) {
         this.point = point;
         this.score = score;
      }
   }

   public static final class Settings {
      public float range;
      public float fov;
      public float height;
      public float clickSpeed;
      public float randomization;
      public float aimSpeed;
      public float aimMult;
      public float aimStrength;
      public float snapDelay;
      public float snapDuration;
      public int select;
      public boolean sideways;
      public boolean multipoint;
      public boolean instantJump;
      public boolean disableAfter;
      public boolean air;
      public boolean damage;
      public boolean backwards;
      public boolean armed;
      public boolean forceOff;
   }

   public static final class State {
      public float clickTimer;
      public int x;
      public int y;
      public int z;
      public int unhurt;
      public int phase;
      public int snapDelay;
      public int snapRemaining;
      public boolean placement;
      public boolean centered;
      public boolean correctMovement;
      public boolean disableLatched;
      public Vec3 selected = new Vec3(0.0, 0.0, 0.0);
      public double jumpAccumulator;
      private final RecoveredRandom.Smooth inward = new RecoveredRandom.Smooth();
      private final RecoveredRandom.Smooth jitterX = new RecoveredRandom.Smooth();
      private final RecoveredRandom.Smooth jitterZ = new RecoveredRandom.Smooth();
      private final RecoveredRandom.Smooth holeX = new RecoveredRandom.Smooth();
      private final RecoveredRandom.Smooth holeZ = new RecoveredRandom.Smooth();
      private final RecoveredRandom.Smooth pitchNoise = new RecoveredRandom.Smooth();
      private final RecoveredRandom.Segment speedX = new RecoveredRandom.Segment();
      private final RecoveredRandom.Segment speedZ = new RecoveredRandom.Segment();

      private static strictfp float f(int a) {
         return ActionConstants.f(a);
      }

      private static strictfp double d(int a) {
         return ActionConstants.d(a);
      }

      private static strictfp float smooth(RecoveredRandom.Smooth s, float low, float high, double period, RecoveredRandom r, double now) {
         return s.sample(low, high, period, now, r);
      }

      public static strictfp boolean scoreRejected(float score, float best, long maximum, long distance) {
         return score > best && maximum <= distance;
      }

      public strictfp ClutchModule.Face face(
         Vec3 eye,
         float yaw,
         float pitch,
         Vec3 position,
         int bx,
         int by,
         int bz,
         int dx,
         int dy,
         int dz,
         ClutchModule.Settings s,
         RecoveredRandom r,
         double now
      ) {
         float inner = smooth(this.inward, 0.0F, f(1401200), d(1401208), r, now);
         float jx = smooth(this.jitterX, f(1401216), f(1401220), d(1401224), r, now);
         float jz = smooth(this.jitterZ, f(1401216), f(1401220), d(1401224), r, now);
         double[] jitter = new double[]{(double)(jz * s.randomization), (double)(jx * s.randomization)};
         if (!s.multipoint) {
            double[] remainder = new double[]{DoubleNumeric.remainder(position.xCoord, d(1401208)), DoubleNumeric.remainder(position.zCoord, d(1401208))};
            double[] observed = new double[]{position.zCoord, position.xCoord};
            double[] previous = new double[]{this.selected.zCoord, this.selected.xCoord};

            for (int lane = 0; lane < 2; lane++) {
               long mask = ActionConstants.longAt(1401232 + lane * 8);
               double value = Double.longBitsToDouble(Double.doubleToRawLongBits(remainder[lane]) & mask);
               boolean beyond = d(1401248 + lane * 8) < value;
               double adjustment = d(1401264 + lane * 8);
               double changed = value + adjustment;
               double folded = beyond ? Double.longBitsToDouble(Double.doubleToRawLongBits(changed) & mask) : value;
               double weight = previous[lane] < observed[lane] ? adjustment : d(1401280 + lane * 8);
               jitter[lane] += folded * weight;
            }
         }

         int ax = PlacementGeometry.magnitude(dx);
         int az = PlacementGeometry.magnitude(dz);
         double offset = (double)(s.randomization * inner) + d(1401296);
         double inverse = d(1401208) - offset;
         double xLow = offset;
         double xHigh = inverse;
         double zLow = (double)(dz >>> 31);
         double zHigh = zLow;
         if (Integer.compareUnsigned(az, ax) < 0) {
            xLow = (double)(dx >>> 31);
            xHigh = xLow;
            zLow = offset;
            zHigh = inverse;
         }

         int blockX = dx + bx;
         int blockY = dy + by;
         int blockZ = dz + bz;
         float pointX = RotationMath.clamp((float)(xLow + (double)blockX), (float)(xHigh + (double)blockX), (float)(jitter[1] + position.xCoord));
         double half = d(1401304);
         double baseY = (double)blockY + half;
         int magnitude = PlacementGeometry.magnitude(dx) + PlacementGeometry.magnitude(dz);
         float pointY = RotationMath.clamp(0.0F, f(1401312), (float)((double)magnitude * half));
         float pointZ = RotationMath.clamp((float)(zLow + (double)blockZ), (float)(zHigh + (double)blockZ), (float)(jitter[0] + position.zCoord));
         Vec3 point = new Vec3((double)pointX, (double)pointY + baseY, (double)pointZ);
         float[] desired = RotationMath.angles(eye, point);
         float yawValue = Float.intBitsToFloat(Float.floatToRawIntBits(RotationMath.wrap(desired[0] - yaw)) & ActionConstants.intAt(1401328));
         float pitchValue = Float.intBitsToFloat(Float.floatToRawIntBits(desired[1] - pitch) & 2147483647);
         return new ClutchModule.Face(point, yawValue + pitchValue);
      }

      public strictfp void placementTick(PlacementFrame i, float cps) {
         if (this.clickTimer > 0.0F) {
            this.clickTimer = this.clickTimer + f(1400944);
            if (this.clickTimer > 0.0F) {
               return;
            }
         }

         this.clickTimer = f(1400948) / cps + this.clickTimer;
         if (i.ready() && this.placement) {
            MovingObjectPosition hit = i.hit();
            if (hit != null && hit.typeOfHit == MovingObjectType.BLOCK) {
               BlockPos block = hit.getBlockPos();
               if (block.getX() == this.x && block.getY() == this.y && block.getZ() == this.z && this.centered != (hit.sideHit.ordinal() != 1)) {
                  i.press();
                  Vec3 adjacent = ActionAccess.adjacentPosition(hit);
                  this.x = RecoveredMath.truncateInt(adjacent.xCoord);
                  this.y = RecoveredMath.truncateInt(adjacent.yCoord);
                  this.z = RecoveredMath.truncateInt(adjacent.zCoord);
               }
            }
         }
      }

      private strictfp boolean search(
         PlacementFrame i, ClutchModule.Settings s, Vec3 eye, AxisAlignedBB b, float yaw, float pitch, RecoveredRandom r, double now
      ) {
         this.centered = false;
         double gx = (b.minX + b.maxX) * d(1400984);
         double gy = b.minY + d(1400992);
         double gz = (b.minZ + b.maxZ) * d(1400984);
         int ix = RecoveredMath.truncateInt(DoubleNumeric.floor(gx));
         int iy = RecoveredMath.truncateInt(DoubleNumeric.floor(gy));
         int iz = RecoveredMath.truncateInt(DoubleNumeric.floor(gz));
         if (s.height > 0.0F) {
            int step = 1;
            int queryY = iy;

            int compared;
            do {
               if (i.block(ix, queryY, iz) != 0) {
                  return false;
               }

               compared = step++;
               queryY--;
            } while (s.height > (float)compared);
         }

         Vec3 position = new Vec3(gx, gy, gz);
         Vec3 bestTarget = eye;
         int bestX = ix;
         int bestY = iy;
         int bestZ = iz;
         float bestScore = f(1401000);
         long maxDistance = -3L;
         int count = ActionConstants.shortAt(1401472 + 2 * RecoveredMath.truncateInt(s.range));
         if ((double)this.y <= gy && i.block(this.x, this.y, this.z) != 0) {
            ClutchModule.Face retained = this.face(eye, yaw, pitch, position, ix, iy, iz, this.x - ix, this.y - iy, this.z - iz, s, r, now);
            if (!(retained.score > s.fov) && PlacementGeometry.squared(eye, retained.point) < d(1401008)) {
               MovingObjectPosition hit = i.ray(eye, retained.point);
               if (hit != null
                  && hit.getBlockPos() != null
                  && hit.getBlockPos().getX() == this.x
                  && hit.getBlockPos().getY() == this.y
                  && hit.getBlockPos().getZ() == this.z) {
                  bestTarget = retained.point;
                  bestX = this.x;
                  bestY = this.y;
                  bestZ = this.z;
                  bestScore = retained.score;
               }
            }
         }

         if (bestScore != f(1401000) || Float.isNaN(bestScore)) {
            int sum = (int)PlacementGeometry.absdiff(bestX, ix) + (int)PlacementGeometry.absdiff(bestY, iy) + (int)PlacementGeometry.absdiff(bestZ, iz);
            maxDistance = Integer.toUnsignedLong(sum) - 2L;
         }

         for (int k = 0; k < count; k++) {
            int code = ActionConstants.shortAt(1401488 + 2 * k);
            int dx = (code & 15) - 8;
            int dy = (code >>> 4 & 15) - 8;
            int dz = (code >>> 8 & 15) - 8;
            int cx = ix + dx;
            int cy = iy + dy;
            int cz = iz + dz;
            if (i.block(cx, cy, cz) != 0) {
               if (dx == 0 && dz == 0) {
                  if (s.sideways) {
                     return false;
                  }

                  float j = smooth(this.inward, 0.0F, f(1401016), d(1401024), r, now);
                  double inset = d(1401032) + (double)(j * s.randomization);
                  float sy = smooth(this.holeX, f(1401040), f(1401044), d(1401024), r, now);
                  float sz = smooth(this.holeZ, f(1401040), f(1401044), d(1401024), r, now);
                  float px = RotationMath.clamp((float)((double)cx + inset), (float)((double)(cx + 1) - inset), (float)(gx + (double)(sy * s.randomization)));
                  float pz = RotationMath.clamp((float)((double)cz + inset), (float)((double)(cz + 1) - inset), (float)(gz + (double)(sz * s.randomization)));
                  this.selected = new Vec3((double)px, d(1401024), (double)pz);
                  this.x = cx;
                  this.y = cy;
                  this.z = cz;
                  this.centered = true;
                  return true;
               }

               long distance = (long)Math.abs(dx) + (long)Math.abs(dy) + (long)Math.abs(dz);
               count = ActionConstants.shortAt(1401472 + 2 * (int)distance);
               ClutchModule.Face candidate = this.face(eye, yaw, pitch, position, ix, iy, iz, dx, dy, dz, s, r, now);
               if (!scoreRejected(candidate.score, bestScore, maxDistance, distance) && PlacementGeometry.squared(eye, candidate.point) < d(1401008)) {
                  bestTarget = candidate.point;
                  bestX = cx;
                  bestY = cy;
                  bestZ = cz;
                  bestScore = candidate.score;
               }
            }
         }

         if (bestScore > s.fov) {
            return false;
         } else {
            this.selected = bestTarget;
            this.x = bestX;
            this.y = bestY;
            this.z = bestZ;
            return true;
         }
      }

      public strictfp void update(PlacementFrame i, ClutchModule.Settings s, RecoveredRandom r, double now) {
         this.placement = this.correctMovement = false;
         if (i.ready()) {
            boolean ground = i.ground();
            this.unhurt = i.hurt() == 0 ? this.unhurt + 1 : 0;
            boolean phaseActive = this.phase > 0;
            if (phaseActive) {
               this.phase--;
            }

            if (phaseActive && ground && i.rotationAvailable() && s.instantJump && i.jump()) {
               float yaw = i.yaw() + i.offsetYaw();
               float pitch = i.pitch() + i.offsetPitch();
               double multiplier = this.jumpAccumulator < 0.0 ? d(1401184) : d(1401176);
               float a = (float)((double)r.interval(f(1401192), f(1401196)) * multiplier);
               float b = (float)((double)r.interval(f(1401192), f(1401196)) * multiplier);
               this.jumpAccumulator += (double)a;
               float outYaw = a * s.randomization + i.storedYaw() - yaw;
               float outPitch = b * s.randomization + i.storedPitch() - pitch;
               i.publish(RotationMath.wrap(outYaw), outPitch);
               this.correctMovement = true;
               this.snapDelay = RecoveredMath.truncateInt(s.snapDuration);
            } else {
               AxisAlignedBB box = i.box();
               Vec3 eye = i.eye();
               float yaw = i.yaw() + i.offsetYaw();
               float pitch = i.pitch() + i.offsetPitch();
               boolean doSearch = (!s.air || !ground) && (this.unhurt < 11 || !s.damage) && !i.mounted() && s.armed && !s.forceOff;
               if (doSearch && s.backwards) {
                  double[] velocity = i.velocity();
                  double length = NumericServices.hypot(velocity[0], velocity[2]);
                  if (length == 0.0) {
                     doSearch = false;
                  } else {
                     float normalized = (float)((double)i.yaw() * d(1400960) / d(1400968) + d(1400960));
                     float fx = RotationMath.sine(normalized);
                     float fy = RotationMath.cosine(normalized);
                     double ratio = NumericServices.acos(((double)fy * velocity[2] + (double)fx * velocity[0]) / length);
                     if (!(d(1400976) > ratio)) {
                        doSearch = false;
                     }
                  }
               }

               boolean found = doSearch && this.search(i, s, eye, box, yaw, pitch, r, now);
               if (found) {
                  if (s.select == 1 || s.select == 2) {
                     i.select(s.select == 2, false);
                  }

                  this.placement = i.heldBlock();
                  if (this.placement) {
                     float high = (float)((double)s.randomization * d(1400984) + d(1401024));
                     this.snapDelay = RecoveredMath.truncateInt(r.interval(f(1401048), high) * s.snapDelay);
                     this.phase = 10;
                     this.disableLatched = s.disableAfter;
                  }
               }

               if (!this.placement) {
                  if (this.snapDelay > 0) {
                     this.snapDelay--;
                     this.snapRemaining = RecoveredMath.truncateInt(s.snapDuration);
                  } else {
                     if (this.snapRemaining <= 0) {
                        if (this.disableLatched) {
                           i.disable();
                           this.disableLatched = false;
                        }

                        return;
                     }

                     this.snapRemaining--;
                  }
               }

               if (i.claim(1000)) {
                  this.correctMovement = true;
                  if (!this.placement) {
                     if (this.snapDelay <= 0) {
                        if (this.snapRemaining < 1) {
                           if (this.disableLatched) {
                              i.disable();
                              this.disableLatched = false;
                           }
                        } else if (i.rotationAvailable()) {
                           float deltaYaw = RotationMath.wrap(i.storedYaw() - (i.yaw() + i.offsetYaw()));
                           float deltaPitch = i.storedPitch() - (i.pitch() + i.offsetPitch());
                           float rate = (float)((double)f(1401048) / (double)this.snapRemaining);
                           i.publish(deltaYaw * rate, deltaPitch * rate);
                        }
                     }
                  } else {
                     Vec3 direction = PlacementGeometry.direction(i.yaw(), i.pitch());
                     double extent = d(1401064);
                     Vec3 collision = new Vec3(extent * direction.xCoord, extent * direction.yCoord, extent * direction.zCoord);
                     double[] query = new double[]{
                        (double)this.x + d(1401072),
                        (double)this.y + d(1401080),
                        (double)this.z + d(1401088),
                        (double)this.x + d(1401096),
                        (double)this.y + 1.0,
                        (double)this.z + d(1401056)
                     };
                     Vec3 hit = PlacementGeometry.rayPoint(query, eye, collision);
                     if (hit == null || !(hit.yCoord < (double)this.y + d(1401104))) {
                        float[] desired = RotationMath.angles(eye, this.selected);
                        float deltaYaw = desired[0] - yaw;
                        float deltaPitch = desired[1] - pitch;
                        float gate = !((double)desired[1] > d(1401112))
                           ? RotationMath.wrap(deltaYaw)
                           : smooth(this.pitchNoise, f(1401120), f(1401124), d(1400984), r, now) * s.randomization;
                        float low = (float)((double)s.randomization * d(1401128) + d(1401024));
                        float high = f(1401048);
                        float a = this.speedX.sample(low, high, d(1401136), d(1401024), r) * s.aimSpeed;
                        float b = this.speedZ.sample(low, high, d(1401136), d(1401024), r) * s.aimSpeed;
                        float negativeA = Float.intBitsToFloat(Float.floatToRawIntBits(a) ^ ActionConstants.intAt(1401152));
                        float negativeB = Float.intBitsToFloat(Float.floatToRawIntBits(b) ^ ActionConstants.intAt(1401152));
                        float first = RotationMath.clamp(negativeA, a, gate);
                        float second = RotationMath.clamp(negativeB, b, deltaPitch);
                        float accumA = s.aimMult;
                        float accumB = s.aimMult;
                        accumA += this.speedX.sample(f(1401168), f(1401016), d(1401136), d(1401024), r);
                        accumB += this.speedZ.sample(f(1401168), f(1401016), d(1401136), d(1401024), r);
                        accumA *= gate;
                        accumB *= deltaPitch;
                        i.publish((accumA - first) * s.aimStrength + first, (accumB - second) * s.aimStrength + second);
                     }
                  }
               }
            }
         }
      }

      public strictfp void clear() {
         this.placement = this.centered = this.correctMovement = this.disableLatched = false;
         this.clickTimer = 0.0F;
         this.x = this.y = this.z = this.unhurt = this.phase = this.snapDelay = this.snapRemaining = 0;
         this.selected = new Vec3(0.0, 0.0, 0.0);
         this.jumpAccumulator = 0.0;
      }
   }
}
