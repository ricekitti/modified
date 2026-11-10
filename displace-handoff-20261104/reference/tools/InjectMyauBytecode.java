import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.commons.*;
public final class InjectMyauBytecode {
 public static void main(String[] args)throws Exception{
  Path reference=Paths.get(args[0]),production=Paths.get(args[1]);int count=0;
  final String prefix="dev/rice/client/scaffold/injectmyau/";
  Set<String> types=new HashSet<>(Arrays.asList(args).subList(2,args.length));
  try(java.util.stream.Stream<Path> paths=Files.walk(reference)){
   for(Path p:(Iterable<Path>)paths.filter(x->x.toString().endsWith(".class"))::iterator){
    String name=reference.relativize(p).toString().replace('\\','/').replace(".class","");
    if(!types.contains(name.split("\\$")[0]))continue;
    ClassNode original=new ClassNode();
    new ClassReader(Files.readAllBytes(p)).accept(new ClassRemapper(original,new Remapper(){@Override public String map(String n){return n.startsWith("myau/")?prefix+n.substring(5):n;}}),0);
    for(MethodNode method:original.methods)for(AbstractInsnNode ins:method.instructions.toArray()){
     if(name.equals("myau/module/modules/Scaffold")&&ins instanceof MethodInsnNode){
      MethodInsnNode call=(MethodInsnNode)ins;
      if(call.getOpcode()==Opcodes.INVOKEVIRTUAL&&call.owner.equals("net/minecraft/client/multiplayer/PlayerControllerMP")&&call.name.equals("onPlayerRightClick"))
       method.instructions.set(ins,new MethodInsnNode(Opcodes.INVOKESTATIC,"dev/rice/client/scaffold/InjectMyauHost","place","(Lnet/minecraft/client/multiplayer/PlayerControllerMP;"+call.desc.substring(1),false));
     }
     if(ins instanceof FieldInsnNode){FieldInsnNode f=(FieldInsnNode)ins;
      if(f.getOpcode()==Opcodes.GETSTATIC&&f.owner.equals("net/minecraft/util/EnumFacing")&&f.name.equals("VALUES"))method.instructions.set(ins,new MethodInsnNode(Opcodes.INVOKESTATIC,f.owner,"values","()[Lnet/minecraft/util/EnumFacing;",false));
     }
    }
    Path actual=production.resolve(prefix+name.substring(5)+".class");
    ClassNode port=new ClassNode();new ClassReader(Files.readAllBytes(actual)).accept(port,0);
    // The user explicitly requested replacing Scaffold aim/placement behavior. All other methods stay compared.
    if(name.equals("myau/module/modules/Scaffold")){
     int overrides=0;
     for(MethodNode method:original.methods)if(method.name.equals("place")||method.name.equals("onUpdate")){
      MethodNode changed=null;for(MethodNode candidate:port.methods)if(candidate.name.equals(method.name)&&candidate.desc.equals(method.desc)&&candidate.access==method.access)changed=candidate;
      if(changed==null)throw new AssertionError("Rice override changed contract: "+method.name);
      method.instructions=changed.instructions;method.tryCatchBlocks=changed.tryCatchBlocks;overrides++;
     }
     if(overrides!=2)throw new AssertionError("Missing declared Rice Scaffold overrides");
    }
    String difference=MayuBytecode.compare(original,port);
    if(difference!=null)throw new AssertionError(name+": "+difference);
    if(port.version!=52)throw new AssertionError("Not Java 8: "+name);
    count++;
   }
  }
  if(count<30)throw new AssertionError("Incomplete InjectMyau bytecode comparison: "+count);
  System.out.println("PASS: "+count+" InjectMyau original classes retain original contracts and unchanged method instructions after namespace/EnumFacing adapters; Scaffold.place/onUpdate intentionally replaced and exercised by behavior tests. Original source compiled independently; no supplied upstream JAR was available.");
 }
}
