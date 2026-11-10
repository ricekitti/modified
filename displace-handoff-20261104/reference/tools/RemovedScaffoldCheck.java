import dev.rice.client.hook.GameHooks;
import dev.rice.client.rotation.RotationService;
import dev.rice.client.mayu.MayuHost;
import net.minecraft.client.settings.KeyBinding;
import java.lang.reflect.*;
import java.nio.file.*;
import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
public final class RemovedScaffoldCheck {
 static void check(boolean value,String message){V115Check.check(value,message);}
 static final class ProbeLoader extends ClassLoader {
  ProbeLoader(){super(RemovedScaffoldCheck.class.getClassLoader());}
  Class<?> define(byte[] bytes){return defineClass("V177MovementProbe",bytes,0,bytes.length);}
 }
 static Method patchedMovement(Path root)throws Exception{
  dev.rice.inject.Mappings.load(null);
  ClassNode vanilla=new ClassNode();
  try(java.util.jar.JarFile jar=new java.util.jar.JarFile(root.resolve("build/minecraft-1.8.9-named.jar").toFile());java.io.InputStream in=jar.getInputStream(jar.getJarEntry("net/minecraft/entity/Entity.class"))){new ClassReader(in).accept(vanilla,ClassReader.SKIP_FRAMES);}
  MethodNode move=null;for(MethodNode m:vanilla.methods)if(m.name.equals("moveFlying")&&m.desc.equals("(FFF)V"))move=m;
  check(move!=null,"missing actual vanilla moveFlying");int edits=0;
  for(dev.rice.inject.transform.RicePatches.ClassEdits cls:dev.rice.inject.transform.RicePatches.create(false))if(cls.target.equals("net/minecraft/entity/Entity"))for(dev.rice.inject.transform.RicePatches.MethodEdit edit:cls.edits)if(edit.name.equals("moveFlying")){edit.apply(move);edits++;}
  check(edits==1,"actual moveFlying hook edits missing");
  move.name="move";move.access=dev.rice.shaded.asm.Opcodes.ACC_PUBLIC|dev.rice.shaded.asm.Opcodes.ACC_STATIC;
  move.desc="(Lnet/minecraft/entity/Entity;FFF)V";
  ClassNode probe=new ClassNode();probe.version=52;probe.access=1;probe.name="V177MovementProbe";probe.superName="java/lang/Object";probe.methods.add(move);
  ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);probe.accept(writer);
  return new ProbeLoader().define(writer.toByteArray()).getMethod("move",net.minecraft.entity.Entity.class,float.class,float.class,float.class);
 }

 public static void main(String[] args)throws Exception{
  V115Check.setup();for(dev.rice.module.Module m:V115Check.manager.all())if(m.isEnabled())V115Check.manager.applyNow(m,false);
  check(V115Check.manager.find("scaffold")==null,"removed Scaffold still registered");
  check(V115Check.manager.find("clutch") instanceof dev.rice.module.impl.ClutchModule,"OG Clutch removed with Scaffold");
  java.nio.file.Path classes=Paths.get(args[0]);
  check(!Files.exists(classes.resolve("dev/rice/module/impl/ScaffoldModule.class"))&&!Files.exists(classes.resolve("dev/rice/module/impl/ScaffoldModule$Frame.class")),"removed Scaffold adapter shipped");
  check(!Files.exists(classes.resolve("dev/rice/client/scaffold/YuriScaffoldHost.class"))&&!Files.exists(classes.resolve("dev/rice/client/scaffold/yuri")),"Scaffold backend shipped");
  com.google.gson.JsonObject legacy=new com.google.gson.JsonObject(),record=new com.google.gson.JsonObject();record.addProperty("__on",true);record.addProperty("safe-walk",true);legacy.add("Scaffold",record);
  String original=legacy.toString();dev.rice.config.RecoveredConfiguration.apply(V115Check.manager,legacy,dev.rice.config.RecoveredConfiguration.Scope.PROFILE);V115Check.manager.applyPendingChanges();check(V115Check.manager.find("scaffold")==null,"legacy profile resurrected removed Scaffold");check(legacy.toString().equals(original),"legacy profile input mutated");
  Method move=patchedMovement(Paths.get(args[1]));V115Check.self.rotationYaw=37;V115Check.self.rotationPitch=11;
  int[] f={1,1,0,-1,-1,-1,0,1},s={0,1,1,1,0,-1,-1,-1};int samples=0;V115Check.controller.placements.clear();V115Check.handler.sent.clear();
  for(int camera=0;camera<8;camera++)for(int input=0;input<8;input++){
   V115Check.self.rotationYaw=camera*45;V115Check.self.rotationPitch=11;V115Check.self.motionX=V115Check.self.motionZ=0;
   RotationService.reset(V115Check.c);RotationService.beginFrame(V115Check.c,0,0);GameHooks.mayuPlayerUpdateHead(V115Check.self);
   check(RotationService.claim(500),"removed Scaffold claimed rotation priority");
   V115Check.self.movementInput.moveForward=f[input];V115Check.self.movementInput.moveStrafe=s[input];
   move.invoke(null,V115Check.self,(float)s[input],(float)f[input],.1F);
   double radians=Math.toRadians(camera*45),ex=s[input]*Math.cos(radians)-f[input]*Math.sin(radians),ez=f[input]*Math.cos(radians)+s[input]*Math.sin(radians);
   double x=V115Check.self.motionX,z=V115Check.self.motionZ;double cosine=(x*ex+z*ez)/Math.sqrt((x*x+z*z)*(ex*ex+ez*ez));check(cosine>.99999,"removed Scaffold changed movement");
   myau.host.MayuMixins.applySpoofedRotationBeforeRidingCheck(V115Check.self,V115Check.self);myau.host.MayuMixins.afterPlayerUpdate(V115Check.self);GameHooks.afterEntities(V115Check.world);
   check(V115Check.self.rotationYaw==camera*45&&V115Check.self.rotationPitch==11,"removed Scaffold changed camera");samples++;
  }
  check(V115Check.controller.placements.isEmpty(),"removed Scaffold placed a block");
  check(!MayuHost.scaffold().isEnabled()&&MayuHost.scaffold().getPreviousHotbarSlot()==-1,"legacy view owns state");
  try(java.util.stream.Stream<Path> files=Files.walk(classes)){for(Path path:(Iterable<Path>)files.filter(p->p.toString().endsWith(".class"))::iterator){ClassNode node=new ClassNode();new ClassReader(Files.readAllBytes(path)).accept(node,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);for(MethodNode method:node.methods)for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode){String owner=((MethodInsnNode)ins).owner;check(!owner.startsWith("dev/rice/client/scaffold/")&&!owner.equals("dev/rice/module/impl/ScaffoldModule"),"retired Scaffold call: "+node.name);}}}
  System.out.println("PASS: Scaffold absent from registry/payload and legacy profiles cannot resurrect it; OG Clutch retained; removal preserves "+samples+" patched vanilla movement/camera traces; no placements or rotation claim; shared renderer adapter inert");
 }
}
