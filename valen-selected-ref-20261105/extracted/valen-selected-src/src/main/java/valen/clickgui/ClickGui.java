/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.GuiTextField
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.util.ChatAllowedCharacters
 *  net.minecraft.util.MathHelper
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 *  valen.utility.CommandHandler
 */
package valen.clickgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.GuiTransform;
import valen.clickgui.animation.OpeningAnimation;
import valen.clickgui.animation.RevealAnimation;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.FocusableTextComponent;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.BindComponent;
import valen.clickgui.components.impl.CategoryComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.components.impl.PlayerListComponent;
import valen.clickgui.components.impl.StringListComponent;
import valen.clickgui.page.ConfigsPage;
import valen.clickgui.page.FriendsPage;
import valen.clickgui.page.GuiPage;
import valen.clickgui.page.GuiSettingsPage;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.PopupMenu;
import valen.clickgui.render.SceneSnapshot;
import valen.clickgui.render.Shape;
import valen.lunar.forge.client.event.GuiScreenEvent;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.fml.client.config.GuiButtonExt;
import valen.lunar.render.LunarPostProcessing;
import valen.module.Module;
import valen.module.impl.client.CommandLine;
import valen.module.impl.client.Gui;
import valen.module.impl.render.BlockESP;
import valen.module.impl.render.Notifications;
import valen.utility.CommandHandler;
import valen.utility.RenderUtils;
import valen.utility.Timer;
import valen.utility.font.ValenFontRenderer;

public class ClickGui
extends GuiScreen {
    private static final String[] REFERENCE_TAB_LABELS = new String[]{"Combat", "Movement", "Utility", "Render", "Ambiance", "Friends", "GUI", "Profiles"};
    private static final Module.category[][] REFERENCE_TAB_CATEGORIES = new Module.category[][]{{Module.category.combat}, {Module.category.movement}, {Module.category.player, Module.category.world, Module.category.minigames, Module.category.fun, Module.category.other}, {Module.category.render}, {Module.category.ambiance}, null, null, null};
    private static final String[] REFERENCE_TAB_ICONS = new String[]{"/assets/valen/textures/gui/icons/combat.png", "/assets/valen/textures/gui/icons/movement.png", "/assets/valen/textures/gui/icons/utility.png", "/assets/valen/textures/gui/icons/render.png", "/assets/valen/textures/gui/icons/ambiance.png", "/assets/valen/textures/gui/icons/friends.png", "/assets/valen/textures/gui/icons/appearance.png", "/assets/valen/textures/gui/icons/configs.png"};
    private static final float DESIGN_WIDTH = 720.0f;
    private static final float DESIGN_HEIGHT = 428.0f;
    private static final float TOP_ROW_HEIGHT = 30.0f;
    private static final float SEARCH_GAP = 14.0f;
    private static final float CONTROL_SIZE = 26.0f;
    private static final float CONTROL_GAP = 8.0f;
    private static final int CONTROL_ICON_SIZE = 14;
    private static final float SEARCH_OPEN_WIDTH = 220.0f;
    private static final float SEARCH_ICON_INSET = 8.0f;
    private static final float SEARCH_TEXT_GAP = 8.0f;
    private static final String ICON_SEARCH = "/assets/valen/textures/gui/icons/search.png";
    private static final String ICON_SAVE = "/assets/valen/textures/gui/icons/save.png";
    private static final String ICON_CHECK = "/assets/valen/textures/gui/icons/check.png";
    private static final long SAVE_FLASH_MS = 1400L;
    private static final int SEARCH_MAX_LENGTH = 32;
    private static final int HIDDEN_POINTER = -100000;
    private ScheduledFuture sf;
    private Timer logoSmoothWidth;
    private Timer logoSmoothLength;
    private ScaledResolution sr;
    private GuiButtonExt commandLineSend;
    private GuiTextField commandLineInput;
    public static ArrayList<CategoryComponent> categories;
    private int actualScreenWidth;
    private int actualScreenHeight;
    private int designViewportWidth;
    private int designViewportHeight;
    private double previousScale;
    private static boolean isNotFirstOpen;
    private boolean pendingScaleRefresh;
    private boolean layoutReady;
    private int selectedReferenceTab;
    private final float[] referenceTabLeft = new float[REFERENCE_TAB_LABELS.length];
    private final float[] referenceTabRight = new float[REFERENCE_TAB_LABELS.length];
    private final float[] referenceTabTop = new float[REFERENCE_TAB_LABELS.length];
    private final float[] referenceTabBottom = new float[REFERENCE_TAB_LABELS.length];
    private final ToggleAnimation[] tabHoverAnim = new ToggleAnimation[REFERENCE_TAB_LABELS.length];
    private final ToggleAnimation[] tabSelectAnim = new ToggleAnimation[REFERENCE_TAB_LABELS.length];
    private final List<CategoryComponent> referencePanels = new ArrayList<CategoryComponent>();
    private final GuiPage[] referenceTabPages = new GuiPage[REFERENCE_TAB_LABELS.length];
    private String referencePanelsKey;
    private Float pendingReferenceScrollOffset;
    private ModuleComponent settingsOverlayModule;
    private final RevealAnimation settingsOverlayAnim = new RevealAnimation(200.0f);
    private String closingOverlayTitle;
    private final ScrollOffsetAnimation settingsScrollAnim = new ScrollOffsetAnimation(190L);
    private final ToggleAnimation closeHoverAnim = new ToggleAnimation(130L);
    private final ToggleAnimation resetHoverAnim = new ToggleAnimation(130L);
    private final ToggleAnimation favoriteHoverAnim = new ToggleAnimation(130L);
    private float settingsCloseLeft;
    private float settingsCloseTop;
    private float settingsCloseRight;
    private float settingsCloseBottom;
    private float settingsResetLeft;
    private float settingsResetTop;
    private float settingsResetRight;
    private float settingsResetBottom;
    private float settingsFavoriteLeft;
    private float settingsFavoriteTop;
    private float settingsFavoriteRight;
    private float settingsFavoriteBottom;
    private float settingsListTop;
    private float settingsListBottom;
    private float settingsContentHeight;
    private String searchQuery = "";
    private boolean searchFocused;
    private float searchBoxLeft;
    private float searchBoxTop;
    private float searchBoxRight;
    private float searchBoxBottom;
    private float saveButtonLeft;
    private float saveButtonTop;
    private float saveButtonRight;
    private float saveButtonBottom;
    private final GuiTransform presentationTransform = new GuiTransform();
    private final OpeningAnimation openingAnimation = new OpeningAnimation();
    private final SceneSnapshot openingScene = new SceneSnapshot();
    private final ToggleAnimation searchPressAnim = new ToggleAnimation(90L);
    private final ToggleAnimation savePressAnim = new ToggleAnimation(90L);
    private int searchPressedButton = -1;
    private boolean savePressed;
    private final ToggleAnimation searchOpenAnim = new ToggleAnimation(300L);
    private final ToggleAnimation searchHoverAnim = new ToggleAnimation(140L);
    private final ToggleAnimation saveHoverAnim = new ToggleAnimation(140L);
    private long saveFlashUntilMs;
    private long saveFailureUntilMs;
    private float contentAreaLeft;
    private float contentAreaTop;
    private float contentAreaRight;
    private float contentAreaBottom;
    public static int lastLogicalMouseX;
    public static int lastLogicalMouseY;
    private static final long animEpochNs;
    private int tabPillTrackedTab = -1;
    private float tabPillAnimStartMs;
    private float tabPillFromTop;
    private float tabPillFromBottom;
    private float tabPillToTop;
    private float tabPillToBottom;
    private static final float OVERLAY_HEADER_HEIGHT = 44.0f;
    private static final float SETTINGS_TOP_INSET = 4.0f;
    private static final float SETTINGS_EDGE_FADE = 12.0f;
    private static final float CLOSE_DIAMETER = 22.0f;
    private static final float HEADER_BUTTON_HEIGHT = 20.0f;
    private static final float TAB_HEIGHT = 26.0f;
    private static final float TAB_GAP_V = 5.0f;
    private static final float SIDEBAR_HEADER_BLOCK = 62.0f;
    private static final int SIDEBAR_BOTTOM_TABS = 3;

    private static float animClockMs() {
        return (float)(System.nanoTime() - animEpochNs) / 1000000.0f;
    }

    public ClickGui() {
        int n;
        for (n = 0; n < REFERENCE_TAB_CATEGORIES.length; ++n) {
            if (REFERENCE_TAB_CATEGORIES[n] != null) continue;
            if ("Profiles".equals(REFERENCE_TAB_LABELS[n])) {
                this.referenceTabPages[n] = new ConfigsPage();
                continue;
            }
            if ("GUI".equals(REFERENCE_TAB_LABELS[n])) {
                this.referenceTabPages[n] = new GuiSettingsPage();
                continue;
            }
            if (!"Friends".equals(REFERENCE_TAB_LABELS[n])) continue;
            this.referenceTabPages[n] = new FriendsPage();
        }
        for (n = 0; n < REFERENCE_TAB_LABELS.length; ++n) {
            this.tabHoverAnim[n] = new ToggleAnimation(130L);
            this.tabSelectAnim[n] = new ToggleAnimation(220L);
        }
        categories = new ArrayList();
        n = 5;
        for (Module.category category2 : Module.category.values()) {
            if (!this.shouldCreateCategory(category2)) continue;
            CategoryComponent categoryComponent = new CategoryComponent(category2);
            categoryComponent.setY(n, false);
            categories.add(categoryComponent);
            n += 20;
        }
    }

    private boolean shouldCreateCategory(Module.category category2) {
        return Valen.moduleManager == null || !Valen.moduleManager.inCategory(category2).isEmpty();
    }

    public void initMain() {
        this.logoSmoothWidth = new Timer(500.0f);
        this.logoSmoothWidth.start();
        this.sf = Valen.getScheduledExecutor().schedule(() -> {
            this.logoSmoothLength = new Timer(650.0f);
            this.logoSmoothLength.start();
        }, 650L, TimeUnit.MILLISECONDS);
    }

    public void initGui() {
        super.initGui();
        double d = this.getRenderScale();
        if (!isNotFirstOpen) {
            isNotFirstOpen = true;
            this.previousScale = d;
        }
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.setScreenSize(this.designViewportWidth, this.designViewportHeight);
        }
        if (Double.compare(this.previousScale, d) != 0) {
            for (CategoryComponent categoryComponent : categories) {
                categoryComponent.limitPositions();
            }
        }
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.reloadModules();
        }
        this.commandLineInput = new GuiTextField(1, this.mc.fontRendererObj, 22, this.designViewportHeight - 100, 150, 20);
        this.commandLineInput.setMaxStringLength(256);
        this.commandLineSend = new GuiButtonExt(2, 22, this.designViewportHeight - 70, 150, 20, "Send");
        this.buttonList.add(this.commandLineSend);
        this.commandLineSend.visible = CommandLine.opened;
        this.previousScale = d;
    }

    private List<CategoryComponent> getCategoriesInRenderOrder() {
        ArrayList<CategoryComponent> arrayList = new ArrayList<CategoryComponent>(categories);
        arrayList.sort(Comparator.comparingLong(categoryComponent -> categoryComponent.lastInteractedTime));
        return arrayList;
    }

    private CategoryComponent getTopmostUnderCursor(List<CategoryComponent> list, int n, int n2) {
        for (int i = list.size() - 1; i >= 0; --i) {
            if (!list.get(i).overRect(n, n2)) continue;
            return list.get(i);
        }
        return null;
    }

    private boolean isReferenceLayout() {
        return true;
    }

    private void renderReferenceLayout(int n, int n2) {
        int n3;
        float f;
        float f2;
        float f3;
        float f4;
        GuiPage guiPage = this.getSelectedPage();
        boolean bl = guiPage != null && guiPage.blocksBackgroundInput();
        float f5 = 720.0f;
        float f6 = 428.0f;
        float f7 = this.presentationTransform.left();
        float f8 = this.presentationTransform.top();
        float f9 = 14.0f;
        float f10 = ClickGui.animClockMs();
        float f11 = 14.0f;
        float f12 = f8 + f11;
        float f13 = f6 - f11 * 2.0f;
        float f14 = f7 + f5;
        float f15 = f12 + f13;
        LunarPostProcessing.prepareBlur();
        Shape.fill(f7, f12, f14, f15, f9, ClickGuiStyle.PANEL_BACKGROUND);
        LunarPostProcessing.blurEnd(2, 3.5f);
        Shape.verticalGradient(f7, f12, f14, f15, f9, ClickGuiStyle.WINDOW_GLASS_TOP, ClickGuiStyle.WINDOW_GLASS_BOTTOM);
        this.drawGlassHighlights(f7, f12, f5, f13, f9);
        float f16 = 2.0f;
        float f17 = f7 + f16;
        float f18 = f12 + f16;
        float f19 = 150.0f;
        float f20 = f13 - f16 * 2.0f;
        float f21 = f17 + f19;
        float f22 = f18 + f20;
        Shape.shadow(f17, f18 + 0.5f, f21, f22 + 0.5f, 12.0f, f16, ClickGuiStyle.SHADOW);
        Shape.surface(f17, f18, f21, f22, ClickGuiStyle.SIDEBAR_BACKGROUND);
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiHeaderFontRenderer();
        this.renderBrand(f17, f18);
        this.layoutSidebarTabs(f17, f21, f18, f20);
        this.renderSidebarTabPill(f10, f18);
        for (int i = 0; i < REFERENCE_TAB_LABELS.length; ++i) {
            int n4;
            f4 = this.referenceTabBottom[i] - this.referenceTabTop[i];
            boolean bl2 = i == this.selectedReferenceTab;
            boolean bl3 = !bl && (float)n >= this.referenceTabLeft[i] && (float)n <= this.referenceTabRight[i] && (float)n2 >= this.referenceTabTop[i] && (float)n2 <= this.referenceTabBottom[i];
            this.tabHoverAnim[i].setTarget(!bl2 && bl3);
            this.tabSelectAnim[i].setTarget(bl2);
            float f23 = this.tabHoverAnim[i].getValue();
            f3 = this.tabSelectAnim[i].getValue();
            if (f23 > 0.001f) {
                n4 = ClickGuiStyle.hovered(ClickGuiStyle.SIDEBAR_BACKGROUND, 1.0f);
                Shape.capsule(this.referenceTabLeft[i], this.referenceTabTop[i], this.referenceTabRight[i], this.referenceTabBottom[i], ClickGuiStyle.withAlpha(n4, Math.round(255.0f * f23)));
            }
            n4 = ClickGuiStyle.blend(ClickGuiStyle.TEXT_SECONDARY, ClickGuiStyle.TEXT_PRIMARY, f3);
            int n5 = 13;
            f2 = this.referenceTabLeft[i] + 11.0f;
            f = this.referenceTabTop[i] + (f4 - (float)n5) / 2.0f;
            RenderUtils.drawIcon(RenderUtils.getIcon(REFERENCE_TAB_ICONS[i]), f2, f, n5, n4);
            valenFontRenderer.drawString(REFERENCE_TAB_LABELS[i], f2 + (float)n5 + 8.0f, PageUI.textY(valenFontRenderer, 1.0f, this.referenceTabTop[i], this.referenceTabBottom[i]), n4, false);
        }
        float f24 = f21 + 26.0f;
        f4 = f12 + 18.0f;
        float f25 = f7 + f5 - f24 - 22.0f;
        float f26 = f13 - 36.0f;
        this.contentAreaLeft = f24;
        this.contentAreaTop = f4;
        this.contentAreaRight = f24 + f25;
        this.contentAreaBottom = f4 + f26;
        int n4 = guiPage instanceof GuiSettingsPage ? ClickGuiStyle.GUI_PANEL_BACKGROUND : (n3 = guiPage instanceof ConfigsPage ? ClickGuiStyle.PROFILES_PANEL_BACKGROUND : ClickGuiStyle.MODULE_PANEL_BACKGROUND);
        if (n3 >>> 24 != 0) {
            Shape.surface(f24, f4, this.contentAreaRight, this.contentAreaBottom, n3);
        }
        if (guiPage != null) {
            guiPage.render(f24, f4, f24 + f25, f4 + f26, n, n2);
            if (bl) {
                Shape.fill(f7, f12, f14, f15, f9, ClickGuiStyle.withAlpha(0, Math.round(120.0f * guiPage.modalOpacity())));
                guiPage.renderModal(f24, f4, f24 + f25, f4 + f26, n, n2);
            }
            return;
        }
        f3 = f24 + 7.0f;
        float f27 = f24 + f25 - 7.0f;
        float f28 = f4 + 2.0f;
        this.saveButtonRight = f27;
        this.saveButtonLeft = f27 - 26.0f;
        this.saveButtonTop = f28;
        this.saveButtonBottom = f28 + 26.0f;
        this.searchOpenAnim.setTarget(this.searchFocused || this.isSearchActive());
        f2 = this.searchOpenAnim.getValue();
        f = Math.max(26.0f, Math.min(220.0f, this.saveButtonLeft - 8.0f - f3));
        this.searchBoxRight = this.saveButtonLeft - 8.0f;
        this.searchBoxLeft = this.searchBoxRight - (26.0f + (f - 26.0f) * f2);
        this.searchBoxTop = f28;
        this.searchBoxBottom = f28 + 26.0f;
        this.drawSearchControl(n, n2, f2);
        this.drawSaveConfigButton(n, n2);
        float f29 = f4 + 30.0f + 14.0f;
        float f30 = f26 - 30.0f - 14.0f;
        List<CategoryComponent> list = this.getReferenceColumnPanels();
        if (list.isEmpty()) {
            if (this.isSearchActive()) {
                String string = "No modules match \"" + this.searchQuery.trim() + "\"";
                valenFontRenderer.drawString(string, f24 + (f25 - (float)valenFontRenderer.getStringWidth(string)) / 2.0f, f29 + 16.0f, ClickGuiStyle.TEXT_MUTED, false);
            }
            this.drawTopControlTooltip(n, n2);
            return;
        }
        CategoryComponent categoryComponent = this.getTopmostUnderCursor(list, n, n2);
        for (CategoryComponent categoryComponent2 : list) {
            categoryComponent2.setScreenSize(this.width, this.height);
            categoryComponent2.setReferenceLayout(f24, f29, f25, false);
            categoryComponent2.setMaxContentHeight(f30);
            if (this.pendingReferenceScrollOffset != null) {
                categoryComponent2.applyScrollTargetOffset(this.pendingReferenceScrollOffset.floatValue());
            }
            categoryComponent2.render(this.fontRendererObj);
            categoryComponent2.mousePosition(n, n2, categoryComponent2 == categoryComponent);
            for (Component component : categoryComponent2.getModules()) {
                if (component == this.settingsOverlayModule) continue;
                component.drawScreen(n, n2);
            }
        }
        this.pendingReferenceScrollOffset = null;
        this.renderSettingsOverlay(f24, f4, f25, f26, n, n2);
        this.drawTopControlTooltip(n, n2);
    }

    private void drawGlassHighlights(float f, float f2, float f3, float f4, float f5) {
        Shape.gradient(f, f2, f + f3, f2 + f4, f5, 0xFFFFFF, 0x8FFFFFF, 0xFFFFFF, 0xFFFFFF);
        float f6 = f + f5 + 3.0f;
        float f7 = f + f3 - f5 - 3.0f;
        float f8 = f6 + (f7 - f6) * 0.68f;
        int n = 0xFFFFFF;
        int n2 = 0x1CFFFFFF;
        RenderUtils.drawHorizontalGradientRect(f6, f2 + 0.5f, f8, f2 + 1.25f, n, n2);
        RenderUtils.drawHorizontalGradientRect(f8, f2 + 0.5f, f7, f2 + 1.25f, n2, n);
    }

    private void renderSettingsOverlay(float f, float f2, float f3, float f4, int n, int n2) {
        float f5;
        this.settingsOverlayAnim.update();
        if (this.settingsOverlayModule == null && !this.settingsOverlayAnim.isAnimating()) {
            return;
        }
        float f6 = this.settingsOverlayAnim.getValue();
        ModuleComponent moduleComponent = this.settingsOverlayModule;
        float f7 = f + f3;
        Shape.surface(f, f2, f7, f2 + f4, ClickGuiStyle.withAlpha(ClickGuiStyle.PANEL_BACKGROUND, Math.round(190.0f * f6)));
        float f8 = 10.0f * (1.0f - f6);
        float f9 = f2 + f8;
        float f10 = f4 - f8;
        float f11 = f9 + f10;
        int n3 = Math.max(1, Math.round(255.0f * f6));
        Shape.surface(f, f9, f7, f11, ClickGuiStyle.withAlpha(ClickGuiStyle.SETTINGS_PANEL_BACKGROUND, Math.round((float)(ClickGuiStyle.SETTINGS_PANEL_BACKGROUND >>> 24) * f6)));
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiHeaderFontRenderer();
        valenFontRenderer.drawString(moduleComponent != null ? moduleComponent.mod.getName() : this.closingOverlayTitle, f + 18.0f, PageUI.textY(valenFontRenderer, 1.0f, f9, f9 + 44.0f), ClickGuiStyle.withAlpha(ClickGuiStyle.TEXT_PRIMARY, n3), false);
        if (moduleComponent == null) {
            return;
        }
        float f12 = 24.0f;
        this.settingsCloseRight = f7 - 12.0f;
        this.settingsCloseLeft = this.settingsCloseRight - f12;
        this.settingsCloseTop = f9 + (44.0f - f12) / 2.0f;
        this.settingsCloseBottom = this.settingsCloseTop + f12;
        boolean bl = (float)n >= this.settingsCloseLeft && (float)n <= this.settingsCloseRight && (float)n2 >= this.settingsCloseTop && (float)n2 <= this.settingsCloseBottom;
        this.closeHoverAnim.setTarget(bl);
        Controls.closeButton(this.settingsCloseLeft + f12 / 2.0f, this.settingsCloseTop + f12 / 2.0f, 22.0f, this.closeHoverAnim.getValue(), f6);
        String string = Valen.configManager != null && Valen.configManager.canUndoReset(this.settingsOverlayModule.mod) ? "Undo reset" : "Reset";
        float f13 = f9 + 12.0f;
        float f14 = PageUI.smallWidth(string) + 20.0f;
        this.settingsResetRight = this.settingsCloseLeft - 6.0f;
        this.settingsResetLeft = this.settingsResetRight - f14;
        this.settingsResetTop = f13;
        this.settingsResetBottom = f13 + 20.0f;
        boolean bl2 = (float)n >= this.settingsResetLeft && (float)n <= this.settingsResetRight && (float)n2 >= this.settingsResetTop && (float)n2 <= this.settingsResetBottom;
        this.resetHoverAnim.setTarget(bl2);
        Controls.button(string, this.settingsResetLeft, this.settingsResetTop, this.settingsResetRight, this.settingsResetBottom, ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, this.resetHoverAnim.getValue(), f6);
        boolean bl3 = Valen.preferences != null && Valen.preferences.isFavorite(moduleComponent.mod);
        String string2 = bl3 ? "Unfavorite" : "Favorite";
        float f15 = PageUI.smallWidth(string2) + 20.0f;
        this.settingsFavoriteRight = this.settingsResetLeft - 6.0f;
        this.settingsFavoriteLeft = this.settingsFavoriteRight - f15;
        this.settingsFavoriteTop = this.settingsResetTop;
        this.settingsFavoriteBottom = this.settingsResetBottom;
        boolean bl4 = (float)n >= this.settingsFavoriteLeft && (float)n <= this.settingsFavoriteRight && (float)n2 >= this.settingsFavoriteTop && (float)n2 <= this.settingsFavoriteBottom;
        this.favoriteHoverAnim.setTarget(bl4);
        Controls.button(string2, this.settingsFavoriteLeft, this.settingsFavoriteTop, this.settingsFavoriteRight, this.settingsFavoriteBottom, bl3 ? ClickGuiStyle.ACCENT_SOFT : ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, this.favoriteHoverAnim.getValue(), f6);
        this.settingsListTop = f9 + 44.0f + 6.0f;
        this.settingsListBottom = f11 - 10.0f;
        float f16 = Math.max(0.0f, this.settingsListBottom - this.settingsListTop);
        this.settingsContentHeight = moduleComponent.getSettingsHeightF() + 4.0f;
        float f17 = Math.max(0.0f, this.settingsContentHeight - f16);
        this.settingsScrollAnim.clampTarget(0.0f, f17);
        float f18 = Math.max(0.0f, Math.min(f17, this.settingsScrollAnim.getValue()));
        moduleComponent.layoutSettingsAt(this.settingsListTop + 4.0f - f18);
        CategoryComponent categoryComponent = moduleComponent.categoryComponent;
        float f19 = categoryComponent.moduleY;
        categoryComponent.moduleY = categoryComponent.getY();
        moduleComponent.drawScreen(n, n2);
        GL11.glEnable((int)3089);
        RenderUtils.scissor(f, this.settingsListTop, f3, f16);
        moduleComponent.renderSettingsOnly();
        GL11.glDisable((int)3089);
        categoryComponent.moduleY = f19;
        int n4 = ClickGuiStyle.SETTINGS_PANEL_BACKGROUND;
        float f20 = Math.min(1.0f, f18 / 12.0f);
        if (f20 > 0.01f) {
            Shape.scrollEdge(f + 2.0f, this.settingsListTop, f7 - 2.0f, 12.0f, true, ClickGuiStyle.withAlpha(n4, Math.round((float)n3 * f20)));
            RenderUtils.drawRect(f + 14.0f, this.settingsListTop - 0.5f, f7 - 14.0f, this.settingsListTop, ClickGuiStyle.withAlpha(ClickGuiStyle.SURFACE_HOVER, Math.round((float)n3 * 0.6f * f20)));
        }
        if ((f5 = Math.min(1.0f, (f17 - f18) / 12.0f)) > 0.01f) {
            Shape.scrollEdge(f + 2.0f, this.settingsListBottom, f7 - 2.0f, 12.0f, false, ClickGuiStyle.withAlpha(n4, Math.round((float)n3 * f5)));
        }
    }

    private void openSettingsOverlay(ModuleComponent moduleComponent) {
        if (moduleComponent == null || !moduleComponent.hasSettings()) {
            return;
        }
        this.settingsOverlayModule = moduleComponent;
        if (moduleComponent.mod instanceof BlockESP) {
            moduleComponent.reloadSettings();
        }
        moduleComponent.isOpened = true;
        moduleComponent.overlayHosted = true;
        this.settingsScrollAnim.reset(0.0f);
        this.settingsOverlayAnim.snap(false);
        this.settingsOverlayAnim.setTarget(true);
    }

    private void closeSettingsOverlay() {
        this.closeSettingsOverlay(true);
    }

    private void closeSettingsOverlayInstant() {
        this.closeSettingsOverlay(false);
    }

    private void closeSettingsOverlay(boolean bl) {
        if (this.settingsOverlayModule == null) {
            return;
        }
        if (bl) {
            this.closingOverlayTitle = this.settingsOverlayModule.mod.getName();
        }
        PopupMenu.closeInstantly();
        this.settingsOverlayModule.overlayHosted = false;
        this.settingsOverlayModule.isOpened = false;
        this.settingsOverlayModule.updateHeight(this.settingsOverlayModule.yPos);
        this.settingsOverlayModule = null;
        this.settingsScrollAnim.reset(0.0f);
        if (bl) {
            this.settingsOverlayAnim.setTarget(false);
        } else {
            this.settingsOverlayAnim.snap(false);
        }
    }

    private boolean isSettingsOverlayOpen() {
        return this.settingsOverlayModule != null;
    }

    private boolean canSaveConfig() {
        return Valen.configManager != null && Valen.configManager.getActiveName() != null;
    }

    private void drawSaveConfigButton(int n, int n2) {
        boolean bl = this.canSaveConfig();
        long l = System.currentTimeMillis();
        boolean bl2 = l < this.saveFlashUntilMs;
        boolean bl3 = l < this.saveFailureUntilMs;
        boolean bl4 = bl && Valen.configManager.isDirty();
        this.saveHoverAnim.setTarget(bl && this.isOverSaveButton(n, n2));
        float f = this.saveHoverAnim.getValue();
        this.savePressAnim.setTarget(bl && this.savePressed && Mouse.isButtonDown((int)0) && this.isOverSaveButton(n, n2));
        float f2 = this.savePressAnim.getValue();
        int n3 = !bl ? ClickGuiStyle.DISABLED_SURFACE : (bl3 ? ClickGuiStyle.blend(ClickGuiStyle.INPUT_BACKGROUND, ClickGuiStyle.ERROR, 0.3f) : (bl2 ? ClickGuiStyle.SURFACE_ACTIVE : (bl4 ? ClickGuiStyle.blend(ClickGuiStyle.INPUT_BACKGROUND, ClickGuiStyle.WARNING, 0.24f) : ClickGuiStyle.INPUT_BACKGROUND)));
        int n4 = ClickGuiStyle.blend(n3, ClickGuiStyle.hovered(n3, 1.0f), f);
        Shape.circle(this.saveButtonLeft + 13.0f, this.saveButtonTop + 13.0f, 26.0f - f2 * 2.0f, n4);
        int n5 = !bl ? ClickGuiStyle.TEXT_DISABLED : (bl3 ? ClickGuiStyle.ERROR : (bl2 ? ClickGuiStyle.ACCENT : (bl4 ? ClickGuiStyle.WARNING : ClickGuiStyle.blend(ClickGuiStyle.TEXT_MUTED, ClickGuiStyle.TEXT_PRIMARY, f))));
        RenderUtils.drawIcon(RenderUtils.getIcon(bl2 && bl ? ICON_CHECK : ICON_SAVE), this.saveButtonLeft + 6.0f, this.saveButtonTop + 6.0f, 14, n5);
    }

    private boolean isOverSaveButton(int n, int n2) {
        return (float)n >= this.saveButtonLeft && (float)n <= this.saveButtonRight && (float)n2 >= this.saveButtonTop && (float)n2 <= this.saveButtonBottom;
    }

    private void saveActiveConfig() {
        if (!this.canSaveConfig()) {
            return;
        }
        if (Valen.configManager.saveActive()) {
            this.saveFlashUntilMs = System.currentTimeMillis() + 1400L;
            this.saveFailureUntilMs = 0L;
        } else {
            this.saveFailureUntilMs = System.currentTimeMillis() + 1400L;
        }
    }

    private void drawTopControlTooltip(int n, int n2) {
        if (this.isSettingsOverlayOpen()) {
            return;
        }
        String string = null;
        if (this.isOverSearchBox(n, n2)) {
            string = "Search modules (Ctrl+F)";
        } else if (this.isOverSaveButton(n, n2)) {
            long l = System.currentTimeMillis();
            if (!this.canSaveConfig()) {
                string = "No profile loaded";
            } else if (l < this.saveFailureUntilMs) {
                string = "Save failed";
            } else if (l < this.saveFlashUntilMs) {
                string = "Saved";
            } else {
                String string2 = string = Valen.configManager.isDirty() ? "Save changes" : "Save profile";
            }
        }
        if (string == null) {
            return;
        }
        float f = PageUI.smallWidth(string) + 12.0f;
        float f2 = PageUI.smallHeight() + 7.0f;
        float f3 = Math.max(this.contentAreaLeft, Math.min(this.contentAreaRight - f, (float)n - f / 2.0f));
        float f4 = Math.min(this.contentAreaBottom - f2, this.saveButtonBottom + 5.0f);
        Shape.capsule(f3, f4, f3 + f, f4 + f2, ClickGuiStyle.TOOLTIP_BACKGROUND);
        PageUI.small(string, f3 + 6.0f, PageUI.smallY(f4, f4 + f2), ClickGuiStyle.TEXT_PRIMARY);
    }

    private void drawSearchControl(int n, int n2, float f) {
        this.searchHoverAnim.setTarget(this.isOverSearchBox(n, n2));
        float f2 = this.searchHoverAnim.getValue();
        this.searchPressAnim.setTarget(this.searchPressedButton >= 0 && Mouse.isButtonDown((int)this.searchPressedButton) && this.isOverSearchBox(n, n2));
        float f3 = this.searchPressAnim.getValue();
        int n3 = ClickGuiStyle.blend(ClickGuiStyle.INPUT_BACKGROUND, ClickGuiStyle.INPUT_BACKGROUND_FOCUSED, f);
        int n4 = ClickGuiStyle.blend(n3, ClickGuiStyle.hovered(n3, 1.0f), f2);
        Shape.capsule(this.searchBoxLeft + f3, this.searchBoxTop + f3, this.searchBoxRight - f3, this.searchBoxBottom - f3, n4);
        float f4 = 6.0f;
        float f5 = this.searchBoxLeft + f4 + (8.0f - f4) * f;
        RenderUtils.drawIcon(RenderUtils.getIcon(ICON_SEARCH), f5, this.searchBoxTop + f4, 14, ClickGuiStyle.blend(ClickGuiStyle.TEXT_MUTED, ClickGuiStyle.TEXT_PRIMARY, Math.max(f2, f)));
        float f6 = (f - 0.4f) / 0.6f;
        if (f6 <= 0.02f) {
            return;
        }
        int n5 = Math.max(1, Math.round(Math.min(1.0f, f6) * 255.0f));
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiHeaderFontRenderer();
        float f7 = f5 + 14.0f + 8.0f;
        float f8 = PageUI.textY(valenFontRenderer, 1.0f, this.searchBoxTop, this.searchBoxBottom);
        float f9 = Math.max(4.0f, this.searchBoxRight - 12.0f - f7);
        String string = this.searchQuery;
        while (!string.isEmpty() && (float)valenFontRenderer.getStringWidth(string) > f9) {
            string = string.substring(1);
        }
        if (string.isEmpty()) {
            valenFontRenderer.drawString(this.searchFocused ? "Type to search" : "Search modules", f7, f8, ClickGuiStyle.withAlpha(ClickGuiStyle.TEXT_MUTED, n5), false);
        } else {
            valenFontRenderer.drawString(string, f7, f8, ClickGuiStyle.withAlpha(ClickGuiStyle.TEXT_PRIMARY, n5), false);
        }
        if (this.searchFocused && System.currentTimeMillis() / 530L % 2L == 0L) {
            float f10 = f7 + (float)valenFontRenderer.getStringWidth(string) + 1.0f;
            Shape.capsule(f10, this.searchBoxTop + 7.0f, f10 + 1.0f, this.searchBoxBottom - 7.0f, ClickGuiStyle.withAlpha(ClickGuiStyle.ACCENT, n5));
        }
    }

    private void renderBrand(float f, float f2) {
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiBrandFontRenderer();
        float f3 = f + 20.0f;
        float f4 = f2 + 20.0f;
        valenFontRenderer.drawString("Valen", f3, f4, ClickGuiStyle.TEXT_PRIMARY, false);
    }

    private void layoutSidebarTabs(float f, float f2, float f3, float f4) {
        int n = REFERENCE_TAB_LABELS.length;
        int n2 = Math.max(0, n - 3);
        float f5 = f3 + 62.0f;
        float f6 = 78.0f + (float)Math.max(0, 2) * 5.0f;
        float f7 = f3 + f4 - 14.0f - f6;
        for (int i = 0; i < n; ++i) {
            float f8 = i < n2 ? f5 + (float)i * 31.0f : f7 + (float)(i - n2) * 31.0f;
            this.referenceTabLeft[i] = f + 9.0f;
            this.referenceTabRight[i] = f2 - 9.0f;
            this.referenceTabTop[i] = f8;
            this.referenceTabBottom[i] = f8 + 26.0f;
        }
    }

    private void renderSidebarTabPill(float f, float f2) {
        float f3;
        float f4;
        float f5 = this.referenceTabLeft[this.selectedReferenceTab];
        float f6 = this.referenceTabRight[this.selectedReferenceTab];
        float f7 = this.referenceTabTop[this.selectedReferenceTab] - f2;
        float f8 = this.referenceTabBottom[this.selectedReferenceTab] - f2;
        if (this.tabPillTrackedTab != this.selectedReferenceTab) {
            f4 = this.tabPillTrackedTab < 0 ? 1.0f : Math.min(1.0f, (f - this.tabPillAnimStartMs) / 220.0f);
            f3 = 1.0f - (1.0f - f4) * (1.0f - f4) * (1.0f - f4);
            this.tabPillFromTop = this.tabPillTrackedTab < 0 ? f7 : this.tabPillFromTop + (this.tabPillToTop - this.tabPillFromTop) * f3;
            this.tabPillFromBottom = this.tabPillTrackedTab < 0 ? f8 : this.tabPillFromBottom + (this.tabPillToBottom - this.tabPillFromBottom) * f3;
            this.tabPillToTop = f7;
            this.tabPillToBottom = f8;
            this.tabPillAnimStartMs = f;
            this.tabPillTrackedTab = this.selectedReferenceTab;
        }
        f4 = Math.min(1.0f, (f - this.tabPillAnimStartMs) / 220.0f);
        f3 = 1.0f - (1.0f - f4) * (1.0f - f4) * (1.0f - f4);
        float f9 = f2 + this.tabPillFromTop + (this.tabPillToTop - this.tabPillFromTop) * f3;
        float f10 = f2 + this.tabPillFromBottom + (this.tabPillToBottom - this.tabPillFromBottom) * f3;
        Shape.capsule(f5, f9, f6, f10, ClickGuiStyle.NAV_SELECTED);
    }

    private GuiPage getSelectedPage() {
        if (this.selectedReferenceTab < 0 || this.selectedReferenceTab >= this.referenceTabPages.length) {
            return null;
        }
        return this.referenceTabPages[this.selectedReferenceTab];
    }

    private List<CategoryComponent> getReferenceColumnPanels() {
        List<CategoryComponent> list = this.getActiveReferenceCategories();
        StringBuilder stringBuilder = new StringBuilder(this.isSearchActive() ? "search:" + this.searchQuery.trim() : "tab:" + this.selectedReferenceTab);
        for (CategoryComponent categoryComponent : list) {
            categoryComponent.opened = true;
            stringBuilder.append('|').append(categoryComponent.category.name()).append('#').append(categoryComponent.getStructureVersion());
        }
        String string = stringBuilder.toString();
        if (!string.equals(this.referencePanelsKey)) {
            this.referencePanelsKey = string;
            this.rebuildReferenceColumnPanels(list);
        }
        return this.referencePanels;
    }

    private void rebuildReferenceColumnPanels(List<CategoryComponent> list) {
        Object object;
        this.pendingReferenceScrollOffset = null;
        if (!this.referencePanels.isEmpty() && !this.isSearchActive()) {
            boolean bl;
            object = this.referencePanels.get(0);
            boolean bl2 = bl = !list.isEmpty() && ((CategoryComponent)object).category == list.get((int)0).category;
            if (bl) {
                this.pendingReferenceScrollOffset = Float.valueOf(((CategoryComponent)object).getScrollTargetOffset());
            }
        }
        this.referencePanels.clear();
        object = new ArrayList();
        Module.category category2 = null;
        for (CategoryComponent categoryComponent : list) {
            if (category2 == null) {
                category2 = categoryComponent.category;
            }
            object.addAll(categoryComponent.getModules());
        }
        if (object.isEmpty()) {
            return;
        }
        this.referencePanels.add(new CategoryComponent(category2, (List<ModuleComponent>)object));
    }

    private List<CategoryComponent> getReferenceCategoriesForSelectedTab() {
        ArrayList<CategoryComponent> arrayList = new ArrayList<CategoryComponent>();
        if (this.selectedReferenceTab < 0 || this.selectedReferenceTab >= REFERENCE_TAB_CATEGORIES.length || REFERENCE_TAB_CATEGORIES[this.selectedReferenceTab] == null) {
            return arrayList;
        }
        for (Module.category category2 : REFERENCE_TAB_CATEGORIES[this.selectedReferenceTab]) {
            CategoryComponent categoryComponent = this.getCategoryComponent(category2);
            if (categoryComponent == null) continue;
            arrayList.add(categoryComponent);
        }
        return arrayList;
    }

    private List<CategoryComponent> getActiveReferenceCategories() {
        if (!this.isSearchActive()) {
            return this.getReferenceCategoriesForSelectedTab();
        }
        ArrayList<CategoryComponent> arrayList = new ArrayList<CategoryComponent>();
        for (CategoryComponent categoryComponent : categories) {
            if (categoryComponent.getModules().isEmpty()) continue;
            arrayList.add(categoryComponent);
        }
        return arrayList;
    }

    private boolean isSearchActive() {
        return !this.searchQuery.trim().isEmpty();
    }

    private void setSearchQuery(String string) {
        this.searchQuery = string;
        String string2 = string.trim();
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.setModuleFilter(string2);
        }
    }

    private boolean isOverSearchBox(int n, int n2) {
        return (float)n >= this.searchBoxLeft && (float)n <= this.searchBoxRight && (float)n2 >= this.searchBoxTop && (float)n2 <= this.searchBoxBottom;
    }

    private void handleSearchKey(char c, int n) {
        if (n == 1) {
            this.setSearchQuery("");
            this.searchFocused = false;
            return;
        }
        if (n == 28 || n == 156) {
            this.searchFocused = false;
            return;
        }
        if (n == 14) {
            if (!this.searchQuery.isEmpty()) {
                this.setSearchQuery(this.searchQuery.substring(0, this.searchQuery.length() - 1));
            }
            return;
        }
        if (ChatAllowedCharacters.isAllowedCharacter((char)c) && this.searchQuery.length() < 32) {
            this.setSearchQuery(this.searchQuery + c);
        }
    }

    private CategoryComponent getCategoryComponent(Module.category category2) {
        for (CategoryComponent categoryComponent : categories) {
            if (categoryComponent.category != category2) continue;
            return categoryComponent;
        }
        return null;
    }

    private boolean handleReferenceNavigationClick(int n, int n2, int n3) {
        if (!this.isReferenceLayout() || n3 != 0) {
            return false;
        }
        for (int i = 0; i < REFERENCE_TAB_LABELS.length; ++i) {
            if (!((float)n >= this.referenceTabLeft[i]) || !((float)n <= this.referenceTabRight[i]) || !((float)n2 >= this.referenceTabTop[i]) || !((float)n2 <= this.referenceTabBottom[i])) continue;
            this.selectTab(i);
            return true;
        }
        return false;
    }

    private void selectTab(int n) {
        PopupMenu.closeInstantly();
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage != null) {
            guiPage.onClosed();
        }
        int n2 = REFERENCE_TAB_LABELS.length;
        this.selectedReferenceTab = (n % n2 + n2) % n2;
        this.closeSettingsOverlayInstant();
        this.layoutReady = false;
        if (this.isSearchActive()) {
            this.setSearchQuery("");
        }
        this.searchFocused = false;
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.markInteracted();
        }
    }

    public void drawScreen(int n, int n2, float f) {
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage instanceof GuiSettingsPage) {
            ((GuiSettingsPage)guiPage).beforeFrame();
        }
        this.openingAnimation.update(System.nanoTime());
        this.presentationTransform.update(this.actualScreenWidth, this.actualScreenHeight, Gui.appearance.scalePercent(), this.openingAnimation.getScale());
        double d = this.getRenderScale();
        this.designViewportWidth = Math.max(1, MathHelper.ceiling_double_int((double)((double)this.actualScreenWidth / d)));
        this.designViewportHeight = Math.max(1, MathHelper.ceiling_double_int((double)((double)this.actualScreenHeight / d)));
        if (this.openingAnimation.isAnimating()) {
            this.openingScene.capture(this.mc.displayWidth, this.mc.displayHeight);
        } else {
            this.openingScene.release();
        }
        int n3 = this.toRenderedLogicalCoordinate(n);
        int n4 = this.toRenderedLogicalCoordinate(n2);
        boolean bl = PopupMenu.isOpen();
        int n5 = bl ? -100000 : n3;
        int n6 = bl ? -100000 : n4;
        lastLogicalMouseX = n5;
        lastLogicalMouseY = n6;
        if (this.isReferenceLayout()) {
            ClickGui.drawRect((int)0, (int)0, (int)this.actualScreenWidth, (int)this.actualScreenHeight, (int)ClickGuiStyle.PAGE_SCRIM);
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale((double)this.getPresentationScale(), (double)this.getPresentationScale(), (double)1.0);
        if (this.isReferenceLayout()) {
            this.renderReferenceLayout(n5, n6);
            PopupMenu.render(n3, n4, this.contentAreaLeft, this.contentAreaTop, this.contentAreaRight, this.contentAreaBottom);
        } else {
            List<CategoryComponent> list = this.getCategoriesInRenderOrder();
            CategoryComponent categoryComponent = this.getTopmostUnderCursor(list, n5, n6);
            for (CategoryComponent categoryComponent2 : list) {
                categoryComponent2.render(this.fontRendererObj);
                categoryComponent2.mousePosition(n5, n6, categoryComponent2 == categoryComponent);
                for (Component component : categoryComponent2.getModules()) {
                    component.drawScreen(n5, n6);
                }
            }
        }
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        if (CommandLine.opened) {
            int n7;
            int n8;
            if (!this.commandLineSend.visible) {
                this.commandLineSend.visible = true;
            }
            int n9 = n8 = CommandLine.animate.isToggled() ? CommandLine.animation.getValueInt(0, 200, 2) : 200;
            if (CommandLine.closed && (n8 = 200 - n8) == 0) {
                CommandLine.closed = false;
                CommandLine.opened = false;
                this.commandLineSend.visible = false;
            }
            ClickGui.drawRect((int)0, (int)0, (int)n8, (int)this.designViewportHeight, (int)-1089466352);
            this.drawHorizontalLine(0, n8 - 1, this.designViewportHeight - 345, -1);
            this.drawHorizontalLine(0, n8 - 1, this.designViewportHeight - 115, -1);
            ClickGui.drawRect((int)(n8 - 1), (int)0, (int)n8, (int)this.designViewportHeight, (int)-1);
            CommandHandler.renderCommandOutput((FontRenderer)this.fontRendererObj, (int)this.designViewportHeight, (int)n8, (double)((double)this.sr.getScaleFactor() * this.getPresentationScale()));
            this.commandLineInput.xPosition = n7 = n8 - 178;
            this.commandLineInput.yPosition = this.designViewportHeight - 100;
            this.commandLineSend.xPosition = n7;
            this.commandLineSend.yPosition = this.designViewportHeight - 70;
            this.commandLineInput.drawTextBox();
            super.drawScreen(n5, n6, f);
        } else if (CommandLine.closed) {
            CommandLine.closed = false;
        }
        GlStateManager.popMatrix();
        this.openingScene.draw(this.sr.getScaledWidth_double(), this.sr.getScaledHeight_double(), 1.0f - this.openingAnimation.getProgress());
        this.layoutReady = true;
        if (guiPage instanceof GuiSettingsPage) {
            ((GuiSettingsPage)guiPage).afterFrame();
        } else if (Valen.configManager != null) {
            Valen.configManager.tickGuiAutosave();
        }
    }

    public void mouseClicked(int n, int n2, int n3) throws IOException {
        int n4;
        if (!this.layoutReady) {
            return;
        }
        int n5 = this.toRenderedLogicalCoordinate(n);
        if (PopupMenu.mouseClicked(n5, n4 = this.toRenderedLogicalCoordinate(n2), n3)) {
            return;
        }
        BindComponent bindComponent = this.activeBinding();
        if (bindComponent != null) {
            if (bindComponent.keySetting == null) {
                bindComponent.moduleComponent.mod.setBind(n3 + 1000);
            } else {
                bindComponent.keySetting.setKey(n3 + 1000);
            }
            bindComponent.isBinding = false;
            return;
        }
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage != null && guiPage.blocksBackgroundInput()) {
            guiPage.onClick(n5, n4, n3);
            return;
        }
        if (this.handleReferenceNavigationClick(n5, n4, n3)) {
            return;
        }
        if (guiPage != null) {
            if (guiPage.onClick(n5, n4, n3)) {
                return;
            }
            if (CommandLine.opened) {
                this.commandLineInput.mouseClicked(n5, n4, n3);
                super.mouseClicked(n5, n4, n3);
            }
            return;
        }
        if (this.isSettingsOverlayOpen()) {
            if (n3 == 0 && (float)n5 >= this.settingsFavoriteLeft && (float)n5 <= this.settingsFavoriteRight && (float)n4 >= this.settingsFavoriteTop && (float)n4 <= this.settingsFavoriteBottom) {
                Module module = this.settingsOverlayModule.mod;
                this.closeSettingsOverlayInstant();
                if (Valen.preferences != null) {
                    Valen.preferences.toggleFavorite(module);
                }
                return;
            }
            if (n3 == 0 && (float)n5 >= this.settingsResetLeft && (float)n5 <= this.settingsResetRight && (float)n4 >= this.settingsResetTop && (float)n4 <= this.settingsResetBottom) {
                if (Valen.configManager != null) {
                    boolean bl;
                    boolean bl2 = Valen.configManager.canUndoReset(this.settingsOverlayModule.mod);
                    boolean bl3 = bl = bl2 ? Valen.configManager.undoModuleReset(this.settingsOverlayModule.mod) : Valen.configManager.resetModuleSettings(this.settingsOverlayModule.mod);
                    if (bl) {
                        this.settingsOverlayModule.reloadSettings();
                        Notifications.pushMessage((bl2 ? "Restored " : "Reset ") + this.settingsOverlayModule.mod.getName() + " settings");
                    }
                }
                return;
            }
            if ((float)n5 >= this.settingsCloseLeft && (float)n5 <= this.settingsCloseRight && (float)n4 >= this.settingsCloseTop && (float)n4 <= this.settingsCloseBottom) {
                this.closeSettingsOverlay();
                return;
            }
            if ((float)n5 >= this.contentAreaLeft && (float)n5 <= this.contentAreaRight && (float)n4 >= this.contentAreaTop && (float)n4 <= this.contentAreaBottom) {
                CategoryComponent categoryComponent3 = this.settingsOverlayModule.categoryComponent;
                float f = categoryComponent3.moduleY;
                categoryComponent3.moduleY = categoryComponent3.getY();
                this.settingsOverlayModule.onSettingClick(n5, n4, n3);
                FocusableTextComponent focusableTextComponent = this.findFocusedTextComponentAt(n5, n4);
                this.enforceSingleFocusedTextInput(focusableTextComponent);
                categoryComponent3.moduleY = f;
                return;
            }
        }
        if (this.isReferenceLayout() && n3 == 0 && this.isOverSaveButton(n5, n4)) {
            this.searchFocused = false;
            if (this.canSaveConfig()) {
                this.savePressed = true;
                this.savePressAnim.snap(true);
            }
            this.saveActiveConfig();
            return;
        }
        if (this.isReferenceLayout() && (n3 == 0 || n3 == 1)) {
            if (this.isOverSearchBox(n5, n4)) {
                this.searchPressedButton = n3;
                this.searchPressAnim.snap(true);
                this.searchFocused = true;
                this.enforceSingleFocusedTextInput(null);
                return;
            }
            this.searchFocused = false;
        }
        List<CategoryComponent> list = this.isReferenceLayout() ? this.getReferenceColumnPanels() : categories;
        ArrayList<CategoryComponent> arrayList = new ArrayList<CategoryComponent>(list);
        arrayList.sort((categoryComponent, categoryComponent2) -> Long.compare(categoryComponent2.lastInteractedTime, categoryComponent.lastInteractedTime));
        CategoryComponent categoryComponent4 = null;
        for (CategoryComponent object : arrayList) {
            if (!object.overRect(n5, n4)) continue;
            categoryComponent4 = object;
            break;
        }
        if (categoryComponent4 != null) {
            categoryComponent4.markInteracted();
        }
        if (n3 == 0) {
            for (CategoryComponent categoryComponent3 : list) {
                categoryComponent3.overTitle(false);
            }
            if (!this.isReferenceLayout() && categoryComponent4 != null && categoryComponent4.draggable(n5, n4)) {
                categoryComponent4.overTitle(true);
                categoryComponent4.xx = (float)n5 - categoryComponent4.getX();
                categoryComponent4.yy = (float)n4 - categoryComponent4.getY();
                categoryComponent4.dragging = true;
            }
        }
        if (!this.isReferenceLayout() && n3 == 1 && categoryComponent4 != null && categoryComponent4.overTitle(n5, n4)) {
            categoryComponent4.mouseClicked(!categoryComponent4.isOpened());
        }
        if (categoryComponent4 != null && categoryComponent4.isOpened() && !categoryComponent4.getModules().isEmpty() && !categoryComponent4.overTitle(n5, n4)) {
            if (n3 == 1 || n3 == 0) {
                for (ModuleComponent moduleComponent : categoryComponent4.getModules()) {
                    if (!moduleComponent.overModuleName(n5, n4) || moduleComponent.overHeaderBind(n5, n4) || n3 != 1 && moduleComponent.mod.canBeEnabled()) continue;
                    this.openSettingsOverlay(moduleComponent);
                    return;
                }
            }
            for (ModuleComponent moduleComponent : categoryComponent4.getModules()) {
                if (moduleComponent.onClick(n5, n4, n3)) break;
            }
        }
        if (CommandLine.opened) {
            this.commandLineInput.mouseClicked(n5, n4, n3);
            super.mouseClicked(n5, n4, n3);
        }
        if (n3 == 0 || n3 == 1) {
            Iterator<ModuleComponent> iterator = this.findFocusedTextComponentAt(n5, n4);
            this.enforceSingleFocusedTextInput((FocusableTextComponent)((Object)iterator));
        }
    }

    public void mouseReleased(int n, int n2, int n3) {
        if (n3 == this.searchPressedButton) {
            this.searchPressedButton = -1;
            this.searchPressAnim.setTarget(false);
        }
        if (n3 == 0) {
            this.savePressed = false;
            this.savePressAnim.setTarget(false);
        }
        int n4 = this.toRenderedLogicalCoordinate(n);
        int n5 = this.toRenderedLogicalCoordinate(n2);
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage != null && guiPage.blocksBackgroundInput()) {
            return;
        }
        if (guiPage != null) {
            guiPage.mouseReleased(n4, n5, n3);
        }
        if (n3 == 0 && this.isSettingsOverlayOpen()) {
            CategoryComponent categoryComponent = this.settingsOverlayModule.categoryComponent;
            float f = categoryComponent.moduleY;
            categoryComponent.moduleY = categoryComponent.getY();
            this.settingsOverlayModule.mouseReleased(n4, n5, n3);
            categoryComponent.moduleY = f;
            return;
        }
        if (n3 == 0) {
            List<CategoryComponent> list = this.isReferenceLayout() ? this.getReferenceColumnPanels() : categories;
            for (CategoryComponent categoryComponent : list) {
                categoryComponent.overTitle(false);
                if (!categoryComponent.isOpened() || categoryComponent.getModules().isEmpty()) continue;
                for (Component component : categoryComponent.getModules()) {
                    component.mouseReleased(n4, n5, n3);
                }
            }
        }
        if (this.pendingScaleRefresh) {
            this.pendingScaleRefresh = false;
            this.refreshLayoutForConfiguredScale();
        }
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int n = Mouse.getDWheel();
        if (!this.layoutReady || n == 0) {
            return;
        }
        if (PopupMenu.scrolled()) {
            return;
        }
        int n2 = Mouse.getEventX() * this.actualScreenWidth / this.mc.displayWidth;
        int n3 = this.actualScreenHeight - Mouse.getEventY() * this.actualScreenHeight / this.mc.displayHeight - 1;
        int n4 = this.toRenderedLogicalCoordinate(n2);
        int n5 = this.toRenderedLogicalCoordinate(n3);
        BindComponent bindComponent = this.activeBinding();
        if (bindComponent != null) {
            bindComponent.onScroll(n);
            return;
        }
        if (!this.isReferenceLayout()) {
            for (CategoryComponent categoryComponent : categories) {
                categoryComponent.onScroll(n, n4, n5);
            }
            return;
        }
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage != null) {
            guiPage.onScroll(n, n4, n5);
            return;
        }
        if (this.isSettingsOverlayOpen()) {
            CategoryComponent categoryComponent = this.settingsOverlayModule.categoryComponent;
            float f = categoryComponent.moduleY;
            categoryComponent.moduleY = categoryComponent.getY();
            boolean bl = false;
            for (Component component : this.settingsOverlayModule.settings) {
                if (component instanceof AbstractSearchListComponent && ((AbstractSearchListComponent)component).capturesCategoryScroll(n4, n5) || component instanceof PlayerListComponent && ((PlayerListComponent)component).capturesCategoryScroll(n4, n5) || component instanceof StringListComponent && ((StringListComponent)component).capturesCategoryScroll(n4, n5)) {
                    component.onScroll(n);
                    bl = true;
                    break;
                }
                if (!(component instanceof BindComponent) || !((BindComponent)component).isBinding) continue;
                component.onScroll(n);
                bl = true;
                break;
            }
            categoryComponent.moduleY = f;
            if (!bl) {
                float f2 = Math.max(0.0f, this.settingsListBottom - this.settingsListTop);
                float f3 = Math.max(0.0f, this.settingsContentHeight - f2);
                this.settingsScrollAnim.extend((float)(-n) / 120.0f * 34.0f);
                this.settingsScrollAnim.clampTarget(0.0f, f3);
            }
            return;
        }
        List<CategoryComponent> list = this.getReferenceColumnPanels();
        for (CategoryComponent categoryComponent : list) {
            categoryComponent.forwardScrollToModules(n);
        }
        for (CategoryComponent categoryComponent : list) {
            if (!categoryComponent.capturesNestedScroll(n4, n5)) continue;
            return;
        }
        if ((float)n4 < this.contentAreaLeft || (float)n4 > this.contentAreaRight || (float)n5 < this.contentAreaTop || (float)n5 > this.contentAreaBottom) {
            return;
        }
        for (CategoryComponent categoryComponent : list) {
            categoryComponent.scrollList(n);
        }
    }

    public void refreshAfterProfileLoad() {
        for (GuiPage guiPage : this.referenceTabPages) {
            if (!(guiPage instanceof GuiSettingsPage)) continue;
            ((GuiSettingsPage)guiPage).profileLoaded();
        }
        if (this.mc == null) {
            this.mc = Minecraft.getMinecraft();
        }
        this.refreshLayoutForConfiguredScale();
    }

    public void refreshFavorites() {
        this.referencePanelsKey = null;
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.reloadModules();
        }
    }

    public void setWorldAndResolution(Minecraft minecraft, int n, int n2) {
        this.mc = minecraft;
        this.itemRender = minecraft.getRenderItem();
        this.fontRendererObj = minecraft.fontRendererObj;
        this.refreshScaledResolution();
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
            this.buttonList.clear();
            this.initGui();
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
    }

    public void keyTyped(char c, int n) {
        if (PopupMenu.keyTyped(n)) {
            return;
        }
        BindComponent bindComponent = this.activeBinding();
        if (bindComponent != null) {
            bindComponent.keyTyped(c, n);
            return;
        }
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage != null) {
            if (guiPage.keyTyped(c, n)) {
                return;
            }
            if (n == 1) {
                this.mc.displayGuiScreen(null);
                return;
            }
            if (!CommandLine.opened) {
                if (n == 203) {
                    this.selectTab(this.selectedReferenceTab - 1);
                    return;
                }
                if (n == 205) {
                    this.selectTab(this.selectedReferenceTab + 1);
                    return;
                }
                if (n >= 2 && n < 2 + REFERENCE_TAB_LABELS.length) {
                    this.selectTab(n - 2);
                    return;
                }
            }
            if (CommandLine.opened) {
                String string = this.commandLineInput.getText();
                if (n == 28 && !string.isEmpty()) {
                    CommandHandler.runCommand((String)string);
                    this.commandLineInput.setText("");
                    return;
                }
                this.commandLineInput.textboxKeyTyped(c, n);
            }
            return;
        }
        if (this.searchFocused && !this.binding()) {
            this.handleSearchKey(c, n);
            return;
        }
        if (this.isReferenceLayout() && !CommandLine.opened && !this.binding() && n == 33 && (Keyboard.isKeyDown((int)29) || Keyboard.isKeyDown((int)157))) {
            this.searchFocused = true;
            this.enforceSingleFocusedTextInput(null);
            return;
        }
        if (this.isSettingsOverlayOpen()) {
            if (n == 1 && !this.binding()) {
                this.closeSettingsOverlay();
                return;
            }
            this.settingsOverlayModule.keyTyped(c, n);
            return;
        }
        FocusableTextComponent focusableTextComponent = this.getActiveFocusedTextInput();
        if (focusableTextComponent != null) {
            for (CategoryComponent categoryComponent : categories) {
                if (!categoryComponent.isOpened() || categoryComponent.getAllModules().isEmpty()) continue;
                for (Component component : categoryComponent.getAllModules()) {
                    component.keyTyped(c, n);
                }
            }
            return;
        }
        if (n == 1 && !this.binding()) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (this.isReferenceLayout() && !CommandLine.opened && !this.binding()) {
            if (n == 203) {
                this.selectTab(this.selectedReferenceTab - 1);
                return;
            }
            if (n == 205) {
                this.selectTab(this.selectedReferenceTab + 1);
                return;
            }
            if (n >= 2 && n < 2 + REFERENCE_TAB_LABELS.length) {
                this.selectTab(n - 2);
                return;
            }
        }
        for (CategoryComponent categoryComponent : categories) {
            if (!categoryComponent.isOpened() || categoryComponent.getAllModules().isEmpty()) continue;
            for (Component component : categoryComponent.getAllModules()) {
                component.keyTyped(c, n);
            }
        }
        if (CommandLine.opened) {
            String string = this.commandLineInput.getText();
            if (n == 28 && !string.isEmpty()) {
                CommandHandler.runCommand((String)this.commandLineInput.getText());
                this.commandLineInput.setText("");
                return;
            }
            this.commandLineInput.textboxKeyTyped(c, n);
        }
    }

    public void actionPerformed(GuiButton guiButton) {
        if (guiButton == this.commandLineSend) {
            CommandHandler.runCommand((String)this.commandLineInput.getText());
            this.commandLineInput.setText("");
        }
    }

    public void onGuiClosed() {
        PopupMenu.closeInstantly();
        this.openingAnimation.reset();
        this.openingScene.release();
        this.searchPressedButton = -1;
        this.savePressed = false;
        this.searchPressAnim.snap(false);
        this.savePressAnim.snap(false);
        this.logoSmoothLength = null;
        if (this.sf != null) {
            this.sf.cancel(true);
            this.sf = null;
        }
        for (GuiPage object : this.referenceTabPages) {
            if (object == null) continue;
            object.onClosed();
        }
        this.searchFocused = false;
        this.setSearchQuery("");
        this.searchOpenAnim.snap(false);
        this.searchHoverAnim.snap(false);
        this.saveHoverAnim.snap(false);
        this.closeSettingsOverlayInstant();
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.dragging = false;
            categoryComponent.onGuiClosed();
            for (Component component : categoryComponent.getAllModules()) {
                component.onGuiClosed();
            }
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private BindComponent activeBinding() {
        Object object;
        GuiPage guiPage = this.getSelectedPage();
        if (guiPage != null && (object = guiPage.activeBinding()) != null) {
            return object;
        }
        for (CategoryComponent categoryComponent : categories) {
            for (ModuleComponent moduleComponent : categoryComponent.getAllModules()) {
                for (Component component : moduleComponent.settings) {
                    if (!(component instanceof BindComponent) || !((BindComponent)component).isBinding) continue;
                    return (BindComponent)component;
                }
            }
        }
        return null;
    }

    private boolean binding() {
        for (CategoryComponent categoryComponent : categories) {
            for (ModuleComponent moduleComponent : categoryComponent.getAllModules()) {
                for (Component component : moduleComponent.settings) {
                    if (component instanceof BindComponent && ((BindComponent)component).isBinding) {
                        return true;
                    }
                    if (!(component instanceof FocusableTextComponent) || !((FocusableTextComponent)((Object)component)).isTextInputFocused()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean unfocusFocusedTextInput() {
        for (CategoryComponent categoryComponent : categories) {
            for (ModuleComponent moduleComponent : categoryComponent.getAllModules()) {
                for (Component component : moduleComponent.settings) {
                    FocusableTextComponent focusableTextComponent;
                    if (!(component instanceof FocusableTextComponent) || !(focusableTextComponent = (FocusableTextComponent)((Object)component)).isTextInputFocused()) continue;
                    focusableTextComponent.unfocusTextInput();
                    return true;
                }
            }
        }
        return false;
    }

    private FocusableTextComponent getActiveFocusedTextInput() {
        FocusableTextComponent focusableTextComponent = null;
        for (CategoryComponent categoryComponent : categories) {
            for (ModuleComponent moduleComponent : categoryComponent.getAllModules()) {
                for (Component component : moduleComponent.settings) {
                    FocusableTextComponent focusableTextComponent2;
                    if (!(component instanceof FocusableTextComponent) || !(focusableTextComponent2 = (FocusableTextComponent)((Object)component)).isTextInputFocused()) continue;
                    if (focusableTextComponent == null) {
                        focusableTextComponent = focusableTextComponent2;
                        continue;
                    }
                    focusableTextComponent2.unfocusTextInput();
                }
            }
        }
        return focusableTextComponent;
    }

    private FocusableTextComponent findFocusedTextComponentAt(int n, int n2) {
        ArrayList<CategoryComponent> arrayList = new ArrayList<CategoryComponent>(this.isReferenceLayout() ? this.getReferenceColumnPanels() : categories);
        arrayList.sort((categoryComponent, categoryComponent2) -> Long.compare(categoryComponent2.lastInteractedTime, categoryComponent.lastInteractedTime));
        for (CategoryComponent categoryComponent3 : arrayList) {
            if (!categoryComponent3.isOpened() || !categoryComponent3.overRect(n, n2)) continue;
            for (ModuleComponent moduleComponent : categoryComponent3.getModules()) {
                for (Component component : moduleComponent.settings) {
                    FocusableTextComponent focusableTextComponent;
                    if (!(component instanceof FocusableTextComponent) || !(focusableTextComponent = (FocusableTextComponent)((Object)component)).isTextInputFocused() || !focusableTextComponent.containsClick(n, n2)) continue;
                    return focusableTextComponent;
                }
            }
        }
        return null;
    }

    private void enforceSingleFocusedTextInput(FocusableTextComponent focusableTextComponent) {
        for (CategoryComponent categoryComponent : categories) {
            for (ModuleComponent moduleComponent : categoryComponent.getAllModules()) {
                for (Component component : moduleComponent.settings) {
                    FocusableTextComponent focusableTextComponent2;
                    if (!(component instanceof FocusableTextComponent) || (focusableTextComponent2 = (FocusableTextComponent)((Object)component)) == focusableTextComponent || !focusableTextComponent2.isTextInputFocused()) continue;
                    focusableTextComponent2.unfocusTextInput();
                }
            }
        }
    }

    public void onSliderChange() {
        for (CategoryComponent categoryComponent : categories) {
            for (ModuleComponent moduleComponent : categoryComponent.getAllModules()) {
                moduleComponent.onSliderChange();
            }
        }
    }

    public void requestScaleRefresh() {
        this.pendingScaleRefresh = true;
    }

    private void refreshLayoutForConfiguredScale() {
        this.refreshScaledResolution();
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.setScreenSize(this.designViewportWidth, this.designViewportHeight);
            categoryComponent.limitPositions();
        }
        this.buttonList.clear();
        this.initGui();
    }

    private void refreshScaledResolution() {
        this.layoutReady = false;
        this.sr = new ScaledResolution(this.mc);
        this.actualScreenWidth = this.sr.getScaledWidth();
        this.actualScreenHeight = this.sr.getScaledHeight();
        this.width = this.actualScreenWidth;
        this.height = this.actualScreenHeight;
        this.presentationTransform.update(this.actualScreenWidth, this.actualScreenHeight, Gui.appearance.scalePercent(), this.openingAnimation.getScale());
        double d = this.getRenderScale();
        this.designViewportWidth = Math.max(1, MathHelper.ceiling_double_int((double)((double)this.actualScreenWidth / d)));
        this.designViewportHeight = Math.max(1, MathHelper.ceiling_double_int((double)((double)this.actualScreenHeight / d)));
    }

    private int toRenderedLogicalCoordinate(int n) {
        return this.presentationTransform.toLogical(n);
    }

    private double getRenderScale() {
        if (this.actualScreenWidth <= 0 || this.actualScreenHeight <= 0) {
            return 1.0;
        }
        return GuiTransform.fitScale(this.actualScreenWidth, this.actualScreenHeight, Gui.appearance.scalePercent());
    }

    private double getPresentationScale() {
        return this.presentationTransform.scale();
    }

    public static double getActiveRenderScale() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.currentScreen instanceof ClickGui ? ((ClickGui)minecraft.currentScreen).getPresentationScale() : 1.0;
    }

    static {
        animEpochNs = System.nanoTime();
    }
}

