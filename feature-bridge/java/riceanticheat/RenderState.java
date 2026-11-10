package dev.rice.anticheat;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
public final class RenderState {
        final int matrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        final boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),blend=GL11.glIsEnabled(GL11.GL_BLEND),alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST),lighting=GL11.glIsEnabled(GL11.GL_LIGHTING),tex=GL11.glIsEnabled(GL11.GL_TEXTURE_2D),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),colorMaterial=GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL),normalize=GL11.glIsEnabled(GL11.GL_NORMALIZE),rescale=GL11.glIsEnabled(GL12_RESCALE_NORMAL);
        static final int GL12_RESCALE_NORMAL=0x803a;
        final boolean light0=GL11.glIsEnabled(GL11.GL_LIGHT0),light1=GL11.glIsEnabled(GL11.GL_LIGHT1);
        final int src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        final FloatBuffer color=BufferUtils.createFloatBuffer(4);
        public RenderState(){GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);}
        public void restore(){
            if(depth)GlStateManager.enableDepth();else GlStateManager.disableDepth();GlStateManager.depthMask(mask);
            if(blend)GlStateManager.enableBlend();else GlStateManager.disableBlend();GlStateManager.tryBlendFuncSeparate(src,dst,srcA,dstA);
            if(alpha)GlStateManager.enableAlpha();else GlStateManager.disableAlpha();if(lighting)GlStateManager.enableLighting();else GlStateManager.disableLighting();
            if(light0)GlStateManager.enableLight(0);else GlStateManager.disableLight(0);if(light1)GlStateManager.enableLight(1);else GlStateManager.disableLight(1);
            if(colorMaterial)GlStateManager.enableColorMaterial();else GlStateManager.disableColorMaterial();if(normalize)GlStateManager.enableNormalize();else GlStateManager.disableNormalize();if(rescale)GlStateManager.enableRescaleNormal();else GlStateManager.disableRescaleNormal();
            if(cull)GlStateManager.enableCull();else GlStateManager.disableCull();GlStateManager.setActiveTexture(active);GlStateManager.bindTexture(texture);if(tex)GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D();GlStateManager.color(color.get(0),color.get(1),color.get(2),color.get(3));
        }
    }
