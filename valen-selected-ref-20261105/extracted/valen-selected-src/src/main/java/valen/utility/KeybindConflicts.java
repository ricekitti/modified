/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.util.ArrayList;
import valen.Valen;
import valen.module.Module;
import valen.module.setting.Setting;
import valen.module.setting.impl.KeySetting;
import valen.utility.config.ClientConfig;

public final class KeybindConflicts {
    private KeybindConflicts() {
    }

    public static String find(int n, Module module, KeySetting keySetting, String string) {
        if (n <= 0) {
            return "";
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        if (Valen.moduleManager != null) {
            for (Module object : Valen.moduleManager.getModules()) {
                if (object.canBeEnabled() && object.getKeycode() == n && (object != module || keySetting != null)) {
                    arrayList.add(object.getName());
                }
                for (Setting setting : object.getSettings()) {
                    if (!(setting instanceof KeySetting) || setting == keySetting || ((KeySetting)setting).getKey() != n) continue;
                    arrayList.add(object.getName() + " / " + setting.getName());
                }
            }
        }
        if (Valen.preferences != null && Valen.configManager != null) {
            for (ClientConfig clientConfig : Valen.configManager.getConfigs()) {
                if (clientConfig.getName().equalsIgnoreCase(string) || Valen.preferences.getProfileBind(clientConfig.getName()) != n) continue;
                arrayList.add(clientConfig.getName() + " profile");
            }
        }
        return String.join((CharSequence)", ", arrayList);
    }
}

