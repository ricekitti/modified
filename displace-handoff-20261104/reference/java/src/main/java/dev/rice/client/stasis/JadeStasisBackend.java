package dev.rice.client.stasis;

import dev.rice.client.SemanticEvent;
import dev.rice.setting.BooleanSetting;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

/** OpenJade Stasis backend, adapted from Tewxx/OpenJade commit af7f296a9b3e8a69fe562a072afec8d2d5870224.
 * Source: JadeClient/src/main/java/jade/client/module/movement/Stasis.java.
 * Rice adapters replace Jade's event bus, settings, direct sends and main-thread packet processing.
 */
public final class JadeStasisBackend {
   private static final int MAX_HOLD_TICKS = 280;
   private static final long MAX_HOLD_MS = 14000L;
   private static final double EPSILON = 1.0E-6;
   private final BooleanSetting hypixelMode;
   private final BooleanSetting shiftPlaceThroughWalls;
   private final StasisInputLock inputLock;
   private final Deque<Packet<?>> heldPackets = new ArrayDeque<>();
   private volatile boolean active;
   private boolean frozenOnGround;
   private volatile boolean resyncPosition;
   private volatile boolean knockbackPending;
   private volatile boolean holdingPackets;
   private volatile boolean packetsWereHeld;
   private volatile boolean releasingPackets;
   private int knockbackTicks;
   private int heldTicks;
   private int ticksSincePositionSync;
   private int lastPlaceTick;
   private long holdStartMs;
   private double frozenX;
   private double frozenY;
   private double frozenZ;
   private double savedMotionX;
   private double savedMotionY;
   private double savedMotionZ;
   private boolean motionSaved;

   private final dev.rice.client.ClientContext context;
   private final net.minecraft.client.Minecraft mc;

   public JadeStasisBackend(dev.rice.client.ClientContext context, BooleanSetting hypixelMode, BooleanSetting shiftPlaceThroughWalls) {
      this.context = context;
      this.mc = context.minecraft();
      this.hypixelMode = hypixelMode;
      this.shiftPlaceThroughWalls = shiftPlaceThroughWalls;
      this.inputLock = new StasisInputLock();
   }

   public void dispatch(SemanticEvent event) {
      if (event.type == 16) this.onPreUpdate(event);
      else if (event.type == 18) this.onPostUpdate(event);
      else if (event.type == SemanticEvent.MOVEMENT_INPUT) this.onMoveInput(event);
      else if (event.type == 14 && event.active) this.onRightClick(event);
      else if (event.type == 2) {
         this.onPacketReceive(event);
         if (event.dispatchMode != 2 && event.packet instanceof S12PacketEntityVelocity) this.onVelocity(event);
      } else if (event.type == 23) this.onPacketSend(event);
   }

   private boolean isEnabled() { return this.active; }
   public synchronized void onEnable() {
      StasisInputLock.reset(this.inputLock);
      if (!this.context.ready()) {
         this.active = false;
      } else {
         this.saveMotion();
         this.frozenOnGround = mc.thePlayer.onGround;
         this.captureFrozenPosition();
         this.resyncPosition = false;
         this.knockbackPending = false;
         this.holdingPackets = false;
         this.packetsWereHeld = false;
         this.releasingPackets = false;
         this.knockbackTicks = 0;
         this.heldTicks = 0;
         this.ticksSincePositionSync = 0;
         this.lastPlaceTick = -1;
         this.holdStartMs = 0L;
         this.active = true;
      }
   }

   public synchronized void onDisable() {
      boolean lockInputAfter = this.active && this.context.ready();
      boolean restoreMotionAfter = this.context.ready() && this.motionSaved && !this.packetsWereHeld;
      this.releaseHeldPackets();
      if (restoreMotionAfter) {
         this.restoreMotion();
      }

      this.active = false;
      this.frozenOnGround = false;
      this.resyncPosition = false;
      this.knockbackPending = false;
      this.holdingPackets = false;
      this.packetsWereHeld = false;
      this.releasingPackets = false;
      this.knockbackTicks = 0;
      this.heldTicks = 0;
      this.ticksSincePositionSync = 0;
      this.lastPlaceTick = -1;
      this.holdStartMs = 0L;
      this.motionSaved = false;
      if (lockInputAfter) {
         StasisInputLock.lockInput(this.inputLock);
      }
   }

   public synchronized void onLoadWorld(SemanticEvent event) {
      this.active = false;
      this.motionSaved = false;
      StasisInputLock.reset(this.inputLock);
      this.discardHeldPackets();
   }

   public void onPreUpdate(SemanticEvent event) {
      if (this.active && this.context.ready()) {
         if (!this.resyncPosition && !this.knockbackPending) {
            this.freezePlayer();
         }
      }
   }

   public void onPostUpdate(SemanticEvent event) {
      if (this.active && this.context.ready()) {
         if (this.resyncPosition) {
            this.captureFrozenPosition();
            this.resyncPosition = false;
         } else if (this.knockbackPending) {
            if (++this.knockbackTicks >= 2) {
               this.captureFrozenPosition();
               this.knockbackPending = false;
               this.knockbackTicks = 0;
               this.freezePlayer();
            }
         } else {
            this.freezePlayer();
         }
      }
   }

   public void onVelocity(SemanticEvent event) {
      if (this.active && this.context.ready()) {
         if (!this.hypixelMode.isEnabled() || this.releasingPackets) {
            if (((S12PacketEntityVelocity)event.packet).getEntityID() == mc.thePlayer.getEntityId()) {
               this.knockbackPending = true;
               this.knockbackTicks = 0;
            }
         }
      }
   }

   public void onMoveInput(SemanticEvent event) {
      if (this.active && this.context.ready() && mc.thePlayer.movementInput != null) {
         event.forward = 0.0F;
         event.sideways = 0.0F;
         mc.thePlayer.movementInput.jump = false;
         mc.thePlayer.movementInput.sneak = this.isShiftPlacing();
      }
   }

   public void onRightClick(SemanticEvent event) {
      if (this.isShiftPlacing()) {
         if (this.lastPlaceTick == mc.thePlayer.ticksExisted) {
            event.cancelled = true;
         } else {
            if (this.tryPlaceThroughWall()) {
               this.lastPlaceTick = mc.thePlayer.ticksExisted;
               event.cancelled = true;
            }
         }
      }
   }

   public synchronized void onPacketReceive(SemanticEvent event) {
      if (this.active && event.dispatchMode != 2) {
         Packet packet = event.packet;
         if (packet != null) {
            if (this.isWorldChangePacket(packet)) {
               this.discardHeldPackets();
            } else if (!this.context.ready()) {
               this.discardHeldPackets();
            } else if (packet instanceof S08PacketPlayerPosLook) {
               if (this.hypixelMode.isEnabled() && this.holdingPackets) {
                  this.holdPacket(packet);
                  event.dispatchMode = 2; event.cancelled = true;
               } else {
                  this.resyncPosition = true;
               }
            } else if (this.hypixelMode.isEnabled()) {
               if (!this.holdingPackets) {
                  if (this.isSelfKnockback(packet)) {
                     this.startHolding();
                     this.holdPacket(packet);
                     event.dispatchMode = 2; event.cancelled = true;
                  } else {
                     if (this.isExplosionKnockback(packet)) {
                        this.startHolding();
                        this.holdPacket(packet);
                        event.dispatchMode = 2; event.cancelled = true;
                     }
                  }
               } else if (!this.isConnectionPacket(packet)) {
                  if (this.isPassthroughPacket(packet)) {
                     this.processInboundPacket(packet);
                     event.dispatchMode = 2; event.cancelled = true;
                  } else {
                     this.holdPacket(packet);
                     event.dispatchMode = 2; event.cancelled = true;
                  }
               }
            }
         }
      }
   }

   public synchronized void onTickStart(SemanticEvent event) {
      if (this.active && !this.hypixelMode.isEnabled() && this.holdingPackets) {
         this.releaseHeldPackets();
      }
      if (this.active && this.hypixelMode.isEnabled() && this.holdingPackets) {
         if (!this.context.ready() || mc.thePlayer.isDead) {
            this.discardHeldPackets();
         } else if (this.heldPackets.isEmpty()) {
            this.discardHeldPackets();
         } else {
            this.heldTicks++;
            if (this.holdExpired()) {
               this.releaseHeldPackets();
            }
         }
      }
   }

   public void onPacketSend(SemanticEvent event) {
      if (this.active && this.context.ready()) {
         if (!this.resyncPosition && !this.knockbackPending) {
            if (event.packet instanceof C03PacketPlayer) {
               C03PacketPlayer packet = (C03PacketPlayer)event.packet;
               if (this.ticksSincePositionSync >= 20) {
                  event.cancelled = true;
                  this.sendFrozenPosition(packet);
                  this.ticksSincePositionSync = 0;
               } else if (!(packet instanceof C04PacketPlayerPosition) && !(packet instanceof C06PacketPlayerPosLook)) {
                  this.ticksSincePositionSync++;
               } else {
                  event.cancelled = true;
                  this.sendStationaryPacket(packet);
                  this.ticksSincePositionSync++;
               }
            }
         }
      }
   }

   private void freezePlayer() {
      if (!this.context.ready()) {
         this.active = false;
      } else {
         mc.thePlayer.motionX = 0.0;
         mc.thePlayer.motionY = 0.0;
         mc.thePlayer.motionZ = 0.0;
         mc.thePlayer.fallDistance = 0.0F;
         mc.thePlayer.setPosition(this.frozenX, this.frozenY, this.frozenZ);
         mc.thePlayer.onGround = this.frozenOnGround;
      }
   }

   private void captureFrozenPosition() {
      if (!this.context.ready()) {
         this.active = false;
      } else {
         this.frozenX = mc.thePlayer.posX;
         this.frozenY = mc.thePlayer.getEntityBoundingBox().minY;
         this.frozenZ = mc.thePlayer.posZ;
         this.frozenOnGround = mc.thePlayer.onGround;
      }
   }

   private void saveMotion() {
      this.savedMotionX = mc.thePlayer.motionX;
      this.savedMotionY = mc.thePlayer.motionY;
      this.savedMotionZ = mc.thePlayer.motionZ;
      this.motionSaved = true;
   }

   private void restoreMotion() {
      mc.thePlayer.motionX = this.savedMotionX;
      mc.thePlayer.motionY = this.savedMotionY;
      mc.thePlayer.motionZ = this.savedMotionZ;
   }

   private void sendFrozenPosition(C03PacketPlayer packet) {
      if (packet.getRotating()) {
         this.sendSilently(new C06PacketPlayerPosLook(this.frozenX, this.frozenY, this.frozenZ, packet.getYaw(), packet.getPitch(), this.frozenOnGround));
      } else {
         this.sendSilently(new C04PacketPlayerPosition(this.frozenX, this.frozenY, this.frozenZ, this.frozenOnGround));
      }
   }

   private void sendStationaryPacket(C03PacketPlayer packet) {
      if (packet.getRotating()) {
         this.sendSilently(new C05PacketPlayerLook(packet.getYaw(), packet.getPitch(), this.frozenOnGround));
      } else {
         this.sendSilently(new C03PacketPlayer(this.frozenOnGround));
      }
   }

   private boolean isShiftPlacing() {
      return this.active
         && this.hypixelMode.isEnabled()
         && this.shiftPlaceThroughWalls.isEnabled()
         && this.context.ready()
         && mc.currentScreen == null
         && mc.gameSettings.keyBindSneak.isKeyDown();
   }

   private boolean tryPlaceThroughWall() {
      ItemStack held = mc.thePlayer.getHeldItem();
      if (held != null && held.getItem() instanceof ItemBlock) {
         double reach = mc.playerController.getBlockReachDistance();
         Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
         Vec3 look = mc.thePlayer.getLook(1.0F);
         Vec3 rayEnd = eyes.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
         MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(eyes, rayEnd, false, false, false);
         if (hit != null && hit.typeOfHit == MovingObjectType.BLOCK && hit.getBlockPos() != null) {
            BlockPos hitPos = hit.getBlockPos();
            StasisPlaceTarget target = this.findRayExit(hitPos, hit.hitVec, look);
            if (target == null) {
               return false;
            } else {
               BlockPos placePos = hitPos.offset(StasisPlaceTarget.face(target));
               if (mc.theWorld.getBlockState(hitPos).getBlock().isReplaceable(mc.theWorld, hitPos) || !mc.theWorld.getBlockState(placePos).getBlock().isReplaceable(mc.theWorld, placePos)) {
                  return false;
               } else if (!((ItemBlock)held.getItem()).canPlaceBlockOnSide(mc.theWorld, hitPos, StasisPlaceTarget.face(target), mc.thePlayer, held)) {
                  return false;
               } else if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, held, hitPos, StasisPlaceTarget.face(target), StasisPlaceTarget.hitVec(target))) {
                  mc.thePlayer.swingItem();
                  return true;
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private StasisPlaceTarget findRayExit(BlockPos pos, Vec3 hitVec, Vec3 look) {
      if (hitVec != null && look != null) {
         double nearest = Double.MAX_VALUE;
         EnumFacing exitFace = null;
         double localX = hitVec.xCoord - pos.getX();
         double localY = hitVec.yCoord - pos.getY();
         double localZ = hitVec.zCoord - pos.getZ();
         if (look.xCoord > EPSILON) {
            double distX = (1.0 - localX) / look.xCoord;
            if (distX > EPSILON && distX < nearest) {
               nearest = distX;
               exitFace = EnumFacing.EAST;
            }
         } else if (look.xCoord < -EPSILON) {
            double distX = (0.0 - localX) / look.xCoord;
            if (distX > EPSILON && distX < nearest) {
               nearest = distX;
               exitFace = EnumFacing.WEST;
            }
         }

         if (look.yCoord > EPSILON) {
            double distY = (1.0 - localY) / look.yCoord;
            if (distY > EPSILON && distY < nearest) {
               nearest = distY;
               exitFace = EnumFacing.UP;
            }
         } else if (look.yCoord < -EPSILON) {
            double distY = (0.0 - localY) / look.yCoord;
            if (distY > EPSILON && distY < nearest) {
               nearest = distY;
               exitFace = EnumFacing.DOWN;
            }
         }

         if (look.zCoord > EPSILON) {
            double distZ = (1.0 - localZ) / look.zCoord;
            if (distZ > EPSILON && distZ < nearest) {
               nearest = distZ;
               exitFace = EnumFacing.SOUTH;
            }
         } else if (look.zCoord < -EPSILON) {
            double distZ = (0.0 - localZ) / look.zCoord;
            if (distZ > EPSILON && distZ < nearest) {
               nearest = distZ;
               exitFace = EnumFacing.NORTH;
            }
         }

         return exitFace == null ? null : new StasisPlaceTarget(exitFace, hitVec.addVector(look.xCoord * nearest, look.yCoord * nearest, look.zCoord * nearest));
      } else {
         return null;
      }
   }

   private void holdPacket(Packet<?> packet) {
      synchronized (this.heldPackets) {
         this.heldPackets.addLast(packet);
      }
   }

   private void startHolding() {
      this.holdingPackets = true;
      this.packetsWereHeld = true;
      this.heldTicks = 0;
      this.holdStartMs = System.currentTimeMillis();
   }

   private void releaseHeldPackets() {
      ArrayList packets = new ArrayList();
      synchronized (this.heldPackets) {
         while (!this.heldPackets.isEmpty()) {
            packets.add(this.heldPackets.removeFirst());
         }
      }

      this.heldTicks = 0;
      this.holdStartMs = 0L;
      this.releasingPackets = true;

      try {
         for (Packet packet : (java.lang.Iterable<Packet>) (java.lang.Iterable<?>) (packets)) {
            this.processInboundPacket(packet);
         }
      } finally {
         this.releasingPackets = false;
      }

      this.holdingPackets = false;
   }

   private void discardHeldPackets() {
      synchronized (this.heldPackets) {
         this.heldPackets.clear();
      }

      this.holdingPackets = false;
      this.heldTicks = 0;
      this.holdStartMs = 0L;
   }

   private boolean holdExpired() {
      return this.heldTicks >= MAX_HOLD_TICKS || this.holdStartMs > 0L && System.currentTimeMillis() - this.holdStartMs >= MAX_HOLD_MS;
   }

   public boolean isProgressActive() {
      return this.isEnabled() && this.hypixelMode.isEnabled() && this.holdingPackets;
   }

   public boolean isHypixelModeActive() {
      return this.isEnabled() && this.hypixelMode.isEnabled();
   }

   public boolean isHoldingPackets() {
      return this.isEnabled() && this.hypixelMode.isEnabled() && this.holdingPackets;
   }

   public float getProgressFraction() {
      return Math.max(0.0F, Math.min(1.0F, this.heldTicks / (float)MAX_HOLD_TICKS));
   }

   public String getProgressLabel() {
      return "Stasis";
   }

   private boolean isWorldChangePacket(Packet<?> packet) {
      return packet instanceof S01PacketJoinGame || packet instanceof S07PacketRespawn || packet instanceof S40PacketDisconnect;
   }

   private boolean isConnectionPacket(Packet<?> packet) {
      return packet instanceof S00PacketKeepAlive || packet instanceof S48PacketResourcePackSend;
   }

   private boolean isPassthroughPacket(Packet<?> packet) {
      return packet instanceof S02PacketChat
         || packet instanceof S45PacketTitle
         || this.isWorldUpdatePacket(packet)
         || this.isPlayerStatusPacket(packet)
         || this.isEntityPacket(packet)
         || this.isScoreboardPacket(packet)
         || this.isSoundPacket(packet);
   }

   private boolean isWorldUpdatePacket(Packet<?> packet) {
      return packet instanceof S21PacketChunkData
         || packet instanceof S22PacketMultiBlockChange
         || packet instanceof S23PacketBlockChange
         || packet instanceof S24PacketBlockAction
         || packet instanceof S25PacketBlockBreakAnim
         || packet instanceof S26PacketMapChunkBulk
         || packet instanceof S28PacketEffect
         || packet instanceof S35PacketUpdateTileEntity
         || packet instanceof S33PacketUpdateSign;
   }

   private boolean isEntityPacket(Packet<?> packet) {
      return packet instanceof S0CPacketSpawnPlayer
         || packet instanceof S0FPacketSpawnMob
         || packet instanceof S0EPacketSpawnObject
         || packet instanceof S10PacketSpawnPainting
         || packet instanceof S11PacketSpawnExperienceOrb
         || packet instanceof S2CPacketSpawnGlobalEntity
         || this.isOtherEntityVelocity(packet)
         || packet instanceof S13PacketDestroyEntities
         || packet instanceof S14PacketEntity
         || packet instanceof S18PacketEntityTeleport
         || packet instanceof S19PacketEntityHeadLook
         || packet instanceof S0BPacketAnimation
         || packet instanceof S0APacketUseBed
         || packet instanceof S1BPacketEntityAttach
         || packet instanceof S1CPacketEntityMetadata
         || packet instanceof S04PacketEntityEquipment
         || packet instanceof S1DPacketEntityEffect
         || packet instanceof S1EPacketRemoveEntityEffect
         || packet instanceof S20PacketEntityProperties
         || packet instanceof S19PacketEntityStatus
         || packet instanceof S0DPacketCollectItem;
   }

   private boolean isOtherEntityVelocity(Packet<?> packet) {
      if (!(packet instanceof S12PacketEntityVelocity)) {
         return false;
      } else {
         S12PacketEntityVelocity velocity = (S12PacketEntityVelocity)packet;
         return mc.thePlayer == null || velocity.getEntityID() != mc.thePlayer.getEntityId();
      }
   }

   private boolean isPlayerStatusPacket(Packet<?> packet) {
      return packet instanceof S06PacketUpdateHealth
         || packet instanceof S1CPacketEntityMetadata
         || packet instanceof S1DPacketEntityEffect
         || packet instanceof S1EPacketRemoveEntityEffect
         || packet instanceof S20PacketEntityProperties
         || packet instanceof S19PacketEntityStatus;
   }

   private boolean isScoreboardPacket(Packet<?> packet) {
      return packet instanceof S3BPacketScoreboardObjective
         || packet instanceof S3CPacketUpdateScore
         || packet instanceof S3DPacketDisplayScoreboard
         || packet instanceof S3EPacketTeams
         || packet instanceof S38PacketPlayerListItem
         || packet instanceof S47PacketPlayerListHeaderFooter;
   }

   private boolean isSoundPacket(Packet<?> packet) {
      return packet instanceof S29PacketSoundEffect || packet instanceof S28PacketEffect;
   }

   private boolean isSelfKnockback(Packet<?> packet) {
      if (packet instanceof S12PacketEntityVelocity && mc.thePlayer != null) {
         S12PacketEntityVelocity velocity = (S12PacketEntityVelocity)packet;
         return velocity.getEntityID() == mc.thePlayer.getEntityId() && (velocity.getMotionX() != 0 || velocity.getMotionY() != 0 || velocity.getMotionZ() != 0);
      } else {
         return false;
      }
   }

   private boolean isExplosionKnockback(Packet<?> packet) {
      if (!(packet instanceof S27PacketExplosion)) {
         return false;
      } else {
         S27PacketExplosion explosion = (S27PacketExplosion)packet;
         double motionX = explosion.func_149149_c();
         double motionY = explosion.func_149144_d();
         double motionZ = explosion.func_149147_e();
         return motionX * motionX + motionY * motionY + motionZ * motionZ > EPSILON;
      }
   }
   private void sendSilently(Packet<?> packet) {
      if (mc.getNetHandler() != null) {
         dev.rice.client.packet.NetworkSendAdapter.sendDirect(mc.getNetHandler().getNetworkManager(), packet);
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private void processInboundPacket(Packet packet) {
      final net.minecraft.client.network.NetHandlerPlayClient handler = mc.getNetHandler();
      final net.minecraft.client.multiplayer.WorldClient world = mc.theWorld;
      if (handler == null || world == null) return;
      Runnable process = () -> {
         if (mc.getNetHandler() != handler || mc.theWorld != world) return;
         if (packet instanceof S12PacketEntityVelocity) {
            SemanticEvent velocity = new SemanticEvent(2); velocity.packet = packet;
            this.onVelocity(velocity);
         } else if (packet instanceof S08PacketPlayerPosLook) {
            this.resyncPosition = true;
         }
         try {
            packet.processPacket(handler);
         } catch (net.minecraft.network.ThreadQuickExitException scheduled) {
            // Vanilla has already scheduled this packet for its main thread.
         } catch (Exception failure) {
            org.apache.logging.log4j.LogManager.getLogger("Rice Stasis").error("Could not replay held packet", failure);
         }
      };
      if (mc.isCallingFromMinecraftThread()) process.run();
      else mc.addScheduledTask(process);
   }
}
