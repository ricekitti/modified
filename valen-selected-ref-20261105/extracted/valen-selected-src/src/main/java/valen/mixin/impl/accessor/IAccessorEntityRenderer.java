/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.EntityRenderer
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.ResourceLocation
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package valen.mixin.impl.accessor;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import valen.lunar.forge.fml.relauncher.Side;
import valen.lunar.forge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityRenderer.class}, remap=false)
public interface IAccessorEntityRenderer {
    @Invoker(value="setupCameraTransform", remap=false)
    public void valen$lunar$callSetupCameraTransform(float var1, int var2);

    @Invoker(value="loadShader", remap=false)
    public void valen$lunar$callLoadShader(ResourceLocation var1);

    @Accessor(value="shaderResourceLocations", remap=false)
    public ResourceLocation[] valen$lunar$getShaderResourceLocations();

    @Accessor(value="useShader", remap=false)
    public boolean valen$lunar$getUseShader();

    @Accessor(value="useShader", remap=false)
    public void valen$lunar$setUseShader(boolean var1);

    @Accessor(value="shaderIndex", remap=false)
    public int valen$lunar$getShaderIndex();

    @Accessor(value="shaderIndex", remap=false)
    public void valen$lunar$setShaderIndex(int var1);

    @Accessor(value="thirdPersonDistance", remap=false)
    public void valen$lunar$setThirdPersonDistance(float var1);

    @Accessor(value="pointedEntity", remap=false)
    public Entity valen$lunar$getPointedEntity();

    @Accessor(value="pointedEntity", remap=false)
    public void valen$lunar$setPointedEntity(Entity var1);
}

