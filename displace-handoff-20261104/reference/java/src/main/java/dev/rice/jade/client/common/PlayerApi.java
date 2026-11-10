// Jade recovery: original class: jade.deps.eLz.dd8H7bUpF
package dev.rice.jade.client.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.UUID;

public class PlayerApi {

   public static PlayerApi$2 lookupProfileByName(String var0) {
      String var1 = HttpUtils.httpRequestWithTimeouts("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + var0, false, false, 5000, 7000);
      PlayerApi$2 var2 = parseProfile(var1);
      if (var2 == null) {
         var1 = HttpUtils.httpRequestWithTimeouts("https://api.mojang.com/users/profiles/minecraft/" + var0, false, false, 5000, 7000);
         var2 = parseProfile(var1);
      }

      return var2;
   }

   private static PlayerApi$2 parseProfile(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         try {
            JsonObject var1 = parseJsonObject(var0);
            String var2 = var1.has("id") ? var1.get("id").getAsString() : "";
            String var3 = var1.has("name") ? var1.get("name").getAsString() : "";
            UUID var4 = parseUndashedUuid(var2);
            return var4 == null ? null : new PlayerApi$2(var4, var3);
         } catch (Exception var5) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static UUID parseUndashedUuid(String var0) {
      if (var0 != null && var0.length() == 32) {
         try {
            return UUID.fromString(
               var0.substring(0, 8) + "-" + var0.substring(8, 12) + "-" + var0.substring(12, 16) + "-" + var0.substring(16, 20) + "-" + var0.substring(20)
            );
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static JsonObject parseJsonObject(String var0) {
      return new JsonParser().parse(var0).getAsJsonObject();
   }
}
