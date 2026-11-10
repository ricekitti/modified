import dev.rice.forge.*;
import dev.rice.inject.transform.RicePatches;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Transforms real Minecraft classes, checks data flow and links every generated Rice hook. */
public final class VerifyForge {
    public static void main(String[] args) throws Exception {
        ForgeEnvironment.setDevelopment(Boolean.parseBoolean(args[0]));
        ForgeEnvironment.initialize();
        RiceTransformer transformer = new RiceTransformer();
        int count = 0, methods = 0, hooks = 0;
        Map<String, ClassNode> own = new HashMap<>();
        try (JarFile mod = new JarFile(args[2])) {
            for (JarEntry entry : Collections.list(mod.entries())) {
                if (!entry.getName().endsWith(".class")) continue;
                ClassNode node = new ClassNode();
                try (InputStream stream = mod.getInputStream(entry)) { new ClassReader(stream).accept(node, ClassReader.SKIP_CODE); }
                own.put(node.name, node);
                for (MethodNode method : node.methods) if ((method.access & Opcodes.ACC_NATIVE) != 0)
                    throw new AssertionError("Native method shipped: " + node.name + "." + method.name);
            }
        }
        try (JarFile game = new JarFile(args[1])) {
            for (RicePatches.ClassEdits patch : RicePatches.create(true)) {
                String name = patch.getRuntimeTarget();
                JarEntry entry = game.getJarEntry(name + ".class");
                if (entry == null) throw new AssertionError("Missing game patch target " + name);
                byte[] original;
                try (InputStream stream = game.getInputStream(entry)) { original = read(stream); }
                byte[] result = transformer.transform(name.replace('/', '.'), name.replace('/', '.'), original);
                if (Arrays.equals(original, result)) throw new AssertionError("Unchanged target " + name);
                if (!Arrays.equals(result, transformer.transform(name.replace('/', '.'), name.replace('/', '.'), result)))
                    throw new AssertionError("Duplicate transformation " + name);
                ClassNode node = new ClassNode();
                new ClassReader(result).accept(node, ClassReader.EXPAND_FRAMES);
                for (MethodNode method : node.methods) {
                    if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                        try { new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name, method); }
                        catch (AnalyzerException failure) { throw new AssertionError(node.name + "." + method.name + method.desc, failure); }
                        methods++;
                    }
                    for (AbstractInsnNode insn : method.instructions) if (insn instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode)insn;
                        if (!call.owner.startsWith("dev/rice/") && !call.owner.startsWith("myau/host/")) continue;
                        ClassNode target = own.get(call.owner);
                        if (target == null) throw new AssertionError("Missing hook owner " + call.owner);
                        boolean found = false;
                        for (MethodNode candidate : target.methods) if (candidate.name.equals(call.name) && candidate.desc.equals(call.desc)) found = true;
                        if (!found) throw new AssertionError("Missing hook " + call.owner + "." + call.name + call.desc);
                        hooks++;
                    }
                }
                count++;
            }
        }
        if (transformer.transform("x", "x", null) != null) throw new AssertionError("Null transform");
        if (!Hierarchy.common("[Ljava/lang/String;", "[Ljava/lang/Integer;").equals("[Ljava/lang/Object;")) throw new AssertionError("Array frame merge");
        if (!Hierarchy.common("[[I", "[[F").equals("[Ljava/lang/Object;")) throw new AssertionError("Primitive array frame merge");
        System.out.println("PASS: " + count + " game classes, " + methods + " methods, " + hooks + " linked hooks; duplicate-transform guard; no JNI.");
    }
    static byte[] read(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int length;
        while ((length = stream.read(buffer)) != -1) output.write(buffer, 0, length);
        return output.toByteArray();
    }
}
