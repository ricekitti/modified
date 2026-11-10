package dev.rice.forge;

import dev.rice.inject.Platform;
import dev.rice.inject.transform.RicePatches;
import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.ClassWriter;
import dev.rice.shaded.asm.tree.ClassNode;
import dev.rice.shaded.asm.tree.MethodNode;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.launchwrapper.IClassTransformer;

/** Installs existing semantic hooks at class load, without JNI/JVMTI or attachment. */
public final class RiceTransformer implements IClassTransformer {
    private static Map<String, RicePatches.ClassEdits> patches;
    private static int transformed;

    private static synchronized Map<String, RicePatches.ClassEdits> patches() {
        if (patches == null) {
            ForgeEnvironment.initialize();
            Map<String, RicePatches.ClassEdits> result = new HashMap<>();
            for (RicePatches.ClassEdits patch : RicePatches.create(true)) {
                if (result.put(patch.getRuntimeTarget().replace('/', '.'), patch) != null)
                    throw new IllegalStateException("Duplicate Rice patch: " + patch.target);
            }
            patches = result;
        }
        return patches;
    }

    public byte[] transform(String name, String transformedName, byte[] input) {
        if (input == null || transformedName == null || !transformedName.startsWith("net.minecraft.")) return input;
        RicePatches.ClassEdits patch = patches().get(transformedName);
        if (patch == null) return input;
        try {
            ClassReader reader = new ClassReader(input);
            Hierarchy.register(reader);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);
            // Mark only complete transformations; existing hooks from another source
            // are not evidence that all of this class's edits have been installed.
            final String marker = "Ldev/rice/forge/RiceTransformed;";
            if (node.invisibleAnnotations != null)
                for (dev.rice.shaded.asm.tree.AnnotationNode annotation : node.invisibleAnnotations)
                    if (marker.equals(annotation.desc)) return input;
            int changes = 0;
            for (RicePatches.MethodEdit edit : patch.edits) {
                MethodNode method = patch.find(node, edit);
                if (method == null) {
                    throw new IllegalStateException("Required hook method missing: " + patch.target + ": " + edit.describe());
                }
                int count = edit.apply(method);
                if (edit.expected >= 0 && count != edit.expected)
                    throw new IllegalStateException("Hook count " + count + " (expected " + edit.expected + "): " + patch.target + ": " + edit.describe());
                changes += count;
            }
            if (patch.hasEveryMethodEdits()) {
                int count = patch.applyEveryMethod(node);
                if (patch.getEveryMethodExpected() >= 0 && count != patch.getEveryMethodExpected())
                    throw new IllegalStateException("Hook count mismatch: " + patch.target + ": " + patch.getEveryMethodLabel());
                changes += count;
            }
            patch.validate(node);
            if (changes == 0) throw new IllegalStateException("No hooks matched " + transformedName);
            if (node.invisibleAnnotations == null) node.invisibleAnnotations = new java.util.ArrayList<>();
            node.invisibleAnnotations.add(new dev.rice.shaded.asm.tree.AnnotationNode(marker));
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
                @Override protected String getCommonSuperClass(String a, String b) { return Hierarchy.common(a, b); }
            };
            node.accept(writer);
            transformed++;
            Platform.log("Forge patched " + transformedName + " (" + changes + " hook points)");
            return writer.toByteArray();
        } catch (Throwable failure) {
            throw new IllegalStateException("Rice Modified could not transform " + transformedName, failure);
        }
    }

    public static int transformedCount() { return transformed; }
}
