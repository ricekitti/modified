/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.lunar.forge.event.entity.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import valen.lunar.forge.event.entity.living.LivingEvent;

public class PlayerEvent
extends LivingEvent {
    public final EntityPlayer entityPlayer;

    public PlayerEvent(EntityPlayer entityPlayer) {
        super((EntityLivingBase)entityPlayer);
        this.entityPlayer = entityPlayer;
    }
}

