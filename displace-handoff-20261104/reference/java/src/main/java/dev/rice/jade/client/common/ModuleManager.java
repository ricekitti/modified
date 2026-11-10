package dev.rice.jade.client.common;

import dev.rice.jade.client.module.Module;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rice host shim for OpenJade's ModuleManager. Only the ported Jade module (Opsec) is registered, so
 * {@code getModule(X.class)} is null for every other Jade module (AutoClicker, BedwarsUtils, InventoryManager,
 * DelayRemover, ...), which is exactly upstream's behaviour when such a module is absent.
 */
public final class ModuleManager {
   private final List<Module> modules = new ArrayList<>();
   private Runnable toggled = () -> {
   };

   public synchronized void register(Module module) {
      if (!this.modules.contains(module)) {
         module.attachManager(this);
         this.modules.add(module);
      }
   }

   public void setToggleListener(Runnable listener) {
      this.toggled = listener == null ? () -> {
      } : listener;
   }

   public synchronized List<Module> getModules() {
      return Collections.unmodifiableList(new ArrayList<>(this.modules));
   }

   public synchronized <T extends Module> T getModule(Class<T> type) {
      for (Module module : this.modules) {
         if (module.getClass() == type) {
            return type.cast(module);
         }
      }

      return null;
   }

   public synchronized Module getModuleByName(String name) {
      for (Module module : this.modules) {
         if (module.getName().equalsIgnoreCase(name)) {
            return module;
         }
      }

      return null;
   }

   public void onModuleEnabled(Module module) {
   }

   public void onModuleDisabled(Module module) {
   }

   public void onModuleToggled(Module module, boolean enabled) {
      this.toggled.run();
   }
}
