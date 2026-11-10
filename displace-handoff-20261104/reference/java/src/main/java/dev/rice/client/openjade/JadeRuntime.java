package dev.rice.client.openjade;

import dev.rice.jade.agent.JadeAgentHooks;
import dev.rice.jade.client.Jade;
import dev.rice.jade.client.common.ClientUtils;
import dev.rice.jade.client.common.DangerousModules;
import dev.rice.jade.client.common.EventBus;
import dev.rice.jade.client.common.EventPhase;
import dev.rice.jade.client.common.FontManager;
import dev.rice.jade.client.common.RotationHandler;
import dev.rice.jade.client.common.Subscribe;
import dev.rice.jade.client.core.QueuedPacketDispatcher;
import dev.rice.jade.client.event.RenderTickEvent;
import dev.rice.jade.client.event.TickEndEvent;
import dev.rice.jade.client.module.Module;
import dev.rice.jade.client.module.client.Gui;
import dev.rice.jade.client.module.minigames.Opsec;
import dev.rice.jade.client.module.render.Arraylist;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * Host runtime for the byte-faithful OpenJade port (Opsec).
 *
 * <p>Start-up mirrors upstream {@code Jade.init}: RotationHandler is registered on the Jade event bus first,
 * then the queued packet dispatcher ({@code Jade.nbT}), then Jade's own tick-end listener (upstream
 * {@code Jade#onTickEnd}: per-module {@code onUpdate()}), and the default "jade" theme is applied
 * (upstream {@code ThemeManager#fHn0}). Jade modules are enabled/disabled through upstream's own
 * {@code Module#enable/disable} (ModuleState: event-bus registration + onEnable/onDisable).
 *
 * <p>Every bridge below forwards to the verbatim {@link JadeAgentHooks} method that Jade's agent
 * injects at the same Minecraft site, but only while Opsec is enabled; otherwise the Rice/vanilla path runs
 * untouched. The OpenJade Scaffold and Clutch Assist were removed in v119 (replaced by Mayu Scaffold and
 * AutoClutch), together with the movement, rotation, click and hotbar bridges only they used.
 */
public final class JadeRuntime {
   private static final List<Integer> DEFAULT_THEME = Arrays.asList(-12326778, -15630261);
   private static boolean initialized;
   private static Opsec opsec;
   private static volatile boolean active;

   private JadeRuntime() {
   }

   public static synchronized void init(dev.rice.module.ModuleManager rice) {
      if (initialized) {
         return;
      }

      initialized = true;
      EventBus.register(RotationHandler.getInstance());
      Jade.nbT = new QueuedPacketDispatcher();
      EventBus.register(Jade.nbT);
      EventBus.register(new TickEndListener());
      int accent = DEFAULT_THEME.get(0);
      Gui.accent.setRgb(accent >> 16 & 0xFF, accent >> 8 & 0xFF, accent & 0xFF);
      Arraylist.setGradientColors(DEFAULT_THEME);
      opsec = new Opsec();
      Jade.getModuleManager().register(opsec);
      Jade.getModuleManager().setToggleListener(JadeRuntime::refreshActive);
   }

   public static Opsec opsec() {
      return opsec;
   }

   public static void setEnabled(Module module, boolean enabled) {
      if (module == null) {
         return;
      }

      if (enabled) {
         module.enable();
      } else {
         module.disable();
      }

      refreshActive();
   }

   private static void refreshActive() {
      active = opsec != null && opsec.isEnabled();
   }

   public static boolean active() {
      return active;
   }

   // --- Minecraft.runTick head / returns ---------------------------------------------------------
   public static void tickStart() {
      if (active) {
         JadeAgentHooks.onRunTickStart();
      }
   }

   public static void tickEnd() {
      if (active) {
         JadeAgentHooks.onRunTickEnd();
      }
   }

   // --- Minecraft.runGameLoop head ----------------------------------------------------------------
   public static void gameLoopStart(Minecraft minecraft) {
      if (active) {
         JadeAgentHooks.onRunGameLoopStart(minecraft);
      }
   }

   // --- GuiIngame.renderGameOverlay returns (upstream onRenderHud: posts RenderTickEvent END) ------
   public static void renderHud(float partialTicks) {
      if (active) {
         EventBus.post(new RenderTickEvent(EventPhase.END, partialTicks));
      }
   }

   // --- EntityRenderer.renderWorldPass, before the hand-pass depth clear ---------------------------
   public static void renderWorldLast(float partialTicks) {
      if (active) {
         JadeAgentHooks.onRenderWorldLast(partialTicks);
      }
   }

   // --- NetworkManager.sendPacket / channelRead0 heads -------------------------------------------------
   public static boolean networkSend(Object packet) {
      return active && JadeAgentHooks.onNetworkSend(packet);
   }

   public static boolean networkReceive(Object packet) {
      return active && JadeAgentHooks.onNetworkReceive(packet);
   }

   // --- GuiContainer (Opsec) --------------------------------------------------------------------------------
   public static void containerDrawSlotBegin(Object container, Object slot) {
      if (active) {
         JadeAgentHooks.onContainerDrawSlotBegin(container, slot);
      }
   }

   public static void containerDrawSlotEnd(Object container) {
      if (active) {
         JadeAgentHooks.onContainerDrawSlotEnd(container);
      }
   }

   public static Object containerDisplayedStack(Object container, Object stack) {
      return active ? JadeAgentHooks.onContainerDisplayedStack(container, stack) : stack;
   }

   public static void containerDrawScreen(Object container, int mouseX, int mouseY) {
      if (active) {
         JadeAgentHooks.onContainerDrawScreen(container, mouseX, mouseY);
      }
   }

   public static boolean containerManualInput(Object container) {
      return active && JadeAgentHooks.onContainerManualInput(container);
   }

   public static boolean containerClick(Object container, Object slot, int slotId, int button, int mode) {
      return active && JadeAgentHooks.onContainerClick(container, slot, slotId, button, mode);
   }

   /**
    * Upstream {@code Jade#onTickEnd}, restricted to what the ported modules use: on the END phase, while in
    * a world, refresh custom fonts, check the (empty unless provided) dangerous-module registry, then run every
    * enabled Jade module's {@code onUpdate()}. Keybind polling is Rice's job, so it is not repeated here.
    */
   public static final class TickEndListener {
      @Subscribe
      public void onTickEnd(TickEndEvent event) {
         if (event.eventPhase == EventPhase.END && ClientUtils.isInWorld()) {
            FontManager.tick();
            DangerousModules.checkLoadedModules();
            for (Module module : Jade.getModuleManager().getModules()) {
               if (module.isEnabled()) {
                  module.onUpdate();
               }
            }
         }
      }
   }
}
