/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package valen.module.setting.impl;

import com.google.gson.JsonObject;
import java.awt.Color;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;

public class ColorSetting
extends Setting {
    private int red;
    private int green;
    private int blue;
    private int alpha;
    private final boolean hasAlpha;
    public GroupSetting groupSetting;

    public ColorSetting(String string, int n, int n2, int n3) {
        this(null, string, n, n2, n3, 255, false);
    }

    public ColorSetting(String string, int n, int n2, int n3, int n4) {
        this(null, string, n, n2, n3, n4, true);
    }

    public ColorSetting(GroupSetting groupSetting, String string, int n, int n2, int n3) {
        this(groupSetting, string, n, n2, n3, 255, false);
    }

    public ColorSetting(GroupSetting groupSetting, String string, int n, int n2, int n3, int n4) {
        this(groupSetting, string, n, n2, n3, n4, true);
    }

    public ColorSetting(GroupSetting groupSetting, String string, int n, int n2, int n3, int n4, boolean bl) {
        super(string);
        this.groupSetting = groupSetting;
        this.red = ColorSetting.clamp(n);
        this.green = ColorSetting.clamp(n2);
        this.blue = ColorSetting.clamp(n3);
        this.alpha = ColorSetting.clamp(n4);
        this.hasAlpha = bl;
    }

    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int n) {
        this.alpha = ColorSetting.clamp(n);
    }

    public void setColor(int n, int n2, int n3) {
        this.red = ColorSetting.clamp(n);
        this.green = ColorSetting.clamp(n2);
        this.blue = ColorSetting.clamp(n3);
    }

    public void setColor(int n, int n2, int n3, int n4) {
        this.setColor(n, n2, n3);
        this.alpha = ColorSetting.clamp(n4);
    }

    public int getColor() {
        return this.alpha << 24 | this.red << 16 | this.green << 8 | this.blue;
    }

    public int getRGB() {
        return this.red << 16 | this.green << 8 | this.blue;
    }

    public float getHue() {
        float[] fArray = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        return fArray[0] * 360.0f;
    }

    public float getSaturation() {
        float[] fArray = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        return fArray[1];
    }

    public float getBrightness() {
        float[] fArray = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        return fArray[2];
    }

    public void setFromHSB(float f, float f2, float f3) {
        int n = Color.HSBtoRGB(f / 360.0f, Math.max(0.0f, Math.min(1.0f, f2)), Math.max(0.0f, Math.min(1.0f, f3)));
        this.red = n >> 16 & 0xFF;
        this.green = n >> 8 & 0xFF;
        this.blue = n & 0xFF;
    }

    public void setHue(float f) {
        float[] fArray = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        this.setFromHSB(f, fArray[1], fArray[2]);
    }

    public void setSaturation(float f) {
        float[] fArray = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        this.setFromHSB(fArray[0] * 360.0f, f, fArray[2]);
    }

    public void setBrightness(float f) {
        float[] fArray = Color.RGBtoHSB(this.red, this.green, this.blue, null);
        this.setFromHSB(fArray[0] * 360.0f, fArray[1], f);
    }

    public boolean hasAlpha() {
        return this.hasAlpha;
    }

    @Override
    public String getProfileKey() {
        return this.groupSetting == null ? this.getName() : this.groupSetting.getName() + "." + this.getName();
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        String string;
        String string2 = this.getProfileKey();
        String string3 = this.getName();
        String string4 = string = jsonObject.has(string2) ? string2 : string3;
        if (jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive()) {
            try {
                String string5 = jsonObject.getAsJsonPrimitive(string).getAsString();
                String[] stringArray = string5.split(",");
                if (stringArray.length >= 3) {
                    this.red = ColorSetting.clamp(Integer.parseInt(stringArray[0].trim()));
                    this.green = ColorSetting.clamp(Integer.parseInt(stringArray[1].trim()));
                    this.blue = ColorSetting.clamp(Integer.parseInt(stringArray[2].trim()));
                    if (stringArray.length >= 4) {
                        this.alpha = ColorSetting.clamp(Integer.parseInt(stringArray[3].trim()));
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private static int clamp(int n) {
        return Math.max(0, Math.min(255, n));
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        String string = this.red + "," + this.green + "," + this.blue;
        if (this.hasAlpha) {
            string = string + "," + this.alpha;
        }
        jsonObject.addProperty(this.getProfileKey(), string);
    }
}

