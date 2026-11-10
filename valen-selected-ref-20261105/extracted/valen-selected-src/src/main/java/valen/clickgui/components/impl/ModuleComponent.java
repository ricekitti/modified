/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 *  valen.clickgui.components.impl.ModuleComponent$1
 *  valen.clickgui.components.impl.PotionSearchComponent
 *  valen.module.setting.impl.BlockEspSetting$Entry
 *  valen.module.setting.impl.TextSetting
 */
package valen.clickgui.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.RevealAnimation;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.BindComponent;
import valen.clickgui.components.impl.BlockSearchComponent;
import valen.clickgui.components.impl.ButtonComponent;
import valen.clickgui.components.impl.CategoryComponent;
import valen.clickgui.components.impl.ColorComponent;
import valen.clickgui.components.impl.DescriptionComponent;
import valen.clickgui.components.impl.GroupComponent;
import valen.clickgui.components.impl.InventoryItemSearchComponent;
import valen.clickgui.components.impl.ItemSearchComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.components.impl.MultiSelectComponent;
import valen.clickgui.components.impl.PlayerListComponent;
import valen.clickgui.components.impl.PotionSearchComponent;
import valen.clickgui.components.impl.SliderComponent;
import valen.clickgui.components.impl.StringListComponent;
import valen.clickgui.components.impl.TextFieldComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.Shape;
import valen.module.Module;
import valen.module.impl.client.Gui;
import valen.module.setting.Setting;
import valen.module.setting.impl.BlockEspSetting;
import valen.module.setting.impl.BlockListSetting;
import valen.module.setting.impl.ButtonSetting;
import valen.module.setting.impl.ColorSetting;
import valen.module.setting.impl.DescriptionSetting;
import valen.module.setting.impl.GroupSetting;
import valen.module.setting.impl.InventoryItemListSetting;
import valen.module.setting.impl.ItemListSetting;
import valen.module.setting.impl.KeySetting;
import valen.module.setting.impl.MultiSelectSetting;
import valen.module.setting.impl.PlayerListSetting;
import valen.module.setting.impl.PotionListSetting;
import valen.module.setting.impl.SliderSetting;
import valen.module.setting.impl.StringListSetting;
import valen.module.setting.impl.TextSetting;
import valen.utility.RenderUtils;
import valen.utility.font.ValenFontRenderer;

public class ModuleComponent
extends Component {
    public Module mod;
    public CategoryComponent categoryComponent;
    public float yPos;
    public ArrayList<Component> settings;
    public boolean isOpened;
    private boolean hovering;
    private boolean bindHover;
    private final ToggleAnimation bindHoverAnim = new ToggleAnimation(130L);
    private final ToggleAnimation hoverAnim = new ToggleAnimation(130L);
    private final ToggleAnimation toggleAnim = new ToggleAnimation(140L);
    private final RevealAnimation revealAnim = new RevealAnimation(240.0f);
    private float smoothingY = 16.0f;
    private float edgeFade = 1.0f;
    private static final float GROUP_CHILD_INDENT = 6.0f;
    private static final float MODULE_ROW_HEIGHT = 50.0f;
    private static final float MODULE_CARD_HEIGHT = 30.0f;
    private static final float SETTING_ROW_HEIGHT = 20.0f;
    private static final float CARD_INSET = 7.0f;
    private static final float DROPDOWN_ANIMATION_MS = 240.0f;
    private static final float DROPDOWN_SLIDE = 13.0f;
    private final Map<Component, GroupComponent> owningGroups = new IdentityHashMap<Component, GroupComponent>();
    private final Map<String, GroupComponent> groupsByName = new HashMap<String, GroupComponent>();
    private BindComponent moduleBind;
    private static final float EDGE_FADE_CUTOFF = 0.01f;
    public boolean overlayHosted;

    public ModuleComponent(Module module, CategoryComponent categoryComponent, float f) {
        this.mod = module;
        this.categoryComponent = categoryComponent;
        this.yPos = f;
        this.settings = new ArrayList();
        this.isOpened = false;
        this.smoothingY = this.getCollapsedHeight();
        this.revealAnim.snap(this.isOpened);
        this.rebuildSettingsList();
    }

    private void rebuildSettingsList() {
        this.settings = new ArrayList();
        this.moduleBind = null;
        float f = this.yPos + this.getSettingStartOffset();
        if (this.mod != null && !this.mod.getSettings().isEmpty()) {
            ArrayList<Setting> arrayList = this.mod.getSettings();
            for (int i = 0; i < arrayList.size(); ++i) {
                Component component;
                Setting setting;
                Setting setting2 = (Setting)arrayList.get(i);
                if (!setting2.visible) continue;
                if (setting2 instanceof SliderSetting) {
                    setting = (SliderSetting)setting2;
                    component = new SliderComponent((SliderSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof MultiSelectSetting) {
                    this.settings.add(new MultiSelectComponent((MultiSelectSetting)setting2, this, f));
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof ButtonSetting) {
                    setting = (ButtonSetting)setting2;
                    component = new ButtonComponent(this.mod, (ButtonSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof DescriptionSetting) {
                    setting = (DescriptionSetting)setting2;
                    component = new DescriptionComponent((DescriptionSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof KeySetting) {
                    setting = (KeySetting)setting2;
                    component = new BindComponent(this, (KeySetting)setting, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof GroupSetting) {
                    setting = (GroupSetting)setting2;
                    component = new GroupComponent((GroupSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof ColorSetting) {
                    setting = (ColorSetting)setting2;
                    component = new ColorComponent((ColorSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof PotionListSetting) {
                    setting = (PotionListSetting)setting2;
                    component = new PotionSearchComponent((PotionListSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof InventoryItemListSetting) {
                    setting = (InventoryItemListSetting)setting2;
                    component = new InventoryItemSearchComponent((InventoryItemListSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof ItemListSetting) {
                    setting = (ItemListSetting)setting2;
                    component = new ItemSearchComponent((ItemListSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    continue;
                }
                if (setting2 instanceof PlayerListSetting) {
                    setting = (PlayerListSetting)setting2;
                    component = new PlayerListComponent((PlayerListSetting)setting, this, f);
                    this.settings.add(component);
                    f += ((PlayerListComponent)component).getHeightF();
                    continue;
                }
                if (setting2 instanceof StringListSetting) {
                    setting = (StringListSetting)setting2;
                    component = new StringListComponent((StringListSetting)setting, this, f);
                    this.settings.add(component);
                    f += ((StringListComponent)component).getHeightF();
                    continue;
                }
                if (setting2 instanceof BlockListSetting) {
                    setting = (BlockListSetting)setting2;
                    component = new BlockSearchComponent((BlockListSetting)setting, this, f);
                    this.settings.add(component);
                    f += 12.0f;
                    if (!(setting instanceof BlockEspSetting)) continue;
                    BlockEspSetting blockEspSetting = (BlockEspSetting)setting;
                    for (String string : blockEspSetting.getBlocks()) {
                        BlockEspSetting.Entry entry = blockEspSetting.entry(string);
                        this.settings.add(new GroupComponent(entry.group, this, f));
                        this.settings.add(new ColorComponent(entry.color, this, f));
                        this.settings.add(new ButtonComponent(this.mod, entry.outline, this, f));
                        this.settings.add(new SliderComponent(entry.range, this, f));
                        ButtonSetting buttonSetting = new ButtonSetting("Remove", () -> {
                            blockEspSetting.removeBlock(string);
                            this.reloadSettings();
                            if (Valen.configManager != null) {
                                Valen.configManager.invalidateDirtyCache();
                            }
                        });
                        buttonSetting.group = entry.group;
                        this.settings.add(new ButtonComponent(this.mod, buttonSetting, this, f));
                    }
                    continue;
                }
                if (!(setting2 instanceof TextSetting)) continue;
                setting = (TextSetting)setting2;
                component = new TextFieldComponent((TextSetting)setting, this, f);
                this.settings.add(component);
                f += ((AbstractTextInputComponent)component).getHeightF();
            }
        }
        if (this.mod.canBeEnabled()) {
            this.moduleBind = new BindComponent(this, f);
            this.settings.add(this.moduleBind);
        }
        this.rebuildGroupOwnershipCache();
    }

    public void reloadSettings() {
        Component component;
        boolean bl = this.isOpened;
        HashMap<SliderSetting, Boolean> hashMap = new HashMap<SliderSetting, Boolean>();
        HashMap<ColorSetting, Boolean> hashMap2 = new HashMap<ColorSetting, Boolean>();
        for (Component component2 : this.settings) {
            if (component2 instanceof SliderComponent) {
                component = (SliderComponent)component2;
                hashMap.put(((SliderComponent)component).sliderSetting, ((SliderComponent)component).heldDown);
                continue;
            }
            if (!(component2 instanceof ColorComponent)) continue;
            component = (ColorComponent)component2;
            hashMap2.put(((ColorComponent)component).colorSetting, ((ColorComponent)component).expanded);
        }
        this.rebuildSettingsList();
        for (Component component2 : this.settings) {
            Boolean bl2;
            if (component2 instanceof SliderComponent) {
                component = (SliderComponent)component2;
                bl2 = (Boolean)hashMap.get(((SliderComponent)component).sliderSetting);
                if (bl2 == null) continue;
                ((SliderComponent)component).heldDown = bl2;
                continue;
            }
            if (!(component2 instanceof ColorComponent)) continue;
            component = (ColorComponent)component2;
            bl2 = (Boolean)hashMap2.get(((ColorComponent)component).colorSetting);
            if (bl2 == null) continue;
            ((ColorComponent)component).restoreExpandedState(bl2);
        }
        this.restoreOpenState(bl);
        this.updateSettingPositions();
    }

    public void toggleDropdown() {
        if (this.settings.isEmpty()) {
            return;
        }
        this.isOpened = !this.isOpened;
        this.updateSettingPositions();
        this.revealAnim.setTarget(this.isOpened);
    }

    public void restoreOpenState(boolean bl) {
        this.isOpened = bl;
        this.revealAnim.snap(this.isOpened);
        this.smoothingY = this.isOpened ? this.expandedHeightF() : this.getCollapsedHeight();
    }

    public void updateAnimationState() {
        this.revealAnim.update();
        float f = this.getCollapsedHeight();
        this.smoothingY = f + (this.expandedHeightF() - f) * this.revealAnim.getValue();
    }

    @Override
    public void updateHeight(float f) {
        this.yPos = f;
        float f2 = this.yPos + this.getCollapsedHeight();
        int n = 0;
        while (n < this.settings.size()) {
            float f3;
            Component component = this.settings.get(n);
            if (!this.isVisibleBase(component)) {
                ++n;
                continue;
            }
            component.updateHeight(f2);
            float f4 = f3 = this.getOwningGroup(component) != null ? 6.0f : 0.0f;
            if (component instanceof SliderComponent) {
                ((SliderComponent)component).xOffset = f3;
            } else if (component instanceof ButtonComponent) {
                ((ButtonComponent)component).xOffset = f3;
            } else if (component instanceof BindComponent && ((BindComponent)component).keySetting != null) {
                ((BindComponent)component).xOffset = f3;
            } else if (component instanceof ColorComponent) {
                ((ColorComponent)component).xOffset = f3;
            } else if (component instanceof AbstractTextInputComponent) {
                ((AbstractTextInputComponent)component).setXOffset(f3);
            }
            f2 += this.getBaseComponentHeightF(component);
            ++n;
        }
    }

    public void setEdgeFade(float f) {
        this.edgeFade = Math.max(0.0f, Math.min(1.0f, f));
    }

    private int fade(int n) {
        if (this.edgeFade >= 1.0f) {
            return n;
        }
        int n2 = n >>> 24 & 0xFF;
        return ClickGuiStyle.withAlpha(n, Math.max(0, Math.round((float)n2 * this.edgeFade)));
    }

    @Override
    public void render() {
        float f;
        if (this.edgeFade <= 0.01f) {
            return;
        }
        boolean bl = this.revealAnim.isAnimating() && !this.overlayHosted;
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiHeaderFontRenderer();
        boolean bl2 = false;
        if (this.hasModuleHeader()) {
            f = 30.0f;
            float f2 = this.categoryComponent.getX() + 7.0f;
            float f3 = this.categoryComponent.getY() + this.yPos + 2.0f;
            float f4 = this.categoryComponent.getX() + this.categoryComponent.getWidth() - 7.0f;
            float f5 = f3 + f;
            float f6 = this.getHeightF();
            float f7 = Math.max(f5, this.categoryComponent.getY() + this.yPos + f6 - 2.0f);
            this.hoverAnim.setTarget(this.hovering);
            float f8 = this.getHeaderCardHeight();
            this.toggleAnim.setTarget(this.mod.isEnabled());
            float f9 = this.toggleAnim.getValue();
            int n = ClickGuiStyle.hovered(ClickGuiStyle.blend(ClickGuiStyle.MODULE_PILL, ClickGuiStyle.ENABLED_MODULE_PILL, f9), this.hoverAnim.getValue());
            Shape.surface(f2, f3, f4, f7, this.fade(n));
            float f10 = f2 + 12.0f;
            float f11 = PageUI.textY(valenFontRenderer, 1.0f, f3, f3 + f8);
            float f12 = f4 - 26.0f - 14.0f;
            float f13 = f3 + (f8 - 13.0f) / 2.0f;
            if (this.mod.canBeEnabled()) {
                Controls.toggle(f12, f13, f9, this.edgeFade);
            } else if (this.hasSettings()) {
                Controls.chevron(f4 - 20.0f, f3 + f8 / 2.0f, 7.0f, -90.0f, this.fade(ClickGuiStyle.TEXT_MUTED));
            }
            float f14 = f4 - 34.0f;
            if (this.moduleBind != null) {
                float f15;
                float f16 = f12 - 12.0f;
                float f17 = f16 - this.headerBindWidth();
                float f18 = f3 + (f8 - 22.0f) / 2.0f;
                this.bindHoverAnim.setTarget(this.bindHover);
                float f19 = f15 = this.moduleBind.isBinding ? 1.0f : this.bindHoverAnim.getValue();
                if (f15 > 0.01f) {
                    Shape.capsule(f17, f18 + 3.0f, f16, f18 + 19.0f, this.fade(ClickGuiStyle.withAlpha(ClickGuiStyle.INPUT_BACKGROUND, Math.round(255.0f * f15))));
                }
                PageUI.smallCentred(this.headerBindText(), f17, f16, f18, f18 + 22.0f, this.fade(this.moduleBind.isBinding ? ClickGuiStyle.ACCENT : (this.mod.getKeycode() == 0 ? ClickGuiStyle.TEXT_MUTED : ClickGuiStyle.TEXT_SECONDARY)));
                f14 = f17 - 12.0f;
            }
            valenFontRenderer.drawString(PageUI.truncateLabel(this.mod.getName(), f14 - f10), f10, f11, this.fade(ClickGuiStyle.moduleText(f9, bl2, false)), false);
        } else {
            f = this.getHeightF();
            if (f > 8.0f) {
                float f20 = this.categoryComponent.getX() + 7.0f;
                float f21 = this.categoryComponent.getX() + this.categoryComponent.getWidth() - 7.0f;
                float f22 = this.categoryComponent.getY() + this.yPos + 2.0f;
                Shape.surface(f20, f22, f21, this.categoryComponent.getY() + this.yPos + f - 2.0f, this.fade(ClickGuiStyle.surface(false)));
            }
        }
        if (bl) {
            f = this.categoryComponent.getModuleY() - this.categoryComponent.getY();
            RenderUtils.scissorPushGui(this.categoryComponent.getX() - 2.0f, this.categoryComponent.getY() + this.yPos + f, this.categoryComponent.getWidth() + 4.0f, this.smoothingY);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)0.0f, (float)(-13.0f * (1.0f - this.revealAnim.getContentValue())), (float)0.0f);
        }
        if ((this.isOpened || bl) && !this.overlayHosted) {
            this.renderSettings();
        }
        if (bl) {
            GlStateManager.popMatrix();
            RenderUtils.scissorPop();
        }
    }

    @Override
    public float getHeightF() {
        if (this.overlayHosted) {
            return this.getCollapsedHeight();
        }
        if (this.revealAnim.isAnimating()) {
            return this.smoothingY;
        }
        if (!this.isOpened) {
            return this.getCollapsedHeight();
        }
        return this.expandedHeightF();
    }

    private float expandedHeightF() {
        float f = this.getCollapsedHeight();
        for (Component component : this.settings) {
            f += this.getAnimatedComponentHeightF(component);
        }
        return f;
    }

    @Override
    public int getHeight() {
        return Math.round(this.getHeightF());
    }

    public void onSliderChange() {
        for (Component component : this.settings) {
            if (!(component instanceof SliderComponent)) continue;
            ((SliderComponent)component).onSliderChange();
        }
    }

    public float getScrollExtentHeightF() {
        if (this.overlayHosted) {
            return this.getCollapsedHeight();
        }
        if (this.isOpened || this.revealAnim.isAnimating()) {
            float f = this.getCollapsedHeight();
            for (Component component : this.settings) {
                if (!this.isVisibleBase(component)) continue;
                f += this.getBaseComponentHeightF(component);
            }
            return f;
        }
        return this.getHeightF();
    }

    @Override
    public void drawScreen(int n, int n2) {
        for (Component component : this.settings) {
            component.drawScreen(n, n2);
        }
        this.bindHover = this.overHeaderBind(n, n2);
        this.hovering = this.hasModuleHeader() && this.overModuleName(n, n2) && this.categoryComponent.opened;
    }

    public boolean hasSettings() {
        return !this.settings.isEmpty();
    }

    public void layoutSettingsAt(float f) {
        this.updateHeight(f - this.categoryComponent.getY() - this.getCollapsedHeight());
    }

    public float getSettingsHeightF() {
        float f = 0.0f;
        for (Component component : this.settings) {
            if (!this.isVisibleBase(component)) continue;
            f += this.getBaseComponentHeightF(component);
        }
        return f;
    }

    public void renderSettingsOnly() {
        this.renderSettings();
    }

    public boolean onSettingClick(int n, int n2, int n3) {
        for (Component component : this.settings) {
            if (!component.onClick(n, n2, n3)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean onClick(int n, int n2, int n3) {
        if (n3 == 0 && this.overHeaderBind(n, n2)) {
            this.moduleBind.isBinding = true;
            return true;
        }
        if (this.hasModuleHeader() && this.overModuleName(n, n2) && n3 == 0 && this.mod.canBeEnabled()) {
            this.mod.toggle();
            return true;
        }
        for (Component component : this.settings) {
            if (!component.onClick(n, n2, n3)) continue;
            return true;
        }
        return false;
    }

    private String headerBindText() {
        return this.moduleBind.isBinding ? "..." : (this.mod.getKeycode() == 0 ? "\u2014" : BindComponent.keyDisplayName(this.mod.getKeycode()));
    }

    private float headerBindWidth() {
        return Math.max(24.0f, PageUI.smallWidth(this.headerBindText()) + 12.0f);
    }

    public boolean overHeaderBind(int n, int n2) {
        if (this.moduleBind == null || this.overlayHosted || !this.categoryComponent.opened) {
            return false;
        }
        float f = this.categoryComponent.getX() + this.categoryComponent.getWidth() - 7.0f - 52.0f;
        float f2 = this.categoryComponent.getModuleY() + this.yPos + 2.0f + (this.getHeaderCardHeight() - 22.0f) / 2.0f;
        return (float)n >= f - this.headerBindWidth() && (float)n <= f && (float)n2 >= f2 && (float)n2 <= f2 + 22.0f;
    }

    @Override
    public void mouseReleased(int n, int n2, int n3) {
        for (Component component : this.settings) {
            component.mouseReleased(n, n2, n3);
        }
    }

    @Override
    public void keyTyped(char c, int n) {
        for (Component component : this.settings) {
            component.keyTyped(c, n);
        }
    }

    @Override
    public void onScroll(int n) {
        for (Component component : this.settings) {
            component.onScroll(n);
        }
    }

    @Override
    public void onGuiClosed() {
        for (Component component : this.settings) {
            component.onGuiClosed();
        }
        this.hovering = false;
        this.revealAnim.snap(this.isOpened);
        this.smoothingY = this.isOpened ? this.expandedHeightF() : this.getCollapsedHeight();
    }

    public boolean overModuleName(int n, int n2) {
        if (!this.hasModuleHeader()) {
            return false;
        }
        float f = this.categoryComponent.getX() + 7.0f;
        float f2 = this.categoryComponent.getX() + this.categoryComponent.getWidth() - 7.0f;
        float f3 = this.categoryComponent.getModuleY() + this.yPos + 2.0f;
        float f4 = f3 + this.getHeaderCardHeight();
        return (float)n >= f && (float)n <= f2 && (float)n2 >= f3 && (float)n2 <= f4;
    }

    public void updateSettingPositions() {
        this.categoryComponent.updateHeight();
    }

    public boolean isVisible(Component component) {
        return this.isVisibleBase(component);
    }

    private GroupComponent getOwningGroup(Component component) {
        return this.owningGroups.get(component);
    }

    private String getGroupName(Component component) {
        if (component instanceof SliderComponent && ((SliderComponent)component).sliderSetting.groupSetting != null) {
            return ((SliderComponent)component).sliderSetting.groupSetting.getName();
        }
        if (component instanceof ButtonComponent && ((ButtonComponent)component).buttonSetting.group != null) {
            return ((ButtonComponent)component).buttonSetting.group.getName();
        }
        if (component instanceof BindComponent && ((BindComponent)component).keySetting != null && ((BindComponent)component).keySetting.group != null) {
            return ((BindComponent)component).keySetting.group.getName();
        }
        if (component instanceof ColorComponent && ((ColorComponent)component).colorSetting.groupSetting != null) {
            return ((ColorComponent)component).colorSetting.groupSetting.getName();
        }
        if (component instanceof AbstractTextInputComponent) {
            return ((AbstractTextInputComponent)component).getGroupName();
        }
        return "";
    }

    private void rebuildGroupOwnershipCache() {
        Object object;
        this.owningGroups.clear();
        this.groupsByName.clear();
        for (Component component : this.settings) {
            if (!(component instanceof GroupComponent)) continue;
            object = (GroupComponent)component;
            this.groupsByName.put(((GroupComponent)object).setting.getName(), (GroupComponent)object);
        }
        for (Component component : this.settings) {
            GroupComponent groupComponent;
            object = this.getGroupName(component);
            if (((String)object).isEmpty() || (groupComponent = this.groupsByName.get(object)) == null) continue;
            this.owningGroups.put(component, groupComponent);
        }
    }

    private float getBaseComponentHeightF(Component component) {
        if (component instanceof SliderComponent) {
            return component.getHeightF();
        }
        if (component instanceof ColorComponent) {
            ColorComponent colorComponent = (ColorComponent)component;
            float f = colorComponent.getAnimationProgress();
            return 20.0f + (colorComponent.getExpandedHeight() - 20.0f) * f;
        }
        if (component instanceof AbstractSearchListComponent || component instanceof TextFieldComponent || component instanceof PlayerListComponent || component instanceof StringListComponent || component instanceof DescriptionComponent || component instanceof MultiSelectComponent) {
            return component.getHeightF();
        }
        if (component instanceof BindComponent) {
            return ((BindComponent)component).keySetting == null ? 20.0f : 20.0f;
        }
        return 20.0f;
    }

    private float getAnimatedComponentHeightF(Component component) {
        if (!this.isVisibleBase(component)) {
            return 0.0f;
        }
        return this.getBaseComponentHeightF(component);
    }

    private void renderSettings() {
        for (Component component : this.settings) {
            if (!this.isVisibleBase(component)) continue;
            component.render();
        }
    }

    private boolean isVisibleBase(Component component) {
        return component.isBaseVisible();
    }

    private boolean hasModuleHeader() {
        return true;
    }

    public boolean matchesSearch(String string) {
        if (!this.hasModuleHeader() || this.mod == null) {
            return false;
        }
        String string2 = ModuleComponent.normalizeSearch(string);
        if (string2.isEmpty()) {
            return true;
        }
        String string3 = ModuleComponent.normalizeSearch(this.mod.getName());
        if (string3.contains(string2) || ModuleComponent.expandedModuleName(string3).contains(string2) || ModuleComponent.searchableCategory(this.mod.moduleCategory()).contains(string2)) {
            return true;
        }
        for (Setting setting : this.mod.getSettings()) {
            if (!ModuleComponent.normalizeSearch(setting.getName()).contains(string2) && !ModuleComponent.normalizeSearch(setting.getProfileKey()).contains(string2)) continue;
            return true;
        }
        return false;
    }

    static String normalizeSearch(String string) {
        if (string == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(string.length());
        String string2 = string.toLowerCase(Locale.ROOT);
        for (int i = 0; i < string2.length(); ++i) {
            char c = string2.charAt(i);
            if (!Character.isLetterOrDigit(c)) continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private static String expandedModuleName(String string) {
        return string.startsWith("inv") && string.length() > 3 ? "inventory" + string.substring(3) : string;
    }

    private static String searchableCategory(Module.category category2) {
        if (category2 == null) {
            return "";
        }
        switch (1.$SwitchMap$valen$module$Module$category[category2.ordinal()]) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: {
                return "utility" + category2.name();
            }
        }
        return ModuleComponent.normalizeSearch(category2.name());
    }

    private float getHeaderCardHeight() {
        return this.getCollapsedHeight() - 4.0f;
    }

    private float getCollapsedHeight() {
        if (!this.hasModuleHeader()) {
            return 8.0f;
        }
        return 44.0f;
    }

    private float getSettingStartOffset() {
        return this.hasModuleHeader() ? this.getCollapsedHeight() - 2.0f : 0.0f;
    }
}

