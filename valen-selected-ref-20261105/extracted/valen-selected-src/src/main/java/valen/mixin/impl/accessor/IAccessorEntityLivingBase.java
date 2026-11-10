/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityLivingBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityLivingBase.class}, remap=false)
public interface IAccessorEntityLivingBase {
    @Accessor(value="jumpTicks", remap=false)
    public void valen$lunar$setJumpTicks(int var1);

    @Accessor(value="jumpTicks", remap=false)
    public int valen$lunar$getJumpTicks();
}

