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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import valen.module.setting.impl.GroupSetting;
import valen.module.setting.impl.ItemListSetting;

public class InventoryItemListSetting
extends ItemListSetting {
    private static final int DEFAULT_ASSIGNED_SLOT = 1;
    private final Map<String, Integer> assignedSlots = new HashMap<String, Integer>();

    public InventoryItemListSetting(String string) {
        super(string);
    }

    public InventoryItemListSetting(String string, String ... stringArray) {
        super(string, stringArray);
    }

    public InventoryItemListSetting(GroupSetting groupSetting, String string) {
        super(groupSetting, string);
    }

    public InventoryItemListSetting(GroupSetting groupSetting, String string, String ... stringArray) {
        super(groupSetting, string, stringArray);
    }

    @Override
    public void addItem(String string) {
        if (string == null || string.isEmpty() || this.containsItem(string)) {
            return;
        }
        int n = this.nextAvailableSlot();
        super.addItem(string);
        this.assignedSlots.put(string, n);
    }

    @Override
    public void removeItem(String string) {
        super.removeItem(string);
        this.assignedSlots.remove(string);
    }

    public Integer getAssignedSlot(String string) {
        if (string == null || !this.getItems().contains(string)) {
            return null;
        }
        Integer n = this.assignedSlots.get(string);
        return n != null ? n : 1;
    }

    public void setAssignedSlot(String string, Integer n) {
        if (string == null || !this.getItems().contains(string)) {
            return;
        }
        this.assignedSlots.put(string, n == null || n < 1 || n > 9 ? 1 : n);
    }

    public boolean hasAssignedSlotConflict(String string) {
        Integer n = this.getAssignedSlot(string);
        if (n == null) {
            return false;
        }
        int n2 = 0;
        for (String string2 : this.getItems()) {
            if (!n.equals(this.getAssignedSlot(string2)) || ++n2 <= 1) continue;
            return true;
        }
        return false;
    }

    public void moveItem(String string, int n) {
        List<String> list = this.getItems();
        int n2 = list.indexOf(string);
        if (n2 < 0) {
            return;
        }
        int n3 = Math.max(0, Math.min(n, list.size() - 1));
        if (n2 == n3) {
            return;
        }
        list.remove(n2);
        if (n3 > list.size()) {
            n3 = list.size();
        }
        list.add(n3, string);
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        String string = null;
        if (jsonObject.has(this.getProfileKey())) {
            string = this.getProfileKey();
        } else if (jsonObject.has(this.getName())) {
            string = this.getName();
        } else {
            for (JsonElement jsonElement2 : this.getLegacyProfileKeys()) {
                if (!jsonObject.has((String)jsonElement2)) continue;
                string = jsonElement2;
                break;
            }
        }
        if (string == null) {
            return;
        }
        this.getItems().clear();
        this.assignedSlots.clear();
        JsonElement jsonElement = jsonObject.get(string);
        if (!jsonElement.isJsonArray()) {
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (JsonElement jsonElement2 : jsonArray) {
            int n;
            String string2;
            String string3;
            if (jsonElement2 == null || jsonElement2.isJsonNull()) continue;
            if (jsonElement2.isJsonPrimitive()) {
                string3 = jsonElement2.getAsString();
                if (string3 == null || string3.isEmpty() || this.containsItem(string3)) continue;
                int n2 = this.nextAvailableSlot();
                super.addItem(string3);
                this.assignedSlots.put(string3, n2);
                continue;
            }
            if (!jsonElement2.isJsonObject() || !(string3 = jsonElement2.getAsJsonObject()).has("id") || (string2 = string3.get("id").getAsString()) == null || string2.isEmpty() || this.containsItem(string2)) continue;
            int n3 = this.nextAvailableSlot();
            super.addItem(string2);
            if (string3.has("slot") && string3.get("slot").isJsonPrimitive() && (n = string3.get("slot").getAsInt()) >= 1 && n <= 9) {
                n3 = n;
            }
            this.assignedSlots.put(string2, n3);
        }
    }

    @Override
    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();
        for (String string : this.getItems()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", string);
            jsonObject.add("slot", (JsonElement)new JsonPrimitive((Number)this.getAssignedSlot(string)));
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    private int nextAvailableSlot() {
        for (int i = 1; i <= 9; ++i) {
            if (this.assignedSlots.containsValue(i)) continue;
            return i;
        }
        return 1;
    }
}

