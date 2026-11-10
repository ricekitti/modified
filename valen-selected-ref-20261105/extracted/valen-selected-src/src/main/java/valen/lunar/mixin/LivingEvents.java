/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityLivingBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.event.entity.living.LivingEvent;

@Mixin(value={EntityLivingBase.class}, remap=false)
public abstract class LivingEvents {
    @Inject(method={"onUpdate"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarLiving(CallbackInfo callbackInfo) {
        EntityLivingBase entityLivingBase = (EntityLivingBase)this;
        if (entityLivingBase.worldObj.isRemote && MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingUpdateEvent(entityLivingBase))) {
            callbackInfo.cancel();
        }
    }
}

