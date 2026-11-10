package dev.rice.client.hook;

import dev.rice.client.MinecraftFields;
import dev.rice.client.mayu.MayuHost;
import dev.rice.client.SemanticEvent;
import dev.rice.client.anticheat.SnowifyAntiCheatHost;
import dev.rice.client.combat.openb4.OpenB4AntiFireballHost;
import dev.rice.client.inventory.InvWalkControllerInputAdapter;
import dev.rice.client.openjade.JadeRuntime;
import dev.rice.client.openjade.OpenJadeHost;
import dev.rice.client.packet.NetworkSendAdapter;
import dev.rice.client.packet.PacketPipeline;
import dev.rice.client.placement.PlacementSelection;
import dev.rice.client.rotation.RotationService;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.AntiFireballModule;
import dev.rice.module.impl.NoHurtCamModule;
import dev.rice.module.impl.SafeWalkModule;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import myau.host.MayuMixins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;

public final class GameHooks {
   private static ModuleManager modules;
   private static SemanticEvent ray;
   private static double previousDisplaySeconds;
   private static final TargetCombatStatus combat = new TargetCombatStatus();
   private static boolean uiDrawn;
   private static boolean baseSwing;

   private GameHooks() {
   }

   public static void install(ModuleManager manager) {
      modules = manager;
      MayuHost.install(manager);
      RenderHooks.install(manager);
   }

   private static boolean local(Entity entity) {
      return modules != null && entity == Minecraft.getMinecraft().thePlayer;
   }

   public static boolean mouseAngles(Entity entity, float yaw, float pitch) {
      return local(entity) && RotationService.mouseAngles(entity, modules.context(), yaw, pitch);
   }

   public static void beginMovement() {
      if (modules != null) {
         RotationService.beginMovement(modules.context());
      }
   }

   public static void endMovement() {
      if (modules != null) {
         RotationService.endMovement(modules.context());
      }
   }

   public static void updateMovement(MovementInput input) {
      beginMovement();

      try {
         input.updatePlayerMoveState();
      } finally {
         endMovement();
      }
   }

   /**
    * MovementInput.updatePlayerMoveState return (EntityPlayerSP.onLivingUpdate), right after Mayu's
    * afterMovementInputUpdate (Scaffold/BedNuker move fix): Rice modules' movement input, then the Stasis lock.
    */
   public static void afterMovementInput(EntityPlayerSP player) {
      if (modules != null && player == modules.context().player()) {
         MovementInput input = player.movementInput;
         SemanticEvent event = new SemanticEvent(SemanticEvent.MOVEMENT_INPUT);
         event.entity = player;
         event.forward = input.moveForward;
         event.sideways = input.moveStrafe;
         modules.dispatch(event);
         input.moveForward = event.forward;
         input.moveStrafe = event.sideways;
         dev.rice.client.blockin.SuppliedBlockInHost.adjustInput(input);
         dev.rice.client.stasis.StasisInputLock.apply(input);
      }
   }

   /**
    * Minecraft.runTick HEAD, before Mayu's PreTickEvent: while Mayu Scaffold or BedNuker is enabled, Mayu claims the
    * tick's rotation (priority 750), so lower Rice rotation owners (KillAura, Displace, Aim Assist, AntiFireball) stand
    * down this tick, as OpenB4/Jade had the Scaffold rotate first.
    */
   public static void mayuTickHead() {
      if (modules != null) {
         MayuHost.rotationAllowed();
      }
   }

   /**
    * EntityPlayerSP.onUpdate HEAD: Mayu MixinEntityPlayerSP.beforePlayerUpdate (RotationUpdateEvent). Rice's rotation
    * arbitration decides first whether Mayu may rotate this tick (MayuHost.rotationAllowed); when a higher-priority Rice
    * owner holds the frame, the generated arbitrated variant runs the same body without posting the event.
    */
   public static void mayuPlayerUpdateHead(EntityPlayerSP player) {
      if (modules != null && modules.context().player() == player && !MayuHost.rotationAllowed()) {
         MayuMixins.beforePlayerUpdateArbitrated(player);
      } else {
         MayuMixins.beforePlayerUpdate(player);
      }
      if (modules != null && modules.context().player() == player) {
         dev.rice.module.impl.KillAuraDisplaceModule.reapplyMovementState(modules.context());
      }
      dev.rice.client.blockin.SuppliedBlockInHost.prePlayer(player);
   }

   /**
    * EntityPlayerSP.onUpdateWalkingPlayer head. OpenB4 AntiCheat's onPreMotion (OpenB4 Hooks.preMotion, same site), so
    * its /report or /pc chat goes out before this tick's C03.
    */
   public static boolean walkingPlayerHead(EntityPlayerSP player) {
      if (modules != null && modules.context().player() == player) {
         return false;
      }
      return false;
   }

   // --- OpenJade (Opsec) bridges; each forwards to the verbatim Jade agent hook for the same Minecraft site and is
   // --- inert unless Opsec is enabled (see JadeRuntime).

   /** Minecraft.runGameLoop head. */
   public static void jadeGameLoopStart(Minecraft minecraft) {
      if (modules != null) {
         JadeRuntime.gameLoopStart(minecraft);
      }
   }

   /** GuiIngame.renderGameOverlay returns. */
   /** The only live Mayu overlay is BedNuker's progress; retain its original event site and body. */
   public static void hudRenderTitle(float partialTicks){
      if(dev.rice.client.render.HudLayout.editing())return;
      if(!dev.rice.client.render.HudLayout.shifted("bednuker")){MayuMixins.afterRenderTitle(partialTicks);return;}
      net.minecraft.client.gui.ScaledResolution sr=new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
      net.minecraft.client.renderer.GlStateManager.pushMatrix();
      try{net.minecraft.client.renderer.GlStateManager.translate(dev.rice.client.render.HudLayout.offset("bednuker",true)*sr.getScaledWidth(),dev.rice.client.render.HudLayout.offset("bednuker",false)*sr.getScaledHeight(),0);MayuMixins.afterRenderTitle(partialTicks);}
      finally{net.minecraft.client.renderer.GlStateManager.popMatrix();}
   }

   public static void jadeRenderHud(float partialTicks) {
      if (modules != null) {
         JadeRuntime.renderHud(partialTicks);
         if(!dev.rice.client.render.HudLayout.editing()){
            dev.rice.client.blockin.SuppliedBlockInHost.renderHud();
            SnowifyAntiCheatHost.renderHud();
         }
         // OpenB4 Scaffold block counter (OpenB4 renders it in the same scaled HUD pass).
      }
   }

   /**
    * NetworkManager.channelRead0 head (Netty thread); true = the inbound packet is cancelled. Order: OpenB4
    * Hooks.onReceive observers (Latency Alerts' packet clock, AntiCheat) and the OpenJade packet observer, which never
    * cancel; then Mayu's MixinNetworkManager.onChannelRead (RawPacketReceiveEvent, incoming buffer, PacketReceiveEvent),
    * which sees every packet on arrival; then OpenJade's Opsec queue.
    */
   public static boolean receivePacket(ChannelHandlerContext context, Packet<?> packet) {
      if (modules == null) {
         return false;
      }

      SnowifyAntiCheatHost.received(packet);
      OpenJadeHost.observePacket(packet, false);
      return MayuMixins.onChannelRead(context, packet) || JadeRuntime.networkReceive(packet);
   }

   /** NetworkManager.sendPacket(Packet, listener, listeners...) head: Mayu MixinNetworkManager.onSendPacketWithListeners. */
   public static boolean mayuSendWithListeners(Packet<?> packet, GenericFutureListener listener, GenericFutureListener[] listeners) {
      return modules != null && MayuMixins.onSendPacketWithListeners(packet, listener, listeners);
   }

   /**
    * World.spawnEntityInWorld head. Fires where Forge posts EntityJoinWorldEvent: after the loaded-chunk check
    * (isChunkLoaded(x, z, true) is chunkProvider.chunkExists), for the client world only.
    */
   public static void entityJoin(World world, Entity entity) {
      if (modules == null || entity == null || world != modules.context().world()) {
         return;
      }

      int chunkX = MathHelper.floor_double(entity.posX / 16.0);
      int chunkZ = MathHelper.floor_double(entity.posZ / 16.0);
      if (!entity.forceSpawn && !(entity instanceof EntityPlayer) && !world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
         return;
      }

      Module module = modules.find("antifireball");
      if (module instanceof AntiFireballModule && module.isEnabled()) {
         ((AntiFireballModule)module).entityJoined(modules.context(), entity);
      }

      SnowifyAntiCheatHost.joined(entity);
   }

   public static boolean openJadeMouseEvent(int button) {
      if (modules == null || button < 0) return false;
      Module defender = modules.find("bed_defender");
      return defender instanceof dev.rice.module.impl.BedDefenderModule && defender.isEnabled()
         && ((dev.rice.module.impl.BedDefenderModule)defender).cancelMouse(button);
   }

   public static void jadeContainerDrawSlotBegin(Object container, Object slot) {
      JadeRuntime.containerDrawSlotBegin(container, slot);
   }

   public static void jadeContainerDrawSlotEnd(Object container) {
      JadeRuntime.containerDrawSlotEnd(container);
   }

   public static ItemStack jadeContainerDisplayedStack(Object container, ItemStack stack) {
      return (ItemStack)JadeRuntime.containerDisplayedStack(container, stack);
   }

   public static void jadeContainerDrawScreen(Object container, int mouseX, int mouseY) {
      JadeRuntime.containerDrawScreen(container, mouseX, mouseY);
   }

   public static boolean jadeContainerManualInput(Object container) {
      return JadeRuntime.containerManualInput(container);
   }

   public static boolean jadeContainerClick(Object container, Object slot, int slotId, int button, int mode) {
      return JadeRuntime.containerClick(container, slot, slotId, button, mode);
   }

   public static void beginCamera() {
      if (modules != null) {
         RotationService.beginCamera(modules.context());
      }
   }

   public static void endCamera() {
      RotationService.endCamera();
   }

   public static void endRender() {
      RotationService.endRender();
   }

   public static void beginRenderInfo() {
      if (modules != null) {
         RotationService.beginRenderInfo(modules.context());
      }
   }

   public static void endRenderInfo() {
      RotationService.endRenderInfo();
   }

   public static float renderViewPitch(float vanilla) {
      return RotationService.renderViewPitch(vanilla);
   }

   public static float renderViewYaw(float vanilla) {
      return RotationService.renderViewYaw(vanilla);
   }

   public static void beginTick() {
      if (modules != null) {
         combat.select(modules.context(), modules);
      } else {
         combat.reset();
      }
   }

   public static EntityPlayer target() {
      return combat.target();
   }

   public static double targetDistanceSquared() {
      return combat.distanceSquared();
   }

   public static void beforeEntities(World world) {
      if (modules != null && world == modules.context().world()) {
         modules.dispatch(new SemanticEvent(16));
         beginMovement();
         modules.dispatch(new SemanticEvent(17));
      }
   }

   public static void afterEntities(World world) {
      if (modules != null && world == modules.context().world()) {
         modules.dispatch(new SemanticEvent(18));
         if (!modules.context().ready()) {
            endMovement();
         } else {
            SemanticEvent event = new SemanticEvent(13);
            event.active = false;
            event.yaw = RotationService.storedRotationValid() ? RotationService.cameraYaw() : modules.context().player().rotationYaw;
            event.pitch = RotationService.storedRotationValid() ? RotationService.cameraPitch() : modules.context().player().rotationPitch;
            modules.dispatch(event);
            RotationService.finishFrame(modules.context(), event);
            RotationService.applyPending(modules.context(), true);
         }
      }
   }

   public static void abortEntities(World world) {
      if (modules != null && world == modules.context().world()) {
         endMovement();
      }
   }

   public static SemanticEvent beforeMoveFlying(Entity player, float sideways, float forward, float friction) {
      SemanticEvent event = new SemanticEvent(21);
      event.sideways = sideways;
      event.forward = forward;
      event.factor = friction;
      if (!local(player)) {
         return event;
      } else {
         event.jump = MinecraftFields.jumpTicks((EntityPlayerSP)player) > 0 && modules.context().minecraft().gameSettings.keyBindJump.isKeyDown();
         modules.dispatch(event);
         return event;
      }
   }

   public static void afterMoveFlying(Entity player, SemanticEvent before) {
      if (local(player)) {
         SemanticEvent event = new SemanticEvent(22);
         event.sideways = before.sideways;
         event.forward = before.forward;
         event.factor = before.factor;
         event.jump = before.jump;
         modules.dispatch(event);
      }
   }

   public static boolean safeWalk(Entity entity) {
      if (local(entity)) {
         Module module = modules.find("safewalk");
         if (module instanceof SafeWalkModule && ((SafeWalkModule)module).allowed()) {
            return true;
         }
      }

      return entity.isSneaking();
   }

   public static void beginRay(float partialTicks) {
      ray = null;
      if (modules != null && modules.context().ready()) {
         RotationService.beginFrame(modules.context(), 0.0, partialTicks);
         beginTick();
         modules.dispatch(new SemanticEvent(0));
         if (modules.context().minecraft().inGameHasFocus) {
            ray = new SemanticEvent(8);
            ray.distance = 3.0;
            modules.dispatch(ray);
         }
      }
   }

   public static void afterFrameRender() {
      if (modules != null) {
         uiDrawn = true;
         RenderHooks.beforeDisplay();
      }
   }

   public static strictfp void beforeDisplay() {
      if (modules != null) {
         if (!uiDrawn) {
            RenderHooks.beforeDisplay();
         }

         uiDrawn = false;
         modules.applyPendingChanges();
         beforeDisplay((double)System.nanoTime() / 1.0E9);
      }
   }

   private static strictfp void beforeDisplay(double now) {
      if (previousDisplaySeconds == 0.0) {
         previousDisplaySeconds = now;
      }

      if (!modules.context().ready()) {
         previousDisplaySeconds = now;
      } else {
         RotationService.beforeDisplay(modules.context());
         double elapsed = now - previousDisplaySeconds;
         if (!(0.002 > elapsed)) {
            SemanticEvent frame = new SemanticEvent(1);
            frame.elapsedSeconds = elapsed;
            frame.timerRenderPartialTicks = MinecraftFields.timer(modules.context().minecraft()).renderPartialTicks;
            modules.dispatch(frame);
            RotationService.applyPending(modules.context(), false);
            previousDisplaySeconds = now;
            RotationService.afterDisplay(modules.context());
         }
      }
   }

   public static boolean customRay() {
      boolean selected = ray != null && RayQueryAdapter.update(modules, ray);
      if (ray != null) {
         PlacementSelection.frameReset();
      }

      return selected;
   }

   public static void prePlayerInteract() {
      if (modules != null && modules.context().ready() && !guiBlocksClicks()) {
         modules.dispatch(new SemanticEvent(SemanticEvent.PRE_PLAYER_INTERACT));
      }
   }

   public static void beforeTickMouseOver() {
      if (modules != null && modules.context().ready()) {
         modules.dispatch(new SemanticEvent(SemanticEvent.PRE_MOUSE_OVER));
      }
   }

   public static boolean beforeEntityAttack(EntityPlayer attacker, Entity target) {
      if (modules == null || attacker != modules.context().player()) return false;
      SemanticEvent event = new SemanticEvent(SemanticEvent.PRE_ENTITY_ATTACK);
      event.entity = target;
      modules.dispatch(event);
      return event.cancelled;
   }

   public static void afterRay(float partialTicks) {
      if (modules != null && modules.context().ready()) {
         SemanticEvent event = new SemanticEvent(SemanticEvent.POST_MOUSE_OVER);
         event.timerRenderPartialTicks = partialTicks;
         modules.dispatch(event);
      }
   }

   public static double entityReach(double vanilla) {
      return ray != null && ray.enabled ? ray.distance : vanilla;
   }

   public static float queryReach(float vanilla) {
      return ray != null && ray.enabled ? (float)Math.max((double)vanilla, ray.distance) : vanilla;
   }

   public static float hitboxBorder(float vanilla) {
      return ray == null ? vanilla : (float)((double)vanilla + ray.expand);
   }

   /** Menu input must never become an attack, use-item or held block-mining action. */
   public static boolean guiBlocksClicks() {
      if (modules == null) return false;
      Module gui = modules.find("gui");
      net.minecraft.client.gui.GuiScreen screen = modules.context().minecraft().currentScreen;
      return gui != null && gui.isEnabled() || screen instanceof dev.rice.client.render.RecoveredGuiScreen
         || screen instanceof dev.rice.client.GuiModuleList || screen instanceof dev.rice.client.GuiModuleSettings;
   }

   public static boolean beforeInteraction(boolean use) {
      if (guiBlocksClicks()) return true;
      if (modules == null) {
         return false;
      } else {
         if (!use) {
            combat.prepare(modules.context());
         }

         SemanticEvent event = combat.event(14, use);
         modules.dispatch(event);
         if (use) {
            PlacementSelection.capture(modules.context());
         }

         return event.cancelled;
      }
   }

   public static void afterInteraction(boolean use) {
      if (modules != null) {
         modules.dispatch(combat.event(15, use));
         if (use) {
            PlacementSelection.consume(modules.context());
         }
      }
   }

   public static void baseSwing(EntityPlayerSP player) {
      baseSwing = true;

      try {
         player.swingItem();
      } finally {
         baseSwing = false;
      }
   }

   public static boolean baseSwingOnly() {
      return baseSwing;
   }

   public static void clear() {
      dev.rice.client.stasis.StasisInputLock.clear();
      ray = null;
      combat.clear();
      previousDisplaySeconds = 0.0;
      try {
         RenderHooks.clear();
      } finally {
         try {
            PlacementSelection.reset();
         } finally {
            try {
               RotationService.reset(null);
            } finally {
               PacketPipeline.releaseAll(false);
            }
         }
      }
   }

   public static void shutdown() {
      if (modules != null) {
         modules.shutdown();
      }

      PacketPipeline.releaseAll(true);
   }

   public static boolean beforeSend(NetworkManager receiver, Packet<?> packet) {
      boolean direct = NetworkSendAdapter.consumeDirect(receiver, packet);
      if (modules != null) {
         // OpenB4 Hooks.onSend: PacketUtils.track runs for every outbound packet. AutoClutch's onPacketSent now
         // runs as a native module on event 23 (it records server yaw/pitch; it never cancels).
         OpenB4AntiFireballHost.observeSend(packet);
         OpenJadeHost.observePacket(packet, true);
         // OpenJade: NetworkManager.sendPacket head (Opsec's queued packet dispatcher)
         if (JadeRuntime.networkSend(packet)) {
            return true;
         }
      }

      if (!direct && modules != null) {
         SemanticEvent event = new SemanticEvent(23);
         event.receiver = receiver;
         event.packet = packet;
         modules.dispatch(event);
         if (event.cancelled) {
            dev.rice.client.mayu.MayuPlacement.rejected(packet);
            return true;
         }
      }

      // Mayu MixinNetworkManager.onSendPacket (PacketSendEvent, PacketActionTracker, outgoing buffers) for every packet
      // that is actually being sent.
      boolean cancelled = modules != null && MayuMixins.onSendPacket(packet);
      // Block In must remember mining that happened before its hold bind was pressed.
      // Observe only after all existing cancellation/buffering decisions have run.
      if (modules != null && !cancelled) {
         dev.rice.client.blockin.SuppliedBlockInHost.observeDig(packet);
      }
      return cancelled;
   }

   public static void updateController() {
      if (modules != null) {
         SemanticEvent event = new SemanticEvent(25);
         modules.dispatch(event);
         if (event.cancelled) {
            InvWalkControllerInputAdapter.drain();
         }
      }
   }

   public static void preRender() {
      if (modules != null) {
         RotationService.prepareMouse(modules.context());
         Module module = modules.find("No Hurt Cam");
         if (module != null && module.isEnabled()) {
            ((NoHurtCamModule)module).applyPreRender(modules.context());
         }
      }
   }
}
