/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.RenderManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderManager.class}, remap=false)
public interface IAccessorRenderManager {
    @Accessor(value="renderPosX", remap=false)
    public double valen$lunar$getRenderPosX();

    @Accessor(value="renderPosY", remap=false)
    public double valen$lunar$getRenderPosY();

    @Accessor(value="renderPosZ", remap=false)
    public double valen$lunar$getRenderPosZ();
}

