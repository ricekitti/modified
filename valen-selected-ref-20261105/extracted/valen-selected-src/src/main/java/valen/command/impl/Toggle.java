/*
 * Decompiled with CFR 0.152.
 */
package valen.command.impl;

import java.util.ArrayList;
import java.util.List;
import valen.command.Command;
import valen.command.CommandInput;
import valen.module.Module;
import valen.module.ModuleManager;

public class Toggle
extends Command {
    public Toggle() {
        super("toggle", "toggle", "t");
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() == 0) {
            this.syntaxError();
            return;
        }
        Module module = this.findModule(commandInput.joinArguments(0));
        if (module == null) {
            this.replyWithHeader("&cModule not found.");
            return;
        }
        module.toggle();
        this.replyWithHeader("&7" + module.getName() + " " + (module.isEnabled() ? "&aenabled" : "&cdisabled") + "&7.");
    }

    @Override
    public List<String> suggest(CommandInput commandInput) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String string = commandInput.argumentCount() == 0 ? "" : commandInput.joinArguments(0).toLowerCase();
        for (Module module : ModuleManager.modules) {
            if (!module.getName().toLowerCase().startsWith(string)) continue;
            arrayList.add(module.getName());
        }
        return arrayList;
    }

    @Override
    public int getSuggestionArgumentStart(CommandInput commandInput) {
        return 0;
    }

    private Module findModule(String string) {
        String string2 = this.normalize(string);
        for (Module module : ModuleManager.modules) {
            if (!this.normalize(module.getName()).equals(string2)) continue;
            return module;
        }
        return null;
    }

    private String normalize(String string) {
        return string == null ? "" : string.replace(" ", "").toLowerCase();
    }
}

