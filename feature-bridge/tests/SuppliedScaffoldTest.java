import com.snowy.config.*;
import com.snowy.module.player.scaffold.*;
import com.snowy.rotation.*;
import com.snowy.core.event.*;
import com.snowy.util.Claims;
import dev.rice.setting.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import java.lang.reflect.*;
import java.util.*;

/** Real game types; deterministic world/controller/network boundaries. */
public final class SuppliedScaffoldTest {
    static int assertions,placements;
    static final Map<BlockPos,net.minecraft.block.state.IBlockState> blocks=new HashMap<>();
    static void check(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
    static void near(double a,double b,String message){check(Math.abs(a-b)<1e-5,message+": "+a+" != "+b);}
    public static class World extends FeatureFixture.World{
        public World(){super();}
        @Override public boolean isBlockLoaded(BlockPos p){return true;}
        @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return blocks.getOrDefault(p,net.minecraft.init.Blocks.air.getDefaultState());}
        @Override public List<AxisAlignedBB> getCollidingBoundingBoxes(net.minecraft.entity.Entity entity,AxisAlignedBB box){
            List<AxisAlignedBB> result=new ArrayList<>();
            for(BlockPos p:blocks.keySet()){AxisAlignedBB b=new AxisAlignedBB(p,p.add(1,1,1));if(b.intersectsWith(box))result.add(b);}
            return result;
        }
        @Override public MovingObjectPosition rayTraceBlocks(Vec3 from,Vec3 to,boolean liquid,boolean ignore,boolean last){
            MovingObjectPosition best=null;double distance=Double.POSITIVE_INFINITY;
            for(BlockPos p:blocks.keySet()){
                MovingObjectPosition hit=new AxisAlignedBB(p,p.add(1,1,1)).calculateIntercept(from,to);
                if(hit!=null&&from.squareDistanceTo(hit.hitVec)<distance){distance=from.squareDistanceTo(hit.hitVec);best=new MovingObjectPosition(hit.hitVec,hit.sideHit,p);}
            }return best;
        }
    }
    public static class Controller extends PlayerControllerMP{
        public Controller(){super(null,null);}
        @Override public float getBlockReachDistance(){return 4.5f;}
        @Override public net.minecraft.world.WorldSettings.GameType getCurrentGameType(){return net.minecraft.world.WorldSettings.GameType.SURVIVAL;}
        @Override public boolean onPlayerRightClick(EntityPlayerSP p,WorldClient world,ItemStack stack,BlockPos against,EnumFacing face,Vec3 hit){
            check(blocks.containsKey(against),"placement needs a real support");
            check(!blocks.containsKey(against.offset(face)),"placement destination must be open");
            check(hit!=null,"placement uses a ray hit");
            blocks.put(against.offset(face),net.minecraft.init.Blocks.stone.getDefaultState());placements++;stack.stackSize--;return true;
        }
    }
    public static void nativePrepare()throws Exception{
        if(FeatureFixture.minecraft==null)FeatureFixture.create();
        FeatureFixture.put(FeatureFixture.self,"mc",FeatureFixture.minecraft);
        FeatureFixture.put(FeatureFixture.minecraft,"renderViewEntity",FeatureFixture.self);
        for(int k:new int[]{17,31,30,32,57,42})KeyBinding.setKeyBindState(k,false);
        FeatureFixture.minecraft.gameSettings.keyBindJump=new KeyBinding("source.jump",57,"test");
        FeatureFixture.minecraft.gameSettings.keyBindSneak=new KeyBinding("source.sneak",42,"test");
        FeatureFixture.minecraft.gameSettings.keyBindSprint=new KeyBinding("source.sprint",29,"test");
        World world=FeatureFixture.blank(World.class);
        FeatureFixture.minecraft.theWorld=world;FeatureFixture.self.worldObj=world;
        FeatureFixture.self.width=.6f;FeatureFixture.self.height=1.8f;FeatureFixture.self.setPosition(1.2,65,.5);
        FeatureFixture.self.onGround=true;FeatureFixture.self.capabilities.isFlying=false;
        FeatureFixture.self.motionX=FeatureFixture.self.motionY=FeatureFixture.self.motionZ=0;
        FeatureFixture.self.rotationYaw=90;FeatureFixture.self.rotationPitch=84;
        FeatureFixture.self.movementInput=new MovementInput();
        Arrays.fill(FeatureFixture.self.inventory.mainInventory,null);
        FeatureFixture.self.inventory.currentItem=0;
        FeatureFixture.self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);
        FeatureFixture.self.inventory.mainInventory[8]=new ItemStack(net.minecraft.init.Blocks.stone,12);
        FeatureFixture.self.inventory.mainInventory[2]=new ItemStack(net.minecraft.init.Blocks.stone,64);
        Controller controller=FeatureFixture.blank(Controller.class);FeatureFixture.minecraft.playerController=controller;
        FeatureFixture.put(controller,"mc",FeatureFixture.minecraft);FeatureFixture.put(controller,"netClientHandler",FeatureFixture.self.sendQueue);
        FeatureFixture.put(controller,"currentGameType",net.minecraft.world.WorldSettings.GameType.SURVIVAL);
        blocks.clear();blocks.put(new BlockPos(0,64,0),net.minecraft.init.Blocks.stone.getDefaultState());placements=0;
    }
    static void seed(Object source)throws Exception{Field f=source.getClass().getDeclaredField("random");f.setAccessible(true);((Random)f.get(source)).setSeed(1);}
    public static void nativeSeed(Class<?> bridge)throws Exception{
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);
        Object module=((Object[])modules.get(null))[4];Field core=module.getClass().getDeclaredField("core");core.setAccessible(true);seed(core.get(module));
    }
    public static Object nativePosition(){return new BlockPos(1,0,0);}
    public static Object nativeInventory(){return FeatureFixture.self.inventory;}
    public static int nativeSlot(){return FeatureFixture.self.inventory.currentItem;}
    public static int nativePlacements(){return placements;}
    public static void nativeSetView(float yaw,float pitch){FeatureFixture.self.rotationYaw=yaw;FeatureFixture.self.rotationPitch=pitch;}
    public static void nativeForceLook()throws Exception{FeatureFixture.put(FeatureFixture.self,"lastReportedYaw",777f);}
    public static void nativePacketMatches(float yaw,float pitch){
        check(FeatureFixture.lastSent instanceof net.minecraft.network.play.client.C03PacketPlayer,"walking sends a player packet");
        net.minecraft.network.play.client.C03PacketPlayer packet=(net.minecraft.network.play.client.C03PacketPlayer)FeatureFixture.lastSent;
        near(packet.getYaw(),yaw,"packet placement yaw");near(packet.getPitch(),pitch,"packet placement pitch");
    }
    public static void nativeVisualMatches(float yaw,float pitch){near(FeatureFixture.self.rotationYawHead,yaw,"model yaw");near(FeatureFixture.self.rotationPitch,pitch,"model pitch");}
    public static void checkNativeMovement(Class<?> bridge)throws Exception{
        Class<?> type=bridge.getClassLoader().loadClass("com.snowy.rotation.RotationManager");
        Object rotation=type.getMethod("get").invoke(null);
        Method aim=type.getMethod("aim",Object.class,int.class,float.class,float.class,boolean.class);
        Method input=bridge.getMethod("input",Object.class),move=bridge.getMethod("mayuMove",Object.class,float.class,float.class,float.class);
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object module=((Object[])modules.get(null))[4];
        Field core=module.getClass().getDeclaredField("core");core.setAccessible(true);Object owner=core.get(module);
        float[] values=new float[13];values[2]=17;values[3]=23;
        bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class).invoke(null,4,13,true,null,values);
        check(values[12]==0&&values[4]==0,"no persistent host key/yaw remapper");
        int cases=0;
        for(float camera:new float[]{-179,-90,0,90,179})
            for(float offset:new float[]{-180,-135,-90,-67.6f,-45,-22.6f,0,22.6f,45,67.6f,90,135,180})
                for(int[] keys:new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}})
                    for(boolean sneak:new boolean[]{false,true}){
                        int f=keys[0],s=keys[1];
                        KeyBinding.setKeyBindState(17,f>0);KeyBinding.setKeyBindState(31,f<0);
                        KeyBinding.setKeyBindState(30,s>0);KeyBinding.setKeyBindState(32,s<0);
                        FeatureFixture.self.rotationYaw=camera;
                        FeatureFixture.self.movementInput.moveForward=f*(sneak?.3f:1);
                        FeatureFixture.self.movementInput.moveStrafe=s*(sneak?.3f:1);
                        FeatureFixture.self.movementInput.sneak=sneak;
                        check((Boolean)aim.invoke(rotation,owner,80,camera+offset,84f,true),"source owns test rotation");
                        input.invoke(null,FeatureFixture.self.movementInput);
                        float cf=FeatureFixture.self.movementInput.moveForward,cs=FeatureFixture.self.movementInput.moveStrafe;
                        float unit=sneak?.3f:1;
                        check((cf==0||Math.abs(cf)==unit)&&(cs==0||Math.abs(cs)==unit),"only supplied keyboard input combinations");
                        FeatureFixture.self.motionX=FeatureFixture.self.motionZ=0;
                        move.invoke(null,FeatureFixture.self,cs,cf,.1f);
                        double yaw=Math.toRadians(camera),ex=s*Math.cos(yaw)-f*Math.sin(yaw),ez=f*Math.cos(yaw)+s*Math.sin(yaw);
                        double ax=FeatureFixture.self.motionX,az=FeatureFixture.self.motionZ;
                        check((ex*ax+ez*az)/(Math.hypot(ex,ez)*Math.hypot(ax,az))>=Math.cos(Math.toRadians(22.6)),"source correction follows intended direction");
                        near(FeatureFixture.self.rotationYaw,camera,"free camera preserved during movement");
                        check(FeatureFixture.minecraft.gameSettings.keyBindBack.isKeyDown()==(f<0),"physical S untouched");
                        cases++;
                    }
        for(int key:new int[]{17,31,30,32})KeyBinding.setKeyBindState(key,false);
        FeatureFixture.self.rotationYaw=90;FeatureFixture.self.movementInput.sneak=false;
        aim.invoke(rotation,owner,80,90f,84f,true);
        System.out.println("Supplied Scaffold movement: "+cases+" discrete direction/sneak cases passed");
    }
    public static void main(String[] args)throws Exception{
        nativePrepare();
        Scaffold core=new Scaffold(new Configuration());seed(core);
        check(core.properties().size()==12,"12 source properties");
        check(core.suffix().equals("Backwards"),"Backwards default");
        RotationManager manager=RotationManager.get();manager.startTick();manager.endTick();
        manager.resetSent(90,84);core.enabled(true);
        for(int tick=0;tick<8;tick++){
            manager.startTick();core.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START,FeatureFixture.self));
            if(tick<2)check(placements==0,"source starts with three ticks of turning");
            if(manager.isActive())manager.recordSent(manager.getYaw(),manager.getPitch(),true);
            manager.endTick();
        }
        check(placements>=1,"source places after a look has been sent");
        check(FeatureFixture.self.inventory.currentItem==2,"matching Blocks selects the largest stack");
        check(com.snowy.util.GameAccess.currentPlayerItem(FeatureFixture.minecraft.playerController)==2,"supplied reflection syncs held item");
        FeatureFixture.self.movementInput.sneak=false;core.preMove(FeatureFixture.self);
        check(FeatureFixture.self.movementInput.sneak,"source Safe Walk requests a scoped crouch");
        core.postMove(FeatureFixture.self);check(!FeatureFixture.self.movementInput.sneak,"source Safe Walk clears its temporary crouch");
        core.enabled(false);check(FeatureFixture.self.inventory.currentItem==0,"source restores original slot");
        Claims.unregister(core);
        check(RotationManager.correctedInput(0,180,0,0,false)==null,"idle input stays idle");
        float[] backwards=RotationManager.correctedInput(0,180,-1,0,false);
        near(backwards[0],1,"S remaps under reverse server yaw");near(backwards[1],0,"S has no sideways input");
        float quantized=AngleUtils.quantize(89.9f,0,.5f,true);near(quantized,Math.rint(quantized/.15)*.15,"source mouse sensitivity grid");
        System.out.println("Supplied Scaffold source behavior: "+assertions+" assertions passed");
    }
}
