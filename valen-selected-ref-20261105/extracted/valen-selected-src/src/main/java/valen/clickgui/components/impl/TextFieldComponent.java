/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 *  valen.module.setting.impl.TextSetting
 */
package valen.clickgui.components.impl;

import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.setting.impl.TextSetting;

public class TextFieldComponent
extends AbstractTextInputComponent {
    public final TextSetting textSetting;
    private String valueWhenFocused;

    public TextFieldComponent(TextSetting textSetting, ModuleComponent moduleComponent, float f) {
        super(moduleComponent, f, textSetting.getPlaceholder(), textSetting.getMaxLength());
        this.textSetting = textSetting;
        this.getTextField().setText(textSetting.getText());
    }

    @Override
    public void render() {
        if (!this.isTextFieldFocused()) {
            this.valueWhenFocused = null;
            if (!this.getTextField().getText().equals(this.textSetting.getText())) {
                this.getTextField().setText(this.textSetting.getText());
            }
        }
        this.renderTextInput(this.textSetting.getName());
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this) || n3 != 0) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        if (this.isTextFieldClicked(n, n2, layout)) {
            if (!this.isTextFieldFocused()) {
                this.valueWhenFocused = this.textSetting.getText();
            }
            this.setTextFieldFocused(true);
            return true;
        }
        if (this.isTextFieldFocused()) {
            this.valueWhenFocused = null;
            this.setTextFieldFocused(false);
        }
        return false;
    }

    @Override
    public void keyTyped(char c, int n) {
        if (!this.moduleComponent.isOpened || !this.isTextFieldFocused()) {
            return;
        }
        if (n == 1) {
            this.revertToSaved();
            this.setTextFieldFocused(false);
            return;
        }
        if (n == 28 || n == 156) {
            this.textSetting.submit();
            this.valueWhenFocused = null;
            this.getTextField().setText(this.textSetting.getText());
            this.setTextFieldFocused(false);
            return;
        }
        if (this.getTextField().textboxKeyTyped(c, n)) {
            this.textSetting.setText(this.getTextField().getText());
        }
    }

    @Override
    public boolean isBaseVisible() {
        return this.textSetting.visible;
    }

    @Override
    public String getGroupName() {
        return this.textSetting.group != null ? this.textSetting.group.getName() : "";
    }

    @Override
    public boolean containsClick(int n, int n2) {
        return this.isTextFieldClicked(n, n2, this.layout(true));
    }

    @Override
    public void unfocusTextInput() {
        this.valueWhenFocused = null;
        super.unfocusTextInput();
    }

    private void revertToSaved() {
        if (this.valueWhenFocused != null) {
            this.getTextField().setText(this.valueWhenFocused);
            this.textSetting.setText(this.valueWhenFocused);
            this.valueWhenFocused = null;
        }
    }
}

