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
import java.util.List;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;

public class BlockListSetting
extends Setting {
    private final List<String> blocks = new ArrayList<String>();
    private final String[] legacyProfileKeys;
    public GroupSetting group;

    public BlockListSetting(String string) {
        this(string, new String[0]);
    }

    public BlockListSetting(String string, String ... stringArray) {
        super(string);
        this.legacyProfileKeys = stringArray != null ? stringArray : new String[]{};
    }

    public BlockListSetting(GroupSetting groupSetting, String string) {
        this(groupSetting, string, new String[0]);
    }

    public BlockListSetting(GroupSetting groupSetting, String string, String ... stringArray) {
        super(string);
        this.group = groupSetting;
        this.legacyProfileKeys = stringArray != null ? stringArray : new String[]{};
    }

    public void addBlock(String string) {
        if (!this.blocks.contains(string)) {
            this.blocks.add(string);
        }
    }

    public void removeBlock(String string) {
        this.blocks.remove(string);
    }

    public List<String> getBlocks() {
        return this.blocks;
    }

    public boolean contains(String string) {
        if (this.blocks.contains(string)) {
            return true;
        }
        String string2 = BlockListSetting.extractRegistryId(string);
        return string2 != null && this.blocks.contains(string2 + ":*");
    }

    @Override
    public String getProfileKey() {
        return this.group == null ? this.getName() : this.group.getName() + "." + this.getName();
    }

    private static String extractRegistryId(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        if (string.endsWith(":*")) {
            return string.substring(0, string.length() - 2);
        }
        String[] stringArray = string.split(":");
        if (stringArray.length >= 3) {
            return stringArray[0] + ":" + stringArray[1];
        }
        if (stringArray.length == 2) {
            return string;
        }
        return null;
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        String string = null;
        if (jsonObject.has(this.getProfileKey())) {
            string = this.getProfileKey();
        } else if (jsonObject.has(this.getName())) {
            string = this.getName();
        } else {
            for (JsonElement jsonElement : this.legacyProfileKeys) {
                if (!jsonObject.has((String)jsonElement)) continue;
                string = jsonElement;
                break;
            }
        }
        if (string == null) {
            return;
        }
        this.blocks.clear();
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement.isJsonArray()) {
            for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
                this.blocks.add(jsonElement2.getAsString());
            }
        }
    }

    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();
        for (String string : this.blocks) {
            jsonArray.add((JsonElement)new JsonPrimitive(string));
        }
        return jsonArray;
    }

    protected String[] getLegacyProfileKeys() {
        return this.legacyProfileKeys;
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        jsonObject.add(this.getProfileKey(), (JsonElement)this.toJsonArray());
    }
}

