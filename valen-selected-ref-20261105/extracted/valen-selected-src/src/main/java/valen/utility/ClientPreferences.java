/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  net.minecraft.client.Minecraft
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 */
package valen.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import valen.Valen;
import valen.helper.MouseHelper;
import valen.module.Module;
import valen.module.impl.render.Notifications;
import valen.utility.RecentColors;
import valen.utility.config.ClientConfig;

public final class ClientPreferences {
    private final File file;
    private final Set<String> favorites = new LinkedHashSet<String>();
    private final Map<String, Integer> profileBinds = new LinkedHashMap<String, Integer>();
    private final RecentColors recentColors = new RecentColors();
    private Integer guiBind;
    private int heldProfileBind;
    private int revision;

    public ClientPreferences() {
        File file = new File(Minecraft.getMinecraft().mcDataDir, "valen");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.file = new File(file, "preferences.json");
        this.load();
    }

    public boolean isFavorite(Module module) {
        return module != null && this.favorites.contains(ClientPreferences.normalize(module.getName()));
    }

    public void toggleFavorite(Module module) {
        if (module == null) {
            return;
        }
        String string = ClientPreferences.normalize(module.getName());
        if (!this.favorites.remove(string)) {
            this.favorites.add(string);
        }
        ++this.revision;
        this.save();
        if (Valen.clickGui != null) {
            Valen.clickGui.refreshFavorites();
        }
    }

    public List<Module> getFavoriteModules() {
        ArrayList<Module> arrayList = new ArrayList<Module>();
        if (Valen.moduleManager == null) {
            return arrayList;
        }
        for (Module module : Valen.moduleManager.getModules()) {
            if (!this.isFavorite(module)) continue;
            arrayList.add(module);
        }
        return arrayList;
    }

    public Integer getGuiBind() {
        return this.guiBind;
    }

    public void setGuiBind(int n) {
        if (this.guiBind != null && this.guiBind == n) {
            return;
        }
        this.guiBind = n;
        ++this.revision;
        this.save();
    }

    public int getProfileBind(String string) {
        if (string == null) {
            return 0;
        }
        Integer n = this.profileBinds.get(ClientPreferences.normalize(string));
        return n == null ? 0 : n;
    }

    public String setProfileBind(String string, int n) {
        String string2 = ClientPreferences.normalize(string);
        if (n == 1000) {
            n = 0;
        }
        String string3 = null;
        if (n > 0) {
            for (Map.Entry<String, Integer> entry : this.profileBinds.entrySet()) {
                if (entry.getKey().equals(string2) || entry.getValue() != n) continue;
                string3 = entry.getKey();
                entry.setValue(0);
                break;
            }
        }
        if (n <= 0) {
            this.profileBinds.remove(string2);
        } else {
            this.profileBinds.put(string2, n);
        }
        ++this.revision;
        this.save();
        return string3;
    }

    public void renameProfile(String string, String string2) {
        String string3 = ClientPreferences.normalize(string);
        Integer n = this.profileBinds.remove(string3);
        if (n != null && n > 0) {
            this.profileBinds.put(ClientPreferences.normalize(string2), n);
        }
        ++this.revision;
        this.save();
    }

    public void deleteProfile(String string) {
        if (this.profileBinds.remove(ClientPreferences.normalize(string)) != null) {
            ++this.revision;
            this.save();
        }
    }

    public int pollProfileBind() {
        if (this.heldProfileBind != 0) {
            if (ClientPreferences.isKeyDown(this.heldProfileBind)) {
                return 0;
            }
            this.heldProfileBind = 0;
        }
        for (Map.Entry<String, Integer> entry : this.profileBinds.entrySet()) {
            int n = entry.getValue();
            if (n <= 0 || !ClientPreferences.isKeyDown(n)) continue;
            this.heldProfileBind = n;
            String string = this.findProfileName(entry.getKey());
            if (string != null && Valen.configManager.loadFromBind(string)) {
                Notifications.pushMessage("Loaded " + string + " profile");
            }
            return n;
        }
        return 0;
    }

    public List<Integer> getRecentColors() {
        return this.recentColors.list();
    }

    public void pushRecentColor(int n) {
        if (this.recentColors.push(n)) {
            ++this.revision;
            this.save();
        }
    }

    public int getRevision() {
        return this.revision;
    }

    private String findProfileName(String string) {
        if (Valen.configManager == null) {
            return null;
        }
        for (ClientConfig clientConfig : Valen.configManager.getConfigs()) {
            if (!ClientPreferences.normalize(clientConfig.getName()).equals(string)) continue;
            return clientConfig.getName();
        }
        return null;
    }

    private static boolean isKeyDown(int n) {
        if (n == 1069 || n == 1070) {
            return MouseHelper.isScrollDown(n);
        }
        if (n >= 1000) {
            int n2 = n - 1000;
            return n2 >= 0 && n2 < Mouse.getButtonCount() && Mouse.isButtonDown((int)n2);
        }
        return n < 256 && Keyboard.isKeyDown((int)n);
    }

    private void load() {
        if (!this.file.isFile()) {
            return;
        }
        try {
            String string = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
            JsonElement jsonElement = new JsonParser().parse(string);
            if (!jsonElement.isJsonObject()) {
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("favorites") && jsonObject.get("favorites").isJsonArray()) {
                for (Object object : jsonObject.getAsJsonArray("favorites")) {
                    if (!object.isJsonPrimitive()) continue;
                    this.favorites.add(ClientPreferences.normalize(object.getAsString()));
                }
            }
            if (jsonObject.has("guiBind") && jsonObject.get("guiBind").isJsonPrimitive()) {
                this.guiBind = jsonObject.get("guiBind").getAsInt();
            }
            if (jsonObject.has("recentColors") && jsonObject.get("recentColors").isJsonArray()) {
                this.recentColors.load(jsonObject.getAsJsonArray("recentColors"));
            }
            if (jsonObject.has("profileBinds") && jsonObject.get("profileBinds").isJsonObject()) {
                for (Object object : jsonObject.getAsJsonObject("profileBinds").entrySet()) {
                    int n;
                    if (!((JsonElement)object.getValue()).isJsonPrimitive() || (n = ((JsonElement)object.getValue()).getAsInt()) <= 0 || n == 1000) continue;
                    this.profileBinds.put(ClientPreferences.normalize((String)object.getKey()), n);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void save() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (String object : this.favorites) {
            jsonArray.add((JsonElement)new JsonPrimitive(object));
        }
        jsonObject.add("favorites", (JsonElement)jsonArray);
        JsonObject jsonObject2 = new JsonObject();
        for (Map.Entry<String, Integer> entry : this.profileBinds.entrySet()) {
            if (entry.getValue() <= 0) continue;
            jsonObject2.addProperty(entry.getKey(), (Number)entry.getValue());
        }
        jsonObject.add("profileBinds", (JsonElement)jsonObject2);
        if (this.guiBind != null) {
            jsonObject.addProperty("guiBind", (Number)this.guiBind);
        }
        jsonObject.add("recentColors", (JsonElement)this.recentColors.toJson());
        try {
            File file = new File(this.file.getParentFile(), this.file.getName() + ".tmp");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.write(file.toPath(), gson.toJson((JsonElement)jsonObject).getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            try {
                Files.move(file.toPath(), this.file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (Exception exception) {
                Files.move(file.toPath(), this.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String normalize(String string) {
        return string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
    }
}

