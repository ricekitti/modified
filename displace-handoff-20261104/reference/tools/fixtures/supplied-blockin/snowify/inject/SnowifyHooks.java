/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.multiplayer.PlayerControllerMP
 *  net.minecraft.client.multiplayer.WorldClient
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.network.NetworkManager
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer
 *  net.minecraft.network.play.server.S02PacketChat
 *  net.minecraft.network.play.server.S04PacketEntityEquipment
 *  net.minecraft.network.play.server.S0DPacketCollectItem
 *  net.minecraft.network.play.server.S12PacketEntityVelocity
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.ChatStyle
 *  net.minecraft.util.EnumChatFormatting
 *  net.minecraft.util.IChatComponent
 *  net.minecraft.util.MovementInput
 *  net.minecraft.world.World
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 */
package com.snowify.inject;

import com.snowify.Snowify;
import com.snowify.anticheat.Anticheat;
import com.snowify.antiinvis.AntiInvis;
import com.snowify.antipearl.core.PacketRotation;
import com.snowify.antipearl.core.RotationManager;
import com.snowify.antipearl.event.AntiPearlHandler;
import com.snowify.autochest.AutoChest;
import com.snowify.bednuker.BedNuker;
import com.snowify.blockin.BlockIn;
import com.snowify.buffer.AutoBuffer;
import com.snowify.clutch.ClutchAssist;
import com.snowify.core.event.ClientChatReceivedEvent;
import com.snowify.core.event.ClientDisconnectEvent;
import com.snowify.core.event.EntityJoinWorldEvent;
import com.snowify.core.event.GuiScreenEvent;
import com.snowify.core.event.InputEvent;
import com.snowify.core.event.RenderGameOverlayEvent;
import com.snowify.core.event.RenderWorldLastEvent;
import com.snowify.core.event.TickEvent;
import com.snowify.core.event.WorldEvent;
import com.snowify.displace.Displace;
import com.snowify.displace.WireRotation;
import com.snowify.fastmine.FastMine;
import com.snowify.ghosthand.GhostHand;
import com.snowify.gui.ProgressBar;
import com.snowify.ignorefatigue.IgnoreFatigue;
import com.snowify.lazify.core.OverlayManager;
import com.snowify.lunar.SnowifyLunar;
import com.snowify.lunarspoofer.LunarSpoofer;
import com.snowify.nickhider.NickHider;
import com.snowify.opsec.PingBoost;
import com.snowify.resourcetracker.ResourceTracker;
import com.snowify.script.ScriptManager;
import com.snowify.stasis.Stasis;
import com.snowify.teamupgrades.TeamUpgrades;
import com.snowify.util.AboveNametag;
import com.snowify.util.GameAccess;
import com.snowify.util.GlSave;
import com.snowify.util.InboundPackets;
import com.snowify.util.InputLock;
import com.snowify.util.ServerRotationRender;
import com.snowify.util.TickActions;
import com.snowify.velobuffer.VeloBuffer;
import com.snowify.velocity.Velocity;
import com.snowify.voidresources.VoidResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class SnowifyHooks {
    private SnowifyHooks() {
    }

    public static void start(Object mc) {
        try {
            SnowifyLunar.start((Minecraft)mc);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("start", t);
        }
    }

    public static void clientTickStart() {
        try {
            Snowify.EVENTS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("clientTickStart", t);
        }
    }

    public static void clientTickEnd() {
        try {
            com.snowify.accounts.AccountUiBridge.tick();
            Snowify.EVENTS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("clientTickEnd", t);
        }
    }

    public static void keyInput() {
        try {
            Snowify.EVENTS.post(new InputEvent.KeyInputEvent());
        }
        catch (Throwable t) {
            SnowifyHooks.fail("keyInput", t);
        }
    }

    public static void renderTickStart() {
        try {
            Snowify.EVENTS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, GameAccess.renderPartialTicks()));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("renderTickStart", t);
        }
    }

    public static void renderTickEnd() {
        try {
            com.snowify.accounts.AccountUiBridge.draw();
            Snowify.EVENTS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, GameAccess.renderPartialTicks()));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("renderTickEnd", t);
        }
    }

    public static void renderWorldLast() {
        try {
            GlSave saved = GlSave.capture();
            try {
                Snowify.EVENTS.post(new RenderWorldLastEvent(GameAccess.renderPartialTicks()));
            }
            finally {
                saved.restore();
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("renderWorldLast", t);
        }
    }

    public static void worldLoad(Object world2) {
        try {
            if (world2 != null) {
                Snowify.EVENTS.post(new WorldEvent.Load((World)((WorldClient)world2)));
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("worldLoad", t);
        }
    }

    public static void playerTickStart(Object player) {
        try {
            Snowify.EVENTS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, (EntityPlayer)player));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("playerTickStart", t);
        }
    }

    public static void playerTickEnd(Object player) {
        try {
            Snowify.EVENTS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, (EntityPlayer)player));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("playerTickEnd", t);
        }
    }

    public static boolean command(Object message) {
        try {
            return SnowifyLunar.onChatSent((String)message);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("command", t);
            return false;
        }
    }

    public static void screenDrawn() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                return;
            }
            ScaledResolution res = new ScaledResolution(mc);
            int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
            int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
            Snowify.EVENTS.post(new GuiScreenEvent.DrawScreenEvent.Post(mc.currentScreen, mouseX, mouseY, GameAccess.renderPartialTicks()));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("screenDrawn", t);
        }
    }

    public static void overlayStart() {
        try {
            NickHider.beginHud();
        }
        catch (Throwable t) {
            SnowifyHooks.fail("overlayStart", t);
        }
    }

    public static void overlay() {
        try {
            NickHider.endHud();
            ProgressBar.newFrame();
            Snowify.EVENTS.post(new RenderGameOverlayEvent.Post(GameAccess.renderPartialTicks(), new ScaledResolution(Minecraft.getMinecraft()), RenderGameOverlayEvent.ElementType.ALL));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("overlay", t);
        }
    }

    public static boolean clickMouse() {
        try {
            if (ClutchAssist.blocksLeftClick()) {
                return true;
            }
            if (BedNuker.blocksLeftClick()) {
                return true;
            }
            if (RotationManager.get().isEngaged()) {
                return true;
            }
            Displace.noteClick();
            AutoBuffer.onAttackClick();
            if (InputLock.locked()) {
                return true;
            }
            AutoBuffer.onInteract();
            VeloBuffer.onInteract();
            return false;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("clickMouse", t);
            return false;
        }
    }

    public static boolean rightClickMouse() {
        try {
            if (BedNuker.blocksRightClick()) {
                return true;
            }
            if (InputLock.locked() || ClutchAssist.blocksRightClick()) {
                return true;
            }
            if (Stasis.holdThrow()) {
                return true;
            }
            AutoBuffer.onInteract();
            VeloBuffer.onInteract();
            return RotationManager.get().isEngaged();
        }
        catch (Throwable t) {
            SnowifyHooks.fail("rightClickMouse", t);
            return false;
        }
    }

    public static boolean holdMining() {
        try {
            return RotationManager.get().isEngaged() || InputLock.locked();
        }
        catch (Throwable t) {
            SnowifyHooks.fail("holdMining", t);
            return false;
        }
    }

    public static void afterMouseOver(Object mc) {
        try {
            BedNuker.afterMouseOver((Minecraft)mc);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("afterMouseOver", t);
        }
        try {
            GhostHand.afterMouseOver((Minecraft)mc);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("afterMouseOver", t);
        }
    }

    public static void fastMineBeforeClickBlock(Object mc) {
        try {
            FastMine.beforeClickBlock((Minecraft)mc);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("fastMineBeforeClickBlock", t);
        }
    }

    public static void fastMineAfterClickBlock(Object mc) {
        try {
            FastMine.afterClickBlock((Minecraft)mc);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("fastMineAfterClickBlock", t);
        }
    }

    public static void playerSpPreUpdate(Object self) {
        try {
            EntityPlayerSP sp = (EntityPlayerSP)self;
            if (sp.worldObj.isBlockLoaded(new BlockPos(sp.posX, 0.0, sp.posZ))) {
                Velocity.onPreUpdate();
            }
            RotationManager.get().noteUpdate(sp.ridingEntity != null);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("playerSpPreUpdate", t);
        }
    }

    public static void fallbackInputCorrection(Object self) {
        try {
            float[] corrected;
            EntityPlayerSP sp = (EntityPlayerSP)self;
            RotationManager rotations = RotationManager.get();
            if (!rotations.isLocalPlayer(sp) || rotations.takeInputCorrected()) {
                return;
            }
            if (rotations.shouldFixMovement() && (corrected = RotationManager.correctedInput(sp.rotationYaw, rotations.getYaw(), sp.moveForward, sp.moveStrafing, sp.movementInput != null && sp.movementInput.sneak)) != null) {
                sp.moveForward = corrected[0];
                sp.moveStrafing = corrected[1];
                if (sp.isSprinting() && sp.moveForward < 0.8f) {
                    sp.setSprinting(false);
                }
            }
            Displace.adjustMovement(sp);
            AutoBuffer.adjustMovement(sp);
            Stasis.adjustMovement(sp);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("fallbackInputCorrection", t);
        }
    }

    public static void beforeMovementPacket(Object self) {
        try {
            if (RotationManager.get().isLocalPlayer((Entity)self)) {
                ClutchAssist.beforeMovementPacket();
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("beforeMovementPacket", t);
        }
    }

    public static void afterWalkingPlayer(Object self) {
        try {
            RotationManager rotations = RotationManager.get();
            rotations.noteWalkingPlayer();
            AntiPearlHandler.afterMovementPacket();
            ScriptManager.onPostMotion();
            Stasis.afterMovementPacket();
            rotations.endTick();
        }
        catch (Throwable t) {
            SnowifyHooks.fail("afterWalkingPlayer", t);
        }
    }

    public static float spoofYaw(Object self) {
        try {
            RotationManager r = RotationManager.get();
            return r.isActive() ? r.getYaw() : ((EntityPlayerSP)self).rotationYaw;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("spoofYaw", t);
            return ((Entity)self).rotationYaw;
        }
    }

    public static float spoofPitch(Object self) {
        try {
            RotationManager r = RotationManager.get();
            return r.isActive() ? r.getPitch() : ((EntityPlayerSP)self).rotationPitch;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("spoofPitch", t);
            return ((Entity)self).rotationPitch;
        }
    }

    public static float moveFlyingYaw(Object self) {
        try {
            RotationManager r = RotationManager.get();
            Entity e = (Entity)self;
            return r.shouldFixMovement() && r.isLocalPlayer(e) ? r.getYaw() : e.rotationYaw;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("moveFlyingYaw", t);
            return ((Entity)self).rotationYaw;
        }
    }

    public static float jumpYaw(Object self) {
        try {
            RotationManager r = RotationManager.get();
            EntityLivingBase e = (EntityLivingBase)self;
            return r.shouldFixMovement() && r.isLocalPlayer(e) ? r.getYaw() : e.rotationYaw;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("jumpYaw", t);
            return ((Entity)self).rotationYaw;
        }
    }

    public static void movementYawIn(Object self) {
        try {
            RotationManager.get().beginMovementYaw((Entity)((EntityLivingBase)self));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("movementYawIn", t);
        }
    }

    public static void movementYawOut(Object self) {
        try {
            RotationManager.get().endMovementYaw((Entity)((EntityLivingBase)self));
        }
        catch (Throwable t) {
            SnowifyHooks.fail("movementYawOut", t);
        }
    }

    public static void correctMovement() {
        try {
            MovementInput in;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && mc.thePlayer.movementInput != null) {
                RotationManager.get().noteInputCorrected();
                try {
                    SnowifyHooks.antipearlFix(mc);
                }
                finally {
                    in = mc.thePlayer.movementInput;
                    Displace.adjustInput(in);
                    AutoBuffer.adjustInput(in);
                    ScriptManager.adjustInput(in);
                    BlockIn.adjustInput(in);
                    Stasis.adjustInput(in);
                }
            }
            if (VoidResources.isBlockingMovement() && mc.thePlayer != null && mc.thePlayer.movementInput != null) {
                in = mc.thePlayer.movementInput;
                in.moveForward = 0.0f;
                in.moveStrafe = 0.0f;
                in.jump = false;
                in.sneak = false;
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("correctMovement", t);
        }
    }

    private static void antipearlFix(Minecraft mc) {
        RotationManager rotations = RotationManager.get();
        if (!rotations.shouldFixMovement()) {
            return;
        }
        float[] corrected = RotationManager.correctedInput(mc.thePlayer.rotationYaw, rotations.getYaw(), mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.sneak);
        if (corrected == null) {
            return;
        }
        mc.thePlayer.movementInput.moveForward = corrected[0];
        mc.thePlayer.movementInput.moveStrafe = corrected[1];
    }

    public static boolean outgoingPacket(Object packet) {
        try {
            Packet p = (Packet)packet;
            if (p instanceof C03PacketPlayer) {
                boolean forced;
                C03PacketPlayer movement = (C03PacketPlayer)p;
                RotationManager rotations = RotationManager.get();
                boolean bl = forced = rotations.isActive() && (rotations.isEngaged() || rotations.isPlacing() || ClutchAssist.forcesLook());
                if (forced) {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc.thePlayer != null) {
                        WireRotation.forceNextMovementLook(rotations.getYaw(), rotations.getPitch(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                    }
                }
                if (!movement.getRotating()) {
                    if (forced) {
                        rotations.recordSent(rotations.getYaw(), rotations.getPitch(), true);
                    }
                } else if (rotations.isActive() && PacketRotation.apply(movement, rotations.getYaw(), rotations.getPitch())) {
                    rotations.recordSent(rotations.getYaw(), rotations.getPitch(), true);
                } else {
                    rotations.recordSent(movement.getYaw(), movement.getPitch(), false);
                }
            }
            TickActions.sent(p);
            if (ScriptManager.onPacketSent(p)) {
                return true;
            }
            if (Stasis.onPacketSend(p)) {
                return true;
            }
            Displace.onQueued(p);
            if (Velocity.onPacketSend(p)) {
                return true;
            }
            return PingBoost.outbound(p);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("outgoingPacket", t);
            return false;
        }
    }

    public static void velocityHandled(Object packet) {
        try {
            S12PacketEntityVelocity p = (S12PacketEntityVelocity)packet;
            InboundPackets.noteHandled(p);
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                Stasis.onVelocityHandled(p);
                Velocity.onVelocityHandled(p);
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("velocityHandled", t);
        }
    }

    public static void collected(Object packet) {
        try {
            S0DPacketCollectItem p = (S0DPacketCollectItem)packet;
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                ResourceTracker.onCollect(p);
                TeamUpgrades.onCollect(p);
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("collected", t);
        }
    }

    public static void equipment(Object packet) {
        try {
            S04PacketEntityEquipment p = (S04PacketEntityEquipment)packet;
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                TeamUpgrades.onEquipment(p);
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("equipment", t);
        }
    }

    public static void inboundHandled(Object packet) {
        try {
            InboundPackets.noteHandled((Packet)packet);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("inboundHandled", t);
        }
    }

    public static boolean inbound(Object manager, Object packet) {
        try {
            if (!GameAccess.isClientConnection((NetworkManager)manager)) {
                return false;
            }
            return InboundPackets.intercept((Packet)packet);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("inbound", t);
            return false;
        }
    }

    public static boolean chat(Object packet) {
        try {
            if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                return false;
            }
            S02PacketChat c = (S02PacketChat)packet;
            ClientChatReceivedEvent event = Snowify.EVENTS.post(new ClientChatReceivedEvent(c.getType(), c.getChatComponent()));
            return event.isCanceled();
        }
        catch (Throwable t) {
            SnowifyHooks.fail("chat", t);
            return false;
        }
    }

    public static void disconnect() {
        try {
            Snowify.EVENTS.post(new ClientDisconnectEvent());
        }
        catch (Throwable t) {
            SnowifyHooks.fail("disconnect", t);
        }
    }

    public static void entityJoin(Object world2, Object entity) {
        try {
            World self = (World)world2;
            if (self.isRemote) {
                Snowify.EVENTS.post(new EntityJoinWorldEvent((Entity)entity, self));
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("entityJoin", t);
        }
    }

    public static boolean attack(Object player, Object target) {
        try {
            if (Velocity.reduceAttacking()) {
                return false;
            }
            Entity t = (Entity)target;
            return Displace.onAttack(t) || Velocity.onAttack(t);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("attack", t);
            return false;
        }
    }

    public static void digStart(Object controller, Object pos) {
        try {
            PlayerControllerMP c = (PlayerControllerMP)controller;
            BlockPos p = (BlockPos)pos;
            if (!GameAccess.hittingBlock(c) || !p.equals((Object)GameAccess.currentBlock(c))) {
                IgnoreFatigue.onDigStart(p);
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("digStart", t);
        }
    }

    public static void digEnd(Object pos) {
        try {
            IgnoreFatigue.onDigEnd((BlockPos)pos);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("digEnd", t);
        }
    }

    public static void revealBefore(Object entity) {
        try {
            EntityLivingBase e = (EntityLivingBase)entity;
            AntiInvis.beforeRender((Entity)e);
            ServerRotationRender.beforeRender(e);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("revealBefore", t);
        }
    }

    public static void revealAfter(Object entity) {
        try {
            EntityLivingBase e = (EntityLivingBase)entity;
            AntiInvis.afterRender((Entity)e);
            ServerRotationRender.afterRender(e);
        }
        catch (Throwable t) {
            SnowifyHooks.fail("revealAfter", t);
        }
    }

    public static void screenMouseInput() {
        try {
            int button;
            int wheel = Mouse.getEventDWheel();
            if (wheel != 0) {
                AutoChest.onScroll(wheel);
            }
            if ((button = Mouse.getEventButton()) >= 0 && Mouse.getEventButtonState()) {
                AutoChest.onKey(button - 100);
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("screenMouseInput", t);
        }
    }

    public static void screenKeyboardInput() {
        try {
            if (Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
                AutoChest.onKey(Keyboard.getEventKey());
            }
        }
        catch (Throwable t) {
            SnowifyHooks.fail("screenKeyboardInput", t);
        }
    }

    public static String nickText(String text) {
        try {
            return NickHider.transform(text);
        }
        catch (Throwable t) {
            return text;
        }
    }

    public static int expLevel(Object player) {
        try {
            return NickHider.level(((EntityPlayer)player).experienceLevel);
        }
        catch (Throwable t) {
            return ((EntityPlayer)player).experienceLevel;
        }
    }

    public static float expFill(Object player) {
        try {
            return NickHider.fill(((EntityPlayer)player).experience);
        }
        catch (Throwable t) {
            return ((EntityPlayer)player).experience;
        }
    }

    public static Object displayName(Object retObj, Object selfObj) {
        try {
            IChatComponent name = (IChatComponent)retObj;
            EntityPlayer self = (EntityPlayer)selfObj;
            String extra = AboveNametag.inlineText(self);
            if (name != null && extra != null) {
                ChatComponentText withExtra = new ChatComponentText("");
                withExtra.appendSibling(name);
                withExtra.appendSibling((IChatComponent)new ChatComponentText(extra));
                name = withExtra;
            }
            if (name != null && Anticheat.marksNametag(self)) {
                ChatComponentText marked = new ChatComponentText("");
                marked.appendSibling(new ChatComponentText("\u26a0 ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
                marked.appendSibling(name);
                name = marked;
            }
            return name;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("displayName", t);
            return retObj;
        }
    }

    public static Object playerName(Object retObj, Object infoObj) {
        try {
            String stats;
            String name = (String)retObj;
            NetworkPlayerInfo info = (NetworkPlayerInfo)infoObj;
            String decorated = Anticheat.decorateTab(info, LunarSpoofer.decorate(info, name));
            if (info != null && info.getGameProfile() != null && (stats = OverlayManager.tabStats(info.getGameProfile().getId())) != null) {
                decorated = decorated + stats;
            }
            return decorated;
        }
        catch (Throwable t) {
            SnowifyHooks.fail("playerName", t);
            return retObj;
        }
    }

    public static Object skin(Object info) {
        try {
            return NickHider.skin(((NetworkPlayerInfo)info).getGameProfile());
        }
        catch (Throwable t) {
            SnowifyHooks.fail("skin", t);
            return null;
        }
    }

    public static Object skinModel(Object info) {
        try {
            return NickHider.skinModel(((NetworkPlayerInfo)info).getGameProfile());
        }
        catch (Throwable t) {
            SnowifyHooks.fail("skinModel", t);
            return null;
        }
    }

    private static void fail(String hook, Throwable t) {
        System.err.println("[Snowify] hook " + hook + " threw: " + t);
    }
}
