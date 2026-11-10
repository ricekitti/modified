import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Check the exact field owners and methods used by the separate native registry. */
public final class ScaffoldMotionHookCheck {
    public static void main(String[] a) throws Exception {
        ClassNode player=new ClassNode();
        try(JarFile jar=new JarFile(a[0]);java.io.InputStream in=jar.getInputStream(jar.getJarEntry(a[1]+".class"))){new ClassReader(in).accept(player,0);}
        Map<String,Integer> reads=new HashMap<>();int calls=0,inputCalls=0;
        for(MethodNode method:player.methods){
            for(AbstractInsnNode insn:method.instructions.toArray()){
                if(method.name.equals(a[2])&&method.desc.equals("()V")&&insn instanceof FieldInsnNode){
                    FieldInsnNode field=(FieldInsnNode)insn;
                    if(field.getOpcode()==Opcodes.GETFIELD)reads.put(field.owner+"/"+field.name,reads.getOrDefault(field.owner+"/"+field.name,0)+1);
                }
                if(method.name.equals(a[10])&&method.desc.equals("()V")&&insn instanceof MethodInsnNode){
                    MethodInsnNode call=(MethodInsnNode)insn;
                    if(call.owner.equals(a[11])&&call.name.equals(a[12])&&call.desc.equals("()V"))inputCalls++;
                }
                if(method.name.equals(a[3])&&method.desc.equals("()V")&&insn instanceof MethodInsnNode){
                    MethodInsnNode call=(MethodInsnNode)insn;
                    if(call.owner.equals(a[1])&&call.name.equals(a[2])&&call.desc.equals("()V"))calls++;
                }
            }
        }
        if(inputCalls!=1)throw new AssertionError("Expected one movement-input call, got "+inputCalls);
        if(calls!=1)throw new AssertionError("Expected one walking-player call in "+a[1]+"."+a[3]+": "+calls);
        for(String key:new String[]{a[1]+"/"+a[4],a[1]+"/"+a[5],a[1]+"/"+a[6],a[8]+"/"+a[7]})
            if(!reads.containsKey(key))throw new AssertionError("Motion field hook misses "+key+"; actual reads "+reads);
        ClassNode hooks=new ClassNode();new ClassReader(Files.readAllBytes(Paths.get(a[9]))).accept(hooks,0);
        for(String name:new String[]{"motionX","motionY","motionZ","motionGround","movementInput"}){
            boolean found=false;
            for(MethodNode method:hooks.methods)if(method.name.equals(name)&&method.desc.equals("(Ljava/lang/Object;)"+(name.equals("movementInput")?"V":name.equals("motionGround")?"Z":"D"))&&(method.access&(Opcodes.ACC_NATIVE|Opcodes.ACC_STATIC))==(Opcodes.ACC_NATIVE|Opcodes.ACC_STATIC))found=true;
            if(!found)throw new AssertionError("Missing motion hook declaration "+name);
        }
        System.out.println("Scaffold motion hook contract: "+a[1]+", one wrapper and all four field-read sites verified");
    }
}
