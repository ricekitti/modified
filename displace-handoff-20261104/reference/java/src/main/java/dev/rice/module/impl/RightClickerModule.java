package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.input.InputSoundBridge;
import dev.rice.client.MinecraftFields;
import dev.rice.client.SemanticEvent;
import dev.rice.client.StatusText;
import dev.rice.client.action.ActionAccess;
import dev.rice.client.hook.GameHooks;
import dev.rice.client.input.LwjglMouseEvents;
import dev.rice.client.rotation.RecoveredRandom;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.NumberSetting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

/**
 * Right Clicker — the Auto Clicker, for the right mouse button. It reuses the Auto Clicker's exact click engine
 * ({@link AutoClickerModule.State}/{@code Settings}/{@code Input}) with the button forced to secondary (right), and
 * drops the Auto Clicker conditions that only make sense for left-click/attack: "Allow breaking blocks",
 * "Holding weapon" and "Not using item". Same pattern, target CPS, randomize, exhaust, inventory handling and
 * Smart randomization as the Auto Clicker; same "Show in Lunar" LWJGL path. Plain module (settings live here; the
 * ClickGUI shows them via its default layout).
 */
public final strictfp class RightClickerModule extends Module {
   private final AutoClickerModule.State state = new AutoClickerModule.State();

   public RightClickerModule() {
      super("rightclicker", "Right Clicker", "Auto-clicks the right mouse button", ModuleCategory.COMBAT);
      add(new ChoiceSetting("pattern", "Click pattern", 0, "Jitter", "Butterfly"));
      add(new NumberSetting("target_cps", "Target CPS", 12, 1, 25, 1));
      add(new BooleanSetting("randomize_cps", "Randomize", true));
      add(new BooleanSetting("exhaust", "Simulate exhaust", true));
      add(new BooleanSetting("allow_inventory", "Click in inventory", false));
      add(new BooleanSetting("inventory_randomize", "Randomize speed", true));
      add(new BooleanSetting("smart", "Smart", false));
      add(new NumberSetting("smart_amount", "Randomness", 50, 0, 100, 1));
      add(new BooleanSetting("lunar_clicks", "Show in Lunar", true));

   }

   private int choice(String key) {
      return ((ChoiceSetting)findSetting(key)).index();
   }

   private float number(String key) {
      return ((NumberSetting)findSetting(key)).floatValue();
   }

   private boolean flag(String key) {
      return ((BooleanSetting)findSetting(key)).isEnabled();
   }

   private strictfp AutoClickerModule.Settings settings() {
      AutoClickerModule.Settings s = new AutoClickerModule.Settings();
      s.pattern = this.choice("pattern");
      s.cps = this.number("target_cps");
      s.randomize = this.flag("randomize_cps");
      s.exhaust = this.flag("exhaust");
      s.inventory = this.flag("allow_inventory");
      s.inventoryRandomize = this.flag("inventory_randomize");
      s.smart = this.flag("smart");
      s.smartAmount = this.number("smart_amount");
      s.lunarClicks = this.flag("lunar_clicks");
      // Attack-only conditions dropped for right-click: never break blocks, never gate on weapon/using-item.
      s.blockBreak = false;
      s.weapon = false;
      s.notUsing = false;
      return s;
   }

   private strictfp AutoClickerModule.Input input(final ClientContext c) {
      return new AutoClickerModule.Input() {
         @Override
         public strictfp boolean ready() {
            return c.inputReady();
         }

         @Override
         public strictfp boolean hardware(int button) {
            return ActionAccess.mouse().physical(button);
         }

         @Override
         public strictfp boolean menu() {
            return c.minecraft().currentScreen != null;
         }

         @Override
         public strictfp boolean inventoryPrimary() {
            return this.hardware(1) && (!RightClickerModule.this.state.modifier || RightClickerModule.this.state.secondary);
         }

         @Override
         public strictfp boolean shift() {
            return ActionAccess.mouse().shift();
         }

         private strictfp Slot hovered() {
            return c.minecraft().currentScreen instanceof GuiContainer ? ActionAccess.hovered((GuiContainer)c.minecraft().currentScreen) : null;
         }

         @Override
         public strictfp int slot() {
            Slot slot = this.hovered();
            return slot == null ? -1 : slot.slotNumber;
         }

         @Override
         public strictfp boolean cursorStack() {
            return c.player() == null || c.player().inventory.getItemStack() != null;
         }

         @Override
         public strictfp boolean slotStack() {
            Slot slot = this.hovered();
            return slot != null && slot.getHasStack();
         }

         @Override
         public strictfp boolean weapon() {
            return true; // weapon condition is dropped; never consulted (s.weapon = false).
         }

         @Override
         public strictfp boolean using() {
            return c.player() != null && c.player().isUsingItem();
         }

         @Override
         public strictfp float yaw() {
            return c.player().rotationYaw;
         }

         @Override
         public strictfp float pitch() {
            return c.player().rotationPitch;
         }

         @Override
         public strictfp boolean target() {
            return GameHooks.target() != null;
         }

         @Override
         public strictfp double targetDistance() {
            return GameHooks.targetDistanceSquared();
         }

         @Override
         public strictfp void emit(int button, boolean down) {
            // secondary is forced, so button 0 = right. "Show in Lunar" routes through LWJGL (LWJGL right = 1) so
            // Lunar's CPS/keystrokes count it; otherwise the ordinary keybind path. Menu clicks keep the keybind path.
            if (RightClickerModule.this.flag("lunar_clicks") && c.minecraft().currentScreen == null) {
               LwjglMouseEvents.press(button == 1 ? 0 : 1, down);
            } else {
               ActionAccess.mouse().emit(c, button, down);
            }
            InputSoundBridge.mouse(false, down);
         }
      };
   }

   @Override
   public strictfp boolean listens(int type) {
      return type == 0 || type == 1 || type == 17 || type == 26;
   }

   @Override
   public strictfp void onEvent(ClientContext c, SemanticEvent e) {
      AutoClickerModule.Input i = this.input(c);
      if (e.type == 26) {
         this.state.clear(i);
      } else if (e.type == 0) {
         this.state.secondary = true; // always the right button (this is the right clicker)
         if (c.inputReady()) {
            this.state.previousYaw = c.player().rotationYaw;
            this.state.previousPitch = c.player().rotationPitch;
         }
      } else if (e.type == 17) {
         if (c.inputReady()) {
            this.state.hitKind = ActionAccess.hitKind(c.minecraft().objectMouseOver);
         }
      } else {
         this.state
            .advance(i, this.settings(), MinecraftFields.timer(c.minecraft()).timerSpeed, e.elapsedSeconds, RecoveredRandom.SHARED, RecoveredRandom.now());
      }
   }

   @Override
   protected strictfp void onDisable(ClientContext c) {
      this.state.clear(this.input(c));
   }

   @Override
   public strictfp void onWorldUnload(ClientContext c) {
      this.state.clear(this.input(c));
   }

   @Override
   public strictfp String statusText() {
      return null;
   }
}
