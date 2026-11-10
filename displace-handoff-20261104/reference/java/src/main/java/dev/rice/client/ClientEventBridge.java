package dev.rice.client;

import dev.rice.client.hook.GameHooks;
import dev.rice.client.openjade.OpenJadeHost;
import dev.rice.client.packet.PacketPipeline;
import dev.rice.client.render.RecoveredGuiScreen;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.Module;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.AutoBlockModule;
import dev.rice.setting.Setting;
import io.netty.channel.Channel;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class ClientEventBridge {
   private static ClientEventBridge instance;
   private final ModuleManager modules;
   private final KeyBinding openGui;
   private final CommandBase command;
   private boolean keyPending;
   private boolean mousePending;
   private final boolean[] bindDown = new boolean[128];
   private final boolean[] profileDown = new boolean[128];
   private Channel attachedChannel;
   private static Field channelField;

   public ClientEventBridge(ModuleManager modules, KeyBinding openGui, CommandBase command) {
      this.modules = modules;
      this.openGui = openGui;
      this.command = command;
   }

   public static void install(ClientEventBridge bridge) {
      synchronized (ClientEventBridge.class) {
         ClientEventBridge previous = instance;
         instance = bridge;
         if (previous != null && previous != bridge) {
            Channel channel = previous.attachedChannel;
            previous.attachedChannel = null;
            previous.keyPending = false;
            previous.mousePending = false;
            if (channel != null && channel.pipeline().get("rice_semantic_packets") != null) {
               channel.pipeline().remove("rice_semantic_packets");
            }
         }
      }
   }

   public static void clientTick(boolean start) {
      ClientEventBridge bridge = instance;
      if (bridge != null) {
         if (start) {
            bridge.attachPipeline();
            // OpenJade: Minecraft.runTick head (TickStart, TickEnd START, RotationHandler.updateTargetRotation)
            dev.rice.client.openjade.JadeRuntime.tickStart();
         }

         bridge.modules.clientTick(start);
         if (!start) {
            OpenJadeHost.tickEnd();
            // OpenB4 ClickGuiMod tick end: rotation upkeep while riding (OpenB4 Scaffold pipeline).
            // OpenJade: Minecraft.runTick returns (TickEnd END -> Jade module onUpdate)
            dev.rice.client.openjade.JadeRuntime.tickEnd();
         }
      }
   }

   // The render-tick route works on Lunar even when its native event-loop redirect misses.
   // GUI presses and releases must also be sampled while its screen owns focus.
   private void pollBinds() {
      try {
         Minecraft minecraft = Minecraft.getMinecraft();
         this.pollProfileBinds(minecraft);
         boolean inGame = minecraft != null && minecraft.currentScreen == null;
         Module guiCandidate=this.modules.find("gui");
         int guiBind=guiCandidate==null?0:guiCandidate.getBinding();
         if(guiCandidate instanceof dev.rice.module.impl.GuiModule && guiBind>0 && guiBind<this.bindDown.length){
            boolean profileOwnsKey=false;
            for(dev.rice.config.RecoveredConfiguration.ProfileBinding profile:this.modules.input().profileBindings())if(profile.binding==guiBind)profileOwnsKey=true;
            boolean down=physicalBind(guiBind);
            ((dev.rice.module.impl.GuiModule)guiCandidate).pollBinding(down,inGame&&!profileOwnsKey||isConfigurationScreen(minecraft.currentScreen));
            // The GUI key belongs to the menu; never send it through generic module toggles a second time.
            this.bindDown[guiBind]=down;
         }

         for (Module module : this.modules.all()) {
            int bind = module.getBinding();
            if (bind <= 0 || bind >= this.bindDown.length || bind == guiBind) {
               continue;
            }
            boolean down = inGame && physicalBind(bind);
            if (down != this.bindDown[bind]) {
               this.bindDown[bind] = down;
               this.modules.input().moduleBindings(bind, (byte)(down ? 1 : 0));
            }
         }
      } catch (Throwable ignored) {
      }
   }

   private static boolean physicalBind(int bind) {
      int code=NativeKeyMap.gameCode(bind);
      if(code<0){int button=code+100;return button>=0&&button<Mouse.getButtonCount()&&Mouse.isButtonDown(button);}
      return code>0&&Keyboard.isKeyDown(code);
   }

   /** Profiles use the same physical-key route as modules, independent of missed native input events. */
   private void pollProfileBinds(Minecraft minecraft) {
      if (minecraft == null) return;
      boolean[] bound = new boolean[this.profileDown.length];
      for (dev.rice.config.RecoveredConfiguration.ProfileBinding binding : this.modules.input().profileBindings()) {
         if (binding.binding > 0 && binding.binding < bound.length) bound[binding.binding] = true;
      }
      Module gui = this.modules.find("gui");
      boolean active = this.modules.context().ready() && minecraft.inGameHasFocus && minecraft.currentScreen == null
         && (gui == null || !gui.isEnabled());
      boolean handled = false;
      for (int id = 1; id < this.profileDown.length; id++) {
         int code = NativeKeyMap.gameCode(id);
         boolean down = code < 0 ? code + 100 >= 0 && code + 100 < Mouse.getButtonCount() && Mouse.isButtonDown(code + 100)
            : code > 0 && Keyboard.isKeyDown(code);
         boolean pressed = down && !this.profileDown[id];
         // Track physical state even in menus/unfocused, so a key held on returning to the game cannot load a config.
         this.profileDown[id] = down;
         if (bound[id]) {
            // A config bind takes precedence over a module using the same key.
            this.bindDown[id] = down;
            if (active && pressed && !handled) {
               handled = true;
               this.modules.input().profileBindings(id, (byte)1);
            }
         }
      }
   }

   public static void renderTick(boolean start) {
      ClientEventBridge bridge = instance;
      if (bridge != null) {
         if (start) {
            // Timer changes the cadence of client ticks. Poll binds from the
            // render loop instead so a slow/fast Timer can always be toggled
            // back off and every other module bind remains responsive.
            bridge.pollBinds();
         }

         bridge.modules.renderTick(start);
         if (!start) {
            GameHooks.afterFrameRender();
         }
      }
   }

   public static boolean keyboardNext() {
      ClientEventBridge bridge = instance;
      if (bridge != null && bridge.keyPending) {
         bridge.keyPending = false;
         bridge.onKeyInput();
      }

      boolean next = Keyboard.next();
      if (bridge != null) {
         bridge.keyPending = next;
      }

      return next;
   }

   public static boolean mouseNext() {
      ClientEventBridge bridge = instance;
      if (bridge != null && bridge.mousePending) {
         bridge.mousePending = false;
         ForgeInputAdapter.mouse(bridge.modules);
      }

      boolean next = Mouse.next();

      while (next && bridge != null && bridge.swallowMouseEvent()) {
         next = Mouse.next();
      }

      if (bridge != null) {
         bridge.mousePending = next;
         if (!next) {
            Module module = bridge.modules.find("autoblock");
            if (module instanceof AutoBlockModule) {
               ((AutoBlockModule)module).prepareAttackInput(bridge.modules.context());
            }
         }
      }

      return next;
   }

   private boolean swallowMouseEvent() {
      if (GameHooks.openJadeMouseEvent(Mouse.getEventButton())) return true;
      // OpenB4 Scaffold onMouse (Forge MouseEvent): cancels clicks while it is placing.
      Module blockIn = this.modules.find("blockin");
      if (blockIn instanceof dev.rice.module.impl.BlockInModule
         && ((dev.rice.module.impl.BlockInModule)blockIn).swallowMouseEvent()) return true;
      Module module = this.modules.find("autoblock");
      return module instanceof AutoBlockModule && ((AutoBlockModule)module).swallowMouseEvent(this.modules.context());
   }

   private void onKeyInput() {
      ForgeInputAdapter.keyboard(this.modules);
      if (this.openGui.isPressed() && this.needsConfiguration()) {
         Minecraft.getMinecraft().displayGuiScreen(new GuiModuleList(this.modules));
      }
   }

   private boolean needsConfiguration() {
      Module gui = this.modules.find("GUI");
      if (gui == null) {
         return true;
      } else {
         for (Setting<?> setting : gui.getSettings()) {
            if (!setting.isConfigured()) {
               return true;
            }
         }

         return false;
      }
   }

   public static void handleKeyboardInput(GuiScreen screen) throws IOException {
      ClientEventBridge bridge = instance;
      if (bridge == null || !bridge.screenKeyInput(screen)) {
         screen.handleKeyboardInput();
      }
   }

   public static void handleMouseInput(GuiScreen screen) throws IOException {
      ClientEventBridge bridge = instance;
      if (bridge == null || !bridge.screenMouseInput(screen)) {
         screen.handleMouseInput();
      }
   }

   private boolean screenKeyInput(GuiScreen screen) {
      if (GuiMenuCommand.handle(screen, Keyboard.getEventKey(), Keyboard.getEventKeyState(), this.modules)) {
         return true;
      } else {
         return isConfigurationScreen(screen) ? false : (ForgeInputAdapter.keyboard(this.modules) & 0xFF) != 0;
      }
   }

   private boolean screenMouseInput(GuiScreen screen) {
      return isConfigurationScreen(screen) ? false : (ForgeInputAdapter.mouse(this.modules) & 0xFF) != 0;
   }

   private static boolean isConfigurationScreen(GuiScreen screen) {
      return screen instanceof RecoveredGuiScreen || screen instanceof GuiModuleList || screen instanceof GuiModuleSettings;
   }

   public static void loadWorld(Minecraft minecraft) {
      ClientEventBridge bridge = instance;
      if (bridge != null && minecraft.theWorld != null) {
         bridge.modules.worldUnload();
      }
   }

   public static void disconnect() {
      final ClientEventBridge bridge = instance;
      if (bridge != null) {
         bridge.attachedChannel = null;
         Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
               bridge.modules.worldUnload();
            }
         });
      }
   }

   private void attachPipeline() {
      NetHandlerPlayClient handler = Minecraft.getMinecraft().getNetHandler();
      final Channel channel = handler == null ? null : channel(handler.getNetworkManager());
      if (channel != null && channel != this.attachedChannel && channel.isOpen()) {
         this.attachedChannel = channel;
         channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
               synchronized (ClientEventBridge.class) {
                  try {
                     // Keep the existing Java 8 synthetic accessor schema for live replacement.
                     Field active = ClientEventBridge.class.getDeclaredField("instance");
                     active.setAccessible(true);
                     if (active.get(null) == ClientEventBridge.this && channel.isOpen()
                        && channel.pipeline().get("rice_semantic_packets") == null
                        && channel.pipeline().get("packet_handler") != null) {
                        channel.pipeline().addBefore("packet_handler", "rice_semantic_packets", new PacketPipeline(ClientEventBridge.this.modules));
                     }
                  } catch (ReflectiveOperationException failure) {
                     throw new IllegalStateException("Could not check the active Rice session", failure);
                  }
               }
            }
         });
      }
   }

   private static Channel channel(NetworkManager manager) {
      if (channelField == null) {
         channelField = ReflectionHelper.findField(NetworkManager.class, "channel", "field_150746_k");
      }

      try {
         return (Channel)channelField.get(manager);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException(var2);
      }
   }

   public static boolean sendChatMessage(String message) {
      ClientEventBridge bridge = instance;
      if (bridge != null && message != null && message.startsWith("/")) {
         String[] words = message.substring(1).trim().split(" ");
         String name = words[0];
         if (name.equalsIgnoreCase(bridge.command.getCommandName())
            || bridge.command.getCommandAliases() != null && bridge.command.getCommandAliases().contains(name)) {
            Minecraft minecraft = Minecraft.getMinecraft();

            try {
               bridge.command.processCommand(minecraft.thePlayer, Arrays.copyOfRange(words, 1, words.length));
            } catch (CommandException var7) {
               ChatComponentTranslation error = new ChatComponentTranslation(var7.getMessage(), var7.getErrorObjects());
               error.getChatStyle().setColor(EnumChatFormatting.RED);
               minecraft.thePlayer.addChatMessage(error);
            } catch (Throwable var8) {
               ChatComponentText errorx = new ChatComponentText("Rice command failed: " + var8);
               errorx.getChatStyle().setColor(EnumChatFormatting.RED);
               minecraft.thePlayer.addChatMessage(errorx);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
