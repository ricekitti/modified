/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  valen.lag.queue.node.api.AbstractLagNode
 *  valen.lag.queue.node.impl.AddRequestLagNode
 *  valen.lag.queue.node.impl.PacketLagNode
 */
package valen.lag.queue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import valen.event.LagStartEvent;
import valen.lag.api.EnumLagDirection;
import valen.lag.api.LagRequest;
import valen.lag.handler.AbstractFastTrackProvider;
import valen.lag.queue.node.api.AbstractLagNode;
import valen.lag.queue.node.impl.AddRequestLagNode;
import valen.lag.queue.node.impl.PacketLagNode;
import valen.lunar.forge.common.MinecraftForge;

public final class BiTrackLagNodeQueue {
    @NotNull
    private final TrackState incomingState;
    @NotNull
    private final TrackState outgoingState;
    @NotNull
    private final List<LagRequest> pendingRequests = new ArrayList<LagRequest>();

    public BiTrackLagNodeQueue(@NotNull AbstractFastTrackProvider abstractFastTrackProvider) {
        this.incomingState = new TrackState(new ArrayDeque(), abstractFastTrackProvider);
        this.outgoingState = new TrackState(new ArrayDeque(), abstractFastTrackProvider);
    }

    public synchronized void clear() {
        this.pendingRequests.clear();
        this.incomingState.clear();
        this.outgoingState.clear();
    }

    public synchronized boolean tick(@Nullable Packet<?> packet, @Nullable EnumLagDirection enumLagDirection) {
        if (packet == null != (enumLagDirection == null)) {
            throw new NullPointerException();
        }
        if (enumLagDirection == null) {
            this.processPendingRequests();
            this.incomingState.tick(null, null);
            this.outgoingState.tick(null, null);
            return false;
        }
        switch (enumLagDirection) {
            case INBOUND: {
                return this.incomingState.tick(packet, enumLagDirection);
            }
            case OUTBOUND: {
                return this.outgoingState.tick(packet, enumLagDirection);
            }
        }
        return false;
    }

    public synchronized void requestLag(@NotNull LagRequest lagRequest) {
        if (lagRequest.isPending()) {
            this.pendingRequests.add(lagRequest);
        } else {
            this.addRequestToStates(lagRequest);
        }
    }

    private void addRequestToStates(@NotNull LagRequest lagRequest) {
        for (EnumLagDirection enumLagDirection : lagRequest.getDirections()) {
            switch (enumLagDirection) {
                case OUTBOUND: {
                    this.outgoingState.addRequest(lagRequest);
                    break;
                }
                case INBOUND: {
                    this.incomingState.addRequest(lagRequest);
                }
            }
        }
    }

    private void processPendingRequests() {
        long l = System.currentTimeMillis();
        ArrayList<LagRequest> arrayList = new ArrayList<LagRequest>();
        ArrayList<LagRequest> arrayList2 = new ArrayList<LagRequest>();
        for (LagRequest lagRequest : this.pendingRequests) {
            if (lagRequest.getTimeout().isTimedOut()) {
                arrayList2.add(lagRequest);
                continue;
            }
            if (l >= lagRequest.getScheduledStartTimeMs()) {
                arrayList.add(lagRequest);
                continue;
            }
            MinecraftForge.EVENT_BUS.post(new LagStartEvent(lagRequest, true));
        }
        this.pendingRequests.removeAll(arrayList2);
        this.pendingRequests.removeAll(arrayList);
        for (LagRequest lagRequest : arrayList) {
            this.addRequestToStates(lagRequest);
            MinecraftForge.EVENT_BUS.post(new LagStartEvent(lagRequest, false));
        }
    }

    public synchronized long timeUntilNextLag() {
        long l = System.currentTimeMillis();
        long l2 = -1L;
        for (LagRequest lagRequest : this.pendingRequests) {
            long l3;
            if (lagRequest.getTimeout().isTimedOut() || (l3 = lagRequest.getScheduledStartTimeMs() - l) <= 0L || l2 != -1L && l3 >= l2) continue;
            l2 = l3;
        }
        return l2;
    }

    public synchronized void releaseExpiredPackets(@NotNull EnumLagDirection enumLagDirection, long l) {
        switch (enumLagDirection) {
            case OUTBOUND: {
                this.outgoingState.releaseExpiredPackets(l);
                break;
            }
            case INBOUND: {
                this.incomingState.releaseExpiredPackets(l);
            }
        }
    }

    public synchronized void releaseTicks(@NotNull EnumLagDirection enumLagDirection, int n) {
        switch (enumLagDirection) {
            case OUTBOUND: {
                this.outgoingState.releaseTicks(n);
                break;
            }
            case INBOUND: {
                this.incomingState.releaseTicks(n);
            }
        }
    }

    private static final class TrackState {
        @NotNull
        private final Deque<AbstractLagNode> track;
        @NotNull
        private final AbstractFastTrackProvider fastTrackProvider;
        @Nullable
        private LagRequest currentlyAwaiting = null;

        private TrackState(@NotNull Deque<AbstractLagNode> deque, @NotNull AbstractFastTrackProvider abstractFastTrackProvider) {
            this.track = deque;
            this.fastTrackProvider = abstractFastTrackProvider;
        }

        private synchronized void addRequest(@NotNull LagRequest lagRequest) {
            this.track.add((AbstractLagNode)new AddRequestLagNode(lagRequest));
        }

        private synchronized boolean tick(@Nullable Packet<?> packet, @Nullable EnumLagDirection enumLagDirection) {
            if (this.track.isEmpty() && (this.currentlyAwaiting == null || this.currentlyAwaiting.getTimeout().isTimedOut())) {
                this.currentlyAwaiting = null;
                return false;
            }
            if (packet != null) {
                this.track.add((AbstractLagNode)new PacketLagNode(packet, enumLagDirection));
            }
            @Nullable LagRequest lagRequest = this.currentlyAwaiting;
            try {
                while (lagRequest == null || lagRequest.getTimeout().isTimedOut()) {
                    @Nullable AbstractLagNode abstractLagNode = this.pop();
                    if (abstractLagNode == null) {
                        lagRequest = null;
                        break;
                    }
                    if (abstractLagNode instanceof PacketLagNode) {
                        ((PacketLagNode)abstractLagNode).goThrough(this.fastTrackProvider);
                        continue;
                    }
                    if (!(abstractLagNode instanceof AddRequestLagNode)) continue;
                    lagRequest = ((AddRequestLagNode)abstractLagNode).getRequest();
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            this.currentlyAwaiting = lagRequest;
            return true;
        }

        private synchronized void releaseExpiredPackets(long l) {
            long l2 = System.currentTimeMillis() - l;
            ArrayList<PacketLagNode> arrayList = new ArrayList<PacketLagNode>();
            for (AbstractLagNode abstractLagNode : new ArrayList<AbstractLagNode>(this.track)) {
                PacketLagNode packetLagNode;
                if (!(abstractLagNode instanceof PacketLagNode) || (packetLagNode = (PacketLagNode)abstractLagNode).getQueuedAtMs() > l2) continue;
                arrayList.add(packetLagNode);
            }
            if (arrayList.isEmpty()) {
                return;
            }
            this.track.removeAll(arrayList);
            for (PacketLagNode packetLagNode : arrayList) {
                packetLagNode.goThrough(this.fastTrackProvider);
            }
        }

        private synchronized void releaseTicks(int n) {
            AbstractLagNode abstractLagNode;
            int n2 = 0;
            while (n2 < n && !this.track.isEmpty() && (abstractLagNode = this.pop()) != null) {
                if (abstractLagNode instanceof PacketLagNode) {
                    PacketLagNode packetLagNode = (PacketLagNode)abstractLagNode;
                    packetLagNode.goThrough(this.fastTrackProvider);
                    if (!(packetLagNode.getPacket() instanceof C03PacketPlayer)) continue;
                    ++n2;
                    continue;
                }
                if (!(abstractLagNode instanceof AddRequestLagNode)) continue;
                this.currentlyAwaiting = ((AddRequestLagNode)abstractLagNode).getRequest();
            }
        }

        private synchronized void clear() {
            this.track.clear();
            this.currentlyAwaiting = null;
        }

        @Nullable
        private AbstractLagNode pop() {
            return this.track.isEmpty() ? null : this.track.pollFirst();
        }
    }
}

