/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.server.S25PacketBlockBreakAnim
 *  net.minecraft.network.play.server.S28PacketEffect
 *  net.minecraft.potion.Potion
 *  net.minecraft.util.BlockPos
 */
package com.snowify.anticheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S28PacketEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

public final class BreakCheck {
    static final String FAST_MINE = "Fast Mine";
    static final String NO_BREAK_DELAY = "No Break Delay";
    private static final double FAST_SHARE = 0.85;
    private static final int MIN_TICKS = 5;
    private static final long NO_DELAY_MS = 180L;
    private static final int QUICK_STARTS = 2;
    private final Queue<Seen> seen = new ConcurrentLinkedQueue<Seen>();
    private final Map<Integer, Dig> digs = new HashMap<Integer, Dig>();

    public void onPacket(Packet<?> packet) {
        if (packet instanceof S25PacketBlockBreakAnim || packet instanceof S28PacketEffect && ((S28PacketEffect)packet).getSoundType() == 2001) {
            this.seen.add(new Seen(packet, System.currentTimeMillis()));
        }
    }

    void clear() {
        this.seen.clear();
        this.digs.clear();
    }

    public void process(Minecraft mc, boolean fastMine, boolean noBreakDelay, Flagger flagger) {
        Seen next;
        while ((next = this.seen.poll()) != null) {
            if (mc.theWorld == null || mc.thePlayer == null) continue;
            if (next.packet instanceof S25PacketBlockBreakAnim) {
                this.crack(mc, (S25PacketBlockBreakAnim)next.packet, next.at, noBreakDelay, flagger);
                continue;
            }
            this.broken(mc, (S28PacketEffect)next.packet, next.at, fastMine, flagger);
        }
        long now = System.currentTimeMillis();
        Iterator<Dig> it = this.digs.values().iterator();
        while (it.hasNext()) {
            if (now - it.next().lastAt <= 10000L) continue;
            it.remove();
        }
    }

    private void crack(Minecraft mc, S25PacketBlockBreakAnim packet, long at, boolean noBreakDelay, Flagger flagger) {
        int id = packet.getBreakerId();
        if (id == mc.thePlayer.getEntityId() || packet.getProgress() < 0 || packet.getProgress() > 9) {
            return;
        }
        Dig dig = this.digs.get(id);
        if (dig == null) {
            dig = new Dig();
            this.digs.put(id, dig);
        }
        dig.lastAt = at;
        if (packet.getPosition().equals((Object)dig.pos)) {
            return;
        }
        if (dig.brokenAt >= 0L) {
            if (at - dig.brokenAt < 180L) {
                ++dig.quickStarts;
                EntityPlayer player = BreakCheck.player(mc, id);
                if (noBreakDelay && player != null && dig.quickStarts >= 2) {
                    flagger.flag(player, NO_BREAK_DELAY, 1);
                    dig.quickStarts = 0;
                }
            } else {
                dig.quickStarts = 0;
            }
            dig.brokenAt = -1L;
        }
        dig.pos = packet.getPosition();
        dig.startedAt = packet.getProgress() == 0 ? at : -1L;
    }

    private void broken(Minecraft mc, S28PacketEffect packet, long at, boolean fastMine, Flagger flagger) {
        BlockPos pos = packet.getSoundPos();
        for (Map.Entry<Integer, Dig> entry : this.digs.entrySet()) {
            int least;
            Dig dig = entry.getValue();
            if (!pos.equals((Object)dig.pos)) continue;
            dig.pos = null;
            dig.lastAt = at;
            EntityPlayer player = BreakCheck.player(mc, entry.getKey());
            int n = least = player == null ? -1 : BreakCheck.leastTicks(player, Block.getStateById((int)packet.getSoundData()), pos);
            if (fastMine && least >= 5) {
                boolean fromLast;
                boolean fromStart = dig.startedAt >= 0L && (double)(at - dig.startedAt) / 50.0 < (double)least * 0.85;
                boolean bl = fromLast = dig.lastBrokeAt >= 0L && (double)(at - dig.lastBrokeAt) / 50.0 < (double)least * 0.85;
                if (fromStart || fromLast) {
                    flagger.flag(player, FAST_MINE, 2);
                }
            }
            dig.lastBrokeAt = at;
            dig.brokenAt = least >= 5 ? at : -1L;
            return;
        }
    }

    private static EntityPlayer player(Minecraft mc, int id) {
        Entity entity = mc.theWorld.getEntityByID(id);
        return entity instanceof EntityPlayer && entity != mc.thePlayer ? (EntityPlayer)entity : null;
    }

    private static int leastTicks(EntityPlayer player, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        float hardness = block.getBlockHardness(player.worldObj, pos);
        if (hardness <= 0.0f) {
            return -1;
        }
        ItemStack held = player.getHeldItem();
        float speed = held == null ? 1.0f : held.getStrVsBlock(block);
        int efficiency = EnchantmentHelper.getEfficiencyModifier((EntityLivingBase)player);
        if (speed > 1.0f && efficiency > 0) {
            speed += (float)(efficiency * efficiency + 1);
        }
        int haste = 1;
        if (player.isPotionActive(Potion.digSpeed)) {
            haste = Math.max(haste, player.getActivePotionEffect(Potion.digSpeed).getAmplifier());
        }
        boolean harvest = block.getMaterial().isToolNotRequired() || held != null && held.canHarvestBlock(block);
        float perTick = (speed *= 1.0f + (float)(haste + 1) * 0.2f) / hardness / (harvest ? 30.0f : 100.0f);
        return perTick >= 1.0f ? -1 : (int)Math.ceil(1.0f / perTick);
    }

    private static final class Dig {
        BlockPos pos;
        long startedAt;
        long lastAt;
        long brokenAt = -1L;
        int quickStarts;
        long lastBrokeAt = -1L;

        private Dig() {
        }
    }

    private static final class Seen {
        final Packet<?> packet;
        final long at;

        Seen(Packet<?> packet, long at) {
            this.packet = packet;
            this.at = at;
        }
    }

    public static interface Flagger {
        public void flag(EntityPlayer var1, String var2, int var3);
    }
}
