package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.client.render.CanvasFonts;
import dev.rice.client.render.DrawPlan;
import dev.rice.client.render.RenderAccess;
import dev.rice.client.render.RenderConstants;
import dev.rice.client.render.RenderEventPayload;
import dev.rice.client.render.RenderMath;
import dev.rice.client.render.RenderModuleAccess;
import dev.rice.module.EvidenceModule;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.setting.ColorSetting;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final strictfp class ModOverlayModule extends EvidenceModule {
   private final ModuleManager manager;
   private final List<ModOverlayModule.Row> rows = new ArrayList<>();
   private boolean dirty;
   private boolean measure;
   private int mask;
   private float timer;

   public ModOverlayModule(ModuleManager manager) {
      super("mod_overlay", "Mod Overlay", ModuleCategory.RENDER);
      this.manager = manager;
   }

   @Override
   public strictfp boolean listens(int t) {
      return t == 0 || t == 5 || t == 9 || t == 10 || t == 11 || t == 24;
   }

   private strictfp boolean category(Module m) {
      switch (RenderModuleAccess.category(m)) {
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

   private strictfp boolean eligible(Module m, boolean requireEnabled) {
      return (!requireEnabled || m.isEnabled())
         && (RenderModuleAccess.observedFlags(m) & 4) == 0
         && (!this.flag("require_bound") || m.getBinding() != 0)
         && this.category(m);
   }

   private strictfp void text(ModOverlayModule.Row row) {
      String name = row.module.hudName();
      row.name = bounded(this.flag("lowercase") ? lowercase(name) : name, 19);
      String status = this.flag("show_details") ? RenderModuleAccess.status(row.module) : null;
      row.status = status == null ? "" : bounded(this.flag("lowercase") ? lowercase(status) : status, 31);
   }

   private static strictfp String bounded(String text, int max) {
      byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
      return bytes.length <= max ? text : new String(bytes, 0, max, StandardCharsets.UTF_8);
   }

   private static strictfp String lowercase(String source) {
      StringBuilder out = new StringBuilder(source.length());

      for (int i = 0; i < source.length(); i++) {
         char c = source.charAt(i);
         out.append(c >= 'A' && c <= 'Z' ? (char)(c + ' ') : c);
      }

      return out.toString();
   }

   private strictfp void append(Module module) {
      ModOverlayModule.Row row = new ModOverlayModule.Row(module);
      this.text(row);
      this.rows.add(row);
   }

   public strictfp void refresh() {
      for (ModOverlayModule.Row row : this.rows) {
         if (!this.eligible(row.module, false)) {
            row.enabled = false;
         }
      }

      for (Module m : this.manager.all()) {
         if (this.eligible(m, true)) {
            ModOverlayModule.Row existing = null;

            for (ModOverlayModule.Row rowx : this.rows) {
               if (rowx.module == m) {
                  existing = rowx;
                  break;
               }
            }

            if (existing != null) {
               existing.enabled = true;
            } else {
               this.append(m);
            }
         }
      }

      this.measure = true;
   }

   public strictfp void control(Module module, boolean enabled) {
      this.timer = Float.intBitsToFloat(1045220557);
      if (module != this) {
         for (ModOverlayModule.Row row : this.rows) {
            if (row.module == module) {
               if (!enabled) {
                  row.enabled = false;
               } else if (this.eligible(module, false)) {
                  row.enabled = true;
                  this.text(row);
                  this.measure = true;
                  if (row.progress < 0.0F) {
                     row.progress = 0.0F;
                  }
               }

               return;
            }
         }

         if (enabled && this.eligible(module, true)) {
            this.append(module);
            this.measure = true;
         }
      } else {
         this.rows.clear();
         if (enabled) {
            for (Module m : this.manager.all()) {
               if (this.eligible(m, true)) {
                  this.append(m);
               }
            }

            this.measure = true;
         }
      }
   }

   public strictfp void collect(ClientContext c) {
      int next = (this.flag("show_combat") ? 1 : 0) + (this.flag("show_move") ? 2 : 0) + (this.flag("show_block") ? 4 : 0) + (this.flag("show_visual") ? 8 : 0);
      next |= (this.flag("show_utility") ? 16 : 0) | (this.flag("require_bound") ? 32 : 0);
      boolean oldDirty = this.dirty;
      if (this.mask != next || oldDirty) {
         this.refresh();
         this.mask = next;
         this.dirty = false;
      }

      boolean anyChanged = false;
      for (ModOverlayModule.Row row : this.rows) {
         String prevStatus = row.status;
         String prevName = row.name;
         this.text(row);
         if (!java.util.Objects.equals(prevStatus, row.status) || !java.util.Objects.equals(prevName, row.name)) {
            anyChanged = true;
         }
      }

      if (anyChanged || oldDirty) {
         this.measure = true;
      }
   }

   public strictfp int redrawFlags(int old) {
      if (this.rows.isEmpty()) {
         return old;
      }
      return old | (!this.measure && !(this.timer > 0.0F) && !this.animating() ? 1 : 3);
   }

   private strictfp boolean animating() {
      float cap = RenderConstants.f(1383528);

      for (ModOverlayModule.Row row : this.rows) {
         if (!row.enabled || row.progress != cap) {
            return true;
         }
      }

      return false;
   }

   public strictfp void settingAction(int kind) {
      if (kind == 0) {
         this.dirty = true;
      }
   }

   @Override
   public strictfp boolean afterSettingControl(String key, ClientContext c) {
      this.dirty = true;
      return false;
   }

   @Override
   public strictfp void onEvent(ClientContext c, SemanticEvent e) {
      RenderEventPayload payload = RenderEventPayload.get(e);
      if (e.type == 9 && payload != null) {
         payload.redrawFlags = this.redrawFlags(payload.redrawFlags);
      } else if (e.type == 11 && payload != null) {
         this.settingAction(payload.action);
      } else if (e.type == 0) {
         this.collect(c);
      } else if (e.type == 5 && e.module != null) {
         this.control(e.module, e.enabled);
      } else if (e.type == 24 && this.flag("require_bound")) {
         this.refresh();
      } else if (e.type == 10 && !dev.rice.client.render.HudLayout.editing()) {
         RenderAccess.submit(e, this.plan(c, (float)e.elapsedSeconds));
      }
   }

   public strictfp DrawPlan plan(ClientContext c, float elapsed) {
      return this.plan((float)c.minecraft().displayWidth, elapsed);
   }

   public strictfp DrawPlan plan(float screen, float elapsed) {
      DrawPlan var5;
      try (CanvasFonts.GuiFaceScope face = CanvasFonts.guiFace()) {
         var5 = this.planWithFace(screen, elapsed);
      }

      return var5;
   }

   private strictfp DrawPlan planWithFace(float screen, float elapsed) {
      float font = this.number("text_size");
      if (this.timer > 0.0F) {
         this.timer -= elapsed;
      }

      double rate = RenderConstants.d(1383520);
      double increase = (double)elapsed * rate;
      double decrease = Double.longBitsToDouble(Double.doubleToRawLongBits((double)elapsed) ^ RenderConstants.l(1383504)) * rate;
      float cap = RenderConstants.f(1383528);

      for (int i = 0; i < this.rows.size(); i++) {
         ModOverlayModule.Row row = this.rows.get(i);
         if (row.enabled) {
            if (row.progress != cap) {
               row.progress = RenderMath.min(cap, (float)((double)row.progress + increase));
            }
         } else {
            row.progress = (float)((double)row.progress + decrease);
            if (!(row.progress > 0.0F)) {
               this.rows.remove(i--);
            }
         }
      }

      float gap = RenderConstants.f(1383532);
      if (this.measure) {
         for (ModOverlayModule.Row row : this.rows) {
            float[] bounds = new float[4];
            CanvasFonts.bounds(row.name, 0.0F, 0.0F, font, 9, bounds);
            row.width = bounds[2];
            row.height = bounds[3];
            if (!row.status.isEmpty()) {
               CanvasFonts.bounds(row.status, 0.0F, 0.0F, font, 9, bounds);
               row.width = bounds[2] + gap + row.width;
            }
         }

         Collections.sort(this.rows, (a, b) -> b.width > a.width ? 1 : (a.width > b.width ? -1 : a.name.compareTo(b.name)));
         this.measure = false;
      }

      return paintRows(screen, this.rows);
   }

   /** Production row painter with isolated editor data; no toggles, live rows or animation state are changed. */
   public DrawPlan editorPlan(float screen) {
      try (CanvasFonts.GuiFaceScope face = CanvasFonts.guiFace()) {
         List<Row> preview = new ArrayList<>();
         for (Module m : manager.all()) if (eligible(m, true)) { Row row = new Row(m); text(row); preview.add(row); }
         if (preview.isEmpty()) for (String id : new String[]{"blockin", "clutch", "killaura"}) {
            Module m = manager.find(id); if (m != null) { Row row = new Row(m); text(row); preview.add(row); }
         }
         float font = number("text_size"), gap = RenderConstants.f(1383532);
         for (Row row : preview) {
            row.progress = RenderConstants.f(1383528);
            float[] box = new float[4]; CanvasFonts.bounds(row.name, 0, 0, font, 9, box);
            row.width = box[2]; row.height = box[3];
            if (!row.status.isEmpty()) { CanvasFonts.bounds(row.status, 0, 0, font, 9, box); row.width += gap + box[2]; }
         }
         Collections.sort(preview, (a,b) -> b.width > a.width ? 1 : a.width > b.width ? -1 : a.name.compareTo(b.name));
         return paintRows(screen, preview);
      }
   }

   private DrawPlan paintRows(float screen, List<Row> sourceRows) {
      float font = number("text_size"), gap = RenderConstants.f(1383532), cap = RenderConstants.f(1383528);
      float y = 0.0F;
      DrawPlan p = dev.rice.client.render.HudLayout.apply("mod_overlay",new DrawPlan().canvas());

      for (int ix = 0; ix < sourceRows.size(); ix++) {
         ModOverlayModule.Row rowx = sourceRows.get(ix);
         float alpha = rowx.progress == cap ? cap : RenderMath.smooth(rowx.progress);
         float corner = this.number("bg_rounding");
         if (ix + 1 < sourceRows.size()) {
            corner = RenderMath.min(rowx.width - sourceRows.get(ix + 1).width, corner);
         }

         corner = RenderMath.max(0.0F, corner);
         float px = this.number("padding_x");
         float twiceX = px + px;
         float py = this.number("padding_y");
         float barX = screen - twiceX - rowx.width * alpha;
         float barWidth = twiceX + rowx.width;
         float barHeight = py + py + rowx.height;
         p.color(0.0F, 0.0F, 0.0F, Float.intBitsToFloat(1060320051) * alpha).rectVarying(barX, y, barWidth, barHeight, 0.0F, 0.0F, 0.0F, corner);
         float textY = py + y;
         float textX = screen - px - rowx.width * alpha;
         float end = p.canvasText(rowx.name, textX, textY, argb(this.color("name_color"), alpha), font, 9);
         if (!rowx.status.isEmpty()) {
            p.canvasText(rowx.status, end + gap, textY, argb(this.color("details_color"), alpha), font, 9);
         }

         py = this.number("padding_y");
         y += alpha * (py + py + rowx.height);
      }

      return p;
   }

   private static strictfp int argb(ColorSetting.Value color, float alpha) {
      return (int)(alpha * 255.0F) << 24 | color.argb() & 16777215;
   }

   public strictfp List<ModOverlayModule.Row> rows() {
      return Collections.unmodifiableList(this.rows);
   }

   @Override
   public strictfp void onWorldUnload(ClientContext c) {
      this.rows.clear();
      this.dirty = true;
   }

   public static final strictfp class Row {
      public final Module module;
      public String name;
      public String status;
      public float width;
      public float height;
      public float progress;
      public boolean enabled = true;

      private Row(Module module) {
         this.module = module;
      }
   }
}
