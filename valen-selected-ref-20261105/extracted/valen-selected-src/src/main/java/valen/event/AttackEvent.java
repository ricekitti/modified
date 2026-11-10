/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import valen.lunar.forge.fml.common.eventhandler.Cancelable;
import valen.lunar.forge.fml.common.eventhandler.Event;

@Cancelable
public class AttackEvent
extends Event {
    public Entity target;
    public EntityPlayer attacker;
    public boolean swing;

    public AttackEvent(Entity entity, EntityPlayer entityPlayer, boolean bl) {
        this.target = entity;
        this.attacker = entityPlayer;
        this.swing = bl;
    }
}

