import dev.rice.client.hook.GameHooks;
import dev.rice.client.rotation.RotationService;
import dev.rice.client.scaffold.YuriScaffoldHost;
import dev.rice.client.scaffold.yuri.*;
import dev.rice.module.impl.ScaffoldModule;
import dev.rice.setting.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.client.settings.KeyBinding;
import java.lang.reflect.*;
import java.nio.file.*;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
public final class YuriScaffoldCheck {
 static void check(boolean v,String message){V115Check.check(v,message);}
 static void mode(ScaffoldModule s,String key,int value){((ChoiceSetting)s.findSetting(key)).set(value);s.settingVisible(key);}
 static void flag(ScaffoldModule s,String key,boolean value){((BooleanSetting)s.findSetting(key)).set(value);s.settingVisible(key);}
 static void begin(ScaffoldModule s){
  V115Check.manager.applyNow(s,false);RotationService.reset(V115Check.c);
  V115Check.world.blocks.clear();V115Check.world.blocks.put(new BlockPos(0,63,0),Blocks.stone.getDefaultState());
  V115Check.place(V115Check.self,1.1,64,.5);V115Check.self.onGround=true;
  V115Check.self.motionX=.25;V115Check.self.motionY=0;V115Check.self.motionZ=0;
  V115Check.self.rotationYaw=37;V115Check.self.rotationPitch=11;
  V115Check.self.inventory.currentItem=0;V115Check.self.inventory.setInventorySlotContents(2,new ItemStack(Blocks.stone,64));
  V115Check.mc.inGameHasFocus=true;V115Check.mc.currentScreen=null;
  V115Input.clear();KeyBinding.unPressAllKeys();V115Input.keys[17]=true;KeyBinding.setKeyBindState(17,true);
  V115Check.self.movementInput.moveForward=1;V115Check.self.movementInput.moveStrafe=0;
  V115Check.manager.applyNow(s,true);RotationService.beginFrame(V115Check.c,0,0);
 }
 static void frame(){
  RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(V115Check.world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  GameHooks.afterMovementInput(V115Check.self);GameHooks.yuriScaffoldLivingUpdate();GameHooks.beforeMoveFlying(V115Check.self,V115Check.self.movementInput.moveStrafe,V115Check.self.movementInput.moveForward,.1F);GameHooks.yuriScaffoldRiding(V115Check.self,V115Check.self);GameHooks.walkingPlayerHead(V115Check.self);GameHooks.yuriScaffoldPlayerUpdateReturn(V115Check.self);GameHooks.afterEntities(V115Check.world);
 }
 static final class ProbeLoader extends ClassLoader {
  ProbeLoader(){super(YuriScaffoldCheck.class.getClassLoader());}
  Class<?> define(byte[] bytes){return defineClass("V172MovementProbe",bytes,0,bytes.length);}
 }
 static Method patchedMovement(Path root)throws Exception{
  dev.rice.inject.Mappings.load(null);
  ClassNode vanilla=new ClassNode();
  try(java.util.jar.JarFile jar=new java.util.jar.JarFile(root.resolve("build/minecraft-1.8.9-named.jar").toFile());java.io.InputStream in=jar.getInputStream(jar.getJarEntry("net/minecraft/entity/Entity.class"))){new ClassReader(in).accept(vanilla,ClassReader.SKIP_FRAMES);}
  MethodNode move=null;for(MethodNode m:vanilla.methods)if(m.name.equals("moveFlying")&&m.desc.equals("(FFF)V"))move=m;
  check(move!=null,"missing actual vanilla moveFlying");int edits=0;
  for(dev.rice.inject.transform.RicePatches.ClassEdits cls:dev.rice.inject.transform.RicePatches.create(false))if(cls.target.equals("net/minecraft/entity/Entity"))for(dev.rice.inject.transform.RicePatches.MethodEdit edit:cls.edits)if(edit.name.equals("moveFlying")){edit.apply(move);edits++;}
  check(edits==2,"actual moveFlying hook edits missing");
  move.name="move";move.access=dev.rice.shaded.asm.Opcodes.ACC_PUBLIC|dev.rice.shaded.asm.Opcodes.ACC_STATIC;
  move.desc="(Lnet/minecraft/entity/Entity;FFF)V";
  ClassNode probe=new ClassNode();probe.version=52;probe.access=1;probe.name="V172MovementProbe";probe.superName="java/lang/Object";probe.methods.add(move);
  ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);probe.accept(writer);
  return new ProbeLoader().define(writer.toByteArray()).getMethod("move",net.minecraft.entity.Entity.class,float.class,float.class,float.class);
 }
 static void steering(ScaffoldModule scaffold,Path root)throws Exception{
  Method move=patchedMovement(root);int samples=0;
  int[] forwards={1,1,0,-1,-1,-1,0,1},sides={0,1,1,1,0,-1,-1,-1};
  for(int fix=0;fix<2;fix++)for(int rotation=0;rotation<3;rotation++)for(int camera=0;camera<8;camera++)for(int input=0;input<8;input++){
   flag(scaffold,"move-fix",fix!=0);mode(scaffold,"rotations",rotation);mode(scaffold,"mode",0);begin(scaffold);
   V115Check.world.blocks.clear();V115Check.world.blocks.put(new BlockPos(0,63,0),Blocks.stone.getDefaultState());V115Check.self.rotationYaw=camera*45;V115Check.self.rotationPitch=11;
   KeyBinding.unPressAllKeys();KeyBinding.setKeyBindState(17,forwards[input]>0);KeyBinding.setKeyBindState(31,forwards[input]<0);
   KeyBinding.setKeyBindState(30,sides[input]>0);KeyBinding.setKeyBindState(32,sides[input]<0);
   for(int tick=0;tick<4;tick++){
    RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(V115Check.world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
    check(V115Check.self.rotationYaw==camera*45&&V115Check.self.rotationPitch==11,"camera changed before movement");
    V115Check.self.movementInput.moveForward=forwards[input];V115Check.self.movementInput.moveStrafe=sides[input];V115Check.self.movementInput.sneak=false;
    GameHooks.afterMovementInput(V115Check.self);
    check(V115Check.self.movementInput.moveForward==forwards[input]&&V115Check.self.movementInput.moveStrafe==sides[input],"silent fix changed vanilla sprint input");
    V115Check.self.motionX=0;V115Check.self.motionZ=0;
    move.invoke(null,V115Check.self,V115Check.self.movementInput.moveStrafe,V115Check.self.movementInput.moveForward,.1F);
    check(V115Check.self.movementInput.moveForward==forwards[input]&&V115Check.self.movementInput.moveStrafe==sides[input],"source tower helper leaked temporary input");
    double x=V115Check.self.motionX,z=V115Check.self.motionZ,radians=Math.toRadians(camera*45);
    double ex=sides[input]*Math.cos(radians)-forwards[input]*Math.sin(radians),ez=forwards[input]*Math.cos(radians)+sides[input]*Math.sin(radians);
    double cosine=(x*ex+z*ez)/Math.sqrt((x*x+z*z)*(ex*ex+ez*ez));
    check(cosine>.99999,"actual moveFlying steered away from input: fix="+fix+" rotation="+rotation+" camera="+camera+" input="+input+" dot="+cosine);
    check(Math.abs(Math.hypot(x,z)-.1)<.0001,"silent movement changed input speed");
    check(Math.abs(YuriScaffoldHost.jumpYaw(0,V115Check.self)-radians)<.0001,"sprint-jump points away from camera");
    GameHooks.yuriScaffoldRiding(V115Check.self,V115Check.self);GameHooks.yuriScaffoldPlayerUpdateReturn(V115Check.self);GameHooks.afterEntities(V115Check.world);
    check(V115Check.self.rotationYaw==camera*45&&V115Check.self.rotationPitch==11,"late rotation leaked into camera");samples++;
   }
   V115Check.manager.applyNow(scaffold,false);
  }
  System.out.println("PASS: "+samples+" actual patched vanilla moveFlying traces across Move Fix off/on, three Yuri rotation modes, eight camera headings/eight inputs; held-W direction/speed and sprint-jump; late walking yaw and camera restore");
 }

 static void settings(ScaffoldModule s)throws Exception{
  java.util.List<Property<?>> refs=new Scaffold().getElements();check(refs.size()==26&&s.getSettings().size()==26,"Yuri settings closure incomplete");
  for(int i=0;i<refs.size();i++){
   Property<?> ref=refs.get(i);Setting<?> setting=s.getSettings().get(i);check(setting.getLabel().equals(ref.getLabel()),"Yuri setting order/name mismatch");
   if(ref instanceof ModeProperty){Enum<?>[] values=((ModeProperty<?>)ref).getValues();ChoiceSetting actual=(ChoiceSetting)setting;check(actual.getOptions().size()==values.length,"missing Yuri mode");for(int n=0;n<values.length;n++){check(actual.getOptions().get(n).equals(values[n].toString()),"mode label mismatch");actual.set(n);s.settingVisible(setting.getKey());check(YuriScaffoldHost.instance.scaffold.getElements().get(i).getValue()==values[n],"choice not connected");}actual.set(((Enum<?>)ref.getValue()).ordinal());}
   else if(ref instanceof NumberProperty){NumberProperty p=(NumberProperty)ref;NumberSetting actual=(NumberSetting)setting;check(actual.getMinimum()==p.getMin()&&actual.getMaximum()==p.getMax()&&actual.getStep()==p.getIncrement(),"Yuri range mismatch");actual.set(p.getMin());s.settingVisible(setting.getKey());check(((Number)YuriScaffoldHost.instance.scaffold.getElements().get(i).getValue()).doubleValue()==p.getMin(),"number not connected");actual.set(p.getValue());}
   else{BooleanSetting actual=(BooleanSetting)setting;actual.set(!(Boolean)ref.getValue());s.settingVisible(setting.getKey());check(YuriScaffoldHost.instance.scaffold.getElements().get(i).getValue().equals(actual.isEnabled()),"toggle not connected");actual.set((Boolean)ref.getValue());}
   s.settingVisible(setting.getKey());
  }
  int samples=0;for(int bridge=0;bridge<5;bridge++)for(int rotation=0;rotation<3;rotation++)for(int tower=0;tower<5;tower++)for(boolean telly:new boolean[]{false,true})for(boolean sneak:new boolean[]{false,true}){
   mode(s,"mode",bridge);mode(s,"rotations",rotation);mode(s,"tower-mode",tower);flag(s,"hypixel-telly",telly);flag(s,"sneak",sneak);
   for(Property<?> p:YuriScaffoldHost.instance.scaffold.getElements()){String key=p.getLabel().toLowerCase(java.util.Locale.ROOT).replace(' ','-');check(s.settingVisible(key)==p.isAvailable(),"menu condition mismatch: "+key);}samples++;
  }
  mode(s,"mode",0);mode(s,"rotations",0);mode(s,"tower-mode",4);flag(s,"hypixel-telly",false);flag(s,"sneak",false);
  check(samples==300,"incomplete condition matrix");System.out.println("PASS: all 26 Yuri properties, exact labels/order/options/ranges, every control wired and conditions in 300 configurations");
 }
 static void placement(ScaffoldModule s)throws Exception{
  int clicks=0;
  for(int ray=0;ray<3;ray++)for(int swap=0;swap<2;swap++){
   mode(s,"mode",0);mode(s,"rotations",2);mode(s,"ray-cast",ray);mode(s,"swap-mode",swap);begin(s);V115Check.handler.sent.clear();V115Check.controller.placements.clear();
   for(int tick=0;tick<8;tick++)frame();
   net.minecraft.network.play.client.C03PacketPlayer pose=null;int placed=0;
   for(net.minecraft.network.Packet<?> packet:V115Check.handler.sent){
    if(packet instanceof net.minecraft.network.play.client.C03PacketPlayer&&((net.minecraft.network.play.client.C03PacketPlayer)packet).getRotating())pose=(net.minecraft.network.play.client.C03PacketPlayer)packet;
    if(packet instanceof net.minecraft.network.play.client.C08PacketPlayerBlockPlacement){check(pose!=null,"Yuri C08 preceded pose");net.minecraft.network.play.client.C08PacketPlayerBlockPlacement p=(net.minecraft.network.play.client.C08PacketPlayerBlockPlacement)packet;if(ray==2){net.minecraft.util.MovingObjectPosition hit=RayCastUtils.blockRay(new org.lwjgl.util.vector.Vector2f(pose.getYaw(),pose.getPitch()),4.5);check(hit!=null&&p.getPosition().equals(hit.getBlockPos())&&hit.sideHit.getIndex()==p.getPlacedBlockDirection(),"strict placement misses submitted face");}placed++;}
   }
   check(placed>0&&placed==V115Check.controller.placements.size(),"Yuri never placed: ray="+ray+" swap="+swap);clicks+=placed;
   check(s.spoofedDisplaySlot()==(swap==1&&ray!=2?0:-1),"Yuri server/client slot display mismatch");
   V115Check.manager.applyNow(s,false);check(V115Check.self.inventory.currentItem==0,"slot not restored");
  }
  mode(s,"ray-cast",1);mode(s,"swap-mode",0);mode(s,"rotations",0);
  begin(s);frame();V115Check.mc.inGameHasFocus=false;s.onClientTickStart(V115Check.c);check(!YuriScaffoldHost.instance.scaffold.isEnabled()&&V115Check.self.inventory.currentItem==0,"focus cleanup");
  V115Check.mc.inGameHasFocus=true;s.onClientTickStart(V115Check.c);check(YuriScaffoldHost.instance.scaffold.isEnabled(),"focus resume");
  V115Check.mc.currentScreen=new net.minecraft.client.gui.GuiChat();s.onClientTickStart(V115Check.c);check(!YuriScaffoldHost.instance.scaffold.isEnabled(),"GUI cleanup");V115Check.mc.currentScreen=null;s.onClientTickStart(V115Check.c);
  s.onWorldUnload(V115Check.c);check(SlotManager.getOriginalSlot()==-1,"world retained slot");V115Check.manager.applyNow(s,false);
  begin(s);RotationService.claim(1000);V115Check.controller.placements.clear();GameHooks.mayuPlayerUpdateHead(V115Check.self);check(V115Check.controller.placements.isEmpty(),"higher rotation owner lost placement priority");V115Check.manager.applyNow(s,false);
  System.out.println("PASS: "+clicks+" Yuri placements across all ray-cast/swap modes; strict face, packet order, hotbar display/restoration, focus/GUI/world cleanup and priority");
 }
 static void bridgeAndTower(ScaffoldModule s)throws Exception{
  for(int bridge=0;bridge<5;bridge++){
   mode(s,"mode",bridge);flag(s,"auto-jump",false);begin(s);
   for(int tick=0;tick<8;tick++)frame();
   check(Double.isFinite(YuriScaffoldHost.instance.scaffold.startY)&&Float.isFinite(RotationManager.rotations.x)&&Float.isFinite(RotationManager.rotations.y),"non-finite bridge state");
   if(bridge==1){s.settingVisible("auto-jump");check(((BooleanSetting)s.findSetting("auto-jump")).isEnabled(),"Yuri Telly did not enable Auto Jump");}
   if(bridge==2){s.settingVisible("sprint-mode");check(((ChoiceSetting)s.findSetting("sprint-mode")).selectedLabel().equals("Legit"),"Yuri Hypixel did not select Legit sprint");}
   V115Check.manager.applyNow(s,false);
  }
  mode(s,"mode",0);flag(s,"auto-jump",false);flag(s,"tower-move",true);begin(s);V115Check.place(V115Check.self,.5,64,.5);
  V115Check.self.movementInput.moveForward=0;V115Check.self.movementInput.moveStrafe=0;V115Check.self.motionX=V115Check.self.motionZ=0;
  KeyBinding.setKeyBindState(57,true);V115Input.keys[57]=true;
  mode(s,"tower-mode",0);V115Check.self.motionY=0;YuriScaffoldHost.instance.scaffold.tower();check(Math.abs(V115Check.self.motionY-.42)<.00001,"Vanilla tower missing");
  mode(s,"tower-mode",3);V115Check.self.motionY=0;YuriScaffoldHost.instance.scaffold.tower();check(Math.abs(V115Check.self.motionY-.41998)<.00001,"NCP tower missing");
  mode(s,"tower-mode",1);V115Check.self.motionY=-.1;YuriScaffoldHost.instance.scaffold.tower();check(Math.abs(V115Check.self.motionY+.191)<.0001,"Polar tower missing");
  mode(s,"tower-mode",2);V115Check.self.onGround=false;V115Check.put(YuriScaffoldHost.instance,"air",4);V115Check.self.motionY=0;YuriScaffoldHost.instance.scaffold.tower();check(Math.abs(V115Check.self.motionY+.03)<.00001,"Hypixel fourth air tick missing");
  V115Check.put(YuriScaffoldHost.instance,"air",5);V115Check.self.motionY=0;YuriScaffoldHost.instance.scaffold.tower();check(Math.abs(V115Check.self.motionY+.5)<.00001,"Hypixel fifth air tick missing");
  mode(s,"tower-mode",4);V115Check.self.motionY=.123;YuriScaffoldHost.instance.scaffold.tower();check(V115Check.self.motionY==.123,"None tower changed movement");
  mode(s,"tower-mode",0);flag(s,"tower-move",false);V115Check.self.movementInput.moveForward=1;V115Check.self.motionY=.123;YuriScaffoldHost.instance.scaffold.tower();check(V115Check.self.motionY==.123,"Tower Move condition ignored");
  V115Check.manager.applyNow(s,false);mode(s,"tower-mode",4);flag(s,"tower-move",true);mode(s,"sprint-mode",4);flag(s,"auto-jump",false);V115Input.clear();KeyBinding.unPressAllKeys();
  System.out.println("PASS: five bridge modes, Telly Auto Jump and Hypixel Legit sprint transitions; all five tower policies, Hypixel air ticks 4/5 and Tower Move condition");
 }
 static void profile(ScaffoldModule s)throws Exception{
  com.google.gson.JsonObject saved=dev.rice.config.RecoveredConfiguration.serialize(V115Check.manager,dev.rice.config.RecoveredConfiguration.Scope.PROFILE,java.util.Collections.emptyList());
  String original=saved.toString();com.google.gson.JsonObject record=saved.getAsJsonObject("Scaffold");
  for(Setting<?> setting:s.getSettings()){check(record.has(setting.getKey()),"profile omitted Yuri property");if(setting instanceof ChoiceSetting)((ChoiceSetting)setting).set(0);else if(setting instanceof BooleanSetting)((BooleanSetting)setting).set(false);else ((NumberSetting)setting).set(((NumberSetting)setting).getMaximum());s.settingVisible(setting.getKey());}
  dev.rice.config.RecoveredConfiguration.apply(V115Check.manager,saved,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);V115Check.manager.applyPendingChanges();
  for(Setting<?> setting:s.getSettings()){s.settingVisible(setting.getKey());check(dev.rice.config.SettingCodec.encode(setting).equals(record.get(setting.getKey())),"profile failed to restore Yuri control: "+setting.getKey());}
  check(saved.toString().equals(original),"profile load mutated saved input");
  System.out.println("PASS: all 26 Yuri settings save/load through the real profile codec without modifying the input record");
 }
 public static void main(String[] args)throws Exception{
  V115Check.setup();ScaffoldModule s=(ScaffoldModule)V115Check.manager.find("scaffold");settings(s);profile(s);placement(s);bridgeAndTower(s);steering(s,Paths.get(args[1]));
  check(!Files.exists(Paths.get(args[0]).resolve("dev/rice/client/scaffold/InjectMyauHost.class")),"retired host shipped");
  Path retired=Paths.get(args[0]).resolve("dev/rice/client/scaffold/injectmyau");check(!Files.exists(retired),"retired backend shipped");
  System.out.println("PASS: Yuri v1.3.1 is the sole Scaffold backend; original planner/helpers and 26 settings; retired InjectMyau closure absent");
 }
}

