import dev.rice.forge.*;
import dev.rice.inject.Mappings;
import dev.rice.inject.transform.RicePatches;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Reproduces the two structural conflicts reported by Lunar's crash log. */
public final class VerifyLunarCompatibility {
    static byte[] read(JarFile jar, String name) throws Exception {
        try (InputStream input = jar.getInputStream(jar.getJarEntry(name + ".class"))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; int size;
            while ((size = input.read(buffer)) != -1) output.write(buffer, 0, size);
            return output.toByteArray();
        }
    }
    public static void main(String[] args) throws Exception {
        ForgeEnvironment.setDevelopment(Boolean.parseBoolean(args[0])); ForgeEnvironment.initialize();
        RiceTransformer transformer = new RiceTransformer();
        try (JarFile game = new JarFile(args[1])) {
            String screen = "net/minecraft/client/gui/GuiScreen";
            byte[] original = read(game, screen);
            if (!Arrays.equals(original, transformer.transform(screen.replace('/', '.'), screen.replace('/', '.'), original)))
                throw new AssertionError("GuiScreen input call sites changed; Lunar mixin targets must be preserved");
            for (RicePatches.ClassEdits patch : RicePatches.create(true))
                if (patch.target.equals(screen)) throw new AssertionError("Conflicting Forge GuiScreen patch registered");

            String manager = "net/minecraft/client/renderer/entity/RenderManager";
            ClassNode node = new ClassNode(); new ClassReader(read(game, manager)).accept(node, 0);
            String pitch = Mappings.mapField(manager, "playerViewX"), yaw = Mappings.mapField(manager, "playerViewY");
            MethodNode extra = new MethodNode(Opcodes.ACC_PUBLIC, "riceFixtureCameraSetter", "(FF)V", null, null);
            extra.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); extra.instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
            extra.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, manager, pitch, "F"));
            extra.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); extra.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
            extra.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, manager, yaw, "F"));
            extra.instructions.add(new InsnNode(Opcodes.RETURN)); extra.maxStack = 2; extra.maxLocals = 3;
            node.methods.add(extra);
            ClassWriter input = new ClassWriter(0); node.accept(input);
            byte[] result = transformer.transform(manager.replace('/', '.'), manager.replace('/', '.'), input.toByteArray());
            ClassNode transformed = new ClassNode(); new ClassReader(result).accept(transformed, ClassReader.EXPAND_FRAMES);
            int extraHooks = 0;
            for (MethodNode method : transformed.methods) {
                if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
                    new Analyzer<BasicValue>(new BasicVerifier()).analyze(manager, method);
                if (method.name.equals("riceFixtureCameraSetter")) for (AbstractInsnNode instruction : method.instructions)
                    if (instruction instanceof MethodInsnNode && ((MethodInsnNode)instruction).owner.equals("dev/rice/client/hook/GameHooks")) extraHooks++;
            }
            if (extraHooks != 2) throw new AssertionError("Additional camera writes did not receive both axis hooks");
            // A truly incompatible class must still fail instead of silently losing camera coverage.
            for (MethodNode method : node.methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode && instruction.getOpcode() == Opcodes.PUTFIELD &&
                    ((FieldInsnNode)instruction).owner.equals(manager) && ((FieldInsnNode)instruction).name.equals(pitch))
                    method.instructions.set(instruction, new InsnNode(Opcodes.POP2));
            }
            ClassWriter missing = new ClassWriter(0); node.accept(missing);
            try {
                transformer.transform(manager.replace('/', '.'), manager.replace('/', '.'), missing.toByteArray());
                throw new AssertionError("Missing camera axis accepted");
            } catch (IllegalStateException expected) {
                if (expected.getCause() == null || !expected.getCause().getMessage().contains("both pitch and yaw")) throw expected;
            }
        }
        System.out.println("PASS: Lunar-style additional camera setters; both axes verified; missing-axis rejection; GuiScreen remains byte-identical for Lunar mixins.");
    }
}
