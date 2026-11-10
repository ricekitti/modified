package com.snowy.gui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
/** Native HUD styling boundary for the supplied BlockCounter layout. */
public final class HudStyle {
    public static FontRenderer font(){return Minecraft.getMinecraft().fontRendererObj;}
    public static int text(){return 0xfff5f1f6;}
    public static int textDim(){return 0xffaaa4af;}
    public static void card(float x,float y,float width,float height,float radius,float alpha,boolean glow){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{
            GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            if(glow)for(int i=3;i>0;i--)rounded(x-i,y-i,width+i*2,height+i*2,radius+i,Theme.stops()[0],alpha*.035f);
            rounded(x,y,width,height,radius,0x17131d,alpha*.8f);
        }finally{GL11.glPopAttrib();}
    }
    private static void rounded(float x,float y,float width,float height,float radius,int color,float alpha){
        float r=Math.max(0,Math.min(radius,Math.min(width,height)/2));
        GL11.glColor4f((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,alpha);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x+width/2,y+height/2);
        for(int corner=0;corner<4;corner++){
            float cx=corner==0||corner==3?x+width-r:x+r;
            float cy=corner<2?y+height-r:y+r;
            for(int step=0;step<=12;step++){
                double angle=Math.toRadians(corner*90+step*7.5);
                GL11.glVertex2f(cx+(float)Math.cos(angle)*r,cy+(float)Math.sin(angle)*r);
            }
        }
        GL11.glVertex2f(x+width,y+height-r);GL11.glEnd();
    }
    public static void afterItems(){
        GlStateManager.disableLighting();GlStateManager.disableRescaleNormal();
        GlStateManager.enableBlend();GlStateManager.color(1,1,1,1);
    }
}
