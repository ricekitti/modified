/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.item.ItemStack
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 *  valen.utility.ItemSearchIndex
 *  valen.utility.ItemSearchIndex$ItemEntry
 */
package valen.clickgui.components.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.components.impl.AbstractItemSearchComponent;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.render.Shape;
import valen.module.impl.client.Gui;
import valen.module.setting.impl.InventoryItemListSetting;
import valen.utility.ItemSearchIndex;
import valen.utility.font.ValenFontRenderer;

public class InventoryItemSearchComponent
extends AbstractItemSearchComponent<InventoryItemListSetting> {
    private static final float SLOT_PILL_MIN_WIDTH = 9.0f;
    private static final float SLOT_PILL_HORIZONTAL_PAD = 3.0f;
    private static final float SLOT_BOX_GAP = 3.0f;
    private static final float DRAG_SCROLL_EDGE = 10.0f;
    private static final float DRAG_SCROLL_SPEED = 3.0f;
    private List<InventorySelectedRowData> selectedRowsCache;
    private String selectedRowsCacheKey;
    private String listeningStorageId;
    private String draggingStorageId;
    private float dragGrabOffsetY;

    public InventoryItemSearchComponent(InventoryItemListSetting inventoryItemListSetting, ModuleComponent moduleComponent, float f) {
        super(inventoryItemListSetting, moduleComponent, f);
    }

    @Override
    protected int getSelectedEntryCount() {
        return ((InventoryItemListSetting)this.setting).getItems().size();
    }

    @Override
    public void drawScreen(int n, int n2) {
        super.drawScreen(n, n2);
        this.updateDragState();
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
        if (n3 == 0) {
            this.draggingStorageId = null;
        }
    }

    @Override
    public void keyTyped(char c, int n) {
        if (!this.moduleComponent.isOpened) {
            return;
        }
        if (this.listeningStorageId != null) {
            if (n == 1) {
                this.listeningStorageId = null;
                return;
            }
            int n2 = this.getHotbarSlotForKey(n);
            if (n2 != -1) {
                ((InventoryItemListSetting)this.setting).setAssignedSlot(this.listeningStorageId, n2);
                this.listeningStorageId = null;
                this.invalidateSelectedRows();
                this.markUnsaved();
            }
            return;
        }
        super.keyTyped(c, n);
    }

    @Override
    protected boolean hasAdditionalTextInputFocus() {
        return this.listeningStorageId != null;
    }

    @Override
    protected void clearAdditionalTextInputFocus() {
        this.listeningStorageId = null;
    }

    @Override
    protected void onDropdownClickHandled(int n, int n2) {
        this.listeningStorageId = null;
    }

    @Override
    protected void onSearchFocusHandled(int n, int n2) {
        this.listeningStorageId = null;
    }

    @Override
    protected void onOutsideClick(int n, int n2, int n3) {
        this.listeningStorageId = null;
    }

    @Override
    protected void renderSelectedRows(AbstractTextInputComponent.Layout layout, float f, int n, int n2) {
        List<String> list = ((InventoryItemListSetting)this.setting).getItems();
        String string = this.selectedRowsCacheKey(list);
        if (this.selectedRowsCache == null || !string.equals(this.selectedRowsCacheKey)) {
            this.selectedRowsCache = new ArrayList<InventorySelectedRowData>();
            for (String object : list) {
                List list2 = ItemSearchIndex.isGroupedSelection((String)object) ? ItemSearchIndex.getSelectionVariants((String)object) : null;
                ArrayList<ItemStack> n3 = null;
                if (list2 != null && !list2.isEmpty()) {
                    n3 = new ArrayList<ItemStack>();
                    for (ItemSearchIndex.ItemEntry itemEntry : list2) {
                        n3.add(itemEntry.toItemStack());
                    }
                }
                this.selectedRowsCache.add(new InventorySelectedRowData(object, ItemSearchIndex.getDisplayName((String)object), ItemSearchIndex.getItemStack((String)object), n3, ((InventoryItemListSetting)this.setting).getAssignedSlot(object)));
            }
            this.selectedRowsCacheKey = string;
        }
        for (int i = n; i < n2; ++i) {
            InventorySelectedRowData inventorySelectedRowData = this.selectedRowsCache.get(i);
            float f2 = this.getSelectedTop(layout) - f + (float)i * 20.0f;
            int n3 = inventorySelectedRowData.storageId.equals(this.draggingStorageId) ? ClickGuiStyle.SURFACE_HOVER : this.getListRowBackground(i, false);
            this.renderSelectedRow(inventorySelectedRowData, layout.left, layout.right, f2, n3);
        }
    }

    @Override
    protected boolean handleSelectedEntryClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        int n3 = this.getSelectedRowIndex(n, n2, layout);
        if (n3 < 0) {
            this.draggingStorageId = null;
            return false;
        }
        String string = ((InventoryItemListSetting)this.setting).getItems().get(n3);
        float f = this.getSelectedTop(layout) - this.selectedScrollAnim.getValue() + (float)n3 * 20.0f;
        if (this.isOverClose(n, n2, f, layout.right)) {
            ((InventoryItemListSetting)this.setting).removeItem(string);
            this.invalidateSelectedRows();
            this.listeningStorageId = null;
            this.draggingStorageId = null;
            this.markUnsaved();
            this.clampSelectedScroll();
            this.updateDropdownAnimation();
            this.moduleComponent.updateSettingPositions();
            return true;
        }
        if (this.isOverSlotPill(n, n2, f, layout.right, string)) {
            this.listeningStorageId = string;
            this.draggingStorageId = null;
            return true;
        }
        this.draggingStorageId = string;
        this.dragGrabOffsetY = (float)n2 - f;
        if (!this.isPointInSlotPill(n, n2, layout)) {
            this.listeningStorageId = null;
        }
        return true;
    }

    @Override
    protected void invalidateSelectedRows() {
        this.selectedRowsCache = null;
        this.selectedRowsCacheKey = null;
    }

    @Override
    protected void onSearchStateReset() {
        this.listeningStorageId = null;
        this.draggingStorageId = null;
    }

    private void renderSelectedRow(InventorySelectedRowData inventorySelectedRowData, float f, float f2, float f3, int n) {
        InventoryItemSearchComponent.drawListRow(f, f3, f2, n);
        this.renderItemInRow(this.getPreviewStack(inventorySelectedRowData), f + 6.0f + 2.0f, f3);
        this.drawListRowText(inventorySelectedRowData.displayName != null ? inventorySelectedRowData.displayName : "", f + 6.0f + 13.0f, f3, ClickGuiStyle.TEXT_SECONDARY);
        float f4 = f2 - 6.0f - 3.0f;
        float f5 = f4 - 3.0f;
        float f6 = f5 - this.getSlotPillWidth(inventorySelectedRowData.storageId);
        this.renderSlotPill(inventorySelectedRowData, f6, f5, f3);
        this.renderCloseIcon(f2, f3);
    }

    private void renderSlotPill(InventorySelectedRowData inventorySelectedRowData, float f, float f2, float f3) {
        boolean bl = inventorySelectedRowData.storageId.equals(this.listeningStorageId);
        boolean bl2 = ((InventoryItemListSetting)this.setting).hasAssignedSlotConflict(inventorySelectedRowData.storageId);
        int n = bl ? ClickGuiStyle.ACCENT_SOFT : (bl2 ? ClickGuiStyle.withAlpha(ClickGuiStyle.ERROR, 180) : ClickGuiStyle.ACCENT_GHOST);
        float f4 = f3 + 2.0f;
        float f5 = f3 + 20.0f - 3.0f;
        Shape.capsule(f, f4, f2, f5, ClickGuiStyle.TRACK);
        Shape.capsule(f + 1.0f, f4 + 1.0f, f2 - 1.0f, f5 - 1.0f, n);
        String string = this.getSlotPillLabel(inventorySelectedRowData.storageId);
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        float f6 = (float)valenFontRenderer.getStringWidth(string) * 0.5f;
        float f7 = f + (f2 - f - f6) / 2.0f;
        float f8 = InventoryItemSearchComponent.centeredScaledTextY(f4, f5 - f4);
        InventoryItemSearchComponent.drawScaledText(string, f7, f8, bl ? ClickGuiStyle.TEXT_PRIMARY : (bl2 ? ClickGuiStyle.ERROR : ClickGuiStyle.TEXT_SECONDARY));
    }

    private void updateDragState() {
        List<String> list;
        int n;
        if (this.draggingStorageId == null || ((InventoryItemListSetting)this.setting).getItems().isEmpty()) {
            return;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        float f = this.getSelectedTop(layout);
        float f2 = this.getSelectedVisibleHeight();
        if (f2 <= 0.0f) {
            this.draggingStorageId = null;
            return;
        }
        if (((InventoryItemListSetting)this.setting).getItems().size() > 7 && this.lastMouseX >= layout.left && this.lastMouseX <= layout.right) {
            if (this.lastMouseY < f + 10.0f) {
                this.selectedScrollAnim.extend(3.0f);
                this.clampSelectedScroll();
            } else if (this.lastMouseY > f + f2 - 10.0f) {
                this.selectedScrollAnim.extend(-3.0f);
                this.clampSelectedScroll();
            }
        }
        if ((n = (list = ((InventoryItemListSetting)this.setting).getItems()).indexOf(this.draggingStorageId)) < 0) {
            this.draggingStorageId = null;
            return;
        }
        float f3 = this.lastMouseY - this.dragGrabOffsetY;
        float f4 = f3 + 10.0f;
        int n2 = (int)Math.floor((f4 - f + this.selectedScrollAnim.getValue()) / 20.0f);
        if ((n2 = Math.max(0, Math.min(n2, list.size() - 1))) != n) {
            ((InventoryItemListSetting)this.setting).moveItem(this.draggingStorageId, n2);
            this.invalidateSelectedRows();
            this.markUnsaved();
        }
    }

    private int getSelectedRowIndex(int n, int n2, AbstractTextInputComponent.Layout layout) {
        float f;
        if (!this.isMouseOverSelectedList(n, n2)) {
            return -1;
        }
        float f2 = this.getSelectedTop(layout);
        int n3 = (int)(((float)n2 - f2 + (f = this.selectedScrollAnim.getValue())) / 20.0f);
        if (n3 < 0 || n3 >= ((InventoryItemListSetting)this.setting).getItems().size()) {
            return -1;
        }
        float f3 = f2 - f + (float)n3 * 20.0f;
        return (float)n2 >= f3 && (float)n2 < f3 + 20.0f ? n3 : -1;
    }

    private boolean isPointInSlotPill(int n, int n2, AbstractTextInputComponent.Layout layout) {
        int n3 = this.getSelectedRowIndex(n, n2, layout);
        if (n3 < 0) {
            return false;
        }
        String string = ((InventoryItemListSetting)this.setting).getItems().get(n3);
        float f = this.getSelectedTop(layout) - this.selectedScrollAnim.getValue() + (float)n3 * 20.0f;
        return this.isOverSlotPill(n, n2, f, layout.right, string);
    }

    private boolean isOverSlotPill(int n, int n2, float f, float f2, String string) {
        float f3 = f2 - 6.0f - 3.0f - 3.0f;
        float f4 = f3 - this.getSlotPillWidth(string);
        return (float)n >= f4 && (float)n <= f3 && (float)n2 >= f + 1.0f && (float)n2 <= f + 20.0f - 1.0f;
    }

    private float getSlotPillWidth(String string) {
        String string2 = this.getSlotPillLabel(string);
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        float f = (float)valenFontRenderer.getStringWidth(string2) * 0.5f;
        return Math.max(9.0f, f + 6.0f);
    }

    private String getSlotPillLabel(String string) {
        if (string != null && string.equals(this.listeningStorageId)) {
            return "...";
        }
        Integer n = ((InventoryItemListSetting)this.setting).getAssignedSlot(string);
        String string2 = Integer.toString(n != null ? n : 1);
        return ((InventoryItemListSetting)this.setting).hasAssignedSlotConflict(string) ? string2 + "!" : string2;
    }

    private String selectedRowsCacheKey(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : list) {
            stringBuilder.append(string).append('=').append(((InventoryItemListSetting)this.setting).getAssignedSlot(string)).append(';');
        }
        return stringBuilder.toString();
    }

    private int getHotbarSlotForKey(int n) {
        Minecraft minecraft = Minecraft.getMinecraft();
        for (int i = 0; i < minecraft.gameSettings.keyBindsHotbar.length; ++i) {
            if (n != minecraft.gameSettings.keyBindsHotbar[i].getKeyCode()) continue;
            return i + 1;
        }
        return -1;
    }

    private static final class InventorySelectedRowData
    extends AbstractSearchListComponent.SelectedRowData {
        final Integer assignedSlot;

        private InventorySelectedRowData(String string, String string2, ItemStack itemStack, List<ItemStack> list, Integer n) {
            super(string, string2, itemStack, list);
            this.assignedSlot = n;
        }
    }
}

