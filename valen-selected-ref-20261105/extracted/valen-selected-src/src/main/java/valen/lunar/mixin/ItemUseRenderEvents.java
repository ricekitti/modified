/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package valen.lunar.mixin;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valen.lunar.render.LunarBlockAnimation;

@Mixin(value={EntityPlayer.class}, remap=false)
public abstract class ItemUseRenderEvents {
    @Inject(method={"getItemInUseCount"}, at={@At(value="RETURN")}, cancellable=true, require=1)
    private void valen$lunarBlockAnimation(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue((Object)LunarBlockAnimation.itemUseCount((EntityPlayer)this, callbackInfoReturnable.getReturnValueI()));
    }
}

