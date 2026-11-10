/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  valen.module.impl.minigames.quickbuy.QuickBuyPresetsSetting$Purchase
 */
package valen.module.impl.minigames.quickbuy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import valen.Valen;
import valen.module.impl.minigames.quickbuy.BedWarsShopItem;
import valen.module.impl.minigames.quickbuy.QuickBuyPresetsSetting;
import valen.module.setting.Setting;

/*
 * Exception performing whole class analysis ignored.
 */
public final class QuickBuyPresetsSetting
extends Setting {
    public static final int PRESET_COUNT = 5;
    private static final int MAX_PURCHASES = 24;
    private static final int MAX_COUNT = 16;
    private final List<List<Purchase>> presets = new ArrayList<List<Purchase>>();
    private int activePreset;

    public QuickBuyPresetsSetting() {
        super("Presets");
        this.visible = false;
        this.reset();
    }

    public int getActivePreset() {
        return this.activePreset;
    }

    public void setActivePreset(int n) {
        this.activePreset = Math.max(0, Math.min(4, n));
        this.changed();
    }

    public String getActivePresetName() {
        return "Preset " + (this.activePreset + 1);
    }

    public List<Purchase> getPreset(int n) {
        if (n < 0 || n >= this.presets.size()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.presets.get(n));
    }

    public List<Purchase> snapshotActivePreset() {
        ArrayList<Purchase> arrayList = new ArrayList<Purchase>();
        for (Purchase purchase : this.presets.get(this.activePreset)) {
            arrayList.add(purchase.copy());
        }
        return arrayList;
    }

    public int getCount(int n, BedWarsShopItem bedWarsShopItem) {
        if (!this.validPreset(n) || bedWarsShopItem == null) {
            return 0;
        }
        for (Purchase purchase : this.presets.get(n)) {
            if (!Purchase.access$000((Purchase)purchase).equals(bedWarsShopItem.getId())) continue;
            return Purchase.access$100((Purchase)purchase);
        }
        return 0;
    }

    public void add(int n, BedWarsShopItem bedWarsShopItem) {
        if (!this.validPreset(n) || bedWarsShopItem == null) {
            return;
        }
        List<Purchase> list = this.presets.get(n);
        for (Purchase purchase : list) {
            if (!Purchase.access$000((Purchase)purchase).equals(bedWarsShopItem.getId())) continue;
            if (bedWarsShopItem.isRepeatable()) {
                Purchase.access$102((Purchase)purchase, (int)Math.min(16, Purchase.access$100((Purchase)purchase) + 1));
                this.changed();
            }
            return;
        }
        if (list.size() >= 24) {
            return;
        }
        list.add(new Purchase(bedWarsShopItem.getId(), 1));
        this.changed();
    }

    public void increment(int n, int n2) {
        BedWarsShopItem bedWarsShopItem;
        Purchase purchase = this.purchaseAt(n, n2);
        BedWarsShopItem bedWarsShopItem2 = bedWarsShopItem = purchase == null ? null : BedWarsShopItem.byId(Purchase.access$000((Purchase)purchase));
        if (purchase == null || bedWarsShopItem == null || !bedWarsShopItem.isRepeatable() || Purchase.access$100((Purchase)purchase) >= 16) {
            return;
        }
        Purchase.access$108((Purchase)purchase);
        this.changed();
    }

    public void decrement(int n, int n2) {
        Purchase purchase = this.purchaseAt(n, n2);
        if (purchase == null) {
            return;
        }
        if (Purchase.access$100((Purchase)purchase) > 1) {
            Purchase.access$110((Purchase)purchase);
        } else {
            this.presets.get(n).remove(n2);
        }
        this.changed();
    }

    public void decrement(int n, BedWarsShopItem bedWarsShopItem) {
        if (!this.validPreset(n) || bedWarsShopItem == null) {
            return;
        }
        List<Purchase> list = this.presets.get(n);
        for (int i = 0; i < list.size(); ++i) {
            if (!Purchase.access$000((Purchase)list.get(i)).equals(bedWarsShopItem.getId())) continue;
            this.decrement(n, i);
            return;
        }
    }

    public void remove(int n, int n2) {
        if (!this.validIndex(n, n2)) {
            return;
        }
        this.presets.get(n).remove(n2);
        this.changed();
    }

    public void move(int n, int n2, int n3) {
        if (!this.validIndex(n, n2)) {
            return;
        }
        int n4 = n2 + n3;
        if (n4 < 0 || n4 >= this.presets.get(n).size()) {
            return;
        }
        Collections.swap(this.presets.get(n), n2, n4);
        this.changed();
    }

    public void clear(int n) {
        if (!this.validPreset(n) || this.presets.get(n).isEmpty()) {
            return;
        }
        this.presets.get(n).clear();
        this.changed();
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        this.reset();
        if (jsonObject == null || !jsonObject.has(this.getName()) || !jsonObject.get(this.getName()).isJsonObject()) {
            return;
        }
        JsonObject jsonObject2 = jsonObject.getAsJsonObject(this.getName());
        if (jsonObject2.has("active") && jsonObject2.get("active").isJsonPrimitive()) {
            try {
                this.activePreset = Math.max(0, Math.min(4, jsonObject2.get("active").getAsInt()));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!jsonObject2.has("presets") || !jsonObject2.get("presets").isJsonArray()) {
            return;
        }
        JsonArray jsonArray = jsonObject2.getAsJsonArray("presets");
        for (int i = 0; i < Math.min(5, jsonArray.size()); ++i) {
            JsonElement jsonElement = jsonArray.get(i);
            if (!jsonElement.isJsonArray()) continue;
            JsonArray jsonArray2 = jsonElement.getAsJsonArray();
            for (JsonElement jsonElement2 : jsonArray2) {
                String string;
                BedWarsShopItem bedWarsShopItem;
                JsonObject jsonObject3;
                if (!jsonElement2.isJsonObject() || this.presets.get(i).size() >= 24 || !(jsonObject3 = jsonElement2.getAsJsonObject()).has("item") || !jsonObject3.get("item").isJsonPrimitive() || (bedWarsShopItem = BedWarsShopItem.byId(string = jsonObject3.get("item").getAsString())) == null || this.contains(i, string)) continue;
                int n = 1;
                if (jsonObject3.has("count") && jsonObject3.get("count").isJsonPrimitive()) {
                    try {
                        n = Math.max(1, Math.min(16, jsonObject3.get("count").getAsInt()));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (!bedWarsShopItem.isRepeatable()) {
                    n = 1;
                }
                this.presets.get(i).add(new Purchase(string, n));
            }
        }
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("active", (Number)this.activePreset);
        JsonArray jsonArray = new JsonArray();
        for (List<Purchase> list : this.presets) {
            JsonArray jsonArray2 = new JsonArray();
            for (Purchase purchase : list) {
                JsonObject jsonObject3 = new JsonObject();
                jsonObject3.addProperty("item", Purchase.access$000((Purchase)purchase));
                jsonObject3.addProperty("count", (Number)Purchase.access$100((Purchase)purchase));
                jsonArray2.add((JsonElement)jsonObject3);
            }
            jsonArray.add((JsonElement)jsonArray2);
        }
        jsonObject2.add("presets", (JsonElement)jsonArray);
        jsonObject.add(this.getName(), (JsonElement)jsonObject2);
    }

    private void reset() {
        this.activePreset = 0;
        this.presets.clear();
        for (int i = 0; i < 5; ++i) {
            this.presets.add(new ArrayList());
        }
    }

    private boolean contains(int n, String string) {
        for (Purchase purchase : this.presets.get(n)) {
            if (!Purchase.access$000((Purchase)purchase).equals(string)) continue;
            return true;
        }
        return false;
    }

    private Purchase purchaseAt(int n, int n2) {
        return this.validIndex(n, n2) ? this.presets.get(n).get(n2) : null;
    }

    private boolean validPreset(int n) {
        return n >= 0 && n < this.presets.size();
    }

    private boolean validIndex(int n, int n2) {
        return this.validPreset(n) && n2 >= 0 && n2 < this.presets.get(n).size();
    }

    private void changed() {
        if (Valen.configManager != null) {
            Valen.configManager.invalidateDirtyCache();
        }
    }
}

