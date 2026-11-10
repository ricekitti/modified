/*
 * Decompiled with CFR 0.152.
 */
package valen.command.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import valen.Valen;
import valen.command.Command;
import valen.command.CommandInput;
import valen.utility.config.ClientConfig;
import valen.utility.config.ConfigManager;

public class Configs
extends Command {
    public Configs() {
        super("configs", "configs", "profiles", "config");
    }

    @Override
    public void execute(CommandInput commandInput) {
        String string;
        ConfigManager configManager = Valen.configManager;
        if (configManager == null) {
            this.reply("&cConfigs are not available yet.");
            return;
        }
        configManager.refresh();
        switch (string = commandInput.argumentCount() == 0 ? "list" : commandInput.getArgument(0).toLowerCase()) {
            case "list": {
                this.list(configManager);
                return;
            }
            case "save": {
                this.save(configManager, commandInput);
                return;
            }
            case "load": {
                this.load(configManager, commandInput);
                return;
            }
            case "delete": 
            case "remove": {
                this.delete(configManager, commandInput);
                return;
            }
            case "rename": {
                this.rename(configManager, commandInput);
                return;
            }
        }
        this.usage();
    }

    private void list(ConfigManager configManager) {
        if (configManager.isEmpty()) {
            this.reply("&7No configs saved. &f" + this.prefixed("configs save <name>"));
            return;
        }
        this.replyWithHeader("&7Configs:");
        for (ClientConfig clientConfig : configManager.getConfigs()) {
            boolean bl = configManager.isActive(clientConfig);
            String string = bl ? (configManager.isDirty() ? " &e(active, unsaved)" : " &a(active)") : "";
            this.reply(" &f" + clientConfig.getName() + " &8\u00b7 " + clientConfig.getEnabledModuleCount() + " on, " + clientConfig.getRelativeAge() + string);
        }
    }

    private void save(ConfigManager configManager, CommandInput commandInput) {
        String string;
        String string2 = string = commandInput.argumentCount() >= 2 ? commandInput.joinArguments(1) : configManager.getActiveName();
        if (string == null) {
            this.reply("&cName it: &f" + this.prefixed("configs save <name>"));
            return;
        }
        String string3 = ConfigManager.normalizeName(string);
        if (string3 == null) {
            this.reply("&cThat name is empty.");
            return;
        }
        boolean bl = configManager.exists(string3);
        if (configManager.save(string3)) {
            this.reply((bl ? "&7Overwrote &f" : "&7Saved &f") + string3);
        } else {
            this.reply("&cCouldn't write that config.");
        }
    }

    private void load(ConfigManager configManager, CommandInput commandInput) {
        if (commandInput.argumentCount() < 2) {
            this.reply("&cWhich one? &f" + this.prefixed("configs load <name>"));
            return;
        }
        String string = commandInput.joinArguments(1);
        if (configManager.load(string)) {
            this.reply("&7Loaded &f" + string);
        } else {
            this.reply("&cNo config called &f" + string);
        }
    }

    private void delete(ConfigManager configManager, CommandInput commandInput) {
        if (commandInput.argumentCount() < 2) {
            this.reply("&cWhich one? &f" + this.prefixed("configs delete <name>"));
            return;
        }
        String string = commandInput.joinArguments(1);
        if (ConfigManager.isDefault(string)) {
            this.reply("&cThe default config can't be deleted.");
            return;
        }
        if (configManager.delete(string)) {
            this.reply("&7Deleted &f" + string);
        } else {
            this.reply("&cNo config called &f" + string);
        }
    }

    private void rename(ConfigManager configManager, CommandInput commandInput) {
        if (commandInput.argumentCount() < 3) {
            this.reply("&cUsage: &f" + this.prefixed("configs rename <old> <new>"));
            return;
        }
        String string = commandInput.getArgument(1);
        String string2 = commandInput.joinArguments(2);
        if (ConfigManager.isDefault(string)) {
            this.reply("&cThe default config can't be renamed.");
            return;
        }
        if (configManager.rename(string, string2)) {
            this.reply("&7Renamed &f" + string + " &7to &f" + string2);
        } else {
            this.reply("&cCouldn't rename that \u2014 check the name isn't taken.");
        }
    }

    private void usage() {
        this.replyWithHeader("&7Configs:");
        this.reply(" &f" + this.prefixed("configs") + " &8\u00b7 list them");
        this.reply(" &f" + this.prefixed("configs save <name>") + " &8\u00b7 save current setup");
        this.reply(" &f" + this.prefixed("configs load <name>"));
        this.reply(" &f" + this.prefixed("configs rename <old> <new>"));
        this.reply(" &f" + this.prefixed("configs delete <name>"));
    }

    @Override
    public List<String> suggest(CommandInput commandInput) {
        ConfigManager configManager = Valen.configManager;
        if (commandInput.argumentCount() <= 1) {
            return this.filterSuggestions(commandInput, "list", "save", "load", "rename", "delete");
        }
        if (configManager == null) {
            return Collections.emptyList();
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (ClientConfig clientConfig : configManager.getConfigs()) {
            arrayList.add(clientConfig.getName());
        }
        return this.filterSuggestions(commandInput, arrayList);
    }
}

