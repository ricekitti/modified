/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 */
package valen.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import valen.helper.MouseHelper;
import valen.lunar.forge.common.MinecraftForge;
import valen.module.ModuleManager;
import valen.module.impl.combat.AntiKnockback;
import valen.module.impl.render.Notifications;
import valen.module.setting.Setting;
import valen.module.setting.impl.ButtonSetting;
import valen.module.setting.impl.SliderSetting;
import valen.utility.Utils;

public class Module {
    protected ArrayList<Setting> settings;
    private String moduleName;
    private category moduleCategory;
    private volatile boolean enabled;
    private int keycode;
    protected static Minecraft mc;
    private boolean isToggled = false;
    public boolean canBeEnabled = true;
    public boolean ignoreOnSave = false;
    public boolean hidden = false;
    public boolean closetModule = false;
    public boolean alwaysOn = false;
    public String lastInfo;
    public static boolean sort;
    public static List<String> categoriesString;

    public Module(String string, category category2, int n) {
        this.moduleName = string;
        this.moduleCategory = category2;
        this.keycode = n;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList();
    }

    public static Module getModule(Class<? extends Module> clazz) {
        return ModuleManager.getModule(clazz);
    }

    public Module(String string, category category2) {
        this.moduleName = string;
        this.moduleCategory = category2;
        this.keycode = 0;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList();
    }

    public void onKeyBind() {
        if (this.keycode != 0) {
            try {
                if (!this.isToggled && (this.keycode >= 1000 ? (this.keycode == 1069 || this.keycode == 1070 ? MouseHelper.isScrollDown(this.keycode) : Mouse.isButtonDown((int)(this.keycode - 1000))) : Keyboard.isKeyDown((int)this.keycode))) {
                    this.toggle();
                    this.isToggled = true;
                } else if (this.keycode >= 1000 ? (this.keycode == 1069 || this.keycode == 1070 ? !MouseHelper.isScrollDown(this.keycode) : !Mouse.isButtonDown((int)(this.keycode - 1000))) : !Keyboard.isKeyDown((int)this.keycode)) {
                    this.isToggled = false;
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
                Utils.sendMessage("&cFailed to check keybinding. Setting to none");
                this.keycode = 0;
            }
        }
    }

    public void syncKeyBindState() {
        if (this.keycode == 0) {
            return;
        }
        try {
            this.isToggled = this.keycode >= 1000 ? (this.keycode == 1069 || this.keycode == 1070 ? MouseHelper.isScrollDown(this.keycode) : Mouse.isButtonDown((int)(this.keycode - 1000))) : Keyboard.isKeyDown((int)this.keycode);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            Utils.sendMessage("&cFailed to check keybinding. Setting to none");
            this.keycode = 0;
            this.isToggled = false;
        }
    }

    public boolean canBeEnabled() {
        return this.canBeEnabled;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean bl) {
        this.hidden = bl;
    }

    public void enable() {
        if (!this.canBeEnabled() || this.isEnabled()) {
            return;
        }
        this.setEnabled(true);
        ModuleManager.organizedModules.add(this);
        if (ModuleManager.hud != null && ModuleManager.hud.isEnabled()) {
            ModuleManager.sort();
        }
        if (!this.alwaysOn) {
            MinecraftForge.EVENT_BUS.register(this);
        }
        this.onEnable();
        this.notifyToggle(true);
    }

    public void disable() {
        if (!this.isEnabled()) {
            return;
        }
        this.setEnabled(false);
        ModuleManager.organizedModules.remove(this);
        if (!this.alwaysOn) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        this.onDisable();
        this.notifyToggle(false);
    }

    private void notifyToggle(boolean bl) {
        try {
            Notifications.onModuleToggled(this, bl);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public String getInfo() {
        return "";
    }

    public String getInfoUpdate() {
        String string = this.getInfo();
        if (!string.equals(this.lastInfo)) {
            sort = true;
        }
        this.lastInfo = string;
        return string;
    }

    public void setEnabled(boolean bl) {
        this.enabled = bl;
    }

    public String getName() {
        return this.moduleName;
    }

    public String getNameInHud() {
        if (this instanceof AntiKnockback) {
            return "Velocity";
        }
        return this.moduleName;
    }

    public ArrayList<Setting> getSettings() {
        return this.settings;
    }

    public void registerSetting(Setting setting) {
        this.settings.add(setting);
    }

    public category moduleCategory() {
        return this.moduleCategory;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void toggle() {
        if (this.isEnabled()) {
            this.disable();
        } else {
            this.enable();
        }
    }

    public void onUpdate() {
    }

    public void guiUpdate() {
    }

    public void guiButtonToggled(ButtonSetting buttonSetting) {
    }

    public void onSlide(SliderSetting sliderSetting) {
    }

    public double getLagDurationMs() {
        return 0.0;
    }

    public int getKeycode() {
        return this.keycode;
    }

    public void setBind(int n) {
        this.keycode = n;
    }

    static {
        categoriesString = new ArrayList<String>();
        for (category category2 : category.values()) {
            categoriesString.add(category2.name());
        }
    }

    public static enum category {
        combat,
        movement,
        player,
        world,
        render,
        ambiance,
        minigames,
        fun,
        other,
        client;

    }
}

