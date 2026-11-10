/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.ChannelPromise
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.network.NetHandlerPlayClient
 *  net.minecraft.network.play.client.C00PacketKeepAlive
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  net.minecraft.network.play.client.C03PacketPlayer$C05PacketPlayerLook
 *  net.minecraft.network.play.client.C03PacketPlayer$C06PacketPlayerPosLook
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.client.C0BPacketEntityAction
 *  net.minecraft.network.play.client.C0BPacketEntityAction$Action
 *  net.minecraft.network.play.client.C0FPacketConfirmTransaction
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.snowify.displace;

import com.snowify.antipearl.core.PacketRotation;
import com.snowify.util.GameAccess;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WireRotation {
    private static final Logger LOGGER = LogManager.getLogger((String)"Snowify");
    private static final String NAME = "snowify_wire_rotation";
    private static boolean pendingForce;
    private static float forceYaw;
    private static float forcePitch;
    private static boolean pendingRestore;
    private static float restoreYaw;
    private static float restorePitch;
    private static boolean wireKnown;
    private static float wireYaw;
    private static float wirePitch;
    private static volatile Float lastForcedYaw;
    private static final Handler HANDLER;
    private static volatile long lastAttackOnWire;
    private static final AtomicInteger placements;
    private static final AtomicInteger turnedAfterPlacement;
    private static boolean placementPending;
    private static volatile float placementYaw;
    private static volatile float placementPitch;
    private static volatile boolean placementLookKnown;
    private static boolean pendingKnown;
    private static float pendingYaw;
    private static float pendingPitch;
    private static final AtomicInteger serverMisses;
    private static boolean wirePosKnown;
    private static double wireX;
    private static double wireY;
    private static double wireZ;
    private static boolean wireSneaking;
    private static BlockPos pendingBlock;
    private static int pendingFace;
    private static double clickX;
    private static double clickY;
    private static double clickZ;
    private static boolean clickSneaking;
    private static volatile boolean wireSprinting;
    private static volatile boolean lastAttackSprinting;
    private static final AtomicInteger transactionReplies;
    private static final AtomicInteger keepAliveReplies;

    private WireRotation() {
    }

    public static void notePlacementLook(float yaw, float pitch) {
        placementYaw = yaw;
        placementPitch = pitch;
        placementLookKnown = true;
    }

    public static int serverMisses() {
        return serverMisses.get();
    }

    public static int placements() {
        return placements.get();
    }

    public static int turnedAfterPlacement() {
        return turnedAfterPlacement.get();
    }

    public static long lastAttackOnWire() {
        return lastAttackOnWire;
    }

    public static boolean lastAttackSprinting() {
        return lastAttackSprinting;
    }

    public static boolean serverSprinting() {
        return wireSprinting;
    }

    public static Float lastForcedYaw() {
        return lastForcedYaw;
    }

    public static int transactionReplies() {
        return transactionReplies.get();
    }

    public static int keepAliveReplies() {
        return keepAliveReplies.get();
    }

    public static boolean forceNextMovementYaw(float yaw, float backYaw, float backPitch) {
        return WireRotation.forceNextMovementLook(yaw, Float.NaN, backYaw, backPitch);
    }

    public static boolean forceNextMovementLook(final float yaw, final float pitch, final float backYaw, final float backPitch) {
        lastForcedYaw = null;
        Channel channel = WireRotation.channel();
        if (channel == null) {
            return false;
        }
        channel.eventLoop().execute(new Runnable(){

            @Override
            public void run() {
                forceYaw = yaw;
                forcePitch = pitch;
                restoreYaw = backYaw;
                restorePitch = backPitch;
                pendingForce = true;
            }
        });
        return true;
    }

    private static Channel channel() {
        NetHandlerPlayClient net = Minecraft.getMinecraft().getNetHandler();
        if (net == null || net.getNetworkManager() == null) {
            return null;
        }
        Channel channel = GameAccess.channel(net.getNetworkManager());
        return channel != null && channel.isOpen() ? channel : null;
    }

    public static void ensurePlaced() {
        Channel channel = WireRotation.channel();
        if (channel == null) {
            return;
        }
        try {
            int wanted;
            ChannelPipeline pipeline = channel.pipeline();
            List names = pipeline.names();
            int ours = names.indexOf(NAME);
            int encoder = names.indexOf("encoder");
            int n = wanted = encoder >= 0 ? encoder + 1 : 0;
            if (ours == wanted) {
                return;
            }
            if (ours >= 0) {
                pipeline.remove(NAME);
            }
            if (encoder >= 0) {
                pipeline.addAfter("encoder", NAME, (ChannelHandler)HANDLER);
            } else {
                pipeline.addFirst(NAME, (ChannelHandler)HANDLER);
            }
        }
        catch (Throwable failure) {
            LOGGER.debug("Could not place the wire rotation handler", failure);
        }
    }

    static {
        forcePitch = Float.NaN;
        HANDLER = new Handler();
        placements = new AtomicInteger();
        turnedAfterPlacement = new AtomicInteger();
        serverMisses = new AtomicInteger();
        transactionReplies = new AtomicInteger();
        keepAliveReplies = new AtomicInteger();
    }

    @ChannelHandler.Sharable
    private static final class Handler
    extends ChannelOutboundHandlerAdapter {
        private Handler() {
        }

        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof C0BPacketEntityAction) {
                C0BPacketEntityAction.Action action = ((C0BPacketEntityAction)msg).getAction();
                if (action == C0BPacketEntityAction.Action.START_SNEAKING) {
                    wireSneaking = true;
                } else if (action == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                    wireSneaking = false;
                }
                if (action == C0BPacketEntityAction.Action.START_SPRINTING) {
                    wireSprinting = true;
                } else if (action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                    wireSprinting = false;
                }
            }
            if (msg instanceof C0FPacketConfirmTransaction) {
                transactionReplies.incrementAndGet();
            } else if (msg instanceof C00PacketKeepAlive) {
                keepAliveReplies.incrementAndGet();
            }
            if (msg instanceof C02PacketUseEntity && ((C02PacketUseEntity)msg).getAction() == C02PacketUseEntity.Action.ATTACK) {
                lastAttackSprinting = wireSprinting;
                lastAttackOnWire = System.nanoTime();
            }
            if (msg instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement)msg).getPlacedBlockDirection() != 255) {
                placements.incrementAndGet();
                placementPending = true;
                C08PacketPlayerBlockPlacement place = (C08PacketPlayerBlockPlacement)msg;
                pendingBlock = wirePosKnown ? place.getPosition() : null;
                pendingFace = place.getPlacedBlockDirection();
                clickX = wireX;
                clickY = wireY;
                clickZ = wireZ;
                clickSneaking = wireSneaking;
                pendingKnown = placementLookKnown;
                pendingYaw = placementYaw;
                pendingPitch = placementPitch;
                placementLookKnown = false;
            }
            if (msg instanceof C03PacketPlayer) {
                try {
                    msg = this.rewrite((C03PacketPlayer)msg);
                }
                catch (Throwable failure) {
                    LOGGER.debug("Wire rotation failed", failure);
                }
                if (placementPending && pendingBlock != null && wireKnown) {
                    double eyeY = clickY + (clickSneaking ? 1.54 : 1.62);
                    float yaw = wireYaw;
                    float pitch = wirePitch;
                    float cosYaw = MathHelper.cos((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
                    float sinYaw = MathHelper.sin((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
                    float cosPitch = -MathHelper.cos((float)(-pitch * ((float)Math.PI / 180)));
                    float sinPitch = MathHelper.sin((float)(-pitch * ((float)Math.PI / 180)));
                    Vec3 eye = new Vec3(clickX, eyeY, clickZ);
                    Vec3 end = eye.addVector((double)(sinYaw * cosPitch) * 6.0, (double)sinPitch * 6.0, (double)(cosYaw * cosPitch) * 6.0);
                    MovingObjectPosition hit = new AxisAlignedBB(pendingBlock, pendingBlock.add(1, 1, 1)).calculateIntercept(eye, end);
                    if (hit == null || hit.sideHit == null || hit.sideHit.getIndex() != pendingFace) {
                        serverMisses.incrementAndGet();
                    }
                }
                if (msg instanceof C03PacketPlayer && ((C03PacketPlayer)msg).isMoving()) {
                    C03PacketPlayer moved = (C03PacketPlayer)msg;
                    wirePosKnown = true;
                    wireX = moved.getPositionX();
                    wireY = moved.getPositionY();
                    wireZ = moved.getPositionZ();
                }
                pendingBlock = null;
                if (placementPending) {
                    placementPending = false;
                    if (pendingKnown && wireKnown && (wireYaw != pendingYaw || wirePitch != pendingPitch)) {
                        turnedAfterPlacement.incrementAndGet();
                    }
                }
            }
            super.write(ctx, msg, promise);
        }

        private C03PacketPlayer rewrite(C03PacketPlayer packet) {
            C03PacketPlayer out = packet;
            if (pendingForce) {
                pendingForce = false;
                boolean hadLook = packet.getRotating();
                float pitch = !Float.isNaN(forcePitch) ? forcePitch : (hadLook ? packet.getPitch() : (wireKnown ? wirePitch : packet.getPitch()));
                pendingRestore = true;
                out = Handler.withLook(packet, forceYaw, pitch);
                lastForcedYaw = Float.valueOf(out.getYaw());
            } else if (pendingRestore) {
                pendingRestore = false;
                if (!packet.getRotating()) {
                    out = Handler.withLook(packet, restoreYaw, restorePitch);
                }
            }
            if (out.getRotating()) {
                wireKnown = true;
                wireYaw = out.getYaw();
                wirePitch = out.getPitch();
            }
            return out;
        }

        private static C03PacketPlayer withLook(C03PacketPlayer packet, float yaw, float pitch) {
            if (packet.getRotating() && PacketRotation.apply(packet, yaw, pitch)) {
                return packet;
            }
            if (packet.isMoving()) {
                return new C03PacketPlayer.C06PacketPlayerPosLook(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ(), yaw, pitch, packet.isOnGround());
            }
            return new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, packet.isOnGround());
        }
    }
}
