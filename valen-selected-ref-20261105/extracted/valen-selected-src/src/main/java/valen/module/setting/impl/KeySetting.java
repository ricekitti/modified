/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 */
package valen.module.setting.impl;

import com.google.gson.JsonObject;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import valen.helper.MouseHelper;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;

public class KeySetting
extends Setting {
    private int key;
    public GroupSetting group;

    public KeySetting(String string, int n) {
        super(string);
        this.key = n;
    }

    public KeySetting(GroupSetting groupSetting, String string, int n) {
        super(string);
        this.group = groupSetting;
        this.key = n;
    }

    public int getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getProfileKey() {
        return this.group == null ? this.getName() : this.group.getName() + "." + this.getName();
    }

    public void setKey(int n) {
        this.key = n;
    }

    public boolean isPressed() {
        if (this.getKey() == 0) {
            return false;
        }
        if (this.getKey() >= 1000) {
            return this.getKey() == 1069 || this.getKey() == 1070 ? MouseHelper.isScrollDown(this.getKey()) : Mouse.isButtonDown((int)(this.getKey() - 1000));
        }
        return Keyboard.isKeyDown((int)this.getKey());
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        String string;
        String string2 = this.getProfileKey();
        String string3 = this.getName();
        String string4 = string = jsonObject.has(string2) ? string2 : string3;
        if (jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive()) {
            int n = this.key;
            try {
                n = jsonObject.getAsJsonPrimitive(string).getAsInt();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.key = n;
        }
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        jsonObject.addProperty(this.getProfileKey(), (Number)this.key);
    }
}

