/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package valen.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.input.Keyboard;
import valen.command.Command;
import valen.command.CommandInput;
import valen.module.Module;
import valen.module.ModuleManager;

public class Bind
extends Command {
    public Bind() {
        super("bind", "b");
    }

    @Override
    public void execute(CommandInput commandInput) {
        String string;
        String string2;
        if (commandInput.argumentCount() < 2) {
            this.syntaxError();
            return;
        }
        String string3 = commandInput.getArgument(0);
        if ("add".equalsIgnoreCase(string3) || "remove".equalsIgnoreCase(string3)) {
            if ("remove".equalsIgnoreCase(string3)) {
                String string4 = commandInput.joinArguments(1);
                Module module = this.findModule(string4);
                if (module == null && commandInput.argumentCount() > 2) {
                    string4 = this.joinArguments(commandInput.getArguments(), 1, commandInput.argumentCount() - 1);
                    module = this.findModule(string4);
                }
                if (module == null) {
                    this.replyWithHeader("&cModule not found.");
                    return;
                }
                module.setBind(0);
                this.replyWithHeader("&7Unbound &b" + module.getName() + "&7.");
                return;
            }
            if (commandInput.argumentCount() < 3) {
                this.syntaxError();
                return;
            }
            string2 = commandInput.getArgument(commandInput.argumentCount() - 1);
            string = this.joinArguments(commandInput.getArguments(), 1, commandInput.argumentCount() - 1);
        } else {
            string2 = commandInput.getArgument(commandInput.argumentCount() - 1);
            string = this.joinArguments(commandInput.getArguments(), 0, commandInput.argumentCount() - 1);
        }
        Module module = this.findModule(string);
        if (module == null) {
            this.replyWithHeader("&cModule not found.");
            return;
        }
        int n = this.parseKey(string2);
        if (n == 0) {
            this.replyWithHeader("&7Invalid key.");
            return;
        }
        module.setBind(n);
        this.replyWithHeader("&7Bound &b" + module.getName() + " &7to &b" + this.keyName(n) + "&7.");
    }

    @Override
    public List<String> suggest(CommandInput commandInput) {
        if (commandInput.argumentCount() <= 1) {
            ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList("add", "remove"));
            arrayList.addAll(this.moduleSuggestions(commandInput.argumentCount() == 0 ? "" : commandInput.joinArguments(0)));
            return this.filterSuggestions(commandInput, arrayList);
        }
        String string = commandInput.getArgument(0);
        if ("add".equalsIgnoreCase(string)) {
            if (commandInput.argumentCount() == 2) {
                return this.moduleSuggestions(commandInput.getArgument(1));
            }
            if (commandInput.argumentCount() >= 3) {
                return this.filterSuggestions(commandInput, "R", "F", "G", "V", "NONE");
            }
        } else {
            if ("remove".equalsIgnoreCase(string)) {
                return this.moduleSuggestions(commandInput.joinArguments(1));
            }
            if (commandInput.argumentCount() >= 2) {
                return this.filterSuggestions(commandInput, "R", "F", "G", "V", "NONE");
            }
        }
        return new ArrayList<String>();
    }

    @Override
    public int getSuggestionArgumentStart(CommandInput commandInput) {
        return Math.max(0, commandInput.argumentCount() - 1);
    }

    private Module findModule(String string) {
        String string2 = this.normalize(string);
        for (Module module : ModuleManager.modules) {
            if (!this.normalize(module.getName()).equals(string2)) continue;
            return module;
        }
        return null;
    }

    private List<String> moduleSuggestions(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String string2 = this.normalize(string);
        for (Module module : ModuleManager.modules) {
            String string3 = module.getName();
            if (!this.normalize(string3).startsWith(string2)) continue;
            arrayList.add(string3);
        }
        return arrayList;
    }

    private int parseKey(String string) {
        if (string == null) {
            return 0;
        }
        if ("NONE".equalsIgnoreCase(string) || "UNBOUND".equalsIgnoreCase(string)) {
            return 0;
        }
        return Keyboard.getKeyIndex((String)string.toUpperCase());
    }

    private String keyName(int n) {
        return n == 0 ? "NONE" : Keyboard.getKeyName((int)n);
    }

    private String normalize(String string) {
        return string == null ? "" : string.replace(" ", "").toLowerCase();
    }

    private String joinArguments(String[] stringArray, int n, int n2) {
        if (stringArray == null || n >= n2) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = n; i < n2; ++i) {
            if (i > n) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(stringArray[i]);
        }
        return stringBuilder.toString();
    }
}

