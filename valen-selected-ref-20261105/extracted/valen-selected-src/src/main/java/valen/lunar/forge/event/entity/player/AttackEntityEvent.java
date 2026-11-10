/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.lunar.forge.event.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import valen.lunar.forge.event.entity.player.PlayerEvent;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;

@Cancelable
public class AttackEntityEvent
extends PlayerEvent {
    public final Entity target;

    public AttackEntityEvent(EntityPlayer entityPlayer, Entity entity) {
        super(entityPlayer);
        this.target = entity;
    }
}

