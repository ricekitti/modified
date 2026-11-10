package dev.rice.jade.client.module.client;

import dev.rice.jade.client.setting.BooleanSetting;
import dev.rice.jade.client.setting.SliderSetting;

/**
 * Rice host shim for the OpenJade client Settings statics that the ported code reads. Rice has no Jade Settings
 * module, so each one is fixed at upstream's default (jade/client/module/client/Settings.java):
 * <ul>
 *   <li>"Rotate body" true and "Full body" false: FIdTNZFlk0 reads both on every rebuilt walking packet.</li>
 *   <li>The arraylist theme timing (Offset 0.5, Time multiplier 0.5).</li>
 * </ul>
 */
public final class Settings {
   public static BooleanSetting rotateBody = new BooleanSetting("Rotate body", true);
   public static BooleanSetting fullBody = new BooleanSetting("Full body", false);
   public static SliderSetting offset = new SliderSetting("Offset", 0.5, -3.0, 3.0, 0.1);
   public static SliderSetting timeMultiplier = new SliderSetting("Time multiplier", 0.5, 0.1, 4.0, 0.1);

   private Settings() {
   }
}
