/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.INetHandler
 *  net.minecraft.network.Packet
 *  net.minecraft.network.ThreadQuickExitException
 *  org.jetbrains.annotations.NotNull
 */
package valen.lag.api;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import org.jetbrains.annotations.NotNull;
import valen.utility.IMinecraftInstance;
import valen.utility.Utils;

public enum EnumLagDirection implements IMinecraftInstance
{
    INBOUND(packet -> {
        try {
            packet.processPacket((INetHandler)mc.getNetHandler());
        }
        catch (ThreadQuickExitException threadQuickExitException) {
        }
        catch (Exception exception) {
            Utils.sendDebugMessage("error while handling packet: " + packet.getClass().getSimpleName());
        }
    }),
    OUTBOUND(packet -> mc.getNetHandler().addToSendQueue(packet));

    @NotNull
    public static final Set<EnumLagDirection> ONLY_INBOUND;
    @NotNull
    public static final Set<EnumLagDirection> ONLY_OUTBOUND;
    @NotNull
    public static final Set<EnumLagDirection> BIDIRECTIONAL;
    @NotNull
    private final Consumer<Packet<?>> channel;

    private EnumLagDirection(Consumer<Packet<?>> consumer) {
        this.channel = consumer;
    }

    public void passThroughChannel(@NotNull Packet<?> packet) {
        this.channel.accept(packet);
    }

    static {
        ONLY_INBOUND = EnumSet.of(INBOUND);
        ONLY_OUTBOUND = EnumSet.of(OUTBOUND);
        BIDIRECTIONAL = EnumSet.allOf(EnumLagDirection.class);
    }
}

