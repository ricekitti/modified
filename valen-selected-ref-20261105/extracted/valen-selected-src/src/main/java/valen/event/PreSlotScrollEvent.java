/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class PreSlotScrollEvent
extends Event {
    public int slot;
    public int previousSlot;

    public PreSlotScrollEvent(int n, int n2) {
        this.slot = n;
        this.previousSlot = n2;
    }
}

