/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.ICommandSender
 */
package dev.rice.anticheat;



import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public final class CommandBlacklist
extends CommandBase {
    public String getCommandName() {
        return "blacklist";
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/blacklist <player> [reason]  |  /blacklist remove <player>";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        String reason;
        if (args.length == 0) {
            Host.message("Anticheat", "\u00a7c" + this.getCommandUsage(sender));
            return;
        }
        if (("remove".equalsIgnoreCase(args[0]) || "unblacklist".equalsIgnoreCase(args[0])) && args.length >= 2) {
            String name = args[1];
            if (Anticheat.unblacklist(name)) {
                Host.message("Anticheat", "\u00a7f" + name + " \u00a77removed from the blacklist.");
            } else {
                Host.message("Anticheat", "\u00a77" + name + " was not blacklisted.");
            }
            return;
        }
        String name = args[0];
        String string = reason = args.length > 1 ? CommandBlacklist.join(args, 1) : "cheating";
        if (Anticheat.blacklist(name, reason)) {
            Host.message("Anticheat", "\u00a7f" + name + " \u00a77blacklisted \u00a77(\u00a7c" + reason + "\u00a77).");
        } else {
            Host.message("Anticheat", "\u00a7cCould not blacklist \u00a7f" + name + "\u00a7c.");
        }
    }

    private static String join(String[] args, int from) {
        StringBuilder out = new StringBuilder();
        for (int i = from; i < args.length; ++i) {
            if (i > from) {
                out.append(' ');
            }
            out.append(args[i]);
        }
        return out.toString();
    }
}
