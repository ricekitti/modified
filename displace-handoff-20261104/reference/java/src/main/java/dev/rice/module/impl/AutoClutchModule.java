package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.clutch.AutoClutchBackend;
import dev.rice.client.clutch.ClutchApi;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import net.minecraft.client.Minecraft;

/**
 * Native AutoClutch: Sam's OpenB4 AutoClutch script ("-original by afk, edited by noot-") as a real Rice module
 * instead of an emulated OpenB4 script runtime. The script body is {@link AutoClutchBackend} (extends
 * {@link ClutchApi}, which rebinds the OpenB4 script API to Rice's engine); this class is its ClickGUI/config face
 * and SemanticEvent router. The rotation publish (event 17) / finalize (event 13) order mirrors Block In, which is
 * OpenB4's own rotation-provider order. The fade-block render runs at the RenderWorldLast point (RenderHooks),
 * not the HUD pass, so it keeps the raw world GL state.
 */
public final class AutoClutchModule extends Module {
   /** Above Scaffold's 750, below Block In's 1000: clutch wins over Scaffold while falling, never fights Block In. */
   private static final int ROTATION_PRIORITY = 800;
   private static AutoClutchModule instance;
   private final AutoClutchBackend backend;
   private float[] rotations;

   public AutoClutchModule(ModuleManager modules) {
      super("autoclutch", "AutoClutch", "Clutches with blocks when knocked toward the void (OpenB4, native)", ModuleCategory.WORLD);
      ClutchApi.bind(Minecraft.getMinecraft(), modules, this::add);
      this.backend = new AutoClutchBackend();
      this.backend.onLoad();
      instance = this;
   }

   /** Called from RenderHooks at the RenderWorldLast point (mask 256), where OpenB4 ran the script's onRenderWorld. */
   public static void renderWorld(float partialTicks) {
      if (instance != null && instance.isEnabled()) {
         instance.backend.onRenderWorld(partialTicks);
      }
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.backend.onEnable();
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.backend.onDisable();
      this.rotations = null;
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      this.rotations = null;
   }

   @Override
   public boolean listens(int type) {
      // 17 pre-update brain, 13 rotation finalize, 27 movement input, 23 outgoing send, 3 inbound packet, 26 unload.
      return type == 13 || type == 17 || type == 23 || type == 27 || type == 3 || type == 26;
   }

   @Override
   public void onEvent(ClientContext c, SemanticEvent e) {
      if (e.type == 26) {
         this.backend.onDisable();
         this.rotations = null;
         return;
      }

      if (e.type == 17) {
         this.backend.onPreUpdate();
         if (c.player() != null) {
            float yaw = c.player().rotationYaw;
            float pitch = c.player().rotationPitch;
            this.rotations = this.backend.getRotations(yaw, pitch);
            if (this.rotations != null && RotationService.claim(ROTATION_PRIORITY)) {
               // Silent mode (Block In's model): retain the real look so the camera does not snap; the server
               // still receives the clutch rotation. Visible mode applies it to the camera directly.
               if (silent()) {
                  RotationService.retain(c, false, yaw, pitch);
               }
               c.player().rotationYaw = this.rotations[0];
               c.player().rotationPitch = this.rotations[1];
            } else {
               this.rotations = null;
            }
         }
      } else if (e.type == 13 && this.rotations != null) {
         e.active = true;
         if (silent()) {
            e.overrideRotation = false;
         } else {
            e.overrideRotation = true;
            e.yaw = this.rotations[0];
            e.pitch = this.rotations[1];
         }
      } else if (e.type == 27) {
         this.backend.onPostPlayerInput();
      } else if (e.type == 23 && e.packet != null) {
         if (!this.backend.onPacketSent(ClutchApi.wrapOutbound(e.packet))) {
            e.cancelled = true;
         }
      } else if (e.type == 3 && e.packet != null) {
         this.backend.onPacketReceived(ClutchApi.wrapInbound(e.packet));
      }
   }

   private boolean silent() {
      dev.rice.setting.Setting<?> setting = findSetting("silent");
      return setting instanceof dev.rice.setting.BooleanSetting && ((dev.rice.setting.BooleanSetting) setting).isEnabled();
   }

   @Override
   public int metadataCategory() {
      return 3;
   }
}
