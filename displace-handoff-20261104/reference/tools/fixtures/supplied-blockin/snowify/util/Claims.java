/*
 * Decompiled with CFR 0.152.
 */
package com.snowify.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Claims {
    private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<Entry>();

    private Claims() {
    }

    public static void register(Object owner, int priority, Holder holder) {
        ENTRIES.add(new Entry(owner, priority, holder));
    }

    public static boolean blocked(Object asker, int priority, Resource resource) {
        for (Entry entry : ENTRIES) {
            if (entry.owner == asker || entry.priority <= priority || !entry.holder.holds(resource)) continue;
            return true;
        }
        return false;
    }

    public static boolean blocked(Object asker, Resource resource) {
        return Claims.blocked(asker, Claims.priorityOf(asker), resource);
    }

    public static boolean held(Resource resource) {
        for (Entry entry : ENTRIES) {
            if (!entry.holder.holds(resource)) continue;
            return true;
        }
        return false;
    }

    private static int priorityOf(Object owner) {
        for (Entry entry : ENTRIES) {
            if (entry.owner != owner) continue;
            return entry.priority;
        }
        return Integer.MIN_VALUE;
    }

    private static final class Entry {
        final Object owner;
        final int priority;
        final Holder holder;

        Entry(Object owner, int priority, Holder holder) {
            this.owner = owner;
            this.priority = priority;
            this.holder = holder;
        }
    }

    public static interface Holder {
        public boolean holds(Resource var1);
    }

    public static enum Resource {
        LOOK,
        SLOT,
        ATTACK,
        CLICKS;

    }
}
