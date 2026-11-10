/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package valen.lunar.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.event.entity.EntityJoinWorldEvent;

@Mixin(value={World.class}, remap=false)
public abstract class WorldEvents {
    @Inject(method={"spawnEntityInWorld"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarJoin(Entity entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        World world = (World)this;
        if (world.isRemote && MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, world))) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }
}

