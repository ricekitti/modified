/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.EntityRenderer
 *  net.minecraft.client.renderer.ItemRenderer
 *  net.minecraft.entity.player.EntityPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.client.event.RenderWorldLastEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.render.LunarBlockAnimation;

@Mixin(value={EntityRenderer.class}, remap=false)
public abstract class RenderEvents {
    @Redirect(method={"*"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V"), require=1)
    private void valen$lunarRenderItem(ItemRenderer itemRenderer, float f) {
        LunarBlockAnimation.render(itemRenderer, (EntityPlayer)Minecraft.getMinecraft().thePlayer, f);
    }

    @Inject(method={"renderWorldPass"}, at={@At(value="CONSTANT", args={"stringValue=hand"})}, require=1)
    private void valen$lunarWorldRender(int n, float f, long l, CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new RenderWorldLastEvent(Minecraft.getMinecraft().renderGlobal, f));
    }
}

