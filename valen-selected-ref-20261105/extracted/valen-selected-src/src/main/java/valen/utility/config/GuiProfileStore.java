/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package valen.utility.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

public final class GuiProfileStore {
    private GuiProfileStore() {
    }

    public static JsonObject settings(JsonObject jsonObject) {
        JsonObject jsonObject2 = GuiProfileStore.gui(jsonObject);
        return jsonObject2.has("settings") && jsonObject2.get("settings").isJsonObject() ? jsonObject2.getAsJsonObject("settings") : new JsonObject();
    }

    public static int bind(JsonObject jsonObject, int n) {
        JsonElement jsonElement = GuiProfileStore.gui(jsonObject).get("bind");
        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            try {
                return jsonElement.getAsInt();
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
        return n;
    }

    private static JsonObject gui(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("modules") || !jsonObject.get("modules").isJsonObject()) {
            return new JsonObject();
        }
        JsonObject jsonObject2 = jsonObject.getAsJsonObject("modules");
        if (!jsonObject2.has("Gui") || !jsonObject2.get("Gui").isJsonObject()) {
            return new JsonObject();
        }
        return jsonObject2.getAsJsonObject("Gui");
    }

    public static JsonObject merge(JsonObject jsonObject, JsonObject jsonObject2) {
        return GuiProfileStore.merge(jsonObject, jsonObject2, null);
    }

    public static JsonObject merge(JsonObject jsonObject, JsonObject jsonObject2, Integer n) {
        JsonObject jsonObject3 = (JsonObject)new Gson().fromJson(jsonObject.toString(), JsonObject.class);
        JsonObject jsonObject4 = GuiProfileStore.object(jsonObject3, "modules");
        JsonObject jsonObject5 = GuiProfileStore.object(jsonObject4, "Gui");
        if (n != null) {
            jsonObject5.addProperty("bind", (Number)n);
        }
        JsonObject jsonObject6 = GuiProfileStore.object(jsonObject5, "settings");
        for (Map.Entry entry : jsonObject2.entrySet()) {
            jsonObject6.add((String)entry.getKey(), (JsonElement)entry.getValue());
        }
        return jsonObject3;
    }

    public static JsonObject withoutBind(JsonObject jsonObject) {
        JsonObject jsonObject2 = (JsonObject)new Gson().fromJson(jsonObject.toString(), JsonObject.class);
        GuiProfileStore.gui(jsonObject2).remove("bind");
        return jsonObject2;
    }

    private static JsonObject object(JsonObject jsonObject, String string) {
        if (jsonObject.has(string) && jsonObject.get(string).isJsonObject()) {
            return jsonObject.getAsJsonObject(string);
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject.add(string, (JsonElement)jsonObject2);
        return jsonObject2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write(File file, JsonObject jsonObject) throws IOException {
        Path path = file.toPath();
        Path path2 = Files.createTempFile(path.toAbsolutePath().getParent(), ".gui-", ".tmp", new FileAttribute[0]);
        try {
            Files.write(path2, new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject).getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            try {
                Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException atomicMoveNotSupportedException) {
                Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(path2);
        }
    }
}

