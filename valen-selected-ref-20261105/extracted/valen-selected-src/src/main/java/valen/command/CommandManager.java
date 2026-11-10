/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.command.CommandManager$ParsedCommand
 *  valen.module.impl.client.ChatCommands
 */
package valen.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import valen.command.Command;
import valen.command.CommandInput;
import valen.command.CommandManager;
import valen.command.impl.Bind;
import valen.command.impl.Binds;
import valen.command.impl.Cname;
import valen.command.impl.Configs;
import valen.command.impl.Debug;
import valen.command.impl.Enemy;
import valen.command.impl.FriendCommand;
import valen.command.impl.HideAll;
import valen.command.impl.Name;
import valen.command.impl.Ping;
import valen.command.impl.Prefix;
import valen.command.impl.ShowAll;
import valen.command.impl.Toggle;
import valen.command.impl.Track;
import valen.command.impl.Unbind;
import valen.module.ModuleManager;
import valen.module.impl.client.ChatCommands;
import valen.utility.Utils;

/*
 * Exception performing whole class analysis ignored.
 */
public class CommandManager {
    private final List<Command> commands = new ArrayList<Command>();
    public final Track trackCommand;

    public CommandManager() {
        this.register(new Ping());
        this.register(new Name());
        this.register(new Toggle());
        this.register(new Bind());
        this.register(new Unbind());
        this.register(new Binds());
        this.register(new Cname());
        this.register(new Debug());
        this.register(new FriendCommand("friend", FriendCommand.Operation.ADD, "f"));
        this.register(new FriendCommand("fl", FriendCommand.Operation.LIST, new String[0]));
        this.register(new FriendCommand("fr", FriendCommand.Operation.REMOVE, new String[0]));
        this.register(new FriendCommand("fra", FriendCommand.Operation.CLEAR, new String[0]));
        this.register(new Enemy());
        this.register(new Prefix());
        this.trackCommand = new Track();
        this.register(this.trackCommand);
        this.register(new Configs());
        this.register(new ShowAll());
        this.register(new HideAll());
    }

    public boolean handleChatMessage(String string) {
        if (!this.isCommand(string)) {
            return false;
        }
        ParsedCommand parsedCommand = this.parse(string, false);
        if (parsedCommand == null) {
            return true;
        }
        if (ParsedCommand.access$000((ParsedCommand)parsedCommand) == null) {
            Utils.sendMessage(this.formatOutput("&cUnknown command. Use tab to browse command suggestions."));
            return true;
        }
        ParsedCommand.access$000((ParsedCommand)parsedCommand).execute(ParsedCommand.access$100((ParsedCommand)parsedCommand));
        return true;
    }

    public String[] getAutoComplete(String string) {
        if (!this.isCommand(string)) {
            return new String[0];
        }
        ParsedCommand parsedCommand = this.parse(string, true);
        if (parsedCommand == null) {
            return this.prefixSuggestions("", string);
        }
        if (ParsedCommand.access$000((ParsedCommand)parsedCommand) == null) {
            return this.prefixSuggestions(ParsedCommand.access$100((ParsedCommand)parsedCommand).getLabel(), string);
        }
        String[] stringArray = this.getCommandLabelSuggestions(string, parsedCommand);
        if (stringArray.length > 0) {
            return stringArray;
        }
        return this.buildAutoCompleteSuggestions(parsedCommand, ParsedCommand.access$000((ParsedCommand)parsedCommand).suggest(ParsedCommand.access$100((ParsedCommand)parsedCommand)));
    }

    public String[] getPreviewSuggestions(String string) {
        if (!this.isCommand(string)) {
            return new String[0];
        }
        ParsedCommand parsedCommand = this.parse(string, true);
        if (parsedCommand == null) {
            return this.prefixSuggestions("", string);
        }
        if (ParsedCommand.access$000((ParsedCommand)parsedCommand) == null) {
            return this.prefixSuggestions(ParsedCommand.access$100((ParsedCommand)parsedCommand).getLabel(), string);
        }
        String[] stringArray = this.getCommandLabelSuggestions(string, parsedCommand);
        if (stringArray.length > 0) {
            return stringArray;
        }
        List<String> list = ParsedCommand.access$000((ParsedCommand)parsedCommand).suggest(ParsedCommand.access$100((ParsedCommand)parsedCommand));
        if (list == null || list.isEmpty()) {
            return new String[0];
        }
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        for (String string2 : list) {
            if (string2 == null || string2.isEmpty()) continue;
            linkedHashSet.add(string2);
        }
        return linkedHashSet.toArray(new String[0]);
    }

    private String[] getCommandLabelSuggestions(String string, ParsedCommand parsedCommand) {
        String[] stringArray;
        if (parsedCommand == null || ParsedCommand.access$100((ParsedCommand)parsedCommand).argumentCount() != 0 || CommandManager.endsWithWhitespace(string)) {
            return new String[0];
        }
        for (String string2 : stringArray = this.prefixSuggestions(ParsedCommand.access$100((ParsedCommand)parsedCommand).getLabel(), string)) {
            if (string2.equalsIgnoreCase(string)) continue;
            return stringArray;
        }
        return new String[0];
    }

    public boolean isEnabled() {
        ChatCommands chatCommands = this.getSettings();
        return chatCommands != null && chatCommands.isEnabled();
    }

    public boolean isCommand(String string) {
        return this.isFriendShortcut(string) || string != null && this.isEnabled() && string.startsWith(this.getPrefix());
    }

    private boolean isFriendShortcut(String string) {
        if (string == null || !string.startsWith("!")) {
            return false;
        }
        String string2 = string.substring(1).split("\\s+", 2)[0];
        return "f".equalsIgnoreCase(string2) || "fl".equalsIgnoreCase(string2) || "fr".equalsIgnoreCase(string2) || "fra".equalsIgnoreCase(string2);
    }

    private String messagePrefix(String string) {
        return this.isFriendShortcut(string) ? "!" : this.getPrefix();
    }

    public String getPrefix() {
        ChatCommands chatCommands = this.getSettings();
        return chatCommands != null ? chatCommands.getPrefix() : ".";
    }

    public String formatOutput(String string) {
        ChatCommands chatCommands = this.getSettings();
        if (chatCommands != null && chatCommands.lowercase()) {
            return string.toLowerCase();
        }
        return string;
    }

    private void register(Command command) {
        this.commands.add(command);
    }

    private ChatCommands getSettings() {
        return ModuleManager.chatCommands;
    }

    private ParsedCommand parse(String string, boolean bl) {
        String string2 = this.stripPrefix(string);
        if (string2 == null) {
            return null;
        }
        String string3 = CommandManager.trimLeadingWhitespace(string2);
        if (string3.isEmpty()) {
            return null;
        }
        String[] stringArray = CommandManager.tokenize(string3, bl && CommandManager.endsWithWhitespace(string2));
        if (stringArray.length == 0) {
            return null;
        }
        String string4 = stringArray[0];
        String[] stringArray2 = stringArray.length > 1 ? Arrays.copyOfRange(stringArray, 1, stringArray.length) : new String[]{};
        return new ParsedCommand(this.findCommand(string4), new CommandInput(string, string4, stringArray2), null);
    }

    private String[] prefixSuggestions(String string, String string2) {
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        String string3 = string == null ? "" : string.toLowerCase();
        for (Command command : this.commands) {
            if (this.isFriendShortcut(string2) && !(command instanceof FriendCommand)) continue;
            if (command.getName().toLowerCase().startsWith(string3)) {
                linkedHashSet.add(this.messagePrefix(string2) + command.getName());
            }
            for (String string4 : command.getAliases()) {
                if (!string4.toLowerCase().startsWith(string3)) continue;
                linkedHashSet.add(this.messagePrefix(string2) + string4);
            }
        }
        return linkedHashSet.toArray(new String[0]);
    }

    private Command findCommand(String string) {
        for (Command command : this.commands) {
            if (!command.matches(string)) continue;
            return command;
        }
        return null;
    }

    private String[] buildAutoCompleteSuggestions(ParsedCommand parsedCommand, List<String> list) {
        if (list == null || list.isEmpty()) {
            return new String[0];
        }
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        int n = Math.max(0, Math.min(ParsedCommand.access$100((ParsedCommand)parsedCommand).argumentCount(), ParsedCommand.access$000((ParsedCommand)parsedCommand).getSuggestionArgumentStart(ParsedCommand.access$100((ParsedCommand)parsedCommand))));
        for (String string : list) {
            if (string == null || string.isEmpty()) continue;
            StringBuilder stringBuilder = new StringBuilder(this.messagePrefix(ParsedCommand.access$100((ParsedCommand)parsedCommand).getRawMessage())).append(ParsedCommand.access$100((ParsedCommand)parsedCommand).getLabel());
            for (int i = 0; i < n; ++i) {
                stringBuilder.append(' ').append(ParsedCommand.access$100((ParsedCommand)parsedCommand).getArgument(i));
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(string);
            linkedHashSet.add(stringBuilder.toString());
        }
        return linkedHashSet.toArray(new String[0]);
    }

    private String stripPrefix(String string) {
        if (!this.isCommand(string)) {
            return null;
        }
        return string.substring(this.messagePrefix(string).length());
    }

    private static String trimLeadingWhitespace(String string) {
        int n;
        for (n = 0; n < string.length() && Character.isWhitespace(string.charAt(n)); ++n) {
        }
        return string.substring(n);
    }

    private static boolean endsWithWhitespace(String string) {
        return !string.isEmpty() && Character.isWhitespace(string.charAt(string.length() - 1));
    }

    private static String[] tokenize(String string, boolean bl) {
        ArrayList<String> arrayList = new ArrayList<String>();
        int n = string.length();
        int n2 = 0;
        while (n2 < n) {
            while (n2 < n && Character.isWhitespace(string.charAt(n2))) {
                ++n2;
            }
            if (n2 >= n) break;
            int n3 = n2;
            while (n2 < n && !Character.isWhitespace(string.charAt(n2))) {
                ++n2;
            }
            arrayList.add(string.substring(n3, n2));
        }
        if (bl) {
            arrayList.add("");
        }
        return arrayList.toArray(new String[0]);
    }
}

