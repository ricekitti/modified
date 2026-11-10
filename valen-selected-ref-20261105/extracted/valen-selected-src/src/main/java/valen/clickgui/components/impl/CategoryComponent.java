/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.renderer.GlStateManager
 *  org.lwjgl.opengl.GL11
 */
package valen.clickgui.components.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.components.Component;
import valen.clickgui.components.impl.AbstractSearchListComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.components.impl.PlayerListComponent;
import valen.clickgui.components.impl.StringListComponent;
import valen.clickgui.render.Shape;
import valen.lunar.render.LunarPostProcessing;
import valen.module.Module;
import valen.module.impl.client.Gui;
import valen.utility.RenderUtils;
import valen.utility.Timer;
import valen.utility.Utils;
import valen.utility.font.ValenFontRenderer;

public class CategoryComponent {
    private static final float DEFAULT_WIDTH = 138.0f;
    private static final float DEFAULT_TITLE_HEIGHT = 20.0f;
    private static final float REFERENCE_TITLE_HEIGHT = 28.0f;
    private static final float CONTENT_INSET = 8.0f;
    private static long interactionSequence;
    public List<ModuleComponent> modules = new CopyOnWriteArrayList<ModuleComponent>();
    private String moduleFilter = "";
    private List<ModuleComponent> visibleModules;
    private int structureVersion;
    public Module.category category;
    public boolean opened;
    public float width;
    public float y;
    public float x;
    public float titleHeight;
    public boolean dragging;
    public boolean referenceLayout = false;
    public float maxContentHeight = -1.0f;
    public float xx;
    public float yy;
    public boolean hovering = false;
    public boolean hoveringOverCategory = false;
    public Timer smoothTimer;
    private Timer textTimer;
    public float big;
    private boolean referenceTitleVisible = true;
    private float lastHeight;
    private float lastNamePos;
    private float animationStartNamePos;
    public float moduleY;
    private float screenHeight;
    private float screenWidth;
    private float animationStartHeight;
    private final ScrollOffsetAnimation scrollAnim = new ScrollOffsetAnimation(190L);
    public long lastInteractedTime = 0L;
    private static final float EDGE_FADE_HEIGHT = 46.0f;
    private static final float VIEWPORT_CULL_MARGIN = 4.0f;

    public void setMaxContentHeight(float f) {
        this.maxContentHeight = f;
    }

    public CategoryComponent(Module.category category2) {
        this.category = category2;
        this.width = 138.0f;
        this.x = 5.0f;
        this.y = 5.0f;
        this.moduleY = 5.0f;
        this.titleHeight = 20.0f;
        float f = this.titleHeight + 3.0f;
        this.scrollAnim.reset(this.moduleY);
        this.animationStartHeight = this.lastHeight = this.y + this.titleHeight + 4.0f;
        for (Module module : this.orderedModules()) {
            ModuleComponent moduleComponent = new ModuleComponent(module, this, f);
            this.modules.add(moduleComponent);
            f += 16.0f;
        }
        this.visibleModules = this.modules;
    }

    public CategoryComponent(Module.category category2, List<ModuleComponent> list) {
        this.category = category2;
        this.width = 138.0f;
        this.x = 5.0f;
        this.y = 5.0f;
        this.moduleY = 5.0f;
        this.titleHeight = 0.0f;
        this.scrollAnim.reset(this.moduleY);
        this.animationStartHeight = this.lastHeight = this.y + this.titleHeight + 4.0f;
        this.opened = true;
        for (ModuleComponent moduleComponent : list) {
            moduleComponent.categoryComponent = this;
            this.modules.add(moduleComponent);
        }
        this.visibleModules = this.modules;
    }

    public List<ModuleComponent> getModules() {
        return this.visibleModules != null ? this.visibleModules : this.modules;
    }

    public List<ModuleComponent> getAllModules() {
        return this.modules;
    }

    public void setModuleFilter(String string) {
        String string2;
        String string3 = string2 = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
        if (string2.equals(this.moduleFilter)) {
            return;
        }
        this.moduleFilter = string2;
        this.applyModuleFilter();
    }

    public int getStructureVersion() {
        return this.structureVersion;
    }

    private void applyModuleFilter() {
        ++this.structureVersion;
        if (this.moduleFilter.isEmpty()) {
            this.visibleModules = this.modules;
            return;
        }
        ArrayList<ModuleComponent> arrayList = new ArrayList<ModuleComponent>();
        for (ModuleComponent moduleComponent : this.modules) {
            if (!moduleComponent.matchesSearch(this.moduleFilter)) continue;
            arrayList.add(moduleComponent);
        }
        this.visibleModules = arrayList;
    }

    public void reloadModules() {
        Map<String, Boolean> map = this.captureModuleOpenStates();
        this.modules.clear();
        this.titleHeight = this.referenceLayout ? (this.referenceTitleVisible ? 28.0f : 0.0f) : 20.0f;
        float f = this.titleHeight + 3.0f;
        for (Module module : this.orderedModules()) {
            ModuleComponent moduleComponent = new ModuleComponent(module, this, f);
            moduleComponent.restoreOpenState(Boolean.TRUE.equals(map.get(module.getName())));
            this.modules.add(moduleComponent);
            f += 16.0f;
        }
        this.syncAfterModuleReload();
    }

    private List<Module> orderedModules() {
        ArrayList<Module> arrayList = new ArrayList<Module>(Valen.getModuleManager().inCategory(this.category));
        arrayList.sort((module, module2) -> {
            boolean bl;
            boolean bl2 = Valen.preferences != null && Valen.preferences.isFavorite((Module)module);
            boolean bl3 = bl = Valen.preferences != null && Valen.preferences.isFavorite((Module)module2);
            if (bl2 != bl) {
                return bl2 ? -1 : 1;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(module.getName(), module2.getName());
        });
        return arrayList;
    }

    private Map<String, Boolean> captureModuleOpenStates() {
        HashMap<String, Boolean> hashMap = new HashMap<String, Boolean>();
        for (ModuleComponent moduleComponent : this.modules) {
            if (moduleComponent.mod == null) continue;
            hashMap.put(moduleComponent.mod.getName(), moduleComponent.isOpened);
        }
        return hashMap;
    }

    private void syncAfterModuleReload() {
        float f;
        this.applyModuleFilter();
        CategoryLayoutMetrics categoryLayoutMetrics = this.computeLayoutMetrics(this.opened || this.smoothTimer != null);
        float f2 = categoryLayoutMetrics.minScrollY;
        float f3 = this.y;
        this.moduleY = f = Math.max(f2, Math.min(f3, this.scrollAnim.getTarget()));
        this.scrollAnim.reset(f);
        if (this.opened && !this.getModules().isEmpty()) {
            this.big = categoryLayoutMetrics.visibleHeight;
            this.lastHeight = categoryLayoutMetrics.contentBottom;
            return;
        }
        if (!this.opened && this.smoothTimer == null) {
            this.big = 0.0f;
        }
        this.lastHeight = this.y + this.titleHeight + 4.0f;
    }

    public void setX(float f, boolean bl) {
        if (bl) {
            f = Math.max(f, 2.0f);
            f = Math.min(f, this.screenWidth - this.width - 4.0f);
        }
        this.x = f;
    }

    public void setY(float f, boolean bl) {
        float f2;
        float f3;
        if (bl) {
            f = Math.max(f, 1.0f);
            f3 = this.screenHeight - this.titleHeight - 5.0f;
            f = Math.min(f, f3);
        }
        f3 = this.scrollAnim.getTarget() - this.y;
        if (this.y == f) {
            return;
        }
        this.y = f;
        this.moduleY = f2 = f + f3;
        this.scrollAnim.shiftBy(f2 - this.scrollAnim.getTarget());
    }

    public void overTitle(boolean bl) {
        this.dragging = bl;
    }

    public boolean isOpened() {
        return this.opened;
    }

    public void markInteracted() {
        this.lastInteractedTime = ++interactionSequence;
    }

    public void mouseClicked(boolean bl) {
        this.animationStartHeight = this.getCurrentAnimatedCategoryHeight();
        this.animationStartNamePos = this.getCurrentAnimatedNamePos();
        float f = 220.0f;
        this.opened = bl;
        this.smoothTimer = new Timer(f);
        this.smoothTimer.start();
        this.textTimer = new Timer(f);
        this.textTimer.start();
    }

    public void onScroll(int n) {
        this.onScroll(n, Float.NaN, Float.NaN);
    }

    public void onScroll(int n, float f, float f2) {
        this.forwardScrollToModules(n);
        if (!this.hoveringOverCategory || !this.opened) {
            return;
        }
        if (this.capturesNestedScroll(f, f2)) {
            return;
        }
        this.scrollList(n);
    }

    public void forwardScrollToModules(int n) {
        for (ModuleComponent moduleComponent : this.getModules()) {
            moduleComponent.onScroll(n);
        }
    }

    public boolean capturesNestedScroll(float f, float f2) {
        if (Float.isNaN(f) || Float.isNaN(f2)) {
            return false;
        }
        for (ModuleComponent moduleComponent : this.getModules()) {
            for (Component component : moduleComponent.settings) {
                if (!moduleComponent.isOpened || !moduleComponent.isVisible(component) || !(component instanceof AbstractSearchListComponent ? ((AbstractSearchListComponent)component).capturesCategoryScroll(f, f2) : (component instanceof PlayerListComponent ? ((PlayerListComponent)component).capturesCategoryScroll(f, f2) : component instanceof StringListComponent && ((StringListComponent)component).capturesCategoryScroll(f, f2)))) continue;
                return true;
            }
        }
        return false;
    }

    public void scrollList(int n) {
        if (!this.opened) {
            return;
        }
        this.markInteracted();
        float f = 25.0f * ((float)n / 120.0f);
        if (f != 0.0f) {
            this.scrollAnim.extend(f);
        }
        this.scrollAnim.clampTarget(this.computeMinScrollY(), this.y);
    }

    private float getTotalScrollExtentHeightF() {
        float f = 0.0f;
        for (ModuleComponent moduleComponent : this.getModules()) {
            f += moduleComponent.getScrollExtentHeightF();
        }
        return f;
    }

    private float computeMinScrollY() {
        return this.computeLayoutMetrics(false).minScrollY;
    }

    public void render(FontRenderer fontRenderer) {
        float f;
        float f2;
        float f3;
        if (!this.referenceLayout) {
            this.width = 138.0f;
            this.titleHeight = 20.0f;
        }
        ValenFontRenderer valenFontRenderer = Gui.getClickGuiHeaderFontRenderer();
        if (this.smoothTimer != null && System.currentTimeMillis() - this.smoothTimer.last >= 280L) {
            this.smoothTimer = null;
        }
        if (this.textTimer != null && System.currentTimeMillis() - this.textTimer.last >= 280L) {
            this.textTimer = null;
        }
        for (ModuleComponent moduleComponent : this.getModules()) {
            moduleComponent.updateAnimationState();
        }
        CategoryLayoutMetrics categoryLayoutMetrics = this.computeLayoutMetrics(this.opened || this.smoothTimer != null);
        this.big = !this.opened && this.smoothTimer == null ? 0.0f : categoryLayoutMetrics.visibleHeight;
        float f4 = this.y;
        float f5 = categoryLayoutMetrics.minScrollY;
        this.scrollAnim.clampTarget(f5, f4);
        this.moduleY = this.scrollAnim.getValue();
        this.moduleY = Math.max(f5, Math.min(f4, this.moduleY));
        String string = ClickGuiStyle.label(this.category.name());
        float f6 = categoryLayoutMetrics.contentBottom;
        if (this.smoothTimer != null) {
            f3 = this.opened ? f6 : this.y + this.titleHeight + 4.0f;
            f2 = this.smoothTimer.getValueFloat(this.animationStartHeight, f3, 2);
            if (this.opened && f2 > f3 || !this.opened && f2 < f3) {
                f2 = f3;
            }
        } else {
            f2 = f6;
        }
        f3 = this.x + 17.0f;
        float f7 = this.textTimer == null ? f3 : this.textTimer.getValueFloat(this.animationStartNamePos, f3, 2);
        this.lastNamePos = f7;
        this.lastHeight = f2;
        GlStateManager.pushMatrix();
        boolean bl = this.opened || this.hovering;
        float f8 = this.x - 2.0f;
        float f9 = this.y;
        float f10 = this.x + this.width + 2.0f;
        float f11 = f2;
        float f12 = f10 - f8;
        float f13 = f11 - f9;
        if (!this.referenceLayout) {
            LunarPostProcessing.prepareBlur();
            Shape.surface(f8, f9, f10, f11, ClickGuiStyle.PANEL_BACKGROUND);
            LunarPostProcessing.blurEnd(2, 3.0f);
            Shape.surface(f8, f9, f10, f11, Utils.mergeAlpha(Color.black.getRGB(), 60));
        }
        if (this.titleHeight > 0.0f) {
            if (this.referenceLayout) {
                valenFontRenderer.drawString(string, this.x + 8.0f, this.y + Math.max(4.0f, (this.titleHeight - (float)valenFontRenderer.getFontHeight()) / 2.0f) + 3.0f, ClickGuiStyle.TEXT_MUTED, false);
            } else {
                f = this.y + Math.max(5.0f, (this.titleHeight - 10.0f) / 2.0f);
                Shape.capsule(this.x + 10.0f, f, this.x + 14.0f, f + 10.0f, bl ? ClickGuiStyle.ACCENT : ClickGuiStyle.ACCENT_SOFT);
                Shape.capsule(this.x + this.width - 18.0f, f + 1.0f, this.x + this.width - 10.0f, f + 9.0f, bl ? ClickGuiStyle.ACCENT : ClickGuiStyle.ACCENT_GHOST);
                valenFontRenderer.drawString(string, f7, this.y + Math.max(4.0f, (this.titleHeight - (float)valenFontRenderer.getFontHeight()) / 2.0f), bl ? ClickGuiStyle.TEXT_PRIMARY : ClickGuiStyle.TEXT_SECONDARY, false);
            }
        }
        f = this.y + this.titleHeight + 8.0f;
        float f14 = f2 - 8.0f;
        float f15 = Math.max(0.0f, f14 - f);
        if (this.referenceLayout && this.maxContentHeight > 0.0f) {
            f15 = Math.max(0.0f, this.maxContentHeight - 8.0f);
            f2 = f14 = f + f15;
        }
        if (this.opened || this.smoothTimer != null) {
            GL11.glEnable((int)3089);
            RenderUtils.scissor(this.x - 3.0f, f, this.width + 6.0f, f15);
            float f16 = this.moduleY - this.y;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)0.0f, (float)f16, (float)0.0f);
            Iterator<ModuleComponent> iterator = this.getModules().iterator();
            while (iterator.hasNext()) {
                ModuleComponent moduleComponent;
                float f17 = this.y + moduleComponent.yPos + f16;
                moduleComponent = iterator.next();
                float f18 = f17 + Math.max(moduleComponent.getHeightF(), moduleComponent.getScrollExtentHeightF());
                if (f18 < f - 4.0f || f17 > f14 + 4.0f) continue;
                moduleComponent.setEdgeFade(this.edgeFadeFor(f17, f14, this.moduleY - f5));
                moduleComponent.render();
            }
            GlStateManager.popMatrix();
            GL11.glDisable((int)3089);
        }
        GlStateManager.popMatrix();
    }

    private float edgeFadeFor(float f, float f2, float f3) {
        float f4 = f2 - f;
        if (f4 >= 46.0f) {
            return 1.0f;
        }
        float f5 = Math.min(1.0f, Math.max(0.0f, f3) / 46.0f);
        return 1.0f - (1.0f - Math.max(0.0f, f4 / 46.0f)) * f5;
    }

    public void updateHeight() {
        float f = this.titleHeight + 8.0f;
        for (Component component : this.getModules()) {
            component.updateHeight(f);
            f += component.getHeightF();
        }
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getModuleY() {
        return this.moduleY;
    }

    public float getWidth() {
        return this.width;
    }

    public void mousePosition(int n, int n2, boolean bl) {
        if (this.dragging) {
            float f = (float)n - this.xx;
            float f2 = (float)n2 - this.yy;
            f = Math.max(f, 2.0f);
            f = Math.min(f, this.screenWidth - this.width - 4.0f);
            f2 = Math.max(f2, 1.0f);
            int n3 = (int)(this.screenHeight - this.titleHeight - 5.0f);
            f2 = Math.min(f2, (float)n3);
            this.setX(f, false);
            this.setY(f2, false);
        }
        this.hoveringOverCategory = bl && this.overCategory(n, n2);
        this.hovering = bl && this.overTitle(n, n2);
    }

    public boolean overTitle(int n, int n2) {
        return (float)n >= this.x && (float)n <= this.x + this.width && (float)n2 >= this.y + 2.0f && (float)n2 <= this.y + this.titleHeight + 1.0f;
    }

    public boolean overCategory(int n, int n2) {
        return (float)n >= this.x - 2.0f && (float)n <= this.x + this.width + 2.0f && (float)n2 >= this.y + 2.0f && (float)n2 <= this.y + this.titleHeight + this.big + 1.0f;
    }

    public boolean draggable(int n, int n2) {
        return (float)n >= this.x && (float)n <= this.x + this.width && (float)n2 >= this.y && (float)n2 <= this.y + this.titleHeight;
    }

    public boolean overRect(int n, int n2) {
        return (float)n >= this.x - 2.0f && (float)n <= this.x + this.width + 2.0f && (float)n2 >= this.y && (float)n2 <= this.lastHeight;
    }

    private float getCurrentCategoryBottomFromContent() {
        if (!this.getModules().isEmpty() && (this.opened || this.smoothTimer != null)) {
            float f = this.y + this.screenHeight * 0.9f;
            return Math.min(this.y + this.titleHeight + this.big + 4.0f, f);
        }
        return this.y + this.titleHeight + 4.0f;
    }

    private float getCurrentAnimatedNamePos() {
        if (this.textTimer != null) {
            return this.lastNamePos;
        }
        return this.x + 17.0f;
    }

    private float getCurrentAnimatedCategoryHeight() {
        if (this.lastHeight > 0.0f) {
            return this.lastHeight;
        }
        if (!this.getModules().isEmpty() && (this.opened || this.smoothTimer != null)) {
            float f = 0.0f;
            for (ModuleComponent moduleComponent : this.getModules()) {
                f += moduleComponent.getHeightF();
            }
            return this.y + this.titleHeight + f + 4.0f;
        }
        return this.y + this.titleHeight + 4.0f;
    }

    public void setScreenSize(float f, float f2) {
        this.screenWidth = f;
        this.screenHeight = f2;
    }

    public void setReferenceLayout(float f, float f2, float f3, boolean bl) {
        this.referenceLayout = true;
        this.referenceTitleVisible = bl;
        this.width = Math.max(160.0f, f3);
        this.titleHeight = bl ? 28.0f : 0.0f;
        this.setX(f, false);
        this.setY(f2, false);
        this.opened = true;
        if (this.smoothTimer == null) {
            this.animationStartHeight = this.y + this.titleHeight + 4.0f;
        }
    }

    public void clearReferenceLayout() {
        if (!this.referenceLayout) {
            return;
        }
        this.referenceLayout = false;
        this.referenceTitleVisible = true;
        this.width = 138.0f;
        this.titleHeight = 20.0f;
    }

    public boolean isReferenceLayout() {
        return this.referenceLayout;
    }

    public float getReferenceLayoutHeight() {
        CategoryLayoutMetrics categoryLayoutMetrics = this.computeLayoutMetrics(true);
        return Math.max(this.titleHeight + 4.0f, categoryLayoutMetrics.contentBottom - this.y);
    }

    public float getRenderedBottom() {
        return this.lastHeight;
    }

    public void limitPositions() {
        this.setX(this.x, true);
        this.setY(this.y, true);
    }

    public void applySavedState(float f, float f2, boolean bl, boolean bl2) {
        if (bl2) {
            this.setX(f, true);
            this.setY(f2, true);
        } else {
            float f3;
            float f4 = this.scrollAnim.getTarget() - this.y;
            this.x = f;
            this.y = f2;
            this.moduleY = f3 = f2 + f4;
            this.scrollAnim.reset(f3);
        }
        this.opened = bl;
        this.smoothTimer = null;
        this.textTimer = null;
        if (bl && !this.getModules().isEmpty()) {
            CategoryLayoutMetrics categoryLayoutMetrics = this.computeLayoutMetrics(true);
            this.big = categoryLayoutMetrics.visibleHeight;
            this.lastHeight = categoryLayoutMetrics.contentBottom;
        } else {
            this.big = 0.0f;
            this.lastHeight = this.y + this.titleHeight + 4.0f;
        }
        this.moduleY = this.y;
        this.scrollAnim.reset(this.y);
    }

    public float getScrollTargetOffset() {
        return this.scrollAnim.getTarget() - this.y;
    }

    public void applyScrollTargetOffset(float f) {
        float f2;
        float f3 = this.y + f;
        CategoryLayoutMetrics categoryLayoutMetrics = this.computeLayoutMetrics(true);
        this.moduleY = f2 = Math.max(categoryLayoutMetrics.minScrollY, Math.min(this.y, f3));
        this.scrollAnim.reset(f2);
    }

    public void onGuiClosed() {
        if (this.smoothTimer != null || this.textTimer != null) {
            float f = this.y + this.titleHeight;
            if (this.opened && !this.modules.isEmpty()) {
                float f2 = 0.0f;
                for (ModuleComponent moduleComponent : this.modules) {
                    f2 += moduleComponent.getHeightF();
                }
                f += f2 + 4.0f;
            } else {
                f += 4.0f;
            }
            this.lastHeight = f;
        }
        this.smoothTimer = null;
        this.textTimer = null;
        this.moduleY = this.scrollAnim.getTarget();
        this.scrollAnim.reset(this.moduleY);
    }

    private CategoryLayoutMetrics computeLayoutMetrics(boolean bl) {
        float f;
        float f2;
        float f3;
        if (this.getModules().isEmpty() || !this.opened && this.smoothTimer == null) {
            return new CategoryLayoutMetrics(0.0f, this.y, this.y + this.titleHeight + 4.0f);
        }
        if (this.referenceLayout && this.maxContentHeight > 0.0f) {
            f3 = Math.max(0.0f, this.maxContentHeight - 8.0f);
        } else {
            f2 = Math.max(32.0f, this.screenHeight - this.y - 8.0f);
            f3 = Math.min(this.screenHeight * 0.9f, f2) - this.titleHeight - 16.0f;
            f3 = Math.max(24.0f, f3);
        }
        f2 = 0.0f;
        float f4 = 0.0f;
        float f5 = (this.referenceLayout ? 0.0f : this.titleHeight) + 8.0f;
        for (ModuleComponent moduleComponent : this.getModules()) {
            if (bl) {
                moduleComponent.updateHeight(f5);
            }
            f = Math.max(moduleComponent.getHeightF(), moduleComponent.getScrollExtentHeightF());
            f5 += f;
            f4 += f;
            if (!(f2 < f3)) continue;
            f2 += Math.min(f, f3 - f2);
        }
        float f6 = Math.max(0.0f, f4 - f3);
        float f7 = f6 > 0.0f ? this.y - f6 : this.y;
        f = this.y + (this.referenceLayout && this.maxContentHeight > 0.0f ? this.maxContentHeight : this.screenHeight * 0.9f);
        float f8 = Math.min(this.y + (this.referenceLayout ? 0.0f : this.titleHeight) + 16.0f + f2, f);
        return new CategoryLayoutMetrics(Math.max(0.0f, f2), f7, f8);
    }

    private static final class CategoryLayoutMetrics {
        private final float visibleHeight;
        private final float minScrollY;
        private final float contentBottom;

        private CategoryLayoutMetrics(float f, float f2, float f3) {
            this.visibleHeight = f;
            this.minScrollY = f2;
            this.contentBottom = f3;
        }
    }
}

