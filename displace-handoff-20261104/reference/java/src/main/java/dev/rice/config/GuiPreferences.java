package dev.rice.config;

import com.google.gson.*;
import dev.rice.module.Module;
import dev.rice.setting.Setting;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** GUI-only preferences, independent of combat profiles and session resets. */
public final class GuiPreferences {
   private final Path path;
   private String saved;

   public GuiPreferences(Path path) { this.path = path; }

   public synchronized void load(Module gui) throws IOException {
      if (gui == null || !Files.isRegularFile(path)) return;
      try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
         JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
         JsonObject settings = root.getAsJsonObject("settings");
         if (settings != null) {
            for (Setting<?> setting : gui.getSettings()) {
               JsonElement value = settings.get(setting.getKey());
               if (value != null) {
                  try { SettingCodec.decode(setting, value); setting.saveCurrent(); }
                  catch (RuntimeException ignored) { }
               }
            }
         }
         if (root.has("binding")) gui.setBinding(root.get("binding").getAsInt());
         dev.rice.client.render.HudLayout.restoreOwnedPositions(gui);
         saved = encode(gui);
      } catch (RuntimeException error) { throw new IOException("Invalid GUI preferences", error); }
   }

   public synchronized void save(Module gui) throws IOException {
      if (gui == null) return;
      String text = encode(gui);
      if (text.equals(saved) && Files.isRegularFile(path)) return;
      Path directory = path.toAbsolutePath().getParent();
      Files.createDirectories(directory);
      Path temporary = Files.createTempFile(directory, "gui-", ".tmp");
      try {
         Files.write(temporary, text.getBytes(StandardCharsets.UTF_8));
         try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
         catch (AtomicMoveNotSupportedException unavailable) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
         saved = text;
      } finally { Files.deleteIfExists(temporary); }
   }

   private static String encode(Module gui) {
      JsonObject root = new JsonObject();
      root.addProperty("version", 1);
      root.addProperty("binding", gui.getBinding());
      JsonObject settings = new JsonObject();
      for (Setting<?> setting : gui.getSettings()) settings.add(setting.getKey(), SettingCodec.encode(setting));
      root.add("settings", settings);
      return root.toString();
   }
}
