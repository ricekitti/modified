import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Verify both actual controller sites; optionally produce a headless fixture copy. */
public final class ScaffoldSlotHookCheck {
    public static void main(String[] a)throws Exception {
        ClassNode node=new ClassNode();
        try(JarFile jar=new JarFile(a[0]);java.io.InputStream in=jar.getInputStream(jar.getJarEntry(a[1]+".class"))){new ClassReader(in).accept(node,0);}
        int slots=0,places=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode insn:method.instructions.toArray()){
            if(method.name.equals(a[2])&&method.desc.equals("()V")&&insn instanceof FieldInsnNode){
                FieldInsnNode field=(FieldInsnNode)insn;
                if(field.getOpcode()==Opcodes.GETFIELD&&field.owner.equals(a[3])&&field.name.equals(a[4])&&field.desc.equals("I")){
                    slots++;method.instructions.set(field,new MethodInsnNode(Opcodes.INVOKESTATIC,"ScaffoldSlotFixtureBridge","inventorySlot","(Ljava/lang/Object;)I",false));
                }
            }
            if(method.name.equals(a[5])&&method.desc.equals(a[6])&&insn instanceof MethodInsnNode){
                MethodInsnNode call=(MethodInsnNode)insn;
                if(call.owner.equals(a[7])&&call.name.equals(a[8])&&call.desc.equals(a[9])){
                    places++;method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,"ScaffoldSlotFixtureBridge","canPlace","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z",false));
                }
            }
        }
        if(slots!=1||places!=1)throw new AssertionError("Controller hook mismatch: slot="+slots+", canPlace="+places);
        if(!a[10].equals("-")){
            ClassWriter writer=new ClassWriter(0);node.accept(writer);Path path=Paths.get(a[10],a[1]+".class");
            Files.createDirectories(path.getParent());Files.write(path,writer.toByteArray());
        }
        System.out.println("Scaffold controller hook contract: "+a[1]+", slot sync and eligible-placement sites verified");
    }
}
