/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class PlayerMoveEvent
extends Event {
    public double x;
    public double y;
    public double z;

    public PlayerMoveEvent(double d, double d2, double d3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
    }
}

