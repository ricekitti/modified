/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderGlobal
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.MovingObjectPosition
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package valen.lunar.mixin;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valen.lunar.forge.client.event.DrawBlockHighlightEvent;
import valen.lunar.forge.common.MinecraftForge;

@Mixin(value={RenderGlobal.class}, remap=false)
public abstract class HighlightEvents {
    @Inject(method={"drawSelectionBox"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarHighlight(EntityPlayer entityPlayer, MovingObjectPosition movingObjectPosition, int n, float f, CallbackInfo callbackInfo) {
        if (MinecraftForge.EVENT_BUS.post(new DrawBlockHighlightEvent((RenderGlobal)this, entityPlayer, movingObjectPosition, n, entityPlayer.getHeldItem(), f))) {
            callbackInfo.cancel();
        }
    }
}

