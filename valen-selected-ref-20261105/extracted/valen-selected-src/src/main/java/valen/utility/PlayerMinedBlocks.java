/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.BlockPos
 *  net.minecraft.world.World
 */
package valen.utility;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public final class PlayerMinedBlocks {
    private static final Map<World, Set<BlockPos>> positions = new WeakHashMap<World, Set<BlockPos>>();

    private PlayerMinedBlocks() {
    }

    public static void record(World world2, BlockPos blockPos) {
        if (world2 == null || blockPos == null) {
            return;
        }
        positions.computeIfAbsent(world2, world -> new HashSet()).add(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    public static boolean contains(World world, BlockPos blockPos) {
        Set<BlockPos> set = positions.get(world);
        return set != null && set.contains(blockPos);
    }
}

