/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui;

import java.awt.Color;

public final class ClickGuiStyle {
    public static final float RADIUS = 12.0f;
    public static final float WINDOW_INSET = 2.0f;
    public static final float WINDOW_RADIUS = 14.0f;
    public static final float SCROLL_SPEED = 25.0f;
    public static final int DEFAULT_PRIMARY = new Color(22, 22, 25).getRGB();
    public static final int DEFAULT_ACCENT = new Color(255, 255, 255).getRGB();
    public static int PAGE_SCRIM;
    public static int NAV_BACKGROUND;
    public static int NAV_SELECTED;
    public static int NAV_HOVER;
    public static int PANEL_BACKGROUND;
    public static int PANEL_BACKGROUND_ACTIVE;
    public static int WINDOW_GLASS_TOP;
    public static int WINDOW_GLASS_BOTTOM;
    public static int SIDEBAR_BACKGROUND;
    public static int PANEL_OUTLINE;
    public static int PANEL_OUTLINE_ACCENT;
    public static int PANEL_OUTLINE_SOFT;
    public static int MODULE_PANEL_BACKGROUND;
    public static int SETTINGS_PANEL_BACKGROUND;
    public static int PROFILES_PANEL_BACKGROUND;
    public static int GUI_PANEL_BACKGROUND;
    public static int MODULE_PILL;
    public static int ENABLED_MODULE_PILL;
    public static int MODULE_TEXT;
    public static int ENABLED_MODULE_TEXT;
    public static int TOGGLE_ON;
    public static int SLIDER_FILL;
    public static int SURFACE;
    public static int SURFACE_ALT;
    public static int SURFACE_HOVER;
    public static int SURFACE_ACTIVE;
    public static int TRACK;
    public static int TOGGLE_OFF;
    public static int DISABLED_SURFACE;
    public static int CHIP_BACKGROUND;
    public static int TOOLTIP_BACKGROUND;
    public static int INPUT_BACKGROUND;
    public static int INPUT_BACKGROUND_FOCUSED;
    public static int INPUT_OUTLINE;
    public static int TEXT_PRIMARY;
    public static int TEXT_SECONDARY;
    public static int TEXT_MUTED;
    public static int TEXT_DISABLED;
    public static int SWITCH_KNOB;
    public static int DROPDOWN_BACKGROUND;
    public static int DROPDOWN_BORDER;
    public static int ACCENT_BASE;
    public static int ACCENT;
    public static int ACCENT_MID;
    public static int ACCENT_SOFT;
    public static int ACCENT_GHOST;
    public static int INPUT_OUTLINE_FOCUSED;
    public static int INPUT_SELECTION;
    public static final int ERROR;
    public static final int WARNING;
    public static final int POSITIVE;
    public static final int HIDDEN;
    public static final int SHADOW;
    public static boolean DARK_THEME;
    private static final int KNOB_ON_LIGHT;

    private ClickGuiStyle() {
    }

    public static void applyTheme(int n, int n2) {
        boolean bl;
        int n3 = n | 0xFF000000;
        DARK_THEME = bl = ClickGuiStyle.luminance(n3) < 0.5;
        PANEL_BACKGROUND = n3;
        NAV_BACKGROUND = ClickGuiStyle.withAlpha(n3, 245);
        PANEL_OUTLINE = 0;
        PANEL_OUTLINE_ACCENT = 0;
        PANEL_OUTLINE_SOFT = 0;
        SWITCH_KNOB = new Color(255, 255, 255).getRGB();
        if (bl) {
            PAGE_SCRIM = new Color(0, 0, 0, 70).getRGB();
            NAV_SELECTED = ClickGuiStyle.shift(n3, 10);
            NAV_HOVER = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, 12), 235);
            PANEL_BACKGROUND_ACTIVE = ClickGuiStyle.shift(n3, 8);
            WINDOW_GLASS_TOP = ClickGuiStyle.withAlpha(n3, 132);
            WINDOW_GLASS_BOTTOM = ClickGuiStyle.withAlpha(n3, 104);
            SIDEBAR_BACKGROUND = ClickGuiStyle.shift(n3, 6);
            SURFACE = ClickGuiStyle.shift(n3, 8);
            SURFACE_ALT = ClickGuiStyle.shift(n3, 4);
            SURFACE_HOVER = ClickGuiStyle.shift(n3, 20);
            SURFACE_ACTIVE = ClickGuiStyle.shift(n3, 30);
            TRACK = ClickGuiStyle.shift(n3, 38);
            TOGGLE_OFF = ClickGuiStyle.shift(n3, 34);
            DISABLED_SURFACE = ClickGuiStyle.shift(n3, 22);
            CHIP_BACKGROUND = ClickGuiStyle.shift(n3, 20);
            TOOLTIP_BACKGROUND = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, 14), 252);
            INPUT_BACKGROUND = ClickGuiStyle.shift(n3, 14);
            INPUT_BACKGROUND_FOCUSED = ClickGuiStyle.shift(n3, 18);
            INPUT_OUTLINE = ClickGuiStyle.shift(n3, 36);
            DROPDOWN_BACKGROUND = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, 14), 252);
            DROPDOWN_BORDER = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, 44), 225);
            TEXT_PRIMARY = ClickGuiStyle.blend(-1, n3, 0.08f);
            TEXT_SECONDARY = ClickGuiStyle.blend(-1, n3, 0.35f);
            TEXT_MUTED = ClickGuiStyle.blend(-1, n3, 0.52f);
            TEXT_DISABLED = ClickGuiStyle.blend(-1, n3, 0.7f);
            ACCENT_GHOST = ClickGuiStyle.shift(n3, 26);
        } else {
            PAGE_SCRIM = new Color(30, 30, 34, 35).getRGB();
            NAV_SELECTED = ClickGuiStyle.shift(n3, 12);
            NAV_HOVER = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, -9), 235);
            PANEL_BACKGROUND_ACTIVE = ClickGuiStyle.shift(n3, 12);
            WINDOW_GLASS_TOP = ClickGuiStyle.withAlpha(n3, 124);
            WINDOW_GLASS_BOTTOM = ClickGuiStyle.withAlpha(n3, 96);
            SIDEBAR_BACKGROUND = ClickGuiStyle.shift(n3, 4);
            SURFACE = ClickGuiStyle.shift(n3, 12);
            SURFACE_ALT = ClickGuiStyle.shift(n3, 3);
            SURFACE_HOVER = ClickGuiStyle.shift(n3, -9);
            SURFACE_ACTIVE = ClickGuiStyle.shift(n3, -19);
            TRACK = ClickGuiStyle.shift(n3, -27);
            TOGGLE_OFF = ClickGuiStyle.shift(n3, -32);
            DISABLED_SURFACE = ClickGuiStyle.shift(n3, -14);
            CHIP_BACKGROUND = ClickGuiStyle.shift(n3, -9);
            TOOLTIP_BACKGROUND = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, 12), 252);
            INPUT_BACKGROUND = ClickGuiStyle.shift(n3, -3);
            INPUT_BACKGROUND_FOCUSED = ClickGuiStyle.shift(n3, -8);
            INPUT_OUTLINE = ClickGuiStyle.shift(n3, -23);
            DROPDOWN_BACKGROUND = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, 12), 252);
            DROPDOWN_BORDER = ClickGuiStyle.withAlpha(ClickGuiStyle.shift(n3, -32), 225);
            TEXT_PRIMARY = ClickGuiStyle.blend(-16777216, n3, 0.1f);
            TEXT_SECONDARY = ClickGuiStyle.blend(-16777216, n3, 0.37f);
            TEXT_MUTED = ClickGuiStyle.blend(-16777216, n3, 0.57f);
            TEXT_DISABLED = ClickGuiStyle.blend(-16777216, n3, 0.77f);
            ACCENT_GHOST = ClickGuiStyle.shift(n3, -22);
        }
        ClickGuiStyle.applyAccent(n2);
        MODULE_PANEL_BACKGROUND = 0;
        SETTINGS_PANEL_BACKGROUND = SURFACE_ALT;
        PROFILES_PANEL_BACKGROUND = 0;
        GUI_PANEL_BACKGROUND = 0;
        MODULE_PILL = SURFACE;
        ENABLED_MODULE_PILL = SURFACE;
        MODULE_TEXT = TEXT_PRIMARY;
        ENABLED_MODULE_TEXT = ACCENT;
        TOGGLE_ON = ACCENT;
        SLIDER_FILL = ACCENT;
    }

    private static void applyAccent(int n) {
        int n2;
        ACCENT_BASE = n2 = n | 0xFF000000;
        ACCENT = n2;
        ACCENT_MID = ClickGuiStyle.blend(n2, -16777216, 0.15f) | 0xFF000000;
        ACCENT_SOFT = ClickGuiStyle.withAlpha(ClickGuiStyle.blend(n2, -16119286, 0.6f), 210);
        INPUT_OUTLINE_FOCUSED = ClickGuiStyle.withAlpha(n2, 220);
        INPUT_SELECTION = ClickGuiStyle.withAlpha(n2, 80);
    }

    public static int panelFill(boolean bl) {
        return bl ? PANEL_BACKGROUND_ACTIVE : PANEL_BACKGROUND;
    }

    public static int panelOutline(boolean bl) {
        return bl ? PANEL_OUTLINE_ACCENT : PANEL_OUTLINE;
    }

    public static int panelOutlineSecondary(boolean bl) {
        return bl ? PANEL_OUTLINE_SOFT : PANEL_OUTLINE;
    }

    public static int surface(boolean bl) {
        return bl ? SURFACE_ACTIVE : SURFACE;
    }

    public static int hovered(int n, float f) {
        float f2 = Math.max(0.0f, Math.min(1.0f, f));
        int n2 = ClickGuiStyle.shift(n, DARK_THEME ? 12 : -12);
        return ClickGuiStyle.blend(n, n2, f2);
    }

    public static int listRow(int n, boolean bl) {
        return bl ? SURFACE_HOVER : 0;
    }

    public static int moduleText(float f, boolean bl, boolean bl2) {
        if (bl) {
            return ERROR;
        }
        if (bl2) {
            return WARNING;
        }
        return ClickGuiStyle.blend(MODULE_TEXT, ENABLED_MODULE_TEXT, f);
    }

    public static int withAlpha(int n, int n2) {
        int n3 = Math.max(0, Math.min(255, n2));
        return n & 0xFFFFFF | n3 << 24;
    }

    public static int blend(int n, int n2, float f) {
        float f2 = Math.max(0.0f, Math.min(1.0f, f));
        float f3 = 1.0f - f2;
        int n3 = Math.round((float)(n >>> 24 & 0xFF) * f3 + (float)(n2 >>> 24 & 0xFF) * f2);
        int n4 = Math.round((float)(n >>> 16 & 0xFF) * f3 + (float)(n2 >>> 16 & 0xFF) * f2);
        int n5 = Math.round((float)(n >>> 8 & 0xFF) * f3 + (float)(n2 >>> 8 & 0xFF) * f2);
        int n6 = Math.round((float)(n & 0xFF) * f3 + (float)(n2 & 0xFF) * f2);
        return n3 << 24 | n4 << 16 | n5 << 8 | n6;
    }

    private static int shift(int n, int n2) {
        int n3 = ClickGuiStyle.clampChannel((n >> 16 & 0xFF) + n2);
        int n4 = ClickGuiStyle.clampChannel((n >> 8 & 0xFF) + n2);
        int n5 = ClickGuiStyle.clampChannel((n & 0xFF) + n2);
        return 0xFF000000 | n3 << 16 | n4 << 8 | n5;
    }

    private static int clampChannel(int n) {
        return Math.max(0, Math.min(255, n));
    }

    private static double luminance(int n) {
        int n2 = n >> 16 & 0xFF;
        int n3 = n >> 8 & 0xFF;
        int n4 = n & 0xFF;
        return (0.299 * (double)n2 + 0.587 * (double)n3 + 0.114 * (double)n4) / 255.0;
    }

    public static int getKnobColor(int n) {
        return ClickGuiStyle.luminance(n) > 0.65 ? KNOB_ON_LIGHT : SWITCH_KNOB;
    }

    public static int knobColorForToggle(float f) {
        float f2 = (float)ClickGuiStyle.luminance(ClickGuiStyle.blend(TOGGLE_OFF, TOGGLE_ON, f));
        float f3 = Math.max(0.0f, Math.min(1.0f, (f2 - 0.84f) / 0.1f));
        f3 = f3 * f3 * (3.0f - 2.0f * f3);
        return ClickGuiStyle.blend(SWITCH_KNOB, KNOB_ON_LIGHT, f3);
    }

    public static String label(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        String string2 = string.replace('_', ' ');
        StringBuilder stringBuilder = new StringBuilder(string2.length());
        boolean bl = true;
        for (int i = 0; i < string2.length(); ++i) {
            char c = string2.charAt(i);
            if (Character.isWhitespace(c)) {
                bl = true;
                stringBuilder.append(c);
                continue;
            }
            stringBuilder.append(bl ? Character.toUpperCase(c) : c);
            bl = false;
        }
        return stringBuilder.toString();
    }

    static {
        ERROR = new Color(200, 60, 70).getRGB();
        WARNING = new Color(190, 130, 40).getRGB();
        POSITIVE = new Color(60, 150, 100).getRGB();
        HIDDEN = new Color(200, 60, 70).getRGB();
        SHADOW = new Color(0, 0, 0, 40).getRGB();
        KNOB_ON_LIGHT = new Color(20, 20, 22).getRGB();
        ClickGuiStyle.applyTheme(DEFAULT_PRIMARY, DEFAULT_ACCENT);
    }
}

