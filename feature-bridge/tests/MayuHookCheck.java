import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Validates every Mayu-specific instruction replaced by the native hook registry. */
public final class MayuHookCheck {
    static final Map<String,String> classes=new HashMap<>(),methods=new HashMap<>(),fields=new HashMap<>();
    static String cls(String n){return classes.getOrDefault(n,n);}
    static String desc(String d){for(Map.Entry<String,String> e:classes.entrySet())d=d.replace("L"+e.getKey()+";","L"+e.getValue()+";");return d;}
    static String method(String c,String n,String d){
        String value=methods.get(c+"/"+n+d);if(value!=null)return value;
        if(n.equals("onUpdate"))return methods.getOrDefault("net/minecraft/entity/Entity/"+n+d,n);
        if(n.equals("onLivingUpdate"))return methods.getOrDefault("net/minecraft/entity/EntityLivingBase/"+n+d,n);
        return n;
    }
    static String field(String c,String n){return fields.getOrDefault(c+"/"+n,n);}
    static void call(JarFile jar,String c,String m,String md,String target,String name,String td,int opcode)throws Exception {
        ClassNode node=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(cls(c)+".class"))).accept(node,0);
        int count=0;
        for(MethodNode fn:node.methods)if(fn.name.equals(method(c,m,md))&&fn.desc.equals(desc(md)))
            for(AbstractInsnNode insn:fn.instructions.toArray())if(insn instanceof MethodInsnNode){
                MethodInsnNode i=(MethodInsnNode)insn;
                if(i.owner.equals(cls(target))&&i.name.equals(method(target,name,td))&&i.desc.equals(desc(td))&&i.getOpcode()==opcode)count++;
            }
        if(count!=1)throw new AssertionError(c+"."+m+" -> "+target+"."+name+" expected one call, got "+count);
    }
    public static void main(String[] args)throws Exception {
        if(!args[1].equals("-"))for(String line:Files.readAllLines(Paths.get(args[1]))){
            String[] p=line.split("\t");if(p[0].equals("CL"))classes.put(p[1],p[2]);
            if(p[0].equals("MD"))methods.put(p[1]+"/"+p[2]+p[3],p[4]);
            if(p[0].equals("FD"))fields.put(p[1]+"/"+p[2],p[3]);
        }
        String p="net/minecraft/client/entity/EntityPlayerSP",mc="net/minecraft/client/Minecraft",inv="net/minecraft/entity/player/InventoryPlayer";
        try(JarFile jar=new JarFile(args[0])){
            call(jar,p,"onUpdate","()V","net/minecraft/world/World","isBlockLoaded","(Lnet/minecraft/util/BlockPos;)Z",Opcodes.INVOKEVIRTUAL);
            call(jar,p,"onUpdate","()V",p,"onUpdateWalkingPlayer","()V",Opcodes.INVOKEVIRTUAL);
            call(jar,p,"onLivingUpdate","()V","net/minecraft/util/MovementInput","updatePlayerMoveState","()V",Opcodes.INVOKEVIRTUAL);
            call(jar,p,"onLivingUpdate","()V","net/minecraft/client/entity/AbstractClientPlayer","onLivingUpdate","()V",Opcodes.INVOKESPECIAL);
            call(jar,mc,"runTick","()V",inv,"changeCurrentItem","(I)V",Opcodes.INVOKEVIRTUAL);
            call(jar,mc,"runTick","()V","net/minecraft/client/gui/GuiIngame","updateTick","()V",Opcodes.INVOKEVIRTUAL);
            call(jar,mc,"runTick","()V",mc,"sendClickBlockToController","(Z)V",Opcodes.INVOKESPECIAL);
            ClassNode node=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(cls(mc)+".class"))).accept(node,0);int writes=0;
            for(MethodNode fn:node.methods)if(fn.name.equals(method(mc,"runTick","()V")))
                for(AbstractInsnNode insn:fn.instructions.toArray())if(insn instanceof FieldInsnNode){
                    FieldInsnNode i=(FieldInsnNode)insn;
                    if(i.owner.equals(cls(inv))&&i.name.equals(field(inv,"currentItem"))&&i.getOpcode()==Opcodes.PUTFIELD)writes++;
                }
            if(writes!=1)throw new AssertionError("Number-key interception expected one slot write, got "+writes);
        }
        ClassNode hooks=new ClassNode();new ClassReader(Files.readAllBytes(Paths.get(args[2]))).accept(hooks,0);
        for(String name:new String[]{"mayuLoaded","mayuLiving","mayuScroll","mayuSelect","mayuBlockClick","mayuGuiUpdate","movementInput","walkingPlayer"})
            if(hooks.methods.stream().noneMatch(m->m.name.equals(name)&&(m.access&Opcodes.ACC_NATIVE)!=0))throw new AssertionError("Missing "+name);
        System.out.println("Mayu hook contracts: update, packet, input, living, scroll, number-key, block interaction verified: "+args[0]);
    }
}
