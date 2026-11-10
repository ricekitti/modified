package dev.rice.client.render.openb4;

import dev.rice.client.RelationshipService;
import dev.rice.inject.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.*;
import net.minecraft.util.IChatComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bridges OpenB4's fake-entity/bot and Hypixel lobby conditions into Rice. */
public final class B4EspHost {
    public static boolean allowed(Entity entity) {
        // Rice's Anti Bot publishes its decisions through this existing flag.
        return !entity.isDead && entity.getEntityId() != -170 && (RelationshipService.flags(entity) & 2) == 0;
    }

    public static boolean inLobby() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.isSingleplayer()) return false;
        boolean hypixel = mc.getCurrentServerData() != null
            && mc.getCurrentServerData().serverIP.toLowerCase(Locale.ROOT).endsWith("hypixel.net");
        if (!hypixel && mc.ingameGUI != null) {
            try {
                IChatComponent header = ReflectionHelper.getPrivateValue(GuiPlayerTabOverlay.class,
                    mc.ingameGUI.getTabList(), "header", "field_175256_i");
                hypixel = header != null && header.getUnformattedText().toLowerCase(Locale.ROOT).contains("hypixel.net");
            } catch (RuntimeException ignored) { }
        }
        if (!hypixel) return false;
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;
        List<Score> scores = new ArrayList<>(scoreboard.getSortedScores(objective));
        for (int i = scores.size() - 1; i >= 0; --i) {
            String name = scores.get(i).getPlayerName();
            if (name == null || name.startsWith("#")) continue;
            String firstLine = ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(name), name);
            return firstLine.contains("  \u00a78L");
        }
        return false;
    }
}
