/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.util.ResourceLocation
 *  valen.clickgui.components.impl.AbstractTextInputComponent$Layout
 *  valen.utility.PlayerRelationsManager$PlayerEntry
 *  valen.utility.PlayerSkinCache
 */
package valen.clickgui.components.impl;

import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ScrollOffsetAnimation;
import valen.clickgui.components.impl.AbstractTextInputComponent;
import valen.clickgui.components.impl.ModuleComponent;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.Shape;
import valen.module.setting.impl.PlayerListSetting;
import valen.utility.PlayerRelationsManager;
import valen.utility.PlayerSkinCache;
import valen.utility.RenderUtils;

public class PlayerListComponent
extends AbstractTextInputComponent {
    private static final String CLOSE_ICON_PATH = "/assets/valen/textures/gui/close.png";
    private static final float SELECTED_LIST_GAP = 4.0f;
    private static final int MAX_VISIBLE_SELECTED = 7;
    private static final int CLOSE_SIZE = 8;
    private static final float CLOSE_PAD = 6.0f;
    private static final float HEAD_SIZE = 12.0f;
    private static final float LIST_ROW_TEXT_SCALE = 0.5f;
    private static final float LIST_ROW_VISUAL_HEIGHT = 19.0f;
    private final PlayerListSetting setting;
    private final ScrollOffsetAnimation selectedScrollAnim = new ScrollOffsetAnimation(200L);
    private float lastMouseX;
    private float lastMouseY;
    private boolean expanded;
    private boolean addInputVisible = true;
    private int visibleRows = 7;

    public void setVisibleRows(int n) {
        this.visibleRows = Math.max(1, Math.min(7, n));
        this.clampSelectedScroll();
    }

    public void setAddInputVisible(boolean bl) {
        this.addInputVisible = bl;
        if (!bl) {
            this.setTextFieldFocused(false);
        }
    }

    public void expand() {
        this.expanded = true;
    }

    public PlayerListComponent(PlayerListSetting playerListSetting, ModuleComponent moduleComponent, float f) {
        super(moduleComponent, f, playerListSetting.getPlaceholder(), playerListSetting.getMaxLength());
        this.setting = playerListSetting;
    }

    @Override
    public void render() {
        AbstractTextInputComponent.Layout layout = this.layout(false);
        if (this.setting.dropdown) {
            float f = layout.cy + this.o;
            if (this.lastMouseX >= layout.left && this.lastMouseX <= layout.right && this.lastMouseY >= f && this.lastMouseY < f + 20.0f) {
                Shape.capsule(layout.left - 5.0f, f, layout.right + 5.0f, f + 20.0f, ClickGuiStyle.SURFACE_HOVER);
            }
            String string = String.valueOf(this.setting.getEntries().size());
            PageUI.small(string, layout.right - 20.0f - PageUI.smallWidth(string), PlayerListComponent.centeredScaledTextY(f, 20.0f), ClickGuiStyle.TEXT_SECONDARY);
            Controls.chevron(layout.right - 5.0f, f + 10.0f, 5.0f, this.expanded ? 0.0f : -90.0f, ClickGuiStyle.TEXT_SECONDARY);
            this.renderLabel(layout, this.setting.getName());
            if (!this.expanded) {
                return;
            }
        } else {
            this.renderLabel(layout, this.setting.getName());
        }
        if (this.addInputVisible) {
            this.renderTextField(layout);
        }
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
        if (n3 == 0 && this.setting.dropdown && (float)n >= layout.left && (float)n <= layout.right && (float)n2 >= layout.cy + this.o && (float)n2 < layout.cy + this.o + 20.0f) {
            this.expanded = !this.expanded;
            this.getTextField().setText("");
            this.setTextFieldFocused(false);
            this.moduleComponent.updateSettingPositions();
            return true;
        }
        if (this.setting.dropdown && !this.expanded) {
            return false;
        }
        if (n3 == 0 && this.addInputVisible && this.isTextFieldClicked(n, n2, layout)) {
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
        if (this.setting.dropdown && !this.expanded) {
            return 20.0f;
        }
        int n = this.setting.getEntries().size();
        float f = n == 0 ? (this.addInputVisible ? 0.0f : 24.0f) : 4.0f + (float)Math.min(this.visibleRows, n) * 20.0f;
        return (this.addInputVisible ? 2.0f : 1.0f) * 20.0f + f;
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
        return (!this.setting.dropdown || this.expanded) && this.setting.getEntries().size() > this.visibleRows && this.isMouseOverSelectedList(f, f2);
    }

    @Override
    public boolean containsClick(int n, int n2) {
        if (this.setting.dropdown && !this.expanded) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        return this.addInputVisible && this.isTextFieldClicked(n, n2, layout) || this.isMouseOverSelectedList(n, n2);
    }

    public void onExternalDataChanged() {
        this.clampSelectedScroll();
    }

    private void submitText() {
        String string = this.getTextField().getText();
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        if (this.setting.addPlayer(string) || PlayerRelationsManager.isValidUsername(string)) {
            this.getTextField().setText("");
        }
        this.moduleComponent.updateSettingPositions();
        this.clampSelectedScroll();
    }

    private void renderSelectedEntries(AbstractTextInputComponent.Layout layout) {
        List<PlayerRelationsManager.PlayerEntry> list = this.setting.getEntries();
        if (list.isEmpty()) {
            if (!this.addInputVisible) {
                PlayerListComponent.drawScaledText("No friends yet", layout.left, PlayerListComponent.centeredScaledTextY(this.getSelectedTop(layout), 20.0f), ClickGuiStyle.TEXT_SECONDARY);
            }
            return;
        }
        float f = this.getSelectedTop(layout);
        float f2 = this.getSelectedVisibleHeight(list.size());
        float f3 = this.moduleComponent.categoryComponent.getModuleY() - layout.cy;
        if (this.addInputVisible) {
            PlayerListComponent.drawListWell(layout.left, f, layout.right, f + f2);
        }
        RenderUtils.scissorPushGui(layout.left, f + f3, layout.right - layout.left, f2);
        float f4 = this.selectedScrollAnim.getValue();
        int n = (int)(f4 / 20.0f);
        int n2 = Math.min(n + this.visibleRows + 1, list.size());
        Map<String, NetworkPlayerInfo> map = this.getPlayerInfoMap();
        for (int i = n; i < n2; ++i) {
            PlayerRelationsManager.PlayerEntry playerEntry = list.get(i);
            float f5 = f - f4 + (float)i * 20.0f;
            boolean bl = this.lastMouseX >= layout.left && this.lastMouseX <= layout.right && this.lastMouseY >= f5 + f3 && this.lastMouseY < f5 + f3 + 20.0f;
            int n3 = ClickGuiStyle.listRow(i, bl);
            this.renderEntryRow(playerEntry, map.get(playerEntry.getKey()), layout.left, layout.right, f5, n3);
        }
        RenderUtils.scissorPop();
        if (list.size() > this.visibleRows) {
            float f6 = f2 * (float)this.visibleRows / (float)list.size();
            float f7 = f + (f2 - f6) * f4 / ((float)(list.size() - this.visibleRows) * 20.0f);
            Shape.capsule(layout.right + 5.0f, f7, layout.right + 7.0f, f7 + f6, ClickGuiStyle.TEXT_MUTED);
        }
    }

    private void renderEntryRow(PlayerRelationsManager.PlayerEntry playerEntry, NetworkPlayerInfo networkPlayerInfo, float f, float f2, float f3, int n) {
        PlayerListComponent.drawListRow(f, f3, f2, n);
        this.renderPlayerHead(playerEntry, networkPlayerInfo, f + 6.0f + 2.0f, f3 + 3.5f);
        PlayerListComponent.drawScaledTextNoShadow(playerEntry.getDisplayName(), f + 6.0f + 20.0f, PlayerListComponent.centeredScaledTextY(f3, 19.0f, 0.5f), ClickGuiStyle.TEXT_PRIMARY);
        this.renderCloseIcon(f2, f3);
    }

    private boolean handleSelectedEntryClick(int n, int n2, AbstractTextInputComponent.Layout layout) {
        if (!this.isMouseOverSelectedList(n, n2)) {
            return false;
        }
        List<PlayerRelationsManager.PlayerEntry> list = this.setting.getEntries();
        float f = this.selectedScrollAnim.getValue();
        for (int i = 0; i < list.size(); ++i) {
            float f2 = this.getSelectedTop(layout) - f + (float)i * 20.0f;
            if (!this.isOverClose(n, n2, f2, layout.right)) continue;
            this.setting.removePlayer(list.get(i).getKey());
            this.moduleComponent.updateSettingPositions();
            this.clampSelectedScroll();
            return true;
        }
        return false;
    }

    private void renderPlayerHead(PlayerRelationsManager.PlayerEntry playerEntry, NetworkPlayerInfo networkPlayerInfo, float f, float f2) {
        ResourceLocation resourceLocation = this.getSkin(playerEntry, networkPlayerInfo);
        if (resourceLocation == null) {
            return;
        }
        RenderUtils.prepareGuiTextureRenderState();
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        Gui.drawScaledCustomSizeModalRect((int)((int)f), (int)((int)f2), (float)8.0f, (float)8.0f, (int)8, (int)8, (int)12, (int)12, (float)64.0f, (float)64.0f);
        Gui.drawScaledCustomSizeModalRect((int)((int)f), (int)((int)f2), (float)40.0f, (float)8.0f, (int)8, (int)8, (int)12, (int)12, (float)64.0f, (float)64.0f);
    }

    private ResourceLocation getSkin(PlayerRelationsManager.PlayerEntry playerEntry, NetworkPlayerInfo networkPlayerInfo) {
        return PlayerSkinCache.getSkin((String)playerEntry.getDisplayName(), (NetworkPlayerInfo)networkPlayerInfo);
    }

    private Map<String, NetworkPlayerInfo> getPlayerInfoMap() {
        HashMap<String, NetworkPlayerInfo> hashMap = new HashMap<String, NetworkPlayerInfo>();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.getNetHandler() == null) {
            return hashMap;
        }
        for (NetworkPlayerInfo networkPlayerInfo : minecraft.getNetHandler().getPlayerInfoMap()) {
            GameProfile gameProfile;
            if (networkPlayerInfo == null || (gameProfile = networkPlayerInfo.getGameProfile()) == null || gameProfile.getName() == null) continue;
            hashMap.put(gameProfile.getName().toLowerCase(), networkPlayerInfo);
        }
        return hashMap;
    }

    private void clampSelectedScroll() {
        int n = this.setting.getEntries().size();
        float f = Math.max(0.0f, (float)(n - this.visibleRows) * 20.0f);
        this.selectedScrollAnim.clampTarget(0.0f, f);
        if (this.selectedScrollAnim.getValue() > f) {
            this.selectedScrollAnim.reset(f);
        }
    }

    private boolean isMouseOverSelectedList(float f, float f2) {
        List<PlayerRelationsManager.PlayerEntry> list = this.setting.getEntries();
        if (list.isEmpty()) {
            return false;
        }
        AbstractTextInputComponent.Layout layout = this.layout(true);
        float f3 = this.getSelectedTop(layout);
        float f4 = this.getSelectedVisibleHeight(list.size());
        return f >= layout.left && f <= layout.right && f2 >= f3 && f2 < f3 + f4;
    }

    private float getSelectedTop(AbstractTextInputComponent.Layout layout) {
        return layout.contentTop - (this.addInputVisible ? 0.0f : 20.0f) + 4.0f;
    }

    private float getSelectedVisibleHeight(int n) {
        return (float)Math.min(this.visibleRows, n) * 20.0f;
    }

    private void renderCloseIcon(float f, float f2) {
        ResourceLocation resourceLocation = RenderUtils.getIcon(CLOSE_ICON_PATH);
        if (resourceLocation == null) {
            return;
        }
        float f3 = f - 8.0f - 6.0f;
        float f4 = f2 + 5.5f;
        boolean bl = this.isOverClose(this.lastMouseX, this.lastMouseY, f2, f);
        RenderUtils.drawIcon(resourceLocation, f3, f4, 8, bl ? ClickGuiStyle.TEXT_PRIMARY : ClickGuiStyle.TEXT_SECONDARY);
    }

    private boolean isOverClose(float f, float f2, float f3, float f4) {
        return f >= f4 - 20.0f && f <= f4 && f2 >= f3 && f2 < f3 + 20.0f;
    }

    private static void drawScaledTextNoShadow(String string, float f, float f2, int n) {
        PlayerListComponent.drawScaledText(string, f, f2, n, 0.5f);
    }
}

