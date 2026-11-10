package dev.rice.client.combat.ravenbs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import dev.rice.client.RelationshipService;
import dev.rice.client.combat.openb4.B4AuraHost;
import dev.rice.client.combat.openb4.B4AuraHost.SliderSetting;
import dev.rice.client.combat.openb4.B4AuraHost.ButtonSetting;
import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemAxe;
import net.minecraft.init.Items;
import org.lwjgl.input.Keyboard;

/** Raven bS KillAura logic adapted to Rice lifecycle/settings hooks.
 * Source: tools/fixtures/ravenbs/KillAura.java.txt. Forge event carriers are supplied by KillAuraModule.
 * Rice does not have bS's opt-in BedWars/SkyWars minigame modules: their unicorn targeting,
 * hostile-only override, and module-owned spawn registries therefore stay at bS's disabled defaults.
 */
public final class RavenBsKillAuraBackend {
   public static RavenBsKillAuraBackend instance;
   public boolean enabled;
   private final Minecraft mc = Minecraft.getMinecraft();
   private static final Field POINTED_ENTITY = ReflectionHelper.findField(EntityRenderer.class, "pointedEntity", "field_78528_u");
   // Resolve LWJGL internals only when this module actually synthesizes a click.
   private static Field mouseButtons;
   private boolean syntheticButton;
   private boolean originalButton;
   public dev.rice.setting.StringListSetting enemies;
   public SliderSetting targetCPS;
   public SliderSetting fov;
   public SliderSetting attackRange;
   public SliderSetting swingRange;
   public SliderSetting aimRange;
   public SliderSetting rotationMode;
   public SliderSetting speed;
   public SliderSetting sortMode;
   public SliderSetting switchDelay;
   public SliderSetting targets;
   public ButtonSetting attackMobs;
   public ButtonSetting targetInvis;
   public ButtonSetting disableInInventory;
   public ButtonSetting disableWhileMining;
   public ButtonSetting aimThroughBlocks;
   public ButtonSetting aimThroughEntities;
   public ButtonSetting ignoreTeammates;
   public ButtonSetting prioritizeEnemies;
   public ButtonSetting notUsingItem;
   public ButtonSetting requireMouseDown;
   public ButtonSetting weaponOnly;
   private String[] rotationModes = new String[]{"Silent", "Lock view", "None"};
   private String[] sortModes = new String[]{"Distance", "Health", "Hurt time", "Yaw"};
   public static EntityLivingBase target;
   public static EntityLivingBase attackingEntity;
   private HashMap<Integer, Integer> hitMap = new HashMap();
   private List<Entity> hostileMobs = new ArrayList();
   private Map<Integer, Boolean> golems = new HashMap();
   private long nextClickTime;
   private Random rand;
   private double targetDistance = Double.MAX_VALUE;

   public boolean isRequireMouseDown() {
      return this.requireMouseDown.isToggled();
   }

   public RavenBsKillAuraBackend() { instance = this; }
   public boolean isEnabled() { return enabled; }
   public double attackRange() { return attackRange.getInput(); }
   public double swingRange() { return swingRange.getInput(); }
   public double aimRange() { return aimRange.getInput(); }
   public boolean inSwingRange() { return target != null && targetDistance <= swingRange.getInput(); }
   public void restoreSyntheticButton() {
      if (!syntheticButton) return;
      try {
         ByteBuffer buttons = (ByteBuffer) mouseButtons.get(null);
         if (buttons != null) buttons.put(0, (byte)(originalButton ? 1 : 0));
      } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
      syntheticButton = false;
   }
   private void setSyntheticButton() {
      if (mouseButtons == null) mouseButtons = ReflectionHelper.findField(Mouse.class, "buttons");
      try {
         ByteBuffer buttons = (ByteBuffer) mouseButtons.get(null);
         if (buttons != null) {
            if (!syntheticButton) originalButton = buttons.get(0) != 0;
            syntheticButton = true;
            buttons.put(0, (byte)1);
         }
      } catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
   }
   public void resetWorld() {
      onDisable(); hostileMobs.clear(); golems.clear(); restoreSyntheticButton();
   }

   public String getInfo() {
      return this.rotationMode.getInput() == (double)2.0F ? (int)this.fov.getInput() + "\u00b0" : this.rotationModes[(int)this.rotationMode.getInput()];
   }

   public void onEnable() {
      this.rand = new Random();
      this.nextClickTime = 0L;
   }

   public void onDisable() {
      this.hitMap.clear();
      this.setTarget((Entity)null);
      this.nextClickTime = 0L;
   }

   public float[] onClientRotation(float baseYaw, float basePitch) {
      if (basicCondition() && settingCondition()) {
         // Rice's BedNuker/Block In ownership replaces the reference BedAura priority API.
         if (B4AuraHost.placementBusy()) { setTarget(null); return null; }
         handleTarget();
         if (target != null) {
            targetDistance = RavenBsRotations.distanceFromEyeToClosestOnAABB(target);
            if (rotationMode.getInput() == 0.0 && targetDistance <= aimRange.getInput()) {
               return RavenBsRotations.toTarget(target, baseYaw, basePitch, (int)speed.getInput(),
                     aimRange.getInput(), aimThroughBlocks.isToggled(), aimThroughEntities.isToggled());
            }
         }
      } else { setTarget(null); }
      return null;
   }

   public void onUpdate() {
      if (this.rotationMode.getInput() == (double)1.0F && target != null) {
         double aimRangeVal = this.aimRange.getInput();
         if (this.targetDistance <= aimRangeVal) {
            int speedVal = (int)this.speed.getInput();
            boolean useBackup = !this.aimThroughBlocks.isToggled() || !this.aimThroughEntities.isToggled();
            float[] rot = RavenBsRotations.toTarget(target, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, speedVal, aimRangeVal, this.aimThroughBlocks.isToggled(), this.aimThroughEntities.isToggled());
            if (rot != null) {
               mc.thePlayer.rotationYaw = rot[0];
               mc.thePlayer.rotationPitch = rot[1];
            }
         }
      }

      if (target != null && this.targetDistance <= this.attackRange.getInput()) {
         attackingEntity = target;
      } else {
         attackingEntity = null;
      }

   }

   public void onPrePlayerInteract() {
      if (nullCheck()) {
         if (target != null) {
            if (!(this.targetDistance > this.swingRange.getInput())) {
               int key = mc.gameSettings.keyBindAttack.getKeyCode();
               long now = System.currentTimeMillis();
               if (this.nextClickTime == 0L) {
                  this.nextClickTime = now;
               }

               int clicks;
               for(clicks = 0; this.nextClickTime <= now; this.nextClickTime += this.nextDelay()) {
                  ++clicks;
               }

               if (this.basicCondition() && this.settingCondition()) {
                  if (!this.notUsingItem.isToggled() || !mc.thePlayer.isUsingItem()) {
                     for(int i = 0; i < clicks; ++i) {
                        KeyBinding.onTick(key);
                        setSyntheticButton();
                     }

                  }
               }
            }
         }
      }
   }

   /** LivingSetAttackTargetEvent equivalent for client entities available through the Rice world. */
   public void refreshHostiles() {
      for (Entity entity : mc.theWorld.loadedEntityList) {
         if (!(entity instanceof EntityCreature)) continue;
         EntityCreature mob = (EntityCreature)entity;
         EntityLivingBase victim = mob.getAttackTarget();
         if (victim == mc.thePlayer && !(bedwarsStatus() == 2 && mob instanceof EntityPigZombie)) {
            if (!hostileMobs.contains(mob)) hostileMobs.add(mob);
         } else if (victim == null) hostileMobs.remove(mob);
      }
   }

   public void setTarget(Entity entity) {
      if (!(entity instanceof EntityLivingBase)) {
         target = null;
         attackingEntity = null;
         this.targetDistance = Double.MAX_VALUE;
         this.nextClickTime = 0L;
      } else {
         target = (EntityLivingBase)entity;
      }

   }

   private void handleTarget() {
      double maxRange = Math.max(this.attackRange.getInput(), this.aimRange.getInput());
      float fovValue = (float)this.fov.getInput();
      List<KillAuraTarget> candidates = new ArrayList();

      for(Entity entity : mc.theWorld.loadedEntityList) {
         Candidate candidate = this.getCandidateTarget(entity, maxRange, fovValue);
         if (candidate != null) {
            KillAuraTarget auraTarget = this.buildKillAuraTarget(candidate.entity, candidate.distance, maxRange);
            if (auraTarget != null) {
               candidates.add(auraTarget);
            }
         }
      }

      if (this.prioritizeEnemies.isToggled()) {
         List<KillAuraTarget> enemies = new ArrayList();

         for(KillAuraTarget candidate : candidates) {
            if (candidate.isEnemy) {
               enemies.add(candidate);
            }
         }

         if (!enemies.isEmpty()) {
            candidates = enemies;
         }
      }

      candidates.sort(this.getTargetComparator().thenComparingDouble((c) -> c.distance));
      double attackRangeValue = this.attackRange.getInput();
      List<KillAuraTarget> attackTargets = new ArrayList();

      for(KillAuraTarget candidate : candidates) {
         if (candidate.distance <= attackRangeValue) {
            attackTargets.add(candidate);
         }
      }

      if (!attackTargets.isEmpty()) {
         KillAuraTarget selectedAttackTarget = this.selectAttackTarget(attackTargets);
         if (selectedAttackTarget != null) {
            this.setTarget(selectedAttackTarget.entity);
         }
      } else if (!candidates.isEmpty()) {
         this.setTarget(((KillAuraTarget)candidates.get(0)).entity);
      } else {
         this.setTarget((Entity)null);
      }
   }

   private Candidate getCandidateTarget(Entity entity, double maxRange, float fovValue) {
      if (entity instanceof EntityLivingBase && entity != mc.thePlayer && !entity.isDead) {
         if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            if ((RelationshipService.flags(player) & 4) != 0 || player.deathTime != 0) {
               return null;
            }

            if ((RelationshipService.flags(entity) & 2) != 0 || this.ignoreTeammates.isToggled() && (RelationshipService.flags(entity) & 1) != 0) {
               return null;
            }
         } else {
            if (!(entity instanceof EntityCreature) || !this.attackMobs.isToggled()) {
               return null;
            }

            EntityCreature creature = (EntityCreature)entity;
            if (ReflectionHelper.getPrivateValue(net.minecraft.entity.EntityLiving.class, creature, "tasks", "field_70714_bg") == null || creature.isAIDisabled() || creature.deathTime != 0) {
               return null;
            }

            String canonicalName = entity.getClass().getCanonicalName();
            if (canonicalName == null || !canonicalName.startsWith("net.minecraft.entity.monster.")) {
               return null;
            }
         }

         if (entity.isInvisible() && !this.targetInvis.isToggled()) {
            return null;
         } else if (fovValue != 360.0F && !inFov(fovValue, entity)) {
            return null;
         } else {
            double distance = RavenBsRotations.distanceFromEyeToClosestOnAABB(entity);
            if (distance > maxRange) {
               return null;
            } else {
               return new Candidate((EntityLivingBase)entity, distance);
            }
         }
      } else {
         return null;
      }
   }

   private KillAuraTarget buildKillAuraTarget(EntityLivingBase entity, double distanceToBoundingBox, double maxRange) {
      if (entity instanceof EntityCreature && this.attackMobs.isToggled() && !this.isHostile((EntityCreature)entity)) {
         return null;
      } else {
         double multipointH = (double)100.0F;
         double multipointV = (double)100.0F;
         if (!RavenBsRotations.hasValidAimPoint(entity, multipointH, multipointV, maxRange, this.aimThroughBlocks.isToggled(), this.aimThroughEntities.isToggled(), true)) {
            return null;
         } else {
            boolean isEnemyPlayer = entity instanceof EntityPlayer && enemies.containsIgnoreCase(entity.getName());
            return new KillAuraTarget(entity, distanceToBoundingBox, entity.getHealth(), entity.hurtTime, RavenBsRotations.distanceFromYaw(entity, false), entity.getEntityId(), isEnemyPlayer);
         }
      }
   }

   private Comparator<KillAuraTarget> getTargetComparator() {
      switch ((int)this.sortMode.getInput()) {
         case 0:
         default:
            return Comparator.comparingDouble((target) -> target.distance);
         case 1:
            return Comparator.comparingDouble((target) -> (double)target.health);
         case 2:
            return Comparator.comparingInt((target) -> target.hurttime);
         case 3:
            return Comparator.comparingDouble((target) -> target.yawDelta);
      }
   }

   private KillAuraTarget selectAttackTarget(List<KillAuraTarget> attackTargets) {
      int ticksExisted = mc.thePlayer.ticksExisted;
      int switchDelayTicks = (int)(this.switchDelay.getInput() / (double)50.0F);
      long noHitTicks = (long)Math.min((double)attackTargets.size(), this.targets.getInput()) * (long)switchDelayTicks;

      for(KillAuraTarget candidate : attackTargets) {
         Integer firstHitTick = (Integer)this.hitMap.get(candidate.entityId);
         if (firstHitTick != null && ticksExisted - firstHitTick < switchDelayTicks) {
            return candidate;
         }
      }

      for(KillAuraTarget candidate : attackTargets) {
         Integer firstHitTick = (Integer)this.hitMap.get(candidate.entityId);
         if (firstHitTick == null || (long)ticksExisted >= (long)firstHitTick + noHitTicks) {
            this.hitMap.put(candidate.entityId, ticksExisted);
            return candidate;
         }
      }

      return null;
   }

   private boolean isHostile(EntityCreature entityCreature) {
      if (entityCreature instanceof EntitySilverfish) {
         String teamColor = firstColor(entityCreature.getCustomNameTag());
         String teamColorSelf = firstColor(mc.thePlayer.getDisplayName().getFormattedText());
         return teamColor.isEmpty() || !teamColorSelf.equals(teamColor) && (RelationshipService.flags(entityCreature) & 1) == 0;
      } else if (!(entityCreature instanceof EntityIronGolem)) {
         return entityCreature instanceof EntityPigZombie && bedwarsStatus() != 2 ? false : this.hostileMobs.contains(entityCreature);
      } else if (bedwarsStatus() != 2) {
         return true;
      } else if (this.golems.containsKey(entityCreature.getEntityId())) {
         return !(Boolean)this.golems.getOrDefault(entityCreature.getEntityId(), false);
      } else {
         double nearestDistance = (double)-1.0F;
         EntityArmorStand nearestArmorStand = null;

         for(Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
               String stripped = strip(entity.getDisplayName().getFormattedText());
               if (stripped.contains("[") && stripped.endsWith("]")) {
                  double distanceSq = entity.getDistanceSq(entityCreature.posX, entityCreature.posY, entityCreature.posZ);
                  if (distanceSq < nearestDistance || nearestDistance == (double)-1.0F) {
                     nearestDistance = distanceSq;
                     nearestArmorStand = (EntityArmorStand)entity;
                  }
               }
            }
         }

         if (nearestArmorStand == null) {
            // The reference consults BedWars.spawnedMobs here. Rice has no BedWars module,
            // so its module-owned spawn registry is empty, as with that module disabled in bS.
            return true;
         } else {
            String teamColor = firstColor(nearestArmorStand.getDisplayName().getFormattedText());
            String teamColorSelf = firstColor(mc.thePlayer.getDisplayName().getFormattedText());
            boolean isTeam = !teamColor.isEmpty() && (teamColorSelf.equals(teamColor) || (RelationshipService.flags(nearestArmorStand) & 1) != 0);
            this.golems.put(entityCreature.getEntityId(), isTeam);
            return !isTeam;
         }
      }
   }

   private boolean basicCondition() {
      if (!nullCheck()) {
         return false;
      } else {
         return !mc.thePlayer.isDead;
      }
   }

   private boolean settingCondition() {
      if (this.requireMouseDown.isToggled() && !Mouse.isButtonDown(0)) {
         return false;
      } else if (this.weaponOnly.isToggled() && !holdingWeapon()) {
         return false;
      } else if (this.disableWhileMining.isToggled() && isMining()) {
         return false;
      } else {
         return !this.disableInInventory.isToggled() || mc.currentScreen == null;
      }
   }

   private long nextDelay() {
      int cps = Math.max(1, (int)this.targetCPS.getInput());
      int baseDelay = 1000 / cps;
      int finalDelay = baseDelay + (this.rand.nextInt(21) - 10);
      return (long)Math.max(33, Math.min(180, finalDelay));
   }

   public SliderSetting getAttackRangeSetting() {
      return this.attackRange;
   }

   public SliderSetting getSwingRangeSetting() {
      return this.swingRange;
   }

   public SliderSetting getAimRangeSetting() {
      return this.aimRange;
   }

   public boolean shouldOverrideMouseOver() {
      return this.isEnabled() && nullCheck() && attackingEntity != null && target == attackingEntity && this.basicCondition() && this.targetDistance <= this.swingRange.getInput();
   }

   public void modifyMouseOverFromGetMouseOver(float partialTicks) {
      if (this.shouldOverrideMouseOver()) {
         Entity viewEntity = mc.getRenderViewEntity();
         if (viewEntity != null) {
            Vec3 eyes = viewEntity.getPositionEyes(partialTicks);
            Vec3 look = viewEntity.getLook(partialTicks);
            double reach = this.attackRange.getInput();
            Vec3 rayEnd = eyes.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
            float border = attackingEntity.getCollisionBorderSize();
            AxisAlignedBB bb = attackingEntity.getEntityBoundingBox().expand((double)border, (double)border, (double)border);
            MovingObjectPosition intercept = bb.calculateIntercept(eyes, rayEnd);
            boolean inside = bb.isVecInside(eyes);
            if (inside || intercept != null) {
               Vec3 hitVec = inside ? (intercept == null ? eyes : intercept.hitVec) : intercept.hitVec;
               if (!this.aimThroughBlocks.isToggled()) {
                  MovingObjectPosition blockHit = RavenBsRotations.rayTraceBlocksIgnoringOpenFenceGates(eyes, hitVec, false, false, true);
                  if (blockHit != null && blockHit.typeOfHit == MovingObjectType.BLOCK) {
                     return;
                  }
               }

               if (this.aimThroughEntities.isToggled() || !RavenBsRotations.isPathBlockedByEntity(eyes, hitVec, attackingEntity)) {
                  mc.objectMouseOver = new MovingObjectPosition(attackingEntity, hitVec);
                  mc.pointedEntity = attackingEntity;
                  EntityRenderer renderer = mc.entityRenderer;
                  try { POINTED_ENTITY.set(renderer, attackingEntity); }
                  catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }

               }
            }
         }
      }
   }

   private boolean inFov(float fov, Entity entity) {
      fov *= 0.5;
      double delta = net.minecraft.util.MathHelper.wrapAngleTo180_double(
            (mc.thePlayer.rotationYaw - RavenBsRotations.i(entity.posX, entity.posZ)) % 360.0F);
      return delta > 0.0 ? delta < fov : delta > -fov;
   }
   private boolean nullCheck() { return mc.thePlayer != null && mc.theWorld != null; }
   private boolean holdingWeapon() {
      return mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
            || mc.thePlayer.getHeldItem().getItem() == Items.stick);
   }
   private boolean isMining() {
      int keyCode = mc.gameSettings.keyBindAttack.getKeyCode();
      if (keyCode == 0) return false;
      boolean attackDown = keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
      if (!attackDown) return false;
      double reach = mc.playerController.getBlockReachDistance();
      float yaw = mc.thePlayer.rotationYaw, pitch = mc.thePlayer.rotationPitch;
      MovingObjectPosition entityHit = RavenBsRotations.rayTrace(reach, 1.0F, new float[]{yaw,pitch}, null);
      if (entityHit != null && entityHit.typeOfHit == MovingObjectType.ENTITY) return false;
      MovingObjectPosition blockHit = RavenBsRotations.rayCastBlock(reach, yaw, pitch);
      return blockHit != null && blockHit.typeOfHit == MovingObjectType.BLOCK && blockHit.getBlockPos() != null;
   }
   private static String strip(String text) { return text == null ? "" : text.replaceAll("\u00a7[0-9a-fk-or]", ""); }
   private static String firstColor(String text) {
      if (text != null) for (int i=0;i+1<text.length();i++) if (text.charAt(i)=='\u00a7'
            && "0123456789abcdef".indexOf(Character.toLowerCase(text.charAt(i+1)))>=0)
         return text.substring(i,i+2);
      return "";
   }
   private int bedwarsStatus() {
      B4AuraHost.ServerState.State state=B4AuraHost.ServerState.detect();
      return state==B4AuraHost.ServerState.State.BEDWARS_GAME_BED || state==B4AuraHost.ServerState.State.BEDWARS_GAME_NO_BED ? 2 : 0;
   }
   private static final class Candidate {
      final EntityLivingBase entity;
      final double distance;

      Candidate(EntityLivingBase entity, double distance) {
         this.entity = entity;
         this.distance = distance;
      }
   }

   static class KillAuraTarget {
      final EntityLivingBase entity;
      final double distance;
      final float health;
      final int hurttime;
      final double yawDelta;
      final int entityId;
      final boolean isEnemy;

      public KillAuraTarget(EntityLivingBase entity, double distance, float health, int hurttime, double yawDelta, int entityId, boolean isEnemy) {
         this.entity = entity;
         this.distance = distance;
         this.health = health;
         this.hurttime = hurttime;
         this.yawDelta = yawDelta;
         this.entityId = entityId;
         this.isEnemy = isEnemy;
      }
   }
}
