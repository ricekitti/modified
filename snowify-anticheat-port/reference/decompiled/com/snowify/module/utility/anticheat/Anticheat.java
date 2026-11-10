/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.server.S22PacketMultiBlockChange
 *  net.minecraft.network.play.server.S22PacketMultiBlockChange$BlockUpdateData
 *  net.minecraft.network.play.server.S23PacketBlockChange
 *  net.minecraft.scoreboard.ScorePlayerTeam
 *  net.minecraft.scoreboard.Team
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 */
package com.snowify.module.utility.anticheat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snowify.Snowify;
import com.snowify.config.Configuration;
import com.snowify.config.Property;
import com.snowify.core.event.EntityJoinWorldEvent;
import com.snowify.core.event.RenderGameOverlayEvent;
import com.snowify.core.event.SubscribeEvent;
import com.snowify.core.event.TickEvent;
import com.snowify.gui.Draw;
import com.snowify.gui.HudEditor;
import com.snowify.gui.HudMovable;
import com.snowify.gui.Theme;
import com.snowify.module.utility.anticheat.BreakCheck;
import com.snowify.util.Conditions;
import com.snowify.util.Gradient;
import com.snowify.util.InboundPackets;
import com.snowify.util.Teams;
import com.snowify.util.font.SmoothFont;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public final class Anticheat
implements HudMovable {
    public static final String CAT = "anticheat";
    private static final String NAME = "Anticheat";
    private static final String AUTOBLOCK = "Autoblock";
    private static final String KEEP_Y = "Keep-Y";
    private static final String TOWER = "Tower";
    private static final String BRIDGE_ASSIST = "Bridge Assist";
    private static final String STASIS = "Stasis";
    private static final long POPUP_MS = 5000L;
    private static Anticheat instance;
    private final Property enabled;
    private final Property flagInterval;
    private final Property ignoreTeammates;
    private final Property shouldPing;
    private final Property tabIcon;
    private final Property threshold;
    private final Property popup;
    private final Property nametagMark;
    private final Property blacklistWarn;
    private final Property autoblock;
    private final Property keepY;
    private final Property tower;
    private final Property bridgeAssist;
    private final Property stasis;
    private final Property fastMine;
    private final Property noBreakDelay;
    private final BreakCheck breaks = new BreakCheck();
    private final Property hudX;
    private final Property hudY;
    private final Map<UUID, PlayerData> players = new HashMap<UUID, PlayerData>();
    private final Map<UUID, Cheater> cheaters = new LinkedHashMap<UUID, Cheater>();
    private boolean loaded;
    private final Set<UUID> announced = new HashSet<UUID>();
    private long lastPing;
    private String popupName;
    private String popupFlags;
    private long popupAt;
    private final Queue<BlockPos> placeEvents = new ConcurrentLinkedQueue<BlockPos>();
    private static final double SCAFFOLD_VL_THRESHOLD = 4.0;
    private static final int KY_WINDOW = 14;
    private static final int KEEP_Y_SUSTAIN = 12;
    private static final int STASIS_WINDOW = 4;
    private static final double STASIS_EPS = 0.05;
    private static final int STASIS_FLAG_EVERY = 10;

    public Anticheat(Configuration configuration) {
        instance = this;
        this.enabled = configuration.get(CAT, "Enabled", false, "Tries to detect cheaters among the other players.");
        Property oldTicks = configuration.getCategory(CAT).remove("Flag interval (ticks)");
        this.flagInterval = configuration.get(CAT, "Flag interval", 20, "Seconds before the same check is told again for a player.", 0, 60);
        if (oldTicks != null) {
            this.flagInterval.set(Math.max(0, Math.min(60, oldTicks.getInt(400) / 20)));
        }
        this.ignoreTeammates = configuration.get(CAT, "Ignore teammates", false, "Never flags your teammates.");
        this.shouldPing = configuration.get(CAT, "Should ping", true, "A ping sound with a flag.");
        this.tabIcon = configuration.get(CAT, "Tab Icon", true, "A mark by cheaters' names in the tab list.");
        this.threshold = configuration.get(CAT, "Cheater threshold", 5, "Flags (of all checks together) before a player is marked as cheating.", 1, 20);
        this.popup = configuration.get(CAT, "Cheater popup", true, "A popup on the screen when a player is marked as cheating.");
        this.nametagMark = configuration.get(CAT, "Nametag mark", true, "A red \"!\" in front of a cheater's name on their nametag.");
        this.blacklistWarn = configuration.get(CAT, "Blacklist warning", true, "When a player on your saved blacklist - flagged in an earlier game - is seen again, a warning in chat and a quiet sound. The blacklist is kept between games.");
        this.autoblock = configuration.get(CAT, AUTOBLOCK, true, "Hitting while blocking with a sword (10 ticks on end).");
        Property oldScaffold = configuration.getCategory(CAT).remove("Scaffold");
        boolean scaffoldOn = oldScaffold == null || oldScaffold.getBoolean(true);
        this.keepY = configuration.get(CAT, KEEP_Y, scaffoldOn, "Bridging backwards at speed while jumping along at one height: looking down, facing away from where they go, placing blocks - scored each tick, flagged quickly.");
        this.tower = configuration.get(CAT, TOWER, scaffoldOn, "Going up on their own blocks faster than jumping allows, facing away from where they go - scored each tick, flagged quickly.");
        this.bridgeAssist = configuration.get(CAT, BRIDGE_ASSIST, true, "Sneak taps while bridging too short and too regular for a hand.");
        this.stasis = configuration.get(CAT, STASIS, true, "Placing blocks while frozen in the air with no motion at all (a stasis / freeze cheat).");
        this.fastMine = configuration.get(CAT, "Fast Mine", true, "Blocks broken much sooner than the tool in hand allows (Haste II always allowed for).");
        this.noBreakDelay = configuration.get(CAT, "No Break Delay", true, "The next block begun right after one breaks, without the wait a hand has, again and again.");
        this.hudX = configuration.get(CAT, "Popup x", 0.5, "", 0.0, 1.0);
        this.hudY = configuration.get(CAT, "Popup y", 0.12, "", 0.0, 1.0);
        InboundPackets.watch(new InboundPackets.Watcher(){

            @Override
            public void seen(Packet<?> packet) {
                if (Anticheat.this.enabled.getBoolean() && (Anticheat.this.fastMine.getBoolean() || Anticheat.this.noBreakDelay.getBoolean())) {
                    Anticheat.this.breaks.onPacket(packet);
                }
                if (Anticheat.this.enabled.getBoolean() && (Anticheat.this.keepY.getBoolean() || Anticheat.this.stasis.getBoolean())) {
                    if (packet instanceof S23PacketBlockChange) {
                        S23PacketBlockChange p = (S23PacketBlockChange)packet;
                        Anticheat.this.noteBlock(p.func_179827_b(), p.func_180728_a());
                    } else if (packet instanceof S22PacketMultiBlockChange) {
                        for (S22PacketMultiBlockChange.BlockUpdateData u : ((S22PacketMultiBlockChange)packet).func_179844_a()) {
                            Anticheat.this.noteBlock(u.func_180090_a(), u.func_180088_c());
                        }
                    }
                }
            }
        });
    }

    public Property enabledProp() {
        return this.enabled;
    }

    public Property flagIntervalProp() {
        return this.flagInterval;
    }

    public Property ignoreTeammatesProp() {
        return this.ignoreTeammates;
    }

    public Property shouldPingProp() {
        return this.shouldPing;
    }

    public Property tabIconProp() {
        return this.tabIcon;
    }

    public Property thresholdProp() {
        return this.threshold;
    }

    public Property popupProp() {
        return this.popup;
    }

    public Property nametagMarkProp() {
        return this.nametagMark;
    }

    public Property blacklistWarnProp() {
        return this.blacklistWarn;
    }

    public Property autoblockProp() {
        return this.autoblock;
    }

    public Property keepYProp() {
        return this.keepY;
    }

    public Property towerProp() {
        return this.tower;
    }

    public Property bridgeAssistProp() {
        return this.bridgeAssist;
    }

    public Property stasisProp() {
        return this.stasis;
    }

    public Property fastMineProp() {
        return this.fastMine;
    }

    public Property noBreakDelayProp() {
        return this.noBreakDelay;
    }

    public static boolean marksNametag(EntityPlayer player) {
        Anticheat self = instance;
        return self != null && self.enabled.getBoolean() && self.nametagMark.getBoolean() && player != null && self.isCheater(player.func_110124_au(), player.func_70005_c_());
    }

    public static String decorateTab(NetworkPlayerInfo info, String name) {
        Anticheat self = instance;
        if (!(self != null && self.enabled.getBoolean() && self.tabIcon.getBoolean() && info != null && name != null && self.isCheater(info.func_178845_a().getId(), info.func_178845_a().getName()))) {
            return name;
        }
        return "\u00a7c\u26a0 \u00a7r" + name;
    }

    private boolean isCheater(UUID uuid, String name) {
        return this.cheaterFor(uuid, name) != null;
    }

    public static boolean isCheater(String name) {
        Anticheat self = instance;
        return self != null && name != null && self.cheaterFor(null, name) != null;
    }

    public static boolean blacklist(String name, String reason) {
        Anticheat self = instance;
        if (self == null || name == null || name.trim().isEmpty()) {
            return false;
        }
        name = name.trim();
        self.load();
        Cheater cheater = self.cheaterFor(null, name);
        if (cheater == null) {
            UUID uuid = UUID.nameUUIDFromBytes(("snowify-manual:" + name.toLowerCase()).getBytes(Charset.forName("UTF-8")));
            cheater = new Cheater(uuid, name);
            self.cheaters.put(uuid, cheater);
        }
        cheater.name = name;
        cheater.reason = reason == null || reason.trim().isEmpty() ? "cheating" : reason.trim();
        self.save();
        return true;
    }

    public static boolean unblacklist(String name) {
        Anticheat self = instance;
        if (self == null || name == null) {
            return false;
        }
        self.load();
        Iterator<Cheater> it = self.cheaters.values().iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (!name.equalsIgnoreCase(it.next().name)) continue;
            it.remove();
            removed = true;
        }
        if (removed) {
            self.save();
        }
        return removed;
    }

    private Cheater cheaterFor(UUID uuid, String name) {
        Cheater byId;
        this.load();
        if (uuid != null && (byId = this.cheaters.get(uuid)) != null) {
            return byId;
        }
        if (name != null && !name.isEmpty()) {
            for (Cheater cheater : this.cheaters.values()) {
                if (!name.equalsIgnoreCase(cheater.name)) continue;
                return cheater;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent event) {
        Cheater cheater;
        EntityPlayer player;
        if (event.entity == Minecraft.func_71410_x().field_71439_g) {
            this.players.clear();
            this.announced.clear();
            this.breaks.clear();
            return;
        }
        if (this.enabled.getBoolean() && event.entity instanceof EntityPlayer && !this.announced.contains((player = (EntityPlayer)event.entity).func_110124_au()) && (cheater = this.cheaterFor(player.func_110124_au(), player.func_70005_c_())) != null) {
            this.announced.add(player.func_110124_au());
            Conditions.message(NAME, "\u00a7l" + player.func_70005_c_() + " \u00a7cis blacklisted \u00a77(" + cheater.describe() + ")");
            if (this.popup.getBoolean()) {
                this.popupName = player.func_70005_c_();
                this.popupFlags = cheater.describe();
                this.popupAt = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        boolean justYou;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x();
        boolean bl = justYou = mc.func_71356_B() && mc.field_71441_e != null && mc.field_71441_e.field_73010_i.size() <= 1;
        if (!this.enabled.getBoolean() || mc.field_71439_g == null || mc.field_71441_e == null || justYou) {
            return;
        }
        this.load();
        this.attributePlacements(mc);
        HashSet<UUID> present = new HashSet<UUID>();
        for (EntityPlayer player : new ArrayList(mc.field_71441_e.field_73010_i)) {
            String check;
            if (player == null || player == mc.field_71439_g || Anticheat.ignored(mc, player)) continue;
            UUID id = player.func_110124_au();
            present.add(id);
            this.announcePrevious(mc, player);
            PlayerData data = this.players.get(id);
            if (data == null) {
                data = new PlayerData();
                this.players.put(id, data);
            }
            this.record(player, data);
            if (this.autoblock.getBoolean() && data.autoblockTicks >= 10) {
                this.flag(mc, player, AUTOBLOCK, 1);
                data.autoblockTicks = 0;
            } else if (this.bridgeAssist.getBoolean() && Anticheat.bridgeAssist(player, data)) {
                this.flag(mc, player, BRIDGE_ASSIST, 3);
            } else if ((this.keepY.getBoolean() || this.tower.getBoolean()) && (check = this.scaffolding(player, data)) != null) {
                this.flag(mc, player, check, 1);
            }
            if (this.stasis.getBoolean() && this.inStasis(player, data)) {
                if (++data.stasisTicks % 10 != 0) continue;
                this.flag(mc, player, STASIS, 1);
                continue;
            }
            data.stasisTicks = 0;
        }
        this.players.keySet().retainAll(present);
        final Minecraft m = mc;
        this.breaks.process(mc, this.fastMine.getBoolean(), this.noBreakDelay.getBoolean(), new BreakCheck.Flagger(){

            @Override
            public void flag(EntityPlayer player, String check, int needed) {
                if (!Anticheat.ignored(m, player)) {
                    Anticheat.this.flag(m, player, check, needed);
                }
            }
        });
    }

    private static boolean ignored(Minecraft mc, EntityPlayer player) {
        return mc.func_147114_u() == null || mc.func_147114_u().func_175102_a(player.func_110124_au()) == null;
    }

    private void record(EntityPlayer player, PlayerData data) {
        boolean block = player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() instanceof ItemBlock;
        boolean placing = block && player.field_82175_bq && player.field_110158_av == 1;
        data.samples.addLast(new double[]{player.field_70165_t, player.field_70163_u, player.field_70161_v, player.field_70122_E ? 1.0 : 0.0, player.field_70177_z, player.field_70125_A, block ? 1.0 : 0.0, placing ? 1.0 : 0.0});
        while (data.samples.size() > 40) {
            data.samples.removeFirst();
        }
        data.autoblockTicks = player.field_82175_bq && player.func_70632_aY() ? data.autoblockTicks + 1 : 0;
    }

    private String scaffolding(EntityPlayer player, PlayerData data) {
        if (player.func_70115_ae() || data.samples.size() < 4) {
            return null;
        }
        double[][] s = (double[][])data.samples.toArray((T[])new double[0][]);
        int n = s.length;
        double[] h0 = s[n - 4];
        double[] h1 = s[n - 3];
        double[] h2 = s[n - 2];
        double[] h3 = s[n - 1];
        int tick = player.field_70173_aa;
        double pitch = player.field_70125_A;
        double dx = (player.field_70165_t - player.field_70142_S) * 20.0;
        double dz = (player.field_70161_v - player.field_70136_U) * 20.0;
        double speedXZsq = dx * dx + dz * dz;
        double speedXZ = Math.sqrt(speedXZsq);
        double speedY = (h3[1] - h2[1]) * 20.0;
        double avgAccelY = 50.0 * (h0[1] - h1[1] - h2[1] + h3[1]);
        double angleDiff = Math.abs(Anticheat.movementYawOffset(player.field_70165_t - player.field_70142_S, player.field_70161_v - player.field_70136_U, player.field_70177_z));
        String type = "";
        double vl = 0.0;
        if (player.field_82175_bq && player.field_70737_aN == 0 && pitch > 45.0 && speedXZsq > 9.0 && player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() instanceof ItemBlock && angleDiff > 158.0 && speedXZsq < 100.0 && Math.abs(avgAccelY) >= 0.001) {
            double severity;
            double pitchFactor = Anticheat.clamp((pitch - 45.0) / 40.0);
            double angleFactor = Anticheat.clamp((angleDiff - 158.0) / 22.0);
            if (speedY >= 4.0 && speedY <= 15.0 && avgAccelY > -25.0 && speedXZsq > 25.0) {
                type = "tower";
                double ySpeedFactor = Anticheat.clamp((speedY - 4.0) / 11.0);
                double accelFactor = Anticheat.clamp((avgAccelY + 25.0) / 25.0);
                severity = Anticheat.clamp(pitchFactor * 0.3 + angleFactor * 0.3 + ySpeedFactor * 0.3 + accelFactor * 0.1);
                vl = 3.0 + severity * 3.0;
            } else if (speedY >= -1.0 && speedY <= 4.0 && Math.abs(speedY) > 0.005 && speedXZsq > 25.0) {
                type = "keep-y";
                double hSpeedFactor = Anticheat.clamp((speedXZ - 5.0) / 5.0);
                severity = Anticheat.clamp(pitchFactor * 0.3 + angleFactor * 0.3 + hSpeedFactor * 0.4);
                vl = 3.0 + severity * 3.0;
            }
        }
        String flagged = null;
        for (int c = 0; c < 2; ++c) {
            int consecutive;
            if (!(c == 0 ? this.keepY : this.tower).getBoolean()) continue;
            if (!type.equals(c == 0 ? "keep-y" : "tower")) {
                boolean reset = Anticheat.decayScaffold(data, c);
                if (!reset || tick - data.scaffoldLastTick[c] <= 60) continue;
                data.scaffoldConsecutive[c] = 0;
                continue;
            }
            data.scaffoldConsecutive[c] = consecutive = tick - data.scaffoldLastTick[c] < 40 ? data.scaffoldConsecutive[c] + 1 : 0;
            data.scaffoldLastTick[c] = tick;
            int n2 = c;
            data.scaffoldVl[n2] = data.scaffoldVl[n2] + vl * (1.0 + (double)Math.min(consecutive, 5) * 0.2);
            if (!(data.scaffoldVl[c] >= 4.0)) continue;
            data.scaffoldVl[c] = 0.0;
            flagged = c == 0 ? KEEP_Y : TOWER;
        }
        if (this.keepY.getBoolean() && player.field_70737_aN == 0 && player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() instanceof ItemBlock && this.keepYMotion(player, data)) {
            if (++data.keepYTicks >= 12) {
                data.keepYTicks = 0;
                if (flagged == null) {
                    flagged = KEEP_Y;
                }
            }
        } else {
            data.keepYTicks = Math.max(0, data.keepYTicks - 3);
        }
        return flagged;
    }

    private boolean keepYMotion(EntityPlayer player, PlayerData data) {
        if (player.func_70115_ae()) {
            return false;
        }
        double[][] s = (double[][])data.samples.toArray((T[])new double[0][]);
        int n = s.length;
        if (n < 14) {
            return false;
        }
        int start = n - 14;
        double minY = Double.MAX_VALUE;
        double maxY = -1.7976931348623157E308;
        for (int i = start; i < n; ++i) {
            minY = Math.min(minY, s[i][1]);
            maxY = Math.max(maxY, s[i][1]);
            if (!(s[i][6] < 0.5)) continue;
            return false;
        }
        if (maxY - minY > 0.9) {
            return false;
        }
        double dxTot = s[n - 1][0] - s[start][0];
        double dzTot = s[n - 1][2] - s[start][2];
        double horiz = Math.sqrt(dxTot * dxTot + dzTot * dzTot);
        if (horiz < 3.0) {
            return false;
        }
        double speed = horiz / 0.65;
        if (speed < 4.0 || !Anticheat.overGap(player)) {
            return false;
        }
        int placed = 0;
        for (int t : data.placeTicks) {
            int age = player.field_70173_aa - t;
            if (age < 0 || age > 18) continue;
            ++placed;
        }
        if (placed < 2) {
            return false;
        }
        return Anticheat.standsOnHeldBlock(player) && Anticheat.bridgingAhead(player);
    }

    private static boolean standsOnHeldBlock(EntityPlayer player) {
        ItemStack held = player.func_70694_bm();
        if (held == null || !(held.func_77973_b() instanceof ItemBlock)) {
            return false;
        }
        Minecraft mc = Minecraft.func_71410_x();
        if (mc.field_71441_e == null) {
            return false;
        }
        Block heldBlock = ((ItemBlock)held.func_77973_b()).func_179223_d();
        int x = MathHelper.func_76128_c((double)player.field_70165_t);
        int y = MathHelper.func_76128_c((double)player.field_70163_u);
        int z = MathHelper.func_76128_c((double)player.field_70161_v);
        for (int d = 1; d <= 3; ++d) {
            Block below = mc.field_71441_e.func_180495_p(new BlockPos(x, y - d, z)).func_177230_c();
            if (below.func_149688_o() == Material.field_151579_a) continue;
            return below == heldBlock;
        }
        return false;
    }

    private static boolean bridgingAhead(EntityPlayer player) {
        Minecraft mc = Minecraft.func_71410_x();
        if (mc.field_71441_e == null) {
            return false;
        }
        double dx = player.field_70165_t - player.field_70142_S;
        double dz = player.field_70161_v - player.field_70136_U;
        double lenSq = dx * dx + dz * dz;
        if (lenSq < 1.0E-6) {
            return false;
        }
        double len = Math.sqrt(lenSq);
        int ax = MathHelper.func_76128_c((double)(player.field_70165_t + dx / len * 2.0));
        int az = MathHelper.func_76128_c((double)(player.field_70161_v + dz / len * 2.0));
        int y = MathHelper.func_76128_c((double)player.field_70163_u);
        return mc.field_71441_e.func_175623_d(new BlockPos(ax, y - 1, az)) && mc.field_71441_e.func_175623_d(new BlockPos(ax, y, az));
    }

    private void noteBlock(BlockPos pos, IBlockState state) {
        Block b = state.func_177230_c();
        if (b.func_149688_o() != Material.field_151579_a && b.func_149688_o().func_76230_c() && b.func_149686_d()) {
            this.placeEvents.add(pos);
        }
    }

    private void attributePlacements(Minecraft mc) {
        BlockPos pos;
        if (mc.field_71441_e == null) {
            return;
        }
        while ((pos = this.placeEvents.poll()) != null) {
            PlayerData data;
            EntityPlayer nearest = null;
            double best = 36.0;
            double bx = (double)pos.func_177958_n() + 0.5;
            double bz = (double)pos.func_177952_p() + 0.5;
            for (EntityPlayer p : mc.field_71441_e.field_73010_i) {
                double dy;
                double dz;
                double dx;
                double d;
                if (p == mc.field_71439_g || !((d = (dx = p.field_70165_t - bx) * dx + (dz = p.field_70161_v - bz) * dz + (dy = p.field_70163_u - (double)pos.func_177956_o()) * dy * 0.25) < best)) continue;
                best = d;
                nearest = p;
            }
            if (nearest == null || (data = this.players.get(nearest.func_110124_au())) == null) continue;
            data.placeTicks.addLast(nearest.field_70173_aa);
            while (data.placeTicks.size() > 40) {
                data.placeTicks.removeFirst();
            }
        }
    }

    private static boolean overGap(EntityPlayer player) {
        Minecraft mc = Minecraft.func_71410_x();
        if (mc.field_71441_e == null) {
            return false;
        }
        int x = MathHelper.func_76128_c((double)player.field_70165_t);
        int z = MathHelper.func_76128_c((double)player.field_70161_v);
        int y = MathHelper.func_76128_c((double)player.field_70163_u);
        int solid = 0;
        for (int d = 1; d <= 6; ++d) {
            if (mc.field_71441_e.func_175623_d(new BlockPos(x, y - d, z))) continue;
            ++solid;
        }
        return solid <= 1;
    }

    private boolean inStasis(EntityPlayer player, PlayerData data) {
        if (player.func_70115_ae() || player.func_70694_bm() == null || !(player.func_70694_bm().func_77973_b() instanceof ItemBlock)) {
            return false;
        }
        if (Anticheat.hasSupportBelow(player)) {
            return false;
        }
        double[][] s = (double[][])data.samples.toArray((T[])new double[0][]);
        int n = s.length;
        if (n < 4) {
            return false;
        }
        double dx = s[n - 1][0] - s[n - 4][0];
        double dy = s[n - 1][1] - s[n - 4][1];
        double dz = s[n - 1][2] - s[n - 4][2];
        if (Math.abs(dx) > 0.05 || Math.abs(dy) > 0.05 || Math.abs(dz) > 0.05) {
            return false;
        }
        for (int t : data.placeTicks) {
            int age = player.field_70173_aa - t;
            if (age < 0 || age > 10) continue;
            return true;
        }
        return false;
    }

    private static boolean hasSupportBelow(EntityPlayer player) {
        Minecraft mc = Minecraft.func_71410_x();
        if (mc.field_71441_e == null) {
            return true;
        }
        AxisAlignedBB box = player.func_174813_aQ();
        int y = MathHelper.func_76128_c((double)(box.field_72338_b - 0.1));
        int minX = MathHelper.func_76128_c((double)box.field_72340_a);
        int maxX = MathHelper.func_76128_c((double)box.field_72336_d);
        int minZ = MathHelper.func_76128_c((double)box.field_72339_c);
        int maxZ = MathHelper.func_76128_c((double)box.field_72334_f);
        for (int bx = minX; bx <= maxX; ++bx) {
            for (int bz = minZ; bz <= maxZ; ++bz) {
                if (mc.field_71441_e.func_175623_d(new BlockPos(bx, y, bz))) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean decayScaffold(PlayerData data, int c) {
        if (data.scaffoldVl[c] <= 0.0) {
            return false;
        }
        double share = data.scaffoldVl[c] / 4.0;
        double rate = share > 0.8 ? 0.75 : (share > 0.5 ? 0.6 : (share > 0.25 ? 0.5 : 0.3));
        data.scaffoldVl[c] = Math.max(0.0, data.scaffoldVl[c] - rate);
        return data.scaffoldVl[c] <= 0.8;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double movementYawOffset(double dx, double dz, float yaw) {
        double moving = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double offset = (moving - (double)yaw) % 360.0;
        if (offset < -180.0) {
            offset += 360.0;
        } else if (offset > 180.0) {
            offset -= 360.0;
        }
        return offset;
    }

    private static boolean bridgeAssist(EntityPlayer player, PlayerData data) {
        boolean placedThen;
        int tick = player.field_70173_aa;
        boolean sneaking = player.func_70093_af();
        if (sneaking && !data.wasSneaking) {
            data.sneakStart = tick;
        }
        if (!sneaking && data.wasSneaking) {
            data.lastUnsneak = tick;
        }
        data.wasSneaking = sneaking;
        if (player.field_82175_bq && player.field_110158_av != data.lastSwingProgress && player.field_110158_av == 1) {
            data.lastSwing = tick;
        }
        data.lastSwingProgress = player.field_110158_av;
        boolean justUnsneaked = !sneaking && data.lastUnsneak > 0 && tick - data.lastUnsneak <= 3 && data.lastCountedUnsneak != data.lastUnsneak;
        boolean blocks = player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() instanceof ItemBlock;
        int held = data.lastUnsneak - data.sneakStart;
        boolean tap = held >= 1 && held <= 2;
        boolean bl = placedThen = data.lastSwing >= data.lastUnsneak - 1 && data.lastSwing <= data.lastUnsneak + 1;
        if (player.field_70125_A >= 70.0f && blocks && player.field_70122_E) {
            if (justUnsneaked && tap && placedThen) {
                data.lastCountedUnsneak = data.lastUnsneak;
                if (tick - data.lastPairTick > 15) {
                    data.pairCount = 0;
                }
                ++data.pairCount;
                data.lastPairTick = tick;
                if (data.pairCount >= 2) {
                    data.pairCount = 0;
                    return true;
                }
            }
            return false;
        }
        if (tick - data.lastPairTick > 15) {
            data.pairCount = 0;
        }
        return false;
    }

    private void flag(Minecraft mc, EntityPlayer player, String check, int needed) {
        if (this.ignoreTeammates.getBoolean() && Teams.isTeammate(mc, player)) {
            return;
        }
        PlayerData data = this.players.get(player.func_110124_au());
        if (data == null) {
            data = new PlayerData();
            this.players.put(player.func_110124_au(), data);
        }
        int count = data.flags.containsKey(check) ? data.flags.get(check) + 1 : 1;
        data.flags.put(check, count);
        if (count < needed) {
            return;
        }
        long now = System.currentTimeMillis();
        int total = 0;
        for (int c : data.flags.values()) {
            total += c;
        }
        boolean wasCheater = this.isCheater(player.func_110124_au(), player.func_70005_c_());
        if (total >= this.threshold.getInt(5) && !wasCheater) {
            this.markCheater(mc, player, data);
        } else if (wasCheater) {
            this.remember(player, data);
        }
        long interval = (long)this.flagInterval.getInt(20) * 1000L;
        Long last = data.lastFlagAt.get(check);
        if (interval > 0L && last != null && now - last <= interval) {
            return;
        }
        data.lastFlagAt.put(check, now);
        Conditions.message(NAME, Anticheat.teamName(mc, player) + " \u00a77flagged \u00a7f" + check + " \u00a77\u00a7ox" + count);
        if (this.shouldPing.getBoolean() && now - this.lastPing >= 1500L) {
            this.lastPing = now;
            mc.field_71439_g.func_85030_a("note.pling", 1.0f, 1.0f);
        }
    }

    private static String teamName(Minecraft mc, EntityPlayer player) {
        if (mc.field_71441_e == null) {
            return player.func_70005_c_();
        }
        ScorePlayerTeam team = mc.field_71441_e.func_96441_U().func_96509_i(player.func_70005_c_());
        return ScorePlayerTeam.func_96667_a((Team)team, (String)player.func_70005_c_());
    }

    private void markCheater(Minecraft mc, EntityPlayer player, PlayerData data) {
        Cheater cheater = this.remember(player, data);
        this.announced.add(player.func_110124_au());
        Conditions.message(NAME, "\u00a7l" + Anticheat.teamName(mc, player) + " \u00a7cis cheating \u00a77(" + cheater.describe() + ")");
        if (this.popup.getBoolean()) {
            this.popupName = player.func_70005_c_();
            this.popupFlags = cheater.describe();
            this.popupAt = System.currentTimeMillis();
        }
        mc.field_71439_g.func_85030_a("note.pling", 1.0f, 0.5f);
    }

    private Cheater remember(EntityPlayer player, PlayerData data) {
        Cheater cheater = this.cheaters.get(player.func_110124_au());
        if (cheater == null) {
            cheater = new Cheater(player.func_110124_au(), player.func_70005_c_());
            this.cheaters.put(player.func_110124_au(), cheater);
        }
        cheater.name = player.func_70005_c_();
        for (Map.Entry<String, Integer> flag : data.flags.entrySet()) {
            Integer before = cheater.flags.get(flag.getKey());
            cheater.flags.put(flag.getKey(), Math.max(before == null ? 0 : before, flag.getValue()));
        }
        this.save();
        return cheater;
    }

    private void announcePrevious(Minecraft mc, EntityPlayer player) {
        UUID id = player.func_110124_au();
        if (this.announced.contains(id)) {
            return;
        }
        Cheater cheater = this.cheaterFor(id, player.func_70005_c_());
        if (cheater != null) {
            this.announced.add(id);
            Conditions.message(NAME, "\u00a7c\u26a0 \u00a7f" + player.func_70005_c_() + " \u00a7cis on your blacklist \u00a77- previously flagged: \u00a7f" + cheater.describe());
            if (this.blacklistWarn.getBoolean() && this.shouldPing.getBoolean() && System.currentTimeMillis() - this.lastPing >= 1500L) {
                this.lastPing = System.currentTimeMillis();
                mc.field_71439_g.func_85030_a("note.pling", 1.0f, 0.5f);
            }
        }
    }

    private static File file() {
        return new File(new File(Minecraft.func_71410_x().field_71412_D, "snowify"), "anticheat_blacklist.json");
    }

    private void load() {
        if (this.loaded) {
            return;
        }
        this.loaded = true;
        File file = Anticheat.file();
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file);){
            JsonObject root = new JsonParser().parse((Reader)reader).getAsJsonObject();
            JsonArray list = root.has("players") && root.get("players").isJsonArray() ? root.getAsJsonArray("players") : new JsonArray();
            for (JsonElement element : list) {
                JsonObject entry = element.getAsJsonObject();
                UUID uuid = UUID.fromString(entry.get("uuid").getAsString());
                Cheater cheater = new Cheater(uuid, entry.has("name") ? entry.get("name").getAsString() : "");
                if (entry.has("reason")) {
                    cheater.reason = entry.get("reason").getAsString();
                }
                if (entry.has("flags") && entry.get("flags").isJsonObject()) {
                    for (Map.Entry flag : entry.getAsJsonObject("flags").entrySet()) {
                        cheater.flags.put((String)flag.getKey(), ((JsonElement)flag.getValue()).getAsInt());
                    }
                }
                this.cheaters.put(uuid, cheater);
            }
        }
        catch (Exception e) {
            this.cheaters.clear();
        }
    }

    private void save() {
        File file = Anticheat.file();
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }
        JsonObject root = new JsonObject();
        JsonArray list = new JsonArray();
        for (Cheater cheater : this.cheaters.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("uuid", cheater.uuid.toString());
            entry.addProperty("name", cheater.name);
            if (cheater.reason != null) {
                entry.addProperty("reason", cheater.reason);
            }
            JsonObject flags = new JsonObject();
            for (Map.Entry<String, Integer> flag : cheater.flags.entrySet()) {
                flags.addProperty(flag.getKey(), (Number)flag.getValue());
            }
            entry.add("flags", (JsonElement)flags);
            list.add((JsonElement)entry);
        }
        root.add("players", (JsonElement)list);
        try (FileWriter writer = new FileWriter(file);){
            new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)root, (Appendable)writer);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.func_71410_x();
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || this.popupName == null || mc.field_71474_y.field_74330_P || mc.field_71462_r instanceof HudEditor) {
            return;
        }
        long age = System.currentTimeMillis() - this.popupAt;
        if (age > 5000L) {
            this.popupName = null;
            return;
        }
        FontRenderer fr = Anticheat.font();
        int[] size = Anticheat.size(fr, this.popupName, this.popupFlags);
        int[] at = this.placeBox(event.resolution.func_78326_a(), event.resolution.func_78328_b(), size[0], size[1]);
        float in = age < 300L ? (float)age / 300.0f : (age > 4700L ? (float)(5000L - age) / 300.0f : 1.0f);
        float eased = 1.0f - (1.0f - in) * (1.0f - in) * (1.0f - in);
        int y = Math.round((float)(-size[1]) + (float)(at[1] + size[1]) * eased);
        Anticheat.drawPopup(fr, at[0], y, size[0], size[1], this.popupName, this.popupFlags, 1.0f);
    }

    private static FontRenderer font() {
        return Snowify.instance.getGuiConfig().noticeFont();
    }

    private static int[] size(FontRenderer fr, String name, String flags) {
        String title = "\u26a0 \u00a7lCHEATER DETECTED";
        int width = Math.max(fr.func_78256_a(title), Math.max(fr.func_78256_a(name), fr.func_78256_a(flags)));
        return new int[]{width + 16, 38};
    }

    private static void drawPopup(FontRenderer fr, int x, int y, int w, int h, String name, String flags, float alpha) {
        Theme.refresh();
        int accent = Theme.accent();
        int[] stops = Theme.stops();
        if (Snowify.instance.getGuiConfig().glow()) {
            Draw.glow(x, y, w, h, 5.0f, 8.0f, 0x70000000, null);
            Draw.glow(x, y, w, h, 5.0f, 5.0f, 0x70FFFFFF, stops);
            Draw.round(x, y, w, h, 5.0f, Theme.window() & 0xFFFFFF | 0xE6000000);
            Draw.roundGradient(x, y, w, h, 5.0f, stops, 32);
            Draw.roundVertical(x, y, w, h, 5.0f, 0x1CFFFFFF, 0xFFFFFF);
            Draw.roundOutline(x, y, w, h, 5.0f, 1.0f, -1056964609, stops);
            Draw.glow(x + 3, y + 6, 2.0f, h - 12, 1.0f, 4.0f, -1325400065, stops);
            Draw.roundGradient(x + 3, y + 6, 2.0f, h - 12, 1.0f, stops, 255);
            if (fr instanceof SmoothFont) {
                ((SmoothFont)fr).drawGlow("\u26a0 \u00a7lCHEATER DETECTED", x + 9, y + 4, 0xB4000000 | accent & 0xFFFFFF);
            }
            fr.func_175063_a("\u26a0 \u00a7lCHEATER DETECTED", (float)(x + 9), (float)(y + 4), accent);
            fr.func_175063_a(name, (float)(x + 9), (float)(y + 15), Theme.text());
            fr.func_175063_a(flags, (float)(x + 9), (float)(y + 26), Theme.textDim());
            return;
        }
        Gui.func_73734_a((int)x, (int)y, (int)(x + w), (int)(y + h), (int)Theme.window());
        Gradient.rect(x, y, x + w, y + h, stops, 34);
        Gradient.rect(x, y, x + 2, y + h, x, y, x + w, y + h, stops, 255);
        Gradient.rect(x + 2, y + h - 1, x + w, y + h, x, y, x + w, y + h, stops, 255);
        fr.func_175063_a("\u26a0 \u00a7lCHEATER DETECTED", (float)(x + 7), (float)(y + 4), accent);
        fr.func_175063_a(name, (float)(x + 7), (float)(y + 15), Theme.text());
        fr.func_175063_a(flags, (float)(x + 7), (float)(y + 26), Theme.textDim());
    }

    public void samplePopup() {
        this.popupName = "Steve";
        this.popupFlags = "Scaffold x3, Autoblock x2";
        this.popupAt = System.currentTimeMillis();
    }

    @Override
    public double hudX() {
        return this.hudX.getDouble(0.5);
    }

    @Override
    public double hudY() {
        return this.hudY.getDouble(0.12);
    }

    @Override
    public void setHud(double x, double y) {
        this.hudX.set(x);
        this.hudY.set(y);
    }

    @Override
    public int[] previewSize(FontRenderer fr) {
        return Anticheat.size(Anticheat.font(), "Steve", "Scaffold x3, Autoblock x2");
    }

    @Override
    public void drawPreview(FontRenderer fr, int x, int y) {
        int[] s = this.previewSize(fr);
        Anticheat.drawPopup(Anticheat.font(), x, y, s[0], s[1], "Steve", "Scaffold x3, Autoblock x2", 1.0f);
    }

    private static final class PlayerData {
        final ArrayDeque<double[]> samples = new ArrayDeque();
        final Map<String, Integer> flags = new HashMap<String, Integer>();
        final Map<String, Long> lastFlagAt = new HashMap<String, Long>();
        int autoblockTicks;
        int lastSwingProgress = -1;
        boolean wasSneaking;
        int sneakStart;
        int lastUnsneak;
        int lastCountedUnsneak = Integer.MIN_VALUE;
        int lastSwing = Integer.MIN_VALUE;
        int pairCount;
        int lastPairTick;
        final double[] scaffoldVl = new double[2];
        final int[] scaffoldLastTick = new int[2];
        final int[] scaffoldConsecutive = new int[2];
        final ArrayDeque<Integer> placeTicks = new ArrayDeque();
        int keepYTicks;
        int stasisTicks;

        private PlayerData() {
        }
    }

    private static final class Cheater {
        final UUID uuid;
        String name;
        String reason;
        final Map<String, Integer> flags = new LinkedHashMap<String, Integer>();

        Cheater(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        String describe() {
            StringBuilder out = new StringBuilder();
            if (this.reason != null && !this.reason.isEmpty()) {
                out.append(this.reason);
            }
            for (Map.Entry<String, Integer> flag : this.flags.entrySet()) {
                out.append(out.length() == 0 ? "" : ", ").append(flag.getKey()).append(" x").append(flag.getValue());
            }
            return out.toString();
        }
    }
}
