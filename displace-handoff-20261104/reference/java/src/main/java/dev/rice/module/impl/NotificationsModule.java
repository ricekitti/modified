package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.render.CanvasFonts;
import dev.rice.client.render.DrawPlan;
import dev.rice.client.render.NotificationQueue;
import dev.rice.client.render.RenderAccess;
import dev.rice.client.render.RenderConstants;
import dev.rice.client.render.RenderEventPayload;
import dev.rice.client.render.RenderModuleAccess;
import dev.rice.module.EvidenceModule;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.ColorSetting;
import java.util.Locale;

public final strictfp class NotificationsModule extends EvidenceModule {
   private final ModuleManager manager;
   private final NotificationQueue queue = new NotificationQueue();
   private final NotificationQueue configLoads = new NotificationQueue();
   private boolean handshake;

   public NotificationsModule(ModuleManager manager) {
      super("notifications", "Notifications", ModuleCategory.RENDER);
      this.manager = manager;
   }

   @Override
   public strictfp boolean listens(int t) {
      return t == 5 || t == 9 || t == 10 || t == 11 || t == 12 || t == 19;
   }

   public strictfp NotificationQueue queue() {
      return this.queue;
   }

   public strictfp boolean clearHandshake() {
      boolean old = this.handshake;
      this.handshake = false;
      return old;
   }

   private strictfp boolean category(Module module) {
      switch (RenderModuleAccess.category(module)) {
         case 0:
            return this.flag("show_combat");
         case 1:
            return this.flag("show_move");
         case 2:
            return this.flag("show_visual");
         case 3:
            return this.flag("show_block");
         case 4:
            return this.flag("show_utility");
         default:
            return true;
      }
   }

   public strictfp void stateChanged(Module module, boolean enabled) {
      if (module == this) {
         if (!enabled) {
            this.queue.clearMessages();
         } else if (!this.handshake) {
            this.handshake = true;
            Module gui = this.manager.find("gui");
            String binding = gui == null ? "" : RenderModuleAccess.keyName(gui.getBinding());
            this.queue.append(1, String.format(Locale.ROOT, RenderConstants.text(1384194), binding));
         }
      } else if ((RenderModuleAccess.observedFlags(module) & 16) == 0
         && ((RenderModuleAccess.observedFlags(module) & 2) == 0 || !this.flag("hide_bind_only"))
         && (!module.isHoldBinding() || !this.flag("hide_hold_notifs"))
         && this.category(module)) {
         this.queue.state(module.hudName(), enabled);
      }
   }

   public strictfp int advance(ClientContext c, float elapsed, int flags) {
      Module gui = this.manager.find("gui");
      int redraw = this.isEnabled() ? this.queue.advance(elapsed, flags, this.flag("hide_ingame") && (gui == null || !gui.isEnabled())) : flags;
      return this.configLoads.advance(elapsed, redraw, false);
   }

   public strictfp void configAction(int kind, String label) {
      if (kind == 0) {
         this.configLoads.append(0, "Loaded config " + (label == null ? "default" : label));
      } else if (kind != 2 && this.isEnabled()) {
         String action = RenderConstants.pointerText(1384336 + 8 * kind);
         this.queue.append(0, action + " " + (label == null ? RenderConstants.text(1384368) : label));
      }
   }

   public strictfp void message(int code, String message) {
      this.queue.append(2, message == null ? RenderConstants.text(1384424).replace("%d", Integer.toString(code)) : message);
   }

   public strictfp void friendChanged(boolean added) {
      this.queue.append(1, added ? "Friend added" : "Friend removed");
   }

   @Override
   public strictfp void onEvent(ClientContext c, SemanticEvent e) {
      RenderEventPayload payload = RenderEventPayload.get(e);
      if (e.type == 11 && payload != null) {
         this.configAction(payload.action, payload.profileName);
      } else if (e.type == 12 && payload != null) {
         this.message(payload.errorCode, payload.message);
      } else if (e.type == 19 && payload != null) {
         this.friendChanged(payload.friendAdded);
      } else if (e.type == 5 && e.module != null) {
         this.stateChanged(e.module, e.enabled);
      } else if (e.type == 9 && payload != null) {
         payload.redrawFlags = this.advance(c, (float)e.elapsedSeconds, payload.redrawFlags);
      } else if (e.type == 10 && !dev.rice.client.render.HudLayout.editing()) {
         Module gui = this.manager.find("gui");
         if (!this.configLoads.messages().isEmpty() || this.isEnabled() && (!this.flag("hide_ingame") || gui != null && gui.isEnabled())) {
            RenderAccess.submit(e, this.plan(c));
         }
      }
   }

   public strictfp DrawPlan plan(ClientContext c) {
      try (CanvasFonts.GuiFaceScope face = CanvasFonts.guiFace()) {
         return this.planWithFace(c);
      }
   }

   public java.util.List<NotificationQueue.Message> visibleMessages() {
      java.util.List<NotificationQueue.Message> messages = new java.util.ArrayList<>();
      Module gui = this.manager.find("gui");
      if (this.isEnabled() && (!this.flag("hide_ingame") || gui != null && gui.isEnabled())) messages.addAll(this.queue.messages());
      messages.addAll(this.configLoads.messages());
      return messages;
   }

   private strictfp DrawPlan planWithFace(ClientContext c) {
      return paintMessages(this.visibleMessages());
   }

   /** Uses the live notification painter without adding a message to either real queue. */
   public DrawPlan editorPlan() {
      NotificationQueue sample = new NotificationQueue(); sample.append(1, "Enabled Block In");
      sample.messages().get(0).age = 1F;
      try (CanvasFonts.GuiFaceScope face = CanvasFonts.guiFace()) { return paintMessages(sample.messages()); }
   }

   private DrawPlan paintMessages(java.util.List<NotificationQueue.Message> messages) {
      DrawPlan p = dev.rice.client.render.HudLayout.apply("notifications",new DrawPlan().canvas());
      float y = RenderConstants.f(1384036);
      float initial = y;
      float one = RenderConstants.f(1384048);
      float white = RenderConstants.f(1384076);
      float dark = RenderConstants.f(1384100);
      float padding = RenderConstants.f(1384108);
      float shrink = RenderConstants.f(1384112);
      float font = RenderConstants.f(1384032);

      for (NotificationQueue.Message m : messages) {
         float amount = NotificationQueue.opacity(m.age);
         float x = (one - amount) * RenderConstants.f(1384072) + initial;
         ColorSetting.Value color;
         if (m.kind == 0) {
            color = new ColorSetting.Value(RenderConstants.f(1384084), RenderConstants.f(1384088), RenderConstants.f(1384092));
         } else if (m.kind == 1) {
            Module gui = this.manager.find("gui");
            color = gui == null ? new ColorSetting.Value(white, white, white) : ((ColorSetting)gui.findSetting("accent_color")).get();
         } else if (m.kind == 2) {
            color = new ColorSetting.Value(white, RenderConstants.f(1384080), RenderConstants.f(1384080));
         } else {
            color = new ColorSetting.Value(white, white, white);
         }

         float width = CanvasFonts.width(m.text, 0, font) + RenderConstants.f(1384096);
         p.color(dark, dark, dark, amount).rect(x, y, width, 42.0F, RenderConstants.f(1384104));
         float sx = x + padding;
         float sy = y + padding;
         float sw = RenderConstants.f(1384116);
         float sh = 42.0F + shrink;
         p.color(color, RenderConstants.f(1384120) * amount).rect(sx, sy, sw, sh, RenderConstants.f(1384124));
         String prefix = RenderConstants.text(1384328);
         float prefixWidth = CanvasFonts.width(prefix, 0, font);
         p.canvasText(prefix, (float)((double)sw * 0.5 + (double)sx), sy + sh * 0.5F, argb(color, amount), font, 18);
         p.canvasText(m.text, sx + sw + padding, y + 21.0F, argb(new ColorSetting.Value(white, white, white), amount), font, 17);
         y += amount * RenderConstants.f(1384128);
      }

      return p;
   }

   private static strictfp int argb(ColorSetting.Value c, float alpha) {
      return (int)(alpha * 255.0F) << 24 | c.argb() & 16777215;
   }

   @Override
   protected strictfp void onDisable(ClientContext c) {
      this.queue.clearMessages();
   }

   @Override
   public strictfp void onWorldUnload(ClientContext c) {
      this.queue.clearAll();
      this.configLoads.clearAll();
   }
}
