/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 */
package valen.clickgui.components.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.gui.GuiScreen;
import valen.Valen;
import valen.clickgui.ClickGui;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.FocusableTextComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.PopupMenu;
import valen.clickgui.render.Shape;
import valen.module.ModuleManager;
import valen.module.impl.client.Gui;
import valen.module.setting.impl.SliderSetting;
import valen.utility.Utils;
import valen.utility.font.ValenFontRenderer;

public class SliderComponent
extends Component
implements FocusableTextComponent {
    public SliderSetting sliderSetting;
    private ModuleComponent moduleComponent;
    public float o;
    public float x;
    private float y;
    public boolean heldDown = false;
    public float xOffset;
    private boolean editing;
    private boolean replaceText;
    private boolean invalidText;
    private String inputText = "";
    private double targetValue;
    private double displayedValue;
    private long displayedValueFrameMs = 0L;
    private static final double SLIDER_RESPONSIVENESS = 55.0;
    private static final float ROW_HEIGHT = 20.0f;
    private static final float LABEL_FRACTION = 0.45f;
    private static final float VALUE_WIDTH = 64.0f;
    private static final float VALUE_GAP = 10.0f;
    private static final float VALUE_PADDING = 8.0f;
    private static final float TRIGGER_MIN_WIDTH = 66.0f;
    private static final float TRIGGER_PADDING = 12.0f;
    private static final float CHEVRON_SPACE = 16.0f;
    private final ToggleAnimation thumbAnim = new ToggleAnimation(120L);
    private final ToggleAnimation valueHoverAnim = new ToggleAnimation(130L);
    private final ToggleAnimation triggerHoverAnim = new ToggleAnimation(130L);

    public SliderComponent(SliderSetting sliderSetting, ModuleComponent moduleComponent, float f) {
        double d;
        this.sliderSetting = sliderSetting;
        this.moduleComponent = moduleComponent;
        this.o = f;
        this.targetValue = d = sliderSetting.getInput() == -1.0 && sliderSetting.canBeDisabled ? -1.0 : sliderSetting.getInput();
        this.displayedValue = d;
    }

    private float rowLeft() {
        return this.moduleComponent.categoryComponent.getX() + 14.0f + this.xOffset / 2.0f;
    }

    private float rowRight() {
        return this.moduleComponent.categoryComponent.getX() + this.moduleComponent.categoryComponent.getWidth() - 14.0f;
    }

    private float trackLeft() {
        return this.rowLeft() + (this.rowRight() - this.rowLeft()) * 0.45f;
    }

    private float valueLeft() {
        return this.rowRight() - 64.0f;
    }

    private float trackRight() {
        return this.valueLeft() - 10.0f;
    }

    private float triggerWidth() {
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        float f = 0.0f;
        for (String string : this.sliderSetting.getOptions()) {
            f = Math.max(f, valenFontRenderer.getScaledStringWidth(string, 0.5f));
        }
        float f2 = f + 12.0f + 16.0f;
        return Math.max(66.0f, Math.min(this.rowRight() - this.trackLeft(), f2));
    }

    private float triggerLeft() {
        return this.rowRight() - this.triggerWidth();
    }

    @Override
    public float getHeightF() {
        return 20.0f;
    }

    private float triggerTop(float f) {
        return f + 2.0f;
    }

    private int selectedIndex() {
        String[] stringArray = this.sliderSetting.getOptions();
        return Math.max(0, Math.min((int)Math.round(this.sliderSetting.getInput()), stringArray.length - 1));
    }

    @Override
    public void render() {
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiSettingFontRenderer();
        float f = this.rowLeft();
        float f2 = this.rowRight();
        float f3 = this.moduleComponent.categoryComponent.getY() + this.o;
        float f4 = PageUI.smallY(f3, f3 + 20.0f);
        float f5 = (this.sliderSetting.isString ? this.triggerLeft() : this.trackLeft()) - f - 8.0f;
        PageUI.small(PageUI.truncateSmall(this.sliderSetting.getName(), f5), f, f4, ClickGuiStyle.TEXT_SECONDARY);
        if (this.sliderSetting.isString) {
            this.renderSelector(valenFontRenderer, f3);
            return;
        }
        double d = this.heldDown ? this.displayedValue : this.sliderSetting.getInput();
        boolean bl = d == -1.0 && this.sliderSetting.canBeDisabled;
        float f6 = bl ? 0.0f : this.valueFraction(d);
        this.thumbAnim.setTarget(this.heldDown || this.isMouseOverTrack());
        Controls.slider(this.trackLeft(), this.trackRight(), f3 + 10.0f, f6, !bl, this.thumbAnim.getValue(), 1.0f);
        this.renderValue(valenFontRenderer, f3, f4);
    }

    private void renderValue(ValenFontRenderer valenFontRenderer, float f, float f2) {
        boolean bl;
        float f3 = this.valueLeft();
        float f4 = this.rowRight();
        float f5 = f + 2.0f;
        float f6 = f5 + 16.0f;
        this.valueHoverAnim.setTarget(this.editing || this.isMouseOverValue());
        float f7 = this.valueHoverAnim.getValue();
        if (f7 > 0.01f) {
            int n = this.editing ? ClickGuiStyle.INPUT_BACKGROUND_FOCUSED : ClickGuiStyle.INPUT_BACKGROUND;
            Shape.capsule(f3, f5, f4, f6, Controls.fade(n, f7));
            if (this.editing) {
                Shape.capsuleRing(f3, f5, f4, f6, 1.0f, this.invalidText ? ClickGuiStyle.ERROR : ClickGuiStyle.INPUT_OUTLINE_FOCUSED);
            }
        }
        String string = this.editing ? this.inputText : this.currentValueText();
        float f8 = 48.0f;
        while (string.length() > 1 && valenFontRenderer.getScaledStringWidth(string, 0.5f) > f8) {
            string = this.editing ? string.substring(1) : string.substring(0, string.length() - 1);
        }
        float f9 = f4 - 8.0f;
        float f10 = valenFontRenderer.getScaledStringWidth(string, 0.5f);
        boolean bl2 = bl = !this.editing && this.sliderSetting.getInput() == -1.0 && this.sliderSetting.canBeDisabled;
        int n = this.invalidText ? ClickGuiStyle.ERROR : (this.editing ? ClickGuiStyle.TEXT_PRIMARY : (bl ? ClickGuiStyle.TEXT_MUTED : ClickGuiStyle.TEXT_PRIMARY));
        valenFontRenderer.drawScaledString(string, f9 - f10, f2, 0.5f, n);
        if (this.editing && System.currentTimeMillis() / 530L % 2L == 0L) {
            Shape.capsule(f9 + 1.0f, f5 + 4.0f, f9 + 2.0f, f6 - 4.0f, ClickGuiStyle.ACCENT);
        }
    }

    private String currentValueText() {
        double d = this.sliderSetting.getInput();
        if (d == -1.0 && this.sliderSetting.canBeDisabled) {
            return "Disabled";
        }
        String string = this.sliderSetting.getSuffix();
        if (d != 1.0 && (string.equals(" second") || string.equals(" block") || string.equals(" tick"))) {
            string = string + "s";
        }
        return Utils.asWholeNum(d) + string;
    }

    private void renderSelector(ValenFontRenderer valenFontRenderer, float f) {
        String[] stringArray = this.sliderSetting.getOptions();
        float f2 = this.triggerLeft();
        float f3 = this.rowRight();
        float f4 = this.triggerTop(f);
        float f5 = f4 + 16.0f;
        PopupMenu.anchor(this, f2, f4, f3, f5);
        this.triggerHoverAnim.setTarget(PopupMenu.isOpenFor(this) || this.isMouseOverTrigger());
        int n = ClickGuiStyle.blend(ClickGuiStyle.INPUT_BACKGROUND, ClickGuiStyle.hovered(ClickGuiStyle.INPUT_BACKGROUND, 1.0f), this.triggerHoverAnim.getValue());
        Shape.capsule(f2, f4, f3, f5, n);
        float f6 = f3 - f2 - 12.0f - 16.0f;
        String string = PageUI.truncateSmall(stringArray[this.selectedIndex()], f6);
        valenFontRenderer.drawScaledString(string, f2 + 12.0f, PageUI.smallY(f4, f5), 0.5f, ClickGuiStyle.TEXT_PRIMARY);
        Controls.popUpIndicator(f3 - 8.0f - 2.0f, (f4 + f5) / 2.0f, ClickGuiStyle.TEXT_SECONDARY);
    }

    private void openMenu(float f) {
        float f2 = this.triggerTop(f);
        PopupMenu.open(this, this.sliderSetting.getOptions(), this.selectedIndex(), this.triggerLeft(), f2, this.rowRight(), f2 + 16.0f, this::selectOption);
    }

    private void selectOption(int n) {
        this.sliderSetting.setValue(n);
        this.moduleComponent.updateSettingPositions();
        this.onSliderChange();
        if (Valen.configManager != null) {
            Valen.configManager.invalidateDirtyCache();
        }
    }

    private float valueFraction(double d) {
        double d2 = this.sliderSetting.getMax() - this.sliderSetting.getMin();
        if (d2 == 0.0) {
            return 0.0f;
        }
        return (float)Math.max(0.0, Math.min(1.0, (d - this.sliderSetting.getMin()) / d2));
    }

    private float scrollOffset() {
        return this.moduleComponent.categoryComponent.getModuleY() - this.moduleComponent.categoryComponent.getY();
    }

    private boolean isMouseOverTrack() {
        float f = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        int n = ClickGui.lastLogicalMouseX;
        int n2 = ClickGui.lastLogicalMouseY;
        return this.moduleComponent.isOpened && this.moduleComponent.isVisible(this) && (float)n >= this.trackLeft() - 4.0f && (float)n <= this.trackRight() + 4.0f && (float)n2 > f && (float)n2 < f + 20.0f;
    }

    private boolean isMouseOverValue() {
        int n = ClickGui.lastLogicalMouseX;
        int n2 = ClickGui.lastLogicalMouseY;
        return this.containsClick(n, n2);
    }

    private boolean isMouseOverTrigger() {
        int n = ClickGui.lastLogicalMouseX;
        int n2 = ClickGui.lastLogicalMouseY;
        float f = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        float f2 = this.triggerTop(f);
        return this.moduleComponent.isOpened && this.moduleComponent.isVisible(this) && (float)n >= this.triggerLeft() && (float)n <= this.rowRight() && (float)n2 >= f2 && (float)n2 <= f2 + 16.0f;
    }

    @Override
    public void drawScreen(int n, int n2) {
        this.y = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        this.x = this.moduleComponent.categoryComponent.getX();
        if (this.heldDown && !this.sliderSetting.isString) {
            float f = Controls.fractionAt(this.trackLeft(), this.trackRight(), n);
            this.targetValue = f <= 0.0f && this.sliderSetting.canBeDisabled ? -1.0 : SliderComponent.roundToInterval((double)f * (this.sliderSetting.getMax() - this.sliderSetting.getMin()) + this.sliderSetting.getMin(), 4);
            long l = System.currentTimeMillis();
            float f2 = this.displayedValueFrameMs <= 0L ? 0.0f : Math.min((float)(l - this.displayedValueFrameMs) / 1000.0f, 0.05f);
            this.displayedValueFrameMs = l;
            if (this.targetValue == -1.0 || this.displayedValue == -1.0) {
                this.displayedValue = this.targetValue == -1.0 ? -1.0 : this.sliderSetting.getMin();
            }
            double d = 1.0 - Math.exp(-55.0 * (double)f2);
            if (this.targetValue != -1.0) {
                this.displayedValue += (this.targetValue - this.displayedValue) * d;
            }
            if (this.targetValue == -1.0) {
                this.sliderSetting.setValueRaw(-1.0);
            } else {
                this.sliderSetting.setValue(this.targetValue);
            }
            if (this.sliderSetting.getInput() != this.sliderSetting.getMin() && ModuleManager.hud != null && ModuleManager.hud.isEnabled() && !ModuleManager.organizedModules.isEmpty()) {
                ModuleManager.sort();
            }
        }
    }

    public void onSliderChange() {
        double d;
        this.targetValue = d = this.sliderSetting.getInput() == -1.0 && this.sliderSetting.canBeDisabled ? -1.0 : this.sliderSetting.getInput();
        this.displayedValue = d;
    }

    private static double roundToInterval(double d, int n) {
        if (n < 0) {
            return 0.0;
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(n, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return false;
        }
        if (this.sliderSetting.isString) {
            boolean bl;
            float f = this.moduleComponent.categoryComponent.getY() + this.o;
            float f2 = this.triggerTop(f) + this.scrollOffset();
            boolean bl2 = bl = (float)n >= this.triggerLeft() && (float)n <= this.rowRight() && (float)n2 >= f2 && (float)n2 <= f2 + 16.0f;
            if (bl && n3 == 0 && this.sliderSetting.getOptions().length >= 2) {
                this.openMenu(f);
                return true;
            }
            return false;
        }
        if (n3 == 0 && this.containsClick(n, n2)) {
            this.editing = true;
            this.replaceText = true;
            this.invalidText = false;
            this.inputText = Utils.asWholeNum(this.sliderSetting.getInput());
            this.heldDown = false;
            return true;
        }
        if (this.editing) {
            this.unfocusTextInput();
        }
        if (this.overRow(n, n2) && (float)n >= this.trackLeft() - 4.0f && (float)n <= this.trackRight() + 4.0f && n3 == 0) {
            this.heldDown = true;
            this.displayedValueFrameMs = 0L;
            this.displayedValue = this.sliderSetting.getInput() == -1.0 && this.sliderSetting.canBeDisabled ? -1.0 : this.sliderSetting.getInput();
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
        boolean bl = this.heldDown;
        this.heldDown = false;
        if (n3 == 0 && bl) {
            this.onSliderChange();
        }
    }

    public boolean overRow(int n, int n2) {
        return (float)n > this.rowLeft() && (float)n < this.rowRight() && (float)n2 > this.y && (float)n2 < this.y + 20.0f;
    }

    @Override
    public void onGuiClosed() {
        this.unfocusTextInput();
        this.heldDown = false;
        if (PopupMenu.isOpenFor(this)) {
            PopupMenu.closeInstantly();
        }
        this.thumbAnim.snap(false);
        this.valueHoverAnim.snap(false);
        this.triggerHoverAnim.snap(false);
    }

    @Override
    public boolean isTextInputFocused() {
        return this.editing;
    }

    @Override
    public boolean containsClick(int n, int n2) {
        return !this.sliderSetting.isString && this.moduleComponent.isOpened && this.moduleComponent.isVisible(this) && (float)n >= this.valueLeft() && (float)n <= this.rowRight() && (float)n2 >= this.y && (float)n2 <= this.y + 20.0f;
    }

    private boolean commitInput() {
        try {
            double d = Double.parseDouble(this.inputText.trim());
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new NumberFormatException();
            }
            if (d == -1.0 && this.sliderSetting.canBeDisabled) {
                this.sliderSetting.setValueRaw(-1.0);
            } else {
                this.sliderSetting.setValue(d);
            }
            this.onSliderChange();
            if (Valen.configManager != null) {
                Valen.configManager.invalidateDirtyCache();
            }
            this.invalidText = false;
            return true;
        }
        catch (NumberFormatException numberFormatException) {
            this.invalidText = true;
            return false;
        }
    }

    @Override
    public void unfocusTextInput() {
        if (this.editing) {
            this.commitInput();
        }
        this.editing = false;
        this.invalidText = false;
    }

    @Override
    public void keyTyped(char c, int n) {
        if (!this.editing) {
            return;
        }
        boolean bl = GuiScreen.isCtrlKeyDown();
        if (n == 1) {
            this.editing = false;
            this.invalidText = false;
        } else if (n == 28 || n == 156) {
            if (this.commitInput()) {
                this.editing = false;
            }
        } else if (bl && n == 30) {
            this.replaceText = true;
        } else if (bl && n == 47) {
            String string = GuiScreen.getClipboardString().trim();
            if (string.length() <= 24) {
                this.inputText = string;
                this.replaceText = false;
            }
        } else if (n == 203 || n == 208 || n == 205 || n == 200) {
            if (this.commitInput()) {
                int n2 = n == 203 || n == 208 ? -1 : 1;
                double d = this.sliderSetting.getInput();
                if (this.sliderSetting.canBeDisabled && n2 < 0 && d <= this.sliderSetting.getMin()) {
                    this.sliderSetting.setValueRaw(-1.0);
                } else {
                    this.sliderSetting.setValue(d == -1.0 && this.sliderSetting.canBeDisabled ? this.sliderSetting.getMin() : d + (double)n2 * this.sliderSetting.getInterval());
                }
                this.inputText = Utils.asWholeNum(this.sliderSetting.getInput());
                this.onSliderChange();
                this.replaceText = true;
            }
        } else if (n == 14 || n == 211) {
            this.inputText = this.replaceText || n == 211 ? "" : this.inputText.substring(0, Math.max(0, this.inputText.length() - 1));
            this.replaceText = false;
        } else if (c >= '0' && c <= '9' || c == '.' || c == '-') {
            if (this.replaceText) {
                this.inputText = "";
            }
            if (this.inputText.length() < 24) {
                this.inputText = this.inputText + c;
            }
            this.replaceText = false;
            this.invalidText = false;
        }
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
        return this.sliderSetting.visible;
    }
}

