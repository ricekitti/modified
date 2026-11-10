import dev.rice.client.hook.GameHooks;
import dev.rice.client.rotation.RotationService;
import dev.rice.client.scaffold.InjectMyauHost;
import dev.rice.client.scaffold.injectmyau.module.modules.Scaffold;
import dev.rice.module.Module;
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

/** Registered module/hook/lifecycle checks. No game, OpenGL context or injector is launched. */
public final class InjectMyauCheck {
 static void check(boolean v,String message){V115Check.check(v,message);}
 static void mode(ScaffoldModule s,String key,int value){((ChoiceSetting)s.findSetting(key)).set(value);}
 static void flag(ScaffoldModule s,String key,boolean value){((BooleanSetting)s.findSetting(key)).set(value);}
 static void begin(ScaffoldModule s){
  V115Check.manager.applyNow(s,false);RotationService.reset(V115Check.c);
  V115Check.world.blocks.clear();V115Check.world.blocks.put(new BlockPos(0,63,0),Blocks.stone.getDefaultState());
  V115Check.place(V115Check.self,1.1,64,.5);V115Check.self.onGround=true;
  V115Check.self.motionX=.25;V115Check.self.motionY=0;V115Check.self.motionZ=0;
  V115Check.self.rotationYaw=37;V115Check.self.rotationPitch=11;
  V115Check.self.inventory.currentItem=0;V115Check.self.inventory.setInventorySlotContents(2,new ItemStack(Blocks.stone,64));
  V115Check.mc.inGameHasFocus=true;V115Check.mc.currentScreen=null;
  KeyBinding.unPressAllKeys();KeyBinding.setKeyBindState(17,true);
  V115Check.self.movementInput.moveForward=1;V115Check.self.movementInput.moveStrafe=0;
  V115Check.manager.applyNow(s,true);RotationService.beginFrame(V115Check.c,0,0);
 }
 static void frame(){
  RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(V115Check.world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
  GameHooks.afterMovementInput(V115Check.self);GameHooks.injectMyauLivingUpdate();GameHooks.injectMyauRiding(V115Check.self,V115Check.self);GameHooks.walkingPlayerHead(V115Check.self);GameHooks.injectMyauPlayerUpdateReturn(V115Check.self);GameHooks.afterEntities(V115Check.world);
 }
 static final class ProbeLoader extends ClassLoader {
  ProbeLoader(){super(InjectMyauCheck.class.getClassLoader());}
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
  for(int fix=0;fix<2;fix++)for(int rotation=1;rotation<6;rotation++)for(int camera=0;camera<8;camera++)for(int input=0;input<8;input++){
   mode(scaffold,"move-fix",fix);mode(scaffold,"rotations",rotation);mode(scaffold,"rotation-mode",0);begin(scaffold);
   V115Check.world.blocks.clear();V115Check.self.rotationYaw=camera*45;V115Check.self.rotationPitch=11;
   KeyBinding.unPressAllKeys();KeyBinding.setKeyBindState(17,forwards[input]>0);KeyBinding.setKeyBindState(31,forwards[input]<0);
   KeyBinding.setKeyBindState(30,sides[input]>0);KeyBinding.setKeyBindState(32,sides[input]<0);
   for(int tick=0;tick<4;tick++){
    float previousAimYaw=tick==0?camera*45:myau.RotationState.headYaw();
    float previousAimPitch=tick==0?11:dev.rice.client.scaffold.injectmyau.management.RotationState.getRotationPitch();
    RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(V115Check.world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
    check(V115Check.self.rotationYaw==camera*45&&V115Check.self.rotationPitch==11,"camera changed before movement");
    check(Math.abs(myau.RotationState.headYaw()-previousAimYaw)<=35.001F,"unbounded model yaw/spin");
    check(Math.abs(dev.rice.client.scaffold.injectmyau.management.RotationState.getPrevRotationYawHead()-previousAimYaw)<.001F,"model interpolation started from a stale head yaw");
    check(Math.abs(dev.rice.client.scaffold.injectmyau.management.RotationState.getRotationPitch()-previousAimPitch)<=25.001F,"unbounded model pitch");
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
    check(Math.abs(InjectMyauHost.jumpYaw(0,V115Check.self)-radians)<.0001,"sprint-jump points away from camera");
    GameHooks.injectMyauRiding(V115Check.self,V115Check.self);GameHooks.injectMyauPlayerUpdateReturn(V115Check.self);GameHooks.afterEntities(V115Check.world);
    check(V115Check.self.rotationYaw==camera*45&&V115Check.self.rotationPitch==11,"late rotation leaked into camera");samples++;
   }
   V115Check.manager.applyNow(scaffold,false);
  }
  System.out.println("PASS: "+samples+" actual patched vanilla moveFlying traces across NONE/SILENT, five rotation modes, eight camera headings/eight inputs; held-W direction/speed and sprint-jump; bounded yaw/pitch every tick; late walking yaw and camera restore");
 }
 static java.util.Map<String,dev.rice.client.scaffold.injectmyau.property.Property<?>> sourceProperties(Scaffold source)throws Exception{
  java.util.Map<String,dev.rice.client.scaffold.injectmyau.property.Property<?>> result=new java.util.LinkedHashMap<>();
  for(Field f:Scaffold.class.getFields())if(dev.rice.client.scaffold.injectmyau.property.Property.class.isAssignableFrom(f.getType())){
   dev.rice.client.scaffold.injectmyau.property.Property<?> p=(dev.rice.client.scaffold.injectmyau.property.Property<?>)f.get(source);result.put(p.getName(),p);
  }
  return result;
 }
 static void settings(ScaffoldModule s)throws Exception{
  Scaffold original=new Scaffold();java.util.Map<String,dev.rice.client.scaffold.injectmyau.property.Property<?>> expected=sourceProperties(original);
  java.util.Map<String,dev.rice.client.scaffold.injectmyau.property.Property<?>> backend=sourceProperties(InjectMyauHost.instance.scaffold);
  java.util.List<String> order=java.util.Arrays.asList("rotations","watchdog-yaw-limit","move-fix","rotation-mode","sprint","ground-motion","air-motion","speed-motion","tower","keep-y","keep-y-on-press","no-keep-y-on-jump-potion","multi-place","safe-walk","swing","item-spoof","block-counter","debug");
  java.util.List<String> actual=new java.util.ArrayList<>();com.google.gson.JsonObject defaults=new com.google.gson.JsonObject();
  for(Setting<?> setting:s.getSettings()){actual.add(setting.getKey());defaults.add(setting.getKey(),dev.rice.config.SettingCodec.encode(setting));}
  check(actual.equals(order)&&new java.util.HashSet<>(actual).equals(expected.keySet()),"Scaffold menu omits or reorders source properties");
  for(Setting<?> setting:s.getSettings()){
   dev.rice.client.scaffold.injectmyau.property.Property<?> ref=expected.get(setting.getKey());
   check(setting.getLabel().equals(ref.getName()),"source setting label mismatch: "+setting.getKey());
   if(ref instanceof dev.rice.client.scaffold.injectmyau.property.properties.ModeProperty){
    dev.rice.client.scaffold.injectmyau.property.properties.ModeProperty m=(dev.rice.client.scaffold.injectmyau.property.properties.ModeProperty)ref;
    ChoiceSetting c=(ChoiceSetting)setting;check(c.getOptions().equals(java.util.Arrays.asList(m.getModes()))&&c.index()==m.getValue(),"source choices/default mismatch: "+setting.getKey());
    for(int i=0;i<m.getModeCount();i++){c.set(i);s.settingVisible(setting.getKey());check(backend.get(setting.getKey()).getValue().equals(i),"choice not wired to source: "+setting.getKey()+"/"+i);}
   }else if(ref instanceof dev.rice.client.scaffold.injectmyau.property.properties.BooleanProperty){
    check(((BooleanSetting)setting).isEnabled()==(Boolean)ref.getValue(),"source toggle default mismatch: "+setting.getKey());
    for(boolean on:new boolean[]{false,true}){((BooleanSetting)setting).set(on);s.settingVisible(setting.getKey());check(backend.get(setting.getKey()).getValue().equals(on),"toggle not wired to source: "+setting.getKey());}
   }else{
    NumberSetting n=(NumberSetting)setting;double min,max,step;
    if(ref instanceof dev.rice.client.scaffold.injectmyau.property.properties.FloatProperty){
     dev.rice.client.scaffold.injectmyau.property.properties.FloatProperty f=(dev.rice.client.scaffold.injectmyau.property.properties.FloatProperty)ref;
     min=f.getMinimum();max=f.getMaximum();step=f.getStep();
    }else{
     dev.rice.client.scaffold.injectmyau.property.properties.PercentProperty f=(dev.rice.client.scaffold.injectmyau.property.properties.PercentProperty)ref;
     min=f.getMinimum();max=f.getMaximum();step=1;
    }
    check(n.getMinimum()==min&&n.getMaximum()==max&&n.getStep()==step&&n.doubleValue()==((Number)ref.getValue()).doubleValue(),"source numeric range/default mismatch: "+setting.getKey());
    for(double v:new double[]{min,max,min+step*2}){n.set(v);s.settingVisible(setting.getKey());check(((Number)backend.get(setting.getKey()).getValue()).doubleValue()==v,"slider not wired to source: "+setting.getKey());}
   }
  }
  for(Setting<?> setting:s.getSettings())dev.rice.config.SettingCodec.decode(setting,defaults.get(setting.getKey()));
  dev.rice.client.render.GuiPresentation view=new dev.rice.client.render.GuiPresentation((text,font,size)->text.length()*size*.5F);
  dev.rice.client.render.InlineModuleSettings ui=new dev.rice.client.render.InlineModuleSettings(V115Check.manager,view,new dev.rice.client.render.GuiPresentation.Tooltip());
  int combinations=0;
  for(int rotation=0;rotation<6;rotation++)for(int keep=0;keep<4;keep++)for(int tower=0;tower<5;tower++)for(int fix=0;fix<2;fix++)for(int submit=0;submit<2;submit++){
   mode(s,"rotations",rotation);mode(s,"keep-y",keep);mode(s,"tower",tower);mode(s,"move-fix",fix);mode(s,"rotation-mode",submit);
   original.rotationMode.setValue(rotation);original.keepY.setValue(keep);original.tower.setValue(tower);original.moveFix.setValue(fix);original.rotationSubmit.setValue(submit);
   java.util.List<String> visible=new java.util.ArrayList<>();for(String key:order)if(expected.get(key).isVisible())visible.add(key);
   dev.rice.client.render.RecoveredGui.Runtime state=new dev.rice.client.render.RecoveredGui.Runtime();state.mouseX=state.mouseY=-10000;
   dev.rice.client.render.DrawPlan plan=new dev.rice.client.render.DrawPlan();
   dev.rice.client.render.RecoveredGui.Layout layout=state.push(new dev.rice.client.render.RenderMath.Rect(10,10,600,6000),3,0,false);
   java.util.List<String> rendered=ui.render(s,state,plan,layout,new float[]{1,.35F,0,1},++combinations);state.pop();
   check(rendered.equals(visible),"actual menu conditions differ from source: rotation="+rotation+" keep="+keep+" tower="+tower+" fix="+fix+" submit="+submit+" rendered="+rendered+" expected="+visible);
   for(String key:visible){boolean label=false;for(dev.rice.client.render.DrawPlan.Op op:plan.operations())if(op.value instanceof String&&op.value.equals(key))label=true;check(label,"setting label absent from draw plan: "+key);}
  }
  check(combinations==480,"incomplete Scaffold condition matrix");
  mode(s,"rotations",5);mode(s,"keep-y",3);flag(s,"keep-y-on-press",true);flag(s,"no-keep-y-on-jump-potion",true);((NumberSetting)s.findSetting("watchdog-yaw-limit")).set(65.0);
  com.google.gson.JsonObject saved=dev.rice.config.RecoveredConfiguration.serialize(V115Check.manager,dev.rice.config.RecoveredConfiguration.Scope.PROFILE,java.util.Collections.emptyList());
  check(saved.getAsJsonObject("Scaffold").get("keep-y").getAsString().equals("Keep-Y"),"save kept legacy Keep-Y label");
  for(Setting<?> setting:s.getSettings())dev.rice.config.SettingCodec.decode(setting,defaults.get(setting.getKey()));
  dev.rice.config.RecoveredConfiguration.apply(V115Check.manager,saved,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);V115Check.manager.applyPendingChanges();
  for(Setting<?> setting:s.getSettings())check(dev.rice.config.SettingCodec.encode(setting).equals(saved.getAsJsonObject("Scaffold").get(setting.getKey())),"profile failed to restore source property: "+setting.getKey());
  for(String[] alias:new String[][]{{"TELLY","Keep-Y"},{"EXTRA","Extrea"},{"VANILLA","Vanilla"},{"Keep-Y","Keep-Y"},{"Extrea","Extrea"}}){
   com.google.gson.JsonObject root=new com.google.gson.JsonObject(),record=new com.google.gson.JsonObject();record.addProperty("keep-y",alias[0]);root.add("Scaffold",record);
   dev.rice.config.RecoveredConfiguration.apply(V115Check.manager,root,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);V115Check.manager.applyPendingChanges();
   check(((ChoiceSetting)s.findSetting("keep-y")).selectedLabel().equals(alias[1]),"legacy Keep-Y profile migration failed: "+alias[0]);
   check(record.get("keep-y").getAsString().equals(alias[0]),"profile migration modified input JSON");
  }
  for(Setting<?> setting:s.getSettings())dev.rice.config.SettingCodec.decode(setting,defaults.get(setting.getKey()));
  System.out.println("PASS: all 18 InjectMyau source settings: exact names/order/options/defaults/ranges; every control wired; actual menu draw plan matches source visibility in 480 configurations; source labels and legacy profile round trips");
 }
 static void placementOrder(ScaffoldModule s)throws Exception{
  int placements=0;
  for(int submit=0;submit<2;submit++)for(boolean multi:new boolean[]{false,true}){
   mode(s,"rotations",1);mode(s,"rotation-mode",submit);mode(s,"keep-y",0);mode(s,"tower",0);flag(s,"multi-place",multi);
   begin(s);V115Check.self.rotationYaw=-90;V115Check.self.rotationPitch=11;
   int attempts=0;
   for(int tick=0;tick<12;tick++){
    V115Check.handler.sent.clear();V115Check.controller.placements.clear();
    RotationService.beginFrame(V115Check.c,0,0);GameHooks.beforeEntities(V115Check.world);GameHooks.mayuPlayerUpdateHead(V115Check.self);
    net.minecraft.network.play.client.C03PacketPlayer pose=null;int poseCount=0,clicks=0;
    for(net.minecraft.network.Packet<?> packet:V115Check.handler.sent){
     if(packet instanceof net.minecraft.network.play.client.C03PacketPlayer){
      net.minecraft.network.play.client.C03PacketPlayer move=(net.minecraft.network.play.client.C03PacketPlayer)packet;
      if(move.isMoving()&&move.getRotating()){pose=move;poseCount++;}
     }
     if(packet instanceof net.minecraft.network.play.client.C08PacketPlayerBlockPlacement){
      check(pose!=null,"C08 prediction happened before position/look: submit="+submit+" multi="+multi);
      check(Math.abs(pose.getPositionX()-V115Check.self.posX)<.00001&&Math.abs(pose.getPositionY()-V115Check.self.getEntityBoundingBox().minY)<.00001&&Math.abs(pose.getPositionZ()-V115Check.self.posZ)<.00001,"placement used stale server position");
      if(submit==0)check(Math.abs(pose.getYaw()-myau.RotationState.headYaw())<.001&&Math.abs(pose.getPitch()-dev.rice.client.scaffold.injectmyau.management.RotationState.getRotationPitch())<.001,"placement used stale server aim");
      net.minecraft.network.play.client.C08PacketPlayerBlockPlacement click=(net.minecraft.network.play.client.C08PacketPlayerBlockPlacement)packet;
      net.minecraft.util.MovingObjectPosition ray=dev.rice.client.scaffold.injectmyau.util.RotationUtil.rayTrace(pose.getYaw(),pose.getPitch(),V115Check.controller.getBlockReachDistance(),1F);
      check(ray!=null&&ray.typeOfHit==net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK&&ray.getBlockPos().equals(click.getPosition())&&ray.sideHit.getIndex()==click.getPlacedBlockDirection(),"C08 face does not match the submitted aim");
      clicks++;placements++;
     }
    }
    if(tick==0)check(clicks==0,"predicted a block before the initial turn reached its face");
    check(clicks==V115Check.controller.placements.size(),"placement prediction/packet trace mismatch");
    if(submit==0)check(poseCount==(clicks>0?1:0),"normal placement repeats pose or sends it without placement");
    check(V115Check.self.rotationYaw==-90&&V115Check.self.rotationPitch==11,"placement synchronization changed camera");
    attempts+=clicks;
    GameHooks.injectMyauRiding(V115Check.self,V115Check.self);GameHooks.injectMyauPlayerUpdateReturn(V115Check.self);GameHooks.afterEntities(V115Check.world);
   }
   check(attempts>0,"packet order test did not exercise placement: submit="+submit+" multi="+multi);
   V115Check.manager.applyNow(s,false);
  }
  flag(s,"multi-place",true);mode(s,"rotation-mode",0);
  // A stale callback after disable must not predict a block or emit another pose.
  V115Check.handler.sent.clear();V115Check.controller.placements.clear();
  boolean result=InjectMyauHost.place(V115Check.controller,V115Check.self,V115Check.world,V115Check.self.getHeldItem(),new BlockPos(0,63,0),net.minecraft.util.EnumFacing.EAST,new net.minecraft.util.Vec3(1,63.5,.5));
  check(!result&&V115Check.handler.sent.isEmpty()&&V115Check.controller.placements.isEmpty(),"stale placement survived disable");
  System.out.println("PASS: "+placements+" real Scaffold C08 traces: current position/look precedes local prediction; NORMAL/GRIM and multi-place on/off; ray-checked faces, turn-before-placement, pose deduplication, camera isolation and disable cleanup");
 }
 static void wrapTurns(ScaffoldModule s)throws Exception{
  for(int submit=0;submit<2;submit++){
   mode(s,"rotations",4);mode(s,"rotation-mode",submit);begin(s);V115Check.world.blocks.clear();
   V115Check.self.rotationYaw=179;V115Check.self.rotationPitch=11;
   float previous=179;
   for(int tick=0;tick<16;tick++){
    V115Check.self.rotationYaw=tick<4?179:-179;
    frame();
    float current=myau.RotationState.headYaw();
    check(Math.abs(current-previous)<=35.001F,"wrapped camera heading caused full model spin");
    check(Math.abs(dev.rice.client.scaffold.injectmyau.management.RotationState.getPrevRotationYawHead()-previous)<.001F,"wrapped heading broke model interpolation");
    previous=current;
   }
   V115Check.manager.applyNow(s,false);
  }
  mode(s,"rotation-mode",0);
  System.out.println("PASS: NORMAL/GRIM smooth turns across camera +179/-179 wrap, with continuous model interpolation and camera restoration");
 }
 public static void main(String[] args)throws Exception{
  V115Check.setup();ScaffoldModule s=(ScaffoldModule)V115Check.manager.find("scaffold");
  settings(s);
  check(s.getSettings().size()==18,"InjectMyau property set incomplete");
  check(((ChoiceSetting)s.findSetting("rotations")).getOptions().size()==6,"missing Smooth/Watchdog modes");
  check(((ChoiceSetting)s.findSetting("rotation-mode")).getOptions().size()==2,"missing Grim submit mode");
  check(((ChoiceSetting)s.findSetting("tower")).getOptions().size()==5,"missing Watchdog prediction tower");
  for(int fix=0;fix<2;fix++){
   mode(s,"move-fix",fix);begin(s);GameHooks.mayuPlayerUpdateHead(V115Check.self);
   check(V115Check.self.rotationYaw==37&&V115Check.self.rotationPitch==11,"Scaffold applied packet rotation before movement");
   float expected=fix==0?37:myau.RotationState.headYaw();
   check(Math.abs(InjectMyauHost.moveFlyingYaw(V115Check.self)-expected)<.001,"movement yaw deviated from InjectMyau UpdateEvent");
   check(Math.abs(InjectMyauHost.jumpYaw(0,V115Check.self)-(float)Math.toRadians(37))<.001,"jump must preserve physical forward direction");
   GameHooks.injectMyauRiding(V115Check.self,V115Check.self);
   check(Math.abs(V115Check.self.rotationYaw-myau.RotationState.headYaw())<.001,"walking packet did not receive Scaffold yaw");
   GameHooks.injectMyauPlayerUpdateReturn(V115Check.self);
   GameHooks.afterEntities(V115Check.world);check(Math.abs(V115Check.self.rotationYaw-37)<.001&&V115Check.self.rotationPitch==11,"camera leaked");
   check(GameHooks.beforeInteraction(false)&&GameHooks.beforeInteraction(true)&&s.cancelBlockClick(),"manual interaction gates missing");
   flag(s,"item-spoof",true);s.onClientTickStart(V115Check.c);check(s.spoofedDisplaySlot()==0,"display slot did not retain original");
   check(s.hotbarChange(5,0),"hotbar event not cancelled");V115Check.manager.applyNow(s,false);
   check(V115Check.self.inventory.currentItem==5,"user hotbar request was not restored on disable");flag(s,"item-spoof",false);
  }
  begin(s);RotationService.claim(1000);float before=V115Check.self.rotationYaw;GameHooks.mayuPlayerUpdateHead(V115Check.self);
  check(V115Check.self.rotationYaw==before,"Scaffold overwrote higher-priority rotation");
  begin(s);GameHooks.mayuPlayerUpdateHead(V115Check.self);GameHooks.injectMyauRiding(V115Check.self,V115Check.self);RotationService.claim(1000);
  V115Check.self.rotationYaw=123;V115Check.self.rotationPitch=45;InjectMyauHost.instance.postUpdate();
  check(V115Check.self.rotationYaw==123&&V115Check.self.rotationPitch==45,"Scaffold cleanup overwrote a later rotation owner");
  begin(s);frame();V115Check.mc.inGameHasFocus=false;s.onClientTickStart(V115Check.c);
  check(!InjectMyauHost.instance.scaffold.isEnabled()&&V115Check.self.inventory.currentItem==0,"focus loss did not suspend/restore slot");
  V115Check.mc.inGameHasFocus=true;s.onClientTickStart(V115Check.c);check(InjectMyauHost.instance.scaffold.isEnabled(),"focus return did not resume");
  V115Check.mc.currentScreen=new net.minecraft.client.gui.GuiChat();s.onClientTickStart(V115Check.c);
  check(!InjectMyauHost.instance.scaffold.isEnabled(),"GUI did not suspend Scaffold");V115Check.mc.currentScreen=null;
  V115Check.put(InjectMyauHost.instance.scaffold,"startY",13);V115Check.put(InjectMyauHost.instance.scaffold,"stage",7);
  s.onWorldUnload(V115Check.c);
  check((Integer)V119Check.get(InjectMyauHost.instance.scaffold,"startY")==256&&(Integer)V119Check.get(InjectMyauHost.instance.scaffold,"stage")==0,"world unload retained keep-Y/tower state");
  check(!dev.rice.client.scaffold.injectmyau.management.RotationState.isRotated(1),"world unload retained model rotations");
  check(s.isEnabled()&&InjectMyauHost.instance.scaffold.getSlot()==-1,"world unload retained runtime slot");
  V115Check.self.inventory.currentItem=4;s.onClientTickStart(V115Check.c);frame();V115Check.manager.applyNow(s,false);
  check(V115Check.self.inventory.currentItem==4,"new world restored old-world slot");
  // Exercise the extra submit and rotation paths on the registered backend, including actual placement.
  for(int rotation=0;rotation<6;rotation++)for(int submit=0;submit<2;submit++){
   mode(s,"rotations",rotation);mode(s,"rotation-mode",submit);begin(s);
   for(int tick=0;tick<5;tick++)frame();
   check(Float.isFinite(V115Check.self.rotationYaw)&&Float.isFinite(V115Check.self.rotationPitch),"non-finite rotation");
   V115Check.manager.applyNow(s,false);
  }
  placementOrder(s);
  wrapTurns(s);
  steering(s,Paths.get(args[1]));
  for(String retired:new String[]{"dev/rice/client/scaffold/MyauScaffoldBackend.class","dev/rice/client/scaffold/openb4/OpenB4ScaffoldBackend.class","myau/ScaffoldModule.class"})check(!Files.exists(Paths.get(args[0]).resolve(retired)),"retired Scaffold still shipped: "+retired);
  check(V115Check.manager.find("tower")==null&&V115Check.manager.find("autoswap")==null,"removed companion became registered");
  System.out.println("PASS: InjectMyau sole Scaffold; 18 properties; six rotation/two submit paths; source movement/jump yaw; click gates; hotbar/item spoof; rotation priority; GUI/focus/disable/world cleanup");
 }
}
