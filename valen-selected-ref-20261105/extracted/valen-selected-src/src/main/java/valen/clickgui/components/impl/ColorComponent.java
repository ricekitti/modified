/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 */
package valen.clickgui.components.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.FocusableTextComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.Shape;
import valen.module.setting.impl.ColorSetting;
import valen.utility.RecentColors;
import valen.utility.RenderUtils;

public class ColorComponent
extends Component
implements FocusableTextComponent {
    public ColorSetting colorSetting;
    private ModuleComponent moduleComponent;
    public float o;
    public float x;
    private float y;
    public float xOffset;
    public boolean expanded;
    private int dragMode;
    private int colorAtDragStart;
    private float cachedHue;
    private float cachedSat;
    private float cachedBri;
    private boolean animating;
    private long animationStartNs;
    private float animationProgress;
    private float animationStartProgress;
    private float animationTargetProgress;
    private static final float EXPAND_RESPONSE = 0.3f;
    private static final float LABEL_HEIGHT = 20.0f;
    private static final float ROW_LEFT_INSET = 14.0f;
    private static final float BODY_GAP = 4.0f;
    private static final float BOTTOM_PAD = 8.0f;
    private static final float SV_WIDTH = 112.0f;
    private static final float SV_HEIGHT = 72.0f;
    private static final float COLUMN_GAP = 16.0f;
    private static final float STRIP_HEIGHT = 10.0f;
    private static final float THUMB = 12.0f;
    private static final float WELL = 16.0f;
    private static final float WELL_RING = 2.0f;
    private static final float WELL_SWATCH = 9.0f;
    private static final float SWATCH = 14.0f;
    private static final float SWATCH_STEP = 20.0f;
    private static final float HEX_WIDTH = 62.0f;
    private static final float CHECKER_CELL = 3.0f;
    private static final int CHECKER_LIGHT = -1776409;
    private static final int CHECKER_DARK = -5197642;
    private static final float BLACK_BRI_EPSILON = 0.001f;
    private static final float GREY_SAT_EPSILON = 0.001f;
    private static final int DRAG_SV = 1;
    private static final int DRAG_HUE = 2;
    private static final int DRAG_ALPHA = 3;
    private boolean hexEditing;
    private boolean hexReplace;
    private boolean hexInvalid;
    private String hexText = "";
    private int hoveredSwatch = -1;
    private boolean hexHovered;
    private final ToggleAnimation hexHoverAnim = new ToggleAnimation(130L);

    public ColorComponent(ColorSetting colorSetting, ModuleComponent moduleComponent, float f) {
        this.colorSetting = colorSetting;
        this.moduleComponent = moduleComponent;
        this.o = f;
        this.animationProgress = 0.0f;
        this.animationStartProgress = 0.0f;
        this.animationTargetProgress = 0.0f;
    }

    public float getExpandedHeight() {
        return 104.0f;
    }

    public float getAnimationProgress() {
        if (this.animating) {
            float f = 20.94395f;
            float f2 = (float)(System.nanoTime() - this.animationStartNs) / 1.0E9f;
            float f3 = 1.0f - (1.0f + f * f2) * (float)Math.exp(-f * f2);
            if (f3 >= 0.998f) {
                this.animating = false;
                this.animationProgress = this.animationTargetProgress;
                this.animationStartProgress = this.animationTargetProgress;
            } else {
                this.animationProgress = this.animationStartProgress + (this.animationTargetProgress - this.animationStartProgress) * f3;
            }
        }
        return this.animationProgress;
    }

    private Frame frame(float f) {
        Frame frame = new Frame();
        float f2 = this.moduleComponent.categoryComponent.getX();
        float f3 = this.moduleComponent.categoryComponent.getWidth();
        frame.rowLeft = f2 + 14.0f + this.xOffset / 2.0f;
        frame.rowRight = f2 + f3 - 14.0f;
        frame.rowTop = f + this.o;
        frame.svLeft = frame.rowLeft;
        frame.svTop = frame.rowTop + 20.0f + 4.0f;
        frame.svRight = frame.svLeft + 112.0f;
        frame.svBottom = frame.svTop + 72.0f;
        frame.columnLeft = frame.svRight + 16.0f;
        frame.columnRight = frame.rowRight;
        boolean bl = this.colorSetting.hasAlpha();
        float f4 = 10.0f + (bl ? 10.0f : 0.0f) + 16.0f;
        float f5 = (72.0f - f4) / (bl ? 4.0f : 3.0f);
        frame.hueTop = frame.svTop + f5;
        frame.alphaTop = frame.hueTop + 10.0f + f5;
        frame.bottomTop = (bl ? frame.alphaTop + 10.0f : frame.hueTop + 10.0f) + f5;
        frame.hexLeft = frame.columnRight - 62.0f;
        float f6 = frame.hexLeft - 10.0f - frame.columnLeft + 6.0f;
        frame.swatches = Math.max(0, Math.min(12, (int)Math.floor(f6 / 20.0f)));
        return frame;
    }

    @Override
    public void render() {
        Frame frame = this.frame(this.moduleComponent.categoryComponent.getY());
        float f = PageUI.smallY(frame.rowTop, frame.rowTop + 20.0f);
        PageUI.small(PageUI.truncateSmall(this.colorSetting.getName(), frame.rowRight - frame.rowLeft - 32.0f), frame.rowLeft, f, ClickGuiStyle.TEXT_SECONDARY);
        float f2 = this.getAnimationProgress();
        float f3 = frame.rowRight - 8.0f;
        float f4 = frame.rowTop + 10.0f;
        Shape.conicRing(f3, f4, 16.0f, 2.0f, 1.0f);
        this.drawSwatch(f3, f4, 9.0f, this.colorSetting.getColor(), this.colorSetting.hasAlpha());
        if (f2 <= 0.0f) {
            return;
        }
        float f5 = this.moduleComponent.categoryComponent.moduleY - this.moduleComponent.categoryComponent.getY();
        float f6 = frame.rowTop + 20.0f;
        float f7 = (this.getExpandedHeight() - 20.0f) * f2;
        RenderUtils.scissorPushGui(frame.rowLeft - 8.0f, f6 + f5, frame.rowRight - frame.rowLeft + 16.0f, f7);
        this.renderPicker(frame);
        RenderUtils.scissorPop();
    }

    private void renderPicker(Frame frame) {
        boolean bl;
        float f = this.dragMode != 0 ? this.cachedBri : this.colorSetting.getBrightness();
        float f2 = this.dragMode != 0 ? this.cachedSat : this.colorSetting.getSaturation();
        boolean bl2 = f < 0.001f;
        boolean bl3 = bl = f2 < 0.001f;
        if (this.dragMode == 0 && !bl2) {
            this.cachedBri = f;
            this.cachedSat = this.colorSetting.getSaturation();
            if (!bl) {
                this.cachedHue = this.colorSetting.getHue();
            }
        }
        boolean bl4 = this.dragMode != 0 || bl2 || bl;
        float f3 = bl4 ? this.cachedHue / 360.0f : this.colorSetting.getHue() / 360.0f;
        float f4 = this.dragMode != 0 || bl2 ? this.cachedSat : f2;
        int n = Color.HSBtoRGB(f3, 1.0f, 1.0f) | 0xFF000000;
        int n2 = this.colorSetting.getColor() | 0xFF000000;
        float f5 = 6.0f;
        Shape.insetGradient(frame.svLeft, frame.svTop, frame.svRight, frame.svBottom, 12.0f, f5, -1, n, -16777216, -16777216);
        Shape.ring(frame.svLeft, frame.svTop, frame.svRight, frame.svBottom, 12.0f, 1.0f, ClickGuiStyle.withAlpha(-1, 18));
        Controls.colorThumb(frame.svLeft + f5 + f4 * (112.0f - f5 * 2.0f), frame.svTop + f5 + (1.0f - f) * (72.0f - f5 * 2.0f), 12.0f, n2, 1.0f);
        Shape.hueCapsule(frame.columnLeft, frame.hueTop, frame.columnRight, frame.hueTop + 10.0f, f5, 1.0f);
        Controls.colorThumb(this.stripX(frame, f3), frame.hueTop + 5.0f, 12.0f, n, 1.0f);
        if (this.colorSetting.hasAlpha()) {
            int n3 = this.colorSetting.getRGB();
            Shape.checker(frame.columnLeft, frame.alphaTop, frame.columnRight, frame.alphaTop + 10.0f, 5.0f, 3.0f, f5, -1776409, -5197642, n3, n3 | 0xFF000000);
            float f6 = this.stripX(frame, (float)this.colorSetting.getAlpha() / 255.0f);
            float f7 = frame.alphaTop + 5.0f;
            Shape.shadow(f6 - 6.0f, f7 - 6.0f + 0.8f, f6 + 6.0f, f7 + 6.0f + 0.8f, 6.0f, 1.6f, ClickGuiStyle.withAlpha(-16777216, 120));
            Shape.circle(f6, f7, 12.0f, -1);
            this.drawSwatch(f6, f7, 8.0f, this.colorSetting.getColor(), true);
        }
        float f8 = frame.bottomTop + 8.0f;
        List<Integer> list = this.recentColors();
        for (int i = 0; i < frame.swatches; ++i) {
            boolean bl5;
            float f9 = frame.columnLeft + 7.0f + (float)i * 20.0f;
            if (i >= list.size()) {
                Shape.circleRing(f9, f8, 14.0f, 1.0f, ClickGuiStyle.withAlpha(ClickGuiStyle.TRACK, 110));
                continue;
            }
            int n4 = list.get(i);
            boolean bl6 = bl5 = (n4 & 0xFFFFFF) == this.colorSetting.getRGB();
            if (bl5 || i == this.hoveredSwatch) {
                Shape.circleRing(f9, f8, 19.0f, 1.5f, bl5 ? ClickGuiStyle.ACCENT : ClickGuiStyle.TEXT_MUTED);
            }
            this.drawSwatch(f9, f8, 14.0f, n4, false);
        }
        this.renderHexField(frame);
    }

    private void renderHexField(Frame frame) {
        float f = frame.bottomTop;
        float f2 = f + 16.0f;
        this.hexHoverAnim.setTarget(this.hexHovered || this.hexEditing);
        int n = this.hexEditing ? ClickGuiStyle.INPUT_BACKGROUND_FOCUSED : ClickGuiStyle.INPUT_BACKGROUND;
        Shape.capsule(frame.hexLeft, f, frame.columnRight, f2, ClickGuiStyle.blend(n, ClickGuiStyle.hovered(n, 1.0f), this.hexEditing ? 0.0f : this.hexHoverAnim.getValue()));
        if (this.hexEditing) {
            Shape.capsuleRing(frame.hexLeft, f, frame.columnRight, f2, 1.0f, this.hexInvalid ? ClickGuiStyle.ERROR : ClickGuiStyle.INPUT_OUTLINE_FOCUSED);
        }
        String string = this.hexEditing ? "#" + this.hexText : RecentColors.hex(this.colorSetting.getRGB());
        float f3 = frame.hexLeft + (62.0f - PageUI.smallWidth(this.hexEditing ? "#000000" : string)) / 2.0f;
        float f4 = PageUI.smallY(f, f2);
        PageUI.small(string, f3, f4, this.hexInvalid ? ClickGuiStyle.ERROR : (this.hexEditing ? ClickGuiStyle.TEXT_PRIMARY : ClickGuiStyle.TEXT_SECONDARY));
        if (this.hexEditing && System.currentTimeMillis() / 530L % 2L == 0L) {
            float f5 = f3 + PageUI.smallWidth(string) + 1.0f;
            Shape.capsule(f5, f + 4.0f, f5 + 1.0f, f2 - 4.0f, ClickGuiStyle.ACCENT);
        }
    }

    private void drawSwatch(float f, float f2, float f3, int n, boolean bl) {
        if (bl && (n >>> 24 & 0xFF) < 255) {
            float f4 = f3 / 2.0f;
            Shape.checker(f - f4, f2 - f4, f + f4, f2 + f4, f4, 3.0f, 0.0f, -1776409, -5197642, 0, 0);
            Shape.circle(f, f2, f3, n);
        } else {
            Shape.circle(f, f2, f3, n | 0xFF000000);
        }
        Shape.circleRing(f, f2, f3, 1.0f, ClickGuiStyle.DARK_THEME ? ClickGuiStyle.withAlpha(-1, 26) : ClickGuiStyle.withAlpha(-16777216, 34));
    }

    private float stripX(Frame frame, float f) {
        float f2 = 6.0f;
        return frame.columnLeft + f2 + (frame.columnRight - frame.columnLeft - f2 * 2.0f) * Math.max(0.0f, Math.min(1.0f, f));
    }

    private float stripFraction(Frame frame, float f) {
        float f2 = 6.0f;
        float f3 = frame.columnRight - frame.columnLeft - f2 * 2.0f;
        return f3 <= 0.0f ? 0.0f : Math.max(0.0f, Math.min(1.0f, (f - frame.columnLeft - f2) / f3));
    }

    private List<Integer> recentColors() {
        return Valen.preferences == null ? Collections.emptyList() : Valen.preferences.getRecentColors();
    }

    @Override
    public void drawScreen(int n, int n2) {
        this.y = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        this.x = this.moduleComponent.categoryComponent.getX();
        this.hoveredSwatch = -1;
        this.hexHovered = false;
        if (this.expanded && this.getAnimationProgress() >= 1.0f && this.moduleComponent.isOpened && this.moduleComponent.isVisible(this)) {
            Frame frame = this.frame(this.moduleComponent.categoryComponent.getModuleY());
            this.hoveredSwatch = this.swatchAt(frame, n, n2);
            this.hexHovered = this.overHex(frame, n, n2);
        }
        if (this.dragMode == 0 || this.getAnimationProgress() < 1.0f) {
            return;
        }
        this.applyPointer(n, n2);
    }

    private void applyPointer(int n, int n2) {
        Frame frame = this.frame(this.moduleComponent.categoryComponent.getModuleY());
        if (this.dragMode == 1) {
            float f = 6.0f;
            this.cachedSat = Math.max(0.0f, Math.min(1.0f, ((float)n - frame.svLeft - f) / (112.0f - f * 2.0f)));
            this.cachedBri = Math.max(0.0f, Math.min(1.0f, 1.0f - ((float)n2 - frame.svTop - f) / (72.0f - f * 2.0f)));
            this.colorSetting.setFromHSB(this.cachedHue, this.cachedSat, this.cachedBri);
            this.markUnsaved();
        } else if (this.dragMode == 2) {
            this.cachedHue = this.stripFraction(frame, n) * 360.0f;
            this.colorSetting.setFromHSB(this.cachedHue, this.cachedSat, this.cachedBri);
            this.markUnsaved();
        } else if (this.dragMode == 3 && this.colorSetting.hasAlpha()) {
            this.colorSetting.setAlpha(Math.round(this.stripFraction(frame, n) * 255.0f));
            this.markUnsaved();
        }
    }

    private int swatchAt(Frame frame, int n, int n2) {
        float f = frame.bottomTop + 8.0f;
        int n3 = Math.min(frame.swatches, this.recentColors().size());
        for (int i = 0; i < n3; ++i) {
            float f2 = frame.columnLeft + 7.0f + (float)i * 20.0f;
            float f3 = (float)n - f2;
            float f4 = (float)n2 - f;
            if (!(f3 * f3 + f4 * f4 <= 81.0f)) continue;
            return i;
        }
        return -1;
    }

    private boolean overHex(Frame frame, int n, int n2) {
        return (float)n >= frame.hexLeft && (float)n <= frame.columnRight && (float)n2 >= frame.bottomTop && (float)n2 <= frame.bottomTop + 16.0f;
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        boolean bl;
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return false;
        }
        float f = this.moduleComponent.categoryComponent.getWidth();
        Frame frame = this.frame(this.moduleComponent.categoryComponent.getModuleY());
        boolean bl2 = this.expanded && this.getAnimationProgress() >= 1.0f;
        boolean bl3 = bl = bl2 && n3 == 0 && this.overHex(frame, n, n2);
        if (this.hexEditing && !bl) {
            this.unfocusTextInput();
        }
        if ((float)n > this.x && (float)n < this.x + f && (float)n2 > this.y && (float)n2 < this.y + 20.0f && (n3 == 0 || n3 == 1)) {
            float f2;
            this.animationStartProgress = f2 = this.getAnimationProgress();
            this.animationProgress = f2;
            this.expanded = !this.expanded;
            this.animationTargetProgress = this.expanded ? 1.0f : 0.0f;
            this.animating = true;
            this.animationStartNs = System.nanoTime();
            this.moduleComponent.updateSettingPositions();
            return true;
        }
        if (n3 != 0 || !bl2) {
            return false;
        }
        if (bl) {
            if (!this.hexEditing) {
                this.hexEditing = true;
                this.hexReplace = true;
                this.hexInvalid = false;
                this.hexText = RecentColors.hex(this.colorSetting.getRGB()).substring(1);
            }
            return true;
        }
        int n4 = this.swatchAt(frame, n, n2);
        if (n4 >= 0) {
            this.applyRgb(this.recentColors().get(n4));
            return true;
        }
        if ((float)n >= frame.svLeft && (float)n <= frame.svRight && (float)n2 >= frame.svTop && (float)n2 <= frame.svBottom) {
            this.beginDrag(1, n, n2);
            return true;
        }
        if ((float)n >= frame.columnLeft - 2.0f && (float)n <= frame.columnRight + 2.0f && (float)n2 >= frame.hueTop - 4.0f && (float)n2 <= frame.hueTop + 10.0f + 4.0f) {
            this.beginDrag(2, n, n2);
            return true;
        }
        if (this.colorSetting.hasAlpha() && (float)n >= frame.columnLeft - 2.0f && (float)n <= frame.columnRight + 2.0f && (float)n2 >= frame.alphaTop - 4.0f && (float)n2 <= frame.alphaTop + 10.0f + 4.0f) {
            this.beginDrag(3, n, n2);
            return true;
        }
        return false;
    }

    private void beginDrag(int n, int n2, int n3) {
        this.cacheHSB();
        this.colorAtDragStart = this.colorSetting.getColor();
        this.dragMode = n;
        this.applyPointer(n2, n3);
    }

    private void applyRgb(int n) {
        this.colorSetting.setColor(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF);
        this.cacheHSB();
        this.markUnsaved();
        this.rememberColor();
    }

    private void rememberColor() {
        if (Valen.preferences != null) {
            Valen.preferences.pushRecentColor(this.colorSetting.getRGB());
        }
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
        if (this.dragMode != 0 && this.colorSetting.getColor() != this.colorAtDragStart) {
            this.rememberColor();
        }
        this.dragMode = 0;
    }

    @Override
    public void keyTyped(char c, int n) {
        if (!this.hexEditing) {
            return;
        }
        boolean bl = GuiScreen.isCtrlKeyDown();
        if (n == 1) {
            this.hexEditing = false;
            this.hexInvalid = false;
        } else if (n == 28 || n == 156) {
            Integer n2 = RecentColors.parse(this.hexText);
            if (n2 == null) {
                this.hexInvalid = true;
            } else {
                this.hexEditing = false;
                this.hexInvalid = false;
                this.applyRgb(n2);
            }
        } else if (bl && n == 30) {
            this.hexReplace = true;
        } else if (bl && n == 46) {
            Integer n3 = RecentColors.parse(this.hexText);
            GuiScreen.setClipboardString((String)RecentColors.hex(n3 != null ? n3.intValue() : this.colorSetting.getRGB()));
        } else if (bl && n == 47) {
            Integer n4 = RecentColors.parse(GuiScreen.getClipboardString());
            if (n4 != null) {
                this.hexText = RecentColors.hex(n4).substring(1);
                this.hexReplace = false;
                this.hexInvalid = false;
            }
        } else if (n == 14 || n == 211) {
            this.hexText = this.hexReplace || n == 211 ? "" : this.hexText.substring(0, Math.max(0, this.hexText.length() - 1));
            this.hexReplace = false;
            this.hexInvalid = false;
        } else if (Character.digit(c, 16) >= 0) {
            if (this.hexReplace) {
                this.hexText = "";
            }
            if (this.hexText.length() < 6) {
                this.hexText = this.hexText + Character.toUpperCase(c);
            }
            this.hexReplace = false;
            this.hexInvalid = false;
        }
    }

    @Override
    public boolean isTextInputFocused() {
        return this.hexEditing;
    }

    @Override
    public void unfocusTextInput() {
        if (this.hexEditing) {
            Integer n = RecentColors.parse(this.hexText);
            this.hexEditing = false;
            this.hexInvalid = false;
            if (n != null && (n & 0xFFFFFF) != this.colorSetting.getRGB()) {
                this.applyRgb(n);
            }
        }
    }

    @Override
    public boolean containsClick(int n, int n2) {
        if (!(this.expanded && this.moduleComponent.isOpened && this.moduleComponent.isVisible(this))) {
            return false;
        }
        return this.overHex(this.frame(this.moduleComponent.categoryComponent.getModuleY()), n, n2);
    }

    @Override
    public void onGuiClosed() {
        this.unfocusTextInput();
        this.dragMode = 0;
        this.hoveredSwatch = -1;
        this.hexHovered = false;
        this.hexHoverAnim.snap(false);
        this.animating = false;
        this.animationStartProgress = this.animationProgress = this.expanded ? 1.0f : 0.0f;
        this.animationTargetProgress = this.animationProgress;
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
        return this.colorSetting.visible;
    }

    public void restoreExpandedState(boolean bl) {
        this.expanded = bl;
        this.animating = false;
        this.animationStartProgress = this.animationProgress = bl ? 1.0f : 0.0f;
        this.animationTargetProgress = this.animationProgress;
    }

    private void cacheHSB() {
        float f = this.colorSetting.getBrightness();
        float f2 = this.colorSetting.getSaturation();
        this.cachedBri = f;
        if (f >= 0.001f) {
            this.cachedSat = f2;
            if (f2 >= 0.001f) {
                this.cachedHue = this.colorSetting.getHue();
            }
        }
    }

    private void markUnsaved() {
        if (Valen.configManager != null) {
            Valen.configManager.invalidateDirtyCache();
        }
    }

    private static final class Frame {
        float rowLeft;
        float rowRight;
        float rowTop;
        float svLeft;
        float svTop;
        float svRight;
        float svBottom;
        float columnLeft;
        float columnRight;
        float hueTop;
        float alphaTop;
        float bottomTop;
        float hexLeft;
        int swatches;

        private Frame() {
        }
    }
}

