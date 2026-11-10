/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 */
package valen.lunar.forge.event.entity.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import valen.lunar.forge.event.entity.EntityEvent;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;

public class LivingEvent
extends EntityEvent {
    public final EntityLivingBase entityLiving;

    public LivingEvent(EntityLivingBase entityLivingBase) {
        super((Entity)entityLivingBase);
        this.entityLiving = entityLivingBase;
    }

    public static class LivingJumpEvent
    extends LivingEvent {
        public LivingJumpEvent(EntityLivingBase entityLivingBase) {
            super(entityLivingBase);
        }
    }

    @Cancelable
    public static class LivingUpdateEvent
    extends LivingEvent {
        public LivingUpdateEvent(EntityLivingBase entityLivingBase) {
            super(entityLivingBase);
        }
    }
}

