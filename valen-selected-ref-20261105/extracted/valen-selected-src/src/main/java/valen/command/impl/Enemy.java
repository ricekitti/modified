/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.utility.PlayerRelationsManager$PlayerEntry
 */
package valen.command.impl;

import java.util.List;
import valen.Valen;
import valen.command.Command;
import valen.command.CommandInput;
import valen.utility.PlayerRelationsManager;
import valen.utility.Utils;

public class Enemy
extends Command {
    public Enemy() {
        super("enemy", "enemy", "e");
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() == 0) {
            List<PlayerRelationsManager.PlayerEntry> list = Valen.playerRelationsManager.getEntries(PlayerRelationsManager.RelationType.ENEMY);
            this.replyWithHeader("&b" + list.size() + " &7enem" + (list.size() == 1 ? "y" : "ies") + ".");
            for (PlayerRelationsManager.PlayerEntry playerEntry : list) {
                this.replyWithHeader(" &b" + playerEntry.getDisplayName());
            }
            return;
        }
        if (commandInput.argumentCount() != 1) {
            this.syntaxError();
            return;
        }
        String string = commandInput.getArgument(0);
        if ("clear".equalsIgnoreCase(string)) {
            int n = Valen.playerRelationsManager.getCount(PlayerRelationsManager.RelationType.ENEMY);
            Valen.playerRelationsManager.clearEnemies();
            this.replyWithHeader("&b" + n + " &7enem" + (n == 1 ? "y" : "ies") + " cleared.");
            return;
        }
        if (Utils.addEnemy(string)) {
            this.replyWithHeader("&7Added enemy&7: &b" + string);
        } else {
            Utils.removeEnemy(string);
            this.replyWithHeader("&7Removed enemy&7: &b" + string);
        }
    }
}

