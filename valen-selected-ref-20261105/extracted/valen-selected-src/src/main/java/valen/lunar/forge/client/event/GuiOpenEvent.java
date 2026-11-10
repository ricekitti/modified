/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 */
package valen.lunar.forge.client.event;

import net.minecraft.client.gui.GuiScreen;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class GuiOpenEvent
extends Event {
    public GuiScreen gui;

    public GuiOpenEvent(GuiScreen guiScreen) {
        this.gui = guiScreen;
    }
}

