/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package valen.command.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.input.Keyboard;
import valen.command.Command;
import valen.command.CommandInput;
import valen.module.Module;
import valen.module.ModuleManager;

public class Binds
extends Command {
    public Binds() {
        super("binds", new String[0]);
    }

    @Override
    public void execute(CommandInput commandInput) {
        if (commandInput.argumentCount() == 0) {
            Map<String, List<String>> map = this.getBindsModulesMap(0);
            int n = this.getTotalModules(map);
            this.replyWithHeader("&b" + n + " &7module" + (n == 1 ? "" : "s") + " have keybinds.");
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                for (String string : entry.getValue()) {
                    this.replyWithHeader(" &b" + entry.getKey() + " &7" + string);
                }
            }
            return;
        }
        if (commandInput.argumentCount() != 1) {
            this.syntaxError();
            return;
        }
        int n = Keyboard.getKeyIndex((String)commandInput.getArgument(0).toUpperCase());
        if (n == 0) {
            this.replyWithHeader("&7Invalid key.");
            return;
        }
        Map<String, List<String>> map = this.getBindsModulesMap(n);
        int n2 = this.getTotalModules(map);
        this.replyWithHeader("&b" + n2 + " &7module" + (n2 == 1 ? "" : "s") + " has keybind &b" + commandInput.getArgument(0).toUpperCase() + "&7.");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String string : entry.getValue()) {
                this.replyWithHeader(" &b" + entry.getKey() + " &7" + string);
            }
        }
    }

    private Map<String, List<String>> getBindsModulesMap(int n) {
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
        for (Module module : ModuleManager.modules) {
            this.addModuleIfMatches(hashMap, module, n);
        }
        return hashMap;
    }

    private void addModuleIfMatches(Map<String, List<String>> map, Module module, int n) {
        if (module.getKeycode() == 0) {
            return;
        }
        if (n != 0 && module.getKeycode() != n) {
            return;
        }
        String string = module.getKeycode() >= 1000 ? "M" + (module.getKeycode() - 1000) : Keyboard.getKeyName((int)module.getKeycode());
        List<String> list = map.get(string);
        if (list == null) {
            list = new ArrayList<String>();
            map.put(string, list);
        }
        list.add(module.getName());
    }

    private int getTotalModules(Map<String, List<String>> map) {
        int n = 0;
        for (List<String> list : map.values()) {
            n += list.size();
        }
        return n;
    }
}

