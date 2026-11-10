/*
 * Decompiled with CFR 0.152.
 */
package valen.utility.config;

import java.io.File;

public final class ClientConfig {
    private final String name;
    private final File file;
    private final long lastModified;
    private final int enabledModuleCount;

    ClientConfig(String string, File file, long l, int n) {
        this.name = string;
        this.file = file;
        this.lastModified = l;
        this.enabledModuleCount = n;
    }

    public String getName() {
        return this.name;
    }

    public File getFile() {
        return this.file;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public int getEnabledModuleCount() {
        return this.enabledModuleCount;
    }

    public String getRelativeAge() {
        long l = System.currentTimeMillis() - this.lastModified;
        if (l < 60000L) {
            return "just now";
        }
        long l2 = l / 60000L;
        if (l2 < 60L) {
            return l2 + "m ago";
        }
        long l3 = l2 / 60L;
        if (l3 < 24L) {
            return l3 + "h ago";
        }
        long l4 = l3 / 24L;
        if (l4 < 365L) {
            return l4 + "d ago";
        }
        return l4 / 365L + "y ago";
    }
}

