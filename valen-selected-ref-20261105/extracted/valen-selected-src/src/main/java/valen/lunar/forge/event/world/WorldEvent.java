/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.World
 */
package valen.lunar.forge.event.world;

import net.minecraft.world.World;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class WorldEvent
extends Event {
    public final World world;

    public WorldEvent(World world) {
        this.world = world;
    }

    public static class Unload
    extends WorldEvent {
        public Unload(World world) {
            super(world);
        }
    }

    public static class Load
    extends WorldEvent {
        public Load(World world) {
            super(world);
        }
    }
}

