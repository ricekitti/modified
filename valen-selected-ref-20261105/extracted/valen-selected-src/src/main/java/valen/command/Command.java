/*
 * Decompiled with CFR 0.152.
 */
package valen.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import valen.Valen;
import valen.command.CommandInput;
import valen.utility.IMinecraftInstance;
import valen.utility.Utils;

public abstract class Command
implements IMinecraftInstance {
    private final String name;
    private final String[] aliases;

    protected Command(String string, String ... stringArray) {
        String[] stringArray2;
        this.name = string;
        if (stringArray == null || stringArray.length == 0) {
            String[] stringArray3 = new String[1];
            stringArray2 = stringArray3;
            stringArray3[0] = string;
        } else {
            stringArray2 = stringArray;
        }
        this.aliases = stringArray2;
    }

    public final String getName() {
        return this.name;
    }

    public final String[] getAliases() {
        return (String[])this.aliases.clone();
    }

    public final boolean matches(String string) {
        if (string == null) {
            return false;
        }
        if (this.name.equalsIgnoreCase(string)) {
            return true;
        }
        for (String string2 : this.aliases) {
            if (!string2.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    public abstract void execute(CommandInput var1);

    public List<String> suggest(CommandInput commandInput) {
        return Collections.emptyList();
    }

    public int getSuggestionArgumentStart(CommandInput commandInput) {
        return Math.max(0, commandInput.argumentCount() - 1);
    }

    protected final void replyWithHeader(String string) {
        this.replyRaw(string);
    }

    protected final void reply(String string) {
        this.replyRaw(string);
    }

    protected final void syntaxError() {
        this.replyRaw("&7Unknown command.");
    }

    protected final String prefixed(String string) {
        return this.getPrefix() + string;
    }

    protected final List<String> filterSuggestions(CommandInput commandInput, List<String> list) {
        String string = commandInput.argumentCount() == 0 ? "" : commandInput.getArgument(commandInput.argumentCount() - 1);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : list) {
            if (string != null && !string.isEmpty() && !string2.toLowerCase().startsWith(string.toLowerCase())) continue;
            arrayList.add(string2);
        }
        return arrayList;
    }

    protected final List<String> filterSuggestions(CommandInput commandInput, String ... stringArray) {
        ArrayList<String> arrayList = new ArrayList<String>();
        Collections.addAll(arrayList, stringArray);
        return this.filterSuggestions(commandInput, arrayList);
    }

    private void replyRaw(String string) {
        Utils.sendMessage(this.formatOutput(string));
    }

    private String formatOutput(String string) {
        return Valen.commandManager != null ? Valen.commandManager.formatOutput(string) : string;
    }

    private String getPrefix() {
        return Valen.commandManager != null ? Valen.commandManager.getPrefix() : ".";
    }
}

