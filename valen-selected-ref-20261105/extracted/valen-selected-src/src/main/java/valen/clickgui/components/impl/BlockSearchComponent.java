/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 *  valen.utility.BlockSearchIndex
 *  valen.utility.BlockSearchIndex$BlockEntry
 *  valen.utility.BlockSearchIndex$GroupedBlockResult
 */
package valen.clickgui.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.setting.impl.BlockEspSetting;
import valen.module.setting.impl.BlockListSetting;
import valen.utility.BlockSearchIndex;

public class BlockSearchComponent
extends AbstractSearchListComponent {
    public final BlockListSetting setting;
    private List<BlockSearchIndex.GroupedBlockResult> cachedResults = Collections.emptyList();
    private String expandedGroupId;
    private List<BlockSearchIndex.BlockEntry> expandedVariants = Collections.emptyList();
    private List<AbstractSearchListComponent.SelectedRowData> selectedRowsCache;

    public BlockSearchComponent(BlockListSetting blockListSetting, ModuleComponent moduleComponent, float f) {
        super(moduleComponent, f, "Search blocks...");
        this.setting = blockListSetting;
    }

    @Override
    protected String getLabelText() {
        int n = this.setting.getBlocks().size();
        return this.setting.getName() + (n > 0 ? " (" + n + ")" : "");
    }

    @Override
    protected int getSelectedEntryCount() {
        if (this.setting instanceof BlockEspSetting) {
            return 0;
        }
        return this.setting.getBlocks().size();
    }

    @Override
    protected int getDropdownRowCount() {
        return this.expandedGroupId != null ? 2 + this.expandedVariants.size() : this.cachedResults.size();
    }

    @Override
    public boolean isBaseVisible() {
        return this.setting.visible;
    }

    @Override
    public String getGroupName() {
        return this.setting.group != null ? this.setting.group.getName() : "";
    }

    @Override
    protected void renderDropdownRows(AbstractTextInputComponent.Layout layout, float f, int n, int n2, int n3) {
        if (this.expandedGroupId != null) {
            String string = !this.expandedVariants.isEmpty() ? this.expandedVariants.get((int)0).displayName : this.expandedGroupId;
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
                BlockSearchIndex.BlockEntry blockEntry = this.expandedVariants.get(i - 2);
                this.renderStandardRow(blockEntry.displayName, blockEntry.toItemStack(), layout.left, layout.right, f2, n4, false);
            }
            return;
        }
        for (int i = n; i < n2; ++i) {
            BlockSearchIndex.GroupedBlockResult groupedBlockResult = this.cachedResults.get(i);
            float f3 = layout.contentTop - f + (float)i * 20.0f;
            int n5 = this.getListRowBackground(i, i == n3);
            if (groupedBlockResult.isSingleVariant()) {
                BlockSearchIndex.BlockEntry blockEntry = (BlockSearchIndex.BlockEntry)groupedBlockResult.variants.get(0);
                this.renderStandardRow(blockEntry.displayName, blockEntry.toItemStack(), layout.left, layout.right, f3, n5, false);
                continue;
            }
            this.renderStandardRow(groupedBlockResult.getGroupLabel(), groupedBlockResult.getCyclingIcon(), layout.left, layout.right, f3, n5, false);
        }
    }

    @Override
    protected void renderSelectedRows(AbstractTextInputComponent.Layout layout, float f, int n, int n2) {
        List<String> list = this.setting.getBlocks();
        if (this.selectedRowsCache == null || this.selectedRowsCache.size() != list.size()) {
            this.selectedRowsCache = new ArrayList<AbstractSearchListComponent.SelectedRowData>();
            for (String object : list) {
                List f2 = null;
                if (BlockSearchIndex.isWildcard((String)object)) {
                    f2 = BlockSearchIndex.getVariants((String)BlockSearchIndex.getRegistryId((String)object));
                }
                ArrayList<ItemStack> n3 = null;
                if (f2 != null && !f2.isEmpty()) {
                    n3 = new ArrayList<ItemStack>();
                    for (BlockSearchIndex.BlockEntry blockEntry : f2) {
                        n3.add(blockEntry.toItemStack());
                    }
                }
                this.selectedRowsCache.add(new AbstractSearchListComponent.SelectedRowData(object, BlockSearchIndex.getDisplayName((String)object), BlockSearchIndex.getItemStack((String)object), n3));
            }
        }
        for (int i = n; i < n2; ++i) {
            AbstractSearchListComponent.SelectedRowData selectedRowData = this.selectedRowsCache.get(i);
            float f2 = this.getSelectedTop(layout) - f + (float)i * 20.0f;
            int n3 = this.getListRowBackground(i, false);
            this.renderStandardRow(selectedRowData.displayName, this.getPreviewStack(selectedRowData), layout.left, layout.right, f2, n3, true);
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
        float f = this.dropdownScrollAnim.getValue();
        int n4 = (int)(((float)n2 - layout.contentTop + f) / 20.0f);
        if (n4 < 0 || n4 >= n3 || (float)n <= layout.left || (float)n >= layout.right) {
            return false;
        }
        float f2 = layout.contentTop - f + (float)n4 * 20.0f;
        if ((float)n2 < f2 || (float)n2 >= f2 + 20.0f) {
            return false;
        }
        if (this.expandedGroupId != null) {
            if (n4 == 0) {
                this.collapseExpandedGroup();
                return true;
            }
            if (n4 == 1) {
                this.setting.addBlock(this.expandedGroupId + ":*");
                this.afterBlockSelectionAdded();
                return true;
            }
            int n5 = n4 - 2;
            if (n5 >= 0 && n5 < this.expandedVariants.size()) {
                this.setting.addBlock(this.expandedVariants.get((int)n5).storageId);
                this.afterBlockSelectionAdded();
                return true;
            }
            return true;
        }
        BlockSearchIndex.GroupedBlockResult groupedBlockResult = this.cachedResults.get(n4);
        if (groupedBlockResult.isSingleVariant()) {
            this.setting.addBlock(((BlockSearchIndex.BlockEntry)groupedBlockResult.variants.get((int)0)).storageId);
            this.afterBlockSelectionAdded();
            return true;
        }
        this.expandedGroupId = groupedBlockResult.registryId;
        this.expandedVariants = new ArrayList<BlockSearchIndex.BlockEntry>();
        for (BlockSearchIndex.BlockEntry blockEntry : groupedBlockResult.variants) {
            if (this.setting.contains(blockEntry.storageId)) continue;
            this.expandedVariants.add(blockEntry);
        }
        this.dropdownScrollAnim.reset(0.0f);
        this.updateDropdownAnimation();
        this.moduleComponent.updateSettingPositions();
        return true;
    }

    @Override
    protected boolean handleSelectedEntryClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        if (this.setting instanceof BlockEspSetting) {
            return false;
        }
        float f = this.selectedScrollAnim.getValue();
        ArrayList<String> arrayList = new ArrayList<String>(this.setting.getBlocks());
        for (int i = 0; i < arrayList.size(); ++i) {
            float f2 = this.getSelectedTop(layout) - f + (float)i * 20.0f;
            if (!this.isOverClose(n, n2, f2, layout.right)) continue;
            this.setting.removeBlock((String)arrayList.get(i));
            this.selectedRowsCache = null;
            this.notifySelectionListChanged();
            return true;
        }
        return false;
    }

    @Override
    protected void onSearchTextChanged(String string) {
        this.expandedGroupId = null;
        this.expandedVariants = Collections.emptyList();
        this.cachedResults = BlockSearchIndex.searchGrouped((String)string, (BlockListSetting)this.setting);
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
            this.cachedResults = BlockSearchIndex.searchGrouped((String)this.getTextField().getText(), (BlockListSetting)this.setting);
        }
    }

    @Override
    protected void resetSearchState() {
        this.cachedResults = Collections.emptyList();
        this.expandedGroupId = null;
        this.expandedVariants = Collections.emptyList();
        this.selectedRowsCache = null;
    }

    private void afterBlockSelectionAdded() {
        this.getTextField().setText("");
        this.setTextFieldFocused(false);
        this.cachedResults = Collections.emptyList();
        this.expandedGroupId = null;
        this.expandedVariants = Collections.emptyList();
        this.dropdownScrollAnim.reset(0.0f);
        this.selectedRowsCache = null;
        this.notifySelectionListChanged();
        if (this.setting instanceof BlockEspSetting) {
            this.moduleComponent.reloadSettings();
        }
    }

    private void collapseExpandedGroup() {
        this.expandedGroupId = null;
        this.expandedVariants = Collections.emptyList();
        this.dropdownScrollAnim.reset(0.0f);
        this.updateDropdownAnimation();
        this.moduleComponent.updateSettingPositions();
    }

    private ItemStack getExpandedAllCyclingIcon() {
        if (this.expandedVariants.isEmpty()) {
            return null;
        }
        int n = (int)(System.currentTimeMillis() / 1000L % (long)this.expandedVariants.size());
        return this.expandedVariants.get(n).toItemStack();
    }
}

