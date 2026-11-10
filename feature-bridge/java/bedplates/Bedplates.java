package dev.rice.bedplates;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static dev.rice.bedplates.BedplatesCore.*;

public final class Bedplates {
    private static Object lastWorld;
    private static final Map<Pos,Bed> beds=new LinkedHashMap<>();
    private static final Map<Integer,ItemStack> icons=new HashMap<>();
    private static final Display display=new Display();
    private static int scanX=Integer.MIN_VALUE,scanZ,scanRadius,cursor;
    private static List<int[]> chunks=new ArrayList<>();
    // Rise-style customize editor state. Scale and screen-space lift are edited in
    // BedplatesCustomize and persisted beside the game profile so they survive restarts.
    static float editScale=0.8f,liftPx=0;
    private static boolean loaded,customizeDown;
    public static float clampScale(float v){return v<.25f?.25f:v>3f?3f:v;}
    public static float clampLift(float v){return v<0?0:v>300?300:v;}
    private static void load(Minecraft mc){
        if(loaded)return;loaded=true;
        if(mc.mcDataDir==null)return;
        try{java.io.File file=new java.io.File(mc.mcDataDir,"config/rice-bedplates.properties");
            if(file.isFile()){java.util.Properties p=new java.util.Properties();
                try(java.io.FileInputStream in=new java.io.FileInputStream(file)){p.load(in);}
                editScale=clampScale(Float.parseFloat(p.getProperty("scale","0.8")));
                liftPx=clampLift(Float.parseFloat(p.getProperty("offset","0")));}
        }catch(Exception ignored){}
    }
    static void save(Minecraft mc){
        try{java.io.File dir=new java.io.File(mc.mcDataDir,"config");dir.mkdirs();
            java.util.Properties p=new java.util.Properties();
            p.setProperty("scale",String.valueOf(editScale));p.setProperty("offset",String.valueOf(liftPx));
            try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,"rice-bedplates.properties"))){p.store(out,"Rice Bedplates customize");}
        }catch(Exception ignored){}
    }
    private static final class Bed {
        final Pos first,second;Map<Integer,Integer> layers=Collections.emptyMap();long checked;
        Bed(Pos a,Pos b){first=a;second=b;}
    }
    public static void reset(){beds.clear();icons.clear();chunks.clear();cursor=0;scanX=Integer.MIN_VALUE;lastWorld=null;display.reset();}
    private static BlockPos pos(Pos p){return new BlockPos(p.x,p.y,p.z);}
    private static int state(Minecraft mc,Pos p){
        if(!mc.theWorld.isBlockLoaded(pos(p),false))return 0;
        IBlockState s=mc.theWorld.getBlockState(pos(p));Block b=s.getBlock();int id=Block.getIdFromBlock(b);
        if(id==0)return 0;
        String name=String.valueOf(Block.blockRegistry.getNameForObject(b));
        if(name.startsWith("minecraft:"))name=name.substring(10);
        return (id<<4)|(BedplatesVariants.INVALID.contains(name)?0:b.getMetaFromState(s)&15);
    }
    private static int rawState(Minecraft mc,Pos p){
        if(!mc.theWorld.isBlockLoaded(pos(p),false))return 0;
        IBlockState s=mc.theWorld.getBlockState(pos(p));return Block.getIdFromBlock(s.getBlock())<<4|s.getBlock().getMetaFromState(s)&15;
    }
    private static double distance(Minecraft mc,Pos p){double x=mc.thePlayer.posX-p.x,y=mc.thePlayer.posY-p.y,z=mc.thePlayer.posZ-p.z;return Math.sqrt(x*x+y*y+z*z);}
    private static void scan(Minecraft mc,int distance){
        int cx=((int)Math.floor(mc.thePlayer.posX))>>4,cz=((int)Math.floor(mc.thePlayer.posZ))>>4,r=(distance+15)/16;
        if(cx!=scanX||cz!=scanZ||r!=scanRadius){
            scanX=cx;scanZ=cz;scanRadius=r;cursor=0;chunks.clear();
            chunks.add(new int[]{0,0});
            for(int ring=1;ring<=r;ring++)for(int x=-ring;x<=ring;x++)for(int z=-ring;z<=ring;z++)
                if(Math.max(Math.abs(x),Math.abs(z))==ring)chunks.add(new int[]{x,z});
        }
        for(int work=0;work<8&&!chunks.isEmpty();work++){
            int[] d=chunks.get(cursor++%chunks.size());
            int x=cx+d[0],z=cz+d[1];
            if(!mc.theWorld.isBlockLoaded(new BlockPos(x*16,64,z*16),false))continue;
            Chunk chunk=mc.theWorld.getChunkFromChunkCoords(x,z);
            for(ExtendedBlockStorage section:chunk.getBlockStorageArray()){
                if(section==null||section.isEmpty())continue;
                char[] data=section.getData();
                for(int i=0;i<data.length;i++)if((data[i]>>>4)==26){
                    Pos p=new Pos(x*16+(i&15),section.getYLocation()+(i>>8),z*16+(i>>4&15));
                    if(distance(mc,p)>distance)continue;
                    Pos[] pair=pair(p,q->rawState(mc,q));if(pair==null||beds.containsKey(pair[0])||beds.size()>=512)continue;
                    beds.put(pair[0],new Bed(pair[0],pair[1]));
                }
            }
        }
    }
    private static void tick(Minecraft mc,double[] f){
        if(mc.theWorld==null||mc.thePlayer==null){reset();return;}
        if(lastWorld!=mc.theWorld){reset();lastWorld=mc.theWorld;}
        int range=(int)f[4];scan(mc,range);long now=System.currentTimeMillis();
        Iterator<Bed> iterator=beds.values().iterator();
        while(iterator.hasNext()){
            Bed b=iterator.next();double dist=distance(mc,b.first);
            if(dist>range+16||(rawState(mc,b.first)>>>4)!=26){iterator.remove();continue;}
            if(b.checked==0||now-b.checked>=delay(dist)){
                b.layers=layers(b.first,b.second,p->state(mc,p));b.checked=now;
            }
        }
    }
    private static boolean key(int index){
        if(index<=0)return false;
        if(index<=36){String names="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";int code=Keyboard.getKeyIndex(String.valueOf(names.charAt(index-1)));return Keyboard.isCreated()&&code>0&&Keyboard.isKeyDown(code);}
        if(index>=41)return Mouse.isCreated()&&Mouse.isButtonDown(index-38);
        int[] codes={Keyboard.KEY_SPACE,Keyboard.KEY_LSHIFT,Keyboard.KEY_LCONTROL,Keyboard.KEY_LMENU};
        return Keyboard.isCreated()&&index<=40&&Keyboard.isKeyDown(codes[index-37]);
    }
    static ItemStack icon(int state){
        ItemStack cached=icons.get(state);if(cached!=null)return cached;
        int id=state>>>4;Item item=id==8||id==9?Items.water_bucket:id==10||id==11?Items.lava_bucket:id==51?Items.flint_and_steel:Item.getItemFromBlock(Block.getBlockById(id));
        ItemStack result=new ItemStack(item==null?Item.getItemFromBlock(net.minecraft.init.Blocks.barrier):item,1,state&15);icons.put(state,result);return result;
    }
    static void rounded(float x,float y,float w,float h,float r,int rgb){
        GlStateManager.disableTexture2D();GlStateManager.color((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,1);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);GL11.glVertex2f(x+w/2,y+h/2);
        for(int corner=0;corner<=4;corner++){
            int c=corner%4;float cx=c==0||c==3?x+r:x+w-r,cy=c<2?y+r:y+h-r;
            for(int step=0;step<=8;step++){double a=Math.toRadians(180+c*90+step*90/8.0);GL11.glVertex2f(cx+(float)Math.cos(a)*r,cy+(float)Math.sin(a)*r);}
        }
        GL11.glEnd();GlStateManager.enableTexture2D();GlStateManager.color(1,1,1,1);
    }
    private static void render(Minecraft mc,double[] f){
        if(mc.theWorld!=lastWorld||mc.thePlayer==null||beds.isEmpty())return;
        if(mc.currentScreen instanceof BedplatesCustomize)return;
        if(!display.update((int)f[6],key((int)f[7]),mc.currentScreen!=null))return;
        List<Bed> ordered=new ArrayList<>(beds.values());ordered.sort((a,b)->Double.compare(distance(mc,b.first),distance(mc,a.first)));
        RenderItem renderer=mc.getRenderItem();float savedZ=renderer.zLevel;
        State saved=new State();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try{
            GL11.glLoadIdentity();GlStateManager.disableDepth();GlStateManager.depthMask(false);GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);GlStateManager.disableAlpha();GlStateManager.disableLighting();
            renderer.zLevel=0;
            for(Bed b:ordered){
                double dist=distance(mc,b.first);if(dist>f[4]||b.layers.isEmpty())continue;
                double[] screen=project((b.first.x+b.second.x)/2.0+.5,b.first.y+f[5],(b.first.z+b.second.z)/2.0+.5,f);if(screen==null)continue;
                int scaleFactor=new ScaledResolution(mc).getScaleFactor();
                screen[1]-=liftPx*scaleFactor;
                float scale=(float)(editScale*(f[1]!=0?Math.max(0,1-dist/f[4]):1))*scaleFactor;if(scale<=.01f)continue;
                GL11.glPushMatrix();
                try{
                    GlStateManager.translate(screen[0],screen[1],0);GlStateManager.scale(scale,scale,1);
                    float width=b.layers.size()*18,x=-width/2,y=f[0]!=0?0:-18;
                    rounded(x-3,y-3,width+6,24,3,0x25252b);
                    int i=0;
                    for(Map.Entry<Integer,Integer> layer:b.layers.entrySet()){
                        float itemX=x+i++*18;rounded(itemX+.5f,y+.5f,17,17,2,0x34363b);
                        RenderHelper.enableGUIStandardItemLighting();GlStateManager.enableDepth();GlStateManager.depthMask(true);
                        renderer.renderItemAndEffectIntoGUI(icon(layer.getKey()),(int)itemX+1,(int)y+1);
                        RenderHelper.disableStandardItemLighting();GlStateManager.disableDepth();GlStateManager.depthMask(false);GlStateManager.disableAlpha();GlStateManager.enableBlend();
                        if(f[2]!=0&&layer.getValue()>1){
                            String text=String.valueOf(layer.getValue());GL11.glPushMatrix();
                            try{GlStateManager.translate(itemX+17-mc.fontRendererObj.getStringWidth(text)*.6,y+17-mc.fontRendererObj.FONT_HEIGHT*.6,0);GlStateManager.scale(.6,.6,1);mc.fontRendererObj.drawStringWithShadow(text,0,0,0xffffff);}finally{GL11.glPopMatrix();}
                        }
                    }
                }finally{GL11.glPopMatrix();}
            }
        }finally{renderer.zLevel=savedZ;GL11.glPopMatrix();saved.restore();GL11.glPopAttrib();GL11.glMatrixMode(saved.matrix);}
    }
    /** Restore both actual GL state and Minecraft's cached state after item rendering. */
    private static final class State {
        final int matrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        final boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),blend=GL11.glIsEnabled(GL11.GL_BLEND),alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST),lighting=GL11.glIsEnabled(GL11.GL_LIGHTING),tex=GL11.glIsEnabled(GL11.GL_TEXTURE_2D),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),colorMaterial=GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL),normalize=GL11.glIsEnabled(GL11.GL_NORMALIZE),rescale=GL11.glIsEnabled(GL12_RESCALE_NORMAL);
        static final int GL12_RESCALE_NORMAL=0x803a;
        final boolean light0=GL11.glIsEnabled(GL11.GL_LIGHT0),light1=GL11.glIsEnabled(GL11.GL_LIGHT1);
        final int src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        // LWJGL 2 validates room for 16 floats even for the four-component current color.
        final FloatBuffer color=BufferUtils.createFloatBuffer(16);
        State(){GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);}
        void restore(){
            if(depth)GlStateManager.enableDepth();else GlStateManager.disableDepth();GlStateManager.depthMask(mask);
            if(blend)GlStateManager.enableBlend();else GlStateManager.disableBlend();GlStateManager.tryBlendFuncSeparate(src,dst,srcA,dstA);
            if(alpha)GlStateManager.enableAlpha();else GlStateManager.disableAlpha();if(lighting)GlStateManager.enableLighting();else GlStateManager.disableLighting();
            if(light0)GlStateManager.enableLight(0);else GlStateManager.disableLight(0);if(light1)GlStateManager.enableLight(1);else GlStateManager.disableLight(1);
            if(colorMaterial)GlStateManager.enableColorMaterial();else GlStateManager.disableColorMaterial();if(normalize)GlStateManager.enableNormalize();else GlStateManager.disableNormalize();if(rescale)GlStateManager.enableRescaleNormal();else GlStateManager.disableRescaleNormal();
            if(cull)GlStateManager.enableCull();else GlStateManager.disableCull();GlStateManager.setActiveTexture(active);GlStateManager.bindTexture(texture);if(tex)GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D();GlStateManager.color(color.get(0),color.get(1),color.get(2),color.get(3));
        }
    }
    public static void event(Object object,int event,double[] frame){
        if(event==5||event==26){reset();return;}
        Minecraft mc=(Minecraft)object;if(mc==null)return;
        load(mc);
        if(event==18){
            tick(mc,frame);
            int customizeKey=frame.length>8?(int)frame[8]:0;boolean down=customizeKey>0&&key(customizeKey);
            if(down&&!customizeDown&&mc.currentScreen==null)mc.displayGuiScreen(new BedplatesCustomize(null));
            customizeDown=down;
        }else if(event==7&&frame.length>=46)render(mc,frame);
    }
    /** Draws one Bedplates panel centered horizontally at centerX with its top at topY, at the given
     *  GUI scale. Shared by the live overlay and the customize editor so both match exactly. */
    static void panel(Minecraft mc,Map<Integer,Integer> layers,float centerX,float topY,float scale,boolean counters,boolean alignTop){
        RenderItem renderer=mc.getRenderItem();float savedZ=renderer.zLevel;
        GlStateManager.pushMatrix();
        try{
            GlStateManager.translate(centerX,topY,0);GlStateManager.scale(scale,scale,1);
            float width=layers.size()*18,x=-width/2,y=alignTop?0:0;renderer.zLevel=0;
            rounded(x-3,y-3,width+6,24,3,0x25252b);
            int i=0;
            for(Map.Entry<Integer,Integer> layer:layers.entrySet()){
                float itemX=x+i++*18;rounded(itemX+.5f,y+.5f,17,17,2,0x34363b);
                RenderHelper.enableGUIStandardItemLighting();GlStateManager.enableDepth();GlStateManager.depthMask(true);
                renderer.renderItemAndEffectIntoGUI(icon(layer.getKey()),(int)itemX+1,(int)y+1);
                RenderHelper.disableStandardItemLighting();GlStateManager.disableDepth();GlStateManager.depthMask(false);
                if(counters&&layer.getValue()>1){
                    String text=String.valueOf(layer.getValue());GlStateManager.pushMatrix();
                    try{GlStateManager.translate(itemX+17-mc.fontRendererObj.getStringWidth(text)*.6,y+17-mc.fontRendererObj.FONT_HEIGHT*.6,0);GlStateManager.scale(.6,.6,1);mc.fontRendererObj.drawStringWithShadow(text,0,0,0xffffff);}finally{GlStateManager.popMatrix();}
                }
            }
        }finally{renderer.zLevel=savedZ;GlStateManager.popMatrix();}
    }
    private Bedplates(){}
}
