/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockBed
 *  net.minecraft.block.BlockBed$EnumPartType
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.util.BlockPos
 */
package valen.utility;

import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import valen.utility.BlockHighlightMatcher;

public final class BedFootHighlightMatcher
implements BlockHighlightMatcher {
    @Override
    public boolean matchesBlock(IBlockState iBlockState) {
        return iBlockState != null && iBlockState.getBlock() instanceof BlockBed;
    }

    @Override
    public boolean shouldIndexAt(BlockPos blockPos, IBlockState iBlockState) {
        if (!this.matchesBlock(iBlockState)) {
            return false;
        }
        return iBlockState.getValue((IProperty)BlockBed.PART) == BlockBed.EnumPartType.FOOT;
    }
}

