/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  valen.clickgui.page.ConfigsPage$Cell
 */
package valen.clickgui.page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.RevealAnimation;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.components.impl.BindComponent;
import valen.clickgui.components.impl.ClickGuiTextField;
import valen.clickgui.page.ConfigsPage;
import valen.clickgui.page.GuiPage;
import valen.clickgui.page.HoverSet;
import valen.clickgui.page.PageUI;
import valen.clickgui.page.PremadeProfilesPage;
import valen.clickgui.render.Shape;
import valen.utility.KeybindConflicts;
import valen.utility.RenderUtils;
import valen.utility.config.ClientConfig;
import valen.utility.config.ConfigManager;

/*
 * Exception performing whole class analysis ignored.
 */
public final class ConfigsPage
implements GuiPage {
    private static final float ROW_HEIGHT = 40.0f;
    private static final float ROW_GAP = 4.0f;
    private static final float ROW_INSET = 7.0f;
    private static final float TOP_BUTTON_HEIGHT = 26.0f;
    private static final float TEXT_LEFT_PAD = 12.0f;
    private static final float RIGHT_MARGIN = 12.0f;
    private static final int ICON_SIZE = 16;
    private static final float ICON_GAP = 8.0f;
    private static final float ICON_HIT_PADDING = 5.0f;
    private static final String ICON_LOAD = "/assets/valen/textures/gui/icons/load.png";
    private static final String ICON_SAVE = "/assets/valen/textures/gui/icons/save.png";
    private static final String ICON_FOLDER = "/assets/valen/textures/gui/icons/folder.png";
    private static final String ICON_DELETE = "/assets/valen/textures/gui/icons/delete.png";
    private boolean showingPremades;
    private final PremadeProfilesPage premades = new PremadeProfilesPage();
    private final float[] premadeBox = new float[4];
    private final float[] restoreBox = new float[4];
    private final HoverSet hovers = new HoverSet();
    private final ScrollOffsetAnimation scroll = new ScrollOffsetAnimation(190L);
    private final List<Cell> cells = new ArrayList<Cell>();
    private float viewportLeft;
    private float viewportTop;
    private float viewportRight;
    private float viewportBottom;
    private float contentHeight;
    private boolean creating;
    private ClickGuiTextField createField;
    private final float[] createCellBox = new float[4];
    private final float[] createFieldBox = new float[4];
    private String pendingDeleteName;
    private String closingDeleteName;
    private final RevealAnimation deleteAnim = new RevealAnimation(200.0f);
    private final float[] modalPanelBox = new float[4];
    private final float[] modalCancelBox = new float[4];
    private final float[] modalDeleteBox = new float[4];
    private String bindingProfile;
    private String menuProfile;
    private final float[] menuPanelBox = new float[4];
    private final float[] menuRenameBox = new float[4];
    private final float[] menuDuplicateBox = new float[4];
    private String renamingProfile;
    private String closingRenameProfile;
    private ClickGuiTextField renameField;
    private final RevealAnimation renameAnim = new RevealAnimation(200.0f);
    private final float[] renamePanelBox = new float[4];
    private final float[] renameFieldBox = new float[4];
    private final float[] renameCancelBox = new float[4];
    private final float[] renameConfirmBox = new float[4];
    private String tooltipText;
    private int tooltipMouseX;
    private int tooltipMouseY;
    private String feedbackText;
    private int feedbackColor;
    private long feedbackUntilMs;

    private static ConfigManager configs() {
        return Valen.configManager;
    }

    @Override
    public void render(float f, float f2, float f3, float f4, int n, int n2) {
        if (ConfigsPage.configs() == null) {
            return;
        }
        ConfigsPage.configs().refreshIfChanged();
        if (this.blocksBackgroundInput()) {
            n = Integer.MIN_VALUE;
            n2 = Integer.MIN_VALUE;
        }
        String string = this.showingPremades ? "Back to Profiles" : "Premade profiles";
        ConfigsPage.box(this.premadeBox, f + 7.0f, f2 + 2.0f, f + 7.0f + Math.max(126.0f, PageUI.smallWidth(string) + 32.0f), f2 + 2.0f + 26.0f);
        ConfigsPage.box(this.restoreBox, f3 - 7.0f - Math.max(108.0f, PageUI.smallWidth("Restore deleted") + 32.0f), f2 + 2.0f, f3 - 7.0f, f2 + 2.0f + 26.0f);
        boolean bl = ConfigsPage.inBox(this.premadeBox, n, n2);
        PageUI.button(string, this.premadeBox[0], this.premadeBox[1], this.premadeBox[2], this.premadeBox[3], ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, this.hovers.ease("premadeButton", bl), true);
        if (!this.showingPremades && ConfigsPage.configs().hasDeletedProfile()) {
            boolean bl2 = ConfigsPage.inBox(this.restoreBox, n, n2);
            PageUI.button("Restore deleted", this.restoreBox[0], this.restoreBox[1], this.restoreBox[2], this.restoreBox[3], ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, this.hovers.ease("restoreButton", bl2), true);
        } else {
            ConfigsPage.clearBox(this.restoreBox);
        }
        f2 += 38.0f;
        if (this.showingPremades) {
            this.premades.render(f, f2, f3, f4, n, n2);
            return;
        }
        this.tooltipText = null;
        this.viewportLeft = f;
        this.viewportTop = f2;
        this.viewportRight = f3;
        this.viewportBottom = f4;
        this.layoutList(f, f2, f3);
        float f5 = Math.max(0.0f, this.contentHeight - (f4 - f2));
        this.scroll.clampTarget(-f5, 0.0f);
        float f6 = Math.max(-f5, Math.min(0.0f, this.scroll.getValue()));
        RenderUtils.scissorPushGui(f - 2.0f, f2, f3 - f + 4.0f, f4 - f2);
        for (Cell cell : this.cells) {
            float f7 = Cell.access$000((Cell)cell) + f6;
            float f8 = Cell.access$100((Cell)cell) + f6;
            if (f8 < f2 - 6.0f || f7 > f4 + 6.0f) continue;
            if (Cell.access$200((Cell)cell) == null) {
                this.renderCreateRow(cell, f6, n, n2);
                continue;
            }
            this.renderConfigRow(cell, f6, n, n2);
        }
        this.renderProfileMenu(f6, n, n2);
        RenderUtils.scissorPop();
        if (f6 < -1.0f) {
            PageUI.gradientV(f, f2, f3, f2 + 9.0f, ClickGuiStyle.withAlpha(ClickGuiStyle.PANEL_BACKGROUND, 190), ClickGuiStyle.withAlpha(ClickGuiStyle.PANEL_BACKGROUND, 0));
        }
        if (f6 > -f5 + 1.0f) {
            PageUI.gradientV(f, f4 - 9.0f, f3, f4, ClickGuiStyle.withAlpha(ClickGuiStyle.PANEL_BACKGROUND, 0), ClickGuiStyle.withAlpha(ClickGuiStyle.PANEL_BACKGROUND, 190));
        }
        this.renderTooltip();
        this.renderFeedback();
    }

    @Override
    public boolean blocksBackgroundInput() {
        return this.pendingDeleteName != null || this.closingDeleteName != null || this.renamingProfile != null || this.closingRenameProfile != null;
    }

    @Override
    public float modalOpacity() {
        this.deleteAnim.update();
        this.renameAnim.update();
        return Math.max(this.deleteAnim.getValue(), this.renameAnim.getValue());
    }

    @Override
    public void renderModal(float f, float f2, float f3, float f4, int n, int n2) {
        String string;
        String string2;
        String string3 = string2 = this.pendingDeleteName != null ? this.pendingDeleteName : this.closingDeleteName;
        if (string2 != null) {
            this.renderDeleteModal(f, f2, f3, f4, n, n2, string2, this.deleteAnim.getValue());
            if (this.pendingDeleteName == null && !this.deleteAnim.isAnimating()) {
                this.closingDeleteName = null;
            }
        }
        String string4 = string = this.renamingProfile != null ? this.renamingProfile : this.closingRenameProfile;
        if (string != null) {
            this.renderRenameModal(f, f2, f3, f4, n, n2, string, this.renameAnim.getValue());
            if (this.renamingProfile == null && !this.renameAnim.isAnimating()) {
                this.closingRenameProfile = null;
                this.renameField = null;
            }
        }
    }

    private void layoutList(float f, float f2, float f3) {
        this.cells.clear();
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.add("modal:cancel");
        hashSet.add("modal:delete");
        hashSet.add("rename:cancel");
        hashSet.add("rename:confirm");
        float f4 = f + 7.0f;
        float f5 = f3 - 7.0f;
        List<ClientConfig> list = ConfigsPage.configs().getConfigs();
        int n = list.size() + 1;
        for (int i = 0; i < n; ++i) {
            Cell cell = new Cell(null);
            Cell.access$202((Cell)cell, i < list.size() ? list.get(i) : null);
            Cell.access$402((Cell)cell, (float)f4);
            Cell.access$502((Cell)cell, (float)f5);
            Cell.access$002((Cell)cell, (float)(f2 + (float)i * 44.0f));
            Cell.access$102((Cell)cell, (float)(Cell.access$000((Cell)cell) + 40.0f));
            Cell.access$602((Cell)cell, (String)(Cell.access$200((Cell)cell) == null ? "create" : "config:" + Cell.access$200((Cell)cell).getName()));
            hashSet.add(Cell.access$600((Cell)cell));
            hashSet.add(Cell.access$600((Cell)cell) + ":bind");
            hashSet.add(Cell.access$600((Cell)cell) + ":load");
            hashSet.add(Cell.access$600((Cell)cell) + ":save");
            hashSet.add(Cell.access$600((Cell)cell) + ":folder");
            hashSet.add(Cell.access$600((Cell)cell) + ":more");
            hashSet.add(Cell.access$600((Cell)cell) + ":delete");
            this.cells.add(cell);
        }
        hashSet.add("menu:rename");
        hashSet.add("menu:duplicate");
        this.contentHeight = (float)n * 40.0f + (float)Math.max(0, n - 1) * 4.0f;
        this.hovers.retain(hashSet);
    }

    private void renderConfigRow(Cell cell, float f, int n, int n2) {
        float f2;
        float f3;
        ClientConfig clientConfig = Cell.access$200((Cell)cell);
        float f4 = Cell.access$000((Cell)cell) + f;
        float f5 = Cell.access$100((Cell)cell) + f;
        ConfigsPage.box(Cell.access$700((Cell)cell), Cell.access$400((Cell)cell), f4, Cell.access$500((Cell)cell), f5);
        boolean bl = ConfigsPage.configs().isActive(clientConfig);
        boolean bl2 = PageUI.inside(n, n2, Cell.access$400((Cell)cell), f4, Cell.access$500((Cell)cell), f5) && (float)n2 >= this.viewportTop && (float)n2 <= this.viewportBottom;
        float f6 = this.hovers.ease(Cell.access$600((Cell)cell), bl2);
        int n3 = bl ? ClickGuiStyle.SURFACE_ACTIVE : ClickGuiStyle.SURFACE;
        PageUI.surface(Cell.access$400((Cell)cell), f4, Cell.access$500((Cell)cell), f5, ClickGuiStyle.hovered(n3, f6));
        boolean bl3 = bl && ConfigsPage.configs().isDirty();
        String string = bl3 ? "Unsaved" : clientConfig.getRelativeAge();
        float f7 = PageUI.smallWidth(string);
        float f8 = this.renderIconCluster(cell, f4, f5, n, n2, f6, bl, bl3);
        float f9 = this.renderBindChip(cell, f4, f5, n, n2, f6, f8, Cell.access$500((Cell)cell) - 12.0f - f7 - 14.0f);
        float f10 = 1.0f - f6;
        if (f10 > 0.01f) {
            int n4 = bl3 ? ClickGuiStyle.WARNING : ClickGuiStyle.TEXT_MUTED;
            f3 = Cell.access$500((Cell)cell) - 12.0f - f7;
            f2 = PageUI.smallY(f4, f5);
            PageUI.small(string, f3, f2, ConfigsPage.scaleAlpha(n4, f10));
        }
        float f11 = Cell.access$400((Cell)cell) + 12.0f;
        f3 = f9 - 8.0f;
        f2 = PageUI.labelY(f4, f5);
        String string2 = PageUI.truncateLabel(clientConfig.getName(), Math.max(10.0f, f3 - f11));
        PageUI.label(string2, f11, f2, ClickGuiStyle.TEXT_PRIMARY);
    }

    private float renderBindChip(Cell cell, float f, float f2, int n, int n2, float f3, float f4, float f5) {
        float f6;
        int n3 = Valen.preferences == null ? 0 : Valen.preferences.getProfileBind(Cell.access$200((Cell)cell).getName());
        boolean bl = Cell.access$200((Cell)cell).getName().equals(this.bindingProfile);
        if (n3 <= 0 && !bl && f3 <= 0.01f) {
            ConfigsPage.clearBox(Cell.access$800((Cell)cell));
            return f5;
        }
        String string = bl ? "Press key" : (n3 > 0 ? BindComponent.keyDisplayName(n3) : "Set bind");
        float f7 = PageUI.smallWidth(string) + 10.0f;
        float f8 = f5 + (f4 - 7.0f - f5) * f3;
        float f9 = f8 - f7;
        float f10 = PageUI.chipHeight();
        float f11 = (f + f2 - f10) / 2.0f;
        ConfigsPage.box(Cell.access$800((Cell)cell), f9, f11, f8, f11 + f10);
        boolean bl2 = ConfigsPage.inBox(Cell.access$800((Cell)cell), n, n2);
        String string2 = KeybindConflicts.find(n3, null, null, Cell.access$200((Cell)cell).getName());
        if (bl2 && !string2.isEmpty()) {
            this.setTooltip("Also used by " + string2, n, n2);
        }
        float f12 = this.hovers.ease(Cell.access$600((Cell)cell) + ":bind", bl2);
        float f13 = f6 = bl ? 1.0f : Math.max(f3, f12);
        if (f6 > 0.01f) {
            int n4 = bl ? ClickGuiStyle.ACCENT_SOFT : ClickGuiStyle.hovered(ClickGuiStyle.SURFACE_ACTIVE, f12);
            PageUI.pill(f9, f11, f8, f11 + f10, ConfigsPage.scaleAlpha(n4, f6));
        }
        float f14 = n3 > 0 || bl ? 1.0f : f3;
        PageUI.small(string, f9 + 5.0f, PageUI.smallY(f11, f11 + f10), ConfigsPage.scaleAlpha(!string2.isEmpty() ? ClickGuiStyle.WARNING : (n3 > 0 || bl ? ClickGuiStyle.TEXT_PRIMARY : ClickGuiStyle.TEXT_SECONDARY), f14));
        return f9;
    }

    private float renderIconCluster(Cell cell, float f, float f2, int n, int n2, float f3, boolean bl, boolean bl2) {
        float f4 = (f + f2) / 2.0f;
        float f5 = 24.0f;
        boolean bl3 = !ConfigManager.isDefault(Cell.access$200((Cell)cell));
        float f6 = Cell.access$500((Cell)cell) - 12.0f - 8.0f;
        if (bl3) {
            int n3 = ClickGuiStyle.blend(ClickGuiStyle.TEXT_MUTED, ClickGuiStyle.ERROR, 0.55f);
            this.drawActionIcon(Cell.access$900((Cell)cell), Cell.access$600((Cell)cell) + ":delete", "/assets/valen/textures/gui/icons/delete.png", "Delete", f6, f4, n, n2, n3, ClickGuiStyle.ERROR, f3);
            f6 -= f5;
        } else {
            ConfigsPage.clearBox(Cell.access$900((Cell)cell));
        }
        float f7 = f6;
        float f8 = f6 -= f5;
        float f9 = f8 - f5;
        float f10 = f9 - f5;
        boolean bl4 = bl && bl2;
        this.drawActionIcon(Cell.access$1000((Cell)cell), Cell.access$600((Cell)cell) + ":load", "/assets/valen/textures/gui/icons/load.png", "Load", f10, f4, n, n2, ClickGuiStyle.TEXT_SECONDARY, ClickGuiStyle.ACCENT, f3);
        this.drawActionIcon(Cell.access$1100((Cell)cell), Cell.access$600((Cell)cell) + ":save", "/assets/valen/textures/gui/icons/save.png", "Save", f9, f4, n, n2, bl4 ? ClickGuiStyle.ACCENT : ClickGuiStyle.TEXT_SECONDARY, ClickGuiStyle.ACCENT, f3);
        this.drawActionIcon(Cell.access$1200((Cell)cell), Cell.access$600((Cell)cell) + ":folder", "/assets/valen/textures/gui/icons/folder.png", "Open folder", f8, f4, n, n2, ClickGuiStyle.TEXT_SECONDARY, ClickGuiStyle.ACCENT, f3);
        this.drawActionText(Cell.access$1300((Cell)cell), Cell.access$600((Cell)cell) + ":more", "...", "More", f7, f4, n, n2, ClickGuiStyle.TEXT_SECONDARY, ClickGuiStyle.TEXT_PRIMARY, f3);
        return f10 - 8.0f;
    }

    private void drawActionIcon(float[] fArray, String string, String string2, String string3, float f, float f2, int n, int n2, int n3, int n4, float f3) {
        boolean bl;
        float f4 = 13.0f;
        fArray[0] = f - f4;
        fArray[1] = f2 - f4;
        fArray[2] = f + f4;
        fArray[3] = f2 + f4;
        boolean bl2 = bl = PageUI.inside(n, n2, fArray[0], fArray[1], fArray[2], fArray[3]) && (float)n2 >= this.viewportTop && (float)n2 <= this.viewportBottom;
        if (bl) {
            this.setTooltip(string3, n, n2);
        }
        float f5 = this.hovers.ease(string, bl);
        if (f3 <= 0.01f) {
            return;
        }
        if (f5 > 0.01f) {
            Shape.circle(f, f2, fArray[2] - fArray[0], ClickGuiStyle.withAlpha(ClickGuiStyle.SURFACE_ACTIVE, Math.round(190.0f * f5 * f3)));
        }
        int n5 = ClickGuiStyle.blend(n3, n4, f5);
        RenderUtils.drawIcon(RenderUtils.getIcon(string2), f - 8.0f, f2 - 8.0f, 16, ConfigsPage.scaleAlpha(n5, f3));
    }

    private void drawActionText(float[] fArray, String string, String string2, String string3, float f, float f2, int n, int n2, int n3, int n4, float f3) {
        boolean bl;
        float f4 = 13.0f;
        ConfigsPage.box(fArray, f - f4, f2 - f4, f + f4, f2 + f4);
        boolean bl2 = bl = PageUI.inside(n, n2, fArray[0], fArray[1], fArray[2], fArray[3]) && (float)n2 >= this.viewportTop && (float)n2 <= this.viewportBottom;
        if (bl) {
            this.setTooltip(string3, n, n2);
        }
        float f5 = this.hovers.ease(string, bl);
        if (f3 <= 0.01f) {
            return;
        }
        if (f5 > 0.01f) {
            Shape.circle(f, f2, fArray[2] - fArray[0], ClickGuiStyle.withAlpha(ClickGuiStyle.SURFACE_ACTIVE, Math.round(190.0f * f5 * f3)));
        }
        int n5 = ConfigsPage.scaleAlpha(ClickGuiStyle.blend(n3, n4, f5), f3);
        PageUI.small(string2, f - PageUI.smallWidth(string2) / 2.0f, f2 - PageUI.smallHeight() / 2.0f - 1.0f, n5);
    }

    private void renderProfileMenu(float f, int n, int n2) {
        if (this.menuProfile == null) {
            ConfigsPage.clearBox(this.menuPanelBox);
            ConfigsPage.clearBox(this.menuRenameBox);
            ConfigsPage.clearBox(this.menuDuplicateBox);
            return;
        }
        Cell cell = null;
        for (Cell cell2 : this.cells) {
            if (Cell.access$200((Cell)cell2) == null || !Cell.access$200((Cell)cell2).getName().equalsIgnoreCase(this.menuProfile)) continue;
            cell = cell2;
            break;
        }
        if (cell == null) {
            this.closeMenu();
            return;
        }
        float f2 = Cell.access$000(cell) + f;
        float f3 = Cell.access$100(cell) + f;
        if (f3 < this.viewportTop || f2 > this.viewportBottom) {
            ConfigsPage.clearBox(this.menuPanelBox);
            ConfigsPage.clearBox(this.menuRenameBox);
            ConfigsPage.clearBox(this.menuDuplicateBox);
            return;
        }
        float f4 = 104.0f;
        float f5 = 22.0f;
        float f6 = f5 * 2.0f;
        float f7 = Cell.access$500((Cell)cell) - 12.0f - f4;
        float f8 = f3 + 3.0f;
        if (f8 + f6 > this.viewportBottom - 2.0f) {
            f8 = f2 - f6 - 3.0f;
        }
        float f9 = f7 + f4;
        float f10 = f8 + f6;
        ConfigsPage.box(this.menuPanelBox, f7, f8, f9, f10);
        ConfigsPage.box(this.menuRenameBox, f7, f8, f9, f8 + f5);
        ConfigsPage.box(this.menuDuplicateBox, f7, f8 + f5, f9, f10);
        PageUI.shadow(f7, f8, f9, f10, 1.0f);
        PageUI.surface(f7, f8, f9, f10, ClickGuiStyle.TOOLTIP_BACKGROUND);
        boolean bl = !ConfigManager.isDefault(this.menuProfile);
        boolean bl2 = bl && ConfigsPage.inBox(this.menuRenameBox, n, n2);
        boolean bl3 = ConfigsPage.inBox(this.menuDuplicateBox, n, n2);
        float f11 = this.hovers.ease("menu:rename", bl2);
        float f12 = this.hovers.ease("menu:duplicate", bl3);
        if (f11 > 0.01f) {
            PageUI.pill(f7 + 3.0f, f8 + 3.0f, f9 - 3.0f, f8 + f5, ConfigsPage.scaleAlpha(ClickGuiStyle.SURFACE_ACTIVE, f11));
        }
        if (f12 > 0.01f) {
            PageUI.pill(f7 + 3.0f, f8 + f5, f9 - 3.0f, f10 - 3.0f, ConfigsPage.scaleAlpha(ClickGuiStyle.SURFACE_ACTIVE, f12));
        }
        PageUI.small("Rename", f7 + 12.0f, PageUI.smallY(f8 + 3.0f, f8 + f5), bl ? ClickGuiStyle.TEXT_PRIMARY : ClickGuiStyle.TEXT_DISABLED);
        PageUI.small("Duplicate", f7 + 12.0f, PageUI.smallY(f8 + f5, f10 - 3.0f), ClickGuiStyle.TEXT_PRIMARY);
    }

    private void setTooltip(String string, int n, int n2) {
        this.tooltipText = string;
        this.tooltipMouseX = n;
        this.tooltipMouseY = n2;
    }

    private void renderTooltip() {
        if (this.tooltipText == null || this.pendingDeleteName != null || this.renamingProfile != null) {
            return;
        }
        float f = PageUI.smallWidth(this.tooltipText) + 12.0f;
        float f2 = PageUI.smallHeight() + 7.0f;
        float f3 = Math.max(this.viewportLeft + 2.0f, Math.min(this.viewportRight - f - 2.0f, (float)this.tooltipMouseX - f / 2.0f));
        float f4 = (float)this.tooltipMouseY + 10.0f;
        if (f4 + f2 > this.viewportBottom - 2.0f) {
            f4 = (float)this.tooltipMouseY - f2 - 8.0f;
        }
        PageUI.pill(f3, f4, f3 + f, f4 + f2, ClickGuiStyle.TOOLTIP_BACKGROUND);
        PageUI.small(this.tooltipText, f3 + 6.0f, PageUI.smallY(f4, f4 + f2), ClickGuiStyle.TEXT_PRIMARY);
    }

    private void showFeedback(String string, int n) {
        this.feedbackText = string;
        this.feedbackColor = n;
        this.feedbackUntilMs = System.currentTimeMillis() + 2400L;
    }

    private void renderFeedback() {
        if (this.feedbackText == null || System.currentTimeMillis() >= this.feedbackUntilMs) {
            this.feedbackText = null;
            return;
        }
        float f = Math.max(80.0f, this.viewportRight - this.viewportLeft - 24.0f);
        String string = PageUI.truncateSmall(this.feedbackText, f - 16.0f);
        float f2 = PageUI.smallWidth(string) + 16.0f;
        float f3 = PageUI.smallHeight() + 8.0f;
        float f4 = this.viewportLeft + (this.viewportRight - this.viewportLeft - f2) / 2.0f;
        float f5 = this.viewportBottom - f3 - 8.0f;
        int n = ClickGuiStyle.blend(ClickGuiStyle.TOOLTIP_BACKGROUND, this.feedbackColor, 0.2f);
        PageUI.pill(f4, f5, f4 + f2, f5 + f3, n);
        PageUI.small(string, f4 + 8.0f, PageUI.smallY(f5, f5 + f3), this.feedbackColor);
    }

    private void renderCreateRow(Cell cell, float f, int n, int n2) {
        float f2 = Cell.access$000((Cell)cell) + f;
        float f3 = Cell.access$100((Cell)cell) + f;
        ConfigsPage.box(this.createCellBox, Cell.access$400((Cell)cell), f2, Cell.access$500((Cell)cell), f3);
        boolean bl = !this.creating && PageUI.inside(n, n2, Cell.access$400((Cell)cell), f2, Cell.access$500((Cell)cell), f3) && (float)n2 >= this.viewportTop && (float)n2 <= this.viewportBottom;
        float f4 = this.hovers.ease(Cell.access$600((Cell)cell), bl);
        PageUI.surface(Cell.access$400((Cell)cell), f2, Cell.access$500((Cell)cell), f3, ClickGuiStyle.withAlpha(ClickGuiStyle.SURFACE_ALT, Math.round(70.0f + 55.0f * f4)));
        if (this.creating) {
            float f5 = Cell.access$400((Cell)cell) + 12.0f;
            float f6 = Cell.access$500((Cell)cell) - 12.0f;
            float f7 = (f2 + f3) / 2.0f - 9.0f;
            float f8 = f7 + 18.0f;
            ConfigsPage.box(this.createFieldBox, f5, f7, f6, f8);
            this.createField.tickCursor();
            this.createField.render(f5, f7, f6, f8);
            return;
        }
        this.createFieldBox[2] = this.createFieldBox[0];
        int n3 = ClickGuiStyle.blend(ClickGuiStyle.TEXT_MUTED, ClickGuiStyle.TEXT_SECONDARY, f4);
        PageUI.small("New profile", Cell.access$400((Cell)cell) + 12.0f, PageUI.smallY(f2, f3), n3);
    }

    private void renderDeleteModal(float f, float f2, float f3, float f4, int n, int n2, String string, float f5) {
        float f6 = Math.min(240.0f, f3 - f - 24.0f);
        float f7 = 94.0f;
        float f8 = f + (f3 - f - f6) / 2.0f;
        float f9 = f2 + (f4 - f2 - f7) / 2.0f + (1.0f - f5) * 10.0f;
        float f10 = f8 + f6;
        float f11 = f9 + f7;
        ConfigsPage.box(this.modalPanelBox, f8, f9, f10, f11);
        PageUI.shadow(f8, f9, f10, f11, f5);
        PageUI.surface(f8, f9, f10, f11, ConfigsPage.scaleAlpha(ClickGuiStyle.SURFACE_ALT, f5));
        String string2 = "Delete \"" + PageUI.truncateLabel(string, f6 - 44.0f) + "\"?";
        float f12 = PageUI.labelWidth(string2);
        PageUI.label(string2, f8 + (f6 - f12) / 2.0f, f9 + 15.0f, ConfigsPage.scaleAlpha(ClickGuiStyle.TEXT_PRIMARY, f5));
        String string3 = "You can restore it from Profiles.";
        PageUI.small(string3, f8 + (f6 - PageUI.smallWidth(string3)) / 2.0f, f9 + 15.0f + PageUI.labelHeight() + 5.0f, ConfigsPage.scaleAlpha(ClickGuiStyle.TEXT_MUTED, f5));
        float f13 = 74.0f;
        float f14 = 24.0f;
        float f15 = 8.0f;
        float f16 = f11 - 14.0f - f14;
        float f17 = f13 * 2.0f + f15;
        float f18 = f8 + (f6 - f17) / 2.0f;
        float f19 = f18 + f13;
        float f20 = f19 + f15;
        float f21 = f20 + f13;
        float f22 = f16 + f14;
        ConfigsPage.box(this.modalCancelBox, f18, f16, f19, f22);
        ConfigsPage.box(this.modalDeleteBox, f20, f16, f21, f22);
        boolean bl = PageUI.inside(n, n2, f18, f16, f19, f22);
        boolean bl2 = PageUI.inside(n, n2, f20, f16, f21, f22);
        this.modalButton("Cancel", f18, f16, f19, f22, ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_SECONDARY, this.hovers.ease("modal:cancel", bl), f5);
        this.modalButton("Delete", f20, f16, f21, f22, ClickGuiStyle.ERROR, -1, this.hovers.ease("modal:delete", bl2), f5);
    }

    private void renderRenameModal(float f, float f2, float f3, float f4, int n, int n2, String string, float f5) {
        float f6 = Math.min(260.0f, f3 - f - 24.0f);
        float f7 = 104.0f;
        float f8 = f + (f3 - f - f6) / 2.0f;
        float f9 = f2 + (f4 - f2 - f7) / 2.0f + (1.0f - f5) * 10.0f;
        float f10 = f8 + f6;
        float f11 = f9 + f7;
        ConfigsPage.box(this.renamePanelBox, f8, f9, f10, f11);
        PageUI.shadow(f8, f9, f10, f11, f5);
        PageUI.surface(f8, f9, f10, f11, ConfigsPage.scaleAlpha(ClickGuiStyle.SURFACE_ALT, f5));
        String string2 = "Rename \"" + PageUI.truncateLabel(string, f6 - 54.0f) + "\"";
        PageUI.label(string2, f8 + 14.0f, f9 + 12.0f, ConfigsPage.scaleAlpha(ClickGuiStyle.TEXT_PRIMARY, f5));
        float f12 = f8 + 14.0f;
        float f13 = f9 + 36.0f;
        float f14 = f10 - 14.0f;
        float f15 = f13 + 18.0f;
        ConfigsPage.box(this.renameFieldBox, f12, f13, f14, f15);
        if (this.renameField != null) {
            this.renameField.tickCursor();
            this.renameField.render(f12, f13, f14, f15);
        }
        float f16 = 74.0f;
        float f17 = 24.0f;
        float f18 = 8.0f;
        float f19 = f11 - 14.0f - f17;
        float f20 = f16 * 2.0f + f18;
        float f21 = f8 + (f6 - f20) / 2.0f;
        float f22 = f21 + f16;
        float f23 = f22 + f18;
        float f24 = f23 + f16;
        float f25 = f19 + f17;
        ConfigsPage.box(this.renameCancelBox, f21, f19, f22, f25);
        ConfigsPage.box(this.renameConfirmBox, f23, f19, f24, f25);
        boolean bl = PageUI.inside(n, n2, f21, f19, f22, f25);
        boolean bl2 = PageUI.inside(n, n2, f23, f19, f24, f25);
        this.modalButton("Cancel", f21, f19, f22, f25, ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_SECONDARY, this.hovers.ease("rename:cancel", bl), f5);
        this.modalButton("Rename", f23, f19, f24, f25, ClickGuiStyle.ACCENT_SOFT, ClickGuiStyle.TEXT_PRIMARY, this.hovers.ease("rename:confirm", bl2), f5);
    }

    private void modalButton(String string, float f, float f2, float f3, float f4, int n, int n2, float f5, float f6) {
        int n3 = ClickGuiStyle.blend(n, ClickGuiStyle.hovered(n, 1.0f), f5);
        PageUI.pill(f, f2, f3, f4, ConfigsPage.scaleAlpha(n3, f6));
        PageUI.smallCentred(string, f, f3, f2, f4, ConfigsPage.scaleAlpha(n2, f6));
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (ConfigsPage.configs() == null) {
            return false;
        }
        if (this.closingDeleteName != null || this.closingRenameProfile != null) {
            return true;
        }
        if (!this.blocksBackgroundInput() && this.bindingProfile == null && !this.creating) {
            if (n3 == 0 && ConfigsPage.inBox(this.premadeBox, n, n2)) {
                this.showingPremades = !this.showingPremades;
                this.closeMenu();
                this.premades.onClosed();
                return true;
            }
            if (this.showingPremades) {
                return this.premades.onClick(n, n2, n3);
            }
            if (n3 == 0 && ConfigsPage.configs().hasDeletedProfile() && ConfigsPage.inBox(this.restoreBox, n, n2)) {
                String string = ConfigsPage.configs().restoreDeletedProfile();
                this.showFeedback(string == null ? "Could not restore profile" : "Restored " + string, string == null ? ClickGuiStyle.ERROR : ClickGuiStyle.POSITIVE);
                return true;
            }
        }
        if (this.renamingProfile != null) {
            if (n3 == 0) {
                if (ConfigsPage.inBox(this.renameConfirmBox, n, n2)) {
                    this.commitRename();
                } else if (ConfigsPage.inBox(this.renameCancelBox, n, n2) || !ConfigsPage.inBox(this.renamePanelBox, n, n2)) {
                    this.closeRename();
                } else if (ConfigsPage.inBox(this.renameFieldBox, n, n2) && this.renameField != null) {
                    this.renameField.setFocused(true);
                }
            }
            return true;
        }
        if (this.pendingDeleteName != null) {
            if (n3 == 0) {
                if (ConfigsPage.inBox(this.modalDeleteBox, n, n2)) {
                    String string = this.pendingDeleteName;
                    if (ConfigsPage.configs().delete(string)) {
                        this.showFeedback("Deleted " + string, ClickGuiStyle.POSITIVE);
                    } else {
                        this.showFeedback("Could not delete " + string, ClickGuiStyle.ERROR);
                    }
                    this.closeDelete();
                } else if (ConfigsPage.inBox(this.modalCancelBox, n, n2)) {
                    this.closeDelete();
                } else if (!ConfigsPage.inBox(this.modalPanelBox, n, n2)) {
                    this.closeDelete();
                }
            }
            return true;
        }
        if (this.bindingProfile != null) {
            if (n3 == 0) {
                this.bindingProfile = null;
            } else if (n3 > 0 && n3 <= 16) {
                this.bindProfile(n3 + 1000);
            }
            return true;
        }
        if (this.menuProfile != null && n3 == 0) {
            if (ConfigsPage.inBox(this.menuRenameBox, n, n2) && !ConfigManager.isDefault(this.menuProfile)) {
                this.beginRename(this.menuProfile);
                return true;
            }
            if (ConfigsPage.inBox(this.menuDuplicateBox, n, n2)) {
                this.duplicateProfile(this.menuProfile);
                return true;
            }
            if (ConfigsPage.inBox(this.menuPanelBox, n, n2)) {
                return true;
            }
            this.closeMenu();
        }
        if (n3 != 0) {
            return false;
        }
        if (this.creating) {
            if (ConfigsPage.inBox(this.createFieldBox, n, n2)) {
                return true;
            }
            this.cancelCreate();
            return false;
        }
        if (ConfigsPage.inBox(this.createCellBox, n, n2)) {
            this.closeMenu();
            this.beginCreate();
            return true;
        }
        for (Cell cell : this.cells) {
            if (Cell.access$200((Cell)cell) == null) continue;
            String string = Cell.access$200((Cell)cell).getName();
            if (ConfigsPage.inBox(Cell.access$1300((Cell)cell), n, n2)) {
                this.menuProfile = string.equalsIgnoreCase(this.menuProfile) ? null : string;
                return true;
            }
            if (ConfigsPage.inBox(Cell.access$800((Cell)cell), n, n2)) {
                this.closeMenu();
                this.bindingProfile = string;
                return true;
            }
            if (ConfigsPage.inBox(Cell.access$1000((Cell)cell), n, n2)) {
                this.closeMenu();
                this.loadProfile(string);
                return true;
            }
            if (ConfigsPage.inBox(Cell.access$1100((Cell)cell), n, n2)) {
                this.closeMenu();
                this.saveProfile(string);
                return true;
            }
            if (ConfigsPage.inBox(Cell.access$1200((Cell)cell), n, n2)) {
                this.closeMenu();
                ConfigsPage.configs().openFolder();
                return true;
            }
            if (!ConfigManager.isDefault(string) && ConfigsPage.inBox(Cell.access$900((Cell)cell), n, n2)) {
                this.closeMenu();
                this.openDelete(string);
                return true;
            }
            if (!ConfigsPage.inBox(Cell.access$700((Cell)cell), n, n2)) continue;
            this.closeMenu();
            this.loadProfile(string);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
    }

    @Override
    public boolean keyTyped(char c, int n) {
        if (this.closingDeleteName != null || this.closingRenameProfile != null) {
            return true;
        }
        if (this.showingPremades) {
            if (n == 1) {
                this.showingPremades = false;
                this.premades.onClosed();
            }
            return true;
        }
        if (ConfigsPage.configs() == null) {
            return false;
        }
        if (this.renamingProfile != null) {
            if (n == 28) {
                this.commitRename();
            } else if (n == 1) {
                this.closeRename();
            } else if (this.renameField != null) {
                this.renameField.textboxKeyTyped(c, n);
            }
            return true;
        }
        if (this.pendingDeleteName != null) {
            if (n == 1) {
                this.closeDelete();
            }
            return true;
        }
        if (this.bindingProfile != null) {
            if (n == 1) {
                this.bindingProfile = null;
            } else if (n == 14 || n == 211) {
                this.bindProfile(0);
            } else if (n > 0) {
                this.bindProfile(n);
            }
            return true;
        }
        if (this.creating) {
            if (n == 28) {
                this.commitCreate();
                return true;
            }
            if (n == 1) {
                this.cancelCreate();
                return true;
            }
            this.createField.textboxKeyTyped(c, n);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(int n, int n2, int n3) {
        if (this.blocksBackgroundInput()) {
            return true;
        }
        if (this.showingPremades) {
            return this.premades.onScroll(n, n2, n3);
        }
        if (this.bindingProfile != null && n != 0) {
            this.bindProfile(n > 0 ? 1069 : 1070);
            return true;
        }
        this.closeMenu();
        if (n == 0 || this.cells.isEmpty()) {
            return false;
        }
        if ((float)n3 < this.viewportTop || (float)n3 > this.viewportBottom) {
            return false;
        }
        this.scroll.extend(25.0f * ((float)n / 120.0f));
        return true;
    }

    @Override
    public void onClosed() {
        this.showingPremades = false;
        this.premades.onClosed();
        this.pendingDeleteName = null;
        this.closingDeleteName = null;
        this.deleteAnim.snap(false);
        this.cancelCreate();
        this.bindingProfile = null;
        this.closeMenu();
        this.renamingProfile = null;
        this.closingRenameProfile = null;
        this.renameField = null;
        this.renameAnim.snap(false);
        this.feedbackText = null;
        this.scroll.reset(0.0f);
    }

    private void openDelete(String string) {
        this.closeMenu();
        this.scroll.reset(this.scroll.getValue());
        this.pendingDeleteName = string;
        this.closingDeleteName = null;
        this.deleteAnim.snap(false);
        this.deleteAnim.setTarget(true);
    }

    private void closeDelete() {
        if (this.pendingDeleteName == null) {
            return;
        }
        this.closingDeleteName = this.pendingDeleteName;
        this.pendingDeleteName = null;
        this.deleteAnim.setTarget(false);
    }

    private void beginCreate() {
        this.closeMenu();
        this.creating = true;
        this.createField = new ClickGuiTextField("Profile name", 24);
        this.createField.setFocused(true);
    }

    private void cancelCreate() {
        this.creating = false;
        this.createField = null;
    }

    private void commitCreate() {
        String string = ConfigManager.normalizeName(this.createField.getText());
        if (string == null) {
            this.showFeedback("Enter a valid profile name", ClickGuiStyle.ERROR);
            return;
        }
        if (ConfigsPage.configs().exists(string)) {
            this.showFeedback("That profile already exists", ClickGuiStyle.ERROR);
            return;
        }
        if (ConfigsPage.configs().save(string)) {
            this.showFeedback("Created " + string, ClickGuiStyle.POSITIVE);
            this.cancelCreate();
        } else {
            this.showFeedback("Could not create " + string, ClickGuiStyle.ERROR);
        }
    }

    private void loadProfile(String string) {
        if (ConfigsPage.configs().load(string)) {
            this.showFeedback("Loaded " + string, ClickGuiStyle.POSITIVE);
        } else {
            this.showFeedback("Could not load " + string, ClickGuiStyle.ERROR);
        }
    }

    private void saveProfile(String string) {
        if (ConfigsPage.configs().save(string)) {
            this.showFeedback("Saved " + string, ClickGuiStyle.POSITIVE);
        } else {
            this.showFeedback("Could not save " + string, ClickGuiStyle.ERROR);
        }
    }

    private void duplicateProfile(String string) {
        this.closeMenu();
        String string2 = ConfigsPage.configs().duplicate(string);
        if (string2 == null) {
            this.showFeedback("Could not duplicate " + string, ClickGuiStyle.ERROR);
        } else {
            this.showFeedback("Created " + string2, ClickGuiStyle.POSITIVE);
        }
    }

    private void beginRename(String string) {
        this.closeMenu();
        this.scroll.reset(this.scroll.getValue());
        this.renamingProfile = string;
        this.closingRenameProfile = null;
        this.renameField = new ClickGuiTextField("Profile name", 24);
        this.renameField.setText(string);
        this.renameField.setFocused(true);
        this.renameAnim.snap(false);
        this.renameAnim.setTarget(true);
    }

    private void commitRename() {
        if (this.renamingProfile == null || this.renameField == null) {
            return;
        }
        String string = this.renamingProfile;
        String string2 = ConfigManager.normalizeName(this.renameField.getText());
        if (string2 == null) {
            this.showFeedback("Enter a valid profile name", ClickGuiStyle.ERROR);
            return;
        }
        if (!string.equalsIgnoreCase(string2) && ConfigsPage.configs().exists(string2)) {
            this.showFeedback("That profile already exists", ClickGuiStyle.ERROR);
            return;
        }
        if (ConfigsPage.configs().rename(string, string2)) {
            this.showFeedback(string.equalsIgnoreCase(string2) ? "Name unchanged" : "Renamed to " + string2, ClickGuiStyle.POSITIVE);
            this.closeRename();
        } else {
            this.showFeedback("Could not rename " + string, ClickGuiStyle.ERROR);
        }
    }

    private void closeRename() {
        if (this.renamingProfile == null) {
            return;
        }
        this.closingRenameProfile = this.renamingProfile;
        this.renamingProfile = null;
        this.renameAnim.setTarget(false);
    }

    private void closeMenu() {
        this.menuProfile = null;
        ConfigsPage.clearBox(this.menuPanelBox);
        ConfigsPage.clearBox(this.menuRenameBox);
        ConfigsPage.clearBox(this.menuDuplicateBox);
    }

    private void bindProfile(int n) {
        String string = this.bindingProfile;
        this.bindingProfile = null;
        if (string == null || Valen.preferences == null) {
            this.showFeedback("Could not update profile bind", ClickGuiStyle.ERROR);
            return;
        }
        String string2 = Valen.preferences.setProfileBind(string, n);
        if (n <= 0) {
            this.showFeedback("Cleared bind for " + string, ClickGuiStyle.POSITIVE);
            return;
        }
        String string3 = BindComponent.keyDisplayName(n);
        if (string2 == null) {
            String string4 = KeybindConflicts.find(n, null, null, string);
            this.showFeedback(string4.isEmpty() ? "Bound " + string3 + " to " + string : "Also used by " + string4, string4.isEmpty() ? ClickGuiStyle.POSITIVE : ClickGuiStyle.WARNING);
            return;
        }
        ClientConfig clientConfig = ConfigsPage.configs().find(string2);
        String string5 = clientConfig == null ? string2 : clientConfig.getName();
        this.showFeedback("Bound " + string3 + "; cleared " + string5, ClickGuiStyle.WARNING);
    }

    private static int scaleAlpha(int n, float f) {
        int n2 = n >>> 24 & 0xFF;
        return ClickGuiStyle.withAlpha(n, Math.round((float)n2 * Math.max(0.0f, Math.min(1.0f, f))));
    }

    private static void box(float[] fArray, float f, float f2, float f3, float f4) {
        fArray[0] = f;
        fArray[1] = f2;
        fArray[2] = f3;
        fArray[3] = f4;
    }

    private static boolean inBox(float[] fArray, int n, int n2) {
        return fArray[2] > fArray[0] && PageUI.inside(n, n2, fArray[0], fArray[1], fArray[2], fArray[3]);
    }

    private static void clearBox(float[] fArray) {
        fArray[0] = 0.0f;
        fArray[1] = 0.0f;
        fArray[2] = 0.0f;
        fArray[3] = 0.0f;
    }
}

