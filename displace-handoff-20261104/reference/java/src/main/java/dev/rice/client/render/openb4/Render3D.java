package dev.rice.client.render.openb4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.*;
import net.minecraft.util.AxisAlignedBB;
import dev.rice.inject.ReflectionHelper;
import java.lang.reflect.Method;
import java.awt.Color;
import org.lwjgl.opengl.GL11;
/** Rendering primitives imported from OpenB4 Render3D. */
public final class Render3D {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final Frustum frustum = new Frustum();
    private static Method setupCameraTransform;
    public static void updateFrustum(float f) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null) {
            double d = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) f;
            double d2 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) f;
            double d3 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) f;
            frustum.setPosition(d, d2, d3);
        }
    }

    public static boolean isVisible(AxisAlignedBB bb) {
        return frustum.isBoundingBoxInFrustum(bb);
    }

    public static boolean isVisible(Entity e) {
        return isVisible(e.getEntityBoundingBox());
    }

    public static boolean isVisible(double x, double y, double z, double x2, double y2, double z2) {
        return frustum.isBoxInFrustum(x, y, z, x2, y2, z2);
    }

    public static void setupCamera(float f) {
        try {
            if (setupCameraTransform == null) {
                setupCameraTransform = ReflectionHelper.findMethod(EntityRenderer.class, mc.entityRenderer,
                        new String[]{"setupCameraTransform", "func_78479_a"}, float.class, int.class);
            }
            setupCameraTransform.invoke(mc.entityRenderer, f, 0);
        } catch (Exception ignored) {
        }
    }

    public static void setupOverlay() {
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, RenderUtils.scaledWidthD, RenderUtils.scaledHeightD, 0.0, 1000.0, 3000.0);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);
    }

    public static void filledBox(AxisAlignedBB a, float r, float g, float b, float alpha) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        w.begin(7, DefaultVertexFormats.POSITION_COLOR);
        double[][] v = {
                {a.minX, a.minY, a.minZ}, {a.minX, a.maxY, a.minZ}, {a.maxX, a.maxY, a.minZ}, {a.maxX, a.minY, a.minZ},
                {a.minX, a.minY, a.maxZ}, {a.maxX, a.minY, a.maxZ}, {a.maxX, a.maxY, a.maxZ}, {a.minX, a.maxY, a.maxZ},
                {a.minX, a.minY, a.minZ}, {a.minX, a.minY, a.maxZ}, {a.minX, a.maxY, a.maxZ}, {a.minX, a.maxY, a.minZ},
                {a.maxX, a.minY, a.minZ}, {a.maxX, a.maxY, a.minZ}, {a.maxX, a.maxY, a.maxZ}, {a.maxX, a.minY, a.maxZ},
                {a.minX, a.minY, a.minZ}, {a.maxX, a.minY, a.minZ}, {a.maxX, a.minY, a.maxZ}, {a.minX, a.minY, a.maxZ},
                {a.minX, a.maxY, a.minZ}, {a.minX, a.maxY, a.maxZ}, {a.maxX, a.maxY, a.maxZ}, {a.maxX, a.maxY, a.minZ}};
        for (double[] p : v) {
            w.pos(p[0], p[1], p[2]).color(r, g, b, alpha).endVertex();
        }
        t.draw();
    }

    public static void entityBox(Entity entity, int n, int n2, float f) {
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        RenderManager rm = mc.getRenderManager();
        double d4 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) f - rm.viewerPosX;
        double d5 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) f - rm.viewerPosY;
        double d6 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) f - rm.viewerPosZ;
        GlStateManager.pushMatrix();
        if (n == 3) {
            GL11.glTranslated(d4, d5 - 0.2, d6);
            GL11.glRotated(-rm.playerViewY, 0.0, 1.0, 0.0);
            GlStateManager.disableDepth();
            GL11.glScalef(0.03f, 0.03f, 0.03f);
            float f2 = 0.5f;
            RenderUtils.beginRect();
            RenderUtils.rawRect(-20.0f - f2, 75.0f, 20.0f + f2, 75.0f + f2, -15987700);
            RenderUtils.rawRect(-20.0f - f2, -1.0f - f2, 20.0f + f2, -1.0f, -15987700);
            RenderUtils.rawRect(-20.0f - f2, -1.0f, -20.0f, 75.0f, -15987700);
            RenderUtils.rawRect(20.0f + f2, -1.0f, 20.0f, 75.0f, -15987700);
            RenderUtils.endRect();
            f2 = 1.5f;
            if (n2 == 0) {
                int n4 = RenderUtils.rainbow(2L, 0L);
                int n5 = RenderUtils.rainbow(2L, 1000L);
                RenderUtils.gradientRect(-20.0f, -1.0f + f2, -20.0f + f2, 75.0f - f2, n4, n5);
                RenderUtils.gradientRect(20.0f, -1.0f + f2, 20.0f - f2, 75.0f - f2, n4, n5);
                RenderUtils.rect(-20.0f, 75.0f, 20.0f, 75.0f - f2, n4);
                RenderUtils.rect(-20.0f, -1.0f, 20.0f, -1.0f + f2, n5);
            } else {
                RenderUtils.rect(-20.0f, 75.0f, 20.0f, 75.0f - f2, n2);
                RenderUtils.rect(-20.0f, -1.0f, 20.0f, -1.0f + f2, n2);
                RenderUtils.rect(-20.0f, -1.0f + f2, -20.0f + f2, 75.0f - f2, n2);
                RenderUtils.rect(20.0f, -1.0f + f2, 20.0f - f2, 75.0f - f2, n2);
            }
            GlStateManager.enableDepth();
        } else if (n == 4) {
            healthBar((EntityLivingBase) entity, d4, d5, d6);
        } else if (n == 6) {
            ring(d4, d5, d6, 0.7, 45, 1.5, n2, n2 == 0);
        } else {
            if (n2 == 0) {
                n2 = RenderUtils.rainbow(2L, 0L);
            }
            float a = (float) (n2 >> 24 & 0xFF) / 255.0f;
            float r = (float) (n2 >> 16 & 0xFF) / 255.0f;
            float g = (float) (n2 >> 8 & 0xFF) / 255.0f;
            float b = (float) (n2 & 0xFF) / 255.0f;
            if (n == 5) {
                GL11.glTranslated(d4, d5 - 0.2, d6);
                GL11.glRotated(-rm.playerViewY, 0.0, 1.0, 0.0);
                GlStateManager.disableDepth();
                GL11.glScalef(0.03f, 0.03f, 0.03f);
                circle(0.0, 95, 10.0, 3, Color.black.getRGB());
                for (int i = 0; i < 6; ++i) {
                    circle(0.0, 95 + (10 - i), 3.0, 4, Color.black.getRGB());
                }
                for (int i = 0; i < 7; ++i) {
                    circle(0.0, 95 + (10 - i), 2.0, 4, n2);
                }
                circle(0.0, 95, 8.0, 3, n2);
                GlStateManager.enableDepth();
            } else {
                AxisAlignedBB bb = entity.getEntityBoundingBox().expand(0.1, 0.1, 0.1);
                AxisAlignedBB bb2 = new AxisAlignedBB(bb.minX - entity.posX + d4, bb.minY - entity.posY + d5, bb.minZ - entity.posZ + d6,
                        bb.maxX - entity.posX + d4, bb.maxY - entity.posY + d5, bb.maxZ - entity.posZ + d6);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(3042);
                GL11.glDisable(3553);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                if (n == 1) {
                    GlStateManager.color(r, g, b, a);
                    GL11.glEnable(2848);
                    GL11.glLineWidth(2.0f);
                    RenderGlobal.drawSelectionBoundingBox(bb2);
                    GL11.glDisable(2848);
                } else if (n == 2) {
                    filledBox(bb2, r, g, b, 0.37f);
                }
                RenderUtils.resetColor();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
            }
        }
        GlStateManager.popMatrix();
    }

    public static float barHeight(EntityLivingBase e) {
        if ((double) e.height < 1.1 || e.isChild()) {
            return 40.0f;
        }
        return 70.0f;
    }

    public static void healthBar(EntityLivingBase e, double d, double d2, double d3) {
        GL11.glTranslated(d, d2 - 0.1, d3);
        GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
        GlStateManager.disableDepth();
        GL11.glScalef(0.03f, 0.03f, 0.03f);
        double d4 = e.getHealth() / e.getMaxHealth();
        if (d4 > 1.0) {
            d4 = 1.0;
        }
        float f = barHeight(e);
        float f2 = (float) ((double) f * d4);
        int n = 21;
        if (e.isChild()) {
            n -= 5;
        }
        RenderUtils.beginRect();
        RenderUtils.rawRect(n, -1.0f, n + 4, f + 1.0f, -15987700);
        RenderUtils.rawRect(n + 1, f2, n + 3, f, -11974327);
        RenderUtils.rawRect(n + 1, 0.0f, n + 3, f2, RenderUtils.healthColor(d4));
        RenderUtils.endRect();
        GlStateManager.enableDepth();
    }

    public static void ring(double d, double d2, double d3, double d4, int n, double d5, int n2, boolean rainbow) {
        if (n < 3) {
            return;
        }
        float a = (float) (n2 >> 24 & 0xFF) / 255.0f;
        float r = (float) (n2 >> 16 & 0xFF) / 255.0f;
        float g = (float) (n2 >> 8 & 0xFF) / 255.0f;
        float b = (float) (n2 & 0xFF) / 255.0f;
        mc.entityRenderer.disableLightmap();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glDepthMask(false);
        GL11.glLineWidth((float) d5);
        if (!rainbow) {
            GlStateManager.color(r, g, b, a);
        }
        GL11.glBegin(1);
        long l = 0L;
        long l2 = 15000L / (long) n;
        long l3 = l2 / 2L;
        for (int i = 0; i < n * 2; ++i) {
            if (rainbow) {
                if (i % 2 != 0) {
                    if (i == 47) {
                        l = l3;
                    }
                    l += l2;
                }
                int c = RenderUtils.rainbow(2L, l);
                GlStateManager.color((float) (c >> 16 & 0xFF) / 255.0f, (float) (c >> 8 & 0xFF) / 255.0f, (float) (c & 0xFF) / 255.0f);
            }
            double d7 = Math.PI * 2 * (double) i / (double) n + Math.toRadians(180.0);
            GL11.glVertex3d(d + Math.cos(d7) * d4, d2, d3 + Math.sin(d7) * d4);
        }
        GL11.glEnd();
        RenderUtils.resetColor();
        GL11.glDepthMask(true);
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        mc.entityRenderer.enableLightmap();
    }

    public static void circle(double d, double d2, double d3, int n, int n2) {
        if (n < 3) {
            return;
        }
        float a = (float) (n2 >> 24 & 0xFF) / 255.0f;
        float r = (float) (n2 >> 16 & 0xFF) / 255.0f;
        float g = (float) (n2 >> 8 & 0xFF) / 255.0f;
        float b = (float) (n2 & 0xFF) / 255.0f;
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        double d4 = Math.toRadians(180.0);
        w.begin(6, DefaultVertexFormats.POSITION);
        for (int i = 0; i < n; ++i) {
            double d5 = Math.PI * 2 * (double) i / (double) n + d4;
            w.pos(d + Math.sin(d5) * d3, d2 + Math.cos(d5) * d3, 0.0).endVertex();
        }
        t.draw();
        RenderUtils.resetColor();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
