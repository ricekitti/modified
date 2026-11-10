package dev.rice.client.render;

import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/** HUD anchors present in the current modified native reference build. */
public final class HudLayout {
    public static final String[] IDS = {"mod_overlay", "notifications"};
    public static final String[] LABELS = {"Module list", "Notifications"};
    public static final String[] OFFSETS = IDS;
    private static ModuleManager manager;
    private HudLayout() {}
    public static void bind(ModuleManager modules) { manager = modules; }
    private static NumberSetting number(Module module, String key) {
        return module != null && module.findSetting(key) instanceof NumberSetting
            ? (NumberSetting) module.findSetting(key) : null;
    }
    private static double value(Module module, String key, double fallback) {
        NumberSetting setting = number(module, key);
        return setting == null ? fallback : setting.doubleValue();
    }
    public static double offset(String id, boolean x) {
        return manager == null ? 0 : value(manager.find("gui"), "hud_" + id + (x ? "_x" : "_y"), 0);
    }
    public static boolean shifted(String id) { return offset(id, true) != 0 || offset(id, false) != 0; }
    public static DrawPlan apply(String id, DrawPlan plan) {
        if (shifted(id)) {
            Minecraft mc = Minecraft.getMinecraft();
            plan.translate((float)(offset(id, true) * mc.displayWidth), (float)(offset(id, false) * mc.displayHeight), 0);
        }
        return plan;
    }
    public static boolean enabled(String id) {
        Module module = manager == null ? null : manager.find(id);
        return module != null && module.isEnabled();
    }
    public static boolean editing() { return Minecraft.getMinecraft().currentScreen instanceof HudEditorScreen; }
    public static int[] bounds(String id, int width, int height, FontRenderer font) {
        return HudPreviewRenderer.bounds(manager, id, width, height, font);
    }
    public static void move(String id, int left, int top, int width, int height, FontRenderer font) {
        int[] old = bounds(id, width, height, font);
        left = Math.max(0, Math.min(Math.max(0, width - old[2]), left));
        top = Math.max(0, Math.min(Math.max(0, height - old[3]), top));
        Module gui = manager.find("gui");
        number(gui, "hud_" + id + "_x").set(offset(id, true) + (double)(left - old[0]) / Math.max(1, width));
        number(gui, "hud_" + id + "_y").set(offset(id, false) + (double)(top - old[1]) / Math.max(1, height));
    }
    public static void reset() {
        Module gui = manager.find("gui");
        for (String id : IDS) {
            number(gui, "hud_" + id + "_x").set(0D);
            number(gui, "hud_" + id + "_y").set(0D);
        }
    }
    public static void restoreOwnedPositions(Module gui) { }
    public static void open(ModuleManager modules) {
        bind(modules);
        Minecraft mc = modules.context().minecraft();
        if (modules.context().ready()) mc.displayGuiScreen(new HudEditorScreen(modules));
    }
}
