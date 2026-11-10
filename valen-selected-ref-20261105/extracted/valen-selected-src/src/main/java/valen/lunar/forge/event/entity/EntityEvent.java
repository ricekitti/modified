/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package valen.lunar.forge.event.entity;

import net.minecraft.entity.Entity;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class EntityEvent
extends Event {
    public final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }
}

