package dev.rice.client.render;

import dev.rice.client.hook.GlStateScope;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL13;

/** Isolates imported rendering from Minecraft and from the next Rice render pass. */
public final class RenderPassScope implements AutoCloseable {
   private final GlStateScope attributes;
   private final int matrixMode;
   private final int program;
   private final int framebuffer;
   private boolean projection;
   private boolean model;
   private boolean closed;

   private RenderPassScope() {
      this.matrixMode = GL11.glGetInteger(2976);
      this.program = GL11.glGetInteger(35725);
      this.framebuffer = GL11.glGetInteger(36006);
      this.attributes = GlStateScope.open();
      try {
         GL11.glMatrixMode(5889);
         GL11.glPushMatrix();
         this.projection = true;
         GL11.glMatrixMode(5888);
         GL11.glPushMatrix();
         this.model = true;
         GL11.glMatrixMode(this.matrixMode);
      } catch (RuntimeException | Error failure) {
         this.close();
         throw failure;
      }
   }

   public static RenderPassScope world() {
      return new RenderPassScope();
   }

   public static RenderPassScope hud(int width, int height) {
      RenderPassScope scope = new RenderPassScope();
      try {
         GL20.glUseProgram(0);
         GL11.glMatrixMode(5889);
         GL11.glLoadIdentity();
         GL11.glOrtho(0.0, width, height, 0.0, 1000.0, 3000.0);
         GL11.glMatrixMode(5888);
         GL11.glLoadIdentity();
         GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
         prepareText();
         GlStateManager.disableDepth();
         GlStateManager.depthMask(false);
         GlStateManager.disableCull();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         return scope;
      } catch (RuntimeException | Error failure) {
         scope.close();
         throw failure;
      }
   }

   /** World lightmaps, fog and item lighting must not modulate formatted name-tag text. */
   public static void prepareText() {
      GL20.glUseProgram(0);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL13.glActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GL11.glDisable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.enableTexture2D();
      GL11.glEnable(3553);
      GlStateManager.disableLighting();
      GL11.glDisable(2896);
      GlStateManager.disableFog();
      GL11.glDisable(2912);
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public void close() {
      if (this.closed) return;
      this.closed = true;
      try {
         GL20.glUseProgram(this.program);
         OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, this.framebuffer);
      } finally {
         try {
            if (this.model) {
               GL11.glMatrixMode(5888);
               GL11.glPopMatrix();
            }
            if (this.projection) {
               GL11.glMatrixMode(5889);
               GL11.glPopMatrix();
            }
            GL11.glMatrixMode(this.matrixMode);
         } finally {
            this.attributes.close();
         }
      }
   }
}
