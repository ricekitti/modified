/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package valen.module.setting.impl;

import com.google.gson.JsonObject;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;

public class ButtonSetting
extends Setting {
    private String name;
    private boolean isEnabled;
    public boolean isMethodButton;
    private Runnable method;
    public GroupSetting group;
    private final String[] legacyProfileKeys;

    public ButtonSetting(String string, boolean bl) {
        super(string);
        this.name = string;
        this.isEnabled = bl;
        this.isMethodButton = false;
        this.legacyProfileKeys = new String[0];
    }

    public ButtonSetting(String string, boolean bl, String ... stringArray) {
        super(string);
        this.name = string;
        this.isEnabled = bl;
        this.isMethodButton = false;
        this.legacyProfileKeys = stringArray != null ? stringArray : new String[]{};
    }

    public ButtonSetting(GroupSetting groupSetting, String string, boolean bl) {
        super(string);
        this.group = groupSetting;
        this.name = string;
        this.isEnabled = bl;
        this.isMethodButton = false;
        this.legacyProfileKeys = new String[0];
    }

    public ButtonSetting(GroupSetting groupSetting, String string, boolean bl, String ... stringArray) {
        super(string);
        this.group = groupSetting;
        this.name = string;
        this.isEnabled = bl;
        this.isMethodButton = false;
        this.legacyProfileKeys = stringArray != null ? stringArray : new String[]{};
    }

    public ButtonSetting(String string, Runnable runnable) {
        super(string);
        this.name = string;
        this.isEnabled = false;
        this.isMethodButton = true;
        this.method = runnable;
        this.legacyProfileKeys = new String[0];
    }

    public void runMethod() {
        if (this.method != null) {
            this.method.run();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getProfileKey() {
        return this.group == null ? this.getName() : this.group.getName() + "." + this.getName();
    }

    public boolean isToggled() {
        return this.isEnabled;
    }

    public void toggle() {
        this.isEnabled = !this.isEnabled;
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void setEnabled(boolean bl) {
        this.isEnabled = bl;
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        String string = this.getProfileKey();
        String string2 = this.getName();
        String string3 = null;
        if (jsonObject.has(string)) {
            string3 = string;
        } else if (jsonObject.has(string2)) {
            string3 = string2;
        } else {
            for (String string4 : this.legacyProfileKeys) {
                if (!jsonObject.has(string4)) continue;
                string3 = string4;
                break;
            }
        }
        if (string3 != null && jsonObject.get(string3).isJsonPrimitive() && !this.isMethodButton) {
            boolean bl = this.isEnabled;
            try {
                bl = jsonObject.getAsJsonPrimitive(string3).getAsBoolean();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.setEnabled(bl);
        }
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        if (this.isMethodButton) {
            return;
        }
        jsonObject.addProperty(this.getProfileKey(), Boolean.valueOf(this.isEnabled));
    }
}

