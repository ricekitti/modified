/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar.bridge;

import java.util.function.Consumer;

public final class StartupBridge {
    private static volatile Consumer<ClassLoader> callback;

    public static void install(Consumer<ClassLoader> consumer) {
        if (callback != null) {
            throw new IllegalStateException("Valen Lunar agent is already installed");
        }
        callback = consumer;
    }

    public static void ready(ClassLoader classLoader) {
        Consumer<ClassLoader> consumer = callback;
        if (consumer != null) {
            consumer.accept(classLoader);
        }
    }
}

