/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GlStateManager
 */
package valen.clickgui.render;

import java.util.function.IntConsumer;
import net.minecraft.client.renderer.GlStateManager;
import valen.clickgui.ClickGuiStyle;
import valen.clickgui.animation.ToggleAnimation;
import valen.clickgui.page.PageUI;
import valen.clickgui.render.Controls;
import valen.clickgui.render.Shape;
import valen.utility.RenderUtils;

public final class PopupMenu {
    private static final float ROW_HEIGHT = 16.0f;
    private static final String ICON_CHECK = "/assets/valen/textures/gui/icons/check.png";
    private static final float PADDING = 4.0f;
    private static final float GAP = 4.0f;
    private static final float EDGE = 6.0f;
    private static final float OVERHANG = 8.0f;
    private static final float CHECK_LEFT = 8.0f;
    private static final float CHECK_SIZE = 8.0f;
    private static final float TEXT_LEFT = 20.0f;
    private static final float TEXT_RIGHT = 12.0f;
    private static final float CLOSED_SCALE = 0.94f;
    private static final float SURFACE_LEAD = 0.4f;
    private static final float CONTENT_DELAY = 0.25f;
    private static final ToggleAnimation reveal = new ToggleAnimation(170L);
    private static Object owner;
    private static String[] options;
    private static int selected;
    private static IntConsumer listener;
    private static boolean open;
    private static boolean anchored;
    private static boolean above;
    private static int highlighted;
    private static int lastMouseX;
    private static int lastMouseY;
    private static float anchorLeft;
    private static float anchorTop;
    private static float anchorRight;
    private static float anchorBottom;
    private static float left;
    private static float top;
    private static float right;
    private static float bottom;

    private PopupMenu() {
    }

    public static void open(Object object, String[] stringArray, int n, float f, float f2, float f3, float f4, IntConsumer intConsumer) {
        owner = object;
        options = stringArray;
        selected = n;
        listener = intConsumer;
        open = true;
        anchored = true;
        highlighted = -1;
        lastMouseX = Integer.MIN_VALUE;
        lastMouseY = Integer.MIN_VALUE;
        PopupMenu.setAnchor(f, f2, f3, f4);
        reveal.snap(false);
        reveal.setTarget(true);
    }

    public static void anchor(Object object, float f, float f2, float f3, float f4) {
        if (owner == object) {
            PopupMenu.setAnchor(f, f2, f3, f4);
            anchored = true;
        }
    }

    public static boolean isOpen() {
        return owner != null && open;
    }

    public static boolean isOpenFor(Object object) {
        return owner == object && open;
    }

    public static void close() {
        if (owner != null && open) {
            open = false;
            highlighted = -1;
            reveal.setTarget(false);
        }
    }

    public static void closeInstantly() {
        owner = null;
        listener = null;
        options = new String[0];
        open = false;
        highlighted = -1;
        reveal.snap(false);
    }

    public static void render(int n, int n2, float f, float f2, float f3, float f4) {
        if (owner == null) {
            return;
        }
        if (!anchored) {
            PopupMenu.closeInstantly();
            return;
        }
        anchored = false;
        float f5 = reveal.getValue();
        if (!open && f5 <= 0.001f) {
            PopupMenu.closeInstantly();
            return;
        }
        PopupMenu.layout(f, f2, f3, f4);
        if (open && (n != lastMouseX || n2 != lastMouseY)) {
            lastMouseX = n;
            lastMouseY = n2;
            highlighted = PopupMenu.rowAt(n, n2);
        }
        float f6 = 0.94f + 0.060000002f * f5;
        float f7 = Controls.clamp01(f5 / 0.4f);
        float f8 = Controls.clamp01((f5 - 0.25f) / 0.75f);
        float f9 = (anchorLeft + anchorRight) / 2.0f;
        float f10 = above ? bottom : top;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)f9, (float)f10, (float)0.0f);
        GlStateManager.scale((float)f6, (float)f6, (float)1.0f);
        GlStateManager.translate((float)(-f9), (float)(-f10), (float)0.0f);
        PageUI.shadow(left, top, right, bottom, f7);
        Shape.surface(left, top, right, bottom, Controls.fade(ClickGuiStyle.DROPDOWN_BACKGROUND | 0xFF000000, f7));
        int n3 = Controls.fade(ClickGuiStyle.TEXT_PRIMARY, f8);
        for (int i = 0; i < options.length; ++i) {
            float f11 = PopupMenu.rowTop(i);
            float f12 = f11 + 16.0f;
            if (i == highlighted) {
                Shape.capsule(left + 4.0f, f11, right - 4.0f, f12, Controls.fade(ClickGuiStyle.SURFACE_ACTIVE, f7));
            }
            if (n3 >>> 24 == 0) continue;
            if (i == selected) {
                RenderUtils.drawIcon(RenderUtils.getIcon(ICON_CHECK), left + 8.0f, (f11 + f12 - 8.0f) / 2.0f, 8, n3);
            }
            String string = PageUI.truncateSmall(options[i], right - left - 20.0f - 12.0f);
            PageUI.small(string, left + 20.0f, PageUI.smallY(f11, f12), n3);
        }
        GlStateManager.popMatrix();
    }

    public static boolean mouseClicked(int n, int n2, int n3) {
        if (!PopupMenu.isOpen()) {
            return false;
        }
        int n4 = PopupMenu.rowAt(n, n2);
        if (n4 >= 0) {
            if (n3 == 0) {
                PopupMenu.choose(n4);
            }
            return true;
        }
        if (!PageUI.inside(n, n2, left, top, right, bottom)) {
            PopupMenu.close();
        }
        return true;
    }

    public static boolean scrolled() {
        if (!PopupMenu.isOpen()) {
            return false;
        }
        PopupMenu.close();
        return true;
    }

    public static boolean keyTyped(int n) {
        if (!PopupMenu.isOpen()) {
            return false;
        }
        if (n == 1) {
            PopupMenu.close();
        } else if (n == 200 || n == 208) {
            int n2 = n == 200 ? -1 : 1;
            int n3 = highlighted >= 0 ? highlighted : selected;
            highlighted = Math.max(0, Math.min(options.length - 1, n3 + (highlighted >= 0 ? n2 : 0)));
        } else if (n == 28 || n == 156 || n == 57) {
            if (highlighted >= 0) {
                PopupMenu.choose(highlighted);
            } else {
                PopupMenu.close();
            }
        }
        return true;
    }

    private static float menuWidth(String[] stringArray) {
        float f = 0.0f;
        for (String string : stringArray) {
            f = Math.max(f, PageUI.smallWidth(string));
        }
        return f + 20.0f + 12.0f;
    }

    private static void choose(int n) {
        IntConsumer intConsumer = listener;
        selected = n;
        PopupMenu.close();
        if (intConsumer != null && n >= 0 && n < options.length) {
            intConsumer.accept(n);
        }
    }

    private static void setAnchor(float f, float f2, float f3, float f4) {
        anchorLeft = f;
        anchorTop = f2;
        anchorRight = f3;
        anchorBottom = f4;
    }

    public static float[] place(float f, float f2, float f3, float f4, float f5, int n, float f6, float f7, float f8, float f9) {
        float f10 = Math.max(f3 - f + 16.0f, f5);
        float f11 = (float)n * 16.0f + 8.0f;
        float f12 = Math.min(f3 + 8.0f, f8 - 6.0f);
        float f13 = f12 - f10;
        if (f13 < f6 + 6.0f) {
            f13 = f6 + 6.0f;
            f12 = f13 + f10;
        }
        float f14 = f9 - 6.0f - (f4 + 4.0f);
        float f15 = f2 - 4.0f - (f7 + 6.0f);
        boolean bl = f14 < f11 && f15 > f14;
        float f16 = bl ? f2 - 4.0f - f11 : f4 + 4.0f;
        f16 = Math.max(f7 + 6.0f, Math.min(f9 - 6.0f - f11, f16));
        return new float[]{f13, f16, f12, f16 + f11, bl ? 1.0f : 0.0f};
    }

    private static void layout(float f, float f2, float f3, float f4) {
        float[] fArray = PopupMenu.place(anchorLeft, anchorTop, anchorRight, anchorBottom, PopupMenu.menuWidth(options), options.length, f, f2, f3, f4);
        left = fArray[0];
        top = fArray[1];
        right = fArray[2];
        bottom = fArray[3];
        above = fArray[4] > 0.0f;
    }

    private static float rowTop(int n) {
        return top + 4.0f + (float)n * 16.0f;
    }

    private static int rowAt(int n, int n2) {
        if ((float)n < left + 4.0f || (float)n > right - 4.0f) {
            return -1;
        }
        for (int i = 0; i < options.length; ++i) {
            float f = PopupMenu.rowTop(i);
            if (!((float)n2 >= f) || !((float)n2 < f + 16.0f)) continue;
            return i;
        }
        return -1;
    }

    static {
        options = new String[0];
        highlighted = -1;
        lastMouseX = Integer.MIN_VALUE;
        lastMouseY = Integer.MIN_VALUE;
    }
}

