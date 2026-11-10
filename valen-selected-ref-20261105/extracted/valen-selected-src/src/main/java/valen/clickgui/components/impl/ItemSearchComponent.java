/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 *  valen.utility.ItemSearchIndex
 *  valen.utility.ItemSearchIndex$ItemEntry
 */
package valen.clickgui.components.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import valen.clickgui.components.impl.AbstractItemSearchComponent;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.setting.impl.ItemListSetting;
import valen.utility.ItemSearchIndex;

public class ItemSearchComponent
extends AbstractItemSearchComponent<ItemListSetting> {
    private List<AbstractSearchListComponent.SelectedRowData> selectedRowsCache;

    public ItemSearchComponent(ItemListSetting itemListSetting, ModuleComponent moduleComponent, float f) {
        super(itemListSetting, moduleComponent, f);
    }

    @Override
    protected int getSelectedEntryCount() {
        return this.setting.getItems().size();
    }

    @Override
    protected void renderSelectedRows(AbstractTextInputComponent.Layout layout, float f, int n, int n2) {
        List<String> list = this.setting.getItems();
        if (this.selectedRowsCache == null || this.selectedRowsCache.size() != list.size()) {
            this.selectedRowsCache = new ArrayList<AbstractSearchListComponent.SelectedRowData>();
            for (String object : list) {
                List list2 = ItemSearchIndex.isGroupedSelection((String)object) ? ItemSearchIndex.getSelectionVariants((String)object) : null;
                ArrayList<ItemStack> n3 = null;
                if (list2 != null && !list2.isEmpty()) {
                    n3 = new ArrayList<ItemStack>();
                    for (ItemSearchIndex.ItemEntry itemEntry : list2) {
                        n3.add(itemEntry.toItemStack());
                    }
                }
                this.selectedRowsCache.add(new AbstractSearchListComponent.SelectedRowData(object, ItemSearchIndex.getDisplayName((String)object), ItemSearchIndex.getItemStack((String)object), n3));
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
    protected boolean handleSelectedEntryClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        float f = this.selectedScrollAnim.getValue();
        ArrayList<String> arrayList = new ArrayList<String>(this.setting.getItems());
        for (int i = 0; i < arrayList.size(); ++i) {
            float f2 = this.getSelectedTop(layout) - f + (float)i * 20.0f;
            if (!this.isOverClose(n, n2, f2, layout.right)) continue;
            this.removeSelection((String)arrayList.get(i));
            return true;
        }
        return false;
    }

    @Override
    protected void invalidateSelectedRows() {
        this.selectedRowsCache = null;
    }
}

