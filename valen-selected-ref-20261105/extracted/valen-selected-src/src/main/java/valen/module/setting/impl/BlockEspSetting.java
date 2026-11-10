/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  valen.module.setting.impl.BlockEspSetting$Entry
 */
package valen.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import valen.module.setting.impl.BlockEspSetting;
import valen.module.setting.impl.BlockListSetting;

public final class BlockEspSetting
extends BlockListSetting {
    private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();

    public BlockEspSetting() {
        super("Add block", "Blocks");
    }

    @Override
    public void addBlock(String string) {
        super.addBlock(string);
        this.entry(string);
    }

    @Override
    public void removeBlock(String string) {
        super.removeBlock(string);
        this.entries.remove(string);
    }

    public Entry entry(String string2) {
        return this.entries.computeIfAbsent(string2, string -> new Entry(string, null));
    }

    public Entry match(IBlockState iBlockState) {
        String string;
        String string2 = String.valueOf(Block.blockRegistry.getNameForObject((Object)iBlockState.getBlock()));
        int n = iBlockState.getBlock().getMetaFromState(iBlockState);
        String string3 = string = n == 0 ? string2 : string2 + ":" + n;
        if (this.getBlocks().contains(string)) {
            return this.entry(string);
        }
        if (this.getBlocks().contains(string2 + ":*")) {
            return this.entry(string2 + ":*");
        }
        return null;
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        super.writeProfile(jsonObject);
        JsonObject jsonObject2 = new JsonObject();
        for (String string : this.getBlocks()) {
            Entry entry = this.entry(string);
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("Color", entry.color.getRed() + "," + entry.color.getGreen() + "," + entry.color.getBlue() + "," + entry.color.getAlpha());
            jsonObject3.addProperty("Outline", Boolean.valueOf(entry.outline.isToggled()));
            jsonObject3.addProperty("Range", (Number)entry.range.getInput());
            jsonObject2.add(string, (JsonElement)jsonObject3);
        }
        jsonObject.add("Block styles", (JsonElement)jsonObject2);
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        super.loadProfile(jsonObject);
        this.entries.clear();
        JsonElement jsonElement = jsonObject.get("Block styles");
        JsonObject jsonObject2 = jsonElement != null && jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
        for (String string : this.getBlocks()) {
            Entry entry = this.entry(string);
            JsonElement jsonElement2 = jsonObject2 == null ? null : jsonObject2.get(string);
            JsonObject jsonObject3 = jsonElement2 != null && jsonElement2.isJsonObject() ? jsonElement2.getAsJsonObject() : jsonObject;
            entry.color.loadProfile(jsonObject3);
            entry.outline.loadProfile(jsonObject3);
            entry.range.loadProfile(jsonObject3);
        }
    }
}

