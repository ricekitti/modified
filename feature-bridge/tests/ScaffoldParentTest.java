import dev.rice.client.*;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.setting.*;
import java.lang.reflect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.*;

/** Readable parent behavior, including branch ordering and temporary state lifetimes. */
public final class ScaffoldParentTest {
   static void check(boolean v,String s){ScaffoldTellyTest.check(v,s);}
   static Field f(Object o,String n)throws Exception{return ScaffoldTellyTest.field(o,n);}
   static Object call(Object o,String n,Class<?>[] t,Object...v)throws Exception{return ScaffoldTellyTest.call(o,n,t,v);}
   static void choice(ScaffoldModule m,String key,int value){ScaffoldTellyTest.choice(m,key,value);}
   static void rotation(float[] r,double yaw,double speed,String message){check(r!=null&&Math.abs(MathHelper.wrapAngleTo180_double(r[0]-yaw))<=1.51&&r[1]>=85&&r[1]<86&&r[2]==speed,message);}
   static int placements;
   public static final class Renderer extends EntityRenderer{
      public Renderer(){super(null,null);}
      @Override public void getMouseOver(float partial){}
   }
   public static final class Controller extends PlayerControllerMP{
      public Controller(){super(null,null);}
      @Override public boolean onPlayerRightClick(EntityPlayerSP p,WorldClient w,ItemStack stack,BlockPos pos,EnumFacing side,Vec3 hit){placements++;return false;}
   }
   public static void main(String[] args)throws Exception{
      Minecraft mc=FeatureFixture.create();ClientContext c=new ClientContext(mc);ScaffoldModule m=new ScaffoldModule();
      mc.gameSettings.keyBindJump=new KeyBinding("parent.jump",57,"test");mc.gameSettings.keyBindSneak=new KeyBinding("parent.sneak",42,"test");mc.gameSettings.keyBindSprint=new KeyBinding("parent.sprint",29,"test");mc.gameSettings.keyBindPickBlock=new KeyBinding("parent.pick",-98,"test");
      Object d=f(m,"driver").get(m);call(d,"onEnable",new Class<?>[]{ClientContext.class},c);
      Class<?>[] parent={ClientContext.class,ScaffoldModule.class};
      choice(m,"yaw_offset",1);call(d,"seedSettings",new Class<?>[]{ScaffoldModule.class},m);
      check(f(d,"targetYaw").getFloat(d)==-135&&f(d,"targetPitch").getFloat(d)==90,"Enable seeds the source backward target with the chosen yaw offset");choice(m,"yaw_offset",0);
      FeatureFixture.self.moveForward=1;FeatureFixture.self.motionZ=.3;FeatureFixture.self.rotationYaw=0;f(d,"targetYaw").setFloat(d,180);f(d,"groundTicks").setInt(d,3);
      choice(m,"sprint",ScaffoldModule.SPRINT_NORMAL);
      check(call(d,"parentWatchdogRotation",parent,c,m)==null,"Normal sprint does not inherit Watchdog parent rotations");
      choice(m,"sprint",ScaffoldModule.SPRINT_WATCHDOG_JUMP_VK);
      rotation((float[])call(d,"parentWatchdogRotation",parent,c,m),-100,10,"Watchdog early ground ticks use the parent ten-degree rotation request");
      check(FeatureFixture.self.rotationYaw==0,"Parent requests keep the visible camera separate");
      Object timing=f(d,"timing").get(d);m.timingPacket(c,new net.minecraft.network.play.server.S08PacketPlayerPosLook(),false);
      call(d,"parentWatchdogRotation",parent,c,m);check(FeatureFixture.self.motionX==0&&FeatureFixture.self.motionZ==0,"Recent correction stops horizontal movement in the parent ground gate");
      FeatureFixture.self.ticksExisted=50;FeatureFixture.self.motionZ=.3;f(d,"groundTicks").setInt(d,11);
      rotation((float[])call(d,"parentWatchdogRotation",parent,c,m),-140,2,"Settled Watchdog uses its two-degree source request");
      check(Math.abs(FeatureFixture.self.motionZ-.3*.9895)<1e-12,"Settled Watchdog applies the parent's horizontal multiplier");
      KeyBinding.setKeyBindState(30,true);KeyBinding.setKeyBindState(31,true);
      rotation((float[])call(d,"parentWatchdogRotation",parent,c,m),190,2,"Later left/back predicate wins over the earlier left-key rotation");
      KeyBinding.setKeyBindState(30,false);KeyBinding.setKeyBindState(31,false);KeyBinding.setKeyBindState(17,true);KeyBinding.setKeyBindState(32,true);FeatureFixture.self.rotationYaw=45;
      rotation((float[])call(d,"parentWatchdogRotation",parent,c,m),185,2,"Diagonal forward/right predicate retains the last source rotation request");
      KeyBinding.setKeyBindState(17,false);KeyBinding.setKeyBindState(32,false);KeyBinding.setKeyBindState(57,true);FeatureFixture.self.moveForward=0;
      rotation((float[])call(d,"parentWatchdogRotation",parent,c,m),-105,10,"Stationary held jump overrides earlier parent rotation branches");
      KeyBinding.setKeyBindState(42,true);FeatureFixture.self.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,100,0));FeatureFixture.self.motionX=.4;FeatureFixture.self.motionZ=.2;
      f(d,"groundTicks").setInt(d,1);
      call(d,"parentWatchdogRotation",parent,c,m);check(Math.abs(FeatureFixture.self.motionX-.3)<1e-12&&Math.abs(FeatureFixture.self.motionZ-.15)<1e-12,"Watchdog potion plus sneak applies its independent parent slowdown");
      FeatureFixture.self.removePotionEffect(Potion.moveSpeed.id);KeyBinding.setKeyBindState(42,false);KeyBinding.setKeyBindState(57,false);
      choice(m,"sprint",ScaffoldModule.SPRINT_NORMAL);SemanticEvent input=new SemanticEvent(34);input.forward=.7f;input.sideways=-.2f;
      call(d,"activateSprint",parent,c,m);f(d,"blockInputTicks").setInt(d,2);m.onEvent(c,input);
      check(input.forward==0&&input.sideways==0&&f(d,"inputForward").getFloat(d)==.7f&&f(d,"inputStrafe").getFloat(d)==-.2f,"Input blocking preserves the original input for rotation heading");
      input=new SemanticEvent(34);input.forward=1;m.onEvent(c,input);check(input.forward==0,"Input blocking lasts for the second requested pass");
      input=new SemanticEvent(34);input.forward=1;m.onEvent(c,input);check(input.forward==1,"Input resumes after the requested block count");
      f(d,"pauseTicks").setInt(d,2);f(d,"sneakingTicks").setInt(d,-1);call(d,"calculateSneaking",parent,c,m);
      check(mc.gameSettings.keyBindSneak.isKeyDown()&&f(d,"pauseTicks").getInt(d)==1&&f(d,"blocksUntilSneak").getInt(d)==0,"Pause forces owned sneak even when Sneak is disabled");
      call(d,"calculateSneaking",parent,c,m);call(d,"calculateSneaking",parent,c,m);
      check(!mc.gameSettings.keyBindSneak.isKeyDown()&&f(d,"pauseTicks").getInt(d)==0,"Pause expires and the source scheduler releases sneak");
      mc.playerController=FeatureFixture.blank(Controller.class);mc.entityRenderer=FeatureFixture.blank(Renderer.class);
      FeatureFixture.put(mc.playerController,"mc",mc);FeatureFixture.put(mc.playerController,"netClientHandler",FeatureFixture.self.sendQueue);
      FeatureFixture.put(mc.playerController,"currentGameType",net.minecraft.world.WorldSettings.GameType.SURVIVAL);
      FeatureFixture.self.inventory.mainInventory[0]=new ItemStack(Blocks.stone,64);call(d,"selectSlot",new Class<?>[]{ClientContext.class},c);
      Class<?> faceType=Class.forName("dev.rice.module.impl.ScaffoldGeometry$EnumFacingOffset");Constructor<?> ctor=faceType.getDeclaredConstructor(EnumFacing.class,Vec3.class);ctor.setAccessible(true);Object face=ctor.newInstance(EnumFacing.UP,new Vec3(0,1,0));
      f(d,"blockFace").set(d,new BlockPos(0,64,0));choice(m,"ray_cast",ScaffoldModule.RAYCAST_OFF);f(d,"pauseTicks").setInt(d,4);
      call(d,"placeBlock",new Class<?>[]{ClientContext.class,ScaffoldModule.class,faceType},c,m,face);check(placements==0,"Long pause suppresses placement before controller or packet work");
      f(d,"pauseTicks").setInt(d,3);call(d,"placeBlock",new Class<?>[]{ClientContext.class,ScaffoldModule.class,faceType},c,m,face);check(placements==1,"Three-tick pause retains the source placement threshold");
      Field layout=FeatureFixture.class.getDeclaredField("voidLayout");layout.setAccessible(true);layout.setInt(null,1);
      FeatureFixture.self.setPosition(.5,65,-.1);FeatureFixture.self.onGround=true;FeatureFixture.self.moveForward=1;FeatureFixture.self.motionY=0;f(d,"groundTicks").setInt(d,3);f(d,"startY").setDouble(d,64);f(d,"pauseTicks").setInt(d,0);f(d,"ticksOnAir").setInt(d,5);
      m.onEvent(c,new SemanticEvent(17));check(placements>1,"Real parent placement loop reaches the controller with eligible blocks and geometry");
      FeatureFixture.self.setPosition(.5,65,.5);f(d,"groundTicks").setInt(d,3);f(d,"startY").setDouble(d,64);m.onEvent(c,new SemanticEvent(17));
      check(f(d,"startY").getDouble(d)==65,"Settled grounded support resynchronizes start height while moving");
      choice(m,"sprint",ScaffoldModule.SPRINT_WATCHDOG_JUMP_VK);choice(m,"rotation_mode",ScaffoldModule.ROT_GRIM);((BooleanSetting)m.findSetting("new_watchdog_rots")).set(false);
      KeyBinding.setKeyBindState(-98,true);f(d,"groundTicks").setInt(d,3);f(d,"targetYaw").setFloat(d,180);f(d,"appliedPitch").setFloat(d,0);f(d,"pauseTicks").setInt(d,10);FeatureFixture.self.motionX=0;FeatureFixture.self.motionZ=.3;
      m.onEvent(c,new SemanticEvent(17));check(f(d,"rotationActive").getBoolean(d)&&f(d,"appliedPitch").getFloat(d)>0&&FeatureFixture.self.rotationPitch==0,"Early Watchdog rotation survives when Grim suppresses final rotation application");KeyBinding.setKeyBindState(-98,false);
      f(d,"pauseTicks").setInt(d,9);f(d,"blockInputTicks").setInt(d,7);call(d,"onDisable",new Class<?>[]{ClientContext.class},c);
      check(f(d,"pauseTicks").getInt(d)==0&&f(d,"blockInputTicks").getInt(d)==0,"Disable releases pause and blocked-input state");
      System.out.println("ALL SCAFFOLD PARENT CHECKS PASSED");
   }
}
