/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package valen.event;

import net.minecraft.entity.Entity;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class StepHeightEvent
extends Event {
    public Entity entity;
    public float stepHeight;

    public StepHeightEvent(Entity entity, float f) {
        this.entity = entity;
        this.stepHeight = f;
    }
}

