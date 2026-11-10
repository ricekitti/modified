/*
 * Decompiled with CFR 0.152.
 */
package valen.command.impl;

import valen.command.Command;
import valen.command.CommandInput;
import valen.helper.PingHelper;

public class Ping
extends Command {
    public Ping() {
        super("ping", new String[0]);
    }

    @Override
    public void execute(CommandInput commandInput) {
        PingHelper.checkPing(true);
    }
}

