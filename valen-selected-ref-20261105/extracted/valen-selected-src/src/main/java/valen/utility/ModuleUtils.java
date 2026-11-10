/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockAir
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C07PacketPlayerDigging
 *  net.minecraft.network.play.client.C07PacketPlayerDigging$Action
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.client.C09PacketHeldItemChange
 *  net.minecraft.network.play.server.S27PacketExplosion
 *  net.minecraft.util.BlockPos
 *  valen.module.impl.combat.Velocity
 */
package valen.utility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;
import valen.event.NoEventPacketEvent;
import valen.event.PostMotionEvent;
import valen.event.PreMotionEvent;
import valen.event.PreUpdateEvent;
import valen.event.ReceivePacketEvent;
import valen.event.SendPacketEvent;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.module.ModuleManager;
import valen.module.impl.combat.Velocity;
import valen.module.impl.movement.LongJump;
import valen.utility.BlockUtils;
import valen.utility.IMinecraftInstance;
import valen.utility.Utils;

public class ModuleUtils
implements IMinecraftInstance {
    public static boolean isBreaking;
    public static boolean threwFireball;
    public static boolean threwFireballLow;
    private int isBreakingTick;
    public static long MAX_EXPLOSION_DIST_SQ;
    private long FIREBALL_TIMEOUT = 500L;
    private long fireballTime = 0L;
    public static int inAirTicks;
    public static int groundTicks;
    public static int stillTicks;
    public static int fadeEdge;
    public static int lastFaceDifference;
    private int lastFace;
    public static double offsetValue;
    public static boolean isAttacking;
    private int attackingTicks;
    public static int profileTicks;
    public static boolean lastTickOnGround;
    public static boolean lastTickPos1;
    private boolean thisTickOnGround;
    private boolean thisTickPos1;
    public static boolean firstDamage;
    public static boolean isBlocked;
    public static boolean damage;
    private int damageTicks;
    private boolean lowhopAir;
    private static boolean allowFriction;
    public static boolean canSlow;
    public static boolean didSlow;
    public static boolean setSlow;

    @SubscribeEvent
    public void onSendPacketNoEvent(NoEventPacketEvent noEventPacketEvent) {
        this.handleAllPacket(noEventPacketEvent.getPacket());
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent sendPacketEvent) {
        this.handleAllPacket(sendPacketEvent.getPacket());
        if (sendPacketEvent.getPacket() instanceof C07PacketPlayerDigging) {
            isBreaking = true;
        }
        if (sendPacketEvent.getPacket() instanceof C08PacketPlayerBlockPlacement && Utils.holdingFireball() && Utils.isBindDown(ModuleUtils.mc.gameSettings.keyBindUseItem)) {
            this.fireballTime = System.currentTimeMillis();
            threwFireball = true;
            if (ModuleUtils.mc.thePlayer.rotationPitch > 50.0f) {
                threwFireballLow = true;
            }
        }
        if (sendPacketEvent.getPacket() instanceof C08PacketPlayerBlockPlacement && Utils.scaffoldDiagonal(false) && ((C08PacketPlayerBlockPlacement)sendPacketEvent.getPacket()).getPlacedBlockDirection() != 1) {
            int n = ((C08PacketPlayerBlockPlacement)sendPacketEvent.getPacket()).getPlacedBlockDirection();
            lastFaceDifference = n == this.lastFace ? ++lastFaceDifference : 0;
            this.lastFace = n;
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
        if (!Utils.nullCheck() || receivePacketEvent.isCanceled()) {
            return;
        }
        if (receivePacketEvent.getPacket() instanceof S27PacketExplosion) {
            S27PacketExplosion s27PacketExplosion = (S27PacketExplosion)receivePacketEvent.getPacket();
            if (threwFireball && ModuleUtils.mc.thePlayer.getPosition().distanceSq(s27PacketExplosion.getX(), s27PacketExplosion.getY(), s27PacketExplosion.getZ()) <= (double)MAX_EXPLOSION_DIST_SQ) {
                ModuleUtils.resetVelocityDisables();
                threwFireball = false;
                receivePacketEvent.setCanceled(false);
            }
        }
    }

    private void handleAllPacket(Packet<?> packet) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (packet instanceof C08PacketPlayerBlockPlacement && Utils.holdingSword() && !BlockUtils.isInteractable(ModuleUtils.mc.objectMouseOver) && !isBlocked) {
            isBlocked = true;
        } else if (packet instanceof C07PacketPlayerDigging && isBlocked) {
            if (((C07PacketPlayerDigging)packet).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                isBlocked = false;
            }
        } else if (packet instanceof C09PacketHeldItemChange && isBlocked) {
            isBlocked = false;
        }
        if (packet instanceof C02PacketUseEntity) {
            isAttacking = true;
            this.attackingTicks = 5;
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent preUpdateEvent) {
        if (damage && ++this.damageTicks >= 8) {
            firstDamage = false;
            damage = false;
            this.damageTicks = 0;
        }
        ++profileTicks;
        if (isAttacking) {
            if (this.attackingTicks <= 0) {
                isAttacking = false;
            } else {
                --this.attackingTicks;
            }
        }
        if (LongJump.slotReset && ++LongJump.slotResetTicks >= 2) {
            LongJump.stopModules = false;
            LongJump.slotResetTicks = 0;
            LongJump.slotReset = false;
        }
        if (ModuleManager.bHop == null || !ModuleManager.bHop.hopping) {
            allowFriction = false;
        } else if (!ModuleUtils.mc.thePlayer.onGround) {
            allowFriction = true;
        }
        if (this.fireballTime > 0L && System.currentTimeMillis() - this.fireballTime > this.FIREBALL_TIMEOUT / 3L) {
            threwFireballLow = false;
            ModuleUtils.resetVelocityDisables();
        }
        if (this.fireballTime > 0L && System.currentTimeMillis() - this.fireballTime > this.FIREBALL_TIMEOUT) {
            threwFireballLow = false;
            threwFireball = false;
            this.fireballTime = 0L;
            ModuleUtils.resetVelocityDisables();
        }
        if (isBreaking && ++this.isBreakingTick >= 1) {
            isBreaking = false;
            this.isBreakingTick = 0;
        }
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent postMotionEvent) {
        if (this.bHopBoostConditions() && firstDamage) {
            Utils.setSpeed(Utils.getHorizontalSpeed());
            firstDamage = false;
        }
    }

    private boolean bHopBoostConditions() {
        return ModuleManager.bHop != null && ModuleManager.bHop.isEnabled() && ModuleManager.bHop.damageBoost.isToggled() && (!ModuleManager.bHop.damageBoostRequireKey.isToggled() || ModuleManager.bHop.damageBoostKey.isPressed());
    }

    public static double applyFrictionMulti() {
        int n = Utils.getSpeedAmplifier();
        if (n > 1 && allowFriction) {
            return 1.0;
        }
        return 1.0;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent preMotionEvent) {
        int n = (int)Math.round(preMotionEvent.posY % 1.0 * 10000.0);
        lastTickOnGround = this.thisTickOnGround;
        this.thisTickOnGround = ModuleUtils.mc.thePlayer.onGround;
        lastTickPos1 = this.thisTickPos1;
        this.thisTickPos1 = ModuleUtils.mc.thePlayer.posY % 1.0 == 0.0;
        inAirTicks = ModuleUtils.mc.thePlayer.onGround ? 0 : (inAirTicks = inAirTicks + 1);
        groundTicks = !ModuleUtils.mc.thePlayer.onGround ? 0 : (groundTicks = groundTicks + 1);
        stillTicks = Utils.isMoving() ? 0 : (stillTicks = stillTicks + 1);
        Block block = BlockUtils.getBlock(new BlockPos(ModuleUtils.mc.thePlayer.posX, ModuleUtils.mc.thePlayer.posY - 1.0, ModuleUtils.mc.thePlayer.posZ));
        Block block2 = BlockUtils.getBlock(new BlockPos(ModuleUtils.mc.thePlayer.posX, ModuleUtils.mc.thePlayer.posY - 2.0, ModuleUtils.mc.thePlayer.posZ));
        Block block3 = BlockUtils.getBlock(new BlockPos(ModuleUtils.mc.thePlayer.posX, ModuleUtils.mc.thePlayer.posY, ModuleUtils.mc.thePlayer.posZ));
        if (!(ModuleManager.bHop == null || !ModuleManager.bHop.didMove || damage && Velocity.vertical.getInput() != 0.0 || ModuleUtils.mc.thePlayer.isCollidedHorizontally)) {
            if (!(block3 instanceof BlockAir) || block instanceof BlockAir && block2 instanceof BlockAir) {
                this.resetLowhop();
            }
            block0 : switch ((int)ModuleManager.bHop.mode.getInput()) {
                case 2: {
                    switch (n) {
                        case 13: {
                            ModuleUtils.mc.thePlayer.motionY -= 0.02483;
                            break;
                        }
                        case 2000: {
                            ModuleUtils.mc.thePlayer.motionY -= 0.1913;
                            break;
                        }
                        case 7016: {
                            ModuleUtils.mc.thePlayer.motionY += 0.08;
                        }
                    }
                    if (inAirTicks > 6 && Utils.isMoving()) {
                        Utils.setSpeed(Utils.getHorizontalSpeed((Entity)ModuleUtils.mc.thePlayer));
                    }
                    if (inAirTicks <= 8) break;
                    this.resetLowhop();
                    break;
                }
                case 3: {
                    switch (n) {
                        case 13: {
                            ModuleUtils.mc.thePlayer.motionY -= 0.045;
                            break;
                        }
                        case 2000: {
                            ModuleUtils.mc.thePlayer.motionY -= 0.175;
                            this.resetLowhop();
                        }
                    }
                    break;
                }
                case 4: {
                    switch (n) {
                        case 4200: {
                            ModuleUtils.mc.thePlayer.motionY = 0.39;
                            break block0;
                        }
                        case 1138: {
                            ModuleUtils.mc.thePlayer.motionY -= 0.13;
                            break block0;
                        }
                        case 2031: {
                            ModuleUtils.mc.thePlayer.motionY -= 0.2;
                            this.resetLowhop();
                        }
                    }
                }
            }
        }
        if (!ModuleUtils.mc.thePlayer.onGround) {
            this.lowhopAir = true;
        } else if (this.lowhopAir) {
            this.resetLowhop();
        }
        if (ModuleManager.bHop != null && ModuleManager.bHop.setRotation) {
            float f = ModuleUtils.mc.thePlayer.rotationYaw - 55.0f;
            preMotionEvent.setYaw(f);
            if (ModuleUtils.mc.thePlayer.onGround) {
                ModuleManager.bHop.setRotation = false;
            }
        }
        if (canSlow && !ModuleUtils.mc.thePlayer.onGround) {
            double d = 0.9 - (double)inAirTicks / 10000.0 - Utils.randomizeDouble(1.0E-5, 6.0E-5);
            if (ModuleUtils.mc.thePlayer.hurtTime == 0 && inAirTicks > 4 && !setSlow) {
                ModuleUtils.mc.thePlayer.motionX *= d;
                ModuleUtils.mc.thePlayer.motionZ *= d;
                setSlow = true;
            }
            didSlow = true;
        } else if (didSlow) {
            didSlow = false;
            canSlow = false;
        }
        if (ModuleUtils.mc.thePlayer.onGround) {
            setSlow = false;
        }
    }

    private void resetLowhop() {
        if (ModuleManager.bHop != null) {
            ModuleManager.bHop.lowhop = false;
            ModuleManager.bHop.didMove = false;
        }
        this.lowhopAir = false;
    }

    private static void resetVelocityDisables() {
        if (ModuleManager.velocity != null) {
            ModuleManager.velocity.disable = false;
        }
        if (ModuleManager.antiKnockback != null) {
            ModuleManager.antiKnockback.disable = false;
        }
    }

    public static void handleSlow() {
        didSlow = false;
        canSlow = true;
    }

    static {
        MAX_EXPLOSION_DIST_SQ = 10L;
        offsetValue = 2.01E-11;
        profileTicks = -1;
    }
}

