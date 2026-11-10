/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.server.S1DPacketEntityEffect
 *  net.minecraft.network.play.server.S1EPacketRemoveEntityEffect
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.World
 */
package com.snowify.module.combat.ignorefatigue;

import com.snowify.config.ConfigCategory;
import com.snowify.config.Configuration;
import com.snowify.config.Property;
import com.snowify.core.event.SubscribeEvent;
import com.snowify.core.event.TickEvent;
import com.snowify.module.player.fastmine.FastMine;
import com.snowify.util.Conditions;
import com.snowify.util.InboundPackets;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class IgnoreFatigue {
    public static final String CAT = "ignorefatigue";
    private static final String OLD_CAT = "antifatigue";
    private static final String NAME = "Ignore Fatigue";
    public static final String OFF = "Off";
    public static final String HOLD_PACKET = "Hold";
    public static final String INCREASE_FASTMINE = "Increase";
    private static final String[] MODES = new String[]{"Off", "Hold", "Increase"};
    private static boolean wasOnItsOwn;
    private static final int FATIGUE;
    private static final long GRACE_MS = 5000L;
    private static IgnoreFatigue instance;
    private final Property mode;
    private final Property speed;
    private final Property debug;
    private BlockPos digPos;
    private int digStartTick;
    private float digSkippedTo;
    private volatile InboundPackets.Hold hold;
    private volatile long expectedEndAt;

    public IgnoreFatigue(Configuration configuration) {
        String carried;
        instance = this;
        boolean wasEnabled = true;
        if (configuration.hasCategory(OLD_CAT)) {
            ConfigCategory old = configuration.getCategory(OLD_CAT);
            if (old.containsKey("Enabled")) {
                wasEnabled = old.get("Enabled").getBoolean(true);
            }
            configuration.removeCategory(old);
        }
        ConfigCategory own = configuration.getCategory(CAT);
        Property oldEnabled = own.remove("Enabled");
        Property oldMode = own.remove("Mode");
        String string = carried = oldMode == null ? INCREASE_FASTMINE : oldMode.getString();
        if ("Hold packet".equals(carried)) {
            carried = HOLD_PACKET;
        } else if ("Increase fastmine".equals(carried)) {
            carried = INCREASE_FASTMINE;
        }
        boolean on = oldEnabled == null ? wasEnabled : oldEnabled.getBoolean(true);
        boolean bl = wasOnItsOwn = oldEnabled != null && on;
        this.mode = configuration.get(CAT, "Ignore fatigue", oldEnabled == null && !wasEnabled ? OFF : (on ? carried : OFF), "Off: dig as fatigue has it. Hold: hide the effect from your client until the server ends it. Increase: keep the effect, dig faster while it is on.", MODES);
        configuration.getCategory(CAT).remove("Fastmine");
        configuration.getCategory(CAT).remove("Wooden tool fastmine");
        Property oldSpeed = configuration.getCategory(CAT).remove("Speed");
        this.speed = configuration.get(CAT, "Dig speed", 75, "Increase: how fast you dig while fatigued, as a share of your speed without fatigue (100% = as if there were none). Whatever the fatigue level and tool.", 30, 150);
        if (oldSpeed != null) {
            this.speed.set((int)Math.round(oldSpeed.getDouble(2.5) * 30.0));
        }
        configuration.getCategory(CAT).remove("Wooden tool dig speed");
        this.debug = configuration.get(CAT, "Debug", false, "Each block you break: the ticks it took, against the ticks it takes without fatigue and with it. Works with Fast Mine off too, to compare.");
        InboundPackets.watch(new InboundPackets.Watcher(){

            @Override
            public void seen(Packet<?> packet) {
                IgnoreFatigue.this.onPacket(packet);
            }
        });
    }

    public static boolean wasOnItsOwn() {
        return wasOnItsOwn;
    }

    private boolean on() {
        return FastMine.isOn() && !OFF.equals(this.mode.getString());
    }

    public Property modeProp() {
        return this.mode;
    }

    public Property speedProp() {
        return this.speed;
    }

    public Property debugProp() {
        return this.debug;
    }

    public static void onDigStart(BlockPos pos) {
        IgnoreFatigue self = instance;
        EntityPlayerSP player = Minecraft.func_71410_x().field_71439_g;
        if (self == null || player == null || !self.debug.getBoolean()) {
            return;
        }
        self.digPos = pos;
        self.digStartTick = player.field_70173_aa;
        self.digSkippedTo = 0.0f;
    }

    public static void onSkipped(float to) {
        IgnoreFatigue self = instance;
        if (self != null) {
            self.digSkippedTo = to;
        }
    }

    public static void onDigEnd(BlockPos pos) {
        IgnoreFatigue self = instance;
        Minecraft mc = Minecraft.func_71410_x();
        EntityPlayerSP player = mc.field_71439_g;
        if (self == null || player == null || mc.field_71441_e == null || !self.debug.getBoolean() || self.digPos == null || !self.digPos.equals((Object)pos)) {
            return;
        }
        self.digPos = null;
        int took = player.field_70173_aa - self.digStartTick + 1;
        Block block = mc.field_71441_e.func_180495_p(pos).func_177230_c();
        float perTick = block.func_180647_a((EntityPlayer)player, (World)mc.field_71441_e, pos);
        PotionEffect effect = player.func_70660_b(Potion.field_76419_f);
        StringBuilder text = new StringBuilder();
        text.append("\u00a7f").append(took).append("t \u00a77").append(block.func_149732_F()).append(" | ");
        if (effect == null) {
            text.append("no fatigue on client, vanilla \u00a7f").append(IgnoreFatigue.ticksFor(perTick, 1.0f)).append('t');
        } else {
            float factor = IgnoreFatigue.fatigueFactor(effect.func_76458_c());
            text.append("fatigue ").append(effect.func_76458_c() + 1).append(", without it \u00a7f").append(IgnoreFatigue.ticksFor(perTick / factor, 1.0f)).append("t\u00a77, with it \u00a7f").append(IgnoreFatigue.ticksFor(perTick, 1.0f)).append("t\u00a77, fastmine should take \u00a7f").append(IgnoreFatigue.ticksFor(perTick, FastMine.times((EntityPlayer)player))).append('t');
        }
        text.append(self.digSkippedTo > 0.0f ? String.format(" \u00a77| skipped to %.2f", Float.valueOf(self.digSkippedTo)) : " \u00a77| no skip");
        if (!player.field_70122_E) {
            text.append(" \u00a77(in the air)");
        }
        Conditions.message(NAME, text.toString());
    }

    private static int ticksFor(float perTick, float times) {
        if (perTick >= 1.0f) {
            return 1;
        }
        if (perTick <= 0.0f) {
            return -1;
        }
        return Math.max(1, MathHelper.func_76123_f((float)(1.0f / Math.max(1.0f, times) / perTick)));
    }

    private boolean holdMode() {
        return HOLD_PACKET.equals(this.mode.getString());
    }

    public static boolean decidesSpeed(EntityPlayer player) {
        IgnoreFatigue self = instance;
        return self != null && self.on() && !self.holdMode() && !player.field_71075_bZ.field_75098_d && player.func_70644_a(Potion.field_76419_f);
    }

    public static float fatigueSpeed(EntityPlayer player) {
        IgnoreFatigue self = instance;
        PotionEffect fatigue = player.func_70660_b(Potion.field_76419_f);
        if (self == null || !self.on() || self.holdMode() || player.field_71075_bZ.field_75098_d || fatigue == null) {
            return 1.0f;
        }
        ItemStack held = player.func_70694_bm();
        boolean wooden = held != null && (held.func_77973_b() == Items.field_151039_o || held.func_77973_b() == Items.field_151053_p);
        float share = (float)self.speed.getInt(75) / 100.0f * (wooden ? 0.71999997f : 1.0f);
        return Math.max(1.0f, share / IgnoreFatigue.fatigueFactor(fatigue.func_76458_c()));
    }

    private static float fatigueFactor(int amplifier) {
        switch (amplifier) {
            case 0: {
                return 0.3f;
            }
            case 1: {
                return 0.09f;
            }
            case 2: {
                return 0.0027f;
            }
        }
        return 8.1E-4f;
    }

    private void onPacket(Packet<?> packet) {
        if (!this.on()) {
            return;
        }
        InboundPackets.Hold current = this.hold;
        if (current != null && !current.isActive()) {
            this.hold = null;
            current = null;
        }
        if (packet instanceof S1DPacketEntityEffect) {
            S1DPacketEntityEffect effect = (S1DPacketEntityEffect)packet;
            if (effect.func_149427_e() != FATIGUE || !IgnoreFatigue.isMe(effect.func_149426_d())) {
                return;
            }
            int seconds = effect.func_180755_e() / 20;
            if (!this.holdMode()) {
                if (current == null) {
                    IgnoreFatigue.say(String.format("\u00a77mining fatigue %d: digging at %d%% (%ds)", effect.func_149428_f() + 1, this.speed.getInt(75), seconds));
                }
                return;
            }
            long end = System.currentTimeMillis() + (long)effect.func_180755_e() * 50L;
            this.expectedEndAt = Math.max(this.expectedEndAt, end);
            if (current == null) {
                this.hold = InboundPackets.hold(NAME, new InboundPackets.Rule(){

                    @Override
                    public boolean matches(Packet<?> p) {
                        return p instanceof S1DPacketEntityEffect && ((S1DPacketEntityEffect)p).func_149427_e() == FATIGUE && IgnoreFatigue.isMe(((S1DPacketEntityEffect)p).func_149426_d());
                    }
                });
                IgnoreFatigue.say("\u00a77mining fatigue hidden (" + seconds + "s)");
            }
        } else if (packet instanceof S1EPacketRemoveEntityEffect) {
            S1EPacketRemoveEntityEffect removal = (S1EPacketRemoveEntityEffect)packet;
            if (current == null || removal.func_149075_d() != FATIGUE || !IgnoreFatigue.isMe(removal.func_149076_c())) {
                return;
            }
            this.release();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || this.hold == null) {
            return;
        }
        if (!this.hold.isActive()) {
            this.hold = null;
        } else if (!this.on() || !this.holdMode()) {
            this.release();
        } else if (System.currentTimeMillis() > this.expectedEndAt + 5000L) {
            this.release();
        }
    }

    private synchronized void release() {
        InboundPackets.Hold current = this.hold;
        this.hold = null;
        this.expectedEndAt = 0L;
        if (current != null) {
            current.release();
        }
    }

    private static boolean isMe(int entityId) {
        Minecraft mc = Minecraft.func_71410_x();
        return mc.field_71439_g != null && mc.field_71439_g.func_145782_y() == entityId;
    }

    private static void say(final String text) {
        Minecraft.func_71410_x().func_152344_a(new Runnable(){

            @Override
            public void run() {
                Conditions.message(IgnoreFatigue.NAME, text);
            }
        });
    }

    static {
        FATIGUE = Potion.field_76419_f.field_76415_H;
    }
}
