/*
 * Decompiled with CFR 0.152.
 */
package valen.lunar;

import valen.Valen;

public final class LunarRuntime {
    private static boolean started;

    public static synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        new Valen().init(null);
        System.setProperty("valen.lunar.initialized", "true");
    }
}

