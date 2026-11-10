package dev.rice.forge;

import dev.rice.shaded.asm.ClassReader;
import dev.rice.shaded.asm.Opcodes;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Class metadata only: never defines or initializes a game class. */
public final class Hierarchy {
    private static final Map<String, Info> types = new HashMap<>();
    static {
        try (InputStream stream = Hierarchy.class.getResourceAsStream("/dev/rice/forge/hierarchy.tsv")) {
            if (stream == null) throw new IllegalStateException("Missing class hierarchy index");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] row = line.split("\t", -1);
                    types.put(row[0], new Info(Integer.parseInt(row[1]), row[2],
                        row.length > 3 && !row[3].isEmpty() ? row[3].split(",") : new String[0]));
                }
            }
        } catch (Exception failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }
    private Hierarchy() {}

    private static synchronized Info info(String name) {
        Info result = types.get(name);
        if (result != null) return result;
        try (InputStream input = Hierarchy.class.getResourceAsStream("/" + name + ".class")) {
            if (input != null) {
                ClassReader reader = new ClassReader(input);
                result = new Info(reader.getAccess(), reader.getSuperName(), reader.getInterfaces());
                types.put(name, result);
                return result;
            }
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot read class hierarchy: " + name, failure);
        }
        throw new IllegalStateException("Missing class hierarchy: " + name);
    }

    public static synchronized void register(ClassReader reader) {
        types.put(reader.getClassName(), new Info(reader.getAccess(), reader.getSuperName(), reader.getInterfaces()));
    }

    public static String[] parents(String name) {
        if (name.startsWith("[")) return new String[]{"java/lang/Object", "java/lang/Cloneable", "java/io/Serializable"};
        Info type = info(name);
        String[] result = new String[type.interfaces.length + (type.parent.isEmpty() ? 0 : 1)];
        int index = 0;
        if (!type.parent.isEmpty()) result[index++] = type.parent;
        for (String iface : type.interfaces) result[index++] = iface;
        return result;
    }

    public static boolean assignable(String target, String source) {
        if (target.equals(source) || target.equals("java/lang/Object")) return true;
        if (source.startsWith("[")) {
            if (target.equals("java/lang/Cloneable") || target.equals("java/io/Serializable")) return true;
            if (!target.startsWith("[")) return false;
            String a = component(target), b = component(source);
            return a != null && b != null && assignable(a, b);
        }
        if (target.startsWith("[")) return false;
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            String next = queue.removeFirst();
            if (!visited.add(next)) continue;
            if (target.equals(next)) return true;
            for (String parent : parents(next)) queue.addLast(parent);
        }
        return false;
    }

    private static String component(String array) {
        String element = array.substring(1);
        return element.startsWith("[") ? element : element.startsWith("L")
            ? element.substring(1, element.length() - 1) : null;
    }

    public static String common(String a, String b) {
        if (assignable(a, b)) return a;
        if (assignable(b, a)) return b;
        if (a.startsWith("[") && b.startsWith("[")) {
            String x = component(a), y = component(b);
            if (x != null && y != null) {
                String shared = common(x, y);
                return shared.startsWith("[") ? "[" + shared : "[L" + shared + ";";
            }
            return "java/lang/Object";
        }
        if (a.startsWith("[") || b.startsWith("[")) return "java/lang/Object";
        if ((info(a).access & Opcodes.ACC_INTERFACE) != 0 || (info(b).access & Opcodes.ACC_INTERFACE) != 0)
            return "java/lang/Object";
        String current = info(a).parent;
        while (!current.isEmpty()) {
            if (assignable(current, b)) return current;
            current = info(current).parent;
        }
        return "java/lang/Object";
    }

    private static final class Info {
        final int access;
        final String parent;
        final String[] interfaces;
        Info(int access, String parent, String[] interfaces) {
            this.access = access;
            this.parent = parent == null ? "" : parent;
            this.interfaces = interfaces;
        }
    }
}
