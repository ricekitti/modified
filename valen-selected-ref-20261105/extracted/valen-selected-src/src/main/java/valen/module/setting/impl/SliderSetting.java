/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package valen.module.setting.impl;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import valen.event.PostSetSliderEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.module.setting.Setting;
import valen.module.setting.impl.GroupSetting;

public class SliderSetting
extends Setting {
    private String settingName;
    private String[] options = null;
    private double defaultValue;
    private double max;
    private double min;
    private double intervals;
    public boolean isString;
    private String suffix = "";
    public boolean canBeDisabled;
    public GroupSetting groupSetting;
    private String[] legacyProfileKeys;

    public SliderSetting(GroupSetting groupSetting, String string, double d, double d2, double d3, double d4) {
        super(string);
        this.groupSetting = groupSetting;
        this.settingName = string;
        this.defaultValue = d;
        this.min = d2;
        this.max = d3;
        this.intervals = d4;
        this.isString = false;
        this.legacyProfileKeys = new String[0];
    }

    public SliderSetting(String string, double d, double d2, double d3, double d4) {
        this((GroupSetting)null, string, d, d2, d3, d4);
    }

    public SliderSetting(GroupSetting groupSetting, String string, String string2, double d, double d2, double d3, double d4) {
        this(groupSetting, string, d, d2, d3, d4);
        this.suffix = string2;
    }

    public SliderSetting(GroupSetting groupSetting, String string, String string2, double d, double d2, double d3, double d4, String ... stringArray) {
        this(groupSetting, string, string2, d, d2, d3, d4);
        this.legacyProfileKeys = stringArray != null ? stringArray : new String[]{};
    }

    public SliderSetting(String string, String string2, double d, double d2, double d3, double d4) {
        this((GroupSetting)null, string, d, d2, d3, d4);
        this.suffix = string2;
    }

    public SliderSetting(String string, boolean bl, double d, double d2, double d3, double d4) {
        this(string, d, d2, d3, d4);
        this.canBeDisabled = bl;
    }

    public SliderSetting(GroupSetting groupSetting, String string, boolean bl, double d, double d2, double d3, double d4) {
        this(groupSetting, string, d, d2, d3, d4);
        this.canBeDisabled = bl;
    }

    public SliderSetting(String string, String string2, boolean bl, double d, double d2, double d3, double d4) {
        this(string, d, d2, d3, d4);
        this.suffix = string2;
        this.canBeDisabled = bl;
    }

    public SliderSetting(GroupSetting groupSetting, String string, int n, String[] stringArray) {
        super(string);
        this.groupSetting = groupSetting;
        this.settingName = string;
        this.options = stringArray;
        this.defaultValue = n;
        this.min = 0.0;
        this.max = stringArray.length - 1;
        this.intervals = 1.0;
        this.isString = true;
        this.legacyProfileKeys = new String[0];
    }

    public SliderSetting(String string, int n, String[] stringArray) {
        this((GroupSetting)null, string, n, stringArray);
    }

    public SliderSetting(String string, int n, String[] stringArray, String ... stringArray2) {
        this((GroupSetting)null, string, n, stringArray);
        this.legacyProfileKeys = stringArray2 != null ? stringArray2 : new String[]{};
    }

    public SliderSetting(String string, String string2, int n, String[] stringArray) {
        this((GroupSetting)null, string, n, stringArray);
        this.suffix = string2;
    }

    public SliderSetting(GroupSetting groupSetting, String string, String string2, int n, String[] stringArray) {
        this(groupSetting, string, n, stringArray);
        this.suffix = string2;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public String[] getOptions() {
        return this.options;
    }

    @Override
    public String getName() {
        return this.settingName;
    }

    @Override
    public String getProfileKey() {
        return this.groupSetting == null ? this.getName() : this.groupSetting.getName() + "." + this.getName();
    }

    public double getInput() {
        return SliderSetting.roundToInterval(this.defaultValue, 4);
    }

    public double getMin() {
        return this.min;
    }

    public double getInterval() {
        return this.intervals;
    }

    public double getMax() {
        return this.max;
    }

    public double setValue(double d) {
        d = SliderSetting.correctValue(d, this.min, this.max);
        this.defaultValue = d = (double)Math.round(d * (1.0 / this.intervals)) / (1.0 / this.intervals);
        return this.defaultValue;
    }

    public void setValueWithEvent(double d) {
        double d2 = this.defaultValue;
        MinecraftForge.EVENT_BUS.post(new PostSetSliderEvent(d2, this.setValue(d)));
    }

    public void setValueRaw(double d) {
        this.defaultValue = d;
    }

    public void setValueRawWithEvent(double d) {
        double d2 = this.defaultValue;
        this.defaultValue = d;
        MinecraftForge.EVENT_BUS.post(new PostSetSliderEvent(d2, d));
    }

    public static double correctValue(double d, double d2, double d3) {
        d = Math.max(d2, d);
        d = Math.min(d3, d);
        return d;
    }

    public void setSuffix(String string) {
        this.suffix = string;
    }

    public static double roundToInterval(double d, int n) {
        if (n < 0) {
            return 0.0;
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(n, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    @Override
    public void loadProfile(JsonObject jsonObject) {
        String string = this.getProfileKey();
        String string2 = this.getName();
        String string3 = null;
        if (jsonObject.has(string)) {
            string3 = string;
        } else if (jsonObject.has(string2)) {
            string3 = string2;
        } else {
            for (String string4 : this.legacyProfileKeys) {
                if (!jsonObject.has(string4)) continue;
                string3 = string4;
                break;
            }
        }
        if (string3 != null && jsonObject.has(string3) && jsonObject.get(string3).isJsonPrimitive()) {
            double d = this.defaultValue;
            try {
                d = jsonObject.getAsJsonPrimitive(string3).getAsDouble();
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (d == -1.0) {
                this.setValueRaw(d);
                return;
            }
            this.setValue(d);
        }
    }

    @Override
    public void writeProfile(JsonObject jsonObject) {
        jsonObject.addProperty(this.getProfileKey(), (Number)this.getInput());
    }
}

