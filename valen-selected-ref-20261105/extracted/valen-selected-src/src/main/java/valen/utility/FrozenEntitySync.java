/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.server.S04PacketEntityEquipment
 *  net.minecraft.network.play.server.S0APacketUseBed
 *  net.minecraft.network.play.server.S0BPacketAnimation
 *  net.minecraft.network.play.server.S0CPacketSpawnPlayer
 *  net.minecraft.network.play.server.S0DPacketCollectItem
 *  net.minecraft.network.play.server.S0EPacketSpawnObject
 *  net.minecraft.network.play.server.S0FPacketSpawnMob
 *  net.minecraft.network.play.server.S10PacketSpawnPainting
 *  net.minecraft.network.play.server.S11PacketSpawnExperienceOrb
 *  net.minecraft.network.play.server.S12PacketEntityVelocity
 *  net.minecraft.network.play.server.S13PacketDestroyEntities
 *  net.minecraft.network.play.server.S14PacketEntity
 *  net.minecraft.network.play.server.S18PacketEntityTeleport
 *  net.minecraft.network.play.server.S19PacketEntityHeadLook
 *  net.minecraft.network.play.server.S19PacketEntityStatus
 *  net.minecraft.network.play.server.S1BPacketEntityAttach
 *  net.minecraft.network.play.server.S1CPacketEntityMetadata
 *  net.minecraft.network.play.server.S1DPacketEntityEffect
 *  net.minecraft.network.play.server.S1EPacketRemoveEntityEffect
 *  net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
 *  net.minecraft.util.Timer
 */
package valen.utility;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S11PacketSpawnExperienceOrb;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.util.Timer;
import valen.mixin.impl.accessor.IAccessorMinecraft;
import valen.utility.PacketUtils;

public class FrozenEntitySync {
    private static final FrozenEntitySync INSTANCE = new FrozenEntitySync();
    private final Queue<Packet<?>> liveEntityQueue = new ConcurrentLinkedQueue();
    private final Queue<Packet<?>> deferredQueue = new ConcurrentLinkedQueue();
    private boolean active;
    private long lastFrozenTickNano;
    private double frozenTickAccumulator;
    private static final long TICK_NANOS = 50000000L;
    private static final int MAX_STEPS_PER_FRAME = 10;

    public static FrozenEntitySync get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return this.active;
    }

    public void start() {
        if (this.active) {
            return;
        }
        this.active = true;
        this.lastFrozenTickNano = System.nanoTime();
        this.frozenTickAccumulator = 0.0;
    }

    public void stop() {
        if (!this.active) {
            return;
        }
        this.active = false;
        this.flush();
    }

    public void clearAll() {
        this.active = false;
        this.liveEntityQueue.clear();
        this.deferredQueue.clear();
        this.frozenTickAccumulator = 0.0;
    }

    public boolean intercept(Packet<?> packet) {
        if (!this.active) {
            return false;
        }
        if (FrozenEntitySync.isEntityPacket(packet)) {
            this.liveEntityQueue.add(packet);
        } else {
            this.deferredQueue.add(packet);
        }
        return true;
    }

    public void pumpFrame() {
        int n;
        if (!this.active) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null || minecraft.thePlayer == null) {
            return;
        }
        this.drainLiveQueue();
        long l = System.nanoTime();
        long l2 = l - this.lastFrozenTickNano;
        this.lastFrozenTickNano = l;
        this.frozenTickAccumulator += (double)l2;
        for (n = 0; this.frozenTickAccumulator >= 5.0E7 && n < 10; ++n) {
            this.frozenTickAccumulator -= 5.0E7;
            this.tickNonLocalEntities(minecraft);
        }
        if (n >= 10) {
            this.frozenTickAccumulator = 0.0;
        }
        float f = (float)(this.frozenTickAccumulator / 5.0E7);
        Timer timer = ((IAccessorMinecraft)minecraft).valen$lunar$getTimer();
        timer.renderPartialTicks = f;
        this.snapLocalPlayerInterpolation(minecraft.thePlayer);
    }

    private void snapLocalPlayerInterpolation(EntityPlayerSP entityPlayerSP) {
        entityPlayerSP.lastTickPosX = entityPlayerSP.posX;
        entityPlayerSP.lastTickPosY = entityPlayerSP.posY;
        entityPlayerSP.lastTickPosZ = entityPlayerSP.posZ;
        entityPlayerSP.prevRotationYaw = entityPlayerSP.rotationYaw;
        entityPlayerSP.prevRotationPitch = entityPlayerSP.rotationPitch;
        entityPlayerSP.prevRotationYawHead = entityPlayerSP.rotationYawHead;
        entityPlayerSP.prevRenderYawOffset = entityPlayerSP.renderYawOffset;
        entityPlayerSP.prevLimbSwingAmount = entityPlayerSP.limbSwingAmount;
        entityPlayerSP.prevSwingProgress = entityPlayerSP.swingProgress;
        entityPlayerSP.prevCameraPitch = entityPlayerSP.cameraPitch;
    }

    public void flush() {
        Packet<?> packet;
        this.drainLiveQueue();
        while ((packet = this.deferredQueue.poll()) != null) {
            PacketUtils.receivePacketNoEvent(packet);
        }
        this.frozenTickAccumulator = 0.0;
    }

    private void drainLiveQueue() {
        Packet<?> packet;
        while ((packet = this.liveEntityQueue.poll()) != null) {
            PacketUtils.receivePacketNoEvent(packet);
        }
    }

    private void tickNonLocalEntities(Minecraft minecraft) {
        EntityPlayerSP entityPlayerSP = minecraft.thePlayer;
        for (int i = 0; i < minecraft.theWorld.weatherEffects.size(); ++i) {
            Entity entity = (Entity)minecraft.theWorld.weatherEffects.get(i);
            try {
                ++entity.ticksExisted;
                entity.onUpdate();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (!entity.isDead) continue;
            minecraft.theWorld.weatherEffects.remove(i--);
        }
        for (Entity entity : minecraft.theWorld.loadedEntityList) {
            if (entity == null || entity.isDead || entity == entityPlayerSP) continue;
            if (entity.ridingEntity != null) {
                if (!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity) continue;
                entity.ridingEntity.riddenByEntity = null;
                entity.ridingEntity = null;
            }
            try {
                minecraft.theWorld.updateEntityWithOptionalForce(entity, true);
            }
            catch (Throwable throwable) {}
        }
    }

    private static boolean isEntityPacket(Packet<?> packet) {
        return packet instanceof S0CPacketSpawnPlayer || packet instanceof S0FPacketSpawnMob || packet instanceof S0EPacketSpawnObject || packet instanceof S10PacketSpawnPainting || packet instanceof S11PacketSpawnExperienceOrb || packet instanceof S2CPacketSpawnGlobalEntity || packet instanceof S12PacketEntityVelocity || packet instanceof S13PacketDestroyEntities || packet instanceof S14PacketEntity || packet instanceof S18PacketEntityTeleport || packet instanceof S19PacketEntityHeadLook || packet instanceof S0BPacketAnimation || packet instanceof S0APacketUseBed || packet instanceof S1BPacketEntityAttach || packet instanceof S1CPacketEntityMetadata || packet instanceof S04PacketEntityEquipment || packet instanceof S1DPacketEntityEffect || packet instanceof S1EPacketRemoveEntityEffect || packet instanceof S19PacketEntityStatus || packet instanceof S0DPacketCollectItem;
    }
}

