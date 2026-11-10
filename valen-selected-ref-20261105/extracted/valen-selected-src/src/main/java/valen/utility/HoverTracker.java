/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.util.Objects;

public final class HoverTracker<T> {
    private T target;
    private int startedAt = -1;

    public void update(T t, int n) {
        if (!Objects.equals(this.target, t)) {
            this.target = t;
            this.startedAt = t == null ? -1 : n;
        }
    }

    public int startedAt() {
        return this.startedAt;
    }

    public void reset() {
        this.target = null;
        this.startedAt = -1;
    }
}

