/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 */
package valen.lunar.forge.client.event;

import org.lwjgl.input.Mouse;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class MouseEvent
extends Event {
    public final int x = Mouse.getEventX();
    public final int y = Mouse.getEventY();
    public final int dx = Mouse.getEventDX();
    public final int dy = Mouse.getEventDY();
    public final int dwheel = Mouse.getEventDWheel();
    public int button = Mouse.getEventButton();
    public boolean buttonstate = Mouse.getEventButtonState();
    public final long nanoseconds = Mouse.getEventNanoseconds();
}

