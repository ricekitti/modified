package dev.rice.client.anticheat;

import dev.rice.client.render.DrawPlan;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

/**
 * Corner-stacked anticheat flag toasts in the supplied style spec: one line
 * "[AC] &lt;player&gt; failed &lt;check&gt; x&lt;VL&gt;" on a dark frosted box, sliding in from the
 * nearest screen edge, held 1.2s, then sliding back out. Newest sits at the dragged anchor and
 * older toasts glide away from the nearest vertical edge; at most five show and a sixth drops the
 * oldest. This is pure state plus a canvas {@link DrawPlan}; it never enables modules, sends
 * packets, mutates the pinned Snowify detector or leaves GL state dirty. Coordinates are
 * scaled-GUI font pixels, rounded to whole pixels so the text stays sharp at whole GUI scales.
 */
public final class AnticheatAlerts {
   // Style spec, in font pixels / seconds (the scaled HUD already bakes in the GUI scale).
   private static final double LIFETIME = 1.2;             // visible seconds before the exit slide starts
   private static final int MAX = 5;                       // on-screen cap; a sixth drops the oldest at once
   private static final double COOLDOWN = 10.0;            // same player+check muted for this many seconds
   private static final float MARGIN = 3.0F;               // off-screen hidden gap for the slide
   private static final float GAP = 1.0F;                  // vertical gap between stacked toasts
   private static final float PAD_LR = 5.0F, PAD_TB = 3.0F, RADIUS = 2.0F, LINE_H = 10.0F, CELL = 9.0F;
   private static final double IN_RATE = 16.0, STACK_RATE = 14.0, DT_CAP = 0.05;
   private static final int GRAY = 0xFFB4B4B8, RED = 0xFFFF6464, WHITE = 0xFFFFFFFF;

   private final List<Alert> alerts = new ArrayList<Alert>();        // newest first (index 0 nearest the corner)
   private final Map<String, Double> cooldowns = new HashMap<String, Double>();
   private double clock;        // seconds, advanced by the rendered frame delta; drives lifetime and cooldown
   private long lastNanos;      // wall clock for the in-game per-frame delta

   private static final class Alert {
      final String player;
      final String check;
      final int vl;
      double age;
      double vis;
      Double y;                 // resting y is unknown until the first frame places it

      Alert(String player, String check, int vl) {
         this.player = player;
         this.check = check;
         this.vl = vl;
      }
   }

   /** Frame delta in seconds since the previous call, capped per the spec; 0 on the first call. */
   public synchronized double consumeDt() {
      long now = System.nanoTime();
      double dt = this.lastNanos == 0L ? 0.0 : Math.min(DT_CAP, (double)(now - this.lastNanos) / 1.0E9);
      this.lastNanos = now;
      return dt;
   }

   public synchronized void clear() {
      this.alerts.clear();
      this.cooldowns.clear();
   }

   public synchronized int active() {
      return this.alerts.size();
   }

   /** Parses a detector flag line ("&lt;name&gt; flagged &lt;check&gt; x&lt;count&gt;") and queues a toast; ignores anything else. */
   public synchronized boolean offerFlagMessage(String text) {
      if (text == null) {
         return false;
      }
      String plain = EnumChatFormatting.getTextWithoutFormattingCodes(text);
      int cut = plain.indexOf(" flagged ");
      if (cut < 0) {
         return false;
      }
      String player = plain.substring(0, cut);
      String rest = plain.substring(cut + " flagged ".length());
      int mark = rest.lastIndexOf(" x");
      if (mark <= 0 || mark + 2 >= rest.length()) {
         return false;
      }
      String check = rest.substring(0, mark);
      int vl;
      try {
         vl = Integer.parseInt(rest.substring(mark + 2));
      } catch (NumberFormatException malformed) {
         return false;
      }
      if (player.isEmpty() || check.isEmpty()) {
         return false;
      }
      return this.flag(player, check, vl);
   }

   /** Queues one toast, honoring the per player+check cooldown and the five-item cap. */
   public synchronized boolean flag(String player, String check, int vl) {
      String key = player.toLowerCase(Locale.ROOT) + '\u0000' + check.toLowerCase(Locale.ROOT);
      Double last = this.cooldowns.get(key);
      if (last != null && this.clock - last < COOLDOWN) {
         return false;
      }
      this.cooldowns.put(key, this.clock);
      this.alerts.add(0, new Alert(player, check, Math.max(1, vl)));
      while (this.alerts.size() > MAX) {
         this.alerts.remove(this.alerts.size() - 1);
      }
      return true;
   }

   private static double step(double current, double target, double dt, double rate) {
      return current + (target - current) * (1.0 - Math.exp(-rate * dt));
   }

   private float contentWidth(FontRenderer fr, String player, String check, int vl) {
      return fr.getStringWidth("[AC] ")
         + fr.getStringWidth(player)
         + fr.getStringWidth(" failed ")
         + fr.getStringWidth(check)
         + fr.getStringWidth(" x" + vl);
   }

   private static int boxHeight() {
      return Math.round(PAD_TB * 2.0F + LINE_H);
   }

   /** Representative sample box for the HUD editor selection outline and bounds. */
   public synchronized int[] sampleSize(FontRenderer fr) {
      float w = PAD_LR * 2.0F + this.contentWidth(fr, "Steve", "Speed", 4);
      return new int[]{Math.round(w), boxHeight()};
   }

   /** One static, fully slid-in sample toast at the dragged anchor, for the editor preview. */
   public synchronized DrawPlan editorPlan(FontRenderer fr, int sw, int sh, double hudX, double hudY) {
      int[] size = this.sampleSize(fr);
      int ax = (int)Math.round(hudX * Math.max(1, sw - size[0]));
      int ay = (int)Math.round(hudY * Math.max(1, sh - size[1]));
      DrawPlan plan = new DrawPlan().canvas();
      this.paint(plan, fr, "Steve", "Speed", 4, ax, ay, size[0]);
      return plan;
   }

   /** Advances the toasts by dt and emits the live stack as a canvas plan in scaled-GUI pixels. */
   public synchronized DrawPlan buildPlan(FontRenderer fr, int sw, int sh, double hudX, double hudY, double dt) {
      dt = Math.max(0.0, Math.min(DT_CAP, dt));
      this.clock += dt;
      DrawPlan plan = new DrawPlan().canvas();
      int[] size = this.sampleSize(fr);
      boolean right = hudX >= 0.5;
      boolean top = hudY < 0.5;
      float refLeft = Math.round(hudX * Math.max(1, sw - size[0]));
      float refRight = refLeft + size[0];
      float refTop = Math.round(hudY * Math.max(1, sh - size[1]));
      float h = boxHeight();
      // The newest toast rests at the anchor; older ones glide away from the nearest vertical edge.
      float cursor = top ? refTop : refTop + h;
      for (int i = 0; i < this.alerts.size(); i++) {
         Alert a = this.alerts.get(i);
         a.age += dt;
         boolean done = a.age >= LIFETIME;
         a.vis = step(a.vis, done ? 0.0 : 1.0, dt, IN_RATE);
         if (done && a.vis < 0.01) {
            this.alerts.remove(i--);
            continue;
         }
         float w = PAD_LR * 2.0F + this.contentWidth(fr, a.player, a.check, a.vl);
         float target = top ? cursor : cursor - h;
         a.y = a.y == null ? (double)target : step(a.y, target, dt, STACK_RATE);
         float eased = 1.0F - (float)Math.pow(1.0 - a.vis, 3.0);     // ease-out cubic slide
         float hidden = w + MARGIN;
         float x = right ? refRight - w + (1.0F - eased) * hidden : refLeft - (1.0F - eased) * hidden;
         this.paint(plan, fr, a.player, a.check, a.vl, Math.round(x), (int)Math.round(a.y), Math.round(w));
         cursor += (top ? 1.0F : -1.0F) * (h + GAP) * eased;
      }
      return plan;
   }

   /** Draws one toast: frosted dark box, hairline border, then the coloured one-line text. */
   private void paint(DrawPlan plan, FontRenderer fr, String player, String check, int vl, int x, int y, int w) {
      int h = boxHeight();
      plan.color(12.0F / 255.0F, 12.0F / 255.0F, 14.0F / 255.0F, 0.30F).rect(x, y, w, h, RADIUS);
      plan.color(1.0F, 1.0F, 1.0F, 0.07F).roundedStroke(x, y, w, h, RADIUS);
      int ty = Math.round(y + (h - CELL) / 2.0F);
      float tx = x + PAD_LR;
      tx = this.run(plan, fr, "[", GRAY, tx, ty);
      tx = this.run(plan, fr, "AC", RED, tx, ty);
      tx = this.run(plan, fr, "] ", GRAY, tx, ty);
      tx = this.run(plan, fr, player, WHITE, tx, ty);
      tx = this.run(plan, fr, " failed ", GRAY, tx, ty);
      tx = this.run(plan, fr, check, RED, tx, ty);
      this.run(plan, fr, " x" + vl, GRAY, tx, ty);
   }

   private float run(DrawPlan plan, FontRenderer fr, String text, int color, float x, int y) {
      plan.text(text, Math.round(x), y, color, true, CELL);
      return x + fr.getStringWidth(text);
   }
}
