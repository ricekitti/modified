package dev.rice.client.render.openb4;

import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import dev.rice.setting.Setting;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;

/**
 * Hand-written live bindings for the generated {@link OpenB4TargetHudBackend} (and the TargetHUD lines of
 * OpenB4KillAuraBackend). The nested classes are stand-ins for the OpenB4 framework types the verbatim code names, so
 * those lines compile unchanged. Each one does what OpenB4 does at its modules.json defaults, or when that part of
 * OpenB4 is absent from Rice.
 */
public final class OpenB4TargetHudHost {
    private static final Map<String, Object> TARGETHUD_SETTINGS = new HashMap<>();
    private static volatile OpenB4TargetHudBackend enabled;
    private static Runnable modified = () -> { };
    private static IntSupplier guiKey = () -> 0;

    private OpenB4TargetHudHost() {
    }

    /** Registers a Rice-backed TargetHUD setting under its OpenB4 name, for the verbatim {@code link()}. */
    public static void bind(String name, Object setting) {
        TARGETHUD_SETTINGS.put(name, setting);
    }

    public static void enable(OpenB4TargetHudBackend backend) {
        enabled = backend;
    }

    public static void disable() {
        enabled = null;
    }

    /** OpenB4 Profiles.markModified() from the position editor: Rice keeps the position in hidden module settings. */
    public static void onModified(Runnable action) {
        modified = action;
    }

    /** OpenB4 ModuleManager.gui.getKeycode(): the key that opens Rice's GUI also closes the position editor. */
    public static void guiKey(IntSupplier key) {
        guiKey = key;
    }

    /**
     * GuiScreen.handleInput() as OpenB4 compiles against it (Forge 1.8.9 declares {@code throws IOException}; Rice's
     * deobfuscated jar does not), so HudEditor's verbatim {@code catch (IOException ignored)} still compiles.
     */
    public static void handleInput(GuiScreen screen) throws IOException {
        screen.handleInput();
    }

    /** OpenB4 module.Category. */
    public enum Category {
        render
    }

    /** OpenB4 module.Module, reduced to the members TargetHudModule reaches. */
    public abstract static class Module {
        protected static final Minecraft mc = Minecraft.getMinecraft();
        private final String name;

        protected Module(String name, Category category, int key) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public boolean isEnabled() {
            return enabled == this;
        }

        public void onDisable() {
        }

        public void guiButtonToggled(ButtonSetting b) {
        }

        public void guiUpdate() {
        }
    }

    /** OpenB4 ModuleManager lookups: TargetHUD's own settings (bound by name), its enabled state and the GUI key. */
    public static final class ModuleManager {
        public static final GuiKeyModule gui = new GuiKeyModule();

        private ModuleManager() {
        }

        public static Module enabled(String name) {
            return "TargetHUD".equals(name) ? enabled : null;
        }

        public static Object setting(Module module, String name) {
            Object setting = module != null && "TargetHUD".equals(module.getName()) ? TARGETHUD_SETTINGS.get(name) : null;
            if (setting == null) {
                throw new IllegalStateException("OpenB4 setting not bound: " + (module == null ? "null" : module.getName()) + " > " + name);
            }

            return setting;
        }
    }

    /** OpenB4 ModuleManager.gui (its ClickGui module): only its keybind is read here. */
    public static final class GuiKeyModule {
        public int getKeycode() {
            return guiKey.getAsInt();
        }
    }

    /** OpenB4 SliderSetting over a Rice NumberSetting or ChoiceSetting (mode settings read the index), or a fixed default. */
    public static final class SliderSetting {
        private final Setting<?> value;
        private final double fixed;
        public final double defaultValue;

        public SliderSetting(Setting<?> value) {
            this.value = value;
            this.fixed = 0.0;
            this.defaultValue = this.getInput();
        }

        SliderSetting(double fixed) {
            this.value = null;
            this.fixed = fixed;
            this.defaultValue = fixed;
        }

        public double getInput() {
            if (this.value == null) {
                return this.fixed;
            }

            return this.value instanceof ChoiceSetting ? ((ChoiceSetting)this.value).index() : ((NumberSetting)this.value).doubleValue();
        }

        public static String formatNumber(double d) {
            return d == Math.floor(d) ? Long.toString((long) d) : Double.toString(d);
        }
    }

    /** OpenB4 ButtonSetting over a Rice BooleanSetting, or a fixed OpenB4 default. */
    public static final class ButtonSetting {
        private final BooleanSetting value;
        private final boolean fixed;

        public ButtonSetting(BooleanSetting value) {
            this.value = value;
            this.fixed = false;
        }

        ButtonSetting(boolean fixed) {
            this.value = null;
            this.fixed = fixed;
        }

        public boolean isToggled() {
            return this.value == null ? this.fixed : this.value.isEnabled();
        }
    }

    /** OpenB4 SettingsModule values reached here, at OpenB4 modules.json defaults. */
    public static final class SettingsModule {
        public static final ButtonSetting healthAsHearts = new ButtonSetting(false);
        public static final ButtonSetting heartSymbol = new ButtonSetting(false);
        public static final ButtonSetting chatAsInventory = new ButtonSetting(false);

        private SettingsModule() {
        }
    }

    /**
     * OpenB4 GuiModule values reached by HudEditor, at modules.json defaults: dark background on, background blur 30.
     * OpenB4's GuiModule sets blurStrength from that setting; updateMovement only acts inside OpenB4's own ClickGui.
     */
    public static final class GuiModule {
        public static final ButtonSetting darkBackground = new ButtonSetting(true);
        public static final SliderSetting backgroundBlur = new SliderSetting(30.0);
        public static float blurStrength = (float) (3.4 * (backgroundBlur.getInput() / 100.0));

        private GuiModule() {
        }

        public static void updateMovement() {
        }
    }

    /** OpenB4's own client watermark (its branding and username) is not drawn in Rice. */
    public static final class Watermark {
        private Watermark() {
        }

        public static void render(int x, int y, boolean animate) {
        }
    }

    /** OpenB4 Profiles: the editor marks the profile modified when the position changed. */
    public static final class Profiles {
        private Profiles() {
        }

        public static void markModified() {
            modified.run();
        }
    }

    /** OpenB4 Backtrack's ghost entity; Rice has no OpenB4 Backtrack, so there is none. */
    public static final class BacktrackModule {
        public static Entity ghost;

        private BacktrackModule() {
        }
    }

    /** Forge GuiButtonExt; Rice runs on vanilla, so the editor's reset button is a vanilla GuiButton. */
    public static class GuiButtonExt extends GuiButton {
        public GuiButtonExt(int id, int x, int y, int width, int height, String text) {
            super(id, x, y, width, height, text);
        }
    }

    /** Forge RenderWorldLastEvent; raised from Rice's render-world pass (event 6). */
    public static final class RenderWorldLastEvent {
        public final float partialTicks;

        public RenderWorldLastEvent(float partialTicks) {
            this.partialTicks = partialTicks;
        }
    }

    /** Forge @SubscribeEvent. Rice dispatches the render-world event itself. */
    public @interface SubscribeEvent {
    }
}
