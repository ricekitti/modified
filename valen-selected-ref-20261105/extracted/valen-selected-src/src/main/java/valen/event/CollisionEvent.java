/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 */
package valen.event;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class CollisionEvent
extends Event {
    public final BlockPos blockPos;
    public final Block block;
    public AxisAlignedBB boundingBox;

    public CollisionEvent(BlockPos blockPos, Block block, AxisAlignedBB axisAlignedBB) {
        this.blockPos = blockPos;
        this.block = block;
        this.boundingBox = axisAlignedBB;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public Block getBlock() {
        return this.block;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB axisAlignedBB) {
        this.boundingBox = axisAlignedBB;
    }
}

