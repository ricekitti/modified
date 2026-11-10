package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.GuiModuleList;
import dev.rice.client.GuiModuleSettings;
import dev.rice.client.SemanticEvent;
import dev.rice.client.render.RecoveredGui;
import dev.rice.client.render.RecoveredGuiScreen;
import dev.rice.client.render.RenderAccess;
import dev.rice.client.render.RenderConstants;
import dev.rice.client.render.RenderEventPayload;
import dev.rice.client.render.RenderModuleAccess;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.ColorSetting;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

public final class GuiModule extends EvidenceModule {
   private final ModuleManager manager;
   private final RecoveredGui.Runtime runtime = new RecoveredGui.Runtime();
   private boolean shutdown;
   private boolean bindingDown;
   private int trackedBinding;

   public GuiModule(ModuleManager manager) {
      super("gui", "GUI", ModuleCategory.RENDER);
      this.manager = manager;
      dev.rice.client.render.HudLayout.bind(manager);
      dev.rice.setting.BooleanSetting editHud=add(new dev.rice.setting.BooleanSetting("edit_hud","Edit HUD",false));editHud.parseAndSet(editHud.format());editHud.saveCurrent();
      for(String id:dev.rice.client.render.HudLayout.IDS)for(String axis:new String[]{"x","y"}){
         dev.rice.setting.NumberSetting coord=add(new dev.rice.setting.NumberSetting("hud_"+id+"_"+axis,"HUD "+id+" "+axis,id.equals("targethud")?(axis.equals("x")?70:40):id.equals("anticheat")?(axis.equals("x")?.5:.12):0,id.equals("targethud")?-4000:-1,id.equals("targethud")?4000:1,0));coord.parseAndSet(coord.format());coord.saveCurrent();
      }
      RenderModuleAccess.bindGuiInteraction(this::interactionRequested);
   }

   @Override public boolean settingVisible(String key){return !key.startsWith("hud_")&&super.settingVisible(key);}
   @Override public boolean afterSettingControl(String key,ClientContext c){
      if(key.equals("edit_hud")&&((dev.rice.setting.BooleanSetting)findSetting(key)).isEnabled()){((dev.rice.setting.BooleanSetting)findSetting(key)).set(false);dev.rice.client.render.HudLayout.open(manager);return true;}
      return super.afterSettingControl(key,c);
   }

   public RecoveredGui.Runtime runtime() {
      return this.runtime;
   }

   public float scale() {
      return RenderConstants.f(1381616 + this.choice("scale") * 4);
   }

   public ColorSetting.Value accent() {
      return this.color("accent_color");
   }

   public boolean compact() {
      return this.flag("compact");
   }

   public boolean wide() {
      return this.flag("wide");
   }

   public boolean interactionRequested() {
      return this.runtime.closeRequested;
   }

   public int redrawFlags(int old) {
      return old | 3;
   }

   @Override
   public boolean listens(int t) {
      return t == 0 || t == 5 || t == 9 || t == 10 || t == 26;
   }

   @Override
   protected void onEnable(ClientContext c) {
      // Lunar may dispatch the GUI bind while its main menu is active.  The
      // recovered renderer expects a live world and otherwise covers the menu
      // with an uninitialised (black) canvas.
      if (!c.ready()) {
         this.manager.requestEnabled(this, false);
         return;
      }

      this.runtime.start(RenderAccess.now());
      if (Mouse.isGrabbed()) {
         Mouse.setCursorPosition(c.minecraft().displayWidth / 2, c.minecraft().displayHeight / 2);
         Mouse.setGrabbed(false);
      }

      if (!(c.minecraft().currentScreen instanceof RecoveredGuiScreen)
         && !(c.minecraft().currentScreen instanceof GuiModuleList)
         && !(c.minecraft().currentScreen instanceof GuiModuleSettings)) {
         c.minecraft().displayGuiScreen(new RecoveredGuiScreen(this.manager, this));
      }
   }

   @Override
   protected void onDisable(ClientContext c) {
      this.runtime.finishModal(true);
      this.runtime.clearInteraction();
      GuiScreen screen = c.minecraft().currentScreen;
      if (screen instanceof RecoveredGuiScreen || screen instanceof GuiModuleList || screen instanceof GuiModuleSettings) {
         c.minecraft().displayGuiScreen(null);
      }

      // A replacement screen (such as TargetHUD position editor) owns mouse focus now.
      if (c.ready() && !this.shutdown && c.minecraft().currentScreen == null) {
         c.minecraft().setIngameFocus();
      }

      // Drop any stale "held" state for the open bind so the next press always
      // reads as a fresh edge; otherwise a release missed during the screen
      // handoff would make the GUI intermittently fail to reopen.
      this.manager.input().resetKey(this.getBinding());
      RenderAccess.releaseCanvasResources();
   }

   /** Both native events and render polling feed this edge state, so a held key cannot toggle twice. */
   private void bindingInput(int key, boolean down, boolean allowed) {
      if (key == 0 || key != this.getBinding()) return;
      if (this.trackedBinding != key) { this.trackedBinding = key; this.bindingDown = false; }
      boolean press = down && !this.bindingDown;
      this.bindingDown = down;
      if (press && allowed) this.manager.requestEnabled(this, !this.isEnabled());
   }

   /** Poll even while a screen owns focus; releases can be missing from the screen's native event stream. */
   public void pollBinding(boolean down, boolean menuAvailable) {
      this.bindingInput(this.getBinding(), down, menuAvailable && this.runtime.mode == 0);
   }

   private boolean menuAvailable() {
      GuiScreen screen=this.manager.context().minecraft().currentScreen;
      return screen == null || screen instanceof RecoveredGuiScreen || screen instanceof GuiModuleList || screen instanceof GuiModuleSettings;
   }

   public long dispatchInput(int kind, int key, boolean pressed, int unchanged, byte[] utf8, int x, int y, float wheel) {
      boolean consumed = this.isEnabled() && this.runtime.input(kind, key, x, y, wheel, pressed, utf8);
      // Capture may assign this very key as the new GUI bind. Observe it AFTER runtime.input,
      // but do not close the menu when a binding editor or text field consumed the press.
      if (kind == 2 && key != 0 && key == this.getBinding()) {
         this.bindingInput(key, pressed, !consumed && this.runtime.mode == 0 && this.menuAvailable());
         return 1L;
      }
      if (consumed) return 1L;
      if (kind == 2 && pressed && key == 87 && this.isEnabled()) {
         this.manager.requestEnabled(this, false);
         return 1L;
      }
      return this.isEnabled() && (kind != 2 || !this.flag("allow_input") || Integer.compareUnsigned(key, 5) <= 0) ? (long)kind & 4294967040L | 1L : 0L;
   }

   public void screenClosed() {
      if (this.isEnabled()) {
         this.manager.requestEnabled(this, false);
      }
   }

   @Override
   public void onEvent(ClientContext c, SemanticEvent e) {
      RenderEventPayload payload = RenderEventPayload.get(e);
      if (e.type == 9 && payload != null) {
         payload.redrawFlags = this.redrawFlags(payload.redrawFlags);
      } else if (e.type == 10 && payload != null && payload.guiDraw != null) {
         payload.guiDraw.run();
      } else if (e.type == 26) {
         this.shutdown = true;
         RenderAccess.releaseCanvasResources();
         this.runtime.finishModal(true);
         this.runtime.clearInteraction();
      } else if (e.type == 0 && Mouse.isGrabbed()) {
         Mouse.setGrabbed(false);
      }
   }

   @Override
   public void onWorldUnload(ClientContext c) {
      this.runtime.finishModal(true);
      this.runtime.clearInteraction();
      RenderAccess.releaseCanvasResources();
   }
}
