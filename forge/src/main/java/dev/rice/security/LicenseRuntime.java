package dev.rice.security;

/** A short-lived local lease, renewed only after a verified server response. */
public final class LicenseRuntime {
    private static volatile KeyAuthClient.Session session;
    private static volatile long checked;
    private LicenseRuntime() {}
    static synchronized void activate(KeyAuthClient.Session value) {
        checked = System.nanoTime(); session = value;
    }
    static synchronized void refresh(KeyAuthClient.Session value) {
        if (session == value) checked = System.nanoTime();
    }
    public static synchronized void revoke() {
        session = null; checked = 0;
        dev.rice.client.hook.HookLifecycle.stop();
    }
    static KeyAuthClient.Session session() { return session; }
    public static boolean valid() {
        KeyAuthClient.Session current = session;
        long age = System.nanoTime() - checked;
        return current != null && age >= 0 && age < 180000000000L && current.expiry > System.currentTimeMillis() / 1000;
    }
    public static void requireValid() {
        if (!valid()) throw new SecurityException("Unlock Rice with a valid license first.");
    }
}
