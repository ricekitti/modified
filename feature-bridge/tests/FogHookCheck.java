import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Checks the actual private fog call sites and generated JNI hook signature. */
public final class FogHookCheck {
    public static void main(String[] args) throws Exception {
        ClassNode renderer=new ClassNode();
        try(JarFile jar=new JarFile(args[0]);java.io.InputStream input=jar.getInputStream(jar.getJarEntry(args[1]+".class"))){new ClassReader(input).accept(renderer,0);}
        int matches=0;
        for(MethodNode method:renderer.methods)if(method.name.equals(args[2])&&method.desc.equals("(IFJ)V"))
            for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)instruction;if(call.owner.equals(args[1])&&call.name.equals(args[3])&&call.desc.equals("(IF)V")&&(call.getOpcode()==Opcodes.INVOKESPECIAL||call.getOpcode()==Opcodes.INVOKEVIRTUAL))matches++;}
        if(matches==0)throw new AssertionError("Missing setupFog call site in "+args[1]+"."+args[2]);
        ClassNode hooks=new ClassNode();new ClassReader(Files.readAllBytes(Paths.get(args[4]))).accept(hooks,0);
        if(!hooks.name.equals("SlinkyHooks18"))throw new AssertionError("Expected fresh bootstrap name for expanded hook schema");
        boolean found=false;int natives=0;
        for(MethodNode method:hooks.methods)if((method.access&Opcodes.ACC_NATIVE)!=0){natives++;if(method.name.equals("setupFog")&&method.desc.equals("(Ljava/lang/Object;IF)V")&&(method.access&Opcodes.ACC_STATIC)!=0)found=true;}
        if(!found||natives!=34)throw new AssertionError("Fog JNI declaration does not match native table (expected 34 natives incl. Mayu event hooks, got "+natives+")");
        System.out.println("Fog hook contract: "+args[1]+"."+args[2]+", "+matches+" setupFog calls; "+natives+" JNI declarations");
    }
}
