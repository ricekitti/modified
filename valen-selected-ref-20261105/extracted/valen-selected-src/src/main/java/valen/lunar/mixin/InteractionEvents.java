/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.multiplayer.PlayerControllerMP
 *  net.minecraft.client.multiplayer.WorldClient
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package valen.lunar.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.event.entity.player.PlayerInteractEvent;

@Mixin(value={PlayerControllerMP.class}, remap=false)
public abstract class InteractionEvents {
    @Inject(method={"onPlayerRightClick"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void valen$lunarInteract(EntityPlayerSP entityPlayerSP, WorldClient worldClient, ItemStack itemStack, BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent((EntityPlayer)entityPlayerSP, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, blockPos, enumFacing, (World)worldClient))) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }
}

