/*
 * Decompiled with CFR 0.152.
 */
package com.alan.clients.module.impl.player;

import com.alan.clients.component.impl.player.BadPacketsComponent;
import com.alan.clients.component.impl.player.RotationComponent;
import com.alan.clients.component.impl.player.SlotComponent;
import com.alan.clients.component.impl.player.rotationcomponent.MovementFix;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.movement.Flight;
import com.alan.clients.module.impl.movement.Speed;
import com.alan.clients.module.impl.movement.Sprint;
import com.alan.clients.module.impl.player.scaffold.downwards.NormalDownwards;
import com.alan.clients.module.impl.player.scaffold.downwards.VerusDownwards;
import com.alan.clients.module.impl.player.scaffold.downwards.WatchdogDownwards;
import com.alan.clients.module.impl.player.scaffold.sprint.BypassSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.DisabledSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.LegitSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.MatrixSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.VerusSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.VulcanSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.WatchdogJumpSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.WatchdogPredictiSprint;
import com.alan.clients.module.impl.player.scaffold.sprint.WatchdogSprint;
import com.alan.clients.module.impl.player.scaffold.tower.AirJumpTower;
import com.alan.clients.module.impl.player.scaffold.tower.LegitTower;
import com.alan.clients.module.impl.player.scaffold.tower.MMCTower;
import com.alan.clients.module.impl.player.scaffold.tower.MatrixTower;
import com.alan.clients.module.impl.player.scaffold.tower.NCPTower;
import com.alan.clients.module.impl.player.scaffold.tower.NormalTower;
import com.alan.clients.module.impl.player.scaffold.tower.VanillaTower;
import com.alan.clients.module.impl.player.scaffold.tower.VerusTower;
import com.alan.clients.module.impl.player.scaffold.tower.VulcanTower;
import com.alan.clients.module.impl.player.scaffold.tower.WatchdogPrediction18Tower;
import com.alan.clients.module.impl.player.scaffold.tower.WatchdogTower;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.input.MoveInputEvent;
import com.alan.clients.newevent.impl.input.RightClickEvent;
import com.alan.clients.newevent.impl.motion.PostMotionEvent;
import com.alan.clients.newevent.impl.motion.PreMotionEvent;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.motion.SprintEvent;
import com.alan.clients.newevent.impl.motion.StrafeEvent;
import com.alan.clients.newevent.impl.other.TeleportEvent;
import com.alan.clients.newevent.impl.packet.PacketReceiveEvent;
import com.alan.clients.newevent.impl.packet.PacketSendEvent;
import com.alan.clients.util.RayCastUtil;
import com.alan.clients.util.chat.ChatUtil;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.packet.PacketUtil;
import com.alan.clients.util.player.EnumFacingOffset;
import com.alan.clients.util.player.MoveUtil;
import com.alan.clients.util.player.PlayerUtil;
import com.alan.clients.util.player.SlotUtil;
import com.alan.clients.util.rotation.RotationUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.util.vector.Vector3d;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.BoundsNumberValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.NumberValue;
import com.alan.clients.value.impl.SubMode;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import lombok.Generated;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.m;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import rip.vantage.network.core.VantageNetwork;

@ModuleInfo(aliases={"module.player.scaffold.name"}, description="module.player.scaffold.description", category=Category.PLAYER)
public class Scaffold
extends Module {
    public BooleanValue sneak;
    @EventLink
    public Listener<TeleportEvent> onTeleport;
    public static boolean ahf;
    public ModeValue mode = new ModeValue("Mode", (Module)this).add(new Mode[]{new SubMode("Normal")}).add(new Mode[]{new SubMode("Godbridge")}).add(new Mode[]{new SubMode("Breesily")}).add(new Mode[]{new SubMode("Snap")}).add(new Mode[]{new SubMode("Telly")}).add(new Mode[]{new SubMode("Eagle")}).setDefault("Normal");
    public boolean rotationBoostPending;
    public Vector3d placeOffset;
    public int extraPlacements;
    @EventLink(value=4)
    public Listener<PreUpdateEvent> onPreUpdate;
    public float prevVisualPitch;
    public double startY;
    public BooleanValue agt;
    public BooleanValue visualBackRotations;
    public BoundsNumberValue age;
    public float prevVisualYaw;
    public ModeValue rotationMode = new ModeValue("Rotation Mode", (Module)this).add(new Mode[]{new SubMode("Normal")}).add(new Mode[]{new SubMode("Grim")}).setDefault("Normal");
    @EventLink
    public Listener<PacketReceiveEvent> onPacketReceiveEvent;
    public NumberValue timer;
    public Vec3 targetBlock;
    public int pauseTicks;
    public BooleanValue blockDiagonalAscend;
    @EventLink
    public Listener<StrafeEvent> onStrafe;
    public float forward;
    public BooleanValue boostOnlyWhileHoldingJump;
    @EventLink
    public Listener<RightClickEvent> onRightClick;
    public BoundsNumberValue startSneaking;
    public BlockPos blockFace;
    @EventLink
    public Listener<SprintEvent> onSprint;
    public BooleanValue safeWalk;
    public float visualYaw;
    public float targetPitch;
    public BooleanValue disableOnFlag;
    public ModeValue downwardsPressS;
    public EnumFacingOffset enumFacing;
    public ModeValue sprint;
    public ModeValue rayCast = new ModeValue("Ray Cast", (Module)this).add(new Mode[]{new SubMode("Off")}).add(new Mode[]{new SubMode("Normal")}).add(new Mode[]{new SubMode("Strict")}).setDefault("Strict");
    public BooleanValue watchdogPrediction;
    public BooleanValue rotationBlockBoost;
    public ModeValue yawOffset;
    public EnumFacingOffset lastEnumFacing;
    public NumberValue sneakingSpeed;
    public int placementIndex;
    public BooleanValue keepYBypass;
    public BoundsNumberValue rotationSpeed;
    public BoundsNumberValue stopSneaking;
    public BooleanValue movementCorrection;
    public BooleanValue advanced;
    public boolean agx;
    public int blocksUntilSneak;
    @EventLink
    public Listener<PostMotionEvent> onPostMotion;
    public int ticksOnAir;
    public int godBridgeSneakTicks;
    public boolean readyToPlace;
    @EventLink
    public Listener<MoveInputEvent> onMoveInput;
    public BooleanValue tellyOnlyOnRightClick;
    @EventLink
    public Listener<PacketSendEvent> onPacketSend;
    public float strafe;
    public BooleanValue extendBlockReachOnWatchdogTelly;
    public int sneakingTicks;
    public BoundsNumberValue placeDelay;
    public ModeValue tower;
    public float randomPitchOffset;
    public BooleanValue watchdogTelly;
    public int lastFlagTick;
    public int predictionPlaceDelay;
    public BooleanValue bypassRaycastWhenFalling;
    public int blockInputTicks;
    public BooleanValue render;
    public NumberValue expand;
    public BoundsNumberValue sneakEvery;
    public int tellyTicks;
    public BooleanValue upSideDown;
    public float randomYawOffset;
    public int tellyJumpDelay;
    @EventLink
    public Listener<MoveInputEvent> onMoveInputMedium;
    public int flagDisableTicks;
    public BooleanValue newWatchdogRots;
    @EventLink
    public Listener<PreMotionEvent> onPreMotionEvent;
    public BoundsNumberValue watchdogTellyRotationSpeed;
    public BoundsNumberValue rotationBlockRotationSpeed;
    public ModeValue sameY;
    public float visualPitch;
    public BooleanValue dontForceRaycastOnWatchdogTelly;
    public float targetYaw;
    public boolean visualRotationsActive;

    public void D(int var1) {
        EnumFacingOffset aib;
        EnumFacingOffset enumFacingOffset = aib = this.enumFacing != null ? this.enumFacing : this.lastEnumFacing;
        if (aib != null) {
            if (((Boolean)this.watchdogPrediction.wo()).booleanValue()) {
                ArrayList<Vector2f> arraylist = new ArrayList<Vector2f>();
                EntityPlayerSP entityplayersp1 = Scaffold.aEg.thePlayer;
                double d3 = entityplayersp1.posY + (double)entityplayersp1.getEyeHeight() - this.targetBlock.yCoord - 0.5 - (Math.random() - 0.5) * 0.1;
                for (int i = -180 + var1; i <= 180; i += 45) {
                    Vector2f vector2f7;
                    entityplayersp1.setPosition(entityplayersp1.posX, entityplayersp1.posY - d3, entityplayersp1.posZ);
                    MovingObjectPosition movingobjectposition1 = RayCastUtil.c((Vector2f)new Vector2f(entityplayersp1.pl + (float)(i * 3), 0.0f), (double)6.0);
                    entityplayersp1.setPosition(entityplayersp1.posX, entityplayersp1.posY + d3, entityplayersp1.posZ);
                    if (movingobjectposition1 == null || movingobjectposition1.hitVec == null || !RayCastUtil.a((Vector2f)(vector2f7 = RotationUtil.h((Vec3)movingobjectposition1.hitVec)), (BlockPos)this.blockFace, (EnumFacing)aib.getEnumFacing()).booleanValue()) continue;
                    arraylist.add(vector2f7);
                }
                if (!arraylist.isEmpty()) {
                    Float f7 = Float.valueOf(RotationComponent.fk != null ? RotationComponent.fk.x : Scaffold.aEg.thePlayer.pl);
                    Float f6 = Float.valueOf(RotationComponent.fk != null ? RotationComponent.fk.y : Scaffold.aEg.thePlayer.rotationPitch);
                    Vector2f vector2f = (Vector2f)arraylist.get(0);
                    Float f8 = Float.valueOf(Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f.x - f7.floatValue()))));
                    Float f9 = Float.valueOf(Math.abs(vector2f.y - f6.floatValue()));
                    float f10 = f8.floatValue() * f8.floatValue() + f9.floatValue() * f9.floatValue();
                    for (Vector2f vector2f1 : arraylist) {
                        Float f12;
                        float f11 = Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f1.x - f7.floatValue())));
                        Float f13 = Float.valueOf(f11 * f11 + (f12 = Float.valueOf(Math.abs(vector2f1.y - f6.floatValue()))).floatValue() * f12.floatValue());
                        if (!(f13.floatValue() < f10)) continue;
                        f10 = f13.floatValue();
                        vector2f = vector2f1;
                    }
                    this.targetYaw = vector2f.x;
                    this.targetPitch = vector2f.y;
                } else {
                    Vector2f vector2f2 = RotationUtil.a((Vector3d)new Vector3d((double)this.blockFace.getX(), (double)this.blockFace.getY(), (double)this.blockFace.getZ()), (EnumFacing)aib.getEnumFacing());
                    this.targetYaw = vector2f2.x;
                    this.targetPitch = vector2f2.y;
                }
            } else {
                EntityPlayerSP entityplayersp = Scaffold.aEg.thePlayer;
                Double d1 = entityplayersp.posY + (double)entityplayersp.getEyeHeight() - this.targetBlock.yCoord - 0.5 - (Math.random() - 0.5) * 0.1;
                Float f14 = Float.valueOf(Scaffold.aEg.thePlayer.pl);
                Float wrapped = Float.valueOf(MathHelper.wrapAngleTo180_float((float)f14.floatValue()));
                Double d2 = MathHelper.wrapAngleTo180_double((double)(Math.toDegrees(Math.atan2(Scaffold.aEg.thePlayer.motionZ, Scaffold.aEg.thePlayer.motionX)) - 90.0));
                float f24 = Math.abs(wrapped.floatValue() - this.targetYaw) - 100.0f;
                int j3 = f24 == 0.0f ? 0 : (f24 < 0.0f ? -1 : 1);
                boolean notAbs = Math.abs(wrapped.floatValue() % 90.0f) <= 10.0f || Math.abs(wrapped.floatValue() % 90.0f) >= 80.0f;
                int j2_hi = notAbs ? -135 + var1 : -180 + var1;
                int limit = notAbs ? 135 : 180;
                ArrayList<Vector2f> arraylist1 = new ArrayList<Vector2f>();
                for (int i2 = j2_hi; i2 <= limit; i2 += 45) {
                    Vector2f vector2f3;
                    entityplayersp.setPosition(entityplayersp.posX, entityplayersp.posY - d1, entityplayersp.posZ);
                    MovingObjectPosition movingobjectposition = RayCastUtil.c((Vector2f)new Vector2f(entityplayersp.pl + (float)(i2 * 3), 0.0f), (double)((Boolean)this.extendBlockReachOnWatchdogTelly.wo() != false ? 4.5 : 5.5));
                    entityplayersp.setPosition(entityplayersp.posX, entityplayersp.posY + d1, entityplayersp.posZ);
                    if (movingobjectposition == null || movingobjectposition.hitVec == null || !RayCastUtil.a((Vector2f)(vector2f3 = RotationUtil.h((Vec3)movingobjectposition.hitVec)), (BlockPos)this.blockFace, (EnumFacing)aib.getEnumFacing()).booleanValue()) continue;
                    arraylist1.add(vector2f3);
                }
                if (!arraylist1.isEmpty()) {
                    Float f16 = Float.valueOf(RotationComponent.fk != null ? RotationComponent.fk.x : Scaffold.aEg.thePlayer.pl);
                    Float f17 = Float.valueOf(RotationComponent.fk != null ? RotationComponent.fk.y : Scaffold.aEg.thePlayer.rotationPitch);
                    Vector2f vector2f4 = (Vector2f)arraylist1.get(0);
                    Float f18 = Float.valueOf(Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f4.x - f16.floatValue()))));
                    Float f19 = Float.valueOf(Math.abs(vector2f4.y - f17.floatValue()));
                    float f20 = f18.floatValue() * f18.floatValue() + f19.floatValue() * f19.floatValue();
                    for (Vector2f vector2f5 : arraylist1) {
                        float f22;
                        float f21 = Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f5.x - f16.floatValue())));
                        float f23 = f21 * f21 + (f22 = Math.abs(vector2f5.y - f17.floatValue())) * f22;
                        if (!(f23 < f20)) continue;
                        f20 = f23;
                        vector2f4 = vector2f5;
                    }
                    this.targetYaw = vector2f4.x;
                    this.targetPitch = vector2f4.y;
                } else {
                    Vector2f vector2f6 = RotationUtil.a((Vector3d)new Vector3d((double)this.blockFace.getX(), (double)this.blockFace.getY(), (double)this.blockFace.getZ()), (EnumFacing)aib.getEnumFacing());
                    if (!RayCastUtil.a((Vector2f)new Vector2f(this.targetYaw, this.targetPitch), (BlockPos)this.blockFace, (EnumFacing)aib.getEnumFacing()).booleanValue()) {
                        this.targetYaw = vector2f6.x;
                        this.targetPitch = vector2f6.y;
                    }
                }
            }
        }
    }

    public void onEnable() {
        this.targetYaw = Scaffold.aEg.thePlayer.pl - 180.0f + (float)Integer.parseInt(((Mode)this.yawOffset.wo()).getName());
        this.targetPitch = 90.0f;
        this.randomPitchOffset = (float)((Math.random() - 0.5) * (Math.random() - 0.5) * 10.0);
        this.randomYawOffset = (float)((Math.random() - 0.5) * (Math.random() - 0.5) * 10.0);
        this.tellyTicks = 0;
        this.startY = Math.floor(Scaffold.aEg.thePlayer.posY);
        this.targetBlock = null;
        this.lastEnumFacing = null;
        this.rotationBoostPending = (Boolean)this.rotationBlockBoost.wo() != false;
        this.tellyJumpDelay = this.age.wv().intValue();
        this.h(new Vector2f(this.targetYaw, this.targetPitch));
        this.sneakingTicks = -1;
        this.extraPlacements = 0;
        this.blocksUntilSneak = 0;
        Scaffold.aEg.thePlayer.crd = false;
        Object object = this.sprint.wo();
        if (object instanceof WatchdogJumpSprint) {
            ((WatchdogJumpSprint)object).getHelper().onEnable();
        }
    }

    public boolean isStrictRayCast() {
        return ((Mode)this.rayCast.wo()).getName().equals("Strict");
    }

    public Vector3d getInterpolatedEyePosition(EntityPlayerSP player, float var2) {
        double d3 = player.prevPosX + (player.posX - player.prevPosX) * (double)var2;
        double d4 = player.prevPosY + (player.posY - player.prevPosY) * (double)var2 + (double)player.getEyeHeight();
        double d5 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)var2;
        return new Vector3d(d3, d4, d5);
    }

    public boolean isDisabledByFlag() {
        return (Boolean)this.watchdogTelly.wo() != false && (Boolean)this.disableOnFlag.wo() != false && this.flagDisableTicks > 0;
    }

    public boolean isBlockPlacement(C08PacketPlayerBlockPlacement packet) {
        return !packet.getPosition().h(new Vector3d(-1.0, -1.0, -1.0)) && packet.getStack() != null && packet.getStack().getItem() instanceof ItemBlock;
    }

    public void placeBlock() {
        if (this.pauseTicks > 3) {
            ChatUtil.c((Object)"Pause is greater than 3", (Object[])new Object[0]);
        } else if (this.enumFacing != null) {
            int equals2 = (int)(((Mode)this.rotationMode.wo()).getName().equals("Grim") ? 1L : 0L);
            Vec3 hitVec = this.getHitVec();
            if (equals2 != 0) {
                float f1 = Scaffold.aEg.thePlayer.pl + MathHelper.wrapAngleTo180_float((float)(this.targetYaw - Scaffold.aEg.thePlayer.pl));
                PacketUtil.send((Packet)new C03PacketPlayer.C06PacketPlayerPosLook(Scaffold.aEg.thePlayer.posX, Scaffold.aEg.thePlayer.posY, Scaffold.aEg.thePlayer.posZ, f1, this.targetPitch, Scaffold.aEg.thePlayer.onGround));
                PlayerControllerMP playercontrollermp1 = Scaffold.aEg.playerController;
                EntityPlayerSP entityplayersp1 = Scaffold.aEg.thePlayer;
                WorldClient worldclient1 = Scaffold.aEg.theWorld;
                SlotComponent slotcomponent1 = (SlotComponent)this.d(SlotComponent.class);
                if (playercontrollermp1.onPlayerRightClick(entityplayersp1, worldclient1, SlotComponent.getItemStack(), this.blockFace, this.enumFacing.getEnumFacing(), hitVec)) {
                    PacketUtil.send((Packet)new m());
                }
                PacketUtil.send((Packet)new C03PacketPlayer.C06PacketPlayerPosLook(Scaffold.aEg.thePlayer.posX, Scaffold.aEg.thePlayer.posY, Scaffold.aEg.thePlayer.posZ, (float)((double)Scaffold.aEg.thePlayer.pl + Math.random() * 0.03), (float)Math.clamp((double)Scaffold.aEg.thePlayer.rotationPitch - Math.random(), -90.0, 90.0), Scaffold.aEg.thePlayer.onGround));
            } else if (((Mode)this.rayCast.wo()).getName().equals("Strict")) {
                aEg.Az();
            } else {
                PlayerControllerMP playercontrollermp = Scaffold.aEg.playerController;
                EntityPlayerSP entityplayersp = Scaffold.aEg.thePlayer;
                WorldClient worldclient = Scaffold.aEg.theWorld;
                SlotComponent slotcomponent = (SlotComponent)this.d(SlotComponent.class);
                if (playercontrollermp.onPlayerRightClick(entityplayersp, worldclient, SlotComponent.getItemStack(), this.blockFace, this.enumFacing.getEnumFacing(), hitVec)) {
                    PacketUtil.send((Packet)new m());
                }
            }
        }
    }

    public boolean ki() {
        return Scaffold.aEg.thePlayer != null && this.isEnabled() && (Boolean)this.watchdogTelly.wo() != false && "Telly".equals(this.getDisplayName()) && MoveUtil.enoughMovementForSprinting();
    }

    public Vector3d getEyePosition(EntityPlayerSP player) {
        return new Vector3d(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
    }

    public boolean isOffsetReplaceableBelow(int var1) {
        return PlayerUtil.p((double)this.placeOffset.getX(), (double)((double)(-var1) + this.placeOffset.getY()), (double)this.placeOffset.getZ()).isReplaceable((World)Scaffold.aEg.theWorld, new BlockPos((Entity)Scaffold.aEg.thePlayer).down(var1));
    }

    public boolean isMovingAwayFrom(EnumFacing facing) {
        Vec3i vec3i;
        Vec3 vec32;
        Vec3 vec3 = new Vec3(Scaffold.aEg.thePlayer.motionX, 0.0, Scaffold.aEg.thePlayer.motionZ);
        if (vec3.lengthVector() < 1.0E-4) {
            return false;
        }
        Vec3 vec = vec3.normalize();
        return vec.dotProduct(vec32 = new Vec3((double)(vec3i = facing.getDirectionVec()).getX(), 0.0, (double)vec3i.getZ()).normalize()) < 0.0;
    }

    public void runMode() {
        if (this.getDisplayName().equals("Telly")) {
            boolean notAbs;
            float wrapped = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
            boolean bl = notAbs = Math.abs(wrapped % 90.0f) <= 10.0f || Math.abs(wrapped % 90.0f) >= 80.0f;
            if (Scaffold.aEg.thePlayer.onGround) {
                MoveUtil.enoughMovementForSprinting();
            }
        }
    }

    public boolean isReplaceableAtStartY() {
        return PlayerUtil.o((double)Scaffold.aEg.thePlayer.posX, (double)(this.startY - 1.0), (double)Scaffold.aEg.thePlayer.posZ).isReplaceable(Scaffold.aEg.thePlayer.worldObj, new BlockPos(Scaffold.aEg.thePlayer.posX, this.startY - 1.0, Scaffold.aEg.thePlayer.posZ));
    }

    public String getDisplayName() {
        this.updateFlagState();
        String s = ((Mode)this.mode.wo()).getName();
        if (s.equals("Telly")) {
            if (this.isDisabledByFlag()) {
                return "Normal";
            }
            if (((Boolean)this.tellyOnlyOnRightClick.wo()).booleanValue()) {
                return Scaffold.aEg.gameSettings.cgI.isKeyDown() ? "Telly" : "Normal";
            }
        }
        return s;
    }

    public float c(float var1, float var2, float var3) {
        float wrapped = MathHelper.wrapAngleTo180_float((float)(var2 - var1));
        MathHelper.wrapAngleTo180_double((double)var2);
        MathHelper.wrapAngleTo180_double((double)(Math.toDegrees(Math.atan2(Scaffold.aEg.thePlayer.motionZ, Scaffold.aEg.thePlayer.motionX)) - 90.0));
        float f3 = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
        boolean notAbs = Math.abs(f3 % 90.0f) <= 15.0f || Math.abs(f3 % 90.0f) >= 75.0f;
        float f4 = MathHelper.clamp_float((float)wrapped, (float)(-var3), (float)var3);
        return var1 + f4;
    }

    public void applyNewWatchdogRotations(BlockPos pos, float var2) {
        boolean notAbs;
        this.getInterpolatedEyePosition(Scaffold.aEg.thePlayer, Scaffold.aEg.timer.bWm);
        Vector3d akax2 = new Vector3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        float f6 = Scaffold.aEg.thePlayer.pl;
        double radians = Math.toRadians(f6);
        Vector3d akaxx = new Vector3d(-Math.sin(radians), 0.0, Math.cos(radians));
        double d5 = akaxx.wg();
        if (d5 != 0.0) {
            akaxx = new Vector3d(akaxx.x / d5, 0.0, akaxx.z / d5);
        }
        boolean toRadians2 = Math.cos(Math.toRadians(f6)) * 1.0 < 0.0;
        Vector3d akaxxx = new Vector3d(akaxx.z, 0.0, -akaxx.x);
        double d6 = akaxxx.wg();
        if (d6 != 0.0) {
            akaxxx = new Vector3d(akaxxx.x / d6, 0.0, akaxxx.z / d6);
        }
        double d7 = -0.49999999;
        Vector3d akaxxxx = akax2.e(akaxxx.ag(-d7));
        Vector3d akaxxxxx = akaxxxx.v(0.0, (Math.random() - 0.5) * 0.05, 0.0);
        float[] afloat = this.rotationsBetween(akax2, akaxxxxx);
        float f7 = this.targetYaw;
        float f8 = this.targetPitch;
        float f9 = this.c(f7, afloat[0], var2);
        float f10 = this.c(f8, afloat[1], var2);
        this.targetYaw = f9;
        this.targetPitch = f10;
        RotationComponent.d((boolean)false);
        float wrapped = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
        boolean bl = notAbs = Math.abs(wrapped % 90.0f) <= 10.0f || Math.abs(wrapped % 90.0f) >= 80.0f;
        if (!(Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() || Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() || MoveUtil.enoughMovementForSprinting() && !(MoveUtil.speed() < 0.09) && !notAbs)) {
            if (!(!(MoveUtil.speed() < 0.21) && Scaffold.aEg.thePlayer.onGround || Scaffold.aEg.gameSettings.keyBindJump.isKeyDown())) {
                RotationComponent.setRotations((Vector2f)new Vector2f(this.targetYaw - 30.0f, this.targetPitch), (double)10.0, (MovementFix)((Boolean)this.movementCorrection.wo() != false ? MovementFix.NORMAL : MovementFix.OFF));
            } else if (MoveUtil.speed() < 0.3 && !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown()) {
                RotationComponent.setRotations((Vector2f)new Vector2f(this.targetYaw - 45.0f, this.targetPitch), (double)10.0, (MovementFix)((Boolean)this.movementCorrection.wo() != false ? MovementFix.NORMAL : MovementFix.OFF));
            }
            if (Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && ((Speed)this.e(Speed.class)).isEnabled()) {
                RotationComponent.setRotations((Vector2f)new Vector2f(this.targetYaw - 10.0f, this.targetPitch), (double)10.0, (MovementFix)((Boolean)this.movementCorrection.wo() != false ? MovementFix.NORMAL : MovementFix.OFF));
            } else if (Scaffold.aEg.gameSettings.keyBindJump.isKeyDown()) {
                RotationComponent.setRotations((Vector2f)new Vector2f(this.targetYaw - 10.0f, this.targetPitch), (double)10.0, (MovementFix)((Boolean)this.movementCorrection.wo() != false ? MovementFix.NORMAL : MovementFix.OFF));
            }
        }
    }

    public float b(float var1, float var2, float var3) {
        float f1 = MathHelper.wrapAngleTo180_float((float)(var2 - var1));
        if (f1 > var3) {
            f1 = var3;
        }
        if (f1 < -var3) {
            f1 = -var3;
        }
        return var1 + f1;
    }

    public static float clampPitch(float var0) {
        if (var0 > 89.9f) {
            return 89.9f;
        }
        return var0 < -89.9f ? -89.9f : var0;
    }

    public boolean isReplaceableBelow(int var1) {
        return Scaffold.aEg.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double((double)Scaffold.aEg.thePlayer.posX), MathHelper.floor_double((double)Scaffold.aEg.thePlayer.posY) - var1, MathHelper.floor_double((double)Scaffold.aEg.thePlayer.posZ))).getBlock().isReplaceable(Scaffold.aEg.thePlayer.worldObj, new BlockPos(MathHelper.floor_double((double)Scaffold.aEg.thePlayer.posX), MathHelper.floor_double((double)Scaffold.aEg.thePlayer.posY) - var1, MathHelper.floor_double((double)Scaffold.aEg.thePlayer.posZ)));
    }

    public Vector2f findPitch(BlockPos pos, EnumFacing facing, float var3, boolean var4) {
        return this.findPitchNear(pos, facing, var3, var4, 82.0f);
    }

    public float[] rotationsBetween(Vector3d var1, Vector3d var2) {
        double d4 = var2.x - var1.x;
        double d5 = var2.y - var1.y;
        double d6 = var2.z - var1.z;
        double d7 = Math.sqrt(d4 * d4 + d6 * d6);
        float degrees = (float)Math.toDegrees(Math.atan2(d6, d4)) - 90.0f;
        float f3 = (float)(-Math.toDegrees(Math.atan2(d5, d7)));
        float wrapped = MathHelper.wrapAngleTo180_float((float)degrees);
        float f5 = MathHelper.clamp_float((float)f3, (float)-90.0f, (float)90.0f);
        return new float[]{wrapped, f5};
    }

    @Generated
    public boolean isReadyToPlace() {
        return this.readyToPlace;
    }

    public void onDisable() {
        if (Scaffold.aEg.thePlayer.ticksExisted % 2 == 0) {
            this.agx = false;
        }
        this.h(new Vector2f(Scaffold.aEg.thePlayer.pl, Scaffold.aEg.thePlayer.rotationPitch));
        this.resetAllKeyBinds();
        Scaffold.aEg.thePlayer.crd = false;
    }

    public Vector2f getTellyRotation(BlockPos pos, EnumFacingOffset var2, float var3, float var4, float var5, int var6) {
        float wrapped = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
        float f3 = MathHelper.clamp_float((float)((float)(85.0 + Math.random() * 0.1)), (float)var4, (float)var5);
        return RotationUtil.m((Vector2f)new Vector2f(wrapped, f3));
    }

    public Vector2f findPitchNear(BlockPos pos, EnumFacing facing, float var3, boolean var4, float var5) {
        for (float f2 : new float[]{0.0f, 1.5f, -1.5f, 3.0f, -3.0f, 4.5f, -4.5f}) {
            float f3 = Scaffold.clampPitch(var5 + f2);
            Vector2f vector2f = new Vector2f(var3, f3);
            int k_hi = (int)(RayCastUtil.a((Vector2f)vector2f, (EnumFacing)facing, (BlockPos)pos, (boolean)var4) ? 1L : 0L);
            if (k_hi == 0) {
                MovingObjectPosition movingobjectposition = RayCastUtil.c((Vector2f)vector2f, (double)((Boolean)this.extendBlockReachOnWatchdogTelly.wo() != false ? 4.5 : 5.5));
                int n = k_hi = movingobjectposition != null && pos.equals((Object)movingobjectposition.getBlockPos()) && movingobjectposition.sideHit == facing ? 1 : 0;
            }
            if (k_hi == 0) continue;
            return vector2f;
        }
        return null;
    }

    public void updateFlagState() {
        if (Scaffold.aEg.thePlayer != null && ((Boolean)this.watchdogTelly.wo()).booleanValue() && ((Boolean)this.disableOnFlag.wo()).booleanValue()) {
            if (Scaffold.aEg.thePlayer.Zl == 1 && this.lastFlagTick != Scaffold.aEg.thePlayer.ticksExisted) {
                this.flagDisableTicks = 10;
                this.lastFlagTick = Scaffold.aEg.thePlayer.ticksExisted;
            }
        } else {
            this.flagDisableTicks = 0;
            this.lastFlagTick = -1;
        }
    }

    @Generated
    public int getTicksOnAir() {
        return this.ticksOnAir;
    }

    public void c(boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6) {
        if (var1) {
            Scaffold.aEg.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindSneak.getKeyCode()));
        }
        if (var2) {
            Scaffold.aEg.gameSettings.keyBindJump.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindJump.getKeyCode()));
        }
        if (var3) {
            Scaffold.aEg.gameSettings.keyBindRight.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindRight.getKeyCode()));
        }
        if (var4) {
            Scaffold.aEg.gameSettings.keyBindLeft.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindLeft.getKeyCode()));
        }
        if (var5) {
            Scaffold.aEg.gameSettings.keyBindForward.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindForward.getKeyCode()));
        }
        if (var6) {
            Scaffold.aEg.gameSettings.keyBindBack.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindBack.getKeyCode()));
        }
    }

    public void h(Vector2f vec2) {
        this.visualRotationsActive = false;
        this.prevVisualYaw = vec2.x;
        this.prevVisualPitch = vec2.y;
        this.visualYaw = vec2.x;
        this.visualPitch = vec2.y;
    }

    public Scaffold() {
        this.sprint = new ModeValue("Sprint", (Module)this).add(new Mode[]{new SubMode("Normal")}).add(new Mode[]{new DisabledSprint("Disabled", this)}).add(new Mode[]{new LegitSprint("Legit", this)}).add(new Mode[]{new BypassSprint("Bypass", this)}).add(new Mode[]{new VulcanSprint("Vulcan", this)}).add(new Mode[]{new VerusSprint("Verus", this)}).add(new Mode[]{new MatrixSprint("Matrix", this)}).add(new Mode[]{new WatchdogPredictiSprint("Watchdog Prediction", this)}).add(new Mode[]{new WatchdogJumpSprint("Watchdog Jump", this)}).add(new Mode[]{new WatchdogSprint("Watchdog", this)}).setDefault("Normal");
        this.tower = new ModeValue("Tower", (Module)this).add(new Mode[]{new SubMode("Disabled")}).add(new Mode[]{new VulcanTower("Vulcan", this)}).add(new Mode[]{new VanillaTower("Vanilla", this)}).add(new Mode[]{new NormalTower("Normal", this)}).add(new Mode[]{new AirJumpTower("Air Jump", this)}).add(new Mode[]{new WatchdogTower("Watchdog", this)}).add(new Mode[]{new MMCTower("MMC", this)}).add(new Mode[]{new NCPTower("NCP", this)}).add(new Mode[]{new MatrixTower("Matrix", this)}).add(new Mode[]{new LegitTower("Legit", this)}).add(new Mode[]{new VerusTower("Verus", this)}).add(new Mode[]{new WatchdogPrediction18Tower("Watchdog Prediction 1.8", this)}).setDefault("Disabled");
        this.sameY = new ModeValue("Same Y", (Module)this).add(new Mode[]{new SubMode("Off")}).add(new Mode[]{new SubMode("On")}).add(new Mode[]{new SubMode("Auto Jump")}).setDefault("Off");
        this.downwardsPressS = new ModeValue("Downwards (Press Sneak)", (Module)this).add(new Mode[]{new SubMode("Off")}).add(new Mode[]{new NormalDownwards("Normal", this)}).add(new Mode[]{new WatchdogDownwards("Watchdog", this)}).add(new Mode[]{new VerusDownwards("Verus", this)}).setDefault("Off");
        this.rotationSpeed = new BoundsNumberValue("Rotation Speed", (Module)this, (Number)5, (Number)10, (Number)0, (Number)10, (Number)1);
        this.placeDelay = new BoundsNumberValue("Place Delay", (Module)this, (Number)0, (Number)0, (Number)0, (Number)5, (Number)1);
        this.timer = new NumberValue("Timer", (Module)this, (Number)1, (Number)0.1, (Number)10, (Number)0.1);
        this.expand = new NumberValue("Expand", (Module)this, (Number)0, (Number)0, (Number)4, (Number)1);
        this.movementCorrection = new BooleanValue("Movement Correction", (Module)this, Boolean.valueOf(false));
        this.safeWalk = new BooleanValue("Safe Walk", (Module)this, Boolean.valueOf(true));
        this.newWatchdogRots = new BooleanValue("New Watchdog Rots", (Module)this, Boolean.valueOf(false));
        this.keepYBypass = new BooleanValue("Keep-Y bypass", (Module)this, Boolean.valueOf(true));
        this.watchdogPrediction = new BooleanValue("Watchdog Prediction", (Module)this, Boolean.valueOf(false));
        this.watchdogTelly = new BooleanValue("Watchdog Telly", (Module)this, Boolean.valueOf(false));
        this.visualBackRotations = new BooleanValue("Visual Back Rotations", (Module)this, Boolean.valueOf(true), () -> {
            int k;
            if (!((Boolean)this.watchdogTelly.wo()).booleanValue()) {
                k = 1;
            } else {
                int j;
                int b0 = -110;
                int i = b0 ^ 0x33;
                k = j = i - -95;
            }
            return k != 0;
        });
        this.blockDiagonalAscend = new BooleanValue("Block Diagonal Ascend", (Module)this, Boolean.valueOf(true), () -> (Boolean)this.watchdogTelly.wo() == false);
        this.dontForceRaycastOnWatchdogTelly = new BooleanValue("Don't force raycast on Watchdog Telly", (Module)this, Boolean.valueOf(false), () -> (Boolean)this.watchdogTelly.wo() == false);
        this.extendBlockReachOnWatchdogTelly = new BooleanValue("Extend Block Reach on Watchdog Telly", (Module)this, Boolean.valueOf(true), () -> (Boolean)this.watchdogTelly.wo() == false);
        this.age = new BoundsNumberValue("Watchdog Telly Jump Delay", (Module)this, (Number)0, (Number)0, (Number)0, (Number)5, (Number)1, () -> (Boolean)this.watchdogTelly.wo() == false);
        this.disableOnFlag = new BooleanValue("Disable On Flag", (Module)this, Boolean.valueOf(true), () -> (Boolean)this.watchdogTelly.wo() == false);
        this.watchdogTellyRotationSpeed = new BoundsNumberValue("Watchdog Telly Rotation Speed", (Module)this, (Number)35, (Number)38, (Number)0, (Number)180, (Number)1, () -> (Boolean)this.watchdogTelly.wo() == false);
        this.rotationBlockBoost = new BooleanValue("Rotation Block Boost", (Module)this, Boolean.valueOf(false), () -> {
            int k;
            if (!((Boolean)this.watchdogTelly.wo()).booleanValue()) {
                k = 1;
            } else {
                int j;
                int b0 = 38;
                int i = b0 + 48;
                k = j = i - 86;
            }
            return k != 0;
        });
        this.rotationBlockRotationSpeed = new BoundsNumberValue("Rotation Block Rotation Speed", (Module)this, (Number)122, (Number)128, (Number)0, (Number)180, (Number)1, () -> (Boolean)this.watchdogTelly.wo() == false || (Boolean)this.rotationBlockBoost.wo() == false);
        this.boostOnlyWhileHoldingJump = new BooleanValue("Boost Only While Holding Jump", (Module)this, Boolean.valueOf(false), () -> (Boolean)this.watchdogTelly.wo() == false || (Boolean)this.rotationBlockBoost.wo() == false);
        this.sneak = new BooleanValue("Sneak", (Module)this, Boolean.valueOf(false));
        this.startSneaking = new BoundsNumberValue("Start Sneaking", (Module)this, (Number)0, (Number)0, (Number)0, (Number)5, (Number)1, () -> (Boolean)this.sneak.wo() == false);
        this.stopSneaking = new BoundsNumberValue("Stop Sneaking", (Module)this, (Number)0, (Number)0, (Number)0, (Number)5, (Number)1, () -> (Boolean)this.sneak.wo() == false);
        this.sneakEvery = new BoundsNumberValue("Sneak every x blocks", (Module)this, (Number)1, (Number)1, (Number)1, (Number)10, (Number)1, () -> (Boolean)this.sneak.wo() == false);
        this.sneakingSpeed = new NumberValue("Sneaking Speed", (Module)this, (Number)0.2, (Number)0.2, (Number)1, (Number)0.05, () -> (Boolean)this.sneak.wo() == false);
        this.tellyOnlyOnRightClick = new BooleanValue("Telly Only on Right Click", (Module)this, Boolean.valueOf(false), () -> !((Mode)this.mode.wo()).getName().equals("Telly"));
        this.render = new BooleanValue("Render", (Module)this, Boolean.valueOf(true));
        this.advanced = new BooleanValue("Advanced", (Module)this, Boolean.valueOf(false));
        this.yawOffset = new ModeValue("Yaw Offset", (Module)this, () -> (Boolean)this.advanced.wo() == false).add(new Mode[]{new SubMode("0")}).add(new Mode[]{new SubMode("45")}).add(new Mode[]{new SubMode("-45")}).setDefault("0");
        this.agt = new BooleanValue("Ignore Speed Effect", (Module)this, Boolean.valueOf(false), () -> (Boolean)this.advanced.wo() == false);
        this.upSideDown = new BooleanValue("Up Side Down", (Module)this, Boolean.valueOf(false), () -> (Boolean)this.advanced.wo() == false);
        this.bypassRaycastWhenFalling = new BooleanValue("Bypass Raycast When Falling", (Module)this, Boolean.valueOf(false), () -> (Boolean)this.advanced.wo() == false);
        this.placeOffset = new Vector3d(0.0, 0.0, 0.0);
        this.onPacketReceiveEvent = PacketUtil::j;
        this.onPreMotionEvent = var1 -> {
            ((Flight)this.e(Flight.class)).isEnabled();
            this.placeOffset = new Vector3d(0.0, 0.0, 0.0);
            if (this.targetBlock != null && this.enumFacing != null && this.blockFace != null) {
                Scaffold.aEg.thePlayer.cHT.aX();
                if (((Number)this.timer.wo()).floatValue() != 1.0f) {
                    Scaffold.aEg.timer.dzD = ((Number)this.timer.wo()).floatValue();
                }
            }
        };
        this.onPostMotion = var1 -> {
            Vector2f vector2f;
            EnumFacingOffset aib;
            float f = 0.0f;
            Object object = null;
            Object object1 = null;
            EnumFacingOffset enumFacingOffset = aib = this.enumFacing != null ? this.enumFacing : this.lastEnumFacing;
            Vector2f vector2f2 = RotationComponent.fn != null ? new Vector2f(RotationComponent.fn) : (vector2f = RotationComponent.fk != null ? new Vector2f(RotationComponent.fk) : new Vector2f(Scaffold.aEg.thePlayer.pl, Scaffold.aEg.thePlayer.rotationPitch));
            if (((Boolean)this.watchdogTelly.wo()).booleanValue() && ((Boolean)this.visualBackRotations.wo()).booleanValue() && this.getDisplayName().equals("Telly")) {
                if (this.blockFace != null && aib != null) {
                    Vector2f vector2f3;
                    boolean notKeyDown = MoveUtil.enoughMovementForSprinting() && Scaffold.aEg.thePlayer.onGround && (Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() || Scaffold.aEg.thePlayer.cqL <= 1 || this.tellyTicks <= 3 || this.tellyJumpDelay > 0 && Scaffold.aEg.thePlayer.cqL == this.tellyJumpDelay);
                    Vector2f vector2f1 = this.findPlacementRotation(this.blockFace, aib, !RayCastUtil.a((Vector2f)vector2f, (EnumFacing)aib.getEnumFacing(), (BlockPos)this.blockFace, (boolean)this.isStrictRayCast()) && this.isStrictRayCast(), this.visualRotationsActive ? this.visualYaw : MathHelper.wrapAngleTo180_float((float)(vector2f.x - 135.0f)), this.visualRotationsActive ? this.visualPitch : 84.0f, 45.0f);
                    if (vector2f1 == null) {
                        vector2f1 = RotationUtil.a((Vector3d)new Vector3d((double)this.blockFace.getX(), (double)this.blockFace.getY(), (double)this.blockFace.getZ()), (EnumFacing)aib.getEnumFacing());
                    }
                    Vector2f vector2f22 = new Vector2f(vector2f1.x, MathHelper.clamp_float((float)vector2f1.y, (float)80.0f, (float)89.9f));
                    boolean notWrapAngleTo180_float = Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f.x - vector2f22.x))) > 22.5f || notKeyDown;
                    Vector2f vector2f4 = vector2f3 = notWrapAngleTo180_float ? vector2f22 : vector2f;
                    if (!notWrapAngleTo180_float && !this.visualRotationsActive) {
                        this.h(vector2f);
                    } else {
                        boolean wo2;
                        if (!this.visualRotationsActive) {
                            this.h(vector2f);
                        }
                        boolean bl = wo2 = this.rotationBoostPending && (Boolean)this.rotationBlockBoost.wo() != false && !Scaffold.aEg.thePlayer.onGround;
                        if (((Boolean)this.boostOnlyWhileHoldingJump.wo()).booleanValue()) {
                            wo2 = wo2 && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown();
                        }
                        float f1 = (wo2 ? this.rotationBlockRotationSpeed.wv().floatValue() : this.watchdogTellyRotationSpeed.wv().floatValue()) * (wo2 ? 0.42f : 0.3f);
                        this.prevVisualYaw = this.visualYaw;
                        this.prevVisualPitch = this.visualPitch;
                        this.visualRotationsActive = true;
                        this.visualYaw = this.b(this.visualYaw, vector2f3.x, Math.max(1.5f, f1));
                        this.visualPitch = this.b(this.visualPitch, vector2f3.y, Math.max(1.0f, f1 * 0.65f));
                        if (!notWrapAngleTo180_float && Math.abs(MathHelper.wrapAngleTo180_float((float)(this.visualYaw - vector2f.x))) <= 2.0f && Math.abs(this.visualPitch - vector2f.y) <= 2.0f) {
                            this.h(vector2f);
                        }
                    }
                } else {
                    this.h(vector2f);
                }
                if (this.visualRotationsActive && Scaffold.aEg.gameSettings.thirdPersonView != 0) {
                    Scaffold.aEg.thePlayer.prevRotationYawHead = this.prevVisualYaw;
                    Scaffold.aEg.thePlayer.rotationYawHead = this.visualYaw;
                    Scaffold.aEg.thePlayer.prevRenderYawOffset = this.prevVisualYaw;
                    Scaffold.aEg.thePlayer.renderYawOffset = this.visualYaw;
                    Scaffold.aEg.thePlayer.bjJ = this.prevVisualPitch;
                    Scaffold.aEg.thePlayer.po = this.visualPitch;
                }
            } else {
                this.h(vector2f);
            }
        };
        this.onTeleport = var0 -> {
            double d0 = var0.getPosY() - (Scaffold.aEg.thePlayer.posY - 2.0);
            int i = d0 == 0.0 ? 0 : (d0 < 0.0 ? -1 : 1);
        };
        this.onRightClick = var1 -> {
            if (((Boolean)this.tellyOnlyOnRightClick.wo()).booleanValue() && !((Mode)this.rayCast.wo()).getName().equals("Strict")) {
                var1.setCancelled();
            }
        };
        this.onPreUpdate = var1 -> {
            int i = 1804046949;
            int j = 298231907;
            int k = 1561466657;
            int i1 = 1905985305;
            int j1 = 705811684;
            if (((Mode)this.rotationMode.wo()).getName().equals("Grim") && Scaffold.aEg.thePlayer.ticksExisted % 20 == 0) {
                if (!ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17) || !ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_20_5)) {
                    // empty if block
                }
                ChatUtil.b((Object)"OnTick rotation only works correctly on versions 1.17-1.20.6. Please switch to a version in that range.", (Object[])new Object[0]);
            }
            this.placementIndex = 0;
            while (this.placementIndex <= this.extraPlacements) {
                EnumFacingOffset aibxx;
                EnumFacingOffset aibx;
                boolean moving;
                int i2;
                boolean flag5;
                boolean bl = flag5 = Scaffold.aEg.thePlayer.motionY < -0.1 && Scaffold.aEg.thePlayer.tR > 3;
                if (this.isOffsetReplaceableBelow(2) && this.isOffsetReplaceableBelow(3) && !((Speed)this.e(Speed.class)).isEnabled() && !Scaffold.aEg.gameSettings.keyBindSneak.isKeyDown() && !Objects.equals(VantageNetwork.aKB().bX(), "")) {
                    Scaffold.aEg.thePlayer.crd = (Boolean)this.safeWalk.wo();
                }
                this.c(false, false, true, true, false, false);
                if (((Boolean)this.upSideDown.wo()).booleanValue()) {
                    this.placeOffset.setY(3.0);
                }
                if (((Number)this.expand.wo()).intValue() != 0 && MoveUtil.isMoving()) {
                    double d0 = MoveUtil.direction((float)Scaffold.aEg.thePlayer.pl, (double)(Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() ? 1.0 : (Scaffold.aEg.gameSettings.keyBindBack.isKeyDown() ? -1.0 : 0.0)), (double)(Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() ? -1.0 : (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() ? 1.0 : 0.0)));
                    for (double d1 = 0.0; d1 <= (double)((Number)this.expand.wo()).intValue(); d1 += 1.0) {
                        if (!(PlayerUtil.blockAheadOfPlayer((double)d1, (double)(this.placeOffset.getY() - 0.5)) instanceof BlockAir)) continue;
                        this.placeOffset = this.placeOffset.e(new Vector3d((double)((int)(-Math.sin(d0) * (d1 + 1.0))), this.placeOffset.getY(), (double)((int)(Math.cos(d0) * (d1 + 1.0)))));
                        break;
                    }
                }
                float f = Scaffold.aEg.thePlayer.pl;
                float f1 = MathHelper.wrapAngleTo180_float((float)f);
                double d2 = MathHelper.wrapAngleTo180_double((double)(Math.toDegrees(Math.atan2(Scaffold.aEg.thePlayer.motionZ, Scaffold.aEg.thePlayer.motionX)) - 90.0));
                float f11 = Math.abs(f1 - this.targetYaw) - 100.0f;
                int n = f11 == 0.0f ? 0 : (i2 = f11 < 0.0f ? -1 : 1);
                if (Math.abs(f1 % 90.0f) <= 10.0f || !(Math.abs(f1 % 90.0f) >= 80.0f)) {
                    // empty if block
                }
                MathHelper.wrapAngleTo180_double((double)Math.toDegrees(MoveUtil.direction()));
                if (((Mode)this.sprint.wo()).getName().equals("Watchdog Jump") && (Math.abs(d2 - (double)this.targetYaw) > 85.0 || Scaffold.aEg.thePlayer.cqL < 2)) {
                    float wrapped = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
                    if (Math.abs(wrapped % 90.0f) <= 10.0f || !(Math.abs(wrapped % 90.0f) >= 80.0f)) {
                        // empty if block
                    }
                    if (!((Mode)((Scaffold)this.e(Scaffold.class)).sameY.wo()).getName().equals("On") || Scaffold.aEg.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        // empty if block
                    }
                    ((Speed)this.e(Speed.class)).isEnabled();
                    if (Scaffold.aEg.thePlayer.cqL > 1) {
                        if (Scaffold.aEg.thePlayer.Zl < 43 && !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && !Scaffold.aEg.gameSettings.keyBindJump.isPressed()) {
                            MoveUtil.stop();
                        }
                        RotationComponent.d((boolean)false);
                        if (Scaffold.aEg.thePlayer.cqL <= 10) {
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)Scaffold.aEg.thePlayer.pl - 99.99999999999999 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)10.0, (MovementFix)MovementFix.OFF);
                        } else {
                            float f3 = Scaffold.aEg.thePlayer.pl;
                            float f4 = MathHelper.wrapAngleTo180_float((float)f3);
                            boolean flag6 = Math.abs(f4 % 90.0f) <= 10.0f || Math.abs(f4 % 90.0f) >= 80.0f;
                            Scaffold.aEg.thePlayer.motionX *= 0.9895;
                            Scaffold.aEg.thePlayer.motionZ *= 0.9895;
                            float f5 = 140.0f;
                            if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown()) {
                                Scaffold.aEg.gameSettings.keyBindForward.isKeyDown();
                            }
                            if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 - 69.99999999999999 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                            if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() && !flag6) {
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 - 150.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            } else if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() && flag6) {
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 - 160.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                            if (!Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() || !Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown()) {
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 - ((double)f5 - 1.0E-14) + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                            if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && !Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 - 140.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                            if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && !Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 + 9.99999999999999 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                            if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && Scaffold.aEg.gameSettings.keyBindBack.isKeyDown()) {
                                if (((Speed)this.e(Speed.class)).isEnabled()) {
                                    Scaffold.aEg.thePlayer.motionX *= 0.9895;
                                    Scaffold.aEg.thePlayer.motionZ *= 0.9895;
                                }
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 + 190.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                            if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() && !flag6) {
                                RotationComponent.d((boolean)false);
                                RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f3 + 140.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                            }
                        }
                    } else if (MoveUtil.isMoving() || MoveUtil.speed() > 0.0) {
                        boolean flag9;
                        float f7 = Scaffold.aEg.thePlayer.pl;
                        float f9 = MathHelper.wrapAngleTo180_float((float)f7);
                        RotationComponent.d((boolean)false);
                        boolean bl2 = flag9 = Math.abs(f9 % 90.0f) <= 10.0f || Math.abs(f9 % 90.0f) >= 80.0f;
                        float f10 = Scaffold.aEg.thePlayer.ticksExisted % 2 == 0 && ((Speed)this.e(Speed.class)).isEnabled() && (Boolean)this.keepYBypass.wo() != false ? 147.0f : (((Speed)this.e(Speed.class)).isEnabled() && (Boolean)this.keepYBypass.wo() != false ? 117.0f : 140.0f);
                        if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() && flag9) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 - 99.99999999999999 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        } else if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 - 69.99999999999999 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                        if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() && !flag9) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 - 150.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        } else if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 - 160.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                        if (!Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() || !Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown()) {
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 - ((double)f10 - 1.0E-14) + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                        if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && !Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 - 140.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                        if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && !Scaffold.aEg.gameSettings.keyBindForward.isKeyDown()) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 + 9.99999999999999 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                        if (Scaffold.aEg.gameSettings.keyBindLeft.isKeyDown() && Scaffold.aEg.gameSettings.keyBindBack.isKeyDown()) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 + 190.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                        if (Scaffold.aEg.gameSettings.keyBindRight.isKeyDown() && Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() && !flag9) {
                            RotationComponent.d((boolean)false);
                            RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)f7 + 140.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)2.0, (MovementFix)MovementFix.OFF);
                        }
                    }
                }
                if (((Mode)this.sprint.wo()).getName().equals("Watchdog Jump") && !MoveUtil.isMoving() && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown()) {
                    RotationComponent.d((boolean)false);
                    RotationComponent.setRotations((Vector2f)new Vector2f((float)((double)Scaffold.aEg.thePlayer.pl - 150.0 + (Math.random() - 0.5) * 3.0), 85.0f + (float)(Math.random() * 1.0)), (double)10.0, (MovementFix)MovementFix.OFF);
                }
                if (((Mode)this.sprint.wo()).getName().equals("Watchdog Jump") && Scaffold.aEg.thePlayer.isPotionActive(Potion.moveSpeed) && Scaffold.aEg.gameSettings.keyBindSneak.isKeyDown()) {
                    Scaffold.aEg.thePlayer.motionX *= 0.75;
                    Scaffold.aEg.thePlayer.motionZ *= 0.75;
                }
                boolean bl3 = moving = (!((Mode)this.sameY.wo()).getName().equals("Off") || ((Speed)this.e(Speed.class)).isEnabled()) && !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && MoveUtil.isMoving();
                if (!((Mode)this.sprint.wo()).getName().equals("Watchdog Prediction") || Sprint.predictionDone || !Scaffold.aEg.thePlayer.onGround || Scaffold.aEg.thePlayer.cqL < 3 || Sprint.predictionTicks == 12) {
                    SlotComponent slotcomponent = (SlotComponent)this.d(SlotComponent.class);
                    SlotComponent.b((int)SlotUtil.vx(), (boolean)((Boolean)this.render.wo()));
                }
                if (Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && !MoveUtil.isMoving() && !((Speed)this.e(Speed.class)).isEnabled()) {
                    this.ticksOnAir = !this.isOffsetReplaceableBelow(1) || moving && (!this.isOffsetReplaceableBelow(2) || !this.isOffsetReplaceableBelow(3)) ? 0 : ++this.ticksOnAir;
                } else {
                    boolean flag7;
                    if (moving) {
                        flag7 = this.isReplaceableAtStartY();
                    } else {
                        boolean bl4 = flag7 = this.isReplaceableBelow(1) && (!moving || this.isReplaceableAtStartY());
                    }
                    this.ticksOnAir = flag7 ? ++this.ticksOnAir : 0;
                }
                int n2 = this.predictionPlaceDelay = Scaffold.aEg.thePlayer.tR >= 2 && !(Math.random() > 0.3) ? 1 : 0;
                if (!((Boolean)this.watchdogPrediction.wo()).booleanValue()) {
                    int l1 = flag5 ? 0 : (int)MathUtil.l((double)((Number)this.placeDelay.wo()).intValue(), (double)this.placeDelay.wA().intValue());
                    this.readyToPlace = !BadPacketsComponent.bad((boolean)false, (boolean)true, (boolean)false, (boolean)false, (boolean)true) && this.ticksOnAir > l1;
                } else {
                    this.readyToPlace = !BadPacketsComponent.bad((boolean)false, (boolean)true, (boolean)false, (boolean)false, (boolean)true) && this.ticksOnAir > this.predictionPlaceDelay;
                }
                BadPacketsComponent.bad((boolean)false, (boolean)true, (boolean)false, (boolean)false, (boolean)true);
                float f6 = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
                boolean flag8 = Math.abs(f6 % 90.0f) <= 10.0f || Math.abs(f6 % 90.0f) >= 80.0f;
                ++WatchdogDownwards.fullBlockTicks;
                if (Scaffold.aEg.thePlayer.posY % 1.0 != 0.0) {
                    WatchdogDownwards.fullBlockTicks = 0;
                }
                if (this.placementIndex == 0) {
                    this.calculateSneaking();
                }
                if (Scaffold.aEg.gameSettings.keyBindSneak.isKeyDown()) {
                    this.extraPlacements = 0;
                }
                if (Scaffold.aEg.gameSettings.keyBindSneak.isKeyDown() && WatchdogDownwards.fullBlockTicks >= 6 && flag8 && ((Mode)this.downwardsPressS.wo()).getName().equals("Watchdog") && (!Scaffold.aEg.gameSettings.keyBindSneak.isPressed() || Scaffold.aEg.thePlayer.ticksExisted % 3 != 1)) {
                    this.extraPlacements = 0;
                    this.placeOffset = new Vector3d(0.0, 0.0, 0.0);
                    Scaffold.aEg.thePlayer.crd = false;
                    this.placeOffset.setY(-1.0);
                }
                this.targetBlock = PlayerUtil.a((double)this.placeOffset.getX(), (double)this.placeOffset.getY(), (double)this.placeOffset.getZ(), moving ? Integer.valueOf((int)Math.floor(this.startY) + (int)this.placeOffset.getY()) : null);
                if (this.targetBlock == null) {
                    return;
                }
                EnumFacingOffset aib = PlayerUtil.a((Vec3)this.targetBlock, (this.placeOffset.getY() < 0.0 ? 1 : 0) != 0);
                boolean l_hi = false;
                if (flag5 && aib != null && aib.getEnumFacing() != EnumFacing.UP && (aibx = PlayerUtil.a((Vec3)this.targetBlock, (boolean)false)) != null && aibx.getEnumFacing() == EnumFacing.UP) {
                    aib = aibx;
                }
                if (aib != null) {
                    this.enumFacing = aib;
                    this.lastEnumFacing = aib;
                } else {
                    aibx = PlayerUtil.a((Vec3)this.targetBlock, (!(this.placeOffset.getY() < 0.0) ? 1 : 0) != 0);
                    if (aibx != null) {
                        this.enumFacing = aibx;
                        this.lastEnumFacing = aibx;
                    } else {
                        if (this.lastEnumFacing == null) {
                            this.enumFacing = null;
                            return;
                        }
                        this.enumFacing = this.lastEnumFacing;
                        l_hi = true;
                    }
                }
                aibx = this.enumFacing != null ? this.enumFacing : this.lastEnumFacing;
                Scaffold.aEg.gameSettings.keyBindSneak.isKeyDown();
                if (aibx == null || aibx.getEnumFacing().getAxis().isHorizontal()) {
                    // empty if block
                }
                BlockPos blockpos = new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord, this.targetBlock.zCoord);
                EnumFacingOffset enumFacingOffset = aibxx = this.enumFacing != null ? this.enumFacing : this.lastEnumFacing;
                if (aibxx == null) {
                    return;
                }
                this.blockFace = blockpos.add(aibxx.getOffset().xCoord, aibxx.getOffset().yCoord, aibxx.getOffset().zCoord);
                if (this.blockFace != null && aibxx.getEnumFacing() != null) {
                    if (!((Mode)this.sprint.wo()).getName().equals("Watchdog Jump") || !((Boolean)this.newWatchdogRots.wo()).booleanValue() || !MoveUtil.isMoving() && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown()) {
                        this.calculateRotations();
                    } else {
                        BlockPos blockpos1 = this.blockFace;
                        float f8 = 12.0f;
                        this.applyNewWatchdogRotations(blockpos1, f8);
                    }
                    if (this.targetBlock != null && this.enumFacing != null && this.blockFace != null) {
                        SlotComponent slotcomponent1 = (SlotComponent)this.d(SlotComponent.class);
                        if (SlotComponent.getItemStack() != null) {
                            SlotComponent slotcomponent2 = (SlotComponent)this.d(SlotComponent.class);
                            if (SlotComponent.getItemStack().getItem() instanceof ItemBlock) {
                                SlotComponent slotcomponent3 = (SlotComponent)this.d(SlotComponent.class);
                                ItemStack itemstack = SlotComponent.getItemStack();
                                if (itemstack.cWo <= 0) {
                                    ChatUtil.c((Object)"RealStackSize is less than or equal to 0", (Object[])new Object[0]);
                                }
                                if (itemstack.getItem() instanceof ItemBlock && itemstack.cWo > 0 && this.enumFacing != null) {
                                    if ((RayCastUtil.overBlock((EnumFacing)this.enumFacing.getEnumFacing(), (BlockPos)this.blockFace, (boolean)((Mode)this.rayCast.wo()).getName().equals("Strict")) || ((Mode)this.rayCast.wo()).getName().equals("Off") || l_hi && moving && Scaffold.aEg.thePlayer.onGround || flag5 && (Boolean)this.bypassRaycastWhenFalling.wo() != false || ((Mode)this.rotationMode.wo()).getName().equals("Grim") && RayCastUtil.a((Vector2f)new Vector2f(this.targetYaw, this.targetPitch), (EnumFacing)this.enumFacing.getEnumFacing(), (BlockPos)this.blockFace, (boolean)false)) && this.readyToPlace) {
                                        this.placeBlock();
                                    }
                                    if (Math.random() > 0.3 && Scaffold.aEg.objectMouseOver != null && Scaffold.aEg.objectMouseOver.typeOfHit != null && Optional.ofNullable(Scaffold.aEg.objectMouseOver.getBlockPos()).map(var1x -> var1x.equals((Object)this.blockFace)).orElse(false).booleanValue() && this.blockFace != null && Scaffold.aEg.objectMouseOver.sideHit == EnumFacing.UP && ((Mode)this.rayCast.wo()).getName().equals("Strict") && !(PlayerUtil.p((double)0.0, (double)-1.0, (double)0.0) instanceof BlockAir)) {
                                        aEg.Az();
                                    }
                                }
                                if (Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && Scaffold.aEg.thePlayer.posY % 1.0 > 0.5) {
                                    this.startY = Math.floor(Scaffold.aEg.thePlayer.posY);
                                }
                                if ((Scaffold.aEg.thePlayer.posY < this.startY || Scaffold.aEg.thePlayer.onGround) && !MoveUtil.isMoving()) {
                                    this.startY = Math.floor(Scaffold.aEg.thePlayer.posY);
                                }
                                if (Scaffold.aEg.thePlayer.cqL > 2 && Math.floor(Scaffold.aEg.thePlayer.posY) != this.startY && !(PlayerUtil.p((double)0.0, (double)-1.0, (double)0.0) instanceof BlockAir)) {
                                    this.startY = Math.floor(Scaffold.aEg.thePlayer.posY);
                                }
                                ++this.placementIndex;
                                continue;
                            }
                        }
                        return;
                    }
                    return;
                }
                return;
            }
        };
        this.onMoveInput = var1 -> {
            float f = 0.0f;
            if (!((Boolean)this.sneak.wo()).booleanValue() && !((Mode)this.sprint.wo()).getName().equals("Matrix")) {
                var1.setSneak(false);
            }
            if (this.getDisplayName().equals("Telly")) {
                boolean notAbs;
                float f1 = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
                boolean bl = notAbs = Math.abs(f1 % 90.0f) <= 10.0f || Math.abs(f1 % 90.0f) >= 80.0f;
                if (Scaffold.aEg.thePlayer.onGround && MoveUtil.enoughMovementForSprinting()) {
                    if (((Boolean)this.watchdogTelly.wo()).booleanValue()) {
                        if (Scaffold.aEg.thePlayer.cqL >= this.tellyJumpDelay) {
                            var1.setJump(true);
                        }
                    } else {
                        var1.setJump(true);
                    }
                }
                if (Scaffold.aEg.thePlayer.tR == 2 || Scaffold.aEg.thePlayer.tR == 3 || Scaffold.aEg.thePlayer.tR == 4 || Scaffold.aEg.thePlayer.tR == 5) {
                    Scaffold.aEg.gameSettings.keyBindJump.isKeyDown();
                }
            }
        };
        this.onMoveInputMedium = var1 -> {
            double d1;
            double d0 = 0.0;
            this.forward = var1.getForward();
            this.strafe = var1.getStrafe();
            if (this.blockInputTicks-- > 0) {
                var1.setForward(0.0f);
                var1.setStrafe(0.0f);
            }
            if (((Boolean)this.sneak.wo()).booleanValue() && !((d1 = ((Number)this.sneakingSpeed.wo()).doubleValue()) <= 0.2)) {
                var1.setSneakSlowDownMultiplier(d1);
            }
        };
        this.onStrafe = var1 -> {
            this.runMode();
            if (!(Objects.equals(((Mode)this.yawOffset.wo()).getName(), "0") || ((Boolean)this.movementCorrection.wo()).booleanValue() || ((Mode)this.sprint.wo()).getName().equals("Watchdog Jump"))) {
                MoveUtil.useDiagonalSpeed();
            }
            if (((Mode)this.sameY.wo()).getName().equals("Auto Jump") && Scaffold.aEg.thePlayer.onGround && MoveUtil.isMoving() && Scaffold.aEg.thePlayer.posY == this.startY) {
                Scaffold.aEg.thePlayer.jump();
            }
        };
        this.onPacketSend = var1 -> {
            Object object = null;
            Object object1 = null;
            Packet packet = var1.dq();
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement c08packetplayerblockplacement = (C08PacketPlayerBlockPlacement)packet;
                if (!c08packetplayerblockplacement.getPosition().h(new Vector3d(-1.0, -1.0, -1.0))) {
                    --this.blocksUntilSneak;
                }
                if (this.isDisabledByFlag() && this.isBlockPlacement(c08packetplayerblockplacement)) {
                    --this.flagDisableTicks;
                }
            }
        };
        this.onSprint = var1 -> {
            float f = 0.0f;
            if (!Scaffold.aEg.thePlayer.onGround) {
                boolean notAbs;
                float f1 = MathHelper.wrapAngleTo180_float((float)Scaffold.aEg.thePlayer.pl);
                boolean bl = notAbs = Math.abs(f1 % 90.0f) <= 10.0f || Math.abs(f1 % 90.0f) >= 80.0f;
                if (notAbs && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && ((Boolean)this.watchdogTelly.wo()).booleanValue() && !Scaffold.aEg.thePlayer.onGround) {
                    Scaffold.aEg.thePlayer.setSprinting(false);
                }
            }
        };
    }

    public void calculateSneaking() {
        if (!((Mode)this.downwardsPressS.wo()).getName().equals("Watchdog") && this.ticksOnAir == 0) {
            Scaffold.aEg.gameSettings.keyBindSneak.setPressed(false);
        }
        --this.sneakingTicks;
        if (((Boolean)this.sneak.wo()).booleanValue() || this.pauseTicks > 0) {
            int intValue2 = this.startSneaking.wv().intValue();
            int intValue3 = this.placeDelay.wv().intValue();
            int intValue4 = this.stopSneaking.wv().intValue();
            if (this.pauseTicks > 0) {
                --this.pauseTicks;
                this.sneakingTicks = 0;
                this.blocksUntilSneak = 0;
            }
            if (this.sneakingTicks >= 0) {
                Scaffold.aEg.gameSettings.keyBindSneak.setPressed(true);
            } else {
                if (this.ticksOnAir > 0) {
                    this.sneakingTicks = intValue4;
                }
                if ((this.ticksOnAir > 0 || PlayerUtil.p((double)(Scaffold.aEg.thePlayer.motionX * (double)intValue2), (double)-0.0784000015258789, (double)(Scaffold.aEg.thePlayer.motionZ * (double)intValue2)) instanceof BlockAir) && this.blocksUntilSneak <= 0) {
                    this.sneakingTicks = intValue2 + intValue3 + intValue4;
                    this.blocksUntilSneak = this.sneakEvery.wv().intValue();
                }
            }
        }
    }

    public Vector2f findPlacementRotation(BlockPos pos, EnumFacingOffset var2, boolean var3, float var4, float var5, float var6) {
        if (var2 == null) {
            return new Vector2f(var4, var5);
        }
        EnumFacing enumfacing = var2.getEnumFacing();
        float f13 = Scaffold.aEg.thePlayer.pl;
        float degrees = (float)Math.toDegrees(MoveUtil.direction());
        ArrayList<Float> arraylist = new ArrayList<Float>();
        arraylist.add(Float.valueOf(this.snapTo45(f13 + var6)));
        arraylist.add(Float.valueOf(this.snapTo45(f13 + 45.0f + var6)));
        arraylist.add(Float.valueOf(this.snapTo45(f13 - 45.0f + var6)));
        arraylist.add(Float.valueOf(this.snapTo45(degrees + var6)));
        ArrayList<Float> arraylist1 = new ArrayList<Float>();
        Iterator iterator = arraylist.iterator();
        while (iterator.hasNext()) {
            float f18 = ((Float)iterator.next()).floatValue();
            boolean j_hi = false;
            for (Float f19 : arraylist1) {
                if (!(Math.abs(MathHelper.wrapAngleTo180_float((float)(f18 - f19.floatValue()))) < 0.1f)) continue;
                j_hi = true;
                break;
            }
            if (j_hi) continue;
            arraylist1.add(Float.valueOf(f18));
        }
        Vector2f vector2f1 = null;
        float f16 = Float.MAX_VALUE;
        for (Float f8 : arraylist1) {
            float f14;
            float f12;
            float f10;
            float f9;
            float f11;
            float f15;
            Vector2f vector2f = this.findPitch(pos, enumfacing, f8.floatValue(), var3);
            if (vector2f == null || !((f15 = (f11 = (f9 = Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f.x - var4)))) * f9 + (f10 = Math.abs(vector2f.y - var5)) * f10) + ((f12 = Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f.x - this.snapTo45(vector2f.x))))) * 0.001f + (f14 = Math.abs(MathHelper.wrapAngleTo180_float((float)(vector2f.x - f13)))) * 5.0E-4f)) < f16)) continue;
            f16 = f15;
            vector2f1 = vector2f;
        }
        return vector2f1 != null ? vector2f1 : RotationUtil.a((Vector3d)new Vector3d((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), (EnumFacing)enumfacing);
    }

    @Generated
    public void setTicksOnAir(int var1) {
        this.ticksOnAir = var1;
    }

    public void resetAllKeyBinds() {
        this.c(true, true, true, true, true, true);
    }

    public void calculateRotations() {
        EnumFacingOffset aib;
        double d = 0.0;
        boolean i5_hi = false;
        int name = Integer.parseInt(String.valueOf(((Mode)this.yawOffset.wo()).getName()));
        double rotationSpeed = ((Number)this.rotationSpeed.wo()).doubleValue();
        double d4 = this.rotationSpeed.wA().doubleValue();
        float f6 = (float)MathUtil.l((double)rotationSpeed, (double)d4);
        MovementFix movementfix = (Boolean)this.movementCorrection.wo() != false ? MovementFix.NORMAL : MovementFix.OFF;
        EnumFacingOffset enumFacingOffset = aib = this.enumFacing != null ? this.enumFacing : this.lastEnumFacing;
        if (aib != null) {
            String s;
            switch (s = this.getDisplayName()) {
                case "Normal": {
                    Scaffold.aEg.entityRenderer.getMouseOver(1.0f);
                    if (!this.readyToPlace || Scaffold.aEg.gameSettings.keyBindPickBlock.isKeyDown() || aib == null || Scaffold.aEg.objectMouseOver.sideHit == aib.getEnumFacing() && Scaffold.aEg.objectMouseOver.getBlockPos().equals((Object)this.blockFace)) break;
                    this.D(name);
                    break;
                }
                case "Breesily": {
                    if (this.readyToPlace && aib != null) {
                        if (aib.getEnumFacing() == EnumFacing.UP) {
                            this.targetPitch = 90.0f;
                        } else {
                            Double d5 = (float)(Math.toDegrees(Math.atan2(aib.getOffset().zCoord, aib.getOffset().xCoord)) % 360.0) - 90.0f;
                            Double d6 = 80.0;
                            this.targetYaw = (float)d5.doubleValue() + this.randomYawOffset;
                            this.targetPitch = (float)d6.doubleValue() + this.randomPitchOffset;
                        }
                    } else if (Math.random() > 0.99 || this.targetPitch % 90.0f == 0.0f) {
                        this.randomYawOffset = (float)(Math.random() - 0.5);
                        this.randomPitchOffset = (float)(Math.random() - 0.5);
                    }
                    if (!Scaffold.aEg.gameSettings.keyBindForward.isKeyDown() || Scaffold.aEg.gameSettings.keyBindJump.isKeyDown()) break;
                    Double d7 = 0.0;
                    Double d8 = 0.0;
                    switch (Scaffold.aEg.thePlayer.getHorizontalFacing()) {
                        case NORTH: {
                            d7 = Scaffold.aEg.thePlayer.posX - Math.floor(Scaffold.aEg.thePlayer.posX);
                            d8 = Scaffold.aEg.thePlayer.motionZ;
                            break;
                        }
                        case EAST: {
                            d7 = Scaffold.aEg.thePlayer.posZ - Math.floor(Scaffold.aEg.thePlayer.posZ);
                            d8 = Scaffold.aEg.thePlayer.motionX;
                            break;
                        }
                        case SOUTH: {
                            d7 = 1.0 - (Scaffold.aEg.thePlayer.posX - Math.floor(Scaffold.aEg.thePlayer.posX));
                            d8 = Scaffold.aEg.thePlayer.motionZ;
                            break;
                        }
                        case WEST: {
                            d7 = 1.0 - (Scaffold.aEg.thePlayer.posZ - Math.floor(Scaffold.aEg.thePlayer.posZ));
                            d8 = Scaffold.aEg.thePlayer.motionX;
                            break;
                        }
                        default: {
                            double random = Math.random();
                            ChatUtil.b((Object)("Unknown " + random), (Object[])new Object[0]);
                        }
                    }
                    Double d10 = Math.abs(d8);
                    if (d10 < 0.086 && Math.abs(d7 - 0.5) < 0.4 && this.placeDelay.wA().intValue() <= 1) break;
                    if (d7 < 0.5 + (Math.random() - 0.5) / 10.0) {
                        Scaffold.aEg.gameSettings.keyBindLeft.setPressed(false);
                        Scaffold.aEg.gameSettings.keyBindRight.setPressed(true);
                        Scaffold.aEg.gameSettings.keyBindRight.setPressed(true);
                        break;
                    }
                    Scaffold.aEg.gameSettings.keyBindRight.setPressed(false);
                    Scaffold.aEg.gameSettings.keyBindLeft.setPressed(true);
                    break;
                }
                case "Snap": {
                    this.D(name);
                    if (aib == null || this.ticksOnAir > 0 && !RayCastUtil.a((Vector2f)RotationComponent.fk, (EnumFacing)aib.getEnumFacing(), (BlockPos)this.blockFace, (boolean)((Mode)this.rayCast.wo()).getName().equals("Strict")) && (Scaffold.aEg.thePlayer.cqL >= 2 || !Scaffold.aEg.thePlayer.onGround)) break;
                    this.targetYaw = (float)Math.toDegrees(MoveUtil.direction((float)Scaffold.aEg.thePlayer.pl, (double)this.forward, (double)this.strafe)) - (float)name;
                    break;
                }
                case "Eagle": {
                    boolean abs2;
                    Float f7 = Float.valueOf((Scaffold.aEg.thePlayer.pl + 1.0E7f) % 360.0f);
                    float f8 = f7.floatValue() - 180.0f - f7.floatValue() % 90.0f + 45.0f;
                    Float f9 = Float.valueOf(78.0f);
                    boolean bl = abs2 = Math.min(Math.abs(f7.floatValue() % 90.0f), Math.abs(90.0f - f7.floatValue()) % 90.0f) < Math.min(Math.abs(f7.floatValue() + 45.0f) % 90.0f, Math.abs(90.0f - (f7.floatValue() + 45.0f)) % 90.0f);
                    if (abs2 && RayCastUtil.c((Vector2f)new Vector2f((float)(f8 + 90.0f), (float)f9.floatValue()), (double)3.0).typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && RayCastUtil.c((Vector2f)new Vector2f((float)f8, (float)f9.floatValue()), (double)3.0).typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                        f8 += 90.0f;
                    }
                    movementfix = MovementFix.NORMAL;
                    double d2 = Math.random();
                    double d3 = Scaffold.aEg.thePlayer.onGround ? 0.5 : 0.2;
                    if (d2 > d3) {
                        SlotComponent slotcomponent = (SlotComponent)this.d(SlotComponent.class);
                        if (SlotComponent.getItemStack().getItem() instanceof ItemBlock) {
                            aEg.Az();
                        }
                    }
                    Scaffold.aEg.thePlayer.movementInput.sneak = Scaffold.aEg.thePlayer.sendQueue.doneLoadingTerrain;
                    if (Scaffold.aEg.thePlayer.tR >= 4 && MoveUtil.isMoving()) {
                        Scaffold.aEg.gameSettings.keyBindSneak.setPressed(true);
                    }
                    if (Scaffold.aEg.thePlayer.cqL == 1) {
                        Scaffold.aEg.gameSettings.keyBindSneak.setPressed(false);
                    }
                    if (!abs2) {
                        f8 += 90.0f;
                    }
                    this.targetYaw = f8 + this.randomYawOffset / 2.0f;
                    this.targetPitch = f9.floatValue() + this.randomPitchOffset / 2.0f;
                    break;
                }
                case "Telly": {
                    if (this.placementIndex == 0 && ((Boolean)this.watchdogTelly.wo()).booleanValue()) {
                        boolean enoughMovementForSprinting2;
                        boolean notAbs;
                        ++this.tellyTicks;
                        Float f10 = Float.valueOf(Scaffold.aEg.thePlayer.pl);
                        float wrapped = MathHelper.wrapAngleTo180_float((float)f10.floatValue());
                        RotationComponent.d((boolean)false);
                        boolean bl = notAbs = Math.abs(wrapped % 90.0f) <= 10.0f || Math.abs(wrapped % 90.0f) >= 80.0f;
                        if (Scaffold.aEg.thePlayer.onGround && Scaffold.aEg.thePlayer.cqL == 1) {
                            this.tellyJumpDelay = this.age.wv().intValue();
                        }
                        boolean bl2 = enoughMovementForSprinting2 = Scaffold.aEg.thePlayer.onGround && MoveUtil.enoughMovementForSprinting() && Scaffold.aEg.thePlayer.cqL == this.tellyJumpDelay - 0 && this.tellyJumpDelay > 0;
                        if (enoughMovementForSprinting2) {
                            float degrees = (float)Math.toDegrees(MoveUtil.direction());
                            Float f13 = Float.valueOf((float)(85.0 + Math.random() * 0.5));
                            this.targetYaw = degrees;
                            this.targetPitch = f13.floatValue();
                            RotationComponent.setRotations((Vector2f)RotationUtil.m((Vector2f)new Vector2f(this.targetYaw, this.targetPitch)), (double)180.0, (MovementFix)MovementFix.NORMAL);
                            boolean k6_hi = true;
                            this.rotationBoostPending = true;
                        } else {
                            boolean notKeyDown;
                            boolean bl3 = notKeyDown = !(!Scaffold.aEg.thePlayer.onGround || !MoveUtil.enoughMovementForSprinting() || (Boolean)this.blockDiagonalAscend.wo() != false && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && !notAbs || !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() && Scaffold.aEg.thePlayer.cqL > 1 && this.tellyTicks > 3);
                            if (notKeyDown) {
                                Vector2f vector2f = this.getTellyRotation(this.blockFace, aib, 360.0f, 0.0f, 90.0f, -45);
                                this.targetYaw = vector2f.x;
                                this.targetPitch = (float)((double)vector2f.y + Math.random() * 0.5);
                                RotationComponent.setRotations((Vector2f)RotationUtil.m((Vector2f)new Vector2f(this.targetYaw, this.targetPitch)), (double)10.0, (MovementFix)MovementFix.NORMAL);
                                boolean i7_hi = true;
                                this.rotationBoostPending = true;
                            } else {
                                boolean keyDown;
                                boolean kl2 = aib != null && !RayCastUtil.a((Vector2f)RotationComponent.fk, (EnumFacing)aib.getEnumFacing(), (BlockPos)this.blockFace, (boolean)this.isStrictRayCast());
                                boolean bl4 = keyDown = Scaffold.aEg.thePlayer.onGround && !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown();
                                if ((kl2 || keyDown || this.tellyTicks <= 5 || aib == null) && aib != null) {
                                    float f15;
                                    float f14;
                                    boolean wo2;
                                    Vector2f vector2f1 = this.findPlacementRotation(this.blockFace, aib, kl2 ? this.isStrictRayCast() : false, (float)((double)this.targetYaw + Math.random() * 0.5), (float)((double)this.targetPitch + Math.random() * 0.5), 45.0f);
                                    boolean bl5 = wo2 = this.rotationBoostPending && (Boolean)this.rotationBlockBoost.wo() != false && !Scaffold.aEg.thePlayer.onGround;
                                    if (((Boolean)this.boostOnlyWhileHoldingJump.wo()).booleanValue()) {
                                        boolean bl6 = wo2 = wo2 && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown();
                                    }
                                    if (wo2) {
                                        f14 = ((Number)this.rotationBlockRotationSpeed.wo()).floatValue();
                                        f15 = this.rotationBlockRotationSpeed.wA().floatValue();
                                        this.rotationBoostPending = false;
                                    } else {
                                        f14 = ((Number)this.watchdogTellyRotationSpeed.wo()).floatValue();
                                        f15 = this.watchdogTellyRotationSpeed.wA().floatValue();
                                    }
                                    this.targetYaw = this.b(this.targetYaw, vector2f1.x, (float)MathUtil.l((double)f14, (double)f15));
                                    this.targetPitch = this.b(this.targetPitch, vector2f1.y, (float)MathUtil.l((double)f14, (double)f15));
                                }
                            }
                        }
                        if (((Boolean)this.dontForceRaycastOnWatchdogTelly.wo()).booleanValue()) break;
                        if (!(Scaffold.aEg.thePlayer.ae < 10 && Scaffold.aEg.thePlayer.tR > 6 && Math.hypot(Scaffold.aEg.thePlayer.crI, Scaffold.aEg.thePlayer.crK) > 0.2 && Math.random() > 0.5 || !MoveUtil.enoughMovementForSprinting() || MoveUtil.speed() < 0.15 && !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() || Scaffold.aEg.thePlayer.isCollidedHorizontally || this.tellyTicks < 15 && Scaffold.aEg.thePlayer.tR > 8 && !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() || Scaffold.aEg.thePlayer.tR > 11 && Math.random() > 0.5)) {
                            this.rayCast.co("Normal");
                            break;
                        }
                        this.rayCast.co("Off");
                        break;
                    }
                    if (this.placementIndex == 0 && ((Boolean)this.watchdogPrediction.wo()).booleanValue()) {
                        int tR2 = Scaffold.aEg.thePlayer.tR;
                        if (tR2 == 1 || tR2 == 0) {
                            aEg.Az();
                        }
                        if (!(tR2 < 0 || Scaffold.aEg.thePlayer.tR > (((Mode)this.sameY.wo()).getName().equals("Off") ? 7 : 10) || Scaffold.aEg.thePlayer.onGround && Scaffold.aEg.gameSettings.keyBindJump.isKeyDown())) {
                            if (aib != null && !RayCastUtil.a((Vector2f)RotationComponent.fk, (EnumFacing)aib.getEnumFacing(), (BlockPos)this.blockFace, (boolean)((Mode)this.rayCast.wo()).getName().equals("Strict"))) {
                                this.D(45);
                            }
                        } else {
                            this.D(Integer.parseInt(String.valueOf(((Mode)this.yawOffset.wo()).getName())));
                            this.targetYaw = Scaffold.aEg.thePlayer.pl;
                        }
                        if ((Scaffold.aEg.thePlayer.tR > 0 || Scaffold.aEg.thePlayer.cqL >= 2) && (!Scaffold.aEg.thePlayer.onGround || !Scaffold.aEg.gameSettings.keyBindJump.isKeyDown() || Scaffold.aEg.thePlayer.cqL >= 2)) break;
                        this.readyToPlace = false;
                        break;
                    }
                    if (this.placementIndex != 0) break;
                    int tR3 = Scaffold.aEg.thePlayer.tR;
                    if (tR3 == 2 || tR3 == 0) {
                        aEg.Az();
                    }
                    if (tR3 < 3 || Scaffold.aEg.thePlayer.tR > (((Mode)this.sameY.wo()).getName().equals("Off") ? 7 : 10)) {
                        this.D(Integer.parseInt(String.valueOf(((Mode)this.yawOffset.wo()).getName())));
                        this.targetYaw = Scaffold.aEg.thePlayer.pl;
                    } else if (aib != null && !RayCastUtil.a((Vector2f)RotationComponent.fk, (EnumFacing)aib.getEnumFacing(), (BlockPos)this.blockFace, (boolean)((Mode)this.rayCast.wo()).getName().equals("Strict"))) {
                        this.D(0);
                    }
                    if (Scaffold.aEg.thePlayer.tR > 3) break;
                    this.readyToPlace = false;
                    break;
                }
                case "Godbridge": {
                    SlotComponent slotcomponent1 = (SlotComponent)this.d(SlotComponent.class);
                    if (SlotComponent.bP() instanceof ItemBlock && this.readyToPlace) {
                        aEg.Az();
                    }
                    this.targetYaw = Scaffold.aEg.thePlayer.pl - Scaffold.aEg.thePlayer.pl % 90.0f - 180.0f + (float)(45 * (Scaffold.aEg.thePlayer.pl > 0.0f ? 1 : -1));
                    this.targetPitch = 76.4f;
                    movementfix = MovementFix.TRADITIONAL;
                    Double d11 = 0.15;
                    boolean notAbs2 = Math.abs(Scaffold.aEg.thePlayer.posX % 1.0) > 1.0 - d11 || Math.abs(Scaffold.aEg.thePlayer.posX % 1.0) < d11;
                    boolean notAbs3 = Math.abs(Scaffold.aEg.thePlayer.posZ % 1.0) > 1.0 - d11 || Math.abs(Scaffold.aEg.thePlayer.posZ % 1.0) < d11;
                    Scaffold.aEg.gameSettings.keyBindRight.setPressed(notAbs2 && notAbs3 || Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindLeft.getKeyCode()));
                    Scaffold.aEg.gameSettings.keyBindBack.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindForward.getKeyCode()));
                    Scaffold.aEg.gameSettings.keyBindForward.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindBack.getKeyCode()));
                    Scaffold.aEg.gameSettings.keyBindLeft.setPressed(Keyboard.isKeyDown((int)Scaffold.aEg.gameSettings.keyBindRight.getKeyCode()));
                    ++this.godBridgeSneakTicks;
                    if (Math.abs(MathHelper.wrapAngleTo180_double((double)(this.targetYaw - RotationComponent.fn.getX()))) > 10.0) {
                        this.godBridgeSneakTicks = (int)(Math.random() * 4.0);
                        this.randomYawOffset = (float)(Math.random() - 0.5) / 10.0f;
                        this.randomPitchOffset = (float)(Math.random() - 0.5) / 10.0f;
                    }
                    if (Math.random() > 0.99) {
                        this.randomYawOffset = (float)(Math.random() - 0.5) / 10.0f;
                        this.randomPitchOffset = (float)(Math.random() - 0.5) / 10.0f;
                    }
                    if (this.godBridgeSneakTicks <= 10) {
                        Scaffold.aEg.gameSettings.keyBindSneak.setPressed(true);
                    } else if (this.godBridgeSneakTicks == 11) {
                        Scaffold.aEg.gameSettings.keyBindSneak.setPressed(false);
                    }
                    this.targetYaw += this.randomYawOffset;
                    this.targetPitch += this.randomPitchOffset;
                }
            }
            if (!(((Mode)this.rotationMode.wo()).getName().equals("Grim") || f6 == 0.0f || this.blockFace == null || this.enumFacing == null && this.lastEnumFacing == null || ((Mode)this.sprint.wo()).getName().equals("Watchdog Prediction") && Scaffold.aEg.thePlayer.onGround)) {
                RotationComponent.setRotations((Vector2f)new Vector2f(this.targetYaw, this.targetPitch), (double)f6, (MovementFix)movementfix);
            }
        }
    }

    public Vec3 getHitVec() {
        Vec3 vec3 = new Vec3((double)this.blockFace.getX() + Math.random(), (double)this.blockFace.getY() + Math.random(), (double)this.blockFace.getZ() + Math.random());
        MovingObjectPosition movingobjectposition = RayCastUtil.c((Vector2f)RotationComponent.fk, (double)Scaffold.aEg.playerController.getBlockReachDistance());
        if (this.enumFacing == null) {
            return vec3;
        }
        switch (this.enumFacing.getEnumFacing()) {
            case NORTH: {
                vec3.zCoord = this.blockFace.getZ();
                break;
            }
            case EAST: {
                vec3.xCoord = this.blockFace.getX() + 1;
                break;
            }
            case SOUTH: {
                vec3.zCoord = this.blockFace.getZ() + 1;
                break;
            }
            case WEST: {
                vec3.xCoord = this.blockFace.getX();
                break;
            }
            case DOWN: {
                vec3.yCoord = this.blockFace.getY();
                break;
            }
            case UP: {
                vec3.yCoord = this.blockFace.getY() + 1;
            }
        }
        if (movingobjectposition != null && movingobjectposition.getBlockPos() != null && movingobjectposition.hitVec != null && movingobjectposition.getBlockPos().equals((Object)this.blockFace) && movingobjectposition.sideHit == this.enumFacing.getEnumFacing()) {
            vec3 = movingobjectposition.hitVec;
        }
        return vec3;
    }

    public float snapTo45(float var1) {
        return (float)Math.round(MathHelper.wrapAngleTo180_float((float)var1) / 45.0f) * 45.0f;
    }

    public boolean shouldDelayTellyJump() {
        int j;
        if (((Boolean)this.watchdogTelly.wo()).booleanValue() && this.age.wA().intValue() > 0) {
            j = 1;
        } else {
            int i;
            int b0 = -81;
            j = i = b0 + 81;
        }
        return j != 0;
    }
}
