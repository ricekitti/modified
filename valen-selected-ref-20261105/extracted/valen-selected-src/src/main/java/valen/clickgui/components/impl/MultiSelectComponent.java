/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.components.impl;

import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Shape;
import valen.module.impl.client.Gui;
import valen.module.setting.impl.MultiSelectSetting;
import valen.utility.font.ValenFontRenderer;

public final class MultiSelectComponent
extends Component {
    private static final float LABEL_HEIGHT = 20.0f;
    private static final float CHIP_HEIGHT = 20.0f;
    private static final float GAP = 4.0f;
    private final MultiSelectSetting setting;
    private final ModuleComponent module;
    private final ToggleAnimation[] selectionAnimations;
    private final ToggleAnimation[] hoverAnimations;
    private float offset;
    private int hovered = -1;
    private float layoutWidth = -1.0f;
    private ValenFontRenderer layoutFont;
    private Layout layout;

    public MultiSelectComponent(MultiSelectSetting multiSelectSetting, ModuleComponent moduleComponent, float f) {
        this.setting = multiSelectSetting;
        this.module = moduleComponent;
        this.offset = f;
        this.selectionAnimations = new ToggleAnimation[multiSelectSetting.size()];
        this.hoverAnimations = new ToggleAnimation[multiSelectSetting.size()];
        for (int i = 0; i < multiSelectSetting.size(); ++i) {
            this.selectionAnimations[i] = new ToggleAnimation(140L);
            this.hoverAnimations[i] = new ToggleAnimation(130L);
        }
    }

    private float left() {
        return this.module.categoryComponent.getX() + 14.0f;
    }

    private Layout layout() {
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        float f = Math.max(1.0f, this.module.categoryComponent.getWidth() - 28.0f);
        if (this.layout == null || f != this.layoutWidth || valenFontRenderer != this.layoutFont) {
            float[] fArray = new float[this.setting.size()];
            for (int i = 0; i < fArray.length; ++i) {
                fArray[i] = Math.max(26.0f, (float)valenFontRenderer.getStringWidth(this.setting.getOption(i)) * 0.5f + 16.0f);
            }
            this.layout = new Layout(f, fArray);
            this.layoutWidth = f;
            this.layoutFont = valenFontRenderer;
        }
        return this.layout;
    }

    @Override
    public void render() {
        Layout layout = this.layout();
        ValenFontRenderer valenFontRenderer = this.layoutFont;
        float f = this.left();
        float f2 = this.module.categoryComponent.getY() + this.offset;
        valenFontRenderer.drawScaledString(this.setting.getName(), f, PageUI.textY(valenFontRenderer, 0.5f, f2, f2 + 20.0f), 0.5f, ClickGuiStyle.TEXT_SECONDARY);
        for (int i = 0; i < this.setting.size(); ++i) {
            this.selectionAnimations[i].setTarget(this.setting.isSelected(i));
            this.hoverAnimations[i].setTarget(this.hovered == i);
            float f3 = this.selectionAnimations[i].getValue();
            float f4 = f + layout.x[i];
            float f5 = f2 + 20.0f + layout.y[i];
            int n = ClickGuiStyle.blend(ClickGuiStyle.hovered(ClickGuiStyle.INPUT_BACKGROUND, this.hoverAnimations[i].getValue()), ClickGuiStyle.TOGGLE_ON, f3);
            Shape.capsule(f4, f5, f4 + layout.widths[i], f5 + 20.0f, n);
            String string = this.setting.getOption(i);
            while (!string.isEmpty() && (float)valenFontRenderer.getStringWidth(string) * 0.5f > layout.widths[i] - 8.0f) {
                string = string.substring(0, string.length() - 1);
            }
            float f6 = f4 + (layout.widths[i] - (float)valenFontRenderer.getStringWidth(string) * 0.5f) / 2.0f;
            int n2 = ClickGuiStyle.blend(ClickGuiStyle.TEXT_MUTED, ClickGuiStyle.knobColorForToggle(1.0f), f3);
            valenFontRenderer.drawScaledString(string, f6, PageUI.textY(valenFontRenderer, 0.5f, f5, f5 + 20.0f), 0.5f, n2);
        }
    }

    private int optionAt(int n, int n2) {
        return this.layout().optionAt((float)n - this.left(), (float)n2 - this.module.categoryComponent.getModuleY() - this.offset - 20.0f);
    }

    @Override
    public void drawScreen(int n, int n2) {
        this.hovered = this.module.isOpened && this.module.isVisible(this) ? this.optionAt(n, n2) : -1;
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (n3 != 0 || !this.module.isOpened || !this.module.isVisible(this)) {
            return false;
        }
        int n4 = this.optionAt(n, n2);
        if (n4 < 0) {
            return false;
        }
        this.setting.toggle(n4);
        return true;
    }

    @Override
    public void updateHeight(float f) {
        this.offset = f;
    }

    @Override
    public float getOffset() {
        return this.offset;
    }

    @Override
    public float getHeightF() {
        return 20.0f + this.layout().height + 4.0f;
    }

    @Override
    public boolean isBaseVisible() {
        return this.setting.visible;
    }

    @Override
    public void onGuiClosed() {
        this.hovered = -1;
        for (ToggleAnimation toggleAnimation : this.hoverAnimations) {
            toggleAnimation.snap(false);
        }
    }

    public static final class Layout {
        public final float[] x;
        public final float[] y;
        public final float[] widths;
        public final float height;

        public Layout(float f, float[] fArray) {
            this.x = new float[fArray.length];
            this.y = new float[fArray.length];
            this.widths = new float[fArray.length];
            float f2 = 0.0f;
            float f3 = 0.0f;
            float f4 = Math.max(1.0f, f);
            for (int i = 0; i < this.widths.length; ++i) {
                this.widths[i] = Math.min(f4, Math.max(1.0f, fArray[i]));
                if (f2 > 0.0f && f2 + this.widths[i] > f4) {
                    f2 = 0.0f;
                    f3 += 24.0f;
                }
                this.x[i] = f2;
                this.y[i] = f3;
                f2 += this.widths[i] + 4.0f;
            }
            this.height = this.widths.length == 0 ? 0.0f : f3 + 20.0f;
        }

        public int optionAt(float f, float f2) {
            for (int i = 0; i < this.widths.length; ++i) {
                if (!(f >= this.x[i]) || !(f < this.x[i] + this.widths[i]) || !(f2 >= this.y[i]) || !(f2 < this.y[i] + 20.0f)) continue;
                return i;
            }
            return -1;
        }
    }
}

