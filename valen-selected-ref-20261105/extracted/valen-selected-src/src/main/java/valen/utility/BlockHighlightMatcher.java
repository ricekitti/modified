/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.util.BlockPos
 */
package valen.utility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public interface BlockHighlightMatcher {
    public boolean matchesBlock(IBlockState var1);

    default public boolean shouldIndexAt(BlockPos blockPos, IBlockState iBlockState) {
        return this.matchesBlock(iBlockState);
    }

    default public boolean isActive() {
        return true;
    }

    default public void beginScanPass() {
    }
}

