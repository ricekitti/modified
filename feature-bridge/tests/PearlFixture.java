import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;

/** Real recovered solver/physics and live native-bound bridge, with deterministic world/network fixtures. */
public final class PearlFixture {
    private static int checks;
    private static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
    private static Field field(Object object,String name)throws Exception{
        for(Class<?> c=object instanceof Class?(Class<?>)object:object.getClass();c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(FeatureFixture.mappedName(c,name));f.setAccessible(true);return f;}catch(NoSuchFieldException missing){}
        throw new NoSuchFieldException(name);
    }
    private static Object get(Object object,String name)throws Exception{return field(object,name).get(object instanceof Class?null:object);}
    private static void set(Object object,String name,Object value)throws Exception{field(object,name).set(object instanceof Class?null:object,value);}
    private static Object invoke(Object object,String name,Class<?>[] types,Object...args)throws Exception{
        for(Class<?> c=object.getClass();c!=null;c=c.getSuperclass())try{Method m=c.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(object,args);}catch(NoSuchMethodException missing){}
        throw new NoSuchMethodException(name);
    }
    private static void bool(Object module,String key,boolean value)throws Exception{Object setting=module.getClass().getMethod("findSetting",String.class).invoke(module,key);setting.getClass().getMethod("set",Object.class).invoke(setting,value);}
    public static void run(Class<?> bridge)throws Exception{
        checks=0;Minecraft mc=FeatureFixture.minecraft;FeatureFixture.Player p=FeatureFixture.self;
        Vec3 enemyPosition=FeatureFixture.enemy.getPositionVector();AxisAlignedBB enemyBox=FeatureFixture.enemy.getEntityBoundingBox();
        Object[] modules=(Object[])get(bridge,"modules");Object pearl=modules[6];Object context=get(bridge,"context");
        Method event=bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class);
        check(modules.length>=7,"Auto Pearl feature slot");
        check(((List<?>)pearl.getClass().getMethod("getSettings").invoke(pearl)).size()==14,"all fourteen source controls");
        check(!(Boolean)pearl.getClass().getMethod("settingVisible",String.class).invoke(pearl,"in_void_for"),"void switch hidden initially");
        bool(pearl,"above_void",true);
        check((Boolean)pearl.getClass().getMethod("settingVisible",String.class).invoke(pearl,"in_void_for"),"above-void exposes duration switch");
        bool(pearl,"in_void_for",true);
        check((Boolean)pearl.getClass().getMethod("settingVisible",String.class).invoke(pearl,"time"),"void duration visible");
        bool(pearl,"predict_safe_landing",true);
        check(!(Boolean)pearl.getClass().getMethod("settingVisible",String.class).invoke(pearl,"time"),"prediction hides timed void");
        bool(pearl,"predict_safe_landing",false);bool(pearl,"in_void_for",false);bool(pearl,"above_void",false);
        p.setPosition(.5,70,.5);p.rotationYaw=-45;p.rotationPitch=25;p.onGround=false;p.motionY=-.2;
        p.inventory.currentItem=0;p.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);p.inventory.mainInventory[2]=new ItemStack(Items.ender_pearl,4);
        mc.currentScreen=null;mc.objectMouseOver=null;
        set(mc.theWorld,"worldAccesses",new ArrayList<>());set(mc.theWorld,"isRemote",true);
        FeatureFixture.pearlBlocks=new HashMap<>();for(int x=3;x<12;x++)for(int z=3;z<12;z++)FeatureFixture.pearlBlocks.put(new BlockPos(x,64,z),Blocks.stone.getDefaultState());
        try{
            event.invoke(null,6,5,true,null,new float[13]);
            float[] rotation=new float[13];rotation[2]=p.rotationYaw;rotation[3]=p.rotationPitch;
            event.invoke(null,6,13,true,null,rotation);
            check((Boolean)get(pearl,"hasSolution"),"recovered candidate search finds a reachable platform");
            check(rotation[12]==1&&rotation[4]==1,"solver requests native rotation and movement correction");
            Class<?> services=bridge.getClassLoader().loadClass("dev.rice.module.impl.pearl.RescueServices");
            check(get(modules[5],"timer")==services.getMethod("timer").invoke(null),"Boost and rescue use the same timer arbiter");
            float[] server=(float[])services.getField("serverRotations").get(null);server[0]=rotation[2];server[1]=rotation[3];
            Object impact=invoke(pearl,"simulate",new Class[]{float.class,float.class,boolean.class},rotation[2],rotation[3],true);
            check(impact!=null,"solved throw trajectory collides with world");
            BlockPos target=(BlockPos)get(pearl,"targetBlock");
            MovingObjectPosition center=new MovingObjectPosition(new Vec3(target.getX()+.5,target.getY()+1,target.getZ()+.5),EnumFacing.UP,target);
            check((Boolean)invoke(pearl,"landsOnTarget",new Class[]{MovingObjectPosition.class,BlockPos.class},center,target),"centered target impact is accepted");
            MovingObjectPosition edge=new MovingObjectPosition(new Vec3(target.getX()+.01,target.getY()+1,target.getZ()+.5),EnumFacing.UP,target);
            check(!(Boolean)invoke(pearl,"landsOnTarget",new Class[]{MovingObjectPosition.class,BlockPos.class},edge,target),"grazing edge is rejected for launch-spread margin");
            BlockPos neighbor=target.east();
            MovingObjectPosition wrong=new MovingObjectPosition(new Vec3(neighbor.getX()+.5,neighbor.getY()+1,neighbor.getZ()+.5),EnumFacing.UP,neighbor);
            check(!(Boolean)invoke(pearl,"landsOnTarget",new Class[]{MovingObjectPosition.class,BlockPos.class},wrong,target),"neighbor tile cannot stand in for selected safe landing");
            Class<?> fall=bridge.getClassLoader().loadClass("dev.rice.module.impl.pearl.RescueFall");
            FeatureFixture.enemy.setPosition(5.5,70,5.5);FeatureFixture.enemy.motionY=-.2;
            Object predicted=fall.getMethod("predict",EntityPlayer.class,double.class).invoke(null,FeatureFixture.enemy,-8d);
            check((Boolean)predicted.getClass().getField("lands").get(predicted),"ballistic fall predicts solid landing");
            Class<?> simulated=bridge.getClassLoader().loadClass("dev.rice.module.impl.pearl.SimulatedPlayer");
            Object sim=simulated.getMethod("fromClientPlayer",MovementInput.class).invoke(null,p.movementInput);
            check(Math.abs((Double)get(sim,"posY")-p.posY)<1e-9,"simulator snapshots mapped private game fields");
            int sent=FeatureFixture.sentCount;
            // Actual post-walking callback uses the currently applied server angles.
            p.rotationYaw=rotation[2];p.rotationPitch=rotation[3];bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
            check(FeatureFixture.sentCount>sent,"post-motion sends a real use-item packet");
            check(p.inventory.currentItem==0,"swap-back restores original hotbar slot");
            boolean used=false;for(net.minecraft.network.Packet packet:FeatureFixture.sentPackets)if(packet instanceof C08PacketPlayerBlockPlacement){ItemStack stack=((C08PacketPlayerBlockPlacement)packet).getStack();if(stack!=null&&stack.getItem()==Items.ender_pearl)used=true;}
            check(used,"outgoing C08 carries pearl stack");
            check((Boolean)get(pearl,"timering"),"successful pearl enters flight timer");
            p.onGround=true;event.invoke(null,6,1,true,null,new float[13]);p.onGround=false;
            check((Boolean)get(pearl,"timering")&&(Boolean)pearl.getClass().getMethod("isEnabled").invoke(pearl),"touching ground before pearl teleport must not finish flight");
            check((Long)get(pearl,"timerDeadlineMs")-System.currentTimeMillis()>9000,"flight watchdog must allow long airborne pearls");
            set(pearl,"timerDeadlineMs",0L);event.invoke(null,6,1,true,null,new float[13]);
            check((Boolean)pearl.getClass().getMethod("isEnabled").invoke(pearl),"unconfirmed timeout must not disable pearl clutch");
            set(pearl,"timering",true);
            event.invoke(null,6,2,true,new S08PacketPlayerPosLook(),new float[13]);
            float[] frame=new float[13];event.invoke(null,6,1,true,null,frame);
            check(!(Boolean)get(pearl,"timering"),"server correction releases flight timer");
            check(frame[12]==-1,"disable afterwards returns native disable sentinel");
            check(!(Boolean)pearl.getClass().getMethod("isEnabled").invoke(pearl),"disable afterwards switches Java state off");
            Object timer=services.getMethod("timer").invoke(null);check(!(Boolean)timer.getClass().getMethod("hasRequests").invoke(timer),"correction leaves no timer owner");
            event.invoke(null,6,5,true,null,new float[13]);
            p.inventory.mainInventory[2]=null;event.invoke(null,6,13,true,null,new float[13]);check(!(Boolean)get(pearl,"hasSolution"),"missing pearl clears solution");
            event.invoke(null,6,26,true,null,new float[13]);check(!(Boolean)get(pearl,"solving"),"world unload clears prediction state");
            event.invoke(null,6,5,false,null,new float[13]);
        } finally {
            FeatureFixture.enemy.setPosition(enemyPosition.xCoord,enemyPosition.yCoord,enemyPosition.zCoord);FeatureFixture.enemy.setEntityBoundingBox(enemyBox);
            FeatureFixture.pearlBlocks=null;p.inventory.mainInventory[2]=null;p.inventory.currentItem=0;FeatureFixture.resetScaffoldFixture();
        }
        System.out.println("Auto Pearl: "+checks+" recovered solver, trajectory, mapped physics, item-use, timer and lifecycle checks passed");
    }
    public static void combo(Class<?> bridge)throws Exception{
        checks=0;Minecraft mc=FeatureFixture.minecraft;FeatureFixture.Player p=FeatureFixture.self;
        Object module=((Object[])get(bridge,"modules"))[7];Method event=bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class);
        FeatureFixture.pearlBlocks=new HashMap<>();FeatureFixture.packetObserverBridge=bridge;
        p.setPosition(.5,60,.5);p.onGround=false;p.motionY=-.2;p.rotationYaw=20;p.rotationPitch=0;p.ticksExisted=10;
        p.inventory.currentItem=0;p.inventory.mainInventory[2]=new ItemStack(Items.ender_pearl,4);p.inventory.mainInventory[3]=new ItemStack(Items.fire_charge,4);
        try{
            check(((List<?>)module.getClass().getMethod("getSettings").invoke(module)).size()==12,"fireball mode has twelve source controls");
            check(module.getClass().getMethod("findSetting",String.class).invoke(module,"swap_back")==null,"fireball mode does not register swap-back");
            event.invoke(null,7,5,true,null,new float[13]);
            float[] rotation=new float[13];rotation[2]=p.rotationYaw;rotation[3]=p.rotationPitch;
            event.invoke(null,7,13,true,null,rotation);
            check((Integer)get(module,"comboPhase")==1,"unrecoverable falling trajectory begins combo");
            check(rotation[12]==1&&rotation[3]==-90,"combo points pearl straight up");
            p.rotationYaw=rotation[2];p.rotationPitch=rotation[3];int first=FeatureFixture.sentPackets.size();
            bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
            check((Integer)get(module,"comboPhase")==2,"first walking callback sends pearl and waits for fireball");
            check(p.inventory.currentItem==2,"pearl slot remains selected between combo stages");
            bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
            check((Integer)get(module,"comboPhase")==2,"same-tick callback cannot send second stage");
            p.ticksExisted++;bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
            check((Integer)get(module,"comboPhase")==3,"following tick sends fireball and enters flight");
            check(p.inventory.currentItem==0,"combo restores original selected slot");
            List<net.minecraft.item.Item> order=new ArrayList<>();for(int i=first;i<FeatureFixture.sentPackets.size();i++)if(FeatureFixture.sentPackets.get(i) instanceof C08PacketPlayerBlockPlacement){ItemStack s=((C08PacketPlayerBlockPlacement)FeatureFixture.sentPackets.get(i)).getStack();if(s!=null)order.add(s.getItem());}
            check(order.size()==2&&order.get(0)==Items.ender_pearl&&order.get(1)==Items.fire_charge,"exactly pearl then fireball use packets");
            Object timer=bridge.getClassLoader().loadClass("dev.rice.module.impl.pearl.RescueServices").getMethod("timer").invoke(null);
            check(Math.abs((Float)timer.getClass().getMethod("getSpeed").invoke(timer)-.03f)<1e-6,"flight timer has source 0.03 speed");
            event.invoke(null,7,2,true,new S08PacketPlayerPosLook(),new float[13]);float[] frame=new float[13];event.invoke(null,7,1,true,null,frame);
            check((Integer)get(module,"comboPhase")==0&&frame[12]==-1,"correction aborts combo and disables afterwards");
            check(!(Boolean)timer.getClass().getMethod("hasRequests").invoke(timer),"combo correction releases timer ownership");
        }finally{event.invoke(null,7,5,false,null,new float[13]);FeatureFixture.packetObserverBridge=null;FeatureFixture.pearlBlocks=null;p.inventory.mainInventory[2]=p.inventory.mainInventory[3]=null;p.inventory.currentItem=0;FeatureFixture.resetScaffoldFixture();}
        System.out.println("Fireball Pearl Clutch: "+checks+" physics, staged packets, tick gating and cleanup checks passed");
    }
    public static void egg(Class<?> bridge)throws Exception{
        checks=0;Minecraft mc=FeatureFixture.minecraft;FeatureFixture.Player p=FeatureFixture.self;
        Object module=((Object[])get(bridge,"modules"))[8];Method event=bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class);
        FeatureFixture.pearlBlocks=new HashMap<>();FeatureFixture.packetObserverBridge=bridge;
        p.setPosition(.5,80,.5);p.onGround=false;p.motionY=-.4;p.motionX=.1;p.motionZ=0;p.ticksExisted=20;p.inventory.currentItem=0;p.inventory.mainInventory[4]=new ItemStack(Items.egg,8);
        try{
            check(((List<?>)module.getClass().getMethod("getSettings").invoke(module)).size()==8,"eight egg settings including fireball priority");
            check(!(Boolean)module.getClass().getMethod("settingVisible",String.class).invoke(module,"time"),"void time hidden by default");
            Class<?> scan=bridge.getClassLoader().loadClass("dev.rice.module.impl.pearl.SupportScan");Method unsupported=scan.getMethod("isUnsupported",net.minecraft.world.World.class,double.class,double.class,double.class);
            check((Boolean)unsupported.invoke(null,mc.theWorld,.5,80d,.5),"empty column is unsupported");
            FeatureFixture.pearlBlocks.put(new BlockPos(0,10,0),Blocks.stone.getDefaultState());
            check(!(Boolean)unsupported.invoke(null,mc.theWorld,.5,80d,.5),"deep solid column counts as support");FeatureFixture.pearlBlocks.clear();
            event.invoke(null,8,5,true,null,new float[13]);
            Object fireball=((Object[])get(bridge,"modules"))[7];
            p.inventory.mainInventory[2]=new ItemStack(Items.ender_pearl,4);p.inventory.mainInventory[3]=new ItemStack(Items.fire_charge,4);
            event.invoke(null,7,5,true,null,new float[13]);
            event.invoke(null,8,13,true,null,new float[13]);
            check(get(get(module,"attempt"),"state").toString().equals("IDLE"),"egg yields to ready fireball before fireball event dispatch");
            bool(module,"wait_for_fireball_pearl_clutch",false);
            check((Boolean)module.getClass().getMethod("canSave").invoke(module),"turning wait condition off permits egg priority");
            bool(module,"wait_for_fireball_pearl_clutch",true);
            p.inventory.mainInventory[3]=null;
            check(!(Boolean)module.getClass().getMethod("canSave").invoke(module),"strict wait holds even before fireball item is available");
            p.inventory.mainInventory[3]=new ItemStack(Items.fire_charge,4);
            bool(fireball,"damaged",true);
            check(!(Boolean)module.getClass().getMethod("canSave").invoke(module),"strict wait holds while fireball damage condition is pending");
            event.invoke(null,8,13,true,null,new float[13]);
            check(get(get(module,"attempt"),"state").toString().equals("IDLE"),"pending fireball condition must not start Egg through rotation dispatch");
            bool(fireball,"damaged",false);
            bool(fireball,"above_void",true);bool(fireball,"in_void_for",true);
            event.invoke(null,7,13,true,null,new float[13]);
            check((Integer)get(fireball,"comboPhase")==0,"fireball duration condition has not elapsed yet");
            event.invoke(null,8,13,true,null,new float[13]);
            check(get(get(module,"attempt"),"state").toString().equals("IDLE"),"Egg must wait through fireball void-duration gate");
            bool(fireball,"above_void",false);bool(fireball,"in_void_for",false);
            float[] comboAim=new float[13];event.invoke(null,7,13,true,null,comboAim);
            check((Integer)get(fireball,"comboPhase")==1,"fireball starts while both default wait settings are enabled");
            check(!(Boolean)module.getClass().getMethod("canSave").invoke(module),"egg waits during fireball attempt");
            p.rotationYaw=comboAim[2];p.rotationPitch=comboAim[3];
            bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);p.ticksExisted++;
            bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
            check((Integer)get(fireball,"comboPhase")==3,"both-enabled rescue sends pearl and fireball before egg");
            check(!(Boolean)module.getClass().getMethod("canSave").invoke(module),"strict wait continues during fireball pearl flight");
            set(fireball,"comboDeadline",0L);event.invoke(null,7,1,true,null,new float[13]);
            check((Boolean)module.getClass().getMethod("canSave").invoke(module),"egg becomes available after fireball flight while still falling");
            p.onGround=true;
            check(!(Boolean)module.getClass().getMethod("canSave").invoke(module),"landing prevents follow-up egg");
            p.onGround=false;p.motionY=-.4;
            check(!(Boolean)module.getClass().getMethod("canSave").invoke(module),"new fall must wait for a new fireball attempt");
            event.invoke(null,7,5,false,null,new float[13]);p.inventory.mainInventory[2]=p.inventory.mainInventory[3]=null;
            check((Boolean)module.getClass().getMethod("canSave").invoke(module),"disabling Fireball Pearl releases strict Egg wait");
            float[] rotation=new float[13];event.invoke(null,8,13,true,null,rotation);
            Object attempt=get(module,"attempt");
            check(get(attempt,"state").toString().equals("AIMING"),"recovered prediction/throw solver arms an egg rescue");
            check(rotation[12]==1&&rotation[4]==1,"egg aim requests native rotation and move fix");
            p.rotationYaw=rotation[2];p.rotationPitch=rotation[3];int first=FeatureFixture.sentPackets.size();
            bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
            check(get(attempt,"state").toString().equals("THROWN"),"post-walking packet callback throws egg");
            check(p.inventory.currentItem==4,"egg retains use slot for source one-tick restoration");
            boolean used=false;for(int i=first;i<FeatureFixture.sentPackets.size();i++)if(FeatureFixture.sentPackets.get(i) instanceof C08PacketPlayerBlockPlacement){ItemStack s=((C08PacketPlayerBlockPlacement)FeatureFixture.sentPackets.get(i)).getStack();if(s!=null&&s.getItem()==Items.egg)used=true;}
            check(used,"actual C08 contains egg stack");p.ticksExisted++;
            event.invoke(null,8,1,true,null,new float[13]);check(p.inventory.currentItem==0,"next player tick restores original slot");
            check(get(attempt,"state").toString().equals("DELAYED_SLOWDOWN"),"source delayed slowdown begins after throw tick");
            Object timer=bridge.getClassLoader().loadClass("dev.rice.module.impl.pearl.RescueServices").getMethod("timer").invoke(null);
            check(Math.abs((Float)timer.getClass().getMethod("getSpeed").invoke(timer)-.03f)<1e-6,"after timer uses source .03 speed");
            BlockPos support=(BlockPos)get(module,"bridge");FeatureFixture.pearlBlocks.put(support,Blocks.stone.getDefaultState());
            event.invoke(null,8,2,true,new S23PacketBlockChange(mc.theWorld,support),new float[13]);
            event.invoke(null,8,1,true,null,new float[13]);
            check(get(attempt,"state").toString().equals("COMPLETE"),"server support update confirms reachable bridge");
            check(!(Boolean)timer.getClass().getMethod("hasRequests").invoke(timer),"support confirmation releases timer");
            event.invoke(null,8,5,false,null,new float[13]);FeatureFixture.pearlBlocks.clear();bool(module,"use_stasis_instead",true);
            check(!(Boolean)module.getClass().getMethod("settingVisible",String.class).invoke(module,"pre_timer_speed"),"stasis hides timer controls");
            p.setPosition(.5,80,.5);p.motionY=-.4;p.motionX=.1;
            event.invoke(null,8,5,true,null,new float[13]);event.invoke(null,8,13,true,null,new float[13]);
            check(p.motionY==0&&get(module,"suspendedMotion")!=null,"stasis captures and suspends source motion");
            double y=p.posY;bridge.getMethod("scaffoldMove",Object.class,double.class,double.class,double.class).invoke(null,p,0d,-1d,0d);
            check(p.posY==y,"moveEntity adapter cancels movement while egg suspension owns it");
            event.invoke(null,8,5,false,null,new float[13]);check(p.motionY==-.4,"disable restores owned suspended motion");
            bool(module,"use_stasis_instead",false);p.motionY=-.4;event.invoke(null,8,5,true,null,new float[13]);event.invoke(null,8,13,true,null,new float[13]);
            event.invoke(null,8,2,true,new S08PacketPlayerPosLook(),new float[13]);event.invoke(null,8,1,true,null,new float[13]);
            check(get(attempt,"state").toString().equals("FAILED"),"server correction aborts active attempt");
            check(!(Boolean)timer.getClass().getMethod("hasRequests").invoke(timer),"correction leaves no egg timer request");
            event.invoke(null,8,26,true,null,new float[13]);check(get(attempt,"state").toString().equals("IDLE"),"world unload clears attempt and queues");
        }finally{event.invoke(null,7,5,false,null,new float[13]);event.invoke(null,8,5,false,null,new float[13]);bool(module,"use_stasis_instead",false);FeatureFixture.packetObserverBridge=null;FeatureFixture.pearlBlocks=null;p.inventory.mainInventory[2]=p.inventory.mainInventory[3]=p.inventory.mainInventory[4]=null;p.inventory.currentItem=0;FeatureFixture.resetScaffoldFixture();}
        System.out.println("Bridge Egg: "+checks+" support, prediction, packet, suspension and lifecycle checks passed");
    }
    public static void expireFromNative(Class<?> bridge)throws Exception{
        Object module=((Object[])get(bridge,"modules"))[6];
        bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class).invoke(null,6,5,true,null,new float[13]);
        set(bridge,"lastWorld",FeatureFixture.minecraft.theWorld);set(bridge,"lastPlayer",FeatureFixture.minecraft.thePlayer);
        set(module,"activeWorld",FeatureFixture.minecraft.theWorld);set(module,"activePlayer",FeatureFixture.minecraft.thePlayer);
        set(module,"timering",true);set(module,"landed",true);set(module,"timerDeadlineMs",0L);
    }
    private static int voidPacketStart;
    private static float[] voidAngles;
    public static void prepareAboveVoid(Class<?> bridge)throws Exception{
        Object module=((Object[])get(bridge,"modules"))[6];
        Method event=bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class);
        FeatureFixture.Player p=FeatureFixture.self;
        set(p,"mc",FeatureFixture.minecraft);
        FeatureFixture.pearlBlocks=new HashMap<>();for(int x=-4;x<=4;x++)for(int z=4;z<=12;z++)FeatureFixture.pearlBlocks.put(new BlockPos(x,64,z),Blocks.stone.getDefaultState());
        p.setPosition(.5,70,.5);p.rotationYaw=p.rotationPitch=0;p.onGround=false;p.motionY=-.2;p.inventory.currentItem=0;p.inventory.mainInventory[2]=new ItemStack(Items.ender_pearl,4);
        bool(module,"above_void",true);bool(module,"in_void_for",false);bool(module,"predict_safe_landing",false);
        event.invoke(null,6,5,true,null,new float[13]);
        FeatureFixture.pearlBlocks.put(new BlockPos(0,64,0),Blocks.stone.getDefaultState());
        event.invoke(null,6,13,true,null,new float[13]);
        check(!(Boolean)get(module,"hasSolution"),"Above void must reject a solid support column");
        FeatureFixture.pearlBlocks.remove(new BlockPos(0,64,0));
        float[] aim=new float[13];event.invoke(null,6,13,true,null,aim);
        check((Boolean)get(module,"voidReady")&&(Boolean)get(module,"hasSolution"),"Above void must arm a reachable pearl rescue over empty columns");
        voidAngles=(float[])bridge.getMethod("rescueMotionRotation").invoke(null);
        check(voidAngles!=null&&Math.abs(voidAngles[1]-p.rotationPitch)>.1,"Rescue packet aim must be independent of camera pitch");
        int before=FeatureFixture.sentPackets.size();p.onUpdateWalkingPlayer();
        bridge.getMethod("rescuePostMotion",Object.class).invoke(null,p);
        check(!(Boolean)get(module,"timering"),"Camera-only walking without rescue aim must not launch an unverified pearl");
        for(int i=before;i<FeatureFixture.sentPackets.size();i++)check(!(FeatureFixture.sentPackets.get(i) instanceof C08PacketPlayerBlockPlacement),"Negative control must not use an item before verified packet aim");
        voidPacketStart=FeatureFixture.sentPackets.size();
    }
    public static void checkAboveVoid(Class<?> bridge)throws Exception{
        Object module=((Object[])get(bridge,"modules"))[6];FeatureFixture.Player p=FeatureFixture.self;
        try{
            boolean look=false,use=false;
            for(int i=voidPacketStart;i<FeatureFixture.sentPackets.size();i++){
                net.minecraft.network.Packet packet=FeatureFixture.sentPackets.get(i);
                if(packet instanceof C03PacketPlayer&&(Boolean)get(packet,"rotating"))look|=Math.abs((Float)get(packet,"yaw")-voidAngles[0])<.001&&Math.abs((Float)get(packet,"pitch")-voidAngles[1])<.001;
                if(packet instanceof C08PacketPlayerBlockPlacement){ItemStack stack=((C08PacketPlayerBlockPlacement)packet).getStack();use|=stack!=null&&stack.getItem()==Items.ender_pearl;}
            }
            check(look,"Real walking packet must carry rescue rotation while Above void is enabled");
            check(use&&(Boolean)get(module,"timering"),"Real walking hook must launch the Above void pearl before restoring camera");
            check(p.rotationYaw==0&&p.rotationPitch==0,"Rescue walking scope must restore freely chosen camera angles");
            check(p.inventory.currentItem==0,"Above void launch must restore selected slot");
            System.out.println("Above void walking integration: supported-ground rejection, empty-void launch, packet aim and camera/slot restoration passed");
        }finally{
            bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class).invoke(null,6,5,false,null,new float[13]);
            bool(module,"above_void",false);FeatureFixture.pearlBlocks=null;p.inventory.mainInventory[2]=null;FeatureFixture.resetScaffoldFixture();
        }
    }
}
