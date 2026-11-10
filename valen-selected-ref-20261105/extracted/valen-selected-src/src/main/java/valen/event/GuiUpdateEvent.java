/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiScreen
 */
package valen.event;

import net.minecraft.client.gui.GuiScreen;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class GuiUpdateEvent
extends Event {
    public GuiScreen guiScreen;
    public boolean opened;

    public GuiUpdateEvent(GuiScreen guiScreen, boolean bl) {
        this.guiScreen = guiScreen;
        this.opened = bl;
    }
}

