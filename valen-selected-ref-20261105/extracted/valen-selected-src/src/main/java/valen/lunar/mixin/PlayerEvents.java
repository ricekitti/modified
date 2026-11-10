/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.fml.common.gameevent.TickEvent;

@Mixin(value={EntityPlayer.class}, remap=false)
public abstract class PlayerEvents {
    @Inject(method={"onUpdate"}, at={@At(value="HEAD")}, require=1)
    private void valen$lunarPlayerStart(CallbackInfo callbackInfo) {
        EntityPlayer entityPlayer = (EntityPlayer)this;
        if (entityPlayer.worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, entityPlayer));
        }
    }

    @Inject(method={"onUpdate"}, at={@At(value="RETURN")}, require=1)
    private void valen$lunarPlayerEnd(CallbackInfo callbackInfo) {
        EntityPlayer entityPlayer = (EntityPlayer)this;
        if (entityPlayer.worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, entityPlayer));
        }
    }
}

