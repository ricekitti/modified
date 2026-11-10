/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.init.Items
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.server.S08PacketPlayerPosLook
 *  net.minecraft.network.play.server.S12PacketEntityVelocity
 *  net.minecraft.network.play.server.S27PacketExplosion
 */
package valen.module.impl.movement;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleConsumer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import valen.Valen;
import valen.event.PreEntityVelocityEvent;
import valen.event.PreExplosionPacketEvent;
import valen.event.ReceivePacketEvent;
import valen.event.RunGameLoopEvent;
import valen.event.SendPacketEvent;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.mixin.impl.accessor.IAccessorMinecraft;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.module.setting.impl.SliderSetting;
import valen.utility.FireballJumpTracker;
import valen.utility.TimerController;
import valen.utility.TimerRangeCycle;
import valen.utility.Utils;

public final class FireballBoost
extends Module {
    private static final String[] MODES = new String[]{"Regular", "Legit"};
    private final SliderSetting mode;
    private final SliderSetting duration;
    private final SliderSetting cooldown;
    private final TimerController timer;
    private final TimerRangeCycle cycle;
    private final FireballJumpTracker jump = new FireballJumpTracker();
    private final AtomicBoolean correctionPending = new AtomicBoolean();
    private Object activeWorld;
    private Object activePlayer;

    public FireballBoost() {
        this(Valen.timerController, (double d) -> ((IAccessorMinecraft)FireballBoost.mc).valen$lunar$getTimer().elapsedPartialTicks += (float)d);
    }

    FireballBoost(TimerController timerController, DoubleConsumer doubleConsumer) {
        super("Fireball Boost", Module.category.movement);
        this.timer = timerController;
        this.cycle = new TimerRangeCycle(timerController, TimerController.Owner.FIREBALL_BOOST, 40, doubleConsumer);
        this.mode = new SliderSetting("Mode", 0, MODES);
        this.registerSetting(this.mode);
        this.duration = new SliderSetting("Duration", "ms", 200.0, 50.0, 2000.0, 50.0);
        this.registerSetting(this.duration);
        this.cooldown = new SliderSetting("Cooldown", "ms", 1000.0, 0.0, 5000.0, 50.0);
        this.registerSetting(this.cooldown);
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    @Override
    public String getInfo() {
        return MODES[(int)this.mode.getInput()];
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onSendPacket(SendPacketEvent sendPacketEvent) {
        if (sendPacketEvent.isCanceled() || !this.clientThread() || !this.prepareContext() || !this.canRun() || this.cycle.active() || !(sendPacketEvent.getPacket() instanceof C08PacketPlayerBlockPlacement)) {
            return;
        }
        C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement = (C08PacketPlayerBlockPlacement)sendPacketEvent.getPacket();
        if (c08PacketPlayerBlockPlacement.getStack() != null && c08PacketPlayerBlockPlacement.getStack().stackSize > 0 && c08PacketPlayerBlockPlacement.getStack().getItem() == Items.fire_charge) {
            this.jump.onUse(System.nanoTime());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onExplosion(PreExplosionPacketEvent preExplosionPacketEvent) {
        if (preExplosionPacketEvent.isCanceled() || !this.clientThread() || !this.prepareContext() || !this.canRun()) {
            return;
        }
        S27PacketExplosion s27PacketExplosion = preExplosionPacketEvent.packet;
        this.jump.onExplosion(System.nanoTime(), FireballBoost.mc.thePlayer.getDistanceSq(s27PacketExplosion.getX(), s27PacketExplosion.getY(), s27PacketExplosion.getZ()), s27PacketExplosion.func_149149_c(), s27PacketExplosion.func_149144_d(), s27PacketExplosion.func_149147_e());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onVelocity(PreEntityVelocityEvent preEntityVelocityEvent) {
        if (preEntityVelocityEvent.isCanceled() || !this.clientThread() || !this.prepareContext() || !this.canRun()) {
            return;
        }
        S12PacketEntityVelocity s12PacketEntityVelocity = preEntityVelocityEvent.packet;
        if (s12PacketEntityVelocity.getEntityID() == FireballBoost.mc.thePlayer.getEntityId()) {
            this.jump.onVelocity(System.nanoTime(), (double)s12PacketEntityVelocity.getMotionX() / 8000.0, (double)s12PacketEntityVelocity.getMotionY() / 8000.0, (double)s12PacketEntityVelocity.getMotionZ() / 8000.0);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
        if (receivePacketEvent.getPacket() instanceof S08PacketPlayerPosLook) {
            this.correctionPending.set(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onGameLoop(RunGameLoopEvent runGameLoopEvent) {
        boolean bl;
        if (!this.prepareContext()) {
            return;
        }
        long l = System.nanoTime();
        if (this.correctionPending.getAndSet(false) || !this.canRun() || this.timer.hasRequestOtherThan(TimerController.Owner.FIREBALL_BOOST)) {
            this.abort(l);
            return;
        }
        boolean bl2 = bl = this.mode.getInput() == 1.0;
        if (this.cycle.active()) {
            if (this.cycle.legit() != bl) {
                this.abort(l);
            } else {
                this.cycle.update(l);
            }
            return;
        }
        if (this.jump.consumeJump(l, !FireballBoost.mc.thePlayer.onGround, FireballBoost.mc.thePlayer.motionY)) {
            this.cycle.begin(l, bl, this.duration.getInput(), this.cooldown.getInput());
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent renderTickEvent) {
        if (renderTickEvent.phase == TickEvent.Phase.END && this.cycle.active() && FireballBoost.mc.currentScreen == null) {
            this.onKeyBind();
        }
    }

    private boolean clientThread() {
        return mc != null && mc.isCallingFromMinecraftThread();
    }

    private boolean prepareContext() {
        if (!Utils.nullCheck()) {
            this.reset();
            return false;
        }
        if (this.activeWorld != FireballBoost.mc.theWorld || this.activePlayer != FireballBoost.mc.thePlayer) {
            this.reset();
            this.activeWorld = FireballBoost.mc.theWorld;
            this.activePlayer = FireballBoost.mc.thePlayer;
        }
        return true;
    }

    private boolean canRun() {
        return FireballBoost.mc.currentScreen == null && FireballBoost.mc.inGameHasFocus && !FireballBoost.mc.thePlayer.isDead && FireballBoost.mc.thePlayer.getHealth() > 0.0f && !FireballBoost.mc.thePlayer.isRiding() && !FireballBoost.mc.thePlayer.capabilities.isFlying && !FireballBoost.mc.thePlayer.isSpectator() && !FireballBoost.mc.thePlayer.isInWater() && !FireballBoost.mc.thePlayer.isInLava() && !FireballBoost.mc.thePlayer.isOnLadder() && !ModuleManager.isBedMining();
    }

    private void abort(long l) {
        this.cycle.abort(l);
        this.jump.reset();
    }

    private void reset() {
        this.cycle.reset();
        this.jump.reset();
        this.activePlayer = null;
        this.activeWorld = null;
        this.correctionPending.set(false);
    }
}

