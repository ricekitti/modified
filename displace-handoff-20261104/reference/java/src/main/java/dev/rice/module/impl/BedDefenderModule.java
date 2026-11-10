package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.beddefense.BedDefenseApi;
import dev.rice.client.beddefense.BedDefenseBackend;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import net.minecraft.client.Minecraft;

/**
 * Bed Defender: Sam's OpenB4 Bed Defender script ("v1.0 by winnie") ported native. The script body is
 * {@link BedDefenseBackend} (extends {@link BedDefenseApi}, which rebinds the OpenB4 script API to Rice's engine);
 * this class is its ClickGUI/config face and SemanticEvent router. The script owns its own block placement — the
 * server-synced raycast that fixes ghost blocks (it tracks serverYaw/serverPitch from the outbound C03 it observes
 * at event 23 and ray-traces with them, placing only once the outbound hook has observed the sent rotation). Rotations
 * are published silently through {@link RotationService} (the AutoClutch / Block In model), never through OpenJade.
 */
public final class BedDefenderModule extends Module {
   // Below Block In (1000): the bed builder should never fight Block In's own clutch rotations.
   private static final int ROTATION_PRIORITY = 800;
   private final BedDefenseBackend backend;
   private float[] rotations;
   private volatile boolean initialized;
   private volatile net.minecraft.client.multiplayer.WorldClient sessionWorld;
   private volatile net.minecraft.client.entity.EntityPlayerSP sessionPlayer;

   public BedDefenderModule(ModuleManager modules) {
      super("bed_defender", "Bed Defender", "Automatic bed defense builder (OpenB4, native)", ModuleCategory.WORLD);
      BedDefenseApi.bind(Minecraft.getMinecraft(), modules, this::add, this);
      this.backend = new BedDefenseBackend();
      this.backend.onLoad();
   }

   private void resetSession() {
      this.initialized = false;
      this.backend.onDisable();
      this.rotations = null;
      this.sessionWorld = null;
      this.sessionPlayer = null;
   }

   private void startSession(ClientContext c) {
      this.resetSession();
      if (!c.inputReady() || c.minecraft().currentScreen != null) return;
      this.backend.onEnable();
      if (this.isEnabled()) {
         this.sessionWorld = c.world();
         this.sessionPlayer = c.player();
         this.initialized = true;
      }
   }

   @Override
   protected void onEnable(ClientContext context) { this.startSession(context); }

   @Override
   protected void onDisable(ClientContext context) { this.resetSession(); }

   @Override
   public void onWorldUnload(ClientContext context) { this.resetSession(); }

   @Override
   public boolean listens(int type) {
      // 17 pre-update brain (place + aim), 13 rotation finalize, 23 outbound send (serverYaw tracking),
      // 6 render-world (ghost box), 26 world unload.
      return type == 6 || type == 13 || type == 17 || type == 23 || type == 26;
   }

   @Override
   public void onEvent(ClientContext c, SemanticEvent e) {
      if (e.type == 26) { this.resetSession(); return; }
      if (e.type == 17) {
         if (!c.inputReady() || c.minecraft().currentScreen != null) { this.resetSession(); return; }
         if (!this.initialized || this.sessionWorld != c.world() || this.sessionPlayer != c.player()) this.startSession(c);
         if (!this.initialized) return;
         this.backend.onPreUpdate();
         if (c.player() != null) {
            float yaw = c.player().rotationYaw;
            float pitch = c.player().rotationPitch;
            this.rotations = this.backend.getRotations(yaw, pitch);
            if (this.rotations != null && RotationService.claim(ROTATION_PRIORITY)) {
               // Silent: the camera stays on the real look while the build rotation is still sent to the server,
               // which the script's server-synced raycast then confirms on the following tick before placing.
               RotationService.retain(c, false, yaw, pitch);
               c.player().rotationYaw = this.rotations[0];
               c.player().rotationPitch = this.rotations[1];
            } else {
               this.rotations = null;
            }
         }
      } else if (this.initialized && c.ready() && this.sessionWorld == c.world() && this.sessionPlayer == c.player() && e.type == 13 && this.rotations != null) {
         e.active = true;
         e.overrideRotation = false;
      } else if (this.initialized && c.ready() && this.sessionWorld == c.world() && this.sessionPlayer == c.player() && e.type == 23 && e.packet != null) {
         if (!this.backend.onPacketSent(BedDefenseApi.wrapOutbound(e.packet))) {
            e.cancelled = true;
         }
      } else if (this.initialized && c.ready() && this.sessionWorld == c.world() && this.sessionPlayer == c.player() && e.type == 6) {
         this.backend.onRenderWorld(0.0F);
      }
   }

   /** GameHooks.openJadeMouseEvent: OpenB4 onMouse returns false to cancel the click; invert to "should cancel". */
   public boolean cancelMouse(int button) {
      return this.isEnabled() && this.initialized && !this.backend.onMouse(button, true);
   }
}
