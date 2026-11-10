package dev.rice.forge;

import dev.rice.RiceMod;
import dev.rice.client.RiceCommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import dev.rice.security.LicenseController;
import dev.rice.security.LicenseRuntime;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.GuiScreenEvent;

@Mod(modid = "ricemodified", name = "Rice Modified", version = "0.2.2-forge",
    acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true, acceptableRemoteVersions = "*")
public final class RiceForgeMod {
    private RiceMod client;
    private LicenseController licensing;
    private boolean prompted, keyDown;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ForgeEnvironment.initialize();
        dev.rice.inject.Platform.log("Rice build " + dev.rice.security.ReleaseIdentity.build());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        licensing = new LicenseController();
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void screenKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (dev.rice.client.ClientEventBridge.beforeScreenKeyboardInput(event.gui)) event.setCanceled(true);
    }

    @SubscribeEvent
    public void screenMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * event.gui.width / Math.max(1, mc.displayWidth);
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / Math.max(1, mc.displayHeight) - 1;
        if (client != null && isModifiedMenu(event.gui) && Mouse.getEventButton() == 0
            && Mouse.getEventButtonState() && editHudHovered(event.gui, mouseX, mouseY)) {
            dev.rice.client.render.HudLayout.open(client.modules());
            event.setCanceled(true);
            return;
        }
        if (dev.rice.client.ClientEventBridge.beforeScreenMouseInput(event.gui)) event.setCanceled(true);
    }

    @SubscribeEvent
    public void drawModifiedMenu(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (client == null || !isModifiedMenu(event.gui)) return;
        int left = event.gui.width / 2 - 55, top = event.gui.height - 26;
        boolean hover = editHudHovered(event.gui, event.mouseX, event.mouseY);
        Gui.drawRect(left, top, left + 110, top + 20, hover ? 0xFF9B55C0 : 0xFF6F3B88);
        String label = "Edit HUD";
        net.minecraft.client.gui.FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        font.drawStringWithShadow(label, left + (110 - font.getStringWidth(label)) / 2, top + 6, 0xFFFFFFFF);
    }

    private static boolean isModifiedMenu(GuiScreen screen) {
        return screen instanceof dev.rice.client.render.RecoveredGuiScreen;
    }

    private static boolean editHudHovered(GuiScreen screen, int mouseX, int mouseY) {
        int left = screen.width / 2 - 55, top = screen.height - 26;
        return mouseX >= left && mouseX < left + 110 && mouseY >= top && mouseY < top + 20;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (LicenseRuntime.valid()) {
            if (client == null) {
                RiceMod starting = new RiceMod();
                try {
                    starting.preInitialize(); starting.initialize();
                    ClientCommandHandler.instance.registerCommand(new RiceCommand(starting.modules()));
                    client = starting;
                } catch (Exception failure) {
                    LicenseRuntime.revoke();
                    if (starting.modules() != null) starting.modules().shutdown();
                    dev.rice.client.ClientEventBridge.install(null);
                    dev.rice.client.hook.GameHooks.install(null);
                    dev.rice.inject.Platform.log("Rice startup failed", failure);
                }
            }
            return;
        }
        if (client != null) {
            try { client.modules().shutdown(); }
            finally {
                dev.rice.client.ClientEventBridge.install(null);
                dev.rice.client.hook.GameHooks.install(null);
                client = null; prompted = false;
            }
        }
        boolean down = Keyboard.isCreated() && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean pressed = down && !keyDown; keyDown = down;
        if ((!prompted && mc.currentScreen instanceof GuiMainMenu) ||
            (pressed && (mc.currentScreen == null || mc.currentScreen instanceof GuiMainMenu))) {
            prompted = true;
            mc.displayGuiScreen(new RiceLicenseScreen(mc.currentScreen, licensing));
        }
    }
}
