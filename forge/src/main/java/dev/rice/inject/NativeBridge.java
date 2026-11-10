package dev.rice.inject;

/** Forge has no native companion. Runtime retransformation is deliberately unavailable. */
public final class NativeBridge {
    private NativeBridge() {}
    public static byte[] getClassBytes(Class<?> type) {
        throw new UnsupportedOperationException("Forge installs Rice hooks at class load");
    }
    public static int retransformClass(Class<?> type, byte[] bytes) {
        throw new UnsupportedOperationException("Forge installs Rice hooks at class load");
    }
    public static void log(String message) { System.out.println("[Rice Modified] " + message); }
    public static void inputSoundEvent(int kind, int code, boolean down) {
        // The Windows companion's input sounds are not part of the Forge mod.
    }
}
