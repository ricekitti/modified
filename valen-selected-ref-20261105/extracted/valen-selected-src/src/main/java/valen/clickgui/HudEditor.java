/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.ScaledResolution
 *  valen.clickgui.HudPlacement
 */
package valen.clickgui;

import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import valen.Valen;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.HudPlacement;
import valen.clickgui.page.PageUI;
import valen.module.ModuleManager;

public final class HudEditor
extends GuiScreen {
    private final GuiScreen parent;
    private int selected;
    private boolean dragging;
    private float grabX;
    private float grabY;
    private String feedback;

    public HudEditor(GuiScreen guiScreen) {
        this.parent = guiScreen;
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private float[] bounds(int n, ScaledResolution scaledResolution) {
        return n == 0 ? ModuleManager.hud.editorBounds(scaledResolution) : ModuleManager.targetHUD.editorBounds(scaledResolution);
    }

    private void move(ScaledResolution scaledResolution, float f, float f2) {
        if (this.selected == 0) {
            ModuleManager.hud.moveEditor(scaledResolution, f, f2);
        } else {
            ModuleManager.targetHUD.moveEditor(scaledResolution, f, f2);
        }
    }

    public void drawScreen(int n, int n2, float f) {
        float f2;
        float[] fArray;
        if (ModuleManager.hud == null || ModuleManager.targetHUD == null) {
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        ModuleManager.hud.prepareEditor();
        HudEditor.drawRect((int)0, (int)0, (int)this.width, (int)this.height, (int)-2013265920);
        if (this.dragging) {
            fArray = this.bounds(this.selected, scaledResolution);
            float[] fArray2 = this.bounds(1 - this.selected, scaledResolution);
            float f3 = (float)n - this.grabX;
            f2 = (float)n2 - this.grabY;
            if (!HudEditor.isShiftKeyDown()) {
                float[] fArray3 = HudPlacement.snap((float)f3, (float)fArray[2], (float)this.width, (float)fArray2[0], (float)fArray2[2]);
                float[] fArray4 = HudPlacement.snap((float)f2, (float)fArray[3], (float)this.height, (float)fArray2[1], (float)fArray2[3]);
                f3 = fArray3[0];
                f2 = fArray4[0];
                if (fArray3[1] >= 0.0f) {
                    HudEditor.drawRect((int)((int)fArray3[1]), (int)0, (int)((int)fArray3[1] + 1), (int)this.height, (int)2006563839);
                }
                if (fArray4[1] >= 0.0f) {
                    HudEditor.drawRect((int)0, (int)((int)fArray4[1]), (int)this.width, (int)((int)fArray4[1] + 1), (int)2006563839);
                }
            }
            this.move(scaledResolution, f3, f2);
        }
        ModuleManager.hud.renderEditor(scaledResolution);
        ModuleManager.targetHUD.renderEditor();
        fArray = this.bounds(this.selected, scaledResolution);
        int n3 = -1711276033;
        HudEditor.drawRect((int)((int)fArray[0] - 2), (int)((int)fArray[1] - 2), (int)((int)(fArray[0] + fArray[2]) + 2), (int)((int)fArray[1] - 1), (int)n3);
        HudEditor.drawRect((int)((int)fArray[0] - 2), (int)((int)(fArray[1] + fArray[3]) + 1), (int)((int)(fArray[0] + fArray[2]) + 2), (int)((int)(fArray[1] + fArray[3]) + 2), (int)n3);
        HudEditor.drawRect((int)((int)fArray[0] - 2), (int)((int)fArray[1] - 1), (int)((int)fArray[0] - 1), (int)((int)(fArray[1] + fArray[3]) + 1), (int)n3);
        HudEditor.drawRect((int)((int)(fArray[0] + fArray[2]) + 1), (int)((int)fArray[1] - 1), (int)((int)(fArray[0] + fArray[2]) + 2), (int)((int)(fArray[1] + fArray[3]) + 1), (int)n3);
        PageUI.smallCentred("HUD layout", 0.0f, this.width, 7.0f, 25.0f, ClickGuiStyle.TEXT_PRIMARY);
        String string = this.feedback != null ? this.feedback : "Drag to align  \u00b7  Shift: free move  \u00b7  Arrows: nudge";
        PageUI.smallCentred(string, 0.0f, this.width, this.height - 53, this.height - 36, ClickGuiStyle.TEXT_PRIMARY);
        f2 = (float)this.width / 2.0f - 122.0f;
        PageUI.button("Reset " + (this.selected == 0 ? "HUD" : "TargetHUD"), f2, this.height - 30, f2 + 106.0f, this.height - 8, ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, PageUI.inside(n, n2, f2, this.height - 30, f2 + 106.0f, this.height - 8) ? 1.0f : 0.0f, true);
        boolean bl = Valen.configManager != null && Valen.configManager.getActiveName() != null;
        PageUI.button("Save", f2 + 114.0f, this.height - 30, f2 + 175.0f, this.height - 8, ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, PageUI.inside(n, n2, f2 + 114.0f, this.height - 30, f2 + 175.0f, this.height - 8) ? 1.0f : 0.0f, bl);
        PageUI.button("Done", f2 + 183.0f, this.height - 30, f2 + 244.0f, this.height - 8, ClickGuiStyle.SURFACE_ACTIVE, ClickGuiStyle.TEXT_PRIMARY, PageUI.inside(n, n2, f2 + 183.0f, this.height - 30, f2 + 244.0f, this.height - 8) ? 1.0f : 0.0f, true);
    }

    protected void mouseClicked(int n, int n2, int n3) throws IOException {
        if (n3 != 0) {
            return;
        }
        float f = (float)this.width / 2.0f - 122.0f;
        if (n2 >= this.height - 30 && n2 <= this.height - 8) {
            if ((float)n >= f && (float)n <= f + 106.0f) {
                if (this.selected == 0) {
                    ModuleManager.hud.resetEditor();
                } else {
                    ModuleManager.targetHUD.resetEditor();
                }
                if (Valen.configManager != null) {
                    Valen.configManager.invalidateDirtyCache();
                }
                this.feedback = "Position reset";
                return;
            }
            if ((float)n >= f + 114.0f && (float)n <= f + 175.0f) {
                this.feedback = Valen.configManager != null && Valen.configManager.saveActive() ? "Saved profile" : "Choose a profile in Profiles to save";
                return;
            }
            if ((float)n >= f + 183.0f && (float)n <= f + 244.0f) {
                this.mc.displayGuiScreen(this.parent);
                return;
            }
        }
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        for (int n4 : new int[]{this.selected, 1 - this.selected}) {
            float[] fArray = this.bounds(n4, scaledResolution);
            if (!((float)n >= fArray[0]) || !((float)n <= fArray[0] + fArray[2]) || !((float)n2 >= fArray[1]) || !((float)n2 <= fArray[1] + fArray[3])) continue;
            this.selected = n4;
            this.dragging = true;
            this.grabX = (float)n - fArray[0];
            this.grabY = (float)n2 - fArray[1];
            this.feedback = null;
            return;
        }
    }

    protected void mouseReleased(int n, int n2, int n3) {
        if (n3 == 0) {
            this.dragging = false;
        }
    }

    protected void keyTyped(char c, int n) throws IOException {
        float f;
        if (n == 1) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        if (n == 15) {
            this.selected = 1 - this.selected;
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        float[] fArray = this.bounds(this.selected, scaledResolution);
        float f2 = f = HudEditor.isShiftKeyDown() ? 10.0f : 1.0f;
        if (n == 203 || n == 205 || n == 200 || n == 208) {
            this.move(scaledResolution, fArray[0] + (n == 203 ? -f : (n == 205 ? f : 0.0f)), fArray[1] + (n == 200 ? -f : (n == 208 ? f : 0.0f)));
            this.feedback = null;
        }
    }

    public void onGuiClosed() {
        this.dragging = false;
        if (Valen.configManager != null) {
            Valen.configManager.invalidateDirtyCache();
        }
    }
}

