import dev.rice.client.*;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.module.NativeFeatureBridge;
import dev.rice.setting.*;
import java.lang.reflect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;

/** Recovered Telly branches and shared timer ownership using actual game objects. */
public final class ScaffoldTellyTest {
   static void check(boolean value,String message){if(!value)throw new AssertionError(message);System.out.println("ok   "+message);}
   static Field field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f;}
   static Object call(Object object,String name,Class<?>[] types,Object...values)throws Exception{Method m=object.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(object,values);}
   static void bool(ScaffoldModule m,String key,boolean value){((BooleanSetting)m.findSetting(key)).set(value);}
   static void choice(ScaffoldModule m,String key,int value){((ChoiceSetting)m.findSetting(key)).set(value);}
   static void range(ScaffoldModule m,String key,int value){((RangeSetting)m.findSetting(key)).set(new RangeSetting.Value(value,value));}
   public static void main(String[] args)throws Exception{
      Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule m=new ScaffoldModule();
      mc.gameSettings.keyBindJump=new KeyBinding("telly.jump",57,"test");mc.gameSettings.keyBindSneak=new KeyBinding("telly.sneak",42,"test");mc.gameSettings.keyBindSprint=new KeyBinding("telly.sprint",29,"test");
      Object d=field(m,"driver").get(m),t=field(d,"telly").get(d),timing=field(d,"timing").get(d);
      call(d,"onEnable",new Class<?>[]{ClientContext.class},c);call(d,"seedSettings",new Class<?>[]{ScaffoldModule.class},m);
      Class<?>[] effective={ClientContext.class,ScaffoldModule.class,timing.getClass()};
      choice(m,"mode",ScaffoldModule.MODE_TELLY);bool(m,"telly_only_on_right_click",true);
      check((Integer)call(t,"effectiveMode",effective,c,m,timing)==ScaffoldModule.MODE_NORMAL,"Right-click-only Telly uses the source Normal fallback while use is released");
      KeyBinding.setKeyBindState(-99,true);
      check((Integer)call(t,"effectiveMode",effective,c,m,timing)==ScaffoldModule.MODE_TELLY,"Held use key activates Telly without changing the selected mode");
      SemanticEvent click=new SemanticEvent(14);click.active=false;m.onEvent(c,click);
      check(click.cancelled,"Right-click-only source handler cancels non-strict manual clicks");
      choice(m,"ray_cast",ScaffoldModule.RAYCAST_STRICT);click=new SemanticEvent(14);click.active=false;m.onEvent(c,click);
      check(!click.cancelled,"Strict raycast retains manual right-click dispatch");
      bool(m,"watchdog_telly",true);FeatureFixture.self.ticksExisted=100;m.timingPacket(c,new S08PacketPlayerPosLook(),false);
      FeatureFixture.self.ticksExisted=101;
      check((Integer)call(t,"effectiveMode",effective,c,m,timing)==ScaffoldModule.MODE_NORMAL,"Disable On Flag selects source Normal fallback one tick after correction");
      for(int n=0;n<12;n++)call(t,"effectiveMode",effective,c,m,timing);
      check(field(t,"flagDisableTicks").getInt(t)==10,"Repeated callbacks cannot rearm or consume the ten-placement flag counter");
      SemanticEvent packet=new SemanticEvent(3);packet.packet=new C08PacketPlayerBlockPlacement((ItemStack)null);m.onEvent(c,packet);
      check(field(t,"flagDisableTicks").getInt(t)==10,"Use-item packets do not consume flag recovery placements");
      FeatureFixture.self.ticksExisted=102;packet.packet=new C08PacketPlayerBlockPlacement(new BlockPos(0,64,0),1,new ItemStack(Blocks.stone),0,0,0);
      for(int n=0;n<9;n++)m.onEvent(c,packet);
      check((Integer)call(t,"effectiveMode",effective,c,m,timing)==ScaffoldModule.MODE_NORMAL,"Flag fallback lasts through nine eligible block-placement packets");
      m.onEvent(c,packet);check((Integer)call(t,"effectiveMode",effective,c,m,timing)==ScaffoldModule.MODE_TELLY,"Tenth eligible placement restores selected Telly behavior");
      FeatureFixture.self.ticksExisted=103;m.timingPacket(c,new S08PacketPlayerPosLook(),false);FeatureFixture.self.ticksExisted=104;call(t,"effectiveMode",effective,c,m,timing);
      bool(m,"disable_on_flag",false);check((Integer)call(t,"effectiveMode",effective,c,m,timing)==ScaffoldModule.MODE_TELLY&&field(t,"flagDisableTicks").getInt(t)==0,"Turning flag response off clears its recovery state");

      Field layout=FeatureFixture.class.getDeclaredField("voidLayout");layout.setAccessible(true);layout.setInt(null,1);
      Class<?> faceType=Class.forName("dev.rice.module.impl.ScaffoldGeometry$EnumFacingOffset");Constructor<?> ctor=faceType.getDeclaredConstructor(EnumFacing.class,Vec3.class);ctor.setAccessible(true);
      Object face=ctor.newInstance(EnumFacing.UP,new Vec3(0,1,0));
      field(d,"blockFace").set(d,new BlockPos(0,64,0));field(d,"enumFacing").set(d,face);field(d,"targetBlock").set(d,new Vec3(0,64,0));
      FeatureFixture.self.setPosition(0,65,0);FeatureFixture.self.onGround=true;FeatureFixture.self.moveForward=1;FeatureFixture.self.rotationYaw=20;FeatureFixture.self.motionZ=.3;
      range(m,"watchdog_telly_jump_delay",3);field(d,"groundTicks").setInt(d,3);field(d,"tellyJumpDelay").setInt(d,3);
      Class<?>[] rotate={ClientContext.class,ScaffoldModule.class,d.getClass(),timing.getClass(),faceType};
      call(t,"rotate",rotate,c,m,d,timing,face);
      check(Math.abs(field(d,"targetYaw").getFloat(d)-20)<.001&&field(d,"targetPitch").getFloat(d)>=85&&field(d,"targetPitch").getFloat(d)<85.5,"Delayed Telly jump uses movement heading and source pitch window");
      check(field(d,"remapMovement").getBoolean(d),"Telly ascent requests source movement correction");
      FeatureFixture.self.rotationYaw=45;KeyBinding.setKeyBindState(57,true);field(d,"groundTicks").setInt(d,1);field(t,"boostPending").setBoolean(t,false);
      call(t,"rotate",rotate,c,m,d,timing,face);
      check(!field(t,"boostPending").getBoolean(t),"Block Diagonal Ascend excludes the diagonal held-jump ascent branch");
      bool(m,"block_diagonal_ascend",false);call(t,"rotate",rotate,c,m,d,timing,face);
      check(field(t,"boostPending").getBoolean(t)&&Math.abs(field(d,"targetYaw").getFloat(d)-45)<1,"Disabling Block Diagonal Ascend allows the diagonal ascent branch");
      FeatureFixture.self.onGround=false;FeatureFixture.self.rotationYaw=0;KeyBinding.setKeyBindState(57,false);field(d,"airTicks").setInt(d,7);field(d,"tellyTicks").setInt(d,20);
      field(d,"appliedYaw").setFloat(d,0);field(d,"appliedPitch").setFloat(d,0);field(d,"targetYaw").setFloat(d,0);field(d,"targetPitch").setFloat(d,0);
      bool(m,"rotation_block_boost",true);range(m,"rotation_block_rotation_speed",20);range(m,"watchdog_telly_rotation_speed",3);
      call(t,"rotate",rotate,c,m,d,timing,face);
      check(Math.abs(field(d,"targetPitch").getFloat(d)-20)<.001&&!field(t,"boostPending").getBoolean(t),"Airborne rotation boost uses its own range once and consumes the pending boost");
      field(d,"targetPitch").setFloat(d,0);field(t,"boostPending").setBoolean(t,true);bool(m,"boost_only_while_holding_jump",true);
      call(t,"rotate",rotate,c,m,d,timing,face);
      check(Math.abs(field(d,"targetPitch").getFloat(d)-3)<.001&&field(t,"boostPending").getBoolean(t),"Held-jump-only boost retains pending state and uses normal rotation speed when released");
      FeatureFixture.self.moveForward=0;call(t,"rotate",rotate,c,m,d,timing,face);
      check(((ChoiceSetting)m.findSetting("ray_cast")).index()==ScaffoldModule.RAYCAST_OFF,"Forced Telly raycast follows source no-movement predicate");
      bool(m,"dont_force_raycast_on_watchdog_telly",true);call(t,"effectiveMode",effective,c,m,timing);
      check(((ChoiceSetting)m.findSetting("ray_cast")).index()==ScaffoldModule.RAYCAST_STRICT,"Disabling forced raycast restores the still-owned original setting");
      bool(m,"dont_force_raycast_on_watchdog_telly",false);call(t,"rotate",rotate,c,m,d,timing,face);choice(m,"ray_cast",ScaffoldModule.RAYCAST_NORMAL);call(t,"clear",new Class<?>[0]);
      check(((ChoiceSetting)m.findSetting("ray_cast")).index()==ScaffoldModule.RAYCAST_NORMAL,"Telly cleanup preserves a newer external raycast choice");

      Field context=NativeFeatureBridge.class.getDeclaredField("context");context.setAccessible(true);context.set(null,c);
      Field modules=NativeFeatureBridge.class.getDeclaredField("modules");modules.setAccessible(true);modules.set(null,new dev.rice.module.Module[]{m,m,m,m,m});
      Method enabled=dev.rice.module.Module.class.getDeclaredMethod("setEnabledInternal",boolean.class,ClientContext.class);enabled.setAccessible(true);enabled.invoke(m,true,c);
      bool(m,"telly_only_on_right_click",false);bool(m,"watchdog_telly",true);bool(m,"visual_back_rotations",true);bool(m,"disable_on_flag",false);
      field(d,"blockFace").set(d,new BlockPos(0,64,0));field(d,"enumFacing").set(d,face);field(d,"targetBlock").set(d,new Vec3(0,64,0));
      field(d,"appliedYaw").setFloat(d,180);field(d,"appliedPitch").setFloat(d,85);FeatureFixture.self.moveForward=1;FeatureFixture.self.rotationYaw=0;FeatureFixture.self.rotationPitch=12;
      mc.gameSettings.thirdPersonView=1;m.onEvent(c,new SemanticEvent(33));
      float[] pose=NativeFeatureBridge.scaffoldVisual(FeatureFixture.self,null);
      check(pose!=null&&FeatureFixture.self.rotationYaw==0&&FeatureFixture.self.rotationPitch!=12,"Telly visual pose changes model pitch and head/body yaw only inside the renderer scope");
      check(FeatureFixture.self.rotationYawHead==FeatureFixture.self.renderYawOffset,"Telly model head and body share the source visual yaw");
      FeatureFixture.self.renderYawOffset=77;NativeFeatureBridge.scaffoldVisual(FeatureFixture.self,pose);
      check(FeatureFixture.self.rotationPitch==12&&FeatureFixture.self.rotationYawHead==pose[1]&&FeatureFixture.self.renderYawOffset==77,"Visual cleanup restores owned fields and preserves a newer body-yaw writer");
      check(NativeFeatureBridge.scaffoldVisual(FeatureFixture.enemy,null)==null,"Telly visual pose never affects another player");
      mc.gameSettings.thirdPersonView=0;check(NativeFeatureBridge.scaffoldVisual(FeatureFixture.self,null)==null,"First-person view leaves camera pitch untouched");
      mc.gameSettings.thirdPersonView=1;bool(m,"visual_back_rotations",false);m.onEvent(c,new SemanticEvent(33));
      check(NativeFeatureBridge.scaffoldVisual(FeatureFixture.self,null)==null,"Disabling Visual Back Rotations clears its model pose");

      FeatureFixture.self.onGround=true;FeatureFixture.self.rotationYaw=0;FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.1;
      field(d,"targetYaw").setFloat(d,0);field(d,"targetPitch").setFloat(d,0);field(d,"appliedYaw").setFloat(d,0);field(d,"appliedPitch").setFloat(d,0);
      call(d,"applyNewWatchdogRotations",new Class<?>[]{ClientContext.class,ScaffoldModule.class},c,m);
      check(field(d,"targetYaw").getFloat(d)==-12&&Math.abs(field(d,"targetPitch").getFloat(d))<3,
            "New Watchdog Rotations preserves the source block-centre side vector and twelve-degree target step");
      check(field(d,"rotationActive").getBoolean(d)&&field(d,"appliedYaw").getFloat(d)<-9.9,
            "New Watchdog Rotations applies the slow-movement thirty-degree server offset");
      field(d,"targetYaw").setFloat(d,-90);field(d,"appliedYaw").setFloat(d,-120);field(d,"targetPitch").setFloat(d,0);field(d,"appliedPitch").setFloat(d,0);
      KeyBinding.setKeyBindState(57,true);call(d,"applyNewWatchdogRotations",new Class<?>[]{ClientContext.class,ScaffoldModule.class},c,m);
      check(field(d,"appliedYaw").getFloat(d)>-110.1&&field(d,"appliedYaw").getFloat(d)<-109.9,
            "Held jump switches the new rotation branch to its ten-degree server offset");
      KeyBinding.setKeyBindState(57,false);KeyBinding.setKeyBindState(30,true);field(d,"rotationActive").setBoolean(d,false);
      call(d,"applyNewWatchdogRotations",new Class<?>[]{ClientContext.class,ScaffoldModule.class},c,m);
      check(!field(d,"rotationActive").getBoolean(d),"Sideways input excludes the new rotation override in source order");
      KeyBinding.setKeyBindState(30,false);KeyBinding.setKeyBindState(17,true);choice(m,"yaw_offset",1);bool(m,"movement_correction",false);
      FeatureFixture.self.motionX=FeatureFixture.self.motionZ=0;m.onEvent(c,new SemanticEvent(21));
      check(Math.abs(FeatureFixture.self.motionZ-.0026000750109401644)<1e-12,"Nonzero yaw offset adds the source one-key diagonal speed adjustment");
      bool(m,"movement_correction",true);FeatureFixture.self.motionZ=0;m.onEvent(c,new SemanticEvent(21));
      check(FeatureFixture.self.motionZ==0,"Movement correction suppresses the separate diagonal speed adjustment");
      bool(m,"movement_correction",false);choice(m,"sprint",ScaffoldModule.SPRINT_WATCHDOG_JUMP_VI);FeatureFixture.self.onGround=false;FeatureFixture.self.motionY=.8;FeatureFixture.self.motionZ=0;m.onEvent(c,new SemanticEvent(21));
      check(FeatureFixture.self.motionZ==0,"Watchdog Jump retains its independent movement behavior with a yaw offset");
      choice(m,"sprint",ScaffoldModule.SPRINT_NORMAL);choice(m,"yaw_offset",0);KeyBinding.setKeyBindState(17,false);

      FeatureFixture.self.onGround=true;

      Object timer=field(d,"timer").get(d);Field clockField=Minecraft.class.getDeclaredField(FeatureFixture.mappedName(Minecraft.class,"timer"));clockField.setAccessible(true);Timer clock=(Timer)clockField.get(mc);
      Class<?>[] timerArgs={ClientContext.class,float.class};
      call(timer,"apply",timerArgs,c,1.7f);check(clock.timerSpeed==1.7f,"Scaffold timer applies an explicit non-default speed");
      call(timer,"release",new Class<?>[0]);check(clock.timerSpeed==1,"Scaffold timer cleanup restores its owned baseline");
      clock.timerSpeed=1.4f;call(timer,"apply",timerArgs,c,2f);check(clock.timerSpeed==1.4f,"Scaffold timer leaves an existing foreign speed untouched");
      clock.timerSpeed=1;call(timer,"apply",timerArgs,c,2f);clock.timerSpeed=.7f;call(timer,"apply",timerArgs,c,2f);clock.timerSpeed=1;call(timer,"apply",timerArgs,c,2f);
      check(clock.timerSpeed==1,"A newer timer writer makes Scaffold yield until its claim is released");
      call(timer,"release",new Class<?>[0]);call(timer,"apply",timerArgs,c,2f);clock.timerSpeed=.8f;call(timer,"release",new Class<?>[0]);
      check(clock.timerSpeed==.8f,"Timer cleanup preserves a newer external timer change");
      clock.timerSpeed=1;call(timer,"apply",timerArgs,c,2f);m.onWorldUnload(c);check(clock.timerSpeed==1,"World unload releases Scaffold timer state");
      field(d,"blockFace").set(d,new BlockPos(0,64,0));field(d,"enumFacing").set(d,face);field(d,"targetBlock").set(d,new Vec3(0,64,0));
      ((NumberSetting)m.findSetting("timer")).set(1.7);m.onEvent(c,new SemanticEvent(32));
      check(clock.timerSpeed==1.7f,"Real PreMotion dispatch applies configured timer only with a complete placement target");
      mc.currentScreen=new net.minecraft.client.gui.GuiChat();m.onEvent(c,new SemanticEvent(32));
      check(clock.timerSpeed==1,"Opening a screen releases the active timer claim");
      mc.currentScreen=null;field(d,"blockFace").set(d,null);m.onEvent(c,new SemanticEvent(32));
      check(clock.timerSpeed==1,"Missing placement geometry does not claim the timer");
      System.out.println("ALL SCAFFOLD TELLY CHECKS PASSED");
   }
}
