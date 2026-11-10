/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.RenderPlayer
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.lunar.forge.client.event;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import valen.lunar.forge.event.entity.player.PlayerEvent;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;

public class RenderPlayerEvent
extends PlayerEvent {
    public final RenderPlayer renderer;
    public final float partialRenderTick;
    public final double x;
    public final double y;
    public final double z;

    public RenderPlayerEvent(EntityPlayer entityPlayer, RenderPlayer renderPlayer, float f, double d, double d2, double d3) {
        super(entityPlayer);
        this.renderer = renderPlayer;
        this.partialRenderTick = f;
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    public static class Post
    extends RenderPlayerEvent {
        public Post(EntityPlayer entityPlayer, RenderPlayer renderPlayer, float f, double d, double d2, double d3) {
            super(entityPlayer, renderPlayer, f, d, d2, d3);
        }
    }

    @Cancelable
    public static class Pre
    extends RenderPlayerEvent {
        public Pre(EntityPlayer entityPlayer, RenderPlayer renderPlayer, float f, double d, double d2, double d3) {
            super(entityPlayer, renderPlayer, f, d, d2, d3);
        }
    }
}

