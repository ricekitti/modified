/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemFood
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.item.ItemFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={ItemFood.class}, remap=false)
public interface IAccessorItemFood {
    @Accessor(value="alwaysEdible", remap=false)
    public boolean valen$lunar$getAlwaysEdible();
}

