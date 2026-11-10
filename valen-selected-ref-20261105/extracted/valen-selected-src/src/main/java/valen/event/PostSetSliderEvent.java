/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Event;

public class PostSetSliderEvent
extends Event {
    public double previousVal;
    public double newVal;

    public PostSetSliderEvent(double d, double d2) {
        this.previousVal = d;
        this.newVal = d2;
    }
}

