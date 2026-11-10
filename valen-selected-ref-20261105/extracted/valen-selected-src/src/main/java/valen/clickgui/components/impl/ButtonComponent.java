/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.components.impl;

import valen.clickgui.ClickGui;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.module.Module;
import valen.module.setting.impl.ButtonSetting;

public class ButtonComponent
extends Component {
    private static final float ROW_HEIGHT = 20.0f;
    private static final float RUN_WIDTH = 44.0f;
    private Module mod;
    public ButtonSetting buttonSetting;
    private ModuleComponent moduleComponent;
    public float o;
    public float x;
    private float y;
    public float xOffset;
    private final ToggleAnimation toggleAnim = new ToggleAnimation(140L);
    private final ToggleAnimation runHoverAnim = new ToggleAnimation(130L);

    public ButtonComponent(Module module, ButtonSetting buttonSetting, ModuleComponent moduleComponent, float f) {
        this.mod = module;
        this.buttonSetting = buttonSetting;
        this.moduleComponent = moduleComponent;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.getWidth();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.yPos;
        this.o = f;
    }

    private float rowLeft() {
        return this.moduleComponent.categoryComponent.getX() + 14.0f + this.xOffset / 2.0f;
    }

    private float rowRight() {
        return this.moduleComponent.categoryComponent.getX() + this.moduleComponent.categoryComponent.getWidth() - 14.0f;
    }

    @Override
    public void render() {
        float f = this.rowLeft();
        float f2 = this.rowRight();
        float f3 = this.moduleComponent.categoryComponent.getY() + this.o;
        float f4 = f3 + 20.0f;
        boolean bl = this.buttonSetting.isMethodButton || this.buttonSetting.isToggled();
        this.renderStatePill(f2, f3, f4);
        PageUI.small(this.buttonSetting.getName(), f, PageUI.smallY(f3, f4), bl ? ClickGuiStyle.TEXT_SECONDARY : ClickGuiStyle.TEXT_MUTED);
    }

    private void renderStatePill(float f, float f2, float f3) {
        if (this.buttonSetting.isMethodButton) {
            float f4 = f2 + 2.0f;
            this.runHoverAnim.setTarget(this.overRunButton(ClickGui.lastLogicalMouseX, ClickGui.lastLogicalMouseY));
            Controls.button("Run", f - 44.0f, f4, f, f4 + 16.0f, ClickGuiStyle.INPUT_BACKGROUND, ClickGuiStyle.TEXT_PRIMARY, this.runHoverAnim.getValue(), 1.0f);
            return;
        }
        this.toggleAnim.setTarget(this.buttonSetting.isToggled());
        Controls.toggle(f - 26.0f, f2 + (f3 - f2 - 13.0f) / 2.0f, this.toggleAnim.getValue(), 1.0f);
    }

    private boolean overRunButton(int n, int n2) {
        float f = this.y + 2.0f;
        return this.moduleComponent.isOpened && this.moduleComponent.isVisible(this) && (float)n >= this.rowRight() - 44.0f && (float)n <= this.rowRight() && (float)n2 >= f && (float)n2 <= f + 16.0f;
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
        return this.buttonSetting.visible;
    }

    @Override
    public void drawScreen(int n, int n2) {
        this.y = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        this.x = this.moduleComponent.categoryComponent.getX();
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (this.overSetting(n, n2) && n3 == 0 && this.moduleComponent.isOpened && this.moduleComponent.isVisible(this)) {
            if (this.buttonSetting.isMethodButton) {
                this.buttonSetting.runMethod();
                return true;
            }
            this.buttonSetting.toggle();
            this.mod.guiButtonToggled(this.buttonSetting);
            return true;
        }
        return false;
    }

    public boolean overSetting(int n, int n2) {
        return (float)n > this.rowLeft() && (float)n < this.rowRight() && (float)n2 > this.y && (float)n2 < this.y + 20.0f;
    }
}

