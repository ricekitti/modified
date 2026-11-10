/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package valen.utility.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import valen.utility.config.ConfigManager;

public final class PremadeProfiles {
    private PremadeProfiles() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static List<Entry> load() {
        ArrayList<Entry> arrayList = new ArrayList<Entry>();
        try (InputStream inputStream = PremadeProfiles.class.getResourceAsStream("/assets/valen/profiles/index.json");){
            if (inputStream == null) {
                ArrayList<Entry> arrayList2 = arrayList;
                return arrayList2;
            }
            JsonArray jsonArray = (JsonArray)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonArray.class);
            Iterator iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                JsonElement jsonElement = (JsonElement)iterator.next();
                try {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String string = jsonObject.get("id").getAsString();
                    String string2 = ConfigManager.normalizeName(jsonObject.get("name").getAsString());
                    if (!string.matches("[a-z0-9-]+") || string2 == null) continue;
                    InputStream inputStream2 = PremadeProfiles.class.getResourceAsStream("/assets/valen/profiles/" + string + ".json");
                    Throwable throwable = null;
                    try {
                        JsonObject jsonObject2;
                        if (inputStream2 == null || (jsonObject2 = (JsonObject)new Gson().fromJson((Reader)new InputStreamReader(inputStream2, StandardCharsets.UTF_8), JsonObject.class)) == null || !jsonObject2.has("modules") || !jsonObject2.get("modules").isJsonObject()) continue;
                        arrayList.add(new Entry(string, string2, jsonObject.get("description").getAsString(), jsonObject2));
                    }
                    catch (Throwable throwable2) {
                        throwable = throwable2;
                        throw throwable2;
                    }
                    finally {
                        if (inputStream2 == null) continue;
                        if (throwable != null) {
                            try {
                                inputStream2.close();
                            }
                            catch (Throwable throwable3) {
                                throwable.addSuppressed(throwable3);
                            }
                            continue;
                        }
                        inputStream2.close();
                    }
                }
                catch (Exception exception) {}
            }
            return Collections.unmodifiableList(arrayList);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Collections.unmodifiableList(arrayList);
    }

    public static final class Entry {
        public final String id;
        public final String name;
        public final String description;
        public final JsonObject profile;

        private Entry(String string, String string2, String string3, JsonObject jsonObject) {
            this.id = string;
            this.name = string2;
            this.description = string3;
            this.profile = jsonObject;
        }
    }
}

