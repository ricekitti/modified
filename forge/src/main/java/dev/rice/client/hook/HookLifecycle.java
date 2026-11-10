package dev.rice.client.hook;

import dev.rice.security.LicenseRuntime;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Hooks are passive until the complete, licensed client has initialized. */
public final class HookLifecycle {
    private static volatile boolean ready;
    private static final ThreadLocal<Map<String,IdentityHashMap<Object,Integer>>> scopes =
        ThreadLocal.withInitial(HashMap::new);
    private HookLifecycle() {}
    public static void start() {
        LicenseRuntime.requireValid();
        if (myau.MyauClient.moduleManager == null || myau.MyauClient.eventBus == null)
            throw new IllegalStateException("Hook infrastructure is not initialized");
        ready = true;
    }
    public static void stop() { ready = false; }
    public static boolean active() {
        return ready && LicenseRuntime.valid() && myau.MyauClient.moduleManager != null && myau.MyauClient.eventBus != null;
    }
    public static boolean hostInitialized() {
        return myau.MyauClient.moduleManager != null || myau.MyauClient.eventBus != null;
    }
    public static boolean enter(String scope, Object key) {
        if (!active()) return false;
        IdentityHashMap<Object,Integer> records = scopes.get().get(scope);
        if (records == null) { records = new IdentityHashMap<>(); scopes.get().put(scope, records); }
        Integer depth = records.get(key); records.put(key, depth == null ? 1 : depth + 1);
        return true;
    }
    public static boolean exit(String scope, Object key) {
        Map<String,IdentityHashMap<Object,Integer>> current = scopes.get();
        IdentityHashMap<Object,Integer> records = current.get(scope);
        if (records == null) return false;
        Integer depth = records.get(key);
        if (depth == null) return false;
        if (depth == 1) records.remove(key); else records.put(key, depth - 1);
        if (records.isEmpty()) current.remove(scope);
        if (current.isEmpty()) scopes.remove();
        // Cleanup is allowed after revocation if its matching before-hook ran.
        return true;
    }
}
