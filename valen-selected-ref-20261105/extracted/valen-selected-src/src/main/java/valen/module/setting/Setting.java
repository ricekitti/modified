/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package valen.module.setting;

import com.google.gson.JsonObject;
import valen.clickgui.ClickGui;
import valen.clickgui.components.impl.CategoryComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.Module;

public abstract class Setting {
    public String name;
    public boolean visible = true;

    public Setting(String string) {
        this.name = string;
    }

    public void setVisible(boolean bl, Module module) {
        if (bl == this.visible) {
            return;
        }
        this.visible = bl;
        block0: for (CategoryComponent categoryComponent : ClickGui.categories) {
            if (categoryComponent.category != module.moduleCategory()) continue;
            for (ModuleComponent moduleComponent : categoryComponent.modules) {
                if (!moduleComponent.mod.getName().equals(module.getName())) continue;
                moduleComponent.reloadSettings();
                continue block0;
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public String getProfileKey() {
        return this.name;
    }

    public abstract void loadProfile(JsonObject var1);

    public void writeProfile(JsonObject jsonObject) {
    }
}

