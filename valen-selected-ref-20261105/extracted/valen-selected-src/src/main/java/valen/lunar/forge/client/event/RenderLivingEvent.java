/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.RendererLivingEntity
 *  net.minecraft.entity.EntityLivingBase
 */
package valen.lunar.forge.client.event;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

public class RenderLivingEvent<T extends EntityLivingBase>
extends Event {
    public final EntityLivingBase entity;
    public final RendererLivingEntity<T> renderer;
    public final double x;
    public final double y;
    public final double z;

    public RenderLivingEvent(EntityLivingBase entityLivingBase, RendererLivingEntity<T> rendererLivingEntity, double d, double d2, double d3) {
        this.entity = entityLivingBase;
        this.renderer = rendererLivingEntity;
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    public static class Specials<T extends EntityLivingBase>
    extends RenderLivingEvent<T> {
        public Specials(EntityLivingBase entityLivingBase, RendererLivingEntity<T> rendererLivingEntity, double d, double d2, double d3) {
            super(entityLivingBase, rendererLivingEntity, d, d2, d3);
        }

        @Cancelable
        public static class Pre<T extends EntityLivingBase>
        extends Specials<T> {
            public Pre(EntityLivingBase entityLivingBase, RendererLivingEntity<T> rendererLivingEntity, double d, double d2, double d3) {
                super(entityLivingBase, rendererLivingEntity, d, d2, d3);
            }
        }
    }
}

