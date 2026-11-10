/*
 * Decompiled with CFR 0.152.
 */
package valen.command.impl;

import valen.command.Command;
import valen.command.CommandInput;
import valen.module.Module;
import valen.module.ModuleManager;

public class HideAll
extends Command {
    public HideAll() {
        super("hide", "hideall");
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() > 0 && !"all".equalsIgnoreCase(commandInput.getArgument(0))) {
            this.syntaxError();
            return;
        }
        int n = 0;
        for (Module module : ModuleManager.modules) {
            if (module.isHidden()) continue;
            module.setHidden(true);
            ++n;
        }
        this.replyWithHeader("&7Hidden &c" + n + " &7module" + (n == 1 ? "" : "s") + " from HUD.");
    }
}

