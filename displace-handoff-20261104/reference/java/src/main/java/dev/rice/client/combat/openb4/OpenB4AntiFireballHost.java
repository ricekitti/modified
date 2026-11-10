package dev.rice.client.combat.openb4;

import dev.rice.client.GuiModuleList;
import dev.rice.client.GuiModuleSettings;
import dev.rice.client.render.RecoveredGuiScreen;
import dev.rice.client.rotation.RotationService;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * Hand-written live bindings for the generated {@link OpenB4AntiFireballBackend}. They stand in for OpenB4's
 * setting objects, its Settings module, Scaffold's active flag, the Forge join event and the camera rotation.
 * The OpenB4 logic itself is in the generated file.
 */
public final class OpenB4AntiFireballHost {
   private OpenB4AntiFireballHost() {
   }

   /**
    * Every outbound packet, at NetworkManager.sendPacket head (OpenB4 Hooks.onSend calls PacketUtils.track before
    * any module can cancel). The C03 rotation is OpenB4's RotationManager.setLast value: the last yaw/pitch the
    * server received.
    */
   public static void observeSend(Packet<?> packet) {
      OpenB4AntiFireballBackend.PacketUtils.track(packet);
      if (packet instanceof C03PacketPlayer && ((C03PacketPlayer)packet).getRotating()) {
         OpenB4AntiFireballBackend.RotationManager.lastYaw = ((C03PacketPlayer)packet).getYaw();
         OpenB4AntiFireballBackend.RotationManager.lastPitch = ((C03PacketPlayer)packet).getPitch();
      }
   }

   /**
    * OpenB4 never writes a silent rotation into rotationYaw, so there rotationYaw is always the camera. While Rice
    * holds a silent rotation, the camera is kept by RotationService instead.
    */
   public static float cameraYaw() {
      return RotationService.storedRotationValid() ? RotationService.cameraYaw() : Minecraft.getMinecraft().thePlayer.rotationYaw;
   }

   public static float cameraPitch() {
      return RotationService.storedRotationValid() ? RotationService.cameraPitch() : Minecraft.getMinecraft().thePlayer.rotationPitch;
   }

   /** OpenB4 Overlay.ownScreen: the client's own ClickGui / HUD editor. */
   public static boolean ownScreen(GuiScreen screen) {
      return screen instanceof RecoveredGuiScreen || screen instanceof GuiModuleList || screen instanceof GuiModuleSettings;
   }

   /** OpenB4 SliderSetting over a Rice NumberSetting, or a fixed OpenB4 default. */
   public static final class SliderSetting {
      private final NumberSetting value;
      private final double fixed;
      private final double min;

      public SliderSetting(NumberSetting value) {
         this.value = value;
         this.fixed = value.doubleValue();
         this.min = value.getMinimum();
      }

      SliderSetting(double fixed, double min) {
         this.value = null;
         this.fixed = fixed;
         this.min = min;
      }

      public double getInput() {
         return this.value == null ? this.fixed : this.value.doubleValue();
      }

      public boolean isAtMin() {
         return this.getInput() == this.min;
      }
   }

   /** OpenB4 ButtonSetting: upstream code reads the public {@code enabled} field, refreshed by {@link #sync()}. */
   public static final class ButtonSetting {
      private final BooleanSetting value;
      public boolean enabled;

      public ButtonSetting(BooleanSetting value) {
         this.value = value;
         this.sync();
      }

      ButtonSetting(boolean fixed) {
         this.value = null;
         this.enabled = fixed;
      }

      public void sync() {
         if (this.value != null) {
            this.enabled = this.value.isEnabled();
         }
      }

      public boolean isToggled() {
         return this.enabled;
      }
   }

   /** OpenB4 SettingsModule values reached by AntiFireball. Rice has no such module, so OpenB4's modules.json defaults apply. */
   public static final class SettingsModule {
      public static final SliderSetting jitterYaw = new SliderSetting(3.0, 0.0);
      public static final SliderSetting aimClosestPoint = new SliderSetting(90.0, 0.0);
      public static final ButtonSetting chatAsInventory = new ButtonSetting(false);

      private SettingsModule() {
      }
   }

   /** OpenB4 ScaffoldModule.active, refreshed from Rice's Scaffold before each backend call. */
   public static final class ScaffoldModule {
      public static boolean active;

      private ScaffoldModule() {
      }
   }

   /** Forge EntityJoinWorldEvent, raised from World.spawnEntityInWorld (see GameHooks.entityJoin). */
   public static final class EntityJoinWorldEvent {
      public final Entity entity;

      public EntityJoinWorldEvent(Entity entity) {
         this.entity = entity;
      }
   }
}
