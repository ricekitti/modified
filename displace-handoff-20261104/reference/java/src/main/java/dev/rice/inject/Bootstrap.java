package dev.rice.inject;

import com.google.common.util.concurrent.ListenableFuture;
import dev.rice.RiceMod;
import dev.rice.client.ClientEventBridge;
import dev.rice.client.hook.GameHooks;
import dev.rice.inject.transform.RicePatches;
import dev.rice.inject.transform.TransformerEngine;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;

public final class Bootstrap {
   private static final String INJECTED_PROPERTY = "rice.injected";

   private Bootstrap() {
   }

   public static void start(String namespace, String payloadPath) throws Throwable {
      synchronized (Bootstrap.class) {
         Field managerField = GameHooks.class.getDeclaredField("modules");
         managerField.setAccessible(true);
         Object previousManager = managerField.get(null);
         if (previousManager != null) {
            System.setProperty("rice.patches.installed", "true");
            Field shutdownField = previousManager.getClass().getDeclaredField("shutdown");
            shutdownField.setAccessible(true);
            if (!((AtomicBoolean)shutdownField.get(previousManager)).get()) {
               Platform.log("Rice is already running in this process; ignoring second injection");
               return;
            }
            ClientEventBridge.install(null);
            GameHooks.install(null);
            Platform.log("Previous Rice session was unloaded; starting a new session");
         }
         System.clearProperty("rice.injected");

         try {
            System.setProperty("rice.injected", "true");
            Platform.initialize(Platform.Namespace.of(namespace), payloadPath, true);

            try (InputStream mappings = Platform.openResource("/dev/rice/inject/mappings.txt")) {
               if (mappings == null && Platform.getNamespace() != Platform.Namespace.NAMED) {
                  throw new IllegalStateException("Payload for " + namespace + " is missing " + "/dev/rice/inject/mappings.txt");
               }

               Mappings.load(mappings);
            }

            Platform.log("Runtime: " + Platform.describe() + ", " + Mappings.size() + " mappings, payload " + payloadPath);
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) {
               throw new IllegalStateException("Minecraft has not been created yet");
            }
            ListenableFuture<Object> task = mc.addScheduledTask(new Runnable() {
               @Override
               public void run() {
                  Bootstrap.initializeOnClientThread();
               }
            });
            task.get(2L, TimeUnit.MINUTES);
         } catch (Throwable failure) {
            System.clearProperty("rice.injected");
            throw failure;
         }
      }
   }

   private static void initializeOnClientThread() {
      RiceMod mod = new RiceMod();

      try {
         mod.preInitialize();
         mod.initialize();
         if (System.getProperty("rice.patches.installed") == null) {
            TransformerEngine transformer = new TransformerEngine();
            transformer.applyAll(RicePatches.create(Platform.isForge()));
            if (transformer.getFailed() != 0) {
               throw new IllegalStateException("Failed to install " + transformer.getFailed() + " Rice class patches");
            }
            System.setProperty("rice.patches.installed", "true");
         } else {
            Platform.log("Reusing the installed Rice game hooks");
         }
         Platform.log("Rice Semantic 0.5.0-settings initialized with " + mod.modules().all().size() + " modules");
      } catch (Throwable var2) {
         try {
            if (mod.modules() != null) {
               mod.modules().shutdown();
            }
         } catch (Throwable cleanupFailure) {
            var2.addSuppressed(cleanupFailure);
         } finally {
            System.clearProperty("rice.injected");
         }
         Platform.log("Rice initialization failed", var2);
         throw var2 instanceof RuntimeException ? (RuntimeException)var2 : new RuntimeException(var2);
      }
   }
}
