/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  valen.utility.config.ProfileRecovery
 */
package valen.utility.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import valen.Valen;
import valen.module.Module;
import valen.module.impl.client.Gui;
import valen.module.impl.render.Notifications;
import valen.module.setting.Setting;
import valen.utility.IMinecraftInstance;
import valen.utility.config.ClientConfig;
import valen.utility.config.CombatSettingsMigration;
import valen.utility.config.GuiProfileStore;
import valen.utility.config.PremadeProfiles;
import valen.utility.config.ProfileRecovery;

public final class ConfigManager
implements IMinecraftInstance {
    public static final int MAX_NAME_LENGTH = 24;
    public static final String DEFAULT_NAME = "Default";
    private static final long DIRTY_RECHECK_INTERVAL_MS = 200L;
    private static final long DIRECTORY_SCAN_INTERVAL_MS = 400L;
    private final List<ClientConfig> configs = new ArrayList<ClientConfig>();
    private final String pristineJson;
    private String activeName;
    private String activeSnapshotJson;
    private boolean dirty;
    private boolean guiSavePending;
    private boolean guiSaveFailed;
    private long guiLastChangeNs;
    private long guiLastSaveAttemptNs;
    private long lastDirtyCheckMs;
    private long lastScanMs;
    private String directorySignature = "";
    private Module resetModule;
    private JsonObject resetBefore;
    private JsonObject resetAfter;
    private String resetProfile;

    private ProfileRecovery recovery() {
        return new ProfileRecovery(new File(ConfigManager.directory(), ".deleted"));
    }

    public boolean hasDeletedProfile() {
        return this.recovery().latest() != null;
    }

    public String restoreDeletedProfile() {
        File file = this.recovery().latest();
        if (file == null) {
            return null;
        }
        try {
            JsonObject jsonObject = this.recovery().read(file);
            String string = this.uniqueName(jsonObject.get("name").getAsString());
            if (this.exists(string)) {
                return null;
            }
            String string2 = jsonObject.get("profile").getAsString();
            JsonObject jsonObject2 = (JsonObject)new Gson().fromJson(string2, JsonObject.class);
            if (jsonObject2 == null || !jsonObject2.has("modules") || !jsonObject2.get("modules").isJsonObject()) {
                return null;
            }
            ConfigManager.writeNewProfile(ConfigManager.fileFor(string), string2);
            Files.delete(file.toPath());
            this.refresh();
            int n = jsonObject.get("bind").getAsInt();
            if (Valen.preferences != null && n > 0 && !this.hasProfileBind(n)) {
                Valen.preferences.setProfileBind(string, n);
            }
            return string;
        }
        catch (Exception exception) {
            this.refresh();
            return null;
        }
    }

    private boolean hasProfileBind(int n) {
        for (ClientConfig clientConfig : this.configs) {
            if (Valen.preferences.getProfileBind(clientConfig.getName()) != n) continue;
            return true;
        }
        return false;
    }

    private JsonObject moduleSettings(Module module) {
        JsonObject jsonObject = new JsonObject();
        for (Setting setting : module.getSettings()) {
            setting.writeProfile(jsonObject);
        }
        return jsonObject;
    }

    public boolean canUndoReset(Module module) {
        return module != null && this.resetModule == module && this.resetBefore != null && Objects.equals(this.activeName, this.resetProfile) && this.moduleSettings(module).equals((Object)this.resetAfter);
    }

    public boolean undoModuleReset(Module module) {
        if (!this.canUndoReset(module)) {
            return false;
        }
        for (Setting setting : module.getSettings()) {
            setting.loadProfile(this.resetBefore);
        }
        this.resetModule = null;
        this.resetBefore = null;
        this.resetAfter = null;
        this.invalidateDirtyCache();
        return true;
    }

    public ConfigManager() {
        CombatSettingsMigration.run(ConfigManager.directory());
        this.pristineJson = ConfigManager.serialize(this.defaultSnapshot());
        this.activeName = ConfigManager.readActiveName();
        this.refresh();
        if (this.activeName != null) {
            ClientConfig clientConfig = this.find(this.activeName);
            String string = this.activeSnapshotJson = clientConfig == null ? null : ConfigManager.readRaw(clientConfig.getFile());
            if (clientConfig == null) {
                this.activeName = null;
            }
        }
    }

    public static boolean isDefault(String string) {
        return DEFAULT_NAME.equalsIgnoreCase(ConfigManager.normalizeName(string));
    }

    public static boolean isDefault(ClientConfig clientConfig) {
        return clientConfig != null && ConfigManager.isDefault(clientConfig.getName());
    }

    private static File directory() {
        File file = new File(ConfigManager.mc.mcDataDir, "valen" + File.separator + "configs");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public void openFolder() {
        File file = ConfigManager.directory();
        Valen.getCachedExecutor().execute(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    private static File activeMarker() {
        File file = new File(ConfigManager.mc.mcDataDir, "valen");
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(file, "active_config.txt");
    }

    private static File fileFor(String string) {
        return new File(ConfigManager.directory(), ConfigManager.normalizeName(string) + ".json");
    }

    public List<ClientConfig> getConfigs() {
        return this.configs;
    }

    public boolean isEmpty() {
        return this.configs.isEmpty();
    }

    public void refresh() {
        File[] fileArray = ConfigManager.directory().listFiles();
        this.rebuild(fileArray, ConfigManager.signatureOf(fileArray));
        this.lastScanMs = System.currentTimeMillis();
        this.ensureDefaultConfig();
    }

    public void refreshIfChanged() {
        long l = System.currentTimeMillis();
        if (l - this.lastScanMs < 400L) {
            return;
        }
        this.lastScanMs = l;
        File[] fileArray = ConfigManager.directory().listFiles();
        String string = ConfigManager.signatureOf(fileArray);
        if (!string.equals(this.directorySignature)) {
            this.rebuild(fileArray, string);
            this.ensureDefaultConfig();
        }
    }

    private void ensureDefaultConfig() {
        if (this.pristineJson == null || this.find(DEFAULT_NAME) != null) {
            return;
        }
        try {
            ConfigManager.writeAtomically(ConfigManager.fileFor(DEFAULT_NAME), this.pristineJson);
        }
        catch (Exception exception) {
            return;
        }
        File[] fileArray = ConfigManager.directory().listFiles();
        this.rebuild(fileArray, ConfigManager.signatureOf(fileArray));
    }

    private void rebuild(File[] fileArray, String string) {
        this.configs.clear();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (!ConfigManager.isConfigFile(file)) continue;
                String string2 = file.getName().substring(0, file.getName().length() - 5);
                this.configs.add(new ClientConfig(string2, file, file.lastModified(), ConfigManager.countEnabledModules(file)));
            }
        }
        this.configs.sort(ConfigManager::compareForListing);
        this.directorySignature = string;
    }

    private static int compareForListing(ClientConfig clientConfig, ClientConfig clientConfig2) {
        boolean bl;
        boolean bl2 = ConfigManager.isDefault(clientConfig);
        if (bl2 != (bl = ConfigManager.isDefault(clientConfig2))) {
            return bl2 ? -1 : 1;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(clientConfig.getName(), clientConfig2.getName());
    }

    private static boolean isConfigFile(File file) {
        return file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private static String signatureOf(File[] fileArray) {
        if (fileArray == null) {
            return "";
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (File file : fileArray) {
            if (!ConfigManager.isConfigFile(file)) continue;
            arrayList.add(file.getName() + ':' + file.lastModified() + ':' + file.length());
        }
        Collections.sort(arrayList);
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : arrayList) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('|');
            }
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    public ClientConfig find(String string) {
        String string2 = ConfigManager.normalizeName(string);
        if (string2 == null) {
            return null;
        }
        for (ClientConfig clientConfig : this.configs) {
            if (!clientConfig.getName().equalsIgnoreCase(string2)) continue;
            return clientConfig;
        }
        return null;
    }

    public boolean exists(String string) {
        return this.find(string) != null;
    }

    public String getActiveName() {
        return this.activeName;
    }

    public boolean isActive(ClientConfig clientConfig) {
        return clientConfig != null && this.activeName != null && clientConfig.getName().equalsIgnoreCase(this.activeName);
    }

    public boolean isDirty() {
        if (this.activeName == null || this.activeSnapshotJson == null) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastDirtyCheckMs >= 200L) {
            this.lastDirtyCheckMs = l;
            this.dirty = !this.activeSnapshotJson.equals(ConfigManager.serialize(this.snapshot()));
        }
        return this.dirty;
    }

    public void invalidateDirtyCache() {
        this.lastDirtyCheckMs = 0L;
    }

    private void setActive(String string, String string2) {
        this.activeName = string;
        this.activeSnapshotJson = string2;
        this.dirty = false;
        this.lastDirtyCheckMs = System.currentTimeMillis();
        ConfigManager.writeActiveName(string);
    }

    public boolean save(String string) {
        if (!this.flushGuiAutosave()) {
            return false;
        }
        String string2 = ConfigManager.normalizeName(string);
        if (string2 == null) {
            return false;
        }
        JsonObject jsonObject = this.snapshot();
        File file = ConfigManager.fileFor(string2);
        try {
            JsonObject jsonObject2;
            JsonObject jsonObject3 = (JsonObject)new Gson().fromJson(ConfigManager.serialize(jsonObject), JsonObject.class);
            if (file.exists() && (jsonObject2 = (JsonObject)new Gson().fromJson(ConfigManager.readRaw(file), JsonObject.class)) != null && jsonObject2.has("premadeId")) {
                jsonObject3.add("premadeId", jsonObject2.get("premadeId"));
            }
            ConfigManager.writeAtomically(file, ConfigManager.serialize(jsonObject3));
        }
        catch (Exception exception) {
            return false;
        }
        this.refresh();
        this.setActive(string2, ConfigManager.serialize(jsonObject));
        return true;
    }

    public void queueGuiAutosave() {
        this.guiSavePending = true;
        this.guiLastChangeNs = System.nanoTime();
        this.invalidateDirtyCache();
    }

    public boolean hasPendingGuiAutosave() {
        return this.guiSavePending;
    }

    public boolean guiAutosaveFailed() {
        return this.guiSaveFailed;
    }

    public void tickGuiAutosave() {
        long l = System.nanoTime();
        if (this.guiSavePending && l - this.guiLastChangeNs >= 250000000L && (!this.guiSaveFailed || l - this.guiLastSaveAttemptNs >= 1000000000L)) {
            this.flushGuiAutosave();
        }
    }

    public boolean flushGuiAutosave() {
        if (!this.guiSavePending) {
            return true;
        }
        this.guiLastSaveAttemptNs = System.nanoTime();
        ClientConfig clientConfig = this.find(this.activeName == null ? DEFAULT_NAME : this.activeName);
        if (clientConfig == null) {
            this.guiSaveFailed = true;
            return false;
        }
        try {
            JsonObject jsonObject = (JsonObject)new Gson().fromJson(ConfigManager.readRaw(clientConfig.getFile()), JsonObject.class);
            if (jsonObject == null) {
                this.guiSaveFailed = true;
                return false;
            }
            JsonObject jsonObject2 = Gui.appearance.snapshot();
            JsonObject jsonObject3 = GuiProfileStore.withoutBind(GuiProfileStore.merge(jsonObject, jsonObject2));
            GuiProfileStore.write(clientConfig.getFile(), jsonObject3);
            if (this.activeName != null && this.activeSnapshotJson != null) {
                JsonObject jsonObject4 = (JsonObject)new Gson().fromJson(this.activeSnapshotJson, JsonObject.class);
                this.activeSnapshotJson = ConfigManager.serialize(GuiProfileStore.withoutBind(GuiProfileStore.merge(jsonObject4, jsonObject2)));
            }
            this.guiSavePending = false;
            this.guiSaveFailed = false;
            this.invalidateDirtyCache();
            this.refresh();
            return true;
        }
        catch (Exception exception) {
            this.guiSaveFailed = true;
            return false;
        }
    }

    public boolean saveActive() {
        return this.activeName != null && this.save(this.activeName);
    }

    public boolean load(String string) {
        if (this.isDirty() && !this.saveActive()) {
            return false;
        }
        return this.load(string, true);
    }

    public boolean loadFromBind(String string) {
        return this.load(string);
    }

    public void loadActiveOnStartup() {
        ClientConfig clientConfig = this.find(this.activeName != null ? this.activeName : DEFAULT_NAME);
        JsonObject jsonObject = null;
        if (clientConfig != null) {
            try {
                jsonObject = (JsonObject)new Gson().fromJson(ConfigManager.readRaw(clientConfig.getFile()), JsonObject.class);
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
        if (this.activeName != null) {
            this.load(this.activeName, false);
        } else if (jsonObject != null) {
            Gui.appearance.load(GuiProfileStore.settings(jsonObject));
            Gui.appearance.apply();
        }
        this.restoreGuiBind(jsonObject);
    }

    private void restoreGuiBind(JsonObject jsonObject) {
        Module module = Module.getModule(Gui.class);
        if (module == null || Valen.preferences == null) {
            return;
        }
        Integer n = Valen.preferences.getGuiBind();
        module.setBind(n != null ? n : GuiProfileStore.bind(jsonObject, 54));
    }

    private boolean load(String string, boolean bl) {
        JsonObject jsonObject;
        if (!this.flushGuiAutosave()) {
            return false;
        }
        ClientConfig clientConfig = this.find(string);
        if (clientConfig == null) {
            return false;
        }
        String string2 = ConfigManager.readRaw(clientConfig.getFile());
        if (string2 == null) {
            return false;
        }
        try {
            jsonObject = (JsonObject)new Gson().fromJson(string2, JsonObject.class);
        }
        catch (Exception exception) {
            return false;
        }
        if (jsonObject == null) {
            return false;
        }
        this.resetModule = null;
        this.apply(ConfigManager.mergeWithDefaults(this.readPristine(), jsonObject));
        this.setActive(clientConfig.getName(), ConfigManager.serialize(this.snapshot()));
        if (bl && Valen.clickGui != null) {
            Valen.clickGui.refreshAfterProfileLoad();
        }
        return true;
    }

    public boolean rename(String string, String string2) {
        if (!this.flushGuiAutosave()) {
            return false;
        }
        if (ConfigManager.isDefault(string)) {
            return false;
        }
        ClientConfig clientConfig = this.find(string);
        String string3 = ConfigManager.normalizeName(string2);
        if (clientConfig == null || string3 == null) {
            return false;
        }
        if (string3.equalsIgnoreCase(clientConfig.getName())) {
            return true;
        }
        if (this.exists(string3)) {
            return false;
        }
        File file = ConfigManager.fileFor(string3);
        try {
            ConfigManager.move(clientConfig.getFile().toPath(), file.toPath());
        }
        catch (IOException iOException) {
            return false;
        }
        boolean bl = this.isActive(clientConfig);
        this.refresh();
        if (bl) {
            this.setActive(string3, this.activeSnapshotJson);
        }
        if (Valen.preferences != null) {
            Valen.preferences.renameProfile(string, string3);
        }
        return true;
    }

    public boolean delete(String string) {
        if (!this.flushGuiAutosave()) {
            return false;
        }
        if (ConfigManager.isDefault(string)) {
            return false;
        }
        ClientConfig clientConfig = this.find(string);
        if (clientConfig == null) {
            return false;
        }
        boolean bl = this.isActive(clientConfig);
        if (bl && this.isDirty() && !this.saveActive()) {
            return false;
        }
        String string2 = ConfigManager.readRaw(clientConfig.getFile());
        if (string2 == null) {
            return false;
        }
        try {
            this.recovery().store(clientConfig.getName(), string2, Valen.preferences == null ? 0 : Valen.preferences.getProfileBind(string));
            Files.delete(clientConfig.getFile().toPath());
        }
        catch (Exception exception) {
            return false;
        }
        this.refresh();
        if (bl) {
            this.setActive(null, null);
        }
        if (Valen.preferences != null) {
            Valen.preferences.deleteProfile(string);
        }
        return true;
    }

    public String findPremadeCopy(String string) {
        for (ClientConfig clientConfig : this.configs) {
            try {
                JsonObject jsonObject = (JsonObject)new Gson().fromJson(ConfigManager.readRaw(clientConfig.getFile()), JsonObject.class);
                if (jsonObject == null || !jsonObject.has("premadeId") || !string.equals(jsonObject.get("premadeId").getAsString())) continue;
                return clientConfig.getName();
            }
            catch (Exception exception) {
            }
        }
        return null;
    }

    public String addPremade(PremadeProfiles.Entry entry) {
        String string = this.findPremadeCopy(entry.id);
        if (string != null) {
            return string;
        }
        String string2 = this.uniqueName(entry.name);
        if (this.exists(string2)) {
            return null;
        }
        JsonObject jsonObject = ConfigManager.mergeWithDefaults(this.readPristine(), entry.profile);
        jsonObject.addProperty("premadeId", entry.id);
        try {
            ConfigManager.writeNewProfile(ConfigManager.fileFor(string2), ConfigManager.serialize(jsonObject));
            this.refresh();
            return string2;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public String duplicate(String string) {
        if (!this.flushGuiAutosave()) {
            return null;
        }
        ClientConfig clientConfig = this.find(string);
        if (this.isActive(clientConfig) && this.isDirty() && !this.saveActive()) {
            return null;
        }
        ClientConfig clientConfig2 = this.find(string);
        if (clientConfig2 == null) {
            return null;
        }
        String string2 = ConfigManager.readRaw(clientConfig2.getFile());
        if (string2 == null) {
            return null;
        }
        String string3 = this.uniqueName(clientConfig2.getName() + " copy");
        try {
            ConfigManager.writeAtomically(ConfigManager.fileFor(string3), string2);
        }
        catch (Exception exception) {
            return null;
        }
        this.refresh();
        return string3;
    }

    public String uniqueName(String string) {
        String string2 = ConfigManager.normalizeName(string);
        if (string2 == null) {
            string2 = "Config";
        }
        if (!this.exists(string2)) {
            return string2;
        }
        for (int i = 2; i < 1000; ++i) {
            String string3 = " " + i;
            String string4 = string2.length() + string3.length() > 24 ? string2.substring(0, 24 - string3.length()) : string2;
            String string5 = string4 + string3;
            if (this.exists(string5)) continue;
            return string5;
        }
        return string2;
    }

    public JsonObject snapshot() {
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonObject2 = new JsonObject();
        for (Module module : this.allModules()) {
            if (module.ignoreOnSave) continue;
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("enabled", Boolean.valueOf(module.isEnabled()));
            if (!(module instanceof Gui)) {
                jsonObject3.addProperty("bind", (Number)module.getKeycode());
            }
            jsonObject3.addProperty("hidden", Boolean.valueOf(module.isHidden()));
            JsonObject jsonObject4 = new JsonObject();
            for (Setting setting : module.getSettings()) {
                setting.writeProfile(jsonObject4);
            }
            jsonObject3.add("settings", (JsonElement)jsonObject4);
            jsonObject2.add(module.getName(), (JsonElement)jsonObject3);
        }
        jsonObject.add("modules", (JsonElement)jsonObject2);
        return jsonObject;
    }

    private JsonObject defaultSnapshot() {
        JsonObject jsonObject = this.snapshot();
        if (!jsonObject.has("modules") || !jsonObject.get("modules").isJsonObject()) {
            return jsonObject;
        }
        for (Map.Entry entry : jsonObject.getAsJsonObject("modules").entrySet()) {
            if (!((JsonElement)entry.getValue()).isJsonObject()) continue;
            ((JsonElement)entry.getValue()).getAsJsonObject().addProperty("enabled", Boolean.valueOf("Notifications".equals(entry.getKey()) || "Teams".equals(entry.getKey())));
        }
        return jsonObject;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void apply(JsonObject jsonObject) {
        JsonObject jsonObject2 = this.readPristine();
        Gui.appearance.load(GuiProfileStore.settings(jsonObject2));
        Gui.appearance.load(GuiProfileStore.settings(jsonObject));
        Gui.appearance.apply();
        if (!jsonObject.has("modules") || !jsonObject.get("modules").isJsonObject()) {
            return;
        }
        JsonObject jsonObject3 = jsonObject.getAsJsonObject("modules");
        Notifications.beginSuppress();
        try {
            if (jsonObject2.has("modules") && jsonObject2.get("modules").isJsonObject()) {
                this.applyDefaultSettings(jsonObject2.getAsJsonObject("modules"));
            }
            this.applyModules(jsonObject3);
            Gui.appearance.apply();
        }
        finally {
            Notifications.endSuppress();
        }
    }

    private void applyModules(JsonObject jsonObject) {
        for (Module module : this.allModules()) {
            if (module.ignoreOnSave || !jsonObject.has(module.getName()) || !jsonObject.get(module.getName()).isJsonObject()) continue;
            JsonObject jsonObject2 = jsonObject.getAsJsonObject(module.getName());
            if (jsonObject2.has("settings") && jsonObject2.get("settings").isJsonObject()) {
                JsonObject jsonObject3 = jsonObject2.getAsJsonObject("settings");
                for (Setting setting : module.getSettings()) {
                    try {
                        setting.loadProfile(jsonObject3);
                    }
                    catch (Exception exception) {}
                }
            }
            if (!(module instanceof Gui) && jsonObject2.has("bind") && jsonObject2.get("bind").isJsonPrimitive()) {
                try {
                    module.setBind(jsonObject2.get("bind").getAsInt());
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (jsonObject2.has("hidden") && jsonObject2.get("hidden").isJsonPrimitive()) {
                try {
                    module.setHidden(jsonObject2.get("hidden").getAsBoolean());
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (!jsonObject2.has("enabled") || !jsonObject2.get("enabled").isJsonPrimitive()) continue;
            boolean bl = false;
            try {
                bl = jsonObject2.get("enabled").getAsBoolean();
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (bl && !module.isEnabled()) {
                module.enable();
                continue;
            }
            if (bl || !module.isEnabled()) continue;
            module.disable();
        }
    }

    private void applyDefaultSettings(JsonObject jsonObject) {
        for (Module module : this.allModules()) {
            JsonObject jsonObject2;
            if (module.ignoreOnSave || !jsonObject.has(module.getName()) || !jsonObject.get(module.getName()).isJsonObject() || !(jsonObject2 = jsonObject.getAsJsonObject(module.getName())).has("settings") || !jsonObject2.get("settings").isJsonObject()) continue;
            JsonObject jsonObject3 = jsonObject2.getAsJsonObject("settings");
            for (Setting setting : module.getSettings()) {
                try {
                    setting.loadProfile(jsonObject3);
                }
                catch (Exception exception) {}
            }
        }
    }

    public boolean resetModuleSettings(Module module) {
        if (module == null) {
            return false;
        }
        try {
            JsonObject jsonObject = (JsonObject)new Gson().fromJson(this.pristineJson, JsonObject.class);
            if (jsonObject == null || !jsonObject.has("modules") || !jsonObject.get("modules").isJsonObject()) {
                return false;
            }
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("modules");
            if (!jsonObject2.has(module.getName()) || !jsonObject2.get(module.getName()).isJsonObject()) {
                return false;
            }
            JsonObject jsonObject3 = jsonObject2.getAsJsonObject(module.getName());
            if (!jsonObject3.has("settings") || !jsonObject3.get("settings").isJsonObject()) {
                return false;
            }
            JsonObject jsonObject4 = jsonObject3.getAsJsonObject("settings");
            this.resetBefore = this.moduleSettings(module);
            this.resetModule = module;
            this.resetProfile = this.activeName;
            for (Setting setting : module.getSettings()) {
                setting.loadProfile(jsonObject4);
            }
            this.resetAfter = this.moduleSettings(module);
            this.invalidateDirtyCache();
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private List<Module> allModules() {
        ArrayList<Module> arrayList = new ArrayList<Module>();
        if (Valen.moduleManager != null) {
            arrayList.addAll(Valen.moduleManager.getModules());
        }
        return arrayList;
    }

    public static String normalizeName(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim();
        StringBuilder stringBuilder = new StringBuilder(string2.length());
        for (int i = 0; i < string2.length(); ++i) {
            char c = string2.charAt(i);
            stringBuilder.append(c < ' ' || "\\/:*?\"<>|".indexOf(c) >= 0 ? (char)'_' : c);
        }
        String string3 = ConfigManager.trimFileEnding(stringBuilder.toString().trim());
        if (string3.isEmpty()) {
            return null;
        }
        if (ConfigManager.isReservedFileName(string3)) {
            string3 = "_" + string3;
        }
        if (string3.length() > 24) {
            string3 = ConfigManager.trimFileEnding(string3.substring(0, 24).trim());
        }
        return string3.isEmpty() ? null : string3;
    }

    static JsonObject mergeWithDefaults(JsonObject jsonObject, JsonObject jsonObject2) {
        JsonObject jsonObject3;
        JsonObject jsonObject4 = ConfigManager.copy((JsonElement)(jsonObject == null ? new JsonObject() : jsonObject)).getAsJsonObject();
        JsonObject jsonObject5 = jsonObject3 = jsonObject2 == null ? null : ConfigManager.copy((JsonElement)jsonObject2).getAsJsonObject();
        if (jsonObject3 != null && jsonObject3.has("modules") && jsonObject3.get("modules").isJsonObject()) {
            JsonObject jsonObject6;
            JsonObject jsonObject7 = jsonObject3.getAsJsonObject("modules");
            if (!jsonObject7.has("Bridge Egg Clutch") && jsonObject7.has("Bridge Egg")) {
                jsonObject7.add("Bridge Egg Clutch", jsonObject7.get("Bridge Egg"));
            }
            jsonObject7.remove("Bridge Egg");
            if (jsonObject7.has("HUD") && jsonObject7.get("HUD").isJsonObject() && (jsonObject6 = jsonObject7.getAsJsonObject("HUD")).has("settings") && jsonObject6.get("settings").isJsonObject()) {
                JsonObject jsonObject8 = jsonObject6.getAsJsonObject("settings");
                jsonObject8.remove("Style");
                jsonObject8.remove("Watermark");
                jsonObject8.remove("Client info");
            }
        }
        ConfigManager.mergeCompatible(jsonObject4, jsonObject3);
        return jsonObject4;
    }

    private static void mergeCompatible(JsonObject jsonObject, JsonObject jsonObject2) {
        if (jsonObject2 == null) {
            return;
        }
        for (Map.Entry entry : jsonObject2.entrySet()) {
            JsonElement jsonElement = (JsonElement)entry.getValue();
            if (jsonElement == null || jsonElement.isJsonNull()) continue;
            JsonElement jsonElement2 = jsonObject.get((String)entry.getKey());
            if (jsonElement2 == null || jsonElement2.isJsonNull()) {
                jsonObject.add((String)entry.getKey(), ConfigManager.copy(jsonElement));
                continue;
            }
            if (jsonElement2.isJsonObject() && jsonElement.isJsonObject()) {
                ConfigManager.mergeCompatible(jsonElement2.getAsJsonObject(), jsonElement.getAsJsonObject());
                continue;
            }
            if (!ConfigManager.sameValueType(jsonElement2, jsonElement)) continue;
            jsonObject.add((String)entry.getKey(), ConfigManager.copy(jsonElement));
        }
    }

    private static boolean sameValueType(JsonElement jsonElement, JsonElement jsonElement2) {
        return jsonElement.isJsonPrimitive() && jsonElement2.isJsonPrimitive() && ConfigManager.samePrimitiveType(jsonElement, jsonElement2) || jsonElement.isJsonArray() && jsonElement2.isJsonArray();
    }

    private static boolean samePrimitiveType(JsonElement jsonElement, JsonElement jsonElement2) {
        return jsonElement.getAsJsonPrimitive().isBoolean() && jsonElement2.getAsJsonPrimitive().isBoolean() || jsonElement.getAsJsonPrimitive().isNumber() && jsonElement2.getAsJsonPrimitive().isNumber() || jsonElement.getAsJsonPrimitive().isString() && jsonElement2.getAsJsonPrimitive().isString();
    }

    private static JsonElement copy(JsonElement jsonElement) {
        return (JsonElement)new Gson().fromJson(jsonElement.toString(), JsonElement.class);
    }

    private JsonObject readPristine() {
        try {
            JsonObject jsonObject = (JsonObject)new Gson().fromJson(this.pristineJson, JsonObject.class);
            return jsonObject == null ? new JsonObject() : jsonObject;
        }
        catch (RuntimeException runtimeException) {
            return new JsonObject();
        }
    }

    private static String trimFileEnding(String string) {
        char c;
        int n;
        for (n = string.length(); n > 0 && ((c = string.charAt(n - 1)) == ' ' || c == '.'); --n) {
        }
        return string.substring(0, n);
    }

    private static boolean isReservedFileName(String string) {
        String string2;
        String string3 = string;
        int n = string3.indexOf(46);
        if (n >= 0) {
            string3 = string3.substring(0, n);
        }
        if ("CON".equals(string2 = string3.toUpperCase(Locale.ROOT)) || "PRN".equals(string2) || "AUX".equals(string2) || "NUL".equals(string2)) {
            return true;
        }
        if (string2.length() == 4) {
            String string4 = string2.substring(0, 3);
            char c = string2.charAt(3);
            return ("COM".equals(string4) || "LPT".equals(string4)) && c >= '1' && c <= '9';
        }
        return false;
    }

    private static String serialize(JsonObject jsonObject) {
        return new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject);
    }

    private static String readRaw(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }
        catch (IOException iOException) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void writeNewProfile(File file, String string) throws IOException {
        Path path = file.toPath().toAbsolutePath().getParent();
        Files.createDirectories(path, new FileAttribute[0]);
        Path path2 = Files.createTempFile(path, ".profile-", ".tmp", new FileAttribute[0]);
        try {
            Files.write(path2, string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            Files.move(path2, file.toPath(), new CopyOption[0]);
        }
        finally {
            Files.deleteIfExists(path2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void writeAtomically(File file, String string) throws IOException {
        Path path = file.toPath();
        Path path2 = path.toAbsolutePath().getParent();
        Path path3 = Files.createTempFile(path2, ".profile-", ".tmp", new FileAttribute[0]);
        try {
            Files.write(path3, string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            ConfigManager.replace(path3, path);
        }
        finally {
            Files.deleteIfExists(path3);
        }
    }

    private static void replace(Path path, Path path2) throws IOException {
        try {
            Files.move(path, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException atomicMoveNotSupportedException) {
            Files.move(path, path2, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void move(Path path, Path path2) throws IOException {
        try {
            Files.move(path, path2, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException atomicMoveNotSupportedException) {
            Files.move(path, path2, new CopyOption[0]);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static int countEnabledModules(File file) {
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = (JsonObject)new Gson().fromJson((Reader)fileReader, JsonObject.class);
            if (jsonObject == null || !jsonObject.has("modules") || !jsonObject.get("modules").isJsonObject()) {
                int n2 = -1;
                return n2;
            }
            int n3 = 0;
            for (Map.Entry entry : jsonObject.getAsJsonObject("modules").entrySet()) {
                JsonObject jsonObject2;
                if (!((JsonElement)entry.getValue()).isJsonObject() || !(jsonObject2 = ((JsonElement)entry.getValue()).getAsJsonObject()).has("enabled") || !jsonObject2.get("enabled").isJsonPrimitive() || !jsonObject2.get("enabled").getAsBoolean()) continue;
                ++n3;
            }
            int n = n3;
            return n;
        }
        catch (Exception exception) {
            return -1;
        }
    }

    private static String readActiveName() {
        File file = ConfigManager.activeMarker();
        if (!file.exists()) {
            return null;
        }
        try {
            String string = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            return ConfigManager.normalizeName(string);
        }
        catch (IOException iOException) {
            return null;
        }
    }

    private static void writeActiveName(String string) {
        File file = ConfigManager.activeMarker();
        if (string == null) {
            file.delete();
            return;
        }
        try {
            ConfigManager.writeAtomically(file, ConfigManager.normalizeName(string));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

