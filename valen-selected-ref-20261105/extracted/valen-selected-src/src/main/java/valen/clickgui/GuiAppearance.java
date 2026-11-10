/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package valen.clickgui;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import valen.clickgui.ClickGuiStyle;
import valen.module.setting.Setting;
import valen.module.setting.impl.ColorSetting;
import valen.module.setting.impl.SliderSetting;
import valen.utility.font.FontManager;

public final class GuiAppearance {
    public static final double MIN_SCALE = 50.0;
    public static final double MAX_SCALE = 100.0;
    public final SliderSetting font = FontManager.fixedFontSetting(null, "GUI font");
    public final SliderSetting scale = new SliderSetting("GUI scale", "%", 100.0, 50.0, 100.0, 1.0){

        @Override
        public double setValue(double d) {
            return super.setValue(GuiAppearance.clampScale(d));
        }

        @Override
        public void setValueRaw(double d) {
            super.setValueRaw(GuiAppearance.clampScale(d));
        }
    };
    public final ColorSetting accent = new ColorSetting("Accent", 255, 255, 255){

        @Override
        public String getProfileKey() {
            return "Palette.Accent";
        }
    };
    public final ColorSetting background = new ColorSetting("Background color", 22, 22, 25){

        @Override
        public String getProfileKey() {
            return "Palette.Primary panel";
        }
    };
    public final SliderSetting glassOpacity = GuiAppearance.percentage("Glass opacity", 50.0);
    public final SliderSetting backgroundDimming = GuiAppearance.percentage("Background dimming", 28.0);
    private final List<Setting> settings = new ArrayList<Setting>();
    private final JsonObject defaults;
    private long appliedFingerprint;

    public GuiAppearance() {
        this.settings.add(this.scale);
        this.settings.add(this.font);
        this.settings.add(this.glassOpacity);
        this.settings.add(this.backgroundDimming);
        this.settings.add(this.accent);
        this.settings.add(this.background);
        this.defaults = this.snapshot();
        this.appliedFingerprint = this.fingerprint();
    }

    public List<Setting> settings() {
        return Collections.unmodifiableList(this.settings);
    }

    public double scalePercent() {
        return GuiAppearance.clampScale(this.scale.getInput());
    }

    public static double clampScale(double d) {
        return Double.isNaN(d) || Double.isInfinite(d) ? 100.0 : Math.max(50.0, Math.min(100.0, d));
    }

    private static SliderSetting percentage(String string, final double d) {
        return new SliderSetting(string, "%", d, 0.0, 100.0, 1.0){

            @Override
            public double setValue(double d2) {
                return super.setValue(GuiAppearance.clampPercent(d2, d));
            }

            @Override
            public void setValueRaw(double d2) {
                super.setValueRaw(GuiAppearance.clampPercent(d2, d));
            }
        };
    }

    private static double clampPercent(double d, double d2) {
        return Double.isNaN(d) || Double.isInfinite(d) ? d2 : Math.max(0.0, Math.min(100.0, d));
    }

    public JsonObject snapshot() {
        JsonObject jsonObject = new JsonObject();
        for (Setting setting : this.settings) {
            setting.writeProfile(jsonObject);
        }
        return jsonObject;
    }

    public void reset() {
        for (Setting setting : this.settings) {
            setting.loadProfile(this.defaults);
        }
    }

    public void load(JsonObject jsonObject) {
        this.reset();
        if (jsonObject != null) {
            for (Setting setting : this.settings) {
                try {
                    setting.loadProfile(jsonObject);
                }
                catch (RuntimeException runtimeException) {}
            }
        }
    }

    private long fingerprint() {
        long l = Double.doubleToLongBits(this.scalePercent());
        l = l * 31L + Double.doubleToLongBits(this.font.getInput());
        l = l * 31L + (long)this.accent.getColor();
        l = l * 31L + (long)this.background.getColor();
        l = l * 31L + Double.doubleToLongBits(this.glassOpacity.getInput());
        return l * 31L + Double.doubleToLongBits(this.backgroundDimming.getInput());
    }

    public boolean applyIfChanged() {
        if (this.appliedFingerprint == this.fingerprint()) {
            return false;
        }
        this.apply();
        return true;
    }

    public void apply() {
        ClickGuiStyle.applyTheme(this.background.getColor(), this.accent.getColor());
        ClickGuiStyle.ENABLED_MODULE_TEXT = ClickGuiStyle.DARK_THEME ? -1 : -15461354;
        ClickGuiStyle.NAV_SELECTED = ClickGuiStyle.SURFACE_ACTIVE;
        ClickGuiStyle.WINDOW_GLASS_TOP = this.glassColor(ClickGuiStyle.WINDOW_GLASS_TOP);
        ClickGuiStyle.WINDOW_GLASS_BOTTOM = this.glassColor(ClickGuiStyle.WINDOW_GLASS_BOTTOM);
        ClickGuiStyle.PAGE_SCRIM = ClickGuiStyle.withAlpha(-16777216, (int)Math.round(this.backgroundDimming.getInput() * 255.0 / 100.0));
        this.appliedFingerprint = this.fingerprint();
    }

    private int glassColor(int n) {
        int n2 = n >>> 24;
        double d = this.glassOpacity.getInput();
        double d2 = d <= 50.0 ? (double)n2 * d / 50.0 : (double)n2 + (double)(255 - n2) * (d - 50.0) / 50.0;
        return ClickGuiStyle.withAlpha(n, (int)Math.round(d2));
    }
}

