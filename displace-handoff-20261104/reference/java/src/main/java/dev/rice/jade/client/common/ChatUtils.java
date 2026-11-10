// Jade recovery: original class: jade.deps.eLz.XzkQGEq
package dev.rice.jade.client.common;

import dev.rice.jade.inject.InjectionAgent;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;

public final class ChatUtils {
   private static final Pattern FLOW_TAG_PATTERN = Pattern.compile("\\{flow:([^}]+)\\}(.*?)\\{/flow\\}");
   private static final Pattern FLOW_OPEN_PATTERN = Pattern.compile("\\{flow:([^}]+)\\}");
   private static final Map<String, String> flowTextCache = new LinkedHashMap<String, String>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> var1) {
         return this.size() > 100;
      }
   };

   public static String applyFlowGradient(String var0, String... var1) {
      if (var0 != null && !var0.isEmpty() && var1 != null && var1.length >= 2) {
         StringBuilder var2 = new StringBuilder();
         var2.append("{flow:");

         for (int var3 = 0; var3 < var1.length; var3++) {
            if (var3 > 0) {
               var2.append(',');
            }

            var2.append(normalizeHexColor(var1[var3]));
         }

         var2.append('}').append(var0).append("{/flow}");
         return var2.toString();
      } else {
         return var0;
      }
   }

   public static boolean hasFlowTag(String var0) {
      return var0 != null && var0.contains("{flow:") && var0.contains("{/flow}");
   }

   public static String resolveFlowText(String var0) {
      if (!hasFlowTag(var0)) {
         return var0;
      } else {
         String var1 = kWr1(var0);
         String var2 = mfX4(var1);
         synchronized (flowTextCache) {
            flowTextCache.put(var2, var0);
         }

         if (var2.startsWith("Jade")) {
            InjectionAgent.recordBadlionGradient("register", "keyLength=" + var2.length() + " rawLength=" + var0.length());

            try {
               Minecraft var7 = Minecraft.getMinecraft();
               GuiNewChat var4 = var7.ingameGUI == null ? null : var7.ingameGUI.getChatGUI();
               InjectionAgent.recordBadlionGradient(
                  "live.classes",
                  "chat="
                     + (var4 == null ? "null" : var4.getClass().getName())
                     + " font="
                     + (var7.fontRendererObj == null ? "null" : var7.fontRendererObj.getClass().getName())
               );
            } catch (Throwable var5) {
               InjectionAgent.recordBadlionGradient("live.classes", "failure=" + var5.getClass().getName());
            }
         }

         InjectionAgent.setHookEnabled("onFontDraw", "(Ljava/lang/Object;Ljava/lang/Object;FFIZ)Ljava/lang/Object;", true);
         InjectionAgent.setHookEnabled("onChatDrawString", "(Ljava/lang/Object;Ljava/lang/Object;FFI)Ljava/lang/Object;", true);
         return var1;
      }
   }

   public static String kWr1(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String var1 = FLOW_TAG_PATTERN.matcher(var0).replaceAll("$2");
         var1 = FLOW_OPEN_PATTERN.matcher(var1).replaceAll("");
         return var1.replace("{/flow}", "");
      } else {
         return var0;
      }
   }

   private static String normalizeHexColor(String var0) {
      if (var0 == null) {
         return "#FFFFFF";
      } else {
         String var1 = var0.trim().toUpperCase(Locale.ROOT);
         if (!var1.startsWith("#")) {
            var1 = "#" + var1;
         }

         if (var1.length() == 4) {
            char var2 = var1.charAt(1);
            char var3 = var1.charAt(2);
            char var4 = var1.charAt(3);
            var1 = "#" + var2 + var2 + var3 + var3 + var4 + var4;
         }

         if (var1.length() != 7) {
            return "#FFFFFF";
         } else {
            try {
               Color.decode(var1);
               return var1;
            } catch (NumberFormatException var5) {
               return "#FFFFFF";
            }
         }
      }
   }

   private static String mfX4(String var0) {
      return ClientUtils.AOAtn(translateAmpersandCodes(var0)).trim();
   }

   private static String translateAmpersandCodes(String var0) {
      if (var0 != null && var0.indexOf(38) >= 0) {
         StringBuilder var1 = new StringBuilder(var0.length());

         for (int var2 = 0; var2 < var0.length(); var2++) {
            char var3 = var0.charAt(var2);
            if (var3 == '&' && var2 + 1 < var0.length()) {
               char var4 = Character.toLowerCase(var0.charAt(var2 + 1));
               if ("0123456789abcdefklmnor".indexOf(var4) >= 0) {
                  var1.append('§').append(var4);
                  var2++;
                  continue;
               }
            }

            var1.append(var3);
         }

         return var1.toString();
      } else {
         return var0 == null ? "" : var0;
      }
   }
}
