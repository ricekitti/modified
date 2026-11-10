package dev.rice.client.hook;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import myau.*;
import myau.mixin.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import myau.host.MayuMixins;

/** Licensed lifecycle boundary around legacy hook callbacks. Redirect fallbacks perform the original vanilla action. */
public final class MayuHooks {
    private MayuHooks() {}
    public static void beforePlayerUpdate(EntityPlayerSP self) {
        if (!HookLifecycle.enter("player", self)) return;
        try { MayuMixins.beforePlayerUpdate(self); }
        catch (RuntimeException | Error failure) { HookLifecycle.exit("player", self); throw failure; }
    }
    public static void beforePlayerUpdateArbitrated(EntityPlayerSP self) {
        if (!HookLifecycle.enter("player", self)) return;
        try { MayuMixins.beforePlayerUpdateArbitrated(self); }
        catch (RuntimeException | Error failure) { HookLifecycle.exit("player", self); throw failure; }
    }
    public static void afterPlayerUpdate(EntityPlayerSP self) {
        if (HookLifecycle.exit("player", self)) MayuMixins.afterPlayerUpdate(self);
    }
    public static boolean applySpoofedRotationBeforeRidingCheck(EntityPlayerSP player, EntityPlayerSP self) {
        if (!HookLifecycle.active()) { return player.isRiding(); }
        return MayuMixins.applySpoofedRotationBeforeRidingCheck(player, self);
    }
    public static void beforeWalkingPlayerUpdate() {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.beforeWalkingPlayerUpdate();
    }
    public static void beforeLivingUpdate() {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.beforeLivingUpdate();
    }
    public static void afterMovementInputUpdate() {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.afterMovementInputUpdate();
    }
    public static float modifyJumpYawRadians(float yawRadians, EntityLivingBase self) {
        if (!HookLifecycle.active()) { return yawRadians; }
        return MayuMixins.modifyJumpYawRadians(yawRadians, self);
    }
    public static void redirectMoveFlying(EntityLivingBase target, float strafe, float forward, float friction, EntityLivingBase self) {
        if (!HookLifecycle.active()) { target.moveFlying(strafe, forward, friction); return; }
        MayuMixins.redirectMoveFlying(target, strafe, forward, friction, self);
    }
    public static boolean beforeSetVelocity(double velocityX, double velocityY, double velocityZ, Entity self) {
        if (!HookLifecycle.active()) { return false; }
        return MayuMixins.beforeSetVelocity(velocityX, velocityY, velocityZ, self);
    }
    public static boolean modifySafeWalkFlag(boolean safeWalk, Entity self) {
        if (!HookLifecycle.active()) { return safeWalk; }
        return MayuMixins.modifySafeWalkFlag(safeWalk, self);
    }
    public static void afterRunGameLoop() {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.afterRunGameLoop();
    }
    public static void beforeRunTick(Minecraft self) {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.beforeRunTick(self);
    }
    public static void afterRunTick(Minecraft self) {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.afterRunTick(self);
    }
    public static void beforeLoadWorld(WorldClient world, String loadingMessage) {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.beforeLoadWorld(world, loadingMessage);
    }
    public static boolean beforeLeftClick(Minecraft self) {
        if (!HookLifecycle.active()) { return false; }
        return MayuMixins.beforeLeftClick(self);
    }
    public static boolean beforeRightClick() {
        if (!HookLifecycle.active()) { return false; }
        return MayuMixins.beforeRightClick();
    }
    public static boolean beforeBlockClick(Minecraft self) {
        if (!HookLifecycle.active()) { return false; }
        return MayuMixins.beforeBlockClick(self);
    }
    public static void redirectHotbarScroll(InventoryPlayer inventoryPlayer, int scrollDirection) {
        if (!HookLifecycle.active()) { inventoryPlayer.changeCurrentItem(scrollDirection); return; }
        MayuMixins.redirectHotbarScroll(inventoryPlayer, scrollDirection);
    }
    public static boolean onIsPressed(boolean returnValue, KeyBinding self) {
        if (!HookLifecycle.active()) { return returnValue; }
        return MayuMixins.onIsPressed(returnValue, self);
    }
    public static ItemStack redirectCurrentHotbarItem(InventoryPlayer inventoryPlayer) {
        if (!HookLifecycle.active()) { return inventoryPlayer.getCurrentItem(); }
        return MayuMixins.redirectCurrentHotbarItem(inventoryPlayer);
    }
    public static void beforeUpdateCameraAndRender(float partialTicks, long nanoTime, EntityRenderer self) {
        if (!HookLifecycle.enter("camera", self)) return;
        try { MayuMixins.beforeUpdateCameraAndRender(partialTicks, nanoTime, self); }
        catch (RuntimeException | Error failure) { HookLifecycle.exit("camera", self); throw failure; }
    }
    public static void afterUpdateCameraAndRender(float partialTicks, long nanoTime, EntityRenderer self) {
        if (HookLifecycle.exit("camera", self)) MayuMixins.afterUpdateCameraAndRender(partialTicks, nanoTime, self);
    }
    public static void beforeUpdateRenderer(EntityRenderer self) {
        if (!HookLifecycle.enter("renderer", self)) return;
        try { MayuMixins.beforeUpdateRenderer(self); }
        catch (RuntimeException | Error failure) { HookLifecycle.exit("renderer", self); throw failure; }
    }
    public static void afterUpdateRenderer(EntityRenderer self) {
        if (HookLifecycle.exit("renderer", self)) MayuMixins.afterUpdateRenderer(self);
    }
    public static void afterRenderTitle(float partialTicks) {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.afterRenderTitle(partialTicks);
    }
    public static boolean onChannelRead(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        if (!HookLifecycle.active()) { return false; }
        return MayuMixins.onChannelRead(channelHandlerContext, packet);
    }
    public static boolean onSendPacket(Packet<?> packet) {
        if (!HookLifecycle.active()) { return false; }
        return MayuMixins.onSendPacket(packet);
    }
    public static boolean onSendPacketWithListeners(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>[] additionalListeners) {
        if (!HookLifecycle.active()) { return false; }
        // The legacy Java 8 artifact exposes an intersection-capture quirk here.
        // The erased descriptor is the same, so keep the adapter boundary raw.
        return MayuMixins.onSendPacketWithListeners(packet, (GenericFutureListener) listener,
            (GenericFutureListener[]) additionalListeners);
    }
    public static void afterRenderWorld(RenderGlobal renderGlobal, float partialTicks) {
        if (!HookLifecycle.active()) { return; }
        MayuMixins.afterRenderWorld(renderGlobal, partialTicks);
    }
    public static void beforeRenderEntityStatic(Entity entity, float partialTicks, boolean hideDebugBox, RenderManager self) {
        if (!HookLifecycle.enter("entity-render", entity)) return;
        try { MayuMixins.beforeRenderEntityStatic(entity, partialTicks, hideDebugBox, self); }
        catch (RuntimeException | Error failure) { HookLifecycle.exit("entity-render", entity); throw failure; }
    }
    public static void afterRenderEntityStatic(Entity entity, float partialTicks, boolean hideDebugBox, RenderManager self) {
        if (HookLifecycle.exit("entity-render", entity)) MayuMixins.afterRenderEntityStatic(entity, partialTicks, hideDebugBox, self);
    }
}
