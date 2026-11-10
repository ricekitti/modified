package dev.rice.quickbuy;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public final class QuickBuyPresetScreen
extends GuiScreen {
    private static final int PANEL_WIDTH = 620;
    private static final int PANEL_HEIGHT = 360;
    private static final int MARGIN = 12;
    private static final int HEADER_HEIGHT = 34;
    private static final int CATEGORY_HEIGHT = 24;
    private static final int CARD_HEIGHT = 38;
    private static final int CARD_GAP = 5;
    private static final int FOOTER_HEIGHT = 34;
    private final QuickBuyPresetsSetting setting;
    private final GuiScreen parent;
    private BedWarsShopItem.Category category = BedWarsShopItem.Category.BLOCKS;
    private int scrollRow;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int gridTop;
    private int gridBottom;
    private int columnWidth;

    public QuickBuyPresetScreen(QuickBuyPresetsSetting quickBuyPresetsSetting, GuiScreen guiScreen) {
        this.setting = quickBuyPresetsSetting;
        this.parent = guiScreen;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents((boolean)true);
        this.updateLayout();
        this.clampScroll();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents((boolean)false);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void drawScreen(int n, int n2, float f) {
        this.drawDefaultBackground();
        this.updateLayout();
        this.clampScroll();
        QuickBuyUi.surface(this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, QuickBuyUi.PANEL_BACKGROUND);
        QuickBuyUi.fill(this.panelLeft + 1, this.panelTop + 1, this.panelRight - 1, this.panelBottom - 1, 11.0f, QuickBuyUi.SIDEBAR_BACKGROUND);
        this.fontRendererObj.drawString("Quick Buy Presets", this.panelLeft + 12, this.panelTop + 13, QuickBuyUi.TEXT_PRIMARY);
        this.drawPresetSelector(n, n2);
        this.drawPill("Done", this.panelRight - 54, this.panelTop + 8, this.panelRight - 12, this.panelTop + 26, QuickBuyPresetScreen.inside(n, n2, this.panelRight - 54, this.panelTop + 8, this.panelRight - 12, this.panelTop + 26), false);
        this.drawCategories(n, n2);
        QuickBuyUi.surface(this.panelLeft + 12, this.gridTop - 5, this.panelRight - 12, this.gridBottom + 5, QuickBuyUi.SURFACE_ALT);
        this.drawItems(n, n2);
        this.drawFooter(n, n2);
        super.drawScreen(n, n2, f);
    }

    private void drawPresetSelector(int n, int n2) {
        int n3 = (this.panelLeft + this.panelRight) / 2;
        int n4 = this.panelTop + 8;
        int n5 = this.panelTop + 26;
        this.drawPill("<", n3 - 62, n4, n3 - 42, n5, QuickBuyPresetScreen.inside(n, n2, n3 - 62, n4, n3 - 42, n5), false);
        this.drawPill(this.setting.getActivePresetName(), n3 - 38, n4, n3 + 38, n5, false, true);
        this.drawPill(">", n3 + 42, n4, n3 + 62, n5, QuickBuyPresetScreen.inside(n, n2, n3 + 42, n4, n3 + 62, n5), false);
    }

    private void drawCategories(int n, int n2) {
        int n3 = this.panelTop + 34;
        int n4 = this.panelRight - this.panelLeft - 24;
        BedWarsShopItem.Category[] categoryArray = BedWarsShopItem.Category.values();
        int n5 = n4 / categoryArray.length;
        for (int i = 0; i < categoryArray.length; ++i) {
            int n6 = this.panelLeft + 12 + i * n5;
            int n7 = i == categoryArray.length - 1 ? this.panelRight - 12 : n6 + n5 - 3;
            boolean bl = this.category == categoryArray[i];
            boolean bl2 = QuickBuyPresetScreen.inside(n, n2, n6, n3 + 2, n7, n3 + 24 - 2);
            this.drawPill(categoryArray[i].getLabel(), n6, n3 + 2, n7, n3 + 24 - 2, bl2, bl);
        }
    }

    private void drawItems(int n, int n2) {
        int n3;
        int n4;
        List<BedWarsShopItem> list = BedWarsShopItem.inCategory(this.category);
        int n5 = this.scrollRow * 2;
        int n6 = this.visibleRows();
        this.pushScissor(this.panelLeft + 12 + 3, this.gridTop, this.panelRight - this.panelLeft - 24 - 6, this.gridBottom - this.gridTop);
        for (int i = n5; i < list.size() && (n4 = (n3 = i - n5) / 2) < n6; ++i) {
            int n7 = n3 % 2;
            int n8 = this.panelLeft + 12 + 5 + n7 * (this.columnWidth + 5);
            int n9 = n8 + this.columnWidth;
            int n10 = this.gridTop + n4 * 43;
            this.drawItem(list.get(i), n8, n10, n9, n10 + 38, n, n2);
        }
        this.popScissor();
    }

    private void drawItem(BedWarsShopItem bedWarsShopItem, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7 = this.setting.getCount(this.setting.getActivePreset(), bedWarsShopItem);
        boolean bl = QuickBuyPresetScreen.inside(n5, n6, n, n2, n3, n4);
        int n8 = n7 > 0 ? QuickBuyUi.ACCENT_SOFT : (bl ? QuickBuyUi.SURFACE_HOVER : QuickBuyUi.SURFACE);
        QuickBuyUi.surface(n, n2, n3, n4, n8);
        this.drawItemIcon(bedWarsShopItem.getIcon(), n + 8, n2 + 10);
        this.fontRendererObj.drawString(this.fit(bedWarsShopItem.getCatalogName(), n3 - n - 105), n + 30, n2 + 8, QuickBuyUi.TEXT_PRIMARY);
        this.fontRendererObj.drawString(this.fit(bedWarsShopItem.getPriceLabel(), n3 - n - 105), n + 30, n2 + 21, QuickBuyUi.TEXT_MUTED);
        int n9 = n3 - 23;
        int n10 = n3 - 63;
        this.drawSmall("-", n10, n2 + 11, n10 + 18, n2 + 29, n5, n6, n7 > 0);
        String string = Integer.toString(n7);
        this.fontRendererObj.drawString(string, n3 - 36 - this.fontRendererObj.getStringWidth(string) / 2, n2 + 16, n7 > 0 ? QuickBuyUi.TEXT_PRIMARY : QuickBuyUi.TEXT_DISABLED);
        this.drawSmall("+", n9, n2 + 11, n9 + 18, n2 + 29, n5, n6, n7 == 0 || bedWarsShopItem.isRepeatable());
    }

    private void drawFooter(int n, int n2) {
        int n3 = this.gridBottom + 9;
        this.drawPill("Clear", this.panelLeft + 12, n3, this.panelLeft + 12 + 46, n3 + 18, QuickBuyPresetScreen.inside(n, n2, this.panelLeft + 12, n3, this.panelLeft + 12 + 46, n3 + 18), false);
        String string = this.totals();
        this.fontRendererObj.drawString(string, this.panelRight - 12 - this.fontRendererObj.getStringWidth(string), n3 + 5, QuickBuyUi.TEXT_SECONDARY);
    }

    private String totals() {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        boolean bl = false;
        for (QuickBuyPresetsSetting.Purchase purchase : this.setting.getPreset(this.setting.getActivePreset())) {
            BedWarsShopItem bedWarsShopItem = BedWarsShopItem.byId(purchase.getItemId());
            if (bedWarsShopItem == null) continue;
            if (bedWarsShopItem.getResource() == null) {
                bl = true;
                continue;
            }
            if (bedWarsShopItem.getResource() == BedWarsShopItem.Resource.IRON) {
                n += bedWarsShopItem.getSoloCost() * purchase.getCount();
                continue;
            }
            if (bedWarsShopItem.getResource() == BedWarsShopItem.Resource.GOLD) {
                n2 += bedWarsShopItem.getSoloCost() * purchase.getCount();
                continue;
            }
            n3 += bedWarsShopItem.getSoloCost() * purchase.getCount();
        }
        StringBuilder stringBuilder = new StringBuilder("Solo: ");
        this.appendCost(stringBuilder, n, "i");
        this.appendCost(stringBuilder, n2, "g");
        this.appendCost(stringBuilder, n3, "e");
        if (bl) {
            if (stringBuilder.length() > 6) {
                stringBuilder.append("  ");
            }
            stringBuilder.append("tools");
        }
        return stringBuilder.length() == 6 ? "Solo: Free" : stringBuilder.toString();
    }

    private void appendCost(StringBuilder stringBuilder, int n, String string) {
        if (n <= 0) {
            return;
        }
        if (stringBuilder.length() > 6) {
            stringBuilder.append("  ");
        }
        stringBuilder.append(n).append(string);
    }

    private void drawItemIcon(ItemStack itemStack, int n, int n2) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        this.itemRender.renderItemAndEffectIntoGUI(itemStack, n, n2);
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void drawPill(String string, int n, int n2, int n3, int n4, boolean bl, boolean bl2) {
        int n5;
        int n6 = bl2 ? QuickBuyUi.ACCENT_SOFT : (n5 = bl ? QuickBuyUi.SURFACE_HOVER : QuickBuyUi.SURFACE);
        int n7 = bl2 ? QuickBuyUi.TEXT_PRIMARY : (bl ? QuickBuyUi.TEXT_SECONDARY : QuickBuyUi.TEXT_MUTED);
        QuickBuyUi.capsule(n, n2, n3, n4, n6);
        this.fontRendererObj.drawString(string, n + (n3 - n - this.fontRendererObj.getStringWidth(string)) / 2, n2 + (n4 - n2 - this.fontRendererObj.FONT_HEIGHT) / 2 + 1, n7);
    }

    private void drawSmall(String string, int n, int n2, int n3, int n4, int n5, int n6, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = bl && QuickBuyPresetScreen.inside(n5, n6, n, n2, n3, n4);
        int n7 = bl ? (bl2 ? QuickBuyUi.SURFACE_ACTIVE : QuickBuyUi.TRACK) : QuickBuyUi.DISABLED_SURFACE;
        int n8 = bl ? QuickBuyUi.TEXT_SECONDARY : QuickBuyUi.TEXT_DISABLED;
        QuickBuyUi.capsule(n, n2, n3, n4, n7);
        this.fontRendererObj.drawString(string, n + (n3 - n - this.fontRendererObj.getStringWidth(string)) / 2, n2 + 5, n8);
    }

    protected void mouseClicked(int n, int n2, int n3) throws IOException {
        int n4;
        int n5;
        if (n3 != 0) {
            return;
        }
        this.updateLayout();
        int n6 = (this.panelLeft + this.panelRight) / 2;
        if (QuickBuyPresetScreen.inside(n, n2, this.panelRight - 54, this.panelTop + 8, this.panelRight - 12, this.panelTop + 26)) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        if (QuickBuyPresetScreen.inside(n, n2, n6 - 62, this.panelTop + 8, n6 - 42, this.panelTop + 26)) {
            this.cyclePreset(-1);
            return;
        }
        if (QuickBuyPresetScreen.inside(n, n2, n6 + 42, this.panelTop + 8, n6 + 62, this.panelTop + 26)) {
            this.cyclePreset(1);
            return;
        }
        int n7 = this.panelTop + 34;
        BedWarsShopItem.Category[] categoryArray = BedWarsShopItem.Category.values();
        int n8 = (this.panelRight - this.panelLeft - 24) / categoryArray.length;
        for (n5 = 0; n5 < categoryArray.length; ++n5) {
            int n9 = this.panelLeft + 12 + n5 * n8;
            int n10 = n4 = n5 == categoryArray.length - 1 ? this.panelRight - 12 : n9 + n8 - 3;
            if (!QuickBuyPresetScreen.inside(n, n2, n9, n7 + 2, n4, n7 + 24 - 2)) continue;
            this.category = categoryArray[n5];
            this.scrollRow = 0;
            return;
        }
        n5 = this.gridBottom + 9;
        if (QuickBuyPresetScreen.inside(n, n2, this.panelLeft + 12, n5, this.panelLeft + 12 + 46, n5 + 18)) {
            this.setting.clear(this.setting.getActivePreset());
            return;
        }
        BedWarsShopItem bedWarsShopItem = this.itemAt(n, n2);
        if (bedWarsShopItem == null) {
            return;
        }
        n4 = this.cardRight(n);
        int n11 = this.cardTop(n2);
        int n12 = this.setting.getCount(this.setting.getActivePreset(), bedWarsShopItem);
        if (QuickBuyPresetScreen.inside(n, n2, n4 - 63, n11 + 11, n4 - 45, n11 + 29)) {
            this.setting.decrement(this.setting.getActivePreset(), bedWarsShopItem);
        } else if (QuickBuyPresetScreen.inside(n, n2, n4 - 23, n11 + 11, n4 - 5, n11 + 29) && (n12 == 0 || bedWarsShopItem.isRepeatable())) {
            this.setting.add(this.setting.getActivePreset(), bedWarsShopItem);
        } else {
            this.setting.add(this.setting.getActivePreset(), bedWarsShopItem);
        }
    }

    protected void keyTyped(char c, int n) throws IOException {
        if (n == 1) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        super.keyTyped(c, n);
    }

    public void handleMouseInput() throws IOException {
        int n;
        super.handleMouseInput();
        int n2 = Mouse.getEventDWheel();
        if (n2 == 0) {
            return;
        }
        int n3 = Mouse.getEventX() * this.width / this.mc.displayWidth;
        if (QuickBuyPresetScreen.inside(n3, n = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1, this.panelLeft + 12, this.gridTop, this.panelRight - 12, this.gridBottom)) {
            this.scrollRow += n2 > 0 ? -1 : 1;
            this.clampScroll();
        }
    }

    private BedWarsShopItem itemAt(int n, int n2) {
        int n3;
        if (!QuickBuyPresetScreen.inside(n, n2, this.panelLeft + 12 + 5, this.gridTop, this.panelRight - 12 - 5, this.gridBottom)) {
            return null;
        }
        int n4 = n < (n3 = this.panelLeft + 12 + 5) + this.columnWidth + 5 ? 0 : 1;
        int n5 = n3 + n4 * (this.columnWidth + 5);
        if (n < n5 || n > n5 + this.columnWidth) {
            return null;
        }
        int n6 = (n2 - this.gridTop) / 43;
        int n7 = this.gridTop + n6 * 43;
        if (n2 > n7 + 38) {
            return null;
        }
        int n8 = (this.scrollRow + n6) * 2 + n4;
        List<BedWarsShopItem> list = BedWarsShopItem.inCategory(this.category);
        return n8 >= 0 && n8 < list.size() ? list.get(n8) : null;
    }

    private int cardRight(int n) {
        int n2 = this.panelLeft + 12 + 5;
        return n < n2 + this.columnWidth + 5 ? n2 + this.columnWidth : n2 + this.columnWidth + 5 + this.columnWidth;
    }

    private int cardTop(int n) {
        int n2 = (n - this.gridTop) / 43;
        return this.gridTop + n2 * 43;
    }

    private void cyclePreset(int n) {
        int n2 = this.setting.getActivePreset() + n;
        if (n2 < 0) {
            n2 = 4;
        } else if (n2 >= 5) {
            n2 = 0;
        }
        this.setting.setActivePreset(n2);
    }

    private void updateLayout() {
        int n = Math.min(620, this.width - 20);
        int n2 = Math.min(360, this.height - 20);
        this.panelLeft = (this.width - n) / 2;
        this.panelTop = (this.height - n2) / 2;
        this.panelRight = this.panelLeft + n;
        this.panelBottom = this.panelTop + n2;
        this.gridTop = this.panelTop + 34 + 24 + 10;
        this.gridBottom = this.panelBottom - 34 - 8;
        this.columnWidth = (n - 24 - 5 - 10) / 2;
    }

    private int visibleRows() {
        return Math.max(1, (this.gridBottom - this.gridTop + 5) / 43);
    }

    private void clampScroll() {
        int n = (BedWarsShopItem.inCategory(this.category).size() + 1) / 2;
        this.scrollRow = QuickBuyPresetScreen.clamp(this.scrollRow, 0, Math.max(0, n - this.visibleRows()));
    }

    private String fit(String string, int n) {
        if (this.fontRendererObj.getStringWidth(string) <= n) {
            return string;
        }
        String string2 = "...";
        int n2 = Math.max(0, n - this.fontRendererObj.getStringWidth(string2));
        String string3 = string;
        while (!string3.isEmpty() && this.fontRendererObj.getStringWidth(string3) > n2) {
            string3 = string3.substring(0, string3.length() - 1);
        }
        return string3 + string2;
    }

    private void pushScissor(int n, int n2, int n3, int n4) {
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        int n5 = scaledResolution.getScaleFactor();
        GL11.glEnable((int)3089);
        GL11.glScissor((int)(n * n5), (int)(this.mc.displayHeight - (n2 + n4) * n5), (int)(n3 * n5), (int)(n4 * n5));
    }

    private void popScissor() {
        GL11.glDisable((int)3089);
    }

    private static int clamp(int n, int n2, int n3) {
        return Math.max(n2, Math.min(n3, n));
    }

    private static boolean inside(int n, int n2, int n3, int n4, int n5, int n6) {
        return n >= n3 && n <= n5 && n2 >= n4 && n2 <= n6;
    }
}

