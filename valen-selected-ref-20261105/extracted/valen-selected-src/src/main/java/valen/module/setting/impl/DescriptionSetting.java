/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package valen.module.setting.impl;

import com.google.gson.JsonObject;
import valen.module.setting.Setting;

public class DescriptionSetting
extends Setting {
    private String desc;

    public DescriptionSetting(String string) {
        super(string);
        this.desc = string;
    }

    public String getDesc() {
        return this.desc;
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
    }
}

