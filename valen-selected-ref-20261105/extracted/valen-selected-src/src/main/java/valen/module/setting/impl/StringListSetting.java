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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;

public class StringListSetting
extends Setting {
    private final String placeholder;
    private final int maxLength;
    private final List<String> entries = new ArrayList<String>();
    public GroupSetting group;

    public StringListSetting(String string, String string2, int n) {
        this(null, string, string2, n);
    }

    public StringListSetting(GroupSetting groupSetting, String string, String string2, int n) {
        super(string);
        this.group = groupSetting;
        this.placeholder = string2 == null ? "" : string2;
        this.maxLength = Math.max(1, n);
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    public boolean addEntry(String string) {
        if (string == null || string.trim().isEmpty()) {
            return false;
        }
        String string2 = string.trim();
        for (String string3 : this.entries) {
            if (!string3.equalsIgnoreCase(string2)) continue;
            return false;
        }
        this.entries.add(string2);
        return true;
    }

    public boolean removeEntry(String string) {
        return this.entries.removeIf(string2 -> string2.equalsIgnoreCase(string));
    }

    public void clearEntries() {
        this.entries.clear();
    }

    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();
        for (String string : this.entries) {
            jsonArray.add((JsonElement)new JsonPrimitive(string));
        }
        return jsonArray;
    }

    public void fromJsonArray(JsonArray jsonArray) {
        this.entries.clear();
        if (jsonArray == null) {
            return;
        }
        for (JsonElement jsonElement : jsonArray) {
            String string;
            if (!jsonElement.isJsonPrimitive() || (string = jsonElement.getAsString()) == null || string.trim().isEmpty()) continue;
            this.entries.add(string.trim());
        }
    }

    @Override
    public String getProfileKey() {
        return this.group == null ? this.getName() : this.group.getName() + "." + this.getName();
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        String string = this.getProfileKey();
        if (jsonObject.has(string) && jsonObject.get(string).isJsonArray()) {
            this.fromJsonArray(jsonObject.getAsJsonArray(string));
        }
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        jsonObject.add(this.getProfileKey(), (JsonElement)this.toJsonArray());
    }
}

