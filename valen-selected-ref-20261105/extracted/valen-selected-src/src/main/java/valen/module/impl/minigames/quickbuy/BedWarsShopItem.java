/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemAxe
 *  net.minecraft.item.ItemPickaxe
 *  net.minecraft.item.ItemPotion
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ItemTool
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.nbt.NBTTagList
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.EnumChatFormatting
 */
package valen.module.impl.minigames.quickbuy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;

public enum BedWarsShopItem {
    WOOL("wool", "Wool", Category.BLOCKS, 16, Resource.IRON, 4, 4, true, BedWarsShopItem.block(Blocks.wool, 14), "wool"),
    HARDENED_CLAY("hardened_clay", "Hardened Clay", Category.BLOCKS, 16, Resource.IRON, 12, 12, true, BedWarsShopItem.block(Blocks.stained_hardened_clay, 14), "hardened clay"),
    WOOD("wood", "Wood", Category.BLOCKS, 16, Resource.GOLD, 4, 4, true, BedWarsShopItem.block(Blocks.planks, 0), "wood", "oak wood planks"),
    BLAST_PROOF_GLASS("blast_proof_glass", "Blast-Proof Glass", Category.BLOCKS, 4, Resource.IRON, 12, 12, true, BedWarsShopItem.block(Blocks.glass, 0), "blast proof glass"),
    END_STONE("end_stone", "End Stone", Category.BLOCKS, 12, Resource.IRON, 24, 24, true, BedWarsShopItem.block(Blocks.end_stone, 0), "end stone", "endstone"),
    LADDERS("ladders", "Ladders", Category.BLOCKS, 8, Resource.IRON, 4, 4, true, BedWarsShopItem.block(Blocks.ladder, 0), "ladder", "ladders"),
    PACKED_ICE("packed_ice", "Packed Ice", Category.BLOCKS, 4, Resource.IRON, 24, 24, true, BedWarsShopItem.block(Blocks.packed_ice, 0), "packed ice"),
    OBSIDIAN("obsidian", "Obsidian", Category.BLOCKS, 4, Resource.EMERALD, 4, 4, true, BedWarsShopItem.block(Blocks.obsidian, 0), "obsidian"),
    STONE_SWORD("stone_sword", "Stone Sword", Category.MELEE, 1, Resource.IRON, 10, 10, true, BedWarsShopItem.item(Items.stone_sword), "stone sword"),
    IRON_SWORD("iron_sword", "Iron Sword", Category.MELEE, 1, Resource.GOLD, 7, 7, true, BedWarsShopItem.item(Items.iron_sword), "iron sword"),
    DIAMOND_SWORD("diamond_sword", "Diamond Sword", Category.MELEE, 1, Resource.EMERALD, 4, 3, true, BedWarsShopItem.item(Items.diamond_sword), "diamond sword"),
    KNOCKBACK_STICK("knockback_stick", "Knockback Stick", Category.MELEE, 1, Resource.GOLD, 5, 5, true, BedWarsShopItem.item(Items.stick), "stick knockback i", "knockback stick"),
    CHAINMAIL_ARMOR("chainmail_armor", "Permanent Chainmail Armor", Category.ARMOR, 1, Resource.IRON, 24, 24, false, BedWarsShopItem.item((Item)Items.chainmail_boots), "permanent chainmail armor", "permanent chain armor"),
    IRON_ARMOR("iron_armor", "Permanent Iron Armor", Category.ARMOR, 1, Resource.GOLD, 12, 12, false, BedWarsShopItem.item((Item)Items.iron_boots), "permanent iron armor"),
    DIAMOND_ARMOR("diamond_armor", "Permanent Diamond Armor", Category.ARMOR, 1, Resource.EMERALD, 6, 6, false, BedWarsShopItem.item((Item)Items.diamond_boots), "permanent diamond armor"),
    SHEARS("shears", "Permanent Shears", Category.TOOLS, 1, Resource.IRON, 20, 20, false, BedWarsShopItem.item((Item)Items.shears), "permanent shears", "shears"),
    PICKAXE("pickaxe", "Pickaxe", Category.TOOLS, 1, "10i > 10i > 3g > 6g", BedWarsShopItem.item(Items.iron_pickaxe)),
    AXE("axe", "Axe", Category.TOOLS, 1, "10i > 10i > 3g > 6g", BedWarsShopItem.item(Items.iron_axe)),
    ARROWS("arrows", "Arrows", Category.RANGED, 6, Resource.GOLD, 2, 2, true, BedWarsShopItem.item(Items.arrow), "arrow", "arrows"),
    BOW("bow", "Bow", Category.RANGED, 1, Resource.GOLD, 12, 12, true, BedWarsShopItem.item((Item)Items.bow), "bow"),
    POWER_BOW("power_bow", "Bow (Power I)", Category.RANGED, 1, Resource.GOLD, 20, 20, true, BedWarsShopItem.enchantedBow(1, 0), "bow power i"),
    PUNCH_BOW("punch_bow", "Bow (Power I, Punch I)", Category.RANGED, 1, Resource.EMERALD, 6, 6, true, BedWarsShopItem.enchantedBow(1, 1), "bow power i punch i"),
    SPEED_POTION("speed_potion", "Speed II Potion", Category.POTIONS, 1, Resource.EMERALD, 1, 1, true, BedWarsShopItem.potion(8194), "speed ii potion 45 seconds", "speed potion"),
    JUMP_POTION("jump_potion", "Jump V Potion", Category.POTIONS, 1, Resource.EMERALD, 1, 1, true, BedWarsShopItem.potion(8203), "jump v potion 45 seconds", "jump boost v potion 45 seconds", "jump potion"),
    INVISIBILITY_POTION("invisibility_potion", "Invisibility Potion", Category.POTIONS, 1, Resource.EMERALD, 2, 2, true, BedWarsShopItem.potion(8206), "invisibility potion 30 seconds", "invisibility potion"),
    GOLDEN_APPLE("golden_apple", "Golden Apple", Category.UTILITY, 1, Resource.GOLD, 3, 3, true, BedWarsShopItem.item(Items.golden_apple), "golden apple"),
    BED_BUG("bed_bug", "Bed Bug", Category.UTILITY, 1, Resource.IRON, 24, 24, true, BedWarsShopItem.item(Items.snowball), "bed bug silverfish snowball", "bed bug", "bedbug"),
    DREAM_DEFENDER("dream_defender", "Dream Defender", Category.UTILITY, 1, Resource.IRON, 120, 120, true, BedWarsShopItem.item(Items.spawn_egg), "dream defender iron golem", "dream defender"),
    FIREBALL("fireball", "Fireball", Category.UTILITY, 1, Resource.IRON, 40, 40, true, BedWarsShopItem.item(Items.fire_charge), "fireball"),
    TNT("tnt", "TNT", Category.UTILITY, 1, Resource.GOLD, 4, 8, true, BedWarsShopItem.block(Blocks.tnt, 0), "tnt"),
    ENDER_PEARL("ender_pearl", "Ender Pearl", Category.UTILITY, 1, Resource.EMERALD, 4, 4, true, BedWarsShopItem.item(Items.ender_pearl), "ender pearl"),
    WATER_BUCKET("water_bucket", "Water Bucket", Category.UTILITY, 1, Resource.GOLD, 2, 3, true, BedWarsShopItem.item(Items.water_bucket), "water bucket"),
    BRIDGE_EGG("bridge_egg", "Bridge Egg", Category.UTILITY, 1, Resource.EMERALD, 1, 1, true, BedWarsShopItem.item(Items.egg), "bridge egg"),
    MAGIC_MILK("magic_milk", "Magic Milk", Category.UTILITY, 1, Resource.GOLD, 4, 4, true, BedWarsShopItem.item(Items.milk_bucket), "magic milk"),
    SPONGE("sponge", "Sponge", Category.UTILITY, 4, Resource.GOLD, 2, 3, true, BedWarsShopItem.block(Blocks.sponge, 0), "sponge"),
    POPUP_TOWER("popup_tower", "Compact Pop-up Tower", Category.UTILITY, 1, Resource.IRON, 24, 24, true, BedWarsShopItem.item(Items.chest_minecart), "compact pop up tower", "pop up tower", "popup tower");

    private final String id;
    private final String displayName;
    private final Category category;
    private final int outputQuantity;
    private final Resource resource;
    private final int soloCost;
    private final int teamCost;
    private final boolean repeatable;
    private final String variableCost;
    private final ItemStack icon;
    private final String[] aliases;

    private BedWarsShopItem(String string2, String string3, Category category2, int n2, Resource resource, int n3, int n4, boolean bl, ItemStack itemStack, String ... stringArray) {
        this.id = string2;
        this.displayName = string3;
        this.category = category2;
        this.outputQuantity = n2;
        this.resource = resource;
        this.soloCost = n3;
        this.teamCost = n4;
        this.repeatable = bl;
        this.variableCost = null;
        this.icon = itemStack;
        this.aliases = stringArray;
    }

    private BedWarsShopItem(String string2, String string3, Category category2, int n2, String string4, ItemStack itemStack) {
        this.id = string2;
        this.displayName = string3;
        this.category = category2;
        this.outputQuantity = n2;
        this.resource = null;
        this.soloCost = 0;
        this.teamCost = 0;
        this.repeatable = true;
        this.variableCost = string4;
        this.icon = itemStack;
        this.aliases = new String[]{string3};
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getOutputQuantity() {
        return this.outputQuantity;
    }

    public Resource getResource() {
        return this.resource;
    }

    public int getSoloCost() {
        return this.soloCost;
    }

    public int getTeamCost() {
        return this.teamCost;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    public ItemStack getIcon() {
        return this.icon.copy();
    }

    public String getPriceLabel() {
        if (this.variableCost != null) {
            return this.variableCost;
        }
        if (this.soloCost == this.teamCost) {
            return this.soloCost + " " + this.resource.getLabel();
        }
        return this.soloCost + " " + this.resource.getLabel() + " / " + this.teamCost + " " + this.resource.getLabel();
    }

    public String getCatalogName() {
        return this.outputQuantity > 1 ? this.displayName + " x" + this.outputQuantity : this.displayName;
    }

    public boolean matches(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        String string = BedWarsShopItem.normalize(itemStack.getDisplayName());
        if (this == PICKAXE) {
            return itemStack.getItem() instanceof ItemPickaxe || string.contains("pickaxe");
        }
        if (this == AXE) {
            return (itemStack.getItem() instanceof ItemAxe || string.endsWith(" axe")) && !string.contains("pickaxe");
        }
        if (this == BOW || this == POWER_BOW || this == PUNCH_BOW) {
            if (itemStack.getItem() != Items.bow && !string.startsWith("bow")) {
                return false;
            }
            int n = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.power.effectId, (ItemStack)itemStack);
            int n2 = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.punch.effectId, (ItemStack)itemStack);
            if (this == PUNCH_BOW) {
                return n2 > 0 || string.contains("punch");
            }
            if (this == POWER_BOW) {
                return n2 == 0 && (n > 0 || string.contains("power"));
            }
            return n == 0 && n2 == 0 && !string.contains("power") && !string.contains("punch");
        }
        for (String string2 : this.aliases) {
            if (!string.equals(BedWarsShopItem.normalize(string2))) continue;
            return true;
        }
        return false;
    }

    public int inventoryProgress(ItemStack[] ... itemStackArray) {
        int n = 0;
        ItemStack[][] itemStackArray2 = itemStackArray;
        int n2 = itemStackArray2.length;
        for (int i = 0; i < n2; ++i) {
            ItemStack[] itemStackArray3;
            for (ItemStack itemStack : itemStackArray3 = itemStackArray2[i]) {
                if (!this.matchesPurchasedStack(itemStack)) continue;
                if (this == PICKAXE || this == AXE) {
                    String string = ((ItemTool)itemStack.getItem()).getToolMaterialName();
                    int n3 = "EMERALD".equals(string) ? 4 : ("IRON".equals(string) ? 3 : ("STONE".equals(string) ? 2 : 1));
                    n = Math.max(n, n3);
                    continue;
                }
                n += itemStack.stackSize;
            }
        }
        return n;
    }

    private boolean matchesPurchasedStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.stackSize <= 0) {
            return false;
        }
        if (this == PICKAXE) {
            return itemStack.getItem() instanceof ItemPickaxe;
        }
        if (this == AXE) {
            return itemStack.getItem() instanceof ItemAxe;
        }
        if (this == BOW || this == POWER_BOW || this == PUNCH_BOW) {
            if (itemStack.getItem() != Items.bow) {
                return false;
            }
            int n = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.power.effectId, (ItemStack)itemStack);
            int n2 = EnchantmentHelper.getEnchantmentLevel((int)Enchantment.punch.effectId, (ItemStack)itemStack);
            return this == PUNCH_BOW ? n2 > 0 : (this == POWER_BOW ? n > 0 && n2 == 0 : n == 0 && n2 == 0);
        }
        if (this == BLAST_PROOF_GLASS) {
            return itemStack.getItem() == Item.getItemFromBlock((Block)Blocks.glass) || itemStack.getItem() == Item.getItemFromBlock((Block)Blocks.stained_glass);
        }
        if (this == SPEED_POTION || this == JUMP_POTION || this == INVISIBILITY_POTION) {
            if (!(itemStack.getItem() instanceof ItemPotion)) {
                return false;
            }
            int n = this == SPEED_POTION ? Potion.moveSpeed.id : (this == JUMP_POTION ? Potion.jump.id : Potion.invisibility.id);
            List list = ((ItemPotion)itemStack.getItem()).getEffects(itemStack);
            if (list != null) {
                for (PotionEffect potionEffect : list) {
                    if (potionEffect.getPotionID() != n) continue;
                    return true;
                }
            }
            return false;
        }
        return itemStack.getItem() == this.icon.getItem();
    }

    public static BedWarsShopItem byId(String string) {
        if (string == null) {
            return null;
        }
        for (BedWarsShopItem bedWarsShopItem : BedWarsShopItem.values()) {
            if (!bedWarsShopItem.id.equalsIgnoreCase(string)) continue;
            return bedWarsShopItem;
        }
        return null;
    }

    public static List<BedWarsShopItem> inCategory(Category category2) {
        ArrayList<BedWarsShopItem> arrayList = new ArrayList<BedWarsShopItem>();
        for (BedWarsShopItem bedWarsShopItem : BedWarsShopItem.values()) {
            if (bedWarsShopItem.category != category2) continue;
            arrayList.add(bedWarsShopItem);
        }
        return Collections.unmodifiableList(arrayList);
    }

    public static boolean isUnavailable(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound nBTTagCompound = itemStack.getTagCompound().getCompoundTag("display");
        if (!nBTTagCompound.hasKey("Lore", 9)) {
            return false;
        }
        NBTTagList nBTTagList = nBTTagCompound.getTagList("Lore", 8);
        for (int i = 0; i < nBTTagList.tagCount(); ++i) {
            String string = BedWarsShopItem.normalize(nBTTagList.getStringTagAt(i));
            if (!string.contains("you don t have enough") && !string.contains("not enough") && !string.contains("unlocked") && !string.contains("maxed")) continue;
            return true;
        }
        return false;
    }

    public static String normalize(String string) {
        String string2 = EnumChatFormatting.getTextWithoutFormattingCodes((String)(string == null ? "" : string));
        return string2.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static ItemStack item(Item item) {
        return new ItemStack(item);
    }

    private static ItemStack block(Block block, int n) {
        return new ItemStack(block, 1, n);
    }

    private static ItemStack potion(int n) {
        return new ItemStack((Item)Items.potionitem, 1, n);
    }

    private static ItemStack enchantedBow(int n, int n2) {
        ItemStack itemStack = BedWarsShopItem.item((Item)Items.bow);
        if (n > 0) {
            itemStack.addEnchantment(Enchantment.power, n);
        }
        if (n2 > 0) {
            itemStack.addEnchantment(Enchantment.punch, n2);
        }
        return itemStack;
    }

    public static enum Resource {
        IRON("Iron"),
        GOLD("Gold"),
        EMERALD("Emerald");

        private final String label;

        private Resource(String string2) {
            this.label = string2;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static enum Category {
        BLOCKS("Blocks"),
        MELEE("Melee"),
        ARMOR("Armor"),
        TOOLS("Tools"),
        RANGED("Ranged"),
        POTIONS("Potions"),
        UTILITY("Utility");

        private final String label;

        private Category(String string2) {
            this.label = string2;
        }

        public String getLabel() {
            return this.label;
        }
    }
}

