/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.world.World
 */
package valen.lunar.forge.event.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import valen.lunar.forge.event.entity.EntityEvent;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;

@Cancelable
public class EntityJoinWorldEvent
extends EntityEvent {
    public final World world;

    public EntityJoinWorldEvent(Entity entity, World world) {
        super(entity);
        this.world = world;
    }
}

