package dev.rice.jade.client.common;

import dev.rice.jade.client.core.ConfigProfile;

/** Rice host shim (type only): Rice saves settings itself, so Jade.Grq stays null and this is never used. */
public final class ConfigEntry {
   private final ConfigProfile profile = new ConfigProfile();

   public ConfigProfile getProfile() {
      return this.profile;
   }
}
