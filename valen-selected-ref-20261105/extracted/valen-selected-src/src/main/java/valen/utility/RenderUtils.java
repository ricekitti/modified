/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockStairs
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.renderer.BlockRendererDispatcher
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.client.renderer.RenderGlobal
 *  net.minecraft.client.renderer.RenderHelper
 *  net.minecraft.client.renderer.Tessellator
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.vertex.DefaultVertexFormats
 *  net.minecraft.client.resources.model.IBakedModel
 *  net.minecraft.client.shader.Framebuffer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.IBlockAccess
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.util.glu.GLU
 *  valen.module.impl.player.Freecam
 *  valen.utility.RenderUtils$1
 *  valen.utility.RenderUtils$ProjectionContext
 *  valen.utility.StairsUtils
 */
package valen.utility;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import valen.Valen;
import valen.clickgui.ClickGui;
import valen.lunar.render.LunarMatrices;
import valen.mixin.impl.accessor.IAccessorMinecraft;
import valen.module.impl.player.Freecam;
import valen.utility.IMinecraftInstance;
import valen.utility.RenderUtils;
import valen.utility.ScaledResolutionCache;
import valen.utility.StairsUtils;
import valen.utility.Utils;

/*
 * Exception performing whole class analysis ignored.
 */
public class RenderUtils
implements IMinecraftInstance {
    private static final EnumFacing[] BLOCK_SHAPE_FACES = EnumFacing.values();
    private static Frustum frustum = new Frustum();
    private static final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer((int)16);
    private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer((int)16);
    private static final IntBuffer VIEWPORT = BufferUtils.createIntBuffer((int)16);
    private static final FloatBuffer SCREEN_COORDS = BufferUtils.createFloatBuffer((int)3);
    private static final int SCISSOR_PUSH_STACK_DEPTH = 4;
    private static final IntBuffer SCISSOR_PUSH_BUF = BufferUtils.createIntBuffer((int)16);
    private static final int[][] scissorPushStack = new int[4][5];
    private static int scissorPushDepth = 0;
    private static final Map<String, ResourceLocation> iconCache = new HashMap<String, ResourceLocation>();

    public static void renderBlock(BlockPos blockPos, int n, boolean bl, boolean bl2) {
        RenderUtils.renderBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0, 1.0, 1.0, n, bl, bl2);
    }

    public static void renderBlockBatch(List<BlockPos> list, int n, int n2, boolean bl, boolean bl2) {
        if (list == null || list.isEmpty()) {
            return;
        }
        double d = RenderUtils.mc.getRenderManager().viewerPosX;
        double d2 = RenderUtils.mc.getRenderManager().viewerPosY;
        double d3 = RenderUtils.mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)2.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        float f5 = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f6 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f7 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f8 = (float)(n2 & 0xFF) / 255.0f;
        for (BlockPos blockPos : list) {
            double d4 = (double)blockPos.getX() - d;
            double d5 = (double)blockPos.getY() - d2;
            double d6 = (double)blockPos.getZ() - d3;
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(d4, d5, d6, d4 + 1.0, d5 + 1.0, d6 + 1.0);
            if (bl) {
                GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f);
                RenderGlobal.drawSelectionBoundingBox((AxisAlignedBB)axisAlignedBB);
            }
            if (!bl2) continue;
            RenderUtils.drawBoundingBox(axisAlignedBB, f6, f7, f8, f5);
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    public static void renderChest(BlockPos blockPos, int n, boolean bl, boolean bl2) {
        RenderUtils.renderBox((float)blockPos.getX() + 0.0625f, blockPos.getY(), (float)blockPos.getZ() + 0.0625f, 0.875, 0.875, 0.875, n, bl, bl2);
    }

    public static void renderChestBatch(List<BlockPos> list, int n, boolean bl, boolean bl2) {
        RenderUtils.renderChestBatch(list, n, n, bl, bl2);
    }

    public static void renderChestBatch(List<BlockPos> list, int n, int n2, boolean bl, boolean bl2) {
        if (list == null || list.isEmpty()) {
            return;
        }
        double d = RenderUtils.mc.getRenderManager().viewerPosX;
        double d2 = RenderUtils.mc.getRenderManager().viewerPosY;
        double d3 = RenderUtils.mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)2.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        float f5 = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f6 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f7 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f8 = (float)(n2 & 0xFF) / 255.0f;
        for (BlockPos blockPos : list) {
            double d4 = (double)blockPos.getX() + 0.0625 - d;
            double d5 = (double)blockPos.getY() - d2;
            double d6 = (double)blockPos.getZ() + 0.0625 - d3;
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(d4, d5, d6, d4 + 0.875, d5 + 0.875, d6 + 0.875);
            if (bl) {
                GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f);
                RenderGlobal.drawSelectionBoundingBox((AxisAlignedBB)axisAlignedBB);
            }
            if (!bl2) continue;
            RenderUtils.drawBoundingBox(axisAlignedBB, f6, f7, f8, f5);
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    public static void renderBlock(BlockPos blockPos, int n, double d, boolean bl, boolean bl2) {
        RenderUtils.renderBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0, d, 1.0, n, bl, bl2);
    }

    public static void scissor(double d, double d2, double d3, double d4) {
        double d5 = ClickGui.getActiveRenderScale();
        d *= d5;
        d2 *= d5;
        d3 *= d5;
        d4 *= d5;
        ScaledResolution scaledResolution = ScaledResolutionCache.get();
        int n = scaledResolution.getScaleFactor();
        int n2 = Minecraft.getMinecraft().displayHeight;
        int n3 = (int)Math.floor(d * (double)n);
        int n4 = (int)Math.ceil((d + d3) * (double)n);
        int n5 = Math.max(0, n4 - n3);
        double d6 = d2 + d4;
        int n6 = (int)Math.floor((double)n2 - d6 * (double)n);
        int n7 = (int)Math.ceil((double)n2 - d2 * (double)n);
        int n8 = Math.max(0, n7 - n6);
        if (n5 < 0 || n8 < 0) {
            return;
        }
        GL11.glScissor((int)n3, (int)n6, (int)n5, (int)n8);
    }

    public static void scissorPushGui(double d, double d2, double d3, double d4) {
        double d5 = ClickGui.getActiveRenderScale();
        d *= d5;
        d2 *= d5;
        d3 *= d5;
        d4 *= d5;
        ScaledResolution scaledResolution = ScaledResolutionCache.get();
        int n = scaledResolution.getScaleFactor();
        int n2 = Minecraft.getMinecraft().displayHeight;
        int n3 = (int)Math.floor(d * (double)n);
        int n4 = (int)Math.ceil((d + d3) * (double)n);
        int n5 = Math.max(0, n4 - n3);
        double d6 = d2 + d4;
        int n6 = (int)Math.floor((double)n2 - d6 * (double)n);
        int n7 = (int)Math.ceil((double)n2 - d2 * (double)n);
        int n8 = Math.max(0, n7 - n6);
        boolean bl = GL11.glIsEnabled((int)3089);
        int[] nArray = scissorPushStack[scissorPushDepth++];
        if (scissorPushDepth > 4) {
            throw new IllegalStateException("Scissor stack overflow");
        }
        if (bl) {
            SCISSOR_PUSH_BUF.clear();
            GL11.glGetInteger((int)3088, (IntBuffer)SCISSOR_PUSH_BUF);
            nArray[0] = 1;
            nArray[1] = SCISSOR_PUSH_BUF.get(0);
            nArray[2] = SCISSOR_PUSH_BUF.get(1);
            nArray[3] = SCISSOR_PUSH_BUF.get(2);
            nArray[4] = SCISSOR_PUSH_BUF.get(3);
            int n9 = Math.max(nArray[1], n3);
            int n10 = Math.max(nArray[2], n6);
            int n11 = Math.max(0, Math.min(nArray[1] + nArray[3], n3 + n5) - n9);
            int n12 = Math.max(0, Math.min(nArray[2] + nArray[4], n6 + n8) - n10);
            GL11.glScissor((int)n9, (int)n10, (int)n11, (int)n12);
        } else {
            nArray[0] = 0;
            GL11.glEnable((int)3089);
            GL11.glScissor((int)n3, (int)n6, (int)n5, (int)n8);
        }
    }

    public static void scissorPop() {
        int[] nArray;
        if ((nArray = scissorPushStack[--scissorPushDepth])[0] == 1) {
            GL11.glScissor((int)nArray[1], (int)nArray[2], (int)nArray[3], (int)nArray[4]);
        } else {
            GL11.glDisable((int)3089);
        }
    }

    public static boolean isInViewFrustum(Entity entity) {
        if (entity == null) {
            return false;
        }
        return RenderUtils.isInViewFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public static boolean isInViewFrustum(AxisAlignedBB axisAlignedBB) {
        if (axisAlignedBB == null) {
            return false;
        }
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) {
            return true;
        }
        frustum.setPosition(entity.posX, entity.posY, entity.posZ);
        return frustum.isBoundingBoxInFrustum(axisAlignedBB);
    }

    public static boolean isWithinDistanceSqToRenderView(Entity entity, double d) {
        if (entity == null) {
            return false;
        }
        Entity entity2 = mc.getRenderViewEntity();
        if (entity2 == null) {
            return false;
        }
        return entity.getDistanceSqToEntity(entity2) <= d;
    }

    public static boolean isBlockPosWithinDistanceSqToView(BlockPos blockPos, double d) {
        double d2;
        double d3;
        if (blockPos == null) {
            return false;
        }
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) {
            return false;
        }
        double d4 = (double)blockPos.getX() + 0.5 - entity.posX;
        return d4 * d4 + (d3 = (double)blockPos.getY() + 0.5 - entity.posY) * d3 + (d2 = (double)blockPos.getZ() + 0.5 - entity.posZ) * d2 <= d;
    }

    public static void drawRect(double d, double d2, double d3, double d4, int n) {
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GlStateManager.color((float)f2, (float)f3, (float)f4, (float)f);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(d, d4, 0.0).endVertex();
        worldRenderer.pos(d3, d4, 0.0).endVertex();
        worldRenderer.pos(d3, d2, 0.0).endVertex();
        worldRenderer.pos(d, d2, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.popMatrix();
    }

    public static void drawPlayerBoundingBox(Vec3 vec3, int n) {
        GlStateManager.pushMatrix();
        double d = vec3.xCoord - RenderUtils.mc.getRenderManager().viewerPosX;
        double d2 = vec3.yCoord - RenderUtils.mc.getRenderManager().viewerPosY;
        double d3 = vec3.zCoord - RenderUtils.mc.getRenderManager().viewerPosZ;
        AxisAlignedBB axisAlignedBB = RenderUtils.mc.thePlayer.getEntityBoundingBox().expand(0.1, 0.1, 0.1);
        AxisAlignedBB axisAlignedBB2 = new AxisAlignedBB(axisAlignedBB.minX - RenderUtils.mc.thePlayer.posX + d, axisAlignedBB.minY - RenderUtils.mc.thePlayer.posY + d2, axisAlignedBB.minZ - RenderUtils.mc.thePlayer.posZ + d3, axisAlignedBB.maxX - RenderUtils.mc.thePlayer.posX + d, axisAlignedBB.maxY - RenderUtils.mc.thePlayer.posY + d2, axisAlignedBB.maxZ - RenderUtils.mc.thePlayer.posZ + d3);
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glLineWidth((float)2.0f);
        GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f);
        RenderUtils.drawBoundingBox(axisAlignedBB2, f2, f3, f4, f);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    public static void drawOutline(float f, float f2, float f3, float f4, float f5, int n) {
        float f6 = (float)(n >> 24 & 0xFF) / 255.0f;
        float f7 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f8 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f9 = (float)(n & 0xFF) / 255.0f;
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GlStateManager.pushMatrix();
        GL11.glColor4f((float)f7, (float)f8, (float)f9, (float)f6);
        GL11.glLineWidth((float)f5);
        GL11.glBegin((int)1);
        GL11.glVertex2d((double)f, (double)f2);
        GL11.glVertex2d((double)f, (double)f4);
        GL11.glVertex2d((double)f3, (double)f4);
        GL11.glVertex2d((double)f3, (double)f2);
        GL11.glVertex2d((double)f, (double)f2);
        GL11.glVertex2d((double)f3, (double)f2);
        GL11.glVertex2d((double)f, (double)f4);
        GL11.glVertex2d((double)f3, (double)f4);
        GL11.glEnd();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.popMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
    }

    public static void renderBox(double d, double d2, double d3, double d4, double d5, double d6, int n, boolean bl, boolean bl2) {
        double d7 = d - RenderUtils.mc.getRenderManager().viewerPosX;
        double d8 = d2 - RenderUtils.mc.getRenderManager().viewerPosY;
        double d9 = d3 - RenderUtils.mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)2.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f);
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(d7, d8, d9, d7 + d4, d8 + d5, d9 + d6);
        if (bl) {
            RenderGlobal.drawSelectionBoundingBox((AxisAlignedBB)axisAlignedBB);
        }
        if (bl2) {
            RenderUtils.drawBoundingBox(axisAlignedBB, f2, f3, f4);
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    public static void renderBlockFaces(BlockPos blockPos, int n, boolean bl, boolean bl2, Set<EnumFacing> set) {
        if (set == null || set.isEmpty()) {
            return;
        }
        double d = (double)blockPos.getX() - RenderUtils.mc.getRenderManager().viewerPosX;
        double d2 = (double)blockPos.getY() - RenderUtils.mc.getRenderManager().viewerPosY;
        double d3 = (double)blockPos.getZ() - RenderUtils.mc.getRenderManager().viewerPosZ;
        double d4 = d + 1.0;
        double d5 = d2 + 1.0;
        double d6 = d3 + 1.0;
        float f = (float)(n >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(n & 0xFF) / 255.0f;
        float f4 = (float)(n >> 24 & 0xFF) / 255.0f;
        float f5 = 0.25f;
        GlStateManager.pushMatrix();
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)2.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        if (bl2) {
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            if (set.contains(EnumFacing.DOWN)) {
                worldRenderer.pos(d, d2, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d2, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d2, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d2, d6).color(f, f2, f3, f5).endVertex();
            }
            if (set.contains(EnumFacing.UP)) {
                worldRenderer.pos(d, d5, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d5, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d5, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d5, d3).color(f, f2, f3, f5).endVertex();
            }
            if (set.contains(EnumFacing.NORTH)) {
                worldRenderer.pos(d, d2, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d5, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d5, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d2, d3).color(f, f2, f3, f5).endVertex();
            }
            if (set.contains(EnumFacing.SOUTH)) {
                worldRenderer.pos(d4, d2, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d5, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d5, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d2, d6).color(f, f2, f3, f5).endVertex();
            }
            if (set.contains(EnumFacing.WEST)) {
                worldRenderer.pos(d, d2, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d5, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d5, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d, d2, d6).color(f, f2, f3, f5).endVertex();
            }
            if (set.contains(EnumFacing.EAST)) {
                worldRenderer.pos(d4, d2, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d5, d6).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d5, d3).color(f, f2, f3, f5).endVertex();
                worldRenderer.pos(d4, d2, d3).color(f, f2, f3, f5).endVertex();
            }
            tessellator.draw();
        }
        if (bl) {
            GL11.glColor4f((float)f, (float)f2, (float)f3, (float)f4);
            worldRenderer.begin(1, DefaultVertexFormats.POSITION);
            if (set.contains(EnumFacing.DOWN)) {
                worldRenderer.pos(d, d2, d3).endVertex();
                worldRenderer.pos(d4, d2, d3).endVertex();
                worldRenderer.pos(d4, d2, d3).endVertex();
                worldRenderer.pos(d4, d2, d6).endVertex();
                worldRenderer.pos(d4, d2, d6).endVertex();
                worldRenderer.pos(d, d2, d6).endVertex();
                worldRenderer.pos(d, d2, d6).endVertex();
                worldRenderer.pos(d, d2, d3).endVertex();
            }
            if (set.contains(EnumFacing.UP)) {
                worldRenderer.pos(d, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d6).endVertex();
                worldRenderer.pos(d4, d5, d6).endVertex();
                worldRenderer.pos(d, d5, d6).endVertex();
                worldRenderer.pos(d, d5, d6).endVertex();
                worldRenderer.pos(d, d5, d3).endVertex();
            }
            if (set.contains(EnumFacing.NORTH)) {
                worldRenderer.pos(d, d2, d3).endVertex();
                worldRenderer.pos(d, d5, d3).endVertex();
                worldRenderer.pos(d, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d3).endVertex();
                worldRenderer.pos(d4, d2, d3).endVertex();
                worldRenderer.pos(d4, d2, d3).endVertex();
                worldRenderer.pos(d, d2, d3).endVertex();
            }
            if (set.contains(EnumFacing.SOUTH)) {
                worldRenderer.pos(d, d2, d6).endVertex();
                worldRenderer.pos(d, d5, d6).endVertex();
                worldRenderer.pos(d, d5, d6).endVertex();
                worldRenderer.pos(d4, d5, d6).endVertex();
                worldRenderer.pos(d4, d5, d6).endVertex();
                worldRenderer.pos(d4, d2, d6).endVertex();
                worldRenderer.pos(d4, d2, d6).endVertex();
                worldRenderer.pos(d, d2, d6).endVertex();
            }
            if (set.contains(EnumFacing.WEST)) {
                worldRenderer.pos(d, d2, d3).endVertex();
                worldRenderer.pos(d, d5, d3).endVertex();
                worldRenderer.pos(d, d5, d3).endVertex();
                worldRenderer.pos(d, d5, d6).endVertex();
                worldRenderer.pos(d, d5, d6).endVertex();
                worldRenderer.pos(d, d2, d6).endVertex();
                worldRenderer.pos(d, d2, d6).endVertex();
                worldRenderer.pos(d, d2, d3).endVertex();
            }
            if (set.contains(EnumFacing.EAST)) {
                worldRenderer.pos(d4, d2, d3).endVertex();
                worldRenderer.pos(d4, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d3).endVertex();
                worldRenderer.pos(d4, d5, d6).endVertex();
                worldRenderer.pos(d4, d5, d6).endVertex();
                worldRenderer.pos(d4, d2, d6).endVertex();
                worldRenderer.pos(d4, d2, d6).endVertex();
                worldRenderer.pos(d4, d2, d3).endVertex();
            }
            tessellator.draw();
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    private static void drawBoxFaceVertex(WorldRenderer worldRenderer, double d, double d2, double d3, int n) {
        worldRenderer.pos(d, d2, d3).color(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, n >> 24 & 0xFF).endVertex();
    }

    private static void drawBoxFaceVertices(WorldRenderer worldRenderer, EnumFacing enumFacing, AxisAlignedBB axisAlignedBB, int n, int n2) {
        switch (1.$SwitchMap$net$minecraft$util$EnumFacing[enumFacing.ordinal()]) {
            case 1: {
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, n2);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, n2);
                break;
            }
            case 2: {
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, n2);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, n2);
                break;
            }
            case 3: {
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, n2);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, n2);
                break;
            }
            case 4: {
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, n2);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, n2);
                break;
            }
            case 5: {
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, n2);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, n2);
                break;
            }
            case 6: {
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, n2);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, n);
                RenderUtils.drawBoxFaceVertex(worldRenderer, axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, n2);
            }
        }
    }

    public static void drawBoxFace(AxisAlignedBB axisAlignedBB, EnumFacing enumFacing, int n, int n2, boolean bl, boolean bl2) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        if (bl) {
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            RenderUtils.drawBoxFaceVertices(worldRenderer, enumFacing, axisAlignedBB, n, n);
            tessellator.draw();
        }
        if (bl2) {
            worldRenderer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            RenderUtils.drawBoxFaceVertices(worldRenderer, enumFacing, axisAlignedBB, n2, n2);
            tessellator.draw();
        }
    }

    public static void renderBlockShape(BlockPos blockPos, IBlockState iBlockState, AxisAlignedBB axisAlignedBB2, int n, boolean bl3, boolean bl4) {
        if (axisAlignedBB2 == null) {
            return;
        }
        double d = RenderUtils.mc.getRenderManager().viewerPosX;
        double d2 = RenderUtils.mc.getRenderManager().viewerPosY;
        double d3 = RenderUtils.mc.getRenderManager().viewerPosZ;
        int n2 = n;
        int n7 = n | 0xFF000000;
        GlStateManager.pushMatrix();
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)2.0f);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        if (iBlockState.getBlock() instanceof BlockStairs) {
            StairsUtils.drawStairs((BlockPos)blockPos, (IBlockState)iBlockState, (AxisAlignedBB)axisAlignedBB2, null, (double)d, (double)d2, (double)d3, (int)n2, (int)n7, (int)n7, (int)n7, (boolean)bl4, (boolean)bl3, (axisAlignedBB, enumFacing, n3, n4, n5, n6, bl, bl2) -> RenderUtils.drawBoxFace(axisAlignedBB, enumFacing, n2, n7, bl, bl2));
        } else {
            AxisAlignedBB axisAlignedBB3 = axisAlignedBB2.offset(-d, -d2, -d3);
            for (EnumFacing enumFacing2 : BLOCK_SHAPE_FACES) {
                RenderUtils.drawBoxFace(axisAlignedBB3, enumFacing2, n2, n7, bl4, bl3);
            }
        }
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    public static void renderBPS(boolean bl, boolean bl2) {
        double d;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String string = "";
        int n = -1;
        if (bl) {
            d = Utils.gbps((Entity)(Freecam.freeEntity == null ? RenderUtils.mc.thePlayer : Freecam.freeEntity), 2);
            n = d < 10.0 ? Color.green.getRGB() : (d < 30.0 ? Color.yellow.getRGB() : (d < 60.0 ? Color.orange.getRGB() : (d < 160.0 ? Color.red.getRGB() : Color.black.getRGB())));
            string = string + d + "bps";
        }
        if (bl2) {
            d = Utils.getHorizontalSpeed();
            if (!string.isEmpty()) {
                string = string + " ";
            }
            string = string + Utils.round(d, 3);
        }
        RenderUtils.mc.fontRendererObj.drawString(string, (float)(scaledResolution.getScaledWidth() / 2 - RenderUtils.mc.fontRendererObj.getStringWidth(string) / 2), (float)(scaledResolution.getScaledHeight() / 2 + 15), n, false);
    }

    public static void renderEntity(Entity entity, int n, double d, double d2, int n2, boolean bl) {
        if (entity instanceof EntityLivingBase) {
            float f = ((IAccessorMinecraft)RenderUtils.mc).valen$lunar$getTimer().renderPartialTicks;
            double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)f - RenderUtils.mc.getRenderManager().viewerPosX;
            double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)f - RenderUtils.mc.getRenderManager().viewerPosY;
            double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)f - RenderUtils.mc.getRenderManager().viewerPosZ;
            float f2 = (float)d / 40.0f;
            if (entity instanceof EntityPlayer && bl && ((EntityPlayer)entity).hurtTime != 0) {
                n2 = Color.RED.getRGB();
            }
            GlStateManager.pushMatrix();
            if (n == 3) {
                GlStateManager.translate((double)d3, (double)(d4 - 0.2), (double)d5);
                LunarMatrices.rotate(-RenderUtils.mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
                GlStateManager.disableDepth();
                GlStateManager.scale((float)(0.03f + f2), (float)(0.03f + f2), (float)(0.03f + f2));
                int n3 = Color.black.getRGB();
                Gui.drawRect((int)-20, (int)-1, (int)-26, (int)75, (int)n3);
                Gui.drawRect((int)20, (int)-1, (int)26, (int)75, (int)n3);
                Gui.drawRect((int)-20, (int)-1, (int)21, (int)5, (int)n3);
                Gui.drawRect((int)-20, (int)70, (int)21, (int)75, (int)n3);
                if (n2 != 0) {
                    Gui.drawRect((int)-21, (int)0, (int)-25, (int)74, (int)n2);
                    Gui.drawRect((int)21, (int)0, (int)25, (int)74, (int)n2);
                    Gui.drawRect((int)-21, (int)0, (int)24, (int)4, (int)n2);
                    Gui.drawRect((int)-21, (int)71, (int)25, (int)74, (int)n2);
                } else {
                    int n4 = Utils.getChroma(2L, 0L);
                    int n5 = Utils.getChroma(2L, 1000L);
                    RenderUtils.dGR(-21, 0, -25, 74, n4, n5);
                    RenderUtils.dGR(21, 0, 25, 74, n4, n5);
                    Gui.drawRect((int)-21, (int)0, (int)21, (int)4, (int)n5);
                    Gui.drawRect((int)-21, (int)71, (int)21, (int)74, (int)n4);
                }
                GlStateManager.enableDepth();
            } else if (n == 4) {
                EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
                double d6 = entityLivingBase.getHealth() / entityLivingBase.getMaxHealth();
                int n6 = (int)(74.0 * d6);
                int n7 = d6 < 0.3 ? Color.red.getRGB() : (d6 < 0.5 ? Color.orange.getRGB() : (d6 < 0.7 ? Color.yellow.getRGB() : Color.green.getRGB()));
                GlStateManager.translate((double)d3, (double)(d4 - 0.2), (double)d5);
                LunarMatrices.rotate(-RenderUtils.mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
                GlStateManager.disableDepth();
                GlStateManager.scale((float)(0.03f + f2), (float)(0.03f + f2), (float)(0.03f + f2));
                int n8 = (int)(21.0 + d2 * 2.0);
                Gui.drawRect((int)n8, (int)-1, (int)(n8 + 4), (int)75, (int)Color.black.getRGB());
                Gui.drawRect((int)(n8 + 1), (int)n6, (int)(n8 + 3), (int)74, (int)Color.darkGray.getRGB());
                Gui.drawRect((int)(n8 + 1), (int)0, (int)(n8 + 3), (int)n6, (int)n7);
                GlStateManager.enableDepth();
            } else if (n == 6) {
                RenderUtils.drawCircle(d3, d4, d5, 0.7f, 45, 1.5f, n2, n2 == 0);
            } else {
                if (n2 == 0) {
                    n2 = Utils.getChroma(2L, 0L);
                }
                float f3 = (float)(n2 >> 24 & 0xFF) / 255.0f;
                float f4 = (float)(n2 >> 16 & 0xFF) / 255.0f;
                float f5 = (float)(n2 >> 8 & 0xFF) / 255.0f;
                float f6 = (float)(n2 & 0xFF) / 255.0f;
                AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand(0.1 + d, 0.1 + d, 0.1 + d);
                AxisAlignedBB axisAlignedBB2 = new AxisAlignedBB(axisAlignedBB.minX - entity.posX + d3, axisAlignedBB.minY - entity.posY + d4, axisAlignedBB.minZ - entity.posZ + d5, axisAlignedBB.maxX - entity.posX + d3, axisAlignedBB.maxY - entity.posY + d4, axisAlignedBB.maxZ - entity.posZ + d5);
                GL11.glBlendFunc((int)770, (int)771);
                GL11.glEnable((int)3042);
                GL11.glDisable((int)3553);
                GL11.glDisable((int)2929);
                GL11.glDepthMask((boolean)false);
                GL11.glLineWidth((float)2.0f);
                GL11.glColor4f((float)f4, (float)f5, (float)f6, (float)f3);
                if (n == 1) {
                    RenderGlobal.drawSelectionBoundingBox((AxisAlignedBB)axisAlignedBB2);
                } else if (n == 2) {
                    RenderUtils.drawBoundingBox(axisAlignedBB2, f4, f5, f6);
                }
                GL11.glEnable((int)3553);
                GL11.glEnable((int)2929);
                GL11.glDepthMask((boolean)true);
                GL11.glDisable((int)3042);
            }
            GlStateManager.popMatrix();
        }
    }

    public static void drawPolygon(double d, double d2, double d3, int n, int n2) {
        if (n < 3) {
            return;
        }
        float f = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n2 & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f);
        worldRenderer.begin(6, DefaultVertexFormats.POSITION);
        for (int i = 0; i < n; ++i) {
            double d4 = Math.PI * 2 * (double)i / (double)n + Math.toRadians(180.0);
            worldRenderer.pos(d + Math.sin(d4) * d3, d2 + Math.cos(d4) * d3, 0.0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawOutlinedBox(AxisAlignedBB axisAlignedBB, double d, double d2, double d3) {
        AxisAlignedBB axisAlignedBB2 = axisAlignedBB.offset(-d, -d2, -d3);
        RenderGlobal.drawSelectionBoundingBox((AxisAlignedBB)axisAlignedBB2);
    }

    public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, float f, float f2, float f3) {
        RenderUtils.drawBoundingBox(axisAlignedBB, f, f2, f3, 0.25f);
    }

    public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, float f, float f2, float f3, float f4) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(f, f2, f3, f4).endVertex();
        tessellator.draw();
    }

    public static void renderBlockModel(IBlockState iBlockState, BlockPos blockPos, int n) {
        RenderUtils.renderBlockModel(iBlockState, blockPos.getX(), blockPos.getY(), blockPos.getZ(), n);
    }

    public static void renderBlockModel(IBlockState iBlockState, double d, double d2, double d3, int n) {
        Minecraft minecraft = Minecraft.getMinecraft();
        BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();
        IBakedModel iBakedModel = blockRendererDispatcher.getModelFromBlockState(iBlockState, (IBlockAccess)minecraft.theWorld, new BlockPos(d, d2, d3));
        double d4 = d - minecraft.getRenderManager().viewerPosX;
        double d5 = d2 - minecraft.getRenderManager().viewerPosY;
        double d6 = d3 - minecraft.getRenderManager().viewerPosZ;
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((double)d4, (double)d5, (double)d6);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc((int)770, (int)771);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.depthMask((boolean)false);
        GlStateManager.color((float)f2, (float)f3, (float)f4, (float)f);
        RenderUtils.renderModelColoredQuads(iBakedModel, f2, f3, f4, f);
        GlStateManager.depthMask((boolean)true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void renderModelColoredQuads(IBakedModel iBakedModel, float f, float f2, float f3, float f4) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        for (EnumFacing enumFacing : EnumFacing.values()) {
            for (BakedQuad bakedQuad : iBakedModel.getFaceQuads(enumFacing)) {
                RenderUtils.drawColoredQuad(worldRenderer, bakedQuad, f, f2, f3, f4, tessellator);
            }
        }
        for (BakedQuad bakedQuad : iBakedModel.getGeneralQuads()) {
            RenderUtils.drawColoredQuad(worldRenderer, bakedQuad, f, f2, f3, f4, tessellator);
        }
    }

    private static void drawColoredQuad(WorldRenderer worldRenderer, BakedQuad bakedQuad, float f, float f2, float f3, float f4, Tessellator tessellator) {
        int[] nArray = bakedQuad.getVertexData();
        int n = nArray.length / 4;
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; ++i) {
            int n2 = i * n;
            float f5 = Float.intBitsToFloat(nArray[n2]);
            float f6 = Float.intBitsToFloat(nArray[n2 + 1]);
            float f7 = Float.intBitsToFloat(nArray[n2 + 2]);
            worldRenderer.pos((double)f5, (double)f6, (double)f7).color(f, f2, f3, f4).endVertex();
        }
        tessellator.draw();
    }

    public static void drawTracerLine(Entity entity, int n, float f, float f2) {
        float f3;
        float f4;
        if (entity == null || mc.getRenderManager() == null) {
            return;
        }
        Entity entity2 = mc.getRenderViewEntity();
        if (entity2 == null) {
            entity2 = RenderUtils.mc.thePlayer;
        }
        if (entity2 == null) {
            return;
        }
        double d = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)f2 - RenderUtils.mc.getRenderManager().viewerPosX;
        double d2 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)f2 - RenderUtils.mc.getRenderManager().viewerPosY + (double)entity.getEyeHeight() + (entity.isSneaking() ? -0.125 : 0.0);
        double d3 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)f2 - RenderUtils.mc.getRenderManager().viewerPosZ;
        double d4 = 0.0;
        double d5 = entity2.getEyeHeight();
        double d6 = 0.0;
        if (entity2 == RenderUtils.mc.thePlayer && RenderUtils.mc.gameSettings.thirdPersonView == 0) {
            f4 = entity2.rotationYaw;
            f3 = entity2.rotationPitch;
            double d7 = -Math.sin(Math.toRadians(f4)) * Math.cos(Math.toRadians(f3));
            double d8 = -Math.sin(Math.toRadians(f3));
            double d9 = Math.cos(Math.toRadians(f4)) * Math.cos(Math.toRadians(f3));
            d4 = d7;
            d5 += d8;
            d6 = d9;
        }
        f4 = (float)(n >> 24 & 0xFF) / 255.0f;
        f3 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f5 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f6 = (float)(n & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glLineWidth((float)f);
        GL11.glColor4f((float)f3, (float)f5, (float)f6, (float)f4);
        GL11.glBegin((int)1);
        GL11.glVertex3d((double)d4, (double)d5, (double)d6);
        GL11.glVertex3d((double)d, (double)d2, (double)d3);
        GL11.glEnd();
        GL11.glLineWidth((float)1.0f);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GL11.glDisable((int)3042);
        GlStateManager.popMatrix();
    }

    public static void dGR(int n, int n2, int n3, int n4, int n5, int n6) {
        int n7;
        if (n < n3) {
            n7 = n;
            n = n3;
            n3 = n7;
        }
        if (n2 < n4) {
            n7 = n2;
            n2 = n4;
            n4 = n7;
        }
        float f = (float)(n5 >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n5 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n5 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n5 & 0xFF) / 255.0f;
        float f5 = (float)(n6 >> 24 & 0xFF) / 255.0f;
        float f6 = (float)(n6 >> 16 & 0xFF) / 255.0f;
        float f7 = (float)(n6 >> 8 & 0xFF) / 255.0f;
        float f8 = (float)(n6 & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GlStateManager.shadeModel((int)7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos((double)n3, (double)n2, 0.0).color(f2, f3, f4, f).endVertex();
        worldRenderer.pos((double)n, (double)n2, 0.0).color(f2, f3, f4, f).endVertex();
        worldRenderer.pos((double)n, (double)n4, 0.0).color(f6, f7, f8, f5).endVertex();
        worldRenderer.pos((double)n3, (double)n4, 0.0).color(f6, f7, f8, f5).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel((int)7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void db(int n, int n2, int n3) {
        int n4 = n3 == -1 ? -1089466352 : n3;
        Gui.drawRect((int)0, (int)0, (int)n, (int)n2, (int)n4);
    }

    public static void drawColoredString(String string, char c, int n, int n2, long l, long l2, boolean bl, FontRenderer fontRenderer) {
        int n3 = n;
        int n4 = 0;
        long l3 = 0L;
        for (int i = 0; i < string.length(); ++i) {
            char c2 = string.charAt(i);
            if (c2 == c) {
                n = n3;
                n2 += fontRenderer.FONT_HEIGHT + 5;
                l3 = l2 * (long)(++n4);
                continue;
            }
            fontRenderer.drawString(String.valueOf(c2), (float)n, (float)n2, Utils.getChroma(l, l3), bl);
            n += fontRenderer.getCharWidth(c2);
            if (c2 == ' ') continue;
            l3 -= 90L;
        }
    }

    public static void drawCircle(double d, double d2, double d3, double d4, int n, float f, int n2, boolean bl) {
        float f2 = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f3 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f4 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f5 = (float)(n2 & 0xFF) / 255.0f;
        RenderUtils.mc.entityRenderer.disableLightmap();
        GL11.glDisable((int)3553);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)2929);
        GL11.glEnable((int)2848);
        GL11.glDepthMask((boolean)false);
        GL11.glLineWidth((float)f);
        if (!bl) {
            GL11.glColor4f((float)f3, (float)f4, (float)f5, (float)f2);
        }
        GL11.glBegin((int)1);
        long l = 0L;
        long l2 = 15000L / (long)n;
        long l3 = l2 / 2L;
        for (int i = 0; i < n * 2; ++i) {
            if (bl) {
                if (i % 2 != 0) {
                    if (i == 47) {
                        l = l3;
                    }
                    l += l2;
                }
                int n3 = Utils.getChroma(2L, l);
                float f6 = (float)(n3 >> 16 & 0xFF) / 255.0f;
                float f7 = (float)(n3 >> 8 & 0xFF) / 255.0f;
                float f8 = (float)(n3 & 0xFF) / 255.0f;
                GL11.glColor3f((float)f6, (float)f7, (float)f8);
            }
            double d5 = Math.PI * 2 * (double)i / (double)n + Math.toRadians(180.0);
            GL11.glVertex3d((double)(d + Math.cos(d5) * d4), (double)d2, (double)(d3 + Math.sin(d5) * d4));
        }
        GL11.glEnd();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)2848);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3553);
        RenderUtils.mc.entityRenderer.enableLightmap();
    }

    public static void drawCaret(float f, float f2, int n, double d, double d2) {
        GlStateManager.pushMatrix();
        GL11.glEnable((int)2848);
        GL11.glDisable((int)3553);
        RenderUtils.glColor(n);
        GL11.glLineWidth((float)((float)d));
        float f3 = (float)(d / 2.0);
        float f4 = f3 / 2.0f;
        float f5 = f3 / 2.0f;
        GL11.glBegin((int)1);
        GL11.glVertex2d((double)(f - f4), (double)(f2 + f5));
        GL11.glVertex2d((double)((double)f + d2 - (double)f4), (double)((double)f2 - d2 + (double)f5));
        GL11.glVertex2d((double)((double)f + d2 - (double)f4), (double)((double)f2 - d2 + (double)f5));
        GL11.glVertex2d((double)((double)f + 2.0 * d2 - (double)f4), (double)(f2 + f5));
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GlStateManager.popMatrix();
    }

    public static void drawTriangle(double d, double d2, double d3, double d4, double d5, int n) {
        boolean bl = GL11.glIsEnabled((int)3042);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GlStateManager.pushMatrix();
        RenderUtils.glColor(n);
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)d, (double)d2);
        GL11.glVertex2d((double)(d - d3 / d4), (double)(d2 + d3));
        GL11.glVertex2d((double)d, (double)(d2 + d3 / d5));
        GL11.glVertex2d((double)(d + d3 / d4), (double)(d2 + d3));
        GL11.glVertex2d((double)d, (double)d2);
        GL11.glEnd();
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.8f);
        GL11.glBegin((int)2);
        GL11.glVertex2d((double)d, (double)d2);
        GL11.glVertex2d((double)(d - d3 / d4), (double)(d2 + d3));
        GL11.glVertex2d((double)d, (double)(d2 + d3 / d5));
        GL11.glVertex2d((double)(d + d3 / d4), (double)(d2 + d3));
        GL11.glVertex2d((double)d, (double)d2);
        GL11.glEnd();
        GlStateManager.popMatrix();
        GL11.glEnable((int)3553);
        if (!bl) {
            GL11.glDisable((int)3042);
        }
        GL11.glDisable((int)2848);
    }

    public static void glColor(int n) {
        GL11.glColor4f((float)((float)(n >> 16 & 0xFF) / 255.0f), (float)((float)(n >> 8 & 0xFF) / 255.0f), (float)((float)(n & 0xFF) / 255.0f), (float)((float)(n >> 24 & 0xFF) / 255.0f));
    }

    private static double getSquircleSin(double d) {
        double d2 = Math.sin(d);
        return Math.sqrt(Math.abs(d2)) * Math.signum(d2);
    }

    private static double getSquircleCos(double d) {
        double d2 = Math.cos(d);
        return Math.sqrt(Math.abs(d2)) * Math.signum(d2);
    }

    public static void drawRoundedGradientOutlinedRectangle(float f, float f2, float f3, float f4, float f5, int n, int n2, int n3) {
        double d;
        int n4;
        f *= 2.0f;
        f2 *= 2.0f;
        f3 *= 2.0f;
        f4 *= 2.0f;
        GlStateManager.pushMatrix();
        GL11.glPushAttrib((int)1048575);
        GlStateManager.scale((double)0.5, (double)0.5, (double)0.5);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glBegin((int)9);
        RenderUtils.glColor(n);
        for (n4 = 0; n4 <= 90; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        for (n4 = 90; n4 <= 180; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        for (n4 = 0; n4 <= 90; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5));
        }
        for (n4 = 90; n4 <= 180; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5));
        }
        GL11.glEnd();
        GlStateManager.pushMatrix();
        GL11.glShadeModel((int)7425);
        GL11.glLineWidth((float)2.0f);
        GL11.glBegin((int)2);
        if ((long)n2 != 0L) {
            RenderUtils.glColor(n2);
        }
        for (n4 = 0; n4 <= 90; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        for (n4 = 90; n4 <= 180; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        if (n3 != 0) {
            RenderUtils.glColor(n3);
        }
        for (n4 = 0; n4 <= 90; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5));
        }
        for (n4 = 90; n4 <= 180; n4 += 3) {
            d = (float)n4 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5));
        }
        GL11.glEnd();
        GlStateManager.popMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
        GL11.glLineWidth((float)1.0f);
        GL11.glShadeModel((int)7424);
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void draw2DPolygon(double d, double d2, double d3, int n, int n2) {
        if (n < 3) {
            return;
        }
        float f = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n2 & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GL11.glEnable((int)2848);
        GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f);
        double d4 = Math.toRadians(180.0);
        worldRenderer.begin(6, DefaultVertexFormats.POSITION);
        for (int i = 0; i < n; ++i) {
            double d5 = Math.PI * 2 * (double)i / (double)n + d4;
            worldRenderer.pos(d + Math.sin(d5) * d3, d2 + Math.cos(d5) * d3, 0.0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return RenderUtils.createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean bl) {
        if (RenderUtils.needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(RenderUtils.mc.displayWidth, RenderUtils.mc.displayHeight, bl);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != RenderUtils.mc.displayWidth || framebuffer.framebufferHeight != RenderUtils.mc.displayHeight;
    }

    public static void drawFramebufferFullscreen(Framebuffer framebuffer) {
        if (framebuffer == null) {
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        GlStateManager.bindTexture((int)framebuffer.framebufferTexture);
        GL11.glBegin((int)7);
        GL11.glTexCoord2d((double)0.0, (double)1.0);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glTexCoord2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)0.0, (double)scaledResolution.getScaledHeight());
        GL11.glTexCoord2d((double)1.0, (double)0.0);
        GL11.glVertex2d((double)scaledResolution.getScaledWidth(), (double)scaledResolution.getScaledHeight());
        GL11.glTexCoord2d((double)1.0, (double)1.0);
        GL11.glVertex2d((double)scaledResolution.getScaledWidth(), (double)0.0);
        GL11.glEnd();
    }

    public static void bindTexture(int n) {
        GL11.glBindTexture((int)3553, (int)n);
    }

    public static void setAlphaLimit(float f) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc((int)516, (float)((float)((double)f * 0.01)));
    }

    public static Color interpolateColorC(Color color, Color color2, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        return new Color(RenderUtils.interpolateInt(color.getRed(), color2.getRed(), f), RenderUtils.interpolateInt(color.getGreen(), color2.getGreen(), f), RenderUtils.interpolateInt(color.getBlue(), color2.getBlue(), f), RenderUtils.interpolateInt(color.getAlpha(), color2.getAlpha(), f));
    }

    public static int interpolateInt(int n, int n2, double d) {
        return RenderUtils.interpolate(n, n2, (float)d).intValue();
    }

    public static Double interpolate(double d, double d2, double d3) {
        return d + (d2 - d) * d3;
    }

    public static void resetColor() {
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static Vec3 convertTo2D(int n, double d, double d2, double d3) {
        GL11.glGetFloat((int)2982, (FloatBuffer)MODELVIEW);
        GL11.glGetFloat((int)2983, (FloatBuffer)PROJECTION);
        GL11.glGetInteger((int)2978, (IntBuffer)VIEWPORT);
        boolean bl = GLU.gluProject((float)((float)d), (float)((float)d2), (float)((float)d3), (FloatBuffer)MODELVIEW, (FloatBuffer)PROJECTION, (IntBuffer)VIEWPORT, (FloatBuffer)SCREEN_COORDS);
        if (bl) {
            return new Vec3((double)(SCREEN_COORDS.get(0) / (float)n), (double)(((float)Display.getHeight() - SCREEN_COORDS.get(1)) / (float)n), (double)SCREEN_COORDS.get(2));
        }
        return null;
    }

    public static ProjectionContext captureProjectionContext(ProjectionContext projectionContext, int n) {
        if (projectionContext == null) {
            projectionContext = new ProjectionContext();
        }
        ProjectionContext.access$002((ProjectionContext)projectionContext, (int)n);
        ProjectionContext.access$100((ProjectionContext)projectionContext).clear();
        ProjectionContext.access$200((ProjectionContext)projectionContext).clear();
        ProjectionContext.access$300((ProjectionContext)projectionContext).clear();
        GL11.glGetFloat((int)2982, (FloatBuffer)ProjectionContext.access$100((ProjectionContext)projectionContext));
        GL11.glGetFloat((int)2983, (FloatBuffer)ProjectionContext.access$200((ProjectionContext)projectionContext));
        GL11.glGetInteger((int)2978, (IntBuffer)ProjectionContext.access$300((ProjectionContext)projectionContext));
        ProjectionContext.access$100((ProjectionContext)projectionContext).rewind();
        ProjectionContext.access$200((ProjectionContext)projectionContext).rewind();
        ProjectionContext.access$300((ProjectionContext)projectionContext).rewind();
        return projectionContext;
    }

    public static boolean projectTo2D(ProjectionContext projectionContext, double d, double d2, double d3, double[] dArray) {
        if (projectionContext == null || dArray == null || dArray.length < 3) {
            return false;
        }
        ProjectionContext.access$400((ProjectionContext)projectionContext).clear();
        boolean bl = GLU.gluProject((float)((float)d), (float)((float)d2), (float)((float)d3), (FloatBuffer)ProjectionContext.access$100((ProjectionContext)projectionContext), (FloatBuffer)ProjectionContext.access$200((ProjectionContext)projectionContext), (IntBuffer)ProjectionContext.access$300((ProjectionContext)projectionContext), (FloatBuffer)ProjectionContext.access$400((ProjectionContext)projectionContext));
        if (!bl) {
            return false;
        }
        dArray[0] = ProjectionContext.access$400((ProjectionContext)projectionContext).get(0) / (float)ProjectionContext.access$000((ProjectionContext)projectionContext);
        dArray[1] = ((float)Display.getHeight() - ProjectionContext.access$400((ProjectionContext)projectionContext).get(1)) / (float)ProjectionContext.access$000((ProjectionContext)projectionContext);
        dArray[2] = ProjectionContext.access$400((ProjectionContext)projectionContext).get(2);
        return true;
    }

    public static void drawRoundedRectangle(float f, float f2, float f3, float f4, float f5, int n) {
        double d;
        int n2;
        if (f3 <= f) {
            return;
        }
        float f6 = f3 - f;
        if (f6 < 3.0f) {
            f5 = Math.min(f5, f6 / 2.0f);
        }
        f = (float)((double)f * 2.0);
        f2 = (float)((double)f2 * 2.0);
        f3 = (float)((double)f3 * 2.0);
        f4 = (float)((double)f4 * 2.0);
        GlStateManager.pushMatrix();
        GL11.glPushAttrib((int)1048575);
        GlStateManager.scale((double)0.5, (double)0.5, (double)0.5);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glBegin((int)9);
        RenderUtils.glColor(n);
        for (n2 = 0; n2 <= 90; n2 += 3) {
            d = (float)n2 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        for (n2 = 90; n2 <= 180; n2 += 3) {
            d = (float)n2 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        if ((double)(f3 - f) >= 4.5) {
            for (n2 = 0; n2 <= 90; ++n2) {
                d = (float)n2 * ((float)Math.PI / 180);
                GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5));
            }
            for (n2 = 90; n2 <= 180; ++n2) {
                d = (float)n2 * ((float)Math.PI / 180);
                GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5));
            }
        }
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void drawRectangleGL(float f, float f2, float f3, float f4, int n) {
        GlStateManager.pushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        RenderUtils.glColor(n);
        GL11.glBegin((int)7);
        GL11.glVertex2f((float)f, (float)f2);
        GL11.glVertex2f((float)f, (float)f4);
        GL11.glVertex2f((float)f3, (float)f4);
        GL11.glVertex2f((float)f3, (float)f2);
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.popMatrix();
    }

    public static void drawRoundedGradientRect(float f, float f2, float f3, float f4, float f5, int n, int n2, int n3, int n4) {
        double d;
        int n5;
        if (f3 <= f) {
            return;
        }
        float f6 = f3 - f;
        if (f6 < 3.0f) {
            f5 = Math.min(f5, f6 / 2.0f);
        }
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glShadeModel((int)7425);
        GlStateManager.pushMatrix();
        GL11.glPushAttrib((int)1048575);
        GlStateManager.scale((double)0.5, (double)0.5, (double)0.5);
        f = (float)((double)f * 2.0);
        f2 = (float)((double)f2 * 2.0);
        f3 = (float)((double)f3 * 2.0);
        f4 = (float)((double)f4 * 2.0);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        RenderUtils.glColor(n);
        GL11.glEnable((int)2848);
        GL11.glShadeModel((int)7425);
        GL11.glBegin((int)9);
        for (n5 = 0; n5 <= 90; n5 += 3) {
            d = (float)n5 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        RenderUtils.glColor(n2);
        for (n5 = 90; n5 <= 180; n5 += 3) {
            d = (float)n5 * ((float)Math.PI / 180);
            GL11.glVertex2d((double)((double)(f + f5) + RenderUtils.getSquircleSin(d) * (double)f5 * -1.0), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5 * -1.0));
        }
        if ((double)(f3 - f) >= 4.5) {
            RenderUtils.glColor(n3);
            for (n5 = 0; n5 <= 90; n5 += 3) {
                d = (float)n5 * ((float)Math.PI / 180);
                GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f4 - f5) + RenderUtils.getSquircleCos(d) * (double)f5));
            }
            RenderUtils.glColor(n4);
            for (n5 = 90; n5 <= 180; n5 += 3) {
                d = (float)n5 * ((float)Math.PI / 180);
                GL11.glVertex2d((double)((double)(f3 - f5) + RenderUtils.getSquircleSin(d) * (double)f5), (double)((double)(f2 + f5) + RenderUtils.getSquircleCos(d) * (double)f5));
            }
        }
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3553);
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glShadeModel((int)7424);
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static int setAlpha(int n, double d) {
        if (d < 0.0 || d > 1.0) {
            d = 0.5;
        }
        int n2 = n >> 16 & 0xFF;
        int n3 = n >> 8 & 0xFF;
        int n4 = n & 0xFF;
        int n5 = (int)(d * 255.0);
        int n6 = n5 << 24 | n2 << 16 | n3 << 8 | n4;
        return n6;
    }

    public static void draw2DCircle(float f, float f2, float f3, int n, float f4, float f5, float f6, float f7, float f8) {
        GlStateManager.pushMatrix();
        GL11.glEnable((int)3042);
        GL11.glEnable((int)2884);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glColor4f((float)f5, (float)f6, (float)f7, (float)f8);
        GL11.glLineWidth((float)f4);
        GL11.glBegin((int)2);
        for (int i = 0; i <= n; ++i) {
            double d = Math.PI * 2 * (double)i / (double)n;
            float f9 = (float)((double)f3 * Math.cos(d)) + f;
            float f10 = (float)((double)f3 * Math.sin(d)) + f2;
            GL11.glVertex2f((float)f9, (float)f10);
        }
        GL11.glEnd();
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glLineWidth((float)1.0f);
        GlStateManager.popMatrix();
    }

    public static void draw2DCircleArc(float f, float f2, float f3, float f4, float f5, float f6, int n) {
        float f7 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f8 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f9 = (float)(n & 0xFF) / 255.0f;
        float f10 = (float)(n >> 24 & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        GL11.glEnable((int)3042);
        GL11.glEnable((int)2884);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glColor4f((float)f7, (float)f8, (float)f9, (float)f10);
        GL11.glLineWidth((float)f6);
        GL11.glBegin((int)3);
        for (float f11 = f4; f11 <= f5; f11 += 1.0f) {
            double d = Math.toRadians(f11 + 180.0f);
            float f12 = (float)((double)f3 * Math.cos(d)) + f;
            float f13 = (float)((double)f3 * Math.sin(d)) + f2;
            GL11.glVertex2f((float)f12, (float)f13);
        }
        GL11.glEnd();
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glLineWidth((float)1.0f);
        GlStateManager.popMatrix();
    }

    public static void drawHorizontalGradientRect(float f, float f2, float f3, float f4, int n, int n2) {
        float f5 = (float)(n >> 24 & 0xFF) / 255.0f;
        float f6 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f7 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f8 = (float)(n & 0xFF) / 255.0f;
        float f9 = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f10 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f11 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f12 = (float)(n2 & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GlStateManager.shadeModel((int)7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos((double)f, (double)f4, 0.0).color(f6, f7, f8, f5).endVertex();
        worldRenderer.pos((double)f3, (double)f4, 0.0).color(f10, f11, f12, f9).endVertex();
        worldRenderer.pos((double)f3, (double)f2, 0.0).color(f10, f11, f12, f9).endVertex();
        worldRenderer.pos((double)f, (double)f2, 0.0).color(f6, f7, f8, f5).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel((int)7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawVerticalGradientRect(float f, float f2, float f3, float f4, int n, int n2) {
        float f5 = (float)(n >> 24 & 0xFF) / 255.0f;
        float f6 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f7 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f8 = (float)(n & 0xFF) / 255.0f;
        float f9 = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f10 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f11 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f12 = (float)(n2 & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GlStateManager.shadeModel((int)7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos((double)f3, (double)f2, 0.0).color(f6, f7, f8, f5).endVertex();
        worldRenderer.pos((double)f, (double)f2, 0.0).color(f6, f7, f8, f5).endVertex();
        worldRenderer.pos((double)f, (double)f4, 0.0).color(f10, f11, f12, f9).endVertex();
        worldRenderer.pos((double)f3, (double)f4, 0.0).color(f10, f11, f12, f9).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel((int)7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void renderItemAndEffectIntoGui3D(ItemStack itemStack, int n, int n2) {
        if (itemStack == null) {
            return;
        }
        GlStateManager.pushMatrix();
        RenderUtils.prepareGuiItemRenderState();
        GlStateManager.depthMask((boolean)true);
        GlStateManager.clear((int)256);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.scale((float)1.0f, (float)1.0f, (float)-0.01f);
        RenderUtils.mc.getRenderItem().zLevel = -150.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, n, n2);
        RenderUtils.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        RenderUtils.prepareGuiTextureRenderState();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void renderItemAndEffectIntoGui2D(ItemStack itemStack, int n, int n2) {
        if (itemStack == null) {
            return;
        }
        RenderUtils.prepareGuiItemRenderState();
        RenderUtils.mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, n, n2 - 8);
        RenderUtils.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.disableDepth();
        RenderUtils.prepareGuiTextureRenderState();
        GlStateManager.disableBlend();
    }

    public static int getDurabilityColor(float f) {
        if (f > 0.6f) {
            return 65280;
        }
        if (f > 0.3f) {
            return 0xFFFF00;
        }
        return 0xFF0000;
    }

    public static void drawDurabilityBar(int n, int n2, float f) {
        int n3 = (int)(f * 13.0f);
        int n4 = RenderUtils.getDurabilityColor(f);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos((double)(n + 2), (double)(n2 + 15), 0.0).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
        worldRenderer.pos((double)(n + 2), (double)(n2 + 16), 0.0).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
        worldRenderer.pos((double)(n + 15), (double)(n2 + 16), 0.0).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
        worldRenderer.pos((double)(n + 15), (double)(n2 + 15), 0.0).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
        tessellator.draw();
        float f2 = (float)(n4 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n4 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n4 & 0xFF) / 255.0f;
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos((double)(n + 2), (double)(n2 + 15), 0.0).color(f2, f3, f4, 1.0f).endVertex();
        worldRenderer.pos((double)(n + 2), (double)(n2 + 16), 0.0).color(f2, f3, f4, 1.0f).endVertex();
        worldRenderer.pos((double)(n + 2 + n3), (double)(n2 + 16), 0.0).color(f2, f3, f4, 1.0f).endVertex();
        worldRenderer.pos((double)(n + 2 + n3), (double)(n2 + 15), 0.0).color(f2, f3, f4, 1.0f).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    public static int getEnchantColor(int n) {
        switch (n) {
            case 1: {
                return 0xFFFFFF;
            }
            case 2: {
                return 0x55FFFF;
            }
            case 3: {
                return 43690;
            }
            case 4: {
                return 0xAA00AA;
            }
            case 5: {
                return 0xFFAA00;
            }
            case 10: {
                return 0xFF55FF;
            }
        }
        return n > 5 ? 0xFF55FF : 0xFFFFFF;
    }

    public static int drawEnchantWithColor(FontRenderer fontRenderer, String string, int n, int n2, int n3) {
        int n4 = fontRenderer.drawStringWithShadow(string, (float)n2, (float)n3, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(String.valueOf(n), (float)n4, (float)n3, RenderUtils.getEnchantColor(n));
        return n4;
    }

    public static void prepareGuiTextureRenderState() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask((boolean)false);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc((int)516, (float)0.1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void prepareGuiItemRenderState() {
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc((int)516, (float)0.1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GlStateManager.enableDepth();
        GlStateManager.depthMask((boolean)true);
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static String getEnchantmentAbbreviated(int n) {
        switch (n) {
            case 0: {
                return "pt";
            }
            case 1: {
                return "frp";
            }
            case 2: {
                return "ff";
            }
            case 3: {
                return "blp";
            }
            case 4: {
                return "prp";
            }
            case 5: {
                return "thr";
            }
            case 6: {
                return "res";
            }
            case 7: {
                return "aa";
            }
            case 16: {
                return "sh";
            }
            case 17: {
                return "smt";
            }
            case 18: {
                return "ban";
            }
            case 19: {
                return "kb";
            }
            case 20: {
                return "fa";
            }
            case 21: {
                return "lot";
            }
            case 32: {
                return "eff";
            }
            case 33: {
                return "sil";
            }
            case 34: {
                return "ub";
            }
            case 35: {
                return "for";
            }
            case 48: {
                return "pow";
            }
            case 49: {
                return "pun";
            }
            case 50: {
                return "flm";
            }
            case 51: {
                return "inf";
            }
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static ResourceLocation buildWhiteMaskedTexture(String string, String string2, ResourceLocation resourceLocation) {
        try (InputStream inputStream = Valen.class.getResourceAsStream(string);){
            if (inputStream == null) {
                ResourceLocation resourceLocation3 = resourceLocation;
                return resourceLocation3;
            }
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            int n = bufferedImage.getWidth();
            int n2 = bufferedImage.getHeight();
            BufferedImage bufferedImage2 = new BufferedImage(n, n2, 2);
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n; ++j) {
                    int n3 = bufferedImage.getRGB(j, i) >>> 24 & 0xFF;
                    if (n3 <= 0) continue;
                    bufferedImage2.setRGB(j, i, n3 << 24 | 0xFFFFFF);
                }
            }
            ResourceLocation resourceLocation2 = mc.getTextureManager().getDynamicTextureLocation(string2, new DynamicTexture(bufferedImage2));
            return resourceLocation2;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return resourceLocation;
        }
    }

    public static ResourceLocation getIcon(String string) {
        ResourceLocation resourceLocation = iconCache.get(string);
        if (resourceLocation != null) {
            return resourceLocation;
        }
        String string2 = "client_icon_" + string.hashCode();
        ResourceLocation resourceLocation2 = RenderUtils.buildWhiteMaskedTexture(string, string2, null);
        if (resourceLocation2 != null) {
            iconCache.put(string, resourceLocation2);
        }
        return resourceLocation2;
    }

    public static void drawIcon(ResourceLocation resourceLocation, float f, float f2, int n, int n2) {
        if (resourceLocation == null) {
            return;
        }
        boolean bl = GL11.glIsEnabled((int)2929);
        boolean bl2 = GL11.glIsEnabled((int)3042);
        boolean bl3 = GL11.glGetBoolean((int)2930);
        RenderUtils.prepareGuiTextureRenderState();
        mc.getTextureManager().bindTexture(resourceLocation);
        float f3 = (float)(n2 >>> 24 & 0xFF) / 255.0f;
        float f4 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f5 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f6 = (float)(n2 & 0xFF) / 255.0f;
        GlStateManager.color((float)f4, (float)f5, (float)f6, (float)f3);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)f, (float)f2, (float)0.0f);
        Gui.drawModalRectWithCustomSizedTexture((int)0, (int)0, (float)0.0f, (float)0.0f, (int)n, (int)n, (float)n, (float)n);
        GlStateManager.popMatrix();
        RenderUtils.restoreGuiRenderState(bl, bl2, bl3);
    }

    public static void restoreGuiRenderState(boolean bl, boolean bl2, boolean bl3) {
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (bl2) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
        if (bl) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
        GlStateManager.depthMask((boolean)bl3);
    }
}

