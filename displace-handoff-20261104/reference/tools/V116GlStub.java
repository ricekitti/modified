import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.AxisAlignedBB;

/**
 * Headless stand-in for the GL calls of the OpenB4 TargetHUD (Legacy path) and Render3D.entityBox. V116Check loads
 * those classes through a loader that redirects every call to GL11, GL13 and GlStateManager here, plus
 * Render3D.filledBox and RenderGlobal.drawSelectionBoundingBox. The real draw path then runs without an OpenGL
 * context, and every primitive can be checked.
 */
public final class V116GlStub {
   /** One glBegin..glEnd primitive: mode, the colour at each vertex, and the vertices (x, y[, z]). */
   public static final class Primitive {
      public final int mode;
      public final List<double[]> vertices = new ArrayList<>();
      public final List<float[]> colors = new ArrayList<>();

      Primitive(int mode) {
         this.mode = mode;
      }

      public double minX() { double v = Double.MAX_VALUE; for (double[] p : vertices) v = Math.min(v, p[0]); return v; }
      public double maxX() { double v = -Double.MAX_VALUE; for (double[] p : vertices) v = Math.max(v, p[0]); return v; }
      public double minY() { double v = Double.MAX_VALUE; for (double[] p : vertices) v = Math.min(v, p[1]); return v; }
      public double maxY() { double v = -Double.MAX_VALUE; for (double[] p : vertices) v = Math.max(v, p[1]); return v; }
   }

   public static final List<Primitive> primitives = new ArrayList<>();
   public static final List<Object[]> boxes = new ArrayList<>();
   public static int matrixMode = 5888, program, framebuffer, activeTexture = 33984;
   public static boolean lighting, fog;
   public static int orthoCalls;
   public static final java.util.Map<Integer,Boolean> textures = new java.util.HashMap<>();
   private static final java.util.Deque<Double> scales = new java.util.ArrayDeque<>();
   public static float[] currentColor() { return color.clone(); }
   public static Object[] saveState() { return new Object[]{lighting,fog,activeTexture,new java.util.HashMap<>(textures),color.clone()}; }
   @SuppressWarnings("unchecked") public static void restoreState(Object[] state) {
      lighting=(Boolean)state[0]; fog=(Boolean)state[1]; activeTexture=(Integer)state[2];
      textures.clear(); textures.putAll((java.util.Map<Integer,Boolean>)state[3]); color=((float[])state[4]).clone();
   }
   public static int calls;
   public static int matrixDepth;
   public static int attribDepth;
   public static double scale = 1;
   private static float[] color = {1, 1, 1, 1};
   private static Primitive open;

   private V116GlStub() {
   }

   public static void reset() {
      primitives.clear();
      boxes.clear();
      scales.clear(); orthoCalls=0; lighting=false; fog=false; activeTexture=33984; textures.clear();
      calls = 0;
      matrixDepth = 0;
      attribDepth = 0;
      scale = 1;
      color = new float[]{1, 1, 1, 1};
      open = null;
   }

   private static void vertex(double... v) {
      calls++;
      if (open == null) throw new AssertionError("vertex outside glBegin");
      open.vertices.add(v);
      open.colors.add(color.clone());
   }

   // org.lwjgl.opengl.GL11 / GL13
   public static void glBegin(int mode) { calls++; if (open != null) throw new AssertionError("nested glBegin"); open = new Primitive(mode); }
   public static void glEnd() { calls++; if (open == null) throw new AssertionError("glEnd without glBegin"); primitives.add(open); open = null; }
   public static void glVertex2d(double x, double y) { vertex(x, y); }
   public static void glVertex3d(double x, double y, double z) { vertex(x, y, z); }
   public static void glVertex2f(float x, float y) { vertex(x, y); }
   public static void glVertex3f(float x, float y, float z) { vertex(x, y, z); }
   public static void glTexCoord2f(float u, float v) { calls++; }
   public static void glTexCoord2d(double u, double v) { calls++; }
   public static void glScaled(double x, double y, double z) { calls++; scale *= x; }
   public static void glScalef(float x, float y, float z) { calls++; scale *= x; }
   public static void glTranslated(double x, double y, double z) { calls++; }
   public static void glRotated(double a, double x, double y, double z) { calls++; }
   public static void glPushMatrix() { calls++; matrixDepth++; scales.push(scale); }
   public static void glPopMatrix() { calls++; matrixDepth--; scale=scales.pop(); if (matrixDepth < 0) throw new AssertionError("matrix stack underflow"); }
   public static void glPushAttrib(int mask) { calls++; attribDepth++; }
   public static void glPopAttrib() { calls++; attribDepth--; if (attribDepth < 0) throw new AssertionError("attrib stack underflow"); }
   public static void glLineWidth(float width) { calls++; }
   public static void glShadeModel(int mode) { calls++; }
   public static void glEnable(int cap) { calls++; if(cap==2896)lighting=true; if(cap==2912)fog=true; if(cap==3553)textures.put(activeTexture,true); }
   public static void glDisable(int cap) { calls++; if(cap==2896)lighting=false; if(cap==2912)fog=false; if(cap==3553)textures.put(activeTexture,false); }
   public static void glBlendFunc(int a, int b) { calls++; }
   public static void glDepthMask(boolean flag) { calls++; }
   public static void glColor4f(float r, float g, float b, float a) { color(r, g, b, a); }
   public static void glBindTexture(int target, int texture) { calls++; }
   public static void glActiveTexture(int texture) { calls++; activeTexture=texture; }
   public static void glUseProgram(int value) { calls++; program=value; }
   public static void glBindFramebuffer(int target,int value) { calls++; framebuffer=value; }
   public static int glGetInteger(int key) { return key==2976 ? matrixMode : key==35725 ? program : key==36006 ? framebuffer : 0; }
   public static void glMatrixMode(int value) { calls++; matrixMode=value; }
   public static void glLoadIdentity() { calls++; scale=1; }
   public static void glOrtho(double l,double r,double b,double t,double n,double f) { calls++; orthoCalls++; }
   public static void glTranslatef(float x,float y,float z) { calls++; }

   // net.minecraft.client.renderer.GlStateManager
   public static void pushMatrix() { glPushMatrix(); }
   public static void popMatrix() { glPopMatrix(); }
   public static void color(float r, float g, float b, float a) { calls++; color = new float[]{r, g, b, a}; }
   public static void color(float r, float g, float b) { color(r, g, b, 1); }
   public static void enableBlend() { calls++; }
   public static void disableBlend() { calls++; }
   public static void blendFunc(int a, int b) { calls++; }
   public static void tryBlendFuncSeparate(int a, int b, int c, int d) { calls++; }
   public static void enableAlpha() { calls++; }
   public static void disableAlpha() { calls++; }
   public static void alphaFunc(int func, float ref) { calls++; }
   public static void enableTexture2D() { glEnable(3553); }
   public static void disableTexture2D() { glDisable(3553); }
   public static void enableDepth() { calls++; }
   public static void disableDepth() { calls++; }
   public static void depthMask(boolean flag) { calls++; }
   public static void enableCull() { calls++; }
   public static void disableCull() { calls++; }
   public static void enableLighting() { glEnable(2896); }
   public static void disableLighting() { glDisable(2896); }
   public static void disableFog() { glDisable(2912); }
   public static void shadeModel(int mode) { calls++; }
   public static void bindTexture(int texture) { calls++; }
   public static void setActiveTexture(int texture) { glActiveTexture(texture); }

   // dev.rice.client.render.openb4.Render3D.entityBox: Rice's verbatim OpenB4 import (checked by the port tool), recorded here
   public static void entityBox(net.minecraft.entity.Entity entity, int mode, int color, float partialTicks) { calls++; boxes.add(new Object[]{entity, mode, color, partialTicks}); }

   // dev.rice.client.render.openb4.Render3D.filledBox (Tessellator path) and RenderGlobal.drawSelectionBoundingBox
   public static void filledBox(AxisAlignedBB box, float r, float g, float b, float alpha) { calls++; boxes.add(new Object[]{box, new float[]{r, g, b, alpha}}); }
   public static void drawSelectionBoundingBox(AxisAlignedBB box) { calls++; boxes.add(new Object[]{box, null}); }
}
