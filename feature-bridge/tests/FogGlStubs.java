import dev.rice.shaded.asm.*;
import dev.rice.shaded.asm.tree.*;
import java.nio.file.*;
import java.util.jar.*;

/** Test-only GL11 recorder; never embedded in the client. */
public final class FogGlStubs {
    public static void main(String[] args) throws Exception {
        ClassNode node=new ClassNode();
        try(JarFile jar=new JarFile(args[0]);java.io.InputStream input=jar.getInputStream(jar.getJarEntry("org/lwjgl/opengl/GL11.class"))){new ClassReader(input).accept(node,ClassReader.SKIP_FRAMES);}
        for(MethodNode method:node.methods){
            method.access&=~(Opcodes.ACC_NATIVE|Opcodes.ACC_ABSTRACT);method.instructions.clear();method.tryCatchBlocks.clear();method.localVariables=null;
            InsnList body=method.instructions;Type result=Type.getReturnType(method.desc);
            if(method.name.equals("<init>")){body.add(new VarInsnNode(Opcodes.ALOAD,0));body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,node.superName,"<init>","()V",false));}
            else if(method.name.equals("glFogi")&&method.desc.equals("(II)V")){body.add(new VarInsnNode(Opcodes.ILOAD,0));body.add(new VarInsnNode(Opcodes.ILOAD,1));body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"FeatureFixture","recordFogMode","(II)V",false));}
            else if(method.name.equals("glFogf")&&method.desc.equals("(IF)V")){body.add(new VarInsnNode(Opcodes.ILOAD,0));body.add(new VarInsnNode(Opcodes.FLOAD,1));body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"FeatureFixture","recordFogFloat","(IF)V",false));}
            else if(method.name.equals("glFog")&&method.desc.equals("(ILjava/nio/FloatBuffer;)V")){body.add(new VarInsnNode(Opcodes.ILOAD,0));body.add(new VarInsnNode(Opcodes.ALOAD,1));body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"FeatureFixture","recordFogColor",method.desc,false));}
            else if(method.name.equals("glGetFloat")&&method.desc.equals("(ILjava/nio/FloatBuffer;)V")){body.add(new VarInsnNode(Opcodes.ILOAD,0));body.add(new VarInsnNode(Opcodes.ALOAD,1));body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"FeatureFixture","validateGlFloatQuery",method.desc,false));}
            else if(method.name.equals("glGetInteger")&&method.desc.equals("(I)I")){body.add(new VarInsnNode(Opcodes.ILOAD,0));body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"FeatureFixture","queryHudGl","(I)I",false));}
            else if(java.util.Arrays.asList("glMatrixMode","glPushMatrix","glPopMatrix","glPushAttrib","glPopAttrib","glPushClientAttrib","glPopClientAttrib","glBegin","glEnd").contains(method.name)
                  &&(method.desc.equals("(I)V")||method.desc.equals("()V"))){
                int op=java.util.Arrays.asList("glMatrixMode","glPushMatrix","glPopMatrix","glPushAttrib","glPopAttrib","glPushClientAttrib","glPopClientAttrib","glBegin","glEnd").indexOf(method.name);
                body.add(new LdcInsnNode(op));if(method.desc.equals("(I)V"))body.add(new VarInsnNode(Opcodes.ILOAD,0));else body.add(new InsnNode(Opcodes.ICONST_0));
                body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"FeatureFixture","recordHudGl","(II)V",false));
            }
            else switch(result.getSort()){
                case Type.VOID:break;case Type.LONG:body.add(new InsnNode(Opcodes.LCONST_0));break;
                case Type.FLOAT:body.add(new InsnNode(Opcodes.FCONST_0));break;case Type.DOUBLE:body.add(new InsnNode(Opcodes.DCONST_0));break;
                case Type.OBJECT:case Type.ARRAY:body.add(new InsnNode(Opcodes.ACONST_NULL));break;default:body.add(new InsnNode(Opcodes.ICONST_0));
            }
            body.add(new InsnNode(result.getOpcode(Opcodes.IRETURN)));
        }
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES){@Override protected String getCommonSuperClass(String a,String b){return "java/lang/Object";}};
        node.accept(writer);Path target=Paths.get(args[1],"org/lwjgl/opengl/GL11.class");Files.createDirectories(target.getParent());Files.write(target,writer.toByteArray());
    }
}
