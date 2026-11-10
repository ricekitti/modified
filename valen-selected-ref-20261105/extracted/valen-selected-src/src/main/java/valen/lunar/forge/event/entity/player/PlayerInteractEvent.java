/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.world.World
 */
package valen.lunar.forge.event.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import valen.lunar.forge.event.entity.player.PlayerEvent;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerInteractEvent
extends PlayerEvent {
    public final Action action;
    public final BlockPos pos;
    public final EnumFacing face;
    public final World world;

    public PlayerInteractEvent(EntityPlayer entityPlayer, Action action, BlockPos blockPos, EnumFacing enumFacing, World world) {
        super(entityPlayer);
        this.action = action;
        this.pos = blockPos;
        this.face = enumFacing;
        this.world = world;
    }

    public static enum Action {
        RIGHT_CLICK_AIR,
        RIGHT_CLICK_BLOCK,
        LEFT_CLICK_BLOCK;

    }
}

