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
 *  valen.utility.PlayerRelationsManager$PlayerEntry
 */
package valen.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import valen.Valen;
import valen.clickgui.ClickGui;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.CategoryComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.components.impl.PlayerListComponent;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.utility.IMinecraftInstance;
import valen.utility.PlayerRelationsManager;
import valen.utility.Utils;

public class PlayerRelationsManager
implements IMinecraftInstance {
    private final File file;
    private final LinkedHashMap<String, String> friends = new LinkedHashMap();
    private final LinkedHashMap<String, String> enemies = new LinkedHashMap();
    private boolean active = true;
    private boolean middleClickFriends;
    private int friendBind;

    public PlayerRelationsManager() {
        this(new File(new File(PlayerRelationsManager.mc.mcDataDir, "valen"), "players.json"));
    }

    public PlayerRelationsManager(File file) {
        File file2 = file.getAbsoluteFile().getParentFile();
        if (!file2.exists()) {
            file2.mkdirs();
        }
        this.file = file;
    }

    public void load() {
        this.friends.clear();
        this.enemies.clear();
        this.active = true;
        this.middleClickFriends = false;
        this.friendBind = 0;
        if (!this.file.exists()) {
            this.syncUtilsViews();
            return;
        }
        try (FileReader fileReader = new FileReader(this.file);){
            JsonObject jsonObject = new JsonParser().parse((Reader)fileReader).getAsJsonObject();
            if (jsonObject == null) {
                this.syncUtilsViews();
                return;
            }
            if (jsonObject.has("middleClickFriends")) {
                this.middleClickFriends = jsonObject.get("middleClickFriends").getAsBoolean();
            }
            if (jsonObject.has("active")) {
                this.active = jsonObject.get("active").getAsBoolean();
            }
            if (jsonObject.has("friendBind")) {
                this.friendBind = jsonObject.get("friendBind").getAsInt();
                if (!this.isValidBind(this.friendBind)) {
                    this.friendBind = 0;
                }
            }
            this.loadEntries(jsonObject.get("friends"), this.friends);
            this.loadEntries(jsonObject.get("enemies"), this.enemies);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.syncUtilsViews();
    }

    public boolean addFriend(String string) {
        return this.addRelation(RelationType.FRIEND, string);
    }

    public boolean addEnemy(String string) {
        return this.addRelation(RelationType.ENEMY, string);
    }

    public boolean removeFriend(String string) {
        return this.removeRelation(RelationType.FRIEND, string);
    }

    public boolean removeEnemy(String string) {
        return this.removeRelation(RelationType.ENEMY, string);
    }

    public void clearFriends() {
        this.clearRelation(RelationType.FRIEND);
    }

    public void clearEnemies() {
        this.clearRelation(RelationType.ENEMY);
    }

    public boolean addRelation(RelationType relationType, String string) {
        boolean bl;
        String string2 = this.normalize(string);
        if (string2.isEmpty()) {
            return false;
        }
        LinkedHashMap<String, String> linkedHashMap = relationType == RelationType.FRIEND ? this.friends : this.enemies;
        LinkedHashMap<String, String> linkedHashMap2 = relationType == RelationType.FRIEND ? this.enemies : this.friends;
        String string3 = this.normalizeDisplayName(string, string2);
        boolean bl2 = !linkedHashMap.containsKey(string2);
        boolean bl3 = linkedHashMap2.remove(string2) != null;
        boolean bl4 = bl = !string3.equals(linkedHashMap.get(string2));
        if (!(bl2 || bl3 || bl)) {
            return false;
        }
        linkedHashMap.put(string2, string3);
        this.persistAndNotify();
        if (bl2 && relationType == RelationType.FRIEND && mc != null && PlayerRelationsManager.mc.thePlayer != null) {
            PlayerRelationsManager.mc.thePlayer.playSound("note.pling", 0.25f, 1.6f);
        }
        return bl2;
    }

    public boolean removeRelation(RelationType relationType, String string) {
        LinkedHashMap<String, String> linkedHashMap;
        String string2 = this.normalize(string);
        if (string2.isEmpty()) {
            return false;
        }
        LinkedHashMap<String, String> linkedHashMap2 = linkedHashMap = relationType == RelationType.FRIEND ? this.friends : this.enemies;
        if (linkedHashMap.remove(string2) == null) {
            return false;
        }
        this.persistAndNotify();
        return true;
    }

    public void clearRelation(RelationType relationType) {
        LinkedHashMap<String, String> linkedHashMap;
        LinkedHashMap<String, String> linkedHashMap2 = linkedHashMap = relationType == RelationType.FRIEND ? this.friends : this.enemies;
        if (linkedHashMap.isEmpty()) {
            return;
        }
        linkedHashMap.clear();
        this.persistAndNotify();
    }

    public boolean isFriend(String string) {
        return this.contains(this.friends, string);
    }

    public int getFriendBind() {
        return this.friendBind;
    }

    public void setFriendBind(int n) {
        if (!this.isValidBind(n)) {
            n = 0;
        }
        if (this.friendBind == n) {
            return;
        }
        this.friendBind = n;
        this.save();
    }

    public static boolean isValidUsername(String string) {
        return string != null && string.trim().matches("[A-Za-z0-9_]{1,16}");
    }

    private boolean isValidBind(int n) {
        return n >= 0 && n < 256 || n >= 1000 && n < 1032 || n == 1069 || n == 1070;
    }

    public boolean isEnemy(String string) {
        return this.active && this.contains(this.enemies, string);
    }

    public int getCount(RelationType relationType) {
        return this.getMap(relationType).size();
    }

    public List<PlayerEntry> getEntries(RelationType relationType) {
        LinkedHashMap<String, String> linkedHashMap = this.getMap(relationType);
        ArrayList<PlayerEntry> arrayList = new ArrayList<PlayerEntry>(linkedHashMap.size());
        for (Map.Entry<String, String> entry : linkedHashMap.entrySet()) {
            arrayList.add(new PlayerEntry(entry.getKey(), entry.getValue()));
        }
        return arrayList;
    }

    public boolean isMiddleClickFriends() {
        return this.middleClickFriends;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean bl) {
        if (this.active == bl) {
            return;
        }
        this.active = bl;
        this.save();
        this.syncRelationshipsModuleState();
    }

    public void setMiddleClickFriends(boolean bl) {
        if (this.middleClickFriends == bl) {
            return;
        }
        this.middleClickFriends = bl;
        this.save();
        this.syncRelationshipsModuleState();
    }

    public void refreshDisplayName(String string) {
        String string2 = this.normalize(string);
        if (string2.isEmpty()) {
            return;
        }
        boolean bl = this.refreshDisplayName(this.friends, string2, string);
        boolean bl2 = bl = this.refreshDisplayName(this.enemies, string2, string) || bl;
        if (bl) {
            this.persistAndNotify();
        }
    }

    private boolean refreshDisplayName(LinkedHashMap<String, String> linkedHashMap, String string, String string2) {
        if (!linkedHashMap.containsKey(string)) {
            return false;
        }
        String string3 = this.normalizeDisplayName(string2, string);
        if (string3.equals(linkedHashMap.get(string))) {
            return false;
        }
        linkedHashMap.put(string, string3);
        return true;
    }

    private void persistAndNotify() {
        this.save();
        this.syncUtilsViews();
        this.syncRelationshipsModuleState();
        this.refreshRelationshipsModuleUi();
    }

    private void syncUtilsViews() {
        Utils.friends.clear();
        Utils.friends.addAll(this.friends.keySet());
        Utils.enemies.clear();
        Utils.enemies.addAll(this.enemies.keySet());
    }

    private void syncRelationshipsModuleState() {
        if (ModuleManager.relationships != null) {
            ModuleManager.relationships.middleClickFriends.setEnabled(this.middleClickFriends);
            if (this.active && !ModuleManager.relationships.isEnabled()) {
                ModuleManager.relationships.enable();
            } else if (!this.active && ModuleManager.relationships.isEnabled()) {
                ModuleManager.relationships.disable();
            }
        }
    }

    private void refreshRelationshipsModuleUi() {
        if (Valen.clickGui == null || ClickGui.categories == null || ModuleManager.relationships == null) {
            return;
        }
        for (CategoryComponent categoryComponent : ClickGui.categories) {
            if (categoryComponent.category != Module.category.client) continue;
            for (ModuleComponent moduleComponent : categoryComponent.modules) {
                if (moduleComponent.mod != ModuleManager.relationships) continue;
                for (Component component : moduleComponent.settings) {
                    if (!(component instanceof PlayerListComponent)) continue;
                    ((PlayerListComponent)component).onExternalDataChanged();
                }
                moduleComponent.updateSettingPositions();
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void save() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("active", Boolean.valueOf(this.active));
        jsonObject.addProperty("middleClickFriends", Boolean.valueOf(this.middleClickFriends));
        jsonObject.addProperty("friendBind", (Number)this.friendBind);
        jsonObject.add("friends", (JsonElement)this.toJsonArray(this.friends));
        jsonObject.add("enemies", (JsonElement)this.toJsonArray(this.enemies));
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path path = this.file.toPath().toAbsolutePath();
            Path path2 = Files.createTempFile(path.getParent(), "players-", ".tmp", new FileAttribute[0]);
            try {
                Files.write(path2, gson.toJson((JsonElement)jsonObject).getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
                try {
                    Files.move(path2, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (AtomicMoveNotSupportedException atomicMoveNotSupportedException) {
                    Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            finally {
                Files.deleteIfExists(path2);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadEntries(JsonElement jsonElement, LinkedHashMap<String, String> linkedHashMap) {
        if (jsonElement == null || !jsonElement.isJsonArray()) {
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (JsonElement jsonElement2 : jsonArray) {
            String string;
            String string2;
            String string3;
            if (jsonElement2 == null || jsonElement2.isJsonNull()) continue;
            if (jsonElement2.isJsonPrimitive()) {
                string3 = jsonElement2.getAsString();
                string2 = this.normalize(string3);
                if (string2.isEmpty()) continue;
                linkedHashMap.put(string2, this.normalizeDisplayName(string3, string2));
                continue;
            }
            if (!jsonElement2.isJsonObject()) continue;
            string3 = jsonElement2.getAsJsonObject();
            string2 = string3.has("key") ? string3.get("key").getAsString() : "";
            String string4 = string = string3.has("displayName") ? string3.get("displayName").getAsString() : string2;
            String string5 = this.normalize(string2);
            if (string5.isEmpty()) continue;
            linkedHashMap.put(string5, this.normalizeDisplayName(string, string5));
        }
    }

    private JsonArray toJsonArray(LinkedHashMap<String, String> linkedHashMap) {
        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<String, String> entry : linkedHashMap.entrySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key", entry.getKey());
            jsonObject.addProperty("displayName", entry.getValue());
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    private LinkedHashMap<String, String> getMap(RelationType relationType) {
        return relationType == RelationType.FRIEND ? this.friends : this.enemies;
    }

    private boolean contains(LinkedHashMap<String, String> linkedHashMap, String string) {
        String string2 = this.normalize(string);
        return !string2.isEmpty() && linkedHashMap.containsKey(string2);
    }

    private String normalize(String string) {
        return !PlayerRelationsManager.isValidUsername(string) ? "" : string.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String string, String string2) {
        String string3 = string == null ? "" : string.trim();
        return string3.isEmpty() ? string2 : string3;
    }

    public static enum RelationType {
        FRIEND("friends"),
        ENEMY("enemies");

        private final String jsonKey;

        private RelationType(String string2) {
            this.jsonKey = string2;
        }

        public String getJsonKey() {
            return this.jsonKey;
        }
    }
}

