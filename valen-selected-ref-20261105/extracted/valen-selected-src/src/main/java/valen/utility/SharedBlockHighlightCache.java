/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockColored
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.EnumDyeColor
 *  net.minecraft.network.play.server.S21PacketChunkData
 *  net.minecraft.network.play.server.S22PacketMultiBlockChange
 *  net.minecraft.network.play.server.S22PacketMultiBlockChange$BlockUpdateData
 *  net.minecraft.network.play.server.S23PacketBlockChange
 *  net.minecraft.network.play.server.S26PacketMapChunkBulk
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.BlockPos$MutableBlockPos
 *  net.minecraft.util.Vec3i
 *  net.minecraft.world.chunk.Chunk
 *  net.minecraft.world.chunk.EmptyChunk
 *  net.minecraft.world.chunk.storage.ExtendedBlockStorage
 *  valen.utility.BlockListHighlightMatcher
 */
package valen.utility;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.BlockColored;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import valen.event.ReceivePacketEvent;
import valen.module.setting.impl.BlockListSetting;
import valen.utility.BedFootHighlightMatcher;
import valen.utility.BlockListHighlightMatcher;

public final class SharedBlockHighlightCache {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final SharedBlockHighlightCache INSTANCE = new SharedBlockHighlightCache();
    private final Map<Long, Set<BlockPos>> blockListByChunk = new ConcurrentHashMap<Long, Set<BlockPos>>();
    private final Map<Long, Set<BlockPos>> bedFootByChunk = new ConcurrentHashMap<Long, Set<BlockPos>>();
    private final Map<Long, Set<BlockPos>> tunnelByChunk = new ConcurrentHashMap<Long, Set<BlockPos>>();
    private final Set<UpdateListener> updateListeners = ConcurrentHashMap.newKeySet();
    private final Deque<long[]> scanQueue = new ArrayDeque<long[]>();
    private final ConcurrentLinkedQueue<BlockPos> tunnelUpdates = new ConcurrentLinkedQueue();
    private BlockListHighlightMatcher blockListMatcher;
    private boolean bedAttached;
    private boolean tunnelsAttached;
    private int tunnelMinClayNeighbors;
    private static final BedFootHighlightMatcher BED_MATCHER = new BedFootHighlightMatcher();
    private static final int TUNNEL_RADIUS = 2;
    private static final int TUNNEL_NEIGHBOR_COUNT = 124;

    private SharedBlockHighlightCache() {
    }

    public static SharedBlockHighlightCache get() {
        return INSTANCE;
    }

    public void attachBlockList(BlockListSetting blockListSetting) {
        this.blockListMatcher = new BlockListHighlightMatcher(blockListSetting);
    }

    public void detachBlockList() {
        this.blockListMatcher = null;
        this.blockListByChunk.clear();
    }

    public void attachBed() {
        this.bedAttached = true;
    }

    public void detachBed() {
        this.bedAttached = false;
        this.bedFootByChunk.clear();
    }

    public void attachTunnels(int n) {
        this.tunnelsAttached = true;
        this.tunnelMinClayNeighbors = SharedBlockHighlightCache.clampTunnelNeighborCount(n);
    }

    public void detachTunnels() {
        this.tunnelsAttached = false;
        this.tunnelByChunk.clear();
        this.tunnelUpdates.clear();
    }

    private boolean isBlockListActive() {
        return this.blockListMatcher != null && this.blockListMatcher.isActive();
    }

    private boolean isBedActive() {
        return this.bedAttached;
    }

    private boolean isTunnelActive() {
        return this.tunnelsAttached;
    }

    public boolean anyConsumerActive() {
        return this.isBlockListActive() || this.isBedActive() || this.isTunnelActive();
    }

    public void clear() {
        this.blockListByChunk.clear();
        this.bedFootByChunk.clear();
        this.tunnelByChunk.clear();
        this.scanQueue.clear();
        this.tunnelUpdates.clear();
        for (UpdateListener updateListener : this.updateListeners) {
            updateListener.onCacheCleared();
        }
    }

    public void addUpdateListener(UpdateListener updateListener) {
        if (updateListener != null) {
            this.updateListeners.add(updateListener);
        }
    }

    public void removeUpdateListener(UpdateListener updateListener) {
        if (updateListener != null) {
            this.updateListeners.remove(updateListener);
        }
    }

    public void enqueueChunk(int n, int n2) {
        if (!this.anyConsumerActive()) {
            return;
        }
        this.scanQueue.addLast(new long[]{n, n2});
        for (UpdateListener updateListener : this.updateListeners) {
            updateListener.onChunkQueued(n, n2);
        }
    }

    public void removeChunk(int n, int n2) {
        long l = SharedBlockHighlightCache.key(n, n2);
        this.blockListByChunk.remove(l);
        this.bedFootByChunk.remove(l);
        this.tunnelByChunk.remove(l);
        for (UpdateListener updateListener : this.updateListeners) {
            updateListener.onChunkRemoved(n, n2);
        }
    }

    public void enqueueLoadedChunks() {
        if (!this.anyConsumerActive()) {
            return;
        }
        this.scanQueue.clear();
        if (SharedBlockHighlightCache.mc.theWorld == null || SharedBlockHighlightCache.mc.thePlayer == null) {
            return;
        }
        int n = SharedBlockHighlightCache.mc.gameSettings.renderDistanceChunks;
        int n2 = (int)SharedBlockHighlightCache.mc.thePlayer.posX >> 4;
        int n3 = (int)SharedBlockHighlightCache.mc.thePlayer.posZ >> 4;
        for (int i = n2 - n; i <= n2 + n; ++i) {
            for (int j = n3 - n; j <= n3 + n; ++j) {
                Chunk chunk = SharedBlockHighlightCache.mc.theWorld.getChunkFromChunkCoords(i, j);
                if (chunk == null || chunk instanceof EmptyChunk) continue;
                this.enqueueChunk(i, j);
            }
        }
    }

    public void tickScan(int n) {
        if (SharedBlockHighlightCache.mc.theWorld == null || !this.anyConsumerActive()) {
            return;
        }
        this.refreshTunnelUpdates();
        if (this.blockListMatcher != null) {
            this.blockListMatcher.beginScanPass();
        }
        int n2 = n;
        while (n2 > 0 && !this.scanQueue.isEmpty()) {
            int n3;
            long[] lArray = this.scanQueue.pollFirst();
            int n4 = (int)lArray[0];
            Chunk chunk = SharedBlockHighlightCache.mc.theWorld.getChunkFromChunkCoords(n4, n3 = (int)lArray[1]);
            if (chunk == null || chunk instanceof EmptyChunk) continue;
            n2 -= this.scanChunk(chunk);
        }
    }

    public void onBlockChange(BlockPos blockPos, IBlockState iBlockState) {
        Set<BlockPos> set;
        if (this.blockListMatcher != null) {
            this.blockListMatcher.beginScanPass();
        }
        long l2 = SharedBlockHighlightCache.key(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (this.isBlockListActive()) {
            if (this.blockListMatcher.matchesBlock(iBlockState) && this.blockListMatcher.shouldIndexAt(blockPos, iBlockState)) {
                this.blockListByChunk.computeIfAbsent(l2, l -> ConcurrentHashMap.newKeySet()).add(blockPos2);
            } else {
                set = this.blockListByChunk.get(l2);
                if (set != null) {
                    set.remove(blockPos);
                }
            }
        }
        if (this.isBedActive()) {
            if (BED_MATCHER.matchesBlock(iBlockState) && BED_MATCHER.shouldIndexAt(blockPos, iBlockState)) {
                this.bedFootByChunk.computeIfAbsent(l2, l -> ConcurrentHashMap.newKeySet()).add(blockPos2);
            } else {
                set = this.bedFootByChunk.get(l2);
                if (set != null) {
                    set.remove(blockPos);
                }
            }
        }
        if (this.isTunnelActive()) {
            this.tunnelUpdates.add(blockPos2);
        }
        for (UpdateListener updateListener : this.updateListeners) {
            updateListener.onBlockChanged(blockPos2, iBlockState);
        }
    }

    public void onBlockListSettingsChanged() {
        if (this.blockListMatcher != null) {
            this.blockListMatcher.beginScanPass();
        }
        this.rescanBlockListLayer();
    }

    public void onTunnelSettingsChanged(int n) {
        int n2 = SharedBlockHighlightCache.clampTunnelNeighborCount(n);
        if (n2 == this.tunnelMinClayNeighbors) {
            return;
        }
        this.tunnelMinClayNeighbors = n2;
        this.tunnelByChunk.clear();
        this.scanQueue.clear();
        this.enqueueLoadedChunks();
    }

    private void rescanBlockListLayer() {
        this.blockListByChunk.clear();
        this.scanQueue.clear();
        if (!this.anyConsumerActive()) {
            return;
        }
        this.enqueueLoadedChunks();
    }

    public Iterable<Map.Entry<Long, Set<BlockPos>>> entriesBlockList() {
        return this.blockListByChunk.entrySet();
    }

    public Iterable<Map.Entry<Long, Set<BlockPos>>> entriesBedFeet() {
        return this.bedFootByChunk.entrySet();
    }

    public Iterable<Map.Entry<Long, Set<BlockPos>>> entriesTunnels() {
        return this.tunnelByChunk.entrySet();
    }

    public boolean containsTunnel(BlockPos blockPos) {
        Set<BlockPos> set = this.tunnelByChunk.get(SharedBlockHighlightCache.key(blockPos.getX() >> 4, blockPos.getZ() >> 4));
        return set != null && set.contains(blockPos);
    }

    public int totalBlockList() {
        int n = 0;
        for (Set<BlockPos> set : this.blockListByChunk.values()) {
            n += set.size();
        }
        return n;
    }

    public int totalBedFeet() {
        int n = 0;
        for (Set<BlockPos> set : this.bedFootByChunk.values()) {
            n += set.size();
        }
        return n;
    }

    public int totalTunnels() {
        int n = 0;
        for (Set<BlockPos> set : this.tunnelByChunk.values()) {
            n += set.size();
        }
        return n;
    }

    public boolean containsBlockList(BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        long l = SharedBlockHighlightCache.key(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        Set<BlockPos> set = this.blockListByChunk.get(l);
        return set != null && set.contains(blockPos);
    }

    public void handleReceivePacket(ReceivePacketEvent receivePacketEvent) {
        block6: {
            block8: {
                block7: {
                    block5: {
                        if (!this.anyConsumerActive()) {
                            return;
                        }
                        if (!(receivePacketEvent.getPacket() instanceof S23PacketBlockChange)) break block5;
                        S23PacketBlockChange s23PacketBlockChange = (S23PacketBlockChange)receivePacketEvent.getPacket();
                        this.onBlockChange(s23PacketBlockChange.getBlockPosition(), s23PacketBlockChange.getBlockState());
                        break block6;
                    }
                    if (!(receivePacketEvent.getPacket() instanceof S22PacketMultiBlockChange)) break block7;
                    S22PacketMultiBlockChange s22PacketMultiBlockChange = (S22PacketMultiBlockChange)receivePacketEvent.getPacket();
                    for (S22PacketMultiBlockChange.BlockUpdateData blockUpdateData : s22PacketMultiBlockChange.getChangedBlocks()) {
                        this.onBlockChange(blockUpdateData.getPos(), blockUpdateData.getBlockState());
                    }
                    break block6;
                }
                if (!(receivePacketEvent.getPacket() instanceof S21PacketChunkData)) break block8;
                S21PacketChunkData s21PacketChunkData = (S21PacketChunkData)receivePacketEvent.getPacket();
                if (s21PacketChunkData.getExtractedSize() == 0) {
                    this.removeChunk(s21PacketChunkData.getChunkX(), s21PacketChunkData.getChunkZ());
                } else {
                    this.enqueueChunk(s21PacketChunkData.getChunkX(), s21PacketChunkData.getChunkZ());
                }
                break block6;
            }
            if (!(receivePacketEvent.getPacket() instanceof S26PacketMapChunkBulk)) break block6;
            S26PacketMapChunkBulk s26PacketMapChunkBulk = (S26PacketMapChunkBulk)receivePacketEvent.getPacket();
            for (int i = 0; i < s26PacketMapChunkBulk.getChunkCount(); ++i) {
                this.enqueueChunk(s26PacketMapChunkBulk.getChunkX(i), s26PacketMapChunkBulk.getChunkZ(i));
            }
        }
    }

    private int scanChunk(Chunk chunk) {
        int n = 0;
        long l = SharedBlockHighlightCache.key(chunk.xPosition, chunk.zPosition);
        ConcurrentHashMap.KeySetView keySetView = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap.KeySetView keySetView2 = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap.KeySetView keySetView3 = ConcurrentHashMap.newKeySet();
        ExtendedBlockStorage[] extendedBlockStorageArray = chunk.getBlockStorageArray();
        int n2 = chunk.xPosition << 4;
        int n3 = chunk.zPosition << 4;
        for (int i = 0; i < extendedBlockStorageArray.length; ++i) {
            ExtendedBlockStorage extendedBlockStorage = extendedBlockStorageArray[i];
            if (extendedBlockStorage == null) continue;
            ++n;
            int n4 = i << 4;
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int i2 = 0; i2 < 16; ++i2) {
                        boolean bl;
                        IBlockState iBlockState = extendedBlockStorage.get(i2, j, k);
                        if (iBlockState == null) continue;
                        boolean bl2 = this.isBlockListActive() && this.blockListMatcher.matchesBlock(iBlockState);
                        boolean bl3 = bl = this.isBedActive() && BED_MATCHER.matchesBlock(iBlockState);
                        if (!bl2 && !bl) continue;
                        BlockPos blockPos = new BlockPos(n2 + i2, n4 + j, n3 + k);
                        if (bl2 && this.blockListMatcher.shouldIndexAt(blockPos, iBlockState)) {
                            keySetView.add(blockPos);
                        }
                        if (!bl || !BED_MATCHER.shouldIndexAt(blockPos, iBlockState)) continue;
                        keySetView2.add(blockPos);
                    }
                }
            }
            if (!this.isTunnelActive()) continue;
            this.scanTunnelSection(extendedBlockStorage, n2, n4, n3, keySetView3);
        }
        if (this.isBlockListActive()) {
            if (!keySetView.isEmpty()) {
                this.blockListByChunk.put(l, keySetView);
            } else {
                this.blockListByChunk.remove(l);
            }
        }
        if (this.isBedActive()) {
            if (!keySetView2.isEmpty()) {
                this.bedFootByChunk.put(l, keySetView2);
            } else {
                this.bedFootByChunk.remove(l);
            }
        }
        if (this.isTunnelActive()) {
            if (!keySetView3.isEmpty()) {
                this.tunnelByChunk.put(l, keySetView3);
            } else {
                this.tunnelByChunk.remove(l);
            }
        }
        return Math.max(n, 1);
    }

    private void scanTunnelSection(ExtendedBlockStorage extendedBlockStorage, int n, int n2, int n3, Set<BlockPos> set) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9 = 20;
        int n10 = n9 + 1;
        int[] nArray = new int[n10 * n10 * n10];
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n8 = 0; n8 < n9; ++n8) {
            n7 = n + n8 - 2;
            for (n6 = 0; n6 < n9; ++n6) {
                n5 = n2 + n6 - 2;
                for (int i = 0; i < n9; ++i) {
                    n4 = n3 + i - 2;
                    int n11 = n8 + 1;
                    int n12 = n6 + 1;
                    int n13 = i + 1;
                    int n14 = SharedBlockHighlightCache.isTargetClay(this.stateAt(mutableBlockPos, n7, n5, n4)) ? 1 : 0;
                    nArray[SharedBlockHighlightCache.prefixIndex((int)n11, (int)n12, (int)n13, (int)n10)] = n14 + nArray[SharedBlockHighlightCache.prefixIndex(n11 - 1, n12, n13, n10)] + nArray[SharedBlockHighlightCache.prefixIndex(n11, n12 - 1, n13, n10)] + nArray[SharedBlockHighlightCache.prefixIndex(n11, n12, n13 - 1, n10)] - nArray[SharedBlockHighlightCache.prefixIndex(n11 - 1, n12 - 1, n13, n10)] - nArray[SharedBlockHighlightCache.prefixIndex(n11 - 1, n12, n13 - 1, n10)] - nArray[SharedBlockHighlightCache.prefixIndex(n11, n12 - 1, n13 - 1, n10)] + nArray[SharedBlockHighlightCache.prefixIndex(n11 - 1, n12 - 1, n13 - 1, n10)];
                }
            }
        }
        n8 = 5;
        for (n7 = 0; n7 < 16; ++n7) {
            for (n6 = 0; n6 < 16; ++n6) {
                for (n5 = 0; n5 < 16; ++n5) {
                    IBlockState iBlockState = extendedBlockStorage.get(n7, n6, n5);
                    if (iBlockState == null || iBlockState.getBlock() != Blocks.air || (n4 = SharedBlockHighlightCache.cubeSum(nArray, n10, n7, n6, n5, n7 + n8, n6 + n8, n5 + n8)) < this.tunnelMinClayNeighbors) continue;
                    set.add(new BlockPos(n + n7, n2 + n6, n3 + n5));
                }
            }
        }
    }

    private void refreshTunnelUpdates() {
        BlockPos blockPos;
        if (!this.isTunnelActive()) {
            this.tunnelUpdates.clear();
            return;
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        while ((blockPos = this.tunnelUpdates.poll()) != null) {
            hashSet.add(blockPos);
        }
        for (BlockPos blockPos2 : hashSet) {
            this.refreshTunnelArea(blockPos2);
        }
    }

    private void refreshTunnelArea(BlockPos blockPos) {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9 = 4;
        int n10 = n9 * 2 + 1;
        int n11 = n10 + 1;
        int[] nArray = new int[n11 * n11 * n11];
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n8 = 0; n8 < n10; ++n8) {
            n7 = blockPos.getX() + n8 - n9;
            for (n6 = 0; n6 < n10; ++n6) {
                n5 = blockPos.getY() + n6 - n9;
                for (int i = 0; i < n10; ++i) {
                    int n12 = blockPos.getZ() + i - n9;
                    n4 = n8 + 1;
                    n3 = n6 + 1;
                    n2 = i + 1;
                    n = SharedBlockHighlightCache.isTargetClay(this.stateAt(mutableBlockPos, n7, n5, n12)) ? 1 : 0;
                    nArray[SharedBlockHighlightCache.prefixIndex((int)n4, (int)n3, (int)n2, (int)n11)] = n + nArray[SharedBlockHighlightCache.prefixIndex(n4 - 1, n3, n2, n11)] + nArray[SharedBlockHighlightCache.prefixIndex(n4, n3 - 1, n2, n11)] + nArray[SharedBlockHighlightCache.prefixIndex(n4, n3, n2 - 1, n11)] - nArray[SharedBlockHighlightCache.prefixIndex(n4 - 1, n3 - 1, n2, n11)] - nArray[SharedBlockHighlightCache.prefixIndex(n4 - 1, n3, n2 - 1, n11)] - nArray[SharedBlockHighlightCache.prefixIndex(n4, n3 - 1, n2 - 1, n11)] + nArray[SharedBlockHighlightCache.prefixIndex(n4 - 1, n3 - 1, n2 - 1, n11)];
                }
            }
        }
        n8 = 5;
        for (n7 = -2; n7 <= 2; ++n7) {
            for (n6 = -2; n6 <= 2; ++n6) {
                for (n5 = -2; n5 <= 2; ++n5) {
                    BlockPos blockPos2 = blockPos.add(n7, n6, n5);
                    IBlockState iBlockState = this.stateAt(mutableBlockPos, blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
                    n4 = n7 + n9;
                    n3 = n6 + n9;
                    n2 = n5 + n9;
                    n = SharedBlockHighlightCache.cubeSum(nArray, n11, n4 - 2, n3 - 2, n2 - 2, n4 - 2 + n8, n3 - 2 + n8, n2 - 2 + n8);
                    this.setTunnelCandidate(blockPos2, iBlockState != null && iBlockState.getBlock() == Blocks.air && n >= this.tunnelMinClayNeighbors);
                }
            }
        }
    }

    private void setTunnelCandidate(BlockPos blockPos, boolean bl) {
        long l2 = SharedBlockHighlightCache.key(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        if (bl) {
            this.tunnelByChunk.computeIfAbsent(l2, l -> ConcurrentHashMap.newKeySet()).add(new BlockPos((Vec3i)blockPos));
            return;
        }
        Set<BlockPos> set = this.tunnelByChunk.get(l2);
        if (set == null) {
            return;
        }
        set.remove(blockPos);
        if (set.isEmpty()) {
            this.tunnelByChunk.remove(l2, set);
        }
    }

    private IBlockState stateAt(BlockPos.MutableBlockPos mutableBlockPos, int n, int n2, int n3) {
        if (n2 < 0 || n2 > 255 || SharedBlockHighlightCache.mc.theWorld == null) {
            return null;
        }
        return SharedBlockHighlightCache.mc.theWorld.getBlockState((BlockPos)mutableBlockPos.set(n, n2, n3));
    }

    private static boolean isTargetClay(IBlockState iBlockState) {
        if (iBlockState == null || iBlockState.getBlock() != Blocks.stained_hardened_clay) {
            return false;
        }
        EnumDyeColor enumDyeColor = (EnumDyeColor)iBlockState.getValue((IProperty)BlockColored.COLOR);
        return enumDyeColor == EnumDyeColor.BLUE || enumDyeColor == EnumDyeColor.RED;
    }

    private static int cubeSum(int[] nArray, int n, int n2, int n3, int n4, int n5, int n6, int n7) {
        return nArray[SharedBlockHighlightCache.prefixIndex(n5, n6, n7, n)] - nArray[SharedBlockHighlightCache.prefixIndex(n2, n6, n7, n)] - nArray[SharedBlockHighlightCache.prefixIndex(n5, n3, n7, n)] - nArray[SharedBlockHighlightCache.prefixIndex(n5, n6, n4, n)] + nArray[SharedBlockHighlightCache.prefixIndex(n2, n3, n7, n)] + nArray[SharedBlockHighlightCache.prefixIndex(n2, n6, n4, n)] + nArray[SharedBlockHighlightCache.prefixIndex(n5, n3, n4, n)] - nArray[SharedBlockHighlightCache.prefixIndex(n2, n3, n4, n)];
    }

    private static int prefixIndex(int n, int n2, int n3, int n4) {
        return (n * n4 + n2) * n4 + n3;
    }

    private static int clampTunnelNeighborCount(int n) {
        return Math.max(1, Math.min(124, n));
    }

    private static long key(int n, int n2) {
        return (long)n << 32 | (long)n2 & 0xFFFFFFFFL;
    }

    public static interface UpdateListener {
        public void onBlockChanged(BlockPos var1, IBlockState var2);

        public void onChunkQueued(int var1, int var2);

        public void onChunkRemoved(int var1, int var2);

        public void onCacheCleared();
    }
}

