/*
 * Decompiled with CFR 0.152.
 */
package valen.command.impl;

import valen.Valen;
import valen.command.Command;
import valen.command.CommandInput;
import valen.helper.DebugHelper;

public class Debug
extends Command {
    public Debug() {
        super("debug", new String[0]);
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() == 0) {
            Valen.DEBUG = !Valen.DEBUG;
            this.replyWithHeader("&7Debug " + (Valen.DEBUG ? "&aenabled" : "&cdisabled") + "&7.");
            return;
        }
        if (commandInput.argumentCount() != 1) {
            this.syntaxError();
            return;
        }
        String string = commandInput.getArgument(0).toLowerCase();
        if ("mixin".equals(string)) {
            DebugHelper.MIXIN = !DebugHelper.MIXIN;
            this.replyWithHeader("&dMixin &7debug " + (DebugHelper.MIXIN ? "&aenabled" : "&cdisabled") + "&7.");
            return;
        }
        if ("bg".equals(string) || "background".equals(string)) {
            DebugHelper.BACKGROUND = !DebugHelper.BACKGROUND;
            this.replyWithHeader("&6Background &7debug " + (DebugHelper.BACKGROUND ? "&aenabled" : "&cdisabled") + "&7.");
            return;
        }
        this.syntaxError();
    }
}

