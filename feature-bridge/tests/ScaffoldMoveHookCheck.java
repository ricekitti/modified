import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact movement call sites that need the air-capable one-use edge clamp. */
public final class ScaffoldMoveHookCheck {
   public static void main(String[] args)throws Exception{
      ClassNode node=new ClassNode();
      try(JarFile jar=new JarFile(args[0]);java.io.InputStream in=jar.getInputStream(jar.getJarEntry(args[1]+".class"))){new ClassReader(in).accept(node,0);}
      int sites=0;
      for(MethodNode method:node.methods)if(method.name.equals(args[2])&&method.desc.equals("(FF)V"))
         for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode){
            MethodInsnNode call=(MethodInsnNode)insn;
            if(call.getOpcode()==Opcodes.INVOKEVIRTUAL&&call.owner.equals(args[1])&&call.name.equals(args[3])&&call.desc.equals("(DDD)V"))sites++;
         }
      if(sites!=3)throw new AssertionError("Expected three real moveEntity sites in "+args[1]+", got "+sites);
      ClassNode hooks=new ClassNode();new ClassReader(Files.readAllBytes(Paths.get(args[4]))).accept(hooks,0);
      boolean found=false;
      for(MethodNode method:hooks.methods)if(method.name.equals("scaffoldMove")&&method.desc.equals("(Ljava/lang/Object;DDD)V")
            &&(method.access&(Opcodes.ACC_NATIVE|Opcodes.ACC_STATIC))==(Opcodes.ACC_NATIVE|Opcodes.ACC_STATIC))found=true;
      if(!found)throw new AssertionError("Movement JNI declaration missing");
      System.out.println("Scaffold movement hook contract: "+args[1]+", all three vanilla movement branches verified");
   }
}
