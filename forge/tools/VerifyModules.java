import dev.rice.RiceMod;
import dev.rice.forge.ForgeEnvironment;
import dev.rice.inject.Mappings;
import dev.rice.module.Module;
import java.io.File;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

/** Lifecycle smoke test against real game classes, without opening a window. */
public final class VerifyModules {
    static Object unsafe;
    static Method allocate;
    static <T> T empty(Class<T> type) throws Exception { return type.cast(allocate.invoke(unsafe, type)); }
    static void field(Class<?> owner, Object instance, String named, Object value) throws Exception {
        Field field = owner.getDeclaredField(Mappings.mapField(owner.getName().replace('.', '/'), named));
        field.setAccessible(true); field.set(instance, value);
    }
    public static void main(String[] args) throws Exception {
        ForgeEnvironment.setDevelopment(Boolean.parseBoolean(args[0]));
        ForgeEnvironment.initialize();
        Class<?> type = Class.forName("sun.misc.Unsafe");
        Field instance = type.getDeclaredField("theUnsafe"); instance.setAccessible(true);
        unsafe = instance.get(null); allocate = type.getMethod("allocateInstance", Class.class);
        Minecraft mc = empty(Minecraft.class);
        field(Minecraft.class, null, "theMinecraft", mc);
        Path run = Files.createTempDirectory(Paths.get(args[1]), "module-smoke-");
        field(Minecraft.class, mc, "mcDataDir", run.toFile());
        GameSettings settings = empty(GameSettings.class);
        List<KeyBinding> keys = new ArrayList<>();
        for (Field binding : GameSettings.class.getDeclaredFields()) {
            if (binding.getType() == KeyBinding.class && !Modifier.isStatic(binding.getModifiers())) {
                KeyBinding key = new KeyBinding("test." + binding.getName(), keys.size() + 1, "test");
                binding.setAccessible(true); binding.set(settings, key); keys.add(key);
            }
        }
        field(GameSettings.class, settings, "keyBindings", keys.toArray(new KeyBinding[0]));
        field(Minecraft.class, mc, "gameSettings", settings);
        net.minecraft.util.Timer timer = empty(net.minecraft.util.Timer.class);
        field(net.minecraft.util.Timer.class, timer, "timerSpeed", 1.0F);
        field(Minecraft.class, mc, "timer", timer);
        RiceMod mod = new RiceMod();
        try { mod.preInitialize(); throw new AssertionError("Unlicensed module startup accepted"); }
        catch (SecurityException expected) {}
        dev.rice.security.SecurityTest.grantFixture();
        Properties identity = new Properties();
        try (java.io.InputStream resource = VerifyModules.class.getResourceAsStream("/dev/rice/security/release.properties")) {
            if (resource == null) throw new AssertionError("Missing build watermark");
            identity.load(resource);
        }
        if (!dev.rice.security.ReleaseIdentity.build().equals(identity.getProperty("build_id")))
            throw new AssertionError("Inconsistent build watermark");
        mod.preInitialize();
        mod.initialize();
        Set<String> names = new HashSet<>();
        for (Module module : mod.modules().all()) {
            if (!names.add(module.getId())) throw new AssertionError("Duplicate module " + module.getId());
        }
        for (String required : new String[]{"knockbackdisplace", "bowaimbot", "fog", "shaders", "stasis", "antifireball", "fireballpearl", "gui"})
            if (mod.modules().find(required) == null) throw new AssertionError("Missing modified module " + required);
        if (mod.modules().find("scaffold") != null) throw new AssertionError("Removed Scaffold registered");
        String imported = "Raven bS / Rise / Vape / OpenB4 / OpenJade / Myau / Mayu / Meow / LiquidBounce";
        String cleaned = dev.rice.client.render.GuiText.clean(imported);
        if (!cleaned.equals("Standard / Standard / Standard / Standard / Standard / Standard / Standard / Standard / Standard"))
            throw new AssertionError("Other client names remain in GUI text: " + cleaned);
        if (!dev.rice.client.render.GuiText.clean("Rice Slinky injected").equals("Rice injected"))
            throw new AssertionError("GUI branding mismatch");
        dev.rice.client.render.DrawPlan plan = new dev.rice.client.render.DrawPlan().text(imported, 0, 0, -1, false, 12);
        if (!cleaned.equals(plan.operations().get(0).value)) throw new AssertionError("Rendered GUI text bypassed branding");
        Module displace = mod.modules().find("knockbackdisplace");
        if (displace.findSetting("legit_void") == null) throw new AssertionError("Missing Legit Void setting");
        Set<String> reference = new HashSet<>(Arrays.asList((
            "aimassist,autoclicker,autosoup,bridgeassist,player_esp,fastplace,reach,velocity,antibot,autoblock,teams,gui,timer,fakelag,criticals,noitemrelease,blink,chest_esp,hitboxes,mod_overlay,notifications,nojumpdelay,sprint,backtrack,freelook,autotool,hitselect,weapons,refill,autopot,invwalk,keepsprint,lagrange,sprintreset,friends,name_tags,pointers,nohitdelay,pingfix,chams,nousedelay,indicators,block_esp,item_esp,fastmine,safewalk,nohurtcam,strafe,fastaccel,instantstop,noslow,itemusefix,timerrange,autorod,antidebuff,piercing,trajectories,knockbackdelay,debug,nullmove,ghosthand,invmanager,autoweapon,knockbackdisplace,blockin,clutch,stasis,shaders,fireball_pearl,fog,antifireball,bowaimbot"
        ).split(",")));
        if (!names.equals(reference)) {
            Set<String> missing = new HashSet<>(reference); missing.removeAll(names);
            Set<String> extra = new HashSet<>(names); extra.removeAll(reference);
            throw new AssertionError("Modified reference registry mismatch; missing=" + missing + " extra=" + extra);
        }
        if (!Arrays.equals(dev.rice.client.render.HudLayout.IDS, new String[]{"mod_overlay", "notifications"}))
            throw new AssertionError("Modified HUD editor contains non-reference widgets");
        Module gui = mod.modules().find("gui");
        if (gui.findSetting("edit_hud") != null || gui.settingVisible("hud_edit"))
            throw new AssertionError("Legacy Edit HUD module setting remains visible");
        List<String> rows = new ArrayList<>();
        for (Module module : mod.modules().all()) rows.add(module.getId() + "\t" + module.getName());
        Files.write(Paths.get(args[1]).resolve("module-inventory.tsv"), rows, java.nio.charset.StandardCharsets.UTF_8);
        mod.modules().shutdown();
        if (dev.rice.client.hook.HookLifecycle.active()) throw new AssertionError("Hooks remained active after shutdown");
        System.out.println("PASS: real module lifecycle, " + names.size() + " registered modules, modified features/settings, isolated configuration, shutdown.");
    }
}
