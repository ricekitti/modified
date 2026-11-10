/* Build-time tool for the Mayu port (tools/port-mayu.py). It never loads or runs classes from its inputs. */
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * <pre>
 * compile-view &lt;named.jar&gt; &lt;out.jar&gt;
 *     Writes the two Minecraft classes javac needs to compile the UNMODIFIED Mayu sources the way Forge exposes them:
 *     EnumFacing with VALUES public (Forge access transformer), Item with Forge's getDigSpeed/getToolClasses/getMetadata
 *     (ItemStack) signatures. Compile-only: put it before the named jar on javac's classpath; it is never packaged.
 *
 * adapt &lt;classes dir&gt; &lt;tools/mayu-adapters.txt&gt; &lt;reference classes dir&gt; &lt;named.jar&gt; &lt;report.txt&gt;
 *     1. Every compiled upstream Mayu class is compared, method by method, instruction by instruction, with Mayu's own
 *        compiled class (the reference); any difference fails the build.
 *     2. Exactly the adapter rewrites below are applied to myau/** (host bridge included), counted per class and rule;
 *        the counts must equal tools/mayu-adapters.txt.
 *     3. Link check over the whole classes dir: every Minecraft field/method reference must resolve in vanilla
 *        (named jar), and no instruction may reference net/minecraftforge, org/spongepowered or myau/mixin any more.
 * </pre>
 */
public final class MayuBytecode {
    static final String FORGE = "myau/host/MayuForge";
    static final String ACCESSORS = "myau/host/MayuAccessors";
    static final String OPENB4 = "dev/rice/client/script/openb4/";
    static final String OPENB4_FORGE = OPENB4 + "OpenB4Forge";
    /** Upstream code the adapters apply to: the Mayu port and the OpenB4 script runtime (+ the script). */
    static final List<String> SCOPES = Arrays.asList("myau");
    static final Set<String> HOST_FILES = new HashSet<>(Arrays.asList(
        "myau/host/MayuForge", "myau/host/MayuAccessors", "myau/host/MayuShadows",
        "myau/MyauClient", "myau/InjectScaffoldView", "myau/LongJumpModule", "myau/TargetStrafeModule", "myau/KillAuraModule", OPENB4_FORGE));
    /** Explicit source-level Rice extensions; still receive every counted Forge/accessor rewrite and link check. */
    static final Set<String> SOURCE_ADAPTED_FILES = new HashSet<>(Arrays.asList("myau/BedNukerModule"));

    public static void main(String[] args) throws Exception {
        if (args.length == 3 && args[0].equals("compile-view")) compileView(Paths.get(args[1]), Paths.get(args[2]));
        else if (args.length == 6 && args[0].equals("adapt")) adapt(Paths.get(args[1]), Paths.get(args[2]), Paths.get(args[3]), Paths.get(args[4]), Paths.get(args[5]));
        else if (args.length == 4 && args[0].equals("source-file")) sourceFile(Paths.get(args[1]), args[2], args[3]);
        else throw new IllegalArgumentException("usage: compile-view <named.jar> <out.jar> | adapt <classes> <adapters.txt> <reference> <named.jar> <report>"
            + " | source-file <classes> <internal class name> <file name>");
    }

    /**
     * Sets a compiled class's SourceFile attribute (OpenB4 compiles each script as a class with a random 200-character
     * name; ScriptInvoker.currentScriptName finds the running script by that file name in stack traces).
     */
    static void sourceFile(Path classes, String internalName, String fileName) throws IOException {
        Path file = classes.resolve(internalName + ".class");
        ClassNode node = read(file);
        node.sourceFile = fileName;
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        Files.write(file, writer.toByteArray());
        System.out.println("SourceFile of " + internalName + " = " + fileName.length() + "-character name");
    }

    // ---------------------------------------------------------------------------------------------- compile view
    static void compileView(Path named, Path out) throws IOException {
        try (JarFile jar = new JarFile(named.toFile()); JarOutputStream view = new JarOutputStream(Files.newOutputStream(out))) {
            ClassNode facing = read(jar, "net/minecraft/util/EnumFacing");
            int widened = 0;
            for (FieldNode field : facing.fields) {
                if (field.name.equals("VALUES") && field.desc.equals("[Lnet/minecraft/util/EnumFacing;")) {
                    field.access = (field.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
                    widened++;
                }
            }
            require(widened == 1, "EnumFacing.VALUES not found");
            ClassNode item = read(jar, "net/minecraft/item/Item");
            stub(item, "getDigSpeed", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)F", null);
            stub(item, "getToolClasses", "(Lnet/minecraft/item/ItemStack;)Ljava/util/Set;", "(Lnet/minecraft/item/ItemStack;)Ljava/util/Set<Ljava/lang/String;>;");
            stub(item, "getMetadata", "(Lnet/minecraft/item/ItemStack;)I", null);
            stub(item, "getRegistryName", "()Ljava/lang/String;", null);
            ClassNode block = read(jar, "net/minecraft/block/Block");
            stub(block, "getRegistryName", "()Ljava/lang/String;", null);
            for (ClassNode node : Arrays.asList(facing, item, block)) {
                ClassWriter writer = new ClassWriter(0);
                node.accept(writer);
                view.putNextEntry(new JarEntry(node.name + ".class"));
                view.write(writer.toByteArray());
                view.closeEntry();
            }
        }
        System.out.println("compile view: EnumFacing.VALUES public; Item + getDigSpeed/getToolClasses/getMetadata(ItemStack)");
    }

    static void stub(ClassNode owner, String name, String desc, String signature) {
        for (MethodNode m : owner.methods) require(!(m.name.equals(name) && m.desc.equals(desc)), "vanilla already has " + name + desc);
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, signature, null);
        method.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        method.instructions.add(new InsnNode(Opcodes.ATHROW));
        method.maxStack = 1;
        method.maxLocals = Type.getArgumentsAndReturnSizes(desc) >> 2;
        owner.methods.add(method);
    }

    // ---------------------------------------------------------------------------------------------- adapt
    static final class Rule {
        final int opcode; final String owner, name, desc; final int newOpcode; final String newOwner, newName, newDesc; final String label;
        Rule(int opcode, String owner, String name, String desc, int newOpcode, String newOwner, String newName, String newDesc, String label) {
            this.opcode = opcode; this.owner = owner; this.name = name; this.desc = desc;
            this.newOpcode = newOpcode; this.newOwner = newOwner; this.newName = newName; this.newDesc = newDesc; this.label = label;
        }
    }

    static final List<Rule> RULES = Arrays.asList(
        new Rule(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/multiplayer/PlayerControllerMP", "onPlayerRightClick", "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/Vec3;)Z",
            Opcodes.INVOKESTATIC, "dev/rice/client/mayu/MayuPlacement", "place", "(Lnet/minecraft/client/multiplayer/PlayerControllerMP;Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/Vec3;)Z", "Scaffold placement cancellation rollback"),
        new Rule(Opcodes.GETSTATIC, "net/minecraft/util/EnumFacing", "VALUES", "[Lnet/minecraft/util/EnumFacing;",
            Opcodes.INVOKESTATIC, FORGE, "enumFacingValues", "()[Lnet/minecraft/util/EnumFacing;", "EnumFacing.VALUES (Forge AT)"),
        new Rule(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item", "getDigSpeed", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)F",
            Opcodes.INVOKESTATIC, FORGE, "getDigSpeed", "(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)F", "Item.getDigSpeed (Forge)"),
        new Rule(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item", "getToolClasses", "(Lnet/minecraft/item/ItemStack;)Ljava/util/Set;",
            Opcodes.INVOKESTATIC, FORGE, "getToolClasses", "(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)Ljava/util/Set;", "Item.getToolClasses (Forge)"),
        new Rule(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item", "getMetadata", "(Lnet/minecraft/item/ItemStack;)I",
            Opcodes.INVOKESTATIC, FORGE, "getMetadata", "(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)I", "Item.getMetadata(ItemStack) (Forge)"),
        new Rule(Opcodes.INVOKESTATIC, "net/minecraftforge/common/ForgeHooks", "onPlayerAttackTarget", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)Z",
            Opcodes.INVOKESTATIC, FORGE, "onPlayerAttackTarget", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)Z", "ForgeHooks.onPlayerAttackTarget (Forge)"),
        new Rule(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item", "getRegistryName", "()Ljava/lang/String;",
            Opcodes.INVOKESTATIC, OPENB4_FORGE, "getRegistryName", "(Lnet/minecraft/item/Item;)Ljava/lang/String;", "Item.getRegistryName (Forge)"),
        new Rule(Opcodes.INVOKEVIRTUAL, "net/minecraft/block/Block", "getRegistryName", "()Ljava/lang/String;",
            Opcodes.INVOKESTATIC, OPENB4_FORGE, "getRegistryName", "(Lnet/minecraft/block/Block;)Ljava/lang/String;", "Block.getRegistryName (Forge)"));

    static void adapt(Path classes, Path expectedFile, Path reference, Path named, Path report) throws IOException {
        Map<String, Path> files = new TreeMap<>();
        for (String scope : SCOPES) {
            try (Stream<Path> walk = Files.walk(classes.resolve(scope))) {
                walk.filter(p -> p.toString().endsWith(".class")).forEach(p -> files.put(classes.relativize(p).toString().replace('\\', '/').replaceAll("\\.class$", ""), p));
            }
        }
        Map<String, String> mixinTargets = new HashMap<>();
        for (Map.Entry<String, Path> e : files.entrySet()) {
            if (e.getKey().startsWith("myau/mixin/Accessor")) mixinTargets.put(e.getKey(), mixinTarget(read(e.getValue())));
        }
        ClassNode accessorHost = read(files.get(ACCESSORS));
        Set<String> hostMethods = accessorHost.methods.stream().map(m -> m.name + m.desc).collect(Collectors.toSet());

        // 1. instruction-level comparison with Mayu's own compiled classes (before any rewrite)
        List<String> compared = new ArrayList<>(), problems = new ArrayList<>();
        for (Map.Entry<String, Path> e : files.entrySet()) {
            String name = e.getKey();
            if (!name.startsWith("myau/") || name.startsWith("myau/host/") || HOST_FILES.contains(name) || isSourceAdapted(name)) continue;
            Path ref = reference.resolve(name + ".class");
            if (!Files.exists(ref)) { problems.add("no reference class for " + name); continue; }
            String diff = compare(read(ref), read(e.getValue()));
            if (diff != null) problems.add(name + ": " + diff);
            compared.add(name);
        }
        require(problems.isEmpty(), "Upstream bytecode mismatch:\n  " + String.join("\n  ", problems));

        // 2. counted rewrites
        Map<String, Integer> counts = new TreeMap<>();
        for (Map.Entry<String, Path> e : files.entrySet()) {
            String name = e.getKey();
            if (HOST_FILES.contains(name)) continue;
            ClassNode node = read(e.getValue());
            boolean changed = false;
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    String label = null;
                    AbstractInsnNode replacement = null;
                    if (insn instanceof FieldInsnNode || insn instanceof MethodInsnNode) {
                        String owner = insn instanceof FieldInsnNode ? ((FieldInsnNode)insn).owner : ((MethodInsnNode)insn).owner;
                        String member = insn instanceof FieldInsnNode ? ((FieldInsnNode)insn).name : ((MethodInsnNode)insn).name;
                        String desc = insn instanceof FieldInsnNode ? ((FieldInsnNode)insn).desc : ((MethodInsnNode)insn).desc;
                        for (Rule rule : RULES) {
                            if (insn.getOpcode() == rule.opcode && owner.equals(rule.owner) && member.equals(rule.name) && desc.equals(rule.desc)) {
                                replacement = new MethodInsnNode(rule.newOpcode, rule.newOwner, rule.newName, rule.newDesc, false);
                                label = rule.label;
                            }
                        }
                        if (replacement == null && insn.getOpcode() == Opcodes.INVOKEINTERFACE && owner.startsWith("myau/mixin/Accessor")) {
                            String target = mixinTargets.get(owner);
                            require(target != null, "unknown accessor " + owner);
                            String simple = owner.substring("myau/mixin/".length());
                            String hostDesc = "(L" + target + ";" + desc.substring(1);
                            require(hostMethods.contains(simple + "_" + member + hostDesc), "MayuAccessors lacks " + simple + "_" + member + hostDesc);
                            replacement = new MethodInsnNode(Opcodes.INVOKESTATIC, ACCESSORS, simple + "_" + member, hostDesc, false);
                            label = "Mixin accessor " + simple + "." + member;
                        }
                    } else if (insn.getOpcode() == Opcodes.CHECKCAST && ((TypeInsnNode)insn).desc.startsWith("myau/mixin/Accessor")) {
                        String target = mixinTargets.get(((TypeInsnNode)insn).desc);
                        require(target != null, "unknown accessor cast " + ((TypeInsnNode)insn).desc);
                        replacement = new TypeInsnNode(Opcodes.CHECKCAST, target);
                        label = "Mixin accessor cast " + ((TypeInsnNode)insn).desc.substring("myau/mixin/".length()) + " -> " + target;
                    }
                    if (replacement != null) {
                        method.instructions.set(insn, replacement);
                        counts.merge(name + "\t" + label, 1, Integer::sum);
                        changed = true;
                    }
                }
                // A local of an accessor type (javac keeps the cast's type in the StackMapTable and, with -g, the
                // LocalVariableTable) becomes the @Mixin target type too, so the verifier sees the rewritten cast's type.
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (!(insn instanceof FrameNode)) continue;
                    FrameNode frame = (FrameNode)insn;
                    for (List<Object> types : Arrays.asList(frame.local, frame.stack)) {
                        if (types == null) continue;
                        for (int i = 0; i < types.size(); i++) {
                            Object type = types.get(i);
                            if (type instanceof String && mixinTargets.containsKey(type)) {
                                types.set(i, mixinTargets.get(type));
                                counts.merge(name + "\tMixin accessor frame type " + ((String)type).substring("myau/mixin/".length()) + " -> " + mixinTargets.get(type), 1, Integer::sum);
                                changed = true;
                            }
                        }
                    }
                }
                if (method.localVariables != null) {
                    for (LocalVariableNode local : method.localVariables) {
                        String type = local.desc.startsWith("L") ? local.desc.substring(1, local.desc.length() - 1) : null;
                        if (type != null && mixinTargets.containsKey(type)) {
                            local.desc = "L" + mixinTargets.get(type) + ";";
                            counts.merge(name + "\tMixin accessor local type " + type.substring("myau/mixin/".length()) + " -> " + mixinTargets.get(type), 1, Integer::sum);
                            changed = true;
                        }
                    }
                }
            }
            if (changed) {
                ClassWriter writer = new ClassWriter(0);
                node.accept(writer);
                Files.write(e.getValue(), writer.toByteArray());
            }
        }
        Map<String, Integer> expected = new TreeMap<>();
        for (String line : Files.readAllLines(expectedFile, StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            expected.put(p[0] + "\t" + p[1], Integer.parseInt(p[2]));
        }

        // 3. link check over everything compiled
        Map<String, ClassNode> vanilla = new HashMap<>();
        try (JarFile jar = new JarFile(named.toFile())) {
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (entry.getName().endsWith(".class")) {
                    ClassNode node = new ClassNode();
                    try (InputStream in = jar.getInputStream(entry)) { new ClassReader(in).accept(node, ClassReader.SKIP_CODE); }
                    vanilla.put(node.name, node);
                }
            }
        }
        List<String> unresolved = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> walk = Files.walk(classes)) {
            for (Path p : walk.filter(x -> x.toString().endsWith(".class")).collect(Collectors.toList())) {
                ClassNode node = read(p);
                for (MethodNode method : node.methods) {
                    for (AbstractInsnNode insn : method.instructions.toArray()) {
                        String owner = null, member = null, desc = null; boolean field = false;
                        if (insn instanceof FieldInsnNode) { owner = ((FieldInsnNode)insn).owner; member = ((FieldInsnNode)insn).name; desc = ((FieldInsnNode)insn).desc; field = true; }
                        else if (insn instanceof MethodInsnNode) { owner = ((MethodInsnNode)insn).owner; member = ((MethodInsnNode)insn).name; desc = ((MethodInsnNode)insn).desc; }
                        else if (insn instanceof TypeInsnNode) owner = ((TypeInsnNode)insn).desc;
                        if (owner == null) continue;
                        if (owner.startsWith("net/minecraftforge/") || owner.startsWith("org/spongepowered/") || owner.startsWith("myau/mixin/"))
                            unresolved.add(node.name + "." + method.name + ": references " + owner + (member == null ? "" : "." + member));
                        if (member != null && owner.startsWith("net/minecraft/")) {
                            checked++;
                            if (!resolves(vanilla, owner, member, desc, field)) unresolved.add(node.name + "." + method.name + ": " + owner + "." + member + desc + " not in vanilla 1.8.9");
                        }
                    }
                }
            }
        }

        StringBuilder out = new StringBuilder();
        out.append("# Mayu bytecode adapter report\n# upstream classes compared instruction-by-instruction with Mayu's compiled classes: ")
           .append(compared.size()).append("\n");
        out.append("# explicit source adaptations still rewritten/link-checked: ").append(SOURCE_ADAPTED_FILES).append('\n');
        for (Map.Entry<String, Integer> e : counts.entrySet()) out.append(e.getKey()).append('\t').append(e.getValue()).append('\n');
        out.append("# Minecraft member references link-checked against vanilla: ").append(checked).append('\n');
        Files.write(report, out.toString().getBytes(StandardCharsets.UTF_8));
        require(counts.equals(expected), "Adapter counts differ from " + expectedFile + "\n  actual: " + counts + "\n  expected: " + expected);
        require(unresolved.isEmpty(), "Link check failed:\n  " + String.join("\n  ", unresolved));
        System.out.println("PASS: " + compared.size() + " upstream classes instruction-identical to Mayu's compiled classes; "
            + counts.values().stream().mapToInt(Integer::intValue).sum() + " counted adapter instructions; " + checked + " Minecraft references link to vanilla");
    }

    static boolean resolves(Map<String, ClassNode> vanilla, String owner, String member, String desc, boolean field) {
        if (owner.startsWith("[")) return true;
        Deque<String> todo = new ArrayDeque<>(Collections.singleton(owner));
        Set<String> seen = new HashSet<>();
        while (!todo.isEmpty()) {
            String name = todo.pop();
            if (!seen.add(name)) continue;
            ClassNode node = vanilla.get(name);
            if (node == null) {
                if (name.startsWith("java/") || name.startsWith("javax/")) return true; // JDK supertypes (Object, Enum, ...)
                continue;
            }
            if (field ? node.fields.stream().anyMatch(f -> f.name.equals(member) && f.desc.equals(desc))
                      : node.methods.stream().anyMatch(m -> m.name.equals(member) && m.desc.equals(desc))) return true;
            if (node.superName != null) todo.add(node.superName);
            todo.addAll(node.interfaces);
        }
        return false;
    }

    static boolean isSourceAdapted(String name) {
        for (String adapted : SOURCE_ADAPTED_FILES) if (name.equals(adapted) || name.startsWith(adapted + "$")) return true;
        return false;
    }

    static String compare(ClassNode reference, ClassNode ours) {
        if (!Objects.equals(reference.superName, ours.superName) || !new HashSet<>(reference.interfaces).equals(new HashSet<>(ours.interfaces)))
            return "class header differs";
        Set<String> refFields = reference.fields.stream().map(f -> f.access + " " + f.name + f.desc).collect(Collectors.toSet());
        Set<String> ourFields = ours.fields.stream().map(f -> f.access + " " + f.name + f.desc).collect(Collectors.toSet());
        if (!refFields.equals(ourFields)) return "fields differ: " + diff(refFields, ourFields);
        Map<String, MethodNode> refMethods = reference.methods.stream().collect(Collectors.toMap(m -> m.name + m.desc, m -> m));
        Map<String, MethodNode> ourMethods = ours.methods.stream().collect(Collectors.toMap(m -> m.name + m.desc, m -> m));
        if (!refMethods.keySet().equals(ourMethods.keySet())) return "methods differ: " + diff(refMethods.keySet(), ourMethods.keySet());
        for (String key : refMethods.keySet()) {
            String a = text(refMethods.get(key)), b = text(ourMethods.get(key));
            if (refMethods.get(key).access != ourMethods.get(key).access) return key + " access differs";
            if (!a.equals(b)) return key + " instructions differ:\n--- mayu\n" + a + "--- ours\n" + b;
        }
        return null;
    }

    static String diff(Set<String> a, Set<String> b) {
        Set<String> onlyA = new TreeSet<>(a); onlyA.removeAll(b);
        Set<String> onlyB = new TreeSet<>(b); onlyB.removeAll(a);
        return "only mayu " + onlyA + ", only ours " + onlyB;
    }

    /**
     * Normalised instruction listing: one line per instruction with every operand, labels numbered by position in the
     * method, line-number entries kept, try/catch blocks listed. Frames and local-variable names (debug info) excluded.
     */
    static String text(MethodNode method) {
        // Only labels the code itself uses (jumps, switches, try/catch, line numbers) are numbered and printed; labels that
        // exist only for debug tables (javac -g local-variable ranges) are not part of the code.
        Set<LabelNode> used = new HashSet<>();
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof JumpInsnNode) used.add(((JumpInsnNode)insn).label);
            else if (insn instanceof LineNumberNode) used.add(((LineNumberNode)insn).start);
            else if (insn instanceof TableSwitchInsnNode) { used.add(((TableSwitchInsnNode)insn).dflt); used.addAll(((TableSwitchInsnNode)insn).labels); }
            else if (insn instanceof LookupSwitchInsnNode) { used.add(((LookupSwitchInsnNode)insn).dflt); used.addAll(((LookupSwitchInsnNode)insn).labels); }
        }
        for (TryCatchBlockNode block : method.tryCatchBlocks) { used.add(block.start); used.add(block.end); used.add(block.handler); }
        Map<LabelNode, Integer> labels = new HashMap<>();
        for (AbstractInsnNode insn : method.instructions.toArray()) if (insn instanceof LabelNode && used.contains(insn)) labels.put((LabelNode)insn, labels.size());
        StringBuilder out = new StringBuilder();
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof FrameNode) continue;
            if (insn instanceof LabelNode) { if (labels.containsKey(insn)) out.append('L').append(labels.get(insn)).append(":\n"); continue; }
            if (insn instanceof LineNumberNode) { out.append("  LINE ").append(((LineNumberNode)insn).line).append(" L").append(labels.get(((LineNumberNode)insn).start)).append('\n'); continue; }
            out.append("  ").append(insn.getOpcode());
            if (insn instanceof VarInsnNode) out.append(' ').append(((VarInsnNode)insn).var);
            else if (insn instanceof IntInsnNode) out.append(' ').append(((IntInsnNode)insn).operand);
            else if (insn instanceof TypeInsnNode) out.append(' ').append(((TypeInsnNode)insn).desc);
            else if (insn instanceof FieldInsnNode) { FieldInsnNode f = (FieldInsnNode)insn; out.append(' ').append(f.owner).append('.').append(f.name).append(' ').append(f.desc); }
            else if (insn instanceof MethodInsnNode) { MethodInsnNode m = (MethodInsnNode)insn; out.append(' ').append(m.owner).append('.').append(m.name).append(m.desc).append(m.itf ? " itf" : ""); }
            else if (insn instanceof JumpInsnNode) out.append(" L").append(labels.get(((JumpInsnNode)insn).label));
            else if (insn instanceof LdcInsnNode) { Object c = ((LdcInsnNode)insn).cst; out.append(' ').append(c.getClass().getSimpleName()).append(' ').append(c); }
            else if (insn instanceof IincInsnNode) out.append(' ').append(((IincInsnNode)insn).var).append(' ').append(((IincInsnNode)insn).incr);
            else if (insn instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode t = (TableSwitchInsnNode)insn;
                out.append(' ').append(t.min).append("..").append(t.max).append(" default L").append(labels.get(t.dflt));
                for (LabelNode l : t.labels) out.append(" L").append(labels.get(l));
            } else if (insn instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode t = (LookupSwitchInsnNode)insn;
                out.append(" keys ").append(t.keys).append(" default L").append(labels.get(t.dflt));
                for (LabelNode l : t.labels) out.append(" L").append(labels.get(l));
            } else if (insn instanceof MultiANewArrayInsnNode) out.append(' ').append(((MultiANewArrayInsnNode)insn).desc).append(' ').append(((MultiANewArrayInsnNode)insn).dims);
            else if (insn instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode d = (InvokeDynamicInsnNode)insn;
                out.append(' ').append(d.name).append(d.desc).append(" bsm ").append(d.bsm).append(' ').append(Arrays.toString(d.bsmArgs));
            }
            out.append('\n');
        }
        for (TryCatchBlockNode block : method.tryCatchBlocks)
            out.append("  TRY L").append(labels.get(block.start)).append(" L").append(labels.get(block.end)).append(" L").append(labels.get(block.handler)).append(' ').append(block.type).append('\n');
        return out.toString();
    }

    static String mixinTarget(ClassNode accessor) {
        List<AnnotationNode> all = new ArrayList<>();
        if (accessor.invisibleAnnotations != null) all.addAll(accessor.invisibleAnnotations);
        if (accessor.visibleAnnotations != null) all.addAll(accessor.visibleAnnotations);
        for (AnnotationNode a : all) {
            if (a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                for (int i = 0; i < a.values.size(); i += 2) {
                    if (a.values.get(i).equals("value")) {
                        List<?> types = (List<?>)a.values.get(i + 1);
                        require(types.size() == 1, accessor.name + " targets several classes");
                        return ((Type)types.get(0)).getInternalName();
                    }
                }
            }
        }
        throw new IllegalStateException(accessor.name + " has no @Mixin target");
    }

    static ClassNode read(Path path) throws IOException {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, 0);
        return node;
    }

    static ClassNode read(JarFile jar, String name) throws IOException {
        JarEntry entry = jar.getJarEntry(name + ".class");
        require(entry != null, "missing " + name);
        ClassNode node = new ClassNode();
        try (InputStream in = jar.getInputStream(entry)) { new ClassReader(in).accept(node, 0); }
        return node;
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
