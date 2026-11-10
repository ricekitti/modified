/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.Client
 *  com.alan.clients.component.impl.player.RotationComponent
 *  com.alan.clients.component.impl.player.SlotComponent
 *  com.alan.clients.component.impl.player.rotationcomponent.MovementFix
 *  com.alan.clients.module.impl.exploit.Disabler
 *  com.alan.clients.module.impl.movement.Speed
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.input.KeyboardInputEvent
 *  com.alan.clients.newevent.impl.input.MoveInputEvent
 *  com.alan.clients.newevent.impl.motion.JumpEvent
 *  com.alan.clients.newevent.impl.motion.PostStrafeEvent
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.newevent.impl.other.TickEvent
 *  com.alan.clients.newevent.impl.packet.PacketSendEvent
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.util.player.PlayerUtil
 *  com.alan.clients.util.player.SlotUtil
 *  com.alan.clients.util.rotation.RotationUtil
 *  com.alan.clients.util.vector.Vector2f
 *  com.alan.clients.util.vector.Vector3d
 *  com.alan.clients.value.Mode
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockAir
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.potion.Potion
 *  net.minecraft.util.MathHelper
 */
package com.alan.clients.module.impl.player.scaffold.tower;

import com.alan.clients.Client;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.impl.exploit.Disabler;
import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.KeyboardInputEvent;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.motion.JumpEvent;
import com.alan.clients.newevent.impl.motion.PostStrafeEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.util.player.SlotUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.Mode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

public class WatchdogTower
extends Mode<Scaffold> {
    public static int moveTicks;
    int dm;
    public static int jumpStage;
    private boolean placingUp;
    private boolean pendingToggle;
    private int upwardPlacements = 0;
    private int akn = 0;
    float savedYaw;
    boolean startedInAir = true;
    boolean aligning = false;
    private int alignStep;
    public static float towerYaw;
    double targetZ = Double.NaN;
    double targetX = Double.NaN;
    private int lastPlaceY;
    private boolean jumpKeyHeld = false;
    @EventLink
    public final Listener<JumpEvent> onJump = var0 -> {};
    @EventLink(value=1)
    public Listener<PreMotionEvent> onPreMotion = var1x -> {
        double d0 = Math.toRadians(WatchdogTower.aEg.thePlayer.pl);
        double d27 = -Math.sin(d0);
        boolean bl = Math.cos(d0) * 1.0 < 0.0;
        if (WatchdogTower.aEg.thePlayer.ticksExisted % 3 != 0 || !WatchdogTower.aEg.thePlayer.onGround || WatchdogTower.aEg.thePlayer.cqL <= 2) {
            float f;
            if (!WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown() && MoveUtil.isMoving()) {
                towerYaw = WatchdogTower.aEg.thePlayer.pl;
                jumpStage = 0;
                moveTicks = 0;
            } else if (!WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown()) {
                jumpStage = 100;
                return;
            }
            if (MoveUtil.isMoving()) {
                ++moveTicks;
            } else if (moveTicks > 20) {
                --moveTicks;
            }
            if (WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown()) {
                ++jumpStage;
            }
            if (moveTicks >= 23) {
                moveTicks = 1;
                towerYaw = WatchdogTower.aEg.thePlayer.pl;
                jumpStage = 99;
            }
            if (WatchdogTower.aEg.thePlayer.onGround) {
                jumpStage = 0;
                this.alignStep = 0;
                towerYaw = WatchdogTower.aEg.thePlayer.pl;
            } else if (jumpStage == 100) {
                MoveUtil.strafe((double)0.0);
            }
            if (!MoveUtil.isMoving()) {
                double d1 = WatchdogTower.aEg.thePlayer.pl;
                double d2 = Math.toRadians(d1);
                d27 = -Math.sin(d2);
                boolean flag2 = Math.cos(d2) * 1.0 < 0.0;
                ((Scaffold)this.getParent()).extraPlacements = 1;
                if (!this.aligning) {
                    if (flag2) {
                        this.targetX = Math.floor(WatchdogTower.aEg.thePlayer.posX) + 0.999999999999;
                        this.targetZ = Double.NaN;
                    } else {
                        this.targetZ = Math.floor(WatchdogTower.aEg.thePlayer.posZ) + 0.999999999999;
                        this.targetX = Double.NaN;
                    }
                    this.aligning = true;
                }
                ++this.alignStep;
                if (Math.abs((double)this.lastPlaceY - WatchdogTower.aEg.thePlayer.posY) >= 1.0) {
                    if (this.alignStep == 1) {
                        MoveUtil.stop();
                        if (!this.isEnclosed()) {
                            double d8;
                            double d7;
                            double d6;
                            if (!Double.isNaN(this.targetX)) {
                                double d3 = WatchdogTower.aEg.thePlayer.posX + (this.targetX - WatchdogTower.aEg.thePlayer.posX) / 3.0;
                                double d4 = WatchdogTower.aEg.thePlayer.posY;
                                double d5 = WatchdogTower.aEg.thePlayer.posZ;
                                if (this.canFitAt(d3, d4, d5)) {
                                    WatchdogTower.aEg.thePlayer.setPosition(d3, d4, d5);
                                }
                            } else if (!Double.isNaN(this.targetZ) && this.canFitAt(d6 = WatchdogTower.aEg.thePlayer.posX, d7 = WatchdogTower.aEg.thePlayer.posY, d8 = WatchdogTower.aEg.thePlayer.posZ + (this.targetZ - WatchdogTower.aEg.thePlayer.posZ) / 3.0)) {
                                WatchdogTower.aEg.thePlayer.setPosition(d6, d7, d8);
                            }
                        }
                    } else if (this.alignStep == 2) {
                        MoveUtil.stop();
                        if (!this.isEnclosed()) {
                            double d14;
                            double d13;
                            double d12;
                            if (!Double.isNaN(this.targetX)) {
                                double d9 = WatchdogTower.aEg.thePlayer.posX + 2.0 * (this.targetX - WatchdogTower.aEg.thePlayer.posX) / 3.0;
                                double d10 = WatchdogTower.aEg.thePlayer.posY;
                                double d11 = WatchdogTower.aEg.thePlayer.posZ;
                                if (this.canFitAt(d9, d10, d11)) {
                                    WatchdogTower.aEg.thePlayer.setPosition(d9, d10, d11);
                                }
                            } else if (!Double.isNaN(this.targetZ) && this.canFitAt(d12 = WatchdogTower.aEg.thePlayer.posX, d13 = WatchdogTower.aEg.thePlayer.posY, d14 = WatchdogTower.aEg.thePlayer.posZ + 2.0 * (this.targetZ - WatchdogTower.aEg.thePlayer.posZ) / 3.0)) {
                                WatchdogTower.aEg.thePlayer.setPosition(d12, d13, d14);
                            }
                            this.updateRotation();
                        } else {
                            this.updateRotation();
                        }
                    } else if (this.alignStep == 3) {
                        MoveUtil.stop();
                        if (!this.isEnclosed()) {
                            double d23;
                            double d22;
                            double d21;
                            if (!Double.isNaN(this.targetX)) {
                                double d15 = this.targetX;
                                double d16 = WatchdogTower.aEg.thePlayer.posY;
                                double d17 = WatchdogTower.aEg.thePlayer.posZ;
                                if (this.canFitAt(d15, d16, d17)) {
                                    WatchdogTower.aEg.thePlayer.setPosition(d15, d16, d17);
                                    this.updateRotation();
                                }
                            } else if (!Double.isNaN(this.targetZ) && this.canFitAt(d21 = WatchdogTower.aEg.thePlayer.posX, d22 = WatchdogTower.aEg.thePlayer.posY, d23 = this.targetZ)) {
                                WatchdogTower.aEg.thePlayer.setPosition(d21, d22, d23);
                            }
                            double d18 = WatchdogTower.aEg.thePlayer.posX;
                            double d19 = WatchdogTower.aEg.thePlayer.posY;
                            double d20 = this.targetZ;
                            this.canFitAt(d18, d19, d20);
                            this.updateRotation();
                            this.alignStep = 0;
                            this.aligning = false;
                        } else {
                            this.updateRotation();
                        }
                    }
                } else {
                    this.updateRotation();
                    this.alignStep = 0;
                    this.aligning = false;
                }
            } else {
                this.aligning = false;
            }
            if (this.pendingToggle && !MoveUtil.isMoving()) {
                WatchdogTower.aEg.gameSettings.keyBindJump.setPressed(true);
            }
            if (WatchdogTower.aEg.thePlayer.Zl < 1) {
                this.aligning = false;
            }
            if (WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown()) {
                this.jumpKeyHeld = true;
            }
            moveTicks = 0;
            if (WatchdogTower.aEg.thePlayer.motionY < 0.3 && this.pendingToggle && WatchdogTower.aEg.thePlayer.motionY > 0.17) {
                ((Scaffold)this.getParent()).toggle();
                if (WatchdogTower.aEg.thePlayer.onGround) {
                    WatchdogTower.aEg.thePlayer.jump();
                }
                this.pendingToggle = false;
            }
            if (this.jumpKeyHeld && !WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown()) {
                if (WatchdogTower.aEg.thePlayer.motionY < 0.3 && this.pendingToggle) {
                    double d24 = WatchdogTower.aEg.thePlayer.motionY - 0.17;
                    int n = d24 == 0.0 ? 0 : (d24 < 0.0 ? -1 : 1);
                }
                this.jumpKeyHeld = false;
            }
            if (!WatchdogTower.aEg.gameSettings.keyBindJump.isPressed()) {
                WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown();
            }
            float f2 = f = jumpStage == 1 ? 90.0f : 0.0f;
            if (MathHelper.wrapAngleTo180_float((float)(WatchdogTower.aEg.thePlayer.pl - towerYaw)) < f) {
                towerYaw = WatchdogTower.aEg.thePlayer.pl;
            } else if (MathHelper.wrapAngleTo180_float((float)(WatchdogTower.aEg.thePlayer.pl - towerYaw)) < 0.0f) {
                towerYaw -= f;
            } else if (MathHelper.wrapAngleTo180_float((float)(WatchdogTower.aEg.thePlayer.pl - towerYaw)) > 0.0f) {
                towerYaw += f;
            }
            if (moveTicks < 20 && !((Speed)Client.a.g().c(Speed.class)).isEnabled()) {
                WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
                if (WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown()) {
                    WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
                    switch (jumpStage) {
                        case 0: {
                            MoveUtil.strafe();
                            if (WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed) && WatchdogTower.aEg.thePlayer.getActivePotionEffect((Potion)Potion.moveSpeed).amplifier + 1 >= 2) {
                                WatchdogTower.aEg.thePlayer.motionZ *= 1.045;
                                WatchdogTower.aEg.thePlayer.motionX *= 1.045;
                            } else if (WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                WatchdogTower.aEg.thePlayer.motionZ *= 1.035;
                                WatchdogTower.aEg.thePlayer.motionX *= 1.035;
                            }
                            ((Mode)((Scaffold)Client.a.g().c(Scaffold.class)).sameY.wo()).getName().equals("Off");
                            double d26 = MoveUtil.speed() - 0.24;
                            int l = d26 == 0.0 ? 0 : (d26 < 0.0 ? -1 : 1);
                            WatchdogTower.aEg.thePlayer.motionY = 0.42;
                            break;
                        }
                        case 1: {
                            int j;
                            MoveUtil.strafe();
                            double d25 = MoveUtil.speed() - 0.24;
                            int n = d25 == 0.0 ? 0 : (j = d25 < 0.0 ? -1 : 1);
                            if (WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                int n2 = WatchdogTower.aEg.thePlayer.getActivePotionEffect((Potion)Potion.moveSpeed).amplifier + 1;
                            }
                            WatchdogTower.aEg.thePlayer.motionY = 0.33;
                            if (WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed) && WatchdogTower.aEg.thePlayer.getActivePotionEffect((Potion)Potion.moveSpeed).amplifier + 1 >= 2) {
                                WatchdogTower.aEg.thePlayer.motionZ *= 1.015;
                                WatchdogTower.aEg.thePlayer.motionX *= 1.015;
                            } else if (WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                WatchdogTower.aEg.thePlayer.motionZ *= 1.005;
                                WatchdogTower.aEg.thePlayer.motionX *= 1.005;
                            }
                            if (!((Mode)((Scaffold)Client.a.g().c(Scaffold.class)).sameY.wo()).getName().equals("Off")) break;
                            break;
                        }
                        case 2: {
                            WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
                            if (((Boolean)((Disabler)this.e(Disabler.class)).watchdogFly.wo()).booleanValue() && ((Disabler)this.e(Disabler.class)).isEnabled()) {
                                MoveUtil.strafe();
                            }
                            WatchdogTower.aEg.thePlayer.motionY = 1.0 - WatchdogTower.aEg.thePlayer.posY % 1.0;
                        }
                    }
                }
            } else if (!WatchdogTower.aEg.thePlayer.onGround) {
                // empty if block
            }
            if (WatchdogTower.aEg.thePlayer.motionY > 0.0) {
                WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown();
            }
            if (moveTicks != 20 && moveTicks != 21) {
                if (moveTicks > 20) {
                    // empty if block
                }
            } else {
                this.savedYaw = towerYaw;
            }
            if (moveTicks == 22 && !((Speed)Client.a.g().c(Speed.class)).isEnabled()) {
                if (WatchdogTower.aEg.thePlayer.cqL == 0) {
                    WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
                }
                WatchdogTower.aEg.thePlayer.motionY = WatchdogTower.aEg.thePlayer.motionY < 0.2 && WatchdogTower.aEg.thePlayer.motionY > 0.1 ? (WatchdogTower.aEg.thePlayer.motionY -= 0.42) : -0.0784000015258789;
                WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
            }
            if (jumpStage == 2) {
                jumpStage = -1;
            }
            ((Scaffold)this.getParent()).extraPlacements = 1;
        }
    };
    @EventLink
    public final Listener<PostStrafeEvent> onPostStrafe = var0 -> {};
    @EventLink
    public final Listener<MoveInputEvent> onMoveInput = var1x -> {
        if (MoveUtil.isMoving() && WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown() && !((Speed)this.e(Speed.class)).isEnabled()) {
            var1x.setForward(var1x.getForward() * 5.0f);
        }
    };
    @EventLink
    public final Listener<StrafeEvent> onStrafe = var0 -> {
        if (WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown() && (WatchdogTower.aEg.gameSettings.keyBindRight.isKeyDown() || WatchdogTower.aEg.gameSettings.keyBindLeft.isKeyDown())) {
            MoveUtil.strafe((double)0.25);
        }
        if (MoveUtil.speed() < 0.19 && WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown() && WatchdogTower.aEg.thePlayer.Zl > 9) {
            MoveUtil.strafe((double)0.19);
        }
        if (moveTicks >= 16) {
            WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed);
        }
    };
    @EventLink
    public final Listener<KeyboardInputEvent> onKeyboardInput = var1x -> {
        if (var1x.getKeyCode() == ((Scaffold)this.getParent()).getKey() && !this.pendingToggle && WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown() && !((Speed)Client.a.g().c(Speed.class)).isEnabled()) {
            var1x.setCancelled();
            this.pendingToggle = true;
        }
    };
    @EventLink
    public final Listener<TickEvent> onTick = var0 -> {};
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = var1x -> {
        C08PacketPlayerBlockPlacement c08packetplayerblockplacement;
        double d0 = Math.toRadians(WatchdogTower.aEg.thePlayer.pl);
        double d1 = -Math.sin(d0);
        boolean bl = Math.cos(d0) * 1.0 < 0.0;
        Packet patt0$temp = var1x.dq();
        if (patt0$temp instanceof C08PacketPlayerBlockPlacement && (c08packetplayerblockplacement = (C08PacketPlayerBlockPlacement)patt0$temp).getPlacedBlockDirection() == 1 && (c08packetplayerblockplacement.getStack() == null || c08packetplayerblockplacement.getStack().getItem() != Item.getItemFromBlock((Block)Blocks.ice))) {
            this.placingUp = true;
            ++this.upwardPlacements;
        } else {
            C08PacketPlayerBlockPlacement c08packetplayerblockplacement1;
            Packet patt1$temp = var1x.dq();
            if (patt1$temp instanceof C08PacketPlayerBlockPlacement && ((c08packetplayerblockplacement1 = (C08PacketPlayerBlockPlacement)patt1$temp).getStack() == null || c08packetplayerblockplacement1.getStack().getItem() != Item.getItemFromBlock((Block)Blocks.ice))) {
                this.upwardPlacements = 0;
                this.placingUp = false;
            }
        }
        if (!this.placingUp || ((Speed)this.e(Speed.class)).isEnabled() || this.upwardPlacements >= 2) {
            // empty if block
        }
    };
    @EventLink
    public final Listener<JumpEvent> onJumpMedium = var1x -> {
        if (!((Speed)this.e(Speed.class)).isEnabled() && WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown() && !WatchdogTower.aEg.thePlayer.isPotionActive(Potion.moveSpeed)) {
            MoveUtil.strafe((double)0.15);
        }
        if (this.pendingToggle) {
            MoveUtil.strafe((double)0.0);
        }
    };

    public WatchdogTower(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
    }

    public void onEnable() {
        this.placingUp = false;
        SlotComponent slotcomponent = (SlotComponent)this.d(SlotComponent.class);
        SlotComponent.setSlot((int)SlotUtil.vx());
        this.akn = 0;
        this.pendingToggle = false;
        this.targetZ = Double.NaN;
        moveTicks = 0;
        this.alignStep = 0;
        towerYaw = WatchdogTower.aEg.thePlayer.pl;
        this.aligning = false;
        jumpStage = WatchdogTower.aEg.thePlayer.onGround ? 0 : 100;
        if (((Mode)((Scaffold)Client.a.g().c(Scaffold.class)).sameY.wo()).getName().equals("Off") && !WatchdogTower.aEg.thePlayer.onGround) {
            this.startedInAir = true;
        }
        this.jumpKeyHeld = false;
    }

    public void onDisable() {
        this.aligning = false;
        this.startedInAir = false;
        this.pendingToggle = false;
        towerYaw = WatchdogTower.aEg.thePlayer.pl;
        jumpStage = 100;
        this.alignStep = 0;
        if (WatchdogTower.aEg.gameSettings.keyBindJump.isKeyDown()) {
            MoveUtil.strafe((double)0.23);
        }
    }

    public void updateRotation() {
        Vector3d aka;
        boolean flag;
        this.lastPlaceY = (int)Math.floor(WatchdogTower.aEg.thePlayer.posY);
        double d0 = WatchdogTower.aEg.thePlayer.pl;
        double radians = Math.toRadians(d0);
        double d2 = -Math.sin(radians);
        boolean bl = flag = Math.cos(radians) * 1.0 < 0.0;
        if (flag) {
            RotationComponent.d((boolean)false);
            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)(WatchdogTower.aEg.thePlayer.pl - 164.0f) + (Math.random() - 0.5) * 3.0), 86.0f), (double)10.0, (MovementFix)MovementFix.OFF);
            aka = new Vector3d(1.0, 0.0, 0.0);
        } else {
            aka = new Vector3d(0.0, 0.0, 1.0);
        }
        ((Scaffold)this.getParent()).placeOffset = aka;
        RotationUtil.d((Vector3d)aka);
        if (!MoveUtil.isMoving() && !flag) {
            RotationComponent.d((boolean)false);
            float f = (float)(d0 + 164.0 + (Math.random() - 0.5) * 3.0);
            float f1 = 86.0f;
            RotationComponent.d((boolean)false);
            RotationComponent.setRotations((Vector2f)new Vector2f(f, f1), (double)10.0, (MovementFix)MovementFix.OFF);
        }
    }

    private boolean isEnclosed() {
        int i = (int)Math.floor(WatchdogTower.aEg.thePlayer.posX);
        int j = (int)Math.floor(WatchdogTower.aEg.thePlayer.posY);
        int k = (int)Math.floor(WatchdogTower.aEg.thePlayer.posZ);
        Block[] ablock = new Block[]{PlayerUtil.o((double)(i + 1), (double)j, (double)k), PlayerUtil.o((double)(i - 1), (double)j, (double)k), PlayerUtil.o((double)i, (double)j, (double)(k + 1)), PlayerUtil.o((double)i, (double)j, (double)(k - 1))};
        int l = ablock.length;
        for (int i1 = 0; i1 < l; ++i1) {
            if (!(ablock[i1] instanceof BlockAir)) continue;
            return false;
        }
        return true;
    }

    private boolean canFitAt(double var1, double var3, double var5) {
        float f = WatchdogTower.aEg.thePlayer.width / 2.0f;
        double d0 = var1 - (double)f;
        double d1 = var3;
        double d2 = var5 - (double)f;
        double d3 = var1 + (double)f;
        double d4 = var3 + (double)WatchdogTower.aEg.thePlayer.height;
        double d5 = var5 + (double)f;
        for (int i = MathHelper.floor_double((double)d0); i <= MathHelper.floor_double((double)d3); ++i) {
            for (int j = MathHelper.floor_double((double)d1); j <= MathHelper.floor_double((double)d4); ++j) {
                for (int k = MathHelper.floor_double((double)d2); k <= MathHelper.floor_double((double)d5); ++k) {
                    if (PlayerUtil.o((double)i, (double)j, (double)k) instanceof BlockAir) continue;
                    return false;
                }
            }
        }
        return true;
    }
}
