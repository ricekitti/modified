/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package valen.utility.font;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import valen.module.setting.impl.GroupSetting;
import valen.module.setting.impl.SliderSetting;
import valen.utility.ScaledResolutionCache;
import valen.utility.font.GlyphFontRenderer;
import valen.utility.font.ValenFontRenderer;

public final class FontManager {
    private static final String MINECRAFT = "Minecraft";
    private static final String RESOURCE_ROOT = "/assets/valen/fonts/";
    private static final int MAX_CACHED_RENDERERS = 512;
    private static final float DEFAULT_HUD_FONT_SIZE = 9.0f;
    private static final float DEFAULT_CLICK_GUI_HEADER_HEIGHT = 16.0f;
    private static final float DEFAULT_CLICK_GUI_SETTING_HEIGHT = 16.0f;
    private static final float DEFAULT_CLICK_GUI_BRAND_HEIGHT = 24.0f;
    private static final float DEFAULT_NAMETAG_FONT_SIZE = 9.0f;
    private static final BundledFont[] BUNDLED_FONTS = new BundledFont[]{new BundledFont("Sf-Ui", "Sf-Ui.ttf")};
    private static final String[] HUD_FONT_OPTIONS = FontManager.buildHudFontOptions();
    private static final Map<String, BundledFont> BUNDLED_FONT_MAP = FontManager.buildBundledFontMap();
    private static final Map<String, Font> BASE_FONT_CACHE = new ConcurrentHashMap<String, Font>();
    private static final Map<String, ValenFontRenderer> FONT_CACHE = new LinkedHashMap<String, ValenFontRenderer>(16, 0.75f, true);

    private FontManager() {
    }

    public static String[] getHudFontOptions() {
        return (String[])HUD_FONT_OPTIONS.clone();
    }

    public static SliderSetting fixedFontSetting(GroupSetting groupSetting, String string) {
        SliderSetting sliderSetting = new SliderSetting(groupSetting, string, 0, FontManager.getHudFontOptions()){

            @Override
            public void setValueRaw(double d) {
                super.setValueRaw(0.0);
            }
        };
        sliderSetting.visible = false;
        return sliderSetting;
    }

    public static ValenFontRenderer getHudRenderer(String string, float f) {
        float f2 = Math.max(0.5f, Math.min(2.0f, f));
        return FontManager.getRenderer(string, 9.0f * f2);
    }

    public static ValenFontRenderer getClickGuiHeaderRenderer(String string) {
        return FontManager.getRendererForPixelHeight(string, 16.0f);
    }

    public static ValenFontRenderer getClickGuiSettingRenderer(String string) {
        return FontManager.getRendererForPixelHeight(string, 16.0f);
    }

    public static ValenFontRenderer getClickGuiBrandRenderer(String string) {
        return FontManager.getRendererForPixelHeight(string, 24.0f);
    }

    public static ValenFontRenderer getNametagRenderer(String string) {
        return FontManager.getRenderer(string, 9.0f);
    }

    private static ValenFontRenderer getRenderer(String string, float f) {
        final float f2 = Math.max(1.0f, f);
        string = "Sf-Ui";
        final BundledFont bundledFont = BUNDLED_FONT_MAP.get(string);
        String string2 = string + "#" + FontManager.quantizeForCacheKey(f2) + "#" + FontManager.getUiScale();
        return FontManager.getCachedRenderer(string2, new Supplier<ValenFontRenderer>(){

            @Override
            public ValenFontRenderer get() {
                Font font = BASE_FONT_CACHE.computeIfAbsent(bundledFont.fileName, string -> FontManager.loadBaseFont(string));
                if (font == null) {
                    return FontManager.getMinecraftRenderer(f2);
                }
                return new GlyphFontRenderer(font.deriveFont(f2), true);
            }
        });
    }

    private static ValenFontRenderer getRendererForPixelHeight(String string, float f) {
        final float f2 = Math.max(1.0f, f);
        string = "Sf-Ui";
        final BundledFont bundledFont = BUNDLED_FONT_MAP.get(string);
        String string2 = string + "#height#" + FontManager.quantizeForCacheKey(f2) + "#" + FontManager.getUiScale();
        return FontManager.getCachedRenderer(string2, new Supplier<ValenFontRenderer>(){

            @Override
            public ValenFontRenderer get() {
                Font font = BASE_FONT_CACHE.computeIfAbsent(bundledFont.fileName, string -> FontManager.loadBaseFont(string));
                if (font == null) {
                    return FontManager.getMinecraftRenderer(f2);
                }
                return FontManager.createHeightMatchedRenderer(font, f2);
            }
        });
    }

    private static ValenFontRenderer createHeightMatchedRenderer(Font font, float f) {
        float f2;
        float f3;
        float f4 = f;
        GlyphFontRenderer glyphFontRenderer = new GlyphFontRenderer(font.deriveFont(f4), true);
        for (int i = 0; i < 2 && !((f3 = Math.abs((f2 = Math.max(1.0f, (float)glyphFontRenderer.getFontHeight())) - f)) <= 0.5f); ++i) {
            GlyphFontRenderer glyphFontRenderer2 = glyphFontRenderer;
            f4 = Math.max(1.0f, f4 * (f / f2));
            glyphFontRenderer = new GlyphFontRenderer(font.deriveFont(f4), true);
            glyphFontRenderer2.destroy();
        }
        return glyphFontRenderer;
    }

    private static ValenFontRenderer getMinecraftRenderer(float f) {
        float f2 = Math.max(1.0f, (float)Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
        float f3 = Math.max(0.5f, Math.min(2.0f, f / f2));
        String string = "Minecraft#" + FontManager.quantizeForCacheKey(f3);
        return FontManager.getCachedRenderer(string, (Supplier<ValenFontRenderer>)new /* Unavailable Anonymous Inner Class!! */);
    }

    public static boolean isMinecraftFont(String string) {
        return string == null || MINECRAFT.equalsIgnoreCase(string);
    }

    private static String[] buildHudFontOptions() {
        return new String[]{"Sf-Ui"};
    }

    private static Map<String, BundledFont> buildBundledFontMap() {
        LinkedHashMap<String, BundledFont> linkedHashMap = new LinkedHashMap<String, BundledFont>();
        for (BundledFont bundledFont : BUNDLED_FONTS) {
            linkedHashMap.put(bundledFont.displayName, bundledFont);
        }
        return linkedHashMap;
    }

    private static Font loadBaseFont(String string) {
        byte[] byArray = FontManager.readFontData(string);
        if (byArray == null) {
            return null;
        }
        try {
            return Font.createFont(0, new ByteArrayInputStream(byArray));
        }
        catch (Exception exception) {
            try {
                return Font.createFont(1, new ByteArrayInputStream(byArray));
            }
            catch (Exception exception2) {
                return null;
            }
        }
    }

    private static int getUiScale() {
        try {
            return Math.max(1, ScaledResolutionCache.get().getScaleFactor());
        }
        catch (Exception exception) {
            return 1;
        }
    }

    private static float quantizeForCacheKey(float f) {
        return (float)Math.round(f * 100.0f) / 100.0f;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static byte[] readFontData(String string) {
        try (InputStream inputStream = FontManager.class.getResourceAsStream(RESOURCE_ROOT + string);){
            int n;
            if (inputStream == null) {
                byte[] byArray2 = null;
                return byArray2;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byArray3 = new byte[4096];
            while ((n = inputStream.read(byArray3)) != -1) {
                byteArrayOutputStream.write(byArray3, 0, n);
            }
            byte[] byArray = byteArrayOutputStream.toByteArray();
            return byArray;
        }
        catch (IOException iOException) {
            return null;
        }
    }

    private static synchronized ValenFontRenderer getCachedRenderer(String string, Supplier<ValenFontRenderer> supplier) {
        ValenFontRenderer valenFontRenderer = FONT_CACHE.get(string);
        if (valenFontRenderer != null) {
            return valenFontRenderer;
        }
        valenFontRenderer = supplier.get();
        FONT_CACHE.put(string, valenFontRenderer);
        FontManager.trimFontCache();
        return valenFontRenderer;
    }

    private static void trimFontCache() {
        while (FONT_CACHE.size() > 512) {
            Iterator<Map.Entry<String, ValenFontRenderer>> iterator = FONT_CACHE.entrySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            Map.Entry<String, ValenFontRenderer> entry = iterator.next();
            iterator.remove();
            if (entry.getValue() == null) continue;
            entry.getValue().destroy();
        }
    }

    private static final class BundledFont {
        private final String displayName;
        private final String fileName;

        private BundledFont(String string, String string2) {
            this.displayName = string;
            this.fileName = string2;
        }
    }
}

