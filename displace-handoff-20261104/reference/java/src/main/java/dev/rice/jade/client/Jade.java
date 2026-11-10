package dev.rice.jade.client;

import dev.rice.jade.client.common.ConfigEntry;
import dev.rice.jade.client.common.ModuleManager;
import dev.rice.jade.client.common.RelationManager;
import dev.rice.jade.client.core.QueuedPacketDispatcher;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;

/**
 * Rice host shim for OpenJade's global {@code jade.client.Jade}: the members the ported Opsec code
 * reads. Field names and types match upstream.
 */
public final class Jade {
   public static boolean profilingEnabled = false;
   public static Minecraft mc = Minecraft.getMinecraft();
   /** Bridges name lookups to Rice's Friends relationship flag. */
   public static RelationManager relationManager = new RelationManager();
   /** Rice has no Jade config profiles; upstream guards every use with a null check. */
   public static ConfigEntry Grq;
   /** Created and registered on the Jade event bus by {@code JadeRuntime}. */
   public static QueuedPacketDispatcher nbT;
   private static final ModuleManager moduleManager = new ModuleManager();
   private static final AtomicInteger threads = new AtomicInteger();
   private static final ExecutorService executorService = Executors.newCachedThreadPool(task -> {
      Thread thread = new Thread(task, "rice-openjade-" + threads.incrementAndGet());
      thread.setDaemon(true);
      return thread;
   });

   private Jade() {
   }

   public static ModuleManager getModuleManager() {
      return moduleManager;
   }

   public static ExecutorService getExecutor() {
      return executorService;
   }

   /** Upstream opens Jade's own pending screens (ClickGui, welcome); Rice has none. */
   public static void openPendingScreen() {
   }

   /** Upstream re-reads Jade's Timer keybind; Rice owns keybinds. */
   public static void refreshTimerModuleKeybind() {
   }
}
