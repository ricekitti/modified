/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiTextField
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.components.impl;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Shape;
import valen.lunar.forge.fml.relauncher.ReflectionHelper;
import valen.module.impl.client.Gui;
import valen.utility.font.ValenFontRenderer;

public class ClickGuiTextField {
    private static final float DEFAULT_TEXT_SCALE = 0.5f;
    private static final Field LINE_SCROLL_OFFSET_FIELD = ReflectionHelper.findField(GuiTextField.class, "lineScrollOffset", "lineScrollOffset");
    private static final Field CURSOR_POSITION_FIELD = ReflectionHelper.findField(GuiTextField.class, "cursorPosition", "cursorPosition");
    private static final Field SELECTION_END_FIELD = ReflectionHelper.findField(GuiTextField.class, "selectionEnd", "selectionEnd");
    private static final Field IS_ENABLED_FIELD = ReflectionHelper.findField(GuiTextField.class, "isEnabled", "isEnabled");
    private static int nextId;
    private final GuiTextField textField;
    private final String placeholder;
    private final float textScale;
    private long lastCursorTick;
    private boolean cursorVisible;

    private static int backgroundColor() {
        return ClickGuiStyle.INPUT_BACKGROUND;
    }

    private static int focusedBackgroundColor() {
        return ClickGuiStyle.INPUT_BACKGROUND_FOCUSED;
    }

    private static int focusedOutlineColor() {
        return ClickGuiStyle.INPUT_OUTLINE_FOCUSED;
    }

    private static int selectionColor() {
        return ClickGuiStyle.INPUT_SELECTION;
    }

    private static int cursorColor() {
        return ClickGuiStyle.TEXT_PRIMARY;
    }

    public ClickGuiTextField(String string, int n) {
        this(string, n, 0.5f);
    }

    public ClickGuiTextField(String string, int n, float f) {
        this.placeholder = string == null ? "" : string;
        this.textScale = f;
        this.textField = new GuiTextField(nextId++, Minecraft.getMinecraft().fontRendererObj, 0, 0, 100, 20);
        this.textField.setMaxStringLength(n);
        this.textField.setEnableBackgroundDrawing(false);
        this.textField.setCanLoseFocus(true);
    }

    public void render(float f, float f2, float f3, float f4) {
        float f5 = f4 - 1.0f;
        float f6 = Math.max(0.0f, f5 - f2);
        float f7 = Math.min(f6 / 2.0f, Math.max(0.0f, (f3 - f) / 2.0f));
        float f8 = f + f7;
        float f9 = Math.max(0.0f, f3 - f - f7 * 2.0f);
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        String string = this.textField.getText();
        int n = Math.max(0, Math.min(ClickGuiTextField.getIntField(this.textField, LINE_SCROLL_OFFSET_FIELD, 0), string.length()));
        String string2 = this.getVisibleText(string, n, f9, valenFontRenderer);
        int n2 = ClickGuiTextField.getIntField(this.textField, CURSOR_POSITION_FIELD, string.length());
        int n3 = ClickGuiTextField.getIntField(this.textField, SELECTION_END_FIELD, n2);
        int n4 = n;
        int n5 = n4 + string2.length();
        float f10 = this.centeredScaledTextY(f2, f6, valenFontRenderer);
        int n6 = ClickGuiTextField.getBooleanField(this.textField, IS_ENABLED_FIELD, true) ? ClickGuiStyle.TEXT_PRIMARY : ClickGuiStyle.TEXT_DISABLED;
        Shape.capsule(f, f2, f3, f5, this.isFocused() ? ClickGuiTextField.focusedBackgroundColor() : ClickGuiTextField.backgroundColor());
        if (this.isFocused()) {
            Shape.capsuleRing(f, f2, f3, f5, 1.0f, ClickGuiTextField.focusedOutlineColor());
        }
        if (!string2.isEmpty()) {
            this.drawSelection(f8, f2, f5, valenFontRenderer, string2, n2, n3, n4, n5);
            this.drawScaledText(string2, f8, f10, n6, valenFontRenderer);
        } else if (!this.textField.isFocused() && !this.placeholder.isEmpty()) {
            this.drawScaledText(this.placeholder, f8, f10, ClickGuiStyle.TEXT_MUTED, valenFontRenderer);
        }
        if (this.textField.isFocused() && this.cursorVisible) {
            this.drawCursor(f8, f2, f5, valenFontRenderer, string2, n2, n4, n5);
        }
    }

    public void tickCursor() {
        if (System.currentTimeMillis() - this.lastCursorTick >= 500L) {
            this.lastCursorTick = System.currentTimeMillis();
            this.cursorVisible = !this.cursorVisible;
        }
        this.textField.updateCursorCounter();
    }

    public boolean contains(int n, int n2, float f, float f2, float f3, float f4) {
        return (float)n >= f && (float)n <= f3 && (float)n2 >= f2 && (float)n2 <= f4;
    }

    public boolean textboxKeyTyped(char c, int n) {
        return this.textField.textboxKeyTyped(c, n);
    }

    public String getText() {
        return this.textField.getText();
    }

    public void setText(String string) {
        this.textField.setText(string == null ? "" : string);
    }

    public boolean isFocused() {
        return this.textField.isFocused();
    }

    public void setFocused(boolean bl) {
        this.textField.setFocused(bl);
        if (bl) {
            this.cursorVisible = true;
            this.lastCursorTick = System.currentTimeMillis();
        }
    }

    private String getVisibleText(String string, int n, float f, ValenFontRenderer valenFontRenderer) {
        if (string == null || string.isEmpty() || f <= 0.0f) {
            return "";
        }
        float f2 = f / this.textScale;
        String string2 = string.substring(n);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string2.length(); ++i) {
            char c = string2.charAt(i);
            String string3 = stringBuilder.toString() + c;
            if ((float)valenFontRenderer.getStringWidth(string3) > f2) break;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private void drawSelection(float f, float f2, float f3, ValenFontRenderer valenFontRenderer, String string, int n, int n2, int n3, int n4) {
        int n5;
        int n6 = Math.min(n, n2);
        if (n6 == (n5 = Math.max(n, n2)) || n5 <= n3 || n6 >= n4) {
            return;
        }
        int n7 = Math.max(0, n6 - n3);
        int n8 = Math.min(string.length(), n5 - n3);
        float f4 = f + (float)valenFontRenderer.getStringWidth(string.substring(0, n7)) * this.textScale;
        float f5 = f + (float)valenFontRenderer.getStringWidth(string.substring(0, n8)) * this.textScale;
        if (f5 > f4) {
            Shape.capsule(f4, f2 + 3.0f, f5, f3 - 3.0f, ClickGuiTextField.selectionColor());
        }
    }

    private void drawCursor(float f, float f2, float f3, ValenFontRenderer valenFontRenderer, String string, int n, int n2, int n3) {
        float f4 = n <= n2 ? f : (n >= n3 ? f + (float)valenFontRenderer.getStringWidth(string) * this.textScale : f + (float)valenFontRenderer.getStringWidth(string.substring(0, n - n2)) * this.textScale);
        Shape.capsule(f4, f2 + 4.0f, f4 + 1.0f, f3 - 4.0f, ClickGuiTextField.cursorColor());
    }

    private float centeredScaledTextY(float f, float f2, ValenFontRenderer valenFontRenderer) {
        return PageUI.textY(valenFontRenderer, this.textScale, f, f + f2);
    }

    private void drawScaledText(String string, float f, float f2, int n, ValenFontRenderer valenFontRenderer) {
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)this.textScale, (double)this.textScale, (double)this.textScale);
        valenFontRenderer.drawString(string, f / this.textScale, f2 / this.textScale, n, false);
        GlStateManager.popMatrix();
    }

    private static int getIntField(GuiTextField guiTextField, Field field, int n) {
        try {
            return field.getInt(guiTextField);
        }
        catch (IllegalAccessException illegalAccessException) {
            return n;
        }
    }

    private static boolean getBooleanField(GuiTextField guiTextField, Field field, boolean bl) {
        try {
            return field.getBoolean(guiTextField);
        }
        catch (IllegalAccessException illegalAccessException) {
            return bl;
        }
    }
}

