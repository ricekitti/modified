/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.multiplayer.PlayerControllerMP
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.MathHelper
 */
package com.snowify.module.player.fastmine;

import com.snowify.config.Configuration;
import com.snowify.config.Property;
import com.snowify.module.combat.ignorefatigue.IgnoreFatigue;
import com.snowify.module.player.bednuker.BedNuker;
import com.snowify.util.GameAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public final class FastMine {
    public static final String CAT = "fastmine";
    private static FastMine instance;
    private final Property enabled;
    private final Property speed;
    private final Property breakDelay;
    private final Property decrementDelay;
    private float savedProgress;
    private int lastDelay;

    public FastMine(Configuration configuration) {
        instance = this;
        this.enabled = configuration.get(CAT, "Enabled", false, "Breaks blocks in fewer ticks.");
        this.speed = configuration.get(CAT, "Speed", 1.5, "How many times faster than normal a block breaks.", 1.0, 3.0);
        this.breakDelay = configuration.get(CAT, "Break delay", 5, "The most ticks between one block breaking and the next starting (vanilla: 5).", 0, 5);
        this.decrementDelay = configuration.get(CAT, "Decrement block hit delay", false, "Takes one more tick off that wait each tick it has not gone down.");
        if (IgnoreFatigue.wasOnItsOwn() && !this.enabled.getBoolean()) {
            this.enabled.set(true);
            this.speed.set(1.0);
        }
    }

    public static boolean isOn() {
        FastMine self = instance;
        return self != null && self.enabled.getBoolean();
    }

    public Property enabledProp() {
        return this.enabled;
    }

    public Property speedProp() {
        return this.speed;
    }

    public Property breakDelayProp() {
        return this.breakDelay;
    }

    public Property decrementDelayProp() {
        return this.decrementDelay;
    }

    public static float times(EntityPlayer player) {
        if (BedNuker.isDigging() && player == Minecraft.func_71410_x().field_71439_g) {
            return BedNuker.times((EntityPlayerSP)player);
        }
        if (IgnoreFatigue.decidesSpeed(player)) {
            return IgnoreFatigue.fatigueSpeed(player);
        }
        FastMine self = instance;
        return self != null && self.enabled.getBoolean() ? (float)self.speed.getDouble(1.5) : 1.0f;
    }

    public static void beforeClickBlock(Minecraft mc) {
        FastMine self = instance;
        if (self == null) {
            return;
        }
        if (mc.field_71439_g == null || mc.field_71442_b == null) {
            self.savedProgress = 0.0f;
            self.lastDelay = 0;
            return;
        }
        PlayerControllerMP pc = mc.field_71442_b;
        self.savedProgress = GameAccess.blockDamage(pc);
        int delay = GameAccess.hitDelay(pc);
        if (BedNuker.isDigging()) {
            GameAccess.setHitDelay(pc, 0);
        } else if (self.enabled.getBoolean()) {
            int limit = self.breakDelay.getInt(5);
            if (delay > limit) {
                GameAccess.setHitDelay(pc, limit);
            } else if (delay > 0 && self.decrementDelay.getBoolean() && delay == self.lastDelay) {
                GameAccess.setHitDelay(pc, delay - 1);
            }
        }
        self.lastDelay = delay;
    }

    public static void afterClickBlock(Minecraft mc) {
        int fast;
        FastMine self = instance;
        if (self == null || mc.field_71439_g == null || mc.field_71441_e == null || !mc.field_71415_G || mc.field_71442_b == null) {
            return;
        }
        PlayerControllerMP pc = mc.field_71442_b;
        float progress = GameAccess.blockDamage(pc);
        if (!GameAccess.hittingBlock(pc) || progress <= 0.0f || self.savedProgress != 0.0f) {
            return;
        }
        float times = FastMine.times((EntityPlayer)mc.field_71439_g);
        if (times <= 1.0f) {
            return;
        }
        int normal = MathHelper.func_76123_f((float)(1.0f / progress));
        int steps = normal - (fast = MathHelper.func_76123_f((float)(1.0f / times / progress))) + 1;
        if (steps > 1) {
            float to = (float)steps * progress;
            GameAccess.setBlockDamage(pc, to);
            IgnoreFatigue.onSkipped(to);
        }
    }
}
