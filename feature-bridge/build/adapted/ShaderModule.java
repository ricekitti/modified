package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.module.Module;
import dev.rice.module.ModuleCategory;
import dev.rice.module.ModuleManager;
import dev.rice.inject.ReflectionHelper;
import dev.rice.setting.ChoiceSetting;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Shaders — native port of OpenB4's {@code ShadersModule}. Drives Minecraft's
 * built-in post-process shader pipeline (the "super secret settings" shaders) via
 * {@link EntityRenderer}: picks one of the bundled {@code shaders/post/*.json}
 * programs and keeps it applied while enabled. Vanilla owns the framebuffers,
 * resolution/resize handling, and GLSL — this module only selects and releases.
 */
public final strictfp class ShaderModule extends Module {
   // The 20 built-in post shaders shipped with Minecraft 1.8.9
   // (EntityRenderer.shaderResourceLocations), in order.
   private static final String[] FILES = {
      "notch", "fxaa", "art", "bumpy", "blobs2", "pencil", "color_convolve", "deconverge",
      "flip", "invert", "ntsc", "outline", "phosphor", "scan_pincushion", "sobel", "bits",
      "desaturate", "green", "blur", "wobble"
   };
   private static final String[] LABELS = {
      "Notch", "FXAA", "Art", "Bumpy", "Blobs", "Pencil", "Color Convolve", "Deconverge",
      "Flip", "Invert", "NTSC", "Outline", "Phosphor", "Scan Pincushion", "Sobel", "Bits",
      "Desaturate", "Green", "Blur", "Wobble"
   };
   private static final ResourceLocation[] SHADERS = buildResources();

   private final ModuleManager modules;
   private final ChoiceSetting shader;
   private int applied = -1;
   // EntityRenderer.loadShader(ResourceLocation) is private; drive it by reflection (as OpenB4 does).
   private Method loadShader;

   public ShaderModule(ModuleManager modules) {
      super("shaders", "Shaders", "Applies a fullscreen post-process shader effect", ModuleCategory.RENDER);
      this.modules = modules;
      this.shader = this.add(new ChoiceSetting("shader", "Shader", 0, LABELS));
   }

   private static ResourceLocation[] buildResources() {
      ResourceLocation[] out = new ResourceLocation[FILES.length];
      for (int i = 0; i < FILES.length; i++) {
         out[i] = new ResourceLocation("shaders/post/" + FILES[i] + ".json");
      }
      return out;
   }

   @Override
   public int metadataCategory() {
      return 2; // Render tab (not present in ModuleDescriptors.json)
   }

   @Override
   protected void onEnable(ClientContext c) {
      if (!OpenGlHelper.shadersSupported) {
         this.modules.setEnabled(this, false, true);
      }
   }

   @Override
   public void onClientTick(ClientContext c) {
      if (!c.ready()) {
         return;
      }
      if (!OpenGlHelper.shadersSupported) {
         this.modules.setEnabled(this, false, true);
         return;
      }
      EntityRenderer renderer = c.minecraft().entityRenderer;
      if (renderer == null) {
         return;
      }
      int selected = clamp(this.shader.index(), 0, SHADERS.length - 1);
      try {
         if (this.loadShader == null) {
            this.loadShader = ReflectionHelper.findMethod(EntityRenderer.class, renderer,
               new String[]{"loadShader", "func_175069_a"}, ResourceLocation.class);
         }
         if (!renderer.isShaderActive() || this.applied != selected) {
            this.loadShader.invoke(renderer, SHADERS[selected]);
            this.applied = selected;
         }
      } catch (Exception ignored) {
         // A failed program load leaves the pipeline untouched; retried next tick.
      }
   }

   @Override
   protected void onDisable(ClientContext c) {
      this.stop(c);
   }

   @Override
   public void onWorldUnload(ClientContext c) {
      this.stop(c);
   }

   private void stop(ClientContext c) {
      this.applied = -1;
      if (c.minecraft() != null && c.minecraft().entityRenderer != null) {
         try {
            c.minecraft().entityRenderer.stopUseShader();
         } catch (Exception ignored) {
         }
      }
   }

   private static int clamp(int v, int lo, int hi) {
      return v < lo ? lo : (v > hi ? hi : v);
   }
}
