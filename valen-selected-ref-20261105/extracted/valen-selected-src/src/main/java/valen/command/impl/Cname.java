/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.module.impl.other.NameHider
 */
package valen.command.impl;

import valen.command.Command;
import valen.command.CommandInput;
import valen.module.impl.other.NameHider;

public class Cname
extends Command {
    public Cname() {
        super("namehider", new String[0]);
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() == 0) {
            this.syntaxError();
            return;
        }
        NameHider.setFakeName((String)commandInput.joinArguments(0));
        this.replyWithHeader("&7Name has been set to &b" + NameHider.fakeName + "&7.");
    }
}

