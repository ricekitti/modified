package dev.rice.config;

import com.google.gson.JsonObject;
import dev.rice.module.ModuleManager;
import java.util.Collections;

public final class ConfigurationSession {
   private volatile String currentProfile;
   private volatile boolean pending;
   private JsonObject savedProfile;

   public String currentProfile() {
      return this.currentProfile;
   }

   public boolean pending() {
      return this.pending;
   }

   public synchronized boolean pending(ModuleManager manager) {
      return this.savedProfile == null ? this.pending
         : !this.savedProfile.equals(RecoveredConfiguration.serialize(manager,
            RecoveredConfiguration.Scope.PROFILE, Collections.emptyList()));
   }

   public synchronized void profileApplied(String name, ModuleManager manager) {
      this.profileApplied(name);
      this.savedProfile = RecoveredConfiguration.serialize(manager,
         RecoveredConfiguration.Scope.PROFILE, Collections.emptyList());
   }

   public void controlChanged() {
      this.pending = true;
   }

   public synchronized void profileApplied(String name) {
      this.savedProfile = null;
      this.currentProfile = name;
      this.pending = false;
   }
}
