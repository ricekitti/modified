/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Entity.class}, remap=false)
public interface IAccessorEntity {
    @Accessor(value="fire", remap=false)
    public int valen$lunar$getFire();

    @Accessor(value="nextStepDistance", remap=false)
    public int valen$lunar$getNextStepDistance();

    @Accessor(value="isInWeb", remap=false)
    public boolean valen$lunar$getIsInWeb();
}

