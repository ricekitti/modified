import dev.rice.client.*;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;

/** Executable readable Vk/vo/Wa branches; this does not establish missing-source parity. */
public final class ScaffoldWatchdogTest {
   static java.lang.reflect.Field f(Object o,String n)throws Exception{return ScaffoldTellyTest.field(o,n);}
   static Object call(Object o,String n,Class<?>[] t,Object...v)throws Exception{return ScaffoldTellyTest.call(o,n,t,v);}
   static void check(boolean v,String m){ScaffoldTellyTest.check(v,m);}
   static void choice(ScaffoldModule m,String k,int v){ScaffoldTellyTest.choice(m,k,v);}
   static double speed(){return Math.hypot(FeatureFixture.self.motionX,FeatureFixture.self.motionZ);}
   public static void main(String[] args)throws Exception{
      Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule m=new ScaffoldModule();
      mc.gameSettings.keyBindJump=new KeyBinding("watchdog.jump",57,"test");
      mc.gameSettings.keyBindSneak=new KeyBinding("watchdog.sneak",42,"test");
      mc.gameSettings.keyBindSprint=new KeyBinding("watchdog.sprint",29,"test");
      Object d=f(m,"driver").get(m),w=f(d,"watchdog").get(d),timing=f(d,"timing").get(d),timer=f(d,"timer").get(d);
      java.lang.reflect.Field context=NativeFeatureBridge.class.getDeclaredField("context");context.setAccessible(true);context.set(null,c);
      java.lang.reflect.Field modules=NativeFeatureBridge.class.getDeclaredField("modules");modules.setAccessible(true);modules.set(null,new dev.rice.module.Module[]{m,m,m,m,m});
      call(d,"onEnable",new Class<?>[]{ClientContext.class},c);
      choice(m,"sprint",ScaffoldModule.SPRINT_WATCHDOG_JUMP_VK);call(d,"activateSprint",new Class<?>[]{ClientContext.class,ScaffoldModule.class},c,m);
      check(f(w,"sprintActive").getBoolean(w)&&!f(w,"towerActive").getBoolean(w),"Vk enables its own state independently of Vi and tower");
      Class<?>[] motion={ClientContext.class,ScaffoldModule.class,d.getClass(),timing.getClass(),timer.getClass(),SemanticEvent.class};
      FeatureFixture.self.moveForward=1;FeatureFixture.self.rotationYaw=0;FeatureFixture.self.ticksExisted=12;
      FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.3;f(d,"groundTicks").setInt(d,3);
      SemanticEvent e=new SemanticEvent(32);e.active=true;e.cameraY=65;
      call(w,"sprintMotion",motion,c,m,d,timing,timer,e);
      check(Math.abs(speed()-.3*1.1225)<1e-12&&Math.abs(e.cameraY-65.001)<1e-10,"Vk axis scaling and vo packet-height offset use independent source predicates");
      check(f(d,"extraPlacements").getInt(d)==1,"Vk requests the parent extra placement pass");
      FeatureFixture.self.rotationYaw=45;FeatureFixture.self.motionX=-.3/Math.sqrt(2);FeatureFixture.self.motionZ=.3/Math.sqrt(2);
      call(w,"sprintMotion",motion,c,m,d,timing,timer,new SemanticEvent(32));
      check(Math.abs(speed()-.3*1.129)<1e-7,"Vk diagonal scaling differs from axis scaling");
      for(int amp=0;amp<2;amp++){
         FeatureFixture.self.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,100,amp));
         FeatureFixture.self.rotationYaw=0;FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.3;
         call(w,"sprintMotion",motion,c,m,d,timing,timer,new SemanticEvent(32));
         check(Math.abs(speed()-.3*(amp==0?1.098:1.1055))<1e-12,"Vk speed-potion tier "+amp+" uses its source multiplier");
         FeatureFixture.self.removePotionEffect(Potion.moveSpeed.id);
      }
      m.timingPacket(c,new S08PacketPlayerPosLook(),false);FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.3;
      call(w,"sprintMotion",motion,c,m,d,timing,timer,new SemanticEvent(32));
      check(Math.abs(speed()-.3)<1e-12,"A recent correction suppresses Vk ground acceleration");
      FeatureFixture.self.ticksExisted=13;
      check(!(Boolean)call(w,"jump",new Class<?>[]{ClientContext.class,d.getClass()},c,d),"vo cancels jumps at modulo one after three grounded ticks");
      FeatureFixture.self.ticksExisted=14;
      check((Boolean)call(w,"jump",new Class<?>[]{ClientContext.class,d.getClass()},c,d),"vo allows the adjacent tick's jump");
      choice(m,"same_y",ScaffoldModule.SAMEY_AUTO_JUMP);((BooleanSetting)m.findSetting("watchdog_jump_extra_blocks")).set(true);
      FeatureFixture.self.onGround=false;FeatureFixture.self.motionY=.2;FeatureFixture.self.prevPosY=66;f(d,"startY").setDouble(d,65);f(d,"airTicks").setInt(d,4);f(d,"groundTicks").setInt(d,0);f(w,"justEnabled").setBoolean(w,false);
      java.lang.reflect.Field layout=FeatureFixture.class.getDeclaredField("voidLayout");layout.setAccessible(true);layout.setInt(null,1);
      call(w,"preUpdate",new Class<?>[]{ClientContext.class,ScaffoldModule.class,d.getClass()},c,m,d);
      check(Math.abs(FeatureFixture.self.motionY-.01)<1e-12,"Vk airborne Auto Jump drag follows the child setting's known level predicate");
      FeatureFixture.self.motionY=.2;((BooleanSetting)m.findSetting("watchdog_jump_extra_blocks")).set(false);
      call(w,"preUpdate",new Class<?>[]{ClientContext.class,ScaffoldModule.class,d.getClass()},c,m,d);
      check(FeatureFixture.self.motionY==.2,"Default child value keeps the source level predicate true");
      call(w,"receive",new Class<?>[]{Object.class},new S23PacketBlockChange());
      check(f(w,"blockChanged").getBoolean(w)&&f(w,"blockChanges").getInt(w)==1,"Vk observes block-change packets in its own counters");
      FeatureFixture.self.onGround=true;FeatureFixture.self.rotationYaw=0;FeatureFixture.self.ticksExisted=15;f(d,"groundTicks").setInt(d,3);
      int sent=FeatureFixture.sentCount;call(w,"sprintDisable",new Class<?>[]{ClientContext.class,d.getClass(),boolean.class},c,d,true);
      check(FeatureFixture.sentCount==sent+1&&FeatureFixture.lastSent instanceof C03PacketPlayer,"Vk explicit disable replays one source ground packet on an axis tick");
      call(w,"sprintEnable",new Class<?>[]{ClientContext.class,ScaffoldModule.class,d.getClass()},c,m,d);
      FeatureFixture.self.rotationYaw=45;sent=FeatureFixture.sentCount;call(w,"sprintDisable",new Class<?>[]{ClientContext.class,d.getClass(),boolean.class},c,d,true);
      check(FeatureFixture.sentCount==sent+2&&!f(w,"sprintActive").getBoolean(w),"Vk diagonal disable replays two packets and clears its state");
      call(w,"sprintEnable",new Class<?>[]{ClientContext.class,ScaffoldModule.class,d.getClass()},c,m,d);FeatureFixture.failNetwork=true;
      try{call(d,"onDisable",new Class<?>[]{ClientContext.class,boolean.class},c,true);}finally{FeatureFixture.failNetwork=false;}
      check(!f(w,"sprintActive").getBoolean(w)&&f(d,"groundTicks").getInt(d)==0,"Failed cleanup packet still releases helper and parent state");

      choice(m,"sprint",ScaffoldModule.SPRINT_NORMAL);choice(m,"tower",ScaffoldModule.TOWER_WATCHDOG_WA);
      FeatureFixture.self.moveForward=1;FeatureFixture.self.rotationYaw=0;FeatureFixture.self.onGround=true;FeatureFixture.self.ticksExisted=20;
      KeyBinding.setKeyBindState(57,true);call(d,"activateSprint",new Class<?>[]{ClientContext.class,ScaffoldModule.class},c,m);
      Class<?>[] tower={ClientContext.class,d.getClass(),timing.getClass()};
      FeatureFixture.self.motionY=.1;call(w,"towerMotion",tower,c,d,timing);
      check(FeatureFixture.self.motionY==.42,"Wa stage zero uses the source double-precision launch");
      FeatureFixture.self.onGround=false;FeatureFixture.self.setPosition(.5,65.42,.5);call(w,"towerMotion",tower,c,d,timing);
      check(FeatureFixture.self.motionY==.33,"Wa stage one uses its independent ascent velocity");
      FeatureFixture.self.setPosition(.5,65.75,.5);call(w,"towerMotion",tower,c,d,timing);
      check(FeatureFixture.self.motionY==.25&&f(w,"jumpStage").getInt(w)==-1,"Wa stage two aligns to the next block and resets its stage");
      SemanticEvent input=new SemanticEvent(26);input.forward=.7f;call(w,"input",new Class<?>[]{ClientContext.class,SemanticEvent.class},c,input);
      check(input.forward==3.5f,"Wa input scales moving forward input by five");
      FeatureFixture.self.ticksExisted=30;FeatureFixture.self.onGround=true;FeatureFixture.self.motionY=.123;f(d,"groundTicks").setInt(d,3);
      call(w,"towerMotion",tower,c,d,timing);check(FeatureFixture.self.motionY==.123,"Wa skips the modulo-three settled-ground tick");
      FeatureFixture.self.onGround=false;FeatureFixture.self.ticksExisted=31;FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.1;
      KeyBinding.setKeyBindState(30,true);call(w,"strafe",new Class<?>[]{ClientContext.class,ScaffoldModule.class,d.getClass(),timing.getClass()},c,m,d,timing);
      check(Math.abs(speed()-.25)<1e-12,"Wa side-key strafe uses its own speed");
      KeyBinding.setKeyBindState(30,false);FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.1;
      call(w,"strafe",new Class<?>[]{ClientContext.class,ScaffoldModule.class,d.getClass(),timing.getClass()},c,m,d,timing);
      check(Math.abs(speed()-.19)<1e-12,"Wa minimum strafe speed observes the correction-age gate");
      call(w,"send",new Class<?>[]{C08PacketPlayerBlockPlacement.class},new C08PacketPlayerBlockPlacement(new BlockPos(0,64,0),1,new ItemStack(Blocks.stone),0,0,0));
      call(w,"send",new Class<?>[]{C08PacketPlayerBlockPlacement.class},new C08PacketPlayerBlockPlacement(new BlockPos(0,64,0),0,new ItemStack(Blocks.ice),0,0,0));
      check(f(w,"upwardPlacements").getInt(w)==1,"Wa ice packets leave the upward-placement counter unchanged");
      check((Boolean)call(w,"bindArmed",new Class<?>[]{ClientContext.class},c),"Held jump arms Wa's deferred bind action");
      call(w,"bindRequest",new Class<?>[0]);FeatureFixture.self.motionY=.2;call(w,"towerMotion",tower,c,d,timing);
      check((Boolean)call(w,"takeDisable",new Class<?>[0])&&!(Boolean)call(w,"takeDisable",new Class<?>[0]),"Wa consumes one deferred disable request in its source velocity window");
      FeatureFixture.self.moveForward=0;FeatureFixture.self.setPosition(.5,68,.5);FeatureFixture.self.onGround=false;FeatureFixture.self.rotationYaw=0;
      call(w,"towerEnable",new Class<?>[]{ClientContext.class},c);call(w,"towerMotion",tower,c,d,timing);
      check(Math.abs(FeatureFixture.self.posZ-(.5+.5/3))<1e-10,"Stationary Wa takes its first collision-tested alignment step");
      call(w,"towerMotion",tower,c,d,timing);
      check(call(d,"motionRotation",new Class<?>[0])!=null&&FeatureFixture.self.rotationYaw==0,"Wa rotation is scoped for packets and leaves camera yaw untouched");
      check(((Vec3)f(w,"offset").get(w)).zCoord==1,"Wa alignment supplies its separate placement offset");
      double x=FeatureFixture.self.posX,z=FeatureFixture.self.posZ;FeatureFixture.self.setPosition(x,64,z);
      check(!(Boolean)call(w,"fits",new Class<?>[]{ClientContext.class,double.class,double.class,double.class},c,x,64.0,z),"Wa alignment rejects a bounding volume occupied by a block");
      choice(m,"tower",ScaffoldModule.TOWER_DISABLED);call(d,"activateSprint",new Class<?>[]{ClientContext.class,ScaffoldModule.class},c,m);
      check(!f(w,"towerActive").getBoolean(w)&&!f(w,"pendingToggle").getBoolean(w),"Switching tower modes clears pending bind and alignment state");
      call(d,"onDisable",new Class<?>[]{ClientContext.class},c);
      check(!f(w,"sprintActive").getBoolean(w)&&!f(w,"towerActive").getBoolean(w)&&call(d,"motionRotation",new Class<?>[0])==null,"Disable clears both helpers and packet rotation ownership");
      System.out.println("ALL SCAFFOLD WATCHDOG CHECKS PASSED");
   }
}
