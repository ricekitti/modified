/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.client.renderer.RenderHelper
 *  net.minecraft.client.renderer.entity.RenderItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.ResourceLocation
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 */
package valen.clickgui.components.impl;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.utility.RenderUtils;
import valen.utility.Timer;

public abstract class AbstractSearchListComponent
extends AbstractTextInputComponent {
    private static final String CLOSE_ICON_PATH = "/assets/valen/textures/gui/close.png";
    private static final String ARROW_ICON_PATH = "/assets/valen/textures/gui/arrow_left.png";
    protected static final float ANIMATION_DURATION = 250.0f;
    protected static final int MAX_VISIBLE_RESULTS = 7;
    protected static final int MAX_VISIBLE_SELECTED = 7;
    protected static final int CLOSE_SIZE = 6;
    protected static final float CLOSE_PAD = 3.0f;
    protected static final float SELECTED_LIST_GAP = 4.0f;
    protected static final float LIST_ROW_TEXT_SCALE = 0.56f;
    protected static final float LIST_ROW_VISUAL_HEIGHT = 19.0f;
    protected final ScrollOffsetAnimation dropdownScrollAnim = new ScrollOffsetAnimation(200L);
    protected final ScrollOffsetAnimation selectedScrollAnim = new ScrollOffsetAnimation(200L);
    protected float lastMouseX;
    protected float lastMouseY;
    private Timer dropdownAnimTimer;
    private float dropdownAnimStartH;
    private float dropdownAnimTargetH;
    private float dropdownAnimH;

    protected AbstractSearchListComponent(ModuleComponent moduleComponent, float f, String string) {
        super(moduleComponent, f, string, 128);
    }

    @Override
    public void render() {
        AbstractTextInputComponent.Layout layout = this.layout(false);
        this.renderLabel(layout, this.getLabelText());
        this.renderTextField(layout);
        this.renderDropdown(layout);
        this.renderSelectedEntries(layout);
    }

    @Override
    public void drawScreen(int n, int n2) {
        super.drawScreen(n, n2);
        this.lastMouseX = n;
        this.lastMouseY = n2;
        this.updateDropdownAnimation();
        this.onAfterBaseDrawScreen();
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        if (n3 == 0 && this.handleDropdownClick(n, n2, layout)) {
            this.onDropdownClickHandled(n, n2);
            return true;
        }
        if (n3 == 0 && this.handleSelectedEntryClick(n, n2, layout)) {
            this.onSelectedEntryClickHandled(n, n2);
            return true;
        }
        if (this.handleTextFieldFocusClick(n, n2, layout)) {
            this.onSearchFocusHandled(n, n2);
            return true;
        }
        if (this.isSearchFocused()) {
            this.unfocusSearch();
        }
        this.onOutsideClick(n, n2, n3);
        return false;
    }

    @Override
    public void keyTyped(char c, int n) {
        if (!this.moduleComponent.isOpened) {
            return;
        }
        if (n == 1 && this.isSearchFocused()) {
            if (!this.handleSearchEscape()) {
                this.unfocusSearch();
            }
            return;
        }
        if ((n == 28 || n == 156) && this.isSearchFocused()) {
            this.unfocusSearch();
            return;
        }
        if (this.getTextField().textboxKeyTyped(c, n)) {
            this.onSearchTextChanged(this.getTextField().getText());
            this.dropdownScrollAnim.reset(0.0f);
            this.updateDropdownAnimation();
            this.moduleComponent.updateSettingPositions();
        }
    }

    @Override
    public void onScroll(int n) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return;
        }
        float f = 25.0f;
        float f2 = f * ((float)n / 120.0f);
        if (this.isMouseOverDropdown()) {
            if (f2 != 0.0f) {
                this.dropdownScrollAnim.extend(-f2);
            }
            this.clampDropdownScroll();
            return;
        }
        if (this.isMouseOverSelectedList() && this.getSelectedEntryCount() > 7) {
            if (f2 != 0.0f) {
                this.selectedScrollAnim.extend(-f2);
            }
            this.clampSelectedScroll();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.getTextField().setText("");
        this.resetSearchState();
        this.dropdownScrollAnim.reset(0.0f);
        this.selectedScrollAnim.reset(0.0f);
        this.dropdownAnimTimer = null;
        this.dropdownAnimH = 0.0f;
        this.dropdownAnimStartH = 0.0f;
        this.dropdownAnimTargetH = 0.0f;
    }

    @Override
    public float getHeightF() {
        return this.getCurrentHeight();
    }

    public boolean isSearchFocused() {
        return this.isTextFieldFocused();
    }

    public void unfocusSearch() {
        this.setTextFieldFocused(false);
        this.updateDropdownAnimation();
    }

    @Override
    public boolean isTextInputFocused() {
        return this.isSearchFocused() || this.hasAdditionalTextInputFocus();
    }

    @Override
    public void unfocusTextInput() {
        this.clearAdditionalTextInputFocus();
        this.unfocusSearch();
    }

    @Override
    public boolean containsClick(int n, int n2) {
        AbstractTextInputComponent.Layout layout = this.layout(true);
        return this.isTextFieldClicked(n, n2, layout) || this.isMouseOverDropdown(n, n2) || this.isMouseOverSelectedList(n, n2);
    }

    public boolean capturesCategoryScroll(float f, float f2) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return false;
        }
        if (this.isMouseOverDropdown(f, f2) && this.getDropdownRowCount() > 7) {
            return true;
        }
        return this.isMouseOverSelectedList(f, f2) && this.getSelectedEntryCount() > 7;
    }

    public float getCurrentHeight() {
        int n = this.getSelectedEntryCount();
        float f = n == 0 ? 0.0f : 4.0f + (float)Math.min(7, n) * 20.0f;
        return 40.0f + this.getAnimatedDropdownHeight() + f;
    }

    protected final float getAnimatedDropdownHeight() {
        if (this.dropdownAnimTimer != null) {
            if ((float)(System.currentTimeMillis() - this.dropdownAnimTimer.last) >= 280.0f) {
                this.dropdownAnimTimer = null;
                this.dropdownAnimH = this.dropdownAnimTargetH;
                this.dropdownAnimStartH = this.dropdownAnimTargetH;
            } else {
                this.dropdownAnimH = this.dropdownAnimTimer.getValueFloat(this.dropdownAnimStartH, this.dropdownAnimTargetH, 1);
                if (this.dropdownAnimH == this.dropdownAnimTargetH) {
                    this.dropdownAnimTimer = null;
                    this.dropdownAnimStartH = this.dropdownAnimTargetH;
                }
            }
        }
        return this.dropdownAnimH;
    }

    protected final float getSelectedTop(AbstractTextInputComponent.Layout layout) {
        return layout.contentTop + this.getAnimatedDropdownHeight() + 4.0f;
    }

    protected final float getSelectedVisibleHeight() {
        return (float)Math.min(7, this.getSelectedEntryCount()) * 20.0f;
    }

    protected final void clampDropdownScroll() {
        float f = Math.max(0.0f, (float)(this.getDropdownRowCount() - 7) * 20.0f);
        this.dropdownScrollAnim.clampTarget(0.0f, f);
    }

    protected final void clampSelectedScroll() {
        float f = Math.max(0.0f, (float)(this.getSelectedEntryCount() - 7) * 20.0f);
        this.selectedScrollAnim.clampTarget(0.0f, f);
    }

    protected final void updateDropdownAnimation() {
        float f = this.computeDropdownTarget();
        if (f != this.dropdownAnimTargetH) {
            this.dropdownAnimStartH = this.dropdownAnimH;
            this.dropdownAnimTargetH = f;
            this.dropdownAnimTimer = new Timer(250.0f);
            this.dropdownAnimTimer.start();
        }
    }

    protected final void notifySelectionListChanged() {
        this.markUnsaved();
        this.updateDropdownAnimation();
        this.moduleComponent.updateSettingPositions();
    }

    protected final void markUnsaved() {
        if (Valen.configManager != null) {
            Valen.configManager.invalidateDirtyCache();
        }
    }

    protected final ItemStack getPreviewStack(SelectedRowData selectedRowData) {
        if (selectedRowData == null) {
            return null;
        }
        if (selectedRowData.cyclingStacks != null && !selectedRowData.cyclingStacks.isEmpty()) {
            int n = (int)(System.currentTimeMillis() / 1000L % (long)selectedRowData.cyclingStacks.size());
            return selectedRowData.cyclingStacks.get(n);
        }
        return selectedRowData.stack;
    }

    protected final void renderBackRow(float f, float f2, float f3, int n, String string) {
        AbstractSearchListComponent.drawListRow(f, f3, f2, n);
        ResourceLocation resourceLocation = RenderUtils.getIcon(ARROW_ICON_PATH);
        if (resourceLocation != null) {
            float f4 = f + 6.0f + 2.0f;
            float f5 = f3 + 6.5f;
            RenderUtils.drawIcon(resourceLocation, f4, f5, 6, ClickGuiStyle.ACCENT_MID);
        }
        this.drawListRowText(string != null ? string : "Back", f + 6.0f + 13.0f, f3, ClickGuiStyle.TEXT_SECONDARY);
    }

    protected final void renderStandardRow(String string, ItemStack itemStack, float f, float f2, float f3, int n, boolean bl) {
        AbstractSearchListComponent.drawListRow(f, f3, f2, n);
        this.renderItemInRow(itemStack, f + 6.0f + 2.0f, f3);
        this.drawListRowText(string != null ? string : "", f + 6.0f + 13.0f, f3, ClickGuiStyle.TEXT_SECONDARY);
        if (bl) {
            this.renderCloseIcon(f2, f3);
        }
    }

    protected final void drawListRowText(String string, float f, float f2, int n) {
        AbstractSearchListComponent.drawScaledText(string, f, AbstractSearchListComponent.centeredScaledTextY(f2, 19.0f, 0.56f), n, 0.56f);
    }

    protected final void renderCloseIcon(float f, float f2) {
        ResourceLocation resourceLocation = RenderUtils.getIcon(CLOSE_ICON_PATH);
        if (resourceLocation == null) {
            return;
        }
        float f3 = f - 6.0f - 3.0f;
        float f4 = f2 + 6.5f;
        RenderUtils.drawIcon(resourceLocation, f3, f4, 6, ClickGuiStyle.HIDDEN);
    }

    protected final int getListRowBackground(int n, boolean bl) {
        return ClickGuiStyle.listRow(n, bl);
    }

    protected final boolean isOverClose(float f, float f2, float f3, float f4) {
        float f5 = f4 - 6.0f - 3.0f;
        float f6 = f3 + 7.0f;
        return f >= f5 && f <= f5 + 6.0f && f2 >= f6 && f2 <= f6 + 6.0f;
    }

    protected final void renderItemInRow(ItemStack itemStack, float f, float f2) {
        if (itemStack == null) {
            return;
        }
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        double d = 0.55;
        float f3 = (float)(16.0 * d);
        float f4 = (19.0f - f3) / 2.0f;
        float f5 = f2 + f4;
        float f6 = (float)((double)f / d);
        float f7 = (float)((double)f5 / d);
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)d, (double)d, (double)d);
        GlStateManager.translate((float)f6, (float)f7, (float)0.0f);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableBlend();
        renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);
        GlStateManager.enableBlend();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    protected final boolean isMouseOverDropdown(float f, float f2) {
        if (this.getDropdownRowCount() == 0) {
            return false;
        }
        float f3 = this.getAnimatedDropdownHeight();
        if (f3 <= 0.0f) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        return f >= layout.left && f <= layout.right && f2 >= layout.contentTop && f2 < layout.contentTop + f3;
    }

    protected final boolean isMouseOverSelectedList(float f, float f2) {
        if (this.getSelectedEntryCount() == 0) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        float f3 = this.getSelectedTop(layout);
        float f4 = this.getSelectedVisibleHeight();
        return f >= layout.left && f <= layout.right && f2 >= f3 && f2 < f3 + f4;
    }

    protected final boolean isMouseOverDropdown() {
        return this.isMouseOverDropdown(this.lastMouseX, this.lastMouseY);
    }

    protected final boolean isMouseOverSelectedList() {
        return this.isMouseOverSelectedList(this.lastMouseX, this.lastMouseY);
    }

    private float computeDropdownTarget() {
        int n = this.getDropdownRowCount();
        if (this.isSearchFocused() && n > 0) {
            return (float)Math.min(7, n) * 20.0f;
        }
        return 0.0f;
    }

    private void renderDropdown(AbstractTextInputComponent.Layout layout) {
        float f;
        int n = this.getDropdownRowCount();
        float f2 = this.getAnimatedDropdownHeight();
        float f3 = this.moduleComponent.categoryComponent.moduleY - layout.cy;
        if (f2 <= 0.0f || n == 0) {
            return;
        }
        float f4 = layout.contentTop + f3;
        AbstractSearchListComponent.drawListWell(layout.left, layout.contentTop, layout.right, layout.contentTop + f2);
        RenderUtils.scissorPushGui(layout.left, f4, layout.right - layout.left, f2);
        float f5 = this.dropdownScrollAnim.getValue();
        int n2 = (int)(f5 / 20.0f);
        int n3 = Math.min(n2 + 7 + 1, n);
        int n4 = -1;
        if (this.lastMouseX >= layout.left && this.lastMouseX <= layout.right && this.lastMouseY >= f4 && this.lastMouseY < f4 + f2 && ((n4 = (int)(((f = this.lastMouseY - f4) + f5) / 20.0f)) < 0 || n4 >= n)) {
            n4 = -1;
        }
        this.renderDropdownRows(layout, f5, n2, n3, n4);
        RenderUtils.scissorPop();
    }

    private void renderSelectedEntries(AbstractTextInputComponent.Layout layout) {
        int n = this.getSelectedEntryCount();
        if (n == 0) {
            return;
        }
        float f = this.getSelectedTop(layout);
        float f2 = this.getSelectedVisibleHeight();
        float f3 = this.moduleComponent.categoryComponent.moduleY - layout.cy;
        AbstractSearchListComponent.drawListWell(layout.left, f, layout.right, f + f2);
        RenderUtils.scissorPushGui(layout.left, f + f3, layout.right - layout.left, f2);
        float f4 = this.selectedScrollAnim.getValue();
        int n2 = (int)(f4 / 20.0f);
        int n3 = Math.min(n2 + 7 + 1, n);
        this.renderSelectedRows(layout, f4, n2, n3);
        RenderUtils.scissorPop();
    }

    private boolean handleTextFieldFocusClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        if (this.isTextFieldClicked(n, n2, layout)) {
            this.setTextFieldFocused(true);
            this.onSearchFieldFocused();
            this.updateDropdownAnimation();
            return true;
        }
        return false;
    }

    protected boolean hasAdditionalTextInputFocus() {
        return false;
    }

    protected void clearAdditionalTextInputFocus() {
    }

    protected void onAfterBaseDrawScreen() {
    }

    protected void onDropdownClickHandled(int n, int n2) {
    }

    protected void onSelectedEntryClickHandled(int n, int n2) {
    }

    protected void onSearchFocusHandled(int n, int n2) {
    }

    protected void onOutsideClick(int n, int n2, int n3) {
    }

    protected abstract String getLabelText();

    protected abstract int getSelectedEntryCount();

    protected abstract int getDropdownRowCount();

    protected abstract void renderDropdownRows(AbstractTextInputComponent.Layout var1, float var2, int var3, int var4, int var5);

    protected abstract void renderSelectedRows(AbstractTextInputComponent.Layout var1, float var2, int var3, int var4);

    protected abstract boolean handleDropdownClick(int var1, int var2, AbstractTextInputComponent.Layout var3);

    protected abstract boolean handleSelectedEntryClick(int var1, int var2, AbstractTextInputComponent.Layout var3);

    protected abstract void onSearchTextChanged(String var1);

    protected abstract boolean handleSearchEscape();

    protected abstract void onSearchFieldFocused();

    protected abstract void resetSearchState();

    protected static class SelectedRowData {
        final String storageId;
        final String displayName;
        final ItemStack stack;
        final List<ItemStack> cyclingStacks;

        protected SelectedRowData(String string, String string2, ItemStack itemStack, List<ItemStack> list) {
            this.storageId = string;
            this.displayName = string2;
            this.stack = itemStack;
            this.cyclingStacks = list;
        }
    }
}

