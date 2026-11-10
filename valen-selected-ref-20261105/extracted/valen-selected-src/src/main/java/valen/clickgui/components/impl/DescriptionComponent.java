/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.components.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.impl.client.Gui;
import valen.module.setting.impl.DescriptionSetting;
import valen.utility.font.ValenFontRenderer;

public class DescriptionComponent
extends Component {
    private static final float ROW_LEFT_INSET = 14.0f;
    public DescriptionSetting desc;
    private ModuleComponent p;
    public float o;
    public float x;
    public float y;
    private String cachedText;
    private float cachedAvailableWidth;
    private String cachedFontName;
    private final List<String> lines = new ArrayList<String>();

    public DescriptionComponent(DescriptionSetting descriptionSetting, ModuleComponent moduleComponent, float f) {
        this.desc = descriptionSetting;
        this.p = moduleComponent;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.getWidth();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.yPos;
        this.o = f;
    }

    @Override
    public void render() {
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        this.ensureWrapped(valenFontRenderer);
        float f = this.getLineHeight(valenFontRenderer);
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)0.5, (double)0.5, (double)0.5);
        for (int i = 0; i < this.lines.size(); ++i) {
            valenFontRenderer.drawString(this.lines.get(i), (this.p.categoryComponent.getX() + 14.0f) * 2.0f, (this.p.categoryComponent.getY() + this.o + 5.0f + (float)i * f) * 2.0f, ClickGuiStyle.TEXT_MUTED, false);
        }
        GlStateManager.popMatrix();
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
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        this.ensureWrapped(valenFontRenderer);
        return (float)Math.max(1, this.lines.size()) * this.getLineHeight(valenFontRenderer) + 10.0f;
    }

    @Override
    public int getHeight() {
        return Math.round(this.getHeightF());
    }

    @Override
    public boolean isBaseVisible() {
        return this.desc.visible;
    }

    private float getLineHeight(ValenFontRenderer valenFontRenderer) {
        return (float)valenFontRenderer.getFontHeight() * 0.5f + 2.0f;
    }

    private void ensureWrapped(ValenFontRenderer valenFontRenderer) {
        String string = this.desc.getDesc() == null ? "" : this.desc.getDesc();
        float f = Math.max(80.0f, (this.p.categoryComponent.getWidth() - 28.0f) * 2.0f);
        String string2 = Gui.getSelectedFontName();
        if (string.equals(this.cachedText) && f == this.cachedAvailableWidth && string2.equals(this.cachedFontName)) {
            return;
        }
        this.cachedText = string;
        this.cachedAvailableWidth = f;
        this.cachedFontName = string2;
        this.lines.clear();
        StringBuilder stringBuilder = new StringBuilder();
        for (String string3 : string.split(" ")) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append(string3);
                continue;
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            if ((float)valenFontRenderer.getStringWidth(stringBuilder2.append((Object)stringBuilder).append(" ").append(string3).toString()) <= f) {
                stringBuilder.append(' ').append(string3);
                continue;
            }
            this.lines.add(stringBuilder.toString());
            stringBuilder.setLength(0);
            stringBuilder.append(string3);
        }
        if (stringBuilder.length() > 0) {
            this.lines.add(stringBuilder.toString());
        }
    }
}

