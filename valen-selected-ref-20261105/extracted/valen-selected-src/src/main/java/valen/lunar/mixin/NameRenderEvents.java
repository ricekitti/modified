/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.RendererLivingEntity
 *  net.minecraft.entity.EntityLivingBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.client.event.RenderLivingEvent;
import valen.lunar.forge.common.MinecraftForge;

@Mixin(value={RendererLivingEntity.class}, remap=false)
public abstract class NameRenderEvents {
    @Inject(method={"renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarName(EntityLivingBase entityLivingBase, double d, double d2, double d3, CallbackInfo callbackInfo) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Specials.Pre(entityLivingBase, (RendererLivingEntity)this, d, d2, d3))) {
            callbackInfo.cancel();
        }
    }
}

