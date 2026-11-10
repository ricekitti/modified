import dev.rice.client.*;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.setting.*;
import java.lang.reflect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Vec3;

/** Real collision-box probes and source one-use request/axis order, including airborne movement. */
public final class ScaffoldSafeWalkTest {
   static void check(boolean value,String message){if(!value)throw new AssertionError(message);System.out.println("ok   "+message);}
   static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
   static void prepare(Object walk,ClientContext c,ScaffoldModule module)throws Exception{
      Method method=walk.getClass().getDeclaredMethod("prepare",ClientContext.class,ScaffoldModule.class,Vec3.class);method.setAccessible(true);method.invoke(walk,c,module,new Vec3(0,0,0));
   }
   public static void main(String[] args)throws Exception{
      Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule module=new ScaffoldModule();
      mc.gameSettings.keyBindSneak=new KeyBinding("walk.sneak",42,"test");mc.gameSettings.keyBindJump=new KeyBinding("walk.jump",57,"test");mc.gameSettings.keyBindSprint=new KeyBinding("walk.sprint",29,"test");
      Method toggle=dev.rice.module.Module.class.getDeclaredMethod("setEnabledInternal",boolean.class,ClientContext.class);toggle.setAccessible(true);toggle.invoke(module,true,c);
      Object driver=field(module,"driver"),walk=field(driver,"safeWalk");FeatureFixture.safeWalkLayout=1;
      // Unsafe fixture allocation skips Entity's constructor dimensions.
      FeatureFixture.self.width=.6f;FeatureFixture.self.height=1.8f;
      FeatureFixture.self.setPosition(.9,65,0);prepare(walk,c,module);
      Object originalBox=FeatureFixture.self.getEntityBoundingBox();FeatureFixture.self.motionX=.5;
      double[] move=module.clipMove(c,FeatureFixture.self,.5,.125,0);
      check(Math.abs(move[0]-.4)<1e-12&&move[1]==.125&&move[2]==0,"Safe Walk reduces positive X in source 0.05 steps and preserves vertical movement");
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"Edge request is consumed by one moveEntity call");
      check(FeatureFixture.self.posX==.9&&FeatureFixture.self.getEntityBoundingBox()==originalBox&&FeatureFixture.self.motionX==.5,
            "Clipping probes preserve client position, bounding box identity and another movement owner's velocity");
      FeatureFixture.self.setPosition(-.9,65,0);prepare(walk,c,module);move=module.clipMove(c,FeatureFixture.self,-.5,-.2,0);
      check(Math.abs(move[0]+.4)<1e-12&&move[1]==-.2,"Negative X clipping preserves the source symmetric decrement rule");
      FeatureFixture.self.setPosition(0,65,.9);prepare(walk,c,module);move=module.clipMove(c,FeatureFixture.self,0,0,.5);
      check(Math.abs(move[2]-.4)<1e-12,"Z support is checked independently before the combined diagonal probe");
      FeatureFixture.safeWalkLayout=2;FeatureFixture.self.setPosition(.9,65,.9);prepare(walk,c,module);
      move=module.clipMove(c,FeatureFixture.self,.6,0,.6);
      check(Math.abs(move[0]-.4)<1e-12&&Math.abs(move[0]-move[2])<1e-12,"Combined diagonal edge probe reduces both axes when each separate axis has support");
      FeatureFixture.safeWalkLayout=1;FeatureFixture.self.onGround=false;FeatureFixture.self.setPosition(.9,65,0);prepare(walk,c,module);
      check(Math.abs(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]-.4)<1e-12,"Source edge flag applies in air independently of vanilla grounded-sneak clipping");
      FeatureFixture.self.setPosition(0,65,0);prepare(walk,c,module);
      check(module.clipMove(c,FeatureFixture.self,.2,.3,.2)[0]==.2,"Supported horizontal movement is unchanged");
      FeatureFixture.safeWalkLayout=0;prepare(walk,c,module);move=module.clipMove(c,FeatureFixture.self,.04,-.1,-.04);
      check(move[0]==0&&move[2]==0&&move[1]==-.1,"Unsupported sub-step movement clamps both signs to zero");
      prepare(walk,c,module);move=module.clipMove(c,FeatureFixture.enemy,.5,0,0);
      check(move[0]==.5&&module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==0,"Other entities neither receive nor consume the local edge request");
      FeatureFixture.safeWalkDeepBlock=true;prepare(walk,c,module);
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"A solid block two below prevents a new source request");FeatureFixture.safeWalkDeepBlock=false;
      KeyBinding.setKeyBindState(42,true);prepare(walk,c,module);
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"Held sneak leaves vanilla input to own edge behavior");KeyBinding.setKeyBindState(42,false);
      prepare(walk,c,module);FeatureFixture.self.noClip=true;
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"No-clip movement remains independent of edge protection");FeatureFixture.self.noClip=false;
      prepare(walk,c,module);check(Double.isNaN(module.clipMove(c,FeatureFixture.self,Double.NaN,0,0)[0]),"Invalid movement cannot enter an unbounded clipping loop");
      prepare(walk,c,module);((ChoiceSetting)module.findSetting("downwards")).set(ScaffoldModule.DOWN_WATCHDOG_VC);
      Field full=driver.getClass().getDeclaredField("fullBlockTicks");full.setAccessible(true);full.setInt(driver,5);
      FeatureFixture.self.ticksExisted=2;FeatureFixture.self.rotationYaw=0;KeyBinding.setKeyBindState(42,true);
      Method downward=driver.getClass().getDeclaredMethod("applyWatchdogDownwards",ClientContext.class,ScaffoldModule.class);downward.setAccessible(true);downward.invoke(driver,c,module);
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"Watchdog downward placement explicitly releases the parent edge request");
      KeyBinding.setKeyBindState(42,false);((ChoiceSetting)module.findSetting("downwards")).set(ScaffoldModule.DOWN_OFF);
      prepare(walk,c,module);((BooleanSetting)module.findSetting("safe_walk")).set(false);
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"Turning Safe Walk off releases its pending request");((BooleanSetting)module.findSetting("safe_walk")).set(true);
      prepare(walk,c,module);mc.currentScreen=new net.minecraft.client.gui.GuiChat();module.onEvent(c,new SemanticEvent(17));mc.currentScreen=null;
      check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"Menu pause releases the request before the next movement");
      prepare(walk,c,module);module.onWorldUnload(c);check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"World unload releases Safe Walk ownership");
      prepare(walk,c,module);toggle.invoke(module,false,c);check(module.clipMove(c,FeatureFixture.self,.5,0,0)[0]==.5,"Disabled Scaffold cannot change movement");
      System.out.println("ALL SCAFFOLD SAFE WALK CHECKS PASSED");
   }
}
