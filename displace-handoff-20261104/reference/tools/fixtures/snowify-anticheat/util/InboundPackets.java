/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.network.NetHandlerPlayClient
 *  net.minecraft.network.INetHandler
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.server.S01PacketJoinGame
 *  net.minecraft.network.play.server.S07PacketRespawn
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.snowify.util;

import com.snowify.core.event.ClientDisconnectEvent;
import com.snowify.core.event.SubscribeEvent;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InboundPackets {
    private static final Logger LOGGER = LogManager.getLogger((String)"Snowify");
    private static final List<Hold> HOLDS = new CopyOnWriteArrayList<Hold>();
    private static final List<Watcher> WATCHERS = new CopyOnWriteArrayList<Watcher>();
    private static final List<Filter> FILTERS = new CopyOnWriteArrayList<Filter>();
    private static volatile long intercepted;
    private static final Map<String, Integer> HANDLED_WHILE_HELD;

    private InboundPackets() {
    }

    public static Rule of(final Class<? extends Packet> type) {
        return new Rule(){

            @Override
            public boolean matches(Packet<?> packet) {
                return type.isInstance(packet);
            }
        };
    }

    public static void watch(Watcher watcher) {
        WATCHERS.add(watcher);
    }

    public static void unwatch(Watcher watcher) {
        WATCHERS.remove(watcher);
    }

    public static void filter(Filter filter) {
        FILTERS.add(filter);
    }

    public static Hold hold(String name, Rule rule) {
        return InboundPackets.add(new Hold(name, rule, 0));
    }

    public static Hold holdNext(String name, Rule rule) {
        return InboundPackets.add(new Hold(name, rule, 1));
    }

    private static Hold add(Hold hold) {
        HOLDS.add(hold);
        return hold;
    }

    public static void dropAll() {
        for (Hold hold : HOLDS) {
            hold.drop();
        }
    }

    public static List<Hold> active() {
        return HOLDS;
    }

    public static boolean intercept(Packet<?> packet) {
        ++intercepted;
        if ((packet instanceof S01PacketJoinGame || packet instanceof S07PacketRespawn) && !HOLDS.isEmpty()) {
            InboundPackets.dropAll();
        }
        for (Watcher watcher : WATCHERS) {
            try {
                watcher.seen(packet);
            }
            catch (Throwable t) {
                LOGGER.warn("Inbound packet watcher failed on {}", new Object[]{packet.getClass().getSimpleName(), t});
            }
        }
        for (Filter filter : FILTERS) {
            try {
                if (!filter.drop(packet)) continue;
                return true;
            }
            catch (Throwable t) {
                LOGGER.warn("Inbound packet filter failed on {}", new Object[]{packet.getClass().getSimpleName(), t});
            }
        }
        for (Hold hold : HOLDS) {
            if (!hold.offer(packet)) continue;
            return true;
        }
        return false;
    }

    public static long interceptedCount() {
        return intercepted;
    }

    public static void noteHandled(Packet<?> packet) {
        for (Hold hold : HOLDS) {
            if (!hold.has(packet)) continue;
            String name = packet.getClass().getSimpleName();
            int cut = name.indexOf("Packet");
            name = cut > 0 ? name.substring(0, cut) : name;
            Integer before = HANDLED_WHILE_HELD.get(name);
            HANDLED_WHILE_HELD.put(name, before == null ? 1 : before + 1);
            return;
        }
    }

    public static String takeHandledWhileHeld() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Integer> entry : HANDLED_WHILE_HELD.entrySet()) {
            out.append(out.length() == 0 ? "" : ", ").append(entry.getKey()).append('x').append(entry.getValue());
        }
        HANDLED_WHILE_HELD.clear();
        return out.toString();
    }

    static {
        HANDLED_WHILE_HELD = new ConcurrentHashMap<String, Integer>();
    }

    public static final class Hold {
        private final String name;
        private final Rule rule;
        private final int limit;
        private final Queue<Packet<?>> queue = new ConcurrentLinkedQueue();
        private final long startedAt = System.currentTimeMillis();
        private int taken;
        private boolean open = true;

        private Hold(String name, Rule rule, int limit) {
            this.name = name;
            this.rule = rule;
            this.limit = limit;
        }

        private synchronized boolean offer(Packet<?> packet) {
            boolean match;
            if (!this.open || this.limit > 0 && this.taken >= this.limit) {
                return false;
            }
            try {
                match = this.rule.matches(packet);
            }
            catch (Throwable t) {
                LOGGER.warn("Hold '{}' rule failed on {}", new Object[]{this.name, packet.getClass().getSimpleName(), t});
                return false;
            }
            if (!match) {
                return false;
            }
            ++this.taken;
            this.queue.add(packet);
            return true;
        }

        public String name() {
            return this.name;
        }

        boolean has(Packet<?> packet) {
            return this.queue.contains(packet);
        }

        public int size() {
            return this.queue.size();
        }

        public long heldMillis() {
            return System.currentTimeMillis() - this.startedAt;
        }

        public synchronized boolean isActive() {
            return this.open;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void release() {
            Hold hold = this;
            synchronized (hold) {
                this.open = false;
            }
            HOLDS.remove(this);
            this.handleKept();
        }

        public void flush() {
            this.handleKept();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void drop() {
            Hold hold = this;
            synchronized (hold) {
                this.open = false;
            }
            HOLDS.remove(this);
            this.queue.clear();
        }

        private void handleKept() {
            final Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(new Runnable(){

                @Override
                public void run() {
                    Packet packet;
                    NetHandlerPlayClient handler = mc.getNetHandler();
                    while ((packet = (Packet)queue.poll()) != null) {
                        if (handler == null) continue;
                        try {
                            packet.processPacket((INetHandler)handler);
                        }
                        catch (Throwable t) {
                            LOGGER.warn("Held {} failed when released", new Object[]{packet.getClass().getSimpleName(), t});
                        }
                    }
                }
            });
        }
    }

    public static final class Lifecycle {
        @SubscribeEvent
        public void onDisconnect(ClientDisconnectEvent event) {
            InboundPackets.dropAll();
        }
    }

    public static interface Filter {
        public boolean drop(Packet<?> var1);
    }

    public static interface Watcher {
        public void seen(Packet<?> var1);
    }

    public static interface Rule {
        public boolean matches(Packet<?> var1);
    }
}
