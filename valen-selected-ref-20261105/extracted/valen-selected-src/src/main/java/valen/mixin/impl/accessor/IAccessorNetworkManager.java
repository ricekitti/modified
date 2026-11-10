/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.INetHandler
 *  net.minecraft.network.NetworkManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={NetworkManager.class}, remap=false)
public interface IAccessorNetworkManager {
    @Accessor(value="packetListener", remap=false)
    public INetHandler valen$lunar$getPacketListener();
}

