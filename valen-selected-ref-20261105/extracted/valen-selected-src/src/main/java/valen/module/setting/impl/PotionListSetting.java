/*
 * Decompiled with CFR 0.152.
 */
package valen.module.setting.impl;

import java.util.List;
import valen.module.setting.impl.BlockListSetting;
import valen.module.setting.impl.GroupSetting;

public class PotionListSetting
extends BlockListSetting {
    public PotionListSetting(String string) {
        super(string);
    }

    public PotionListSetting(String string, String ... stringArray) {
        super(string, stringArray);
    }

    public PotionListSetting(GroupSetting groupSetting, String string) {
        super(groupSetting, string);
    }

    public PotionListSetting(GroupSetting groupSetting, String string, String ... stringArray) {
        super(groupSetting, string, stringArray);
    }

    public void addPotion(String string) {
        this.addBlock(string);
    }

    public void removePotion(String string) {
        this.removeBlock(string);
    }

    public boolean containsPotion(String string) {
        return this.contains(string);
    }

    public List<String> getPotions() {
        return this.getBlocks();
    }
}

