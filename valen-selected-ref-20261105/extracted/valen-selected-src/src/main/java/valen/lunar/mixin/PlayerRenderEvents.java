/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.entity.AbstractClientPlayer
 *  net.minecraft.client.renderer.entity.RenderPlayer
 *  net.minecraft.entity.player.EntityPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.client.event.RenderPlayerEvent;
import valen.lunar.forge.common.MinecraftForge;

@Mixin(value={RenderPlayer.class}, remap=false)
public abstract class PlayerRenderEvents {
    @Inject(method={"doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarPlayerRenderPre(AbstractClientPlayer abstractClientPlayer, double d, double d2, double d3, float f, float f2, CallbackInfo callbackInfo) {
        if (MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Pre((EntityPlayer)abstractClientPlayer, (RenderPlayer)this, f2, d, d2, d3))) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V"}, at={@At(value="RETURN")}, require=1)
    private void valen$lunarPlayerRenderPost(AbstractClientPlayer abstractClientPlayer, double d, double d2, double d3, float f, float f2, CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Post((EntityPlayer)abstractClientPlayer, (RenderPlayer)this, f2, d, d2, d3));
    }
}

