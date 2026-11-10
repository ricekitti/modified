package dev.rice.forge;

import dev.rice.security.LicenseController;
import dev.rice.security.LicenseRuntime;
import java.io.IOException;
import java.util.Arrays;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

public final class RiceLicenseScreen extends GuiScreen {
    private final GuiScreen parent;
    private final LicenseController controller;
    private GuiTextField key;
    private GuiButton submit;
    public RiceLicenseScreen(GuiScreen parent, LicenseController controller) { this.parent = parent; this.controller = controller; }
    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        key = new GuiTextField(0, fontRendererObj, width / 2 - 145, height / 2 - 26, 290, 22) {
            @Override public void drawTextBox() {
                String raw = getText(); int cursor = getCursorPosition(), selection = getSelectionEnd();
                char[] mask = new char[raw.length()]; Arrays.fill(mask, '*');
                setText(new String(mask)); setCursorPosition(cursor); setSelectionPos(selection);
                try { super.drawTextBox(); }
                finally { setText(raw); setCursorPosition(cursor); setSelectionPos(selection); }
            }
        };
        key.setMaxStringLength(4096); key.setFocused(true);
        submit = new GuiButton(1, width / 2 - 145, height / 2 + 10, 140, 22, "Unlock Rice");
        buttonList.add(submit);
        buttonList.add(new GuiButton(2, width / 2 + 5, height / 2 + 10, 140, 22, "Back"));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1 && !controller.busy()) { controller.authenticate(key.getText()); key.setText(""); }
        if (button.id == 2) mc.displayGuiScreen(parent);
    }
    @Override public void updateScreen() {
        key.updateCursorCounter(); submit.enabled = !controller.busy();
        if (LicenseRuntime.valid()) mc.displayGuiScreen(parent);
    }
    @Override protected void keyTyped(char character, int code) throws IOException {
        if (code == Keyboard.KEY_RETURN || code == Keyboard.KEY_NUMPADENTER) actionPerformed(submit);
        else if (code == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else if (!controller.busy()) key.textboxKeyTyped(character, code);
    }
    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button); key.mouseClicked(x, y, button);
    }
    @Override public void drawScreen(int x, int y, float partial) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Rice Modified", width / 2, height / 2 - 85, 0xffe6a5c0);
        drawCenteredString(fontRendererObj, "License key", width / 2, height / 2 - 49, 0xffeeeeee);
        key.drawTextBox();
        int messageWidth = Math.min(420, width - 32);
        fontRendererObj.drawSplitString(controller.status(), (width - messageWidth) / 2, height / 2 + 48, messageWidth, 0xffcccccc);
        super.drawScreen(x, y, partial);
    }
    @Override public void onGuiClosed() { if (key != null) key.setText(""); Keyboard.enableRepeatEvents(false); }
    @Override public boolean doesGuiPauseGame() { return true; }
}
