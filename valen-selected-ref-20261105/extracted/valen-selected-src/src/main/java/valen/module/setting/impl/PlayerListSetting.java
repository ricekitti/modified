/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  valen.utility.PlayerRelationsManager$PlayerEntry
 */
package valen.module.setting.impl;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import valen.Valen;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;
import valen.utility.PlayerRelationsManager;

public class PlayerListSetting
extends Setting {
    private final PlayerRelationsManager.RelationType relationType;
    private final String placeholder;
    private final int maxLength;
    public GroupSetting group;
    public boolean dropdown;

    public PlayerListSetting(GroupSetting groupSetting, String string, PlayerRelationsManager.RelationType relationType, String string2, int n) {
        super(string);
        this.group = groupSetting;
        this.relationType = relationType;
        this.placeholder = string2 == null ? "" : string2;
        this.maxLength = Math.max(1, n);
    }

    public PlayerRelationsManager.RelationType getRelationType() {
        return this.relationType;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public boolean addPlayer(String string) {
        return Valen.playerRelationsManager != null && Valen.playerRelationsManager.addRelation(this.relationType, string);
    }

    public boolean removePlayer(String string) {
        return Valen.playerRelationsManager != null && Valen.playerRelationsManager.removeRelation(this.relationType, string);
    }

    public void clearPlayers() {
        if (Valen.playerRelationsManager != null) {
            Valen.playerRelationsManager.clearRelation(this.relationType);
        }
    }

    public List<PlayerRelationsManager.PlayerEntry> getEntries() {
        if (Valen.playerRelationsManager == null) {
            return Collections.emptyList();
        }
        return Valen.playerRelationsManager.getEntries(this.relationType);
    }

    @Override
    public String getProfileKey() {
        return this.group == null ? this.getName() : this.group.getName() + "." + this.getName();
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
    }
}

