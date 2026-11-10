import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Build-time metadata index and a narrow access transformer for direct game references. */
public final class BuildSupport {
    static final Map<String,ClassNode> classes = new TreeMap<>();
    static void read(Path path) throws Exception {
        try (JarFile jar = new JarFile(path.toFile())) {
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (!entry.getName().endsWith(".class")) continue;
                try (InputStream stream = jar.getInputStream(entry)) {
                    ClassNode node = new ClassNode();
                    new ClassReader(stream).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    classes.putIfAbsent(node.name, node);
                }
            }
        }
    }
    static String owner(String name, String member, String desc, boolean method, Set<String> seen) {
        if (!seen.add(name)) return null;
        ClassNode node = classes.get(name);
        if (node == null && name.startsWith("java/")) {
            try (InputStream input = BuildSupport.class.getResourceAsStream("/" + name + ".class")) {
                if (input != null) {
                    node = new ClassNode();
                    new ClassReader(input).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    classes.put(name, node);
                }
            } catch (IOException failure) { throw new IllegalStateException(failure); }
        }
        if (node == null) return null;
        if (method) {
            for (MethodNode row : node.methods) if (row.name.equals(member) && row.desc.equals(desc)) return name;
        } else {
            for (FieldNode row : node.fields) if (row.name.equals(member) && row.desc.equals(desc)) return name;
        }
        if (node.superName != null) {
            String found = owner(node.superName, member, desc, method, seen);
            if (found != null) return found;
        }
        for (String parent : node.interfaces) {
            String found = owner(parent, member, desc, method, seen);
            if (found != null) return found;
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        // output hierarchy, output AT, named/SRG mod jar, game jar, dependency jars...
        for (int i = 3; i < args.length; i++) read(Paths.get(args[i]));
        read(Paths.get(args[2]));
        List<String> index = new ArrayList<>();
        for (ClassNode node : classes.values()) index.add(node.name + "\t" + node.access + "\t" +
            (node.superName == null ? "" : node.superName) + "\t" + String.join(",", node.interfaces));
        Files.write(Paths.get(args[0]), index, StandardCharsets.UTF_8);
        Set<String> access = new TreeSet<>();
        try (JarFile mod = new JarFile(args[2])) {
            for (JarEntry entry : Collections.list(mod.entries())) {
                if (!entry.getName().endsWith(".class")) continue;
                ClassNode code = new ClassNode();
                try (InputStream stream = mod.getInputStream(entry)) { new ClassReader(stream).accept(code, 0); }
                for (MethodNode method : code.methods) for (AbstractInsnNode insn : method.instructions) {
                    String name, member, desc; boolean isMethod;
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode call = (FieldInsnNode)insn;
                        name = call.owner; member = call.name; desc = call.desc; isMethod = false;
                    } else if (insn instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode)insn;
                        name = call.owner; member = call.name; desc = call.desc; isMethod = true;
                    } else continue;
                    if (!name.startsWith("net/minecraft/")) continue;
                    String declaration = owner(name, member, desc, isMethod, new HashSet<>());
                    if (declaration == null) throw new IllegalStateException("Unresolved game reference " + code.name + ": " + name + "." + member + desc);
                    ClassNode target = classes.get(declaration);
                    if ((target.access & Opcodes.ACC_PUBLIC) == 0) access.add("public " + declaration.replace('/', '.'));
                    if (isMethod) {
                        for (MethodNode row : target.methods) if (row.name.equals(member) && row.desc.equals(desc) && (row.access & Opcodes.ACC_PUBLIC) == 0)
                            access.add("public " + declaration.replace('/', '.') + " " + member + desc);
                    } else {
                        for (FieldNode row : target.fields) if (row.name.equals(member) && row.desc.equals(desc) && (row.access & Opcodes.ACC_PUBLIC) == 0)
                            access.add("public " + declaration.replace('/', '.') + " " + member);
                    }
                }
            }
        }
        Files.write(Paths.get(args[1]), access, StandardCharsets.UTF_8);
        System.out.println("Indexed " + classes.size() + " classes; " + access.size() + " access rules; game member references resolved.");
    }
}
