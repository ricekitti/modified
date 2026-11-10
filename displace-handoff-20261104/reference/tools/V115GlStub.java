import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.AxisAlignedBB;

/**
 * Headless stand-in for the GL calls the converted Fireball ESP makes. V115Check redirects calls to GL11,
 * GlStateManager, RenderGlobal.drawSelectionBoundingBox and RavenScriptApi.filledBox here, so the real draw path
 * runs without an OpenGL context and every primitive can be checked.
 */
public final class V115GlStub {
   /** One glBegin..glEnd primitive with the colour current at glBegin. */
   public static final class Primitive {
      public final int mode;
      public final float[] color;
      public final float lineWidth;
      public final List<double[]> vertices = new ArrayList<>();

      Primitive(int mode, float[] color, float lineWidth) {
         this.mode = mode;
         this.color = color;
         this.lineWidth = lineWidth;
      }
   }

   public static final List<Primitive> primitives = new ArrayList<>();
   public static final List<Object[]> boxes = new ArrayList<>();
   public static final List<AxisAlignedBB> outlines = new ArrayList<>();
   public static boolean suppressCachedColor;
   public static final List<float[]> outlineColors = new ArrayList<>();
   public static int calls;
   public static int matrixDepth;
   public static int attribDepth;
   private static final java.util.Deque<float[]> attributes = new java.util.ArrayDeque<>();
   private static float[] color = {1, 1, 1, 1};
   private static float lineWidth = 1;
   private static Primitive open;

   private V115GlStub() {
   }

   public static void reset() {
      suppressCachedColor = false;
      outlineColors.clear();
      primitives.clear();
      boxes.clear();
      outlines.clear();
      calls = 0;
      matrixDepth = 0;
      color = new float[]{1, 1, 1, 1};
      lineWidth = 1;
      attributes.clear(); attribDepth = 0;
      open = null;
   }

   // org.lwjgl.opengl.GL11
   public static void glBegin(int mode) { calls++; if (open != null) throw new AssertionError("nested glBegin"); open = new Primitive(mode, color.clone(), lineWidth); }
   public static void glEnd() { calls++; if (open == null) throw new AssertionError("glEnd without glBegin"); primitives.add(open); open = null; }
   public static void glVertex3d(double x, double y, double z) { calls++; if (open == null) throw new AssertionError("vertex outside glBegin"); open.vertices.add(new double[]{x, y, z}); }
   public static void glVertex3f(float x, float y, float z) { glVertex3d(x, y, z); }
   public static void glLineWidth(float width) { calls++; lineWidth = width; }
   public static void glPushAttrib(int mask) { attributes.push(new float[]{color[0],color[1],color[2],color[3],lineWidth}); attribDepth++; }
   public static void glPopAttrib() { float[] old=attributes.pop();color=new float[]{old[0],old[1],old[2],old[3]};lineWidth=old[4];attribDepth--; }
   public static void glHint(int target, int mode) { calls++; }
   public static void glEnable(int cap) { calls++; }
   public static void glDisable(int cap) { calls++; }
   public static void glBlendFunc(int a, int b) { calls++; }
   public static void glDepthMask(boolean flag) { calls++; }

   // net.minecraft.client.renderer.GlStateManager
   public static void pushMatrix() { calls++; matrixDepth++; }
   public static void popMatrix() { calls++; matrixDepth--; if (matrixDepth < 0) throw new AssertionError("matrix stack underflow"); }
   public static void color(float r, float g, float b, float a) { calls++; if (!suppressCachedColor) color = new float[]{r, g, b, a}; }
   public static void glColor4f(float r, float g, float b, float a) { calls++; color = new float[]{r, g, b, a}; }
   public static void color(float r, float g, float b) { color(r, g, b, 1); }
   public static void enableBlend() { calls++; }
   public static void disableBlend() { calls++; }
   public static void tryBlendFuncSeparate(int a, int b, int c, int d) { calls++; }
   public static void enableAlpha() { calls++; }
   public static void disableAlpha() { calls++; }
   public static void enableTexture2D() { calls++; }
   public static void disableTexture2D() { calls++; }
   public static void enableLighting() { calls++; }
   public static void disableLighting() { calls++; }
   public static void enableCull() { calls++; }
   public static void disableCull() { calls++; }
   public static void enableDepth() { calls++; }
   public static void disableDepth() { calls++; }
   public static void depthMask(boolean flag) { calls++; }

   // net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox
   public static void drawSelectionBoundingBox(AxisAlignedBB box) { calls++; outlines.add(box); outlineColors.add(color.clone()); }

   // dev.rice.client.render.RavenScriptApi.filledBox (Tessellator path)
   public static void filledBox(AxisAlignedBB box, float r, float g, float b, float alpha) { calls++; boxes.add(new Object[]{box, new float[]{r, g, b, alpha}}); }
}
