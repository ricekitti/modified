package dev.rice.jade.client.module.client;

import dev.rice.jade.client.common.FontManager;
import dev.rice.jade.client.common.IFont;
import dev.rice.jade.client.module.Category;
import dev.rice.jade.client.module.Module;
import dev.rice.jade.client.setting.ColorSetting;
import dev.rice.jade.client.setting.FontSetting;
import dev.rice.jade.client.setting.SliderSetting;

/**
 * Rice host shim for OpenJade's Gui module statics used by the Opsec screens and TextField. The static
 * settings are created with the exact defaults of upstream's Gui constructor (GUI style "Box", ClickGUI
 * font "Modern", accent 26/168/121); {@code JadeRuntime} then applies the default "jade" theme to the
 * accent exactly like upstream ThemeManager does at start-up. The static methods are verbatim upstream.
 */
public final class Gui extends Module {
   public static SliderSetting guiStyle = new SliderSetting("GUI style", 0, new String[]{"Box", "Dropdown"});
   public static ColorSetting accent = new ColorSetting("Accent", 26, 168, 121);
   public static FontSetting clickGUIFont = new FontSetting("ClickGUI font", "Modern");

   private Gui() {
      super("Gui", Category.client, 25);
   }

   public static String ISjhxoi() {
      return clickGUIFont == null ? FontManager.getDefaultHudFontName() : clickGUIFont.getResolvedFontName();
   }

   public static IFont getHeaderFont() {
      return JzraV3() ? FontManager.getPixelHeightRenderer(ISjhxoi(), 10.0F) : FontManager.getClickGuiHeaderRenderer(ISjhxoi());
   }

   public static IFont getSettingFont() {
      return JzraV3() ? FontManager.getPixelHeightRenderer(ISjhxoi(), 10.0F) : FontManager.getClickGuiSettingRenderer(ISjhxoi());
   }

   public static boolean JzraV3() {
      return guiStyle != null && guiStyle.getInput() >= 1.0;
   }
}
