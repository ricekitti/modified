/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.components.impl;

import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.impl.client.Gui;
import valen.module.setting.impl.GroupSetting;
import valen.utility.font.ValenFontRenderer;

public class GroupComponent
extends Component {
    public GroupSetting setting;
    private ModuleComponent component;
    public float o;
    public final boolean opened = true;

    public GroupComponent(GroupSetting groupSetting, ModuleComponent moduleComponent, float f) {
        this.setting = groupSetting;
        this.component = moduleComponent;
        this.o = f;
    }

    public float getAnimationProgress() {
        return 1.0f;
    }

    @Override
    public void render() {
        float f = this.component.categoryComponent.getX() + 14.0f;
        float f2 = this.component.categoryComponent.getY() + this.o + 2.0f;
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)0.5, (double)0.5, (double)0.5);
        valenFontRenderer.drawString(this.setting.getName(), f * 2.0f, (f2 + 4.0f) * 2.0f, ClickGuiStyle.TEXT_MUTED, false);
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
    public void drawScreen(int n, int n2) {
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        return false;
    }

    @Override
    public void onGuiClosed() {
    }

    @Override
    public boolean isBaseVisible() {
        return this.setting.visible;
    }
}

