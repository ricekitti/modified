/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 */
package valen.clickgui.components.impl;

import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.components.Component;
import valen.clickgui.components.FocusableTextComponent;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ClickGuiTextField;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Shape;
import valen.module.impl.client.Gui;
import valen.utility.font.ValenFontRenderer;

public abstract class AbstractTextInputComponent
extends Component
implements FocusableTextComponent {
    protected static final float ROW_HEIGHT = 20.0f;
    protected static final float TEXT_SCALE = 0.5f;
    protected static final int DEFAULT_TEXT_MAX_LENGTH = 128;
    protected static final float LIST_CONTENT_INSET = 6.0f;
    protected final ModuleComponent moduleComponent;
    public float o;
    public float xOffset;
    private final ClickGuiTextField textField;

    protected AbstractTextInputComponent(ModuleComponent moduleComponent, float f, String string, int n) {
        this.moduleComponent = moduleComponent;
        this.o = f;
        this.textField = new ClickGuiTextField(string, n, 0.5f);
    }

    protected final ClickGuiTextField getTextField() {
        return this.textField;
    }

    protected final Layout layout(boolean bl) {
        Layout layout = new Layout();
        layout.cx = this.moduleComponent.categoryComponent.getX();
        layout.cy = bl ? this.moduleComponent.categoryComponent.getModuleY() : this.moduleComponent.categoryComponent.getY();
        layout.cw = this.moduleComponent.categoryComponent.getWidth();
        layout.left = layout.cx + 14.0f + this.xOffset / 2.0f;
        layout.right = layout.cx + layout.cw - 14.0f;
        layout.searchTop = layout.cy + this.o + 20.0f;
        layout.contentTop = layout.cy + this.o + 40.0f;
        return layout;
    }

    protected final void renderTextInput(String string) {
        Layout layout = this.layout(false);
        this.renderLabel(layout, string);
        this.renderTextField(layout);
    }

    protected final void renderLabel(Layout layout, String string) {
        AbstractTextInputComponent.drawScaledText(string, layout.left, AbstractTextInputComponent.centeredScaledTextY(layout.cy + this.o, 20.0f), ClickGuiStyle.TEXT_SECONDARY);
    }

    protected final void renderTextField(Layout layout) {
        float f = layout.searchTop + 1.0f;
        float f2 = layout.searchTop + 20.0f - 1.0f;
        this.textField.render(layout.left, f, layout.right, f2);
    }

    protected final boolean isTextFieldClicked(int n, int n2, Layout layout) {
        float f = layout.searchTop + 1.0f;
        float f2 = layout.searchTop + 20.0f - 1.0f;
        return this.textField.contains(n, n2, layout.left, f, layout.right, f2);
    }

    protected final float getTextFieldTop(Layout layout) {
        return layout.searchTop + 1.0f;
    }

    protected final float getTextFieldBottom(Layout layout) {
        return layout.searchTop + 20.0f - 1.0f;
    }

    protected final void setTextFieldFocused(boolean bl) {
        this.textField.setFocused(bl);
    }

    protected final boolean isTextFieldFocused() {
        return this.textField.isFocused();
    }

    @Override
    public void drawScreen(int n, int n2) {
        this.textField.tickCursor();
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
    public float getHeightF() {
        return 40.0f;
    }

    @Override
    public boolean isTextInputFocused() {
        return this.isTextFieldFocused();
    }

    @Override
    public boolean containsClick(int n, int n2) {
        return this.isTextFieldClicked(n, n2, this.layout(true));
    }

    @Override
    public void unfocusTextInput() {
        this.setTextFieldFocused(false);
    }

    @Override
    public void onGuiClosed() {
        this.setTextFieldFocused(false);
    }

    public void setXOffset(float f) {
        this.xOffset = f;
    }

    public abstract String getGroupName();

    protected static void drawListWell(float f, float f2, float f3, float f4) {
        Shape.surface(f, f2, f3, f4, ClickGuiStyle.INPUT_BACKGROUND);
    }

    protected static void drawListRow(float f, float f2, float f3, int n) {
        Shape.capsule(f + 1.0f, f2 + 1.0f, f3 - 1.0f, f2 + 20.0f - 1.0f, n);
    }

    protected static float centeredScaledTextY(float f, float f2) {
        return AbstractTextInputComponent.centeredScaledTextY(f, f2, 0.5f);
    }

    protected static float centeredScaledTextY(float f, float f2, float f3) {
        return PageUI.textY(Gui.getClickGuiSettingFontRenderer(), f3, f, f + f2);
    }

    protected static void drawScaledText(String string, float f, float f2, int n) {
        AbstractTextInputComponent.drawScaledText(string, f, f2, n, 0.5f);
    }

    protected static void drawScaledText(String string, float f, float f2, int n, float f3) {
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)f3, (double)f3, (double)f3);
        valenFontRenderer.drawString(string, f / f3, f2 / f3, n, false);
        GlStateManager.popMatrix();
    }
}

