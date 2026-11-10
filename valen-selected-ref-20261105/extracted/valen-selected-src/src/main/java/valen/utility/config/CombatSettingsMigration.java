/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 */
package valen.utility.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import valen.utility.config.ConfigManager;

final class CombatSettingsMigration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    CombatSettingsMigration() {
    }

    static void run(File file2) {
        File file3 = new File(file2, ".combat-settings-2026-10-09");
        File file4 = new File(file3, "complete");
        if (file4.isFile()) {
            return;
        }
        File[] fileArray = file2.listFiles((file, string) -> string.toLowerCase(Locale.ROOT).endsWith(".json"));
        if (fileArray == null) {
            return;
        }
        boolean bl = true;
        try {
            Files.createDirectories(file3.toPath(), new FileAttribute[0]);
            for (File file5 : fileArray) {
                File file6;
                if (!file5.isFile() || (file6 = new File(file3, file5.getName() + ".done")).isFile()) continue;
                try {
                    String string2 = new String(Files.readAllBytes(file5.toPath()), StandardCharsets.UTF_8);
                    JsonObject jsonObject = (JsonObject)GSON.fromJson(string2, JsonObject.class);
                    if (jsonObject == null || CombatSettingsMigration.object(jsonObject, "modules") == null) {
                        throw new IllegalArgumentException("Invalid profile structure");
                    }
                    if (CombatSettingsMigration.update(jsonObject)) {
                        File file7 = new File(file3, file5.getName() + ".bak");
                        if (!file7.exists()) {
                            Files.copy(file5.toPath(), file7.toPath(), new CopyOption[0]);
                        }
                        ConfigManager.writeAtomically(file5, GSON.toJson((JsonElement)jsonObject));
                    }
                    ConfigManager.writeAtomically(file6, "complete");
                }
                catch (Exception exception) {
                    bl = false;
                    System.err.println("[Valen] Could not migrate profile " + file5.getName() + ": " + exception.getClass().getSimpleName());
                }
            }
            if (bl) {
                ConfigManager.writeAtomically(file4, "complete");
            }
        }
        catch (Exception exception) {
            System.err.println("[Valen] Could not finish combat settings migration: " + exception.getClass().getSimpleName());
        }
    }

    static boolean update(JsonObject jsonObject) {
        JsonElement jsonElement;
        JsonObject jsonObject2 = CombatSettingsMigration.object(jsonObject, "modules");
        if (jsonObject2 == null) {
            return false;
        }
        boolean bl = false;
        JsonObject jsonObject3 = CombatSettingsMigration.object(CombatSettingsMigration.object(jsonObject2, "Aim Assist"), "settings");
        if (jsonObject3 != null) {
            jsonElement = jsonObject3.get("Stop when breaking");
            JsonElement jsonElement2 = jsonObject3.get("Hover delay");
            if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean() && jsonElement.getAsBoolean() && jsonElement2 != null && jsonElement2.isJsonPrimitive() && jsonElement2.getAsJsonPrimitive().isNumber() && jsonElement2.getAsDouble() < 50.0) {
                jsonObject3.addProperty("Hover delay", (Number)50);
                bl = true;
            }
        }
        if ((jsonElement = CombatSettingsMigration.object(CombatSettingsMigration.object(jsonObject2, "Auto Clicker"), "settings")) != null && jsonElement.has("Not using item") && !new JsonPrimitive(Boolean.valueOf(false)).equals((Object)jsonElement.get("Not using item"))) {
            jsonElement.addProperty("Not using item", Boolean.valueOf(false));
            bl = true;
        }
        return bl;
    }

    private static JsonObject object(JsonObject jsonObject, String string) {
        return jsonObject != null && jsonObject.has(string) && jsonObject.get(string).isJsonObject() ? jsonObject.getAsJsonObject(string) : null;
    }
}

