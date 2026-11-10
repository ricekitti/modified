/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 */
package valen.lunar.forge.client.event;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class GuiScreenEvent
extends Event {
    public final GuiScreen gui;

    public GuiScreenEvent(GuiScreen guiScreen) {
        this.gui = guiScreen;
    }

    public static class InitGuiEvent
    extends GuiScreenEvent {
        public List<GuiButton> buttonList;

        public InitGuiEvent(GuiScreen guiScreen, List<GuiButton> list) {
            super(guiScreen);
            this.buttonList = list;
        }

        public static class Post
        extends InitGuiEvent {
            public Post(GuiScreen guiScreen, List<GuiButton> list) {
                super(guiScreen, list);
            }
        }

        @Cancelable
        public static class Pre
        extends InitGuiEvent {
            public Pre(GuiScreen guiScreen, List<GuiButton> list) {
                super(guiScreen, list);
            }
        }
    }
}

