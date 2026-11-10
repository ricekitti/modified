/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.module.impl.client.ChatCommands
 */
package valen.command.impl;

import java.util.Arrays;
import java.util.List;
import valen.command.Command;
import valen.command.CommandInput;
import valen.module.ModuleManager;
import valen.module.impl.client.ChatCommands;

public class Prefix
extends Command {
    private static final List<String> EXAMPLE_PREFIXES = Arrays.asList(".", ",", ";", "/", "-", "=", "[", "]", "\\", "'", "`");

    public Prefix() {
        super("prefix", new String[0]);
    }

    @Override
    public void execute(CommandInput commandInput) {
        ChatCommands chatCommands = ModuleManager.chatCommands;
        if (chatCommands == null) {
            this.replyWithHeader("&7Chat commands are unavailable.");
            return;
        }
        if (commandInput.argumentCount() == 0) {
            this.replyWithHeader("&7Current prefix: &b" + chatCommands.getPrefix());
            return;
        }
        if (commandInput.argumentCount() != 1) {
            this.syntaxError();
            return;
        }
        String string = commandInput.getArgument(0);
        if (!ChatCommands.isValidPrefix((String)string)) {
            this.replyWithHeader("&7Prefix must be a single non-space character.");
            return;
        }
        chatCommands.setPrefix(string);
        this.replyWithHeader("&7Chat command prefix set to &b" + chatCommands.getPrefix() + "&7.");
    }

    @Override
    public List<String> suggest(CommandInput commandInput) {
        return this.filterSuggestions(commandInput, EXAMPLE_PREFIXES);
    }
}

