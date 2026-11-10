/*
 * Decompiled with CFR 0.152.
 */
package valen.clickgui.page;

import valen.clickgui.components.impl.BindComponent;

public interface GuiPage {
    default public BindComponent activeBinding() {
        return null;
    }

    default public boolean blocksBackgroundInput() {
        return false;
    }

    default public float modalOpacity() {
        return 0.0f;
    }

    default public void renderModal(float f, float f2, float f3, float f4, int n, int n2) {
    }

    public void render(float var1, float var2, float var3, float var4, int var5, int var6);

    public boolean onClick(int var1, int var2, int var3);

    public void mouseReleased(int var1, int var2, int var3);

    public boolean keyTyped(char var1, int var2);

    public boolean onScroll(int var1, int var2, int var3);

    public void onClosed();
}

