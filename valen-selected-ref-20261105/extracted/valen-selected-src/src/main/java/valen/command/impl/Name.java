/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.utility.system.SystemUtils
 */
package valen.command.impl;

import valen.command.Command;
import valen.command.CommandInput;
import valen.utility.Utils;
import valen.utility.system.SystemUtils;

public class Name
extends Command {
    public Name() {
        super("name", "ign", "name");
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (!Utils.nullCheck()) {
            return;
        }
        SystemUtils.addToClipboard((String)Name.mc.thePlayer.getName());
        this.replyWithHeader("&7Copied &b" + Name.mc.thePlayer.getName() + " &7to clipboard");
    }
}

