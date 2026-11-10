/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.scoreboard.ScorePlayerTeam
 *  net.minecraft.scoreboard.Team
 */
package dev.rice.client.anticheat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

public final class Teams {
    private Teams() {
    }

    public static boolean isTeammate(Minecraft mc, EntityPlayer player) {
        try {
            return SnowifyAntiCheatHost.isSlinkyTeammate(player) || Teams.sameTeam(Teams.ownTeam(mc), player.getTeam());
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isTeammate(Minecraft mc, NetworkPlayerInfo info) {
        try {
            return Teams.sameTeam(Teams.ownTeam(mc), (Team)info.getPlayerTeam());
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static NetworkPlayerInfo ownInfo(Minecraft mc) {
        if (mc.thePlayer == null || mc.getNetHandler() == null) {
            return null;
        }
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info != null ? info : mc.getNetHandler().getPlayerInfo(mc.thePlayer.getName());
    }

    public static boolean isSelf(Minecraft mc, NetworkPlayerInfo info) {
        if (info == null || info.getGameProfile() == null || mc.thePlayer == null) {
            return false;
        }
        NetworkPlayerInfo own = Teams.ownInfo(mc);
        return info == own || mc.thePlayer.getUniqueID().equals(info.getGameProfile().getId());
    }

    public static Team ownTeam(Minecraft mc) {
        NetworkPlayerInfo own = Teams.ownInfo(mc);
        ScorePlayerTeam team = own != null ? own.getPlayerTeam() : null;
        return team != null ? team : mc.thePlayer.getTeam();
    }

    private static boolean sameTeam(Team mine, Team theirs) {
        try {
            if (mine == null || theirs == null) {
                return false;
            }
            if (mine.isSameTeam(theirs)) {
                return true;
            }
            String ours = Teams.colourOf(mine);
            return ours != null && ours.equals(Teams.colourOf(theirs));
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static String colourOf(Team team) {
        String decorated;
        try {
            decorated = team.formatString("");
        }
        catch (Throwable ignored) {
            return null;
        }
        if (decorated == null) {
            return null;
        }
        int i = 0;
        while (i + 1 < decorated.length()) {
            char code;
            if (decorated.charAt(i) == '\u00a7' && "0123456789abcdef".indexOf(code = Character.toLowerCase(decorated.charAt(i + 1))) >= 0) {
                return String.valueOf(code);
            }
            ++i;
        }
        return null;
    }
}
