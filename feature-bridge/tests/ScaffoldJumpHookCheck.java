import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Verify the vanilla jump call site actually exists in each supported namespace. */
public final class ScaffoldJumpHookCheck {
   public static void main(String[] a)throws Exception{
      ClassNode node=new ClassNode();
      try(JarFile jar=new JarFile(a[0]);java.io.InputStream in=jar.getInputStream(jar.getJarEntry(a[1]+".class"))){new ClassReader(in).accept(node,0);}
      int sites=0;
      for(MethodNode m:node.methods)if(m.name.equals(a[2])&&m.desc.equals("()V"))
         for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)i;
            if(c.getOpcode()==Opcodes.INVOKEVIRTUAL&&c.owner.equals(a[1])&&c.name.equals(a[3])&&c.desc.equals("()V"))sites++;
         }
      if(sites!=1)throw new AssertionError("Expected one real jump call in "+a[1]+", got "+sites);
      ClassNode hooks=new ClassNode();new ClassReader(Files.readAllBytes(Paths.get(a[4]))).accept(hooks,0);
      boolean found=false;
      for(MethodNode m:hooks.methods)if(m.name.equals("scaffoldJump")&&m.desc.equals("(Ljava/lang/Object;)V")&&(m.access&(Opcodes.ACC_NATIVE|Opcodes.ACC_STATIC))==(Opcodes.ACC_NATIVE|Opcodes.ACC_STATIC))found=true;
      if(!found)throw new AssertionError("Jump JNI declaration missing");
      System.out.println("Scaffold jump hook contract: "+a[1]+", vanilla jump call and native signature verified");
   }
}
