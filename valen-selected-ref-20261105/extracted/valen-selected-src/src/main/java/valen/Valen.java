/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package valen;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.client.Minecraft;
import valen.clickgui.ClickGui;
import valen.command.CommandManager;
import valen.event.PostSetSliderEvent;
import valen.event.RunGameLoopEvent;
import valen.helper.DebugHelper;
import valen.helper.MouseHelper;
import valen.helper.PingHelper;
import valen.helper.RotationHelper;
import valen.lag.handler.UnifiedLagHandler;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.event.entity.EntityJoinWorldEvent;
import valen.lunar.forge.fml.common.Mod;
import valen.lunar.forge.fml.common.event.FMLInitializationEvent;
import valen.lunar.forge.fml.common.eventhandler.SubscribeEvent;
import valen.lunar.forge.fml.common.gameevent.TickEvent;
import valen.mixin.impl.accessor.IAccessorMinecraft;
import valen.module.Module;
import valen.module.ModuleManager;
import valen.module.impl.render.LicenseNotice;
import valen.module.impl.render.Notifications;
import valen.utility.BlockHighlightSharedHandler;
import valen.utility.ClientPreferences;
import valen.utility.FrozenEntitySync;
import valen.utility.LicenseStatusService;
import valen.utility.ModuleUtils;
import valen.utility.PacketsHandler;
import valen.utility.PlayerRelationsManager;
import valen.utility.ProtectionEstimator;
import valen.utility.ReflectionUtils;
import valen.utility.TimerController;
import valen.utility.Utils;
import valen.utility.config.ConfigManager;

@Mod(modid="valen", name="Valen", version="18", acceptedMinecraftVersions="[1.8.9]")
public class Valen {
    public static boolean DEBUG = false;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static final TimerController timerController = new TimerController(d -> {
        ((IAccessorMinecraft)Valen.mc).valen$lunar$getTimer().timerSpeed = (float)d;
    });
    private Object timerWorld;
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private static final ExecutorService cachedExecutor = Executors.newCachedThreadPool();
    public static ModuleManager moduleManager;
    public static ClickGui clickGui;
    public static CommandManager commandManager;
    public static PlayerRelationsManager playerRelationsManager;
    public static PacketsHandler packetsHandler;
    public static UnifiedLagHandler lagHandler;
    public static ConfigManager configManager;
    public static ClientPreferences preferences;

    public Valen() {
        moduleManager = new ModuleManager();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent fMLInitializationEvent) {
        Runtime.getRuntime().addShutdownHook(new Thread(scheduledExecutor::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(cachedExecutor::shutdown));
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DebugHelper());
        MinecraftForge.EVENT_BUS.register(new MouseHelper());
        MinecraftForge.EVENT_BUS.register(RotationHelper.get());
        MinecraftForge.EVENT_BUS.register(new PingHelper());
        packetsHandler = new PacketsHandler();
        MinecraftForge.EVENT_BUS.register(packetsHandler);
        MinecraftForge.EVENT_BUS.register(new ModuleUtils());
        lagHandler = new UnifiedLagHandler();
        MinecraftForge.EVENT_BUS.register(lagHandler);
        ReflectionUtils.setupFields();
        playerRelationsManager = new PlayerRelationsManager();
        playerRelationsManager.load();
        moduleManager.register();
        MinecraftForge.EVENT_BUS.register(new ProtectionEstimator());
        MinecraftForge.EVENT_BUS.register(new BlockHighlightSharedHandler());
        preferences = new ClientPreferences();
        clickGui = new ClickGui();
        ReflectionUtils.setKeyBindings();
        configManager = new ConfigManager();
        configManager.loadActiveOnStartup();
        commandManager = new CommandManager();
        Notifications.arm();
        MinecraftForge.EVENT_BUS.register(new LicenseNotice());
        LicenseStatusService.beginFetch(cachedExecutor);
    }

    @SubscribeEvent
    public void onGameLoop(RunGameLoopEvent runGameLoopEvent) {
        if (!Utils.nullCheck() || this.timerWorld != Valen.mc.theWorld) {
            timerController.clear();
            this.timerWorld = Valen.mc.theWorld;
        } else {
            timerController.apply();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase == TickEvent.Phase.END) {
            if (Utils.nullCheck()) {
                if (ReflectionUtils.ERROR) {
                    Utils.sendMessage("&cThere was an error, relaunch the game.");
                    ReflectionUtils.ERROR = false;
                }
                MouseHelper.updateWheelCache();
                if (ModuleManager.friends != null) {
                    ModuleManager.friends.pollBind(Valen.mc.currentScreen == null && Valen.mc.thePlayer.getHealth() > 0.0f);
                }
                int n = Valen.mc.currentScreen == null && preferences != null ? preferences.pollProfileBind() : 0;
                for (Module module : Valen.getModuleManager().getModules()) {
                    if (Valen.mc.currentScreen == null && module.canBeEnabled()) {
                        if (n != 0 && module.getKeycode() == n) {
                            module.syncKeyBindState();
                        } else {
                            module.onKeyBind();
                        }
                    } else if (Valen.mc.currentScreen instanceof ClickGui) {
                        module.guiUpdate();
                        module.syncKeyBindState();
                    } else {
                        module.syncKeyBindState();
                    }
                    if (!module.isEnabled()) continue;
                    module.onUpdate();
                }
                if (Valen.mc.currentScreen instanceof ClickGui && Valen.mc.thePlayer.getHealth() <= 0.0f) {
                    mc.displayGuiScreen(null);
                }
            }
        } else {
            MouseHelper.clearWheelCache();
        }
    }

    @SubscribeEvent
    public void onPostSetSlider(PostSetSliderEvent postSetSliderEvent) {
        clickGui.onSliderChange();
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent entityJoinWorldEvent) {
        if (entityJoinWorldEvent.entity == Valen.mc.thePlayer) {
            FrozenEntitySync.get().clearAll();
        }
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public static ExecutorService getCachedExecutor() {
        return cachedExecutor;
    }

    public static void handleFrozenKeybinds() {
        if (!Utils.nullCheck()) {
            return;
        }
        MouseHelper.updateWheelCache();
        if (ModuleManager.friends != null) {
            ModuleManager.friends.pollBind(Valen.mc.currentScreen == null && Valen.mc.thePlayer.getHealth() > 0.0f);
        }
        if (Valen.mc.currentScreen == null) {
            for (Module module : Valen.getModuleManager().getModules()) {
                if (!module.canBeEnabled()) continue;
                module.onKeyBind();
            }
        } else if (Valen.mc.currentScreen instanceof ClickGui) {
            for (Module module : Valen.getModuleManager().getModules()) {
                module.guiUpdate();
                module.syncKeyBindState();
            }
        } else {
            for (Module module : Valen.getModuleManager().getModules()) {
                module.syncKeyBindState();
            }
        }
    }
}

