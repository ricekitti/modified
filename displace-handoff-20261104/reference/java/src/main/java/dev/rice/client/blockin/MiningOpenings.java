package dev.rice.client.blockin;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/** Nearby cells mined by this player, retained across Block In bind presses. */
final class MiningOpenings {
    private static final int MAX_CELLS = 64;
    private static final int PENDING_TICKS = 100;
    private final Map<BlockPos, Dig> cells = new LinkedHashMap<BlockPos, Dig>();
    private EntityPlayerSP player;
    private World world;
    private long tick;

    private static final class Dig {
        long lastActivity;
        boolean removed;
        Dig(long tick) { lastActivity = tick; }
    }

    synchronized void clear() {
        cells.clear();
        player = null;
        world = null;
        tick = 0;
    }

    private boolean session(EntityPlayerSP nextPlayer, World nextWorld) {
        if (player != nextPlayer || world != nextWorld) {
            clear();
            player = nextPlayer;
            world = nextWorld;
        }
        return player != null && world != null;
    }

    private boolean open(BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    private boolean nearby(BlockPos pos) {
        double dx = pos.getX() + 0.5 - player.posX;
        double dy = pos.getY() + 0.5 - (player.posY + player.getEyeHeight());
        double dz = pos.getZ() + 0.5 - player.posZ;
        return dx * dx + dy * dy + dz * dz <= 36.0;
    }

    synchronized void observe(C07PacketPlayerDigging packet, EntityPlayerSP nextPlayer, World nextWorld) {
        C07PacketPlayerDigging.Action action = packet.getStatus();
        if (action != C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
                && action != C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
                && action != C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) return;
        if (!session(nextPlayer, nextWorld)) return;
        BlockPos pos = packet.getPosition();
        if (!nearby(pos)) return;
        Dig dig = cells.get(pos);
        if (action == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
            // An aborted swing against an intact block is not a mined opening.
            if (dig != null && !dig.removed && !open(pos)) cells.remove(pos);
            return;
        }
        if (dig == null && !open(pos)) {
            pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            dig = new Dig(tick);
            cells.put(pos, dig);
            if (cells.size() > MAX_CELLS) cells.remove(cells.keySet().iterator().next());
        }
        if (dig != null) dig.lastActivity = tick;
    }

    synchronized void update(EntityPlayerSP nextPlayer, World nextWorld) {
        if (!session(nextPlayer, nextWorld)) return;
        ++tick;
        Iterator<Map.Entry<BlockPos, Dig>> entries = cells.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<BlockPos, Dig> entry = entries.next();
            BlockPos pos = entry.getKey();
            Dig dig = entry.getValue();
            if (!nearby(pos)) {
                entries.remove();
            } else if (open(pos)) {
                dig.removed = true;
            } else if (dig.removed || tick - dig.lastActivity > PENDING_TICKS) {
                // A replacement/server correction retires the old opening.
                entries.remove();
            }
        }
    }

    synchronized boolean protects(BlockPos pos) {
        Dig dig = cells.get(pos);
        if (dig == null || player == null || world == null || !nearby(pos)) return false;
        // The placement planner may run in the same tick as the local removal.
        if (open(pos)) {
            dig.removed = true;
            return true;
        }
        if (dig.removed) cells.remove(pos);
        return false;
    }
}
