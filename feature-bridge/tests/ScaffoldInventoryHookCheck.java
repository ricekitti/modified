import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Validate and optionally adapt the source-backed shadow held-item accessor. */
public final class ScaffoldInventoryHookCheck {
    public static void main(String[] a)throws Exception {
        ClassNode node=new ClassNode();
        try(JarFile jar=new JarFile(a[0]);java.io.InputStream in=jar.getInputStream(jar.getJarEntry(a[1]+".class"))){new ClassReader(in).accept(node,0);}
        int count=0;
        for(MethodNode method:node.methods)if(method.name.equals(a[2])&&method.desc.equals(a[3]))
            for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof FieldInsnNode){
                FieldInsnNode field=(FieldInsnNode)insn;
                if(field.getOpcode()==Opcodes.GETFIELD&&field.owner.equals(a[1])&&field.name.equals(a[4])&&field.desc.equals("I")){
                    count++;method.instructions.set(field,new MethodInsnNode(Opcodes.INVOKESTATIC,"ScaffoldSlotFixtureBridge","inventorySlot","(Ljava/lang/Object;)I",false));
                }
            }
        if(count!=3)throw new AssertionError("Expected three held-item slot reads, got "+count);
        if(!a[5].equals("-")){
            ClassWriter writer=new ClassWriter(0);node.accept(writer);Path path=Paths.get(a[5],a[1]+".class");
            Files.createDirectories(path.getParent());Files.write(path,writer.toByteArray());
        }
        System.out.println("Scaffold inventory hook contract: "+a[1]+", three shadow held-item reads verified");
    }
}
