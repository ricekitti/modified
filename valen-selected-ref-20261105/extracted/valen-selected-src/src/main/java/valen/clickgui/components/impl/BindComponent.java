/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package valen.clickgui.components.impl;

import org.lwjgl.input.Keyboard;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Shape;
import valen.module.impl.client.Gui;
import valen.module.setting.impl.KeySetting;
import valen.utility.KeybindConflicts;
import valen.utility.RenderUtils;
import valen.utility.font.ValenFontRenderer;

public class BindComponent
extends Component {
    private static final String EYE_ICON_PATH = "/assets/valen/textures/gui/eye.png";
    private static final String EYE_OFF_ICON_PATH = "/assets/valen/textures/gui/eye_off.png";
    private static final int EYE_ICON_PADDING = 14;
    public boolean isBinding;
    public ModuleComponent moduleComponent;
    public float o;
    public float x;
    private float y;
    public KeySetting keySetting;
    public float xOffset;

    public BindComponent(ModuleComponent moduleComponent, float f) {
        this.moduleComponent = moduleComponent;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.getWidth();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.yPos;
        this.o = f;
    }

    public BindComponent(ModuleComponent moduleComponent, KeySetting keySetting, float f) {
        this.moduleComponent = moduleComponent;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.getWidth();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.yPos;
        this.keySetting = keySetting;
        this.o = f;
    }

    @Override
    public void updateHeight(float f) {
        this.o = f;
    }

    @Override
    public float getOffset() {
        return this.o;
    }

    @Override
    public boolean isBaseVisible() {
        return this.keySetting == null || this.keySetting.visible;
    }

    @Override
    public void render() {
        float f;
        float f2;
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        float f3 = this.moduleComponent.categoryComponent.getX() + 14.0f + this.xOffset / 2.0f;
        float f4 = this.moduleComponent.categoryComponent.getX() + this.moduleComponent.categoryComponent.getWidth() - 14.0f;
        float f5 = 20.0f;
        float f6 = this.moduleComponent.categoryComponent.getY() + this.o;
        String string = KeybindConflicts.find(this.keySetting == null ? this.moduleComponent.mod.getKeycode() : this.keySetting.getKey(), this.moduleComponent.mod, this.keySetting, null);
        String string2 = this.getBindLabel();
        String string3 = this.getBindValue();
        float f7 = f4;
        PageUI.small(string2, f3, PageUI.smallY(f6, f6 + f5), ClickGuiStyle.TEXT_SECONDARY);
        if (this.keySetting == null && !(this.moduleComponent.mod instanceof Gui)) {
            int n = this.getEyeIconSize();
            f2 = this.getEyeIconX(n);
            f = f6 + (f5 - (float)n) / 2.0f;
            f7 = f2 - 4.0f;
            int n2 = !this.moduleComponent.mod.hidden ? ClickGuiStyle.ACCENT : ClickGuiStyle.HIDDEN;
            String string4 = this.moduleComponent.mod.isHidden() ? EYE_OFF_ICON_PATH : EYE_ICON_PATH;
            RenderUtils.drawIcon(RenderUtils.getIcon(string4), f2, f, n, n2);
        }
        float f8 = 14.0f;
        f2 = Math.max(28.0f, (float)valenFontRenderer.getStringWidth(string3) * 0.5f + 16.0f);
        f = Math.max(f3 + 32.0f, f7 - f2);
        float f9 = f6 + (f5 - f8) / 2.0f;
        int n = this.isBinding ? ClickGuiStyle.ACCENT : (!string.isEmpty() ? ClickGuiStyle.WARNING : ClickGuiStyle.TEXT_SECONDARY);
        Shape.capsule(f, f9, f7, f9 + f8, this.isBinding ? ClickGuiStyle.INPUT_BACKGROUND_FOCUSED : ClickGuiStyle.INPUT_BACKGROUND);
        if (this.isBinding) {
            Shape.capsuleRing(f, f9, f7, f9 + f8, 1.0f, ClickGuiStyle.INPUT_OUTLINE_FOCUSED);
        }
        PageUI.smallCentred(string3, f, f7, f9, f9 + f8, n);
    }

    @Override
    public void drawScreen(int n, int n2) {
        this.y = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        this.x = this.moduleComponent.categoryComponent.getX();
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (!(this.overSetting(n, n2) && this.moduleComponent.isOpened && this.moduleComponent.isVisible(this))) {
            return false;
        }
        if (n3 == 0 && this.overEyeIcon(n, n2)) {
            this.moduleComponent.mod.setHidden(!this.moduleComponent.mod.isHidden());
            return true;
        }
        if ((this.keySetting != null || this.moduleComponent.mod.canBeEnabled()) && n3 == 0 && this.overBindControl(n, n2)) {
            this.isBinding = !this.isBinding;
            return true;
        }
        if ((this.keySetting != null || this.moduleComponent.mod.canBeEnabled()) && n3 > 1 && this.isBinding) {
            if (this.keySetting != null) {
                this.keySetting.setKey(n3 + 1000);
            } else {
                this.moduleComponent.mod.setBind(n3 + 1000);
            }
            this.isBinding = false;
            return true;
        }
        return false;
    }

    private boolean overEyeIcon(int n, int n2) {
        if (this.keySetting != null || this.moduleComponent.mod instanceof Gui) {
            return false;
        }
        int n3 = this.getEyeIconSize();
        float f = this.getEyeIconX(n3);
        float f2 = this.getEyeIconY(n3);
        return (float)n >= f && (float)n < f + (float)n3 && (float)n2 >= f2 && (float)n2 < f2 + (float)n3;
    }

    private String getBindLabel() {
        if (this.keySetting != null) {
            return this.keySetting.getName();
        }
        return this.moduleComponent.mod instanceof Gui ? "Open GUI" : "Bind";
    }

    private String getBindValue() {
        if (this.keySetting == null && !this.moduleComponent.mod.canBeEnabled()) {
            return "Unavailable";
        }
        return this.isBinding ? "..." : ((this.keySetting != null ? this.keySetting.getKey() : this.moduleComponent.mod.getKeycode()) == 0 ? "\u2014" : this.getKeyAsStr(this.keySetting != null));
    }

    private boolean overBindControl(int n, int n2) {
        if (!this.overSetting(n, n2)) {
            return false;
        }
        return this.keySetting != null || !this.overEyeIcon(n, n2);
    }

    private int getEyeIconSize() {
        int n = Math.round((float)Gui.getClickGuiSettingFontRenderer().getFontHeight() * 0.5f);
        return Math.max(6, n - 1);
    }

    private float getEyeIconX(int n) {
        return this.moduleComponent.categoryComponent.getX() + this.moduleComponent.categoryComponent.getWidth() - (float)n - 14.0f;
    }

    private float getEyeIconY(int n) {
        return this.moduleComponent.categoryComponent.getModuleY() + this.o + (20.0f - (float)n) / 2.0f;
    }

    @Override
    public void onScroll(int n) {
        if (!this.isBinding || n == 0) {
            return;
        }
        if (this.keySetting != null) {
            this.keySetting.setKey(n > 0 ? 1069 : 1070);
        } else {
            this.moduleComponent.mod.setBind(n > 0 ? 1069 : 1070);
        }
        this.isBinding = false;
    }

    @Override
    public void keyTyped(char c, int n) {
        if (!this.isBinding) {
            return;
        }
        if (n == 11 || n == 1) {
            if (this.moduleComponent.mod instanceof Gui) {
                this.moduleComponent.mod.setBind(54);
            } else if (this.keySetting != null) {
                this.keySetting.setKey(0);
            } else {
                this.moduleComponent.mod.setBind(0);
            }
        } else if (this.keySetting != null) {
            this.keySetting.setKey(n);
        } else {
            this.moduleComponent.mod.setBind(n);
        }
        this.isBinding = false;
    }

    public boolean overSetting(int n, int n2) {
        float f = this.moduleComponent.categoryComponent.getX() + 14.0f + this.xOffset / 2.0f;
        float f2 = this.moduleComponent.categoryComponent.getX() + this.moduleComponent.categoryComponent.getWidth() - 14.0f;
        float f3 = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        return (float)n > f && (float)n < f2 && (float)n2 > f3 && (float)n2 < f3 + 20.0f;
    }

    public String getKeyAsStr(boolean bl) {
        return BindComponent.keyDisplayName(bl ? this.keySetting.getKey() : this.moduleComponent.mod.getKeycode());
    }

    public static String keyDisplayName(int n) {
        if (n >= 1000) {
            if (n == 1069) {
                return "MScrollUp";
            }
            if (n == 1070) {
                return "MScrollDown";
            }
            return "M" + (n - 1000);
        }
        String string = Keyboard.getKeyName((int)n);
        return string == null ? "?" : string;
    }

    @Override
    public float getHeightF() {
        return 20.0f;
    }

    @Override
    public int getHeight() {
        return Math.round(this.getHeightF());
    }

    @Override
    public void onGuiClosed() {
        this.isBinding = false;
    }
}

