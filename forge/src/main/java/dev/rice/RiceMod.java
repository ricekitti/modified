package dev.rice;

import dev.rice.client.ClientEventBridge;
import dev.rice.client.RiceCommand;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.render.GuiAppearance;
import dev.rice.client.render.ProfileServices;
import dev.rice.config.ConfigStore;
import dev.rice.config.InitialSettings;
import dev.rice.config.GuiPreferences;
import dev.rice.inject.ReloadState;
import dev.rice.module.CoreModules;
import dev.rice.module.ModuleManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RiceMod {
   public static final String MOD_ID = "ricemodified";
   public static final String NAME = "Rice Modified";
   public static final String VERSION = "0.2.2-forge";
   private ModuleManager modules;
   private ConfigStore config;
   private final Logger logger = LogManager.getLogger("Rice Semantic");

   public void preInitialize() {
      dev.rice.client.hook.HookLifecycle.stop();
      dev.rice.security.LicenseRuntime.requireValid();
      this.modules = new ModuleManager();
      CoreModules.register(this.modules);
      GuiAppearance.configureMissing(this.modules.find("GUI"));
      InitialSettings.applyMissing(this.modules);
      dev.rice.module.Module notifications = this.modules.find("notifications");
      if (notifications != null) {
         this.modules.requestEnabled(notifications, true);
      }
      GameHooks.install(this.modules);
      Path base = riceDirectory();
      Path configPath = base.resolve("rice-semantic.json");

      try {
         Files.createDirectories(base);
      } catch (IOException var5) {
         this.logger.error("Could not create the configuration directory", var5);
      }

      this.config = new ConfigStore(configPath);
      final GuiPreferences guiPreferences = new GuiPreferences(base.resolve("gui.json"));
      try {
         guiPreferences.load(this.modules.find("GUI"));
         guiPreferences.save(this.modules.find("GUI"));
      } catch (IOException error) {
         this.logger.error("Could not load GUI preferences", error);
      }
      this.modules.setPersistence(() -> {
         try { guiPreferences.save(this.modules.find("GUI")); }
         catch (IOException error) { this.logger.error("Could not save GUI preferences", error); }
      });
      ProfileServices.FilesHost profiles = new ProfileServices.FilesHost(this.modules, base.resolve("profiles"), this.config);
      this.modules.input().profiles(this.config, profiles);
      try {
         // Session state is intentionally ephemeral. Named profiles are still
         // available through the Profiles page, but module toggles and setting
         // edits from a previous run must never be restored automatically.
         // GUI appearance/keybind lives independently in gui.json.
         this.config.discardSavedSession();
      } catch (IOException var4) {
         this.logger.error("Could not discard the previous Rice session", var4);
      }
   }

   public void initialize() {
      dev.rice.security.LicenseRuntime.requireValid();
      GuiAppearance.configureMissing(this.modules.find("GUI"));
      KeyBinding openGui = new KeyBinding("key.ricesemantic.open", 54, "key.categories.ricesemantic");
      ClientEventBridge.install(new ClientEventBridge(this.modules, openGui, new RiceCommand(this.modules)));
      Thread hook = new Thread(new Runnable() {
         @Override
         public void run() {
            RiceMod.this.modules.persistOnExit();
         }
      }, "rice-config-save");
      ReloadState.replaceHook(hook);
      dev.rice.client.hook.HookLifecycle.start();
   }

   public ModuleManager modules() {
      return this.modules;
   }

   public static Path riceDirectory() {
      String home = System.getProperty("user.home");
      return Minecraft.getMinecraft().mcDataDir.toPath().resolve("config").resolve("rice-modified");
   }

}
