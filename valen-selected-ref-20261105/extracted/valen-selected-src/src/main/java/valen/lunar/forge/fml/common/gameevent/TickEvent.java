/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.lunar.forge.fml.common.gameevent;

import net.minecraft.entity.player.EntityPlayer;
import valen.lunar.forge.fml.common.eventhandler.Event;
import valen.lunar.forge.fml.relauncher.Side;

public class TickEvent
extends Event {
    public final Type type;
    public final Side side;
    public final Phase phase;

    public TickEvent(Type type, Side side, Phase phase) {
        this.type = type;
        this.side = side;
        this.phase = phase;
    }

    public static class RenderTickEvent
    extends TickEvent {
        public final float renderTickTime;

        public RenderTickEvent(Phase phase, float f) {
            super(Type.RENDER, Side.CLIENT, phase);
            this.renderTickTime = f;
        }
    }

    public static class PlayerTickEvent
    extends TickEvent {
        public final EntityPlayer player;

        public PlayerTickEvent(Phase phase, EntityPlayer entityPlayer) {
            super(Type.PLAYER, Side.CLIENT, phase);
            this.player = entityPlayer;
        }
    }

    public static class ClientTickEvent
    extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(Type.CLIENT, Side.CLIENT, phase);
        }
    }

    public static enum Phase {
        START,
        END;

    }

    public static enum Type {
        WORLD,
        PLAYER,
        CLIENT,
        SERVER,
        RENDER;

    }
}

