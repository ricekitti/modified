/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 */
package valen.clickgui.page;

import java.util.Collections;
import org.lwjgl.input.Mouse;
import valen.Valen;
import valen.clickgui.ClickGui;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ScaleDrag;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.FocusableTextComponent;
import valen.clickgui.components.impl.BindComponent;
import valen.clickgui.components.impl.CategoryComponent;
import valen.clickgui.components.impl.ColorComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.components.impl.SliderComponent;
import valen.clickgui.page.GuiPage;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.Shape;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.module.impl.client.Gui;
import valen.module.impl.render.Notifications;
import valen.utility.RenderUtils;
import valen.utility.ScaledResolutionCache;
import valen.utility.config.ConfigManager;

public final class GuiSettingsPage
implements GuiPage {
    private static final float TOP_ROW_HEIGHT = 30.0f;
    private static final float CONTROL_HEIGHT = 26.0f;
    private static final float EDGE = 7.0f;
    private static final float CARD_TOP = 54.0f;
    private static final float CARD_PADDING = 8.0f;
    private static final float SCALE_ROW_HEIGHT = 20.0f;
    private static final float EDGE_FADE = 12.0f;
    private final ScaleDrag scaleDrag = new ScaleDrag();
    private ModuleComponent settings;
    private BindComponent bindComponent;
    private int lastBind;
    private float left;
    private float right;
    private float top;
    private float bottom;
    private float listTop;
    private float listBottom;
    private final ScrollOffsetAnimation scroll = new ScrollOffsetAnimation(190L);
    private float trackLeft;
    private float trackRight;
    private float trackY;
    private final float[] resetBox = new float[4];
    private final ToggleAnimation resetHover = new ToggleAnimation(130L);
    private final ToggleAnimation scaleThumb = new ToggleAnimation(120L);

    private void ensureSettings() {
        if (this.settings != null) {
            return;
        }
        Module module = ModuleManager.getModule(Gui.class);
        if (module == null) {
            return;
        }
        CategoryComponent categoryComponent = new CategoryComponent(Module.category.client, Collections.emptyList());
        this.settings = new ModuleComponent(module, categoryComponent, 0.0f);
        this.settings.settings.removeIf(component -> component instanceof SliderComponent && ((SliderComponent)component).sliderSetting == Gui.appearance.scale);
        for (Component component2 : this.settings.settings) {
            if (!(component2 instanceof BindComponent) || ((BindComponent)component2).keySetting != null) continue;
            this.bindComponent = (BindComponent)component2;
            break;
        }
        if (this.bindComponent != null) {
            this.settings.settings.remove(this.bindComponent);
            this.settings.settings.add(0, this.bindComponent);
        }
        this.lastBind = module.getKeycode();
        this.settings.overlayHosted = true;
        this.settings.restoreOpenState(true);
    }

    @Override
    public BindComponent activeBinding() {
        return this.bindComponent != null && this.bindComponent.isBinding ? this.bindComponent : null;
    }

    public void beforeFrame() {
        if (this.scaleDrag.isActive()) {
            Gui.appearance.scale.setValue(this.scaleDrag.valueAt(Mouse.getX()));
            if (!Mouse.isButtonDown((int)0)) {
                this.scaleDrag.end();
                this.commit(true);
            }
        }
    }

    public void afterFrame() {
        this.commit(false);
        if (Valen.configManager != null) {
            Valen.configManager.tickGuiAutosave();
        }
    }

    @Override
    public void render(float f, float f2, float f3, float f4, int n, int n2) {
        float f5;
        this.ensureSettings();
        this.left = f;
        this.right = f3;
        this.top = f2;
        this.bottom = f4;
        float f6 = f2 + 2.0f;
        float f7 = Math.max(64.0f, PageUI.smallWidth("Reset") + 32.0f);
        this.resetBox[0] = f3 - 7.0f - f7;
        this.resetBox[1] = f6;
        this.resetBox[2] = f3 - 7.0f;
        this.resetBox[3] = f6 + 26.0f;
        this.resetHover.setTarget(GuiSettingsPage.inside(n, n2, this.resetBox));
        PageUI.button("Reset", this.resetBox[0], this.resetBox[1], this.resetBox[2], this.resetBox[3], ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, this.resetHover.getValue(), true);
        ConfigManager configManager = Valen.configManager;
        if (configManager != null && configManager.guiAutosaveFailed()) {
            PageUI.small("Save failed", f + 7.0f + 14.0f, PageUI.smallY(f6, f6 + 26.0f), ClickGuiStyle.WARNING);
        }
        float f8 = f + 7.0f;
        float f9 = f3 - 7.0f;
        float f10 = f2 + 54.0f;
        float f11 = f10 + 8.0f;
        this.listTop = f11 + 20.0f;
        float f12 = this.settings == null ? 0.0f : this.settings.getSettingsHeightF();
        float f13 = Math.min(f4, this.listTop + f12 + 8.0f);
        this.listBottom = f13 - 8.0f;
        PageUI.surface(f8, f10, f9, f13, ClickGuiStyle.MODULE_PILL);
        float f14 = f8 + 14.0f;
        float f15 = f9 - 14.0f;
        float f16 = PageUI.smallY(f11, f11 + 20.0f);
        PageUI.small("GUI scale", f14, f16, ClickGuiStyle.TEXT_SECONDARY);
        String string = Math.round(Gui.appearance.scalePercent()) + "%";
        PageUI.small(string, f15 - 8.0f - PageUI.smallWidth(string), f16, ClickGuiStyle.TEXT_PRIMARY);
        this.trackLeft = f14 + (f15 - f14) * 0.45f;
        this.trackRight = f15 - 74.0f;
        this.trackY = f11 + 10.0f;
        float f17 = (float)((Gui.appearance.scalePercent() - 50.0) / 50.0);
        this.scaleThumb.setTarget(this.scaleDrag.isActive() || this.overScaleTrack(n, n2));
        Controls.slider(this.trackLeft, this.trackRight, this.trackY, f17, true, this.scaleThumb.getValue(), 1.0f);
        if (this.settings == null) {
            return;
        }
        CategoryComponent categoryComponent = this.settings.categoryComponent;
        categoryComponent.setReferenceLayout(f8, this.listTop, f9 - f8, false);
        categoryComponent.moduleY = categoryComponent.getY();
        float f18 = this.maxScroll();
        this.scroll.clampTarget(0.0f, f18);
        float f19 = Math.max(0.0f, Math.min(f18, this.scroll.getValue()));
        this.settings.layoutSettingsAt(this.listTop - f19);
        if (!Mouse.isButtonDown((int)0)) {
            this.settings.mouseReleased(n, n2, 0);
        }
        this.settings.drawScreen(n, n2);
        RenderUtils.scissorPushGui(f + 2.0f, this.listTop, f3 - f - 4.0f, this.listBottom - this.listTop);
        for (Component component : this.settings.settings) {
            f5 = categoryComponent.getY() + component.getOffset();
            float f20 = component instanceof ColorComponent ? ((ColorComponent)component).getExpandedHeight() : component.getHeightF();
            float f21 = f20;
            if (!(f5 + f21 >= this.listTop) || !(f5 <= this.listBottom)) continue;
            component.render();
        }
        RenderUtils.scissorPop();
        int n3 = ClickGuiStyle.MODULE_PILL;
        float f22 = Math.min(1.0f, f19 / 12.0f);
        if (f22 > 0.01f) {
            Shape.scrollEdge(f8 + 2.0f, this.listTop, f9 - 2.0f, 12.0f, true, ClickGuiStyle.withAlpha(n3, Math.round(255.0f * f22)));
            RenderUtils.drawRect(f14, this.listTop - 0.5f, f15, this.listTop, ClickGuiStyle.withAlpha(ClickGuiStyle.SURFACE_HOVER, Math.round(153.0f * f22)));
        }
        if ((f5 = Math.min(1.0f, (f18 - f19) / 12.0f)) > 0.01f) {
            Shape.scrollEdge(f8 + 2.0f, this.listBottom, f9 - 2.0f, 12.0f, false, ClickGuiStyle.withAlpha(n3, Math.round(255.0f * f5)));
        }
    }

    private float maxScroll() {
        return this.settings == null ? 0.0f : Math.max(0.0f, this.settings.getSettingsHeightF() - (this.listBottom - this.listTop));
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (n3 == 0 && GuiSettingsPage.inside(n, n2, this.resetBox)) {
            this.scaleDrag.end();
            if (this.settings != null) {
                this.settings.onGuiClosed();
            }
            this.settings = null;
            this.scroll.reset(0.0f);
            Gui.appearance.reset();
            this.commit(true);
            return true;
        }
        if (n3 == 0 && this.overScaleTrack(n, n2)) {
            this.unfocusTextInputs(Integer.MIN_VALUE, Integer.MIN_VALUE);
            double d = Controls.fractionAt(this.trackLeft, this.trackRight, n);
            Gui.appearance.scale.setValue(50.0 + d * 50.0);
            this.scaleDrag.begin(Mouse.getX(), Gui.appearance.scalePercent(), (double)Controls.travel(this.trackLeft, this.trackRight) * ClickGui.getActiveRenderScale() * (double)ScaledResolutionCache.get().getScaleFactor());
            this.commit(false);
            return true;
        }
        if (this.settings != null && PageUI.inside(n, n2, this.left, this.listTop, this.right, this.listBottom)) {
            this.settings.onSettingClick(n, n2, n3);
            this.unfocusTextInputs(n, n2);
            this.commit(false);
            return true;
        }
        this.unfocusTextInputs(Integer.MIN_VALUE, Integer.MIN_VALUE);
        return PageUI.inside(n, n2, this.left, this.top, this.right, this.bottom);
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
        if (n3 == 0) {
            if (this.scaleDrag.isActive()) {
                Gui.appearance.scale.setValue(this.scaleDrag.valueAt(Mouse.getX()));
            }
            this.scaleDrag.end();
        }
        if (this.settings != null) {
            this.settings.drawScreen(n, n2);
            this.settings.mouseReleased(n, n2, n3);
        }
        this.commit(true);
    }

    @Override
    public boolean keyTyped(char c, int n) {
        if (n == 1 && this.scaleDrag.isActive()) {
            this.scaleDrag.end();
            this.commit(true);
            return true;
        }
        if (this.settings != null && this.focusedTextInput() != null) {
            this.settings.keyTyped(c, n);
            this.commit(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(int n, int n2, int n3) {
        if (this.settings != null && PageUI.inside(n2, n3, this.left, this.listTop, this.right, this.listBottom)) {
            this.scroll.extend((float)(-n) / 120.0f * 28.0f);
            this.scroll.clampTarget(0.0f, this.maxScroll());
        }
        return PageUI.inside(n2, n3, this.left, this.top, this.right, this.bottom);
    }

    private boolean overScaleTrack(int n, int n2) {
        return (float)n >= this.trackLeft - 5.0f && (float)n <= this.trackRight + 5.0f && (float)n2 >= this.trackY - 10.0f && (float)n2 <= this.trackY + 10.0f;
    }

    private FocusableTextComponent focusedTextInput() {
        if (this.settings == null) {
            return null;
        }
        for (Component component : this.settings.settings) {
            if (!(component instanceof FocusableTextComponent) || !((FocusableTextComponent)((Object)component)).isTextInputFocused()) continue;
            return (FocusableTextComponent)((Object)component);
        }
        return null;
    }

    private void unfocusTextInputs(int n, int n2) {
        if (this.settings == null) {
            return;
        }
        for (Component component : this.settings.settings) {
            FocusableTextComponent focusableTextComponent;
            if (!(component instanceof FocusableTextComponent) || !(focusableTextComponent = (FocusableTextComponent)((Object)component)).isTextInputFocused() || focusableTextComponent.containsClick(n, n2)) continue;
            focusableTextComponent.unfocusTextInput();
        }
    }

    @Override
    public void onClosed() {
        this.scaleDrag.end();
        if (this.settings != null) {
            this.settings.onGuiClosed();
        }
        this.commit(true);
    }

    public void profileLoaded() {
        this.scroll.reset(0.0f);
        this.scaleDrag.end();
        if (this.settings != null) {
            this.settings.onGuiClosed();
        }
        this.settings = null;
        this.bindComponent = null;
    }

    private void commit(boolean bl) {
        boolean bl2 = Gui.appearance.applyIfChanged();
        if (this.bindComponent != null && this.lastBind != this.bindComponent.moduleComponent.mod.getKeycode()) {
            this.lastBind = this.bindComponent.moduleComponent.mod.getKeycode();
            bl2 = true;
        }
        if (bl2 && Valen.configManager != null) {
            Valen.configManager.queueGuiAutosave();
        }
        if (bl && Valen.configManager != null) {
            boolean bl3 = Valen.configManager.guiAutosaveFailed();
            if (!Valen.configManager.flushGuiAutosave() && !bl3) {
                Notifications.pushMessage("Could not save GUI settings");
            }
        }
    }

    private static boolean inside(int n, int n2, float[] fArray) {
        return PageUI.inside(n, n2, fArray[0], fArray[1], fArray[2], fArray[3]);
    }
}

