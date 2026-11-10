/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.ResourceLocation
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 */
package valen.clickgui.components.impl;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.module.setting.impl.StringListSetting;
import valen.utility.RenderUtils;

public class StringListComponent
extends AbstractTextInputComponent {
    private static final String CLOSE_ICON_PATH = "/assets/valen/textures/gui/close.png";
    private static final float SELECTED_LIST_GAP = 4.0f;
    private static final int MAX_VISIBLE_SELECTED = 7;
    private static final int CLOSE_SIZE = 6;
    private static final float CLOSE_PAD = 3.0f;
    private static final float LIST_ROW_TEXT_SCALE = 0.56f;
    private static final float LIST_ROW_VISUAL_HEIGHT = 19.0f;
    private final StringListSetting setting;
    private final ScrollOffsetAnimation selectedScrollAnim = new ScrollOffsetAnimation(200L);
    private float lastMouseX;
    private float lastMouseY;

    public StringListComponent(StringListSetting stringListSetting, ModuleComponent moduleComponent, float f) {
        super(moduleComponent, f, stringListSetting.getPlaceholder(), stringListSetting.getMaxLength());
        this.setting = stringListSetting;
    }

    @Override
    public void render() {
        AbstractTextInputComponent.Layout layout = this.layout(false);
        this.renderLabel(layout, this.setting.getName());
        this.renderTextField(layout);
        this.renderSelectedEntries(layout);
    }

    @Override
    public void drawScreen(int n, int n2) {
        super.drawScreen(n, n2);
        this.lastMouseX = n;
        this.lastMouseY = n2;
        this.clampSelectedScroll();
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        if (n3 == 0 && this.isTextFieldClicked(n, n2, layout)) {
            this.setTextFieldFocused(true);
            return true;
        }
        if (n3 == 0 && this.handleSelectedEntryClick(n, n2, layout)) {
            return true;
        }
        if (this.isTextFieldFocused()) {
            this.getTextField().setText("");
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
            this.getTextField().setText("");
            this.setTextFieldFocused(false);
            return;
        }
        if (n == 28 || n == 156) {
            this.submitText();
            this.setTextFieldFocused(false);
            return;
        }
        this.getTextField().textboxKeyTyped(c, n);
    }

    @Override
    public void onScroll(int n) {
        if (!this.moduleComponent.isOpened || !this.moduleComponent.isVisible(this)) {
            return;
        }
        if (!this.capturesCategoryScroll(this.lastMouseX, this.lastMouseY)) {
            return;
        }
        float f = 25.0f * ((float)n / 120.0f);
        if (f != 0.0f) {
            this.selectedScrollAnim.extend(-f);
        }
        this.clampSelectedScroll();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.getTextField().setText("");
        this.selectedScrollAnim.reset(0.0f);
    }

    @Override
    public float getHeightF() {
        int n = this.setting.getEntries().size();
        float f = n == 0 ? 0.0f : 4.0f + (float)Math.min(7, n) * 20.0f;
        return 40.0f + f;
    }

    @Override
    public boolean isBaseVisible() {
        return this.setting.visible;
    }

    @Override
    public String getGroupName() {
        return this.setting.group != null ? this.setting.group.getName() : "";
    }

    public boolean capturesCategoryScroll(float f, float f2) {
        return this.setting.getEntries().size() > 7 && this.isMouseOverSelectedList(f, f2);
    }

    @Override
    public boolean containsClick(int n, int n2) {
        AbstractTextInputComponent.Layout layout = this.layout(true);
        return this.isTextFieldClicked(n, n2, layout) || this.isMouseOverSelectedList(n, n2);
    }

    private void submitText() {
        String string = this.getTextField().getText();
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        if (this.setting.addEntry(string)) {
            this.getTextField().setText("");
        }
        this.moduleComponent.updateSettingPositions();
        this.clampSelectedScroll();
    }

    private void renderSelectedEntries(AbstractTextInputComponent.Layout layout) {
        List<String> list = this.setting.getEntries();
        if (list.isEmpty()) {
            return;
        }
        float f = this.getSelectedTop(layout);
        float f2 = this.getSelectedVisibleHeight(list.size());
        float f3 = this.moduleComponent.categoryComponent.getModuleY() - layout.cy;
        StringListComponent.drawListWell(layout.left, f, layout.right, f + f2);
        RenderUtils.scissorPushGui(layout.left, f + f3, layout.right - layout.left, f2);
        float f4 = this.selectedScrollAnim.getValue();
        int n = (int)(f4 / 20.0f);
        int n2 = Math.min(n + 7 + 1, list.size());
        for (int i = n; i < n2; ++i) {
            String string = list.get(i);
            float f5 = f - f4 + (float)i * 20.0f;
            int n3 = ClickGuiStyle.listRow(i, false);
            this.renderEntryRow(string, layout.left, layout.right, f5, n3);
        }
        RenderUtils.scissorPop();
    }

    private void renderEntryRow(String string, float f, float f2, float f3, int n) {
        StringListComponent.drawListRow(f, f3, f2, n);
        StringListComponent.drawScaledTextNoShadow(string, f + 6.0f + 4.0f, StringListComponent.centeredScaledTextY(f3, 19.0f, 0.56f), ClickGuiStyle.TEXT_SECONDARY);
        this.renderCloseIcon(f2, f3);
    }

    private boolean handleSelectedEntryClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        List<String> list = this.setting.getEntries();
        float f = this.selectedScrollAnim.getValue();
        for (int i = 0; i < list.size(); ++i) {
            float f2 = this.getSelectedTop(layout) - f + (float)i * 20.0f;
            if (!this.isOverClose(n, n2, f2, layout.right)) continue;
            this.setting.removeEntry(list.get(i));
            this.moduleComponent.updateSettingPositions();
            this.clampSelectedScroll();
            return true;
        }
        return false;
    }

    private void clampSelectedScroll() {
        int n = this.setting.getEntries().size();
        float f = Math.max(0.0f, (float)(n - 7) * 20.0f);
        this.selectedScrollAnim.clampTarget(0.0f, f);
        if (this.selectedScrollAnim.getValue() > f) {
            this.selectedScrollAnim.reset(f);
        }
    }

    private boolean isMouseOverSelectedList(float f, float f2) {
        List<String> list = this.setting.getEntries();
        if (list.isEmpty()) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        float f3 = this.getSelectedTop(layout);
        float f4 = this.getSelectedVisibleHeight(list.size());
        return f >= layout.left && f <= layout.right && f2 >= f3 && f2 < f3 + f4;
    }

    private float getSelectedTop(AbstractTextInputComponent.Layout layout) {
        return layout.contentTop + 4.0f;
    }

    private float getSelectedVisibleHeight(int n) {
        return (float)Math.min(7, n) * 20.0f;
    }

    private void renderCloseIcon(float f, float f2) {
        ResourceLocation resourceLocation = RenderUtils.getIcon(CLOSE_ICON_PATH);
        if (resourceLocation == null) {
            return;
        }
        float f3 = f - 6.0f - 3.0f;
        float f4 = f2 + 6.5f;
        RenderUtils.drawIcon(resourceLocation, f3, f4, 6, ClickGuiStyle.HIDDEN);
    }

    private boolean isOverClose(float f, float f2, float f3, float f4) {
        float f5 = f4 - 6.0f - 3.0f;
        float f6 = f3 + 6.5f;
        return f >= f5 && f <= f5 + 6.0f && f2 >= f6 && f2 <= f6 + 6.0f;
    }

    private static void drawScaledTextNoShadow(String string, float f, float f2, int n) {
        StringListComponent.drawScaledText(string, f, f2, n, 0.56f);
    }
}

