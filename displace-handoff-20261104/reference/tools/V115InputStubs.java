import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.ClassWriter;
import dev.rice.shaded.asm.Opcodes;
import dev.rice.shaded.asm.Type;
import dev.rice.shaded.asm.tree.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * Writes test-only copies of org.lwjgl.input.Keyboard and Mouse into a directory that the headless V115Check puts
 * ahead of LWJGL on its classpath. Upstream OpenJade Scaffold/Clutch poll physical keys (Keyboard.isKeyDown /
 * Mouse.isButtonDown), and those throw without a native device.
 *
 * <p>Every method keeps its signature from the real LWJGL class. isKeyDown and isButtonDown read V115Input; every
 * other method (and the static initializer) returns a default value: 0, false, null, or true for isCreated. Never
 * shipped; used only by the guarded build's check step.
 *
 * <p>usage: java V115InputStubs <lwjgl jar> <output dir>
 */
public final class V115InputStubs {
   public static void main(String[] args) throws Exception {
      Path out = Paths.get(args[1]);
      try (JarFile jar = new JarFile(args[0])) {
         for (String name : new String[]{"org/lwjgl/input/Keyboard", "org/lwjgl/input/Mouse"}) {
            ClassNode node = new ClassNode();
            try (InputStream in = jar.getInputStream(jar.getJarEntry(name + ".class"))) {
               new ClassReader(read(in)).accept(node, ClassReader.SKIP_FRAMES);
            }
            for (MethodNode method : node.methods) {
               if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
                  method.access &= ~Opcodes.ACC_NATIVE;
               }
               method.instructions.clear();
               method.tryCatchBlocks.clear();
               method.localVariables = null;
               InsnList body = new InsnList();
               Type ret = Type.getReturnType(method.desc);
               if (method.name.equals("<init>")) {
                  body.add(new VarInsnNode(Opcodes.ALOAD, 0));
                  body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, "<init>", "()V", false));
               } else if (method.name.equals("isKeyDown") && method.desc.equals("(I)Z")) {
                  body.add(new VarInsnNode(Opcodes.ILOAD, 0));
                  body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "V115Input", "key", "(I)Z", false));
               } else if (method.name.equals("isButtonDown") && method.desc.equals("(I)Z")) {
                  body.add(new VarInsnNode(Opcodes.ILOAD, 0));
                  body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "V115Input", "button", "(I)Z", false));
               } else if (method.name.equals("getButtonCount") && method.desc.equals("()I")) {
                  body.add(new IntInsnNode(Opcodes.BIPUSH, 16));
               } else if (method.name.equals("isCreated") && method.desc.equals("()Z")) {
                  body.add(new InsnNode(Opcodes.ICONST_1));
               } else {
                  switch (ret.getSort()) {
                     case Type.VOID: break;
                     case Type.LONG: body.add(new InsnNode(Opcodes.LCONST_0)); break;
                     case Type.FLOAT: body.add(new InsnNode(Opcodes.FCONST_0)); break;
                     case Type.DOUBLE: body.add(new InsnNode(Opcodes.DCONST_0)); break;
                     case Type.OBJECT: case Type.ARRAY: body.add(new InsnNode(Opcodes.ACONST_NULL)); break;
                     default: body.add(new InsnNode(Opcodes.ICONST_0));
                  }
               }
               body.add(new InsnNode(ret.getOpcode(Opcodes.IRETURN)));
               method.instructions.add(body);
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
               @Override protected String getCommonSuperClass(String a, String b) { return "java/lang/Object"; }
            };
            node.accept(writer);
            Path target = out.resolve(name + ".class");
            Files.createDirectories(target.getParent());
            Files.write(target, writer.toByteArray());
         }
      }
      System.out.println("wrote headless Keyboard/Mouse stand-ins to " + out);
   }

   private static byte[] read(InputStream in) throws Exception {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] part = new byte[8192];
      for (int r; (r = in.read(part)) >= 0; ) buffer.write(part, 0, r);
      return buffer.toByteArray();
   }
}
