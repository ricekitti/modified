/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.BlockPos
 */
package com.snowify.module.combat.ignorefatigue;

import net.minecraft.util.BlockPos;

public interface MiningAccess {
    public boolean snowify$isHittingBlock();

    public BlockPos snowify$getCurrentBlock();

    public float snowify$getDamage();

    public void snowify$setDamage(float var1);

    public int snowify$getHitDelay();

    public void snowify$setHitDelay(int var1);
}
