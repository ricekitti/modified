/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  valen.utility.PlayerRelationsManager$PlayerEntry
 */
package valen.command.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.NetworkPlayerInfo;
import valen.Valen;
import valen.command.Command;
import valen.command.CommandInput;
import valen.utility.PlayerRelationsManager;

public final class FriendCommand
extends Command {
    private final Operation operation;

    public FriendCommand(String string, Operation operation, String ... stringArray) {
        super(string, stringArray);
        this.operation = operation;
    }

    @Override
    public void execute(CommandInput commandInput) {
        PlayerRelationsManager playerRelationsManager = Valen.playerRelationsManager;
        if (playerRelationsManager == null) {
            return;
        }
        if (this.operation == Operation.LIST || this.operation == Operation.ADD && commandInput.argumentCount() == 0) {
            if (commandInput.argumentCount() != 0) {
                this.usage();
                return;
            }
            List<PlayerRelationsManager.PlayerEntry> list = playerRelationsManager.getEntries(PlayerRelationsManager.RelationType.FRIEND);
            this.replyWithHeader("&b" + list.size() + " &7friend" + (list.size() == 1 ? "" : "s") + ".");
            for (PlayerRelationsManager.PlayerEntry playerEntry : list) {
                this.replyWithHeader(" &b" + playerEntry.getDisplayName());
            }
            return;
        }
        if (this.operation == Operation.CLEAR) {
            if (commandInput.argumentCount() != 0) {
                this.usage();
                return;
            }
            int n = playerRelationsManager.getCount(PlayerRelationsManager.RelationType.FRIEND);
            playerRelationsManager.clearFriends();
            this.replyWithHeader("&7Removed all &b" + n + " &7friends.");
            return;
        }
        if (commandInput.argumentCount() != 1 || !PlayerRelationsManager.isValidUsername(commandInput.getArgument(0))) {
            this.usage();
            return;
        }
        String string = commandInput.getArgument(0);
        if (this.operation == Operation.ADD) {
            this.replyWithHeader(playerRelationsManager.addFriend(string) ? "&7Added &afriend&7: &b" + string : "&b" + string + " &7is already a friend.");
        } else {
            this.replyWithHeader(playerRelationsManager.removeFriend(string) ? "&7Removed &afriend&7: &b" + string : "&b" + string + " &7is not in your friend list.");
        }
    }

    private void usage() {
        this.replyWithHeader("&7Use &b!f <username>&7, &b!fl&7, &b!fr <username>&7, or &b!fra&7.");
    }

    @Override
    public List<String> suggest(CommandInput commandInput) {
        ArrayList<String> arrayList;
        block3: {
            block2: {
                arrayList = new ArrayList<String>();
                if (this.operation != Operation.REMOVE || Valen.playerRelationsManager == null) break block2;
                for (PlayerRelationsManager.PlayerEntry playerEntry : Valen.playerRelationsManager.getEntries(PlayerRelationsManager.RelationType.FRIEND)) {
                    arrayList.add(playerEntry.getDisplayName());
                }
                break block3;
            }
            if (this.operation != Operation.ADD || mc == null || mc.getNetHandler() == null) break block3;
            for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                arrayList.add(networkPlayerInfo.getGameProfile().getName());
            }
        }
        return this.filterSuggestions(commandInput, arrayList);
    }

    public static enum Operation {
        ADD,
        LIST,
        REMOVE,
        CLEAR;

    }
}

