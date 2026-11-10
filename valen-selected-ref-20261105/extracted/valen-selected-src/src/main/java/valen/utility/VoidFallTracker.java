/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.World
 *  valen.utility.VoidFallTracker$FallState
 */
package valen.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import valen.utility.Utils;
import valen.utility.VoidFallTracker;

/*
 * Exception performing whole class analysis ignored.
 */
public final class VoidFallTracker {
    private static final double MIN_DESCENT = 0.5;
    private static final int SCAN_FLOOR = 0;
    private final Map<Integer, FallState> falling = new HashMap<Integer, FallState>();

    public boolean hasFallenFor(World world, EntityPlayer entityPlayer, long l, long l2) {
        int n = entityPlayer.getEntityId();
        if (entityPlayer.onGround || !this.isOverVoid(world, entityPlayer)) {
            this.falling.remove(n);
            return false;
        }
        FallState fallState = this.falling.get(n);
        if (fallState == null) {
            this.falling.put(n, new FallState(l, entityPlayer.posY, null));
            return false;
        }
        return FallState.access$100((FallState)fallState) - entityPlayer.posY >= 0.5 && Utils.timeBetween(l, FallState.access$200((FallState)fallState)) >= l2;
    }

    public void pruneStale(World world) {
        Iterator<Integer> iterator = this.falling.keySet().iterator();
        while (iterator.hasNext()) {
            if (world.getEntityByID(iterator.next().intValue()) != null) continue;
            iterator.remove();
        }
    }

    public void clear() {
        this.falling.clear();
    }

    private boolean isOverVoid(World world, EntityPlayer entityPlayer) {
        int n = MathHelper.floor_double((double)entityPlayer.posX);
        int n2 = MathHelper.floor_double((double)entityPlayer.posZ);
        int n3 = MathHelper.floor_double((double)entityPlayer.posY);
        if (n3 < 0) {
            return true;
        }
        for (int i = Math.min(n3, 255); i >= 0; --i) {
            if (world.getBlockState(new BlockPos(n, i, n2)).getBlock() == Blocks.air) continue;
            return false;
        }
        return true;
    }
}

