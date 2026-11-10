package dev.rice.client.render;

import dev.rice.client.SemanticEvent;
import dev.rice.client.blockin.SuppliedBlockInHost;
import dev.rice.client.mayu.MayuHost;
import dev.rice.inject.ReflectionHelper;
import dev.rice.module.ModuleManager;
import dev.rice.module.impl.AntiCheatModule;
import dev.rice.module.impl.ModOverlayModule;
import dev.rice.module.impl.NotificationsModule;
import dev.rice.module.impl.TargetHudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;

/** Editor samples use the production render paths, with no module enable/tick/packet calls. */
public final class HudPreviewRenderer {
    private HudPreviewRenderer() {}

    public static DrawPlan canvasPlan(ModuleManager modules, String id) {
        if (id.equals("mod_overlay")) return ((ModOverlayModule)modules.find(id)).editorPlan(Minecraft.getMinecraft().displayWidth);
        if (id.equals("notifications")) return ((NotificationsModule)modules.find(id)).editorPlan();
        throw new IllegalArgumentException(id);
    }

    /** Measure actual background geometry, including saved canvas translation, in scaled GUI coordinates. */
    public static int[] canvasBounds(DrawPlan plan, int sw, int sh) {
        Minecraft mc = Minecraft.getMinecraft();
        double sx = (double)sw / Math.max(1,mc.displayWidth), sy = (double)sh / Math.max(1,mc.displayHeight);
        float tx=0,ty=0; double left=Double.POSITIVE_INFINITY,top=left,right=Double.NEGATIVE_INFINITY,bottom=right;
        for(DrawPlan.Op op:plan.operations()) {
            float[] v=op.values;
            if(op.kind.equals("translate")){tx+=v[0];ty+=v[1];}
            else if(op.kind.equals("roundedRect")||op.kind.equals("roundedRectVarying")) {
                left=Math.min(left,(v[0]+tx)*sx);top=Math.min(top,(v[1]+ty)*sy);
                right=Math.max(right,(v[0]+tx+v[2])*sx);bottom=Math.max(bottom,(v[1]+ty+v[3])*sy);
            }
        }
        if(!Double.isFinite(left))return new int[]{0,0,1,1};
        int x=(int)Math.floor(left+1E-4),y=(int)Math.floor(top+1E-4);
        return new int[]{x,y,Math.max(1,(int)Math.ceil(right-1E-4)-x),Math.max(1,(int)Math.ceil(bottom-1E-4)-y)};
    }

    public static int[] bounds(ModuleManager modules,String id,int sw,int sh,FontRenderer font) {
        if(id.equals("mod_overlay")||id.equals("notifications"))return canvasBounds(canvasPlan(modules,id),sw,sh);
        if(id.equals("targethud"))return ((TargetHudModule)modules.find(id)).editorBounds(sw,sh,font);
        if(id.equals("anticheat")){
            AntiCheatModule m=(AntiCheatModule)modules.find(id);int[] size=m.backend.alerts.sampleSize(font),at=m.backend.core.placeBox(sw,sh,size[0],size[1]);
            return new int[]{at[0],at[1],size[0],size[1]};
        }
        int x,y,w,h;
        if(id.equals("blockin")){w=Math.max(100,font.getStringWidth("Block In 5/9"));h=5+font.FONT_HEIGHT;x=sw/2-w/2;y=sh/2+16;}
        else if(id.equals("bednuker")){float scale=bedScale();w=(int)Math.ceil(font.getStringWidth("50%")*scale);h=(int)Math.ceil(font.FONT_HEIGHT*scale);x=(int)Math.floor(sw/2F-w/2F);y=(int)(sh/5F*2F);}
        else throw new IllegalArgumentException(id);
        return new int[]{x+(int)Math.round(HudLayout.offset(id,true)*sw),y+(int)Math.round(HudLayout.offset(id,false)*sh),w,h};
    }

    public static void draw(ModuleManager modules,String id,FontRenderer font) {
        Minecraft mc=modules.context().minecraft();ScaledResolution sr=new ScaledResolution(mc);
        if(id.equals("mod_overlay")||id.equals("notifications")){
            try(RenderPassScope pass=RenderPassScope.hud(mc.displayWidth,mc.displayHeight)) {
                // Match the live ForgeUiSurface projection: canvas fills reset the modelview to identity (z=0),
                // so the editor must use a z-range that includes 0. RenderPassScope.hud's 1000..3000 near-plane
                // (with its -2000 translate) clips those fills once the fill path drops the translate; -3000..3000 does not.
                org.lwjgl.opengl.GL11.glMatrixMode(5889);
                org.lwjgl.opengl.GL11.glLoadIdentity();
                org.lwjgl.opengl.GL11.glOrtho(0.0,(double)mc.displayWidth,(double)mc.displayHeight,0.0,-3000.0,3000.0);
                org.lwjgl.opengl.GL11.glMatrixMode(5888);
                org.lwjgl.opengl.GL11.glLoadIdentity();
                RenderAccess.submit(new SemanticEvent(10),canvasPlan(modules,id));
            }
        }else if(id.equals("targethud"))((TargetHudModule)modules.find(id)).renderEditor();
        else if(id.equals("anticheat")){
            int sw=sr.getScaledWidth(),sh=sr.getScaledHeight();AntiCheatModule ac=(AntiCheatModule)modules.find(id);
            try(RenderPassScope pass=RenderPassScope.hud(mc.displayWidth,mc.displayHeight)) {
                // Styled flag-toast sample through the real alert painter, under the canvas z-range (see draw() above).
                org.lwjgl.opengl.GL11.glMatrixMode(5889);
                org.lwjgl.opengl.GL11.glLoadIdentity();
                org.lwjgl.opengl.GL11.glOrtho(0.0,(double)sw,(double)sh,0.0,-3000.0,3000.0);
                org.lwjgl.opengl.GL11.glMatrixMode(5888);
                org.lwjgl.opengl.GL11.glLoadIdentity();
                RenderAccess.submit(new SemanticEvent(10),ac.backend.alerts.editorPlan(font,sw,sh,ac.backend.core.hudX(),ac.backend.core.hudY()));
            }
        }
        else try(RenderPassScope pass=RenderPassScope.hud(sr.getScaledWidth(),sr.getScaledHeight())) {
            if(id.equals("blockin"))SuppliedBlockInHost.ProgressBar.preview(sr,"Block In 5/9",5F/9F);
            else if(id.equals("bednuker")){
                GlStateManager.translate(HudLayout.offset(id,true)*sr.getScaledWidth(),HudLayout.offset(id,false)*sr.getScaledHeight(),0);
                BedSample.INSTANCE.showProgress.setValue(Math.max(1,((dev.rice.setting.ChoiceSetting)modules.find(id).findSetting("show_progress")).index()));
                BedSample.INSTANCE.onRenderOverlay(new myau.RenderOverlayEvent(0));
            }
        }
    }

    private static float bedScale(){MayuHost.bedNuker();return ((myau.HUDModule)myau.MyauClient.moduleManager.modules.get(myau.HUDModule.class)).scale.getValue();}

    /** Unregistered render-only sample. Its own damage fields never touch the real BedNuker or world. */
    private static final class BedSample extends myau.BedNukerModule {
        static final BedSample INSTANCE=new BedSample();
        BedSample(){
            enabled=true;
            try {
                ReflectionHelper.findField(myau.BedNukerModule.class,"targetBlock").set(this,BlockPos.ORIGIN);
                ReflectionHelper.findField(myau.BedNukerModule.class,"accumulatedBreakDamage").setFloat(this,.5F);
                ((java.util.concurrent.ScheduledExecutorService)ReflectionHelper.findField(myau.BedNukerModule.class,"bedScanExecutor").get(this)).shutdown();
            }catch(IllegalAccessException error){throw new IllegalStateException(error);}
        }
    }
}
