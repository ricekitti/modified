/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package valen.utility;

import net.minecraft.client.Minecraft;
import valen.event.ReceivePacketEvent;
import valen.lunar.forge.event.entity.EntityJoinWorldEvent;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.module.ModuleManager;
import valen.utility.SharedBlockHighlightCache;
import valen.utility.Utils;

public final class BlockHighlightSharedHandler {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
        SharedBlockHighlightCache.get().handleReceivePacket(receivePacketEvent);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        SharedBlockHighlightCache sharedBlockHighlightCache = SharedBlockHighlightCache.get();
        if (!sharedBlockHighlightCache.anyConsumerActive()) {
            return;
        }
        int n = 0;
        if (ModuleManager.blockESP != null) {
            n = Math.max(n, ModuleManager.blockESP.getScanSpeedBudget());
        }
        if (ModuleManager.bedESP != null) {
            n = Math.max(n, ModuleManager.bedESP.getScanSpeedBudget());
        }
        if (ModuleManager.tunnels != null) {
            n = Math.max(n, ModuleManager.tunnels.getScanSpeedBudget());
        }
        if (n > 0) {
            sharedBlockHighlightCache.tickScan(n);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent entityJoinWorldEvent) {
        if (entityJoinWorldEvent.entity != BlockHighlightSharedHandler.mc.thePlayer) {
            return;
        }
        SharedBlockHighlightCache sharedBlockHighlightCache = SharedBlockHighlightCache.get();
        if (!sharedBlockHighlightCache.anyConsumerActive()) {
            return;
        }
        sharedBlockHighlightCache.clear();
        sharedBlockHighlightCache.enqueueLoadedChunks();
    }
}

