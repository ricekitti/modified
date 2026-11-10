/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.MouseHelper
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.util.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={MouseHelper.class}, remap=false)
public interface IAccessorMouseHelper {
    @Accessor(value="deltaX", remap=false)
    public int valen$lunar$getDeltaX();

    @Accessor(value="deltaY", remap=false)
    public int valen$lunar$getDeltaY();
}

