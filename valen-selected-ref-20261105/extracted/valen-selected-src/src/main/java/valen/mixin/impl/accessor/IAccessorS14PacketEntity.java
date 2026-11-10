/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.play.server.S14PacketEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.network.play.server.S14PacketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={S14PacketEntity.class}, remap=false)
public interface IAccessorS14PacketEntity {
    @Accessor(value="entityId", remap=false)
    public int valen$lunar$getEntityId();
}

