import dev.rice.mayu.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import java.util.*;
import java.lang.reflect.*;

/** Real 1.8.9 game types with recorded world/controller boundaries. No game or native window. */
public final class MayuScaffoldTest {
    static int assertions,placements;
    static final Map<BlockPos,net.minecraft.block.state.IBlockState> blocks=new HashMap<>();
    static void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    static void near(double a,double b,String message){check(Math.abs(a-b)<1e-7,message+": "+a+" != "+b);}
    public static class World extends FeatureFixture.World{
        public World(){super();}
        @Override public boolean isBlockLoaded(BlockPos p){return true;}
        @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return blocks.getOrDefault(p,Blocks.air.getDefaultState());}
        @Override public java.util.List<AxisAlignedBB> getCollidingBoundingBoxes(net.minecraft.entity.Entity entity,AxisAlignedBB box){
            List<AxisAlignedBB> result=new ArrayList<>();
            for(BlockPos p:blocks.keySet()){AxisAlignedBB b=new AxisAlignedBB(p,p.add(1,1,1));if(b.intersectsWith(box))result.add(b);}
            return result;
        }
        @Override public MovingObjectPosition rayTraceBlocks(Vec3 start,Vec3 end,boolean liquid,boolean ignore,boolean last){
            MovingObjectPosition best=null;double distance=Double.POSITIVE_INFINITY;
            for(BlockPos p:blocks.keySet()){
                MovingObjectPosition hit=new AxisAlignedBB(p,p.add(1,1,1)).calculateIntercept(start,end);
                if(hit!=null&&start.squareDistanceTo(hit.hitVec)<distance){distance=start.squareDistanceTo(hit.hitVec);best=new MovingObjectPosition(hit.hitVec,hit.sideHit,p);}
            }return best;
        }
    }
    public static class Controller extends PlayerControllerMP{
        public Controller(){super(null,null);}
        @Override public float getBlockReachDistance(){return 4.5f;}
        @Override public net.minecraft.world.WorldSettings.GameType getCurrentGameType(){return net.minecraft.world.WorldSettings.GameType.SURVIVAL;}
        @Override public boolean onPlayerRightClick(EntityPlayerSP p,WorldClient world,ItemStack stack,BlockPos support,EnumFacing face,Vec3 hit){
            check(blocks.containsKey(support),"placement requires an existing support");
            check(!blocks.containsKey(support.offset(face)),"placement requires a replaceable neighbor");
            check(hit!=null,"placement uses the verified ray hit");
            blocks.put(support.offset(face),Blocks.stone.getDefaultState());placements++;stack.stackSize--;return true;
        }
    }
    static ScaffoldModule setup()throws Exception{
        if(FeatureFixture.minecraft==null)FeatureFixture.create();
        FeatureFixture.put(FeatureFixture.self,"mc",FeatureFixture.minecraft);
        FeatureFixture.put(FeatureFixture.minecraft,"renderViewEntity",FeatureFixture.self);
        FeatureFixture.minecraft.gameSettings.keyBindJump=new KeyBinding("test.jump",57,"test");
        FeatureFixture.minecraft.gameSettings.keyBindSneak=new KeyBinding("test.sneak",42,"test");
        FeatureFixture.minecraft.gameSettings.keyBindSprint=new KeyBinding("test.sprint",29,"test");
        for(int k:new int[]{17,31,30,32,57,42})KeyBinding.setKeyBindState(k,false);
        World world=FeatureFixture.blank(World.class);
        FeatureFixture.minecraft.theWorld=world;FeatureFixture.self.worldObj=world;
        FeatureFixture.minecraft.playerController=FeatureFixture.blank(Controller.class);
        FeatureFixture.self.width=.6f;FeatureFixture.self.height=1.8f;
        FeatureFixture.self.setPosition(1.2,65,.5);
        FeatureFixture.self.onGround=true;FeatureFixture.self.motionX=FeatureFixture.self.motionY=FeatureFixture.self.motionZ=0;
        FeatureFixture.self.rotationYaw=90;FeatureFixture.self.rotationPitch=84;
        Arrays.fill(FeatureFixture.self.inventory.mainInventory,null);
        FeatureFixture.self.inventory.currentItem=0;
        FeatureFixture.self.inventory.mainInventory[0]=new ItemStack(Items.diamond_sword);
        FeatureFixture.self.inventory.mainInventory[8]=new ItemStack(Blocks.stone,12);
        FeatureFixture.self.inventory.mainInventory[2]=new ItemStack(Blocks.stone,64);
        blocks.clear();blocks.put(new BlockPos(0,64,0),Blocks.stone.getDefaultState());placements=0;
        ScaffoldModule module=new ScaffoldModule();module.setEnabled(true);return module;
    }
    static RotationUpdateEvent update(ScaffoldModule m,float yaw,float pitch){RotationUpdateEvent e=new RotationUpdateEvent(yaw,pitch,yaw,pitch);m.onRotationUpdate(e);return e;}
    public static void nativePrepare()throws Exception{setup().setEnabled(false);}
    public static Object nativePosition(){return new BlockPos(1,0,0);}
    public static int nativeSlot(){return FeatureFixture.self.inventory.currentItem;}
    public static int nativePlacements(){return placements;}
    public static Object nativeInventory(){return FeatureFixture.self.inventory;}
    static int guiSlot;
    public static class Hud extends net.minecraft.client.gui.GuiIngame{
        public Hud(){super(null);}
        @Override public void updateTick(){guiSlot=FeatureFixture.self.inventory.currentItem;}
    }
    public static Object nativeGui()throws Exception{return FeatureFixture.blank(Hud.class);}
    public static int nativeGuiSlot(){return guiSlot;}
    public static void nativeSetView(float yaw,float pitch){FeatureFixture.self.rotationYaw=yaw;FeatureFixture.self.rotationPitch=pitch;}
    public static void nativeForceLook()throws Exception{FeatureFixture.put(FeatureFixture.self,"lastReportedYaw",777f);}
    public static void nativePacketMatches(float yaw,float pitch){
        check(FeatureFixture.lastSent instanceof net.minecraft.network.play.client.C03PacketPlayer,"walking sends a player packet");
        net.minecraft.network.play.client.C03PacketPlayer packet=(net.minecraft.network.play.client.C03PacketPlayer)FeatureFixture.lastSent;
        near(packet.getYaw(),yaw,"walking packet uses the independent placement yaw");
        near(packet.getPitch(),pitch,"walking packet uses the independent placement pitch");
    }
    public static void nativeVisualMatches(float yaw,float pitch){
        near(FeatureFixture.self.rotationYawHead,yaw,"third-person head faces placement");
        near(FeatureFixture.self.rotationPitch,pitch,"third-person pitch faces placement");
    }
    public static void checkNativeMovement(Class<?> bridge)throws Exception{
        Field contextField=bridge.getDeclaredField("context");contextField.setAccessible(true);
        Object context=contextField.get(null);
        ClassLoader loader=bridge.getClassLoader();
        Class<?> state=loader.loadClass("dev.rice.mayu.RotationState");
        Method rotation=state.getMethod("update",boolean.class,float.class,float.class,float.class,int.class);
        Method input=bridge.getMethod("input",Object.class);
        Method move=bridge.getMethod("mayuMove",Object.class,float.class,float.class,float.class);
        float[] untouched=new float[13];untouched[2]=17;untouched[3]=23;
        float[] response=(float[])bridge.getMethod("event",int.class,int.class,boolean.class,Object.class,float[].class)
            .invoke(null,4,13,true,null,untouched);
        check(response[12]==0&&response[4]==0&&response[2]==17&&response[3]==23,
            "Scaffold must leave the host persistent rotation/key-remapping event untouched");
        int[][] directions={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        int cases=0;
        for(float camera:new float[]{-179,-90,0,90,179})
            for(float delta:new float[]{-180,-157.6f,-157.4f,-135,-112.6f,-112.4f,-90,-67.6f,-67.4f,-45,-22.6f,-22.4f,0,22.4f,22.6f,45,67.4f,67.6f,90,112.4f,112.6f,135,157.4f,157.6f,180})
                for(int[] direction:directions){
                    int forward=direction[0],strafe=direction[1];
                    KeyBinding.setKeyBindState(17,forward>0);KeyBinding.setKeyBindState(31,forward<0);
                    KeyBinding.setKeyBindState(30,strafe>0);KeyBinding.setKeyBindState(32,strafe<0);
                    FeatureFixture.self.rotationYaw=camera;
                    FeatureFixture.self.movementInput.moveForward=forward;
                    FeatureFixture.self.movementInput.moveStrafe=strafe;
                    FeatureFixture.self.movementInput.sneak=false;FeatureFixture.self.movementInput.jump=false;
                    rotation.invoke(null,true,camera+delta,80f,camera+delta,3);
                    input.invoke(null,FeatureFixture.self.movementInput);
                    FeatureFixture.self.motionX=FeatureFixture.self.motionZ=0;
                    check((Boolean)move.invoke(null,FeatureFixture.self,
                        FeatureFixture.self.movementInput.moveStrafe,FeatureFixture.self.movementInput.moveForward,.1f),
                        "Mayu must consume corrected movement");
                    double radians=Math.toRadians(camera);
                    double expectedX=strafe*Math.cos(radians)-forward*Math.sin(radians);
                    double expectedZ=forward*Math.cos(radians)+strafe*Math.sin(radians);
                    double actualX=FeatureFixture.self.motionX,actualZ=FeatureFixture.self.motionZ;
                    double alignment=(actualX*expectedX+actualZ*expectedZ)/(Math.hypot(actualX,actualZ)*Math.hypot(expectedX,expectedZ));
                    check(alignment>=.999999,
                        "WASD moved in the wrong world direction: camera="+camera+" spoof delta="+delta+" input="+forward+","+strafe+" alignment="+alignment);
                    check(FeatureFixture.self.rotationYaw==camera,"Movement must preserve camera yaw");
                    check(FeatureFixture.minecraft.gameSettings.keyBindForward.isKeyDown()==(forward>0)
                        &&FeatureFixture.minecraft.gameSettings.keyBindBack.isKeyDown()==(forward<0)
                        &&FeatureFixture.minecraft.gameSettings.keyBindLeft.isKeyDown()==(strafe>0)
                        &&FeatureFixture.minecraft.gameSettings.keyBindRight.isKeyDown()==(strafe<0),
                        "Mayu must not rewrite physical WASD key states");
                    if(camera==0&&Math.abs(delta)==180&&forward==-1&&strafe==0)
                        check(actualZ<0,"S must move backwards while Scaffold faces forwards");
                    cases++;
                }
        checkSteadyMovement(bridge);
        for(int key:new int[]{17,31,30,32})KeyBinding.setKeyBindState(key,false);
        FeatureFixture.self.rotationYaw=90;FeatureFixture.self.motionX=FeatureFixture.self.motionZ=0;
        rotation.invoke(null,true,90f,84f,90f,3);
        System.out.println("Mayu native movement: "+cases+" camera/rotation/WASD cases passed, including reversed placement yaw");
    }
    public static void checkSteadyMovement(Class<?> bridge)throws Exception{
        Class<?> state=bridge.getClassLoader().loadClass("dev.rice.mayu.RotationState");
        Method rotation=state.getMethod("update",boolean.class,float.class,float.class,float.class,int.class);
        Method input=bridge.getMethod("input",Object.class),living=bridge.getMethod("mayuLiving",Object.class);
        Method move=bridge.getMethod("mayuMove",Object.class,float.class,float.class,float.class);
        Method configure=bridge.getMethod("configure",int.class,double[].class);
        double[] defaults={2,1,0,100,100,100,0,0,0,1,1,1,0,1};
        int speedCases=0;
        for(int percent:new int[]{100,50,1,0}){
            double[] settings=defaults.clone();settings[3]=settings[4]=settings[5]=percent;
            configure.invoke(null,4,settings);
            for(boolean sneaking:new boolean[]{false,true})
                for(int[] direction:new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}){
                    int f=direction[0],s=direction[1];
                    KeyBinding.setKeyBindState(17,f>0);KeyBinding.setKeyBindState(31,f<0);
                    KeyBinding.setKeyBindState(30,s>0);KeyBinding.setKeyBindState(32,s<0);
                    double baseline=-1;
                    for(float target:new float[]{0,22.4f,22.6f,45,67.4f,67.6f,90,135,179,-179}){
                        FeatureFixture.self.rotationYaw=0;
                        FeatureFixture.self.movementInput.moveForward=f*(sneaking?.3f:1);
                        FeatureFixture.self.movementInput.moveStrafe=s*(sneaking?.3f:1);
                        FeatureFixture.self.movementInput.sneak=sneaking;
                        rotation.invoke(null,true,target,80f,target,3);
                        input.invoke(null,FeatureFixture.self.movementInput);living.invoke(null,FeatureFixture.self);
                        FeatureFixture.self.motionX=FeatureFixture.self.motionZ=0;
                        move.invoke(null,FeatureFixture.self,FeatureFixture.self.movementInput.moveStrafe*.98f,
                            FeatureFixture.self.movementInput.moveForward*.98f,.1f);
                        double speed=Math.hypot(FeatureFixture.self.motionX,FeatureFixture.self.motionZ);
                        if(baseline<0)baseline=speed;
                        near(speed,baseline,"Placement yaw must not change movement speed (percent="+percent+", sneak="+sneaking+")");
                        speedCases++;
                    }
                }
        }
        configure.invoke(null,4,defaults);
        KeyBinding.setKeyBindState(17,true);
        for(int key:new int[]{31,30,32})KeyBinding.setKeyBindState(key,false);
        FeatureFixture.self.rotationYaw=0;FeatureFixture.self.motionX=FeatureFixture.self.motionZ=0;
        double lateralPosition=0;
        for(int tick=0;tick<200;tick++){
            float target=tick%2==0?22.4f:22.6f;
            rotation.invoke(null,true,target,80f,target,3);
            FeatureFixture.self.movementInput.moveForward=1;FeatureFixture.self.movementInput.moveStrafe=0;
            FeatureFixture.self.movementInput.sneak=false;
            input.invoke(null,FeatureFixture.self.movementInput);living.invoke(null,FeatureFixture.self);
            FeatureFixture.self.motionX*=.91;FeatureFixture.self.motionZ*=.91;
            move.invoke(null,FeatureFixture.self,FeatureFixture.self.movementInput.moveStrafe*.98f,
                FeatureFixture.self.movementInput.moveForward*.98f,.1f);
            lateralPosition+=FeatureFixture.self.motionX;
            check(Math.abs(FeatureFixture.self.motionX)<1e-6,"Crossing the old octant boundary must not sway sideways");
        }
        check(Math.abs(lateralPosition)<1e-4,"Repeated placement rotation changes must not build lateral drift");
        System.out.println("Steady Scaffold movement: "+speedCases+" speed/sneak/rotation cases and 200 alternating-yaw ticks passed");
    }
    public static void checkItemSpoof(Class<?> bridge)throws Exception{
        Field modules=bridge.getDeclaredField("modules");modules.setAccessible(true);Object host=((Object[])modules.get(null))[4];
        Object toggle=host.getClass().getMethod("findSetting",String.class).invoke(host,"item-spoof");
        toggle.getClass().getMethod("set",Object.class).invoke(toggle,Boolean.TRUE);
        host.getClass().getMethod("settingVisible",String.class).invoke(host,"item-spoof");
        Field coreField=host.getClass().getDeclaredField("core");coreField.setAccessible(true);Object core=coreField.get(host);
        final int shown=(Integer)core.getClass().getMethod("getPreviousHotbarSlot").invoke(core);
        final int actual=FeatureFixture.self.inventory.currentItem;
        final net.minecraft.client.renderer.ItemRenderer renderer=FeatureFixture.blank(net.minecraft.client.renderer.ItemRenderer.class);
        FeatureFixture.put(FeatureFixture.minecraft,"itemRenderer",renderer);
        final Field item=net.minecraft.client.renderer.ItemRenderer.class.getDeclaredField(FeatureFixture.mappedName(net.minecraft.client.renderer.ItemRenderer.class,"itemToRender"));item.setAccessible(true);
        final Field slot=net.minecraft.client.renderer.ItemRenderer.class.getDeclaredField(FeatureFixture.mappedName(net.minecraft.client.renderer.ItemRenderer.class,"equippedItemSlot"));slot.setAccessible(true);
        final Object original=FeatureFixture.self.getHeldItem();item.set(renderer,original);slot.setInt(renderer,actual);
        Method scope=bridge.getDeclaredMethod("withScaffoldItemSpoof",boolean.class,Runnable.class);scope.setAccessible(true);
        final RuntimeException failure=new IllegalStateException("fixture render failure");
        try{scope.invoke(null,true,(Runnable)()->{
            check(FeatureFixture.self.inventory.currentItem==shown,"Render-only spoof selects saved hotbar slot");
            try{check(item.get(renderer)==FeatureFixture.self.inventory.getStackInSlot(shown)&&slot.getInt(renderer)==shown,"First-person cache shows saved item");}
            catch(IllegalAccessException e){throw new AssertionError(e);}
            throw failure;
        });throw new AssertionError("Expected render failure");}
        catch(InvocationTargetException e){check(e.getCause()==failure,"Renderer exception preserved");}
        check(FeatureFixture.self.inventory.currentItem==actual&&item.get(renderer)==original&&slot.getInt(renderer)==actual,"Spoof restores placement slot/cache even on failure");
        Method visual=bridge.getMethod("scaffoldVisual",Object.class,float[].class);
        float[] saved=(float[])visual.invoke(null,FeatureFixture.self,null);
        check(saved!=null&&FeatureFixture.self.inventory.currentItem==shown,"Third-person model shows saved item");
        visual.invoke(null,FeatureFixture.self,saved);check(FeatureFixture.self.inventory.currentItem==actual,"Third-person render restores placement slot");
        toggle.getClass().getMethod("set",Object.class).invoke(toggle,Boolean.FALSE);host.getClass().getMethod("settingVisible",String.class).invoke(host,"item-spoof");
        scope.invoke(null,true,(Runnable)()->check(FeatureFixture.self.inventory.currentItem==actual,"Spoof off leaves actual slot visible"));
        System.out.println("Scaffold item spoof: saved slot/item, inactive path and renderer-failure restoration passed");
    }
    static Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    public static void main(String[] args)throws Exception{
        ScaffoldModule m=setup();
        check(Arrays.equals(m.rotations.getModes(),new String[]{"NONE","DEFAULT","BACKWARDS","SIDEWAYS"}),"Mayu's four rotation modes");
        check(m.rotations.getValue()==2&&m.moveFix.getValue()==1&&m.sprint.getValue()==0,"Mayu mode defaults");
        check(m.multiPlace.getValue()&&m.safeWalk.getValue()&&m.swing.getValue()&&m.blockCounter.getValue()&&!m.itemSpoof.getValue(),"Mayu boolean defaults");
        check(!m.keepYOnPress.isVisible(),"Keep-Y on-press hidden when disabled");
        m.keepY.setValue(1);check(m.keepYOnPress.isVisible(),"Keep-Y conditional visibility");m.keepY.setValue(0);
        m.multiPlace.setValue(false);
        RotationUpdateEvent e=update(m,90,84);
        check(FeatureFixture.self.inventory.currentItem==8,"Mayu searches backward from current slot, rather than largest stack");
        check(placements==1,"valid support and final rotation produce placement");
        check(FeatureFixture.self.inventory.mainInventory[8].stackSize==11,"one successful placement consumes one block");
        check(e.isModified()&&e.getMovementPriority()==3,"rotation and movement priority 3");
        m.onHotbarChange(new HotbarChangeEvent(4,0));
        check(FeatureFixture.self.inventory.currentItem==8&&m.getPreviousHotbarSlot()==4,"number key changes restored slot while holding the placement slot");
        m.onHotbarChange(new HotbarChangeEvent(-1,1));check(m.getPreviousHotbarSlot()==3,"scroll updates restored slot");
        m.setEnabled(false);check(FeatureFixture.self.inventory.currentItem==3,"disable restores user slot");

        m=setup();m.rotations.setValue(0);m.multiPlace.setValue(false);
        update(m,-90,0);check(placements==0,"NONE mode does not place through an invalid final view ray");
        blocks.clear();update(m,90,84);check(placements==0,"no support means no placement");
        m.setEnabled(false);

        m=setup();FeatureFixture.self.setPosition(.5,65,.5);m.keepY.setValue(1);
        KeyBinding.setKeyBindState(17,true);update(m,0,75);
        FeatureFixture.self.movementInput.jump=false;m.onMovementInput(new MovementInputEvent());
        check(FeatureFixture.self.movementInput.jump,"Keep-Y schedules jump after vanilla input refresh");
        FeatureFixture.self.onGround=false;FeatureFixture.self.movementInput.jump=false;m.onMovementInput(new MovementInputEvent());
        check(!FeatureFixture.self.movementInput.jump,"Keep-Y does not schedule an airborne jump");
        m.setEnabled(false);

        m=setup();FeatureFixture.self.inventory.currentItem=8;m.tower.setValue(1);
        FeatureFixture.self.setPosition(.5,65,.5);KeyBinding.setKeyBindState(57,true);
        MoveFlyingEvent move=new MoveFlyingEvent(0,0,.1f);m.onMoveFlying(move);
        near(FeatureFixture.self.motionY,-0.0784000015258789,"Vanilla tower initial settle");
        m.onMoveFlying(move);near(FeatureFixture.self.motionY,(double).42f,"Vanilla tower takeoff");
        FeatureFixture.self.posY=65.42;m.onMoveFlying(move);near(FeatureFixture.self.motionY,.75-.42,"Vanilla tower second phase");
        FeatureFixture.self.posY=65.75;m.onMoveFlying(move);near(FeatureFixture.self.motionY,.25,"Vanilla tower third phase");
        KeyBinding.setKeyBindState(57,false);m.onMoveFlying(move);check((Integer)field(m,"towerPhase")==0,"releasing jump resets tower");
        m.setEnabled(false);

        m=setup();FeatureFixture.self.motionX=.5;
        SafeWalkEvent safe=new SafeWalkEvent(false);m.onSafeWalk(safe);check(safe.isSafeWalk(),"edge request when predicted support is missing");
        FeatureFixture.self.onGround=false;safe=new SafeWalkEvent(false);m.onSafeWalk(safe);check(!safe.isSafeWalk(),"no airborne Safe Walk");
        FeatureFixture.self.onGround=true;FeatureFixture.self.motionY=.1;safe=new SafeWalkEvent(false);m.onSafeWalk(safe);check(!safe.isSafeWalk(),"no ascending Safe Walk");
        FeatureFixture.self.motionY=0;m.groundMotion.setValue(50);
        FeatureFixture.self.movementInput.moveForward=FeatureFixture.self.movementInput.moveStrafe=1;
        m.onLivingUpdate(new LivingUpdateEvent());
        near(FeatureFixture.self.movementInput.moveForward,.5/Math.sqrt(2),"ground-motion diagonal normalization");
        near(FeatureFixture.self.movementInput.moveStrafe,.5/Math.sqrt(2),"ground-motion strafe normalization");
        m.setEnabled(false);

        m=setup();FeatureFixture.self.setPosition(.5,65,.5);m.tower.setValue(3);KeyBinding.setKeyBindState(17,true);KeyBinding.setKeyBindState(57,true);
        update(m,0,75);check((Integer)field(m,"placementDelayTicks")==3,"Telly takeoff delays placement by three ticks");
        m.setEnabled(false);
        dev.rice.module.impl.ScaffoldModule adapter=new dev.rice.module.impl.ScaffoldModule();
        check(adapter.getSettings().size()==14,"exact Mayu setting surface, no Rise settings");
        check(adapter.findSetting("watchdog_telly")==null&&adapter.findSetting("same_y")==null,"retired Rise modes absent");
        System.out.println("Mayu Scaffold behavior: "+assertions+" assertions passed");
    }
}
