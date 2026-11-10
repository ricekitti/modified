/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  net.minecraft.util.Vec3
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package valen.lag.handler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import valen.event.GameTickEvent;
import valen.event.ReceivePacketEvent;
import valen.event.SendPacketEvent;
import valen.lag.api.EnumLagDirection;
import valen.lag.api.LagRequest;
import valen.lag.handler.AbstractFastTrackProvider;
import valen.lag.queue.BiTrackLagNodeQueue;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;

public final class UnifiedLagHandler
extends AbstractFastTrackProvider {
    @NotNull
    private final BiTrackLagNodeQueue queue = new BiTrackLagNodeQueue(this);
    @NotNull
    private final Set<Packet<?>> packetFastTrack = Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap()));
    @Nullable
    private volatile Vec3 serverPosition;

    public void requestLag(@NotNull LagRequest lagRequest) {
        this.queue.requestLag(lagRequest);
    }

    public long timeUntilNextLag() {
        return this.queue.timeUntilNextLag();
    }

    public void releaseExpiredPackets(@NotNull EnumLagDirection enumLagDirection, long l) {
        this.queue.releaseExpiredPackets(enumLagDirection, l);
    }

    public void releaseTicks(@NotNull EnumLagDirection enumLagDirection, int n) {
        this.queue.releaseTicks(enumLagDirection, n);
    }

    @Nullable
    public Vec3 getLastReleasedServerPosition() {
        return this.serverPosition;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onSendPacket(@NotNull SendPacketEvent sendPacketEvent) {
        if (Minecraft.getMinecraft().getNetHandler() == null) {
            this.queue.clear();
            this.clearServerPositions();
            return;
        }
        @NotNull Packet<?> packet = sendPacketEvent.getPacket();
        boolean bl = this.consumeFastTrack(packet);
        if (sendPacketEvent.isCanceled()) {
            return;
        }
        if (bl) {
            this.updateServerPosition(packet);
            return;
        }
        if (this.queue.tick(packet, EnumLagDirection.OUTBOUND)) {
            sendPacketEvent.setCanceled(true);
            return;
        }
        this.updateServerPosition(packet);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent receivePacketEvent) {
        if (Minecraft.getMinecraft().getNetHandler() == null) {
            this.queue.clear();
            this.clearServerPositions();
            return;
        }
        @NotNull Packet<?> packet = receivePacketEvent.getPacket();
        boolean bl = this.consumeFastTrack(packet);
        if (receivePacketEvent.isCanceled()) {
            return;
        }
        if (bl) {
            return;
        }
        if (this.queue.tick(packet, EnumLagDirection.INBOUND)) {
            receivePacketEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGameTick(@NotNull GameTickEvent gameTickEvent) {
        if (Minecraft.getMinecraft().getNetHandler() == null) {
            this.queue.clear();
            this.clearServerPositions();
            return;
        }
        this.queue.tick(null, null);
    }

    @Override
    public void forPacket(@NotNull Packet<?> packet) {
        this.packetFastTrack.add(packet);
    }

    private boolean consumeFastTrack(@NotNull Packet<?> packet) {
        return this.packetFastTrack.remove(packet);
    }

    private void updateServerPosition(@NotNull Packet<?> packet) {
        if (!(packet instanceof C03PacketPlayer)) {
            return;
        }
        C03PacketPlayer c03PacketPlayer = (C03PacketPlayer)packet;
        if (!c03PacketPlayer.isMoving()) {
            return;
        }
        this.serverPosition = new Vec3(c03PacketPlayer.getPositionX(), c03PacketPlayer.getPositionY(), c03PacketPlayer.getPositionZ());
    }

    private void clearServerPositions() {
        this.serverPosition = null;
    }
}

