/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 */
package valen.module.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import valen.module.setting.Setting;

public final class MultiSelectSetting
extends Setting {
    private final String[] options;
    private final boolean[] selected;
    private final boolean[] defaults;

    public MultiSelectSetting(String string, String[] stringArray, String ... stringArray2) {
        super(string);
        this.options = (String[])stringArray.clone();
        this.selected = new boolean[stringArray.length];
        for (String string2 : stringArray2) {
            this.setSelected(string2, true);
        }
        this.defaults = (boolean[])this.selected.clone();
    }

    public int size() {
        return this.options.length;
    }

    public String getOption(int n) {
        return this.options[n];
    }

    public boolean isSelected(int n) {
        return n >= 0 && n < this.selected.length && this.selected[n];
    }

    public boolean isSelected(String string) {
        for (int i = 0; i < this.options.length; ++i) {
            if (!this.options[i].equals(string)) continue;
            return this.selected[i];
        }
        return false;
    }

    public void setSelected(String string, boolean bl) {
        for (int i = 0; i < this.options.length; ++i) {
            if (!this.options[i].equals(string)) continue;
            this.selected[i] = bl;
            return;
        }
    }

    public void toggle(int n) {
        if (n >= 0 && n < this.selected.length) {
            this.selected[n] = !this.selected[n];
        }
    }

    public void clear() {
        Arrays.fill(this.selected, false);
    }

    public void reset() {
        System.arraycopy(this.defaults, 0, this.selected, 0, this.selected.length);
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get(this.getProfileKey());
        if (jsonElement == null || !jsonElement.isJsonArray()) {
            return;
        }
        this.clear();
        for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
            if (!jsonElement2.isJsonPrimitive() || !jsonElement2.getAsJsonPrimitive().isString()) continue;
            this.setSelected(jsonElement2.getAsString(), true);
        }
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < this.options.length; ++i) {
            if (!this.selected[i]) continue;
            jsonArray.add((JsonElement)new JsonPrimitive(this.options[i]));
        }
        jsonObject.add(this.getProfileKey(), (JsonElement)jsonArray);
    }
}

