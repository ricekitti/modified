/*
 * Decompiled with CFR 0.152.
 */
package valen.event;

import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class KeyPressEvent
extends Event {
    public char typedChar;
    public int keyCode;

    public KeyPressEvent(char c, int n) {
        this.typedChar = c;
        this.keyCode = n;
    }
}

