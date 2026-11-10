package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.SemanticEvent;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.BooleanSetting;
import dev.rice.setting.ChoiceSetting;
import dev.rice.setting.ColorSetting;
import dev.rice.setting.NumberSetting;
import java.nio.FloatBuffer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Fog — a faithful port of the <b>Fog Customizer</b> Forge 1.8.9 mod
 * ({@code fogcustomizer-1.0.0.jar}) onto rice's render pipeline.
 *
 * <p><b>Why a port and not a drop-in:</b> Fog Customizer is a Forge mod that hooks the Forge
 * {@code EntityViewRenderEvent.FogColors} / {@code EntityViewRenderEvent.FogDensity} /
 * {@code EntityViewRenderEvent.RenderFogEvent} events (see its {@code FogEventHandler}). Rice is a
 * native-ASM client, not Forge — those events and OneConfig's GUI/command do not exist here. So the
 * <em>algorithm</em> from {@code FogEventHandler} is reproduced exactly and driven by rice's own
 * fog hook {@link dev.rice.client.hook.RenderHooks#afterSetupFog()} (event type {@value #FOG_EVENT}),
 * which is injected right after {@code EntityRenderer.setupFog}. That is the GL-state equivalent of
 * the three Forge fog events firing: by the time this runs, vanilla has written the live fog
 * colour/mode/density, and the port overwrites exactly what Fog Customizer would have overwritten.</p>
 *
 * <p>Fog Customizer config fields and their defaults, reproduced 1:1 (from {@code FogConfig}):</p>
 * <ul>
 *   <li><b>Override Fog Color</b> (switch, default on) — {@code overrideFogColor}.</li>
 *   <li><b>Fog Color</b> (default RGB 170,205,255) — {@code fogColor}.</li>
 *   <li><b>Color Opacity</b> (slider 0..100, default 100) — {@code colorStrength}.</li>
 *   <li><b>Override Fog Intensity</b> (switch, default on) — {@code overrideFogIntensity}.</li>
 *   <li><b>Falloff</b> (dropdown Smooth/Soft/Linear/Constant, default Smooth) — {@code fogFalloff}.</li>
 *   <li><b>Intensity Multiplier</b> (slider 0..20, default 1.0) — {@code intensityMultiplier}.</li>
 * </ul>
 */
public final strictfp class FogModule extends Module {
   public static final int FOG_EVENT = 30;

   // ── Fog Customizer settings, 1:1 (label + range + default taken from FogConfig's OneConfig annotations) ──
   // "Override Fog Color" — Use the selected color for the fog.
   private final BooleanSetting overrideFogColor = this.add(new BooleanSetting("override_fog_color", "Override Fog Color", true));
   // "Fog Color" — The color applied to the fog. (OneConfig OneColor(170,205,255); alpha ignored, allowAlpha=false)
   private final ColorSetting fogColor = this.add(new ColorSetting("fog_color", "Fog Color"));
   // "Color Opacity" — How visible the selected fog color is. (0..100, default 100)
   private final NumberSetting colorStrength = this.add(new NumberSetting("color_strength", "Color Opacity", 100.0, 0.0, 100.0, 1.0));
   // "Override Fog Intensity" — Scale the distance and density of the fog.
   private final BooleanSetting overrideFogIntensity = this.add(new BooleanSetting("override_fog_intensity", "Override Fog Intensity", true));
   // "Falloff" — Smooth=EXP2, Soft=EXP, Linear=adaptive linear, Constant=flat even haze. (default Smooth)
   private final ChoiceSetting fogFalloff = this.add(new ChoiceSetting("fog_falloff", "Falloff", 0, "Smooth", "Soft", "Linear", "Constant"));
   // "Intensity Multiplier" — 1.0 is vanilla fog; higher pulls fog closer; 0 disables visible fog. (0..20, default 1.0)
   private final NumberSetting intensityMultiplier = this.add(new NumberSetting("intensity_multiplier", "Intensity Multiplier", 1.0, 0.0, 20.0, 0.05));

   private final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(16);

   // --- TEMP DIAGNOSTIC (v62): confirm the hook fires + report live values to chat once/sec ---
   private static final boolean DEBUG = false;
   private long lastDebugMs;
   private int firesSinceReport;

   public FogModule() {
      super("fog", "Fog", "Fully customizable fog colour and distance", ModuleCategory.RENDER);
      // FogConfig default: fogColor = new OneColor(170, 205, 255). ColorSetting defaults to black, so seed it.
      this.fogColor.set(new ColorSetting.Value(170.0F / 255.0F, 205.0F / 255.0F, 255.0F / 255.0F));
   }

   @Override
   public int metadataCategory() {
      return 2; // Render
   }

   @Override
   public boolean listens(int type) {
      return type == FOG_EVENT;
   }

   @Override
   public void onEvent(ClientContext c, SemanticEvent e) {
      if (e.type == FOG_EVENT) {
         this.applyFog(c);
      }
   }

   // ── FogEventHandler helpers, reproduced exactly ──

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   // getColorOpacity(): overrideFogColor ? clamp(colorStrength/100, 0, 1) : 1
   private float getColorOpacity() {
      return this.overrideFogColor.isEnabled()
         ? clamp((float) this.colorStrength.floatValue() / 100.0F, 0.0F, 1.0F)
         : 1.0F;
   }

   // getEffectiveIntensity(): (overrideFogIntensity ? curve(intensityMultiplier) : 1) * getColorOpacity()
   private float getEffectiveIntensity() {
      float intensity;
      if (this.overrideFogIntensity.isEnabled()) {
         float m = clamp((float) this.intensityMultiplier.floatValue(), 0.0F, 20.0F);
         // Ease-in on the multiplier so LOWER slider values read much lighter, while the top of
         // the range still reaches full whiteout. 0.25 linear floor + 0.75 quadratic ramp over 0..20.
         intensity = m * (0.25F + 0.75F * (m / 20.0F));
      } else {
         intensity = 1.0F;
      }
      return intensity * this.getColorOpacity();
   }

   // shouldScaleFog(): overrideFogIntensity || getColorOpacity() < 1
   private boolean shouldScaleFog() {
      return this.overrideFogIntensity.isEnabled() || this.getColorOpacity() < 1.0F;
   }

   @Override
   protected void onEnable(ClientContext context) {
      this.lastDebugMs = 0L;
      this.firesSinceReport = 0;
      if (DEBUG) {
         this.chat(context, "§b[Fog] enabled — watching setupFog hook, will report once/sec");
      }
   }

   private void chat(ClientContext c, String msg) {
      try {
         if (c != null && c.minecraft() != null && c.minecraft().thePlayer != null) {
            c.minecraft().thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(msg));
         }
      } catch (Throwable ignored) {
      }
   }

   @Override
   protected void onDisable(ClientContext context) {
      // Restore roughly-vanilla overworld fog immediately. We write fog with raw GL (so our overrides
      // actually reach the driver every frame regardless of GlStateManager's state cache); because of
      // that, GlStateManager's cache can still think it last set LINEAR and skip re-issuing when this
      // module stops overriding — so push a sane LINEAR band back ourselves on disable.
      try {
         float far = 192.0F;
         if (context != null && context.minecraft() != null && context.minecraft().gameSettings != null) {
            far = Math.max((float) (context.minecraft().gameSettings.renderDistanceChunks * 16), 1.0F);
         }
         fogMode(GL11.GL_LINEAR);
         fogDensity(1.0F);
         fogStart(far * 0.75F);
         fogEnd(far);
      } catch (Throwable ignored) {
      }
   }

   /**
    * Runs right after vanilla's {@code setupFog}, with the GL fog state live. Module being enabled is
    * Fog Customizer's {@code isActive()} (config.enabled). All fog state is written with <b>raw GL</b>
    * ({@code glFogi}/{@code glFogf}/{@code glFog}) rather than {@code GlStateManager}: GlStateManager
    * caches fog mode/density/start/end and suppresses the GL call when its cached value matches, which —
    * interleaved with vanilla's own per-frame setupFog — swallowed our density/distance overrides and
    * made Intensity Multiplier / Color Opacity appear dead. Raw GL writes unconditionally every frame, so
    * the override always wins. Normal fog is restored in {@link #onDisable(ClientContext)}.
    */
   private void applyFog(ClientContext c) {
      try {
         boolean report = false;
         if (DEBUG) {
            this.firesSinceReport++;
            long now = System.currentTimeMillis();
            if (now - this.lastDebugMs >= 1000L) {
               report = true;
               this.lastDebugMs = now;
            }
         }

         // onFogColors: if (active && overrideFogColor && fogColor != null) → replace GL_FOG_COLOR.
         if (this.overrideFogColor.isEnabled()) {
            ColorSetting.Value col = this.fogColor.get();
            if (col != null) {
               this.colorBuffer.clear();
               this.colorBuffer.put(col.red).put(col.green).put(col.blue).put(1.0F);
               this.colorBuffer.flip();
               GL11.glFog(GL11.GL_FOG_COLOR, this.colorBuffer);
            }
         }

         float far = 1.0F;
         try {
            far = Math.max((float) (c.minecraft().gameSettings.renderDistanceChunks * 16), 1.0F);
         } catch (Throwable ignored) {
         }
         float eff = this.getEffectiveIntensity();
         int falloff = this.fogFalloff.index();

         // Density/distance only when Fog Customizer would scale the fog.
         if (this.shouldScaleFog()) {
            Material eyeMaterial = this.eyeMaterial(c);
            if (eyeMaterial == Material.water || eyeMaterial == Material.lava) {
               // onFogDensity: underwater exponential fog scaled by effective intensity.
               float base = eyeMaterial == Material.lava ? 2.0F : 0.1F;
               fogMode(falloff == 1 ? GL11.GL_EXP : GL11.GL_EXP2);
               fogDensity(base * eff);
            } else if (eff <= 0.0F) {
               // 0 disables visible fog: push the linear band far past the view.
               fogMode(GL11.GL_LINEAR);
               fogStart(far * 4.0F);
               fogEnd(far * 4.0F + 1.0F);
            } else if (falloff == 0) {
               // Smooth → EXP2
               fogMode(GL11.GL_EXP2);
               fogDensity(1.5F * eff / far);
            } else if (falloff == 1) {
               // Soft → EXP
               fogMode(GL11.GL_EXP);
               fogDensity(3.0F * eff / far);
            } else if (falloff == 3) {
               // Constant → flat even haze, no distance falloff
               fogMode(GL11.GL_LINEAR);
               float t = clamp(eff / 20.0F, 0.0F, 1.0F);
               float span = far * 1024.0F;
               float end = (1.0F - t) * span;
               fogStart(end - span);
               fogEnd(end);
            } else {
               // Linear (mode == 2) → adaptive linear band
               fogMode(GL11.GL_LINEAR);
               float f4 = far / eff;
               float f5 = eff > 1.0F ? 0.75F / eff : 0.75F;
               float f6 = f4 * f5;
               f4 = Math.max(f6 + 0.01F, f4);
               fogStart(f6);
               fogEnd(f4);
            }
         }

         if (report) {
            // Read back the LIVE GL fog state right after our write — this tells us whether the
            // override actually reached the driver, whether GL_FOG is even enabled, and what the
            // terrain will be drawn with. This is the ground truth static analysis can't give.
            int glMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
            boolean glOn = GL11.glIsEnabled(GL11.GL_FOG);
            this.colorBuffer.clear();
            GL11.glGetFloat(GL11.GL_FOG_DENSITY, this.colorBuffer);
            float glDensity = this.colorBuffer.get(0);
            this.colorBuffer.clear();
            GL11.glGetFloat(GL11.GL_FOG_START, this.colorBuffer);
            float glStart = this.colorBuffer.get(0);
            this.colorBuffer.clear();
            GL11.glGetFloat(GL11.GL_FOG_END, this.colorBuffer);
            float glEnd = this.colorBuffer.get(0);
            this.chat(
               c,
               String.format(
                  "§b[Fog] fires=%d ovInt=%b iMul=%.2f cStr=%.0f eff=%.3f scale=%b falloff=%d far=%.0f",
                  this.firesSinceReport, this.overrideFogIntensity.isEnabled(), this.intensityMultiplier.floatValue(),
                  this.colorStrength.floatValue(), eff, this.shouldScaleFog(), falloff, far
               )
            );
            this.chat(
               c,
               String.format(
                  "§e[Fog GL] on=%b mode=%d (EXP=2048 EXP2=2049 LIN=9729) density=%.5f start=%.1f end=%.1f",
                  glOn, glMode, glDensity, glStart, glEnd
               )
            );
            this.firesSinceReport = 0;
         }
      } catch (Throwable ignored) {
         // Fog is cosmetic; never let it break the render loop.
      }
   }

   // Belt-and-suspenders fog writes: push through GlStateManager (keeps its state cache coherent so
   // vanilla's own per-frame setupFog won't fight us) AND raw GL (forces the call even if the cache
   // would have skipped it). Either failure mode alone would have let our override slip; doing both
   // guarantees the value reaches the driver.
   private static void fogMode(int mode) {
      try {
         GlStateManager.setFog(mode);
      } catch (Throwable ignored) {
      }
      GL11.glFogi(GL11.GL_FOG_MODE, mode);
   }

   private static void fogDensity(float density) {
      try {
         GlStateManager.setFogDensity(density);
      } catch (Throwable ignored) {
      }
      GL11.glFogf(GL11.GL_FOG_DENSITY, density);
   }

   private static void fogStart(float start) {
      try {
         GlStateManager.setFogStart(start);
      } catch (Throwable ignored) {
      }
      GL11.glFogf(GL11.GL_FOG_START, start);
   }

   private static void fogEnd(float end) {
      try {
         GlStateManager.setFogEnd(end);
      } catch (Throwable ignored) {
      }
      GL11.glFogf(GL11.GL_FOG_END, end);
   }

   // The block material at the camera eye — Fog Customizer's FogDensity.block, used for the water/lava case.
   private Material eyeMaterial(ClientContext c) {
      try {
         Minecraft mc = c.minecraft();
         World world = c.world();
         Entity view = mc == null ? null : mc.getRenderViewEntity();
         if (view == null || world == null) {
            return null;
         }
         BlockPos eye = new BlockPos(view.posX, view.posY + (double) view.getEyeHeight(), view.posZ);
         return world.getBlockState(eye).getBlock().getMaterial();
      } catch (Throwable ignored) {
         return null;
      }
   }
}
