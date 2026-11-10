package dev.rice.client.render;

import dev.rice.module.ModuleManager;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Modified reference HUD editor: Module list and Notifications only. */
public final class HudEditorScreen extends GuiScreen {
    private final ModuleManager modules;
    private int selected = -1, dx, dy;
    public HudEditorScreen(ModuleManager modules) { this.modules = modules; }
    @Override public boolean doesGuiPauseGame() { return false; }
    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 105, height - 28, 100, 20, "Done"));
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 100, 20, "Reset positions"));
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        if (selected >= 0) HudLayout.move(HudLayout.IDS[selected], mouseX - dx, mouseY - dy, width, height, fontRendererObj);
        drawCenteredString(fontRendererObj, "Edit HUD", width / 2, 8, 0xFFFFFFFF);
        for (int i = 0; i < HudLayout.IDS.length; i++) {
            String id = HudLayout.IDS[i];
            int[] bounds = HudLayout.bounds(id, width, height, fontRendererObj);
            int color = i == selected ? 0xFFB76DDB : 0xFF77778A;
            HudPreviewRenderer.draw(modules, id, fontRendererObj);
            boolean hovered = mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2]
                && mouseY >= bounds[1] && mouseY <= bounds[1] + bounds[3];
            if (i == selected || hovered) {
                drawRect(bounds[0] - 1, bounds[1] - 1, bounds[0] + bounds[2] + 1, bounds[1], color);
                drawRect(bounds[0] - 1, bounds[1] + bounds[3], bounds[0] + bounds[2] + 1, bounds[1] + bounds[3] + 1, color);
                drawRect(bounds[0] - 1, bounds[1], bounds[0], bounds[1] + bounds[3], color);
                drawRect(bounds[0] + bounds[2], bounds[1], bounds[0] + bounds[2] + 1, bounds[1] + bounds[3], color);
                drawString(fontRendererObj, HudLayout.LABELS[i], Math.max(2, bounds[0]), Math.max(2, bounds[1] - 11), 0xFFCCCCDD);
            }
        }
        super.drawScreen(mouseX, mouseY, partial);
    }
    @Override protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 0 && mouseY < height - 32) {
            for (int i = HudLayout.IDS.length - 1; i >= 0; i--) {
                int[] bounds = HudLayout.bounds(HudLayout.IDS[i], width, height, fontRendererObj);
                if (mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2]
                    && mouseY >= bounds[1] && mouseY <= bounds[1] + bounds[3]) {
                    selected = i; dx = mouseX - bounds[0]; dy = mouseY - bounds[1]; return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    @Override protected void mouseReleased(int mouseX, int mouseY, int button) { selected = -1; super.mouseReleased(mouseX, mouseY, button); }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 0) mc.displayGuiScreen(null); else if (button.id == 1) HudLayout.reset();
    }
    @Override public void onGuiClosed() { selected = -1; modules.persist(); }
}
