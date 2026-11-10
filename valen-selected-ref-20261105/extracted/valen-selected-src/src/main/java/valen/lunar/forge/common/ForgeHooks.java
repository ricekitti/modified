/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 */
package valen.lunar.forge.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import valen.lunar.forge.common.MinecraftForge;
import valen.lunar.forge.event.entity.living.LivingEvent;
import valen.lunar.forge.event.entity.player.AttackEntityEvent;

public final class ForgeHooks {
    public static boolean onPlayerAttackTarget(EntityPlayer entityPlayer, Entity entity) {
        return !MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(entityPlayer, entity));
    }

    public static void onLivingJump(EntityLivingBase entityLivingBase) {
        MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(entityLivingBase));
    }
}

