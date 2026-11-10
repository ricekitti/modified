package dev.rice.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.rice.client.RecoveredMath;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RecoveredConfiguration {
   private RecoveredConfiguration() {
   }

   private static boolean included(Module module, RecoveredConfiguration.Scope scope) {
      if (module.getId().equals("gui")) return false;
      boolean global = (module.descriptorFlags() & 16) != 0;
      return global == (scope == RecoveredConfiguration.Scope.GLOBAL);
   }

   public static JsonObject serialize(
      ModuleManager manager, RecoveredConfiguration.Scope scope, Iterable<RecoveredConfiguration.ProfileBinding> profileBindings
   ) {
      JsonObject root = new JsonObject();

      for (Module module : manager.all()) {
         if (included(module, scope)) {
            JsonObject record = new JsonObject();
            int flags = module.descriptorFlags();
            if ((flags & 2) == 0 && module.isEnabled()) {
               record.addProperty("__on", true);
            }

            if ((flags & 1) == 0) {
               if (module.getBinding() != 0) {
                  record.addProperty("__bind", (double)Integer.toUnsignedLong(module.getBinding()));
               }

               if ((flags & 8) == 0 && module.isHoldBinding()) {
                  record.addProperty("__hold", true);
               }
            }

            for (Setting<?> setting : module.getSettings()) {
               if (setting.isConfigured()) {
                  record.add(setting.getKey(), SettingCodec.encode(setting));
               }
            }

            root.add(module.getName(), record);
         }
      }

      if (scope == RecoveredConfiguration.Scope.GLOBAL) {
         JsonArray bindings = new JsonArray();

         for (RecoveredConfiguration.ProfileBinding profile : profileBindings) {
            if (profile.binding != 0) {
               JsonObject recordx = new JsonObject();
               recordx.addProperty("name", profile.name);
               recordx.addProperty("bind", (double)Integer.toUnsignedLong(profile.binding));
               bindings.add(recordx);
            }
         }

         root.add("profile_binds", bindings);
      }

      return root;
   }

   public static List<RecoveredConfiguration.ProfileBinding> profileBindings(JsonObject root) {
      List<RecoveredConfiguration.ProfileBinding> result = new ArrayList<>();
      JsonElement rows = root.get("profile_binds");
      if (rows != null && rows.isJsonArray()) {
         for (JsonElement element : rows.getAsJsonArray()) {
            if (element.isJsonObject()) {
               JsonObject row = element.getAsJsonObject();
               JsonElement name = row.get("name");
               if (name != null && name.isJsonPrimitive() && name.getAsJsonPrimitive().isString()) {
                  result.add(new RecoveredConfiguration.ProfileBinding(name.getAsString(), binding(row.get("bind"))));
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   public static int binding(JsonElement node) {
      if (node != null && node.isJsonPrimitive() && node.getAsJsonPrimitive().isNumber()) {
         int converted = (int)RecoveredMath.truncateLong(node.getAsDouble() + 0.5);
         return Integer.compareUnsigned(converted - 104, -103) >= 0 ? converted : 0;
      } else {
         return 0;
      }
   }

   private static boolean enabled(JsonElement node) {
      return node != null && node.isJsonPrimitive() && node.getAsJsonPrimitive().isBoolean() && node.getAsBoolean();
   }

   // v147 used one record with KA-prefixed settings. Split it only if no explicit KA record exists.
   private static JsonObject moduleRecord(Module module, JsonObject root) {
      JsonElement node = root.get(module.getName());
      if (node != null && node.isJsonObject()) return node.getAsJsonObject();
      if (!module.getId().equals("killauradisplace")) return new JsonObject();
      JsonElement unified = root.get("Knockback Displace");
      if (unified == null || !unified.isJsonObject()) return new JsonObject();
      JsonObject record = unified.getAsJsonObject();
      JsonObject split = new JsonObject();
      boolean found = false;
      for (Setting<?> setting : module.getSettings()) {
         JsonElement value = record.get("ka_" + setting.getKey());
         if (value != null) { split.add(setting.getKey(), value); found = true; }
      }
      if (found && record.has("__on")) split.add("__on", record.get("__on"));
      // The merged bind stays on regular Displace; do not duplicate a key across two toggles.
      return split;
   }

   public static boolean hasRecords(ModuleManager manager, JsonObject root, RecoveredConfiguration.Scope scope) {
      for (Module module : manager.all()) {
         JsonElement node = root.get(module.getName());
         if (included(module, scope) && node != null && node.isJsonObject()) return true;
      }
      return false;
   }

   public static void apply(ModuleManager manager, JsonObject root, RecoveredConfiguration.Scope scope) {
      for (Module module : manager.all()) {
         if (included(module, scope)) {
            JsonObject record = moduleRecord(module, root);
            // The former OpenB4 pairing stored direction in its Mode choice.
            // Copy only that record so loading a profile never mutates its JSON.
            if (module.getId().equals("killauradisplace") && record.has("mode")
               && record.get("mode").isJsonPrimitive() && record.get("mode").getAsJsonPrimitive().isString()) {
               String oldMode = record.get("mode").getAsString();
               if (oldMode.equals("Left") || oldMode.equals("Right") || oldMode.equals("Random")) {
                  JsonObject migrated = new JsonObject();
                  for (java.util.Map.Entry<String, JsonElement> entry : record.entrySet()) migrated.add(entry.getKey(), entry.getValue());
                  migrated.addProperty("mode", "Offset");
                  if (!migrated.has("direction")) migrated.addProperty("direction", oldMode.equals("Right") ? "Right" : "Left");
                  record = migrated;
               }
            }
            if(module.getId().equals("anticheat")) {
               JsonObject migrated=new JsonObject();for(java.util.Map.Entry<String,JsonElement> entry:record.entrySet())migrated.add(entry.getKey(),entry.getValue());
               if(!migrated.has("flag_interval_ticks")&&migrated.has("flag_interval")&&migrated.get("flag_interval").isJsonPrimitive()&&migrated.get("flag_interval").getAsJsonPrimitive().isNumber())
                  migrated.addProperty("flag_interval_ticks",Math.max(0,Math.min(1200,migrated.get("flag_interval").getAsDouble()*20)));
               if(migrated.has("scaffold")) {if(!migrated.has("keep_y"))migrated.add("keep_y",migrated.get("scaffold"));if(!migrated.has("tower"))migrated.add("tower",migrated.get("scaffold"));}
               record=migrated;
            }
            int flags = module.descriptorFlags();
            if ((flags & 1) == 0) {
               manager.setBinding(module, binding(record.get("__bind")));
               module.setHoldBinding((flags & 8) == 0 && enabled(record.get("__hold")));
            }

            for (Setting<?> setting : module.getSettings()) {
               JsonElement value = record.get(setting.getKey());
               if (value == null) {
                  setting.restoreSaved();
               } else {
                  setting.restoreSaved();
                  // Only the retired Scaffold Keep-Y labels need migration; preserve the source labels on save.
                  if (module.getId().equals("scaffold") && setting.getKey().equals("keep-y")
                     && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                     String label = value.getAsString();
                     if (label.equalsIgnoreCase("TELLY")) value = new com.google.gson.JsonPrimitive("Keep-Y");
                     else if (label.equalsIgnoreCase("EXTRA")) value = new com.google.gson.JsonPrimitive("Extrea");
                     else if (label.equalsIgnoreCase("VANILLA")) value = new com.google.gson.JsonPrimitive("Vanilla");
                  }
                  SettingCodec.decode(setting, value);
               }
            }

            if ((flags & 2) == 0) {
               manager.requestEnabled(module, enabled(record.get("__on")));
            }
         }
      }
   }

   public static void resetProfile(ModuleManager manager) {
      reset(manager, RecoveredConfiguration.Scope.PROFILE);
   }

   public static void reset(ModuleManager manager, RecoveredConfiguration.Scope scope) {
      for (Module module : manager.all()) {
         if (included(module, scope)) {
            manager.setBinding(module, 0);
            module.setHoldBinding(false);
            if ((module.descriptorFlags() & 2) == 0) {
               manager.requestEnabled(module, false);
            }

            for (Setting<?> setting : module.getSettings()) {
               setting.restoreSaved();
            }
         }
      }
   }

   public static final class ProfileBinding {
      public final String name;
      public final int binding;

      public ProfileBinding(String name, int binding) {
         this.name = Objects.requireNonNull(name);
         this.binding = binding;
      }
   }

   public static enum Scope {
      GLOBAL,
      PROFILE;
   }
}
