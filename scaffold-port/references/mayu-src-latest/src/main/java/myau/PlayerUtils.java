package myau;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;

public final class PlayerUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();

    private PlayerUtils() {
    }

    public static boolean isJumpKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindJump.getKeyCode());
    }

    public static boolean isSneakKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindSneak.getKeyCode());
    }

    public static boolean isLeftKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindLeft.getKeyCode());
    }

    public static boolean isRightKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindRight.getKeyCode());
    }

    public static boolean isAttackKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindAttack.getKeyCode());
    }

    public static boolean isUseItemKeyDown() {
        return PlayerUtils.isGameplayKeyDown(PlayerUtils.minecraft.gameSettings.keyBindUseItem.getKeyCode());
    }

    private static boolean isGameplayKeyDown(int keyCode) {
        return PlayerUtils.minecraft.currentScreen == null && InputUtils.isKeyDown(keyCode);
    }

    public static boolean wouldTakeFallDamage(float safeFallDistance) {
        if (PlayerUtils.minecraft.thePlayer.capabilities.allowFlying || PlayerUtils.minecraft.thePlayer.capabilities.disableDamage) {
            return false;
        }
        PotionEffect jumpBoost = PlayerUtils.minecraft.thePlayer.getActivePotionEffect(Potion.jump);
        float jumpReduction = jumpBoost == null ? 0.0f : (float)(jumpBoost.getAmplifier() + 1);
        float projectedFallDistance = PlayerUtils.minecraft.thePlayer.fallDistance;
        if (PlayerUtils.minecraft.thePlayer.motionY < -0.67 || !PlayerUtils.hasCollisionBelow()) {
            projectedFallDistance -= (float)PlayerUtils.minecraft.thePlayer.motionY;
        }
        return MathHelper.ceiling_float_int((float)(projectedFallDistance - safeFallDistance - jumpReduction)) > 0;
    }

    public static boolean isAirBelow(int depth) {
        int y;
        if (PlayerUtils.minecraft.thePlayer.capabilities.allowFlying || PlayerUtils.minecraft.thePlayer.capabilities.disableDamage) {
            return false;
        }
        int playerY = MathHelper.floor_double((double)PlayerUtils.minecraft.thePlayer.posY);
        for (int offset = 0; offset <= depth && (y = playerY - offset) >= 0; ++offset) {
            Block block = PlayerUtils.minecraft.theWorld.getBlockState(new BlockPos(PlayerUtils.minecraft.thePlayer.posX, (double)y, PlayerUtils.minecraft.thePlayer.posZ)).getBlock();
            if (block instanceof BlockAir) continue;
            return false;
        }
        return true;
    }

    public static boolean isOverVoid() {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().expand(-1.0E-6, 0.0, -1.0E-6);
        return PlayerUtils.isBoundsOverVoid(bounds);
    }

    public static boolean isBoundsOverVoid(AxisAlignedBB bounds) {
        if (PlayerUtils.minecraft.thePlayer.isInWater() || PlayerUtils.minecraft.thePlayer.isInLava()) {
            return false;
        }
        int startY = MathHelper.floor_double((double)bounds.minY);
        if (startY < 0) {
            return true;
        }
        int minX = MathHelper.floor_double((double)bounds.minX);
        int maxX = MathHelper.floor_double((double)(bounds.maxX + 1.0));
        int minZ = MathHelper.floor_double((double)bounds.minZ);
        int maxZ = MathHelper.floor_double((double)(bounds.maxZ + 1.0));
        for (int x = minX; x < maxX; ++x) {
            for (int z = minZ; z < maxZ; ++z) {
                for (int y = startY; y >= 0; --y) {
                    if (BlockUtils.isReplaceableAt(new BlockPos(x, y, z))) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isOffsetCollisionFree(double offsetX, double offsetY, double offsetZ) {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ);
        return PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }

    public static boolean isNarrowOffsetCollisionFree(double offsetX, double offsetY, double offsetZ, float widthFactor) {
        double horizontalInset = PlayerUtils.minecraft.thePlayer.width / -2.0f * (1.0f - widthFactor);
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ).expand(horizontalInset, 0.0, horizontalInset);
        return PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }

    public static boolean hasCollisionBelow() {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(0.0, -1.0, 0.0);
        return !PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }

    public static boolean hasCollisionAbove() {
        AxisAlignedBB bounds = PlayerUtils.minecraft.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);
        return !PlayerUtils.minecraft.theWorld.getCollidingBoundingBoxes((Entity)PlayerUtils.minecraft.thePlayer, bounds).isEmpty();
    }

    public static boolean isBlockWithinReach(BlockPos position, double reach) {
        return PlayerUtils.isBlockWithinReachFrom(position, PlayerUtils.minecraft.thePlayer.posX, PlayerUtils.minecraft.thePlayer.posY + (double)PlayerUtils.minecraft.thePlayer.getEyeHeight(), PlayerUtils.minecraft.thePlayer.posZ, reach);
    }

    public static boolean isBlockWithinReachFrom(BlockPos position, double eyeX, double eyeY, double eyeZ, double reach) {
        return position.distanceSqToCenter(eyeX, eyeY, eyeZ) < reach * reach;
    }

    public static void attackLocally(Entity target) {
        if (!ForgeHooks.onPlayerAttackTarget((EntityPlayer)PlayerUtils.minecraft.thePlayer, (Entity)target) || !target.canAttackWithItem() || target.hitByEntity((Entity)PlayerUtils.minecraft.thePlayer)) {
            return;
        }
        float baseDamage = (float)PlayerUtils.minecraft.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        ItemStack heldItem = PlayerUtils.minecraft.thePlayer.getHeldItem();
        EnumCreatureAttribute creatureAttribute = target instanceof EntityLivingBase ? ((EntityLivingBase)target).getCreatureAttribute() : EnumCreatureAttribute.UNDEFINED;
        float enchantmentDamage = EnchantmentHelper.func_152377_a((ItemStack)heldItem, (EnumCreatureAttribute)creatureAttribute);
        int knockbackLevel = EnchantmentHelper.getKnockbackModifier((EntityLivingBase)PlayerUtils.minecraft.thePlayer);
        if (PlayerUtils.minecraft.thePlayer.isSprinting()) {
            ++knockbackLevel;
        }
        if (baseDamage <= 0.0f && enchantmentDamage <= 0.0f) {
            return;
        }
        boolean criticalHit = PlayerUtils.isCriticalAttack();
        if (criticalHit && baseDamage > 0.0f) {
            baseDamage *= 1.5f;
        }
        float totalDamage = baseDamage + enchantmentDamage;
        int fireAspectLevel = EnchantmentHelper.getFireAspectModifier((EntityLivingBase)PlayerUtils.minecraft.thePlayer);
        boolean temporaryFire = false;
        if (target instanceof EntityLivingBase && fireAspectLevel > 0 && !target.isBurning()) {
            temporaryFire = true;
            target.setFire(1);
        }
        double previousMotionX = target.motionX;
        double previousMotionY = target.motionY;
        double previousMotionZ = target.motionZ;
        boolean hit = target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)PlayerUtils.minecraft.thePlayer), totalDamage);
        if (!hit) {
            if (temporaryFire) {
                target.extinguish();
            }
            return;
        }
        if (knockbackLevel > 0) {
            target.addVelocity((double)(-MathHelper.sin((float)(PlayerUtils.minecraft.thePlayer.rotationYaw * (float)Math.PI / 180.0f)) * (float)knockbackLevel * 0.5f), 0.1, (double)(MathHelper.cos((float)(PlayerUtils.minecraft.thePlayer.rotationYaw * (float)Math.PI / 180.0f)) * (float)knockbackLevel * 0.5f));
            PlayerUtils.applyPostHitSprintSlowdown(target);
        }
        if (target instanceof EntityPlayerMP && target.velocityChanged) {
            ((EntityPlayerMP)target).playerNetServerHandler.sendPacket((Packet)new S12PacketEntityVelocity(target));
            target.velocityChanged = false;
            target.motionX = previousMotionX;
            target.motionY = previousMotionY;
            target.motionZ = previousMotionZ;
        }
        if (criticalHit) {
            PlayerUtils.minecraft.thePlayer.onCriticalHit(target);
        }
        if (enchantmentDamage > 0.0f) {
            PlayerUtils.minecraft.thePlayer.onEnchantmentCritical(target);
        }
        if (totalDamage >= 18.0f) {
            PlayerUtils.minecraft.thePlayer.triggerAchievement((StatBase)AchievementList.overkill);
        }
        PlayerUtils.minecraft.thePlayer.setLastAttacker(target);
        if (target instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)((EntityLivingBase)target), (Entity)PlayerUtils.minecraft.thePlayer);
        }
        EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)PlayerUtils.minecraft.thePlayer, (Entity)target);
        if (target instanceof EntityLivingBase) {
            PlayerUtils.minecraft.thePlayer.addStat(StatList.damageDealtStat, Math.round(totalDamage * 10.0f));
            if (fireAspectLevel > 0) {
                target.setFire(fireAspectLevel * 4);
            }
        }
        PlayerUtils.minecraft.thePlayer.addExhaustion(0.3f);
    }

    private static boolean isCriticalAttack() {
        return PlayerUtils.minecraft.thePlayer.fallDistance > 0.0f && !PlayerUtils.minecraft.thePlayer.onGround && !PlayerUtils.minecraft.thePlayer.isOnLadder() && !PlayerUtils.minecraft.thePlayer.isInWater() && !PlayerUtils.minecraft.thePlayer.isPotionActive(Potion.blindness) && PlayerUtils.minecraft.thePlayer.ridingEntity == null;
    }

    private static void applyPostHitSprintSlowdown(Entity target) {
        boolean useKeepSprint;
        KeepSprintModule keepSprint = (KeepSprintModule)MyauClient.moduleManager.modules.get(KeepSprintModule.class);
        boolean bl = useKeepSprint = keepSprint != null && keepSprint.isEnabled();
        if (useKeepSprint && ((Boolean)keepSprint.groundOnly.getValue()).booleanValue() && !PlayerUtils.minecraft.thePlayer.onGround) {
            useKeepSprint = false;
        }
        if (useKeepSprint && ((Boolean)keepSprint.reachOnly.getValue()).booleanValue() && RotationUtils.distanceToEntity(target) <= 3.0) {
            useKeepSprint = false;
        }
        if (useKeepSprint) {
            double multiplier = 0.6 + 0.4 * (1.0 - ((Integer)keepSprint.slowdown.getValue()).doubleValue() / 100.0);
            PlayerUtils.minecraft.thePlayer.motionX *= multiplier;
            PlayerUtils.minecraft.thePlayer.motionZ *= multiplier;
        } else {
            PlayerUtils.minecraft.thePlayer.motionX *= 0.6;
            PlayerUtils.minecraft.thePlayer.motionZ *= 0.6;
            PlayerUtils.minecraft.thePlayer.setSprinting(false);
        }
    }

    public static float estimateAttackDamage(Entity target) {
        ItemStack heldItem = PlayerUtils.minecraft.thePlayer.getHeldItem();
        float baseDamage = 1.0f;
        if (heldItem != null) {
            Multimap modifiers = heldItem.getAttributeModifiers();
            String attackDamageAttribute = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();
            for (AttributeModifier modifier : (Iterable<AttributeModifier>)(Object)modifiers.get((Object)attackDamageAttribute)) {
                baseDamage += (float)modifier.getAmount();
            }
        }
        EnumCreatureAttribute creatureAttribute = target instanceof EntityLivingBase ? ((EntityLivingBase)target).getCreatureAttribute() : EnumCreatureAttribute.UNDEFINED;
        float enchantmentDamage = EnchantmentHelper.func_152377_a((ItemStack)heldItem, (EnumCreatureAttribute)creatureAttribute);
        if (baseDamage <= 0.0f && enchantmentDamage <= 0.0f) {
            return 0.0f;
        }
        if (PlayerUtils.isCriticalAttack() && baseDamage > 0.0f) {
            baseDamage *= 1.5f;
        }
        return baseDamage + enchantmentDamage;
    }

    public static int swingDuration() {
        if (PlayerUtils.minecraft.thePlayer.isPotionActive(Potion.digSpeed)) {
            return 6 - (1 + PlayerUtils.minecraft.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier());
        }
        if (PlayerUtils.minecraft.thePlayer.isPotionActive(Potion.digSlowdown)) {
            return 6 + (1 + PlayerUtils.minecraft.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2;
        }
        return 6;
    }

    public static void swingLocally() {
        if (PlayerUtils.minecraft.thePlayer.isSwingInProgress && PlayerUtils.minecraft.thePlayer.swingProgressInt < PlayerUtils.swingDuration() / 2 && PlayerUtils.minecraft.thePlayer.swingProgressInt >= 0) {
            return;
        }
        PlayerUtils.minecraft.thePlayer.swingProgressInt = -1;
        PlayerUtils.minecraft.thePlayer.isSwingInProgress = true;
    }
}

