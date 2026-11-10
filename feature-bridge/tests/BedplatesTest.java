import dev.rice.bedplates.*;
import static dev.rice.bedplates.BedplatesCore.*;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public final class BedplatesTest {
    static int checks;
    static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    static Map<Pos,Integer> shell(Pos a,Pos b,int material){
        Map<Pos,Integer> result=new HashMap<>();
        for(Pos p:new Pos[]{a.add(1,0,0),a.add(0,0,-1),a.add(0,0,1),a.add(0,1,0),b.add(-1,0,0),b.add(0,0,-1),b.add(0,0,1),b.add(0,1,0)})result.put(p,material);
        return result;
    }
    static final Map<BlockPos,IBlockState> blocks=new HashMap<>();static Chunk chunk;static int chunkReads;
    public static class World extends FeatureFixture.World {
        public World(){super();}
        @Override public boolean isBlockLoaded(BlockPos p,boolean empty){return p.getX()>>4==0&&p.getZ()>>4==0;}
        @Override public IBlockState getBlockState(BlockPos p){return blocks.getOrDefault(p,Blocks.air.getDefaultState());}
        @Override public Chunk getChunkFromChunkCoords(int x,int z){chunkReads++;if(x!=0||z!=0)throw new AssertionError("must not request unloaded chunks");return chunk;}
    }
    static int drawnIcons,drawnCounts;static boolean failRender;
    public static class Renderer extends net.minecraft.client.renderer.entity.RenderItem {
        public Renderer(){super(null,null);}
        @Override public void renderItemAndEffectIntoGUI(net.minecraft.item.ItemStack item,int x,int y){drawnIcons++;if(failRender)throw new IllegalStateException("fixture icon failure");}
    }
    public static class Font extends net.minecraft.client.gui.FontRenderer {
        public Font(){super(null,null,null,false);}
        @Override public int getStringWidth(String text){return text.length()*6;}
        @Override public int drawStringWithShadow(String text,float x,float y,int color){drawnCounts++;return text.length()*6;}
    }
    public static class Locale extends net.minecraft.client.resources.LanguageManager {
        public Locale(){super(null,"en_US");}
        @Override public boolean isCurrentLocaleUnicode(){return false;}
    }
    static Object value(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(null);}
    public static void main(String[] args)throws Exception{
        Pos a=new Pos(0,64,0),b=new Pos(-1,64,0);int wool=35<<4|14;
        Map<Pos,Integer> world=shell(a,b,wool);Reader reader=p->world.getOrDefault(p,0);
        check(layers(a,b,reader).get(wool)==8,"Full first layer must show eight defense blocks");
        world.put(a.add(1,0,0),0);check(layers(a,b,reader).get(wool)==7,"Minor air gap accepted");
        world.put(a.add(0,1,0),0);check(layers(a,b,reader).isEmpty(),"More than 20 percent air rejects incomplete layer");
        world.clear();world.putAll(shell(a,b,wool));world.put(a.add(1,0,0),20<<4);
        check(!layers(a,b,reader).containsKey(20<<4),"Sparse material below 20 percent omitted");
        world.put(a.add(0,1,0),20<<4);check(layers(a,b,reader).get(20<<4)==2,"Material at 25 percent retained with count");
        final int[] samples={0};layers(a,b,p->{samples[0]++;return 0;});check(samples[0]<100,"Two air layers stop the scan early");
        int[][] directions={{0,1},{-1,0},{0,-1},{1,0}};
        for(int dir=0;dir<4;dir++){
            Pos foot=new Pos(-17,64,-33),head=foot.add(directions[dir][0],0,directions[dir][1]);Map<Pos,Integer> beds=new HashMap<>();beds.put(foot,26<<4|dir);beds.put(head,26<<4|dir|8);
            Pos[] first=pair(foot,p->beds.getOrDefault(p,0)),second=pair(head,p->beds.getOrDefault(p,0));
            check(first[0].equals(second[0])&&first[1].equals(second[1]),"Both halves deduplicate in every direction and negative coordinates");
        }
        check(pair(a,p->0)==null,"Non-bed rejected");check(pair(a,p->p.equals(a)?26<<4:0)[0].equals(a),"Missing half is safe");
        check(delay(10)==1000&&delay(30)==2000&&delay(75)==3000&&delay(150)==4000,"Distance refresh delays match supplied script");
        Display display=new Display();check(display.update(0,false,false),"Static always visible");check(display.update(1,false,false),"Toggle starts visible");check(!display.update(1,true,false),"Toggle key hides");check(!display.update(1,true,false),"Holding toggle must not repeat");display.update(1,false,false);check(display.update(1,true,false),"Next edge shows");check(!display.update(2,false,false),"Hold hidden without key");check(display.update(2,true,false),"Hold visible while pressed");check(!display.update(2,true,true),"Hold hidden in menu");display.reset();check(display.update(0,false,false),"Display resets for world change");
        double[] frame=new double[46];frame[11]=1920;frame[12]=1080;for(int i=0;i<4;i++){frame[14+i*5]=1;frame[30+i*5]=1;}
        check(Arrays.equals(project(0,0,0,frame),new double[]{960,540}),"Centered world projection");check(project(0,0,2,frame)==null,"Beyond clip plane hidden");check(project(2,0,0,frame)==null,"Offscreen hidden");frame[8]=-20;frame[9]=64;frame[10]=-30;check(Arrays.equals(project(-20,64,-30,frame),new double[]{960,540}),"Camera subtraction handles negative world coordinates");frame[45]=-1;check(project(-20,64,-30,frame)==null,"Behind camera hidden");
        FeatureFixture.create();World gameWorld=FeatureFixture.blank(World.class);FeatureFixture.minecraft.theWorld=gameWorld;FeatureFixture.self.worldObj=gameWorld;FeatureFixture.self.setPosition(8,65,8);
        chunk=new Chunk(gameWorld,0,0);ExtendedBlockStorage section=new ExtendedBlockStorage(64,true);chunk.getBlockStorageArray()[4]=section;
        BlockPos foot=new BlockPos(8,64,8),head=new BlockPos(7,64,8);blocks.put(foot,Blocks.bed.getStateFromMeta(1));blocks.put(head,Blocks.bed.getStateFromMeta(9));
        for(Map.Entry<BlockPos,IBlockState> e:blocks.entrySet())section.set(e.getKey().getX(),0,e.getKey().getZ(),e.getValue());
        for(Map.Entry<Pos,Integer> e:shell(new Pos(8,64,8),new Pos(7,64,8),wool).entrySet()){Pos p=e.getKey();blocks.put(new BlockPos(p.x,p.y,p.z),Blocks.wool.getStateFromMeta(14));}
        double[] config={0,1,1,.8,150,1,0,0};Bedplates.event(FeatureFixture.minecraft,18,config);
        Map<?,?> found=(Map<?,?>)value(Bedplates.class,"beds");check(found.size()==1,"Runtime discovers bed without player swing and deduplicates halves");check(chunkReads<=8,"Discovery work bounded per tick");
        Object bed=found.values().iterator().next();Field layers=bed.getClass().getDeclaredField("layers");layers.setAccessible(true);check(((Map<?,?>)layers.get(bed)).get(wool).equals(8),"Runtime counts wool color in real game block states");
        FeatureFixture.minecraft.displayWidth=1920;FeatureFixture.minecraft.displayHeight=1080;FeatureFixture.minecraft.gameSettings.guiScale=1;
        FeatureFixture.put(FeatureFixture.minecraft,"mcLanguageManager",FeatureFixture.blank(Locale.class));
        FeatureFixture.put(FeatureFixture.minecraft,"renderItem",FeatureFixture.blank(Renderer.class));
        Font font=FeatureFixture.blank(Font.class);font.FONT_HEIGHT=9;FeatureFixture.minecraft.fontRendererObj=font;
        net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit=33984;
        double[] render=new double[46];System.arraycopy(config,0,render,0,8);render[8]=8;render[9]=65;render[10]=8;render[11]=1920;render[12]=1080;
        for(int index=0;index<4;index++){render[14+index*5]=1;render[30+index*5]=1;}
        boolean rejectedSmallQuery=false;
        try{org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_CURRENT_COLOR,org.lwjgl.BufferUtils.createFloatBuffer(4));}catch(IllegalArgumentException expected){rejectedSmallQuery=true;}
        check(rejectedSmallQuery,"Test GL binding must enforce real LWJGL's 16-float minimum");
        FeatureFixture.resetHudGl();Bedplates.event(FeatureFixture.minecraft,7,render);
        check(FeatureFixture.lastColorQueryCapacity>=16,"Production render-state query must meet LWJGL buffer capacity");
        check(drawnIcons==1&&drawnCounts==1,"Render event must draw the material icon and block count");
        check(FeatureFixture.hudGlBalanced(),"Render must balance matrices, attributes and drawing primitives");
        render[2]=0;Bedplates.event(FeatureFixture.minecraft,7,render);check(drawnIcons==2&&drawnCounts==1,"Count toggle hides only the count");
        failRender=true;boolean thrown=false;FeatureFixture.resetHudGl();
        try{Bedplates.event(FeatureFixture.minecraft,7,render);}catch(IllegalStateException expected){thrown=true;}finally{failRender=false;}
        check(thrown&&FeatureFixture.hudGlBalanced(),"Item-render failure must still restore GL stacks");
        blocks.remove(foot);blocks.remove(head);Bedplates.event(FeatureFixture.minecraft,18,config);check(((Map<?,?>)value(Bedplates.class,"beds")).isEmpty(),"Destroyed bed removed immediately");
        Bedplates.event(FeatureFixture.minecraft,26,config);check(value(Bedplates.class,"lastWorld")==null,"World unload clears references");
        Bedplates.event(FeatureFixture.minecraft,5,config);check(((Map<?,?>)value(Bedplates.class,"icons")).isEmpty(),"Toggle releases icon cache");
        check(Bedplates.clampScale(5f)==3f&&Bedplates.clampScale(0.1f)==0.25f&&Bedplates.clampScale(1f)==1f,"Customize scale clamps to the editor range");
        check(Bedplates.clampLift(-5f)==0f&&Bedplates.clampLift(500f)==300f&&Bedplates.clampLift(21f)==21f,"Customize lift clamps to the editor range");
        System.out.println("Bedplates: "+checks+" defense, pairing, display, projection, loaded-chunk, customize and lifecycle checks passed");
    }
}
