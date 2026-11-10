/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.gui.inventory.GuiChest
 *  net.minecraft.inventory.ContainerChest
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.inventory.Slot
 *  net.minecraft.item.ItemStack
 *  org.lwjgl.input.Mouse
 *  valen.module.impl.minigames.quickbuy.PurchaseConfirmation
 *  valen.module.impl.minigames.quickbuy.PurchaseConfirmation$Status
 *  valen.module.impl.minigames.quickbuy.QuickBuyPresetsSetting$Purchase
 */
package valen.module.impl.minigames;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.mixin.impl.accessor.IAccessorGuiContainer;
import valen.module.Module;
import valen.module.impl.minigames.quickbuy.BedWarsShopItem;
import valen.module.impl.minigames.quickbuy.PurchaseConfirmation;
import valen.module.impl.minigames.quickbuy.QuickBuyPresetScreen;
import valen.module.impl.minigames.quickbuy.QuickBuyPresetsSetting;
import valen.module.setting.impl.ButtonSetting;
import valen.module.setting.impl.KeySetting;
import valen.utility.Utils;

public final class AutoQuickBuy
extends Module {
    private static final long OPEN_DELAY_MS = 250L;
    private static final long CLICK_DELAY_MS = 125L;
    private static final long CATEGORY_DELAY_MS = 175L;
    private static final long CATEGORY_TIMEOUT_MS = 1400L;
    private static final long PURCHASE_TIMEOUT_MS = 3000L;
    private final QuickBuyPresetsSetting presets;
    private final KeySetting cyclePresetKey;
    private List<QuickBuyPresetsSetting.Purchase> queue = new ArrayList<QuickBuyPresetsSetting.Purchase>();
    private boolean cycleKeyHeld;
    private boolean inShop;
    private int windowId = -1;
    private int queueIndex;
    private int remaining;
    private boolean navigated;
    private long navigationDeadline;
    private long nextActionAt;
    private PurchaseConfirmation pendingPurchase;
    private BedWarsShopItem pendingItem;

    public AutoQuickBuy() {
        super("Auto Quick Buy", Module.category.minigames);
        this.presets = new QuickBuyPresetsSetting();
        this.registerSetting(this.presets);
        this.cyclePresetKey = new KeySetting("Cycle preset", 0);
        this.registerSetting(this.cyclePresetKey);
        this.registerSetting(new ButtonSetting("Edit preset", this::openEditor));
        this.closetModule = true;
    }

    @Override
    public String getInfo() {
        return this.presets.getActivePresetName();
    }

    @Override
    public void onEnable() {
        this.resetSession();
    }

    @Override
    public void onDisable() {
        this.resetSession();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        if (!Utils.nullCheck()) {
            this.resetSession();
            return;
        }
        this.updateCycleKey();
        ContainerChest containerChest = this.currentItemShop();
        if (containerChest == null) {
            this.inShop = false;
            this.clearQueue();
            return;
        }
        if (!this.inShop || this.windowId != containerChest.windowId) {
            this.beginSession(containerChest.windowId);
        }
        this.inShop = true;
        long l = System.nanoTime() / 1000000L;
        if (this.pendingPurchase != null) {
            this.checkPendingPurchase(l);
            return;
        }
        if (this.queueIndex >= this.queue.size() || l < this.nextActionAt) {
            return;
        }
        QuickBuyPresetsSetting.Purchase purchase = this.queue.get(this.queueIndex);
        BedWarsShopItem bedWarsShopItem = BedWarsShopItem.byId(purchase.getItemId());
        if (bedWarsShopItem == null) {
            this.advance(l);
            return;
        }
        int n = this.findItemSlot(containerChest, bedWarsShopItem);
        if (n >= 0) {
            ItemStack itemStack = containerChest.getLowerChestInventory().getStackInSlot(n);
            if (BedWarsShopItem.isUnavailable(itemStack)) {
                this.advance(l);
                return;
            }
            this.pendingItem = bedWarsShopItem;
            this.pendingPurchase = new PurchaseConfirmation(this.inventoryProgress(bedWarsShopItem), l, 3000L);
            if (!this.clickSlotCenter(containerChest, n)) {
                this.pendingItem = null;
                this.pendingPurchase = null;
                this.advance(l);
            }
            return;
        }
        if (!this.navigated) {
            int n2 = this.findCategorySlot(containerChest, bedWarsShopItem.getCategory());
            if (n2 < 0) {
                this.advance(l);
                return;
            }
            this.clickSlotCenter(containerChest, n2);
            this.navigated = true;
            this.navigationDeadline = l + 1400L;
            this.nextActionAt = l + 175L;
            return;
        }
        if (l >= this.navigationDeadline) {
            this.advance(l);
        } else {
            this.nextActionAt = l + 50L;
        }
    }

    private int inventoryProgress(BedWarsShopItem bedWarsShopItem) {
        return bedWarsShopItem.inventoryProgress(AutoQuickBuy.mc.thePlayer.inventory.mainInventory, AutoQuickBuy.mc.thePlayer.inventory.armorInventory);
    }

    private void checkPendingPurchase(long l) {
        PurchaseConfirmation.Status status = this.pendingPurchase.status(this.inventoryProgress(this.pendingItem), l);
        if (status == PurchaseConfirmation.Status.WAITING) {
            return;
        }
        if (status == PurchaseConfirmation.Status.TIMED_OUT) {
            this.clearQueue();
            Utils.sendMessage("&cAuto Quick Buy: purchase not confirmed. Reopen the shop to retry.");
            return;
        }
        this.pendingPurchase = null;
        this.pendingItem = null;
        --this.remaining;
        if (this.remaining <= 0) {
            this.advance(l);
        } else {
            this.nextActionAt = l + 125L;
        }
    }

    private void openEditor() {
        GuiScreen guiScreen = AutoQuickBuy.mc.currentScreen;
        mc.displayGuiScreen((GuiScreen)new QuickBuyPresetScreen(this.presets, guiScreen));
    }

    private void updateCycleKey() {
        boolean bl = this.cyclePresetKey.isPressed();
        if (bl && !this.cycleKeyHeld && AutoQuickBuy.mc.currentScreen == null) {
            this.presets.setActivePreset((this.presets.getActivePreset() + 1) % 5);
        }
        this.cycleKeyHeld = bl;
    }

    private ContainerChest currentItemShop() {
        if (!(AutoQuickBuy.mc.currentScreen instanceof GuiChest) || !(AutoQuickBuy.mc.thePlayer.openContainer instanceof ContainerChest)) {
            return null;
        }
        ContainerChest containerChest = (ContainerChest)AutoQuickBuy.mc.thePlayer.openContainer;
        IInventory iInventory = containerChest.getLowerChestInventory();
        if (iInventory == null || iInventory.getSizeInventory() < 45) {
            return null;
        }
        String string = BedWarsShopItem.normalize(iInventory.getDisplayName().getUnformattedText());
        return string.equals("item shop") || string.endsWith(" item shop") || string.equals("quick buy") ? containerChest : null;
    }

    private void beginSession(int n) {
        this.windowId = n;
        this.queue = this.presets.snapshotActivePreset();
        this.queueIndex = 0;
        this.remaining = this.queue.isEmpty() ? 0 : this.queue.get(0).getCount();
        this.navigated = false;
        this.navigationDeadline = 0L;
        this.pendingPurchase = null;
        this.pendingItem = null;
        this.nextActionAt = System.nanoTime() / 1000000L + 250L;
    }

    private void advance(long l) {
        ++this.queueIndex;
        this.remaining = this.queueIndex < this.queue.size() ? this.queue.get(this.queueIndex).getCount() : 0;
        this.navigated = false;
        this.navigationDeadline = 0L;
        this.nextActionAt = l + 125L;
    }

    private int findItemSlot(ContainerChest containerChest, BedWarsShopItem bedWarsShopItem) {
        IInventory iInventory = containerChest.getLowerChestInventory();
        int n = Math.min(45, iInventory.getSizeInventory());
        for (int i = 18; i < n; ++i) {
            if (i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || !bedWarsShopItem.matches(iInventory.getStackInSlot(i))) continue;
            return i;
        }
        return -1;
    }

    private int findCategorySlot(ContainerChest containerChest, BedWarsShopItem.Category category2) {
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

    private boolean clickSlotCenter(ContainerChest containerChest, int n) {
        if (!(AutoQuickBuy.mc.currentScreen instanceof GuiChest) || AutoQuickBuy.mc.thePlayer.openContainer != containerChest) {
            return false;
        }
        Slot slot = containerChest.getSlot(n);
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int n2 = containerChest.getLowerChestInventory().getSizeInventory() / 9;
        int n3 = (scaledResolution.getScaledWidth() - 176) / 2;
        int n4 = (scaledResolution.getScaledHeight() - (114 + n2 * 18)) / 2;
        int n5 = n3 + slot.xDisplayPosition + 8;
        int n6 = n4 + slot.yDisplayPosition + 8;
        int n7 = n5 * AutoQuickBuy.mc.displayWidth / scaledResolution.getScaledWidth();
        int n8 = AutoQuickBuy.mc.displayHeight - n6 * AutoQuickBuy.mc.displayHeight / scaledResolution.getScaledHeight() - 1;
        Mouse.setCursorPosition((int)n7, (int)n8);
        ((IAccessorGuiContainer)AutoQuickBuy.mc.currentScreen).valen$mouseClicked(n5, n6, 0);
        return true;
    }

    private void resetSession() {
        this.inShop = false;
        this.cycleKeyHeld = false;
        this.windowId = -1;
        this.clearQueue();
    }

    private void clearQueue() {
        this.queue = new ArrayList<QuickBuyPresetsSetting.Purchase>();
        this.queueIndex = 0;
        this.remaining = 0;
        this.navigated = false;
        this.navigationDeadline = 0L;
        this.nextActionAt = 0L;
        this.pendingPurchase = null;
        this.pendingItem = null;
    }
}

