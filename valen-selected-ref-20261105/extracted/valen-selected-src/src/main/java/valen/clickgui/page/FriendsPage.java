/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.page;

import java.util.Collections;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.FocusableTextComponent;
import valen.clickgui.components.impl.BindComponent;
import valen.clickgui.components.impl.ButtonComponent;
import valen.clickgui.components.impl.CategoryComponent;
import valen.clickgui.components.impl.ClickGuiTextField;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.components.impl.PlayerListComponent;
import valen.clickgui.page.GuiPage;
import valen.clickgui.page.HoverSet;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Shape;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.utility.PlayerRelationsManager;
import valen.utility.RenderUtils;

public final class FriendsPage
implements GuiPage {
    private ModuleComponent settings;
    private PlayerListComponent friendList;
    private final ClickGuiTextField username = new ClickGuiTextField("Username", 16);
    private final HoverSet hovers = new HoverSet();
    private final float[] inputBox = new float[4];
    private final float[] addBox = new float[4];
    private final float[] clearBox = new float[4];
    private String feedback = "";
    private long feedbackUntil;
    private final ScrollOffsetAnimation scroll = new ScrollOffsetAnimation(190L);
    private float left;
    private float top;
    private float right;
    private float bottom;

    private void ensureSettings() {
        if (this.settings != null || ModuleManager.friends == null) {
            return;
        }
        CategoryComponent categoryComponent = new CategoryComponent(Module.category.client, Collections.emptyList());
        this.settings = new ModuleComponent(ModuleManager.friends, categoryComponent, 0.0f);
        this.settings.settings.removeIf(component -> component instanceof ButtonComponent);
        for (Component component2 : this.settings.settings) {
            if (!(component2 instanceof PlayerListComponent)) continue;
            this.friendList = (PlayerListComponent)component2;
            this.friendList.setAddInputVisible(false);
            this.friendList.expand();
        }
        this.settings.overlayHosted = true;
        this.settings.restoreOpenState(true);
    }

    @Override
    public BindComponent activeBinding() {
        if (this.settings == null) {
            return null;
        }
        for (Component component : this.settings.settings) {
            if (!(component instanceof BindComponent) || !((BindComponent)component).isBinding) continue;
            return (BindComponent)component;
        }
        return null;
    }

    @Override
    public void render(float f, float f2, float f3, float f4, int n, int n2) {
        this.ensureSettings();
        this.left = f + 7.0f;
        this.right = f3 - 7.0f;
        this.top = f2 + 54.0f;
        this.bottom = f4 - 8.0f;
        float f5 = Math.max(86.0f, PageUI.smallWidth("Remove all") + 28.0f);
        float f6 = Math.max(52.0f, PageUI.smallWidth("Add") + 28.0f);
        FriendsPage.box(this.clearBox, this.right - f5, f2 + 2.0f, this.right, f2 + 28.0f);
        FriendsPage.box(this.addBox, this.clearBox[0] - 8.0f - f6, f2 + 2.0f, this.clearBox[0] - 8.0f, f2 + 28.0f);
        FriendsPage.box(this.inputBox, this.left, f2 + 2.0f, this.addBox[0] - 8.0f, f2 + 28.0f);
        this.username.tickCursor();
        this.username.render(this.inputBox[0], this.inputBox[1], this.inputBox[2], this.inputBox[3]);
        boolean bl = PlayerRelationsManager.isValidUsername(this.username.getText());
        boolean bl2 = ModuleManager.friends != null && !ModuleManager.friends.friendList.getEntries().isEmpty();
        PageUI.button("Add", this.addBox[0], this.addBox[1], this.addBox[2], this.addBox[3], ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, this.hovers.ease("add", FriendsPage.inside(this.addBox, n, n2)), bl);
        PageUI.button("Remove all", this.clearBox[0], this.clearBox[1], this.clearBox[2], this.clearBox[3], ClickGuiStyle.SURFACE, ClickGuiStyle.TEXT_SECONDARY, this.hovers.ease("clear", FriendsPage.inside(this.clearBox, n, n2)), bl2);
        if (System.currentTimeMillis() < this.feedbackUntil) {
            PageUI.small(PageUI.truncateSmall(this.feedback, this.right - this.left - 28.0f), this.left + 14.0f, PageUI.smallY(f2 + 30.0f, this.top - 2.0f), ClickGuiStyle.TEXT_SECONDARY);
        }
        if (this.settings == null) {
            return;
        }
        float f7 = this.settings.getSettingsHeightF() - this.friendList.getHeightF();
        this.friendList.setVisibleRows((int)((this.bottom - this.top - 16.0f - f7 - 24.0f) / 20.0f));
        CategoryComponent categoryComponent = this.settings.categoryComponent;
        categoryComponent.setReferenceLayout(this.left, this.top, this.right - this.left, false);
        categoryComponent.moduleY = categoryComponent.getY();
        float f8 = this.maxScroll();
        this.scroll.clampTarget(0.0f, f8);
        float f9 = Math.max(0.0f, Math.min(f8, this.scroll.getValue()));
        this.settings.layoutSettingsAt(this.top + 8.0f - f9);
        this.settings.drawScreen(n, n2);
        float f10 = Math.min(this.bottom, this.top + this.settings.getSettingsHeightF() + 16.0f);
        PageUI.surface(this.left, this.top, this.right, f10, ClickGuiStyle.MODULE_PILL);
        RenderUtils.scissorPushGui(this.left, this.top + 8.0f, this.right - this.left, Math.max(0.0f, f10 - this.top - 16.0f));
        for (Component component : this.settings.settings) {
            if (!this.settings.isVisible(component)) continue;
            component.render();
        }
        RenderUtils.scissorPop();
        if (f9 > 0.0f) {
            Shape.scrollEdge(this.left + 2.0f, this.top + 8.0f, this.right - 2.0f, 10.0f, true, ClickGuiStyle.MODULE_PILL);
        }
        if (f9 < f8) {
            Shape.scrollEdge(this.left + 2.0f, f10 - 8.0f, this.right - 2.0f, 10.0f, false, ClickGuiStyle.MODULE_PILL);
        }
    }

    private float maxScroll() {
        return this.settings == null ? 0.0f : Math.max(0.0f, this.settings.getSettingsHeightF() + 16.0f - (this.bottom - this.top));
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (this.settings == null) {
            return false;
        }
        if (n3 == 0 && FriendsPage.inside(this.inputBox, n, n2)) {
            this.username.setFocused(true);
            BindComponent bindComponent = this.activeBinding();
            if (bindComponent != null) {
                bindComponent.isBinding = false;
            }
            return true;
        }
        if (n3 == 0 && FriendsPage.inside(this.addBox, n, n2)) {
            this.addUsername();
            return true;
        }
        this.username.setFocused(false);
        if (n3 == 0 && FriendsPage.inside(this.clearBox, n, n2)) {
            if (!ModuleManager.friends.friendList.getEntries().isEmpty()) {
                ModuleManager.friends.friendList.clearPlayers();
                this.showFeedback("Removed all friends");
            }
            return true;
        }
        if (PageUI.inside(n, n2, this.left, this.top + 8.0f, this.right, this.bottom - 8.0f)) {
            this.settings.onSettingClick(n, n2, n3);
        }
        for (Component component : this.settings.settings) {
            FocusableTextComponent focusableTextComponent;
            if (!(component instanceof FocusableTextComponent) || (focusableTextComponent = (FocusableTextComponent)((Object)component)).containsClick(n, n2)) continue;
            focusableTextComponent.unfocusTextInput();
        }
        return PageUI.inside(n, n2, this.left, this.top, this.right, this.bottom);
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
        if (this.settings != null) {
            this.settings.mouseReleased(n, n2, n3);
        }
    }

    @Override
    public boolean keyTyped(char c, int n) {
        if (this.username.isFocused()) {
            if (n == 1) {
                this.username.setFocused(false);
            } else if (n == 28 || n == 156) {
                this.addUsername();
            } else {
                this.username.textboxKeyTyped(c, n);
                this.feedbackUntil = 0L;
            }
            return true;
        }
        if (this.settings == null) {
            return false;
        }
        for (Component component : this.settings.settings) {
            if (!(component instanceof FocusableTextComponent) || !((FocusableTextComponent)((Object)component)).isTextInputFocused()) continue;
            component.keyTyped(c, n);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(int n, int n2, int n3) {
        if (this.settings == null || !PageUI.inside(n2, n3, this.left, this.top, this.right, this.bottom)) {
            return false;
        }
        for (Component component : this.settings.settings) {
            if (!(component instanceof PlayerListComponent) || !((PlayerListComponent)component).capturesCategoryScroll(n2, n3)) continue;
            component.onScroll(n);
            return true;
        }
        this.scroll.extend((float)(-n) / 120.0f * 28.0f);
        this.scroll.clampTarget(0.0f, this.maxScroll());
        return true;
    }

    @Override
    public void onClosed() {
        this.username.setFocused(false);
        this.username.setText("");
        this.feedbackUntil = 0L;
        if (this.settings != null) {
            this.settings.onGuiClosed();
        }
    }

    private void addUsername() {
        String string = this.username.getText().trim();
        if (!PlayerRelationsManager.isValidUsername(string)) {
            this.showFeedback("Use 1\u201316 letters, numbers or underscores");
            return;
        }
        boolean bl = ModuleManager.friends.friendList.addPlayer(string);
        this.showFeedback(bl ? "Added " + string : string + " is already a friend");
        this.username.setText("");
        this.username.setFocused(true);
        this.friendList.expand();
    }

    private void showFeedback(String string) {
        this.feedback = string;
        this.feedbackUntil = System.currentTimeMillis() + 3500L;
    }

    private static boolean inside(float[] fArray, int n, int n2) {
        return PageUI.inside(n, n2, fArray[0], fArray[1], fArray[2], fArray[3]);
    }

    private static void box(float[] fArray, float f, float f2, float f3, float f4) {
        fArray[0] = f;
        fArray[1] = f2;
        fArray[2] = f3;
        fArray[3] = f4;
    }
}

