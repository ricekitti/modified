/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  valen.utility.ItemSearchIndex
 */
package valen.module.setting.impl;

import java.util.List;
import net.minecraft.item.ItemStack;
import valen.module.setting.impl.BlockListSetting;
import valen.module.setting.impl.GroupSetting;
import valen.utility.ItemSearchIndex;

public class ItemListSetting
extends BlockListSetting {
    public ItemListSetting(String string) {
        super(string);
    }

    public ItemListSetting(String string, String ... stringArray) {
        super(string, stringArray);
    }

    public ItemListSetting(GroupSetting groupSetting, String string) {
        super(groupSetting, string);
    }

    public ItemListSetting(GroupSetting groupSetting, String string, String ... stringArray) {
        super(groupSetting, string, stringArray);
    }

    public void addItem(String string) {
        this.addBlock(string);
    }

    public void removeItem(String string) {
        this.removeBlock(string);
    }

    public List<String> getItems() {
        return this.getBlocks();
    }

    public boolean containsItem(String string) {
        return this.contains(string);
    }

    public boolean matches(ItemStack itemStack) {
        return ItemSearchIndex.matches(this.getItems(), (ItemStack)itemStack);
    }
}

