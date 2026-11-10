/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 *  valen.utility.ItemSearchIndex
 *  valen.utility.ItemSearchIndex$GroupedItemResult
 *  valen.utility.ItemSearchIndex$ItemEntry
 */
package valen.clickgui.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.setting.Setting;
import valen.module.setting.impl.ItemListSetting;
import valen.utility.ItemSearchIndex;

public abstract class AbstractItemSearchComponent<T extends ItemListSetting>
extends AbstractSearchListComponent {
    protected final T setting;
    private List<ItemSearchIndex.GroupedItemResult> cachedResults = Collections.emptyList();
    private String expandedGroupId;
    private String expandedGroupLabel;
    private String expandedAllSelectionStorageId;
    private List<ItemSearchIndex.ItemEntry> expandedVariants = Collections.emptyList();

    protected AbstractItemSearchComponent(T t, ModuleComponent moduleComponent, float f) {
        super(moduleComponent, f, "Search items...");
        this.setting = t;
    }

    @Override
    protected String getLabelText() {
        int n = ((ItemListSetting)this.setting).getItems().size();
        return ((Setting)this.setting).getName() + (n > 0 ? " (" + n + ")" : "");
    }

    @Override
    public boolean isBaseVisible() {
        return ((ItemListSetting)this.setting).visible;
    }

    @Override
    public String getGroupName() {
        return ((ItemListSetting)this.setting).group != null ? ((ItemListSetting)this.setting).group.getName() : "";
    }

    @Override
    protected int getDropdownRowCount() {
        return this.expandedGroupId != null ? 2 + this.expandedVariants.size() : this.cachedResults.size();
    }

    @Override
    protected void renderDropdownRows(AbstractTextInputComponent.Layout layout, float f, int n, int n2, int n3) {
        if (this.expandedGroupId != null) {
            String string = this.expandedGroupLabel != null ? this.expandedGroupLabel : this.expandedGroupId;
            for (int i = n; i < n2; ++i) {
                float f2 = layout.contentTop - f + (float)i * 20.0f;
                int n4 = this.getListRowBackground(i, i == n3);
                if (i == 0) {
                    this.renderBackRow(layout.left, layout.right, f2, n4, string);
                    continue;
                }
                if (i == 1) {
                    this.renderStandardRow(string + " (All)", this.getExpandedAllCyclingIcon(), layout.left, layout.right, f2, n4, false);
                    continue;
                }
                ItemSearchIndex.ItemEntry itemEntry = this.expandedVariants.get(i - 2);
                this.renderStandardRow(itemEntry.displayName, itemEntry.toItemStack(), layout.left, layout.right, f2, n4, false);
            }
            return;
        }
        for (int i = n; i < n2; ++i) {
            ItemSearchIndex.GroupedItemResult groupedItemResult = this.cachedResults.get(i);
            float f3 = layout.contentTop - f + (float)i * 20.0f;
            int n5 = this.getListRowBackground(i, i == n3);
            if (groupedItemResult.isSingleVariant()) {
                ItemSearchIndex.ItemEntry itemEntry = (ItemSearchIndex.ItemEntry)groupedItemResult.variants.get(0);
                this.renderStandardRow(itemEntry.displayName, itemEntry.toItemStack(), layout.left, layout.right, f3, n5, false);
                continue;
            }
            this.renderStandardRow(groupedItemResult.getGroupLabel(), groupedItemResult.getCyclingIcon(), layout.left, layout.right, f3, n5, false);
        }
    }

    @Override
    protected boolean handleDropdownClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        if (!this.isSearchFocused()) {
            return false;
        }
        int n3 = this.getDropdownRowCount();
        if (n3 == 0) {
            return false;
        }
        float f = (float)n2 - layout.contentTop;
        float f2 = this.dropdownScrollAnim.getValue();
        int n4 = (int)((f + f2) / 20.0f);
        if (n4 < 0 || n4 >= n3 || (float)n <= layout.left || (float)n >= layout.right) {
            return false;
        }
        float f3 = layout.contentTop - f2 + (float)n4 * 20.0f;
        if ((float)n2 < f3 || (float)n2 >= f3 + 20.0f) {
            return false;
        }
        if (this.expandedGroupId != null) {
            if (n4 == 0) {
                this.collapseExpandedGroup();
                return true;
            }
            if (n4 == 1) {
                ((ItemListSetting)this.setting).addItem(this.expandedAllSelectionStorageId != null ? this.expandedAllSelectionStorageId : this.expandedGroupId + ":*");
                this.afterSelectionAdded();
                return true;
            }
            int n5 = n4 - 2;
            if (n5 >= 0 && n5 < this.expandedVariants.size()) {
                ((ItemListSetting)this.setting).addItem(this.expandedVariants.get((int)n5).storageId);
                this.afterSelectionAdded();
                return true;
            }
            return true;
        }
        ItemSearchIndex.GroupedItemResult groupedItemResult = this.cachedResults.get(n4);
        if (groupedItemResult.isSingleVariant()) {
            ((ItemListSetting)this.setting).addItem(((ItemSearchIndex.ItemEntry)groupedItemResult.variants.get((int)0)).storageId);
            this.afterSelectionAdded();
            return true;
        }
        this.expandedGroupId = groupedItemResult.registryId;
        this.expandedGroupLabel = groupedItemResult.getGroupDisplayName();
        this.expandedAllSelectionStorageId = groupedItemResult.getAllSelectionStorageId();
        this.expandedVariants = new ArrayList<ItemSearchIndex.ItemEntry>();
        for (ItemSearchIndex.ItemEntry itemEntry : groupedItemResult.variants) {
            if (((ItemListSetting)this.setting).containsItem(itemEntry.storageId)) continue;
            this.expandedVariants.add(itemEntry);
        }
        this.dropdownScrollAnim.reset(0.0f);
        this.updateDropdownAnimation();
        this.moduleComponent.updateSettingPositions();
        return true;
    }

    @Override
    protected void onSearchTextChanged(String string) {
        this.collapseExpandedGroupStateOnly();
        this.cachedResults = ItemSearchIndex.searchGrouped((String)string, this.setting);
    }

    @Override
    protected boolean handleSearchEscape() {
        if (this.expandedGroupId != null) {
            this.collapseExpandedGroup();
            return true;
        }
        return false;
    }

    @Override
    protected void onSearchFieldFocused() {
        if (!this.getTextField().getText().isEmpty() && this.cachedResults.isEmpty()) {
            this.cachedResults = ItemSearchIndex.searchGrouped((String)this.getTextField().getText(), this.setting);
        }
    }

    @Override
    protected void resetSearchState() {
        this.cachedResults = Collections.emptyList();
        this.collapseExpandedGroupStateOnly();
        this.onSearchStateReset();
    }

    protected final void removeSelection(String string) {
        ((ItemListSetting)this.setting).removeItem(string);
        this.invalidateSelectedRows();
        this.notifySelectionListChanged();
    }

    protected final void refreshSelectedRows() {
        this.invalidateSelectedRows();
    }

    protected final T getItemSetting() {
        return this.setting;
    }

    protected final ItemSearchIndex.GroupedItemResult getCachedResult(int n) {
        return this.cachedResults.get(n);
    }

    protected final List<ItemSearchIndex.ItemEntry> getExpandedVariants() {
        return this.expandedVariants;
    }

    private void collapseExpandedGroup() {
        this.collapseExpandedGroupStateOnly();
        this.dropdownScrollAnim.reset(0.0f);
        this.updateDropdownAnimation();
        this.moduleComponent.updateSettingPositions();
    }

    private void collapseExpandedGroupStateOnly() {
        this.expandedGroupId = null;
        this.expandedGroupLabel = null;
        this.expandedAllSelectionStorageId = null;
        this.expandedVariants = Collections.emptyList();
    }

    private void afterSelectionAdded() {
        this.getTextField().setText("");
        this.setTextFieldFocused(false);
        this.cachedResults = Collections.emptyList();
        this.collapseExpandedGroupStateOnly();
        this.dropdownScrollAnim.reset(0.0f);
        this.invalidateSelectedRows();
        this.notifySelectionListChanged();
    }

    private ItemStack getExpandedAllCyclingIcon() {
        if (this.expandedVariants.isEmpty()) {
            return null;
        }
        int n = (int)(System.currentTimeMillis() / 1000L % (long)this.expandedVariants.size());
        return this.expandedVariants.get(n).toItemStack();
    }

    protected void onSearchStateReset() {
    }

    protected abstract void invalidateSelectedRows();
}

