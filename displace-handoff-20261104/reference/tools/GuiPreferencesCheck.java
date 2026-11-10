import dev.rice.client.ClientContext;
import dev.rice.config.*;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.*;
import com.google.gson.*;
import java.nio.file.*;

public final class GuiPreferencesCheck {
   static final class Gui extends Module {
      final ChoiceSetting scale = add(new ChoiceSetting("scale", "Scale", 0, "Small", "Large"));
      final BooleanSetting wide = add(new BooleanSetting("wide", "Wide", false));
      final ColorSetting accent = add(new ColorSetting("accent", "Accent"));
      Gui() {
         super("gui", "GUI", "preferences fixture", ModuleCategory.RENDER);
         setBinding(99);
         accent.set(new ColorSetting.Value(1, .35F, 0));
         for (Setting<?> setting : getSettings()) { setting.parseAndSet(setting.format()); setting.saveCurrent(); }
      }
   }
   static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
   public static void main(String[] args) throws Exception {
      Path directory = Files.createTempDirectory(Paths.get(args[0]), "gui-preferences-test-");
      Path file = directory.resolve("gui.json");
      ModuleManager manager = new ModuleManager(new ClientContext(null));
      Gui gui = new Gui(); manager.register(gui);
      GuiPreferences preferences = new GuiPreferences(file);
      manager.setPersistence(() -> { try { preferences.save(gui); } catch (Exception error) { throw new RuntimeException(error); } });
      gui.scale.set(1); gui.wide.set(true); gui.setBinding(54); manager.persist();
      require(Files.isRegularFile(file), "GUI edit did not autosave");
      JsonObject legacy = new JsonObject(), record = new JsonObject();
      record.addProperty("scale", "Small"); record.addProperty("wide", false); record.addProperty("__bind", 1);
      legacy.add("GUI", record);
      RecoveredConfiguration.apply(manager, legacy, RecoveredConfiguration.Scope.PROFILE);
      RecoveredConfiguration.apply(manager, legacy, RecoveredConfiguration.Scope.GLOBAL);
      RecoveredConfiguration.resetProfile(manager);
      require(gui.scale.index()==1 && gui.wide.isEnabled() && gui.getBinding()==54, "profile changed GUI");
      require(!RecoveredConfiguration.serialize(manager, RecoveredConfiguration.Scope.PROFILE, java.util.Collections.emptyList()).has("GUI"), "GUI leaked into combat profile");
      Gui restarted = new Gui(); new GuiPreferences(file).load(restarted);
      require(restarted.scale.index()==1 && restarted.wide.isEnabled() && restarted.getBinding()==54, "GUI did not survive relaunch");
      String first = new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8);
      manager.persist();
      require(first.equals(new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8)), "unchanged preferences were altered");
      System.out.println("PASS: GUI autosave, profile isolation, default profile reset, keybind, and relaunch persistence");
   }
}
