/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alan.clients.Client
 *  com.alan.clients.component.impl.player.BadPacketsComponent
 *  com.alan.clients.component.impl.player.BlinkComponent
 *  com.alan.clients.component.impl.player.RotationComponent
 *  com.alan.clients.component.impl.player.SlotComponent
 *  com.alan.clients.component.impl.player.rotationcomponent.MovementFix
 *  com.alan.clients.module.impl.movement.Flight
 *  com.alan.clients.module.impl.movement.LongJump
 *  com.alan.clients.module.impl.movement.Speed
 *  com.alan.clients.module.impl.player.Scaffold
 *  com.alan.clients.module.impl.player.scaffold.sprint.WatchdogJumpSprintHelper
 *  com.alan.clients.module.impl.player.scaffold.tower.WatchdogTower
 *  com.alan.clients.newevent.Listener
 *  com.alan.clients.newevent.annotations.EventLink
 *  com.alan.clients.newevent.impl.motion.PreMotionEvent
 *  com.alan.clients.newevent.impl.motion.PreUpdateEvent
 *  com.alan.clients.newevent.impl.motion.StrafeEvent
 *  com.alan.clients.newevent.impl.packet.PacketReceiveEvent
 *  com.alan.clients.util.packet.PacketUtil
 *  com.alan.clients.util.player.MoveUtil
 *  com.alan.clients.util.player.PlayerUtil
 *  com.alan.clients.util.player.SlotUtil
 *  com.alan.clients.util.vector.Vector2f
 *  com.alan.clients.value.Mode
 *  com.alan.clients.value.impl.BooleanValue
 *  lombok.Generated
 *  net.minecraft.block.BlockAir
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.ItemSword
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  net.minecraft.network.play.server.S23PacketBlockChange
 *  net.minecraft.potion.Potion
 *  net.minecraft.util.MathHelper
 */
package com.alan.clients.module.impl.player.scaffold.sprint;

import com.alan.clients.Client;
import com.alan.clients.component.impl.player.BadPacketsComponent;
import com.alan.clients.component.impl.player.BlinkComponent;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.impl.movement.Flight;
import com.alan.clients.module.impl.movement.LongJump;
import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.module.impl.player.Scaffold;
import com.alan.clients.module.impl.player.scaffold.sprint.WatchdogJumpSprintHelper;
import com.alan.clients.module.impl.player.scaffold.tower.WatchdogTower;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.util.player.SlotUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import java.util.Random;
import lombok.Generated;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

public class WatchdogJumpSprint
extends Mode<Scaffold> {
    private boolean HJ = false;
    private boolean wasJumping = false;
    private boolean justEnabled = false;
    private int startTick = -1;
    public static boolean boostPending = false;
    public static boolean boostNextJump = false;
    public static boolean blockChanged = false;
    public static boolean ajz = false;
    private int groundTicks = 0;
    private int blockChanges;
    public final BooleanValue placeExtraBlocks = new BooleanValue("Place Extra blocks", (Mode)this, Boolean.valueOf(false));
    private final WatchdogJumpSprintHelper helper;
    @EventLink(value=3)
    public final Listener<PreMotionEvent> onPreMotion = var1x -> {
        double d1;
        float f;
        ((Scaffold)this.getParent()).extraPlacements = 1;
        if (Math.abs(WatchdogJumpSprint.aEg.thePlayer.motionY) < 0.1) {
            // empty if block
        }
        boolean flag = Math.abs((f = MathHelper.wrapAngleTo180_float((float)WatchdogJumpSprint.aEg.thePlayer.pl)) % 90.0f) <= 10.0f || Math.abs(f % 90.0f) >= 80.0f;
        double d0 = MathHelper.wrapAngleTo180_double((double)Math.toDegrees(MoveUtil.direction()));
        if (Math.abs(d0 - (d1 = MathHelper.wrapAngleTo180_double((double)(Math.toDegrees(Math.atan2(WatchdogJumpSprint.aEg.thePlayer.motionZ, WatchdogJumpSprint.aEg.thePlayer.motionX)) - 90.0)))) < 5.0 && WatchdogJumpSprint.aEg.thePlayer.Zl > 7 && !WatchdogJumpSprint.aEg.gameSettings.cgI.isKeyDown() && !WatchdogJumpSprint.aEg.gameSettings.keyBindSneak.isKeyDown()) {
            if (!(WatchdogJumpSprint.aEg.thePlayer.cqL <= 2 || WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown() || ((Speed)Client.a.g().c(Speed.class)).isEnabled() || WatchdogJumpSprint.aEg.thePlayer.isPotionActive(Potion.moveSpeed) || WatchdogJumpSprint.aEg.thePlayer.Zl <= 7)) {
                MoveUtil.strafe();
                if (flag) {
                    WatchdogJumpSprint.aEg.thePlayer.motionZ *= 1.1225;
                    WatchdogJumpSprint.aEg.thePlayer.motionX *= 1.1225;
                } else {
                    WatchdogJumpSprint.aEg.thePlayer.motionZ *= 1.129;
                    WatchdogJumpSprint.aEg.thePlayer.motionX *= 1.129;
                }
            } else if (WatchdogJumpSprint.aEg.thePlayer.cqL > 2 && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown() && !((Speed)Client.a.g().c(Speed.class)).isEnabled() && WatchdogJumpSprint.aEg.thePlayer.isPotionActive(Potion.moveSpeed) && WatchdogJumpSprint.aEg.thePlayer.getActivePotionEffect((Potion)Potion.moveSpeed).amplifier + 1 >= 2 && WatchdogJumpSprint.aEg.thePlayer.Zl > 7) {
                MoveUtil.strafe();
                WatchdogJumpSprint.aEg.thePlayer.motionZ *= 1.1055;
                WatchdogJumpSprint.aEg.thePlayer.motionX *= 1.1055;
            } else if (WatchdogJumpSprint.aEg.thePlayer.cqL > 2 && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown() && !((Speed)Client.a.g().c(Speed.class)).isEnabled() && WatchdogJumpSprint.aEg.thePlayer.isPotionActive(Potion.moveSpeed) && WatchdogJumpSprint.aEg.thePlayer.getActivePotionEffect((Potion)Potion.moveSpeed).amplifier + 1 == 1 && WatchdogJumpSprint.aEg.thePlayer.Zl > 7) {
                MoveUtil.strafe();
                WatchdogJumpSprint.aEg.thePlayer.motionZ *= 1.098;
                WatchdogJumpSprint.aEg.thePlayer.motionX *= 1.098;
            }
        }
        if (!WatchdogJumpSprint.aEg.gameSettings.keyBindSneak.isKeyDown()) {
            int i = WatchdogJumpSprint.aEg.thePlayer.ticksExisted;
            if (!((Flight)this.e(Flight.class)).isEnabled() && !((LongJump)this.e(LongJump.class)).isEnabled() && !BadPacketsComponent.bad((boolean)true, (boolean)true, (boolean)false, (boolean)true, (boolean)false) && Math.random() > 0.5 && blockChanged && MoveUtil.isMoving() && !(WatchdogJumpSprint.aEg.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
                Random random = new Random();
                random.nextFloat();
                random.nextFloat();
                blockChanged = false;
            }
            if ((this.startTick == -1 || i - this.startTick < 4) && this.startTick == -1) {
                this.startTick = i;
            }
            if (WatchdogJumpSprint.aEg.thePlayer.onGround) {
                MoveUtil.strafe();
            }
            if (this.wasJumping && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
                WatchdogTower.towerYaw = WatchdogJumpSprint.aEg.thePlayer.pl;
                WatchdogTower.moveTicks = 17;
                WatchdogTower.jumpStage = 0;
                MoveUtil.stop();
                this.wasJumping = false;
                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)WatchdogJumpSprint.aEg.thePlayer.pl + (Math.random() - 0.5) * 3.0), 90.0f), (double)10.0, (MovementFix)MovementFix.NORMAL);
                if (!(PlayerUtil.o((double)WatchdogJumpSprint.aEg.thePlayer.posX, (double)(WatchdogJumpSprint.aEg.thePlayer.aI - 2.0), (double)WatchdogJumpSprint.aEg.thePlayer.posY) instanceof BlockAir)) {
                    ((Scaffold)this.getParent()).startY = WatchdogJumpSprint.aEg.thePlayer.aI - 1.0;
                }
            }
            if (WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
                this.wasJumping = true;
            }
        }
    };
    @EventLink(value=2)
    public final Listener<PreUpdateEvent> onPreUpdate = var1x -> {
        boolean flag;
        block17: {
            SlotComponent slotcomponent;
            block16: {
                boolean bl = flag = WatchdogJumpSprint.aEg.thePlayer.aI == ((Scaffold)this.getParent()).startY;
                if (!((Boolean)this.placeExtraBlocks.wo()).booleanValue()) {
                    flag = true;
                }
                slotcomponent = (SlotComponent)this.d(SlotComponent.class);
                if (SlotComponent.getItemStack() != null && WatchdogJumpSprint.aEg.thePlayer.posY > ((Scaffold)this.getParent()).startY && WatchdogJumpSprint.aEg.thePlayer.posY + MoveUtil.predictedMotion((double)WatchdogJumpSprint.aEg.thePlayer.motionY, (int)2) < ((Scaffold)this.getParent()).startY + 1.0 && (!this.justEnabled || boostPending) && ((Speed)Client.a.g().c(Speed.class)).isEnabled() && !((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump")) break block16;
                slotcomponent = (SlotComponent)this.d(SlotComponent.class);
                if (SlotComponent.getItemStack() == null || !(WatchdogJumpSprint.aEg.thePlayer.posY > ((Scaffold)this.getParent()).startY) || !(WatchdogJumpSprint.aEg.thePlayer.posY + MoveUtil.predictedMotion((double)WatchdogJumpSprint.aEg.thePlayer.motionY, (int)2) < ((Scaffold)this.getParent()).startY + 1.0) || !((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump")) break block17;
            }
            slotcomponent = (SlotComponent)this.d(SlotComponent.class);
            SlotComponent.setSlot((int)SlotUtil.vx());
        }
        if (((Speed)Client.a.g().c(Speed.class)).isEnabled()) {
            flag = false;
        }
        if (this.justEnabled) {
            flag = true;
        }
        if (WatchdogJumpSprint.aEg.thePlayer.tR > 8) {
            this.justEnabled = false;
        }
        if ((PlayerUtil.ad((double)2.0) || PlayerUtil.ad((double)1.0) || PlayerUtil.ad((double)0.0) || PlayerUtil.ad((double)3.0) || WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isPressed() || WatchdogJumpSprint.aEg.thePlayer.isPotionActive(Potion.moveSpeed) || this.wasJumping) && WatchdogJumpSprint.aEg.thePlayer.cqL < 1) {
            WatchdogJumpSprint.aEg.thePlayer.crd = false;
        }
        if (WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isPressed() && WatchdogJumpSprint.aEg.thePlayer.onGround && !this.wasJumping && (boostNextJump || ((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump")) && !((Speed)Client.a.g().c(Speed.class)).isEnabled()) {
            boostNextJump = false;
            MoveUtil.strafe((double)(MoveUtil.vd() * 0.9));
            if (((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump")) {
                MoveUtil.strafe((double)(MoveUtil.getAllowedHorizontalDistance() - 0.001));
            }
        }
        if (WatchdogJumpSprint.aEg.thePlayer.tR == 4 && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown() && !((Speed)Client.a.g().c(Speed.class)).isEnabled() && !flag && ((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump")) {
            WatchdogJumpSprint.aEg.thePlayer.motionY -= 0.19;
        }
        if (WatchdogJumpSprint.aEg.thePlayer.tR == 1 && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
            MoveUtil.strafe();
        }
        if (PlayerUtil.p((double)0.0, (double)WatchdogJumpSprint.aEg.thePlayer.motionY, (double)0.0) != Blocks.air && WatchdogJumpSprint.aEg.thePlayer.tR > 2) {
            MoveUtil.strafe();
        }
        if (flag && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown() && !WatchdogJumpSprint.aEg.thePlayer.onGround && !((Speed)this.e(Speed.class)).isEnabled() && ((Boolean)this.placeExtraBlocks.wo()).booleanValue() || !flag || WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown() || WatchdogJumpSprint.aEg.thePlayer.onGround || ((Boolean)this.placeExtraBlocks.wo()).booleanValue()) {
            // empty if block
        }
        if (flag && WatchdogJumpSprint.aEg.thePlayer.onGround && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
            ++this.groundTicks;
        }
        if (!flag) {
            this.groundTicks = 0;
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = var1x -> {
        if (var1x.getPacket() instanceof S23PacketBlockChange) {
            blockChanged = true;
            ++this.blockChanges;
        }
    };
    @EventLink
    public final Listener<StrafeEvent> onStrafe = var1x -> {
        if (!((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump") && WatchdogJumpSprint.aEg.thePlayer.onGround && this.justEnabled && boostPending && !((Speed)Client.a.g().c(Speed.class)).isEnabled() && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
            boostPending = false;
        }
    };

    public WatchdogJumpSprint(String var1, Scaffold scaffold) {
        super(var1, (Object)scaffold);
        this.helper = new WatchdogJumpSprintHelper(scaffold);
    }

    public void onEnable() {
        blockChanged = false;
        ajz = false;
        if (!((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("Auto Jump")) {
            if (WatchdogJumpSprint.aEg.thePlayer.cqL > 9) {
                boostPending = true;
                this.justEnabled = true;
            }
        } else {
            WatchdogJumpSprint.aEg.thePlayer.crd = false;
        }
        boostNextJump = true;
        this.wasJumping = false;
        if (!WatchdogJumpSprint.aEg.thePlayer.onGround && !WatchdogJumpSprint.aEg.gameSettings.keyBindJump.isKeyDown()) {
            WatchdogJumpSprint.aEg.thePlayer.motionX *= 0.0;
            WatchdogJumpSprint.aEg.thePlayer.motionZ *= 0.0;
        }
        this.startTick = -1;
        this.blockChanges = 0;
        boostNextJump = true;
    }

    public void onDisable() {
        boolean flag;
        this.HJ = false;
        blockChanged = false;
        WatchdogJumpSprint.aEg.thePlayer.crd = false;
        boostPending = false;
        this.justEnabled = false;
        float f = MathHelper.wrapAngleTo180_float((float)WatchdogJumpSprint.aEg.thePlayer.pl);
        boolean bl = flag = Math.abs(f % 90.0f) <= 10.0f || Math.abs(f % 90.0f) >= 80.0f;
        if (flag) {
            if (WatchdogJumpSprint.aEg.thePlayer.cqL > 2 && WatchdogJumpSprint.aEg.thePlayer.ticksExisted % 3 == 1) {
                WatchdogJumpSprint.aEg.timer.dzD = 0.5f;
                PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprint.aEg.thePlayer.onGround));
            } else if (WatchdogJumpSprint.aEg.thePlayer.cqL > 2 && WatchdogJumpSprint.aEg.thePlayer.ticksExisted % 3 == 2) {
                WatchdogJumpSprint.aEg.timer.dzD = 0.33f;
                PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprint.aEg.thePlayer.onGround));
                PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprint.aEg.thePlayer.onGround));
            } else if (WatchdogJumpSprint.aEg.thePlayer.cqL > 2) {
                WatchdogJumpSprint.aEg.timer.dzD = 0.5f;
                PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprint.aEg.thePlayer.onGround));
            }
        } else if (WatchdogJumpSprint.aEg.thePlayer.cqL > 2) {
            WatchdogJumpSprint.aEg.timer.dzD = 0.33f;
            PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprint.aEg.thePlayer.onGround));
            PacketUtil.send((Packet)new C03PacketPlayer(WatchdogJumpSprint.aEg.thePlayer.onGround));
        }
        boostNextJump = false;
        BlinkComponent.dispatch();
    }

    @Generated
    public WatchdogJumpSprintHelper getHelper() {
        return this.helper;
    }
}
