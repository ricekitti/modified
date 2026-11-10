package dev.rice.quickbuy;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;import net.minecraft.inventory.IInventory;import net.minecraft.item.ItemStack;
public final class QuickBuyShopMatcher {
private static final Minecraft mc=Minecraft.getMinecraft();
public static ContainerChest currentItemShop() {
        if (!(mc.currentScreen instanceof GuiChest) || !(mc.thePlayer.openContainer instanceof ContainerChest)) {
            return null;
        }
        ContainerChest containerChest = (ContainerChest)mc.thePlayer.openContainer;
        IInventory iInventory = containerChest.getLowerChestInventory();
        if (iInventory == null || iInventory.getSizeInventory() < 45) {
            return null;
        }
        String string = BedWarsShopItem.normalize(iInventory.getDisplayName().getUnformattedText());
        return string.equals("item shop") || string.endsWith(" item shop") || string.equals("quick buy") ? containerChest : null;
    }
public static int findItemSlot(ContainerChest containerChest, BedWarsShopItem bedWarsShopItem) {
        IInventory iInventory = containerChest.getLowerChestInventory();
        int n = Math.min(45, iInventory.getSizeInventory());
        for (int i = 18; i < n; ++i) {
            if (i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || !bedWarsShopItem.matches(iInventory.getStackInSlot(i))) continue;
            return i;
        }
        return -1;
    }
public static int findCategorySlot(ContainerChest containerChest, BedWarsShopItem.Category category2) {
        IInventory iInventory = containerChest.getLowerChestInventory();
        int n = Math.min(9, iInventory.getSizeInventory());
        String string = BedWarsShopItem.normalize(category2.getLabel());
        for (int i = 0; i < n; ++i) {
            ItemStack itemStack = iInventory.getStackInSlot(i);
            if (itemStack == null || !BedWarsShopItem.normalize(itemStack.getDisplayName()).equals(string)) continue;
            return i;
        }
        return -1;
    }
}
