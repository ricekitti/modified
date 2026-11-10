/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.command.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import valen.command.Command;
import valen.command.CommandInput;
import valen.lunar.forge.client.event.RenderWorldLastEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.fml.common.eventhandler.EventPriority;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.module.ModuleManager;
import valen.module.impl.world.AntiBot;
import valen.utility.RenderUtils;
import valen.utility.Utils;

public class Track
extends Command {
    public final List<EntityPlayer> trackedPlayers = new ArrayList<EntityPlayer>();

    public Track() {
        super("track", new String[0]);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() == 0) {
            if (this.trackedPlayers.isEmpty()) {
                this.replyWithHeader("&b0 &7players tracked.");
                return;
            }
            this.replyWithHeader("&7Tracking &b" + this.trackedPlayers.size() + " &7player" + (this.trackedPlayers.size() == 1 ? "" : "s") + ".");
            for (EntityPlayer entityPlayer : this.trackedPlayers) {
                this.replyWithHeader(" &b" + entityPlayer.getName());
            }
            return;
        }
        if (commandInput.argumentCount() != 1) {
            this.syntaxError();
            return;
        }
        String string = commandInput.getArgument(0);
        if ("clear".equalsIgnoreCase(string)) {
            this.replyWithHeader("&b" + this.trackedPlayers.size() + " &7player" + (this.trackedPlayers.size() == 1 ? "" : "s") + " cleared.");
            this.trackedPlayers.clear();
            return;
        }
        EntityPlayer entityPlayer = Track.mc.theWorld.getPlayerEntityByName(string);
        if (entityPlayer == Track.mc.thePlayer) {
            this.replyWithHeader("&cYou cannot track yourself.");
            return;
        }
        if (entityPlayer == null) {
            this.replyWithHeader("&b" + string + " &7not found.");
            return;
        }
        if (this.trackedPlayers.contains(entityPlayer)) {
            this.trackedPlayers.remove(entityPlayer);
            this.replyWithHeader("&7Stopped tracking &b" + string);
            return;
        }
        this.trackedPlayers.add(entityPlayer);
        this.replyWithHeader("&7Started tracking &b" + string);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onRenderWorld(RenderWorldLastEvent renderWorldLastEvent) {
        if (!Utils.nullCheck() || this.trackedPlayers.isEmpty()) {
            return;
        }
        for (EntityPlayer entityPlayer : new ArrayList<EntityPlayer>(this.trackedPlayers)) {
            if (entityPlayer == null || entityPlayer.isDead || entityPlayer.deathTime != 0 || Track.mc.thePlayer != entityPlayer && AntiBot.isBot((Entity)entityPlayer) || ModuleManager.murderMystery != null && ModuleManager.murderMystery.isEnabled() && !ModuleManager.murderMystery.isEmpty()) continue;
            RenderUtils.renderEntity((Entity)entityPlayer, 2, 0.0, 0.0, Color.RED.getRGB(), false);
        }
    }
}

