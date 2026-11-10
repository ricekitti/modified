/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Packet
 *  org.jetbrains.annotations.NotNull
 */
package valen.lag.handler;

import net.minecraft.network.Packet;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFastTrackProvider {
    public abstract void forPacket(@NotNull Packet<?> var1);
}

