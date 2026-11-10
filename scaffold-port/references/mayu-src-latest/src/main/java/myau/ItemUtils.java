package myau;

import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

public final class ItemUtils {
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    private static final List<Integer> BENEFICIAL_POTION_IDS = new BeneficialPotionIds();
    private static final int INVENTORY_SEARCH_SIZE = 36;
    private static final int HOTBAR_SIZE = 9;
    private static final int TOTAL_INVENTORY_SLOTS = 40;
    private static final int MINIMUM_TOOL_DURABILITY = 30;

    private ItemUtils() {
    }

    public static boolean isDiscardableItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            return !ItemUtils.isSuitablePlacementBlock((ItemBlock)item);
        }
        if (item instanceof ItemPotion) {
            List effects = ((ItemPotion)item).getEffects(stack);
            return ((List<PotionEffect>)(List<?>)effects).stream().map(PotionEffect::getPotionID).noneMatch(BENEFICIAL_POTION_IDS::contains);
        }
        if (item instanceof ItemFishingRod || item instanceof ItemSnowball || item instanceof ItemEgg || item instanceof ItemBucket || item instanceof ItemEnderPearl) {
            return false;
        }
        if (item instanceof ItemFood && item != Items.spider_eye) {
            return false;
        }
        return item != Items.nether_star;
    }

    public static boolean isPlaceableBlockStack(ItemStack stack) {
        return stack != null && stack.stackSize >= 1 && stack.getItem() instanceof ItemBlock && ItemUtils.isSuitablePlacementBlock((ItemBlock)stack.getItem());
    }

    public static boolean isSuitablePlacementBlock(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        return !BlockUtils.isInteractable(block) && BlockUtils.isSuitableFullBlock(block);
    }

    public static double weaponScore(ItemStack stack) {
        if (stack == null) {
            return 0.0;
        }
        double score = 0.0;
        Multimap modifiers = stack.getAttributeModifiers();
        for (String attributeName : (Iterable<String>)(Object)modifiers.keySet()) {
            Collection attackModifiers;
            if (!"generic.attackDamage".equals(attributeName) || (attackModifiers = modifiers.get((Object)attributeName)).isEmpty()) continue;
            score += ((AttributeModifier)attackModifiers.iterator().next()).getAmount();
        }
        if (stack.isItemEnchanted()) {
            score += (double)EnchantmentHelper.getEnchantmentLevel((int)Enchantment.fireAspect.effectId, (ItemStack)stack);
            score += (double)EnchantmentHelper.getEnchantmentLevel((int)Enchantment.sharpness.effectId, (ItemStack)stack) * 1.25;
        }
        return score;
    }

    public static float toolEfficiencyScore(ItemStack stack) {
        int efficiencyLevel;
        if (stack == null || !(stack.getItem() instanceof ItemTool)) {
            return 1.0f;
        }
        float score = ((ItemTool)stack.getItem()).getToolMaterial().getEfficiencyOnProperMaterial();
        if (score > 1.0f && (efficiencyLevel = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.efficiency.effectId, (ItemStack)stack)) > 0) {
            score += (float)(efficiencyLevel * efficiencyLevel + 1);
        }
        return score;
    }

    public static double armorScore(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemArmor)) {
            return 0.0;
        }
        double score = ((ItemArmor)stack.getItem()).damageReduceAmount;
        if (stack.isItemEnchanted()) {
            score += (double)EnchantmentHelper.getEnchantmentLevel((int)Enchantment.protection.effectId, (ItemStack)stack) * 0.25;
        }
        return score;
    }

    public static int findBestWeaponSlot(int startSlot, boolean avoidLowDurability) {
        int bestSlot = -1;
        double bestScore = 0.0;
        for (int offset = 0; offset < 36; ++offset) {
            double score;
            int slot = ItemUtils.wrapInventorySlot(startSlot + offset);
            ItemStack stackCandidate = ItemUtils.minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stackCandidate == null || !(stackCandidate.getItem() instanceof ItemSword) || avoidLowDurability && ItemUtils.hasLowDurability(stackCandidate) || !((score = ItemUtils.weaponScore(stackCandidate)) > bestScore)) continue;
            bestSlot = slot;
            bestScore = score;
        }
        return bestSlot;
    }

    public static int findBestToolSlot(String toolClass, int startSlot, boolean avoidLowDurability) {
        int bestSlot = -1;
        float bestScore = 1.0f;
        for (int offset = 0; offset < 36; ++offset) {
            float score;
            int slot = ItemUtils.wrapInventorySlot(startSlot + offset);
            ItemStack stack = ItemUtils.minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof ItemTool) || !stack.getItem().getToolClasses(stack).contains(toolClass) || avoidLowDurability && ItemUtils.hasLowDurability(stack) || !((score = ItemUtils.toolEfficiencyScore(stack)) > bestScore)) continue;
            bestSlot = slot;
            bestScore = score;
        }
        return bestSlot;
    }

    public static int findBestToolForBlock(int preferredSlot, Block block) {
        int bestSlot = preferredSlot;
        ItemStack preferredStack = ItemUtils.minecraft.thePlayer.inventory.getStackInSlot(preferredSlot);
        float bestStrength = preferredStack == null ? 1.0f : preferredStack.getStrVsBlock(block);
        for (int slot = 0; slot < 9; ++slot) {
            float strength;
            ItemStack stackSecondary = ItemUtils.minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stackSecondary == null || !((strength = stackSecondary.getStrVsBlock(block)) > bestStrength)) continue;
            bestSlot = slot;
            bestStrength = strength;
        }
        return bestSlot;
    }

    public static int findBestArmorSlot(int armorType, boolean avoidLowDurability) {
        int bestSlot = -1;
        double bestScore = 0.0;
        for (int slot = 0; slot < 40; ++slot) {
            double score;
            ItemStack stackAlternate = ItemUtils.minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stackAlternate == null || !(stackAlternate.getItem() instanceof ItemArmor)) continue;
            ItemArmor armor = (ItemArmor)stackAlternate.getItem();
            if (armor.armorType != armorType || avoidLowDurability && ItemUtils.hasLowDurability(stackAlternate) || !((score = ItemUtils.armorScore(stackAlternate)) >= bestScore)) continue;
            bestSlot = slot;
            bestScore = score;
        }
        return bestSlot;
    }

    public static int findLargestBlockStackSlot(int startSlot) {
        int bestSlot = -1;
        int largestStackSize = 0;
        for (int offset = 0; offset < 36; ++offset) {
            int slot = ItemUtils.wrapInventorySlot(startSlot + offset);
            ItemStack stackResult = ItemUtils.minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (!ItemUtils.isPlaceableBlockStack(stackResult) || stackResult.stackSize <= largestStackSize) continue;
            bestSlot = slot;
            largestStackSize = stackResult.stackSize;
        }
        return bestSlot;
    }

    public static boolean isHoldingWeapon() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        if (heldItem == null) {
            return false;
        }
        if (heldItem.hasTagCompound()) {
            Item.ToolMaterial material;
            long itemId;
            NBTTagCompound extraAttributes;
            NBTTagCompound tags = heldItem.getTagCompound();
            if (tags.hasKey("ExtraAttributes") && (extraAttributes = tags.getCompoundTag("ExtraAttributes")).hasKey("UHCid") && ((itemId = extraAttributes.getLong("UHCid")) == 50006L || itemId == 50009L)) {
                return true;
            }
            if (tags.hasKey("HideFlags") && heldItem.getItem() instanceof ItemSpade && (material = ((ItemSpade)heldItem.getItem()).getToolMaterial()) == Item.ToolMaterial.EMERALD) {
                return true;
            }
            if (tags.hasKey("ItemModel") && "minecraft:mace".equals(tags.getString("ItemModel"))) {
                return true;
            }
        }
        if (heldItem.getItem() instanceof ItemEnchantedBook) {
            return false;
        }
        Map enchantments = EnchantmentHelper.getEnchantments((ItemStack)heldItem);
        return enchantments.containsKey(19) || heldItem.getItem() instanceof ItemSword;
    }

    public static boolean isHoldingSword() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemSword;
    }

    public static boolean isHoldingTool() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemTool;
    }

    public static boolean isHoldingConsumable() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        if (heldItem == null) {
            return false;
        }
        if (ItemPotion.isSplash((int)heldItem.getItem().getMetadata(heldItem))) {
            return false;
        }
        EnumAction action = heldItem.getItemUseAction();
        return action == EnumAction.EAT || action == EnumAction.DRINK;
    }

    public static boolean isHoldingBow() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemBow;
    }

    public static boolean isHoldingBlocks() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        return heldItem != null && heldItem.stackSize >= 1 && heldItem.getItem() instanceof ItemBlock;
    }

    public static boolean isHoldingPlaceableBlock() {
        return ItemUtils.isPlaceableBlockStack(ItemUtils.minecraft.thePlayer.getHeldItem());
    }

    public static boolean isHoldingFireball() {
        ItemStack heldItem = ItemUtils.minecraft.thePlayer.getHeldItem();
        return heldItem != null && heldItem.stackSize >= 1 && heldItem.getItem() instanceof ItemFireball;
    }

    private static int wrapInventorySlot(int slot) {
        return (slot % 36 + 36) % 36;
    }

    private static boolean hasLowDurability(ItemStack stackValue) {
        return stackValue.isItemDamaged() && stackValue.getMaxDamage() - stackValue.getItemDamage() < 30;
    }
}

